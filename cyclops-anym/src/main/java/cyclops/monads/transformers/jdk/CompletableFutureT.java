package cyclops.monads.transformers.jdk;

import com.oath.cyclops.types.Filters;
import com.oath.cyclops.types.MonadicValue;
import com.oath.cyclops.anym.transformers.ValueTransformer;
import com.oath.cyclops.types.foldable.To;
import com.oath.cyclops.types.functor.ReactiveTransformable;
import com.oath.cyclops.types.functor.Transformable;
import cyclops.control.Future;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.monads.AnyM;
import cyclops.monads.WitnessType;
import cyclops.reactive.ReactiveSeq;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import org.reactivestreams.Publisher;

import java.util.Iterator;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.*;

/**
* Monad Transformer for CompletableFuture's nested within another monadic type

 *
 * FutureT allows the deeply wrapped Future to be manipulating within it's nested /contained context
 *
 * @author johnmcclean
 *
 * @param <T> Type of data stored inside the nested Future(s)
 */
public final class CompletableFutureT<W extends WitnessType<W>,T> extends ValueTransformer<W,T>
                                                                  implements To<CompletableFutureT<W,T>>,
                                                                             ReactiveTransformable<T>,
                                                                             Transformable<T>,
                                                                             Filters<T> {

    private final AnyM<W,CompletableFuture<T>> run;

    @Override
    public Iterator<T> iterator() {
        return stream().iterator();
    }

                                                                         @Override
    public ReactiveSeq<T> stream() {
        return run.stream().map(CompletableFuture::join);
    }

    @Override
    public <R> CompletableFutureT<W,R> retry(Function<? super T, ? extends R> fn) {
        return (CompletableFutureT<W,R>)ReactiveTransformable.super.retry(fn);
    }

    @Override
    public <R> CompletableFutureT<W,R> retry(Function<? super T, ? extends R> fn, int retries, long delay, TimeUnit timeUnit) {
        return (CompletableFutureT<W,R>)ReactiveTransformable.super.retry(fn,retries,delay,timeUnit);
    }

    /**
     * @return The wrapped AnyM
     */
    @Override
    public AnyM<W,CompletableFuture<T>> unwrap() {
        return run;
    }

    public <R> R unwrapTo(Function<? super AnyM<W,CompletableFuture<T>>, ? extends R> fn) {
        return unwrap().to(fn);
    }

    private CompletableFutureT(final AnyM<W,CompletableFuture<T>> run) {
        this.run = run;
    }


    @Override @Deprecated (/*DO NOT USE INTERNAL USE ONLY*/)
    protected <R> CompletableFutureT<W,R> unitAnyM(AnyM<W,? super MonadicValue<R>> traversable) {

        return of((AnyM) traversable);
    }

    @Override
    public AnyM<W,? extends MonadicValue<T>> transformerStream() {

        return run.map(Future::of);
    }

    @Override
    public CompletableFutureT<W,T> filter(final Predicate<? super T> test) {
        return of(run.map(f->f.thenApply(in->Tuple.tuple(in,test.test(in))))
                     .filter( f->f.join()._2() )
                     .map( f->f.thenApply(in->in._1())));
    }

    /**
     * Peek at the current value of the Future
     * <pre>
     * {@code
     *    FutureT.of(AnyM.fromStream(Arrays.asFuture(10))
     *             .peek(System.out::println);
     *
     *     //prints 10
     * }
     * </pre>
     *
     * @param peek  Consumer to accept current value of Future
     * @return FutureT with peek call
     */
    @Override
    public CompletableFutureT<W,T> peek(final Consumer<? super T> peek) {
        return of(run.peek(future -> future.thenApply(a -> {
            peek.accept(a);
            return a;
        })));
    }

    /**
     * Map the wrapped Future
     *
     * <pre>
     * {@code
     *  FutureT.of(AnyM.fromStream(Arrays.asFuture(10))
     *             .map(t->t=t+1);
     *
     *
     *  //FutureT<AnyMSeq<Stream<Future[11]>>>
     * }
     * </pre>
     *
     * @param f Mapping function for the wrapped Future
     * @return FutureT that applies the transform function to the wrapped Future
     */
    @Override
    public <B> CompletableFutureT<W,B> map(final Function<? super T, ? extends B> f) {
        return new CompletableFutureT<W,B>(
                                  run.map(o -> o.thenApply(f)));
    }
    public <B> CompletableFutureT<W,B> map(final Function<? super T, ? extends B> f, Executor ex) {
        return new CompletableFutureT<W,B>(
                                  run.map(o -> o.thenApplyAsync(f,ex)));
    }

    /**
     * Flat Map the wrapped Future
      * <pre>
     * {@code
     *  FutureT.of(AnyM.fromStream(Arrays.asFuture(10))
     *             .flatMap(t->Future.completedFuture(20));
     *
     *
     *  //FutureT<AnyMSeq<Stream<Future[20]>>>
     * }
     * </pre>
     * @param f FlatMap function
     * @return FutureT that applies the flatMap function to the wrapped Future
     */

    public <B> CompletableFutureT<W,B> flatMapT(final Function<? super T, CompletableFutureT<W,B>> f) {
        return of(run.map(future -> future.thenCompose(a -> f.apply(a).run.stream()
                                                                      .toList()
                                                                      .get(0))));
    }

    private static <W extends WitnessType<W>,B> AnyM<W,CompletableFuture<B>> narrow(final AnyM<W,CompletableFuture<? extends B>> run) {
        return (AnyM) run;
    }


    public <B> CompletableFutureT<W,B> flatMap(final Function<? super T, ? extends CompletableFuture<? extends B>> f) {

        final AnyM<W,CompletableFuture<? extends B>> mapped = run.map(o -> o.thenCompose(f.andThen(s->s.toCompletableFuture())));
        return of(narrow(mapped));

    }

    /**
     * Lift a function into one that accepts and returns an FutureT
     * This allows multiple monad types to add functionality to existing function and methods
     *
     * e.g. to add list handling  / iteration (via Future) and iteration (via Stream) to an existing function
     * <pre>
     * {@code
        Function<Integer,Integer> add2 = i -> i+2;
    	Function<FutureT<Integer>, FutureT<Integer>> optTAdd2 = FutureT.lift(add2);

    	Stream<Integer> withNulls = Stream.of(1,2,3);
    	AnyMSeq<Integer> stream = AnyM.fromStream(withNulls);
    	AnyMSeq<CompletableFuture<Integer>> streamOpt = stream.map(Future::completedFuture);
    	List<Integer> results = optTAdd2.applyHKT(FutureT.of(streamOpt))
    									.unwrap()
    									.<Stream<CompletableFuture<Integer>>>unwrap()
    									.map(Future::join)
    									.collect(CyclopsCollectors.toList());


    	//Future.completedFuture(List[3,4]);
     *
     *
     * }</pre>
     *
     *
     * @param fn Function to enhance with functionality from Future and another monad type
     * @return Function that accepts and returns an FutureT
     */
    public static <W extends WitnessType<W>,U, R> Function<CompletableFutureT<W,U>, CompletableFutureT<W,R>> lift(final Function<? super U, ? extends R> fn) {
        return optTu -> optTu.map(input -> fn.apply(input));
    }

    /**
     * Lift a BiFunction into one that accepts and returns  FutureTs
     * This allows multiple monad types to add functionality to existing function and methods
     *
     * e.g. to add list handling / iteration (via Future), iteration (via Stream)  and asynchronous execution (Future)
     * to an existing function
     *
     * <pre>
     * {@code
    	BiFunction<Integer,Integer,Integer> add = (a,b) -> a+b;
    	BiFunction<FutureT<Integer>,FutureT<Integer>,FutureT<Integer>> optTAdd2 = FutureT.lift2(add);

    	Stream<Integer> withNulls = Stream.of(1,2,3);
    	AnyMSeq<Integer> stream = AnyM.ofMonad(withNulls);
    	AnyMSeq<CompletableFuture<Integer>> streamOpt = stream.map(Future::completedFuture);

    	CompletableFuture<CompletableFuture<Integer>> two = Future.completedFuture(Future.completedFuture(2));
    	AnyMSeq<CompletableFuture<Integer>> future=  AnyM.fromFuture(two);
    	List<Integer> results = optTAdd2.applyHKT(FutureT.of(streamOpt),FutureT.of(future))
    									.unwrap()
    									.<Stream<CompletableFuture<Integer>>>unwrap()
    									.map(Future::join)
    									.collect(CyclopsCollectors.toList());

    		//Future.completedFuture(List[3,4,5]);
      }
      </pre>
     * @param fn BiFunction to enhance with functionality from Future and another monad type
     * @return Function that accepts and returns an FutureT
     */
    public static <W extends WitnessType<W>, U1,  U2, R> BiFunction<CompletableFutureT<W,U1>, CompletableFutureT<W,U2>, CompletableFutureT<W,R>> lift2(
            final BiFunction<? super U1, ? super U2, ? extends R> fn) {
        return (optTu1, optTu2) -> optTu1.flatMapT(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
    }

    /**
     * Construct an FutureT from an AnyM that contains a monad type that contains type other than Future
     * The values in the underlying monad will be mapped to CompletableFuture<A>
     *
     * @param anyM AnyM that doesn't contain a monad wrapping an Future
     * @return FutureT
     */
    public static <W extends WitnessType<W>,A> CompletableFutureT<W,A> fromAnyM(final AnyM<W,A> anyM) {
        return of(anyM.map(e-> {
            CompletableFuture<A> f = new CompletableFuture<A>();
           f.complete(e);
           return f;
        }));
    }

    /**
     * Construct an FutureT from an AnyM that wraps a monad containing  Futures
     *
     * @param monads AnyM that contains a monad wrapping an Future
     * @return FutureT
     */
    public static <W extends WitnessType<W>,A> CompletableFutureT<W,A> of(final AnyM<W,CompletableFuture<A>> monads) {
        return new CompletableFutureT<>(
                                 monads);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("CompletableFutureT[%s]", run.unwrap().toString());
    }




    public <R> CompletableFutureT<W,R> unitIterable(final Iterable<R> it){
        return of(run.unitIterable(it)
                     .map(e-> {
                         CompletableFuture<R> f = new CompletableFuture<>();
                         f.complete(e);
                         return f;
                     }));
    }

    @Override
    public <R> CompletableFutureT<W,R> unit(final R value) {
        CompletableFuture<R> f = new CompletableFuture<>();
        f.complete(value);
        return of(run.unit(f));
    }

    @Override
    public <R> CompletableFutureT<W,R> empty() {
        return of(run.unit(new CompletableFuture<>()));
    }




    @Override
    public int hashCode() {
        return run.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof CompletableFutureT) {
            return run.equals(((CompletableFutureT) o).run);
        }
        return false;
    }


  /* (non-Javadoc)
   * @see cyclops2.monads.transformers.values.ValueTransformer#iterate(java.util.function.UnaryOperator)
   */
    @Override
    public AnyM<W, ? extends ReactiveSeq<T>> iterate(UnaryOperator<T> fn, T alt) {

        return super.iterate(fn,alt);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#generate()
     */
    @Override
    public AnyM<W, ? extends ReactiveSeq<T>> generate(T alt) {

        return super.generate(alt);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    public <T2, R> CompletableFutureT<W, R> zip(Iterable<? extends T2> iterable,
                                                BiFunction<? super T, ? super T2, ? extends R> fn) {

        return (CompletableFutureT<W, R>)super.zip(iterable, fn);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#zip(java.util.function.BiFunction, org.reactivestreams.Publisher)
     */
    @Override
    public <T2, R> CompletableFutureT<W, R> zip(BiFunction<? super T, ? super T2, ? extends R> fn, Publisher<? extends T2> publisher) {

        return (CompletableFutureT<W, R>)super.zip(fn, publisher);
    }


  /* (non-Javadoc)
   * @see cyclops2.monads.transformers.values.ValueTransformer#zip(java.lang.Iterable)
   */
    @Override
    public <U> CompletableFutureT<W, Tuple2<T, U>> zip(Iterable<? extends U> other) {

        return (CompletableFutureT)super.zip(other);
    }


    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#forEach4(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.QuadFunction)
     */
    @Override
    public <T2, R1, R2, R3, R> CompletableFutureT<W, R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
                                                                 BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                                                 Function3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
                                                                 Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return (CompletableFutureT<W, R>)super.forEach4(value1, value2, value3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#forEach4(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.QuadFunction, com.oath.cyclops.util.function.QuadFunction)
     */
    @Override
    public <T2, R1, R2, R3, R> CompletableFutureT<W, R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
                                                                 BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                                                 Function3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
                                                                 Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                                 Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return (CompletableFutureT<W, R>)super.forEach4(value1, value2, value3, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#forEach3(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction)
     */
    @Override
    public <T2, R1, R2, R> CompletableFutureT<W, R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
                                                             BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                                             Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (CompletableFutureT<W, R>)super.forEach3(value1, value2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#forEach3(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.TriFunction)
     */
    @Override
    public <T2, R1, R2, R> CompletableFutureT<W, R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
                                                             BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                                             Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                                                             Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (CompletableFutureT<W, R>)super.forEach3(value1, value2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    public <R1, R> CompletableFutureT<W, R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
                                                     BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return (CompletableFutureT<W, R>)super.forEach2(value1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    public <R1, R> CompletableFutureT<W, R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
                                                     BiFunction<? super T, ? super R1, Boolean> filterFunction,
                                                     BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return (CompletableFutureT<W, R>)super.forEach2(value1, filterFunction, yieldingFunction);
    }



    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#concatMap(java.util.function.Function)
     */
    @Override
    public <R> CompletableFutureT<W, R> concatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {

        return (CompletableFutureT<W, R>)super.concatMap(mapper);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#flatMapP(java.util.function.Function)
     */
    @Override
    public <R> CompletableFutureT<W, R> mergeMap(Function<? super T, ? extends Publisher<? extends R>> mapper) {

        return (CompletableFutureT<W, R>)super.mergeMap(mapper);
    }
    public <T2, R1, R2, R3, R> CompletableFutureT<W,R> forEach4M(Function<? super T, ? extends CompletableFutureT<W,R1>> value1,
                                                                 BiFunction<? super T, ? super R1, ? extends CompletableFutureT<W,R2>> value2,
                                                                 Function3<? super T, ? super R1, ? super R2, ? extends CompletableFutureT<W,R3>> value3,
                                                                 Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return this.flatMapT(in->value1.apply(in)
                .flatMapT(in2-> value2.apply(in,in2)
                        .flatMapT(in3->value3.apply(in,in2,in3)
                                .map(in4->yieldingFunction.apply(in,in2,in3,in4)))));

    }
    public <T2, R1, R2, R3, R> CompletableFutureT<W,R> forEach4M(Function<? super T, ? extends CompletableFutureT<W,R1>> value1,
                                                                 BiFunction<? super T, ? super R1, ? extends CompletableFutureT<W,R2>> value2,
                                                                 Function3<? super T, ? super R1, ? super R2, ? extends CompletableFutureT<W,R3>> value3,
                                                                 Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                                 Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return this.flatMapT(in->value1.apply(in)
                    .flatMapT(in2-> value2.apply(in,in2)
                            .flatMapT(in3->value3.apply(in,in2,in3)
                                                 .filter(in4->filterFunction.apply(in,in2,in3,in4))
                                                 .map(in4->yieldingFunction.apply(in,in2,in3,in4)))));

    }

    public <T2, R1, R2, R> CompletableFutureT<W,R> forEach3M(Function<? super T, ? extends CompletableFutureT<W,R1>> value1,
                                                             BiFunction<? super T, ? super R1, ? extends CompletableFutureT<W,R2>> value2,
                                                             Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return this.flatMapT(in->value1.apply(in).flatMapT(in2-> value2.apply(in,in2)
                                                 .map(in3->yieldingFunction.apply(in,in2,in3))));

    }

    public <T2, R1, R2, R> CompletableFutureT<W,R> forEach3M(Function<? super T, ? extends CompletableFutureT<W,R1>> value1,
                                                             BiFunction<? super T, ? super R1, ? extends CompletableFutureT<W,R2>> value2,
                                                             Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                                                             Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return this.flatMapT(in->value1.apply(in).flatMapT(in2-> value2.apply(in,in2).filter(in3->filterFunction.apply(in,in2,in3))
                                                                                     .map(in3->yieldingFunction.apply(in,in2,in3))));

    }
    public <R1, R> CompletableFutureT<W,R> forEach2M(Function<? super T, ? extends CompletableFutureT<W,R1>> value1,
                                                     BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {


        return this.flatMapT(in->value1.apply(in)
                    .map(in2->yieldingFunction.apply(in,in2)));
    }

    public <R1, R> CompletableFutureT<W,R> forEach2M(Function<? super T, ? extends CompletableFutureT<W,R1>> value1,
                                                     BiFunction<? super T, ? super R1, Boolean> filterFunction,
                                                     BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {


        return this.flatMapT(in->value1.apply(in)
                                       .filter(in2->filterFunction.apply(in,in2))
                                       .map(in2->yieldingFunction.apply(in,in2)));
    }

    public String mkString(){
        return toString();
    }


    @Override
    public <U> CompletableFutureT<W,U> ofType(Class<? extends U> type) {
        return (CompletableFutureT<W,U>)Filters.super.ofType(type);
    }

    @Override
    public CompletableFutureT<W,T> filterNot(Predicate<? super T> predicate) {
        return (CompletableFutureT<W,T>)Filters.super.filterNot(predicate);
    }

    @Override
    public CompletableFutureT<W,T> notNull() {
        return (CompletableFutureT<W,T>)Filters.super.notNull();
    }



  @Override
    public <U> CompletableFutureT<W,Tuple2<T, U>> zipWithPublisher(Publisher<? extends U> other) {
        return (CompletableFutureT)super.zipWithPublisher(other);
    }

    @Override
    public <S, U> CompletableFutureT<W,Tuple3<T, S, U>> zip3(Iterable<? extends S> second, Iterable<? extends U> third) {
        return (CompletableFutureT)super.zip3(second,third);
    }

    @Override
    public <S, U, R> CompletableFutureT<W,R> zip3(Iterable<? extends S> second, Iterable<? extends U> third, Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (CompletableFutureT<W,R>)super.zip3(second,third, fn3);
    }

    @Override
    public <T2, T3, T4> CompletableFutureT<W,Tuple4<T, T2, T3, T4>> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth) {
        return (CompletableFutureT)super.zip4(second,third,fourth);
    }

    @Override
    public <T2, T3, T4, R> CompletableFutureT<W,R> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth, Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (CompletableFutureT<W,R>)super.zip4(second,third,fourth,fn);
    }
}
