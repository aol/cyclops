package cyclops.control;

import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.hkt.Higher2;
import com.oath.cyclops.types.Filters;
import com.oath.cyclops.types.OrElseValue;
import com.oath.cyclops.types.Value;
import com.oath.cyclops.types.factory.Unit;
import com.oath.cyclops.types.foldable.To;
import com.oath.cyclops.types.functor.BiTransformable;
import com.oath.cyclops.types.functor.Transformable;
import com.oath.cyclops.types.reactive.ValueSubscriber;
import com.oath.cyclops.types.traversable.IterableX;
import cyclops.function.*;
import com.oath.cyclops.hkt.DataWitness.ior;
import cyclops.reactive.ReactiveSeq;
import cyclops.typeclasses.*;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functions.MonoidK;
import cyclops.typeclasses.functions.SemigroupKs;
import cyclops.typeclasses.functor.BiFunctor;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import org.reactivestreams.Publisher;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.*;

/**
 * Inclusive Or (can be one of Primary, Secondary or Both Primary and Secondary)
 *
 * An Either or Union type, but right biased. Primary and Secondary are used instead of Right & Left.
 * 'Right' (or right type) biased disjunct union.
 *  No 'projections' are provided, swap() and secondaryXXXX alternative methods can be used instead.
 *
 *
 *  For exclusive or @see Either
 *
 * @author johnmcclean
 *
 * @param <LT> Left type
 * @param <RT> Right type
 */
public interface Ior<LT, RT> extends To<Ior<LT, RT>>, Value<RT>,OrElseValue<RT,Ior<LT, RT>>,Unit<RT>, Transformable<RT>, Filters<RT>,  BiTransformable<LT, RT> ,Higher2<ior, LT, RT> {

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
    default <W1> Product<Higher<ior, LT>,W1, RT> product(Active<W1, RT> active){
        return Product.of(allTypeclasses(),active);
    }
    default <W1> Coproduct<W1,Higher<ior, LT>, RT> coproduct(InstanceDefinitions<W1> def2){
        return Coproduct.right(this,def2, Instances.definitions());
    }
    default Active<Higher<ior, LT>, RT> allTypeclasses(){
        return Active.of(this, Ior.Instances.definitions());
    }
    default <W2,R> Nested<Higher<ior, LT>,W2,R> mapM(Function<? super RT,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
        return Nested.of(map(fn), Instances.definitions(), defs);
    }

    Ior<LT,RT> recover(Supplier<? extends RT> value);
    Ior<LT,RT> recover(RT value);
    Ior<LT,RT> recoverWith(Supplier<? extends Ior<LT,RT>> fn);

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
     * Construct an Ior that contains a single value extracted from the supplied reactive-streams Publisher

     * <pre>
     * {@code
     *   ReactiveSeq<Integer> stream =  ReactiveSeq.of(1,2,3);

        Ior<Throwable,Integer> future = Ior.fromPublisher(stream);

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
        return sub.toEither()
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
    public static <ST, T> Ior<ST, T> fromIterable(final Iterable<T> iterable, T alt) {
        final Iterator<T> it = iterable.iterator();
        return Ior.right(it.hasNext() ? it.next() : alt);
    }

    /**
     * Create an instance of the right type. Most methods are biased to the right type,
     * which means, for example, that the transform method operates on the right type but does nothing on left Iors
     *
     * <pre>
     * {@code
     *   Ior.<Integer,Integer>right(10).map(i->i+1);
     * //Ior.right[11]
     *
     *
     * }
     * </pre>
     *
     *
     * @param right To construct an Ior from
     * @return Primary type instanceof Ior
     */
    public static <LT, RT> Ior<LT, RT> right(final RT right) {
        return new Primary<>(
                             right);
    }
    /**
     * Create an instance of the left type. Most methods are biased to the right type,
     * so you will need to use swap() or leftXXXX to manipulate the wrapped value
     *
     * <pre>
     * {@code
     *   Ior.<Integer,Integer>left(10).map(i->i+1);
     *   //Ior.left[10]
     *
     *    Ior.<Integer,Integer>left(10).swap().map(i->i+1);
     *    //Ior.right[11]
     * }
     * </pre>
     *
     *
     * @param left to wrap
     * @return Secondary instance of Ior
     */
    public static <LT, RT> Ior<LT, RT> left(final LT left) {
        return new Secondary<>(
                               left);
    }



    /**
     * Create an Ior instance that contains both left and right types
     *
     * <pre>
     * {@code
     *    Ior<String,Ingeger> kv = Ior.both("hello",90);
     *    //Ior["hello",90]
     * }
     * </pre>
     *
     * @param left Secondary value
     * @param right Primary value
     * @return Ior that contains both the left and the right value
     */
    public static <ST, PT> Ior<ST, PT> both(final ST left, final PT right) {
        return new Both<ST, PT>(
                               left, right);
    }




    default <T2, R1, R2, R3, R> Ior<LT,R> forEach4(Function<? super RT, ? extends Ior<LT,R1>> value1,
                                                   BiFunction<? super RT, ? super R1, ? extends Ior<LT,R2>> value2,
                                                   Function3<? super RT, ? super R1, ? super R2, ? extends Ior<LT,R3>> value3,
                                                   Function4<? super RT, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return this.flatMap(in-> {

            Ior<LT,R1> a = value1.apply(in);
            return a.flatMap(ina-> {
                Ior<LT,R2> b = value2.apply(in,ina);
                return b.flatMap(inb-> {
                    Ior<LT,R3> c= value3.apply(in,ina,inb);
                    return c.map(in2->yieldingFunction.apply(in,ina,inb,in2));
                });

            });

        });
    }

    default <T2, R1, R2, R> Ior<LT,R> forEach3(Function<? super RT, ? extends Ior<LT,R1>> value1,
                                               BiFunction<? super RT, ? super R1, ? extends Ior<LT,R2>> value2,
                                               Function3<? super RT, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return this.flatMap(in-> {

            Ior<LT,R1> a = value1.apply(in);
            return a.flatMap(ina-> {
                Ior<LT,R2> b = value2.apply(in,ina);
                return b.map(in2->yieldingFunction.apply(in,ina, in2));
            });

        });
    }


    default <R1, R> Ior<LT,R> forEach2(Function<? super RT, ? extends Ior<LT,R1>> value1,
                                       BiFunction<? super RT, ? super R1, ? extends R> yieldingFunction) {

        return this.flatMap(in-> {
            Ior<LT,R1> b = value1.apply(in);
            return b.map(in2->yieldingFunction.apply(in, in2));
        });
    }




    /* (non-Javadoc)
     * @see com.oath.cyclops.types.MonadicValue#unit(java.lang.Object)
     */
    @Override
    default <T> Ior<LT, T> unit(final T unit) {
        return Ior.right(unit);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.Filters#filter(java.util.function.Predicate)
     */
    @Override
    Option<RT> filter(Predicate<? super RT> test);

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.Value#toLazyEither()
     */
    Either<LT, RT> toEither();

    /**
     * @return Convert to an Either, dropping the right type if this Ior contains both
     */
    Either<LT, RT> toEitherDropRight(); //drop PT

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.Value#toLazyEither(java.lang.Object)
     */
    @Override
    default <ST2> Either<ST2, RT> toEither(final ST2 secondary) {
        return visit(s -> Either.left(secondary), p -> Either.right(p), (s, p) -> Either.right(p));
    }






    <R> Ior<R, RT> mapLeft(Function<? super LT, ? extends R> fn);


    @Override
    <R> Ior<LT, R> map(Function<? super RT, ? extends R> fn);


    Ior<LT, RT> peekLeft(Consumer<? super LT> action);

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.functor.Transformable#peek(java.util.function.Consumer)
     */
    @Override
    Ior<LT, RT> peek(Consumer<? super RT> action);

    /**
     * @return Ior with Primary and Secondary types and value swapped
     */
    Ior<RT, LT> swap();


    default <R> Ior<LT, R> coflatMap(final Function<? super Ior<LT, RT>, R> mapper) {
        return mapper.andThen(r -> unit(r))
                .apply(this);
    }

    //cojoin
    default Ior<LT, Ior<LT, RT>> nest() {
        return this.map(t -> unit(t));
    }


    /**
     * @return An zero Option if this Ior only has lazy the Secondary or Primary type. Or an Optional containing a Tuple2
     * with both the Secondary and Primary types if they are both present.
     */
    Option<Tuple2<LT, RT>> both();




    /* (non-Javadoc)
     * @see com.oath.cyclops.types.functor.BiTransformable#bimap(java.util.function.Function, java.util.function.Function)
     */
    @Override
     <R1, R2> Ior<R1, R2> bimap(final Function<? super LT, ? extends R1> fn1, final Function<? super RT, ? extends R2> fn2);

    /**
     * Visitor pattern for this Ior.
     * Execute the left function if this Ior contains an element of the left type only
     * Execute the right function if this Ior contains an element of the right type only
     * Execute the both function if this Ior contains an element of both type
     *
     * <pre>
     * {@code
     *  Ior.right(10)
     *     .visit(left->"no", right->"yes",(sec,pri)->"oops!")
     *  //Ior["yes"]

        Ior.left(90)
           .visit(left->"no", right->"yes",(sec,pri)->"oops!")
        //Ior["no"]

        Ior.both(10, "eek")
           .visit(left->"no", right->"yes",(sec,pri)->"oops!")
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
     <R> R visit(final Function<? super LT, ? extends R> secondary, final Function<? super RT, ? extends R> primary,
                 final BiFunction<? super LT, ? super RT, ? extends R> both) ;


    /* (non-Javadoc)
     * @see java.util.function.Supplier#getValue()
     */
    Option<RT> get();

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.foldable.Convertable#isPresent()
     */
    @Override
    default boolean isPresent() {
        return isRight() || isBoth();
    }



    Option<LT> getLeft();




    public <RT1> Ior<LT, RT1> flatMap(final Function<? super RT, ? extends Ior<? extends LT, ? extends RT1>> mapper);


    /**
     * Perform a flatMap operation on the Secondary type
     *
     * @param mapper Flattening transformation function
     * @return Ior containing the value inside the result of the transformation function as the Secondary value, if the Secondary type was present
     */
    <LT1> Ior<LT1, RT> flatMapLeft(Function<? super LT, ? extends Ior<LT1, RT>> mapper);


    /**
     * @return True if this is a right (only) Ior
     */
    public boolean isRight();

    /**
     * @return True if this was a left (only) Ior
     */
    public boolean isLeft();

    /**
     * @return True if this Ior has both left and right types
     */
    public boolean isBoth();

    /**
     *  Turn a toX of Iors into a single Ior with Lists of values.
     *  Primary and left types are swapped during this operation.
     *
     * <pre>
     * {@code
     *  Ior<String,Integer> just  = Ior.right(10);
        Ior<String,Integer> none = Ior.left("none");
     *  Ior<ListX<Integer>,ListX<String>> iors =Ior.sequenceLeft(ListX.of(just,none,Ior.right(1)));
        //Ior.right(ListX.of("none")))
     *
     * }
     * </pre>
     *
     *
     * @param iors Iors to sequence
     * @return Ior sequenced and swapped
     */
    public static <ST, PT> Ior<PT, ReactiveSeq<ST>> sequenceLeft(final IterableX<? extends Ior<ST, PT>> iors) {
        return sequence(iors.stream().filterNot(Ior::isRight).map(Ior::swap));

    }

    /**
     * Accumulate the result of the Secondary types in the Collection of Iors provided using the supplied Reducer  {@see cyclops2.Reducers}.
     *
     * <pre>
     * {@code
     *  Ior<String,Integer> just  = Ior.right(10);
        Ior<String,Integer> none = Ior.left("none");
     *  Ior<?,PersistentSetX<String>> iors = Ior.accumulateLeft(ListX.of(just,none,Ior.right(1)),Reducers.<String>toPersistentSetX());
      //Ior.right(PersistentSetX.of("none"))));
      * }
     * </pre>
     * @param iors Collection of Iors to accumulate left values
     * @param reducer Reducer to accumulate results
     * @return Ior populated with the accumulate left operation
     */
    public static <ST, PT, R> Ior<PT, R> accumulateLeft(final IterableX<Ior<ST, PT>> iors, final Reducer<R,ST> reducer) {
        return sequenceLeft(iors).map(s -> s.mapReduce(reducer));
    }

    /**
     * Accumulate the results only from those Iors which have a Secondary type present, using the supplied mapping function to
     * convert the data from each Ior before reducing them using the supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see cyclops2.Monoids }.
     *
     * <pre>
     * {@code
     *  Ior<String,Integer> just  = Ior.right(10);
        Ior<String,Integer> none = Ior.left("none");

     *  Ior<?,String> iors = Ior.accumulateLeft(ListX.of(just,none,Ior.left("1")),i->""+i,Monoids.stringConcat);
        //Ior.right("none1")
     *
     * }
     * </pre>
     *
     *
     *
     * @param iors Collection of Iors to accumulate left values
     * @param mapper Mapping function to be applied to the result of each Ior
     * @param reducer Semigroup to combine values from each Ior
     * @return Ior populated with the accumulate Secondary operation
     */
    public static <ST, PT, R> Ior<PT, R> accumulateLeft(final IterableX<Ior<ST, PT>> iors, final Function<? super ST, R> mapper,
                                                               final Monoid<R> reducer) {
        return sequenceLeft(iors).map(s -> s.map(mapper)
                                                 .reduce(reducer));
    }

    /**
     *  Accumulate the results only from those Iors which have a Secondary type present, using the supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see cyclops2.Monoids }.
     *
     * <pre>
     * {@code
     *
     *  Ior<String,Integer> just  = Ior.right(10);
        Ior<String,Integer> none = Ior.left("none");

     * Ior<?,Integer> iors = Ior.accumulateLeft(Monoids.intSum,ListX.of(Ior.both(2, "boo!"),Ior.left(1)));
       //Ior.right(3);  2+1
     *
     *
     * }
     * </pre>
     *
     *
     * @param iors Collection of Iors to accumulate left values
     * @param reducer  Semigroup to combine values from each Ior
     * @return populated with the accumulate Secondary operation
     */
    public static <ST, PT> Ior<PT, ST> accumulateLeft(final Monoid<ST> reducer, final IterableX<Ior<ST, PT>> iors) {
        return sequenceLeft(iors).map(s -> s.reduce(reducer));
    }

    /**
     *  Turn a toX of Iors into a single Ior with Lists of values.
     *
     * <pre>
     * {@code
     *
     * Ior<String,Integer> just  = Ior.right(10);
       Ior<String,Integer> none = Ior.left("none");


     * Ior<String,ReactiveSeq<Integer>> iors =Ior.sequenceRight(ListX.of(just,none,Ior.right(1)));
       //Ior.right(ListX.of(10,1)));
     *
     * }</pre>
     *
     *
     *
     * @param iors Iors to sequence
     * @return Ior Sequenced
     */
    public static <ST, PT> Ior<ST, ReactiveSeq<PT>> sequenceRight(final IterableX<Ior<ST, PT>> iors) {
        return sequence(iors.stream().filterNot(Ior::isLeft));
    }
  public static  <L,T> Ior<L,ReactiveSeq<T>> sequence(ReactiveSeq<? extends Ior<L,T>> stream) {

    Ior<L, ReactiveSeq<T>> identity = right(ReactiveSeq.empty());

    BiFunction<Ior<L,ReactiveSeq<T>>,Ior<L,T>,Ior<L,ReactiveSeq<T>>> combineToStream = (acc,next) ->acc.zip(next,(a,b)->a.appendAll(b));

    BinaryOperator<Ior<L,ReactiveSeq<T>>> combineStreams = (a,b)-> a.zip(b,(z1,z2)->z1.appendStream(z2));

    return stream.reduce(identity,combineToStream,combineStreams);
  }
  public static <L,T,R> Ior<L,ReactiveSeq<R>> traverse(Function<? super T,? extends R> fn,ReactiveSeq<Ior<L,T>> stream) {
    return sequence(stream.map(h->h.map(fn)));
  }

    /**
     * Accumulate the result of the Primary types in the Collection of Iors provided using the supplied Reducer  {@see cyclops2.Reducers}.

     * <pre>
     * {@code
     *  Ior<String,Integer> just  = Ior.right(10);
        Ior<String,Integer> none = Ior.left("none");
     *
     *  Ior<?,PersistentSetX<Integer>> iors =Ior.accumulateRight(ListX.of(just,none,Ior.right(1)),Reducers.toPersistentSetX());
        //Ior.right(PersistentSetX.of(10,1))));
     * }
     * </pre>
     * @param iors Collection of Iors to accumulate right values
     * @param reducer Reducer to accumulate results
     * @return Ior populated with the accumulate right operation
     */
    public static <ST, PT, R> Ior<ST, R> accumulateRight(final IterableX<Ior<ST, PT>> iors, final Reducer<R,PT> reducer) {
        return sequenceRight(iors).map(s -> s.mapReduce(reducer));
    }

    /**
     * Accumulate the results only from those Iors which have a Primary type present, using the supplied mapping function to
     * convert the data from each Ior before reducing them using the supplied Semgigroup (a combining BiFunction/BinaryOperator that takes two
     * input values of the same type and returns the combined result) {@see cyclops2.SemigroupK }.
     *
     * <pre>
     * {@code
     *  Ior<String,Integer> just  = Ior.right(10);
        Ior<String,Integer> none = Ior.left("none");

     * Ior<?,String> iors = Ior.accumulateRight(ListX.of(just,none,Ior.right(1)),i->""+i,SemigroupK.stringConcat);
       //Ior.right("101"));
     * }
     * </pre>
     *
     *
     * @param iors Collection of Iors to accumulate right values
     * @param mapper Mapping function to be applied to the result of each Ior
     * @param reducer Reducer to accumulate results
     * @return Ior populated with the accumulate right operation
     */
    public static <ST, PT, R> Ior<ST, R> accumulateRight(final IterableX<Ior<ST, PT>> iors, final Function<? super PT, R> mapper,
                                                                final Semigroup<R> reducer) {
        return sequenceRight(iors).map(s -> s.map(mapper)
                                               .reduce(reducer)
                                               .get());
    }

    /**
     *  Accumulate the results only from those Iors which have a Primary type present, using the supplied  Semgigroup (a combining BiFunction/BinaryOperator that takes two
     * input values of the same type and returns the combined result) {@see cyclops2.SemigroupK }.
     *
     * <pre>
     * {@code
     *  Ior<String,Integer> just  = Ior.right(10);
        Ior<String,Integer> none = Ior.left("none");
     *
     *  Ior<?,Integer> iors =Ior.accumulateRight(ListX.of(just,none,Ior.right(1)),SemigroupK.intSum);
        //Ior.right(11);
     *
     * }
     * </pre>
     *
     *
     *
     * @param iors Collection of Iors to accumulate right values
     * @param reducer  Reducer to accumulate results
     * @return  Ior populated with the accumulate right operation
     */
    public static <ST, PT> Ior<ST, PT> accumulateRight(final IterableX<Ior<ST, PT>> iors, final Semigroup<PT> reducer) {
        return sequenceRight(iors).map(s -> s.reduce(reducer)
                                               .get());
    }




    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Filters#ofType(java.lang.Class)
     */
    @Override
    default <U> Option<U> ofType(final Class<? extends U> type) {

        return (Option<U>) Filters.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Filters#filterNot(java.util.function.Predicate)
     */
    @Override
    default Option<RT> filterNot(final Predicate<? super RT> fn) {

        return (Option<RT>) Filters.super.filterNot(fn);
    }


    @Override
    default Option<RT> notNull() {

        return (Option<RT>) Filters.super.notNull();
    }



    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Transformable#trampoline(java.util.function.Function)
     */
    @Override
    default <R> Ior<LT, R> trampoline(final Function<? super RT, ? extends Trampoline<? extends R>> mapper) {

        return (Ior<LT, R>) Transformable.super.trampoline(mapper);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.BiTransformable#bipeek(java.util.function.Consumer, java.util.function.Consumer)
     */
    @Override
    default Ior<LT, RT> bipeek(final Consumer<? super LT> c1, final Consumer<? super RT> c2) {

        return (Ior<LT, RT>) BiTransformable.super.bipeek(c1, c2);
    }


    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.BiTransformable#bitrampoline(java.util.function.Function, java.util.function.Function)
     */
    @Override
    default <R1, R2> Ior<R1, R2> bitrampoline(final Function<? super LT, ? extends Trampoline<? extends R1>> mapper1,
            final Function<? super RT, ? extends Trampoline<? extends R2>> mapper2) {

        return (Ior<R1, R2>) BiTransformable.super.bitrampoline(mapper1, mapper2);
    }

    default <T2, R> Ior<LT, R> zip(final Ior<LT,? extends T2> app, final BiFunction<? super RT, ? super T2, ? extends R> fn){
        return flatMap(t->app.map(t2->fn.apply(t,t2)));
    }
    default <T2, R> Ior<LT, R> zip(final Either<LT,? extends T2> app, final BiFunction<? super RT, ? super T2, ? extends R> fn){
        return flatMap(t->app.map(t2->fn.apply(t,t2)).toIor());
    }


    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @EqualsAndHashCode(of = { "value" })
    public static class Primary<ST, PT> implements Ior<ST, PT> {
        private final PT value;

        @Override
        public Either<ST, PT> toEither() {
            return Either.right(value);
        }

        @Override
        public Either<ST, PT> toEitherDropRight() {
            return Either.right(value);
        }


      @Override
      public Ior<ST, PT> recover(Supplier<? extends PT> value) {
        return this;
      }

      @Override
      public Ior<ST, PT> recover(PT value) {
        return this;
      }

      @Override
      public Ior<ST, PT> recoverWith(Supplier<? extends Ior<ST, PT>> fn) {
        return this;
      }

      @Override
        public <R> Ior<R, PT> mapLeft(final Function<? super ST, ? extends R> fn) {
            return (Ior<R, PT>) this;
        }

        @Override
        public <R> Ior<ST, R> map(final Function<? super PT, ? extends R> fn) {
            return new Primary<ST, R>(
                    fn.apply(value));
        }

        @Override
        public Ior<ST, PT> peekLeft(final Consumer<? super ST> action) {
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
            return Ior.<R1, R2>right(fn2.apply(value));
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
        public Option<ST> getLeft() {
            return Option.none();
        }



        @Override
        public <RT1> Ior<ST, RT1> flatMap(final Function<? super PT, ? extends Ior<? extends ST,? extends RT1>> mapper) {
            Ior<? extends ST, ? extends RT1> x = mapper.apply(value);
            return (Ior<ST,RT1>)x;
        }

        @Override
        public <LT1> Ior<LT1, PT> flatMapLeft(final Function<? super ST, ? extends Ior<LT1, PT>> mapper) {
            return (Ior<LT1, PT>) this;
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
        public boolean isRight() {
            return true;
        }

        @Override
        public boolean isLeft() {
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
            return "Ior.right[" + value + "]";
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
          public Ior<ST, PT> recover(Supplier<? extends PT> value) {
            return Ior.both(this.value,value.get());
          }

          @Override
          public Ior<ST, PT> recover(PT value) {
            return Ior.both(this.value,value);
          }

          @Override
          public Ior<ST, PT> recoverWith(Supplier<? extends Ior<ST, PT>> fn) {
            return fn.get();
          }

          @Override
            public boolean isLeft() {
                return true;
            }

            @Override
            public boolean isRight() {
                return false;
            }

            @Override
            public Either<ST, PT> toEither() {
                return Either.left(value);
            }

            @Override
            public Either<ST, PT> toEitherDropRight() {
                return Either.left(value);
            }


            @Override
            public <R> Ior<R, PT> mapLeft(final Function<? super ST, ? extends R> fn) {
                return new Secondary<R, PT>(
                        fn.apply(value));
            }

            @Override
            public <R> Ior<ST, R> map(final Function<? super PT, ? extends R> fn) {
                return (Ior<ST, R>) this;
            }

            @Override
            public Ior<ST, PT> peekLeft(final Consumer<? super ST> action) {
                return mapLeft((Function) FluentFunctions.expression(action));
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
                return Ior.<R1, R2>left(fn1.apply(value));
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
            public Option<ST> getLeft() {
                return Option.some(value);
            }



            @Override
            public <RT1> Ior<ST, RT1> flatMap(final Function<? super PT, ? extends Ior<? extends ST,? extends RT1>> mapper) {
                return (Ior<ST, RT1>) this;
            }

            @Override
            public <LT1> Ior<LT1, PT> flatMapLeft(final Function<? super ST, ? extends Ior<LT1, PT>> mapper) {
                return mapper.apply(value);
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
                return "Ior.left[" + value + "]";
            }




            @Override
            public <R> R visit(Function<? super PT, ? extends R> present, Supplier<? extends R> absent) {
                return absent.get();
            }
        }

        @AllArgsConstructor(access = AccessLevel.PACKAGE)
        @EqualsAndHashCode(of = {"left", "right"})
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
          public Ior<ST, PT> recover(Supplier<? extends PT> value) {
            return this;
          }

          @Override
          public Ior<ST, PT> recover(PT value) {
            return this;
          }

          @Override
          public Ior<ST, PT> recoverWith(Supplier<? extends Ior<ST, PT>> fn) {
            return this;
          }


            @Override
            public Iterator<PT> iterator() {
                return stream().iterator();
            }

            @Override
            public Either<ST, PT> toEither() {
                return Either.right(primary);
            }

            @Override
            public Either<ST, PT> toEitherDropRight() {
                return Either.left(secondary);
            }

            @Override
            public <R> Ior<R, PT> mapLeft(final Function<? super ST, ? extends R> fn) {
                return Both.both(fn.apply(secondary), primary);
            }

            @Override
            public <R> Ior<ST, R> map(final Function<? super PT, ? extends R> fn) {
                return Both.<ST, R>both(secondary, fn.apply(primary));
            }

            @Override
            public Ior<ST, PT> peekLeft(final Consumer<? super ST> action) {
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
                return Either.right(primary).filter(test);
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
            public Option<ST> getLeft() {
                return Option.of(secondary);
            }



            @Override
            public <RT1> Ior<ST, RT1> flatMap(final Function<? super PT, ? extends Ior<? extends ST,? extends RT1>> mapper) {
                Ior<? extends ST, ? extends RT1> x = mapper.apply(primary);
                return (Ior<ST,RT1>)x;
            }

            @Override
            public <LT1> Ior<LT1, PT> flatMapLeft(final Function<? super ST, ? extends Ior<LT1, PT>> mapper) {
                return mapper.apply(secondary);

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
            public boolean isRight() {

                return false;
            }

            @Override
            public boolean isLeft() {

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
                public <T, R> Option<MonadZero<Higher<ior, L>>> monadZero() {
                    return Option.some(Instances.monadZero());
                }

                @Override
                public <T> Option<MonadPlus<Higher<ior, L>>> monadPlus() {
                    return Option.some(Instances.monadPlus());
                }

                @Override
                public <T> MonadRec<Higher<ior, L>> monadRec() {
                    return Instances.monadRec();
                }

                @Override
                public <T> Option<MonadPlus<Higher<ior, L>>> monadPlus(MonoidK<Higher<ior, L>> m) {
                    return Option.some(Instances.monadPlus(m));
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
                public <T> Option<Comonad<Higher<ior, L>>> comonad() {
                    return Maybe.nothing();
                }

                @Override
                public <T> Option<Unfoldable<Higher<ior, L>>> unfoldable() {
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
                    return Ior.right(value);
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

                    return maybe.visit(left->  applicative.unit(Ior.<L,R>left(left)),
                            right->applicative.map(m->Ior.right(m), fn.apply(right)),
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
                    return Ior.left(null);
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
                public <T, R> Higher<Higher<ior, X>, R> tailRec(T initial, Function<? super T, ? extends Higher<Higher<ior, X>, ? extends Either<T, R>>> fn) {
                    Ior<X,? extends Either<T, R>> next[] = new Ior[1];
                    next[0] = Ior.right(Either.left(initial));
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


          MonoidK<Higher<ior, L>> m = new MonoidK<Higher<ior, L>>() {
            @Override
            public <T> Higher<Higher<ior, L>, T> zero() {
              return Instances.<L>monadZero().zero();
            }

            @Override
            public <T> Higher<Higher<ior, L>, T> apply(Higher<Higher<ior, L>, T> t1, Higher<Higher<ior, L>, T> t2) {
              return SemigroupKs.<L>firstPrimaryIor().apply(t1,t2);
            }
          };
          return monadPlus(m);
        }
        public static <L,T> MonadPlus<Higher<ior, L>> monadPlus(MonoidK<Higher<ior, L>> m) {
            return new MonadPlus<Higher<ior, L>>() {

                @Override
                public MonoidK<Higher<ior, L>> monoid() {
                    return m;
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
