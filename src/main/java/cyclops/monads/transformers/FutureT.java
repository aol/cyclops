package cyclops.monads.transformers;

import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import com.aol.cyclops2.types.foldable.To;
import com.aol.cyclops2.types.functor.Transformable;
import cyclops.async.Future;
import cyclops.control.Trampoline;
import com.aol.cyclops2.types.*;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Publisher;

import cyclops.monads.AnyM;
import cyclops.stream.ReactiveSeq;
import cyclops.monads.WitnessType;
import com.aol.cyclops2.types.anyM.transformers.ValueTransformer;
import cyclops.function.Fn4;
import cyclops.function.Fn3;

/**
* Monad Transformer for Future's nest within another monadic type

 * 
 * FutureT allows the deeply wrapped Future to be manipulating within it's nest /contained context
 *
 * @author johnmcclean
 *
 * @param <T> Type of data stored inside the nest Future(s)
 */
public final class FutureT<W extends WitnessType<W>,T> extends ValueTransformer<W,T> 
                                                       implements To<FutureT<W,T>>,
        Transformable<T>,
        Filters<T> {

    private final AnyM<W,Future<T>> run;

    @Override
    public Iterator<T> iterator() {
        return stream().iterator();
    }

                                                                         @Override
    public ReactiveSeq<T> stream() {
        return run.stream().map(Future::get);
    }



    /**
     * @return The wrapped AnyM
     */
    @Override
    public AnyM<W,Future<T>> unwrap() {
        return run;
    }

    public <R> R unwrapTo(Function<? super AnyM<W,Future<T>>, ? extends R> fn) {
        return unwrap().to(fn);
    }

    private FutureT(final AnyM<W,Future<T>> run) {
        this.run = run;
    }

    
    @Override @Deprecated (/*DO NOT USE INTERNAL USE ONLY*/)
    protected <R> FutureT<W,R> unitAnyM(AnyM<W,? super MonadicValue<R>> traversable) {

        return of((AnyM) traversable);
    }

    @Override
    public AnyM<W,? extends MonadicValue<T>> transformerStream() {

        return run;
    }

    @Override
    public FutureT<W,T> filter(final Predicate<? super T> test) {
        return of(run.map(f->f.map(in->Tuple.tuple(in,test.test(in))))
                     .filter( f->f.get().v2 )
                     .map( f->f.map(in->in.v1)));
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
    public FutureT<W,T> peek(final Consumer<? super T> peek) {
        return of(run.peek(future -> future.map(a -> {
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
     * @return FutureT that applies the map function to the wrapped Future
     */
    @Override
    public <B> FutureT<W,B> map(final Function<? super T, ? extends B> f) {
        return new FutureT<W,B>(
                                  run.map(o -> o.map(f)));
    }
    public <B> FutureT<W,B> map(final Function<? super T, ? extends B> f, Executor ex) {
        return new FutureT<W,B>(
                                  run.map(o -> o.map(f,ex)));
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

    public <B> FutureT<W,B> flatMapT(final Function<? super T, FutureT<W,B>> f) {
        return of(run.map(future -> future.flatMap(a -> f.apply(a).run.stream()
                                                                      .toList()
                                                                      .get(0))));
    }

    private static <W extends WitnessType<W>,B> AnyM<W,Future<B>> narrow(final AnyM<W,Future<? extends B>> run) {
        return (AnyM) run;
    }

    @Override
    public <B> FutureT<W,B> flatMap(final Function<? super T, ? extends MonadicValue<? extends B>> f) {

        final AnyM<W,Future<? extends B>> mapped = run.map(o -> o.flatMap(f));
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
    	AnyMSeq<Integer> reactiveStream = AnyM.fromStream(withNulls);
    	AnyMSeq<Future<Integer>> streamOpt = reactiveStream.map(Future::completedFuture);
    	List<Integer> results = optTAdd2.applyHKT(FutureT.of(streamOpt))
    									.unwrap()
    									.<Stream<Future<Integer>>>unwrap()
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
    public static <W extends WitnessType<W>,U, R> Function<FutureT<W,U>, FutureT<W,R>> lift(final Function<? super U, ? extends R> fn) {
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
    	AnyMSeq<Integer> reactiveStream = AnyM.ofMonad(withNulls);
    	AnyMSeq<Future<Integer>> streamOpt = reactiveStream.map(Future::completedFuture);
    	
    	Future<Future<Integer>> two = Future.completedFuture(Future.completedFuture(2));
    	AnyMSeq<Future<Integer>> future=  AnyM.fromFuture(two);
    	List<Integer> results = optTAdd2.applyHKT(FutureT.of(streamOpt),FutureT.of(future))
    									.unwrap()
    									.<Stream<Future<Integer>>>unwrap()
    									.map(Future::join)
    									.collect(CyclopsCollectors.toList());
    									
    		//Future.completedFuture(List[3,4,5]);
      }
      </pre>
     * @param fn BiFunction to enhance with functionality from Future and another monad type
     * @return Function that accepts and returns an FutureT
     */
    public static <W extends WitnessType<W>, U1,  U2, R> BiFunction<FutureT<W,U1>, FutureT<W,U2>, FutureT<W,R>> lift2(
            final BiFunction<? super U1, ? super U2, ? extends R> fn) {
        return (optTu1, optTu2) -> optTu1.flatMapT(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
    }

    /**
     * Construct an FutureT from an AnyM that contains a monad type that contains type other than Future
     * The values in the underlying monad will be mapped to Future<A>
     * 
     * @param anyM AnyM that doesn't contain a monad wrapping an Future
     * @return FutureT
     */
    public static <W extends WitnessType<W>,A> FutureT<W,A> fromAnyM(final AnyM<W,A> anyM) {
        return of(anyM.map(Future::ofResult));
    }

    /**
     * Construct an FutureT from an AnyM that wraps a monad containing  Futures
     * 
     * @param monads AnyM that contains a monad wrapping an Future
     * @return FutureT
     */
    public static <W extends WitnessType<W>,A> FutureT<W,A> of(final AnyM<W,Future<A>> monads) {
        return new FutureT<>(
                                 monads);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("FutureT[%s]", run.unwrap().toString());
    }

    


    public <R> FutureT<W,R> unitIterator(final Iterator<R> it) {
        return of(run.unitIterator(it)
                     .map(i -> Future.ofResult(i)));
    }

    @Override
    public <R> FutureT<W,R> unit(final R value) {
        return of(run.unit(Future.ofResult(value)));
    }

    @Override
    public <R> FutureT<W,R> empty() {
        return of(run.unit(Future.<R>empty()));
    }

    

   
    @Override
    public int hashCode() {
        return run.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof FutureT) {
            return run.equals(((FutureT) o).run);
        }
        return false;
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#combine(com.aol.cyclops2.types.Value, java.util.function.BiFunction)
     */
    @Override
    public <T2, R> FutureT<W,R> combine(Value<? extends T2> app,
            BiFunction<? super T, ? super T2, ? extends R> fn) {
        return (FutureT<W,R>)super.combine(app, fn);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#combine(java.util.function.BinaryOperator, com.aol.cyclops2.types.Combiner)
     */
    @Override
    public FutureT<W, T> zip(BinaryOperator<Zippable<T>> combiner, Zippable<T> app) {
        
        return (FutureT<W, T>)super.zip(combiner, app);
    }



    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#iterate(java.util.function.UnaryOperator)
     */
    @Override
    public AnyM<W, ? extends ReactiveSeq<T>> iterate(UnaryOperator<T> fn) {
        
        return super.iterate(fn);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#generate()
     */
    @Override
    public AnyM<W, ? extends ReactiveSeq<T>> generate() {
        
        return super.generate();
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    public <T2, R> FutureT<W, R> zip(Iterable<? extends T2> iterable,
            BiFunction<? super T, ? super T2, ? extends R> fn) {
        
        return (FutureT<W, R>)super.zip(iterable, fn);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#zip(java.util.function.BiFunction, org.reactivestreams.Publisher)
     */
    @Override
    public <T2, R> FutureT<W, R> zipP(Publisher<? extends T2> publisher,BiFunction<? super T, ? super T2, ? extends R> fn) {
        
        return (FutureT<W, R>)super.zipP(publisher,fn);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#zip(java.util.stream.Stream)
     */
    @Override
    public <U> FutureT<W, Tuple2<T, U>> zipS(Stream<? extends U> other) {
        
        return (FutureT)super.zipS(other);
    }


    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#zip(java.lang.Iterable)
     */
    @Override
    public <U> FutureT<W, Tuple2<T, U>> zip(Iterable<? extends U> other) {
        
        return (FutureT)super.zip(other);
    }


    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    public <T2, R1, R2, R3, R> FutureT<W, R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            Fn3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
            Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return (FutureT<W, R>)super.forEach4(value1, value2, value3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    public <T2, R1, R2, R3, R> FutureT<W, R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            Fn3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
            Fn4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
            Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return (FutureT<W, R>)super.forEach4(value1, value2, value3, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    public <T2, R1, R2, R> FutureT<W, R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        
        return (FutureT<W, R>)super.forEach3(value1, value2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    public <T2, R1, R2, R> FutureT<W, R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            Fn3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
            Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        
        return (FutureT<W, R>)super.forEach3(value1, value2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    public <R1, R> FutureT<W, R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        
        return (FutureT<W, R>)super.forEach2(value1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    public <R1, R> FutureT<W, R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, Boolean> filterFunction,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        
        return (FutureT<W, R>)super.forEach2(value1, filterFunction, yieldingFunction);
    }



    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#flatMapI(java.util.function.Function)
     */
    @Override
    public <R> FutureT<W, R> flatMapIterable(Function<? super T, ? extends Iterable<? extends R>> mapper) {
        
        return (FutureT<W, R>)super.flatMapIterable(mapper);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#flatMapP(java.util.function.Function)
     */
    @Override
    public <R> FutureT<W, R> flatMapPublisher(Function<? super T, ? extends Publisher<? extends R>> mapper) {
        
        return (FutureT<W, R>)super.flatMapPublisher(mapper);
    }
    public <T2, R1, R2, R3, R> FutureT<W,R> forEach4M(Function<? super T, ? extends FutureT<W,R1>> value1,
                                                      BiFunction<? super T, ? super R1, ? extends FutureT<W,R2>> value2,
                                                      Fn3<? super T, ? super R1, ? super R2, ? extends FutureT<W,R3>> value3,
                                                      Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return this.flatMapT(in->value1.apply(in)
                .flatMapT(in2-> value2.apply(in,in2)
                        .flatMapT(in3->value3.apply(in,in2,in3)
                                .map(in4->yieldingFunction.apply(in,in2,in3,in4)))));

    }
    public <T2, R1, R2, R3, R> FutureT<W,R> forEach4M(Function<? super T, ? extends FutureT<W,R1>> value1,
                                                              BiFunction<? super T, ? super R1, ? extends FutureT<W,R2>> value2,
                                                              Fn3<? super T, ? super R1, ? super R2, ? extends FutureT<W,R3>> value3,
                                                              Fn4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                              Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return this.flatMapT(in->value1.apply(in)
                    .flatMapT(in2-> value2.apply(in,in2)
                            .flatMapT(in3->value3.apply(in,in2,in3)
                                                 .filter(in4->filterFunction.apply(in,in2,in3,in4))
                                                 .map(in4->yieldingFunction.apply(in,in2,in3,in4)))));

    }

    public <T2, R1, R2, R> FutureT<W,R> forEach3M(Function<? super T, ? extends FutureT<W,R1>> value1,
                                                          BiFunction<? super T, ? super R1, ? extends FutureT<W,R2>> value2,
                                                          Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return this.flatMapT(in->value1.apply(in).flatMapT(in2-> value2.apply(in,in2)
                                                 .map(in3->yieldingFunction.apply(in,in2,in3))));

    }

    public <T2, R1, R2, R> FutureT<W,R> forEach3M(Function<? super T, ? extends FutureT<W,R1>> value1,
                                                          BiFunction<? super T, ? super R1, ? extends FutureT<W,R2>> value2,
                                                          Fn3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                                                          Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return this.flatMapT(in->value1.apply(in).flatMapT(in2-> value2.apply(in,in2).filter(in3->filterFunction.apply(in,in2,in3))
                                                                                     .map(in3->yieldingFunction.apply(in,in2,in3))));

    }
    public <R1, R> FutureT<W,R> forEach2M(Function<? super T, ? extends FutureT<W,R1>> value1,
                                                   BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {


        return this.flatMapT(in->value1.apply(in)
                    .map(in2->yieldingFunction.apply(in,in2)));
    }

    public <R1, R> FutureT<W,R> forEach2M(Function<? super T, ? extends FutureT<W,R1>> value1,
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
    public <U> FutureT<W,U> cast(Class<? extends U> type) {
        return (FutureT<W,U>)super.cast(type);
    }

    @Override
    public <U> FutureT<W,U> ofType(Class<? extends U> type) {
        return (FutureT<W,U>)Filters.super.ofType(type);
    }

    @Override
    public FutureT<W,T> filterNot(Predicate<? super T> predicate) {
        return (FutureT<W,T>)Filters.super.filterNot(predicate);
    }

    @Override
    public FutureT<W,T> notNull() {
        return (FutureT<W,T>)Filters.super.notNull();
    }

    @Override
    public <R> FutureT<W,R> zipWith(Iterable<Function<? super T, ? extends R>> fn) {
        return (FutureT<W,R>)super.zipWith(fn);
    }

    @Override
    public <R> FutureT<W,R> zipWithS(Stream<Function<? super T, ? extends R>> fn) {
        return (FutureT<W,R>)super.zipWithS(fn);
    }

    @Override
    public <R> FutureT<W,R> zipWithP(Publisher<Function<? super T, ? extends R>> fn) {
        return (FutureT<W,R>)super.zipWithP(fn);
    }

    @Override
    public <R> FutureT<W,R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (FutureT<W,R>)super.trampoline(mapper);
    }

    @Override
    public <U, R> FutureT<W,R> zipS(Stream<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return (FutureT<W,R>)super.zipS(other,zipper);
    }

    @Override
    public <U> FutureT<W,Tuple2<T, U>> zipP(Publisher<? extends U> other) {
        return (FutureT)super.zipP(other);
    }

    @Override
    public <S, U> FutureT<W,Tuple3<T, S, U>> zip3(Iterable<? extends S> second, Iterable<? extends U> third) {
        return (FutureT)super.zip3(second,third);
    }

    @Override
    public <S, U, R> FutureT<W,R> zip3(Iterable<? extends S> second, Iterable<? extends U> third, Fn3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (FutureT<W,R>)super.zip3(second,third, fn3);
    }

    @Override
    public <T2, T3, T4> FutureT<W,Tuple4<T, T2, T3, T4>> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth) {
        return (FutureT)super.zip4(second,third,fourth);
    }

    @Override
    public <T2, T3, T4, R> FutureT<W,R> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth, Fn4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (FutureT<W,R>)super.zip4(second,third,fourth,fn);
    }
}