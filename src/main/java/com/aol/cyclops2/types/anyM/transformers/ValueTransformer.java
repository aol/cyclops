package com.aol.cyclops2.types.anyM.transformers;

import java.util.function.*;
import java.util.stream.Stream;

import com.aol.cyclops2.types.*;
import com.aol.cyclops2.types.factory.Unit;
import com.aol.cyclops2.types.foldable.Folds;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import cyclops.control.anym.AnyM;
import cyclops.reactive.ReactiveSeq;
import cyclops.control.anym.WitnessType;
import cyclops.function.Function4;
import cyclops.function.Function3;

public abstract class ValueTransformer<W extends WitnessType<W>,T> implements Publisher<T>,
                                                                            Unwrapable,
                                                                            Unit<T>,
                                                                            Folds<T>,
                                                                            Zippable<T> {
    public abstract <R> ValueTransformer<W,R> empty();
   // public abstract <R> ValueTransformer<W,R> flatMap(final Function<? super T, ? extends MonadicValue<? extends R>> f);
    public abstract AnyM<W,? extends MonadicValue<T>> transformerStream();
    protected abstract <R> ValueTransformer<W,R> unitAnyM(AnyM<W,? super MonadicValue<R>> anyM);

    public boolean isPresent(){

        return !stream().isEmpty();
    }
    public Option<T> get(){
        return stream().findOne();
    }

    public T orElse(T value){
        return stream().findAny().orElse(value);
    }
    public T orElseGet(Supplier<? super T> s){
       return stream().findAny().orElseGet((Supplier<T>)s);
    }
    public <X extends Throwable> T orElseThrow(Supplier<? super X> s) throws X {
        return stream().findAny().orElseThrow((Supplier<X>)s);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#combine(com.aol.cyclops2.types.Value, java.util.function.BiFunction)
     */
    public <T2, R> ValueTransformer<W,R> combine(Value<? extends T2> app,
            BiFunction<? super T, ? super T2, ? extends R> fn) {

        return unitAnyM(this.transformerStream().map(v->v.combine(app, fn)));
    }
    
    
    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#forEachAsync(org.reactivestreams.Subscriber)
     */
     @Override
    public void subscribe(final Subscriber<? super T> s) {

       transformerStream().forEach(v->v.subscribe(s));

    }

   

   // <T> TransformerSeq<W,T> unitStream(ReactiveSeq<T> traversable);
    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Combiner#combine(java.util.function.BinaryOperator, com.aol.cyclops2.types.Combiner)
     */
   
    public  ValueTransformer<W,T> zip(BinaryOperator<Zippable<T>> combiner, Zippable<T> app) {
        return this.unitAnyM(this.transformerStream().map(v->v.zip(combiner, app)));
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Value#iterate(java.util.function.UnaryOperator)
     */
  //@TODO Return StreamT
    public AnyM<W,? extends ReactiveSeq<T>> iterate(UnaryOperator<T> fn, T altSeed) {
        
        return this.transformerStream().map(v->v.asSupplier(altSeed).iterate(fn));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Value#generate()
     */
    //@TODO Return StreamT
    public AnyM<W,? extends ReactiveSeq<T>> generate(T altSeed) {
        
        return this.transformerStream().map(v->v.asSupplier(altSeed).generate());
    }

    
    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Zippable#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
   
    public <T2, R> ValueTransformer<W,R> zip(Iterable<? extends T2> iterable,
            BiFunction<? super T, ? super T2, ? extends R> fn) {
        return this.unitAnyM(this.transformerStream().map(v->v.zip(iterable,fn)));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Zippable#zip(java.util.function.BiFunction, org.reactivestreams.Publisher)
     */
   
    public <T2, R> ValueTransformer<W,R> zipP(Publisher<? extends T2> publisher,BiFunction<? super T, ? super T2, ? extends R> f) {
        
        return unitAnyM(this.transformerStream().map(v->v.zipP(publisher,f)));
    }
     /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Zippable#zip(java.util.stream.Stream)
     */
   
    public <U> ValueTransformer<W,Tuple2<T,U>> zipS(Stream<? extends U> other) {
        
        return this.unitAnyM(this.transformerStream().map(v->v.zipS(other)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Zippable#zip(java.lang.Iterable)
     */
   
    public <U> ValueTransformer<W,Tuple2<T,U>> zip(Iterable<? extends U> other) {
        
        return unitAnyM(this.transformerStream().map(v->v.zip(other)));
    }


   
    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    public <T2, R1, R2, R3, R> ValueTransformer<W,R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            Function3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
            Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return unitAnyM(this.transformerStream().map(v->v.forEach4(value1, value2, value3, yieldingFunction)));
       
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
   
    public <T2, R1, R2, R3, R> ValueTransformer<W,R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            Function3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
            Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
            Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return unitAnyM(this.transformerStream().map(v->v.forEach4(value1, value2, value3, filterFunction,yieldingFunction)));
       
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction)
     */
   
    public <T2, R1, R2, R> ValueTransformer<W,R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        return unitAnyM(this.transformerStream().map(v->v.forEach3(value1, value2, yieldingFunction)));
        
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.TriFunction)
     */
   
    public <T2, R1, R2, R> ValueTransformer<W,R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
            Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        
        return unitAnyM(this.transformerStream().map(v->v.forEach3(value1, value2, filterFunction,yieldingFunction)));
        
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
   
    public <R1, R> ValueTransformer<W,R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        
        return unitAnyM(this.transformerStream().map(v->v.forEach2(value1,  yieldingFunction)));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
   
    public <R1, R> ValueTransformer<W,R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, Boolean> filterFunction,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        
        return unitAnyM(this.transformerStream().map(v->v.forEach2(value1, filterFunction, yieldingFunction)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#flatMapI(java.util.function.Function)
     */
   
    public <R> ValueTransformer<W,R> flatMapIterable(Function<? super T, ? extends Iterable<? extends R>> mapper) {
        
        return unitAnyM(this.transformerStream().map(v->v.flatMapI(mapper)));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#flatMapP(java.util.function.Function)
     */
   
    public <R> ValueTransformer<W,R> flatMapPublisher(Function<? super T, ? extends Publisher<? extends R>> mapper) {
        return unitAnyM(this.transformerStream().map(v->v.flatMapP(mapper)));
    }


    public <R> AnyM<W,R> visit(Function<? super T, ? extends R> some, Supplier<? extends R> none){
        return this.transformerStream().map(v->v.visit(some,none));
    }
    

}
