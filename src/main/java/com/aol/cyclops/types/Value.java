package com.aol.cyclops.types;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;

import org.jooq.lambda.Collectable;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.Reducer;
import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.FeatureToggle;
import com.aol.cyclops.control.Ior;
import com.aol.cyclops.control.LazyReact;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.SimpleReact;
import com.aol.cyclops.control.Try;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.data.LazyImmutable;
import com.aol.cyclops.data.Mutable;
import com.aol.cyclops.data.collections.extensions.persistent.PBagX;
import com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX;
import com.aol.cyclops.data.collections.extensions.persistent.PQueueX;
import com.aol.cyclops.data.collections.extensions.persistent.PSetX;
import com.aol.cyclops.data.collections.extensions.persistent.PStackX;
import com.aol.cyclops.data.collections.extensions.persistent.PVectorX;
import com.aol.cyclops.data.collections.extensions.standard.DequeX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.QueueX;
import com.aol.cyclops.data.collections.extensions.standard.SetX;
import com.aol.cyclops.data.collections.extensions.standard.SortedSetX;
import com.aol.cyclops.types.futurestream.LazyFutureStream;
import com.aol.cyclops.types.futurestream.SimpleReactStream;
import com.aol.cyclops.types.stream.CyclopsCollectable;
import com.aol.cyclops.types.stream.reactive.ValueSubscriber;
import com.aol.cyclops.util.function.Predicates;

import lombok.AllArgsConstructor;

public interface Value<T> extends Supplier<T>, 
                                  Foldable<T>, 
                                  Convertable<T>,
                                  Publisher<T>,
                                  Predicate<T>,
                                  CyclopsCollectable<T>{
    
    


    default boolean test(T t){
        if(!(t instanceof Value))
            return Predicates.eqv(Maybe.ofNullable(t)).test(this);
        else
            return Predicates.eqv((Value)t).test(this);
            
    }
    default ValueSubscriber<T> newSubscriber(){
        return ValueSubscriber.subscriber();
    }
	@Override
    default void subscribe(Subscriber<? super T> sub) {
	     sub.onSubscribe(new Subscription(){
	            
            AtomicBoolean running =  new AtomicBoolean(true);
             
             @Override
             public void request(long n) {
                 
                 if(n<1){
                     sub.onError(new IllegalArgumentException("3.9 While the Subscription is not cancelled, Subscription.request(long n) MUST throw a java.lang.IllegalArgumentException if the argument is <= 0."));
                 }
                
                 if(!running.compareAndSet(true, false)){
                     
                     return;
                 }
                try {
                    
                    sub.onNext(get());

                } catch (Throwable t) {
                    sub.onError(t);

                }
                try {
                    sub.onComplete();

                } finally {
                    

                }
                 
             }
             @Override
             public void cancel() {
                
                 running.set(false);
                 
             }
             
         });
        
    }
    public static <T> Value<T> of(Supplier<T> supplier){
		 return new ValueImpl<T>(supplier);
	 }
	 @AllArgsConstructor
	 public static class ValueImpl<T> implements Value<T>{
		 private final Supplier<T> delegate;
		 
		 public T get(){
			 return delegate.get();
		 }

		

		@Override
		public Iterator<T> iterator() {
			return stream().iterator();
		}
	 }
	 
	 

	 
	default ReactiveSeq<T> stream() {
			return ReactiveSeq.of(Try.withCatch(()->get(),NoSuchElementException.class)).filter(Try::isSuccess).map(Try::get);
	}
	 
	 default ListX<?> unapply(){
		 return toListX();
	 }
	 default ReactiveSeq<T> iterate(UnaryOperator<T> fn){
			return ReactiveSeq.iterate(get(),fn);
	 }
	 default ReactiveSeq<T> generate(){
		return ReactiveSeq.generate(this);
	 }
	 default <E> E mapReduce(Reducer<E> monoid){
		 return monoid.mapReduce(toStream());
	 }
	 default T fold(Monoid<T> monoid){
		 return monoid.reduce(toStream());
	 }
	 default  T fold(T identity,BinaryOperator<T> accumulator){
	     Optional<T> opt = toOptional();
	     if(opt.isPresent())
	         return accumulator.apply(identity, get());
	     return identity;
	 }
	 default LazyImmutable<T> toLazyImmutable(){
		 return LazyImmutable.of(get());
	 }
	 default Mutable<T> toMutable(){
		 return Mutable.of(get());
	 }
	 default Xor<?,T> toXor(){
	     if(this instanceof Xor)
	         return (Xor)this;
	     Optional<T> o = toOptional();
	     return o.isPresent() ? Xor.primary(o.get()) : Xor.secondary(new NoSuchElementException());
		
	 }
	 /**
      * Convert to an Xor where the secondary value will be used if no primary value is present
      * 
     * @param secondary Value to use in case no primary value is present
     * @return
     */
    default <ST> Xor<ST,T> toXor(ST secondary){
         Optional<T> o = toOptional();
         return o.isPresent() ? Xor.primary(o.get()) : Xor.secondary(secondary);
     }
     default <X extends Throwable> Try<T,X> toTry(X throwable){
         return toXor().visit(secondary->Try.failure(throwable),
                              primary->Try.success(primary));
         
      }
	 
	 default Try<T,Throwable> toTry(){
	    return toXor().visit(secondary->Try.failure(new NoSuchElementException()),
	                         primary->Try.success(primary));
		
	 }
	 default <X extends Throwable> Try<T,X> toTry(Class<X>... classes){
		 return Try.withCatch( ()->get(),classes);
	 }
	 default   Ior<?,T> toIor(){
	     if(this instanceof Ior)
             return (Ior)this;
         Optional<T> o = toOptional();
         return o.isPresent() ? Ior.primary(o.get()) : Ior.secondary(new NoSuchElementException());
	 }
	 default FeatureToggle<T> toFeatureToggle(){
	       Optional<T> opt = toOptional();
	       return opt.isPresent() ? FeatureToggle.enable(opt.get()) : FeatureToggle.disable(null);
	 }
	 default Eval<T> toEvalNow(){
		 return Eval.now(get());
	 }
	 default Eval<T> toEvalLater(){
		 return Eval.later(this);
	 }
	 default Eval<T> toEvalAlways(){
		 return Eval.always(this);
	 }
	 default Maybe<T> toMaybe(){
		  return visit(p-> Maybe.ofNullable(p),()->Maybe.none());
	 }
	 default ListX<T> toListX(){
		 return ListX.fromIterable(toList());
	 }
	 default SetX<T> toSetX(){
		 return SetX.fromIterable(toList());
	 }
	 default SortedSetX<T> toSortedSetX(){
		 return SortedSetX.fromIterable(toList());
	 }
	 default QueueX<T> toQueueX(){
		 return QueueX.fromIterable(toList());
	 }
	 default DequeX<T> toDequeX(){
		 return DequeX.fromIterable(toList());
	 }
	 default PStackX<T> toPStackX(){
		 return PStackX.fromCollection(toList());
	 }
	 default PVectorX<T> toPVectorX(){
		 return PVectorX.fromCollection(toList());
	 }
	 default PQueueX<T> toPQueueX(){
		 return PQueueX.fromCollection(toList());
	 }
	 default PSetX<T> toPSetX(){
		 return PSetX.fromCollection(toList());
	 }
	 default POrderedSetX<T> toPOrderedSetX(){
		 return POrderedSetX.fromCollection(toList());
	 }
	 default PBagX<T> toPBagX(){
		 return PBagX.fromCollection(toList());
	 }
	 default String mkString(){
		
		 if(isPresent())
			 return this.getClass().getSimpleName()+"[" + get() + "]";
		 return this.getClass().getSimpleName()+"[]";
	 }

	default LazyFutureStream<T> toFutureStream(LazyReact reactor) {
		return reactor.ofAsync(this);
	}

	default LazyFutureStream<T> toFutureStream() {
		return new LazyReact().ofAsync(this);
	}

	default SimpleReactStream<T> toSimpleReact(SimpleReact reactor) {
		return reactor.ofAsync(this);
	}

	default SimpleReactStream<T> toSimpleReact() {
		return new SimpleReact().ofAsync(this);
	}
	
	default <R, A> R collect(Collector<? super T, A, R> collector) {
	    final A state = collector.supplier().get();
	    collector.accumulator().accept(state, get());
	    return collector.finisher().apply(state);
	}
	
	default List<T> toList() {
	    return Convertable.super.toList();
	}
	
	default Collectable<T> collectable() {
	    return this;
	}
	
}
