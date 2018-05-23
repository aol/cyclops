package cyclops.control;

import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.hkt.Higher2;
import com.oath.cyclops.matching.Sealed2;
import com.oath.cyclops.types.Filters;
import com.oath.cyclops.types.OrElseValue;
import com.oath.cyclops.types.Value;
import com.oath.cyclops.types.factory.Unit;
import com.oath.cyclops.types.foldable.To;
import com.oath.cyclops.types.functor.BiTransformable;
import com.oath.cyclops.types.functor.Transformable;

import cyclops.data.LazySeq;
import cyclops.data.Vector;
import cyclops.function.*;
import cyclops.companion.Semigroups;

import com.oath.cyclops.types.reactive.ValueSubscriber;
import com.oath.cyclops.hkt.DataWitness.either;

import cyclops.reactive.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.reactivestreams.Publisher;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.*;
import java.util.stream.Stream;

/**
 *  Either left or right.
 *
 * 'Right' (or right type) biased disjunct union. Often called Either, but in a generics heavy Java world Either is half the length of Either.
 *
 *  No 'projections' are provided, swap() and leftXXXX alternative methods can be used instead.
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
 *  Right : Most methods operate naturally on the right type, if it is present. If it is not, nothing happens.
 *  Left : Most methods do nothing to the left type if it is present.
 *              To operate on the Left type first call swap() or use left analogs of the main operators.
 *
 *  Instantiating an Either - Right
 *  <pre>
 *  {@code
 *      Either.right("hello").map(v->v+" world")
 *    //Either.right["hello world"]
 *  }
 *  </pre>
 *
 *  Instantiating an Either - Left
 *  <pre>
 *  {@code
 *      Either.left("hello").map(v->v+" world")
 *    //Either.seconary["hello"]
 *  }
 *  </pre>
 *
 *  Either can operate (via transform/flatMap) as a Functor / Monad and via combine as an ApplicativeFunctor
 *
 *   Values can be accumulated via
 *  <pre>
 *  {@code
 *  Either.accumulateLeft(Seq.of(Either.left("failed1"),
                                                    Either.left("failed2"),
                                                    Either.right("success")),
                                                    SemigroupK.stringConcat)
 *
 *  //failed1failed2
 *
 *   Either<String,String> fail1 = Either.left("failed1");
     fail1.swap().combine((a,b)->a+b)
                 .combine(Either.left("failed2").swap())
                 .combine(Either.<String,String>right("success").swap())
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
 * @param <LT> Left type
 * @param <RT> Right type
 */
public interface Either<LT, RT> extends To<Either<LT, RT>>,
                                     BiTransformable<LT, RT>,
                                     Sealed2<LT, RT>,Value<RT>,
                                     OrElseValue<RT,Either<LT, RT>>,
                                     Unit<RT>, Transformable<RT>, Filters<RT>,
                                     Serializable,
                                     Higher2<either, LT, RT> {


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

    public static <L,T> Higher<Higher<either,L>, T> widen(Either<L,T> narrow) {
        return narrow;
    }

    default int arity(){
        return 2;
    }


    default Eval<Either<LT, RT>> nestedEval(){
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
     *   ReactiveSeq<Integer> stream =  ReactiveSeq.of(1,2,3);

         Either<Throwable,Integer> future = Either.fromPublisher(stream);

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

         Either<Throwable,Integer> future = Either.fromPublisher(stream);

         //Either[1]
     *
     * }
     * </pre>
     * @param iterable Iterable to construct an Either from
     * @return Either constructed from the supplied Iterable
     */
    public static <ST, T> Either<ST, T> fromIterable(final Iterable<T> iterable) {

        final Iterator<T> it = iterable.iterator();
        return it.hasNext() ? Either.right(  it.next()) :Either.left(null);
    }

    /**
     * Create an instance of the left type. Most methods are biased to the right type,
     * so you will need to use swap() or leftXXXX to manipulate the wrapped value
     *
     * <pre>
     * {@code
     *   Either.<Integer,Integer>left(10).map(i->i+1);
     *   //Either.left[10]
     *
     *    Either.<Integer,Integer>left(10).swap().map(i->i+1);
     *    //Either.right[11]
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
     * Create an instance of the right type. Most methods are biased to the right type,
     * which means, for example, that the transform method operates on the right type but does nothing on left Eithers
     *
     * <pre>
     * {@code
     *   Either.<Integer,Integer>right(10).map(i->i+1);
     *   //Either.right[11]
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






    default <T2, R1, R2, R3, R> Either<LT,R> forEach4(Function<? super RT, ? extends Either<LT,R1>> value1,
                                                      BiFunction<? super RT, ? super R1, ? extends Either<LT,R2>> value2,
                                                      Function3<? super RT, ? super R1, ? super R2, ? extends Either<LT,R3>> value3,
                                                      Function4<? super RT, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return this.flatMap(in-> {

            Either<LT,R1> a = value1.apply(in);
            return a.flatMap(ina-> {
                Either<LT,R2> b = value2.apply(in,ina);
                return b.flatMap(inb-> {
                    Either<LT,R3> c= value3.apply(in,ina,inb);
                    return c.map(in2->yieldingFunction.apply(in,ina,inb,in2));
                });

            });

        });
    }




    default <T2, R1, R2, R> Either<LT,R> forEach3(Function<? super RT, ? extends Either<LT,R1>> value1,
                                                  BiFunction<? super RT, ? super R1, ? extends Either<LT,R2>> value2,
                                                  Function3<? super RT, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return this.flatMap(in-> {

            Either<LT,R1> a = value1.apply(in);
            return a.flatMap(ina-> {
                Either<LT,R2> b = value2.apply(in,ina);
                return b.map(in2->yieldingFunction.apply(in,ina, in2));
            });

        });
    }





    default <R1, R> Either<LT,R> forEach2(Function<? super RT, ? extends Either<LT,R1>> value1,
                                          BiFunction<? super RT, ? super R1, ? extends R> yieldingFunction) {

        return this.flatMap(in-> {
            Either<LT,R1> b = value1.apply(in);
            return b.map(in2->yieldingFunction.apply(in, in2));
        });
    }


    //cojoin
    /* (non-Javadoc)
     * @see com.oath.cyclops.types.MonadicValue#nest()
     */
    default Either<LT, Either<LT, RT>> nest() {
        return this.map(t -> unit(t));
    }



    /* (non-Javadoc)
     * @see com.oath.cyclops.types.MonadicValue#unit(java.lang.Object)
     */
    @Override
    default <T> Either<LT, T> unit(final T unit) {
        return Either.right(unit);
    }


    /* (non-Javadoc)
     * @see com.oath.cyclops.types.Filters#filter(java.util.function.Predicate)
     */
    @Override
    Option<RT> filter(Predicate<? super RT> test);

    Either<LT, RT> filter(Predicate<? super RT> test, Function<? super RT,? extends LT> rightToLeft);
    /**
     * If this Either contains the Left type, transform it's value so that it contains the Right type
     *
     *
     * @param fn Function to transform left type to right
     * @return Either with left type mapped to right
     */
    Either<LT, RT> mapLeftToRight(Function<? super LT, ? extends RT> fn);

    /**
     * Always transform the Left type of this Either if it is present using the provided transformation function
     *
     * @param fn Transformation function for Left types
     * @return Either with Left type transformed
     */
    <R> Either<R, RT> mapLeft(Function<? super LT, ? extends R> fn);

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.MonadicValue#transform(java.util.function.Function)
     */
    @Override
    <R> Either<LT, R> map(Function<? super RT, ? extends R> fn);

    /**
     * Peek at the Left type value if present
     *
     * @param action Consumer to peek at the Left type value
     * @return Either with the same values as before
     */
    Either<LT, RT> peekLeft(Consumer<? super LT> action);

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.Functor#peek(java.util.function.Consumer)
     */
    @Override
    Either<LT, RT> peek(Consumer<? super RT> action);

    /**
     * Swap types so operations directly affect the current (pre-swap) Left type
     *<pre>
     *  {@code
     *
     *    Either.left("hello")
     *       .map(v->v+" world")
     *    //Either.seconary["hello"]
     *
     *    Either.left("hello")
     *       .swap()
     *       .map(v->v+" world")
     *       .swap()
     *    //Either.seconary["hello world"]
     *  }
     *  </pre>
     *
     *
     * @return Swap the right and left types, allowing operations directly on what was the Left type
     */
    Either<RT, LT> swap();



    /* (non-Javadoc)
     * @see com.oath.cyclops.types.foldable.Convertable#isPresent()
     */
    @Override
    default boolean isPresent() {
        return isRight();
    }


    /* (non-Javadoc)
     * @see com.oath.cyclops.types.Value#toLazyEither(java.lang.Object)
     */
    @Override
    default <ST2> Either<ST2, RT> toEither(final ST2 left) {
        return visit(s -> left(left), p -> right(p));
    }
    /**
     *  Turn a Collection of Eithers into a single Either with Lists of values.
     *  Right and left types are swapped during this operation.
     *
     * <pre>
     * {@code
     *  Either<String,Integer> just  = Either.right(10);
        Either<String,Integer> none = Either.left("none");
     *  Either<Seq<Integer>,Seq<String>> xors =Either.sequenceLeft(Seq.of(just,none,Either.right(1)));
        //Either.right(Seq.of("none")))
     *
     * }
     * </pre>
     *
     *
     * @param xors Eithers to sequence
     * @return Either sequenced and swapped
     */
    public static <ST, PT> Either<PT, ReactiveSeq<ST>> sequenceLeft(final Iterable<Either<ST, PT>> xors) {
        return sequence(ReactiveSeq.fromIterable(xors).filter(Either::isLeft).map(i->i.swap())).map(s->ReactiveSeq.fromStream(s));
    }
  public static  <L,T> Either<L,Stream<T>> sequence(Stream<? extends Either<L,T>> stream) {

    Either<L, Stream<T>> identity = Either.right(ReactiveSeq.empty());

    BiFunction<Either<L,Stream<T>>,Either<L,T>,Either<L,Stream<T>>> combineToStream = (acc,next) ->acc.zip(next,(a,b)->ReactiveSeq.fromStream(a).append(b));

    BinaryOperator<Either<L,Stream<T>>> combineStreams = (a,b)-> a.zip(b,(z1,z2)->ReactiveSeq.fromStream(z1).appendStream(z2));

   return stream.reduce(identity,combineToStream,combineStreams);
  }
  public static <L,T,R> Either<L,Stream<R>> traverse(Function<? super T,? extends R> fn,Stream<Either<L,T>> stream) {
    return sequence(stream.map(h->h.map(fn)));
  }
    /**
     * Accumulate the result of the Left types in the Collection of Eithers provided using the supplied Reducer  {@see cyclops2.Reducers}.
     *
     * <pre>
     * {@code
     *  Either<String,Integer> just  = Either.right(10);
        Either<String,Integer> none = Either.left("none");

     *  Either<?,PersistentSetX<String>> xors = Either.accumulateLeft(Seq.of(just,none,Either.right(1)),Reducers.<String>toPersistentSetX());
      //Either.right(PersistentSetX.of("none"))));
      * }
     * </pre>
     * @param xors Collection of Iors to accumulate left values
     * @param reducer Reducer to accumulate results
     * @return Either populated with the accumulate left operation
     */
    public static <LT, RT, R> Either<RT, R> accumulateLeft(final Iterable<Either<LT, RT>> xors, final Reducer<R, LT> reducer) {
        return sequenceLeft(xors).map(s -> s.mapReduce(reducer));
    }
    /**
     * Accumulate the results only from those Eithers which have a Left type present, using the supplied mapping function to
     * convert the data from each Either before reducing them using the supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see cyclops2.Monoids }..
     *
     * <pre>
     * {@code
     *  Either<String,Integer> just  = Either.right(10);
        Either<String,Integer> none = Either.left("none");

     *  Either<?,String> xors = Either.accumulateLeft(Seq.of(just,none,Either.left("1")),i->""+i,Monoids.stringConcat);

        //Either.right("none1")
     *
     * }
     * </pre>
     *
     *
     *
     * @param xors Collection of Iors to accumulate left values
     * @param mapper Mapping function to be applied to the result of each Ior
     * @param reducer Semigroup to combine values from each Ior
     * @return Either populated with the accumulate Left operation
     */
    public static <ST, PT, R> Either<PT, R> accumulateLeft(final Iterable<Either<ST, PT>> xors, final Function<? super ST, R> mapper,
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
     * Either<String,Integer> just  = Either.right(10);
       Either<String,Integer> none = Either.left("none");


     * Either<Seq<String>,Seq<Integer>> xors =Either.sequenceRight(Seq.of(just,none,Either.right(1)));
       //Either.right(Seq.of(10,1)));
     *
     * }</pre>
     *
     *
     *
     * @param eithers Eithers to sequence
     * @return Either Sequenced
     */
    public static <ST, PT> Either<ST, ReactiveSeq<PT>> sequenceRight(final Iterable<Either<ST, PT>> eithers) {
        return sequence(ReactiveSeq.fromIterable(eithers).filter(Either::isRight)).map(s->ReactiveSeq.fromStream(s));
    }
    /**
     * Accumulate the result of the Right types in the Collection of Eithers provided using the supplied Reducer  {@see cyclops2.Reducers}.

     * <pre>
     * {@code
     *  Either<String,Integer> just  = Either.right(10);
        Either<String,Integer> none = Either.left("none");

     *  Either<?,PersistentSetX<Integer>> xors =Either.accumulateRight(Seq.of(just,none,Either.right(1)),Reducers.toPersistentSetX());
        //Either.right(PersistentSetX.of(10,1))));
     * }
     * </pre>
     * @param xors Collection of Iors to accumulate right values
     * @param reducer Reducer to accumulate results
     * @return Either populated with the accumulate right operation
     */
    public static <LT, RT, R> Either<LT, R> accumulateRight(final Iterable<Either<LT, RT>> xors, final Reducer<R,RT> reducer) {
        return sequenceRight(xors).map(s -> s.mapReduce(reducer));
    }

    /**
     * Accumulate the results only from those Iors which have a Right type present, using the supplied mapping function to
     * convert the data from each Either before reducing them using the supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see cyclops2.Monoids }..
     *
     * <pre>
     * {@code
     *  Either<String,Integer> just  = Either.right(10);
        Either<String,Integer> none = Either.left("none");

     * Either<?,String> iors = Either.accumulateRight(Seq.of(just,none,Either.right(1)),i->""+i,Monoids.stringConcat);
       //Either.right("101"));
     * }
     * </pre>
     *
     *
     * @param xors Collection of Iors to accumulate right values
     * @param mapper Mapping function to be applied to the result of each Ior
     * @param reducer Reducer to accumulate results
     * @return Either populated with the accumulate right operation
     */
    public static <ST, PT, R> Either<ST, R> accumulateRight(final Iterable<Either<ST, PT>> xors, final Function<? super PT, R> mapper,
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
     *  Either<String,Integer> just  = Either.right(10);
        Either<String,Integer> none = Either.left("none");
     *
     *  Either<?,Integer> xors XIor.accumulateRight(Monoids.intSum,Seq.of(just,none,Ior.right(1)));
        //Ior.right(11);
     *
     * }
     * </pre>
     *
     *
     *
     * @param xors Collection of Eithers to accumulate right values
     * @param reducer  Reducer to accumulate results
     * @return  Either populated with the accumulate right operation
     */
    public static <ST, PT> Either<ST, PT> accumulateRight(final Monoid<PT> reducer, final Iterable<Either<ST, PT>> xors) {
        return sequenceRight(xors).map(s -> s.reduce(reducer));
    }

    /**
     *
     * Accumulate the results only from those Eithers which have a Left type present, using the supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see cyclops2.Monoids }.
     * <pre>
     * {@code
     * Either.accumulateLeft(Seq.of(Either.left("failed1"),
    												Either.left("failed2"),
    												Either.right("success")),
    												SemigroupK.stringConcat)


     * //Eithers.Right[failed1failed2]
     * }
     * </pre>
     * <pre>
     * {@code
     *
     *  Either<String,Integer> just  = Either.right(10);
        Either<String,Integer> none = Either.left("none");

     * Either<?,Integer> iors = Either.accumulateLeft(Monoids.intSum,Seq.of(Either.both(2, "boo!"),Either.left(1)));
       //Either.right(3);  2+1
     *
     *
     * }
     * </pre>
     *
     * @param xors Collection of Eithers to accumulate left values
     * @param reducer  Semigroup to combine values from each Either
     * @return Either populated with the accumulate Left operation
     */
    public static <ST, PT> Either<PT, ST> accumulateLeft(final Monoid<ST> reducer, final Iterable<Either<ST, PT>> xors) {
        return sequenceLeft(xors).map(s -> s.reduce(reducer));
    }

    /**
     * Visitor pattern for this Ior.
     * Execute the left function if this Either contains an element of the left type
     * Execute the right function if this Either contains an element of the right type
     *
     *
     * <pre>
     * {@code
     *  Either.right(10)
     *     .visit(left->"no", right->"yes")
     *  //Either["yes"]

        Either.left(90)
           .visit(left->"no", right->"yes")
        //Either["no"]


     *
     * }
     * </pre>
     *
     * @param left Function to execute if this is a Left Either
     * @param right Function to execute if this is a Right Ior
     * @return Result of executing the appropriate function
     */
    <R> R visit(Function<? super LT, ? extends R> left, Function<? super RT, ? extends R> right);


    @Override
    default <R1, R2> Either<R1, R2> bimap(Function<? super LT, ? extends R1> left, Function<? super RT, ? extends R2> right) {
        if (isLeft())
            return (Either<R1, R2>) swap().map(left)
                                       .swap();
        return (Either<R1, R2>) map(right);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.functor.BiTransformable#bipeek(java.util.function.Consumer, java.util.function.Consumer)
     */
    @Override
    default Either<LT, RT> bipeek(Consumer<? super LT> c1, Consumer<? super RT> c2) {

        return (Either<LT, RT>)BiTransformable.super.bipeek(c1, c2);
    }





    /* (non-Javadoc)
     * @see java.util.function.Supplier#getValue()
     */
    Option<RT> get();

    /**
     * @return The Left Value if present, otherwise null
     */
    Option<LT> getLeft();
    LT leftOrElse(LT alt);

    Either<LT, RT> recover(Supplier<? extends RT> value);
    Either<LT, RT> recover(RT value);
    Either<LT, RT> recoverWith(Supplier<? extends Either<LT, RT>> fn);

    /**
     * @return A Stream containing the Either Left value if present, otherwise an zero Stream
     */
    ReactiveSeq<LT> leftToStream();


    <RT1> Either<LT, RT1> flatMap(Function<? super RT, ? extends Either<? extends LT,? extends RT1>> mapper);
    /**
     * Perform a flatMap operation on the Left type
     *
     * @param mapper Flattening transformation function
     * @return Either containing the value inside the result of the transformation function as the Left value, if the Left type was present
     */
    <LT1> Either<LT1, RT> flatMapLeft(Function<? super LT, ? extends Either<LT1, RT>> mapper);
    /**
     * A flatMap operation that keeps the Left and Right types the same
     *
     * @param fn Transformation function
     * @return Either
     */
    Either<LT, RT> flatMapLeftToRight(Function<? super LT, ? extends Either<LT, RT>> fn);

    /**
     * @return True if this is a right Either
     */
    public boolean isRight();
    /**
     * @return True if this is a left Either
     */
    public boolean isLeft();


    default <T2, R> Either<LT, R> zip(final Ior<LT,? extends T2> app, final BiFunction<? super RT, ? super T2, ? extends R> fn){
        return flatMap(t->app.map(t2->fn.apply(t,t2)).toEither());
    }
    default <T2, R> Either<LT, R> zip(final Either<LT,? extends T2> app, final BiFunction<? super RT, ? super T2, ? extends R> fn){
        return flatMap(t->app.map(t2->fn.apply(t,t2)));
    }


    default Either<LazySeq<LT>, RT> lazySeq() {
        return mapLeft(LazySeq::of);
    }

    /**
     * Accumulate lefts into a LazySeq (extended Persistent List) and Right with the supplied combiner function
     * Right accumulation only occurs if all phases are right
     *
     * @param app Value to combine with
     * @param fn Combiner function for right values
     * @return Combined Either
     */
    default <T2, R> Either<LazySeq<LT>, R> combineToLazySeq(final Either<LT, ? extends T2> app, final BiFunction<? super RT, ? super T2, ? extends R> fn) {
        return lazySeq().combine(app.lazySeq(), Semigroups.lazySeqConcat(), fn);
    }
    default <T2, R> Either<Vector<LT>, R> combineToVector(final Either<LT, ? extends T2> app, final BiFunction<? super RT, ? super T2, ? extends R> fn) {
        return mapLeft(Vector::of).combine(app.mapLeft(Vector::of), Semigroups.vectorConcat(), fn);
    }

    /**
     * Accumulate left values with the provided BinaryOperator / Semigroup {@link Semigroups}
     * Right accumulation only occurs if all phases are right
     *
     * <pre>
     * {@code
     *  Either<String,String> fail1 =  Either.left("failed1");
        Either<LazySeq<String>,String> result = fail1.list().combine(Either.left("failed2").list(), SemigroupK.collectionConcat(),(a,b)->a+b);

        //Left of [LazySeq.of("failed1","failed2")))]
     * }
     * </pre>
     *
     * @param app Value to combine with
     * @param semigroup to combine left types
     * @param fn To combine right types
     * @return Combined Either
     */

    default <T2, R> Either<LT, R> combine(final Either<? extends LT, ? extends T2> app, final BinaryOperator<LT> semigroup,
                                          final BiFunction<? super RT, ? super T2, ? extends R> fn) {
        return this.visit(left -> app.visit(s2 -> Either.left(semigroup.apply(s2, left)), p2 -> Either.left(left)),
                          right -> app.visit(s2 -> Either.left(s2), p2 -> Either.right(fn.apply(right, p2))));
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

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Filters#notNull()
     */
    @Override
    default Option<RT> notNull() {

        return (Option<RT>) Filters.super.notNull();
    }



    Ior<LT, RT> toIor();

    default Trampoline<Either<LT, RT>> toTrampoline() {
        return Trampoline.more(()->Trampoline.done(this));
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Right<L, RT> implements Either<L, RT> {
        private final RT value;
        private static final long serialVersionUID = 1L;

      @Override
      public Either<L, RT> recover(Supplier<? extends RT> value) {
        return this;
      }

      @Override
      public Either<L, RT> recover(RT value) {
        return this;
      }

      @Override
      public Either<L, RT> recoverWith(Supplier<? extends Either<L, RT>> fn) {
        return this;
      }

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
        public Either<L, RT> filter(Predicate<? super RT> test, Function<? super RT, ? extends L> rightToLeft) {
          return test.test(value) ? this : Either.left(rightToLeft.apply(value));
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
            return Ior.right(value);
        }

        @Override
        public <R> R visit(final Function<? super L, ? extends R> left, final Function<? super RT, ? extends R> right) {
            return right.apply(value);
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
        private static final long serialVersionUID = 1L;

        @Override
        public Either<L, R> recover(Supplier<? extends R> value) {
          return right(value.get());
        }

        @Override
        public Either<L, R> recover(R value) {
          return right(value);
        }

        @Override
        public Either<L, R> recoverWith(Supplier<? extends Either<L, R>> fn) {
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
        public Either<L, R> filter(Predicate<? super R> test, Function<? super R, ? extends L> rightToLeft) {
          return this;
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
        public <R2> R2 visit(final Function<? super L, ? extends R2> left, final Function<? super R, ? extends R2> right) {
            return left.apply(value);
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
            return Ior.left(value);
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


}
