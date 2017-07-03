package cyclops.control;

import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.hkt.Higher2;
import com.aol.cyclops2.types.foldable.To;
import com.aol.cyclops2.types.functor.BiTransformable;
import cyclops.collections.immutable.LinkedListX;
import cyclops.companion.Monoids;
import cyclops.function.*;
import cyclops.companion.Semigroups;
import com.aol.cyclops2.data.collections.extensions.CollectionX;
import cyclops.collections.mutable.ListX;
import com.aol.cyclops2.types.*;
import com.aol.cyclops2.types.anyM.AnyMValue;
import cyclops.monads.Witness;
import com.aol.cyclops2.types.reactive.ValueSubscriber;
import cyclops.companion.Streams;
import cyclops.monads.AnyM;
import cyclops.monads.Witness.xor;
import cyclops.monads.WitnessType;
import cyclops.monads.transformers.XorT;
import cyclops.stream.ReactiveSeq;
import cyclops.typeclasses.Active;
import cyclops.typeclasses.InstanceDefinitions;
import cyclops.typeclasses.Nested;
import cyclops.typeclasses.Pure;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.comonad.ComonadByPure;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Publisher;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * eXclusive Or (Xor)
 * 
 * 'Right' (or primary type) biased disjunct union. Often called Either, but in a generics heavy Java world Xor is half the length of Either.
 * 
 *  No 'projections' are provided, swap() and secondaryXXXX alternative methods can be used instead.
 *  
 *  Xor is used to represent values that can be one of two states (for example a validation result, lazy everything is ok - or we have an error).
 *  It can be used to avoid a common design anti-pattern where an Object has two fields one of which is always null (or worse, both are defined as Optionals).
 *
 *  <pre>
 *  {@code
 *
 *     public class Member{
 *           Xor<SeniorTeam,JuniorTeam> team;
 *     }
 *
 *     Rather than
 *
 *     public class Member{
 *           @Setter
 *           SeniorTeam seniorTeam = null;
 *           @Setter
 *           JuniorTeam juniorTeam = null;
 *     }
 *  }
 *  </pre>
 *
 *  Xor's have two states
 *  Primary : Most methods operate naturally on the primary type, if it is present. If it is not, nothing happens.
 *  Secondary : Most methods do nothing to the secondary type if it is present.
 *              To operate on the Secondary type takeOne call swap() or use secondary analogs of the main operators.
 *
 *  Instantiating an Xor - Primary
 *  <pre>
 *  {@code
 *      Xor.primary("hello").map(v->v+" world")
 *    //Xor.primary["hello world"]
 *  }
 *  </pre>
 *
 *  Instantiating an Xor - Secondary
 *  <pre>
 *  {@code
 *      Xor.secondary("hello").map(v->v+" world")
 *    //Xor.seconary["hello"]
 *  }
 *  </pre>
 *
 *  Xor can operate (via map/flatMap) as a Functor / Monad and via combine as an ApplicativeFunctor
 *
 *   Values can be accumulated via
 *  <pre>
 *  {@code
 *  Xor.accumulateSecondary(ListX.of(Xor.secondary("failed1"),
                                                    Xor.secondary("failed2"),
                                                    Xor.primary("success")),
                                                    Semigroups.stringConcat)
 *
 *  //failed1failed2
 *
 *   Xor<String,String> fail1 = Xor.secondary("failed1");
     fail1.swap().combine((a,b)->a+b)
                 .combine(Xor.secondary("failed2").swap())
                 .combine(Xor.<String,String>primary("success").swap())
 *
 *  //failed1failed2
 *  }
 *  </pre>
 *
 *
 * For Inclusive Ors @see Ior
 *
 * @author johnmcclean
 *
 * @param <ST> Secondary type
 * @param <PT> Primary type
 */
public interface Xor<ST, PT> extends To<Xor<ST,PT>>,
                                     MonadicValue<PT>,
                                     BiTransformable<ST,PT>,
                                     Higher2<xor,ST,PT> {


    default Active<Higher<xor,ST>,PT> allTypeclasses(){
        return Active.of(this, Instances.definitions());
    }
    default <W2,R> Nested<Higher<xor,ST>,W2,R> mapM(Function<? super PT,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
        return Nested.of(map(fn), Instances.definitions(), defs);
    }
    default <W extends WitnessType<W>> XorT<W, ST,PT> liftM(W witness) {
        return XorT.of(witness.adapter().unit(this));
    }

    default Eval<Xor<ST, PT>> nestedEval(){
        return Eval.later(()->this);
    }
    /**
     * Static method useful as a method reference for fluent consumption of any value type stored in this Either
     * (will capture the lowest common type)
     *
     * <pre>
     * {@code
     *
     *   myEither.to(Xor::consumeAny)
                 .accept(System.out::println);
     * }
     * </pre>
     *
     * @param either Xor to consume value for
     * @return Consumer we can applyHKT to consume value
     */
    static <X, LT extends X, M extends X, RT extends X>  Consumer<Consumer<? super X>> consumeAny(Xor<LT,RT> either){
        return in->visitAny(in,either);
    }

    static <X, LT extends X, M extends X, RT extends X,R>  Function<Function<? super X, R>,R> applyAny(Xor<LT,RT> either){
        return in->visitAny(either,in);
    }

    static <X, PT extends X, ST extends X,R> R visitAny(Xor<ST,PT> either, Function<? super X, ? extends R> fn){
        return either.visit(fn, fn);
    }

    static <X, LT extends X, RT extends X> X visitAny(Consumer<? super X> c,Xor<LT,RT> either){
        Function<? super X, X> fn = x ->{
            c.accept(x);
            return x;
        };
        return visitAny(either,fn);
    }

    public static <ST,T> Xor<ST,T> narrowK2(final Higher2<xor, ST,T> xor) {
        return (Xor<ST,T>)xor;
    }
    public static <ST,T> Xor<ST,T> narrowK(final Higher<Higher<xor, ST>,T> xor) {
        return (Xor<ST,T>)xor;
    }
    /**
     * Construct a Primary Xor from the supplied publisher
     * <pre>
     * {@code
     *   ReactiveSeq<Integer> reactiveStream =  ReactiveSeq.of(1,2,3);

         Xor<Throwable,Integer> future = Xor.fromPublisher(reactiveStream);

         //Xor[1]
     *
     * }
     * </pre>
     * @param pub Publisher to construct an Xor from
     * @return Xor constructed from the supplied Publisher
     */
    public static <T> Xor<Throwable, T> fromPublisher(final Publisher<T> pub) {
        final ValueSubscriber<T> sub = ValueSubscriber.subscriber();
        pub.subscribe(sub);
        return sub.toXor();
    }

    /**
     * Construct a Primary Xor from the supplied Iterable
     * <pre>
     * {@code
     *   List<Integer> list =  Arrays.asList(1,2,3);

         Xor<Throwable,Integer> future = Xor.fromPublisher(reactiveStream);

         //Xor[1]
     *
     * }
     * </pre>
     * @param iterable Iterable to construct an Xor from
     * @return Xor constructed from the supplied Iterable
     */
    public static <ST, T> Xor<ST, T> fromIterable(final Iterable<T> iterable) {

        final Iterator<T> it = iterable.iterator();
        return Xor.primary(it.hasNext() ? it.next() : null);
    }

    /**
     * Create an instance of the secondary type. Most methods are biased to the primary type,
     * so you will need to use swap() or secondaryXXXX to manipulate the wrapped value
     *
     * <pre>
     * {@code
     *   Xor.<Integer,Integer>secondary(10).map(i->i+1);
     *   //Xor.secondary[10]
     *
     *    Xor.<Integer,Integer>secondary(10).swap().map(i->i+1);
     *    //Xor.primary[11]
     * }
     * </pre>
     *
     *
     * @param value to wrap
     * @return Secondary instance of Xor
     */
    public static <ST, PT> Xor<ST, PT> secondary(final ST value) {
        return new Secondary<>(
                               value);
    }

    /**
     * Create an instance of the primary type. Most methods are biased to the primary type,
     * which means, for example, that the map method operates on the primary type but does nothing on secondary Xors
     *
     * <pre>
     * {@code
     *   Xor.<Integer,Integer>primary(10).map(i->i+1);
     *   //Xor.primary[11]
     *
     *
     * }
     * </pre>
     *
     *
     * @param value To construct an Xor from
     * @return Primary type instanceof Xor
     */
    public static <ST, PT> Xor<ST, PT> primary(final PT value) {
        return new Primary<>(
                             value);
    }
    @Override
    default int arity() {
        return 2;
    }

    @Override
    default <R> Xor<ST,R> zipWith(Iterable<Function<? super PT, ? extends R>> fn) {
        return (Xor<ST,R>)MonadicValue.super.zipWith(fn);
    }

    @Override
    default <R> Xor<ST,R> zipWithS(Stream<Function<? super PT, ? extends R>> fn) {
        return (Xor<ST,R>)MonadicValue.super.zipWithS(fn);
    }

    @Override
    default <R> Xor<ST,R> zipWithP(Publisher<Function<? super PT, ? extends R>> fn) {
        return (Xor<ST,R>)MonadicValue.super.zipWithP(fn);
    }

    @Override
    default <R> Xor<ST,R> retry(final Function<? super PT, ? extends R> fn) {
        return (Xor<ST,R>)MonadicValue.super.retry(fn);
    }

    @Override
    default <U> Xor<ST,Tuple2<PT, U>> zipP(final Publisher<? extends U> other) {
        return (Xor)MonadicValue.super.zipP(other);
    }

    @Override
    default <R> Xor<ST,R> retry(final Function<? super PT, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (Xor<ST,R>)MonadicValue.super.retry(fn,retries,delay,timeUnit);
    }

    @Override
    default <S, U> Xor<ST,Tuple3<PT, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {
        return (Xor)MonadicValue.super.zip3(second,third);
    }

    @Override
    default <S, U, R> Xor<ST,R> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third, final Fn3<? super PT, ? super S, ? super U, ? extends R> fn3) {
        return (Xor<ST,R>)MonadicValue.super.zip3(second,third,fn3);
    }

    @Override
    default <T2, T3, T4> Xor<ST,Tuple4<PT, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth) {
        return (Xor)MonadicValue.super.zip4(second,third,fourth);
    }

    @Override
    default <T2, T3, T4, R> Xor<ST,R> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Fn4<? super PT, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (Xor<ST,R>)MonadicValue.super.zip4(second,third,fourth,fn);
    }

    @Override
    default <R> Xor<ST,R> flatMapS(final Function<? super PT, ? extends Stream<? extends R>> mapper) {
        return (Xor<ST,R>)MonadicValue.super.flatMapS(mapper);
    }


    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    default <T2, R1, R2, R3, R> Xor<ST,R> forEach4(Function<? super PT, ? extends MonadicValue<R1>> value1,
            BiFunction<? super PT, ? super R1, ? extends MonadicValue<R2>> value2,
            Fn3<? super PT, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
            Fn4<? super PT, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return (Xor<ST,R>)MonadicValue.super.forEach4(value1, value2, value3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    default <T2, R1, R2, R3, R> Xor<ST,R> forEach4(Function<? super PT, ? extends MonadicValue<R1>> value1,
            BiFunction<? super PT, ? super R1, ? extends MonadicValue<R2>> value2,
            Fn3<? super PT, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
            Fn4<? super PT, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
            Fn4<? super PT, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return (Xor<ST,R>)MonadicValue.super.forEach4(value1, value2, value3, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    default <T2, R1, R2, R> Xor<ST,R> forEach3(Function<? super PT, ? extends MonadicValue<R1>> value1,
            BiFunction<? super PT, ? super R1, ? extends MonadicValue<R2>> value2,
            Fn3<? super PT, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (Xor<ST,R>)MonadicValue.super.forEach3(value1, value2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    default <T2, R1, R2, R> Xor<ST,R> forEach3(Function<? super PT, ? extends MonadicValue<R1>> value1,
            BiFunction<? super PT, ? super R1, ? extends MonadicValue<R2>> value2,
            Fn3<? super PT, ? super R1, ? super R2, Boolean> filterFunction,
            Fn3<? super PT, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (Xor<ST,R>)MonadicValue.super.forEach3(value1, value2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> Xor<ST,R> forEach2(Function<? super PT, ? extends MonadicValue<R1>> value1,
            BiFunction<? super PT, ? super R1, ? extends R> yieldingFunction) {

        return (Xor<ST,R>)MonadicValue.super.forEach2(value1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> Xor<ST,R> forEach2(Function<? super PT, ? extends MonadicValue<R1>> value1,
            BiFunction<? super PT, ? super R1, Boolean> filterFunction,
            BiFunction<? super PT, ? super R1, ? extends R> yieldingFunction) {
        return (Xor<ST,R>)MonadicValue.super.forEach2(value1, filterFunction, yieldingFunction);
    }


    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#fromEither5()
     */
    default AnyMValue<xor,PT> anyM() {
        return AnyM.fromXor(this);
    }




    @Override
    default Xor<ST,PT> zip(BinaryOperator<Zippable<PT>> combiner, Zippable<PT> app) {

        return (Xor<ST,PT>)MonadicValue.super.zip(combiner, app);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#flatMapI(java.util.function.Function)
     */
    @Override
    default <R> Xor<ST, R> flatMapI(Function<? super PT, ? extends Iterable<? extends R>> mapper) {
        return (Xor<ST, R>)MonadicValue.super.flatMapI(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#flatMapP(java.util.function.Function)
     */
    @Override
    default <R> Xor<ST, R> flatMapP(Function<? super PT, ? extends Publisher<? extends R>> mapper) {
        return (Xor<ST, R>)MonadicValue.super.flatMapP(mapper);
    }

    /* (non-Javadoc)
    * @see com.aol.cyclops2.types.MonadicValue#coflatMap(java.util.function.Function)
    */
    @Override
    default <R> Xor<ST, R> coflatMap(final Function<? super MonadicValue<PT>, R> mapper) {
        return (Xor<ST, R>) MonadicValue.super.coflatMap(mapper);
    }

    //cojoin
    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#nest()
     */
    @Override
    default Xor<ST, MonadicValue<PT>> nest() {
        return this.map(t -> unit(t));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#combine(cyclops2.function.Monoid, com.aol.cyclops2.types.MonadicValue)
     */
    @Override
    default Xor<ST, PT> combineEager(final Monoid<PT> monoid, final MonadicValue<? extends PT> v2) {
        return (Xor<ST, PT>) MonadicValue.super.combineEager(monoid, v2);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#unit(java.lang.Object)
     */
    @Override
    default <T> Xor<ST, T> unit(final T unit) {
        return Xor.primary(unit);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.foldable.Convertable#optional()
     */
    @Override
    default Optional<PT> toOptional() {
        return isPrimary() ? Optional.of(get()) : Optional.empty();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Filters#filter(java.util.function.Predicate)
     */
    @Override
    Xor<ST, PT> filter(Predicate<? super PT> test);

    /**
     * If this Xor contains the Secondary type, map it's value so that it contains the Primary type
     *
     *
     * @param fn Function to map secondary type to primary
     * @return Xor with secondary type mapped to primary
     */
    Xor<ST, PT> secondaryToPrimayMap(Function<? super ST, ? extends PT> fn);

    /**
     * Always map the Secondary type of this Xor if it is present using the provided transformation function
     *
     * @param fn Transformation function for Secondary types
     * @return Xor with Secondary type transformed
     */
    <R> Xor<R, PT> secondaryMap(Function<? super ST, ? extends R> fn);

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#map(java.util.function.Function)
     */
    @Override
    <R> Xor<ST, R> map(Function<? super PT, ? extends R> fn);

    /**
     * Peek at the Secondary type value if present
     *
     * @param action Consumer to peek at the Secondary type value
     * @return Xor with the same values as before
     */
    Xor<ST, PT> secondaryPeek(Consumer<? super ST> action);

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Functor#peek(java.util.function.Consumer)
     */
    @Override
    Xor<ST, PT> peek(Consumer<? super PT> action);

    /**
     * Swap types so operations directly affect the current (pre-swap) Secondary type
     *<pre>
     *  {@code
     *
     *    Xor.secondary("hello")
     *       .map(v->v+" world")
     *    //Xor.seconary["hello"]
     *
     *    Xor.secondary("hello")
     *       .swap()
     *       .map(v->v+" world")
     *       .swap()
     *    //Xor.seconary["hello world"]
     *  }
     *  </pre>
     *
     *
     * @return Swap the primary and secondary types, allowing operations directly on what was the Secondary type
     */
    Xor<PT, ST> swap();

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Value#toIor()
     */
    @Override
    Ior<ST, PT> toIor();

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.foldable.Convertable#isPresent()
     */
    @Override
    default boolean isPresent() {
        return isPrimary();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Value#toXor()
     */
    @Override
    default Xor<ST, PT> toXor() {
        return this;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Value#toXor(java.lang.Object)
     */
    @Override
    default <ST2> Xor<ST2, PT> toXor(final ST2 secondary) {
        return visit(s -> secondary(secondary), p -> primary(p));
    }
    /**
     *  Turn a toX of Xors into a singleUnsafe Xor with Lists of values.
     *  Primary and secondary types are swapped during this operation.
     *
     * <pre>
     * {@code
     *  Xor<String,Integer> just  = Xor.primary(10);
        Xor<String,Integer> none = Xor.secondary("none");
     *  Xor<ListX<Integer>,ListX<String>> xors =Xor.sequenceSecondary(ListX.of(just,none,Xor.primary(1)));
        //Xor.primary(ListX.of("none")))
     *
     * }
     * </pre>
     *
     *
     * @param xors Xors to sequence
     * @return Xor sequenced and swapped
     */
    public static <ST, PT> Xor<ListX<PT>, ListX<ST>> sequenceSecondary(final CollectionX<Xor<ST, PT>> xors) {
        return AnyM.sequence(xors.stream().filter(Xor::isSecondary).map(i->AnyM.fromXor(i.swap())).toListX(), xor.INSTANCE)
                    .to(Witness::xor);
    }
    /**
     * Accumulate the result of the Secondary types in the Collection of Xors provided using the supplied Reducer  {@see cyclops2.Reducers}.
     *
     * <pre>
     * {@code
     *  Xor<String,Integer> just  = Xor.primary(10);
        Xor<String,Integer> none = Xor.secondary("none");

     *  Xor<?,PersistentSetX<String>> xors = Xor.accumulateSecondary(ListX.of(just,none,Xor.primary(1)),Reducers.<String>toPersistentSetX());
      //Xor.primary(PersistentSetX.of("none"))));
      * }
     * </pre>
     * @param xors Collection of Iors to accumulate secondary values
     * @param reducer Reducer to accumulate results
     * @return Xor populated with the accumulate secondary operation
     */
    public static <ST, PT, R> Xor<?, R> accumulateSecondary(final CollectionX<Xor<ST, PT>> xors, final Reducer<R> reducer) {
        return sequenceSecondary(xors).map(s -> s.mapReduce(reducer));
    }
    /**
     * Accumulate the results only from those Xors which have a Secondary type present, using the supplied mapping function to
     * convert the data from each Xor before reducing them using the supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see cyclops2.Monoids }..
     *
     * <pre>
     * {@code
     *  Xor<String,Integer> just  = Xor.primary(10);
        Xor<String,Integer> none = Xor.secondary("none");

     *  Xor<?,String> xors = Xor.accumulateSecondary(ListX.of(just,none,Xor.secondary("1")),i->""+i,Monoids.stringConcat);

        //Xor.primary("none1")
     *
     * }
     * </pre>
     *
     *
     *
     * @param xors Collection of Iors to accumulate secondary values
     * @param mapper Mapping function to be applied to the result of each Ior
     * @param reducer Semigroup to combine values from each Ior
     * @return Xor populated with the accumulate Secondary operation
     */
    public static <ST, PT, R> Xor<?, R> accumulateSecondary(final CollectionX<Xor<ST, PT>> xors, final Function<? super ST, R> mapper,
            final Monoid<R> reducer) {
        return sequenceSecondary(xors).map(s -> s.map(mapper)
                                                 .reduce(reducer));
    }


    /**
     *  Turn a toX of Xors into a singleUnsafe Ior with Lists of values.
     *
     * <pre>
     * {@code
     *
     * Xor<String,Integer> just  = Xor.primary(10);
       Xor<String,Integer> none = Xor.secondary("none");


     * Xor<ListX<String>,ListX<Integer>> xors =Xor.sequencePrimary(ListX.of(just,none,Xor.primary(1)));
       //Xor.primary(ListX.of(10,1)));
     *
     * }</pre>
     *
     *
     *
     * @param iors Xors to sequence
     * @return Xor Sequenced
     */
    public static <ST, PT> Xor<ListX<ST>, ListX<PT>> sequencePrimary(final CollectionX<Xor<ST, PT>> xors) {
        return AnyM.sequence(xors.stream().filter(Xor::isPrimary).map(AnyM::fromXor).toListX(), xor.INSTANCE)
                    .to(Witness::xor);
    }
    /**
     * Accumulate the result of the Primary types in the Collection of Xors provided using the supplied Reducer  {@see cyclops2.Reducers}.

     * <pre>
     * {@code
     *  Xor<String,Integer> just  = Xor.primary(10);
        Xor<String,Integer> none = Xor.secondary("none");

     *  Xor<?,PersistentSetX<Integer>> xors =Xor.accumulatePrimary(ListX.of(just,none,Xor.primary(1)),Reducers.toPersistentSetX());
        //Xor.primary(PersistentSetX.of(10,1))));
     * }
     * </pre>
     * @param Xors Collection of Iors to accumulate primary values
     * @param reducer Reducer to accumulate results
     * @return Xor populated with the accumulate primary operation
     */
    public static <ST, PT, R> Xor<?, R> accumulatePrimary(final CollectionX<Xor<ST, PT>> xors, final Reducer<R> reducer) {
        return sequencePrimary(xors).map(s -> s.mapReduce(reducer));
    }

    /**
     * Accumulate the results only from those Iors which have a Primary type present, using the supplied mapping function to
     * convert the data from each Xor before reducing them using the supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see cyclops2.Monoids }..
     *
     * <pre>
     * {@code
     *  Xor<String,Integer> just  = Xor.primary(10);
        Xor<String,Integer> none = Xor.secondary("none");

     * Xor<?,String> iors = Xor.accumulatePrimary(ListX.of(just,none,Xor.primary(1)),i->""+i,Monoids.stringConcat);
       //Xor.primary("101"));
     * }
     * </pre>
     *
     *
     * @param xors Collection of Iors to accumulate primary values
     * @param mapper Mapping function to be applied to the result of each Ior
     * @param reducer Reducer to accumulate results
     * @return Xor populated with the accumulate primary operation
     */
    public static <ST, PT, R> Xor<?, R> accumulatePrimary(final CollectionX<Xor<ST, PT>> xors, final Function<? super PT, R> mapper,
            final Monoid<R> reducer) {
        return sequencePrimary(xors).map(s -> s.map(mapper)
                                               .reduce(reducer));
    }
    /**
     *  Accumulate the results only from those Xors which have a Primary type present, using the supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see cyclops2.Monoids }.
     *
     * <pre>
     * {@code
     *  Xor<String,Integer> just  = Xor.primary(10);
        Xor<String,Integer> none = Xor.secondary("none");
     *
     *  Xor<?,Integer> xors XIor.accumulatePrimary(Monoids.intSum,ListX.of(just,none,Ior.primary(1)));
        //Ior.primary(11);
     *
     * }
     * </pre>
     *
     *
     *
     * @param xors Collection of Xors to accumulate primary values
     * @param reducer  Reducer to accumulate results
     * @return  Xor populated with the accumulate primary operation
     */
    public static <ST, PT> Xor<?, PT> accumulatePrimary(final Monoid<PT> reducer,final CollectionX<Xor<ST, PT>> xors) {
        return sequencePrimary(xors).map(s -> s.reduce(reducer));
    }

    /**
     *
     * Accumulate the results only from those Xors which have a Secondary type present, using the supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see cyclops2.Monoids }.
     * <pre>
     * {@code
     * Xor.accumulateSecondary(ListX.of(Xor.secondary("failed1"),
    												Xor.secondary("failed2"),
    												Xor.primary("success")),
    												Semigroups.stringConcat)


     * //Xors.Primary[failed1failed2]
     * }
     * </pre>
     * <pre>
     * {@code
     *
     *  Xor<String,Integer> just  = Xor.primary(10);
        Xor<String,Integer> none = Xor.secondary("none");

     * Xor<?,Integer> iors = Xor.accumulateSecondary(Monoids.intSum,ListX.of(Xor.both(2, "boo!"),Xor.secondary(1)));
       //Xor.primary(3);  2+1
     *
     *
     * }
     * </pre>
     *
     * @param xors Collection of Xors to accumulate secondary values
     * @param reducer  Semigroup to combine values from each Xor
     * @return Xor populated with the accumulate Secondary operation
     */
    public static <ST, PT> Xor<?, ST> accumulateSecondary(final Monoid<ST> reducer,final CollectionX<Xor<ST, PT>> xors) {
        return sequenceSecondary(xors).map(s -> s.reduce(reducer));
    }

    /**
     * Visitor pattern for this Ior.
     * Execute the secondary function if this Xor contains an element of the secondary type
     * Execute the primary function if this Xor contains an element of the primary type
     *
     *
     * <pre>
     * {@code
     *  Xor.primary(10)
     *     .visit(secondary->"no", primary->"yes")
     *  //Xor["yes"]

        Xor.secondary(90)
           .visit(secondary->"no", primary->"yes")
        //Xor["no"]


     *
     * }
     * </pre>
     *
     * @param secondary Function to execute if this is a Secondary Xor
     * @param primary Function to execute if this is a Primary Ior
     * @param both Function to execute if this Ior contains both types
     * @return Result of executing the appropriate function
     */
    <R> R visit(Function<? super ST, ? extends R> secondary, Function<? super PT, ? extends R> primary);


    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.functor.BiTransformable#bimap(java.util.function.Function, java.util.function.Function)
     */
    @Override
    default <R1, R2> Xor<R1, R2> bimap(Function<? super ST, ? extends R1> secondary, Function<? super PT, ? extends R2> primary) {
        if (isSecondary())
            return (Xor<R1, R2>) swap().map(secondary)
                                       .swap();
        return (Xor<R1, R2>) map(primary);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.functor.BiTransformable#bipeek(java.util.function.Consumer, java.util.function.Consumer)
     */
    @Override
    default Xor<ST, PT> bipeek(Consumer<? super ST> c1, Consumer<? super PT> c2) {

        return (Xor<ST, PT>)BiTransformable.super.bipeek(c1, c2);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.functor.BiTransformable#bicast(java.lang.Class, java.lang.Class)
     */
    @Override
    default <U1, U2> Xor<U1, U2> bicast(Class<U1> type1, Class<U2> type2) {

        return (Xor<U1, U2>)BiTransformable.super.bicast(type1, type2);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.functor.BiTransformable#bitrampoline(java.util.function.Function, java.util.function.Function)
     */
    @Override
    default <R1, R2> Xor<R1, R2> bitrampoline(Function<? super ST, ? extends Trampoline<? extends R1>> mapper1,
            Function<? super PT, ? extends Trampoline<? extends R2>> mapper2) {

        return  (Xor<R1, R2>)BiTransformable.super.bitrampoline(mapper1, mapper2);
    }



    /* (non-Javadoc)
     * @see java.util.function.Supplier#get()
     */
    @Override
    PT get();
    /**
     * @return A Value containing the secondary Value if present
     */
    Value<ST> secondaryValue();
    /**
     * @return The Secondary Value if present, otherwise null
     */
    ST secondaryGet();
    /**
     * @return The Secondary value wrapped in an Optional if present, otherwise an empty Optional
     */
    Optional<ST> secondaryToOptional();
    /**
     * @return A Stream containing the secondary value if present, otherwise an empty Stream
     */
    ReactiveSeq<ST> secondaryToStream();

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#flatMap(java.util.function.Function)
     */
    @Override
    <RT1> Xor<ST, RT1> flatMap(Function<? super PT, ? extends MonadicValue<? extends RT1>> mapper);
    /**
     * Perform a flatMap operation on the Secondary type
     *
     * @param mapper Flattening transformation function
     * @return Xor containing the value inside the result of the transformation function as the Secondary value, if the Secondary type was present
     */
    <LT1> Xor<LT1, PT> secondaryFlatMap(Function<? super ST, ? extends Xor<LT1, PT>> mapper);
    /**
     * A flatMap operation that keeps the Secondary and Primary types the same
     *
     * @param fn Transformation function
     * @return Xor
     */
    Xor<ST, PT> secondaryToPrimayFlatMap(Function<? super ST, ? extends Xor<ST, PT>> fn);

    @Deprecated //use bipeek
    void peek(Consumer<? super ST> stAction, Consumer<? super PT> ptAction);
    /**
     * @return True if this is a primary Xor
     */
    public boolean isPrimary();
    /**
     * @return True if this is a secondary Xor
     */
    public boolean isSecondary();

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.applicative.ApplicativeFunctor#ap(com.aol.cyclops2.types.Value, java.util.function.BiFunction)
     */
    @Override
    <T2, R> Xor<ST, R> combine(Value<? extends T2> app, BiFunction<? super PT, ? super T2, ? extends R> fn);

    /**
     * @return An Xor with the secondary type converted to a persistent list, for use with accumulating app function  {@link Xor#combine(Xor,BiFunction)}
     */
    default Xor<LinkedListX<ST>, PT> list() {
        return secondaryMap(LinkedListX::of);
    }

    /**
     * Accumulate secondarys into a LinkedListX (extended Persistent List) and Primary with the supplied combiner function
     * Primary accumulation only occurs if all phases are primary
     *
     * @param app Value to combine with
     * @param fn Combiner function for primary values
     * @return Combined Xor
     */
    default <T2, R> Xor<LinkedListX<ST>, R> combineToList(final Xor<ST, ? extends T2> app, final BiFunction<? super PT, ? super T2, ? extends R> fn) {
        return list().combine(app.list(), Semigroups.collectionXConcat(), fn);
    }

    /**
     * Accumulate secondary values with the provided BinaryOperator / Semigroup {@link Semigroups}
     * Primary accumulation only occurs if all phases are primary
     *
     * <pre>
     * {@code
     *  Xor<String,String> fail1 =  Xor.secondary("failed1");
        Xor<LinkedListX<String>,String> result = fail1.list().combine(Xor.secondary("failed2").list(), Semigroups.collectionConcat(),(a,b)->a+b);

        //Secondary of [LinkedListX.of("failed1","failed2")))]
     * }
     * </pre>
     *
     * @param app Value to combine with
     * @param semigroup to combine secondary types
     * @param fn To combine primary types
     * @return Combined Xor
     */

    default <T2, R> Xor<ST, R> combine(final Xor<? extends ST, ? extends T2> app, final BinaryOperator<ST> semigroup,
            final BiFunction<? super PT, ? super T2, ? extends R> fn) {
        return this.visit(secondary -> app.visit(s2 -> Xor.secondary(semigroup.apply(s2, secondary)), p2 -> Xor.secondary(secondary)),
                          primary -> app.visit(s2 -> Xor.secondary(s2), p2 -> Xor.primary(fn.apply(primary, p2))));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.applicative.ApplicativeFunctor#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <T2, R> Xor<ST, R> zip(final Iterable<? extends T2> app, final BiFunction<? super PT, ? super T2, ? extends R> fn) {
        return map(v -> Tuple.tuple(v, Curry.curry2(fn)
                                            .apply(v))).flatMap(tuple -> Xor.fromIterable(app)
                                                                            .visit(i -> Xor.primary(tuple.v2.apply(i)), () -> Xor.secondary(null)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.applicative.ApplicativeFunctor#zip(java.util.function.BiFunction, org.reactivestreams.Publisher)
     */
    @Override
    default <T2, R> Xor<ST, R> zipP( final Publisher<? extends T2> app, final BiFunction<? super PT, ? super T2, ? extends R> fn) {
        return map(v -> Tuple.tuple(v, Curry.curry2(fn)
                                            .apply(v))).flatMap(tuple -> Xor.fromPublisher(app)
                                                                            .visit(i -> Xor.primary(tuple.v2.apply(i)), () -> Xor.secondary(null)));
    }


    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Zippable#zip(java.util.reactiveStream.Stream, java.util.function.BiFunction)
     */
    @Override
    default <U, R> Xor<ST, R> zipS(final Stream<? extends U> other, final BiFunction<? super PT, ? super U, ? extends R> zipper) {

        return (Xor<ST, R>) MonadicValue.super.zipS(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Zippable#zip(java.util.reactiveStream.Stream)
     */
    @Override
    default <U> Xor<ST, Tuple2<PT, U>> zipS(final Stream<? extends U> other) {

        return (Xor) MonadicValue.super.zipS(other);
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Zippable#zip(java.lang.Iterable)
     */
    @Override
    default <U> Xor<ST, Tuple2<PT, U>> zip(final Iterable<? extends U> other) {

        return (Xor) MonadicValue.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Filters#ofType(java.lang.Class)
     */
    @Override
    default <U> Xor<ST, U> ofType(final Class<? extends U> type) {

        return (Xor<ST, U>) MonadicValue.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Filters#filterNot(java.util.function.Predicate)
     */
    @Override
    default Xor<ST, PT> filterNot(final Predicate<? super PT> fn) {

        return (Xor<ST, PT>) MonadicValue.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Filters#notNull()
     */
    @Override
    default Xor<ST, PT> notNull() {

        return (Xor<ST, PT>) MonadicValue.super.notNull();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Functor#cast(java.lang.Class)
     */
    @Override
    default <U> Xor<ST, U> cast(final Class<? extends U> type) {

        return (Xor<ST, U>) MonadicValue.super.cast(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Functor#trampoline(java.util.function.Function)
     */
    @Override
    default <R> Xor<ST, R> trampoline(final Function<? super PT, ? extends Trampoline<? extends R>> mapper) {

        return (Xor<ST, R>) MonadicValue.super.trampoline(mapper);
    }

    default Trampoline<Xor<ST,PT>> toTrampoline() {
        return Trampoline.more(()->Trampoline.done(this));
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class Primary<ST, PT> implements Xor<ST, PT> {
        private final PT value;

        @Override
        public Xor<ST, PT> secondaryToPrimayMap(final Function<? super ST, ? extends PT> fn) {
            return this;
        }

        @Override
        public <R> Xor<R, PT> secondaryMap(final Function<? super ST, ? extends R> fn) {
            return (Xor<R, PT>) this;
        }

        @Override
        public <R> Xor<ST, R> map(final Function<? super PT, ? extends R> fn) {
            return new Primary<ST, R>(
                                      fn.apply(value));
        }

        @Override
        public Xor<ST, PT> secondaryPeek(final Consumer<? super ST> action) {
            return this;
        }

        @Override
        public Xor<ST, PT> peek(final Consumer<? super PT> action) {
            action.accept(value);
            return this;
        }

        @Override
        public Xor<ST, PT> filter(final Predicate<? super PT> test) {
            if (test.test(value))
                return this;
            return Xor.secondary(null);
        }

        @Override
        public Xor<PT, ST> swap() {
            return new Secondary<PT, ST>(
                                         value);
        }

        @Override
        public PT get() {
            return value;
        }

        @Override
        public ST secondaryGet() {
            return null;
        }

        @Override
        public Optional<ST> secondaryToOptional() {
            return Optional.empty();
        }

        @Override
        public ReactiveSeq<ST> secondaryToStream() {
            return ReactiveSeq.empty();
        }

        @Override
        public <RT1> Xor<ST, RT1> flatMap(Function<? super PT, ? extends MonadicValue<? extends RT1>> mapper){
            return (Xor<ST, RT1>) mapper.apply(value)
                                         .toXor();
        }

        @Override
        public <LT1> Xor<LT1, PT> secondaryFlatMap(final Function<? super ST, ? extends Xor<LT1, PT>> mapper) {
            return (Xor<LT1, PT>) this;
        }

        @Override
        public Xor<ST, PT> secondaryToPrimayFlatMap(final Function<? super ST, ? extends Xor<ST, PT>> fn) {
            return this;
        }

        @Override
        public void peek(final Consumer<? super ST> stAction, final Consumer<? super PT> ptAction) {
            ptAction.accept(value);
        }

        @Override
        public boolean isPrimary() {
            return true;
        }

        @Override
        public boolean isSecondary() {
            return false;
        }

        @Override
        public Value<ST> secondaryValue() {
            return Value.of(() -> null);
        }

        @Override
        public String toString() {
            return mkString();
        }

        @Override
        public String mkString() {
            return "Xor.primary[" + value + "]";
        }

        @Override
        public Ior<ST, PT> toIor() {
            return Ior.primary(value);
        }

        @Override
        public <R> R visit(final Function<? super ST, ? extends R> secondary, final Function<? super PT, ? extends R> primary) {
            return primary.apply(value);
        }


        /* (non-Javadoc)
         * @see com.aol.cyclops2.types.applicative.ApplicativeFunctor#ap(com.aol.cyclops2.types.Value, java.util.function.BiFunction)
         */
        @Override
        public <T2, R> Xor<ST, R> combine(final Value<? extends T2> app, final BiFunction<? super PT, ? super T2, ? extends R> fn) {
            return app.toXor()
                      .visit(s -> Xor.secondary(null), f -> Xor.primary(fn.apply(get(), app.get())));
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (!(obj instanceof Xor))
                return false;
            Xor other = (Xor) obj;
            if(!other.isPrimary())
                return false;
            if (value == null) {
                if (other.get() != null)
                    return false;
            } else if (!value.equals(other.get()))
                return false;
            return true;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            return result;
        }

    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class Secondary<ST, PT> implements Xor<ST, PT> {
        private final ST value;

        @Override
        public boolean isSecondary() {
            return true;
        }

        @Override
        public boolean isPrimary() {
            return false;
        }


        @Override
        public Xor<ST, PT> secondaryToPrimayMap(final Function<? super ST, ? extends PT> fn) {
            return new Primary<ST, PT>(
                                       fn.apply(value));
        }

        @Override
        public <R> Xor<R, PT> secondaryMap(final Function<? super ST, ? extends R> fn) {
            return new Secondary<R, PT>(
                                        fn.apply(value));
        }

        @Override
        public <R> Xor<ST, R> map(final Function<? super PT, ? extends R> fn) {
            return (Xor<ST, R>) this;
        }

        @Override
        public Xor<ST, PT> secondaryPeek(final Consumer<? super ST> action) {
            return secondaryMap((Function) FluentFunctions.expression(action));
        }

        @Override
        public Xor<ST, PT> peek(final Consumer<? super PT> action) {
            return this;
        }

        @Override
        public Xor<ST, PT> filter(final Predicate<? super PT> test) {
            return this;
        }

        @Override
        public Xor<PT, ST> swap() {
            return new Primary<PT, ST>(
                                       value);
        }

        @Override
        public PT get() {
            throw new NoSuchElementException();
        }

        @Override
        public ST secondaryGet() {
            return value;
        }

        @Override
        public Optional<ST> secondaryToOptional() {
            return Optional.ofNullable(value);
        }

        @Override
        public ReactiveSeq<ST> secondaryToStream() {
            return ReactiveSeq.fromStream(Streams.optionalToStream(secondaryToOptional()));
        }

        @Override
        public < RT1> Xor<ST, RT1> flatMap(final Function<? super PT, ? extends MonadicValue<? extends RT1>> mapper) {
            return (Xor<ST, RT1>) this;
        }

        @Override
        public <LT1> Xor<LT1, PT> secondaryFlatMap(final Function<? super ST, ? extends Xor<LT1, PT>> mapper) {
            return mapper.apply(value);
        }

        @Override
        public Xor<ST, PT> secondaryToPrimayFlatMap(final Function<? super ST, ? extends Xor<ST, PT>> fn) {
            return fn.apply(value);
        }

        @Override
        public void peek(final Consumer<? super ST> stAction, final Consumer<? super PT> ptAction) {
            stAction.accept(value);

        }

        @Override
        public <R> R visit(final Function<? super ST, ? extends R> secondary, final Function<? super PT, ? extends R> primary) {
            return secondary.apply(value);
        }

        @Override
        public Maybe<PT> toMaybe() {
            return Maybe.none();
        }

        @Override
        public Optional<PT> toOptional() {
            return Optional.empty();
        }

        @Override
        public Value<ST> secondaryValue() {
            return Value.of(() -> value);
        }

        @Override
        public String toString() {
            return mkString();
        }

        @Override
        public String mkString() {
            return "Xor.secondary[" + value + "]";
        }



        @Override
        public Ior<ST, PT> toIor() {
            return Ior.secondary(value);
        }

        /* (non-Javadoc)
         * @see com.aol.cyclops2.types.applicative.ApplicativeFunctor#ap(com.aol.cyclops2.types.Value, java.util.function.BiFunction)
         */
        @Override
        public <T2, R> Xor<ST, R> combine(final Value<? extends T2> app, final BiFunction<? super PT, ? super T2, ? extends R> fn) {
            return (Xor<ST, R>) this;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            return result;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            Xor other = (Xor) obj;
            if(other.isPrimary())
                return false;
            if (value == null) {
                if (other.secondaryGet() != null)
                    return false;
            } else if (!value.equals(other.secondaryGet()))
                return false;
            
            return true;
        }

    }

    public static class Instances {

        public static <L> InstanceDefinitions<Higher<xor, L>> definitions(){
            return new InstanceDefinitions<Higher<xor, L>>() {
                @Override
                public <T, R> Functor<Higher<xor, L>> functor() {
                    return Instances.functor();
                }

                @Override
                public <T> Pure<Higher<xor, L>> unit() {
                    return Instances.unit();
                }

                @Override
                public <T, R> Applicative<Higher<xor, L>> applicative() {
                    return Instances.applicative();
                }

                @Override
                public <T, R> Monad<Higher<xor, L>> monad() {
                    return Instances.monad();
                }

                @Override
                public <T, R> Maybe<MonadZero<Higher<xor, L>>> monadZero() {
                    return Maybe.just(Instances.monadZero());
                }

                @Override
                public <T> Maybe<MonadPlus<Higher<xor, L>>> monadPlus() {
                    return Maybe.just(Instances.monadPlus());
                }

                @Override
                public <T> Maybe<MonadPlus<Higher<xor, L>>> monadPlus(Monoid<Higher<Higher<xor, L>, T>> m) {
                    return Maybe.just(Instances.monadPlus(m));
                }

                @Override
                public <C2, T> Maybe<Traverse<Higher<xor, L>>> traverse() {
                    return Maybe.just(Instances.traverse());
                }

                @Override
                public <T> Maybe<Foldable<Higher<xor, L>>> foldable() {
                    return Maybe.just(Instances.foldable());
                }

                @Override
                public <T> Maybe<Comonad<Higher<xor, L>>> comonad() {
                    return Maybe.just(Instances.comonad());
                }

                @Override
                public <T> Maybe<Unfoldable<Higher<xor, L>>> unfoldable() {
                    return Maybe.none();
                }
            };
        }
        public static <L> Functor<Higher<xor, L>> functor() {
            return new Functor<Higher<xor, L>>() {

                @Override
                public <T, R> Higher<Higher<xor, L>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<xor, L>, T> ds) {
                    Xor<L,T> xor = Xor.narrowK(ds);
                    return xor.map(fn);
                }
            };
        }
        public static <L> Pure<Higher<xor, L>> unit() {
            return new Pure<Higher<xor, L>>() {

                @Override
                public <T> Higher<Higher<xor, L>, T> unit(T value) {
                    return Xor.primary(value);
                }
            };
        }
        public static <L> Applicative<Higher<xor, L>> applicative() {
            return new Applicative<Higher<xor, L>>() {


                @Override
                public <T, R> Higher<Higher<xor, L>, R> ap(Higher<Higher<xor, L>, ? extends Function<T, R>> fn, Higher<Higher<xor, L>, T> apply) {
                    Xor<L,T>  xor = Xor.narrowK(apply);
                    Xor<L, ? extends Function<T, R>> xorFn = Xor.narrowK(fn);
                    return xorFn.combine(xor,(a,b)->a.apply(b));

                }

                @Override
                public <T, R> Higher<Higher<xor, L>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<xor, L>, T> ds) {
                    return Instances.<L>functor().map(fn,ds);
                }

                @Override
                public <T> Higher<Higher<xor, L>, T> unit(T value) {
                    return Instances.<L>unit().unit(value);
                }
            };
        }
        public static <L> Monad<Higher<xor, L>> monad() {
            return new Monad<Higher<xor, L>>() {

                @Override
                public <T, R> Higher<Higher<xor, L>, R> flatMap(Function<? super T, ? extends Higher<Higher<xor, L>, R>> fn, Higher<Higher<xor, L>, T> ds) {
                    Xor<L,T> xor = Xor.narrowK(ds);
                    return xor.flatMap(fn.andThen(Xor::narrowK));
                }

                @Override
                public <T, R> Higher<Higher<xor, L>, R> ap(Higher<Higher<xor, L>, ? extends Function<T, R>> fn, Higher<Higher<xor, L>, T> apply) {
                   return Instances.<L>applicative().ap(fn,apply);

                }

                @Override
                public <T, R> Higher<Higher<xor, L>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<xor, L>, T> ds) {
                    return Instances.<L>functor().map(fn,ds);
                }

                @Override
                public <T> Higher<Higher<xor, L>, T> unit(T value) {
                    return Instances.<L>unit().unit(value);
                }
            };
        }
        public static <L> Traverse<Higher<xor, L>> traverse() {
            return new Traverse<Higher<xor, L>>() {

                @Override
                public <C2, T, R> Higher<C2, Higher<Higher<xor, L>, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn, Higher<Higher<xor, L>, T> ds) {
                    Xor<L,T> maybe = Xor.narrowK(ds);
                    return maybe.visit(left->  applicative.unit(Xor.<L,R>secondary(left)),
                                        right->applicative.map(m->Xor.primary(m), fn.apply(right)));
                }

                @Override
                public <C2, T> Higher<C2, Higher<Higher<xor, L>, T>> sequenceA(Applicative<C2> applicative, Higher<Higher<xor, L>, Higher<C2, T>> ds) {
                    return traverseA(applicative,Function.identity(),ds);
                }



                @Override
                public <T, R> Higher<Higher<xor, L>, R> ap(Higher<Higher<xor, L>, ? extends Function<T, R>> fn, Higher<Higher<xor, L>, T> apply) {
                    return Instances.<L>applicative().ap(fn,apply);

                }

                @Override
                public <T, R> Higher<Higher<xor, L>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<xor, L>, T> ds) {
                    return Instances.<L>functor().map(fn,ds);
                }

                @Override
                public <T> Higher<Higher<xor, L>, T> unit(T value) {
                    return Instances.<L>unit().unit(value);
                }
            };
        }
        public static <L> Foldable<Higher<xor, L>> foldable() {
            return new Foldable<Higher<xor, L>>() {


                @Override
                public <T> T foldRight(Monoid<T> monoid, Higher<Higher<xor, L>, T> ds) {
                    Xor<L,T> xor = Xor.narrowK(ds);
                    return xor.foldRight(monoid);
                }

                @Override
                public <T> T foldLeft(Monoid<T> monoid, Higher<Higher<xor, L>, T> ds) {
                    Xor<L,T> xor = Xor.narrowK(ds);
                    return xor.foldLeft(monoid);
                }
            };
        }
        public static <L> MonadZero<Higher<xor, L>> monadZero() {
            return new MonadZero<Higher<xor, L>>() {

                @Override
                public Higher<Higher<xor, L>, ?> zero() {
                    return Xor.secondary(null);
                }

                @Override
                public <T, R> Higher<Higher<xor, L>, R> flatMap(Function<? super T, ? extends Higher<Higher<xor, L>, R>> fn, Higher<Higher<xor, L>, T> ds) {
                    Xor<L,T> xor = Xor.narrowK(ds);
                    return xor.flatMap(fn.andThen(Xor::narrowK));
                }

                @Override
                public <T, R> Higher<Higher<xor, L>, R> ap(Higher<Higher<xor, L>, ? extends Function<T, R>> fn, Higher<Higher<xor, L>, T> apply) {
                    return Instances.<L>applicative().ap(fn,apply);

                }

                @Override
                public <T, R> Higher<Higher<xor, L>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<xor, L>, T> ds) {
                    return Instances.<L>functor().map(fn,ds);
                }

                @Override
                public <T> Higher<Higher<xor, L>, T> unit(T value) {
                    return Instances.<L>unit().unit(value);
                }
            };
        }
        public static <L> MonadPlus<Higher<xor, L>> monadPlus() {
            Monoid m = Monoids.firstPrimaryXor((Xor)Xor.narrowK(Instances.<L>monadZero().zero()));

            return monadPlus(m);
        }
        public static <L,T> MonadPlus<Higher<xor, L>> monadPlus(Monoid<Higher<Higher<xor, L>, T>> m) {
            return new MonadPlus<Higher<xor, L>>() {

                @Override
                public Monoid<Higher<Higher<xor, L>, ?>> monoid() {
                    return (Monoid)m;
                }

                @Override
                public Higher<Higher<xor, L>, ?> zero() {
                    return Instances.<L>monadZero().zero();
                }

                @Override
                public <T, R> Higher<Higher<xor, L>, R> flatMap(Function<? super T, ? extends Higher<Higher<xor, L>, R>> fn, Higher<Higher<xor, L>, T> ds) {
                    Xor<L,T> xor = Xor.narrowK(ds);
                    return xor.flatMap(fn.andThen(Xor::narrowK));
                }

                @Override
                public <T, R> Higher<Higher<xor, L>, R> ap(Higher<Higher<xor, L>, ? extends Function<T, R>> fn, Higher<Higher<xor, L>, T> apply) {
                    return Instances.<L>applicative().ap(fn,apply);

                }

                @Override
                public <T, R> Higher<Higher<xor, L>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<xor, L>, T> ds) {
                    return Instances.<L>functor().map(fn,ds);
                }

                @Override
                public <T> Higher<Higher<xor, L>, T> unit(T value) {
                    return Instances.<L>unit().unit(value);
                }
            };
        }
        public static <L> Comonad<Higher<xor, L>> comonad() {
            return new ComonadByPure<Higher<xor, L>>() {


                @Override
                public <T> T extract(Higher<Higher<xor, L>, T> ds) {
                    Xor<L,T> xor = Xor.narrowK(ds);
                    return xor.get();
                }


                @Override
                public <T, R> Higher<Higher<xor, L>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<xor, L>, T> ds) {
                    return Instances.<L>functor().map(fn,ds);
                }

                @Override
                public <T> Higher<Higher<xor, L>, T> unit(T value) {
                    return Instances.<L>unit().unit(value);
                }
            };
        }
    }
}