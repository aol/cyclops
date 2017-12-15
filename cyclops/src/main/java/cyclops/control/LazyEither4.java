package cyclops.control;

import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.hkt.Higher4;
import com.oath.cyclops.types.Filters;
import com.oath.cyclops.types.OrElseValue;
import com.oath.cyclops.types.factory.Unit;
import com.oath.cyclops.types.foldable.To;
import com.oath.cyclops.types.functor.BiTransformable;
import com.oath.cyclops.types.functor.Transformable;
import com.oath.cyclops.types.reactive.Completable;
import com.oath.cyclops.types.traversable.IterableX;
import cyclops.function.*;

import com.oath.cyclops.hkt.DataWitness.lazyEither4;
import cyclops.reactive.ReactiveSeq;
import cyclops.typeclasses.*;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functions.MonoidK;
import cyclops.typeclasses.functor.Functor;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.*;

/**
 * A right biased Lazy Either4 type. transform / flatMap operators are tail-call optimized
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
 * @param <RT> Right type (operations are performed on this type if present)
 */
public interface LazyEither4<LT1, LT2,LT3, RT> extends Transformable<RT>,
                                                      Filters<RT>,
                                                      Higher4<lazyEither4,LT1,LT2,LT3,RT>,
                                                      BiTransformable<LT3, RT>,
                                                      To<LazyEither4<LT1, LT2,LT3, RT>>,
                                                      OrElseValue<RT,LazyEither4<LT1,LT2,LT3,RT>>,
                                                      Unit<RT>{

    Option<RT> get();
    public static  <LT1,LT2,LT3,T> Kleisli<Higher<Higher<Higher<lazyEither4, LT1>, LT2>,LT3>,LazyEither4<LT1,LT2,LT3,T>,T> kindKleisli(){
        return Kleisli.of(Instances.monad(), LazyEither4::widen);
    }
    public static <LT1,LT2,LT3,T> Higher<Higher<Higher<Higher<lazyEither4, LT1>, LT2>,LT3>,T> widen(LazyEither4<LT1,LT2,LT3,T> narrow) {
        return narrow;
    }
    public static  <LT1,LT2,LT3,T> Cokleisli<Higher<Higher<Higher<lazyEither4, LT1>, LT2>,LT3>,T,LazyEither4<LT1,LT2,LT3,T>> kindCokleisli(){
        return Cokleisli.of(LazyEither4::narrowK);
    }
    /**
     * Create a reactiveBuffer CompletableEither
     *
     * <pre>
     *  {@code
     *      ___Example 1___
     *
     *      CompletableEither<Integer,Integer> completable = Either4.lazyEither4();
            Either4<Throwable,String,Integer> mapped = completable.map(i->i*2)
                                                                .flatMap(i->Eval.later(()->i+1));

            completable.complete(5);

            mapped.printOut();
            //11

            ___Example 2___

            CompletableEither<Integer,Integer> completable = Either4.lazyEither4();
            Either4<Throwable,String,Integer> mapped = completable.map(i->i*2)
                                                                .flatMap(i->Eval.later(()->i+1));


            completable.complete(null);

            //Either4:Left4[NoSuchElementException]

            ___Example 3___

            CompletableEither<Integer,Integer> completable = Either4.lazyEither4();
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
    static <LT2,LT3,RT> LazyEither4.CompletableEither4<RT,LT2,LT3,RT> either4(){
        Completable.CompletablePublisher<RT> c = new Completable.CompletablePublisher<RT>();
        return new LazyEither4.CompletableEither4<RT,LT2,LT3, RT>(c,fromFuture(Future.fromPublisher(c)));
    }

    default LazyEither4<LT1,LT2,LT3, RT> filter(Predicate<? super RT> test, Function<? super RT, ? extends LT1> rightToLeft){
      return flatMap(e->test.test(e) ? LazyEither4.right(e) : LazyEither4.left1(rightToLeft.apply(e)));
    }

    @AllArgsConstructor
    static class CompletableEither4<ORG,LT1,LT2,RT> implements LazyEither4<Throwable,LT1,LT2,RT>, Completable<ORG> {

        public final Completable.CompletablePublisher<ORG> complete;
        public final LazyEither4<Throwable, LT1,LT2, RT> either;

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
        public Option<RT> get() {
            return either.get();
        }

        @Override
        public <R> R visit(Function<? super Throwable, ? extends R> left1, Function<? super LT1, ? extends R> left2, Function<? super LT2, ? extends R> left3, Function<? super RT, ? extends R> right) {
            return either.visit(left1,left2,left3,right);
        }

        @Override
        public Maybe<RT> filter(Predicate<? super RT> test) {
            return either.filter(test);
        }

        @Override
        public <RT1> LazyEither4<Throwable, LT1, LT2, RT1> flatMap(Function<? super RT, ? extends LazyEither4<Throwable,LT1,LT2,? extends RT1>> mapper) {
            return either.flatMap(mapper);
        }

        @Override
        public LazyEither4<Throwable, LT1, RT, LT2> swap3() {
            return either.swap3();
        }

        @Override
        public LazyEither4<Throwable, RT, LT2, LT1> swap2() {
            return either.swap2();
        }

        @Override
        public LazyEither4<RT, LT1, LT2, Throwable> swap1() {
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
        public <R1, R2> LazyEither4<Throwable, LT1, R1, R2> bimap(Function<? super LT2, ? extends R1> fn1, Function<? super RT, ? extends R2> fn2) {
            return either.bimap(fn1,fn2);
        }

        @Override
        public <R> LazyEither4<Throwable, LT1, LT2, R> map(Function<? super RT, ? extends R> fn) {
            return either.map(fn);
        }

        @Override
        public <T> LazyEither4<Throwable, LT1, LT2, T> unit(T unit) {
            return either.unit(unit);
        }

        @Override
        public <R> R visit(Function<? super RT, ? extends R> present, Supplier<? extends R> absent) {
            return either.visit(present,absent);
        }
    }


        /**
     * Static method useful as a method reference for fluent consumption of any value type stored in this Either
     * (will capture the lowest common type)
     *
     * <pre>
     * {@code
     *
     *   myEither.to(Either4::consumeAny)
                 .accept(System.out::println);
     * }
     * </pre>
     *
     * @param either Either to consume value for
     * @return Consumer we can applyHKT to consume value
     */
    static <X, LT1 extends X, LT2 extends X, LT3 extends X, RT extends X> Consumer<Consumer<? super X>> consumeAny(
            LazyEither4<LT1, LT2, LT3, RT> either) {
        return in -> visitAny(in, either);
    }

    static <X, LT1 extends X, LT2 extends X, LT3 extends X, RT extends X, R> Function<Function<? super X, R>, R> applyAny(
            LazyEither4<LT1, LT2, LT3, RT> either) {
        return in -> visitAny(either, in);
    }

    static <X, LT1 extends X, LT2 extends X, LT3 extends X, RT extends X, R> R visitAny(
            LazyEither4<LT1, LT2, LT3, RT> either, Function<? super X, ? extends R> fn) {
        return either.visit(fn, fn, fn, fn);
    }

    static <X, LT1 extends X, LT2 extends X, LT3 extends X, RT extends X> X visitAny(Consumer<? super X> c,
                                                                                     LazyEither4<LT1, LT2, LT3, RT> either) {
        Function<? super X, X> fn = x -> {
            c.accept(x);
            return x;
        };
        return visitAny(either, fn);
    }




      static <LT1,LT2,LT3,RT> LazyEither4<LT1,LT2,LT3,RT> fromLazy(Eval<LazyEither4<LT1,LT2,LT3,RT>> lazy){
               return new LazyEither4.Lazy<>(lazy);
     }

     static <LT2,LT3,RT> LazyEither4<Throwable,LT2,LT3,RT> fromFuture(Future<RT> future){
         return fromLazy(Eval.<LazyEither4<Throwable,LT2,LT3,RT>>fromFuture(
                 future.map(e->e!=null? LazyEither4.<Throwable,LT2,LT3,RT>right(e): LazyEither4.<Throwable,LT2,LT3,RT>left1(new NoSuchElementException()))
                         .recover(t-> LazyEither4.<Throwable,LT2,LT3,RT>left1(t.getCause()))));
     }

    /**
     *  Turn a toX of Either3 into a single Either with Lists of values.
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
     * @param xors Either3 to sequence
     * @return Either3 Sequenced
     */
    public static <LT1,LT2,LT3, PT> LazyEither4<LT1,LT2,LT3,ReactiveSeq<PT>> sequence(final IterableX<? extends LazyEither4<LT1, LT2, LT3, PT>> xors) {
        Objects.requireNonNull(xors);
        return sequence(xors.stream().filter(LazyEither4::isRight));
    }
  public static  <L1,L2,L3,T> LazyEither4<L1, L2, L3, ReactiveSeq<T>> sequence(ReactiveSeq<? extends LazyEither4<L1, L2, L3, T>> stream) {

    LazyEither4<L1, L2, L3, ReactiveSeq<T>> identity = right(ReactiveSeq.empty());

    BiFunction<LazyEither4<L1, L2, L3, ReactiveSeq<T>>,LazyEither4<L1, L2, L3, T>,LazyEither4<L1, L2, L3,ReactiveSeq<T>>> combineToStream = (acc,next) ->acc.zip(next,(a,b)->a.appendAll(b));

    BinaryOperator<LazyEither4<L1, L2, L3,ReactiveSeq<T>>> combineStreams = (a,b)-> a.zip(b,(z1,z2)->z1.appendStream(z2));

    return stream.reduce(identity,combineToStream,combineStreams);
  }
  public static <L1,L2,L3,T,R> LazyEither4<L1, L2, L3, ReactiveSeq<R>> traverse(Function<? super T,? extends R> fn,ReactiveSeq<LazyEither4<L1, L2, L3,T>> stream) {
    return sequence(stream.map(h->h.map(fn)));
  }
    /**
     * TraverseOps a Collection of Either3 producing an Either4 with a ListX, applying the transformation function to every
     * element in the list
     *
     * @param xors Either4s to sequence and transform
     * @param fn Transformation function
     * @return An Either4 with a transformed list
     */
    public static <LT1,LT2, LT3,PT,R> LazyEither4<LT1,LT2,LT3,ReactiveSeq<R>> traverse(final IterableX<LazyEither4<LT1, LT2, LT3, PT>> xors, Function<? super PT, ? extends R> fn) {
        return  sequence(xors).map(l->l.map(fn));
    }


    /**
     *  Accumulate the results only from those Either3 which have a Right type present, using the supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see com.oath.cyclops.Monoids }.
     *
     * <pre>
     * {@code
     * Either4<String,String,String,Integer> just  = Either4.right(10);
       Either4<String,String,String,Integer> none = Either4.left("none");
     *
     *  Either4<ListX<String>,ListX<String>,Integer> xors = Either4.accumulateRight(Monoids.intSum,ListX.of(just,none,Either4.right(1)));
        //Either4.right(11);
     *
     * }
     * </pre>
     *
     *
     *
     * @param xors Collection of Eithers to accumulate right values
     * @param reducer  Reducer to accumulate results
     * @return  Either4 populated with the accumulate right operation
     */
    public static <LT1,LT2,LT3, RT> LazyEither4<LT1, LT2,LT3, RT> accumulate(final Monoid<RT> reducer, final IterableX<LazyEither4<LT1, LT2, LT3, RT>> xors) {
        return sequence(xors).map(s -> s.reduce(reducer));
    }



    /**
     * Lazily construct a Right Either from the supplied publisher
     * <pre>
     * {@code
     *   ReactiveSeq<Integer> reactiveStream =  ReactiveSeq.of(1,2,3);

         Either4<Throwable,String,String,Integer> lazy = Either4.fromPublisher(reactiveStream);

         //Either[1]
     *
     * }
     * </pre>
     * @param pub Publisher to construct an Either from
     * @return Either constructed from the supplied Publisher
     */
    public static <T1,T2,T> LazyEither4<Throwable, T1, T2, T> fromPublisher(final Publisher<T> pub) {
        return fromFuture(Future.fromPublisher(pub));
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
    public static <ST, T, T2,RT> LazyEither4<ST, T,T2,RT> fromIterable(final Iterable<RT> iterable) {

        final Iterator<RT> it = iterable.iterator();
        return it.hasNext() ? LazyEither4.right( it.next()) : LazyEither4.left1(null);
    }

    /**
     * Construct a Either4#Right from an Eval
     *
     * @param right Eval to construct Either4#Right from
     * @return Either4 right instance
     */
    public static <LT, M1,B, RT> LazyEither4<LT, M1,B, RT> rightEval(final Eval<RT> right) {
        return new Right<>(
                           right);
    }

    /**
     * Construct a Either4#Left1 from an Eval
     *
     * @param left Eval to construct Either4#Left1 from
     * @return Either4 Left1 instance
     */
    public static <LT, M1, B, RT> LazyEither4<LT, M1, B, RT> left1Eval(final Eval<LT> left) {
        return new Left1<>(
                          left);
    }

    /**
     * Construct a Either4#Right
     *
     * @param right Value to store
     * @return Either4 Right instance
     */
    public static <LT, M1, B, RT> LazyEither4<LT, M1, B, RT> right(final RT right) {
        return new Right<>(
                           Eval.later(()->right));
    }

    /**
     * Construct a Either4#Left1
     *
     * @param left Value to store
     * @return Left1 instance
     */
    public static <LT, M1, B, RT> LazyEither4<LT, M1, B, RT> left1(final LT left) {
        return new Left1<>(
                          Eval.now(left));
    }

    /**
     * Construct a Either4#Second
     *
     * @param middle Value to store
     * @return Second instance
     */
    public static <LT, M1, B, RT> LazyEither4<LT, M1, B, RT> left2(final M1 middle) {
        return new Left2<>(
                            Eval.now(middle));
    }
    /**
     * Construct a Either4#Third
     *
     * @param middle Value to store
     * @return Third instance
     */
    public static <LT, M1, B, RT> LazyEither4<LT, M1, B, RT> left3(final B middle) {
        return new Left3<>(
                            Eval.now(middle));
    }

    /**
     * Construct a Either4#Second from an Eval
     *
     * @param middle Eval to construct Either4#middle from
     * @return Either4 second instance
     */
    public static <LT, M1, B, RT> LazyEither4<LT, M1, B, RT> left2Eval(final Eval<M1> middle) {
        return new Left2<>(
                            middle);
    }
    /**
     * Construct a Either4#Third from an Eval
     *
     * @param middle Eval to construct Either4#middle from
     * @return Either4 third instance
     */
    public static <LT, M1, B, RT> LazyEither4<LT, M1, B, RT> left3Eval(final Eval<B> middle) {
        return new Left3<>(
                middle);
    }


    @Override
    default <R> LazyEither4<LT1,LT2,LT3,R> retry(final Function<? super RT, ? extends R> fn) {
       return (LazyEither4<LT1,LT2,LT3,R>)Transformable.super.retry(fn);
    }



    @Override
    default <R> LazyEither4<LT1,LT2,LT3,R> retry(final Function<? super RT, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (LazyEither4<LT1,LT2,LT3,R>)Transformable.super.retry(fn,retries,delay,timeUnit);
    }

    default Trampoline<LazyEither4<LT1,LT2,LT3,RT>> toTrampoline() {
        return Trampoline.more(()->Trampoline.done(this));
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
    < RT1> LazyEither4<LT1, LT2,LT3, RT1> flatMap(
            Function<? super RT, ? extends LazyEither4<LT1,LT2,LT3,? extends RT1>> mapper);
    /**
     * @return Swap the third and the right types
     */
    LazyEither4<LT1,LT2, RT, LT3> swap3();
    /**
     * @return Swap the second and the right types
     */
    LazyEither4<LT1, RT,LT3, LT2> swap2();

    /**
     * @return Swap the right and left types
     */
    LazyEither4<RT, LT2,LT3, LT1> swap1();

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


    default <T2, R> LazyEither4<LT1, LT2, LT3,R> zip(final LazyEither4<LT1, LT2,LT3,? extends T2> app, final BiFunction<? super RT, ? super T2, ? extends R> fn){
        return flatMap(t->app.map(t2->fn.apply(t,t2)));
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.Filters#ofType(java.lang.Class)
     */
    @Override
    default <U> Maybe<U> ofType(Class<? extends U> type) {

        return (Maybe<U>)Filters.super.ofType(type);
    }
    /* (non-Javadoc)
     * @see com.oath.cyclops.types.Filters#filterNot(java.util.function.Predicate)
     */
    @Override
    default Maybe<RT> filterNot(Predicate<? super RT> predicate) {

        return (Maybe<RT>)Filters.super.filterNot(predicate);
    }
    /* (non-Javadoc)
     * @see com.oath.cyclops.types.Filters#notNull()
     */
    @Override
    default Maybe<RT> notNull() {

        return (Maybe<RT>)Filters.super.notNull();
    }
    /*
     * (non-Javadoc)
     *
     * @see com.oath.cyclops.types.functor.BiTransformable#bimap(java.util.function.Function,
     * java.util.function.Function)
     */
    @Override
    <R1, R2> LazyEither4<LT1, LT2, R1, R2> bimap(Function<? super LT3, ? extends R1> fn1,
                                                 Function<? super RT, ? extends R2> fn2);

    /*
     * (non-Javadoc)
     *
     * @see com.oath.cyclops.types.functor.Transformable#transform(java.util.function.Function)
     */
    @Override
    <R> LazyEither4<LT1,LT2,LT3, R> map(Function<? super RT, ? extends R> fn);

                                                        /**
     * Return an Ior that can be this object or a Ior.right or Ior.left
     * @return new Ior
     */
     default Ior<LT1, RT> toIor() {
        return this.visit(l->Ior.left(l),
                          m->Ior.left(null),
                          m->Ior.left(null),
                          r->Ior.right(r));
    }
     default Either<LT1, RT> toEither() {
         return this.visit(l-> Either.left(l),
                           m-> Either.left(null),
                           m-> Either.left(null),
                           r-> Either.right(r));
     }




    default <R> LazyEither4<LT1,LT2,LT3,R> coflatMap(Function<? super LazyEither4<LT1,LT2,LT3,RT>, R> mapper) {
        return mapper.andThen(r -> unit(r))
                .apply(this);
    }

    default LazyEither4<LT1,LT2,LT3,LazyEither4<LT1,LT2,LT3,RT>> nest() {

        return this.map(t -> unit(t));
    }

    default <T2, R1, R2, R3, R> LazyEither4<LT1,LT2,LT3,R> forEach4(Function<? super RT, ? extends LazyEither4<LT1,LT2,LT3,R1>> value1,
                                                                    BiFunction<? super RT, ? super R1, ? extends LazyEither4<LT1,LT2,LT3,R2>> value2,
                                                                    Function3<? super RT, ? super R1, ? super R2, ? extends LazyEither4<LT1,LT2,LT3,R3>> value3,
                                                                    Function4<? super RT, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return this.flatMap(in-> {

            LazyEither4<LT1,LT2,LT3,R1> a = value1.apply(in);
            return a.flatMap(ina-> {
                LazyEither4<LT1,LT2,LT3,R2> b = value2.apply(in,ina);
                return b.flatMap(inb-> {
                    LazyEither4<LT1,LT2,LT3,R3> c= value3.apply(in,ina,inb);
                    return c.map(in2->yieldingFunction.apply(in,ina,inb,in2));
                });

            });

        });
    }


    default <T2, R1, R2, R> LazyEither4<LT1,LT2,LT3,R> forEach3(Function<? super RT, ? extends LazyEither4<LT1,LT2,LT3,R1>> value1,
                                                                BiFunction<? super RT, ? super R1, ? extends LazyEither4<LT1,LT2,LT3,R2>> value2,
                                                                Function3<? super RT, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return this.flatMap(in-> {

            LazyEither4<LT1,LT2,LT3,R1> a = value1.apply(in);
            return a.flatMap(ina-> {
                LazyEither4<LT1,LT2,LT3,R2> b = value2.apply(in,ina);
                return b.map(in2->yieldingFunction.apply(in,ina, in2));
            });

        });
    }


    default <R1, R> LazyEither4<LT1,LT2,LT3,R> forEach2(Function<? super RT, ? extends LazyEither4<LT1,LT2,LT3,R1>> value1,
                                                        BiFunction<? super RT, ? super R1, ? extends R> yieldingFunction) {

        return this.flatMap(in-> {
            LazyEither4<LT1,LT2,LT3,R1> b = value1.apply(in);
            return b.map(in2->yieldingFunction.apply(in, in2));
        });
    }



    /*
     * (non-Javadoc)
     *
     * @see com.oath.cyclops.types.Pure#unit(java.lang.Object)
     */
    @Override
    <T> LazyEither4<LT1, LT2,LT3, T> unit(T unit);

    /*
     * (non-Javadoc)
     *
     * @see com.oath.cyclops.types.functor.BiTransformable#bipeek(java.util.function.Consumer,
     * java.util.function.Consumer)
     */
    @Override
    default LazyEither4<LT1, LT2, LT3, RT> bipeek(final Consumer<? super LT3> c1, final Consumer<? super RT> c2) {

        return (LazyEither4<LT1, LT2, LT3, RT>) BiTransformable.super.bipeek(c1, c2);
    }


    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.types.functor.BiTransformable#bitrampoline(java.util.function.Function,
     * java.util.function.Function)
     */
    @Override
    default <R1, R2> LazyEither4<LT1, LT2, R1, R2> bitrampoline(
            final Function<? super LT3, ? extends Trampoline<? extends R1>> mapper1,
            final Function<? super RT, ? extends Trampoline<? extends R2>> mapper2) {

        return (LazyEither4<LT1,LT2, R1, R2>) BiTransformable.super.bitrampoline(mapper1, mapper2);
    }



    /*
     * (non-Javadoc)
     *
     * @see com.oath.cyclops.types.functor.Transformable#peek(java.util.function.Consumer)
     */
    @Override
    default LazyEither4<LT1, LT2, LT3, RT> peek(final Consumer<? super RT> c) {

        return (LazyEither4<LT1, LT2, LT3, RT>) Transformable.super.peek(c);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.types.functor.Transformable#trampoline(java.util.function.Function)
     */
    @Override
    default <R> LazyEither4<LT1, LT2, LT3, R> trampoline(final Function<? super RT, ? extends Trampoline<? extends R>> mapper) {

        return (LazyEither4<LT1, LT2, LT3, R>) Transformable.super.trampoline(mapper);
    }


    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    final static class Lazy<ST, M,M2, PT> implements LazyEither4<ST, M,M2, PT> {

        private final Eval<LazyEither4<ST, M,M2, PT>> lazy;

        public LazyEither4<ST, M,M2, PT> resolve() {
            return lazy.get()
                       .visit(LazyEither4::left1, LazyEither4::left2, LazyEither4::left3, LazyEither4::right);
        }

        private static <ST, M,M2, PT> Lazy<ST, M,M2, PT> lazy(final Eval<LazyEither4<ST, M,M2, PT>> lazy) {
            return new Lazy<>(lazy);
        }


        @Override
        public <R> LazyEither4<ST, M,M2, R> map(final Function<? super PT, ? extends R> mapper) {
            return flatMap(t -> LazyEither4.right(mapper.apply(t)));
        }

        @Override
        public <RT1> LazyEither4<ST, M,M2, RT1> flatMap(
                final Function<? super PT, ? extends LazyEither4<ST,M,M2,? extends RT1>> mapper) {
            return LazyEither4.fromLazy(lazy.map(m->m.flatMap(mapper)));
        }

        @Override
        public Maybe<PT> filter(final Predicate<? super PT> test) {

            return Maybe.fromEval(Eval.later(() -> resolve().filter(test)))
                        .flatMap(Function.identity());

        }
        @Override
        public Trampoline<LazyEither4<ST,M,M2,PT>> toTrampoline() {
            Trampoline<LazyEither4<ST,M,M2,PT>> trampoline = lazy.toTrampoline();
            return new Trampoline<LazyEither4<ST,M,M2,PT>>() {
                @Override
                public LazyEither4<ST,M,M2,PT> get() {
                    LazyEither4<ST,M,M2,PT> either = lazy.get();
                    while (either instanceof LazyEither4.Lazy) {
                        either = ((LazyEither4.Lazy<ST,M,M2,PT>) either).lazy.get();
                    }
                    return either;
                }
                @Override
                public boolean complete(){
                    return false;
                }
                @Override
                public Trampoline<LazyEither4<ST,M,M2,PT>> bounce() {
                    LazyEither4<ST,M,M2,PT> either = lazy.get();
                    if(either instanceof LazyEither4.Lazy){
                        return either.toTrampoline();
                    }
                    return Trampoline.done(either);

                }
            };
        }

        @Override
        public Option<PT> get() {
            return trampoline().get();
        }

        private LazyEither4<ST,M,M2,PT> trampoline(){
            LazyEither4<ST,M,M2,PT> maybe = lazy.get();
            while (maybe instanceof Lazy) {
                maybe = ((Lazy<ST,M,M2,PT>) maybe).lazy.get();
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
        public <R> R visit(final Function<? super ST, ? extends R> first,
                final Function<? super M, ? extends R> second,
                final Function<? super M2, ? extends R> third,
                final Function<? super PT, ? extends R> primary) {

            return trampoline()
                       .visit(first, second,third, primary);
        }
        @Override
        public LazyEither4<ST, M, PT, M2> swap3() {
            return lazy(Eval.later(() -> resolve().swap3()));
        }
        @Override
        public LazyEither4<ST, PT, M2, M> swap2() {
            return lazy(Eval.later(() -> resolve().swap2()));
        }

        @Override
        public LazyEither4<PT, M,M2, ST> swap1() {
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
        public <R1, R2> LazyEither4<ST, M,R1, R2> bimap(final Function<? super M2, ? extends R1> fn1,
                                                        final Function<? super PT, ? extends R2> fn2) {
            return lazy(Eval.later(() -> resolve().bimap(fn1, fn2)));
        }

        @Override
        public <T> LazyEither4<ST, M, M2,T> unit(final T unit) {

            return LazyEither4.right(unit);
        }
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return this.visit(LazyEither4::left1, LazyEither4::left2, LazyEither4::left3, LazyEither4::right).hashCode();
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            return this.visit(LazyEither4::left1, LazyEither4::left2, LazyEither4::left3, LazyEither4::right).equals(obj);
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
    static class Right<ST, M,M2, PT> implements LazyEither4<ST, M,M2, PT> {
        private final Eval<PT> value;

        @Override
        public <R> LazyEither4<ST, M, M2, R> map(final Function<? super PT, ? extends R> fn) {
            return new Right<ST, M, M2, R>(
                                       value.map(fn));
        }

        @Override
        public LazyEither4<ST, M,M2, PT> peek(final Consumer<? super PT> action) {
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
        public <RT1> LazyEither4<ST, M, M2, RT1> flatMap(
                final Function<? super PT, ? extends LazyEither4<ST,M,M2,? extends RT1>> mapper) {
            Eval<? extends LazyEither4<? extends ST, ? extends M, ? extends M2, ? extends RT1>> et = value.map(mapper);


           final Eval<LazyEither4<ST, M, M2, RT1>> e3 =  (Eval<LazyEither4<ST, M, M2, RT1>>)et;
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
            return "Either4.right[" + value.get() + "]";
        }

        @Override
        public <R> R visit(final Function<? super ST, ? extends R> secondary,
                final Function<? super M, ? extends R> mid,
                final Function<? super M2, ? extends R> mid2,final Function<? super PT, ? extends R> primary) {
            return primary.apply(value.get());
        }



        @Override
        public <R1, R2> LazyEither4<ST,M, R1, R2> bimap(final Function<? super M2, ? extends R1> fn1,
                                                        final Function<? super PT, ? extends R2> fn2) {
            return (LazyEither4<ST, M,R1, R2>) this.map(fn2);
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
        public <T> LazyEither4<ST, M, M2, T> unit(final T unit) {
            return LazyEither4.right(unit);
        }
        @Override
        public LazyEither4<ST,  M, PT, M2> swap3() {

            return  new Left3<>(value);
        }


        @Override
        public LazyEither4<ST, PT, M2, M> swap2() {

            return  new Left2<>(value);
        }


        @Override
        public LazyEither4<PT, M,M2, ST> swap1() {

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
    static class Left1<ST, M, M2,PT> implements LazyEither4<ST, M,M2, PT> {
        private final Eval<ST> value;

        @Override
        public <R> LazyEither4<ST, M, M2, R> map(final Function<? super PT, ? extends R> fn) {
            return (LazyEither4<ST, M, M2,R>) this;
        }

        @Override
        public LazyEither4<ST, M, M2, PT> peek(final Consumer<? super PT> action) {
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
        public <RT1> LazyEither4<ST, M, M2, RT1> flatMap(
                final Function<? super PT, ? extends LazyEither4<ST,M,M2,? extends RT1>> mapper) {

            return (LazyEither4) this;

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
        public String toString() {
            return mkString();
        }

        @Override
        public String mkString() {
            return "Either4.left1[" + value.get() + "]";
        }

        @Override
        public <R> R visit(final Function<? super ST, ? extends R> secondary,
                final Function<? super M, ? extends R> mid,
                final Function<? super M2, ? extends R> mid2,
                final Function<? super PT, ? extends R> primary) {
            return secondary.apply(value.get());
        }


        @Override
        public <R1, R2> LazyEither4<ST, M,R1, R2> bimap(final Function<? super M2, ? extends R1> fn1,
                                                        final Function<? super PT, ? extends R2> fn2) {
            return (LazyEither4<ST,M, R1, R2>) this;
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
        public <T> LazyEither4<ST, M,M2, T> unit(final T unit) {
            return LazyEither4.right(unit);
        }
        @Override
        public LazyEither4<ST, M,PT, M2> swap3() {

            return (LazyEither4<ST, M,PT, M2>) this;
        }

        @Override
        public LazyEither4<ST, PT,M2, M> swap2() {

            return (LazyEither4<ST, PT,M2, M>) this;
        }

        @Override
        public LazyEither4<PT, M,M2, ST> swap1() {

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
    static class Left2<ST, M,M2, PT> implements LazyEither4<ST, M, M2, PT> {
        private final Eval<M> value;

        @Override
        public <R> LazyEither4<ST, M, M2,R> map(final Function<? super PT, ? extends R> fn) {
            return (LazyEither4<ST, M, M2,R>) this;
        }

        @Override
        public LazyEither4<ST, M, M2, PT> peek(final Consumer<? super PT> action) {
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
        public void subscribe(final Subscriber<? super PT> s) {
            s.onComplete();
        }
        @Override
        public <RT1> LazyEither4<ST, M, M2,RT1> flatMap(
                final Function<? super PT, ? extends LazyEither4<ST,M,M2,? extends RT1>> mapper) {

            return (LazyEither4) this;

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
            return "Either4.left2[" + value.get() + "]";
        }

        @Override
        public <R> R visit(final Function<? super ST, ? extends R> secondary,
                final Function<? super M, ? extends R> mid1,
                final Function<? super M2, ? extends R> mid2,
                final Function<? super PT, ? extends R> primary) {
            return mid1.apply(value.get());
        }

        @Override
        public <R1, R2> LazyEither4<ST, M, R1, R2> bimap(final Function<? super M2, ? extends R1> fn1,
                                                         final Function<? super PT, ? extends R2> fn2) {
            return (LazyEither4<ST, M,R1, R2>) this;
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
        public <T> LazyEither4<ST, M,M2, T> unit(final T unit) {
            return LazyEither4.right(unit);
        }
        @Override
        public LazyEither4<ST, M, PT,M2> swap3() {
            return (LazyEither4<ST, M, PT,M2>) this;

        }
        @Override
        public LazyEither4<ST, PT,M2, M> swap2() {
            return new Right<>(
                               value);

        }

        @Override
        public LazyEither4<PT, M, M2,ST> swap1() {
            return (LazyEither4<PT, M,M2, ST>) this;

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
    static class Left3<ST, M,M2, PT> implements LazyEither4<ST, M, M2, PT> {
        private final Eval<M2> value;

        @Override
        public <R> LazyEither4<ST, M, M2,R> map(final Function<? super PT, ? extends R> fn) {
            return (LazyEither4<ST, M, M2,R>) this;
        }

        @Override
        public LazyEither4<ST, M, M2, PT> peek(final Consumer<? super PT> action) {
            return this;

        }

        @Override
        public Maybe<PT> filter(final Predicate<? super PT> test) {

            return Maybe.nothing();

        }
        @Override
        public void subscribe(final Subscriber<? super PT> s) {
            s.onComplete();
        }

        @Override
        public Option<PT> get() {
           return Option.none();
        }

        @Override
        public <RT1> LazyEither4<ST, M, M2,RT1> flatMap(
                final Function<? super PT, ? extends LazyEither4<ST,M,M2,? extends RT1>> mapper) {

            return (LazyEither4) this;

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
        public String toString() {
            return mkString();
        }

        @Override
        public String mkString() {
            return "Either4.left3[" + value.get() + "]";
        }

        @Override
        public <R> R visit(final Function<? super ST, ? extends R> secondary,
                final Function<? super M, ? extends R> mid1,
                final Function<? super M2, ? extends R> mid2,
                final Function<? super PT, ? extends R> primary) {
            return mid2.apply(value.get());
        }


        @Override
        public <R1, R2> LazyEither4<ST, M, R1, R2> bimap(final Function<? super M2, ? extends R1> fn1,
                                                         final Function<? super PT, ? extends R2> fn2) {
            return (LazyEither4<ST, M,R1, R2>) this;
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
        public <T> LazyEither4<ST, M,M2, T> unit(final T unit) {
            return LazyEither4.right(unit);
        }
        @Override
        public LazyEither4<ST, M, PT,M2> swap3() {
            return new Right<>(
                    value);

        }
        @Override
        public LazyEither4<ST, PT,M2, M> swap2() {
           return (LazyEither4<ST, PT,M2, M>)this;

        }

        @Override
        public LazyEither4<PT, M, M2,ST> swap1() {
            return (LazyEither4<PT, M,M2, ST>) this;

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


    public static <L1,L2,L3,T> LazyEither4<L1,L2,L3,T> narrowK3(final Higher4<lazyEither4, L1,L2,L3,T> xor) {
        return (LazyEither4<L1,L2,L3,T>)xor;
    }
    public static <L1,L2,L3,T> LazyEither4<L1,L2,L3,T> narrowK(final Higher<Higher<Higher<Higher<lazyEither4, L1>,L2>,L3>,T> xor) {
        return (LazyEither4<L1,L2,L3,T>)xor;
    }
    default Active<Higher<Higher<Higher<lazyEither4, LT1>, LT2>,LT3>,RT> allTypeclasses(){
        return Active.of(this,Instances.definitions());
    }
    default <W2,R> Nested<Higher<Higher<Higher<lazyEither4, LT1>, LT2>,LT3>,W2,R> mapM(Function<? super RT,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
        return Nested.of(map(fn), Instances.definitions(), defs);
    }
    public static class Instances {

        public static <L1,L2,L3> InstanceDefinitions<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>> definitions() {
            return new InstanceDefinitions<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>>() {


                @Override
                public <T, R> Functor<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>> functor() {
                    return Instances.functor();
                }

                @Override
                public <T> Pure<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>> unit() {
                    return Instances.unit();
                }

                @Override
                public <T, R> Applicative<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>> applicative() {
                    return Instances.applicative();
                }

                @Override
                public <T, R> Monad<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>> monad() {
                    return Instances.monad();
                }

                @Override
                public <T, R> Option<MonadZero<Higher<Higher<Higher<lazyEither4, L1>, L2>, L3>>> monadZero() {
                    return Option.some(Instances.monadZero());
                }

                @Override
                public <T> Option<MonadPlus<Higher<Higher<Higher<lazyEither4, L1>, L2>, L3>>> monadPlus() {
                    return Maybe.nothing();
                }

                @Override
                public <T> MonadRec<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>> monadRec() {
                    return Instances.monadRec();
                }

                @Override
                public <T> Option<MonadPlus<Higher<Higher<Higher<lazyEither4, L1>, L2>, L3>>> monadPlus(MonoidK<Higher<Higher<Higher<lazyEither4, L1>, L2>, L3>> m) {
                    return Maybe.nothing();
                }

                @Override
                public <C2, T> Traverse<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>> traverse() {
                    return Instances.traverse();
                }

                @Override
                public <T> Foldable<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>> foldable() {
                    return Instances.foldable();
                }

                @Override
                public <T> Option<Comonad<Higher<Higher<Higher<lazyEither4, L1>, L2>, L3>>> comonad() {
                    return Maybe.nothing();
                }

                @Override
                public <T> Option<Unfoldable<Higher<Higher<Higher<lazyEither4, L1>, L2>, L3>>> unfoldable() {
                    return Maybe.nothing();
                }
            };

        }
        public static <L1,L2,L3> Functor<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>> functor() {
            return new Functor<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>>() {

                @Override
                public <T, R> Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, T> ds) {
                    return narrowK(ds).map(fn);
                }
            };
        }
        public static <L1,L2,L3> Pure<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>> unit() {
            return new Pure<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>>() {

                @Override
                public <T> Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, T> unit(T value) {
                    return LazyEither4.right(value);
                }
            };
        }
        public static <L1,L2,L3> Applicative<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>> applicative() {
            return new Applicative<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>>() {


                @Override
                public <T, R> Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, R> ap(Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, ? extends Function<T, R>> fn, Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, T> apply) {
                    return  narrowK(fn).flatMap(x -> narrowK(apply).map(x));

                }

                @Override
                public <T, R> Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, T> ds) {
                    return Instances.<L1,L2,L3>functor().map(fn,ds);
                }

                @Override
                public <T> Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, T> unit(T value) {
                    return Instances.<L1,L2,L3>unit().unit(value);
                }
            };
        }
        public static <L1,L2,L3> Monad<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>> monad() {
            return new Monad<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>>() {


                @Override
                public <T, R> Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, R> ap(Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, ? extends Function<T, R>> fn, Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, T> apply) {
                    return Instances.<L1,L2,L3>applicative().ap(fn,apply);
                }

                @Override
                public <T, R> Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, T> ds) {
                    return Instances.<L1,L2,L3>functor().map(fn,ds);
                }

                @Override
                public <T> Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, T> unit(T value) {
                    return Instances.<L1,L2,L3>unit().unit(value);
                }

                @Override
                public <T, R> Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, R> flatMap(Function<? super T, ? extends Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, R>> fn, Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, T> ds) {
                    return narrowK(ds).flatMap(fn.andThen(m->narrowK(m)));
                }
            };
        }
        public static <L1,L2,L3,T,R> MonadRec<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>> monadRec(){

            return new MonadRec<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>>(){

                @Override
                public <T, R> Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, R> tailRec(T initial, Function<? super T, ? extends Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, ? extends Either<T, R>>> fn) {
                    return narrowK(fn.apply(initial)).flatMap( eval ->
                            eval.visit(s->narrowK(tailRec(s,fn)),p-> LazyEither4.right(p)));
                }


            };
        }
        public static <L1,L2,L3> MonadZero<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>> monadZero() {
            return new MonadZero<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>>() {


                @Override
                public <T, R> Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, R> ap(Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, ? extends Function<T, R>> fn, Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, T> apply) {
                    return Instances.<L1,L2,L3>applicative().ap(fn,apply);
                }

                @Override
                public <T, R> Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, T> ds) {
                    return Instances.<L1,L2,L3>functor().map(fn,ds);
                }

                @Override
                public Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, ?> zero() {
                    return LazyEither4.left1(null);
                }

                @Override
                public <T> Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, T> unit(T value) {
                    return Instances.<L1,L2,L3>unit().unit(value);
                }

                @Override
                public <T, R> Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, R> flatMap(Function<? super T, ? extends Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, R>> fn, Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, T> ds) {
                    return Instances.<L1,L2,L3>monad().flatMap(fn,ds);
                }
            };
        }
        public static  <L1,L2,L3> Traverse<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>> traverse() {
            return new Traverse<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>> () {


                @Override
                public <T, R> Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, R> ap(Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, ? extends Function<T, R>> fn, Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, T> apply) {
                    return Instances.<L1,L2,L3>applicative().ap(fn,apply);
                }

                @Override
                public <T, R> Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, T> ds) {
                    return Instances.<L1,L2,L3>functor().map(fn,ds);
                }

                @Override
                public <T> Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, T> unit(T value) {
                    return Instances.<L1, L2,L3>unit().unit(value);
                }

                @Override
                public <C2, T, R> Higher<C2, Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn, Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, T> ds) {
                    LazyEither4<L1,L2,L3,T> maybe = narrowK(ds);
                    return maybe.visit(left->  applicative.unit(LazyEither4.<L1,L2,L3,R>left1(left)),
                            middle1->applicative.unit(LazyEither4.<L1,L2,L3,R>left2(middle1)),
                            middle2->applicative.unit(LazyEither4.<L1,L2,L3,R>left3(middle2)),
                            right->applicative.map(m-> LazyEither4.right(m), fn.apply(right)));
                }

                @Override
                public <C2, T> Higher<C2, Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, T>> sequenceA(Applicative<C2> applicative, Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, Higher<C2, T>> ds) {
                    return traverseA(applicative,Function.identity(),ds);
                }


            };
        }
        public static <L1,L2,L3> Foldable<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>> foldable() {
            return new Foldable<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>>() {


                @Override
                public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>, L3>, T> nestedA) {
                    return foldLeft(mb,narrowK(nestedA).<R>map(fn));
                }

                @Override
                public <T> T foldRight(Monoid<T> monoid, Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, T> ds) {
                    return narrowK(ds).fold(monoid);
                }

                @Override
                public <T> T foldLeft(Monoid<T> monoid, Higher<Higher<Higher<Higher<lazyEither4, L1>, L2>,L3>, T> ds) {
                    return narrowK(ds).fold(monoid);
                }

            };
        }


    }

}
