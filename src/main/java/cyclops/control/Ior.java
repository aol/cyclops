package cyclops.control;

import com.aol.cyclops2.data.collections.extensions.CollectionX;
import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.hkt.Higher2;
import com.aol.cyclops2.types.*;
import com.aol.cyclops2.types.anyM.AnyMValue;
import com.aol.cyclops2.types.factory.Unit;
import com.aol.cyclops2.types.foldable.To;
import com.aol.cyclops2.types.functor.BiTransformable;
import com.aol.cyclops2.types.functor.Transformable;
import com.aol.cyclops2.types.reactive.ValueSubscriber;
import cyclops.companion.Monoids;
import cyclops.collections.mutable.ListX;
import cyclops.control.lazy.Maybe;
import cyclops.control.lazy.Trampoline;
import cyclops.function.*;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import cyclops.monads.Witness.ior;
import cyclops.stream.ReactiveSeq;
import cyclops.typeclasses.*;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functor.BiFunctor;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import cyclops.collections.tuple.Tuple;
import cyclops.collections.tuple.Tuple2;
import org.reactivestreams.Publisher;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.*;

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
public interface Ior<ST, PT> extends To<Ior<ST, PT>>, Value<PT>,OrElseValue<PT,Ior<ST,PT>>,Unit<PT>, Transformable<PT>, Filters<PT>,  BiTransformable<ST, PT> ,Higher2<ior,ST,PT> {

    public static  <L,T> Kleisli<Higher<ior,L>,Ior<L,T>,T> kindKleisli(){
        return Kleisli.of(Instances.monad(), Ior::widen);
    }
    public static <L,T> Higher<Higher<ior,L>, T> widen(Ior<L,T> narrow) {
        return narrow;
    }
    public static  <L,T> Cokleisli<Higher<ior,L>,T,Ior<L,T>> kindCokleisli(){
        return Cokleisli.of(Ior::narrowK);
    }
    public static <W1,ST,PT> Nested<Higher<ior,ST>,W1,PT> nested(Ior<ST,Higher<W1,PT>> nested, InstanceDefinitions<W1> def2){
        return Nested.of(nested, Instances.definitions(),def2);
    }
    default <W1> Product<Higher<ior,ST>,W1,PT> product(Active<W1,PT> active){
        return Product.of(allTypeclasses(),active);
    }
    default <W1> Coproduct<W1,Higher<ior,ST>,PT> coproduct(InstanceDefinitions<W1> def2){
        return Coproduct.right(this,def2, Instances.definitions());
    }
    default Active<Higher<ior,ST>,PT> allTypeclasses(){
        return Active.of(this, Ior.Instances.definitions());
    }
    default <W2,R> Nested<Higher<ior,ST>,W2,R> mapM(Function<? super PT,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
        return Nested.of(map(fn), Instances.definitions(), defs);
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

    /**
     * Construct an Ior that contains a singleUnsafe value extracted from the supplied reactiveBuffer-streams Publisher

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
     * Construct an Ior that contains a singleUnsafe value extracted from the supplied Iterable
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
    public static <ST, T> Ior<ST, T> fromIterable(final Iterable<T> iterable, T alt) {
        final Iterator<T> it = iterable.iterator();
        return Ior.primary(it.hasNext() ? it.next() : alt);
    }

    /**
     * Create an instance of the primary type. Most methods are biased to the primary type,
     * which means, for example, that the transform method operates on the primary type but does nothing on secondary Iors
     *
     * <pre>
     * {@code
     *   Ior.<Integer,Integer>primary(10).transform(i->i+1);
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
     *   Ior.<Integer,Integer>secondary(10).transform(i->i+1);
     *   //Ior.secondary[10]
     *
     *    Ior.<Integer,Integer>secondary(10).swap().transform(i->i+1);
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
                               secondary, primary);
    }




    default <T2, R1, R2, R3, R> Ior<ST,R> forEach4(Function<? super PT, ? extends Ior<ST,R1>> value1,
            BiFunction<? super PT, ? super R1, ? extends Ior<ST,R2>> value2,
            Function3<? super PT, ? super R1, ? super R2, ? extends Ior<ST,R3>> value3,
            Function4<? super PT, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return this.flatMap(in-> {

            Ior<ST,R1> a = value1.apply(in);
            return a.flatMap(ina-> {
                Ior<ST,R2> b = value2.apply(in,ina);
                return b.flatMap(inb-> {
                    Ior<ST,R3> c= value3.apply(in,ina,inb);
                    return c.map(in2->yieldingFunction.apply(in,ina,inb,in2));
                });

            });

        });
    }

    default <T2, R1, R2, R> Ior<ST,R> forEach3(Function<? super PT, ? extends Ior<ST,R1>> value1,
            BiFunction<? super PT, ? super R1, ? extends Ior<ST,R2>> value2,
            Function3<? super PT, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return this.flatMap(in-> {

            Ior<ST,R1> a = value1.apply(in);
            return a.flatMap(ina-> {
                Ior<ST,R2> b = value2.apply(in,ina);
                return b.map(in2->yieldingFunction.apply(in,ina, in2));
            });

        });
    }


    default <R1, R> Ior<ST,R> forEach2(Function<? super PT, ? extends Ior<ST,R1>> value1,
            BiFunction<? super PT, ? super R1, ? extends R> yieldingFunction) {

        return this.flatMap(in-> {
            Ior<ST,R1> b = value1.apply(in);
            return b.map(in2->yieldingFunction.apply(in, in2));
        });
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#fromEither5()
     */
    default AnyMValue<ior,PT> anyM() {
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
    Option<PT> filter(Predicate<? super PT> test);

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Value#toXor()
     */
    Xor<ST, PT> toXor();

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



    /**
     * If this Ior contains the Secondary type only, transform it's value so that it contains the Primary type only
     * If this Ior contains both types, this method has no effect in the default implementations
     *
     * @param fn Function to transform secondary type to primary
     * @return Ior with secondary type mapped to primary
     */
    Ior<ST, PT> secondaryToPrimayMap(Function<? super ST, ? extends PT> fn);

    /**
     * Always transform the Secondary type of this Ior if it is present using the provided transformation function
     *
     * @param fn Transformation function for Secondary types
     * @return Ior with Secondary type transformed
     */
    <R> Ior<R, PT> secondaryMap(Function<? super ST, ? extends R> fn);

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#transform(java.util.function.Function)
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
     * @see com.aol.cyclops2.types.functor.Transformable#peek(java.util.function.Consumer)
     */
    @Override
    Ior<ST, PT> peek(Consumer<? super PT> action);

    /**
     * @return Ior with Primary and Secondary types and value swapped
     */
    Ior<PT, ST> swap();


    default <R> Ior<ST, R> coflatMap(final Function<? super Ior<ST,PT>, R> mapper) {
        return mapper.andThen(r -> unit(r))
                .apply(this);
    }

    //cojoin
    default Ior<ST, Ior<ST,PT>> nest() {
        return this.map(t -> unit(t));
    }


    /**
     * @return An zero Option if this Ior only has lazy the Secondary or Primary type. Or an Optional containing a Tuple2
     * with both the Secondary and Primary types if they are both present.
     */
    Option<Tuple2<ST, PT>> both();




    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.functor.BiTransformable#bimap(java.util.function.Function, java.util.function.Function)
     */
    @Override
     <R1, R2> Ior<R1, R2> bimap(final Function<? super ST, ? extends R1> fn1, final Function<? super PT, ? extends R2> fn2);

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
     <R> R visit(final Function<? super ST, ? extends R> secondary, final Function<? super PT, ? extends R> primary,
            final BiFunction<? super ST, ? super PT, ? extends R> both) ;


    /* (non-Javadoc)
     * @see java.util.function.Supplier#get()
     */
    Option<PT> get();

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.foldable.Convertable#isPresent()
     */
    @Override
    default boolean isPresent() {
        return isPrimary() || isBoth();
    }



    /**
     * @return The Secondary Value if present, otherwise null
     */
    Option<ST> secondaryGet();




    public <RT1> Ior<ST, RT1> flatMap(final Function<? super PT, ? extends Ior<? extends ST, ? extends RT1>> mapper);


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
     *  Turn a toX of Iors into a singleUnsafe Ior with Lists of values.
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
        return AnyM.sequence(iors.stream().filterNot(Ior::isPrimary).map(i->AnyM.fromIor(i.swap())).toListX(), ior.INSTANCE)
                   .to(Witness::ior);

    }

    /**
     * Accumulate the result of the Secondary types in the Collection of Iors provided using the supplied Reducer  {@see cyclops2.Reducers}.
     *
     * <pre>
     * {@code
     *  Ior<String,Integer> just  = Ior.primary(10);
        Ior<String,Integer> none = Ior.secondary("none");
     *  Ior<?,PersistentSetX<String>> iors = Ior.accumulateSecondary(ListX.of(just,none,Ior.primary(1)),Reducers.<String>toPersistentSetX());
      //Ior.primary(PersistentSetX.of("none"))));
      * }
     * </pre>
     * @param iors Collection of Iors to accumulate secondary values
     * @param reducer Reducer to accumulate results
     * @return Ior populated with the accumulate secondary operation
     */
    public static <ST, PT, R> Ior<ListX<PT>, R> accumulateSecondary(final CollectionX<Ior<ST, PT>> iors, final Reducer<R> reducer) {
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
    public static <ST, PT, R> Ior<ListX<PT>, R> accumulateSecondary(final CollectionX<Ior<ST, PT>> iors, final Function<? super ST, R> mapper,
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
    public static <ST, PT> Ior<ListX<PT>, ST> accumulateSecondary(final Monoid<ST> reducer,final CollectionX<Ior<ST, PT>> iors) {
        return sequenceSecondary(iors).map(s -> s.reduce(reducer));
    }

    /**
     *  Turn a toX of Iors into a singleUnsafe Ior with Lists of values.
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
        return AnyM.sequence(iors.stream().filterNot(Ior::isSecondary).map(AnyM::fromIor).toListX(), ior.INSTANCE)
                   .to(Witness::ior);
    }

    /**
     * Accumulate the result of the Primary types in the Collection of Iors provided using the supplied Reducer  {@see cyclops2.Reducers}.

     * <pre>
     * {@code
     *  Ior<String,Integer> just  = Ior.primary(10);
        Ior<String,Integer> none = Ior.secondary("none");
     *
     *  Ior<?,PersistentSetX<Integer>> iors =Ior.accumulatePrimary(ListX.of(just,none,Ior.primary(1)),Reducers.toPersistentSetX());
        //Ior.primary(PersistentSetX.of(10,1))));
     * }
     * </pre>
     * @param iors Collection of Iors to accumulate primary values
     * @param reducer Reducer to accumulate results
     * @return Ior populated with the accumulate primary operation
     */
    public static <ST, PT, R> Ior<ListX<ST>, R> accumulatePrimary(final CollectionX<Ior<ST, PT>> iors, final Reducer<R> reducer) {
        return sequencePrimary(iors).map(s -> s.mapReduce(reducer));
    }

    /**
     * Accumulate the results only from those Iors which have a Primary type present, using the supplied mapping function to
     * convert the data from each Ior before reducing them using the supplied Semgigroup (a combining BiFunction/BinaryOperator that takes two
     * input values of the same type and returns the combined result) {@see cyclops2.SemigroupK }.
     *
     * <pre>
     * {@code
     *  Ior<String,Integer> just  = Ior.primary(10);
        Ior<String,Integer> none = Ior.secondary("none");

     * Ior<?,String> iors = Ior.accumulatePrimary(ListX.of(just,none,Ior.primary(1)),i->""+i,SemigroupK.stringConcat);
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
    public static <ST, PT, R> Ior<ListX<ST>, R> accumulatePrimary(final CollectionX<Ior<ST, PT>> iors, final Function<? super PT, R> mapper,
            final Semigroup<R> reducer) {
        return sequencePrimary(iors).map(s -> s.map(mapper)
                                               .reduce(reducer)
                                               .get());
    }

    /**
     *  Accumulate the results only from those Iors which have a Primary type present, using the supplied  Semgigroup (a combining BiFunction/BinaryOperator that takes two
     * input values of the same type and returns the combined result) {@see cyclops2.SemigroupK }.
     *
     * <pre>
     * {@code
     *  Ior<String,Integer> just  = Ior.primary(10);
        Ior<String,Integer> none = Ior.secondary("none");
     *
     *  Ior<?,Integer> iors =Ior.accumulatePrimary(ListX.of(just,none,Ior.primary(1)),SemigroupK.intSum);
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
    public static <ST, PT> Ior<ListX<ST>, PT> accumulatePrimary(final CollectionX<Ior<ST, PT>> iors, final Semigroup<PT> reducer) {
        return sequencePrimary(iors).map(s -> s.reduce(reducer)
                                               .get());
    }




    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Filters#ofType(java.lang.Class)
     */
    @Override
    default <U> Option<U> ofType(final Class<? extends U> type) {

        return (Option<U>) Filters.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Filters#filterNot(java.util.function.Predicate)
     */
    @Override
    default Option< PT> filterNot(final Predicate<? super PT> fn) {

        return (Option<PT>) Filters.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Filters#notNull()
     */
    @Override
    default Option< PT> notNull() {

        return (Option<PT>) Filters.super.notNull();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Transformable#cast(java.lang.Class)
     */
    @Override
    default <U> Ior<ST, U> cast(final Class<? extends U> type) {

        return (Ior<ST, U>) Transformable.super.cast(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Transformable#trampoline(java.util.function.Function)
     */
    @Override
    default <R> Ior<ST, R> trampoline(final Function<? super PT, ? extends Trampoline<? extends R>> mapper) {

        return (Ior<ST, R>) Transformable.super.trampoline(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.BiTransformable#bipeek(java.util.function.Consumer, java.util.function.Consumer)
     */
    @Override
    default Ior<ST, PT> bipeek(final Consumer<? super ST> c1, final Consumer<? super PT> c2) {

        return (Ior<ST, PT>) BiTransformable.super.bipeek(c1, c2);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.BiTransformable#bicast(java.lang.Class, java.lang.Class)
     */
    @Override
    default <U1, U2> Ior<U1, U2> bicast(final Class<U1> type1, final Class<U2> type2) {

        return (Ior<U1, U2>) BiTransformable.super.bicast(type1, type2);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.BiTransformable#bitrampoline(java.util.function.Function, java.util.function.Function)
     */
    @Override
    default <R1, R2> Ior<R1, R2> bitrampoline(final Function<? super ST, ? extends Trampoline<? extends R1>> mapper1,
            final Function<? super PT, ? extends Trampoline<? extends R2>> mapper2) {

        return (Ior<R1, R2>) BiTransformable.super.bitrampoline(mapper1, mapper2);
    }

    default <T2, R> Ior<ST, R> zip(final Ior<ST,? extends T2> app, final BiFunction<? super PT, ? super T2, ? extends R> fn){
        return flatMap(t->app.map(t2->fn.apply(t,t2)));
    }
    default <T2, R> Ior<ST, R> zip(final Xor<ST,? extends T2> app, final BiFunction<? super PT, ? super T2, ? extends R> fn){
        return flatMap(t->app.map(t2->fn.apply(t,t2)).toIor());
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
        public Option<PT> filter(final Predicate<? super PT> test) {
            if (test.test(value))
                return Option.some(value);
            return Option.none();
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
        public Option<PT> get() {
            return Option.some(value);
        }

        @Override
        public Option<ST> secondaryGet() {
            return Option.none();
        }



        @Override
        public <RT1> Ior<ST, RT1> flatMap(final Function<? super PT, ? extends Ior<? extends ST,? extends RT1>> mapper) {
            Ior<? extends ST, ? extends RT1> x = mapper.apply(value);
            return (Ior<ST,RT1>)x;
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
        public Option<Tuple2<ST, PT>> both() {
            return Option.none();
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
        public String toString() {
            return mkString();
        }

        @Override
        public String mkString() {
            return "Ior.primary[" + value + "]";
        }


        @Override
        public <R> R visit(Function<? super PT, ? extends R> present, Supplier<? extends R> absent) {
            return present.apply(value);
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
            public Option<Tuple2<ST, PT>> both() {
                return Option.none();
            }

            @Override
            public Ior<ST, PT> peek(final Consumer<? super PT> action) {
                return this;
            }

            @Override
            public Option<PT> filter(final Predicate<? super PT> test) {
                return Option.none();
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
            public Option<PT> get() {
                return Option.none();
            }

            @Override
            public Option<ST> secondaryGet() {
                return Option.some(value);
            }



            @Override
            public <RT1> Ior<ST, RT1> flatMap(final Function<? super PT, ? extends Ior<? extends ST,? extends RT1>> mapper) {
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
                return Maybe.nothing();
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




            @Override
            public <R> R visit(Function<? super PT, ? extends R> present, Supplier<? extends R> absent) {
                return absent.get();
            }
        }

        @AllArgsConstructor(access = AccessLevel.PACKAGE)
        @EqualsAndHashCode(of = {"secondary", "primary"})
        public static class Both<ST, PT> implements Ior<ST, PT> {
            private final ST secondary;
            private final PT primary;

            private static <ST, PT> Ior<ST, PT> both(final ST secondary, final PT primary) {
                return new Both<ST, PT>(
                        secondary, primary);
            }

            @Override
            public ReactiveSeq<PT> stream() {
                return ReactiveSeq.of(primary);
            }

            @Override
            public Iterator<PT> iterator() {
                return stream().iterator();
            }

            @Override
            public Xor<ST, PT> toXor() {
                return Xor.primary(primary);
            }

            @Override
            public Xor<ST, PT> toXorDropPrimary() {
                return Xor.secondary(secondary);
            }

            @Override
            public Ior<ST, PT> secondaryToPrimayMap(final Function<? super ST, ? extends PT> fn) {
                return this;
            }

            @Override
            public <R> Ior<R, PT> secondaryMap(final Function<? super ST, ? extends R> fn) {
                return Both.both(fn.apply(secondary), primary);
            }

            @Override
            public <R> Ior<ST, R> map(final Function<? super PT, ? extends R> fn) {
                return Both.<ST, R>both(secondary, fn.apply(primary));
            }

            @Override
            public Ior<ST, PT> secondaryPeek(final Consumer<? super ST> action) {
                action.accept(secondary);
                return this;
            }

            @Override
            public Ior<ST, PT> peek(final Consumer<? super PT> action) {
                action.accept(primary);
                return this;
            }

            @Override
            public Option<PT> filter(final Predicate<? super PT> test) {
                return Xor.primary(primary).filter(test);
            }

            @Override
            public Ior<PT, ST> swap() {
                return Both.both(primary, secondary);

            }

            @Override
            public <R> R visit(final Function<? super ST, ? extends R> secondary, final Function<? super PT, ? extends R> primary,
                               final BiFunction<? super ST, ? super PT, ? extends R> both) {
                return both.apply(this.secondary,this.primary);
            }

            @Override
            public Option<Tuple2<ST, PT>> both() {
                return Option.some(Tuple.tuple(secondary, primary));
            }

            @Override
            public Option<PT> get() {
                return Option.of(primary);
            }


            @Override
            public Option<ST> secondaryGet() {
                return Option.of(secondary);
            }



            @Override
            public <RT1> Ior<ST, RT1> flatMap(final Function<? super PT, ? extends Ior<? extends ST,? extends RT1>> mapper) {
                Ior<? extends ST, ? extends RT1> x = mapper.apply(primary);
                return (Ior<ST,RT1>)x;
            }

            @Override
            public <LT1> Ior<LT1, PT> secondaryFlatMap(final Function<? super ST, ? extends Ior<LT1, PT>> mapper) {
                return mapper.apply(secondary);

            }

            @Override
            public Ior<ST, PT> secondaryToPrimayFlatMap(final Function<? super ST, ? extends Ior<ST, PT>> fn) {
                return fn.apply(secondary);
            }

            @Override
            public Ior<ST, PT> bipeek(final Consumer<? super ST> actionA, final Consumer<? super PT> actionB) {
                actionA.accept(secondary);
                actionB.accept(primary);
                return this;
            }

            @Override
            public <R1, R2> Ior<R1, R2> bimap(final Function<? super ST, ? extends R1> fn1, final Function<? super PT, ? extends R2> fn2) {
                return Both.both(fn1.apply(secondary), fn2.apply(primary));
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




            @Override
            public <R> R visit(Function<? super PT, ? extends R> present, Supplier<? extends R> absent) {
                return present.apply(primary);
            }
        }
    public static <ST,T> Ior<ST,T> narrowK2(final Higher2<ior, ST,T> ior) {
        return (Ior<ST,T>)ior;
    }
    public static <ST,T> Ior<ST,T> narrowK(final Higher<Higher<ior, ST>,T> ior) {
        return (Ior<ST,T>)ior;
    }
    public static class Instances {

        public static <L> InstanceDefinitions<Higher<ior, L>> definitions(){
            return new InstanceDefinitions<Higher<ior, L>>() {


                @Override
                public <T, R> Functor<Higher<ior, L>> functor() {
                    return Instances.functor();
                }

                @Override
                public <T> Pure<Higher<ior, L>> unit() {
                    return Instances.unit();
                }

                @Override
                public <T, R> Applicative<Higher<ior, L>> applicative() {
                    return Instances.applicative();
                }

                @Override
                public <T, R> Monad<Higher<ior, L>> monad() {
                    return Instances.monad();
                }

                @Override
                public <T, R> Maybe<MonadZero<Higher<ior, L>>> monadZero() {
                    return Maybe.just(Instances.monadZero());
                }

                @Override
                public <T> Maybe<MonadPlus<Higher<ior, L>>> monadPlus() {
                    return Maybe.just(Instances.monadPlus());
                }

                @Override
                public <T> MonadRec<Higher<ior, L>> monadRec() {
                    return Instances.monadRec();
                }

                @Override
                public <T> Maybe<MonadPlus<Higher<ior, L>>> monadPlus(Monoid<Higher<Higher<ior, L>, T>> m) {
                    return Maybe.just(Instances.monadPlus(m));
                }

                @Override
                public <C2, T> Traverse<Higher<ior, L>> traverse() {
                    return Instances.traverse();
                }

                @Override
                public <T> Foldable<Higher<ior, L>> foldable() {
                    return Instances.foldable();
                }

                @Override
                public <T> Maybe<Comonad<Higher<ior, L>>> comonad() {
                    return Maybe.nothing();
                }

                @Override
                public <T> Maybe<Unfoldable<Higher<ior, L>>> unfoldable() {
                    return Maybe.nothing();
                }
            };
        }
        public static <L> Functor<Higher<ior, L>> functor() {
            return new Functor<Higher<ior, L>>() {

                @Override
                public <T, R> Higher<Higher<ior, L>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<ior, L>, T> ds) {
                    Ior<L,T> ior = Ior.narrowK(ds);
                    return ior.map(fn);
                }
            };
        }
        public static <L> Pure<Higher<ior, L>> unit() {
            return new Pure<Higher<ior, L>>() {

                @Override
                public <T> Higher<Higher<ior, L>, T> unit(T value) {
                    return Ior.primary(value);
                }
            };
        }
        public static <L> Applicative<Higher<ior, L>> applicative() {
            return new Applicative<Higher<ior, L>>() {


                @Override
                public <T, R> Higher<Higher<ior, L>, R> ap(Higher<Higher<ior, L>, ? extends Function<T, R>> fn, Higher<Higher<ior, L>, T> apply) {
                    Ior<L,T>  ior = Ior.narrowK(apply);
                    Ior<L, ? extends Function<T, R>> iorFn = Ior.narrowK(fn);
                    return iorFn.zip(ior,(a,b)->a.apply(b));

                }

                @Override
                public <T, R> Higher<Higher<ior, L>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<ior, L>, T> ds) {
                    return Instances.<L>functor().map(fn,ds);
                }

                @Override
                public <T> Higher<Higher<ior, L>, T> unit(T value) {
                    return Instances.<L>unit().unit(value);
                }
            };
        }
        public static BiFunctor<ior> bifunctor(){
            return new BiFunctor<ior>() {
                @Override
                public <T, R, T2, R2> Higher2<ior, R, R2> bimap(Function<? super T, ? extends R> fn, Function<? super T2, ? extends R2> fn2, Higher2<ior, T, T2> ds) {
                    return narrowK(ds).bimap(fn,fn2);
                }
            };
        }
        public static <L> Monad<Higher<ior, L>> monad() {
            return new Monad<Higher<ior, L>>() {

                @Override
                public <T, R> Higher<Higher<ior, L>, R> flatMap(Function<? super T, ? extends Higher<Higher<ior, L>, R>> fn, Higher<Higher<ior, L>, T> ds) {
                    Ior<L,T> ior = Ior.narrowK(ds);
                    return ior.flatMap(fn.andThen(Ior::narrowK));
                }

                @Override
                public <T, R> Higher<Higher<ior, L>, R> ap(Higher<Higher<ior, L>, ? extends Function<T, R>> fn, Higher<Higher<ior, L>, T> apply) {
                    return Instances.<L>applicative().ap(fn,apply);

                }

                @Override
                public <T, R> Higher<Higher<ior, L>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<ior, L>, T> ds) {
                    return Instances.<L>functor().map(fn,ds);
                }

                @Override
                public <T> Higher<Higher<ior, L>, T> unit(T value) {
                    return Instances.<L>unit().unit(value);
                }
            };
        }
        public static <L> Traverse<Higher<ior, L>> traverse() {
            return new Traverse<Higher<ior, L>>() {

                @Override
                public <C2, T, R> Higher<C2, Higher<Higher<ior, L>, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn, Higher<Higher<ior, L>, T> ds) {
                    Ior<L,T> maybe = Ior.narrowK(ds);

                    return maybe.visit(left->  applicative.unit(Ior.<L,R>secondary(left)),
                            right->applicative.map(m->Ior.primary(m), fn.apply(right)),
                            (l,r)-> applicative.map(m->Ior.both(l,m), fn.apply(r)));
                }

                @Override
                public <C2, T> Higher<C2, Higher<Higher<ior, L>, T>> sequenceA(Applicative<C2> applicative, Higher<Higher<ior, L>, Higher<C2, T>> ds) {
                    return traverseA(applicative,Function.identity(),ds);
                }



                @Override
                public <T, R> Higher<Higher<ior, L>, R> ap(Higher<Higher<ior, L>, ? extends Function<T, R>> fn, Higher<Higher<ior, L>, T> apply) {
                    return Instances.<L>applicative().ap(fn,apply);

                }

                @Override
                public <T, R> Higher<Higher<ior, L>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<ior, L>, T> ds) {
                    return Instances.<L>functor().map(fn,ds);
                }

                @Override
                public <T> Higher<Higher<ior, L>, T> unit(T value) {
                    return Instances.<L>unit().unit(value);
                }
            };
        }
        public static <L> Foldable<Higher<ior, L>> foldable() {
            return new Foldable<Higher<ior, L>>() {


                @Override
                public <T> T foldRight(Monoid<T> monoid, Higher<Higher<ior, L>, T> ds) {
                    Ior<L,T> ior = Ior.narrowK(ds);
                    return ior.fold(monoid);
                }

                @Override
                public <T> T foldLeft(Monoid<T> monoid, Higher<Higher<ior, L>, T> ds) {
                    Ior<L,T> ior = Ior.narrowK(ds);
                    return ior.fold(monoid);
                }

                @Override
                public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<Higher<ior, L>, T> nestedA) {
                    return narrowK(nestedA).<R>map(fn).fold(mb);
                }
            };
        }
        public static <L> MonadZero<Higher<ior, L>> monadZero() {
            return new MonadZero<Higher<ior, L>>() {

                @Override
                public Higher<Higher<ior, L>, ?> zero() {
                    return Ior.secondary(null);
                }

                @Override
                public <T, R> Higher<Higher<ior, L>, R> flatMap(Function<? super T, ? extends Higher<Higher<ior, L>, R>> fn, Higher<Higher<ior, L>, T> ds) {
                    Ior<L,T> ior = Ior.narrowK(ds);
                    return ior.flatMap(fn.andThen(Ior::narrowK));
                }

                @Override
                public <T, R> Higher<Higher<ior, L>, R> ap(Higher<Higher<ior, L>, ? extends Function<T, R>> fn, Higher<Higher<ior, L>, T> apply) {
                    return Instances.<L>applicative().ap(fn,apply);

                }

                @Override
                public <T, R> Higher<Higher<ior, L>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<ior, L>, T> ds) {
                    return Instances.<L>functor().map(fn,ds);
                }

                @Override
                public <T> Higher<Higher<ior, L>, T> unit(T value) {
                    return Instances.<L>unit().unit(value);
                }
            };
        }
        public static <X,T,R> MonadRec<Higher<ior, X>> monadRec() {

            return new MonadRec<Higher<ior, X>>(){
                @Override
                public <T, R> Higher<Higher<ior, X>, R> tailRec(T initial, Function<? super T, ? extends Higher<Higher<ior, X>, ? extends Xor<T, R>>> fn) {
                    Ior<X,? extends Xor<T, R>> next[] = new Ior[1];
                    next[0] = Ior.primary(Xor.secondary(initial));
                    boolean cont = true;
                    do {
                        cont = next[0].visit(p -> p.visit(s -> {
                            next[0] = narrowK(fn.apply(s));
                            return true;
                        }, pr -> false), () -> false);
                    } while (cont);
                    return next[0].map(x->x.orElse(null));
                }


            };


        }
        public static <L> MonadPlus<Higher<ior, L>> monadPlus() {
            Monoid m = Monoids.firstPrimaryIor((Ior)Ior.narrowK(Instances.<L>monadZero().zero()));

            return monadPlus(m);
        }
        public static <L,T> MonadPlus<Higher<ior, L>> monadPlus(Monoid<Higher<Higher<ior, L>, T>> m) {
            return new MonadPlus<Higher<ior, L>>() {

                @Override
                public Monoid<Higher<Higher<ior, L>, ?>> monoid() {
                    return (Monoid)m;
                }

                @Override
                public Higher<Higher<ior, L>, ?> zero() {
                    return Instances.<L>monadZero().zero();
                }

                @Override
                public <T, R> Higher<Higher<ior, L>, R> flatMap(Function<? super T, ? extends Higher<Higher<ior, L>, R>> fn, Higher<Higher<ior, L>, T> ds) {
                    Ior<L,T> ior = Ior.narrowK(ds);
                    return ior.flatMap(fn.andThen(Ior::narrowK));
                }

                @Override
                public <T, R> Higher<Higher<ior, L>, R> ap(Higher<Higher<ior, L>, ? extends Function<T, R>> fn, Higher<Higher<ior, L>, T> apply) {
                    return Instances.<L>applicative().ap(fn,apply);

                }

                @Override
                public <T, R> Higher<Higher<ior, L>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<ior, L>, T> ds) {
                    return Instances.<L>functor().map(fn,ds);
                }

                @Override
                public <T> Higher<Higher<ior, L>, T> unit(T value) {
                    return Instances.<L>unit().unit(value);
                }
            };
        }

    }
}