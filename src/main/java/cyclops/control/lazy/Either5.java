package cyclops.control.lazy;

import com.aol.cyclops2.data.collections.extensions.CollectionX;
import com.aol.cyclops2.types.*;
import com.aol.cyclops2.types.foldable.To;
import com.aol.cyclops2.types.functor.BiTransformable;
import com.aol.cyclops2.types.functor.Transformable;
import com.aol.cyclops2.types.reactive.Completable;
import com.aol.cyclops2.types.reactive.ValueSubscriber;
import cyclops.async.Future;
import cyclops.collections.mutable.ListX;
import cyclops.control.*;
import cyclops.function.*;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import cyclops.stream.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * A right biased Lazy Either4 type. map / flatMap operators are tail-call optimized
 * 
 * 
 * Can be one of 4 types
 * Left1
 * Left2
 * Left3
 * Right
 * 
 * 
 * 
 * @author johnmcclean
 *
 * @param <LT1> First type (Left type)
 * @param <LT2> Second type
 * @param <LT3> Third Type
 * @param <LT4> Fourth Type
 * @param <RT> Right type (operations are performed on this type if present)
 */
public interface Either5<LT1, LT2,LT3, LT4,RT> extends Transformable<RT>,
                                                        Filters<RT>,
        BiTransformable<LT4, RT>,
        To<Either5<LT1, LT2,LT3, LT4,RT>>,
                                                        MonadicValue<RT>,
                                                        Supplier<RT>{


    /**
     * Create a reactive CompletableEither
     *
     * <pre>
     *  {@code
     *      ___Example 1___
     *
     *      CompletableEither<Integer,Integer> completable = Either4.either4();
    Either4<Throwable,String,Integer> mapped = completable.map(i->i*2)
    .flatMap(i->Eval.later(()->i+1));

    completable.complete(5);

    mapped.printOut();
    //11

    ___Example 2___

    CompletableEither<Integer,Integer> completable = Either4.either4();
    Either4<Throwable,String,Integer> mapped = completable.map(i->i*2)
    .flatMap(i->Eval.later(()->i+1));


    completable.complete(null);

    //Either4:Left4[NoSuchElementException]

    ___Example 3___

    CompletableEither<Integer,Integer> completable = Either4.either4();
    Either4<Throwable,String,Integer> mapped = completable.map(i->i*2)
    .flatMap(i->Eval.later(()->i+1));

    completable.complete(new IllegalStateException());

    //Either:Left[IllegalStateElementException]
     *     }
     * </pre>
     *
     * @param <RT>
     * @return
     */
    static <LT2,LT3,LT4,RT> Either5.CompletableEither5<RT,LT2,LT3,LT4,RT> either5(){
        Completable.CompletablePublisher<RT> c = new Completable.CompletablePublisher<RT>();
        return new CompletableEither5<RT,LT2,LT3,LT4, RT>(c,fromFuture(Future.fromPublisher(c)));
    }
    @AllArgsConstructor
    static class CompletableEither5<ORG,LT1,LT2,LT3,RT> implements Either5<Throwable,LT1,LT2,LT3,RT>, Completable<ORG> {

        public final Completable.CompletablePublisher<ORG> complete;
        public final Either5<Throwable, LT1, LT2, LT3, RT> either;

        @Override
        public boolean isFailed() {
            return complete.isFailed();
        }

        @Override
        public boolean isDone() {
            return complete.isDone();
        }

        @Override
        public boolean complete(ORG done) {
            return complete.complete(done);
        }

        @Override
        public boolean completeExceptionally(Throwable error) {
            return complete.completeExceptionally(error);
        }

        @Override
        public RT get() {
            return either.get();
        }

        @Override
        public <R> R visit(Function<? super Throwable, ? extends R> left1, Function<? super LT1, ? extends R> left2, Function<? super LT2, ? extends R> left3, Function<? super LT3, ? extends R> left4, Function<? super RT, ? extends R> right) {
            return either.visit(left1,left2,left3,left4,right);
        }

        @Override
        public Maybe<RT> filter(Predicate<? super RT> test) {
            return either.filter(test);
        }

        @Override
        public <RT1> Either5<Throwable, LT1, LT2, LT3, RT1> flatMap(Function<? super RT, ? extends MonadicValue<? extends RT1>> mapper) {
            return either.flatMap(mapper);
        }

        @Override
        public Either5<Throwable, LT1, LT2, RT, LT3> swap4() {
            return either.swap4();
        }

        @Override
        public Either5<Throwable, LT1, RT, LT3, LT2> swap3() {
            return either.swap3();
        }

        @Override
        public Either5<Throwable, RT, LT2, LT3, LT1> swap2() {
            return either.swap2();
        }

        @Override
        public Either5<RT, LT1, LT2, LT3, Throwable> swap1() {
            return either.swap1();
        }

        @Override
        public boolean isRight() {
            return either.isRight();
        }

        @Override
        public boolean isLeft1() {
            return either.isLeft1();
        }

        @Override
        public boolean isLeft2() {
            return either.isLeft2();
        }

        @Override
        public boolean isLeft3() {
            return either.isLeft3();
        }

        @Override
        public boolean isLeft4() {
            return either.isLeft4();
        }

        @Override
        public <R1, R2> Either5<Throwable, LT1, LT2, R1, R2> bimap(Function<? super LT3, ? extends R1> fn1, Function<? super RT, ? extends R2> fn2) {
            return either.bimap(fn1,fn2);
        }

        @Override
        public <R> Either5<Throwable, LT1, LT2, LT3, R> map(Function<? super RT, ? extends R> fn) {
            return either.map(fn);
        }

        @Override
        public <T> Either5<Throwable, LT1, LT2, LT3, T> unit(T unit) {
            return either.unit(unit);
        }
    }


        /**
     * Static method useful as a method reference for fluent consumption of any value type stored in this Either 
     * (will capture the lowest common type)
     * 
     * <pre>
     * {@code 
     * 
     *   myEither.to(Either5::consumeAny)
                 .accept(System.out::println);
     * }
     * </pre>
     *
     * @param either Either to consume value for
     * @return Consumer we can applyHKT to consume value
     */
    static <X, LT1 extends X, LT2 extends X, LT3 extends X, LT4 extends X, RT extends X> Consumer<Consumer<? super X>> consumeAny(
            Either5<LT1, LT2, LT3, LT4, RT> either) {
        return in -> visitAny(in, either);
    }

    static <X, LT1 extends X, LT2 extends X, LT3 extends X, LT4 extends X, RT extends X, R> Function<Function<? super X, R>, R> applyAny(
            Either5<LT1, LT2, LT3, LT4, RT> either) {
        return in -> visitAny(either, in);
    }

    static <X, LT1 extends X, LT2 extends X, LT3 extends X, LT4 extends X, RT extends X, R> R visitAny(
            Either5<LT1, LT2, LT3, LT4, RT> either, Function<? super X, ? extends R> fn) {
        return either.visit(fn, fn, fn, fn, fn);
    }

    static <X, LT1 extends X, LT2 extends X, LT3 extends X, LT4 extends X, RT extends X> X visitAny(
            Consumer<? super X> c, Either5<LT1, LT2, LT3, LT4, RT> either) {
        Function<? super X, X> fn = x -> {
            c.accept(x);
            return x;
        };
        return visitAny(either, fn);
    }

    static <LT1, LT2, LT3, LT4, RT> Either5<LT1, LT2, LT3, LT4, RT> fromMonadicValue(MonadicValue<RT> mv5) {
        if (mv5 instanceof Either5) {
            return (Either5) mv5;
        }
        return mv5.toOptional()
                  .isPresent() ? Either5.right(mv5.get()) : Either5.left1(null);

    }

    static <LT2,LT3,LT4,RT> Either5<Throwable,LT2,LT3,LT4,RT> either4(){
        return Either5.<LT2,LT3,LT4,RT>fromFuture(Future.<RT>future());
    }

    static <LT1,LT2,LT3,LT4,RT> Either5<LT1,LT2,LT3,LT4,RT> fromLazy(Eval<Either5<LT1,LT2,LT3,LT4,RT>> lazy){
        return new Either5.Lazy<>(lazy);
    }

    static <LT2,LT3,LT4,RT> Either5<Throwable,LT2,LT3,LT4,RT> fromFuture(Future<RT> future){
        return fromLazy(Eval.<Either5<Throwable,LT2,LT3,LT4,RT>>fromFuture(
                future.map(e->e!=null?Either5.<Throwable,LT2,LT3,LT4,RT>right(e):Either5.<Throwable,LT2,LT3,LT4,RT>left1(new NoSuchElementException()))
                        .recover(t->Either5.<Throwable,LT2,LT3,LT4,RT>left1(t.getCause()))));
    }


    /**
     *  Turn a toX of Either3 into a singleUnsafe Either with Lists of values.
     *
     * <pre>
     * {@code
     *
     * Either4<String,String,String,Integer> just  = Either4.right(10);
       Either4<String,String,String,Integer> none = Either4.left("none");


     * Either4<ListX<String>,ListX<String>,ListX<String>,ListX<Integer>> xors =Either4.sequence(ListX.of(just,none,Either4.right(1)));
       //Eitehr.right(ListX.of(10,1)));
     *
     * }</pre>
     *
     *
     *
     * @param Either3 Either3 to sequence
     * @return Either3 Sequenced
     */
    public static <LT1,LT2,LT3,LT4,PT> Either5<ListX<LT1>,ListX<LT2>,ListX<LT3>,ListX<LT4>,ListX<PT>> sequence(final CollectionX<Either5<LT1, LT2, LT3, LT4, PT>> xors) {
        Objects.requireNonNull(xors);
        return AnyM.sequence(xors.stream().filter(Either5::isRight).map(AnyM::fromEither5).to().listX(),Witness.either5.INSTANCE)
                .to(Witness::either5);
    }
    /**
     * Traverse a Collection of Either3 producing an Either4 with a ListX, applying the transformation function to every
     * element in the list
     *
     * @param xors Either4s to sequence and transform
     * @param fn Transformation function
     * @return An Either4 with a transformed list
     */
    public static <LT1,LT2, LT3,LT4,PT,R> Either5<ListX<LT1>,ListX<LT2>,ListX<LT3>,ListX<LT4>,ListX<R>> traverse(final CollectionX<Either5<LT1, LT2, LT3, LT4, PT>> xors, Function<? super PT, ? extends R> fn) {
        return  sequence(xors).map(l->l.map(fn));
    }


    /**
     *  Accumulate the results only from those Either3 which have a Right type present, using the supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see com.aol.cyclops2.Monoids }.
     *
     * <pre>
     * {@code
     * Either4<String,String,String,Integer> just  = Either4.right(10);
       Either4<String,String,String,Integer> none = Either4.left("none");
     *
     *  Either4<ListX<String>,ListX<String>,Integer> xors = Either4.accumulatePrimary(Monoids.intSum,ListX.of(just,none,Either4.right(1)));
        //Either4.right(11);
     *
     * }
     * </pre>
     *
     *
     *
     * @param xors Collection of Eithers to accumulate primary values
     * @param reducer  Reducer to accumulate results
     * @return  Either4 populated with the accumulate primary operation
     */
    public static <LT1,LT2,LT3,LT4, RT> Either5<ListX<LT1>, ListX<LT2>,ListX<LT3>,ListX<LT4>, RT> accumulate(final Monoid<RT> reducer, final CollectionX<Either5<LT1, LT2, LT3, LT4, RT>> xors) {
        return sequence(xors).map(s -> s.reduce(reducer));
    }



    /**
     * Lazily construct a Right Either from the supplied publisher
     * <pre>
     * {@code
     *   ReactiveSeq<Integer> reactiveStream =  ReactiveSeq.of(1,2,3);

         Either5<Throwable,String,String,String,Integer> lazy = Either5.fromPublisher(reactiveStream);

         //Either[1]
     *
     * }
     * </pre>
     * @param pub Publisher to construct an Either from
     * @return Either constructed from the supplied Publisher
     */
    public static <T1,T2,T3,T> Either5<Throwable, T1, T2,T3, T> fromPublisher(final Publisher<T> pub) {
        final ValueSubscriber<T> sub = ValueSubscriber.subscriber();
        pub.subscribe(sub);
        Either5<Throwable, T1,T2,T3, Xor<Throwable,T>> xor = Either5.rightEval(Eval.later(()->sub.toXor()));
        return  xor.flatMap(x->x.visit(Either5::left1,Either5::right));
    }
    /**
     * Construct a Right Either4 from the supplied Iterable
     * <pre>
     * {@code
     *   List<Integer> list =  Arrays.asList(1,2,3);

         Either4<Throwable,String,Integer> future = Either4.fromIterable(list);

         //Either4[1]
     *
     * }
     * </pre>
     * @param iterable Iterable to construct an Either from
     * @return Either constructed from the supplied Iterable
     */
    public static <ST, T, T2,T3,RT> Either5<ST, T,T2,T3,RT> fromIterable(final Iterable<RT> iterable) {

        final Iterator<RT> it = iterable.iterator();
        return it.hasNext() ? Either5.right( it.next()) : Either5.left1(null);
    }

    /**
     * Construct a Either4#Right from an Eval
     *
     * @param right Eval to construct Either4#Right from
     * @return Either4 right instance
     */
    public static <LT, M1,B, T4,RT> Either5<LT, M1,B,T4, RT> rightEval(final Eval<RT> right) {
        return new Right<>(
                           right);
    }

    /**
     * Construct a Either4#Left1 from an Eval
     *
     * @param left Eval to construct Either4#Left1 from
     * @return Either4 Left1 instance
     */
    public static <LT, M1, B, T4,RT> Either5<LT, M1, B,T4, RT> left1Eval(final Eval<LT> left) {
        return new Left1<>(
                          left);
    }

    /**
     * Construct a Either4#Right
     *
     * @param right Value to store
     * @return Either4 Right instance
     */
    public static <LT, M1, B, T4,RT> Either5<LT, M1, B, T4,RT> right(final RT right) {
        return new Right<>(
                           Eval.later(()->right));
    }

    /**
     * Construct a Either4#Left1
     *
     * @param left Value to store
     * @return Left1 instance
     */
    public static <LT, M1, B, T4,RT> Either5<LT, M1, B, T4,RT> left1(final LT left) {
        return new Left1<>(
                          Eval.now(left));
    }

    /**
     * Construct a Either4#Second
     *
     * @param middle Value to store
     * @return Second instance
     */
    public static <LT, M1, B, T4,RT> Either5<LT, M1, B, T4,RT> left2(final M1 middle) {
        return new Left2<>(
                            Eval.now(middle));
    }
    /**
     * Construct a Either4#Third
     *
     * @param middle Value to store
     * @return Third instance
     */
    public static <LT, M1, B, T4, RT> Either5<LT, M1, B,T4, RT> left3(final B middle) {
        return new Left3<>(
                            Eval.now(middle));
    }

    /**
     * Construct a Either4#Third
     *
     * @param middle Value to store
     * @return Third instance
     */
    public static <LT, M1, B, T4, RT> Either5<LT, M1, B,T4, RT> left4(final T4 middle) {
        return new Left4<>(
                            Eval.now(middle));
    }
    /**
     * Construct a Either4#Second from an Eval
     *
     * @param second Eval to construct Either4#middle from
     * @return Either4 second instance
     */
    public static <LT, M1, B, T4, RT> Either5<LT, M1, B, T4, RT> left2Eval(final Eval<M1> middle) {
        return new Left2<>(
                            middle);
    }
    /**
     * Construct a Either4#Third from an Eval
     *
     * @param third Eval to construct Either4#middle from
     * @return Either4 third instance
     */
    public static <LT, M1, B, T4, RT> Either5<LT, M1, B, T4, RT> left3Eval(final Eval<B> middle) {
        return new Left3<>(
                            middle);
    }
    /**
     * Construct a Either4#Third from an Eval
     *
     * @param third Eval to construct Either4#middle from
     * @return Either4 third instance
     */
    public static <LT, M1, B, T4, RT> Either5<LT, M1, B, T4, RT> left4Eval(final Eval<T4> middle) {
        return new Left4<>(
                            middle);
    }
    /**
     * Construct a Either4#Third from an Eval
     *
     * @param third Eval to construct Either4#middle from
     * @return Either4 third instance
     */
    public static <LT, M1, B, T4, RT> Either5<LT, M1, B, T4, RT> foEval(final Eval<B> middle) {
        return new Left3<>(
                            middle);
    }
    @Override
    default int arity() {
        return 5;
    }
    @Override
    default <R> Either5<LT1,LT2,LT3,LT4,R> zipWith(Iterable<Function<? super RT, ? extends R>> fn) {
        return (Either5<LT1,LT2,LT3,LT4,R>)MonadicValue.super.zipWith(fn);
    }

    @Override
    default <R> Either5<LT1,LT2,LT3,LT4,R> zipWithS(Stream<Function<? super RT, ? extends R>> fn) {
        return (Either5<LT1,LT2,LT3,LT4,R>)MonadicValue.super.zipWithS(fn);
    }

    @Override
    default <R> Either5<LT1,LT2,LT3,LT4,R> zipWithP(Publisher<Function<? super RT, ? extends R>> fn) {
        return (Either5<LT1,LT2,LT3,LT4,R>)MonadicValue.super.zipWithP(fn);
    }

    @Override
    default <R> Either5<LT1,LT2,LT3,LT4,R> retry(final Function<? super RT, ? extends R> fn) {
        return (Either5<LT1,LT2,LT3,LT4,R>)MonadicValue.super.retry(fn);
    }

    @Override
    default <U> Either5<LT1,LT2,LT3,LT4,Tuple2<RT, U>> zipP(final Publisher<? extends U> other) {
        return (Either5)MonadicValue.super.zipP(other);
    }

    @Override
    default <R> Either5<LT1,LT2,LT3,LT4,R> retry(final Function<? super RT, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (Either5<LT1,LT2,LT3,LT4,R>)MonadicValue.super.retry(fn,retries,delay,timeUnit);
    }

    @Override
    default <S, U> Either5<LT1,LT2,LT3,LT4,Tuple3<RT, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {
        return (Either5)MonadicValue.super.zip3(second,third);
    }

    @Override
    default <S, U, R> Either5<LT1,LT2,LT3,LT4,R> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third, final Fn3<? super RT, ? super S, ? super U, ? extends R> fn3) {
        return (Either5<LT1,LT2,LT3,LT4,R>)MonadicValue.super.zip3(second,third,fn3);
    }

    @Override
    default <T2, T3, T4> Either5<LT1,LT2,LT3,LT4,Tuple4<RT, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth) {
        return (Either5)MonadicValue.super.zip4(second,third,fourth);
    }

    @Override
    default <T2, T3, T4, R> Either5<LT1,LT2,LT3,LT4,R> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Fn4<? super RT, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (Either5<LT1,LT2,LT3,LT4,R>)MonadicValue.super.zip4(second,third,fourth,fn);
    }

    @Override
    default <R> Either5<LT1,LT2,LT3,LT4,R> flatMapS(final Function<? super RT, ? extends Stream<? extends R>> mapper) {
        return (Either5<LT1,LT2,LT3,LT4,R>)MonadicValue.super.flatMapS(mapper);
    }

    default < RT1> Either5<LT1, LT2, LT3, LT4, RT1> flatMapI(Function<? super RT, ? extends Iterable<? extends RT1>> mapper){
        return this.flatMap(a -> {
            return Either5.fromIterable(mapper.apply(a));

        });
    }
    default < RT1> Either5<LT1, LT2, LT3,LT4,RT1> flatMapP(Function<? super RT, ? extends Publisher<? extends RT1>> mapper){
        return this.flatMap(a -> {
            final Publisher<? extends RT1> publisher = mapper.apply(a);
            final ValueSubscriber<RT1> sub = ValueSubscriber.subscriber();

            publisher.subscribe(sub);
            return unit(sub.get());

        });
    }

    /**
     * Visit the types in this Either4, only one user supplied function is executed depending on the type
     *
     * @param left1 Function to execute if this Either4 is a Left1 instance
     * @param left2 Function to execute if this Either4 is a Left2 instance
     * @param left3 Function to execute if this Either4 is a Left3 instance
     * @param right Function to execute if this Either4 is a right instance
     * @return Result of executed function
     */
    <R> R visit(final Function<? super LT1, ? extends R> left1, final Function<? super LT2, ? extends R> left2
            , final Function<? super LT3, ? extends R> left3,
                final Function<? super LT4, ? extends R> left4,
                final Function<? super RT, ? extends R> right);

    /**
     * Filter this Either4 resulting in a Maybe#none if it is not a Right instance or if the predicate does not
     * hold. Otherwise results in a Maybe containing the current value
     *
     * @param test Predicate to applyHKT to filter this Either4
     * @return Maybe containing the current value if this is a Right instance and the predicate holds, otherwise Maybe#none
     */
    Maybe<RT> filter(Predicate<? super RT> test);

    /**
     * Flattening transformation on this Either4. Contains an internal trampoline so will convert tail-recursive calls
     * to iteration.
     *
     * @param mapper Mapping function
     * @return Mapped Either4
     */
    < RT1> Either5<LT1, LT2,LT3, LT4,RT1> flatMap(
            Function<? super RT, ? extends MonadicValue<? extends RT1>> mapper);

    /**
     * @return Swap the fourth and the right types
     */
    Either5<LT1,LT2, LT3,RT,LT4> swap4();
    /**
     * @return Swap the third and the right types
     */
    Either5<LT1,LT2, RT,LT4, LT3> swap3();
    /**
     * @return Swap the second and the right types
     */
    Either5<LT1, RT,LT3, LT4, LT2> swap2();

    /**
     * @return Swap the right and left types
     */
    Either5<RT, LT2,LT3, LT4,LT1> swap1();

    /**
     * @return True if this lazy contains the right type
     */
    boolean isRight();

    /**
     * @return True if this lazy contains the left1 type
     */
    boolean isLeft1();

    /**
     * @return True if this lazy contains the left2 type
     */
    boolean isLeft2();
    /**
     * @return True if this lazy contains the left3 type
     */
    boolean isLeft3();
    /**
     * @return True if this lazy contains the left4 type
     */
    public boolean isLeft4();


    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Filters#ofType(java.lang.Class)
     */
    @Override
    default <U> Maybe<U> ofType(Class<? extends U> type) {

        return (Maybe<U>)MonadicValue.super.ofType(type);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Filters#filterNot(java.util.function.Predicate)
     */
    @Override
    default Maybe<RT> filterNot(Predicate<? super RT> predicate) {

        return (Maybe<RT>)MonadicValue.super.filterNot(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Filters#notNull()
     */
    @Override
    default Maybe<RT> notNull() {

        return (Maybe<RT>)MonadicValue.super.notNull();
    }
    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.functor.BiTransformable#bimap(java.util.function.Function,
     * java.util.function.Function)
     */
    @Override
    <R1, R2> Either5<LT1, LT2, LT3,R1, R2> bimap(Function<? super LT4, ? extends R1> fn1,
                                                 Function<? super RT, ? extends R2> fn2);

    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.functor.Transformable#map(java.util.function.Function)
     */
    @Override
    <R> Either5<LT1,LT2,LT3, LT4, R> map(Function<? super RT, ? extends R> fn);

    /**
     * Return an Ior that can be this object or a Ior.primary or Ior.secondary
     * @return new Ior
     */
     default Ior<LT1, RT> toIor() {
        return this.visit(l->Ior.secondary(l),
                          m->Ior.secondary(null),
                          m->Ior.secondary(null),
                          m->Ior.secondary(null),
                          r->Ior.primary(r));
    }
     default Xor<LT1, RT> toXor() {
         return this.visit(l->Xor.secondary(l),
                           m->Xor.secondary(null),
                           m->Xor.secondary(null),
                           m->Xor.secondary(null),
                           r->Xor.primary(r));
     }




    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#coflatMap(java.util.function.Function)
     */
    @Override
    default <R> Either5<LT1,LT2,LT3,LT4,R> coflatMap(Function<? super MonadicValue<RT>, R> mapper) {

        return (Either5<LT1,LT2,LT3,LT4,R>)MonadicValue.super.coflatMap(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#nest()
     */
    @Override
    default Either5<LT1,LT2,LT3,LT4,MonadicValue<RT>> nest() {

        return (Either5<LT1,LT2,LT3,LT4,MonadicValue<RT>>)MonadicValue.super.nest();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    default <T2, R1, R2, R3, R> Either5<LT1,LT2,LT3,LT4,R> forEach4(Function<? super RT, ? extends MonadicValue<R1>> value1,
                                                                    BiFunction<? super RT, ? super R1, ? extends MonadicValue<R2>> value2,
                                                                    Fn3<? super RT, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
                                                                    Fn4<? super RT, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return (Either5<LT1,LT2,LT3,LT4,R>)MonadicValue.super.forEach4(value1, value2, value3, yieldingFunction);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    default <T2, R1, R2, R3, R> Either5<LT1,LT2,LT3,LT4,R> forEach4(Function<? super RT, ? extends MonadicValue<R1>> value1,
                                                                    BiFunction<? super RT, ? super R1, ? extends MonadicValue<R2>> value2,
                                                                    Fn3<? super RT, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
                                                                    Fn4<? super RT, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                                    Fn4<? super RT, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return (Either5<LT1,LT2,LT3,LT4,R>)MonadicValue.super.forEach4(value1, value2, value3, filterFunction, yieldingFunction);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    default <T2, R1, R2, R> Either5<LT1,LT2,LT3,LT4,R> forEach3(Function<? super RT, ? extends MonadicValue<R1>> value1,
                                                                BiFunction<? super RT, ? super R1, ? extends MonadicValue<R2>> value2,
                                                                Fn3<? super RT, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (Either5<LT1,LT2,LT3,LT4,R>)MonadicValue.super.forEach3(value1, value2, yieldingFunction);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    default <T2, R1, R2, R> Either5<LT1,LT2,LT3,LT4,R> forEach3(Function<? super RT, ? extends MonadicValue<R1>> value1,
                                                                BiFunction<? super RT, ? super R1, ? extends MonadicValue<R2>> value2,
                                                                Fn3<? super RT, ? super R1, ? super R2, Boolean> filterFunction,
                                                                Fn3<? super RT, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (Either5<LT1,LT2,LT3,LT4,R>)MonadicValue.super.forEach3(value1, value2, filterFunction, yieldingFunction);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> Either5<LT1,LT2,LT3,LT4,R> forEach2(Function<? super RT, ? extends MonadicValue<R1>> value1,
                                                        BiFunction<? super RT, ? super R1, ? extends R> yieldingFunction) {

        return (Either5<LT1,LT2,LT3,LT4,R>)MonadicValue.super.forEach2(value1, yieldingFunction);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> Either5<LT1,LT2,LT3,LT4,R> forEach2(Function<? super RT, ? extends MonadicValue<R1>> value1,
                                                        BiFunction<? super RT, ? super R1, Boolean> filterFunction,
                                                        BiFunction<? super RT, ? super R1, ? extends R> yieldingFunction) {

        return (Either5<LT1,LT2,LT3,LT4,R>)MonadicValue.super.forEach2(value1, filterFunction, yieldingFunction);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#combineEager(com.aol.cyclops2.Monoid, com.aol.cyclops2.types.MonadicValue)
     */
    @Override
    default Either5<LT1,LT2,LT3,LT4,RT> combineEager(Monoid<RT> monoid, MonadicValue<? extends RT> v2) {

        return (Either5<LT1,LT2,LT3,LT4,RT>)MonadicValue.super.combineEager(monoid, v2);
    }
    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.Combiner#combine(com.aol.cyclops2.types.Value,
     * java.util.function.BiFunction)
     */
    @Override
    default <T2, R> Either5<LT1, LT2, LT3,LT4, R> combine(final Value<? extends T2> app,
                                                          final BiFunction<? super RT, ? super T2, ? extends R> fn) {

        return (Either5<LT1, LT2, LT3, LT4, R>) MonadicValue.super.combine(app, fn);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.aol.cyclops2.types.Combiner#combine(java.util.function.BinaryOperator,
     * com.aol.cyclops2.types.Combiner)
     */
    @Override
    default Either5<LT1, LT2, LT3,LT4, RT> zip(final BinaryOperator<Zippable<RT>> combiner, final Zippable<RT> app) {

        return (Either5<LT1, LT2, LT3, LT4, RT>) MonadicValue.super.zip(combiner, app);
    }



    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.Zippable#zip(java.util.reactiveStream.Stream,
     * java.util.function.BiFunction)
     */
    @Override
    default <U, R> Either5<LT1, LT2, LT3, LT4, R> zipS(final Stream<? extends U> other,
                                                      final BiFunction<? super RT, ? super U, ? extends R> zipper) {

        return (Either5<LT1, LT2, LT3, LT4, R>) MonadicValue.super.zipS(other, zipper);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.Zippable#zip(java.util.reactiveStream.Stream)
     */
    @Override
    default <U> Either5<LT1, LT2, LT3, LT4, Tuple2<RT, U>> zipS(final Stream<? extends U> other) {

        return (Either5) MonadicValue.super.zipS(other);
    }



    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.Zippable#zip(java.lang.Iterable)
     */
    @Override
    default <U> Either5<LT1, LT2, LT3, LT4, Tuple2<RT, U>> zip(final Iterable<? extends U> other) {

        return (Either5) MonadicValue.super.zip(other);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.Pure#unit(java.lang.Object)
     */
    @Override
    <T> Either5<LT1, LT2,LT3,LT4, T> unit(T unit);

    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.applicative.ApplicativeFunctor#zip(java.lang.
     * Iterable, java.util.function.BiFunction)
     */
    @Override
    default <T2, R> Either5<LT1, LT2, LT3,LT4, R> zip(final Iterable<? extends T2> app,
                                                      final BiFunction<? super RT, ? super T2, ? extends R> fn) {

        return (Either5<LT1, LT2, LT3, LT4, R>) MonadicValue.super.zip(app, fn);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.applicative.ApplicativeFunctor#zip(java.util.
     * function.BiFunction, org.reactivestreams.Publisher)
     */
    @Override
    default <T2, R> Either5<LT1, LT2,LT3,LT4, R> zipP(final Publisher<? extends T2> app,
                                                      final BiFunction<? super RT, ? super T2, ? extends R> fn) {

        return (Either5<LT1, LT2, LT3,LT4, R>) MonadicValue.super.zipP(app,fn);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.functor.BiTransformable#bipeek(java.util.function.Consumer,
     * java.util.function.Consumer)
     */
    @Override
    default Either5<LT1, LT2, LT3, LT4, RT> bipeek(final Consumer<? super LT4> c1, final Consumer<? super RT> c2) {

        return (Either5<LT1, LT2, LT3,LT4, RT>) BiTransformable.super.bipeek(c1, c2);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.functor.BiTransformable#bicast(java.lang.Class,
     * java.lang.Class)
     */
    @Override
    default <U1, U2> Either5<LT1, LT2, LT3, U1, U2> bicast(final Class<U1> type1, final Class<U2> type2) {

        return (Either5<LT1, LT2,LT3, U1, U2>) BiTransformable.super.bicast(type1, type2);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.aol.cyclops2.types.functor.BiTransformable#bitrampoline(java.util.function.Function,
     * java.util.function.Function)
     */
    @Override
    default <R1, R2> Either5<LT1, LT2, LT3, R1, R2> bitrampoline(
            final Function<? super LT4, ? extends Trampoline<? extends R1>> mapper1,
            final Function<? super RT, ? extends Trampoline<? extends R2>> mapper2) {

        return (Either5<LT1,LT2,LT3, R1, R2>) BiTransformable.super.bitrampoline(mapper1, mapper2);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.functor.Transformable#cast(java.lang.Class)
     */
    @Override
    default <U> Either5<LT1, LT2, LT3, LT4, U> cast(final Class<? extends U> type) {

        return (Either5<LT1, LT2, LT3,LT4, U>) MonadicValue.super.cast(type);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.functor.Transformable#peek(java.util.function.Consumer)
     */
    @Override
    default Either5<LT1, LT2, LT3,LT4, RT> peek(final Consumer<? super RT> c) {

        return (Either5<LT1, LT2, LT3, LT4, RT>) MonadicValue.super.peek(c);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.aol.cyclops2.types.functor.Transformable#trampoline(java.util.function.Function)
     */
    @Override
    default <R> Either5<LT1, LT2, LT3,LT4, R> trampoline(final Function<? super RT, ? extends Trampoline<? extends R>> mapper) {

        return (Either5<LT1, LT2, LT3,LT4, R>) MonadicValue.super.trampoline(mapper);
    }



    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    final static class Lazy<ST, M,M2,M3, PT> implements Either5<ST, M,M2,M3, PT> {

        private final Eval<Either5<ST, M,M2,M3, PT>> lazy;

        public Either5<ST, M,M2,M3, PT> resolve() {
            return lazy.get()
                       .visit(Either5::left1, Either5::left2,Either5::left3,Either5::left4, Either5::right);
        }

        private static <ST, M,M2,M3, PT> Lazy<ST, M,M2, M3,PT> lazy(final Eval<Either5<ST, M,M2,M3, PT>> lazy) {
            return new Lazy<>(lazy);
        }


        @Override
        public <R> Either5<ST, M,M2,M3, R> map(final Function<? super PT, ? extends R> mapper) {
            return flatMap(t -> Either5.right(mapper.apply(t)));
        }

        @Override
        public <RT1> Either5<ST, M,M2,M3, RT1> flatMap(
                final Function<? super PT, ? extends MonadicValue<? extends RT1>> mapper) {
            return Either5.fromLazy(lazy.map(m->m.flatMap(mapper)));
        }

        @Override
        public Maybe<PT> filter(final Predicate<? super PT> test) {

            return Maybe.fromEval(Eval.later(() -> resolve().filter(test)))
                        .flatMap(Function.identity());

        }

        @Override
        public PT get() {
            return trampoline().get();
        }

        private Either5<ST,M,M2,M3,PT> trampoline(){
            Either5<ST,M,M2,M3,PT> maybe = lazy.get();
            while (maybe instanceof Lazy) {
                maybe = ((Lazy<ST,M,M2,M3,PT>) maybe).lazy.get();
            }
            return maybe;
        }
        @Override
        public ReactiveSeq<PT> stream() {

            return trampoline()
                       .stream();
        }

        @Override
        public Iterator<PT> iterator() {

            return trampoline()
                       .iterator();
        }

        @Override
        public <R> R visit(final Function<? super PT, ? extends R> present, final Supplier<? extends R> absent) {

            return trampoline()
                       .visit(present, absent);
        }

        @Override
        public void subscribe(final Subscriber<? super PT> s) {

            lazy.get()
                .subscribe(s);
        }

        @Override
        public boolean test(final PT t) {
            return trampoline()
                       .test(t);
        }

        @Override
        public <R> R visit(final Function<? super ST, ? extends R> first,
                final Function<? super M, ? extends R> second,
                final Function<? super M2, ? extends R> third,
                final Function<? super M3, ? extends R> fourth,
                final Function<? super PT, ? extends R> primary) {

            return trampoline()
                       .visit(first, second,third,fourth, primary);
        }
        @Override
        public Either5<ST, M, M2,PT, M3> swap4() {
            return lazy(Eval.later(() -> resolve().swap4()));
        }
        @Override
        public Either5<ST, M, PT, M3, M2> swap3() {
            return lazy(Eval.later(() -> resolve().swap3()));
        }
        @Override
        public Either5<ST, PT, M2, M3, M> swap2() {
            return lazy(Eval.later(() -> resolve().swap2()));
        }

        @Override
        public Either5<PT, M,M2, M3,ST> swap1() {
            return lazy(Eval.later(() -> resolve().swap1()));
        }

        @Override
        public boolean isRight() {
            return trampoline()
                       .isRight();
        }

        @Override
        public boolean isLeft1() {
            return trampoline()
                       .isLeft1();
        }

        @Override
        public boolean isLeft2() {
            return trampoline()
                       .isLeft2();
        }
        @Override
        public boolean isLeft3() {
            return trampoline()
                       .isLeft3();
        }
        @Override
        public boolean isLeft4() {
            return trampoline()
                       .isLeft4();
        }

        @Override
        public <R1, R2> Either5<ST, M,M2,R1, R2> bimap(final Function<? super M3, ? extends R1> fn1,
                final Function<? super PT, ? extends R2> fn2) {
            return lazy(Eval.later(() -> resolve().bimap(fn1, fn2)));
        }

        @Override
        public <T> Either5<ST, M, M2,M3,T> unit(final T unit) {

            return Either5.right(unit);
        }
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return this.visit(Either5::left1,Either5::left2,Either5::left3,Either5::left4,Either5::right).hashCode();
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            return this.visit(Either5::left1,Either5::left2,Either5::left3,Either5::left4,Either5::right).equals(obj);
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return trampoline().toString();
        }

    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class Right<ST, M,M2,M3, PT> implements Either5<ST, M,M2,M3, PT> {
        private final Eval<PT> value;

        @Override
        public <R> Either5<ST, M, M2, M3, R> map(final Function<? super PT, ? extends R> fn) {
            return new Right<ST, M, M2,M3, R>(
                                       value.map(fn));
        }

        @Override
        public Either5<ST, M,M2,M3, PT> peek(final Consumer<? super PT> action) {
            return map(i -> {
                action.accept(i);
                return i;
            });

        }

        @Override
        public Maybe<PT> filter(final Predicate<? super PT> test) {

            return Maybe.fromEval(Eval.later(() -> test.test(get()) ? Maybe.just(get()) : Maybe.<PT> none()))
                        .flatMap(Function.identity());

        }

        @Override
        public PT get() {
            return value.get();
        }

        @Override
        public <RT1> Either5<ST, M, M2, M3, RT1> flatMap(
                final Function<? super PT, ? extends MonadicValue<? extends RT1>> mapper) {
            Eval<? extends Either5<? extends ST, ? extends M, ? extends M2,? extends M3, ? extends RT1>> et = value.map(mapper.andThen(Either5::fromMonadicValue));


           final Eval<Either5<ST, M, M2,M3, RT1>> e3 =  (Eval<Either5<ST, M, M2,M3, RT1>>)et;
           return new Lazy<>(
                             e3);


        }

        @Override
        public boolean isRight() {
            return true;
        }

        @Override
        public boolean isLeft1() {
            return false;
        }

        @Override
        public String toString() {
            return mkString();
        }

        @Override
        public String mkString() {
            return "Either5.right[" + value.get() + "]";
        }

        @Override
        public <R> R visit(final Function<? super ST, ? extends R> secondary,
                final Function<? super M, ? extends R> mid,
                final Function<? super M2, ? extends R> mid2,
                final Function<? super M3, ? extends R> mid3,
                final Function<? super PT, ? extends R> primary) {
            return primary.apply(value.get());
        }

        /*
         * (non-Javadoc)
         *
         * @see com.aol.cyclops2.types.applicative.ApplicativeFunctor#ap(com.aol.
         * cyclops2.types.Value, java.util.function.BiFunction)
         */
        @Override
        public <T2, R> Either5<ST, M,M2, M3, R> combine(final Value<? extends T2> app,
                final BiFunction<? super PT, ? super T2, ? extends R> fn) {
            return new Right<>(
                               value.combine(app, fn));

        }

        @Override
        public <R1, R2> Either5<ST,M, M2,R1, R2> bimap(final Function<? super M3, ? extends R1> fn1,
                final Function<? super PT, ? extends R2> fn2) {
            return (Either5<ST, M,M2,R1, R2>) this.map(fn2);
        }

        @Override
        public ReactiveSeq<PT> stream() {
            return value.stream();
        }

        @Override
        public Iterator<PT> iterator() {
            return value.iterator();
        }

        @Override
        public <R> R visit(final Function<? super PT, ? extends R> present, final Supplier<? extends R> absent) {
            return value.visit(present, absent);
        }

        @Override
        public void subscribe(final Subscriber<? super PT> s) {
            value.subscribe(s);

        }

        @Override
        public boolean test(final PT t) {
            return value.test(t);
        }

        @Override
        public <T> Either5<ST, M, M2, M3, T> unit(final T unit) {
            return Either5.right(unit);
        }
        @Override
        public Either5<ST,  M, M2,PT,M3> swap4() {

            return  new Left4<>(value);
        }
        @Override
        public Either5<ST,  M, PT,M3, M2> swap3() {

            return  new Left3<>(value);
        }


        @Override
        public Either5<ST, PT, M2, M3, M> swap2() {

            return  new Left2<>(value);
        }


        @Override
        public Either5<PT, M,M2,M3, ST> swap1() {

            return new Left1<>(
                              value);
        }

        @Override
        public boolean isLeft2() {

            return false;
        }
        @Override
        public boolean isLeft3() {

            return false;
        }

        @Override
        public boolean isLeft4() {

            return false;
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
            if(obj instanceof Lazy){
                return ((Lazy)obj).equals(this);
            }
            if (getClass() != obj.getClass())
                return false;
            Right other = (Right) obj;
            if (value == null) {
                if (other.value != null)
                    return false;
            } else if (!value.equals(other.value))
                return false;
            return true;
        }


    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class Left1<ST, M, M2, M3,PT> implements Either5<ST, M,M2, M3, PT> {
        private final Eval<ST> value;

        @Override
        public <R> Either5<ST, M, M2,M3, R> map(final Function<? super PT, ? extends R> fn) {
            return (Either5<ST, M, M2, M3,R>) this;
        }

        @Override
        public Either5<ST, M, M2,M3, PT> peek(final Consumer<? super PT> action) {
            return this;

        }

        @Override
        public Maybe<PT> filter(final Predicate<? super PT> test) {

            return Maybe.none();

        }

        @Override
        public PT get() {
            throw new NoSuchElementException(
                                             "Attempt to access right value on a Left Either4");
        }

        @Override
        public <RT1> Either5<ST, M, M2, M3,RT1> flatMap(
                final Function<? super PT, ? extends MonadicValue<? extends RT1>> mapper) {

            return (Either5) this;

        }

        @Override
        public boolean isRight() {
            return false;
        }

        @Override
        public boolean isLeft1() {
            return true;
        }
        @Override
        public boolean isLeft3() {

            return false;
        }
        @Override
        public boolean isLeft4() {

            return false;
        }

        @Override
        public String toString() {
            return mkString();
        }

        @Override
        public String mkString() {
            return "Either5.left1[" + value.get() + "]";
        }

        @Override
        public <R> R visit(final Function<? super ST, ? extends R> secondary,
                final Function<? super M, ? extends R> mid,
                final Function<? super M2, ? extends R> mid2,
                final Function<? super M3, ? extends R> mid3,
                final Function<? super PT, ? extends R> primary) {
            return secondary.apply(value.get());
        }

        /*
         * (non-Javadoc)
         *
         * @see com.aol.cyclops2.types.applicative.ApplicativeFunctor#ap(com.aol.
         * cyclops2.types.Value, java.util.function.BiFunction)
         */
        @Override
        public <T2, R> Either5<ST, M, M2, M3, R> combine(final Value<? extends T2> app,
                final BiFunction<? super PT, ? super T2, ? extends R> fn) {
            return (Either5<ST, M,M2,M3, R>) this;

        }

        @Override
        public <R1, R2> Either5<ST, M,M2,R1, R2> bimap(final Function<? super M3, ? extends R1> fn1,
                final Function<? super PT, ? extends R2> fn2) {
            return (Either5<ST,M, M2,R1, R2>) this;
        }

        @Override
        public ReactiveSeq<PT> stream() {
            return ReactiveSeq.empty();
        }

        @Override
        public Iterator<PT> iterator() {
            return Arrays.<PT> asList()
                         .iterator();
        }

        @Override
        public <R> R visit(final Function<? super PT, ? extends R> present, final Supplier<? extends R> absent) {
            return absent.get();
        }

        @Override
        public void subscribe(final Subscriber<? super PT> s) {

        }

        @Override
        public boolean test(final PT t) {
            return false;
        }

        @Override
        public <T> Either5<ST, M,M2,M3, T> unit(final T unit) {
            return Either5.right(unit);
        }
        @Override
        public Either5<ST, M,M2,PT,M3> swap4() {

            return (Either5<ST, M,M2,PT,M3>) this;
        }
        @Override
        public Either5<ST, M,PT,M3, M2> swap3() {

            return (Either5<ST, M,PT,M3, M2>) this;
        }

        @Override
        public Either5<ST, PT,M2,M3, M> swap2() {

            return (Either5<ST, PT,M2,M3, M>) this;
        }

        @Override
        public Either5<PT, M,M2,M3, ST> swap1() {

            return new Right<>(
                               value);
        }

        @Override
        public boolean isLeft2() {

            return false;
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
            if(obj instanceof Lazy){
                return ((Lazy)obj).equals(this);
            }
            if (getClass() != obj.getClass())
                return false;
            Left1 other = (Left1) obj;
            if (value == null) {
                if (other.value != null)
                    return false;
            } else if (!value.equals(other.value))
                return false;
            return true;
        }


    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class Left2<ST, M,M2, M3, PT> implements Either5<ST, M, M2, M3, PT> {
        private final Eval<M> value;

        @Override
        public <R> Either5<ST, M, M2, M3,R> map(final Function<? super PT, ? extends R> fn) {
            return (Either5<ST, M, M2,M3,R>) this;
        }

        @Override
        public Either5<ST, M, M2,M3, PT> peek(final Consumer<? super PT> action) {
            return this;

        }

        @Override
        public Maybe<PT> filter(final Predicate<? super PT> test) {

            return Maybe.none();

        }

        @Override
        public PT get() {
            throw new NoSuchElementException(
                                             "Attempt to access right value on a Middle Either4");
        }

        @Override
        public <RT1> Either5<ST, M, M2, M3, RT1> flatMap(
                final Function<? super PT, ? extends MonadicValue<? extends RT1>> mapper) {

            return (Either5) this;

        }

        @Override
        public boolean isRight() {
            return false;
        }

        @Override
        public boolean isLeft1() {
            return false;
        }
        @Override
        public boolean isLeft4() {
            return false;
        }


        @Override
        public String toString() {
            return mkString();
        }

        @Override
        public String mkString() {
            return "Either5.left2[" + value.get() + "]";
        }

        @Override
        public <R> R visit(final Function<? super ST, ? extends R> secondary,
                final Function<? super M, ? extends R> mid1,
                final Function<? super M2, ? extends R> mid2,
                final Function<? super M3, ? extends R> mid3,
                final Function<? super PT, ? extends R> primary) {
            return mid1.apply(value.get());
        }

        /*
         * (non-Javadoc)
         *
         * @see com.aol.cyclops2.types.applicative.ApplicativeFunctor#ap(com.aol.
         * cyclops2.types.Value, java.util.function.BiFunction)
         */
        @Override
        public <T2, R> Either5<ST, M, M2,M3,R> combine(final Value<? extends T2> app,
                final BiFunction<? super PT, ? super T2, ? extends R> fn) {
            return (Either5<ST, M, M2,M3,R>) this;

        }

        @Override
        public <R1, R2> Either5<ST, M, M2,R1, R2> bimap(final Function<? super M3, ? extends R1> fn1,
                final Function<? super PT, ? extends R2> fn2) {
            return (Either5<ST, M,M2,R1, R2>) this;
        }

        @Override
        public ReactiveSeq<PT> stream() {
            return ReactiveSeq.empty();
        }

        @Override
        public Iterator<PT> iterator() {
            return Arrays.<PT> asList()
                         .iterator();
        }

        @Override
        public <R> R visit(final Function<? super PT, ? extends R> present, final Supplier<? extends R> absent) {
            return absent.get();
        }

        @Override
        public void subscribe(final Subscriber<? super PT> s) {

        }

        @Override
        public boolean test(final PT t) {
            return false;
        }

        @Override
        public <T> Either5<ST, M,M2,M3, T> unit(final T unit) {
            return Either5.right(unit);
        }
        @Override
        public Either5<ST, M, M2,PT,M3> swap4() {
            return (Either5<ST, M, M2,PT,M3>) this;

        }
        @Override
        public Either5<ST, M, PT,M3,M2> swap3() {
            return (Either5<ST, M, PT,M3,M2>) this;

        }
        @Override
        public Either5<ST, PT,M2, M3, M> swap2() {
            return new Right<>(
                               value);

        }

        @Override
        public Either5<PT, M, M2,M3,ST> swap1() {
            return (Either5<PT, M,M2, M3, ST>) this;

        }

        @Override
        public boolean isLeft2() {

            return true;
        }
        @Override
        public boolean isLeft3() {

            return false;
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
            if(obj instanceof Lazy){
                return ((Lazy)obj).equals(this);
            }
            if (getClass() != obj.getClass())
                return false;
            Left2 other = (Left2) obj;
            if (value == null) {
                if (other.value != null)
                    return false;
            } else if (!value.equals(other.value))
                return false;
            return true;
        }


    }
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class Left3<ST, M,M2, M3, PT> implements Either5<ST, M, M2, M3,PT> {
        private final Eval<M2> value;

        @Override
        public <R> Either5<ST, M, M2, M3,R> map(final Function<? super PT, ? extends R> fn) {
            return (Either5<ST, M, M2,M3,R>) this;
        }

        @Override
        public Either5<ST, M, M2,M3, PT> peek(final Consumer<? super PT> action) {
            return this;

        }

        @Override
        public Maybe<PT> filter(final Predicate<? super PT> test) {

            return Maybe.none();

        }

        @Override
        public PT get() {
            throw new NoSuchElementException(
                                             "Attempt to access right value on a Middle Either4");
        }

        @Override
        public <RT1> Either5<ST, M, M2, M3,RT1> flatMap(
                final Function<? super PT, ? extends MonadicValue<
                                                ? extends RT1>> mapper) {

            return (Either5) this;

        }

        @Override
        public boolean isRight() {
            return false;
        }

        @Override
        public boolean isLeft1() {
            return false;
        }
        @Override
        public boolean isLeft3() {

            return true;
        }
        @Override
        public boolean isLeft4() {

            return false;
        }
        @Override
        public String toString() {
            return mkString();
        }

        @Override
        public String mkString() {
            return "Either5.left3[" + value.get() + "]";
        }

        @Override
        public <R> R visit(final Function<? super ST, ? extends R> secondary,
                final Function<? super M, ? extends R> mid1,
                final Function<? super M2, ? extends R> mid2,
                final Function<? super M3, ? extends R> mid3,
                final Function<? super PT, ? extends R> primary) {
            return mid2.apply(value.get());
        }

        /*
         * (non-Javadoc)
         *
         * @see com.aol.cyclops2.types.applicative.ApplicativeFunctor#ap(com.aol.
         * cyclops2.types.Value, java.util.function.BiFunction)
         */
        @Override
        public <T2, R> Either5<ST, M, M2,M3,R> combine(final Value<? extends T2> app,
                final BiFunction<? super PT, ? super T2, ? extends R> fn) {
            return (Either5<ST, M, M2,M3,R>) this;

        }

        @Override
        public <R1, R2> Either5<ST, M, M2,R1, R2> bimap(final Function<? super M3, ? extends R1> fn1,
                final Function<? super PT, ? extends R2> fn2) {
            return (Either5<ST, M,M2,R1, R2>) this;
        }

        @Override
        public ReactiveSeq<PT> stream() {
            return ReactiveSeq.empty();
        }

        @Override
        public Iterator<PT> iterator() {
            return Arrays.<PT> asList()
                         .iterator();
        }

        @Override
        public <R> R visit(final Function<? super PT, ? extends R> present, final Supplier<? extends R> absent) {
            return absent.get();
        }

        @Override
        public void subscribe(final Subscriber<? super PT> s) {

        }

        @Override
        public boolean test(final PT t) {
            return false;
        }

        @Override
        public <T> Either5<ST, M,M2,M3, T> unit(final T unit) {
            return Either5.right(unit);
        }
        @Override
        public Either5<ST, M,M2,PT,M3> swap4() {
           return (Either5<ST, M,M2,PT,M3>)this;

        }
        @Override
        public Either5<ST, M, PT,M3, M2> swap3() {
            return new Right<>(
                    value);

        }
        @Override
        public Either5<ST, PT,M2, M3, M> swap2() {
           return (Either5<ST, PT,M2, M3, M>)this;

        }

        @Override
        public Either5<PT, M, M2, M3, ST> swap1() {
            return (Either5<PT, M,M2, M3, ST>) this;

        }

        @Override
        public boolean isLeft2() {

            return false;
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
            if(obj instanceof Lazy){
                return ((Lazy)obj).equals(this);
            }
            if (getClass() != obj.getClass())
                return false;
            Left3 other = (Left3) obj;
            if (value == null) {
                if (other.value != null)
                    return false;
            } else if (!value.equals(other.value))
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
    static class Left4<ST, M,M2, M3, PT> implements Either5<ST, M, M2, M3,PT> {
        private final Eval<M3> value;

        @Override
        public <R> Either5<ST, M, M2, M3,R> map(final Function<? super PT, ? extends R> fn) {
            return (Either5<ST, M, M2,M3,R>) this;
        }

        @Override
        public Either5<ST, M, M2,M3, PT> peek(final Consumer<? super PT> action) {
            return this;

        }

        @Override
        public Maybe<PT> filter(final Predicate<? super PT> test) {

            return Maybe.none();

        }

        @Override
        public PT get() {
            throw new NoSuchElementException(
                                             "Attempt to access right value on a Middle Either4");
        }

        @Override
        public <RT1> Either5<ST, M, M2, M3,RT1> flatMap(
                final Function<? super PT, ? extends MonadicValue<? extends RT1>> mapper) {

            return (Either5) this;

        }

        @Override
        public boolean isRight() {
            return false;
        }

        @Override
        public boolean isLeft1() {
            return false;
        }
        @Override
        public boolean isLeft3() {

            return false;
        }
        @Override
        public boolean isLeft4() {

            return true;
        }
        @Override
        public String toString() {
            return mkString();
        }

        @Override
        public String mkString() {
            return "Either5.left4[" + value.get() + "]";
        }

        @Override
        public <R> R visit(final Function<? super ST, ? extends R> secondary,
                final Function<? super M, ? extends R> mid1,
                final Function<? super M2, ? extends R> mid2, 
                final Function<? super M3, ? extends R> mid3, 
                final Function<? super PT, ? extends R> primary) {
            return mid3.apply(value.get());
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.aol.cyclops2.types.applicative.ApplicativeFunctor#ap(com.aol.
         * cyclops2.types.Value, java.util.function.BiFunction)
         */
        @Override
        public <T2, R> Either5<ST, M, M2,M3,R> combine(final Value<? extends T2> app,
                final BiFunction<? super PT, ? super T2, ? extends R> fn) {
            return (Either5<ST, M, M2,M3,R>) this;

        }

        @Override
        public <R1, R2> Either5<ST, M, M2,R1, R2> bimap(final Function<? super M3, ? extends R1> fn1,
                final Function<? super PT, ? extends R2> fn2) {
            return (Either5<ST, M,M2,R1, R2>) this;
        }

        @Override
        public ReactiveSeq<PT> stream() {
            return ReactiveSeq.empty();
        }

        @Override
        public Iterator<PT> iterator() {
            return Arrays.<PT> asList()
                         .iterator();
        }

        @Override
        public <R> R visit(final Function<? super PT, ? extends R> present, final Supplier<? extends R> absent) {
            return absent.get();
        }

        @Override
        public void subscribe(final Subscriber<? super PT> s) {

        }

        @Override
        public boolean test(final PT t) {
            return false;
        }

        @Override
        public <T> Either5<ST, M,M2,M3, T> unit(final T unit) {
            return Either5.right(unit);
        }
        @Override
        public Either5<ST, M,M2,PT,M3> swap4() {
            return new Right<>(
                    value);

        }
        @Override
        public Either5<ST, M, PT,M3, M2> swap3() {
            return (Either5<ST, M, PT,M3, M2>)this;
            
        }
        @Override
        public Either5<ST, PT,M2, M3, M> swap2() {
           return (Either5<ST, PT,M2, M3, M>)this;

        }

        @Override
        public Either5<PT, M, M2, M3, ST> swap1() {
            return (Either5<PT, M,M2, M3, ST>) this;

        }

        @Override
        public boolean isLeft2() {

            return false;
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
            if(obj instanceof Lazy){
                return ((Lazy)obj).equals(this);
            }
            if (getClass() != obj.getClass())
                return false;
            Left4 other = (Left4) obj;
            if (value == null) {
                if (other.value != null)
                    return false;
            } else if (!value.equals(other.value))
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

}
