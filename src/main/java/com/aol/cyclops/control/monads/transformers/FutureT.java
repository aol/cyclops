package com.aol.cyclops.control.monads.transformers;

import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.reactivestreams.Publisher;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.types.Combiner;
import com.aol.cyclops.types.Filterable;
import com.aol.cyclops.types.Functor;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.To;
import com.aol.cyclops.types.Value;
import com.aol.cyclops.types.anyM.WitnessType;
import com.aol.cyclops.types.anyM.transformers.ValueTransformer;
import com.aol.cyclops.util.function.QuadFunction;
import com.aol.cyclops.util.function.TriFunction;

/**
* Monad Transformer for FutureW's nested within Sequential or non-scalar data types (e.g. Lists, Streams etc)

 * 
 * FutureWT allows the deeply wrapped FutureW to be manipulating within it's nested /contained context
 *
 * @author johnmcclean
 *
 * @param <T> Type of data stored inside the nested FutureW(s)
 */
public final class FutureT<W extends WitnessType,T> extends ValueTransformer<W,T> 
                                                    implements To<FutureT<W,T>>,
                                                               Functor<T>, 
                                                               Filterable<T> {

    private final AnyM<W,FutureW<T>> run;

    /**
     * @return The wrapped AnyM
     */
    @Override
    public AnyM<W,FutureW<T>> unwrap() {
        return run;
    }

    private FutureT(final AnyM<W,FutureW<T>> run) {
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
     * Peek at the current value of the FutureW
     * <pre>
     * {@code 
     *    FutureWT.of(AnyM.fromStream(Arrays.asFutureW(10))
     *             .peek(System.out::println);
     *             
     *     //prints 10        
     * }
     * </pre>
     * 
     * @param peek  Consumer to accept current value of FutureW
     * @return FutureWT with peek call
     */
    @Override
    public FutureT<W,T> peek(final Consumer<? super T> peek) {
        return of(run.peek(future -> future.map(a -> {
            peek.accept(a);
            return a;
        })));
    }

    /**
     * Map the wrapped FutureW
     * 
     * <pre>
     * {@code 
     *  FutureWT.of(AnyM.fromStream(Arrays.asFutureW(10))
     *             .map(t->t=t+1);
     *  
     *  
     *  //FutureWT<AnyMSeq<Stream<FutureW[11]>>>
     * }
     * </pre>
     * 
     * @param f Mapping function for the wrapped FutureW
     * @return FutureWT that applies the map function to the wrapped FutureW
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
     * Flat Map the wrapped FutureW
      * <pre>
     * {@code 
     *  FutureWT.of(AnyM.fromStream(Arrays.asFutureW(10))
     *             .flatMap(t->FutureW.completedFuture(20));
     *  
     *  
     *  //FutureWT<AnyMSeq<Stream<FutureW[20]>>>
     * }
     * </pre>
     * @param f FlatMap function
     * @return FutureWT that applies the flatMap function to the wrapped FutureW
     */

    public <B> FutureT<W,B> flatMapT(final Function<? super T, FutureT<W,B>> f) {
        return of(run.map(future -> future.flatMap(a -> f.apply(a).run.stream()
                                                                      .toList()
                                                                      .get(0))));
    }

    private static <W extends WitnessType,B> AnyM<W,FutureW<B>> narrow(final AnyM<W,FutureW<? extends B>> run) {
        return (AnyM) run;
    }

    @Override
    public <B> FutureT<W,B> flatMap(final Function<? super T, ? extends MonadicValue<? extends B>> f) {

        final AnyM<W,FutureW<? extends B>> mapped = run.map(o -> o.flatMap(f));
        return of(narrow(mapped));

    }

    /**
     * Lift a function into one that accepts and returns an FutureWT
     * This allows multiple monad types to add functionality to existing functions and methods
     * 
     * e.g. to add list handling  / iteration (via FutureW) and iteration (via Stream) to an existing function
     * <pre>
     * {@code 
        Function<Integer,Integer> add2 = i -> i+2;
    	Function<FutureWT<Integer>, FutureWT<Integer>> optTAdd2 = FutureWT.lift(add2);
    	
    	Stream<Integer> withNulls = Stream.of(1,2,3);
    	AnyMSeq<Integer> stream = AnyM.fromStream(withNulls);
    	AnyMSeq<FutureW<Integer>> streamOpt = stream.map(FutureW::completedFuture);
    	List<Integer> results = optTAdd2.apply(FutureWT.of(streamOpt))
    									.unwrap()
    									.<Stream<FutureW<Integer>>>unwrap()
    									.map(FutureW::join)
    									.collect(Collectors.toList());
    	
    	
    	//FutureW.completedFuture(List[3,4]);
     * 
     * 
     * }</pre>
     * 
     * 
     * @param fn Function to enhance with functionality from FutureW and another monad type
     * @return Function that accepts and returns an FutureWT
     */
    public static <W extends WitnessType,U, R> Function<FutureT<W,U>, FutureT<W,R>> lift(final Function<? super U, ? extends R> fn) {
        return optTu -> optTu.map(input -> fn.apply(input));
    }

    /**
     * Lift a BiFunction into one that accepts and returns  FutureWTs
     * This allows multiple monad types to add functionality to existing functions and methods
     * 
     * e.g. to add list handling / iteration (via FutureW), iteration (via Stream)  and asynchronous execution (FutureW) 
     * to an existing function
     * 
     * <pre>
     * {@code 
    	BiFunction<Integer,Integer,Integer> add = (a,b) -> a+b;
    	BiFunction<FutureWT<Integer>,FutureWT<Integer>,FutureWT<Integer>> optTAdd2 = FutureWT.lift2(add);
    	
    	Stream<Integer> withNulls = Stream.of(1,2,3);
    	AnyMSeq<Integer> stream = AnyM.ofMonad(withNulls);
    	AnyMSeq<FutureW<Integer>> streamOpt = stream.map(FutureW::completedFuture);
    	
    	FutureW<FutureW<Integer>> two = FutureW.completedFuture(FutureW.completedFuture(2));
    	AnyMSeq<FutureW<Integer>> future=  AnyM.fromFutureW(two);
    	List<Integer> results = optTAdd2.apply(FutureWT.of(streamOpt),FutureWT.of(future))
    									.unwrap()
    									.<Stream<FutureW<Integer>>>unwrap()
    									.map(FutureW::join)
    									.collect(Collectors.toList());
    									
    		//FutureW.completedFuture(List[3,4,5]);						
      }
      </pre>
     * @param fn BiFunction to enhance with functionality from FutureW and another monad type
     * @return Function that accepts and returns an FutureWT
     */
    public static <W extends WitnessType, U1,  U2, R> BiFunction<FutureT<W,U1>, FutureT<W,U2>, FutureT<W,R>> lift2(
            final BiFunction<? super U1, ? super U2, ? extends R> fn) {
        return (optTu1, optTu2) -> optTu1.flatMapT(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
    }

    /**
     * Construct an FutureWT from an AnyM that contains a monad type that contains type other than FutureW
     * The values in the underlying monad will be mapped to FutureW<A>
     * 
     * @param anyM AnyM that doesn't contain a monad wrapping an FutureW
     * @return FutureWT
     */
    public static <W extends WitnessType,A> FutureT<W,A> fromAnyM(final AnyM<W,A> anyM) {
        return of(anyM.map(FutureW::ofResult));
    }

    /**
     * Construct an FutureWT from an AnyM that wraps a monad containing  FutureWs
     * 
     * @param monads AnyM that contains a monad wrapping an FutureW
     * @return FutureWT
     */
    public static <W extends WitnessType,A> FutureT<W,A> of(final AnyM<W,FutureW<A>> monads) {
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
        return String.format("FutureTSeq[%s]", run);
    }

    


    public <R> FutureT<W,R> unitIterator(final Iterator<R> it) {
        return of(run.unitIterator(it)
                     .map(i -> FutureW.ofResult(i)));
    }

    @Override
    public <R> FutureT<W,R> unit(final R value) {
        return of(run.unit(FutureW.ofResult(value)));
    }

    @Override
    public <R> FutureT<W,R> empty() {
        return of(run.unit(FutureW.<R>empty()));
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
     * @see com.aol.cyclops.control.monads.transformers.values.ValueTransformer#combine(com.aol.cyclops.types.Value, java.util.function.BiFunction)
     */
    @Override
    public <T2, R> FutureT<W,R> combine(Value<? extends T2> app,
            BiFunction<? super T, ? super T2, ? extends R> fn) {
        return (FutureT<W,R>)super.combine(app, fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ValueTransformer#combine(java.util.function.BinaryOperator, com.aol.cyclops.types.Combiner)
     */
    @Override
    public FutureT<W, T> combine(BinaryOperator<Combiner<T>> combiner, Combiner<T> app) {
        
        return (FutureT<W, T>)super.combine(combiner, app);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ValueTransformer#unapply()
     */
    @Override
    public ListT<W,?> unapply() {
        
        return super.unapply();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ValueTransformer#iterate(java.util.function.UnaryOperator)
     */
    @Override
    public AnyM<W, ? extends ReactiveSeq<T>> iterate(UnaryOperator<T> fn) {
        
        return super.iterate(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ValueTransformer#generate()
     */
    @Override
    public AnyM<W, ? extends ReactiveSeq<T>> generate() {
        
        return super.generate();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ValueTransformer#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    public <T2, R> FutureT<W, R> zip(Iterable<? extends T2> iterable,
            BiFunction<? super T, ? super T2, ? extends R> fn) {
        
        return (FutureT<W, R>)super.zip(iterable, fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ValueTransformer#zip(java.util.function.BiFunction, org.reactivestreams.Publisher)
     */
    @Override
    public <T2, R> FutureT<W, R> zip(BiFunction<? super T, ? super T2, ? extends R> fn,
            Publisher<? extends T2> publisher) {
        
        return (FutureT<W, R>)super.zip(fn, publisher);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ValueTransformer#zip(java.util.stream.Stream)
     */
    @Override
    public <U> FutureT<W, Tuple2<T, U>> zip(Stream<? extends U> other) {
        
        return (FutureT)super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ValueTransformer#zip(org.jooq.lambda.Seq)
     */
    @Override
    public <U> FutureT<W, Tuple2<T, U>> zip(Seq<? extends U> other) {
        
        return (FutureT)super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ValueTransformer#zip(java.lang.Iterable)
     */
    @Override
    public <U> FutureT<W, Tuple2<T, U>> zip(Iterable<? extends U> other) {
        
        return (FutureT)super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ValueTransformer#and(java.util.function.Predicate)
     */
    @Override
    public FutureT<W, T> and(Predicate<? super T> other) {
        
        return (FutureT<W, T>)super.and(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ValueTransformer#negate()
     */
    @Override
    public FutureT<W, T> negate() {
        
        return (FutureT<W, T>)super.negate();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ValueTransformer#or(java.util.function.Predicate)
     */
    @Override
    public FutureT<W, T> or(Predicate<? super T> other) {
        
        return (FutureT<W, T>)super.or(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ValueTransformer#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops.util.function.TriFunction, com.aol.cyclops.util.function.QuadFunction)
     */
    @Override
    public <T2, R1, R2, R3, R> FutureT<W, R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            TriFunction<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
            QuadFunction<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return (FutureT<W, R>)super.forEach4(value1, value2, value3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ValueTransformer#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops.util.function.TriFunction, com.aol.cyclops.util.function.QuadFunction, com.aol.cyclops.util.function.QuadFunction)
     */
    @Override
    public <T2, R1, R2, R3, R> FutureT<W, R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            TriFunction<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
            QuadFunction<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
            QuadFunction<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return (FutureT<W, R>)super.forEach4(value1, value2, value3, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ValueTransformer#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops.util.function.TriFunction)
     */
    @Override
    public <T2, R1, R2, R> FutureT<W, R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            TriFunction<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        
        return (FutureT<W, R>)super.forEach3(value1, value2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ValueTransformer#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops.util.function.TriFunction, com.aol.cyclops.util.function.TriFunction)
     */
    @Override
    public <T2, R1, R2, R> FutureT<W, R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            TriFunction<? super T, ? super R1, ? super R2, Boolean> filterFunction,
            TriFunction<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        
        return (FutureT<W, R>)super.forEach3(value1, value2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ValueTransformer#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    public <R1, R> FutureT<W, R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        
        return (FutureT<W, R>)super.forEach2(value1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ValueTransformer#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    public <R1, R> FutureT<W, R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, Boolean> filterFunction,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        
        return (FutureT<W, R>)super.forEach2(value1, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ValueTransformer#combineEager(com.aol.cyclops.Monoid, com.aol.cyclops.types.MonadicValue)
     */
    @Override
    public FutureT<W, T> combineEager(Monoid<T> monoid, MonadicValue<? extends T> v2) {
        
        return (FutureT<W, T>)super.combineEager(monoid, v2);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ValueTransformer#flatMapIterable(java.util.function.Function)
     */
    @Override
    public <R> FutureT<W, R> flatMapIterable(Function<? super T, ? extends Iterable<? extends R>> mapper) {
        
        return (FutureT<W, R>)super.flatMapIterable(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ValueTransformer#flatMapPublisher(java.util.function.Function)
     */
    @Override
    public <R> FutureT<W, R> flatMapPublisher(Function<? super T, ? extends Publisher<? extends R>> mapper) {
        
        return (FutureT<W, R>)super.flatMapPublisher(mapper);
    }

   

    
  
}