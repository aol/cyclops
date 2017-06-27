package cyclops.monads.transformers;

import com.aol.cyclops2.types.*;
import com.aol.cyclops2.types.anyM.transformers.ValueTransformer;

import com.aol.cyclops2.types.foldable.To;
import com.aol.cyclops2.types.functor.Transformable;
import cyclops.control.Maybe;
import cyclops.control.Trampoline;
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
import java.util.Optional;
import java.util.function.*;
import java.util.stream.Stream;



/**
* Monad Transformer for Optional's

 * 
 * OptionalT allows the deeply wrapped Optional toNested be manipulating within it's nested /contained context
 *
 * @author johnmcclean
 *
 * @param <T> Type of data stored inside the nested Optional(s)
 */
public final class OptionalT<W extends WitnessType<W>,T> extends ValueTransformer<W,T> 
                                                       implements To<OptionalT<W,T>>,
        Transformable<T>,
        Filters<T> {

    private final AnyM<W,Optional<T>> run;

    @Override
    public Iterator<T> iterator() {
        return stream().iterator();
    }

                                                                         @Override
    public ReactiveSeq<T> stream() {
        return run.stream().map(Optional::get);
    }



    /**
     * @return The wrapped AnyM
     */
    @Override
    public AnyM<W,Optional<T>> unwrap() {
        return run;
    }

    public <R> R unwrapTo(Function<? super AnyM<W,Optional<T>>, ? extends R> fn) {
        return unwrap().to(fn);
    }

    private OptionalT(final AnyM<W,Optional<T>> run) {
        this.run = run;
    }

    
    @Override @Deprecated (/*DO NOT USE INTERNAL USE ONLY*/)
    protected <R> OptionalT<W,R> unitAnyM(AnyM<W,? super MonadicValue<R>> traversable) {

        return of((AnyM) traversable);
    }

    @Override
    public AnyM<W,? extends MonadicValue<T>> transformerStream() {

        return run.map(Maybe::fromOptional);
    }

    @Override
    public OptionalT<W,T> filter(final Predicate<? super T> test) {
        return of(run.map(f->f.map(in->Tuple.tuple(in,test.test(in))))
                     .filter( f->f.get().v2 )
                     .map( f->f.map(in->in.v1)));
    }

    /**
     * Peek at the current value of the Optional
     * <pre>
     * {@code 
     *    OptionalWT.of(AnyM.fromStream(Arrays.asOptionalW(10))
     *             .peek(System.out::println);
     *             
     *     //prints 10        
     * }
     * </pre>
     * 
     * @param peek  Consumer toNested accept current value of Optional
     * @return OptionalWT with peek call
     */
    @Override
    public OptionalT<W,T> peek(final Consumer<? super T> peek) {
        return map(e->{
            peek.accept(e);
            return e;
        });
    }

    /**
     * Map the wrapped Optional
     * 
     * <pre>
     * {@code 
     *  OptionalWT.of(AnyM.fromStream(Arrays.asOptionalW(10))
     *             .map(t->t=t+1);
     *  
     *  
     *  //OptionalWT<AnyMSeq<Stream<Optional[11]>>>
     * }
     * </pre>
     * 
     * @param f Mapping function for the wrapped Optional
     * @return OptionalWT that applies the map function toNested the wrapped Optional
     */
    @Override
    public <B> OptionalT<W,B> map(final Function<? super T, ? extends B> f) {
        return new OptionalT<W,B>(
                                  run.map(o -> o.map(f)));
    }

    /**
     * Flat Map the wrapped Optional
      * <pre>
     * {@code 
     *  OptionalWT.of(AnyM.fromStream(Arrays.asOptionalW(10))
     *             .flatMap(t->Optional.completedOptional(20));
     *  
     *  
     *  //OptionalWT<AnyMSeq<Stream<Optional[20]>>>
     * }
     * </pre>
     * @param f FlatMap function
     * @return OptionalWT that applies the flatMap function toNested the wrapped Optional
     */

    public <B> OptionalT<W,B> flatMapT(final Function<? super T, OptionalT<W,B>> f) {
        return of(run.map(Optional -> Optional.flatMap(a -> f.apply(a).run.stream()
                                                                      .toList()
                                                                      .get(0))));
    }

    private static <W extends WitnessType<W>,B> AnyM<W,Optional<B>> narrow(final AnyM<W,Optional<? extends B>> run) {
        return (AnyM) run;
    }

    @Override
    public <B> OptionalT<W,B> flatMap(final Function<? super T, ? extends MonadicValue<? extends B>> f) {

        final AnyM<W,Optional<? extends B>> mapped = run.map(o -> o.flatMap(f.andThen(m->m.toOptional())));
        return of(narrow(mapped));

    }

    /**
     * Lift a function into one that accepts and returns an OptionalWT
     * This allows multiple monad types toNested add functionality toNested existing function and methods
     * 
     * e.g. toNested add list handling  / iteration (via Optional) and iteration (via Stream) toNested an existing function
     * <pre>
     * {@code 
        Function<Integer,Integer> add2 = i -> i+2;
    	Function<OptionalWT<Integer>, OptionalWT<Integer>> optTAdd2 = OptionalWT.lift(add2);
    	
    	Stream<Integer> withNulls = Stream.of(1,2,3);
    	AnyMSeq<Integer> reactiveStream = AnyM.fromStream(withNulls);
    	AnyMSeq<Optional<Integer>> streamOpt = reactiveStream.map(Optional::completedOptional);
    	List<Integer> results = optTAdd2.applyHKT(OptionalWT.of(streamOpt))
    									.unwrap()
    									.<Stream<Optional<Integer>>>unwrap()
    									.map(Optional::join)
    									.collect(CyclopsCollectors.toList());
    	
    	
    	//Optional.completedOptional(List[3,4]);
     * 
     * 
     * }</pre>
     * 
     * 
     * @param fn Function toNested enhance with functionality from Optional and another monad type
     * @return Function that accepts and returns an OptionalWT
     */
    public static <W extends WitnessType<W>,U, R> Function<OptionalT<W,U>, OptionalT<W,R>> lift(final Function<? super U, ? extends R> fn) {
        return optTu -> optTu.map(input -> fn.apply(input));
    }

    /**
     * Lift a BiFunction into one that accepts and returns  OptionalWTs
     * This allows multiple monad types toNested add functionality toNested existing function and methods
     * 
     * e.g. toNested add list handling / iteration (via Optional), iteration (via Stream)  and asynchronous execution (Optional)
     * toNested an existing function
     * 
     * <pre>
     * {@code 
    	BiFunction<Integer,Integer,Integer> add = (a,b) -> a+b;
    	BiFunction<OptionalWT<Integer>,OptionalWT<Integer>,OptionalWT<Integer>> optTAdd2 = OptionalWT.lift2(add);
    	
    	Stream<Integer> withNulls = Stream.of(1,2,3);
    	AnyMSeq<Integer> reactiveStream = AnyM.ofMonad(withNulls);
    	AnyMSeq<Optional<Integer>> streamOpt = reactiveStream.map(Optional::completedOptional);
    	
    	Optional<Optional<Integer>> two = Optional.completedOptional(Optional.completedOptional(2));
    	AnyMSeq<Optional<Integer>> Optional=  AnyM.fromOptionalW(two);
    	List<Integer> results = optTAdd2.applyHKT(OptionalWT.of(streamOpt),OptionalWT.of(Optional))
    									.unwrap()
    									.<Stream<Optional<Integer>>>unwrap()
    									.map(Optional::join)
    									.collect(CyclopsCollectors.toList());
    									
    		//Optional.completedOptional(List[3,4,5]);
      }
      </pre>
     * @param fn BiFunction toNested enhance with functionality from Optional and another monad type
     * @return Function that accepts and returns an OptionalWT
     */
    public static <W extends WitnessType<W>, U1,  U2, R> BiFunction<OptionalT<W,U1>, OptionalT<W,U2>, OptionalT<W,R>> lift2(
            final BiFunction<? super U1, ? super U2, ? extends R> fn) {
        return (optTu1, optTu2) -> optTu1.flatMapT(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
    }

    /**
     * Construct an OptionalWT from an AnyM that contains a monad type that contains type other than Optional
     * The values in the underlying monad will be mapped toNested Optional<A>
     * 
     * @param anyM AnyM that doesn't contain a monad wrapping an Optional
     * @return OptionalWT
     */
    public static <W extends WitnessType<W>,A> OptionalT<W,A> fromAnyM(final AnyM<W,A> anyM) {
        return of(anyM.map(Optional::ofNullable));
    }

    /**
     * Construct an OptionalWT from an AnyM that wraps a monad containing  OptionalWs
     * 
     * @param monads AnyM that contains a monad wrapping an Optional
     * @return OptionalWT
     */
    public static <W extends WitnessType<W>,A> OptionalT<W,A> of(final AnyM<W,Optional<A>> monads) {
        return new OptionalT<>(
                                 monads);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("OptionalT[%s]", run.unwrap().toString());
    }

    


    public <R> OptionalT<W,R> unitIterator(final Iterator<R> it) {
        return of(run.unitIterator(it)
                     .map(i -> Optional.ofNullable(i)));
    }

    @Override
    public <R> OptionalT<W,R> unit(final R value) {
        return of(run.unit(Optional.ofNullable(value)));
    }

    @Override
    public <R> OptionalT<W,R> empty() {
        return of(run.unit(Optional.<R>empty()));
    }

    

   
    @Override
    public int hashCode() {
        return run.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof OptionalT) {
            return run.equals(((OptionalT) o).run);
        }
        return false;
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#combine(com.aol.cyclops2.types.Value, java.util.function.BiFunction)
     */
    @Override
    public <T2, R> OptionalT<W,R> combine(Value<? extends T2> app,
                                          BiFunction<? super T, ? super T2, ? extends R> fn) {
        return (OptionalT<W,R>)super.combine(app, fn);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#combine(java.util.function.BinaryOperator, com.aol.cyclops2.types.Combiner)
     */
    @Override
    public OptionalT<W, T> zip(BinaryOperator<Zippable<T>> combiner, Zippable<T> app) {
        
        return (OptionalT<W, T>)super.zip(combiner, app);
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
    public <T2, R> OptionalT<W, R> zip(Iterable<? extends T2> iterable,
                                       BiFunction<? super T, ? super T2, ? extends R> fn) {
        
        return (OptionalT<W, R>)super.zip(iterable, fn);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#zip(java.util.function.BiFunction, org.reactivestreams.Publisher)
     */
    @Override
    public <T2, R> OptionalT<W, R> zipP(Publisher<? extends T2> publisher, BiFunction<? super T, ? super T2, ? extends R> fn) {
        
        return (OptionalT<W, R>)super.zipP(publisher,fn);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#zip(java.util.reactiveStream.Stream)
     */
    @Override
    public <U> OptionalT<W, Tuple2<T, U>> zipS(Stream<? extends U> other) {
        
        return (OptionalT)super.zipS(other);
    }


    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#zip(java.lang.Iterable)
     */
    @Override
    public <U> OptionalT<W, Tuple2<T, U>> zip(Iterable<? extends U> other) {
        
        return (OptionalT)super.zip(other);
    }


    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    public <T2, R1, R2, R3, R> OptionalT<W, R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
                                                        BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                                        Fn3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
                                                        Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return (OptionalT<W, R>)super.forEach4(value1, value2, value3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    public <T2, R1, R2, R3, R> OptionalT<W, R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
                                                        BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                                        Fn3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
                                                        Fn4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                        Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return (OptionalT<W, R>)super.forEach4(value1, value2, value3, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    public <T2, R1, R2, R> OptionalT<W, R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
                                                    BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                                    Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        
        return (OptionalT<W, R>)super.forEach3(value1, value2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    public <T2, R1, R2, R> OptionalT<W, R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
                                                    BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                                    Fn3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                                                    Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        
        return (OptionalT<W, R>)super.forEach3(value1, value2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    public <R1, R> OptionalT<W, R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
                                            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        
        return (OptionalT<W, R>)super.forEach2(value1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    public <R1, R> OptionalT<W, R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
                                            BiFunction<? super T, ? super R1, Boolean> filterFunction,
                                            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        
        return (OptionalT<W, R>)super.forEach2(value1, filterFunction, yieldingFunction);
    }



    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#flatMapI(java.util.function.Function)
     */
    @Override
    public <R> OptionalT<W, R> flatMapIterable(Function<? super T, ? extends Iterable<? extends R>> mapper) {
        
        return (OptionalT<W, R>)super.flatMapIterable(mapper);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#flatMapP(java.util.function.Function)
     */
    @Override
    public <R> OptionalT<W, R> flatMapPublisher(Function<? super T, ? extends Publisher<? extends R>> mapper) {
        
        return (OptionalT<W, R>)super.flatMapPublisher(mapper);
    }
    public <T2, R1, R2, R3, R> OptionalT<W,R> forEach4M(Function<? super T, ? extends OptionalT<W,R1>> value1,
                                                        BiFunction<? super T, ? super R1, ? extends OptionalT<W,R2>> value2,
                                                        Fn3<? super T, ? super R1, ? super R2, ? extends OptionalT<W,R3>> value3,
                                                        Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return this.flatMapT(in->value1.apply(in)
                .flatMapT(in2-> value2.apply(in,in2)
                        .flatMapT(in3->value3.apply(in,in2,in3)
                                .map(in4->yieldingFunction.apply(in,in2,in3,in4)))));

    }
    public <T2, R1, R2, R3, R> OptionalT<W,R> forEach4M(Function<? super T, ? extends OptionalT<W,R1>> value1,
                                                        BiFunction<? super T, ? super R1, ? extends OptionalT<W,R2>> value2,
                                                        Fn3<? super T, ? super R1, ? super R2, ? extends OptionalT<W,R3>> value3,
                                                        Fn4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                        Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return this.flatMapT(in->value1.apply(in)
                    .flatMapT(in2-> value2.apply(in,in2)
                            .flatMapT(in3->value3.apply(in,in2,in3)
                                                 .filter(in4->filterFunction.apply(in,in2,in3,in4))
                                                 .map(in4->yieldingFunction.apply(in,in2,in3,in4)))));

    }

    public <T2, R1, R2, R> OptionalT<W,R> forEach3M(Function<? super T, ? extends OptionalT<W,R1>> value1,
                                                    BiFunction<? super T, ? super R1, ? extends OptionalT<W,R2>> value2,
                                                    Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return this.flatMapT(in->value1.apply(in).flatMapT(in2-> value2.apply(in,in2)
                                                 .map(in3->yieldingFunction.apply(in,in2,in3))));

    }

    public <T2, R1, R2, R> OptionalT<W,R> forEach3M(Function<? super T, ? extends OptionalT<W,R1>> value1,
                                                    BiFunction<? super T, ? super R1, ? extends OptionalT<W,R2>> value2,
                                                    Fn3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                                                    Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return this.flatMapT(in->value1.apply(in).flatMapT(in2-> value2.apply(in,in2).filter(in3->filterFunction.apply(in,in2,in3))
                                                                                     .map(in3->yieldingFunction.apply(in,in2,in3))));

    }
    public <R1, R> OptionalT<W,R> forEach2M(Function<? super T, ? extends OptionalT<W,R1>> value1,
                                            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {


        return this.flatMapT(in->value1.apply(in)
                    .map(in2->yieldingFunction.apply(in,in2)));
    }

    public <R1, R> OptionalT<W,R> forEach2M(Function<? super T, ? extends OptionalT<W,R1>> value1,
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
    public <U> OptionalT<W,U> cast(Class<? extends U> type) {
        return (OptionalT<W,U>)super.cast(type);
    }

    @Override
    public <U> OptionalT<W,U> ofType(Class<? extends U> type) {
        return (OptionalT<W,U>)Filters.super.ofType(type);
    }

    @Override
    public OptionalT<W,T> filterNot(Predicate<? super T> predicate) {
        return (OptionalT<W,T>)Filters.super.filterNot(predicate);
    }

    @Override
    public OptionalT<W,T> notNull() {
        return (OptionalT<W,T>)Filters.super.notNull();
    }

    @Override
    public <R> OptionalT<W,R> zipWith(Iterable<Function<? super T, ? extends R>> fn) {
        return (OptionalT<W,R>)super.zipWith(fn);
    }

    @Override
    public <R> OptionalT<W,R> zipWithS(Stream<Function<? super T, ? extends R>> fn) {
        return (OptionalT<W,R>)super.zipWithS(fn);
    }

    @Override
    public <R> OptionalT<W,R> zipWithP(Publisher<Function<? super T, ? extends R>> fn) {
        return (OptionalT<W,R>)super.zipWithP(fn);
    }

    @Override
    public <R> OptionalT<W,R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (OptionalT<W,R>)super.trampoline(mapper);
    }

    @Override
    public <U, R> OptionalT<W,R> zipS(Stream<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return (OptionalT<W,R>)super.zipS(other,zipper);
    }

    @Override
    public <U> OptionalT<W,Tuple2<T, U>> zipP(Publisher<? extends U> other) {
        return (OptionalT)super.zipP(other);
    }

    @Override
    public <S, U> OptionalT<W,Tuple3<T, S, U>> zip3(Iterable<? extends S> second, Iterable<? extends U> third) {
        return (OptionalT)super.zip3(second,third);
    }

    @Override
    public <S, U, R> OptionalT<W,R> zip3(Iterable<? extends S> second, Iterable<? extends U> third, Fn3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (OptionalT<W,R>)super.zip3(second,third, fn3);
    }

    @Override
    public <T2, T3, T4> OptionalT<W,Tuple4<T, T2, T3, T4>> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth) {
        return (OptionalT)super.zip4(second,third,fourth);
    }

    @Override
    public <T2, T3, T4, R> OptionalT<W,R> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth, Fn4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (OptionalT<W,R>)super.zip4(second,third,fourth,fn);
    }
}