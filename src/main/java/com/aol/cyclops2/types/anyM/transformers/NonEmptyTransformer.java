package com.aol.cyclops2.types.anyM.transformers;

import com.aol.cyclops2.types.MonadicValue;
import com.aol.cyclops2.types.Unwrapable;
import com.aol.cyclops2.types.Value;
import com.aol.cyclops2.types.factory.Unit;
import com.aol.cyclops2.types.foldable.Folds;
import com.aol.cyclops2.types.functor.Transformable;
import cyclops.control.Option;
import cyclops.monads.AnyM;
import cyclops.monads.WitnessType;
import cyclops.monads.transformers.StreamT;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.function.*;

public abstract class NonEmptyTransformer<W extends WitnessType<W>,T> implements Publisher<T>,
                                                                            Unwrapable,Transformable<T>,
                                                                            Unit<T>,
                                                                            Folds<T>{

    public abstract AnyM<W,? extends Value<T>> transformerStream();
    protected abstract <R> NonEmptyTransformer<W,R> unitAnyM(AnyM<W,? super MonadicValue<R>> anyM);

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
     * @see com.aol.cyclops2.types.Traversable#forEachAsync(org.reactivestreams.Subscriber)
     */
     @Override
    public void subscribe(final Subscriber<? super T> s) {

       transformerStream().forEach(v->v.subscribe(s));

    }

   




    public StreamT<W,T> iterate(UnaryOperator<T> fn, T altSeed) {
        
        return StreamT.of(this.transformerStream().map(v->v.asSupplier(altSeed).iterate(fn)));
    }

    public StreamT<W,T> generate(T altSeed) {
        
        return StreamT.of(this.transformerStream().map(v->v.asSupplier(altSeed).generate()));
    }



    public <R> AnyM<W,R> visit(Function<? super T, ? extends R> some, Supplier<? extends R> none){
        return this.transformerStream().map(v->v.visit(some,none));
    }
    

}
