package cyclops.control;

import com.oath.cyclops.data.collections.extensions.CollectionX;
import com.oath.cyclops.types.Filters;
import com.oath.cyclops.types.OrElseValue;
import com.oath.cyclops.types.Value;
import com.oath.cyclops.types.factory.Unit;
import com.oath.cyclops.types.foldable.To;
import com.oath.cyclops.types.functor.BiTransformable;
import com.oath.cyclops.types.functor.Transformable;
import com.oath.cyclops.types.reactive.Completable;
import cyclops.async.Future;
import cyclops.collections.mutable.ListX;
import cyclops.function.*;

import cyclops.monads.DataWitness;
import cyclops.reactive.ReactiveSeq;
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
 * @param <LT4> Fourth Type
 * @param <RT> Right type (operations are performed on this type if present)
 */
public interface LazyEither5<LT1, LT2,LT3, LT4,RT> extends Transformable<RT>,
  Filters<RT>,
                                                        BiTransformable<LT4, RT>,
                                                        To<LazyEither5<LT1, LT2,LT3, LT4,RT>>,
  OrElseValue<RT,LazyEither5<LT1,LT2,LT3,LT4,RT>>,
                                                        Unit<RT>,
  Value<RT> {


    Option<RT> get();
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
    static <LT2,LT3,LT4,RT> LazyEither5.CompletableEither5<RT,LT2,LT3,LT4,RT> either5(){
        Completable.CompletablePublisher<RT> c = new Completable.CompletablePublisher<RT>();
        return new CompletableEither5<RT,LT2,LT3,LT4, RT>(c,fromFuture(Future.fromPublisher(c)));
    }
    @AllArgsConstructor
    static class CompletableEither5<ORG,LT1,LT2,LT3,RT> implements LazyEither5<Throwable,LT1,LT2,LT3,RT>, Completable<ORG> {

        public final Completable.CompletablePublisher<ORG> complete;
        public final LazyEither5<Throwable, LT1, LT2, LT3, RT> either;

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
        public <R> R visit(Function<? super Throwable, ? extends R> left1, Function<? super LT1, ? extends R> left2, Function<? super LT2, ? extends R> left3, Function<? super LT3, ? extends R> left4, Function<? super RT, ? extends R> right) {
            return either.visit(left1,left2,left3,left4,right);
        }

        @Override
        public Maybe<RT> filter(Predicate<? super RT> test) {
            return either.filter(test);
        }

        @Override
        public <RT1> LazyEither5<Throwable, LT1, LT2, LT3, RT1> flatMap(Function<? super RT, ? extends LazyEither5<Throwable, LT1, LT2, LT3, ? extends RT1>> mapper) {
            return either.flatMap(mapper);
        }

        @Override
        public LazyEither5<Throwable, LT1, LT2, RT, LT3> swap4() {
            return either.swap4();
        }

        @Override
        public LazyEither5<Throwable, LT1, RT, LT3, LT2> swap3() {
            return either.swap3();
        }

        @Override
        public LazyEither5<Throwable, RT, LT2, LT3, LT1> swap2() {
            return either.swap2();
        }

        @Override
        public LazyEither5<RT, LT1, LT2, LT3, Throwable> swap1() {
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
        public <R1, R2> LazyEither5<Throwable, LT1, LT2, R1, R2> bimap(Function<? super LT3, ? extends R1> fn1, Function<? super RT, ? extends R2> fn2) {
            return either.bimap(fn1,fn2);
        }

        @Override
        public <R> LazyEither5<Throwable, LT1, LT2, LT3, R> map(Function<? super RT, ? extends R> fn) {
            return either.map(fn);
        }

        @Override
        public <T> LazyEither5<Throwable, LT1, LT2, LT3, T> unit(T unit) {
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
     *   myEither.to(Either5::consumeAny)
                 .accept(System.out::println);
     * }
     * </pre>
     *
     * @param either Either to consume value for
     * @return Consumer we can applyHKT to consume value
     */
    static <X, LT1 extends X, LT2 extends X, LT3 extends X, LT4 extends X, RT extends X> Consumer<Consumer<? super X>> consumeAny(
            LazyEither5<LT1, LT2, LT3, LT4, RT> either) {
        return in -> visitAny(in, either);
    }

    static <X, LT1 extends X, LT2 extends X, LT3 extends X, LT4 extends X, RT extends X, R> Function<Function<? super X, R>, R> applyAny(
            LazyEither5<LT1, LT2, LT3, LT4, RT> either) {
        return in -> visitAny(either, in);
    }

    static <X, LT1 extends X, LT2 extends X, LT3 extends X, LT4 extends X, RT extends X, R> R visitAny(
            LazyEither5<LT1, LT2, LT3, LT4, RT> either, Function<? super X, ? extends R> fn) {
        return either.visit(fn, fn, fn, fn, fn);
    }

    static <X, LT1 extends X, LT2 extends X, LT3 extends X, LT4 extends X, RT extends X> X visitAny(
            Consumer<? super X> c, LazyEither5<LT1, LT2, LT3, LT4, RT> either) {
        Function<? super X, X> fn = x -> {
            c.accept(x);
            return x;
        };
        return visitAny(either, fn);
    }


    static <LT2,LT3,LT4,RT> LazyEither5<Throwable,LT2,LT3,LT4,RT> either4(){
        return LazyEither5.<LT2,LT3,LT4,RT>fromFuture(Future.<RT>future());
    }

    static <LT1,LT2,LT3,LT4,RT> LazyEither5<LT1,LT2,LT3,LT4,RT> fromLazy(Eval<LazyEither5<LT1,LT2,LT3,LT4,RT>> lazy){
        return new LazyEither5.Lazy<>(lazy);
    }

    static <LT2,LT3,LT4,RT> LazyEither5<Throwable,LT2,LT3,LT4,RT> fromFuture(Future<RT> future){
        return fromLazy(Eval.<LazyEither5<Throwable,LT2,LT3,LT4,RT>>fromFuture(
                future.map(e->e!=null? LazyEither5.<Throwable,LT2,LT3,LT4,RT>right(e): LazyEither5.<Throwable,LT2,LT3,LT4,RT>left1(new NoSuchElementException()))
                        .recover(t-> LazyEither5.<Throwable,LT2,LT3,LT4,RT>left1(t.getCause()))));
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
    public static <LT1,LT2,LT3,LT4,PT> LazyEither5<ListX<LT1>,ListX<LT2>,ListX<LT3>,ListX<LT4>,ListX<PT>> sequence(final CollectionX<LazyEither5<LT1, LT2, LT3, LT4, PT>> xors) {
        Objects.requireNonNull(xors);
        return AnyM.sequence(xors.stream().filter(LazyEither5::isRight).map(AnyM::fromEither5).to().listX(), Witness.lazyEither5.INSTANCE)
                .to(Witness::lazyEither5);
    }
    /**
     * Traverse a Collection of Either3 producing an Either4 with a ListX, applying the transformation function to every
     * element in the list
     *
     * @param xors Either4s to sequence and transform
     * @param fn Transformation function
     * @return An Either4 with a transformed list
     */
    public static <LT1,LT2, LT3,LT4,PT,R> LazyEither5<ListX<LT1>,ListX<LT2>,ListX<LT3>,ListX<LT4>,ListX<R>> traverse(final CollectionX<LazyEither5<LT1, LT2, LT3, LT4, PT>> xors, Function<? super PT, ? extends R> fn) {
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
    public static <LT1,LT2,LT3,LT4, RT> LazyEither5<ListX<LT1>, ListX<LT2>,ListX<LT3>,ListX<LT4>, RT> accumulate(final Monoid<RT> reducer, final CollectionX<LazyEither5<LT1, LT2, LT3, LT4, RT>> xors) {
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
    public static <T1,T2,T3,T> LazyEither5<Throwable, T1, T2,T3, T> fromPublisher(final Publisher<T> pub) {
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
    public static <ST, T, T2,T3,RT> LazyEither5<ST, T,T2,T3,RT> fromIterable(final Iterable<RT> iterable) {

        final Iterator<RT> it = iterable.iterator();
        return it.hasNext() ? LazyEither5.right( it.next()) : LazyEither5.left1(null);
    }

    /**
     * Construct a Either4#Right from an Eval
     *
     * @param right Eval to construct Either4#Right from
     * @return Either4 right instance
     */
    public static <LT, M1,B, T4,RT> LazyEither5<LT, M1,B,T4, RT> rightEval(final Eval<RT> right) {
        return new Right<>(
                           right);
    }

    /**
     * Construct a Either4#Left1 from an Eval
     *
     * @param left Eval to construct Either4#Left1 from
     * @return Either4 Left1 instance
     */
    public static <LT, M1, B, T4,RT> LazyEither5<LT, M1, B,T4, RT> left1Eval(final Eval<LT> left) {
        return new Left1<>(
                          left);
    }

    /**
     * Construct a Either4#Right
     *
     * @param right Value to store
     * @return Either4 Right instance
     */
    public static <LT, M1, B, T4,RT> LazyEither5<LT, M1, B, T4,RT> right(final RT right) {
        return new Right<>(
                           Eval.later(()->right));
    }

    /**
     * Construct a Either4#Left1
     *
     * @param left Value to store
     * @return Left1 instance
     */
    public static <LT, M1, B, T4,RT> LazyEither5<LT, M1, B, T4,RT> left1(final LT left) {
        return new Left1<>(
                          Eval.now(left));
    }

    /**
     * Construct a Either4#Second
     *
     * @param middle Value to store
     * @return Second instance
     */
    public static <LT, M1, B, T4,RT> LazyEither5<LT, M1, B, T4,RT> left2(final M1 middle) {
        return new Left2<>(
                            Eval.now(middle));
    }
    /**
     * Construct a Either4#Third
     *
     * @param middle Value to store
     * @return Third instance
     */
    public static <LT, M1, B, T4, RT> LazyEither5<LT, M1, B,T4, RT> left3(final B middle) {
        return new Left3<>(
                            Eval.now(middle));
    }

    /**
     * Construct a Either4#Third
     *
     * @param middle Value to store
     * @return Third instance
     */
    public static <LT, M1, B, T4, RT> LazyEither5<LT, M1, B,T4, RT> left4(final T4 middle) {
        return new Left4<>(
                            Eval.now(middle));
    }
    /**
     * Construct a Either4#Second from an Eval
     *
     * @param middle Eval to construct Either4#middle from
     * @return Either4 second instance
     */
    public static <LT, M1, B, T4, RT> LazyEither5<LT, M1, B, T4, RT> left2Eval(final Eval<M1> middle) {
        return new Left2<>(
                            middle);
    }
    /**
     * Construct a Either4#Third from an Eval
     *
     * @param middle Eval to construct Either4#middle from
     * @return Either4 third instance
     */
    public static <LT, M1, B, T4, RT> LazyEither5<LT, M1, B, T4, RT> left3Eval(final Eval<B> middle) {
        return new Left3<>(
                            middle);
    }
    /**
     * Construct a Either4#Third from an Eval
     *
     * @param middle Eval to construct Either4#middle from
     * @return Either4 third instance
     */
    public static <LT, M1, B, T4, RT> LazyEither5<LT, M1, B, T4, RT> left4Eval(final Eval<T4> middle) {
        return new Left4<>(
                            middle);
    }
    /**
     * Construct a Either4#Third from an Eval
     *
     * @param middle Eval to construct Either4#middle from
     * @return Either4 third instance
     */
    public static <LT, M1, B, T4, RT> LazyEither5<LT, M1, B, T4, RT> foEval(final Eval<B> middle) {
        return new Left3<>(
                            middle);
    }
    default Trampoline<LazyEither5<LT1,LT2,LT3,LT4,RT>> toTrampoline() {
        return Trampoline.more(()->Trampoline.done(this));
    }

    @Override
    default <R> LazyEither5<LT1,LT2,LT3,LT4,R> retry(final Function<? super RT, ? extends R> fn) {
        return (LazyEither5<LT1,LT2,LT3,LT4,R>)Transformable.super.retry(fn);
    }

    @Override
    default <R> LazyEither5<LT1,LT2,LT3,LT4,R> retry(final Function<? super RT, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (LazyEither5<LT1,LT2,LT3,LT4,R>)Transformable.super.retry(fn,retries,delay,timeUnit);
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
    < RT1> LazyEither5<LT1, LT2,LT3, LT4,RT1> flatMap(
            Function<? super RT, ? extends LazyEither5<LT1, LT2,LT3, LT4,? extends RT1>> mapper);

    /**
     * @return Swap the fourth and the right types
     */
    LazyEither5<LT1,LT2, LT3,RT,LT4> swap4();
    /**
     * @return Swap the third and the right types
     */
    LazyEither5<LT1,LT2, RT,LT4, LT3> swap3();
    /**
     * @return Swap the second and the right types
     */
    LazyEither5<LT1, RT,LT3, LT4, LT2> swap2();

    /**
     * @return Swap the right and left types
     */
    LazyEither5<RT, LT2,LT3, LT4,LT1> swap1();

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
    <R1, R2> LazyEither5<LT1, LT2, LT3,R1, R2> bimap(Function<? super LT4, ? extends R1> fn1,
                                                     Function<? super RT, ? extends R2> fn2);

    /*
     * (non-Javadoc)
     *
     * @see com.oath.cyclops.types.functor.Transformable#transform(java.util.function.Function)
     */
    @Override
    <R> LazyEither5<LT1,LT2,LT3, LT4, R> map(Function<? super RT, ? extends R> fn);

    /**
     * Return an Ior that can be this object or a Ior.right or Ior.left
     * @return new Ior
     */
     default Ior<LT1, RT> toIor() {
        return this.visit(l->Ior.left(l),
                          m->Ior.left(null),
                          m->Ior.left(null),
                          m->Ior.left(null),
                          r->Ior.right(r));
    }
     default Either<LT1, RT> toXor() {
         return this.visit(l-> Either.left(l),
                           m-> Either.left(null),
                           m-> Either.left(null),
                           m-> Either.left(null),
                           r-> Either.right(r));
     }




    default <R> LazyEither5<LT1,LT2,LT3,LT4,R> coflatMap(Function<? super LazyEither5<LT1,LT2,LT3,LT4,RT>, R> mapper) {

        return mapper.andThen(r -> unit(r))
                .apply(this);
    }

    default LazyEither5<LT1,LT2,LT3,LT4,LazyEither5<LT1,LT2,LT3,LT4,RT>> nest() {

        return this.map(t -> unit(t));
    }

    default <T2, R1, R2, R3, R> LazyEither5<LT1,LT2,LT3,LT4,R> forEach4(Function<? super RT, ? extends LazyEither5<LT1,LT2,LT3,LT4,R1>> value1,
                                                                        BiFunction<? super RT, ? super R1, ? extends LazyEither5<LT1,LT2,LT3,LT4,R2>> value2,
                                                                        Function3<? super RT, ? super R1, ? super R2, ? extends LazyEither5<LT1,LT2,LT3,LT4,R3>> value3,
                                                                        Function4<? super RT, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return this.flatMap(in-> {

            LazyEither5<LT1,LT2,LT3,LT4,R1> a = value1.apply(in);
            return a.flatMap(ina-> {
                LazyEither5<LT1,LT2,LT3,LT4,R2> b = value2.apply(in,ina);
                return b.flatMap(inb-> {
                    LazyEither5<LT1,LT2,LT3,LT4,R3> c= value3.apply(in,ina,inb);
                    return c.map(in2->yieldingFunction.apply(in,ina,inb,in2));
                });

            });

        });
    }


    default <T2, R1, R2, R> LazyEither5<LT1,LT2,LT3,LT4,R> forEach3(Function<? super RT, ? extends LazyEither5<LT1,LT2,LT3,LT4,R1>> value1,
                                                                    BiFunction<? super RT, ? super R1, ? extends LazyEither5<LT1,LT2,LT3,LT4,R2>> value2,
                                                                    Function3<? super RT, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return this.flatMap(in-> {

            LazyEither5<LT1,LT2,LT3,LT4,R1> a = value1.apply(in);
            return a.flatMap(ina-> {
                LazyEither5<LT1,LT2,LT3,LT4,R2> b = value2.apply(in,ina);
                return b.map(in2->yieldingFunction.apply(in,ina, in2));
            });

        });
    }


    default <R1, R> LazyEither5<LT1,LT2,LT3,LT4,R> forEach2(Function<? super RT, ? extends LazyEither5<LT1,LT2,LT3,LT4,R1>> value1,
                                                            BiFunction<? super RT, ? super R1, ? extends R> yieldingFunction) {

        return this.flatMap(in-> {
            LazyEither5<LT1,LT2,LT3,LT4,R1> b = value1.apply(in);
            return b.map(in2->yieldingFunction.apply(in, in2));
        });
    }


    default <T2, R> LazyEither5<LT1, LT2,LT3,LT4,R> zip(final LazyEither5<LT1, LT2,LT3,LT4,? extends T2> app, final BiFunction<? super RT, ? super T2, ? extends R> fn){
        return flatMap(t->app.map(t2->fn.apply(t,t2)));
    }

    /*
     * (non-Javadoc)
     *
     * @see com.oath.cyclops.types.Pure#unit(java.lang.Object)
     */
    @Override
    <T> LazyEither5<LT1, LT2,LT3,LT4, T> unit(T unit);


    /*
     * (non-Javadoc)
     *
     * @see com.oath.cyclops.types.functor.BiTransformable#bipeek(java.util.function.Consumer,
     * java.util.function.Consumer)
     */
    @Override
    default LazyEither5<LT1, LT2, LT3, LT4, RT> bipeek(final Consumer<? super LT4> c1, final Consumer<? super RT> c2) {

        return (LazyEither5<LT1, LT2, LT3,LT4, RT>) BiTransformable.super.bipeek(c1, c2);
    }


    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.types.functor.BiTransformable#bitrampoline(java.util.function.Function,
     * java.util.function.Function)
     */
    @Override
    default <R1, R2> LazyEither5<LT1, LT2, LT3, R1, R2> bitrampoline(
            final Function<? super LT4, ? extends Trampoline<? extends R1>> mapper1,
            final Function<? super RT, ? extends Trampoline<? extends R2>> mapper2) {

        return (LazyEither5<LT1,LT2,LT3, R1, R2>) BiTransformable.super.bitrampoline(mapper1, mapper2);
    }


    /*
     * (non-Javadoc)
     *
     * @see com.oath.cyclops.types.functor.Transformable#peek(java.util.function.Consumer)
     */
    @Override
    default LazyEither5<LT1, LT2, LT3,LT4, RT> peek(final Consumer<? super RT> c) {

        return (LazyEither5<LT1, LT2, LT3, LT4, RT>) Transformable.super.peek(c);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.types.functor.Transformable#trampoline(java.util.function.Function)
     */
    @Override
    default <R> LazyEither5<LT1, LT2, LT3,LT4, R> trampoline(final Function<? super RT, ? extends Trampoline<? extends R>> mapper) {

        return (LazyEither5<LT1, LT2, LT3,LT4, R>) Transformable.super.trampoline(mapper);
    }



    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    final static class Lazy<ST, M,M2,M3, PT> implements LazyEither5<ST, M,M2,M3, PT> {

        private final Eval<LazyEither5<ST, M,M2,M3, PT>> lazy;

        public LazyEither5<ST, M,M2,M3, PT> resolve() {
            return lazy.get()
                       .visit(LazyEither5::left1, LazyEither5::left2, LazyEither5::left3, LazyEither5::left4, LazyEither5::right);
        }

        private static <ST, M,M2,M3, PT> Lazy<ST, M,M2, M3,PT> lazy(final Eval<LazyEither5<ST, M,M2,M3, PT>> lazy) {
            return new Lazy<>(lazy);
        }


        @Override
        public <R> LazyEither5<ST, M,M2,M3, R> map(final Function<? super PT, ? extends R> mapper) {
            return flatMap(t -> LazyEither5.right(mapper.apply(t)));
        }

        @Override
        public <RT1> LazyEither5<ST, M,M2,M3, RT1> flatMap(
                final Function<? super PT, ? extends LazyEither5<ST, M,M2,M3, ? extends RT1>> mapper) {
            return LazyEither5.fromLazy(lazy.map(m->m.flatMap(mapper)));
        }

        @Override
        public Maybe<PT> filter(final Predicate<? super PT> test) {

            return Maybe.fromEval(Eval.later(() -> resolve().filter(test)))
                        .flatMap(Function.identity());

        }

        @Override
        public Trampoline<LazyEither5<ST,M,M2,M3,PT>> toTrampoline() {
            Trampoline<LazyEither5<ST,M,M2,M3,PT>> trampoline = lazy.toTrampoline();
            return new Trampoline<LazyEither5<ST,M,M2,M3,PT>>() {
                @Override
                public LazyEither5<ST,M,M2,M3,PT> get() {
                    LazyEither5<ST,M,M2,M3,PT> either = lazy.get();
                    while (either instanceof LazyEither5.Lazy) {
                        either = ((LazyEither5.Lazy<ST,M,M2,M3,PT>) either).lazy.get();
                    }
                    return either;
                }
                @Override
                public boolean complete(){
                    return false;
                }
                @Override
                public Trampoline<LazyEither5<ST,M,M2,M3,PT>> bounce() {
                    LazyEither5<ST,M,M2,M3,PT> either = lazy.get();
                    if(either instanceof LazyEither5.Lazy){
                        return either.toTrampoline();
                    }
                    return Trampoline.done(either);

                }
            };
        }

        public Option<PT> get() {
            return trampoline().get();
        }

        private LazyEither5<ST,M,M2,M3,PT> trampoline(){
            LazyEither5<ST,M,M2,M3,PT> maybe = lazy.get();
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
        public <R> R visit(final Function<? super ST, ? extends R> first,
                final Function<? super M, ? extends R> second,
                final Function<? super M2, ? extends R> third,
                final Function<? super M3, ? extends R> fourth,
                final Function<? super PT, ? extends R> primary) {

            return trampoline()
                       .visit(first, second,third,fourth, primary);
        }
        @Override
        public LazyEither5<ST, M, M2,PT, M3> swap4() {
            return lazy(Eval.later(() -> resolve().swap4()));
        }
        @Override
        public LazyEither5<ST, M, PT, M3, M2> swap3() {
            return lazy(Eval.later(() -> resolve().swap3()));
        }
        @Override
        public LazyEither5<ST, PT, M2, M3, M> swap2() {
            return lazy(Eval.later(() -> resolve().swap2()));
        }

        @Override
        public LazyEither5<PT, M,M2, M3,ST> swap1() {
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
        public <R1, R2> LazyEither5<ST, M,M2,R1, R2> bimap(final Function<? super M3, ? extends R1> fn1,
                                                           final Function<? super PT, ? extends R2> fn2) {
            return lazy(Eval.later(() -> resolve().bimap(fn1, fn2)));
        }

        @Override
        public <T> LazyEither5<ST, M, M2,M3,T> unit(final T unit) {

            return LazyEither5.right(unit);
        }
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return this.visit(LazyEither5::left1, LazyEither5::left2, LazyEither5::left3, LazyEither5::left4, LazyEither5::right).hashCode();
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            return this.visit(LazyEither5::left1, LazyEither5::left2, LazyEither5::left3, LazyEither5::left4, LazyEither5::right).equals(obj);
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
    static class Right<ST, M,M2,M3, PT> implements LazyEither5<ST, M,M2,M3, PT> {
        private final Eval<PT> value;

        @Override
        public <R> LazyEither5<ST, M, M2, M3, R> map(final Function<? super PT, ? extends R> fn) {
            return new Right<ST, M, M2,M3, R>(
                                       value.map(fn));
        }

        @Override
        public LazyEither5<ST, M,M2,M3, PT> peek(final Consumer<? super PT> action) {
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
        public <RT1> LazyEither5<ST, M, M2, M3, RT1> flatMap(
                final Function<? super PT, ? extends LazyEither5<ST, M,M2,M3, ? extends RT1>> mapper) {
            Eval<? extends LazyEither5<? extends ST, ? extends M, ? extends M2,? extends M3, ? extends RT1>> et = value.map(mapper);


           final Eval<LazyEither5<ST, M, M2,M3, RT1>> e3 =  (Eval<LazyEither5<ST, M, M2,M3, RT1>>)et;
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



        @Override
        public <R1, R2> LazyEither5<ST,M, M2,R1, R2> bimap(final Function<? super M3, ? extends R1> fn1,
                                                           final Function<? super PT, ? extends R2> fn2) {
            return (LazyEither5<ST, M,M2,R1, R2>) this.map(fn2);
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
        public <T> LazyEither5<ST, M, M2, M3, T> unit(final T unit) {
            return LazyEither5.right(unit);
        }
        @Override
        public LazyEither5<ST,  M, M2,PT,M3> swap4() {

            return  new Left4<>(value);
        }
        @Override
        public LazyEither5<ST,  M, PT,M3, M2> swap3() {

            return  new Left3<>(value);
        }


        @Override
        public LazyEither5<ST, PT, M2, M3, M> swap2() {

            return  new Left2<>(value);
        }


        @Override
        public LazyEither5<PT, M,M2,M3, ST> swap1() {

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
    static class Left1<ST, M, M2, M3,PT> implements LazyEither5<ST, M,M2, M3, PT> {
        private final Eval<ST> value;

        @Override
        public <R> LazyEither5<ST, M, M2,M3, R> map(final Function<? super PT, ? extends R> fn) {
            return (LazyEither5<ST, M, M2, M3,R>) this;
        }

        @Override
        public LazyEither5<ST, M, M2,M3, PT> peek(final Consumer<? super PT> action) {
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
        public <RT1> LazyEither5<ST, M, M2, M3,RT1> flatMap(
                final Function<? super PT, ? extends LazyEither5<ST, M,M2,M3, ? extends RT1>> mapper) {

            return (LazyEither5) this;

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


        @Override
        public <R1, R2> LazyEither5<ST, M,M2,R1, R2> bimap(final Function<? super M3, ? extends R1> fn1,
                                                           final Function<? super PT, ? extends R2> fn2) {
            return (LazyEither5<ST,M, M2,R1, R2>) this;
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
            s.onComplete();
        }


        @Override
        public <T> LazyEither5<ST, M,M2,M3, T> unit(final T unit) {
            return LazyEither5.right(unit);
        }
        @Override
        public LazyEither5<ST, M,M2,PT,M3> swap4() {

            return (LazyEither5<ST, M,M2,PT,M3>) this;
        }
        @Override
        public LazyEither5<ST, M,PT,M3, M2> swap3() {

            return (LazyEither5<ST, M,PT,M3, M2>) this;
        }

        @Override
        public LazyEither5<ST, PT,M2,M3, M> swap2() {

            return (LazyEither5<ST, PT,M2,M3, M>) this;
        }

        @Override
        public LazyEither5<PT, M,M2,M3, ST> swap1() {

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
    static class Left2<ST, M,M2, M3, PT> implements LazyEither5<ST, M, M2, M3, PT> {
        private final Eval<M> value;

        @Override
        public <R> LazyEither5<ST, M, M2, M3,R> map(final Function<? super PT, ? extends R> fn) {
            return (LazyEither5<ST, M, M2,M3,R>) this;
        }

        @Override
        public LazyEither5<ST, M, M2,M3, PT> peek(final Consumer<? super PT> action) {
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
        public <RT1> LazyEither5<ST, M, M2, M3, RT1> flatMap(
                final Function<? super PT, ? extends LazyEither5<ST, M,M2,M3, ? extends RT1>> mapper) {

            return (LazyEither5) this;

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


        @Override
        public <R1, R2> LazyEither5<ST, M, M2,R1, R2> bimap(final Function<? super M3, ? extends R1> fn1,
                                                            final Function<? super PT, ? extends R2> fn2) {
            return (LazyEither5<ST, M,M2,R1, R2>) this;
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
            s.onComplete();
        }


        @Override
        public <T> LazyEither5<ST, M,M2,M3, T> unit(final T unit) {
            return LazyEither5.right(unit);
        }
        @Override
        public LazyEither5<ST, M, M2,PT,M3> swap4() {
            return (LazyEither5<ST, M, M2,PT,M3>) this;

        }
        @Override
        public LazyEither5<ST, M, PT,M3,M2> swap3() {
            return (LazyEither5<ST, M, PT,M3,M2>) this;

        }
        @Override
        public LazyEither5<ST, PT,M2, M3, M> swap2() {
            return new Right<>(
                               value);

        }

        @Override
        public LazyEither5<PT, M, M2,M3,ST> swap1() {
            return (LazyEither5<PT, M,M2, M3, ST>) this;

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
    static class Left3<ST, M,M2, M3, PT> implements LazyEither5<ST, M, M2, M3,PT> {
        private final Eval<M2> value;

        @Override
        public <R> LazyEither5<ST, M, M2, M3,R> map(final Function<? super PT, ? extends R> fn) {
            return (LazyEither5<ST, M, M2,M3,R>) this;
        }

        @Override
        public LazyEither5<ST, M, M2,M3, PT> peek(final Consumer<? super PT> action) {
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
        public <RT1> LazyEither5<ST, M, M2, M3,RT1> flatMap(
                final Function<? super PT, ? extends LazyEither5<ST, M,M2,M3,
                                                                ? extends RT1>> mapper) {

            return (LazyEither5) this;

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


        @Override
        public <R1, R2> LazyEither5<ST, M, M2,R1, R2> bimap(final Function<? super M3, ? extends R1> fn1,
                                                            final Function<? super PT, ? extends R2> fn2) {
            return (LazyEither5<ST, M,M2,R1, R2>) this;
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
            s.onComplete();
        }


        @Override
        public <T> LazyEither5<ST, M,M2,M3, T> unit(final T unit) {
            return LazyEither5.right(unit);
        }
        @Override
        public LazyEither5<ST, M,M2,PT,M3> swap4() {
           return (LazyEither5<ST, M,M2,PT,M3>)this;

        }
        @Override
        public LazyEither5<ST, M, PT,M3, M2> swap3() {
            return new Right<>(
                    value);

        }
        @Override
        public LazyEither5<ST, PT,M2, M3, M> swap2() {
           return (LazyEither5<ST, PT,M2, M3, M>)this;

        }

        @Override
        public LazyEither5<PT, M, M2, M3, ST> swap1() {
            return (LazyEither5<PT, M,M2, M3, ST>) this;

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
    static class Left4<ST, M,M2, M3, PT> implements LazyEither5<ST, M, M2, M3,PT> {
        private final Eval<M3> value;

        @Override
        public <R> LazyEither5<ST, M, M2, M3,R> map(final Function<? super PT, ? extends R> fn) {
            return (LazyEither5<ST, M, M2,M3,R>) this;
        }

        @Override
        public LazyEither5<ST, M, M2,M3, PT> peek(final Consumer<? super PT> action) {
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
        public <RT1> LazyEither5<ST, M, M2, M3,RT1> flatMap(
                final Function<? super PT, ? extends LazyEither5<ST, M,M2,M3, ? extends RT1>> mapper) {

            return (LazyEither5) this;

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

        @Override
        public <R1, R2> LazyEither5<ST, M, M2,R1, R2> bimap(final Function<? super M3, ? extends R1> fn1,
                                                            final Function<? super PT, ? extends R2> fn2) {
            return (LazyEither5<ST, M,M2,R1, R2>) this;
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
        public <T> LazyEither5<ST, M,M2,M3, T> unit(final T unit) {
            return LazyEither5.right(unit);
        }
        @Override
        public LazyEither5<ST, M,M2,PT,M3> swap4() {
            return new Right<>(
                    value);

        }
        @Override
        public LazyEither5<ST, M, PT,M3, M2> swap3() {
            return (LazyEither5<ST, M, PT,M3, M2>)this;

        }
        @Override
        public LazyEither5<ST, PT,M2, M3, M> swap2() {
           return (LazyEither5<ST, PT,M2, M3, M>)this;

        }

        @Override
        public LazyEither5<PT, M, M2, M3, ST> swap1() {
            return (LazyEither5<PT, M,M2, M3, ST>) this;

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
