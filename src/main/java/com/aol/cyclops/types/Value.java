package com.aol.cyclops.types;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.Reducer;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.FeatureToggle;
import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.Ior;
import com.aol.cyclops.control.LazyReact;
import com.aol.cyclops.control.Matchable;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.SimpleReact;
import com.aol.cyclops.control.Try;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.control.monads.transformers.values.CompletableFutureT;
import com.aol.cyclops.control.monads.transformers.values.EvalT;
import com.aol.cyclops.control.monads.transformers.values.MaybeT;
import com.aol.cyclops.control.monads.transformers.values.OptionalT;
import com.aol.cyclops.control.monads.transformers.values.TryT;
import com.aol.cyclops.control.monads.transformers.values.XorT;
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
import com.aol.cyclops.types.stream.reactive.ValueSubscriber;
import com.aol.cyclops.util.function.Predicates;

import lombok.AllArgsConstructor;

public interface Value<T> extends Supplier<T>, 
                                  Foldable<T>, 
                                  Matchable<T>, 
                                  Convertable<T>,
                                  Publisher<T>,
                                  Predicate<T>{
    
   
   @AllArgsConstructor
   public static class Transformers<T>{
       Value<T> value;
      
       public  Transformers<T>.MaybeTBuilder maybeTBuilder(){
           return new MaybeTBuilder();
       }
       public class MaybeTBuilder{
           public MaybeT<T> fromMaybe(Function<? super Maybe<T>,? extends Maybe<Maybe<T>>> conv){
               return  MaybeT.of(AnyM.fromMaybe(conv.apply(value.toMaybe())));     
           }
         
           public MaybeT<T> fromEval(Function<? super Maybe<T>,? extends Eval<Maybe<T>>> conv){
               return  MaybeT.of(AnyM.fromEval(conv.apply(value.toMaybe())));     
           }
           public MaybeT<T> fromFutureW(Function<? super Maybe<T>,? extends FutureW<Maybe<T>>> conv){
               return  MaybeT.of(AnyM.fromFutureW(conv.apply(value.toMaybe())));     
           }
           public MaybeT<T> fromOptional(Function<? super Maybe<T>,? extends Optional<Maybe<T>>> conv){
               return  MaybeT.of(AnyM.fromOptional(conv.apply(value.toMaybe())));     
           }
           public MaybeT<T> fromCompletableFuture(Function<? super Maybe<T>,? extends CompletableFuture<Maybe<T>>> conv){
               return  MaybeT.of(AnyM.fromCompletableFuture(conv.apply(value.toMaybe())));     
           }
           public MaybeT<T> fromXor(Function<? super Maybe<T>,? extends Xor<?,Maybe<T>>> conv){
               return  MaybeT.of(AnyM.fromXor(conv.apply(value.toMaybe())));     
           }
           public MaybeT<T> fromTry(Function<? super Maybe<T>,? extends Try<Maybe<T>,?>> conv){
               return  MaybeT.of(AnyM.fromTry(conv.apply(value.toMaybe())));     
           }
       }
       public  Transformers<T>.OptionalTBuilder optionalTBuilder(){
           return new OptionalTBuilder();
       }
       public class OptionalTBuilder{
           public OptionalT<T> fromMaybe(Function<? super Optional<T>,? extends Maybe<Optional<T>>> conv){
               return  OptionalT.of(AnyM.fromMaybe(conv.apply(value.toOptional())));     
           }
           public OptionalT<T> fromEval(Function<? super Optional<T>,? extends Eval<Optional<T>>> conv){
               return  OptionalT.of(AnyM.fromEval(conv.apply(value.toOptional())));     
           }
           public OptionalT<T> fromFutureW(Function<? super Optional<T>,? extends FutureW<Optional<T>>> conv){
               return  OptionalT.of(AnyM.fromFutureW(conv.apply(value.toOptional())));     
           }
           public OptionalT<T> fromOptional(Function<? super Optional<T>,? extends Optional<Optional<T>>> conv){
               return  OptionalT.of(AnyM.fromOptional(conv.apply(value.toOptional())));     
           }
           public OptionalT<T> fromCompletableFuture(Function<? super Optional<T>,? extends CompletableFuture<Optional<T>>> conv){
               return  OptionalT.of(AnyM.fromCompletableFuture(conv.apply(value.toOptional())));     
           }
           public OptionalT<T> fromXor(Function<? super Optional<T>,? extends Xor<?,Optional<T>>> conv){
               return  OptionalT.of(AnyM.fromXor(conv.apply(value.toOptional())));     
           }
           public OptionalT<T> fromTry(Function<? super Optional<T>,? extends Try<Optional<T>,?>> conv){
               return  OptionalT.of(AnyM.fromTry(conv.apply(value.toOptional())));     
           }
       }
       public  Transformers<T>.EvalTBuilder evalTBuilder(){
           return new EvalTBuilder();
       }
       public class EvalTBuilder{
           public EvalT<T> fromMaybe(Function<? super Eval<T>,? extends Maybe<Eval<T>>> conv){
               return  EvalT.of(AnyM.fromMaybe(conv.apply(value.toEvalLater())));     
           }
           public EvalT<T> fromEval(Function<? super Eval<T>,? extends Eval<Eval<T>>> conv){
               return  EvalT.of(AnyM.fromEval(conv.apply(value.toEvalLater())));     
           }
           public EvalT<T> fromFutureW(Function<? super Eval<T>,? extends FutureW<Eval<T>>> conv){
               return  EvalT.of(AnyM.fromFutureW(conv.apply(value.toEvalLater())));     
           }
           public EvalT<T> fromOptional(Function<? super Eval<T>,? extends Optional<Eval<T>>> conv){
               return  EvalT.of(AnyM.fromOptional(conv.apply(value.toEvalLater())));     
           }
           public EvalT<T> fromCompletableFuture(Function<? super Eval<T>,? extends CompletableFuture<Eval<T>>> conv){
               return  EvalT.of(AnyM.fromCompletableFuture(conv.apply(value.toEvalLater())));     
           }
           public EvalT<T> fromXor(Function<? super Eval<T>,? extends Xor<?,Eval<T>>> conv){
               return  EvalT.of(AnyM.fromXor(conv.apply(value.toEvalLater())));     
           }
           public EvalT<T> fromTry(Function<? super Eval<T>,? extends Try<Eval<T>,?>> conv){
               return  EvalT.of(AnyM.fromTry(conv.apply(value.toEvalLater())));     
           }
       }
       public  Transformers<T>.CompletableFutureTBuilder completableFutureTBuilder(){
           return new CompletableFutureTBuilder();
       }
       public class CompletableFutureTBuilder{
           public CompletableFutureT<T> fromMaybe(Function<? super CompletableFuture<T>,? extends Maybe<CompletableFuture<T>>> conv){
               return  CompletableFutureT.of(AnyM.fromMaybe(conv.apply(value.toCompletableFuture())));     
           }
           public CompletableFutureT<T> fromEval(Function<? super CompletableFuture<T>,? extends Eval<CompletableFuture<T>>> conv){
               return  CompletableFutureT.of(AnyM.fromEval(conv.apply(value.toCompletableFuture())));     
           }
           public CompletableFutureT<T> fromFutureW(Function<? super CompletableFuture<T>,? extends FutureW<CompletableFuture<T>>> conv){
               return  CompletableFutureT.of(AnyM.fromFutureW(conv.apply(value.toCompletableFuture())));     
           }
           public CompletableFutureT<T> fromOptional(Function<? super CompletableFuture<T>,? extends Optional<CompletableFuture<T>>> conv){
               return  CompletableFutureT.of(AnyM.fromOptional(conv.apply(value.toCompletableFuture())));     
           }
           public CompletableFutureT<T> fromCompletableFuture(Function<? super CompletableFuture<T>,? extends CompletableFuture<CompletableFuture<T>>> conv){
               return  CompletableFutureT.of(AnyM.fromCompletableFuture(conv.apply(value.toCompletableFuture())));     
           }
           public CompletableFutureT<T> fromXor(Function<? super CompletableFuture<T>,? extends Xor<?,CompletableFuture<T>>> conv){
               return  CompletableFutureT.of(AnyM.fromXor(conv.apply(value.toCompletableFuture())));     
           }
           public CompletableFutureT<T> fromTry(Function<? super CompletableFuture<T>,? extends Try<CompletableFuture<T>,?>> conv){
               return  CompletableFutureT.of(AnyM.fromTry(conv.apply(value.toCompletableFuture())));     
           }
       }
       public  <ST> Transformers<T>.XorTBuilder<ST> xorTBuilder(){
           return new XorTBuilder<ST>();
       }
       public class XorTBuilder<ST>{
           public XorT<ST,T> fromMaybe(Function<? super Xor<?,T>,? extends Maybe<Xor<ST,T>>> conv){
               return  XorT.of(AnyM.fromMaybe(conv.apply(value.toXor())));     
           }
           public XorT<ST,T> fromEval(Function<? super Xor<?,T>,? extends Eval<Xor<ST,T>>> conv){
               return  XorT.of(AnyM.fromEval(conv.apply(value.toXor())));     
           }
           public XorT<ST,T> fromFutureW(Function<? super Xor<?,T>,? extends FutureW<Xor<ST,T>>> conv){
               return  XorT.of(AnyM.fromFutureW(conv.apply(value.toXor())));     
           }
           public XorT<ST,T> fromOptional(Function<? super Xor<?,T>,? extends Optional<Xor<ST,T>>> conv){
               return  XorT.of(AnyM.fromOptional(conv.apply(value.toXor())));     
           }
           public XorT<ST,T> fromCompletableFuture(Function<? super Xor<?,T>,? extends CompletableFuture<Xor<ST,T>>> conv){
               return  XorT.of(AnyM.fromCompletableFuture(conv.apply(value.toXor())));     
           }
           public XorT<ST,T> fromXor(Function<? super Xor<?,T>,? extends Xor<ST,Xor<ST,T>>> conv){
               return  XorT.of(AnyM.fromXor(conv.apply(value.toXor())));     
           }
           public XorT<ST,T> fromTry(Function<? super Xor<?,T>,? extends Try<Xor<ST,T>,?>> conv){
               return  XorT.of(AnyM.fromTry(conv.apply(value.toXor())));     
           }
       }
      
       public  Transformers<T>.TryTBuilder tryTBuilder(){
           return new TryTBuilder();
       }
       public class TryTBuilder{
           public TryT<T,Throwable> fromMaybe(Function<? super Try<T,Throwable>,? extends Maybe<Try<T,Throwable>>> conv){
               return  TryT.of(AnyM.fromMaybe(conv.apply(value.toTry())));     
           }
           public TryT<T,?> fromEval(Function<? super Try<T,Throwable>,? extends Eval<Try<T,Throwable>>> conv){
               return  TryT.of(AnyM.fromEval(conv.apply(value.toTry())));     
           }
           public TryT<T,?> fromFutureW(Function<? super Try<T,Throwable>,? extends FutureW<Try<T,Throwable>>> conv){
               return  TryT.of(AnyM.fromFutureW(conv.apply(value.toTry())));     
           }
           public TryT<T,?> fromOptional(Function<? super Try<T,Throwable>,? extends Optional<Try<T,Throwable>>> conv){
               return  TryT.of(AnyM.fromOptional(conv.apply(value.toTry())));     
           }
           public TryT<T,?> fromCompletableFuture(Function<? super Try<T,Throwable>,? extends CompletableFuture<Try<T,Throwable>>> conv){
               return  TryT.of(AnyM.fromCompletableFuture(conv.apply(value.toTry())));     
           }
           public TryT<T,?> fromXor(Function<? super Try<T,Throwable>,? extends Xor<?,Try<T,Throwable>>> conv){
               return  TryT.of(AnyM.fromXor(conv.apply(value.toTry())));     
           }
           public TryT<T,?> fromTry(Function<? super Try<T,Throwable>,? extends Try<Try<T,Throwable>,?>> conv){
               return  TryT.of(AnyM.fromTry(conv.apply(value.toTry())));     
           }
       }
      
   }
   
   default Transformers<T>.MaybeTBuilder toMaybeT(){
       return new Transformers(this).maybeTBuilder();
   }
   default Transformers<T>.EvalTBuilder toEvalT(){
       return new Transformers(this).evalTBuilder();
   }
   default Transformers<T>.OptionalTBuilder toOptionalT(){
       return new Transformers(this).optionalTBuilder();
   }
   default Transformers<T>.CompletableFutureTBuilder toCompletableFutureT(){
       return new Transformers(this).completableFutureTBuilder();
   }
   default <ST> Transformers<T>.XorTBuilder<ST> toXorT(){
       return new Transformers(this).xorTBuilder();
   }
   default Transformers<T>.TryTBuilder toTryT(){
       return new Transformers(this).tryTBuilder();
   }
    

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
	 
	 default <R> R convertTo(Function<? super Maybe<? super T>,? extends R> convertTo){
		 return convertTo.apply(this.toMaybe());
	 }
	 default<R> FutureW<R> convertToAsync(Function<? super CompletableFuture<? super T>,? extends CompletableFuture<R>> convertTo){
		 CompletableFuture<T> future =  this.toCompletableFuture();
		 return FutureW.of(future.<R>thenCompose(t->convertTo.apply(future)));
	 }
	 
	default ReactiveSeq<T> stream() {
			return ReactiveSeq.of(Try.withCatch(()->get(),NoSuchElementException.class)).filter(Try::isSuccess).map(Try::get);
	}
	 /**
	  * @return matchable
	  */
	 default T getMatchable(){
		return get();
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
         return o.isPresent() ? Ior.primary(o.get()) : Ior.secondary(null);
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
		 return Maybe.fromOptional(toOptional());
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
		 Optional<T> opt = this.toOptional();
		 if(opt.isPresent())
			 return this.getClass().getSimpleName()+"[" + opt.get() + "]";
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
	
}
