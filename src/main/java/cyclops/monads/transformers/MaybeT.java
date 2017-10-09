package cyclops.monads.transformers;

import com.aol.cyclops2.types.*;
import com.aol.cyclops2.types.anyM.transformers.ValueTransformer;
import com.aol.cyclops2.types.foldable.To;
import com.aol.cyclops2.types.functor.Transformable;
import cyclops.control.Maybe;
import cyclops.control.Trampoline;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.monads.AnyM;
import cyclops.monads.WitnessType;
import cyclops.stream.ReactiveSeq;
import cyclops.collections.tuple.Tuple;
import cyclops.collections.tuple.Tuple2;
import cyclops.collections.tuple.Tuple3;
import cyclops.collections.tuple.Tuple4;
import org.reactivestreams.Publisher;

import java.util.Iterator;
import java.util.function.*;
import java.util.stream.Stream;

/**
* Monad Transformer for Maybe's

 * 
 * MaybeT allows the deeply wrapped Maybe to be manipulating within it's nested /contained context
 *
 * @author johnmcclean
 *
 * @param <T> Type of data stored inside the nested Maybe(s)
 */
//@TODO should be OptionT
public final class MaybeT<W extends WitnessType<W>,T> extends ValueTransformer<W,T> 
                                                       implements To<MaybeT<W,T>>,
        Transformable<T>,
        Filters<T> {

    private final AnyM<W,Maybe<T>> run;

    @Override
    public Iterator<T> iterator() {
        return stream().iterator();
    }

                                                                         @Override
    public ReactiveSeq<T> stream() {
        return run.stream().flatMap(Maybe::stream);
    }



    /**
     * @return The wrapped AnyM
     */
    @Override
    public AnyM<W,Maybe<T>> unwrap() {
        return run;
    }

    public <R> R unwrapTo(Function<? super AnyM<W,Maybe<T>>, ? extends R> fn) {
        return unwrap().to(fn);
    }

    private MaybeT(final AnyM<W,Maybe<T>> run) {
        this.run = run;
    }

    
    @Override @Deprecated (/*DO NOT USE INTERNAL USE ONLY*/)
    protected <R> MaybeT<W,R> unitAnyM(AnyM<W,? super MonadicValue<R>> traversable) {

        return of((AnyM) traversable);
    }

    @Override
    public AnyM<W,? extends MonadicValue<T>> transformerStream() {

        return run;
    }

    @Override
    public MaybeT<W,T> filter(final Predicate<? super T> test) {
        return of(run.map(f->f.map(in->Tuple.tuple(in,test.test(in))))
                     .filter( f->f.visit(t->t._2(),()->false) )
                     .map( f->f.map(in->in._1())));
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
    public MaybeT<W,T> peek(final Consumer<? super T> peek) {
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
     *             .transform(t->t=t+1);
     *  
     *  
     *  //MaybeWT<AnyMSeq<Stream<Maybe[11]>>>
     * }
     * </pre>
     * 
     * @param f Mapping function for the wrapped Maybe
     * @return MaybeWT that applies the transform function to the wrapped Maybe
     */
    @Override
    public <B> MaybeT<W,B> map(final Function<? super T, ? extends B> f) {
        return new MaybeT<W,B>(
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

    public <B> MaybeT<W,B> flatMapT(final Function<? super T, MaybeT<W,B>> f) {
        return of(run.map(Maybe -> Maybe.flatMap(a -> f.apply(a).run.stream()
                                                                      .toList()
                                                                      .get(0))));
    }

    private static <W extends WitnessType<W>,B> AnyM<W,Maybe<B>> narrow(final AnyM<W,Maybe<? extends B>> run) {
        return (AnyM) run;
    }


    public <B> MaybeT<W,B> flatMap(final Function<? super T, ? extends Maybe<? extends B>> f) {

        final AnyM<W,Maybe<? extends B>> mapped = run.map(o -> o.flatMap(f));
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
    	
    	Stream<Integer> withNulls = Stream.of(1,2,3);
    	AnyMSeq<Integer> reactiveStream = AnyM.fromStream(withNulls);
    	AnyMSeq<Maybe<Integer>> streamOpt = reactiveStream.transform(Maybe::completedMaybe);
    	List<Integer> results = optTAdd2.applyHKT(MaybeWT.of(streamOpt))
    									.unwrap()
    									.<Stream<Maybe<Integer>>>unwrap()
    									.transform(Maybe::join)
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
    public static <W extends WitnessType<W>,U, R> Function<MaybeT<W,U>, MaybeT<W,R>> lift(final Function<? super U, ? extends R> fn) {
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
    	
    	Stream<Integer> withNulls = Stream.of(1,2,3);
    	AnyMSeq<Integer> reactiveStream = AnyM.ofMonad(withNulls);
    	AnyMSeq<Maybe<Integer>> streamOpt = reactiveStream.transform(Maybe::completedMaybe);
    	
    	Maybe<Maybe<Integer>> two = Maybe.completedMaybe(Maybe.completedMaybe(2));
    	AnyMSeq<Maybe<Integer>> Maybe=  AnyM.fromMaybeW(two);
    	List<Integer> results = optTAdd2.applyHKT(MaybeWT.of(streamOpt),MaybeWT.of(Maybe))
    									.unwrap()
    									.<Stream<Maybe<Integer>>>unwrap()
    									.transform(Maybe::join)
    									.collect(CyclopsCollectors.toList());
    									
    		//Maybe.completedMaybe(List[3,4,5]);
      }
      </pre>
     * @param fn BiFunction to enhance with functionality from Maybe and another monad type
     * @return Function that accepts and returns an MaybeWT
     */
    public static <W extends WitnessType<W>, U1,  U2, R> BiFunction<MaybeT<W,U1>, MaybeT<W,U2>, MaybeT<W,R>> lift2(
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
    public static <W extends WitnessType<W>,A> MaybeT<W,A> fromAnyM(final AnyM<W,A> anyM) {
        return of(anyM.map(Maybe::ofNullable));
    }

    /**
     * Construct an MaybeWT from an AnyM that wraps a monad containing  MaybeWs
     * 
     * @param monads AnyM that contains a monad wrapping an Maybe
     * @return MaybeWT
     */
    public static <W extends WitnessType<W>,A> MaybeT<W,A> of(final AnyM<W,Maybe<A>> monads) {
        return new MaybeT<>(
                                 monads);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("MaybeT[%s]", run.unwrap().toString());
    }

    


    public <R> MaybeT<W,R> unitIterator(final Iterator<R> it) {
        return of(run.unitIterator(it)
                     .map(i -> Maybe.ofNullable(i)));
    }

    @Override
    public <R> MaybeT<W,R> unit(final R value) {
        return of(run.unit(Maybe.ofNullable(value)));
    }

    @Override
    public <R> MaybeT<W,R> empty() {
        return of(run.unit(Maybe.<R>none()));
    }

    

   
    @Override
    public int hashCode() {
        return run.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof MaybeT) {
            return run.equals(((MaybeT) o).run);
        }
        return false;
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#combine(com.aol.cyclops2.types.Value, java.util.function.BiFunction)
     */
    @Override
    public <T2, R> MaybeT<W,R> combine(Value<? extends T2> app,
                                       BiFunction<? super T, ? super T2, ? extends R> fn) {
        return (MaybeT<W,R>)super.combine(app, fn);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#combine(java.util.function.BinaryOperator, com.aol.cyclops2.types.Combiner)
     */
    @Override
    public MaybeT<W, T> zip(BinaryOperator<Zippable<T>> combiner, Zippable<T> app) {
        
        return (MaybeT<W, T>)super.zip(combiner, app);
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
    public <T2, R> MaybeT<W, R> zip(Iterable<? extends T2> iterable,
                                    BiFunction<? super T, ? super T2, ? extends R> fn) {
        
        return (MaybeT<W, R>)super.zip(iterable, fn);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#zip(java.util.function.BiFunction, org.reactivestreams.Publisher)
     */
    @Override
    public <T2, R> MaybeT<W, R> zipP(Publisher<? extends T2> publisher, BiFunction<? super T, ? super T2, ? extends R> fn) {
        
        return (MaybeT<W, R>)super.zipP(publisher,fn);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#zip(java.util.stream.Stream)
     */
    @Override
    public <U> MaybeT<W, Tuple2<T, U>> zipS(Stream<? extends U> other) {
        
        return (MaybeT)super.zipS(other);
    }


    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#zip(java.lang.Iterable)
     */
    @Override
    public <U> MaybeT<W, Tuple2<T, U>> zip(Iterable<? extends U> other) {
        
        return (MaybeT)super.zip(other);
    }


    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    public <T2, R1, R2, R3, R> MaybeT<W, R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
                                                     BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                                     Function3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
                                                     Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return (MaybeT<W, R>)super.forEach4(value1, value2, value3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    public <T2, R1, R2, R3, R> MaybeT<W, R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
                                                     BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                                     Function3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
                                                     Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                     Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return (MaybeT<W, R>)super.forEach4(value1, value2, value3, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    public <T2, R1, R2, R> MaybeT<W, R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
                                                 BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                                 Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        
        return (MaybeT<W, R>)super.forEach3(value1, value2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    public <T2, R1, R2, R> MaybeT<W, R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
                                                 BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                                 Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                                                 Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        
        return (MaybeT<W, R>)super.forEach3(value1, value2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    public <R1, R> MaybeT<W, R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
                                         BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        
        return (MaybeT<W, R>)super.forEach2(value1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    public <R1, R> MaybeT<W, R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
                                         BiFunction<? super T, ? super R1, Boolean> filterFunction,
                                         BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        
        return (MaybeT<W, R>)super.forEach2(value1, filterFunction, yieldingFunction);
    }



    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#flatMapI(java.util.function.Function)
     */
    @Override
    public <R> MaybeT<W, R> flatMapIterable(Function<? super T, ? extends Iterable<? extends R>> mapper) {
        
        return (MaybeT<W, R>)super.flatMapIterable(mapper);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#flatMapP(java.util.function.Function)
     */
    @Override
    public <R> MaybeT<W, R> flatMapPublisher(Function<? super T, ? extends Publisher<? extends R>> mapper) {
        
        return (MaybeT<W, R>)super.flatMapPublisher(mapper);
    }
    public <T2, R1, R2, R3, R> MaybeT<W,R> forEach4M(Function<? super T, ? extends MaybeT<W,R1>> value1,
                                                     BiFunction<? super T, ? super R1, ? extends MaybeT<W,R2>> value2,
                                                     Function3<? super T, ? super R1, ? super R2, ? extends MaybeT<W,R3>> value3,
                                                     Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return this.flatMapT(in->value1.apply(in)
                .flatMapT(in2-> value2.apply(in,in2)
                        .flatMapT(in3->value3.apply(in,in2,in3)
                                .map(in4->yieldingFunction.apply(in,in2,in3,in4)))));

    }
    public <T2, R1, R2, R3, R> MaybeT<W,R> forEach4M(Function<? super T, ? extends MaybeT<W,R1>> value1,
                                                     BiFunction<? super T, ? super R1, ? extends MaybeT<W,R2>> value2,
                                                     Function3<? super T, ? super R1, ? super R2, ? extends MaybeT<W,R3>> value3,
                                                     Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                     Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return this.flatMapT(in->value1.apply(in)
                    .flatMapT(in2-> value2.apply(in,in2)
                            .flatMapT(in3->value3.apply(in,in2,in3)
                                                 .filter(in4->filterFunction.apply(in,in2,in3,in4))
                                                 .map(in4->yieldingFunction.apply(in,in2,in3,in4)))));

    }

    public <T2, R1, R2, R> MaybeT<W,R> forEach3M(Function<? super T, ? extends MaybeT<W,R1>> value1,
                                                 BiFunction<? super T, ? super R1, ? extends MaybeT<W,R2>> value2,
                                                 Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return this.flatMapT(in->value1.apply(in).flatMapT(in2-> value2.apply(in,in2)
                                                 .map(in3->yieldingFunction.apply(in,in2,in3))));

    }

    public <T2, R1, R2, R> MaybeT<W,R> forEach3M(Function<? super T, ? extends MaybeT<W,R1>> value1,
                                                 BiFunction<? super T, ? super R1, ? extends MaybeT<W,R2>> value2,
                                                 Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                                                 Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return this.flatMapT(in->value1.apply(in).flatMapT(in2-> value2.apply(in,in2).filter(in3->filterFunction.apply(in,in2,in3))
                                                                                     .map(in3->yieldingFunction.apply(in,in2,in3))));

    }
    public <R1, R> MaybeT<W,R> forEach2M(Function<? super T, ? extends MaybeT<W,R1>> value1,
                                         BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {


        return this.flatMapT(in->value1.apply(in)
                    .map(in2->yieldingFunction.apply(in,in2)));
    }

    public <R1, R> MaybeT<W,R> forEach2M(Function<? super T, ? extends MaybeT<W,R1>> value1,
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
    public <U> MaybeT<W,U> cast(Class<? extends U> type) {
        return (MaybeT<W,U>)super.cast(type);
    }

    @Override
    public <U> MaybeT<W,U> ofType(Class<? extends U> type) {
        return (MaybeT<W,U>)Filters.super.ofType(type);
    }

    @Override
    public MaybeT<W,T> filterNot(Predicate<? super T> predicate) {
        return (MaybeT<W,T>)Filters.super.filterNot(predicate);
    }

    @Override
    public MaybeT<W,T> notNull() {
        return (MaybeT<W,T>)Filters.super.notNull();
    }

    @Override
    public <R> MaybeT<W,R> zipWith(Iterable<Function<? super T, ? extends R>> fn) {
        return (MaybeT<W,R>)super.zipWith(fn);
    }

    @Override
    public <R> MaybeT<W,R> zipWithS(Stream<Function<? super T, ? extends R>> fn) {
        return (MaybeT<W,R>)super.zipWithS(fn);
    }

    @Override
    public <R> MaybeT<W,R> zipWithP(Publisher<Function<? super T, ? extends R>> fn) {
        return (MaybeT<W,R>)super.zipWithP(fn);
    }

    @Override
    public <R> MaybeT<W,R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (MaybeT<W,R>)super.trampoline(mapper);
    }

    @Override
    public <U, R> MaybeT<W,R> zipS(Stream<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return (MaybeT<W,R>)super.zipS(other,zipper);
    }

    @Override
    public <U> MaybeT<W,Tuple2<T, U>> zipP(Publisher<? extends U> other) {
        return (MaybeT)super.zipP(other);
    }

    @Override
    public <S, U> MaybeT<W,Tuple3<T, S, U>> zip3(Iterable<? extends S> second, Iterable<? extends U> third) {
        return (MaybeT)super.zip3(second,third);
    }

    @Override
    public <S, U, R> MaybeT<W,R> zip3(Iterable<? extends S> second, Iterable<? extends U> third, Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (MaybeT<W,R>)super.zip3(second,third, fn3);
    }

    @Override
    public <T2, T3, T4> MaybeT<W,Tuple4<T, T2, T3, T4>> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth) {
        return (MaybeT)super.zip4(second,third,fourth);
    }

    @Override
    public <T2, T3, T4, R> MaybeT<W,R> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth, Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (MaybeT<W,R>)super.zip4(second,third,fourth,fn);
    }
}