package cyclops.control;

import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.hkt.Higher3;
import com.oath.cyclops.matching.Sealed3;
import com.oath.cyclops.types.Filters;
import com.oath.cyclops.types.OrElseValue;
import com.oath.cyclops.types.Value;
import com.oath.cyclops.types.factory.Unit;
import com.oath.cyclops.types.foldable.To;
import com.oath.cyclops.types.functor.BiTransformable;
import com.oath.cyclops.types.functor.Transformable;
import com.oath.cyclops.types.reactive.Completable;
import cyclops.function.*;

import com.oath.cyclops.hkt.DataWitness.lazyEither3;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.*;

/**
 * A right biased Lazy Either3 type. transform / flatMap operators are tail-call optimized
 *
 * Can be one of 3 types
 *
 *
 *
 * @author johnmcclean
 *
 * @param <LT1> Left1 type
 * @param <LT2> Left2 type
 * @param <RT> Right type (operations are performed on this type if present)
 */
public interface LazyEither3<LT1, LT2, RT> extends Value<RT>,
                                                OrElseValue<RT,LazyEither3<LT1,LT2,RT>>,
                                                Unit<RT>,
                                                Transformable<RT>,
                                                 Filters<RT>,
                                                BiTransformable<LT2, RT>,
                                                To<LazyEither3<LT1, LT2, RT>>,
                                                Sealed3<LT1,LT2,RT>,
                                                Higher3<lazyEither3,LT1,LT2,RT> {


    public static <LT1,LT2,T> Higher<Higher<Higher<lazyEither3, LT1>, LT2>,T> widen(LazyEither3<LT1,LT2,T> narrow) {
      return narrow;
    }
    default LazyEither3<LT1,LT2, RT> filter(Predicate<? super RT> test, Function<? super RT, ? extends LT1> rightToLeft){
      return flatMap(e->test.test(e) ? LazyEither3.right(e) : LazyEither3.left1(rightToLeft.apply(e)));
    }

    default <T2, R> LazyEither3<LT1, LT2,R> zip(final LazyEither3<LT1, LT2,? extends T2> app, final BiFunction<? super RT, ? super T2, ? extends R> fn){
        return flatMap(t->app.map(t2->fn.apply(t,t2)));
    }
    default <T2, R>  LazyEither3<LT1, LT2,R> zip(final Publisher<? extends T2> app, final BiFunction<? super RT, ? super T2, ? extends R> fn){
    return zip(LazyEither.fromPublisher(app),fn);
  }

    /**
     * Create a reactive CompletableEither
     *
     * <pre>
     *  {@code
     *      ___Example 1___
     *
     *      CompletableEither<Integer,Integer> completable = Either3.lazyEither3();
            Either3<Throwable,String,Integer> mapped = completable.map(i->i*2)
                                                                  .flatMap(i->Eval.later(()->i+1));

            completable.complete(5);

            mapped.printOut();
            //11

            ___Example 2___

            CompletableEither<Integer,Integer> completable = Either3.lazyEither3();
            Either3<Throwable,String,Integer> mapped = completable.map(i->i*2)
                                                                  .flatMap(i->Eval.later(()->i+1));


            completable.complete(null);

            //Either3:Left3[NoSuchElementException]

            ___Example 3___

            CompletableEither<Integer,Integer> completable = Either3.lazyEither3();
            Either3<Throwable,String,Integer> mapped = completable.map(i->i*2)
                                                                 .flatMap(i->Eval.later(()->i+1));

            completable.complete(new IllegalStateException());

    //Either:Left[IllegalStateElementException]
     *     }
     * </pre>
     *
     * @param <RT>
     * @return
     */
    static <LT2,RT> LazyEither3.CompletableEither3<RT,LT2,RT> either3(){
        CompletableFuture<RT> c = new CompletableFuture<RT>();
        return new LazyEither3.CompletableEither3<RT,LT2, RT>(c,fromFuture(Future.of(c)));
    }



    @AllArgsConstructor
    static class CompletableEither3<ORG,LT1,RT> implements LazyEither3<Throwable,LT1,RT>, Completable<ORG> {

        public final CompletableFuture<ORG> complete;
        public final LazyEither3<Throwable, LT1,RT> either;

        @Override
        public boolean isFailed() {
            return complete.isCompletedExceptionally();
        }

        @Override
        public boolean isDone() {
            return complete.isDone();
        }

        @Override
        public boolean complete(ORG v) {
            return complete.complete(v);
        }

        @Override
        public boolean completeExceptionally(Throwable error) {
            return complete.completeExceptionally(error);
        }

        @Override
        public Option<RT> get() {
            return either.get();
        }

        @Override
        public <R> R fold(Function<? super Throwable, ? extends R> left1, Function<? super LT1, ? extends R> mid, Function<? super RT, ? extends R> right) {
            return either.fold(left1,mid,right);
        }

        @Override
        public Maybe<RT> filter(Predicate<? super RT> test) {
            return either.filter(test);
        }


        @Override
        public <R2> LazyEither3<Throwable, LT1, R2> flatMap(Function<? super RT, ? extends LazyEither3<Throwable,LT1,? extends R2>> mapper) {
            return either.flatMap(mapper);
        }

        @Override
        public LazyEither3<Throwable, LT1, RT> recoverWith(Supplier<? extends LazyEither3<Throwable, LT1, RT>> fn) {
            return either.recoverWith(fn);
        }

        @Override
        public LazyEither3<Throwable, RT, LT1> swap2() {
            return either.swap2();
        }

        @Override
        public LazyEither3<RT, LT1, Throwable> swap1() {
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
        public <R1, R2> LazyEither3<Throwable, R1, R2> bimap(Function<? super LT1, ? extends R1> fn1, Function<? super RT, ? extends R2> fn2) {
            return either.bimap(fn1,fn2);
        }

        @Override
        public <R> LazyEither3<Throwable, LT1, R> map(Function<? super RT, ? extends R> fn) {
            return either.map(fn);
        }

        @Override
        public <T> LazyEither3<Throwable, LT1, T> unit(T unit) {
            return either.unit(unit);
        }


        @Override
        public <R> R fold(Function<? super RT, ? extends R> present, Supplier<? extends R> absent) {
            return either.fold(present,absent);
        }
        @Override
        public void subscribe(Subscriber<? super RT> sub) {
            either.subscribe(sub);
        }
    }

    static <LT1,LT2,RT> LazyEither3<LT1,LT2,RT> fromLazy(Eval<LazyEither3<LT1,LT2,RT>> lazy){
        return new LazyEither3.Lazy<>(lazy);
    }

    static <LT2,RT> LazyEither3<Throwable,LT2,RT> fromFuture(Future<RT> future){
        return fromLazy(Eval.fromFuture(future.<LazyEither3<Throwable,LT2,RT>>map(LazyEither3::right)
            .recover(e->LazyEither3.left1(e.getCause()))));

    }
    /**
     *  Turn an IterableX of Either3 into a single Either with Lists of values.
     *
     * <pre>
     * {@code
     *
     * Either3<String,String,Integer> just  = Either3.right(10);
       Either3<String,String,Integer> none = Either3.left("none");


     * Either3<String,String,ReactiveSeq<Integer>> xors =Either3.sequence(Seq.of(just,none,Either3.right(1)));
       //Eitehr.right(ReactiveSeq.of(10,1)));
     *
     * }</pre>
     *
     *
     *
     * @param xors Either3 to sequence
     * @return Either3 Sequenced
     */
    public static <LT1,LT2, PT> LazyEither3<LT1,LT2,ReactiveSeq<PT>> sequence(final Iterable<? extends LazyEither3<LT1, LT2, PT>> xors) {
        Objects.requireNonNull(xors);
        return sequence(ReactiveSeq.fromIterable(xors).filter(LazyEither3::isRight));

    }
  public static  <L1,L2,T> LazyEither3<L1,L2,ReactiveSeq<T>> sequence(ReactiveSeq<? extends LazyEither3<L1,L2,T>> stream) {

    LazyEither3<L1,L2, ReactiveSeq<T>> identity = right(ReactiveSeq.empty());

    BiFunction<LazyEither3<L1,L2,ReactiveSeq<T>>,LazyEither3<L1,L2,T>,LazyEither3<L1,L2,ReactiveSeq<T>>> combineToStream = (acc,next) ->acc.zip(next,(a,b)->a.append(b));

    BinaryOperator<LazyEither3<L1,L2,ReactiveSeq<T>>> combineStreams = (a,b)-> a.zip(b,(z1,z2)->z1.appendStream(z2));

    return stream.reduce(identity,combineToStream,combineStreams);
  }
  public static <L1,L2,T,R> LazyEither3<L1,L2,ReactiveSeq<R>> traverse(Function<? super T,? extends R> fn,ReactiveSeq<LazyEither3<L1,L2,T>> stream) {
    return sequence(stream.map(h->h.map(fn)));
  }
    /**
     * Traverse a Collection of Either3 producing an Either3 with a Seq, applying the transformation function to every
     * element in the list
     *
     * @param xors Either3s to sequence and transform
     * @param fn Transformation function
     * @return An Either3 with a transformed list
     */
    public static <LT1,LT2, PT,R> LazyEither3<LT1,LT2,ReactiveSeq<R>> traverse(final Iterable<LazyEither3<LT1, LT2, PT>> xors, Function<? super PT, ? extends R> fn) {
        return  sequence(xors).map(l->l.map(fn));
    }


    /**
     *  Accumulate the results only from those Either3 which have a Right type present, using the supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see com.oath.cyclops.Monoids }.
     *
     * <pre>
     * {@code
     * Either3<String,String,Integer> just  = Either3.right(10);
       Either3<String,String,Integer> none = Either3.left("none");
     *
     *  Either3<Seq<String>,Seq<String>,Integer> xors = Either3.accumulateRight(Monoids.intSum,Seq.of(just,none,Either3.right(1)));
        //Either3.right(11);
     *
     * }
     * </pre>
     *
     *
     *
     * @param xors Collection of Eithers to accumulate right values
     * @param reducer  Reducer to accumulate results
     * @return  Either3 populated with the accumulate right operation
     */
    public static <LT1,LT2, RT> LazyEither3<LT1,LT2, RT> accumulate(final Monoid<RT> reducer, final Iterable<LazyEither3<LT1, LT2, RT>> xors) {
        return sequence(xors).map(s -> s.reduce(reducer));
    }


    /**
     * Lazily construct a Right Either from the supplied publisher
     * <pre>
     * {@code
     *   ReactiveSeq<Integer> stream =  ReactiveSeq.of(1,2,3);

         Either3<Throwable,String,Integer> future = Either3.fromPublisher(stream);

         //Either[1]
     *
     * }
     * </pre>
     * @param pub Publisher to construct an Either from
     * @return Either constructed from the supplied Publisher
     */
    public static <T1,T> LazyEither3<Throwable, T1, T> fromPublisher(final Publisher<T> pub) {
        if(pub instanceof LazyEither3)
            return (LazyEither3<Throwable,T1,T>)pub;
        return fromFuture(Future.fromPublisher(pub));
    }
    /**
     * Construct a Right Either3 from the supplied Iterable
     * <pre>
     * {@code
     *   List<Integer> list =  Arrays.asList(1,2,3);

         Either3<Throwable,String,Integer> future = Either3.fromIterable(list);

         //Either[1]
     *
     * }
     * </pre>
     * @param iterable Iterable to construct an Either from
     * @return Either constructed from the supplied Iterable
     */
    public static <ST, T,RT> LazyEither3<ST, T,RT> fromIterable(final Iterable<RT> iterable) {
        return LazyEither3.later(()->{
            final Iterator<RT> it = iterable.iterator();
            return it.hasNext() ? LazyEither3.right( it.next()) : LazyEither3.left1(null);
        });
    }
    static <ST, T,RT> LazyEither3<ST, T,RT> later(Supplier<LazyEither3<ST,T,RT>> lazy){
        return new LazyEither3.Lazy<>(Eval.later(lazy));
    }
    static <ST, T,RT> LazyEither3<ST, T,RT> always(Supplier<LazyEither3<ST,T,RT>> lazy){
        return new LazyEither3.Lazy<>(Eval.always(lazy));
    }
    /**
     * Static method useful as a method reference for fluent consumption of any value type stored in this Either
     * (will capture the lowest common type)
     *
     * <pre>
     * {@code
     *
     *   myEither.to(Either3::consumeAny)
                 .accept(System.out::println);
     * }
     * </pre>
     *
     * @param either Either to consume value for
     * @return Consumer we can applyHKT to consume value
     */
    static <X, LT extends X, M extends X, RT extends X>  Consumer<Consumer<? super X>> consumeAny(LazyEither3<LT, M, RT> either){
        return in->visitAny(in,either);
    }

    static <X, LT extends X, M extends X, RT extends X,R>  Function<Function<? super X, R>,R> applyAny(LazyEither3<LT, M, RT> either){
        return in->visitAny(either,in);
    }
    @Deprecated //use foldAny
    static <X, LT extends X, M extends X, RT extends X,R> R visitAny(LazyEither3<LT, M, RT> either, Function<? super X, ? extends R> fn){
        return foldAny(either,fn);
    }
    static <X, LT extends X, M extends X, RT extends X,R> R foldAny(LazyEither3<LT, M, RT> either, Function<? super X, ? extends R> fn){
        return either.fold(fn, fn,fn);
    }

    static <X, LT extends X, M extends X, RT extends X> X visitAny(Consumer<? super X> c, LazyEither3<LT, M, RT> either){
        Function<? super X, X> fn = x ->{
            c.accept(x);
            return x;
        };
        return visitAny(either,fn);
    }
    /**
     * Construct a Either3#Right from an Eval
     *
     * @param right Eval to construct Either3#Right from
     * @return Either3 right instance
     */
    public static <LT, B, RT> LazyEither3<LT, B, RT> rightEval(final Eval<RT> right) {
        return new Right<>(
                           right);
    }

    /**
     * Construct a Either3#Left1 from an Eval
     *
     * @param left Eval to construct Either3#Left from
     * @return Either3 left instance
     */
    public static <LT, B, RT> LazyEither3<LT, B, RT> left1Eval(final Eval<LT> left) {
        return new Left1<>(
                          left);
    }

    /**
     * Construct a Either3#Right
     *
     * @param right Value to store
     * @return Either3 Right instance
     */
    public static <LT, B, RT> LazyEither3<LT, B, RT> right(final RT right) {
        return new Right<>(
                           Eval.later(()->right));
    }

    /**
     * Construct a Either3#Left1
     *
     * @param left Value to store
     * @return Left instance
     */
    public static <LT, B, RT> LazyEither3<LT, B, RT> left1(final LT left) {
        return new Left1<>(
                          Eval.now(left));
    }

    /**
     * Construct a Either3#Left2
     *
     * @param middle Value to store
     * @return Left2 instance
     */
    public static <LT, B, RT> LazyEither3<LT, B, RT> left2(final B middle) {
        return new Left2<>(
                            Eval.now(middle));
    }

    /**
     * Construct a Either3#Left2 from an Eval
     *
     * @param middle Eval to construct Either3#middle from
     * @return Either3 Left2 instance
     */
    public static <LT, B, RT> LazyEither3<LT, B, RT> left2Eval(final Eval<B> middle) {
        return new Left2<>(
                            middle);
    }

    /**
     * Visit the types in this Either3, only one user supplied function is executed depending on the type
     *
     * @param left1 Function to execute if this Either3 is a Left instance
     * @param mid Function to execute if this Either3 is a middle instance
     * @param right Function to execute if this Either3 is a right instance
     * @return Result of executed function
     */
    <R> R fold(final Function<? super LT1, ? extends R> left1, final Function<? super LT2, ? extends R> mid,
               final Function<? super RT, ? extends R> right);

    /**
     * Filter this Either3 resulting in a Maybe#none if it is not a Right instance or if the predicate does not
     * hold. Otherwise results in a Maybe containing the current value
     *
     * @param test Predicate to applyHKT to filter this Either3
     * @return Maybe containing the current value if this is a Right instance and the predicate holds, otherwise Maybe#none
     */
    Maybe<RT> filter(Predicate<? super RT> test);


    /**
     * Flattening transformation on this Either3. Contains an internal trampoline so will convert tail-recursive calls
     * to iteration.
     *
     * @param mapper Mapping function
     * @return Mapped Either3
     */
    <R2> LazyEither3<LT1, LT2, R2> flatMap(Function<? super RT, ? extends LazyEither3<LT1,LT2,? extends R2>> mapper);



    @Override
    LazyEither3<LT1, LT2, RT> recoverWith(Supplier<? extends LazyEither3<LT1, LT2, RT>> fn);

    /**
     * @return Swap the middle and the right types
     */
    LazyEither3<LT1, RT, LT2> swap2();

    /**
     * @return Swap the right and left types
     */
    LazyEither3<RT, LT2, LT1> swap1();

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
     * Return an Ior that can be this object or a Ior.right or Ior.left
     * @return new Ior
     */
     default Ior<LT1, RT> toIor() {
        return this.fold(l->Ior.left(l),
                          m->Ior.left(null),
                          r->Ior.right(r));
    }
     default Either<LT1, RT> toEither() {
         return this.fold(l-> Either.left(l),
                           m-> Either.left(null),
                           r-> Either.right(r));
     }



     Option<RT> get();

    @Override
    default <U> Maybe<U> ofType(Class<? extends U> type) {
        return (Maybe<U> )Filters.super.ofType(type);
    }


    @Override
    default Maybe<RT> filterNot(Predicate<? super RT> predicate) {
        return (Maybe<RT>)Filters.super.filterNot(predicate);
    }

    default Trampoline<LazyEither3<LT1,LT2,RT>> toTrampoline() {
        return Trampoline.more(()->Trampoline.done(this));
    }


    @Override
    default Maybe<RT> notNull() {

        return (Maybe<RT>)Filters.super.notNull();
    }



    default <R> LazyEither3<LT1,LT2,R> coflatMap(Function<? super LazyEither3<LT1,LT2,RT>, R> mapper) {

        return mapper.andThen(r -> unit(r))
                .apply(this);
    }

    default LazyEither3<LT1,LT2,LazyEither3<LT1,LT2,RT>> nest() {

        return this.map(t -> unit(t));
    }

    default <T2, R1, R2, R3, R> LazyEither3<LT1,LT2,R> forEach4(Function<? super RT, ? extends LazyEither3<LT1,LT2,R1>> value1,
                                                                BiFunction<? super RT, ? super R1, ? extends LazyEither3<LT1,LT2,R2>> value2,
                                                                Function3<? super RT, ? super R1, ? super R2, ? extends LazyEither3<LT1,LT2,R3>> value3,
                                                                Function4<? super RT, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return this.flatMap(in-> {

            LazyEither3<LT1,LT2,R1> a = value1.apply(in);
            return a.flatMap(ina-> {
                LazyEither3<LT1,LT2,R2> b = value2.apply(in,ina);
                return b.flatMap(inb-> {
                    LazyEither3<LT1,LT2,R3> c= value3.apply(in,ina,inb);
                    return c.map(in2 -> yieldingFunction.apply(in, ina, inb, in2));
                });

            });

        });
    }


    default <T2, R1, R2, R> LazyEither3<LT1,LT2,R> forEach3(Function<? super RT, ? extends LazyEither3<LT1,LT2,R1>> value1,
                                                            BiFunction<? super RT, ? super R1, ? extends LazyEither3<LT1,LT2,R2>> value2,
                                                            Function3<? super RT, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return this.flatMap(in-> {

            LazyEither3<LT1,LT2,R1> a = value1.apply(in);
            return a.flatMap(ina-> {
                LazyEither3<LT1,LT2,R2> b = value2.apply(in,ina);
                return b.map(in2->yieldingFunction.apply(in,ina, in2));
            });

        });
    }

    default <R1, R> LazyEither3<LT1,LT2,R> forEach2(Function<? super RT, ? extends LazyEither3<LT1,LT2,R1>> value1,
                                                    BiFunction<? super RT, ? super R1, ? extends R> yieldingFunction) {

        return this.flatMap(in-> {
            LazyEither3<LT1,LT2,R1> b = value1.apply(in);
            return b.map(in2->yieldingFunction.apply(in, in2));
        });
    }


    /*
     * (non-Javadoc)
     *
     * @see com.oath.cyclops.types.functor.BiTransformable#bimap(java.util.function.Function,
     * java.util.function.Function)
     */
    @Override
    <R1, R2> LazyEither3<LT1, R1, R2> bimap(Function<? super LT2, ? extends R1> fn1, Function<? super RT, ? extends R2> fn2);

    /*
     * (non-Javadoc)
     *
     * @see com.oath.cyclops.types.Functor#transform(java.util.function.Function)
     */
    @Override
    <R> LazyEither3<LT1, LT2, R> map(Function<? super RT, ? extends R> fn);






    /*
     * (non-Javadoc)
     *
     * @see com.oath.cyclops.types.Pure#unit(java.lang.Object)
     */
    @Override
    <T> LazyEither3<LT1, LT2, T> unit(T unit);


    /*
     * (non-Javadoc)
     *
     * @see com.oath.cyclops.types.functor.BiTransformable#bipeek(java.util.function.Consumer,
     * java.util.function.Consumer)
     */
    @Override
    default LazyEither3<LT1, LT2, RT> bipeek(final Consumer<? super LT2> c1, final Consumer<? super RT> c2) {

        return (LazyEither3<LT1, LT2, RT>) BiTransformable.super.bipeek(c1, c2);
    }




    /*
     * (non-Javadoc)
     *
     * @see com.oath.cyclops.types.Functor#peek(java.util.function.Consumer)
     */
    @Override
    default LazyEither3<LT1, LT2, RT> peek(final Consumer<? super RT> c) {

        return (LazyEither3<LT1, LT2, RT>) Transformable.super.peek(c);
    }





    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    final static class Lazy<ST, M, PT> implements LazyEither3<ST, M, PT> {

        private final Eval<LazyEither3<ST, M, PT>> lazy;



        private static <ST, M, PT> Lazy<ST, M, PT> lazy(final Eval<LazyEither3<ST, M, PT>> lazy) {
            return new Lazy<>(
                              lazy);
        }

        @Override
        public <R> LazyEither3<ST, M, R> map(final Function<? super PT, ? extends R> mapper) {

            return flatMap(t -> LazyEither3.right(mapper.apply(t)));

        }

        @Override
        public <RT1> LazyEither3<ST, M, RT1> flatMap(
                final Function<? super PT, ? extends LazyEither3<ST,M,? extends RT1>> mapper) {

            return LazyEither3.fromLazy(lazy.map(m->m.flatMap(mapper)));


        }

        @Override
        public LazyEither3<ST, M, PT> recoverWith(Supplier<? extends LazyEither3<ST, M, PT>> fn) {
            return new Lazy(
                lazy.map(m -> m.recoverWith(fn)));
        }

        @Override
        public Trampoline<LazyEither3<ST,M,PT>> toTrampoline() {
            Trampoline<LazyEither3<ST,M,PT>> trampoline = lazy.toTrampoline();
            return new Trampoline<LazyEither3<ST,M,PT>>() {
                @Override
                public LazyEither3<ST,M,PT> get() {
                    LazyEither3<ST,M,PT> either = lazy.get();
                    while (either instanceof LazyEither3.Lazy) {
                        either = ((LazyEither3.Lazy<ST,M,PT>) either).lazy.get();
                    }
                    return either;
                }
                @Override
                public boolean complete(){
                    return false;
                }
                @Override
                public Trampoline<LazyEither3<ST,M,PT>> bounce() {
                    LazyEither3<ST,M,PT> either = lazy.get();
                    if(either instanceof LazyEither3.Lazy){
                        return either.toTrampoline();
                    }
                    return Trampoline.done(either);

                }
            };
        }

        @Override
        public Maybe<PT> filter(final Predicate<? super PT> test) {
            return Maybe.fromLazy(lazy.map(m->m.filter(test)));

        }


        public Option<PT> get() {
            return trampoline().get();
        }

        private LazyEither3<ST,M,PT> trampoline(){
            LazyEither3<ST,M,PT> maybe = lazy.get();
            while (maybe instanceof Lazy) {
                maybe = ((Lazy<ST,M,PT>) maybe).lazy.get();
            }
            return maybe;
        }
        @Override
        public ReactiveSeq<PT> stream() {

            return Spouts.from(this);
        }

        @Override
        public Iterator<PT> iterator() {

            return trampoline()
                       .iterator();
        }

        @Override
        public <R> R fold(final Function<? super PT, ? extends R> present, final Supplier<? extends R> absent) {

            return trampoline()
                       .fold(present, absent);
        }

        @Override
        public final void subscribe(final Subscriber<? super PT> sub) {
            lazy.subscribe(new Subscriber<LazyEither3<ST, M,PT>>() {
                boolean onCompleteSent = false;
                @Override
                public void onSubscribe(Subscription s) {
                    sub.onSubscribe(s);
                }

                @Override
                public void onNext(LazyEither3<ST,M, PT> pts) {
                    if(pts.isRight()){
                        PT v = pts.orElse(null);
                        if(v!=null)
                            sub.onNext(v);
                    }else if(pts.isLeft1()){
                        ST v = pts.swap1().orElse(null);
                        if(v instanceof Throwable)
                            sub.onError((Throwable)v);
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
        public <R> R fold(final Function<? super ST, ? extends R> secondary,
                          final Function<? super M, ? extends R> mid, final Function<? super PT, ? extends R> primary) {

            return trampoline()
                       .fold(secondary, mid, primary);
        }

        @Override
        public LazyEither3<ST, PT, M> swap2()
        {
            return LazyEither3.fromLazy(lazy.map(m->m.swap2()));
        }

        @Override
        public LazyEither3<PT, M, ST> swap1() {
            return LazyEither3.fromLazy(lazy.map(m->m.swap1()));
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
        public <R1, R2> LazyEither3<ST, R1, R2> bimap(final Function<? super M, ? extends R1> fn1,
                                                      final Function<? super PT, ? extends R2> fn2) {
            return LazyEither3.fromLazy(lazy.map(m->m.bimap(fn1,fn2)));
        }

        @Override
        public <T> LazyEither3<ST, M, T> unit(final T unit) {

            return LazyEither3.right(unit);
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return this.fold(LazyEither3::left1, LazyEither3::left2, LazyEither3::right).hashCode();
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
           return this.fold(LazyEither3::left1, LazyEither3::left2, LazyEither3::right).equals(obj);
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
    static class Right<ST, M, PT> implements LazyEither3<ST, M, PT> {
        private final Eval<PT> value;

        @Override
        public <R> LazyEither3<ST, M, R> map(final Function<? super PT, ? extends R> fn) {
            return new Right<ST, M, R>(
                                       value.map(fn));
        }

        @Override
        public LazyEither3<ST, M, PT> peek(final Consumer<? super PT> action) {
            return map(i -> {
                action.accept(i);
                return i;
            });

        }

        @Override
        public Maybe<PT> filter(final Predicate<? super PT> test) {

            return Maybe.fromEval(Eval.later(() -> test.test(value.get()) ? Maybe.just(value.get()) : Maybe.<PT>nothing()))
                        .flatMap(Function.identity());

        }

        @Override
        public Option<PT> get() {
            return Option.some(value.get());
        }

        @Override
        public <RT1> LazyEither3<ST, M, RT1> flatMap(
                final Function<? super PT, ? extends LazyEither3<ST,M,? extends RT1>> mapper) {

            Eval<? extends LazyEither3<? extends ST, ? extends M, ? extends RT1>> et = value.map(mapper);


            final Eval<LazyEither3<ST, M, RT1>> e3 =  (Eval<LazyEither3<ST, M, RT1>>)et;
            return new Lazy<>(
                              e3);


        }

        @Override
        public LazyEither3<ST, M, PT> recoverWith(Supplier<? extends LazyEither3<ST, M, PT>> fn) {
            return this;
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
            return "Either3.right[" + value.get() + "]";
        }

        @Override
        public <R> R fold(final Function<? super ST, ? extends R> secondary,
                          final Function<? super M, ? extends R> mid, final Function<? super PT, ? extends R> primary) {
            return primary.apply(value.get());
        }



        @Override
        public <R1, R2> LazyEither3<ST, R1, R2> bimap(final Function<? super M, ? extends R1> fn1,
                                                      final Function<? super PT, ? extends R2> fn2) {
            return (LazyEither3<ST, R1, R2>) this.map(fn2);
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
        public <R> R fold(final Function<? super PT, ? extends R> present, final Supplier<? extends R> absent) {
            return value.fold(present, absent);
        }

        @Override
        public void subscribe(final Subscriber<? super PT> s) {
            value.subscribe(s);

        }


        @Override
        public <T> LazyEither3<ST, M, T> unit(final T unit) {
            return LazyEither3.right(unit);
        }

        @Override
        public LazyEither3<ST, PT, M> swap2() {

            return new Left2<>(value);
        }

        @Override
        public LazyEither3<PT, M, ST> swap1() {

            return new Left1<>(
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
    static class Left1<ST, M, PT> implements LazyEither3<ST, M, PT> {
        private final Eval<ST> value;

        @Override
        public <R> LazyEither3<ST, M, R> map(final Function<? super PT, ? extends R> fn) {
            return (LazyEither3<ST, M, R>) this;
        }

        @Override
        public LazyEither3<ST, M, PT> peek(final Consumer<? super PT> action) {
            return this;

        }
        @Override
        public void subscribe(final Subscriber<? super PT> s) {
            s.onComplete();
        }

        @Override
        public Maybe<PT> filter(final Predicate<? super PT> test) {

            return Maybe.nothing();

        }

        @Override
        public Option<PT> get() {
            return Option.none();
        }

        @Override
        public <RT1> LazyEither3<ST, M, RT1> flatMap(
                final Function<? super PT, ? extends LazyEither3<ST,M,? extends RT1>> mapper) {

            return (LazyEither3) this;

        }

        @Override
        public LazyEither3<ST, M, PT> recoverWith(Supplier<? extends LazyEither3<ST, M, PT>> fn) {
            return new Lazy<ST,M,PT>(Eval.narrow(Eval.later((fn))));
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
        public String toString() {
            return mkString();
        }

        @Override
        public String mkString() {
            return "Either3.left1[" + value.get() + "]";
        }

        @Override
        public <R> R fold(final Function<? super ST, ? extends R> secondary,
                          final Function<? super M, ? extends R> mid, final Function<? super PT, ? extends R> primary) {
            return secondary.apply(value.get());
        }



        @Override
        public <R1, R2> LazyEither3<ST, R1, R2> bimap(final Function<? super M, ? extends R1> fn1,
                                                      final Function<? super PT, ? extends R2> fn2) {
            return (LazyEither3<ST, R1, R2>) this;
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
        public <R> R fold(final Function<? super PT, ? extends R> present, final Supplier<? extends R> absent) {
            return absent.get();
        }


        @Override
        public <T> LazyEither3<ST, M, T> unit(final T unit) {
            return LazyEither3.right(unit);
        }

        @Override
        public LazyEither3<ST, PT, M> swap2() {

            return (LazyEither3<ST, PT, M>) this;
        }

        @Override
        public LazyEither3<PT, M, ST> swap1() {

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
    static class Left2<ST, M, PT> implements LazyEither3<ST, M, PT> {
        private final Eval<M> value;

        @Override
        public <R> LazyEither3<ST, M, R> map(final Function<? super PT, ? extends R> fn) {
            return (LazyEither3<ST, M, R>) this;
        }

        @Override
        public LazyEither3<ST, M, PT> peek(final Consumer<? super PT> action) {
            return this;

        }

        @Override
        public Maybe<PT> filter(final Predicate<? super PT> test) {

            return Maybe.nothing();

        }

        @Override
        public Option<PT> get() {
            return Option.none();
        }

        @Override
        public <RT1> LazyEither3<ST, M, RT1> flatMap(
                final Function<? super PT, ? extends LazyEither3<ST,M,? extends RT1>> mapper) {

            return (LazyEither3) this;

        }

        @Override
        public LazyEither3<ST, M, PT> recoverWith(Supplier<? extends LazyEither3<ST, M, PT>> fn) {
            return new Lazy<ST,M,PT>(Eval.narrow(Eval.later((fn))));
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
        public String toString() {
            return mkString();
        }

        @Override
        public String mkString() {
            return "Either3.left2[" + value.get() + "]";
        }

        @Override
        public <R> R fold(final Function<? super ST, ? extends R> secondary,
                          final Function<? super M, ? extends R> mid, final Function<? super PT, ? extends R> primary) {
            return mid.apply(value.get());
        }


        @Override
        public <R1, R2> LazyEither3<ST, R1, R2> bimap(final Function<? super M, ? extends R1> fn1,
                                                      final Function<? super PT, ? extends R2> fn2) {
            return (LazyEither3<ST, R1, R2>) this;
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
        public <R> R fold(final Function<? super PT, ? extends R> present, final Supplier<? extends R> absent) {
            return absent.get();
        }

        @Override
        public void subscribe(final Subscriber<? super PT> s) {
                s.onComplete();
        }


        @Override
        public <T> LazyEither3<ST, M, T> unit(final T unit) {
            return LazyEither3.right(unit);
        }

        @Override
        public LazyEither3<ST, PT, M> swap2() {
            return new Right<>(
                               value);

        }

        @Override
        public LazyEither3<PT, M, ST> swap1() {
            return (LazyEither3<PT, M, ST>) this;

        }

        @Override
        public boolean isLeft2() {

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

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            if(obj instanceof Lazy){
                return ((Lazy)obj).equals(this);
            }
            Left2 other = (Left2) obj;
            if (value == null) {
                if (other.value != null)
                    return false;
            } else if (!value.equals(other.value))
                return false;
            return true;
        }

    }
    public static <L1,L2,T> LazyEither3<L1,L2,T> narrowK3(final Higher3<lazyEither3, L1,L2,T> xor) {
        return (LazyEither3<L1,L2,T>)xor;
    }
    public static <L1,L2,T> LazyEither3<L1,L2,T> narrowK(final Higher<Higher<Higher<lazyEither3, L1>,L2>,T> xor) {
        return (LazyEither3<L1,L2,T>)xor;
    }


}
