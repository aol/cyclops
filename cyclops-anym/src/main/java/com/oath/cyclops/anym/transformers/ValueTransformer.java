package com.oath.cyclops.anym.transformers;

import java.util.function.*;

import com.oath.cyclops.types.factory.Unit;
import com.oath.cyclops.types.foldable.Folds;
import com.oath.cyclops.types.MonadicValue;
import com.oath.cyclops.types.Unwrappable;
import com.oath.cyclops.types.Zippable;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import cyclops.monads.AnyM;
import cyclops.reactive.ReactiveSeq;
import cyclops.monads.WitnessType;
import cyclops.function.Function4;
import cyclops.function.Function3;

@Deprecated
public abstract class ValueTransformer<W extends WitnessType<W>,T> implements Publisher<T>,
  Unwrappable,
  Unit<T>,
  Folds<T>,
  Zippable<T> {
    public abstract <R> ValueTransformer<W,R> empty();
   // public abstract <R> ValueTransformer<W,R> flatMap(final Function<? super T, ? extends MonadicValue<? extends R>> f);
    public abstract <X extends MonadicValue<T> & Zippable<T>> AnyM<W,? extends X> transformerStream();
    protected abstract <R> ValueTransformer<W,R> unitAnyM(AnyM<W,? super MonadicValue<R>> anyM);

    public boolean isPresent(){

        return !stream().isEmpty();
    }
    public Option<T> get(){
        return stream().takeOne();
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
   * @see com.oath.cyclops.types.Traversable#forEachAsync(org.reactivestreams.Subscriber)
   */
     @Override
    public void subscribe(final Subscriber<? super T> s) {

       transformerStream().forEach(v->v.subscribe(s));

    }



  /* (non-Javadoc)
   * @see com.oath.cyclops.types.Value#iterate(java.util.function.UnaryOperator)
   */
  //@TODO Return StreamT
    public AnyM<W,? extends ReactiveSeq<T>> iterate(UnaryOperator<T> fn, T altSeed) {

        return this.transformerStream().map(v->v.asSupplier(altSeed).iterate(fn));
    }
    /* (non-Javadoc)
     * @see com.oath.cyclops.types.Value#generate()
     */
    //@TODO Return StreamT
    public AnyM<W,? extends ReactiveSeq<T>> generate(T altSeed) {

        return this.transformerStream().map(v->v.asSupplier(altSeed).generate());
    }


    /* (non-Javadoc)
     * @see com.oath.cyclops.types.Zippable#zip(java.lang.Iterable, java.util.function.BiFunction)
     */

    public <T2, R> ValueTransformer<W,R> zip(Iterable<? extends T2> iterable,
            BiFunction<? super T, ? super T2, ? extends R> fn) {
        return this.unitAnyM(this.transformerStream().map(v->v.zip(iterable,fn)));
    }
    /* (non-Javadoc)
     * @see com.oath.cyclops.types.Zippable#zip(java.util.function.BiFunction, org.reactivestreams.Publisher)
     */

    public <T2, R> ValueTransformer<W,R> zip(BiFunction<? super T, ? super T2, ? extends R> f, Publisher<? extends T2> publisher) {

        return unitAnyM(this.transformerStream().map(v->v.zip(f, publisher)));
    }
     /* (non-Javadoc)
     * @see com.oath.cyclops.types.Zippable#zip(java.util.stream.Stream)
     */

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.Zippable#zip(java.lang.Iterable)
     */

    public <U> ValueTransformer<W,Tuple2<T,U>> zip(Iterable<? extends U> other) {

        return unitAnyM(this.transformerStream().map(v->v.zip(other)));
    }



    /* (non-Javadoc)
     * @see com.oath.cyclops.types.MonadicValue#forEach4(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.QuadFunction)
     */
    public <T2, R1, R2, R3, R> ValueTransformer<W,R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            Function3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
            Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return unitAnyM(this.transformerStream().map(v->v.forEach4(value1, value2, value3, yieldingFunction)));

    }
    /* (non-Javadoc)
     * @see com.oath.cyclops.types.MonadicValue#forEach4(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.QuadFunction, com.oath.cyclops.util.function.QuadFunction)
     */

    public <T2, R1, R2, R3, R> ValueTransformer<W,R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            Function3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
            Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
            Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return unitAnyM(this.transformerStream().map(v->v.forEach4(value1, value2, value3, filterFunction,yieldingFunction)));

    }
    /* (non-Javadoc)
     * @see com.oath.cyclops.types.MonadicValue#forEach3(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction)
     */

    public <T2, R1, R2, R> ValueTransformer<W,R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        return unitAnyM(this.transformerStream().map(v->v.forEach3(value1, value2, yieldingFunction)));

    }
    /* (non-Javadoc)
     * @see com.oath.cyclops.types.MonadicValue#forEach3(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.TriFunction)
     */

    public <T2, R1, R2, R> ValueTransformer<W,R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
            Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return unitAnyM(this.transformerStream().map(v->v.forEach3(value1, value2, filterFunction,yieldingFunction)));

    }
    /* (non-Javadoc)
     * @see com.oath.cyclops.types.MonadicValue#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */

    public <R1, R> ValueTransformer<W,R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return unitAnyM(this.transformerStream().map(v->v.forEach2(value1,  yieldingFunction)));
    }
    /* (non-Javadoc)
     * @see com.oath.cyclops.types.MonadicValue#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */

    public <R1, R> ValueTransformer<W,R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, Boolean> filterFunction,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return unitAnyM(this.transformerStream().map(v->v.forEach2(value1, filterFunction, yieldingFunction)));
    }



    public <R> ValueTransformer<W,R> concatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {

        return unitAnyM(this.transformerStream().map(v->v.concatMap(mapper)));
    }
    /* (non-Javadoc)
     * @see com.oath.cyclops.types.MonadicValue#flatMapP(java.util.function.Function)
     */

    public <R> ValueTransformer<W,R> mergeMap(Function<? super T, ? extends Publisher<? extends R>> mapper) {
        return unitAnyM(this.transformerStream().map(v->v.mergeMap(mapper)));
    }


    public <R> AnyM<W,R> fold(Function<? super T, ? extends R> some, Supplier<? extends R> none){
        return this.transformerStream().map(v->v.fold(some,none));
    }


}
