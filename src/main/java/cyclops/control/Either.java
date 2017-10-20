package cyclops.control;

import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.hkt.Higher2;
import com.aol.cyclops2.matching.Sealed2;
import com.aol.cyclops2.types.factory.Unit;
import com.aol.cyclops2.types.foldable.To;
import com.aol.cyclops2.types.functor.BiTransformable;
import com.aol.cyclops2.types.functor.Transformable;
import cyclops.collectionx.immutable.LinkedListX;
import cyclops.companion.Monoids;
import cyclops.control.lazy.Eval;
import cyclops.control.lazy.Maybe;
import cyclops.control.lazy.Trampoline;
import cyclops.function.*;
import cyclops.companion.Semigroups;
import com.aol.cyclops2.data.collections.extensions.CollectionX;
import cyclops.collectionx.mutable.ListX;
import com.aol.cyclops2.types.*;
import com.aol.cyclops2.types.anyM.AnyMValue;
import cyclops.control.anym.Witness;
import com.aol.cyclops2.types.reactive.ValueSubscriber;
import cyclops.control.anym.AnyM;
import cyclops.control.anym.Witness.either;
import cyclops.control.anym.WitnessType;
import cyclops.control.anym.transformers.EitherT;
import cyclops.reactive.ReactiveSeq;
import cyclops.typeclasses.*;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functor.BiFunctor;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.reactivestreams.Publisher;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.*;

/**
 * eXclusive Or (Either)
 *
 * 'Right' (or lazyRight type) biased disjunct union. Often called Either, but in a generics heavy Java world Either is half the length of Either.
 *
 *  No 'projections' are provided, swap() and secondaryXXXX alternative methods can be used instead.
 *
 *  Either is used to represent values that can be one of two states (for example a validation result, lazy everything is ok - or we have an error).
 *  It can be used to avoid a common design anti-pattern where an Object has two fields one of which is always null (or worse, both are defined as Optionals).
 *
 *  <pre>
 *  {@code
 *
 *     public class Member{
 *           Either<SeniorTeam,JuniorTeam> team;
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
 *  Either's have two states
 *  Right : Most methods operate naturally on the lazyRight type, if it is present. If it is not, nothing happens.
 *  Left : Most methods do nothing to the lazyLeft type if it is present.
 *              To operate on the Left type first call swap() or use lazyLeft analogs of the main operators.
 *
 *  Instantiating an Either - Right
 *  <pre>
 *  {@code
 *      Either.lazyRight("hello").map(v->v+" world")
 *    //Either.lazyRight["hello world"]
 *  }
 *  </pre>
 *
 *  Instantiating an Either - Left
 *  <pre>
 *  {@code
 *      Either.lazyLeft("hello").map(v->v+" world")
 *    //Either.seconary["hello"]
 *  }
 *  </pre>
 *
 *  Either can operate (via transform/flatMap) as a Functor / Monad and via combine as an ApplicativeFunctor
 *
 *   Values can be accumulated via
 *  <pre>
 *  {@code
 *  Either.accumulateLeft(ListX.of(Either.lazyLeft("failed1"),
                                                    Either.lazyLeft("failed2"),
                                                    Either.lazyRight("success")),
                                                    SemigroupK.stringConcat)
 *
 *  //failed1failed2
 *
 *   Either<String,String> fail1 = Either.lazyLeft("failed1");
     fail1.swap().combine((a,b)->a+b)
                 .combine(Either.lazyLeft("failed2").swap())
                 .combine(Either.<String,String>lazyRight("success").swap())
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
 * @param <ST> Left type
 * @param <PT> Right type
 */
public interface Either<ST, PT> extends To<Either<ST,PT>>,
                                     BiTransformable<ST,PT>,
                                     Sealed2<ST,PT>,Value<PT>,
                                     OrElseValue<PT,Either<ST,PT>>,
                                     Unit<PT>, Transformable<PT>, Filters<PT>,
                                     Higher2<either,ST,PT> {


    default Either<ST,PT> accumulate(Either<ST,PT> next, Semigroup<PT> sg){
        return flatMap(s1->next.map(s2->sg.apply(s1,s2)));
    }
    default Either<ST,PT> accumulateRight(Semigroup<PT> sg, Either<ST,PT>... values){
        Either<ST,PT> acc= this;
        for(Either<ST,PT> next : values){
            acc = acc.accumulateRight(sg,next);
        }
        return acc;
    }
    default Either<ST,PT> accumulate(Semigroup<ST> sg, Either<ST,PT> next){
        return flatMapLeft(s1->next.mapLeft(s2->sg.apply(s1,s2)));
    }
    default Either<ST,PT> accumulate(Semigroup<ST> sg, Either<ST,PT>... values){
        Either<ST,PT> acc= this;
        for(Either<ST,PT> next : values){
            acc = acc.accumulate(sg,next);
        }
        return acc;
    }

    public static  <L,T,R> Either<L,R> tailRec(T initial, Function<? super T, ? extends Either<L,? extends Either<T, R>>> fn){
        Either<L,? extends Either<T, R>> next[] = new Either[1];
        next[0] = Either.right(Either.left(initial));
        boolean cont = true;
        do {
            cont = next[0].visit(p -> p.visit(s -> {
                next[0] = narrowK(fn.apply(s));
                return true;
            }, pr -> false), () -> false);
        } while (cont);

        return next[0].map(x->x.visit(l->null,r->r));
    }
    public static  <L,T> Kleisli<Higher<either,L>,Either<L,T>,T> kindKleisli(){
        return Kleisli.of(Instances.monad(), Either::widen);
    }
    public static <L,T> Higher<Higher<either,L>, T> widen(Either<L,T> narrow) {
        return narrow;
    }
    public static  <L,T> Cokleisli<Higher<either,L>,T,Either<L,T>> kindCokleisli(){
        return Cokleisli.of(Either::narrowK);
    }
    public static <W1,ST,PT> Nested<Higher<either,ST>,W1,PT> nested(Either<ST,Higher<W1,PT>> nested, InstanceDefinitions<W1> def2){
        return Nested.of(nested, Instances.definitions(),def2);
    }

     default <W1> Product<Higher<either,ST>,W1,PT> product(Active<W1,PT> active){
        return Product.of(allTypeclasses(),active);
    }
    default <W1> Coproduct<W1,Higher<either,ST>,PT> coproduct(InstanceDefinitions<W1> def2){
        return Coproduct.right(this,def2, Instances.definitions());
    }
    default Active<Higher<either,ST>,PT> allTypeclasses(){
        return Active.of(this, Instances.definitions());
    }
    default <W2,R> Nested<Higher<either,ST>,W2,R> mapM(Function<? super PT,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
        return Nested.of(map(fn), Instances.definitions(), defs);
    }
    default <W extends WitnessType<W>> EitherT<W, ST,PT> liftM(W witness) {
        return EitherT.of(witness.adapter().unit(this));
    }

    default Eval<Either<ST, PT>> nestedEval(){
        return Eval.later(()->this);
    }
    /**
     * Static method useful as a method reference for fluent consumption of any value type stored in this Either
     * (will capture the lowest common type)
     *
     * <pre>
     * {@code
     *
     *   myEither.to(Either::consumeAny)
                 .accept(System.out::println);
     * }
     * </pre>
     *
     * @param either Either to consume value for
     * @return Consumer we can applyHKT to consume value
     */
    static <X, LT extends X, M extends X, RT extends X>  Consumer<Consumer<? super X>> consumeAny(Either<LT,RT> either){
        return in->visitAny(in,either);
    }

    static <X, LT extends X, M extends X, RT extends X,R>  Function<Function<? super X, R>,R> applyAny(Either<LT,RT> either){
        return in->visitAny(either,in);
    }

    static <X, PT extends X, ST extends X,R> R visitAny(Either<ST,PT> either, Function<? super X, ? extends R> fn){
        return either.visit(fn, fn);
    }

    static <X, LT extends X, RT extends X> X visitAny(Consumer<? super X> c,Either<LT,RT> either){
        Function<? super X, X> fn = x ->{
            c.accept(x);
            return x;
        };
        return visitAny(either,fn);
    }

    public static <ST,T> Either<ST,T> narrowK2(final Higher2<either, ST,T> xor) {
        return (Either<ST,T>)xor;
    }
    public static <ST,T> Either<ST,T> narrowK(final Higher<Higher<either, ST>,T> xor) {
        return (Either<ST,T>)xor;
    }
    /**
     * Construct a Right Either from the supplied publisher
     * <pre>
     * {@code
     *   ReactiveSeq<Integer> reactiveStream =  ReactiveSeq.of(1,2,3);

         Either<Throwable,Integer> future = Either.fromPublisher(reactiveStream);

         //Either[1]
     *
     * }
     * </pre>
     * @param pub Publisher to construct an Either from
     * @return Either constructed from the supplied Publisher
     */
    public static <T> Either<Throwable, T> fromPublisher(final Publisher<T> pub) {
        final ValueSubscriber<T> sub = ValueSubscriber.subscriber();
        pub.subscribe(sub);
        return sub.toLazyEither();
    }

    /**
     * Construct a Right Either from the supplied Iterable
     * <pre>
     * {@code
     *   List<Integer> list =  Arrays.asList(1,2,3);

         Either<Throwable,Integer> future = Either.fromPublisher(reactiveStream);

         //Either[1]
     *
     * }
     * </pre>
     * @param iterable Iterable to construct an Either from
     * @return Either constructed from the supplied Iterable
     */
    public static <ST, T> Either<ST, T> fromIterable(final Iterable<T> iterable) {

        final Iterator<T> it = iterable.iterator();
        return Either.right(it.hasNext() ? it.next() : null);
    }

    /**
     * Create an instance of the lazyLeft type. Most methods are biased to the lazyRight type,
     * so you will need to use swap() or secondaryXXXX to manipulate the wrapped value
     *
     * <pre>
     * {@code
     *   Either.<Integer,Integer>lazyLeft(10).map(i->i+1);
     *   //Either.lazyLeft[10]
     *
     *    Either.<Integer,Integer>lazyLeft(10).swap().map(i->i+1);
     *    //Either.lazyRight[11]
     * }
     * </pre>
     *
     *
     * @param value to wrap
     * @return Left instance of Either
     */
    public static <ST, PT> Either<ST, PT> left(final ST value) {
        return new Left<>(
                               value);
    }

    /**
     * Create an instance of the lazyRight type. Most methods are biased to the lazyRight type,
     * which means, for example, that the transform method operates on the lazyRight type but does nothing on lazyLeft Eithers
     *
     * <pre>
     * {@code
     *   Either.<Integer,Integer>lazyRight(10).map(i->i+1);
     *   //Either.lazyRight[11]
     *
     *
     * }
     * </pre>
     *
     *
     * @param value To construct an Either from
     * @return Right type instanceof Either
     */
    public static <ST, PT> Either<ST, PT> right(final PT value) {
        return new Right<>(
                             value);
    }






    default <T2, R1, R2, R3, R> Either<ST,R> forEach4(Function<? super PT, ? extends Either<ST,R1>> value1,
                                                      BiFunction<? super PT, ? super R1, ? extends Either<ST,R2>> value2,
                                                      Function3<? super PT, ? super R1, ? super R2, ? extends Either<ST,R3>> value3,
                                                      Function4<? super PT, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return this.flatMap(in-> {

            Either<ST,R1> a = value1.apply(in);
            return a.flatMap(ina-> {
                Either<ST,R2> b = value2.apply(in,ina);
                return b.flatMap(inb-> {
                    Either<ST,R3> c= value3.apply(in,ina,inb);
                    return c.map(in2->yieldingFunction.apply(in,ina,inb,in2));
                });

            });

        });
    }




    default <T2, R1, R2, R> Either<ST,R> forEach3(Function<? super PT, ? extends Either<ST,R1>> value1,
                                                  BiFunction<? super PT, ? super R1, ? extends Either<ST,R2>> value2,
                                                  Function3<? super PT, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return this.flatMap(in-> {

            Either<ST,R1> a = value1.apply(in);
            return a.flatMap(ina-> {
                Either<ST,R2> b = value2.apply(in,ina);
                return b.map(in2->yieldingFunction.apply(in,ina, in2));
            });

        });
    }





    default <R1, R> Either<ST,R> forEach2(Function<? super PT, ? extends Either<ST,R1>> value1,
                                          BiFunction<? super PT, ? super R1, ? extends R> yieldingFunction) {

        return this.flatMap(in-> {
            Either<ST,R1> b = value1.apply(in);
            return b.map(in2->yieldingFunction.apply(in, in2));
        });
    }





    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#fromEither5()
     */
    default AnyMValue<either,PT> anyM() {
        return AnyM.fromLazyEither(this);
    }







    //cojoin
    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#nest()
     */
    default Either<ST, Either<ST,PT>> nest() {
        return this.map(t -> unit(t));
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#unit(java.lang.Object)
     */
    @Override
    default <T> Either<ST, T> unit(final T unit) {
        return Either.right(unit);
    }


    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Filters#filter(java.util.function.Predicate)
     */
    @Override
    Option<PT> filter(Predicate<? super PT> test);

    /**
     * If this Either contains the Left type, transform it's value so that it contains the Right type
     *
     *
     * @param fn Function to transform lazyLeft type to lazyRight
     * @return Either with lazyLeft type mapped to lazyRight
     */
    Either<ST, PT> mapLeftToRight(Function<? super ST, ? extends PT> fn);

    /**
     * Always transform the Left type of this Either if it is present using the provided transformation function
     *
     * @param fn Transformation function for Left types
     * @return Either with Left type transformed
     */
    <R> Either<R, PT> mapLeft(Function<? super ST, ? extends R> fn);

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#transform(java.util.function.Function)
     */
    @Override
    <R> Either<ST, R> map(Function<? super PT, ? extends R> fn);

    /**
     * Peek at the Left type value if present
     *
     * @param action Consumer to peek at the Left type value
     * @return Either with the same values as before
     */
    Either<ST, PT> peekLeft(Consumer<? super ST> action);

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Functor#peek(java.util.function.Consumer)
     */
    @Override
    Either<ST, PT> peek(Consumer<? super PT> action);

    /**
     * Swap types so operations directly affect the current (pre-swap) Left type
     *<pre>
     *  {@code
     *
     *    Either.lazyLeft("hello")
     *       .map(v->v+" world")
     *    //Either.seconary["hello"]
     *
     *    Either.lazyLeft("hello")
     *       .swap()
     *       .map(v->v+" world")
     *       .swap()
     *    //Either.seconary["hello world"]
     *  }
     *  </pre>
     *
     *
     * @return Swap the lazyRight and lazyLeft types, allowing operations directly on what was the Left type
     */
    Either<PT, ST> swap();



    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.foldable.Convertable#isPresent()
     */
    @Override
    default boolean isPresent() {
        return isRight();
    }


    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Value#toLazyEither(java.lang.Object)
     */
    @Override
    default <ST2> Either<ST2, PT> toEither(final ST2 secondary) {
        return visit(s -> left(secondary), p -> right(p));
    }
    /**
     *  Turn a toX of Eithers into a singleUnsafe Either with Lists of values.
     *  Right and lazyLeft types are swapped during this operation.
     *
     * <pre>
     * {@code
     *  Either<String,Integer> just  = Either.lazyRight(10);
        Either<String,Integer> none = Either.lazyLeft("none");
     *  Either<ListX<Integer>,ListX<String>> xors =Either.sequenceLeft(ListX.of(just,none,Either.lazyRight(1)));
        //Either.lazyRight(ListX.of("none")))
     *
     * }
     * </pre>
     *
     *
     * @param xors Eithers to sequence
     * @return Either sequenced and swapped
     */
    public static <ST, PT> Either<ListX<PT>, ListX<ST>> sequenceLeft(final CollectionX<Either<ST, PT>> xors) {
        return AnyM.sequence(xors.stream().filter(Either::isLeft).map(i->AnyM.fromLazyEither(i.swap())).toListX(), either.INSTANCE)
                    .to(Witness::either);
    }
    /**
     * Accumulate the result of the Left types in the Collection of Eithers provided using the supplied Reducer  {@see cyclops2.Reducers}.
     *
     * <pre>
     * {@code
     *  Either<String,Integer> just  = Either.lazyRight(10);
        Either<String,Integer> none = Either.lazyLeft("none");

     *  Either<?,PersistentSetX<String>> xors = Either.accumulateLeft(ListX.of(just,none,Either.lazyRight(1)),Reducers.<String>toPersistentSetX());
      //Either.lazyRight(PersistentSetX.of("none"))));
      * }
     * </pre>
     * @param xors Collection of Iors to accumulate lazyLeft values
     * @param reducer Reducer to accumulate results
     * @return Either populated with the accumulate lazyLeft operation
     */
    public static <ST, PT, R> Either<ListX<PT>, R> accumulateLeft(final CollectionX<Either<ST, PT>> xors, final Reducer<R> reducer) {
        return sequenceLeft(xors).map(s -> s.mapReduce(reducer));
    }
    /**
     * Accumulate the results only from those Eithers which have a Left type present, using the supplied mapping function to
     * convert the data from each Either before reducing them using the supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see cyclops2.Monoids }..
     *
     * <pre>
     * {@code
     *  Either<String,Integer> just  = Either.lazyRight(10);
        Either<String,Integer> none = Either.lazyLeft("none");

     *  Either<?,String> xors = Either.accumulateLeft(ListX.of(just,none,Either.lazyLeft("1")),i->""+i,Monoids.stringConcat);

        //Either.lazyRight("none1")
     *
     * }
     * </pre>
     *
     *
     *
     * @param xors Collection of Iors to accumulate lazyLeft values
     * @param mapper Mapping function to be applied to the result of each Ior
     * @param reducer Semigroup to combine values from each Ior
     * @return Either populated with the accumulate Left operation
     */
    public static <ST, PT, R> Either<ListX<PT>, R> accumulateLeft(final CollectionX<Either<ST, PT>> xors, final Function<? super ST, R> mapper,
                                                                  final Monoid<R> reducer) {
        return sequenceLeft(xors).map(s -> s.map(mapper)
                                                 .reduce(reducer));
    }


    /**
     *  Turn a Collection of Eithers into a single Either with Lists of values.
     *
     * <pre>
     * {@code
     *
     * Either<String,Integer> just  = Either.lazyRight(10);
       Either<String,Integer> none = Either.lazyLeft("none");


     * Either<ListX<String>,ListX<Integer>> xors =Either.sequenceRight(ListX.of(just,none,Either.lazyRight(1)));
       //Either.lazyRight(ListX.of(10,1)));
     *
     * }</pre>
     *
     *
     *
     * @param iors Eithers to sequence
     * @return Either Sequenced
     */
    public static <ST, PT> Either<ListX<ST>, ListX<PT>> sequenceRight(final CollectionX<Either<ST, PT>> xors) {
        return AnyM.sequence(xors.stream().filter(Either::isRight).map(AnyM::fromLazyEither).toListX(), either.INSTANCE)
                    .to(Witness::either);
    }
    /**
     * Accumulate the result of the Right types in the Collection of Eithers provided using the supplied Reducer  {@see cyclops2.Reducers}.

     * <pre>
     * {@code
     *  Either<String,Integer> just  = Either.lazyRight(10);
        Either<String,Integer> none = Either.lazyLeft("none");

     *  Either<?,PersistentSetX<Integer>> xors =Either.accumulateRight(ListX.of(just,none,Either.lazyRight(1)),Reducers.toPersistentSetX());
        //Either.lazyRight(PersistentSetX.of(10,1))));
     * }
     * </pre>
     * @param Eithers Collection of Iors to accumulate lazyRight values
     * @param reducer Reducer to accumulate results
     * @return Either populated with the accumulate lazyRight operation
     */
    public static <ST, PT, R> Either<ListX<ST>, R> accumulateRight(final CollectionX<Either<ST, PT>> xors, final Reducer<R> reducer) {
        return sequenceRight(xors).map(s -> s.mapReduce(reducer));
    }

    /**
     * Accumulate the results only from those Iors which have a Right type present, using the supplied mapping function to
     * convert the data from each Either before reducing them using the supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see cyclops2.Monoids }..
     *
     * <pre>
     * {@code
     *  Either<String,Integer> just  = Either.lazyRight(10);
        Either<String,Integer> none = Either.lazyLeft("none");

     * Either<?,String> iors = Either.accumulateRight(ListX.of(just,none,Either.lazyRight(1)),i->""+i,Monoids.stringConcat);
       //Either.lazyRight("101"));
     * }
     * </pre>
     *
     *
     * @param xors Collection of Iors to accumulate lazyRight values
     * @param mapper Mapping function to be applied to the result of each Ior
     * @param reducer Reducer to accumulate results
     * @return Either populated with the accumulate lazyRight operation
     */
    public static <ST, PT, R> Either<ListX<ST>, R> accumulateRight(final CollectionX<Either<ST, PT>> xors, final Function<? super PT, R> mapper,
                                                                   final Monoid<R> reducer) {
        return sequenceRight(xors).map(s -> s.map(mapper)
                                               .reduce(reducer));
    }
    /**
     *  Accumulate the results only from those Eithers which have a Right type present, using the supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see cyclops2.Monoids }.
     *
     * <pre>
     * {@code
     *  Either<String,Integer> just  = Either.lazyRight(10);
        Either<String,Integer> none = Either.lazyLeft("none");
     *
     *  Either<?,Integer> xors XIor.accumulateRight(Monoids.intSum,ListX.of(just,none,Ior.lazyRight(1)));
        //Ior.lazyRight(11);
     *
     * }
     * </pre>
     *
     *
     *
     * @param xors Collection of Eithers to accumulate lazyRight values
     * @param reducer  Reducer to accumulate results
     * @return  Either populated with the accumulate lazyRight operation
     */
    public static <ST, PT> Either<ListX<ST>, PT> accumulateRight(final Monoid<PT> reducer, final CollectionX<Either<ST, PT>> xors) {
        return sequenceRight(xors).map(s -> s.reduce(reducer));
    }

    /**
     *
     * Accumulate the results only from those Eithers which have a Left type present, using the supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see cyclops2.Monoids }.
     * <pre>
     * {@code
     * Either.accumulateLeft(ListX.of(Either.lazyLeft("failed1"),
    												Either.lazyLeft("failed2"),
    												Either.lazyRight("success")),
    												SemigroupK.stringConcat)


     * //Eithers.Right[failed1failed2]
     * }
     * </pre>
     * <pre>
     * {@code
     *
     *  Either<String,Integer> just  = Either.lazyRight(10);
        Either<String,Integer> none = Either.lazyLeft("none");

     * Either<?,Integer> iors = Either.accumulateLeft(Monoids.intSum,ListX.of(Either.both(2, "boo!"),Either.lazyLeft(1)));
       //Either.lazyRight(3);  2+1
     *
     *
     * }
     * </pre>
     *
     * @param xors Collection of Eithers to accumulate lazyLeft values
     * @param reducer  Semigroup to combine values from each Either
     * @return Either populated with the accumulate Left operation
     */
    public static <ST, PT> Either<ListX<PT>, ST> accumulateLeft(final Monoid<ST> reducer, final CollectionX<Either<ST, PT>> xors) {
        return sequenceLeft(xors).map(s -> s.reduce(reducer));
    }

    /**
     * Visitor pattern for this Ior.
     * Execute the lazyLeft function if this Either contains an element of the lazyLeft type
     * Execute the lazyRight function if this Either contains an element of the lazyRight type
     *
     *
     * <pre>
     * {@code
     *  Either.lazyRight(10)
     *     .visit(lazyLeft->"no", lazyRight->"yes")
     *  //Either["yes"]

        Either.lazyLeft(90)
           .visit(lazyLeft->"no", lazyRight->"yes")
        //Either["no"]


     *
     * }
     * </pre>
     *
     * @param secondary Function to execute if this is a Left Either
     * @param primary Function to execute if this is a Right Ior
     * @param both Function to execute if this Ior contains both types
     * @return Result of executing the appropriate function
     */
    <R> R visit(Function<? super ST, ? extends R> secondary, Function<? super PT, ? extends R> primary);


    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.functor.BiTransformable#bimap(java.util.function.Function, java.util.function.Function)
     */
    @Override
    default <R1, R2> Either<R1, R2> bimap(Function<? super ST, ? extends R1> secondary, Function<? super PT, ? extends R2> primary) {
        if (isLeft())
            return (Either<R1, R2>) swap().map(secondary)
                                       .swap();
        return (Either<R1, R2>) map(primary);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.functor.BiTransformable#bipeek(java.util.function.Consumer, java.util.function.Consumer)
     */
    @Override
    default Either<ST, PT> bipeek(Consumer<? super ST> c1, Consumer<? super PT> c2) {

        return (Either<ST, PT>)BiTransformable.super.bipeek(c1, c2);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.functor.BiTransformable#bicast(java.lang.Class, java.lang.Class)
     */
    @Override
    default <U1, U2> Either<U1, U2> bicast(Class<U1> type1, Class<U2> type2) {

        return (Either<U1, U2>)BiTransformable.super.bicast(type1, type2);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.functor.BiTransformable#bitrampoline(java.util.function.Function, java.util.function.Function)
     */
    @Override
    default <R1, R2> Either<R1, R2> bitrampoline(Function<? super ST, ? extends Trampoline<? extends R1>> mapper1,
                                                 Function<? super PT, ? extends Trampoline<? extends R2>> mapper2) {

        return  (Either<R1, R2>)BiTransformable.super.bitrampoline(mapper1, mapper2);
    }



    /* (non-Javadoc)
     * @see java.util.function.Supplier#getValue()
     */
    Option<PT> get();

    /**
     * @return The Left Value if present, otherwise null
     */
    Option<ST> getLeft();
    ST leftOrElse(ST alt);

    /**
     * @return A Stream containing the lazyLeft value if present, otherwise an zero Stream
     */
    ReactiveSeq<ST> leftToStream();

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#flatMap(java.util.function.Function)
     */

    <RT1> Either<ST, RT1> flatMap(Function<? super PT, ? extends Either<? extends ST,? extends RT1>> mapper);
    /**
     * Perform a flatMap operation on the Left type
     *
     * @param mapper Flattening transformation function
     * @return Either containing the value inside the result of the transformation function as the Left value, if the Left type was present
     */
    <LT1> Either<LT1, PT> flatMapLeft(Function<? super ST, ? extends Either<LT1, PT>> mapper);
    /**
     * A flatMap operation that keeps the Left and Right types the same
     *
     * @param fn Transformation function
     * @return Either
     */
    Either<ST, PT> flatMapLeftToRight(Function<? super ST, ? extends Either<ST, PT>> fn);

    @Deprecated //use bipeek
    void peek(Consumer<? super ST> stAction, Consumer<? super PT> ptAction);
    /**
     * @return True if this is a lazyRight Either
     */
    public boolean isRight();
    /**
     * @return True if this is a lazyLeft Either
     */
    public boolean isLeft();


    default <T2, R> Either<ST, R> zip(final Ior<ST,? extends T2> app, final BiFunction<? super PT, ? super T2, ? extends R> fn){
        return flatMap(t->app.map(t2->fn.apply(t,t2)).toEither());
    }
    default <T2, R> Either<ST, R> zip(final Either<ST,? extends T2> app, final BiFunction<? super PT, ? super T2, ? extends R> fn){
        return flatMap(t->app.map(t2->fn.apply(t,t2)));
    }
    /**
     * @return An Either with the lazyLeft type converted to a persistent list, for use with accumulating app function  {@link Either#combine(Either,BiFunction)}
     */
    default Either<LinkedListX<ST>, PT> list() {
        return mapLeft(LinkedListX::of);
    }

    /**
     * Accumulate secondarys into a LinkedListX (extended Persistent List) and Right with the supplied combiner function
     * Right accumulation only occurs if all phases are lazyRight
     *
     * @param app Value to combine with
     * @param fn Combiner function for lazyRight values
     * @return Combined Either
     */
    default <T2, R> Either<LinkedListX<ST>, R> combineToList(final Either<ST, ? extends T2> app, final BiFunction<? super PT, ? super T2, ? extends R> fn) {
        return list().combine(app.list(), Semigroups.collectionXConcat(), fn);
    }

    /**
     * Accumulate lazyLeft values with the provided BinaryOperator / Semigroup {@link Semigroups}
     * Right accumulation only occurs if all phases are lazyRight
     *
     * <pre>
     * {@code
     *  Either<String,String> fail1 =  Either.lazyLeft("failed1");
        Either<LinkedListX<String>,String> result = fail1.list().combine(Either.lazyLeft("failed2").list(), SemigroupK.collectionConcat(),(a,b)->a+b);

        //Left of [LinkedListX.of("failed1","failed2")))]
     * }
     * </pre>
     *
     * @param app Value to combine with
     * @param semigroup to combine lazyLeft types
     * @param fn To combine lazyRight types
     * @return Combined Either
     */

    default <T2, R> Either<ST, R> combine(final Either<? extends ST, ? extends T2> app, final BinaryOperator<ST> semigroup,
                                          final BiFunction<? super PT, ? super T2, ? extends R> fn) {
        return this.visit(secondary -> app.visit(s2 -> Either.left(semigroup.apply(s2, secondary)), p2 -> Either.left(secondary)),
                          primary -> app.visit(s2 -> Either.left(s2), p2 -> Either.right(fn.apply(primary, p2))));
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
    default Option<PT> filterNot(final Predicate<? super PT> fn) {

        return (Option<PT>) Filters.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Filters#notNull()
     */
    @Override
    default Option<PT> notNull() {

        return (Option<PT>) Filters.super.notNull();
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Functor#trampoline(java.util.function.Function)
     */
    @Override
    default <R> Either<ST, R> trampoline(final Function<? super PT, ? extends Trampoline<? extends R>> mapper) {

        return (Either<ST, R>) Transformable.super.trampoline(mapper);
    }

    Ior<ST, PT> toIor();

    default Trampoline<Either<ST,PT>> toTrampoline() {
        return Trampoline.more(()->Trampoline.done(this));
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Right<L, RT> implements Either<L, RT> {
        private final RT value;

        @Override
        public Either<L, RT> mapLeftToRight(final Function<? super L, ? extends RT> fn) {
            return this;
        }

        @Override
        public <R> Either<R, RT> mapLeft(final Function<? super L, ? extends R> fn) {
            return (Either<R, RT>) this;
        }

        @Override
        public <R> Either<L, R> map(final Function<? super RT, ? extends R> fn) {
            return new Right<L, R>(
                                      fn.apply(value));
        }

        @Override
        public Either<L, RT> peekLeft(final Consumer<? super L> action) {
            return this;
        }

        @Override
        public Either<L, RT> peek(final Consumer<? super RT> action) {
            action.accept(value);
            return this;
        }

        @Override
        public Option<RT> filter(final Predicate<? super RT> test) {
            return test.test(value) ? Option.some(value) : Option.none();
        }

        @Override
        public Either<RT, L> swap() {
            return new Left<RT, L>(
                                         value);
        }

        @Override
        public Option<RT> get() {
            return Option.some(value);
        }

        @Override
        public Option<L> getLeft() {
            return Option.none();
        }

        @Override
        public L leftOrElse(L alt) {
            return alt;
        }


        @Override
        public ReactiveSeq<L> leftToStream() {
            return ReactiveSeq.empty();
        }

        @Override
        public <RT1> Either<L, RT1> flatMap(Function<? super RT, ? extends Either<? extends L,? extends RT1>> mapper){
            return (Either<L, RT1>) mapper.apply(value);
        }

        @Override
        public <LT1> Either<LT1, RT> flatMapLeft(final Function<? super L, ? extends Either<LT1, RT>> mapper) {
            return (Either<LT1, RT>) this;
        }

        @Override
        public Either<L, RT> flatMapLeftToRight(final Function<? super L, ? extends Either<L, RT>> fn) {
            return this;
        }

        @Override
        public void peek(final Consumer<? super L> stAction, final Consumer<? super RT> ptAction) {
            ptAction.accept(value);
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
        public String toString() {
            return mkString();
        }

        @Override
        public String mkString() {
            return "Either.right[" + value + "]";
        }

        @Override
        public Ior<L, RT> toIor() {
            return Ior.primary(value);
        }

        @Override
        public <R> R visit(final Function<? super L, ? extends R> secondary, final Function<? super RT, ? extends R> primary) {
            return primary.apply(value);
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
            if (!(obj instanceof Either))
                return false;
            Either other = (Either) obj;
            if(!other.isRight())
                return false;
            return Objects.equals(value,other.orElse(null));
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

        @Override
        public <R> R fold(Function<? super L, ? extends R> fn1, Function<? super RT, ? extends R> fn2) {
            return fn2.apply(value);
        }

        @Override
        public <R> R visit(Function<? super RT, ? extends R> present, Supplier<? extends R> absent) {
            return present.apply(value);
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Left<L, R> implements Either<L, R> {
        private final L value;

        @Override
        public boolean isLeft() {
            return true;
        }

        @Override
        public boolean isRight() {
            return false;
        }


        @Override
        public Either<L, R> mapLeftToRight(final Function<? super L, ? extends R> fn) {
            return new Right<L, R>(
                                       fn.apply(value));
        }

        @Override
        public <R2> Either<R2, R> mapLeft(final Function<? super L, ? extends R2> fn) {
            return new Left<R2, R>(
                                        fn.apply(value));
        }

        @Override
        public <R2> Either<L, R2> map(final Function<? super R, ? extends R2> fn) {
            return (Either<L, R2>) this;
        }

        @Override
        public Either<L, R> peekLeft(final Consumer<? super L> action) {
            return mapLeft((Function) FluentFunctions.expression(action));
        }

        @Override
        public Either<L, R> peek(final Consumer<? super R> action) {
            return this;
        }

        @Override
        public Option<R> filter(final Predicate<? super R> test) {
            return Option.none();
        }

        @Override
        public Either<R, L> swap() {
            return new Right<R, L>(
                                       value);
        }

        @Override
        public Option<R> get() { return Option.none();
        }

        @Override
        public Option<L> getLeft() {
            return Option.some(value);
        }

        @Override
        public L leftOrElse(L alt) {
            return value;
        }


        @Override
        public ReactiveSeq<L> leftToStream() {
            return ReactiveSeq.of(value);
        }

        @Override
        public <RT1> Either<L, RT1> flatMap(Function<? super R, ? extends Either<? extends L, ? extends RT1>> mapper) {
            return (Either<L, RT1>)this;
        }



        @Override
        public <LT1> Either<LT1, R> flatMapLeft(final Function<? super L, ? extends Either<LT1, R>> mapper) {
            return mapper.apply(value);
        }

        @Override
        public Either<L, R> flatMapLeftToRight(final Function<? super L, ? extends Either<L, R>> fn) {
            return fn.apply(value);
        }

        @Override
        public void peek(final Consumer<? super L> stAction, final Consumer<? super R> ptAction) {
            stAction.accept(value);

        }

        @Override
        public <R2> R2 visit(final Function<? super L, ? extends R2> secondary, final Function<? super R, ? extends R2> primary) {
            return secondary.apply(value);
        }

        @Override
        public Maybe<R> toMaybe() {
            return Maybe.nothing();
        }

        @Override
        public Optional<R> toOptional() {
            return Optional.empty();
        }



        @Override
        public String toString() {
            return mkString();
        }

        @Override
        public String mkString() {
            return "Either.left[" + value + "]";
        }



        @Override
        public Ior<L, R> toIor() {
            return Ior.secondary(value);
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
            Either other = (Either) obj;
            if(other.isRight())
                return false;
            return Objects.equals(value,other.swap().orElse(null));
        }

        @Override
        public <R2> R2 fold(Function<? super L, ? extends R2> fn1, Function<? super R, ? extends R2> fn2) {
            return fn1.apply(value);
        }

        @Override
        public <R2> R2 visit(Function<? super R, ? extends R2> present, Supplier<? extends R2> absent) {
            return absent.get();
        }
    }

    public static class Instances {

        public static <L> InstanceDefinitions<Higher<either, L>> definitions(){
            return new InstanceDefinitions<Higher<either, L>>() {
                @Override
                public <T, R> Functor<Higher<either, L>> functor() {
                    return Instances.functor();
                }

                @Override
                public <T> Pure<Higher<either, L>> unit() {
                    return Instances.unit();
                }

                @Override
                public <T, R> Applicative<Higher<either, L>> applicative() {
                    return Instances.applicative();
                }

                @Override
                public <T, R> Monad<Higher<either, L>> monad() {
                    return Instances.monad();
                }

                @Override
                public <T, R> Maybe<MonadZero<Higher<either, L>>> monadZero() {
                    return Maybe.just(Instances.monadZero());
                }

                @Override
                public <T> Maybe<MonadPlus<Higher<either, L>>> monadPlus() {
                    return Maybe.just(Instances.monadPlus());
                }

                @Override
                public <T> MonadRec<Higher<either, L>> monadRec() {
                    return Instances.monadRec();
                }


                @Override
                public <T> Maybe<MonadPlus<Higher<either, L>>> monadPlus(Monoid<Higher<Higher<either, L>, T>> m) {
                    return Maybe.just(Instances.monadPlus(m));
                }

                @Override
                public <C2, T> Traverse<Higher<either, L>> traverse() {
                    return Instances.traverse();
                }

                @Override
                public <T> Foldable<Higher<either, L>> foldable() {
                    return Instances.foldable();
                }

                @Override
                public <T> Maybe<Comonad<Higher<either, L>>> comonad() {
                    return Maybe.nothing();
                }

                @Override
                public <T> Maybe<Unfoldable<Higher<either, L>>> unfoldable() {
                    return Maybe.nothing();
                }
            };
        }
        public static <L> Functor<Higher<either, L>> functor() {
            return new Functor<Higher<either, L>>() {

                @Override
                public <T, R> Higher<Higher<either, L>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<either, L>, T> ds) {
                    Either<L,T> xor = Either.narrowK(ds);
                    return xor.map(fn);
                }
            };
        }
        public static <L> Pure<Higher<either, L>> unit() {
            return new Pure<Higher<either, L>>() {

                @Override
                public <T> Higher<Higher<either, L>, T> unit(T value) {
                    return Either.right(value);
                }
            };
        }
        public static <L> Applicative<Higher<either, L>> applicative() {
            return new Applicative<Higher<either, L>>() {


                @Override
                public <T, R> Higher<Higher<either, L>, R> ap(Higher<Higher<either, L>, ? extends Function<T, R>> fn, Higher<Higher<either, L>, T> apply) {
                    Either<L,T> xor = Either.narrowK(apply);
                    Either<L, ? extends Function<T, R>> xorFn = Either.narrowK(fn);
                    return xorFn.zip(xor,(a,b)->a.apply(b));

                }

                @Override
                public <T, R> Higher<Higher<either, L>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<either, L>, T> ds) {
                    return Instances.<L>functor().map(fn,ds);
                }

                @Override
                public <T> Higher<Higher<either, L>, T> unit(T value) {
                    return Instances.<L>unit().unit(value);
                }
            };
        }
        public static <L> Monad<Higher<either, L>> monad() {
            return new Monad<Higher<either, L>>() {

                @Override
                public <T, R> Higher<Higher<either, L>, R> flatMap(Function<? super T, ? extends Higher<Higher<either, L>, R>> fn, Higher<Higher<either, L>, T> ds) {
                    Either<L,T> xor = Either.narrowK(ds);
                    return xor.flatMap(fn.andThen(Either::narrowK));
                }

                @Override
                public <T, R> Higher<Higher<either, L>, R> ap(Higher<Higher<either, L>, ? extends Function<T, R>> fn, Higher<Higher<either, L>, T> apply) {
                   return Instances.<L>applicative().ap(fn,apply);

                }

                @Override
                public <T, R> Higher<Higher<either, L>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<either, L>, T> ds) {
                    return Instances.<L>functor().map(fn,ds);
                }

                @Override
                public <T> Higher<Higher<either, L>, T> unit(T value) {
                    return Instances.<L>unit().unit(value);
                }
            };
        }
        public static <X,T,R> MonadRec<Higher<either, X>> monadRec() {

            return new MonadRec<Higher<either, X>>(){
                @Override
                public <T, R> Higher<Higher<either, X>, R> tailRec(T initial, Function<? super T, ? extends Higher<Higher<either, X>, ? extends Either<T, R>>> fn) {
                    return Either.tailRec(initial,fn.andThen(Either::narrowK));
                }


            };


        }
        public static BiFunctor<either> bifunctor(){
            return new BiFunctor<either>() {
                @Override
                public <T, R, T2, R2> Higher2<either, R, R2> bimap(Function<? super T, ? extends R> fn, Function<? super T2, ? extends R2> fn2, Higher2<either, T, T2> ds) {
                    return narrowK(ds).bimap(fn,fn2);
                }
            };
        }
        public static <L> Traverse<Higher<either, L>> traverse() {
            return new Traverse<Higher<either, L>>() {

                @Override
                public <C2, T, R> Higher<C2, Higher<Higher<either, L>, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn, Higher<Higher<either, L>, T> ds) {
                    Either<L,T> maybe = Either.narrowK(ds);
                    return maybe.visit(left->  applicative.unit(Either.<L,R>left(left)),
                                        right->applicative.map(m-> Either.right(m), fn.apply(right)));
                }

                @Override
                public <C2, T> Higher<C2, Higher<Higher<either, L>, T>> sequenceA(Applicative<C2> applicative, Higher<Higher<either, L>, Higher<C2, T>> ds) {
                    return traverseA(applicative,Function.identity(),ds);
                }



                @Override
                public <T, R> Higher<Higher<either, L>, R> ap(Higher<Higher<either, L>, ? extends Function<T, R>> fn, Higher<Higher<either, L>, T> apply) {
                    return Instances.<L>applicative().ap(fn,apply);

                }

                @Override
                public <T, R> Higher<Higher<either, L>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<either, L>, T> ds) {
                    return Instances.<L>functor().map(fn,ds);
                }

                @Override
                public <T> Higher<Higher<either, L>, T> unit(T value) {
                    return Instances.<L>unit().unit(value);
                }
            };
        }
        public static <L> Foldable<Higher<either, L>> foldable() {
            return new Foldable<Higher<either, L>>() {


                @Override
                public <T> T foldRight(Monoid<T> monoid, Higher<Higher<either, L>, T> ds) {
                    Either<L,T> xor = Either.narrowK(ds);
                    return xor.fold(monoid);
                }

                @Override
                public <T> T foldLeft(Monoid<T> monoid, Higher<Higher<either, L>, T> ds) {
                    Either<L,T> xor = Either.narrowK(ds);
                    return xor.fold(monoid);
                }

                @Override
                public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<Higher<either, L>, T> nestedA) {
                    return foldLeft(mb,narrowK(nestedA).<R>map(fn));
                }
            };
        }
        public static <L> MonadZero<Higher<either, L>> monadZero() {
            return new MonadZero<Higher<either, L>>() {

                @Override
                public Higher<Higher<either, L>, ?> zero() {
                    return Either.left(null);
                }

                @Override
                public <T, R> Higher<Higher<either, L>, R> flatMap(Function<? super T, ? extends Higher<Higher<either, L>, R>> fn, Higher<Higher<either, L>, T> ds) {
                    Either<L,T> xor = Either.narrowK(ds);
                    return xor.flatMap(fn.andThen(Either::narrowK));
                }

                @Override
                public <T, R> Higher<Higher<either, L>, R> ap(Higher<Higher<either, L>, ? extends Function<T, R>> fn, Higher<Higher<either, L>, T> apply) {
                    return Instances.<L>applicative().ap(fn,apply);

                }

                @Override
                public <T, R> Higher<Higher<either, L>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<either, L>, T> ds) {
                    return Instances.<L>functor().map(fn,ds);
                }

                @Override
                public <T> Higher<Higher<either, L>, T> unit(T value) {
                    return Instances.<L>unit().unit(value);
                }
            };
        }
        public static <L> MonadPlus<Higher<either, L>> monadPlus() {
            Monoid m = Monoids.firstRightEither((Either) Either.narrowK(Instances.<L>monadZero().zero()));

            return monadPlus(m);
        }
        public static <L,T> MonadPlus<Higher<either, L>> monadPlus(Monoid<Higher<Higher<either, L>, T>> m) {
            return new MonadPlus<Higher<either, L>>() {

                @Override
                public Monoid<Higher<Higher<either, L>, ?>> monoid() {
                    return (Monoid)m;
                }

                @Override
                public Higher<Higher<either, L>, ?> zero() {
                    return Instances.<L>monadZero().zero();
                }

                @Override
                public <T, R> Higher<Higher<either, L>, R> flatMap(Function<? super T, ? extends Higher<Higher<either, L>, R>> fn, Higher<Higher<either, L>, T> ds) {
                    Either<L,T> xor = Either.narrowK(ds);
                    return xor.flatMap(fn.andThen(Either::narrowK));
                }

                @Override
                public <T, R> Higher<Higher<either, L>, R> ap(Higher<Higher<either, L>, ? extends Function<T, R>> fn, Higher<Higher<either, L>, T> apply) {
                    return Instances.<L>applicative().ap(fn,apply);

                }

                @Override
                public <T, R> Higher<Higher<either, L>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<either, L>, T> ds) {
                    return Instances.<L>functor().map(fn,ds);
                }

                @Override
                public <T> Higher<Higher<either, L>, T> unit(T value) {
                    return Instances.<L>unit().unit(value);
                }
            };
        }
        public static <L> ApplicativeError<Higher<either, L>,L> applicativeError(){
            return new ApplicativeError<Higher<either, L>,L>() {

                @Override
                public <T, R> Higher<Higher<either, L>, R> ap(Higher<Higher<either, L>, ? extends Function<T, R>> fn, Higher<Higher<either, L>, T> apply) {
                    return Instances.<L>applicative().ap(fn,apply);
                }

                @Override
                public <T, R> Higher<Higher<either, L>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<either, L>, T> ds) {
                    return Instances.<L>applicative().map(fn,ds);
                }

                @Override
                public <T> Higher<Higher<either, L>, T> unit(T value) {
                    return Instances.<L>applicative().unit(value);
                }

                @Override
                public <T> Higher<Higher<either, L>, T> raiseError(L l) {
                    return Either.left(l);
                }

                @Override
                public <T> Higher<Higher<either, L>, T> handleErrorWith(Function<? super L, ? extends Higher<Higher<either, L>, ? extends T>> fn, Higher<Higher<either, L>, T> ds) {
                    Function<? super L, ? extends Either<L, T>> fn2 = fn.andThen(s -> {

                        Higher<Higher<either, L>, T> x = (Higher<Higher<either, L>, T>)s;
                        Either<L, T> r = narrowK(x);
                        return r;
                    });
                    return narrowK(ds).flatMapLeftToRight(fn2);
                }
            };
        }

    }
}