package cyclops.control;

import com.oath.cyclops.hkt.DataWitness.either;
import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.hkt.Higher2;
import com.oath.cyclops.types.reactive.Completable;
import com.oath.cyclops.types.MonadicValue;
import cyclops.companion.Semigroups;
import cyclops.data.LazySeq;
import cyclops.function.*;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.InvalidObjectException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.*;

/**
 * A totally Lazy Either implementation with tail call optimization for transform and flatMap operators. LazyEither can operate reactively (i.e. suppports data arriving asynchronsouly
 the monadic chain of computations will only be executed once data arrives).
 *
 * 'Right' (or right type) biased disjunct union.

 *  No 'projections' are provided, swap() and secondaryXXXX alternative methods can be used instead.
 *
 *  Either is used to represent values that can be one of two states (for example a validation result, lazy everything is ok - or we have an error).
 *  It can be used to avoid a common design anti-pattern where an Object has two fields one of which is always null (or worse, both are defined as Optionals).
 *
 *  <pre>
 *  {@code
 *
 *     public class Member{
 *           LazyEither<SeniorTeam,JuniorTeam> team;
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
 *  Instantiating a LazyEither - Right
 *  <pre>
 *  {@code
 *      LazyEither.right("hello").map(v->v+" world")
 *    //LazyEither.right["hello world"]
 *  }
 *  </pre>
 *
 *  Instantiating an LazyEither - Left
 *  <pre>
 *  {@code
 *      LazyEither.left("hello").map(v->v+" world")
 *    //LazyEither.left["hello"]
 *  }
 *  </pre>
 *
 *  LazyEither can operate (via transform/flatMap) as a Functor / Monad and via combine as an ApplicativeFunctor
 *
 *   Values can be accumulated via
 *  <pre>
 *  {@code
 *  LazyEither.accumulateLeft(Seq.of(Either.left("failed1"),
                                                    Either.left("failed2"),
                                                    Either.right("success")),
                                                    SemigroupK.stringConcat)
 *
 *  //failed1failed2
 *
 *   LazyEither<String,String> fail1 = Either.left("failed1");
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
public interface LazyEither<LT, RT> extends Either<LT, RT> {


  public static <ST,T> LazyEither<ST,T> narrowK2(final Higher2<either, ST,T> xor) {
     return fromEither(Either.narrowK2(xor));

  }
  public static <ST,T> LazyEither<ST,T> narrowK(final Higher<Higher<either, ST>,T> xor) {
    return fromEither(Either.narrowK(xor));
  }

    public static  <L,T,R> LazyEither<L,R> tailRec(T initial, Function<? super T, ? extends LazyEither<L,? extends Either<T, R>>> fn){
        LazyEither<L,? extends Either<T, R>> next[] = new LazyEither[1];
        next[0] = LazyEither.right(Either.left(initial));
        boolean cont = true;
        do {
            cont = next[0].fold(p -> p.fold(s -> {
                next[0] = fn.apply(s);
                return true;
            }, pr -> false), () -> false);
        } while (cont);
        return next[0].map(x->x.orElse(null));
    }

    default int arity(){
        return 2;
    }

    static <RT> LazyEither<Throwable,RT> async(final Executor ex, final Supplier<RT> s){
        return fromFuture(Future.of(s,ex));
    }
    /**
     * Create a reactive CompletableEither
     *
     * <pre>
     *  {@code
     *      ___Example 1___
     *
     *      CompletableEither<Integer,Integer> completable = Either.lazy();
            Either<Throwable,Integer> mapped = completable.map(i->i*2)
                                                          .flatMap(i->Eval.later(()->i+1));

            completable.complete(5);

            mapped.printOut();
            //11

            ___Example 2___

            CompletableEither<Integer,Integer> completable = Either.lazy();
            Either<Throwable,Integer> mapped = completable.map(i->i*2)
                                                          .flatMap(i->Eval.later(()->i+1));


            completable.complete(null);

            //Either:Left[NoSuchElementException]

            ___Example 3___

            CompletableEither<Integer,Integer> completable = Either.lazy();
            Either<Throwable,Integer> mapped = completable.map(i->i*2)
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
        CompletableFuture<RT> c = new CompletableFuture<RT>();
        return new LazyEither.CompletableEither<RT, RT>(c,fromFuture(Future.of(c)));
    }

    default Either<LT,RT> toEither(){
        return fold(Either::left,Either::right);
    }

    @AllArgsConstructor
    static class CompletableEither<ORG,RT> implements LazyEither<Throwable,RT>, Completable<ORG> {

        public final CompletableFuture<ORG> complete;
        public final LazyEither<Throwable,RT> either;


      @Override
      public LazyEither<Throwable, RT> recover(Supplier<? extends RT> value) {
        return either.recover(value);
      }

      @Override
      public LazyEither<Throwable, RT> recover(RT value) {
        return either.recover(value);
      }

      @Override
      public LazyEither<Throwable, RT> recoverWith(Supplier<? extends Either<Throwable, RT>> fn) {
        return either.recoverWith(fn);
      }

      private Object writeReplace() {
            return toEither();
        }
        private Object readResolve() throws InvalidObjectException {
            throw new InvalidObjectException("Use Serialization Proxy instead.");
        }
        @Override
        public boolean isFailed() {
            return complete.isCompletedExceptionally();
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
        public Maybe<RT> filter(Predicate<? super RT> test) {
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
        public LazyEither<Throwable, RT> flatMapLeftToRight(Function<? super Throwable, ? extends Either<Throwable, RT>> fn) {
            return either.flatMapLeft(fn);
        }

        @Override
        public <LT1> LazyEither<LT1, RT> flatMapLeft(Function<? super Throwable, ? extends Either<LT1, RT>> mapper) {
            return either.flatMapLeft(mapper);
        }

        @Override
        public <R> R fold(Function<? super java.lang.Throwable, ? extends R> secondary, Function<? super RT, ? extends R> primary) {
            return either.fold(secondary,primary);
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
        public <R> R fold(Function<? super RT, ? extends R> present, Supplier<? extends R> absent) {
            return either.fold(i->absent.get(),present);
        }
        @Override
        public void subscribe(Subscriber<? super RT> sub) {
            either.subscribe(sub);
        }
    }

    static <ST,PT> LazyEither<ST,PT> fromEither(Either<ST,PT> xor){
        if(xor instanceof LazyEither)
            return (LazyEither<ST,PT>)xor;
        return xor.fold(LazyEither::left, LazyEither::right);
    }

    static <LT,RT> LazyEither<LT,RT> fromLazy(Eval<LazyEither<LT,RT>> lazy){
        return new LazyEither.Lazy<>(lazy);
    }

    static <LT,RT> LazyEither<LT,RT> later(Supplier<Either<LT,RT>> lazy){
        return new LazyEither.Lazy<>(Eval.later(lazy).map(LazyEither::fromEither));
    }
    static <LT,RT> LazyEither<LT,RT> always(Supplier<Either<LT,RT>> lazy){
        return new LazyEither.Lazy<>(Eval.always(lazy).map(LazyEither::fromEither));
    }

    static <T> LazyEither<Throwable,T> fromFuture(Future<T> future){
        return fromLazy(Eval.fromFuture(future.<LazyEither<Throwable,T>>map(LazyEither::right).recover(e->LazyEither.<Throwable,T>left(e.getCause()))));

    }

    /**
     *  Turn an IterableX of Eithers into a single Either with Lists of values.
     *
     * <pre>
     * {@code
     *
     * Either<String,Integer> just  = Either.right(10);
       Either<String,Integer> none = Either.left("none");


     * Either<Seq<String>,Seq<Integer>> xors =Either.sequence(Seq.of(just,none,Either.right(1)));
       //Eitehr.right(Seq.of(10,1)));
     *
     * }</pre>
     *
     *
     *
     * @param xors Either to sequence
     * @return Either Sequenced
     */
    public static <LT1, PT> LazyEither<LT1,ReactiveSeq<PT>> sequenceRight(final Iterable<LazyEither<LT1, PT>> xors) {
        Objects.requireNonNull(xors);
        return sequence(ReactiveSeq.fromIterable(xors).filter(LazyEither::isRight));
    }
    public static <LT1, PT> LazyEither<ReactiveSeq<LT1>,PT> sequenceLeft(final Iterable<LazyEither<LT1, PT>> xors) {
        Objects.requireNonNull(xors);
      LazyEither<PT, ReactiveSeq<LT1>> res = sequence(ReactiveSeq.fromIterable(xors)
                            .filter(LazyEither::isRight)
                        .map(i -> i.swap()));
        return res.swap();
    }
  public static  <L,T> LazyEither<L,ReactiveSeq<T>> sequence(ReactiveSeq<? extends LazyEither<L,T>> stream) {

    LazyEither<L, ReactiveSeq<T>> identity = right(ReactiveSeq.empty());

    BiFunction<LazyEither<L,ReactiveSeq<T>>,LazyEither<L,T>,LazyEither<L,ReactiveSeq<T>>> combineToStream = (acc,next) ->acc.zip(next,(a,b)->a.append(b));

    BinaryOperator<LazyEither<L,ReactiveSeq<T>>> combineStreams = (a,b)-> a.zip(b,(z1,z2)->z1.appendStream(z2));

    return stream.reduce(identity,combineToStream,combineStreams);
  }
  public static <L,T,R> LazyEither<L,ReactiveSeq<R>> traverse(Function<? super T,? extends R> fn,ReactiveSeq<LazyEither<L,T>> stream) {
    return sequence(stream.map(h->h.map(fn)));
  }
    /**
     * TraverseOps a Collection of Either producting an Either3 with a Seq, applying the transformation function to every
     * element in the list
     *
     * @param xors Eithers to sequence and transform
     * @param fn Transformation function
     * @return An Either with a transformed list
     */
    public static <LT1, PT,R> LazyEither<LT1,ReactiveSeq<R>> traverseRight(final Iterable<LazyEither<LT1, PT>> xors, Function<? super PT, ? extends R> fn) {
        return  sequenceRight(xors).map(l->l.map(fn));
    }
    public static <LT1, PT,R> LazyEither<ReactiveSeq<R>,PT> traverseLeft(final Iterable<LazyEither<LT1, PT>> xors, Function<? super LT1, ? extends R> fn) {
        return  sequenceLeft(xors).mapLeft(l->l.map(fn));
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
     * @return  Either populated with the accumulate right operation
     */
    public static <LT1, RT> LazyEither<LT1, RT> accumulate(final Monoid<RT> reducer, final Iterable<LazyEither<LT1, RT>> xors) {
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
    @Deprecated //use foldAny
    static <X, PT extends X, ST extends X,R> R visitAny(LazyEither<ST, PT> either, Function<? super X, ? extends R> fn){
        return foldAny(either,fn);
    }
    static <X, PT extends X, ST extends X,R> R foldAny(LazyEither<ST, PT> either, Function<? super X, ? extends R> fn){
        return either.fold(fn, fn);
    }

    static <X, LT extends X, RT extends X> X visitAny(Consumer<? super X> c, LazyEither<LT, RT> either){
        Function<? super X, X> fn = x ->{
            c.accept(x);
            return x;
        };
        return visitAny(either,fn);
    }
    @Override
    LazyEither<LT, RT> recover(Supplier<? extends RT> value);

    @Override
    LazyEither<LT, RT> recover(RT value);

    @Override
    LazyEither<LT, RT> recoverWith(Supplier<? extends Either<LT, RT>> fn);

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
     * @see com.oath.cyclops.control.Xor#combineToList(com.oath.cyclops.control.Xor, java.util.function.BiFunction)
     */
    @Override
    default <T2, R> LazyEither<LazySeq<LT>, R> combineToLazySeq(Either<LT, ? extends T2> app,
                                                                BiFunction<? super RT, ? super T2, ? extends R> fn) {

        return (LazyEither<LazySeq<LT>, R>)Either.super.combineToLazySeq(app, fn);
    }
    /* (non-Javadoc)
     * @see com.oath.cyclops.control.Xor#combine(com.oath.cyclops.control.Xor, java.util.function.BinaryOperator, java.util.function.BiFunction)
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
     *   ReactiveSeq<Integer> stream =  ReactiveSeq.of(1,2,3);

         Either<Throwable,Integer> future = Either.fromPublisher(stream);

         //Either[1]
     *
     * }
     * </pre>
     * @param pub Publisher to construct an Either from
     * @return Either constructed from the supplied Publisher
     */
    public static <T> LazyEither<Throwable, T> fromPublisher(final Publisher<T> pub) {
        if(pub instanceof LazyEither)
            return (LazyEither<Throwable,T>)pub;
        CompletableEither<T, T> result = LazyEither.either();

        pub.subscribe(new Subscriber<T>() {
            Subscription sub;
            @Override
            public void onSubscribe(Subscription s) {
                sub =s;
                s.request(1l);
            }

            @Override
            public void onNext(T t) {
                result.complete(t);

            }

            @Override
            public void onError(Throwable t) {
                result.completeExceptionally(t);
            }

            @Override
            public void onComplete() {
                if(!result.isDone())  {
                   result.completeExceptionally(new NoSuchElementException());
                }
            }
        });
        return result;


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
        return later(()-> {
            final Iterator<T> it = iterable.iterator();
            return it.hasNext() ? LazyEither.right(it.next()) : LazyEither.right(alt);
        });
    }


    /**
     * Create an instance of the left type. Most methods are biased to the right type,
     * so you will need to use swap() or secondaryXXXX to manipulate the wrapped value
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
    public static <ST, PT> LazyEither<ST, PT> left(final ST value) {
        return new Left(Eval.later(()-> value));
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
    public static <ST, PT> LazyEither<ST, PT> right(final PT value) {

        return new Right(Eval.later(()->
                           value));
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
     * @see com.oath.cyclops.types.MonadicValue#nest()
     */

    default LazyEither<LT, Either<LT,RT>> nest() {
        return this.map(t -> unit(t));
    }

  default <T2, R> LazyEither<LT, R> zip(final Ior<LT,? extends T2> app, final BiFunction<? super RT, ? super T2, ? extends R> fn){
    return flatMap(t->app.map(t2->fn.apply(t,t2)).toEither());
  }
  default <T2, R> LazyEither<LT, R> zip(final Either<LT,? extends T2> app, final BiFunction<? super RT, ? super T2, ? extends R> fn){
    return flatMap(t->app.map(t2->fn.apply(t,t2)));
  }

    /*
     * (non-Javadoc)
     *
     * @see com.oath.cyclops.types.MonadicValue#unit(java.lang.Object)
     */
    @Override
    default <T> LazyEither<LT, T> unit(final T unit) {
        return LazyEither.right(unit);
    }



    @Override
    Maybe<RT> filter(Predicate<? super RT> test);
    default LazyEither<LT, RT> filter(Predicate<? super RT> test, Function<? super RT, ? extends LT> rightToLeft){
      return flatMap(e->test.test(e) ? LazyEither.right(e) : LazyEither.left(rightToLeft.apply(e)));
    }

    /**
     * If this Either contains the Left type, transform it's value so that it contains the Right type
     *
     *
     * @param fn Function to transform left type to right
     * @return Either with left type mapped to right
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
     * @see com.oath.cyclops.types.MonadicValue#transform(java.util.function.Function)
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
     * @see com.oath.cyclops.types.Functor#peek(java.util.function.Consumer)
     */
    @Override
    LazyEither<LT, RT> peek(Consumer<? super RT> action);


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
    LazyEither<RT, LT> swap();

    /*
     * (non-Javadoc)
     *
     * @see com.oath.cyclops.types.Value#toIor()
     */
    @Override
    Ior<LT, RT> toIor();

    /*
     * (non-Javadoc)
     *
     * @see com.oath.cyclops.types.foldable.Convertable#isPresent()
     */
    @Override
    default boolean isPresent() {
        return isRight();
    }


    /*
     * (non-Javadoc)
     *
     * @see com.oath.cyclops.types.Value#toLazyEither(java.lang.Object)
     */
    @Override
    default <ST2> Either<ST2, RT> toEither(final ST2 secondary) {
        return fold(s -> Either.left(secondary), p -> Either.right(p));
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
     * @param secondary Function to execute if this is a Left Either
     * @param primary Function to execute if this is a Right Ior
     * @return Result of executing the appropriate function
     */
    <R> R fold(Function<? super LT, ? extends R> secondary, Function<? super RT, ? extends R> primary);



    /*
     * (non-Javadoc)
     *
     * @see com.oath.cyclops.types.functor.BiTransformable#bimap(java.util.function.Function,
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
     * @see com.oath.cyclops.types.functor.BiTransformable#bipeek(java.util.function.Consumer,
     * java.util.function.Consumer)
     */
    @Override
    default LazyEither<LT, RT> bipeek(Consumer<? super LT> c1, Consumer<? super RT> c2) {

        return (LazyEither<LT, RT>) Either.super.bipeek(c1, c2);
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
     * @return A Stream containing the left value if present, otherwise an zero Stream
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


    /**
     * @return True if this is a right Either
     */
    public boolean isRight();

    /**
     * @return True if this is a left Either
     */
    public boolean isLeft();



    /**
     * @return An Either with the left type converted to a persistent list, for use with accumulating app function
     */
    default LazyEither<LazySeq<LT>, RT> lazySeq() {
        return mapLeft(LazySeq::of);
    }

    /**
     * Accumulate secondarys into a LazySeq (extended Persistent List) and Right with the supplied combiner function
     * Right accumulation only occurs if all phases are right
     *
     * @param app Value to combine with
     * @param fn Combiner function for right values
     * @return Combined Either
     */
    default <T2, R> LazyEither<LazySeq<LT>, R> combineToList(final LazyEither<LT, ? extends T2> app,
                                                                 final BiFunction<? super RT, ? super T2, ? extends R> fn) {
        return lazySeq().combine(app.lazySeq(), Semigroups.lazySeqConcat(), fn);
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

    default <T2, R> LazyEither<LT, R> combine(final LazyEither<? extends LT, ? extends T2> app,
                                              final BinaryOperator<LT> semigroup, final BiFunction<? super RT, ? super T2, ? extends R> fn) {
        return this.fold(secondary -> app.fold(s2 -> LazyEither.left(semigroup.apply(s2, secondary)),
                                                 p2 -> LazyEither.left(secondary)),
                          primary -> app.fold(s2 -> LazyEither.left(s2),
                                               p2 -> LazyEither.right(fn.apply(primary, p2))));
    }



    static <ST, PT> LazyEither<ST, PT> narrow(final LazyEither<? extends ST, ? extends PT> broad) {
        return (LazyEither<ST, PT>) broad;
    }

   // @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static final class Lazy<ST, PT> implements LazyEither<ST, PT> {
        private Lazy(Eval<LazyEither<ST, PT>> lazy) {
            this.lazy = lazy;
        }

        private final Eval<LazyEither<ST, PT>> lazy;

        private Object writeReplace() {
            return toEither();
        }
        private Object readResolve() throws InvalidObjectException {
            throw new InvalidObjectException("Use Serialization Proxy instead.");
        }

      @Override
      public LazyEither<ST, PT> recover(Supplier<? extends PT> value) {
          return new Lazy<ST,PT>(
              lazy.map(m -> m.recover(value)));
      }

      @Override
      public LazyEither<ST, PT> recover(PT value) {
          return new Lazy<ST,PT>(
              lazy.map(m -> m.recover(value)));
      }

      @Override
      public LazyEither<ST, PT> recoverWith(Supplier<? extends Either<ST, PT>> fn) {
          return new Lazy<ST,PT>(
              lazy.map(m -> m.recoverWith(fn)));

      }
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


        @Override
        public <R> LazyEither<ST, R> map(final Function<? super PT, ? extends R> mapper) {
            return flatMap(t -> LazyEither.right(mapper.apply(t)));


        }

        private <PT> LazyEither<ST, PT> toEither(MonadicValue<? extends PT> value) {
            return value.fold(p -> LazyEither.right(p), () -> LazyEither.left(null));
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
                                        // we could safely propagate an error if pts was a left
                        PT v = pts.orElse(null);
                        if(v!=null)
                            sub.onNext(v);

                        if(!onCompleteSent){
                            sub.onComplete();
                            onCompleteSent =true;
                        }
                    }else if(pts.isLeft()){
                        ST v = pts.swap().orElse(null);
                        if(v instanceof Throwable) {
                            sub.onError((Throwable) v);

                        }
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
       public ReactiveSeq<PT> stream() {
           return Spouts.from(this);
       }

       @Override
        public Maybe<PT> filter(final Predicate<? super PT> test) {
            return Maybe.fromPublisher(this).filter(test);

        }

        @Override
        public LazyEither<ST, PT> filter(Predicate<? super PT> test, Function<? super PT, ? extends ST> rightToLeft) {
            return LazyEither.fromLazy(lazy.map(m->m.filter(test,rightToLeft)));
        }



        @Override
        public LazyEither<ST, PT> mapLeftToRight(Function<? super ST, ? extends PT> fn) {
            return LazyEither.fromLazy(lazy.map(m->m.mapLeftToRight(fn)));

        }


        @Override
        public <R> LazyEither<R, PT> mapLeft(Function<? super ST, ? extends R> fn) {
            return LazyEither.fromLazy(lazy.map(m->m.mapLeft(fn)));
        }


        @Override
        public LazyEither<ST, PT> peekLeft(Consumer<? super ST> action) {
            return LazyEither.fromLazy(lazy.map(m -> m.peekLeft(action)));
        }


        @Override
        public LazyEither<ST, PT> peek(Consumer<? super PT> action) {
            return LazyEither.fromLazy(lazy.map(m->m.peek(action)));
        }


        @Override
        public LazyEither<PT, ST> swap() {
            return LazyEither.fromLazy(lazy.map(m->m.swap()));
        }


        @Override
        public Ior<ST, PT> toIor() {
            return trampoline()
                       .toIor();
        }


        @Override
        public <R> R fold(Function<? super ST, ? extends R> secondary, Function<? super PT, ? extends R> primary) {
            return trampoline()
                       .fold(secondary, primary);
        }
        private LazyEither<ST,PT> trampoline(){
            LazyEither<ST,PT> maybe = lazy.get();
            while (maybe instanceof Lazy) {
                maybe = ((Lazy<ST,PT>) maybe).lazy.get();
            }
            return maybe;
        }


        @Override
        public Option<PT> get() {

            return Maybe.fromIterable((this));
        }


        @Override
        public Option<ST> getLeft() {
            return Maybe.fromIterable(swap());
        }

        @Override
        public ST leftOrElse(ST alt) {
            return trampoline().leftOrElse(alt);
        }




        @Override
        public ReactiveSeq<ST> leftToStream() {
            return ReactiveSeq.generate(() -> trampoline()
                                                  .leftToStream())
                              .flatMap(Function.identity());
        }


        @Override
        public <LT1> LazyEither<LT1, PT> flatMapLeft(Function<? super ST, ? extends Either<LT1, PT>> mapper) {
            return LazyEither.fromLazy(lazy.map(m->m.flatMapLeft(mapper)));

        }


        @Override
        public LazyEither<ST, PT> flatMapLeftToRight(Function<? super ST, ? extends Either<ST, PT>> fn) {
            return flatMapLeft(fn);

        }



        @Override
        public boolean isRight() {
            return trampoline()
                       .isRight();
        }


        @Override
        public boolean isLeft() {
            return trampoline()
                       .isLeft();
        }




        @Override
        public int hashCode() {
            return trampoline().hashCode();

        }


        @Override
        public boolean equals(final Object obj) {

            return trampoline().equals(obj);
        }
        @Override
        public String toString(){
            return trampoline().toString();
        }



        @Override
        public <R> R fold(Function<? super PT, ? extends R> present, Supplier<? extends R> absent) {
            return trampoline().fold(present,absent);
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class Right<ST, PT> implements LazyEither<ST, PT> {
        private final Eval<PT> value;

      @Override
      public LazyEither<ST, PT> recover(Supplier<? extends PT> value) {
        return this;
      }

      @Override
      public LazyEither<ST, PT> recover(PT value) {
        return this;
      }

      @Override
      public LazyEither<ST, PT> recoverWith(Supplier<? extends Either<ST, PT>> fn) {
        return this;
      }

      private Object writeReplace() {
            return toEither();
        }
        private Object readResolve() throws InvalidObjectException {
            throw new InvalidObjectException("Use Serialization Proxy instead.");
        }
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
        public Maybe<PT> filter(final Predicate<? super PT> test) {
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
        public ReactiveSeq<ST> leftToStream() {
            return ReactiveSeq.empty();
        }

        @Override
        public <RT1> LazyEither<ST, RT1> flatMap(Function<? super PT, ? extends Either<? extends ST, ? extends RT1>> mapper) {
            Eval<? extends LazyEither<? extends ST,  ? extends RT1>> ret = value.map(mapper.andThen(LazyEither::fromEither));


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
            return "Either.right[" + value.get() + "]";
        }

        @Override
        public Ior<ST, PT> toIor() {
            return Ior.right(value.get());
        }

        @Override
        public <R> R fold(final Function<? super ST, ? extends R> secondary,
                          final Function<? super PT, ? extends R> primary) {
            return primary.apply(value.get());
        }





        @Override
        public int hashCode() {
            return Objects.hashCode(value.get());

        }


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
        public <R> R fold(Function<? super PT, ? extends R> present, Supplier<? extends R> absent) {
            return present.apply(value.get());
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class Left<ST, PT> implements LazyEither<ST, PT> {
        private final Eval<ST> value;


      @Override
      public LazyEither<ST, PT> recover(Supplier<? extends PT> value) {
          return new Lazy<>(Eval.later(() -> LazyEither.right(value.get())));
      }

      @Override
      public LazyEither<ST, PT> recover(PT value) {
          return new Lazy<>(Eval.later(()->LazyEither.right(value)));

      }

      @Override
      public LazyEither<ST, PT> recoverWith(Supplier<? extends Either<ST, PT>> fn) {
        return new Lazy<>(Eval.later(()->LazyEither.fromEither(fn.get())));
      }

      private Object writeReplace() {
            return toEither();
        }
        private Object readResolve() throws InvalidObjectException {
            throw new InvalidObjectException("Use Serialization Proxy instead.");
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
        public Maybe<PT> filter(final Predicate<? super PT> test) {
            return Maybe.nothing();
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
        public ReactiveSeq<ST> leftToStream() {
            return ReactiveSeq.fromIterable(getLeft());
        }

        @Override
        public <RT1> LazyEither<ST, RT1> flatMap(Function<? super PT, ? extends Either<? extends ST, ? extends RT1>> mapper) {
            return (LazyEither<ST, RT1>) this;
        }


        @Override
        public <LT1> LazyEither<LT1, PT> flatMapLeft(
                final Function<? super ST, ? extends Either<LT1, PT>> mapper) {
            Eval<? extends Either<LT1,? extends PT>> ret = value.map(mapper);
            Eval<? extends LazyEither<? extends LT1,  ? extends PT>> et = ret.map(LazyEither::fromEither);

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
        public <R> R fold(final Function<? super ST, ? extends R> secondary,
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
            return "Either.left[" + value.get() + "]";
        }



        @Override
        public Ior<ST, PT> toIor() {
            return Ior.left(value.get());
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
        public <R> R fold(Function<? super PT, ? extends R> present, Supplier<? extends R> absent) {
            return absent.get();
        }
    }

}
