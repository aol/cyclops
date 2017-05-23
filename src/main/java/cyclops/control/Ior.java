package cyclops.control;

import com.aol.cyclops2.data.collections.extensions.CollectionX;
import com.aol.cyclops2.types.*;
import com.aol.cyclops2.types.anyM.AnyMValue;
import com.aol.cyclops2.types.stream.reactive.ValueSubscriber;
import cyclops.companion.Streams;
import cyclops.collections.mutable.ListX;
import cyclops.function.*;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import cyclops.stream.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
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
 * Inclusive Or (can be one of Primary, Secondary or Both Primary and Secondary)
 * 
 * An Either or Union type, but right biased. Primary and Secondary are used instead of Right & Left.
 * 'Right' (or primary type) biased disjunct union.
 *  No 'projections' are provided, swap() and secondaryXXXX alternative methods can be used instead.
 *  
 *  
 *  For eXclusive Ors @see Xor
 * 
 * @author johnmcclean
 *
 * @param <ST> Secondary type
 * @param <PT> Primary type
 */
public interface Ior<ST, PT> extends To<Ior<ST, PT>>, MonadicValue<PT>, BiFunctor<ST, PT> {


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
     * @return Consumer we can apply to consume value
     */
    static <X, LT extends X, M extends X, RT extends X>  Consumer<Consumer<? super X>> consumeAny(Ior<LT,RT> either){
        return in->visitAny(in,either);
    }

    static <X, LT extends X, M extends X, RT extends X,R>  Function<Function<? super X, R>,R> applyAny(Ior<LT,RT> either){
        return in->visitAny(either,in);
    }

    static <X, PT extends X, ST extends X,R> R visitAny(Ior<ST,PT> either, Function<? super X, ? extends R> fn){
        return either.visit(fn, fn, (a,b)-> fn.apply(a));
    }

    static <X, LT extends X, RT extends X> X visitAny(Consumer<? super X> c,Ior<LT,RT> either){
        Function<? super X, X> fn = x ->{
            c.accept(x);
            return x;
        };
        return visitAny(either,fn);
    }
    @Deprecated //internal use only
    public static <ST, PT> Ior<ST, PT> both(final Ior<ST, PT> secondary, final Ior<ST, PT> primary) {
        return new Both<ST, PT>(
                                secondary, primary);
    }
    /**
     * Construct an Ior that contains a single value extracted from the supplied reactive-streams Publisher

     * <pre>
     * {@code
     *   ReactiveSeq<Integer> reactiveStream =  ReactiveSeq.of(1,2,3);

        Ior<Throwable,Integer> future = Ior.fromPublisher(reactiveStream);

        //Ior[1]
     *
     * }
     * </pre>
     *
     * @param pub Publisher to extract value from
     * @return Ior populated from Publisher
     */
    public static <T> Ior<Throwable, T> fromPublisher(final Publisher<T> pub) {
        final ValueSubscriber<T> sub = ValueSubscriber.subscriber();
        pub.subscribe(sub);
        return sub.toXor()
                  .toIor();
    }

    /**
     * Construct an Ior that contains a single value extracted from the supplied Iterable
     * <pre>
     * {@code
     *   List<Integer> list =  Arrays.asList(1,2,3);

        Ior<Throwable,Integer> future = Ior.fromPublisher(list);

        //Ior[1]
     *
     * }
     * </pre>
     * @param iterable Iterable to extract value from
     * @return Ior populated from Iterable
     */
    public static <ST, T> Ior<ST, T> fromIterable(final Iterable<T> iterable) {
        final Iterator<T> it = iterable.iterator();
        return Ior.primary(it.hasNext() ? it.next() : null);
    }

    /**
     * Create an instance of the primary type. Most methods are biased to the primary type,
     * which means, for example, that the map method operates on the primary type but does nothing on secondary Iors
     *
     * <pre>
     * {@code
     *   Ior.<Integer,Integer>primary(10).map(i->i+1);
     * //Ior.primary[11]
     *
     *
     * }
     * </pre>
     *
     *
     * @param value To construct an Ior from
     * @return Primary type instanceof Ior
     */
    public static <ST, PT> Ior<ST, PT> primary(final PT primary) {
        return new Primary<>(
                             primary);
    }
    /**
     * Create an instance of the secondary type. Most methods are biased to the primary type,
     * so you will need to use swap() or secondaryXXXX to manipulate the wrapped value
     *
     * <pre>
     * {@code
     *   Ior.<Integer,Integer>secondary(10).map(i->i+1);
     *   //Ior.secondary[10]
     *
     *    Ior.<Integer,Integer>secondary(10).swap().map(i->i+1);
     *    //Ior.primary[11]
     * }
     * </pre>
     *
     *
     * @param value to wrap
     * @return Secondary instance of Ior
     */
    public static <ST, PT> Ior<ST, PT> secondary(final ST secondary) {
        return new Secondary<>(
                               secondary);
    }



    /**
     * Create an Ior instance that contains both secondary and primary types
     *
     * <pre>
     * {@code
     *    Ior<String,Ingeger> kv = Ior.both("hello",90);
     *    //Ior["hello",90]
     * }
     * </pre>
     *
     * @param secondary Secondary value
     * @param primary Primary value
     * @return Ior that contains both the secondary and the primary value
     */
    public static <ST, PT> Ior<ST, PT> both(final ST secondary, final PT primary) {
        return new Both<ST, PT>(
                                Ior.secondary(secondary), Ior.primary(primary));
    }

    @Override
    default <R> Ior<ST,R> zipWith(Iterable<Function<? super PT, ? extends R>> fn) {
        return (Ior<ST,R>)MonadicValue.super.zipWith(fn);
    }

    @Override
    default <R> Ior<ST,R> zipWithS(Stream<Function<? super PT, ? extends R>> fn) {
        return (Ior<ST,R>)MonadicValue.super.zipWithS(fn);
    }

    @Override
    default <R> Ior<ST,R> zipWithP(Publisher<Function<? super PT, ? extends R>> fn) {
        return (Ior<ST,R>)MonadicValue.super.zipWithP(fn);
    }

    @Override
    default <R> Ior<ST,R> retry(final Function<? super PT, ? extends R> fn) {
        return (Ior<ST,R>)MonadicValue.super.retry(fn);
    }

    @Override
    default <U> Ior<ST,Tuple2<PT, U>> zipP(final Publisher<? extends U> other) {
        return (Ior)MonadicValue.super.zipP(other);
    }

    @Override
    default <R> Ior<ST,R> retry(final Function<? super PT, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (Ior<ST,R>)MonadicValue.super.retry(fn,retries,delay,timeUnit);
    }

    @Override
    default <S, U> Ior<ST,Tuple3<PT, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {
        return (Ior)MonadicValue.super.zip3(second,third);
    }

    @Override
    default <S, U, R> Ior<ST,R> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third, final Fn3<? super PT, ? super S, ? super U, ? extends R> fn3) {
        return (Ior<ST,R>)MonadicValue.super.zip3(second,third,fn3);
    }

    @Override
    default <T2, T3, T4> Ior<ST,Tuple4<PT, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth) {
        return (Ior)MonadicValue.super.zip4(second,third,fourth);
    }

    @Override
    default <T2, T3, T4, R> Ior<ST,R> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Fn4<? super PT, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (Ior<ST,R>)MonadicValue.super.zip4(second,third,fourth,fn);
    }

    @Override
    default <R> Ior<ST,R> flatMapS(final Function<? super PT, ? extends Stream<? extends R>> mapper) {
        return (Ior<ST,R>)MonadicValue.super.flatMapS(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    default <T2, R1, R2, R3, R> Ior<ST,R> forEach4(Function<? super PT, ? extends MonadicValue<R1>> value1,
            BiFunction<? super PT, ? super R1, ? extends MonadicValue<R2>> value2,
            Fn3<? super PT, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
            Fn4<? super PT, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return (Ior<ST,R>)MonadicValue.super.forEach4(value1, value2, value3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    default <T2, R1, R2, R3, R> Ior<ST,R> forEach4(Function<? super PT, ? extends MonadicValue<R1>> value1,
            BiFunction<? super PT, ? super R1, ? extends MonadicValue<R2>> value2,
            Fn3<? super PT, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
            Fn4<? super PT, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
            Fn4<? super PT, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return (Ior<ST,R>)MonadicValue.super.forEach4(value1, value2, value3, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    default <T2, R1, R2, R> Ior<ST,R> forEach3(Function<? super PT, ? extends MonadicValue<R1>> value1,
            BiFunction<? super PT, ? super R1, ? extends MonadicValue<R2>> value2,
            Fn3<? super PT, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (Ior<ST,R>)MonadicValue.super.forEach3(value1, value2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    default <T2, R1, R2, R> Ior<ST,R> forEach3(Function<? super PT, ? extends MonadicValue<R1>> value1,
            BiFunction<? super PT, ? super R1, ? extends MonadicValue<R2>> value2,
            Fn3<? super PT, ? super R1, ? super R2, Boolean> filterFunction,
            Fn3<? super PT, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (Ior<ST,R>)MonadicValue.super.forEach3(value1, value2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> Ior<ST,R> forEach2(Function<? super PT, ? extends MonadicValue<R1>> value1,
            BiFunction<? super PT, ? super R1, ? extends R> yieldingFunction) {

        return (Ior<ST,R>)MonadicValue.super.forEach2(value1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> Ior<ST,R> forEach2(Function<? super PT, ? extends MonadicValue<R1>> value1,
            BiFunction<? super PT, ? super R1, Boolean> filterFunction,
            BiFunction<? super PT, ? super R1, ? extends R> yieldingFunction) {
        return (Ior<ST,R>)MonadicValue.super.forEach2(value1, filterFunction, yieldingFunction);
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#flatMapI(java.util.function.Function)
     */
    @Override
    default <R> Ior<ST, R> flatMapI(Function<? super PT, ? extends Iterable<? extends R>> mapper) {
        return (Ior<ST, R>)MonadicValue.super.flatMapI(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#flatMapP(java.util.function.Function)
     */
    @Override
    default <R> Ior<ST, R> flatMapP(Function<? super PT, ? extends Publisher<? extends R>> mapper) {
        return (Ior<ST, R>)MonadicValue.super.flatMapP(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#fromEither5()
     */
    default AnyMValue<Witness.ior,PT> anyM() {
        return AnyM.fromIor(this);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#unit(java.lang.Object)
     */
    @Override
    default <T> Ior<ST, T> unit(final T unit) {
        return Ior.primary(unit);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Filters#filter(java.util.function.Predicate)
     */
    @Override
    Ior<ST, PT> filter(Predicate<? super PT> test);

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Value#toXor()
     */
    @Override
    Xor<ST, PT> toXor(); //drop ST

    /**
     * @return Convert to an Xor, dropping the primary type if this Ior contains both
     */
    Xor<ST, PT> toXorDropPrimary(); //drop PT

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Value#toXor(java.lang.Object)
     */
    @Override
    default <ST2> Xor<ST2, PT> toXor(final ST2 secondary) {
        return visit(s -> Xor.secondary(secondary), p -> Xor.primary(p), (s, p) -> Xor.primary(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Value#toIor()
     */
    @Override
    default Ior<ST, PT> toIor() {
        return this;
    }

    /**
     * If this Ior contains the Secondary type only, map it's value so that it contains the Primary type only
     * If this Ior contains both types, this method has no effect in the default implementations
     *
     * @param fn Function to map secondary type to primary
     * @return Ior with secondary type mapped to primary
     */
    Ior<ST, PT> secondaryToPrimayMap(Function<? super ST, ? extends PT> fn);

    /**
     * Always map the Secondary type of this Ior if it is present using the provided transformation function
     *
     * @param fn Transformation function for Secondary types
     * @return Ior with Secondary type transformed
     */
    <R> Ior<R, PT> secondaryMap(Function<? super ST, ? extends R> fn);

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#map(java.util.function.Function)
     */
    @Override
    <R> Ior<ST, R> map(Function<? super PT, ? extends R> fn);

    /**
     * Peek at the Secondary type value if present
     *
     * @param action Consumer to peek at the Secondary type value
     * @return Ior with the same values as before
     */
    Ior<ST, PT> secondaryPeek(Consumer<? super ST> action);

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Transformable#peek(java.util.function.Consumer)
     */
    @Override
    Ior<ST, PT> peek(Consumer<? super PT> action);

    /**
     * @return Ior with Primary and Secondary types and value swapped
     */
    Ior<PT, ST> swap();

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#coflatMap(java.util.function.Function)
     */
    @Override
    default <R> Ior<ST, R> coflatMap(final Function<? super MonadicValue<PT>, R> mapper) {
        return (Ior<ST, R>) MonadicValue.super.coflatMap(mapper);
    }

    //cojoin
    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#nest()
     */
    @Override
    default Ior<ST, MonadicValue<PT>> nest() {
        return this.map(t -> unit(t));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#combine(cyclops2.function.Monoid, com.aol.cyclops2.types.MonadicValue)
     */
    @Override
    default Ior<ST, PT> combineEager(final Monoid<PT> monoid, final MonadicValue<? extends PT> v2) {
        return (Ior<ST, PT>) MonadicValue.super.combineEager(monoid, v2);
    }

    /**
     * @return An empty Optional if this Ior only has lazy the Secondary or Primary type. Or an Optional containing a Tuple2
     * with both the Secondary and Primary types if they are both present.
     */
    Optional<Tuple2<ST, PT>> both();

    /**
     * @return The result of @see Ior#both wrapped in a Value
     */
    default Value<Optional<Tuple2<ST, PT>>> bothValue() {
        return () -> both();
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.BiFunctor#bimap(java.util.function.Function, java.util.function.Function)
     */
    @Override
    default <R1, R2> Ior<R1, R2> bimap(final Function<? super ST, ? extends R1> fn1, final Function<? super PT, ? extends R2> fn2) {
        final Eval<Ior<R1, R2>> ptMap = (Eval) Eval.later(() -> this.map(fn2)); //force unused secondary to required
        final Eval<Ior<R1, R2>> stMap = (Eval) Eval.later(() -> this.secondaryMap(fn1)); //force unused primary to required
        if (isPrimary())
            return Ior.<R1, R2> primary(ptMap.get()
                                             .get());
        if (isSecondary())
            return Ior.<R1, R2> secondary(stMap.get()
                                               .swap()
                                               .get());

        return Both.both(stMap.get(), ptMap.get());
    }


    /**
     * Visitor pattern for this Ior.
     * Execute the secondary function if this Ior contains an element of the secondary type only
     * Execute the primary function if this Ior contains an element of the primary type only
     * Execute the both function if this Ior contains an element of both type
     *
     * <pre>
     * {@code
     *  Ior.primary(10)
     *     .visit(secondary->"no", primary->"yes",(sec,pri)->"oops!")
     *  //Ior["yes"]

        Ior.secondary(90)
           .visit(secondary->"no", primary->"yes",(sec,pri)->"oops!")
        //Ior["no"]

        Ior.both(10, "eek")
           .visit(secondary->"no", primary->"yes",(sec,pri)->"oops!")
        //Ior["oops!"]
     *
     *
     * }
     * </pre>
     *
     * @param secondary Function to execute if this is a Secondary Ior
     * @param primary Function to execute if this is a Primary Ior
     * @param both Function to execute if this Ior contains both types
     * @return Result of executing the appropriate function
     */
    default <R> R visit(final Function<? super ST, ? extends R> secondary, final Function<? super PT, ? extends R> primary,
            final BiFunction<? super ST, ? super PT, ? extends R> both) {

        if (isSecondary())
            return swap().visit(secondary, () -> null);
        if (isPrimary())
            return visit(primary, () -> null);

        return both().get()
                         .map((a, b) -> both.apply(a, b));
    }


    /* (non-Javadoc)
     * @see java.util.function.Supplier#get()
     */
    @Override
    PT get();

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Convertable#isPresent()
     */
    @Override
    default boolean isPresent() {
        return isPrimary() || isBoth();
    }

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
    public < RT1> Ior<ST, RT1> flatMap(final Function<? super PT, ? extends MonadicValue< ? extends RT1>> mapper);


    /**
     * Perform a flatMap operation on the Secondary type
     *
     * @param mapper Flattening transformation function
     * @return Ior containing the value inside the result of the transformation function as the Secondary value, if the Secondary type was present
     */
    <LT1> Ior<LT1, PT> secondaryFlatMap(Function<? super ST, ? extends Ior<LT1,PT>> mapper);

    /**
     * A flatMap operation that keeps the Secondary and Primary types the same
     *
     * @param fn Transformation function
     * @return Ior
     */
    Ior<ST, PT> secondaryToPrimayFlatMap(Function<? super ST, ? extends Ior<ST, PT>> fn);

    /**
     * @return True if this is a primary (only) Ior
     */
    public boolean isPrimary();

    /**
     * @return True if this was a secondary (only) Ior
     */
    public boolean isSecondary();

    /**
     * @return True if this Ior has both secondary and primary types
     */
    public boolean isBoth();

    /**
     *  Turn a collection of Iors into a single Ior with Lists of values.
     *  Primary and secondary types are swapped during this operation.
     *
     * <pre>
     * {@code
     *  Ior<String,Integer> just  = Ior.primary(10);
        Ior<String,Integer> none = Ior.secondary("none");
     *  Ior<ListX<Integer>,ListX<String>> iors =Ior.sequenceSecondary(ListX.of(just,none,Ior.primary(1)));
        //Ior.primary(ListX.of("none")))
     *
     * }
     * </pre>
     *
     *
     * @param iors Iors to sequence
     * @return Ior sequenced and swapped
     */
    public static <ST, PT> Ior<ListX<PT>, ListX<ST>> sequenceSecondary(final CollectionX<? extends Ior<ST, PT>> iors) {
        return AnyM.sequence(iors.stream().filterNot(Ior::isPrimary).map(i->AnyM.fromIor(i.swap())).toListX(),Witness.ior.INSTANCE)
                   .to(Witness::ior);

    }

    /**
     * Accumulate the result of the Secondary types in the Collection of Iors provided using the supplied Reducer  {@see cyclops2.Reducers}.
     *
     * <pre>
     * {@code
     *  Ior<String,Integer> just  = Ior.primary(10);
        Ior<String,Integer> none = Ior.secondary("none");
<<<<<<< HEAD
<<<<<<< HEAD

     *  Ior<?,PersistentSetX<String>> iors = Ior.accumulateSecondary(ListX.of(just,none,Ior.primary(1)),Reducers.<String>toPSetX());
=======
        
     *  Ior<?,PersistentSetX<String>> iors = Ior.accumulateSecondary(ListX.of(just,none,Ior.primary(1)),Reducers.<String>toPersistentSetX());
>>>>>>> master
=======
        
     *  Ior<?,PersistentSetX<String>> iors = Ior.accumulateSecondary(ListX.of(just,none,Ior.primary(1)),Reducers.<String>toPersistentSetX());
>>>>>>> master
      //Ior.primary(PersistentSetX.of("none"))));
      * }
     * </pre>
     * @param iors Collection of Iors to accumulate secondary values
     * @param reducer Reducer to accumulate results
     * @return Ior populated with the accumulate secondary operation
     */
    public static <ST, PT, R> Ior<?, R> accumulateSecondary(final CollectionX<Ior<ST, PT>> iors, final Reducer<R> reducer) {
        return sequenceSecondary(iors).map(s -> s.mapReduce(reducer));
    }

    /**
     * Accumulate the results only from those Iors which have a Secondary type present, using the supplied mapping function to
     * convert the data from each Ior before reducing them using the supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see cyclops2.Monoids }.
     *
     * <pre>
     * {@code
     *  Ior<String,Integer> just  = Ior.primary(10);
        Ior<String,Integer> none = Ior.secondary("none");

     *  Ior<?,String> iors = Ior.accumulateSecondary(ListX.of(just,none,Ior.secondary("1")),i->""+i,Monoids.stringConcat);
        //Ior.primary("none1")
     *
     * }
     * </pre>
     *
     *
     *
     * @param iors Collection of Iors to accumulate secondary values
     * @param mapper Mapping function to be applied to the result of each Ior
     * @param reducer Semigroup to combine values from each Ior
     * @return Ior populated with the accumulate Secondary operation
     */
    public static <ST, PT, R> Ior<?, R> accumulateSecondary(final CollectionX<Ior<ST, PT>> iors, final Function<? super ST, R> mapper,
            final Monoid<R> reducer) {
        return sequenceSecondary(iors).map(s -> s.map(mapper)
                                                 .reduce(reducer));
    }

    /**
     *  Accumulate the results only from those Iors which have a Secondary type present, using the supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see cyclops2.Monoids }.
     *
     * <pre>
     * {@code
     *
     *  Ior<String,Integer> just  = Ior.primary(10);
        Ior<String,Integer> none = Ior.secondary("none");

     * Ior<?,Integer> iors = Ior.accumulateSecondary(Monoids.intSum,ListX.of(Ior.both(2, "boo!"),Ior.secondary(1)));
       //Ior.primary(3);  2+1
     *
     *
     * }
     * </pre>
     *
     *
     * @param iors Collection of Iors to accumulate secondary values
     * @param reducer  Semigroup to combine values from each Ior
     * @return populated with the accumulate Secondary operation
     */
    public static <ST, PT> Ior<?, ST> accumulateSecondary(final Monoid<ST> reducer,final CollectionX<Ior<ST, PT>> iors) {
        return sequenceSecondary(iors).map(s -> s.reduce(reducer));
    }

    /**
     *  Turn a collection of Iors into a single Ior with Lists of values.
     *
     * <pre>
     * {@code
     *
     * Ior<String,Integer> just  = Ior.primary(10);
       Ior<String,Integer> none = Ior.secondary("none");


     * Ior<ListX<String>,ListX<Integer>> iors =Ior.sequencePrimary(ListX.of(just,none,Ior.primary(1)));
       //Ior.primary(ListX.of(10,1)));
     *
     * }</pre>
     *
     *
     *
     * @param iors Iors to sequence
     * @return Ior Sequenced
     */
    public static <ST, PT> Ior<ListX<ST>, ListX<PT>> sequencePrimary(final CollectionX<Ior<ST, PT>> iors) {
        return AnyM.sequence(iors.stream().filterNot(Ior::isSecondary).map(AnyM::fromIor).toListX(),Witness.ior.INSTANCE)
                   .to(Witness::ior);
    }

    /**
     * Accumulate the result of the Primary types in the Collection of Iors provided using the supplied Reducer  {@see cyclops2.Reducers}.

     * <pre>
     * {@code
     *  Ior<String,Integer> just  = Ior.primary(10);
        Ior<String,Integer> none = Ior.secondary("none");
<<<<<<< HEAD
<<<<<<< HEAD
     *
     *  Ior<?,PersistentSetX<Integer>> iors =Ior.accumulatePrimary(ListX.of(just,none,Ior.primary(1)),Reducers.toPSetX());
=======
     * 
     *  Ior<?,PersistentSetX<Integer>> iors =Ior.accumulatePrimary(ListX.of(just,none,Ior.primary(1)),Reducers.toPersistentSetX());
>>>>>>> master
=======
     * 
     *  Ior<?,PersistentSetX<Integer>> iors =Ior.accumulatePrimary(ListX.of(just,none,Ior.primary(1)),Reducers.toPersistentSetX());
>>>>>>> master
        //Ior.primary(PersistentSetX.of(10,1))));
     * }
     * </pre>
     * @param iors Collection of Iors to accumulate primary values
     * @param reducer Reducer to accumulate results
     * @return Ior populated with the accumulate primary operation
     */
    public static <ST, PT, R> Ior<?, R> accumulatePrimary(final CollectionX<Ior<ST, PT>> iors, final Reducer<R> reducer) {
        return sequencePrimary(iors).map(s -> s.mapReduce(reducer));
    }

    /**
     * Accumulate the results only from those Iors which have a Primary type present, using the supplied mapping function to
     * convert the data from each Ior before reducing them using the supplied Semgigroup (a combining BiFunction/BinaryOperator that takes two
     * input values of the same type and returns the combined result) {@see cyclops2.Semigroups }.
     *
     * <pre>
     * {@code
     *  Ior<String,Integer> just  = Ior.primary(10);
        Ior<String,Integer> none = Ior.secondary("none");

     * Ior<?,String> iors = Ior.accumulatePrimary(ListX.of(just,none,Ior.primary(1)),i->""+i,Semigroups.stringConcat);
       //Ior.primary("101"));
     * }
     * </pre>
     *
     *
     * @param iors Collection of Iors to accumulate primary values
     * @param mapper Mapping function to be applied to the result of each Ior
     * @param reducer Reducer to accumulate results
     * @return Ior populated with the accumulate primary operation
     */
    public static <ST, PT, R> Ior<?, R> accumulatePrimary(final CollectionX<Ior<ST, PT>> iors, final Function<? super PT, R> mapper,
            final Semigroup<R> reducer) {
        return sequencePrimary(iors).map(s -> s.map(mapper)
                                               .reduce(reducer)
                                               .get());
    }

    /**
     *  Accumulate the results only from those Iors which have a Primary type present, using the supplied  Semgigroup (a combining BiFunction/BinaryOperator that takes two
     * input values of the same type and returns the combined result) {@see cyclops2.Semigroups }.
     *
     * <pre>
     * {@code
     *  Ior<String,Integer> just  = Ior.primary(10);
        Ior<String,Integer> none = Ior.secondary("none");
     *
     *  Ior<?,Integer> iors =Ior.accumulatePrimary(ListX.of(just,none,Ior.primary(1)),Semigroups.intSum);
        //Ior.primary(11);
     *
     * }
     * </pre>
     *
     *
     *
     * @param iors Collection of Iors to accumulate primary values
     * @param reducer  Reducer to accumulate results
     * @return  Ior populated with the accumulate primary operation
     */
    public static <ST, PT> Ior<?, PT> accumulatePrimary(final CollectionX<Ior<ST, PT>> iors, final Semigroup<PT> reducer) {
        return sequencePrimary(iors).map(s -> s.reduce(reducer)
                                               .get());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Applicative#combine(com.aol.cyclops2.types.Value, java.util.function.BiFunction)
     */
    @Override
    default <T2, R> Ior<ST,R> combine(Value<? extends T2> app,
            BiFunction<? super PT, ? super T2, ? extends R> fn) {
        return (Ior<ST,R>)MonadicValue.super.combine(app, fn);
    }

    @Override
    default  Ior<ST,PT> zip(BinaryOperator<Zippable<PT>> combiner, Zippable<PT> app) {

        return (Ior<ST,PT>)MonadicValue.super.zip(combiner, app);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Filters#ofType(java.lang.Class)
     */
    @Override
    default <U> Ior<ST, U> ofType(final Class<? extends U> type) {

        return (Ior<ST, U>) MonadicValue.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Filters#filterNot(java.util.function.Predicate)
     */
    @Override
    default Ior<ST, PT> filterNot(final Predicate<? super PT> fn) {

        return (Ior<ST, PT>) MonadicValue.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Filters#notNull()
     */
    @Override
    default Ior<ST, PT> notNull() {

        return (Ior<ST, PT>) MonadicValue.super.notNull();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Transformable#cast(java.lang.Class)
     */
    @Override
    default <U> Ior<ST, U> cast(final Class<? extends U> type) {

        return (Ior<ST, U>) MonadicValue.super.cast(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Transformable#trampoline(java.util.function.Function)
     */
    @Override
    default <R> Ior<ST, R> trampoline(final Function<? super PT, ? extends Trampoline<? extends R>> mapper) {

        return (Ior<ST, R>) MonadicValue.super.trampoline(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.BiFunctor#bipeek(java.util.function.Consumer, java.util.function.Consumer)
     */
    @Override
    default Ior<ST, PT> bipeek(final Consumer<? super ST> c1, final Consumer<? super PT> c2) {

        return (Ior<ST, PT>) BiFunctor.super.bipeek(c1, c2);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.BiFunctor#bicast(java.lang.Class, java.lang.Class)
     */
    @Override
    default <U1, U2> Ior<U1, U2> bicast(final Class<U1> type1, final Class<U2> type2) {

        return (Ior<U1, U2>) BiFunctor.super.bicast(type1, type2);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.BiFunctor#bitrampoline(java.util.function.Function, java.util.function.Function)
     */
    @Override
    default <R1, R2> Ior<R1, R2> bitrampoline(final Function<? super ST, ? extends Trampoline<? extends R1>> mapper1,
            final Function<? super PT, ? extends Trampoline<? extends R2>> mapper2) {

        return (Ior<R1, R2>) BiFunctor.super.bitrampoline(mapper1, mapper2);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.applicative.ApplicativeFunctor#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <T2, R> Ior<ST, R> zip(final Iterable<? extends T2> app, final BiFunction<? super PT, ? super T2, ? extends R> fn) {
        return map(v -> Tuple.tuple(v, Curry.curry2(fn)
                                            .apply(v))).flatMap(tuple -> Ior.fromIterable(app)
                                                                            .visit(i -> Ior.primary(tuple.v2.apply(i)), () -> Ior.secondary(null)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.applicative.ApplicativeFunctor#zip(java.util.function.BiFunction, org.reactivestreams.Publisher)
     */
    @Override
    default <T2, R> Ior<ST, R> zipP(final Publisher<? extends T2> app,final BiFunction<? super PT, ? super T2, ? extends R> fn) {
        return map(v -> Tuple.tuple(v, Curry.curry2(fn)
                                            .apply(v))).flatMap(tuple -> Xor.fromPublisher(app)
                                                                            .visit(i -> Xor.primary(tuple.v2.apply(i)), () -> Xor.secondary(null)));
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Zippable#zip(java.util.reactiveStream.Stream, java.util.function.BiFunction)
     */
    @Override
    default <U, R> Ior<ST, R> zipS(final Stream<? extends U> other, final BiFunction<? super PT, ? super U, ? extends R> zipper) {

        return (Ior<ST, R>) MonadicValue.super.zipS(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Zippable#zip(java.util.reactiveStream.Stream)
     */
    @Override
    default <U> Ior<ST, Tuple2<PT, U>> zipS(final Stream<? extends U> other) {

        return (Ior) MonadicValue.super.zipS(other);
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Zippable#zip(java.lang.Iterable)
     */
    @Override
    default <U> Ior<ST, Tuple2<PT, U>> zip(final Iterable<? extends U> other) {

        return (Ior) MonadicValue.super.zip(other);
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @EqualsAndHashCode(of = { "value" })
    public static class Primary<ST, PT> implements Ior<ST, PT> {
        private final PT value;

        @Override
        public Xor<ST, PT> toXor() {
            return Xor.primary(value);
        }

        @Override
        public Xor<ST, PT> toXorDropPrimary() {
            return Xor.primary(value);
        }

        @Override
        public Ior<ST, PT> secondaryToPrimayMap(final Function<? super ST, ? extends PT> fn) {
            return this;
        }

        @Override
        public <R> Ior<R, PT> secondaryMap(final Function<? super ST, ? extends R> fn) {
            return (Ior<R, PT>) this;
        }

        @Override
        public <R> Ior<ST, R> map(final Function<? super PT, ? extends R> fn) {
            return new Primary<ST, R>(
                    fn.apply(value));
        }

        @Override
        public Ior<ST, PT> secondaryPeek(final Consumer<? super ST> action) {
            return this;
        }

        @Override
        public <R> R visit(final Function<? super ST, ? extends R> secondary, final Function<? super PT, ? extends R> primary,
                           final BiFunction<? super ST, ? super PT, ? extends R> both) {
            return primary.apply(value);
        }

        @Override
        public Ior<ST, PT> peek(final Consumer<? super PT> action) {
            action.accept(value);
            return this;
        }

        @Override
        public Ior<ST, PT> filter(final Predicate<? super PT> test) {
            if (test.test(value))
                return this;
            return Ior.secondary(null);
        }

        @Override
        public <R1, R2> Ior<R1, R2> bimap(final Function<? super ST, ? extends R1> fn1, final Function<? super PT, ? extends R2> fn2) {
            return Ior.<R1, R2>primary(fn2.apply(value));
        }

        @Override
        public Ior<PT, ST> swap() {
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
        public <RT1> Ior<ST, RT1> flatMap(final Function<? super PT, ? extends MonadicValue<? extends RT1>> mapper) {
            return (Ior<ST, RT1>) mapper.apply(value)
                    .toIor();
        }

        @Override
        public <LT1> Ior<LT1, PT> secondaryFlatMap(final Function<? super ST, ? extends Ior<LT1, PT>> mapper) {
            return (Ior<LT1, PT>) this;
        }

        @Override
        public Ior<ST, PT> secondaryToPrimayFlatMap(final Function<? super ST, ? extends Ior<ST, PT>> fn) {
            return this;
        }

        @Override
        public Ior<ST, PT> bipeek(final Consumer<? super ST> stAction, final Consumer<? super PT> ptAction) {
            ptAction.accept(value);
            return this;
        }

        @Override
        public Optional<Tuple2<ST, PT>> both() {
            return Optional.empty();
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
        public boolean isBoth() {
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
            return "Ior.primary[" + value + "]";
        }

        /* (non-Javadoc)
         * @see com.aol.cyclops2.types.applicative.ApplicativeFunctor#ap(com.aol.cyclops2.types.Value, java.util.function.BiFunction)
         */
        @Override
        public <T2, R> Ior<ST, R> combine(final Value<? extends T2> app, final BiFunction<? super PT, ? super T2, ? extends R> fn) {

            return app.toXor()
                    .visit(s -> Ior.secondary(null), f -> Ior.primary(fn.apply(get(), app.get())));
        }

    }
        @AllArgsConstructor(access = AccessLevel.PRIVATE)
        @EqualsAndHashCode(of = {"value"})
        public static class Secondary<ST, PT> implements Ior<ST, PT> {
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
            public Xor<ST, PT> toXor() {
                return Xor.secondary(value);
            }

            @Override
            public Xor<ST, PT> toXorDropPrimary() {
                return Xor.secondary(value);
            }

            @Override
            public Ior<ST, PT> secondaryToPrimayMap(final Function<? super ST, ? extends PT> fn) {
                return new Primary<ST, PT>(
                        fn.apply(value));
            }

            @Override
            public <R> Ior<R, PT> secondaryMap(final Function<? super ST, ? extends R> fn) {
                return new Secondary<R, PT>(
                        fn.apply(value));
            }

            @Override
            public <R> Ior<ST, R> map(final Function<? super PT, ? extends R> fn) {
                return (Ior<ST, R>) this;
            }

            @Override
            public Ior<ST, PT> secondaryPeek(final Consumer<? super ST> action) {
                return secondaryMap((Function) FluentFunctions.expression(action));
            }

            @Override
            public Optional<Tuple2<ST, PT>> both() {
                return Optional.empty();
            }

            @Override
            public Ior<ST, PT> peek(final Consumer<? super PT> action) {
                return this;
            }

            @Override
            public Ior<ST, PT> filter(final Predicate<? super PT> test) {
                return this;
            }

            @Override
            public <R1, R2> Ior<R1, R2> bimap(final Function<? super ST, ? extends R1> fn1, final Function<? super PT, ? extends R2> fn2) {
                return Ior.<R1, R2>secondary(fn1.apply(value));
            }

            @Override
            public Ior<PT, ST> swap() {
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
            public <RT1> Ior<ST, RT1> flatMap(final Function<? super PT, ? extends MonadicValue<? extends RT1>> mapper) {
                return (Ior<ST, RT1>) this;
            }

            @Override
            public <LT1> Ior<LT1, PT> secondaryFlatMap(final Function<? super ST, ? extends Ior<LT1, PT>> mapper) {
                return mapper.apply(value);
            }

            @Override
            public Ior<ST, PT> secondaryToPrimayFlatMap(final Function<? super ST, ? extends Ior<ST, PT>> fn) {
                return fn.apply(value);
            }

            @Override
            public Ior<ST, PT> bipeek(final Consumer<? super ST> stAction, final Consumer<? super PT> ptAction) {
                stAction.accept(value);
                return this;
            }

            @Override
            public Value<ST> secondaryValue() {
                return Value.of(() -> value);
            }

            @Override
            public <R> R visit(final Function<? super ST, ? extends R> secondary, final Function<? super PT, ? extends R> primary,
                               final BiFunction<? super ST, ? super PT, ? extends R> both) {
                return swap().visit(secondary, () -> null);
            }

            @Override
            public boolean isBoth() {
                return false;
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
            public String toString() {
                return mkString();
            }

            @Override
            public String mkString() {
                return "Ior.secondary[" + value + "]";
            }


            /* (non-Javadoc)
             * @see com.aol.cyclops2.types.applicative.ApplicativeFunctor#ap(com.aol.cyclops2.types.Value, java.util.function.BiFunction)
             */
            @Override
            public <T2, R> Ior<ST, R> combine(final Value<? extends T2> app, final BiFunction<? super PT, ? super T2, ? extends R> fn) {
                return (Ior<ST, R>) this;
            }
        }

        @AllArgsConstructor(access = AccessLevel.PACKAGE)
        @EqualsAndHashCode(of = {"secondary", "primary"})
        public static class Both<ST, PT> implements Ior<ST, PT> {
            private final Ior<ST, PT> secondary;
            private final Ior<ST, PT> primary;

            private static <ST, PT> Ior<ST, PT> both(final Ior<ST, PT> secondary, final Ior<ST, PT> primary) {
                return new Both<ST, PT>(
                        secondary, primary);
            }

            @Override
            public ReactiveSeq<PT> stream() {
                return primary.stream();
            }

            @Override
            public Iterator<PT> iterator() {
                return primary.iterator();
            }

            @Override
            public Xor<ST, PT> toXor() {
                return primary.toXor();
            }

            @Override
            public Xor<ST, PT> toXorDropPrimary() {
                return secondary.toXor();
            }

            @Override
            public Ior<ST, PT> secondaryToPrimayMap(final Function<? super ST, ? extends PT> fn) {
                return this;
            }

            @Override
            public <R> Ior<R, PT> secondaryMap(final Function<? super ST, ? extends R> fn) {
                return Both.both(secondary.secondaryMap(fn), primary.secondaryMap(fn));
            }

            @Override
            public <R> Ior<ST, R> map(final Function<? super PT, ? extends R> fn) {
                return Both.<ST, R>both(secondary.map(fn), primary.map(fn));
            }

            @Override
            public Ior<ST, PT> secondaryPeek(final Consumer<? super ST> action) {
                secondary.secondaryPeek(action);
                return this;
            }

            @Override
            public Ior<ST, PT> peek(final Consumer<? super PT> action) {
                primary.peek(action);
                return this;
            }

            @Override
            public Ior<ST, PT> filter(final Predicate<? super PT> test) {
                return primary.filter(test);
            }

            @Override
            public Ior<PT, ST> swap() {
                return Both.both(primary.swap(), secondary.swap());

            }

            @Override
            public <R> R visit(final Function<? super ST, ? extends R> secondary, final Function<? super PT, ? extends R> primary,
                               final BiFunction<? super ST, ? super PT, ? extends R> both) {
                return both().get()
                        .map((a, b) -> both.apply(a, b));
            }

            @Override
            public Optional<Tuple2<ST, PT>> both() {
                return Optional.of(Tuple.tuple(secondary.secondaryGet(), primary.get()));
            }

            @Override
            public PT get() {
                return primary.get();
            }

            @Override
            public Value<ST> secondaryValue() {
                return secondary.secondaryValue();
            }

            @Override
            public ST secondaryGet() {
                return secondary.secondaryGet();
            }

            @Override
            public Optional<ST> secondaryToOptional() {
                return secondary.secondaryToOptional();
            }

            @Override
            public ReactiveSeq<ST> secondaryToStream() {
                return secondary.secondaryToStream();
            }

            @Override
            public <RT1> Ior<ST, RT1> flatMap(final Function<? super PT, ? extends MonadicValue<? extends RT1>> mapper) {
                return Both.both((Ior<ST, RT1>) secondary, primary.flatMap(mapper));
            }

            @Override
            public <LT1> Ior<LT1, PT> secondaryFlatMap(final Function<? super ST, ? extends Ior<LT1, PT>> mapper) {
                return Both.both(secondary.secondaryFlatMap(mapper), primary.secondaryFlatMap(mapper));
            }

            @Override
            public Ior<ST, PT> secondaryToPrimayFlatMap(final Function<? super ST, ? extends Ior<ST, PT>> fn) {
                return Both.both(secondary.secondaryToPrimayFlatMap(fn), primary.secondaryToPrimayFlatMap(fn));
            }

            @Override
            public Ior<ST, PT> bipeek(final Consumer<? super ST> stAction, final Consumer<? super PT> ptAction) {
                secondary.secondaryPeek(stAction);
                primary.peek(ptAction);
                return this;
            }

            @Override
            public <R1, R2> Ior<R1, R2> bimap(final Function<? super ST, ? extends R1> fn1, final Function<? super PT, ? extends R2> fn2) {
                return Both.both((Ior) secondary.secondaryMap(fn1), (Ior) primary.map(fn2));
            }

            @Override
            public boolean isPrimary() {

                return false;
            }

            @Override
            public boolean isSecondary() {

                return false;
            }

            @Override
            public boolean isBoth() {
                return true;
            }

            @Override
            public String toString() {
                return mkString();
            }

            @Override
            public String mkString() {
                return "Ior.both[" + primary.toString() + ":" + secondary.toString() + "]";
            }


            /* (non-Javadoc)
             * @see com.aol.cyclops2.types.applicative.ApplicativeFunctor#ap(com.aol.cyclops2.types.Value, java.util.function.BiFunction)
             */
            @Override
            public <T2, R> Ior<ST, R> combine(final Value<? extends T2> app, final BiFunction<? super PT, ? super T2, ? extends R> fn) {
                return app.toXor()
                        .visit(s -> Ior.secondary(this.secondaryGet()), f -> Ior.both(this.secondaryGet(), fn.apply(get(), app.get())));
            }
        }

}