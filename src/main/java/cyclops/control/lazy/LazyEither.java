package cyclops.control.lazy;

import com.aol.cyclops2.data.collections.extensions.CollectionX;
import com.aol.cyclops2.types.reactive.Completable;
import com.aol.cyclops2.types.MonadicValue;
import cyclops.collectionx.immutable.LinkedListX;
import cyclops.companion.Semigroups;
import cyclops.companion.Streams;
import cyclops.async.Future;
import cyclops.collectionx.mutable.ListX;
import cyclops.control.*;
import cyclops.function.*;
import cyclops.control.anym.AnyM;
import cyclops.control.anym.Witness;
import cyclops.control.anym.Witness.lazyEither;
import cyclops.reactive.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.*;

/**
 * A totally Lazy Either implementation with tail call optimization for transform and flatMap operators. Either can operate reactively (i.e. suppports data arriving asynchronsouly
 the monadic chain of computations will only be executed once data arrives).
 * 
 * 'Right' (or lazyRight type) biased disjunct union.
 
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
 *      Either.lazyRight("hello").transform(v->v+" world")
 *    //Either.lazyRight["hello world"]
 *  }
 *  </pre>
 *
 *  Instantiating an Either - Left
 *  <pre>
 *  {@code
 *      Either.lazyLeft("hello").transform(v->v+" world")
 *    //Either.seconary["hello"]
 *  }
 *  </pre>
 *
 *  Either can operate (via transform/flatMap) as a Functor / Monad and via combine as an ApplicativeFunctor
 *
 *   Values can be accumulated via
 *  <pre>
 *  {@code
 *  Xor.accumulateLeft(ListX.of(Either.lazyLeft("failed1"),
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
 * @param <LT> Left type
 * @param <RT> Right type
 */
public interface LazyEither<LT, RT> extends Either<LT, RT> {

    /**
    default Either<NonEmptyList<LT>, RT> nel() {
        return Either.fromLazy(Eval.always(()->visit(s->Either.lazyLeft(NonEmptyList.of(s)),p->Either.lazyRight(p))));
    }
    **/
    default LazyEither<LT,RT> accumulate(Either<LT,RT> next, Semigroup<RT> sg){
        return flatMap(s1->next.map(s2->sg.apply(s1,s2)));
    }
    default LazyEither<LT,RT> accumulateRight(Semigroup<RT> sg, Either<LT,RT>... values){
        return (LazyEither<LT,RT>)Either.super.accumulateRight(sg,values);
    }
    default LazyEither<LT,RT> accumulate(Semigroup<LT> sg, Either<LT,RT> next){
        return flatMapLeft(s1->next.mapLeft(s2->sg.apply(s1,s2)));
    }
    default LazyEither<LT,RT> accumulate(Semigroup<LT> sg, Either<LT,RT>... values){
        return (LazyEither<LT,RT>)Either.super.accumulate(sg,values);
    }
    public static  <L,T,R> LazyEither<L,R> tailRec(T initial, Function<? super T, ? extends LazyEither<L,? extends Either<T, R>>> fn){
        LazyEither<L,? extends Either<T, R>> next[] = new LazyEither[1];
        next[0] = LazyEither.right(Either.left(initial));
        boolean cont = true;
        do {
            cont = next[0].visit(p -> p.visit(s -> {
                next[0] = fn.apply(s);
                return true;
            }, pr -> false), () -> false);
        } while (cont);
        return next[0].map(x->x.orElse(null));
    }


    static <RT> LazyEither<Throwable,RT> async(final Executor ex, final Supplier<RT> s){
        return fromFuture(Future.of(s,ex));
    }
    /**
     * Create a reactiveBuffer CompletableEither
     *
     * <pre>
     *  {@code
     *      ___Example 1___
     *
     *      CompletableEither<Integer,Integer> completable = Either.lazy();
            Either<Throwable,Integer> mapped = completable.transform(i->i*2)
                                                          .flatMap(i->Eval.later(()->i+1));

            completable.complete(5);

            mapped.printOut();
            //11

            ___Example 2___

            CompletableEither<Integer,Integer> completable = Either.lazy();
            Either<Throwable,Integer> mapped = completable.transform(i->i*2)
                                                          .flatMap(i->Eval.later(()->i+1));


            completable.complete(null);

            //Either:Left[NoSuchElementException]

            ___Example 3___

            CompletableEither<Integer,Integer> completable = Either.lazy();
            Either<Throwable,Integer> mapped = completable.transform(i->i*2)
                                                          .flatMap(i->Eval.later(()->i+1));

            completable.complete(new IllegalStateException());

            //Either:Left[IllegalStateElementException]
     *     }
     * </pre>
     *
     * @param <RT>
     * @return
     */
    static <RT> CompletableEither<RT,RT> either(){
        Completable.CompletablePublisher<RT> c = new Completable.CompletablePublisher<RT>();
        return new LazyEither.CompletableEither<RT, RT>(c,fromFuture(Future.fromPublisher(c)));
    }

    @AllArgsConstructor
    static class CompletableEither<ORG,RT> implements LazyEither<Throwable,RT>, Completable<ORG> {

        public final Completable.CompletablePublisher<ORG> complete;
        public final LazyEither<Throwable,RT> either;

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
        public boolean completeExceptionally(java.lang.Throwable error) {
            return complete.completeExceptionally(error);
        }

        @Override
        public Option<RT> filter(Predicate<? super RT> test) {
            return either.filter(test);
        }



        @Override
        public <R> LazyEither<Throwable, R> map(Function<? super RT, ? extends R> fn) {
            return either.map(fn);
        }

        @Override
        public LazyEither<Throwable, RT> peek(Consumer<? super RT> action) {
            return either.peek(action);
        }

        @Override
        public LazyEither<RT, Throwable> swap() {
            return either.swap();
        }

        @Override
        public Ior<java.lang.Throwable, RT> toIor() {
            return either.toIor();
        }

        @Override
        public Option<RT> get() {
            return either.get();
        }



        @Override
        public Option<Throwable> getLeft() {
            return either.getLeft();
        }

        @Override
        public Throwable leftOrElse(Throwable alt) {
            return either.leftOrElse(alt);
        }

        @Override
        public Optional<java.lang.Throwable> secondaryToOptional() {
            return either.secondaryToOptional();
        }

        @Override
        public ReactiveSeq<java.lang.Throwable> leftToStream() {
            return either.leftToStream();
        }

        @Override
        public <RT1> LazyEither<Throwable, RT1> flatMap(Function<? super RT, ? extends Either<? extends Throwable, ? extends RT1>> mapper) {
            return either.flatMap(mapper);
        }




        @Override
        public boolean isRight() {
            return either.isRight();
        }

        @Override
        public boolean isLeft() {
            return either.isLeft();
        }



        @Override
        public void peek(Consumer<? super Throwable> stAction, Consumer<? super RT> ptAction) {
            either.peek(stAction,ptAction);
        }

        @Override
        public LazyEither<Throwable, RT> flatMapLeftToRight(Function<? super Throwable, ? extends Either<Throwable, RT>> fn) {
            return either.flatMapLeftToRight(fn);
        }

        @Override
        public <LT1> LazyEither<LT1, RT> flatMapLeft(Function<? super Throwable, ? extends Either<LT1, RT>> mapper) {
            return either.flatMapLeft(mapper);
        }

        @Override
        public <R> R visit(Function<? super java.lang.Throwable, ? extends R> secondary, Function<? super RT, ? extends R> primary) {
            return either.visit(secondary,primary);
        }

        @Override
        public LazyEither<Throwable, RT> peekLeft(Consumer<? super Throwable> action) {
            return either.peekLeft(action);
        }

        @Override
        public <R> LazyEither<R, RT> mapLeft(Function<? super Throwable, ? extends R> fn) {
            return either.mapLeft(fn);
        }

        @Override
        public LazyEither<Throwable, RT> mapLeftToRight(Function<? super Throwable, ? extends RT> fn) {
            return either.mapLeftToRight(fn);
        }

        @Override
        public int hashCode() {
            return either.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return either.equals(obj);
        }

        @Override
        public <R> R fold(Function<? super Throwable, ? extends R> fn1, Function<? super RT, ? extends R> fn2) {
            return either.fold(fn1,fn2);
        }

        @Override
        public <R> R visit(Function<? super RT, ? extends R> present, Supplier<? extends R> absent) {
            return either.visit(i->absent.get(),present);
        }
    }

    static <ST,PT> LazyEither<ST,PT> fromXor(Either<ST,PT> xor){
        if(xor instanceof LazyEither)
            return (LazyEither<ST,PT>)xor;
        return xor.visit(LazyEither::left, LazyEither::right);
    }

    static <LT,RT> LazyEither<LT,RT> fromLazy(Eval<LazyEither<LT,RT>> lazy){
        return new LazyEither.Lazy<>(lazy);
    }

    static <T> LazyEither<Throwable,T> fromFuture(Future<T> future){
        return fromLazy(Eval.<LazyEither<Throwable,T>>fromFuture(
                        future.map(e->e!=null? LazyEither.<Throwable,T>right(e) : LazyEither.<Throwable,T>left(new NoSuchElementException()))
                            .recover(t-> LazyEither.<Throwable,T>left(t.getCause()))));
    }

    /**
     *  Turn a toX of Eithers into a singleUnsafe Either with Lists of values.
     *
     * <pre>
     * {@code
     *
     * Either<String,Integer> just  = Either.lazyRight(10);
       Either<String,Integer> none = Either.lazyLeft("none");


     * Either<ListX<String>,ListX<Integer>> xors =Either.sequence(ListX.of(just,none,Either.lazyRight(1)));
       //Eitehr.lazyRight(ListX.of(10,1)));
     *
     * }</pre>
     *
     *
     *
     * @param Either Either to sequence
     * @return Either Sequenced
     */
    public static <LT1, PT> LazyEither<ListX<LT1>,ListX<PT>> sequenceRight(final CollectionX<LazyEither<LT1, PT>> xors) {
        Objects.requireNonNull(xors);
        return AnyM.sequence(xors.stream().filter(LazyEither::isRight).map(AnyM::fromLazyEither).to().listX(), lazyEither.INSTANCE)
                .to(Witness::lazyEither);
    }
    public static <LT1, PT> LazyEither<ListX<LT1>,ListX<PT>> sequenceLeft(final CollectionX<LazyEither<LT1, PT>> xors) {
        Objects.requireNonNull(xors);
        LazyEither<ListX<PT>,ListX<LT1>> res = AnyM.sequence(xors.stream()
                                 .filter(LazyEither::isRight)
                                 .map(i->AnyM.fromLazyEither(i.swap())).to()
                                 .listX(),
                                lazyEither.INSTANCE)
                    .to(Witness::lazyEither);
        return res.swap();
    }
    /**
     * TraverseOps a Collection of Either producting an Either3 with a ListX, applying the transformation function to every
     * element in the list
     *
     * @param xors Eithers to sequence and transform
     * @param fn Transformation function
     * @return An Either with a transformed list
     */
    public static <LT1, PT,R> LazyEither<ListX<LT1>,ListX<R>> traverseRight(final CollectionX<LazyEither<LT1, PT>> xors, Function<? super PT, ? extends R> fn) {
        return  sequenceRight(xors).map(l->l.map(fn));
    }
    public static <LT1, PT,R> LazyEither<ListX<R>,ListX<PT>> traverseLeft(final CollectionX<LazyEither<LT1, PT>> xors, Function<? super LT1, ? extends R> fn) {
        return  sequenceLeft(xors).mapLeft(l->l.map(fn));
    }


    /**
     *  Accumulate the results only from those Either3 which have a Right type present, using the supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see com.aol.cyclops2.Monoids }.
     *
     * <pre>
     * {@code
     * Either3<String,String,Integer> just  = Either3.lazyRight(10);
       Either3<String,String,Integer> none = Either3.lazyLeft("none");
     *
     *  Either3<ListX<String>,ListX<String>,Integer> xors = Either3.accumulateRight(Monoids.intSum,ListX.of(just,none,Either3.lazyRight(1)));
        //Either3.lazyRight(11);
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
    public static <LT1, RT> LazyEither<ListX<LT1>, RT> accumulate(final Monoid<RT> reducer, final CollectionX<LazyEither<LT1, RT>> xors) {
        return sequenceRight(xors).map(s -> s.reduce(reducer));
    }

    public static <LT, B, RT> LazyEither<LT,RT> rightEval(final Eval<RT> right) {
        return new Right(
                           right);
    }

    public static <LT, B, RT> LazyEither<LT, RT> leftEval(final Eval<LT> left) {
        return new Left(
                          left);
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
    static <X, LT extends X, M extends X, RT extends X>  Consumer<Consumer<? super X>> consumeAny(LazyEither<LT, RT> either){
        return in->visitAny(in,either);
    }

    static <X, LT extends X, M extends X, RT extends X,R>  Function<Function<? super X, R>,R> applyAny(LazyEither<LT, RT> either){
        return in->visitAny(either,in);
    }

    static <X, PT extends X, ST extends X,R> R visitAny(LazyEither<ST, PT> either, Function<? super X, ? extends R> fn){
        return either.visit(fn, fn);
    }

    static <X, LT extends X, RT extends X> X visitAny(Consumer<? super X> c, LazyEither<LT, RT> either){
        Function<? super X, X> fn = x ->{
            c.accept(x);
            return x;
        };
        return visitAny(either,fn);
    }
    default Trampoline<RT> toTrampoline(Supplier<RT> defaultValue) {
        return Trampoline.more(()->Trampoline.done(orElse(null)));
    }
    default Trampoline<RT> toTrampoline(RT defaultValue) {
        return Trampoline.more(()->Trampoline.done(orElse(null)));
    }

    @Override
    default <T2, R1, R2, R3, R> LazyEither<LT, R> forEach4(Function<? super RT, ? extends Either<LT, R1>> value1, BiFunction<? super RT, ? super R1, ? extends Either<LT, R2>> value2,
                                                           Function3<? super RT, ? super R1, ? super R2, ? extends Either<LT, R3>> value3, Function4<? super RT, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return (LazyEither<LT,R>)Either.super.forEach4(value1,value2,value3,yieldingFunction);
    }

    @Override
    default <T2, R1, R2, R> Either<LT, R> forEach3(Function<? super RT, ? extends Either<LT, R1>> value1, BiFunction<? super RT, ? super R1, ? extends Either<LT, R2>> value2, Function3<? super RT, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        return (LazyEither<LT, R>)Either.super.forEach3(value1, value2, yieldingFunction);
    }

    @Override
    default <R1, R> Either<LT, R> forEach2(Function<? super RT, ? extends Either<LT, R1>> value1, BiFunction<? super RT, ? super R1, ? extends R> yieldingFunction) {
        return (LazyEither<LT, R>)Either.super.forEach2(value1, yieldingFunction);
    }


    /* (non-Javadoc)
     * @see com.aol.cyclops2.control.Xor#combineToList(com.aol.cyclops2.control.Xor, java.util.function.BiFunction)
     */
    @Override
    default <T2, R> LazyEither<LinkedListX<LT>, R> combineToList(Either<LT, ? extends T2> app,
                                                                 BiFunction<? super RT, ? super T2, ? extends R> fn) {

        return (LazyEither<LinkedListX<LT>, R>)Either.super.combineToList(app, fn);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops2.control.Xor#combine(com.aol.cyclops2.control.Xor, java.util.function.BinaryOperator, java.util.function.BiFunction)
     */
    @Override
    default <T2, R> LazyEither<LT, R> combine(Either<? extends LT, ? extends T2> app, BinaryOperator<LT> semigroup,
                                              BiFunction<? super RT, ? super T2, ? extends R> fn) {

        return (LazyEither<LT, R>)Either.super.combine(app, semigroup, fn);
    }
    /**
     * Lazily construct a Right Either from the supplied publisher
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
    public static <T> LazyEither<Throwable, T> fromPublisher(final Publisher<T> pub) {
        return fromFuture(Future.fromPublisher(pub));
    }

    /**
     * Construct a Right Either from the supplied Iterable
     * <pre>
     * {@code
     *   List<Integer> list =  Arrays.asList(1,2,3);

         Either<Throwable,Integer> future = Either.fromIterable(list);

         //Either[1]
     *
     * }
     * </pre>
     * @param iterable Iterable to construct an Either from
     * @return Either constructed from the supplied Iterable
     */
    public static <ST, T> LazyEither<ST, T> fromIterable(final Iterable<T> iterable, T alt) {

        final Iterator<T> it = iterable.iterator();
        return it.hasNext() ? LazyEither.right( it.next()) : LazyEither.right(alt);
    }


    /**
     * Create an instance of the lazyLeft type. Most methods are biased to the lazyRight type,
     * so you will need to use swap() or secondaryXXXX to manipulate the wrapped value
     *
     * <pre>
     * {@code
     *   Either.<Integer,Integer>lazyLeft(10).transform(i->i+1);
     *   //Either.lazyLeft[10]
     *
     *    Either.<Integer,Integer>lazyLeft(10).swap().transform(i->i+1);
     *    //Either.lazyRight[11]
     * }
     * </pre>
     *
     *
     * @param value to wrap
     * @return Left instance of Either
     */
    public static <ST, PT> LazyEither<ST, PT> left(final ST value) {
        return new Left(Eval.later(()-> value));
    }

    /**
     * Create an instance of the lazyRight type. Most methods are biased to the lazyRight type,
     * which means, for example, that the transform method operates on the lazyRight type but does nothing on lazyLeft Eithers
     *
     * <pre>
     * {@code
     *   Either.<Integer,Integer>lazyRight(10).transform(i->i+1);
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
    public static <ST, PT> LazyEither<ST, PT> right(final PT value) {

        return new Right(Eval.later(()->
                           value));
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.MonadicValue#fromEither5()
     */

    default AnyM<lazyEither, RT> anyMEither() {
        return AnyM.fromLazyEither(this);
    }







    default Trampoline<Either<LT,RT>> toTrampoline() {
        return (Trampoline)toEitherTrampoline();
    }
    default Trampoline<LazyEither<LT,RT>> toEitherTrampoline() {
        return Trampoline.more(()->Trampoline.done(this));
    }
    // cojoin
    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.MonadicValue#nest()
     */

    default LazyEither<LT, Either<LT,RT>> nest() {
        return this.map(t -> unit(t));
    }



    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.MonadicValue#unit(java.lang.Object)
     */
    @Override
    default <T> LazyEither<LT, T> unit(final T unit) {
        return LazyEither.right(unit);
    }


    /*
     * (non-Javadoc)
     *
     * @see
     * com.aol.cyclops2.types.Filters#filter(java.util.function.Predicate)
     */
    @Override
    Option<RT> filter(Predicate<? super RT> test);

    /**
     * If this Either contains the Left type, transform it's value so that it contains the Right type
     *
     *
     * @param fn Function to transform lazyLeft type to lazyRight
     * @return Either with lazyLeft type mapped to lazyRight
     */
    LazyEither<LT, RT> mapLeftToRight(Function<? super LT, ? extends RT> fn);

    /**
     * Always transform the Left type of this Either if it is present using the provided transformation function
     *
     * @param fn Transformation function for Left types
     * @return Either with Left type transformed
     */
    <R> LazyEither<R, RT> mapLeft(Function<? super LT, ? extends R> fn);

    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.MonadicValue#transform(java.util.function.Function)
     */
    @Override
    <R> LazyEither<LT, R> map(Function<? super RT, ? extends R> fn);

    /**
     * Peek at the Left type value if present
     *
     * @param action Consumer to peek at the Left type value
     * @return Either with the same values as before
     */
    LazyEither<LT, RT> peekLeft(Consumer<? super LT> action);

    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.Functor#peek(java.util.function.Consumer)
     */
    @Override
    LazyEither<LT, RT> peek(Consumer<? super RT> action);



    @Override
    default <R> LazyEither<LT,R> retry(final Function<? super RT, ? extends R> fn) {
        return (LazyEither<LT,R>)Either.super.retry(fn);
    }


    @Override
    default <R> LazyEither<LT,R> retry(final Function<? super RT, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (LazyEither<LT,R>)Either.super.retry(fn,retries,delay,timeUnit);
    }


    /**
     * Swap types so operations directly affect the current (pre-swap) Left type
     *<pre>
     *  {@code
     *
     *    Either.lazyLeft("hello")
     *       .transform(v->v+" world")
     *    //Either.seconary["hello"]
     *
     *    Either.lazyLeft("hello")
     *       .swap()
     *       .transform(v->v+" world")
     *       .swap()
     *    //Either.seconary["hello world"]
     *  }
     *  </pre>
     *
     *
     * @return Swap the lazyRight and lazyLeft types, allowing operations directly on what was the Left type
     */
    LazyEither<RT, LT> swap();

    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.Value#toIor()
     */
    @Override
    Ior<LT, RT> toIor();

    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.foldable.Convertable#isPresent()
     */
    @Override
    default boolean isPresent() {
        return isRight();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.Value#toLazyEither()
     */
    default Either<LT, RT> toXor() {
        return visit(Either::left, Either::right);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.Value#toLazyEither(java.lang.Object)
     */
    @Override
    default <ST2> Either<ST2, RT> toEither(final ST2 secondary) {
        return visit(s -> Either.left(secondary), p -> Either.right(p));
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
    <R> R visit(Function<? super LT, ? extends R> secondary, Function<? super RT, ? extends R> primary);

    @Deprecated // use bimap instead
    default <R1, R2> LazyEither<R1, R2> mapBoth(final Function<? super LT, ? extends R1> secondary,
                                                final Function<? super RT, ? extends R2> primary) {
        if (isLeft())
            return (LazyEither<R1, R2>) swap().map(secondary)
                                          .swap();
        return (LazyEither<R1, R2>) map(primary);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.functor.BiTransformable#bimap(java.util.function.Function,
     * java.util.function.Function)
     */
    @Override
    default <R1, R2> LazyEither<R1, R2> bimap(Function<? super LT, ? extends R1> secondary,
                                              Function<? super RT, ? extends R2> primary) {
        if (isLeft())
            return (LazyEither<R1, R2>) swap().map(secondary)
                                          .swap();
        return (LazyEither<R1, R2>) map(primary);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.functor.BiTransformable#bipeek(java.util.function.Consumer,
     * java.util.function.Consumer)
     */
    @Override
    default LazyEither<LT, RT> bipeek(Consumer<? super LT> c1, Consumer<? super RT> c2) {

        return (LazyEither<LT, RT>) Either.super.bipeek(c1, c2);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.functor.BiTransformable#bicast(java.lang.Class,
     * java.lang.Class)
     */
    @Override
    default <U1, U2> LazyEither<U1, U2> bicast(Class<U1> type1, Class<U2> type2) {

        return (LazyEither<U1, U2>) Either.super.bicast(type1, type2);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.aol.cyclops2.types.functor.BiTransformable#bitrampoline(java.util.function.Function,
     * java.util.function.Function)
     */
    @Override
    default <R1, R2> LazyEither<R1, R2> bitrampoline(Function<? super LT, ? extends Trampoline<? extends R1>> mapper1,
                                                     Function<? super RT, ? extends Trampoline<? extends R2>> mapper2) {

        return (LazyEither<R1, R2>) Either.super.bitrampoline(mapper1, mapper2);
    }




    /*
     * (non-Javadoc)
     *
     * @see java.util.function.Supplier#getValue()
     */
    @Override
    Option<RT> get();



    /**
     * @return The Left Value if present, otherwise null
     */
    Option<LT> getLeft();

    /**
     * @return The Left value wrapped in an Optional if present, otherwise an zero Optional
     */
    Optional<LT> secondaryToOptional();

    /**
     * @return A Stream containing the lazyLeft value if present, otherwise an zero Stream
     */
    ReactiveSeq<LT> leftToStream();


    <RT1> LazyEither<LT, RT1> flatMap(
            Function<? super RT, ? extends Either<? extends LT,? extends RT1>> mapper);



    /**
     * Perform a flatMap operation on the Left type
     *
     * @param mapper Flattening transformation function
     * @return Either containing the value inside the result of the transformation function as the Left value, if the Left type was present
     */
    <LT1> LazyEither<LT1, RT> flatMapLeft(Function<? super LT, ? extends Either<LT1, RT>> mapper);


    /**
     * A flatMap operation that keeps the Left and Right types the same
     *
     * @param fn Transformation function
     * @return Either
     */
    LazyEither<LT, RT> flatMapLeftToRight(Function<? super LT, ? extends Either<LT, RT>> fn);

    @Deprecated // use bipeek
    void peek(Consumer<? super LT> stAction, Consumer<? super RT> ptAction);

    /**
     * @return True if this is a lazyRight Either
     */
    public boolean isRight();

    /**
     * @return True if this is a lazyLeft Either
     */
    public boolean isLeft();



    /**
     * @return An Either with the lazyLeft type converted to a persistent list, for use with accumulating app function  {@link LazyEither#combine(LazyEither,BiFunction)}
     */
    default LazyEither<LinkedListX<LT>, RT> list() {
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
    default <T2, R> LazyEither<LinkedListX<LT>, R> combineToList(final LazyEither<LT, ? extends T2> app,
                                                                 final BiFunction<? super RT, ? super T2, ? extends R> fn) {
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

    default <T2, R> LazyEither<LT, R> combine(final LazyEither<? extends LT, ? extends T2> app,
                                              final BinaryOperator<LT> semigroup, final BiFunction<? super RT, ? super T2, ? extends R> fn) {
        return this.visit(secondary -> app.visit(s2 -> LazyEither.left(semigroup.apply(s2, secondary)),
                                                 p2 -> LazyEither.left(secondary)),
                          primary -> app.visit(s2 -> LazyEither.left(s2),
                                               p2 -> LazyEither.right(fn.apply(primary, p2))));
    }


    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.lambda.monads.Functor#trampoline(java.util.function.
     * Function)
     */
    @Override
    default <R> LazyEither<LT, R> trampoline(final Function<? super RT, ? extends Trampoline<? extends R>> mapper) {

        return (LazyEither<LT, R>) Either.super.trampoline(mapper);
    }

    static <ST, PT> LazyEither<ST, PT> narrow(final LazyEither<? extends ST, ? extends PT> broad) {
        return (LazyEither<ST, PT>) broad;
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static final class Lazy<ST, PT> implements LazyEither<ST, PT> {

        private final Eval<LazyEither<ST, PT>> lazy;

        public Eval<Either<ST, PT>> nestedEval(){
            return (Eval)lazy;
        }
        private static <ST, PT> Lazy<ST, PT> lazy(Eval<LazyEither<ST, PT>> lazy) {
            return new Lazy<>(
                              lazy);
        }
        @Override
        public Trampoline<LazyEither<ST,PT>> toEitherTrampoline() {
            Trampoline<LazyEither<ST,PT>> trampoline = lazy.toTrampoline();
            return new Trampoline<LazyEither<ST,PT>>() {
                @Override
                public LazyEither<ST,PT> get() {
                    LazyEither<ST,PT> either = lazy.get();
                    while (either instanceof LazyEither.Lazy) {
                        either = ((LazyEither.Lazy<ST,PT>) either).lazy.get();
                    }
                    return either;
                }
                @Override
                public boolean complete(){
                    return false;
                }
                @Override
                public Trampoline<LazyEither<ST,PT>> bounce() {
                    LazyEither<ST,PT> either = lazy.get();
                    if(either instanceof LazyEither.Lazy){
                        return either.toEitherTrampoline();
                    }
                    return Trampoline.done(either);

                }
            };
        }

        @Override
        public Iterator<PT> iterator() {
            return new Iterator<PT>() {

                Iterator<PT> it;

                @Override
                public boolean hasNext() {
                    if(it==null){
                        it=trampoline().iterator();
                    }
                    return it.hasNext();
                }

                @Override
                public PT next() {
                    if(it==null){
                        it=trampoline().iterator();
                    }
                    return it.next();
                }
            };
        }

        public LazyEither<ST, PT> resolve() {
          return lazy.get()
                       .visit(LazyEither::left, LazyEither::right);
        }
        @Override
        public <R> LazyEither<ST, R> map(final Function<? super PT, ? extends R> mapper) {
            return flatMap(t -> LazyEither.right(mapper.apply(t)));


        }

        private <PT> LazyEither<ST, PT> toEither(MonadicValue<? extends PT> value) {
            return value.visit(p -> LazyEither.right(p), () -> LazyEither.left(null));
        }

        @Override
        public <RT1> LazyEither<ST, RT1> flatMap(Function<? super PT, ? extends Either<? extends ST, ? extends RT1>> mapper) {
            return LazyEither.fromLazy(lazy.map(m->m.flatMap(mapper)));
        }


        @Override
        public final void subscribe(final Subscriber<? super PT> sub) {
            lazy.subscribe(new Subscriber<LazyEither<ST, PT>>() {
                boolean onCompleteSent = false;
                @Override
                public void onSubscribe(Subscription s) {
                    sub.onSubscribe(s);
                }

                @Override
                public void onNext(LazyEither<ST, PT> pts) {
                    if(pts.isRight()){ //if we create a LazyThrowable type
                                        // we could safely propagate an error if pts was a lazyLeft
                        PT v = pts.orElse(null);
                        if(v!=null)
                            sub.onNext(v);
                    }
                    if(!onCompleteSent){
                        sub.onComplete();
                        onCompleteSent =true;
                    }
                }

                @Override
                public void onError(Throwable t) {
                    sub.onError(t);
                }

                @Override
                public void onComplete() {
                    if(!onCompleteSent){
                        sub.onComplete();
                        onCompleteSent =true;
                    }
                }
            });
        }

        @Override
        public Maybe<PT> filter(final Predicate<? super PT> test) {
            return Maybe.fromIterable(this).filter(test);

        }


        /*
         * (non-Javadoc)
         *
         * @see com.aol.cyclops2.sum.types.Either#mapLeftToRight(java.util.
         * function.Function)
         */
        @Override
        public LazyEither<ST, PT> mapLeftToRight(Function<? super ST, ? extends PT> fn) {
            return lazy(Eval.later(() ->  resolve()
                                             .mapLeftToRight(fn)));

        }

        /*
         * (non-Javadoc)
         *
         * @see
         * com.aol.cyclops2.sum.types.Either#mapLeft(java.util.function.
         * Function)
         */
        @Override
        public <R> LazyEither<R, PT> mapLeft(Function<? super ST, ? extends R> fn) {
            return lazy(Eval.later(() -> resolve().mapLeft(fn)));
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * com.aol.cyclops2.sum.types.Either#peekLeft(java.util.function.
         * Consumer)
         */
        @Override
        public LazyEither<ST, PT> peekLeft(Consumer<? super ST> action) {
            return lazy(Eval.later(() -> resolve().peekLeft(action)));
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * com.aol.cyclops2.sum.types.Either#peek(java.util.function.Consumer)
         */
        @Override
        public LazyEither<ST, PT> peek(Consumer<? super PT> action) {
            return lazy(Eval.later(() -> resolve().peek(action)));
        }

        /*
         * (non-Javadoc)
         *
         * @see com.aol.cyclops2.sum.types.Either#swap()
         */
        @Override
        public LazyEither<PT, ST> swap() {
            return lazy(Eval.later(() ->  resolve()
                                             .swap()));
        }

        /*
         * (non-Javadoc)
         *
         * @see com.aol.cyclops2.sum.types.Either#toIor()
         */
        @Override
        public Ior<ST, PT> toIor() {
            return trampoline()
                       .toIor();
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * com.aol.cyclops2.sum.types.Either#visit(java.util.function.Function,
         * java.util.function.Function)
         */
        @Override
        public <R> R visit(Function<? super ST, ? extends R> secondary, Function<? super PT, ? extends R> primary) {
            return trampoline()
                       .visit(secondary, primary);
        }
        private LazyEither<ST,PT> trampoline(){
            LazyEither<ST,PT> maybe = lazy.get();
            while (maybe instanceof Lazy) {
                maybe = ((Lazy<ST,PT>) maybe).lazy.get();
            }
            return maybe;
        }

        /*
         * (non-Javadoc)
         *
         * @see com.aol.cyclops2.sum.types.Either#getValue()
         */
        @Override
        public Option<PT> get() {

            return Maybe.fromIterable((this));
        }

        /*
         * (non-Javadoc)
         *
         * @see com.aol.cyclops2.sum.types.Either#getLeft()
         */
        @Override
        public Option<ST> getLeft() {
            return Maybe.fromIterable(swap());
        }

        @Override
        public ST leftOrElse(ST alt) {
            return trampoline().leftOrElse(alt);
        }

        /*
         * (non-Javadoc)
         *
         * @see com.aol.cyclops2.sum.types.Either#secondaryToOptional()
         */
        @Override
        public Optional<ST> secondaryToOptional() {
            return trampoline()
                       .secondaryToOptional();
        }

        /*
         * (non-Javadoc)
         *
         * @see com.aol.cyclops2.sum.types.Either#leftToStream()
         */
        @Override
        public ReactiveSeq<ST> leftToStream() {
            return ReactiveSeq.generate(() -> trampoline()
                                                  .leftToStream())
                              .flatMap(Function.identity());
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * com.aol.cyclops2.sum.types.Either#flatMapLeft(java.util.function.
         * Function)
         */
        @Override
        public <LT1> LazyEither<LT1, PT> flatMapLeft(Function<? super ST, ? extends Either<LT1, PT>> mapper) {
            return lazy(Eval.later(() -> resolve()
                                             .flatMapLeft(mapper)));
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * com.aol.cyclops2.sum.types.Either#flatMapLeftToRight(java.util.
         * function.Function)
         */
        @Override
        public LazyEither<ST, PT> flatMapLeftToRight(Function<? super ST, ? extends Either<ST, PT>> fn) {
            return lazy(Eval.later(() -> resolve().flatMapLeftToRight(fn)));
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * com.aol.cyclops2.sum.types.Either#peek(java.util.function.Consumer,
         * java.util.function.Consumer)
         */
        @Override
        public void peek(Consumer<? super ST> stAction, Consumer<? super PT> ptAction) {
            trampoline()
                .peek(stAction, ptAction);

        }

        /*
         * (non-Javadoc)
         *
         * @see com.aol.cyclops2.sum.types.Either#isRight()
         */
        @Override
        public boolean isRight() {
            return trampoline()
                       .isRight();
        }

        /*
         * (non-Javadoc)
         *
         * @see com.aol.cyclops2.sum.types.Either#isLeft()
         */
        @Override
        public boolean isLeft() {
            return trampoline()
                       .isLeft();
        }




        /*
         * (non-Javadoc)
         *
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return trampoline().hashCode();

        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(final Object obj) {

            return trampoline().equals(obj);
        }
        @Override
        public String toString(){
            return trampoline().toString();
        }

        @Override
        public <R> R fold(Function<? super ST, ? extends R> fn1, Function<? super PT, ? extends R> fn2) {
            return lazy.get().fold(fn1,fn2);
        }

        @Override
        public <R> R visit(Function<? super PT, ? extends R> present, Supplier<? extends R> absent) {
            return trampoline().visit(present,absent);
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class Right<ST, PT> implements LazyEither<ST, PT> {
        private final Eval<PT> value;

        @Override
        public LazyEither<ST, PT> mapLeftToRight(final Function<? super ST, ? extends PT> fn) {
            return this;
        }

        @Override
        public <R> LazyEither<R, PT> mapLeft(final Function<? super ST, ? extends R> fn) {
            return (LazyEither<R, PT>) this;
        }

        @Override
        public <R> LazyEither<ST, R> map(final Function<? super PT, ? extends R> fn) {
            return new LazyEither.Right(
                                    value.map(fn));
        }

        @Override
        public LazyEither<ST, PT> peekLeft(final Consumer<? super ST> action) {
            return this;
        }

        @Override
        public LazyEither<ST, PT> peek(final Consumer<? super PT> action) {
            return map(i -> {
                action.accept(i);
                return i;
            });

        }

        @Override
        public Option<PT> filter(final Predicate<? super PT> test) {
            return value.filter(test);
        }

        @Override
        public LazyEither<PT, ST> swap() {
            return new LazyEither.Left(
                                    value);
        }

        @Override
        public Option<PT> get() {
            return value.toMaybe();
        }

        @Override
        public Option<ST> getLeft() {
            return Option.none();
        }

        @Override
        public ST leftOrElse(ST alt) {
            return alt;
        }

        @Override
        public Optional<ST> secondaryToOptional() {
            return Optional.empty();
        }

        @Override
        public ReactiveSeq<ST> leftToStream() {
            return ReactiveSeq.empty();
        }

        @Override
        public <RT1> LazyEither<ST, RT1> flatMap(Function<? super PT, ? extends Either<? extends ST, ? extends RT1>> mapper) {
            Eval<? extends LazyEither<? extends ST,  ? extends RT1>> ret = value.map(mapper.andThen(LazyEither::fromXor));


            final Eval<LazyEither<ST, RT1>> e3 =  (Eval<LazyEither<ST,  RT1>>)ret;
            return new Lazy<>(
                    e3);
        }



        @Override
        public <LT1> LazyEither<LT1, PT> flatMapLeft(
                final Function<? super ST, ? extends Either<LT1, PT>> mapper) {
            return (LazyEither<LT1, PT>) this;
        }

        @Override
        public LazyEither<ST, PT> flatMapLeftToRight(final Function<? super ST, ? extends Either<ST, PT>> fn) {
            return this;
        }

        @Override
        public void peek(final Consumer<? super ST> stAction, final Consumer<? super PT> ptAction) {
            ptAction.accept(value.get());
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
            return "Either.lazyRight[" + value.get() + "]";
        }

        @Override
        public Ior<ST, PT> toIor() {
            return Ior.primary(value.get());
        }

        @Override
        public <R> R visit(final Function<? super ST, ? extends R> secondary,
                final Function<? super PT, ? extends R> primary) {
            return primary.apply(value.get());
        }





        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return value.get().hashCode();
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
            if(obj instanceof Either.Right){
                return Objects.equals(value.get(),((Either.Right)obj).orElse(null));

            }
            if (getClass() != obj.getClass())
                return false;
            LazyEither.Right other = (LazyEither.Right) obj;
            return Objects.equals(value.get(),other.orElse(null));

        }

        @Override
        public <R> R fold(Function<? super ST, ? extends R> fn1, Function<? super PT, ? extends R> fn2) {
            return fn2.apply(value.get());
        }

        @Override
        public <R> R visit(Function<? super PT, ? extends R> present, Supplier<? extends R> absent) {
            return present.apply(value.get());
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class Left<ST, PT> implements LazyEither<ST, PT> {
        private final Eval<ST> value;

        @Override
        public boolean isLeft() {
            return true;
        }

        @Override
        public boolean isRight() {
            return false;
        }



        @Override
        public LazyEither<ST, PT> mapLeftToRight(final Function<? super ST, ? extends PT> fn) {
            return new LazyEither.Right(
                                     value.map(fn));
        }

        @Override
        public <R> LazyEither<R, PT> mapLeft(final Function<? super ST, ? extends R> fn) {
            return new LazyEither.Left(
                                   value.map(fn));
        }

        @Override
        public <R> LazyEither<ST, R> map(final Function<? super PT, ? extends R> fn) {
            return (LazyEither<ST, R>) this;
        }

        @Override
        public LazyEither<ST, PT> peekLeft(final Consumer<? super ST> action) {
            return mapLeft((Function) FluentFunctions.expression(action));
        }

        @Override
        public LazyEither<ST, PT> peek(final Consumer<? super PT> action) {
            return this;
        }

        @Override
        public Option<PT> filter(final Predicate<? super PT> test) {
            return Option.none();
        }

        @Override
        public LazyEither<PT, ST> swap() {
            return new LazyEither.Right(
                                     value);
        }

        @Override
        public Option<PT> get() {
            return Option.none();
        }

        @Override
        public Option<ST> getLeft() {
            return value.toMaybe();
        }

        @Override
        public ST leftOrElse(ST alt) {
            return value.get();
        }

        @Override
        public Optional<ST> secondaryToOptional() {
            return Optional.ofNullable(value.get());
        }

        @Override
        public ReactiveSeq<ST> leftToStream() {
            return ReactiveSeq.fromStream(Streams.optionalToStream(secondaryToOptional()));
        }

        @Override
        public <RT1> LazyEither<ST, RT1> flatMap(Function<? super PT, ? extends Either<? extends ST, ? extends RT1>> mapper) {
            return (LazyEither<ST, RT1>) this;
        }


        @Override
        public <LT1> LazyEither<LT1, PT> flatMapLeft(
                final Function<? super ST, ? extends Either<LT1, PT>> mapper) {
            Eval<? extends Either<LT1,? extends PT>> ret = value.map(mapper);
            Eval<? extends LazyEither<? extends LT1,  ? extends PT>> et = ret.map(LazyEither::fromXor);
            
           final Eval<LazyEither<LT1, PT>> e3 =  (Eval<LazyEither<LT1,  PT>>)et;
           return new Lazy<>(
                             e3);

        }

        @Override
        public LazyEither<ST, PT> flatMapLeftToRight(final Function<? super ST, ? extends Either<ST, PT>> fn) {
            return new Lazy<ST, PT>(
                    Eval.now(this)).flatMapLeftToRight(fn);
        }

        @Override
        public void peek(final Consumer<? super ST> stAction, final Consumer<? super PT> ptAction) {
            stAction.accept(value.get());

        }

        @Override
        public <R> R visit(final Function<? super ST, ? extends R> secondary,
                final Function<? super PT, ? extends R> primary) {
            return secondary.apply(value.get());
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
            return "Either.lazyLeft[" + value.get() + "]";
        }



        @Override
        public Ior<ST, PT> toIor() {
            return Ior.secondary(value.get());
        }



        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return value.hashCode();
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
            if(obj instanceof Either.Left){
                return Objects.equals(value.get(),((Either.Left)obj).leftOrElse(null));
            }
            if (getClass() != obj.getClass())
                return false;
            LazyEither.Left other = (LazyEither.Left) obj;
            return Objects.equals(value.get(),other.leftOrElse(null));
        }


        @Override
        public <R> R fold(Function<? super ST, ? extends R> fn1, Function<? super PT, ? extends R> fn2) {
            return fn1.apply(value.get());
        }

        @Override
        public <R> R visit(Function<? super PT, ? extends R> present, Supplier<? extends R> absent) {
            return absent.get();
        }
    }

}
