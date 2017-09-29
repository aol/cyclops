package cyclops.monads.transformers;

import com.aol.cyclops2.types.Filters;
import com.aol.cyclops2.types.MonadicValue;
import com.aol.cyclops2.types.Value;
import com.aol.cyclops2.types.Zippable;
import com.aol.cyclops2.types.anyM.transformers.ValueTransformer;
import com.aol.cyclops2.types.foldable.To;
import com.aol.cyclops2.types.functor.Transformable;
import cyclops.control.Maybe;
import cyclops.control.Trampoline;
import cyclops.control.Xor;
import cyclops.function.Fn3;
import cyclops.function.Fn4;
import cyclops.monads.AnyM;
import cyclops.monads.WitnessType;
import cyclops.stream.ReactiveSeq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Publisher;

import java.util.Iterator;
import java.util.function.*;
import java.util.stream.Stream;

/**
* Monad Transformer for Xor's

 * 
 * MaybeT allows the deeply wrapped Maybe to be manipulating within it's nested /contained context
 *
 * @author johnmcclean
 *
 * @param <T> Type of data stored inside the nested Maybe(s)
 */
public final class XorT<W extends WitnessType<W>, ST,T> extends ValueTransformer<W, T> implements  To<XorT<W, ST,T>>,
                                                                                                    Transformable<T>,
                                                                                                     Filters<T> {

    private final AnyM<W,Xor<ST,T>> run;

    @Override
    public Iterator<T> iterator() {
        return stream().iterator();
    }

    @Override
    public ReactiveSeq<T> stream() {
        return run.stream().map(Xor::get);
    }



    /**
     * @return The wrapped AnyM
     */
    @Override
    public AnyM<W,Xor<ST,T>> unwrap() {
        return run;
    }

    public <R> R unwrapTo(Function<? super AnyM<W,Xor<ST,T>>, ? extends R> fn) {
        return unwrap().to(fn);
    }

    private XorT(final AnyM<W,Xor<ST,T>> run) {
        this.run = run;
    }

    
    @Override @Deprecated (/*DO NOT USE INTERNAL USE ONLY*/)
    protected <R> XorT<W,ST,R> unitAnyM(AnyM<W,? super MonadicValue<R>> traversable) {

        return of((AnyM) traversable);
    }

    @Override
    public AnyM<W,? extends MonadicValue<T>> transformerStream() {

        return run;
    }

    @Override
    public XorT<W,ST,T> filter(final Predicate<? super T> test) {
        return of(run.map(f->f.map(in->Tuple.tuple(in,test.test(in))))
                     .filter( f->f.get().v2 )
                     .map( f->f.map(in->in.v1)));
    }

    /**
     * Peek at the current value of the Maybe
     * <pre>
     * {@code 
     *    MaybeWT.of(AnyM.fromStream(Arrays.asMaybeW(10))
     *             .peek(System.out::println);
     *             
     *     //prints 10        
     * }
     * </pre>
     * 
     * @param peek  Consumer to accept current value of Maybe
     * @return MaybeWT with peek call
     */
    @Override
    public XorT<W,ST,T> peek(final Consumer<? super T> peek) {
        return map(e->{
            peek.accept(e);
            return e;
        });
    }

    /**
     * Map the wrapped Maybe
     * 
     * <pre>
     * {@code 
     *  MaybeWT.of(AnyM.fromStream(Arrays.asMaybeW(10))
     *             .map(t->t=t+1);
     *  
     *  
     *  //MaybeWT<AnyMSeq<Stream<Maybe[11]>>>
     * }
     * </pre>
     * 
     * @param f Mapping function for the wrapped Maybe
     * @return MaybeWT that applies the map function to the wrapped Maybe
     */
    @Override
    public <B> XorT<W,ST,B> map(final Function<? super T, ? extends B> f) {
        return new XorT<W,ST,B>(
                                  run.map(o -> o.map(f)));
    }

    /**
     * Flat Map the wrapped Maybe
      * <pre>
     * {@code 
     *  MaybeWT.of(AnyM.fromStream(Arrays.asMaybeW(10))
     *             .flatMap(t->Maybe.completedMaybe(20));
     *  
     *  
     *  //MaybeWT<AnyMSeq<Stream<Maybe[20]>>>
     * }
     * </pre>
     * @param f FlatMap function
     * @return MaybeWT that applies the flatMap function to the wrapped Maybe
     */

    public <B> XorT<W,ST,B> flatMapT(final Function<? super T, XorT<W,ST,B>> f) {
        return of(run.map(Maybe -> Maybe.flatMap(a -> f.apply(a).run.stream()
                                                                      .toList()
                                                                      .get(0))));
    }

    private static <W extends WitnessType<W>,ST,B> AnyM<W,Xor<ST,B>> narrow(final AnyM<W,Xor<ST,? extends B>> run) {
        return (AnyM) run;
    }

    @Override
    public <B> XorT<W,ST,B> flatMap(final Function<? super T, ? extends MonadicValue<? extends B>> f) {

        final AnyM<W,Xor<ST,? extends B>> mapped = run.map(o -> o.flatMap(f));
        return of(narrow(mapped));

    }

    /**
     * Lift a function into one that accepts and returns an MaybeWT
     * This allows multiple monad types to add functionality to existing function and methods
     * 
     * e.g. to add list handling  / iteration (via Maybe) and iteration (via Stream) to an existing function
     * <pre>
     * {@code 
        Function<Integer,Integer> add2 = i -> i+2;
    	Function<MaybeWT<Integer>, MaybeWT<Integer>> optTAdd2 = MaybeWT.lift(add2);
    	
    	LazyList<Integer> withNulls = LazyList.of(1,2,3);
    	AnyMSeq<Integer> reactiveStream = AnyM.fromStream(withNulls);
    	AnyMSeq<Maybe<Integer>> streamOpt = reactiveStream.map(Maybe::completedMaybe);
    	List<Integer> results = optTAdd2.applyHKT(MaybeWT.of(streamOpt))
    									.unwrap()
    									.<Stream<Maybe<Integer>>>unwrap()
    									.map(Maybe::join)
    									.collect(CyclopsCollectors.toList());
    	
    	
    	//Maybe.completedMaybe(List[3,4]);
     * 
     * 
     * }</pre>
     * 
     * 
     * @param fn Function to enhance with functionality from Maybe and another monad type
     * @return Function that accepts and returns an MaybeWT
     */
    public static <W extends WitnessType<W>,U,ST, R> Function<XorT<W,ST,U>, XorT<W,ST,R>> lift(final Function<? super U, ? extends R> fn) {
        return optTu -> optTu.map(input -> fn.apply(input));
    }

    /**
     * Lift a BiFunction into one that accepts and returns  MaybeWTs
     * This allows multiple monad types to add functionality to existing function and methods
     * 
     * e.g. to add list handling / iteration (via Maybe), iteration (via Stream)  and asynchronous execution (Maybe)
     * to an existing function
     * 
     * <pre>
     * {@code 
    	BiFunction<Integer,Integer,Integer> add = (a,b) -> a+b;
    	BiFunction<MaybeWT<Integer>,MaybeWT<Integer>,MaybeWT<Integer>> optTAdd2 = MaybeWT.lift2(add);
    	
    	LazyList<Integer> withNulls = LazyList.of(1,2,3);
    	AnyMSeq<Integer> reactiveStream = AnyM.ofMonad(withNulls);
    	AnyMSeq<Maybe<Integer>> streamOpt = reactiveStream.map(Maybe::completedMaybe);
    	
    	Maybe<Maybe<Integer>> two = Maybe.completedMaybe(Maybe.completedMaybe(2));
    	AnyMSeq<Maybe<Integer>> Maybe=  AnyM.fromMaybeW(two);
    	List<Integer> results = optTAdd2.applyHKT(MaybeWT.of(streamOpt),MaybeWT.of(Maybe))
    									.unwrap()
    									.<Stream<Maybe<Integer>>>unwrap()
    									.map(Maybe::join)
    									.collect(CyclopsCollectors.toList());
    									
    		//Maybe.completedMaybe(List[3,4,5]);
      }
      </pre>
     * @param fn BiFunction to enhance with functionality from Maybe and another monad type
     * @return Function that accepts and returns an MaybeWT
     */
    public static <W extends WitnessType<W>, ST,U1,  U2, R> BiFunction<XorT<W,ST,U1>, XorT<W,ST,U2>, XorT<W,ST,R>> lift2(
            final BiFunction<? super U1, ? super U2, ? extends R> fn) {
        return (optTu1, optTu2) -> optTu1.flatMapT(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
    }

    /**
     * Construct an MaybeWT from an AnyM that contains a monad type that contains type other than Maybe
     * The values in the underlying monad will be mapped to Maybe<A>
     * 
     * @param anyM AnyM that doesn't contain a monad wrapping an Maybe
     * @return MaybeWT
     */
    public static <W extends WitnessType<W>,ST,A> XorT<W,ST,A> fromAnyM(final AnyM<W,A> anyM) {
        return of(anyM.map(Xor::primary));
    }

    /**
     * Construct an MaybeWT from an AnyM that wraps a monad containing  MaybeWs
     * 
     * @param monads AnyM that contains a monad wrapping an Maybe
     * @return MaybeWT
     */
    public static <W extends WitnessType<W>,ST,A> XorT<W,ST,A> of(final AnyM<W,Xor<ST,A>> monads) {
        return new XorT<>(
                                 monads);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("XorT[%s]", run.unwrap().toString());
    }

    


    public <R> XorT<W,ST,R> unitIterator(final Iterator<R> it) {
        return of(run.unitIterator(it)
                     .map(i -> Xor.primary(i)));
    }

    @Override
    public <R> XorT<W,ST,R> unit(final R value) {
        return of(run.unit(Xor.primary(value)));
    }

    @Override
    public <R> XorT<W,ST,R> empty() {
        return of(run.unit(Xor.<ST,R>secondary(null)));
    }

    

   
    @Override
    public int hashCode() {
        return run.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof XorT) {
            return run.equals(((XorT) o).run);
        }
        return false;
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#combine(com.aol.cyclops2.types.Value, java.util.function.BiFunction)
     */
    @Override
    public <T2, R> XorT<W,ST,R> combine(Value<? extends T2> app,
                                     BiFunction<? super T, ? super T2, ? extends R> fn) {
        return (XorT<W,ST,R>)super.combine(app, fn);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#combine(java.util.function.BinaryOperator, com.aol.cyclops2.types.Combiner)
     */
    @Override
    public XorT<W, ST,T> zip(BinaryOperator<Zippable<T>> combiner, Zippable<T> app) {
        
        return (XorT<W, ST,T>)super.zip(combiner, app);
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
    public <T2, R> XorT<W,ST,R> zip(Iterable<? extends T2> iterable,
                                  BiFunction<? super T, ? super T2, ? extends R> fn) {
        
        return (XorT<W,ST,R>)super.zip(iterable, fn);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#zip(java.util.function.BiFunction, org.reactivestreams.Publisher)
     */
    @Override
    public <T2, R> XorT<W,ST,R> zipP(Publisher<? extends T2> publisher, BiFunction<? super T, ? super T2, ? extends R> fn) {
        
        return (XorT<W,ST,R>)super.zipP(publisher,fn);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#zip(java.util.reactiveStream.Stream)
     */
    @Override
    public <U> XorT<W,ST,Tuple2<T, U>> zipS(Stream<? extends U> other) {
        
        return (XorT)super.zipS(other);
    }


    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#zip(java.lang.Iterable)
     */
    @Override
    public <U> XorT<W,ST,Tuple2<T, U>> zip(Iterable<? extends U> other) {
        
        return (XorT)super.zip(other);
    }


    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    public <T2, R1, R2, R3, R> XorT<W,ST,R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
                                                   BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                                   Fn3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
                                                   Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return (XorT<W,ST,R>)super.forEach4(value1, value2, value3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    public <T2, R1, R2, R3, R> XorT<W,ST,R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
                                                   BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                                   Fn3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
                                                   Fn4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                   Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return (XorT<W,ST,R>)super.forEach4(value1, value2, value3, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    public <T2, R1, R2, R> XorT<W,ST,R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
                                               BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                               Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        
        return (XorT<W,ST,R>)super.forEach3(value1, value2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    public <T2, R1, R2, R> XorT<W,ST,R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
                                               BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                               Fn3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                                               Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        
        return (XorT<W,ST,R>)super.forEach3(value1, value2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    public <R1, R> XorT<W,ST,R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
                                       BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        
        return (XorT<W,ST,R>)super.forEach2(value1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    public <R1, R> XorT<W,ST,R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
                                       BiFunction<? super T, ? super R1, Boolean> filterFunction,
                                       BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        
        return (XorT<W,ST,R>)super.forEach2(value1, filterFunction, yieldingFunction);
    }



    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#flatMapI(java.util.function.Function)
     */
    @Override
    public <R> XorT<W,ST,R> flatMapIterable(Function<? super T, ? extends Iterable<? extends R>> mapper) {
        
        return (XorT<W,ST,R>)super.flatMapIterable(mapper);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#flatMapP(java.util.function.Function)
     */
    @Override
    public <R> XorT<W,ST,R> flatMapPublisher(Function<? super T, ? extends Publisher<? extends R>> mapper) {
        
        return (XorT<W,ST,R>)super.flatMapPublisher(mapper);
    }
    public <T2, R1, R2, R3, R> XorT<W,ST,R> forEach4M(Function<? super T, ? extends XorT<W,ST,R1>> value1,
                                                   BiFunction<? super T, ? super R1, ? extends XorT<W,ST,R2>> value2,
                                                   Fn3<? super T, ? super R1, ? super R2, ? extends XorT<W,ST,R3>> value3,
                                                   Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return this.flatMapT(in->value1.apply(in)
                .flatMapT(in2-> value2.apply(in,in2)
                        .flatMapT(in3->value3.apply(in,in2,in3)
                                .map(in4->yieldingFunction.apply(in,in2,in3,in4)))));

    }
    public <T2, R1, R2, R3, R> XorT<W,ST,R> forEach4M(Function<? super T, ? extends XorT<W,ST,R1>> value1,
                                                   BiFunction<? super T, ? super R1, ? extends XorT<W,ST,R2>> value2,
                                                   Fn3<? super T, ? super R1, ? super R2, ? extends XorT<W,ST,R3>> value3,
                                                   Fn4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                   Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return this.flatMapT(in->value1.apply(in)
                    .flatMapT(in2-> value2.apply(in,in2)
                            .flatMapT(in3->value3.apply(in,in2,in3)
                                                 .filter(in4->filterFunction.apply(in,in2,in3,in4))
                                                 .map(in4->yieldingFunction.apply(in,in2,in3,in4)))));

    }

    public <T2, R1, R2, R> XorT<W,ST,R> forEach3M(Function<? super T, ? extends XorT<W,ST,R1>> value1,
                                               BiFunction<? super T, ? super R1, ? extends XorT<W,ST,R2>> value2,
                                               Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return this.flatMapT(in->value1.apply(in).flatMapT(in2-> value2.apply(in,in2)
                                                 .map(in3->yieldingFunction.apply(in,in2,in3))));

    }

    public <T2, R1, R2, R> XorT<W,ST,R> forEach3M(Function<? super T, ? extends XorT<W,ST,R1>> value1,
                                               BiFunction<? super T, ? super R1, ? extends XorT<W,ST,R2>> value2,
                                               Fn3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                                               Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return this.flatMapT(in->value1.apply(in).flatMapT(in2-> value2.apply(in,in2).filter(in3->filterFunction.apply(in,in2,in3))
                                                                                     .map(in3->yieldingFunction.apply(in,in2,in3))));

    }
    public <R1, R> XorT<W,ST,R> forEach2M(Function<? super T, ? extends XorT<W,ST,R1>> value1,
                                       BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {


        return this.flatMapT(in->value1.apply(in)
                    .map(in2->yieldingFunction.apply(in,in2)));
    }

    public <R1, R> XorT<W,ST,R> forEach2M(Function<? super T, ? extends XorT<W,ST,R1>> value1,
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
    public <U> XorT<W,ST,U> cast(Class<? extends U> type) {
        return (XorT<W,ST,U>)super.cast(type);
    }

    @Override
    public <U> XorT<W,ST,U> ofType(Class<? extends U> type) {
        return (XorT<W,ST,U>)Filters.super.ofType(type);
    }

    @Override
    public XorT<W,ST,T> filterNot(Predicate<? super T> predicate) {
        return (XorT<W,ST,T>)Filters.super.filterNot(predicate);
    }

    @Override
    public XorT<W,ST,T> notNull() {
        return (XorT<W,ST,T>)Filters.super.notNull();
    }

    @Override
    public <R> XorT<W,ST,R> zipWith(Iterable<Function<? super T, ? extends R>> fn) {
        return (XorT<W,ST,R>)super.zipWith(fn);
    }

    @Override
    public <R> XorT<W,ST,R> zipWithS(Stream<Function<? super T, ? extends R>> fn) {
        return (XorT<W,ST,R>)super.zipWithS(fn);
    }

    @Override
    public <R> XorT<W,ST,R> zipWithP(Publisher<Function<? super T, ? extends R>> fn) {
        return (XorT<W,ST,R>)super.zipWithP(fn);
    }

    @Override
    public <R> XorT<W,ST,R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (XorT<W,ST,R>)super.trampoline(mapper);
    }

    @Override
    public <U, R> XorT<W,ST,R> zipS(Stream<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return (XorT<W,ST,R>)super.zipS(other,zipper);
    }

    @Override
    public <U> XorT<W,ST,Tuple2<T, U>> zipP(Publisher<? extends U> other) {
        return (XorT)super.zipP(other);
    }

    @Override
    public <S, U> XorT<W,ST,Tuple3<T, S, U>> zip3(Iterable<? extends S> second, Iterable<? extends U> third) {
        return (XorT)super.zip3(second,third);
    }

    @Override
    public <S, U, R> XorT<W,ST,R> zip3(Iterable<? extends S> second, Iterable<? extends U> third, Fn3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (XorT<W,ST,R>)super.zip3(second,third, fn3);
    }

    @Override
    public <T2, T3, T4> XorT<W,ST,Tuple4<T, T2, T3, T4>> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth) {
        return (XorT)super.zip4(second,third,fourth);
    }

    @Override
    public <T2, T3, T4, R> XorT<W,ST,R> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth, Fn4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (XorT<W,ST,R>)super.zip4(second,third,fourth,fn);
    }
}