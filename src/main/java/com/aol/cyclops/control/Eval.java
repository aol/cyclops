package com.aol.cyclops.control;

import static com.aol.cyclops.control.For.Values.each2;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.reactivestreams.Publisher;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.Reducer;
import com.aol.cyclops.Semigroup;
import com.aol.cyclops.control.Eval.Module.EvalSemigroupApplyer;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.persistent.PVectorX;
import com.aol.cyclops.data.collections.extensions.standard.DequeX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.ConvertableFunctor;
import com.aol.cyclops.types.Filterable;
import com.aol.cyclops.types.Functor;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.Value;
import com.aol.cyclops.types.applicative.ApplicativeFunctor;
import com.aol.cyclops.types.stream.reactive.ValueSubscriber;
import com.aol.cyclops.util.function.Memoize;

/**
 * Represents a computation that can be defered (always), cached (later) or immediate(now).
 * Supports tail recursion via map / flatMap. 
 * Computations are always Lazy even when performed against a Now instance. 
 * Heavily inspired by Cats Eval @link https://github.com/typelevel/cats/blob/master/core/src/main/scala/cats/Eval.scala
 * 
 * Tail Recursion example
 * <pre>
 * {@code 
 * 
 * public void odd(){
        System.out.println(even(Eval.now(200000)).get());
    }
    public Eval<String> odd(Eval<Integer> n )  {
       
       return n.flatMap(x->even(Eval.now(x-1)));
    }
    public Eval<String> even(Eval<Integer> n )  {
        return n.flatMap(x->{
            return x<=0 ? Eval.now("done") : odd(Eval.now(x-1));
        });
     }
 * }
 * </pre>
 * 
 * @author johnmcclean
 *
 * @param <T>
 */
public interface Eval<T> extends Supplier<T>, 
                                 MonadicValue<T>, 
                                 Functor<T>, 
                                 Filterable<T>, 
                                 ApplicativeFunctor<T>,
                                 Matchable.ValueAndOptionalMatcher<T>{

    public static <T> Eval<T> fromPublisher(Publisher<T> pub){
        ValueSubscriber<T> sub = ValueSubscriber.subscriber();
        pub.subscribe(sub);
        return sub.toEvalLater();
    }
    public static <T> Eval<T> fromIterable(Iterable<T> iterable){
        Iterator<T> it = iterable.iterator();
        return Eval.later(()->it.hasNext() ? it.next() : null);
    }
	public static<T> Eval<T> now(T value){
	    return always(()->value);
		
	}
	public static<T> Eval<T> later(Supplier<T> value){
		
		return new Module.Later<T>(in->value.get());
	}
	public static<T> Eval<T> always(Supplier<T> value){
		return new Module.Always<T>(in->value.get());
	}
	
	public static <T> Eval<ListX<T>> sequence(CollectionX<Eval<T>> evals){
	    return sequence(evals.stream()).map(s->s.toListX());
		
	}
	public static <T> Eval<ReactiveSeq<T>> sequence(Stream<Eval<T>> evals){
	    return AnyM.sequence(evals.map(f->AnyM.fromEval(f)),
                ()->AnyM.fromEval(Eval.now(ReactiveSeq.<T>empty()))
	             ).map(s->ReactiveSeq.fromStream(s))
                
                .unwrap();
        
    }
	
	public static <T,R> Eval<R> accumulate(CollectionX<Eval<T>> evals,Reducer<R> reducer){
		return sequence(evals).map(s->s.mapReduce(reducer));
	}
	public static <T,R> Eval<R> accumulate(CollectionX<Eval<T>> maybes,Function<? super T, R> mapper,Semigroup<R> reducer){
		return sequence(maybes).map(s->s.map(mapper).reduce(reducer.reducer()).get());
	}
	public static <T> Eval<T> accumulate(CollectionX<Eval<T>> maybes,Semigroup<T> reducer){
        return sequence(maybes).map(s->s.reduce(reducer.reducer()).get());
    }
	public <T> Eval<T> unit(T unit);
	public <R> Eval<R> map(Function<? super T, ? extends R> mapper);
	public <R> Eval<R> flatMap(Function<? super T, ? extends Eval<? extends R>> mapper);
	
	default PVectorX<Function<Object,Object>> steps(){
	    return PVectorX.of(__->get());
	}
	
	
	
	/* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#coflatMap(java.util.function.Function)
     */
    @Override
    default <R> Eval<R> coflatMap(Function<? super MonadicValue<T>, R> mapper) {
        return (Eval<R>)MonadicValue.super.coflatMap(mapper);
    }
    default Eval<T> combine(Monoid<T> monoid, Eval<? extends T> v2){
        return unit(each2(this, t1->v2, (t1,t2)->monoid.combiner().apply(t1, t2))
                .orElseGet(()->this.orElseGet(()->monoid.zero())));
    }
    
    default Eval<MonadicValue<T>> nest(){
        return (Eval<MonadicValue<T>>)MonadicValue.super.nest();
    }
   
    public T get();
	
	
	@Override
    default <U> Maybe<U> ofType(Class<? extends U> type) {
       
        return (Maybe<U>)Filterable.super.ofType(type);
    }
    @Override
    default Maybe<T> filterNot(Predicate<? super T> fn) {
       
        return (Maybe<T>)Filterable.super.filterNot(fn);
    }
    @Override
    default Maybe<T> notNull() {
       
        return (Maybe<T>)Filterable.super.notNull();
    }
    default Maybe<T> filter(Predicate<? super T> pred){
	    return toMaybe().filter(pred);
	}
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#cast(java.lang.Class)
	 */
	@Override
	default <U> Eval<U> cast(Class<? extends U> type) {
		return (Eval<U>)ApplicativeFunctor.super.cast(type);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#peek(java.util.function.Consumer)
	 */
	@Override
	default Eval<T> peek(Consumer<? super T> c) {
		return (Eval<T>)ApplicativeFunctor.super.peek(c);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#trampoline(java.util.function.Function)
	 */
	@Override
	default <R> Eval<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
		
		return (Eval<R>)ApplicativeFunctor.super.trampoline(mapper);
	}
	default Eval<CompletableFuture<T>> asyncNow(Executor ex){
		return Eval.now(this.toCompletableFutureAsync(ex));
	}
	default Eval<CompletableFuture<T>> asyncNow(){
		return Eval.now(this.toCompletableFuture());
	}
	default Eval<CompletableFuture<T>> asyncLater(Executor ex){
		return Eval.later(()->this.toCompletableFutureAsync(ex));
	}
	default Eval<CompletableFuture<T>> asyncLater(){
		return Eval.later(()->this.toCompletableFutureAsync());
	}
	default Eval<CompletableFuture<T>> asyncAlways(Executor ex){
		return Eval.always(()->this.toCompletableFutureAsync(ex));
	}
	default Eval<CompletableFuture<T>> asyncAlways(){
		return Eval.always(()->this.toCompletableFutureAsync());
	}
	@Override
	default <R> R visit(Function<? super T,? extends R> present,Supplier<? extends R> absent){
	    T value = get();
        if(value!=null)
              return present.apply(value);
        return absent.get();
    }
	
    static <R> Eval<R> narrow(Eval<? extends R> broad){
		return (Eval<R>)broad;
	}

    @Override
    default  EvalSemigroupApplyer<T> ap(BiFunction<T,T,T> fn){
        return  new EvalSemigroupApplyer<T>(fn, this);
    }
    @Override
    default  EvalSemigroupApplyer<T> ap(Semigroup<T> fn){
        return  new EvalSemigroupApplyer<>(fn.combiner(), this);
    }
    /**
     * Apply a function across two values at once.
     * 
     * @param app
     * @param fn
     * @return
     */
    @Override
    default <T2,R> Eval<R> ap(Value<T2> app, BiFunction<? super T,? super T2,? extends R> fn){
        
        return  (Eval<R> )ApplicativeFunctor.super.ap(app, fn);
    }
    /**
     * Equivalent to ap, but accepts an Iterable and takes the first value only from that iterable.
     * 
     * @param app
     * @param fn
     * @return
     */
    @Override
    default <T2,R> Eval<R> zip(Iterable<T2> app,BiFunction<? super T,? super T2,? extends R> fn){
        
        return  (Eval<R> )ApplicativeFunctor.super.zip(app, fn);
    } 
    /**
     * Equivalent to ap, but accepts a Publisher and takes the first value only from that publisher.
     * 
     * @param app
     * @param fn
     * @return
     */
    @Override
    default <T2,R> Eval<R> zip(BiFunction<? super T,? super T2,? extends R> fn,Publisher<T2> app){
        return  (Eval<R> )ApplicativeFunctor.super.zip(fn,app);
        
    } 
     
	
    
	static class Module{
	    public static class EvalSemigroupApplyer<T> extends SemigroupApplyer<T> {
            
	           public Eval<T> eval(){
	                return (Eval<T>)super.functor;
	            }
	            
	           
	            /* (non-Javadoc)
	             * @see com.aol.cyclops.types.applicative.Applicativable.SemigroupApplyer#withFunctor(com.aol.cyclops.types.ConvertableFunctor)
	             */
	            @Override
	            public EvalSemigroupApplyer<T> withFunctor(ConvertableFunctor<T> functor) {
	               
	                return new EvalSemigroupApplyer<T>(super.combiner,super.functor);
	            }


	            public EvalSemigroupApplyer<T> ap(ConvertableFunctor<T> fn) {
	                
	              return  withFunctor(eval().ap(fn, super.combiner));
	             
	            }
	            

	            public EvalSemigroupApplyer(BiFunction<T, T, T> combiner, ConvertableFunctor<T> functor) {
	                super(combiner, functor);
	                
	            }
	        }
	    public static class Later<T> extends Rec<T> implements Eval<T>{
	        
	        
	        Later(Function <Object,? extends T> s){
	            super(PVectorX.of(Rec.raw(Memoize.memoizeFunction(s))));    
	        }
	        Later(PVectorX<Function<Object,Object>> s){
	            super(s);
	          
	        }
	        public <R> Eval<R> map(Function<? super T, ? extends R> mapper){
	            
	            return new Later<R>(super.fns.plus(Rec.raw(Memoize.memoizeFunction(mapper))));
	        }
	        public <R>  Eval<R> flatMap(Function<? super T, ? extends Eval<? extends R>> mapper){
	            RecFunction s = __ -> mapper.apply(super.apply()).steps();
	            return  new Later<R>(PVectorX.of(s));
	            
	        }
	        @Override
	        public T get() {
	            return super.get();
	        }
	        /* (non-Javadoc)
	         * @see com.aol.cyclops.lambda.monads.Unit#unit(java.lang.Object)
	         */
	        @Override
	        public <T> Eval<T> unit(T unit) {
	            return Eval.later(()->unit);
	        }
	        /* (non-Javadoc)
	         * @see com.aol.cyclops.value.Value#toEvalLater()
	         */
	        @Override
	        public Eval<T> toEvalLater() {
	            return this;
	        }
	        /* (non-Javadoc)
	         * @see java.lang.Object#hashCode()
	         */
	        @Override
	        public int hashCode() {
	            return get().hashCode();
	        }
	        /* (non-Javadoc)
	         * @see java.lang.Object#equals(java.lang.Object)
	         */
	        @Override
	        public boolean equals(Object obj) {
	            if(!(obj instanceof Eval))
	                return false;
	            return Objects.equals(get(), ((Eval)obj).get());
	        }
	        @Override
	        public String toString(){
	            return mkString();
	        }
	        
	        
	        
	    }
	    public static class Always<T> extends Rec<T> implements Eval<T>{
	       
	        Always(Function <Object,? extends T> s){
	            super(PVectorX.of(Rec.raw(s)));   
	        }
	        Always(PVectorX<Function<Object,Object>> s){
	            super(s);
	          
	        }
	        public <R> Eval<R> map(Function<? super T, ? extends R> mapper){
	            
	            return new Always<R>(fns.plus(Rec.raw(mapper)));
	            
	        }
	        public <R>  Eval<R> flatMap(Function<? super T, ? extends Eval<? extends R>> mapper){
	            RecFunction s = __ -> mapper.apply(apply()).steps();
	            return  new Always<R>(PVectorX.of(s));
	        }
	        @Override
	        public T get() {
	            return super.get();
	        }
	        @Override
	        public <T> Eval<T> unit(T unit) {
	            return Eval.always(()->unit);
	        }
	        /* (non-Javadoc)
	         * @see com.aol.cyclops.value.Value#toEvalAlways()
	         */
	        @Override
	        public Eval<T> toEvalAlways() {
	            return this;
	        }
	        /* (non-Javadoc)
	         * @see java.lang.Object#hashCode()
	         */
	        @Override
	        public int hashCode() {
	            return get().hashCode();
	        }
	        /* (non-Javadoc)
	         * @see java.lang.Object#equals(java.lang.Object)
	         */
	        @Override
	        public boolean equals(Object obj) {
	            if(!(obj instanceof Eval))
	                return false;
	            return Objects.equals(get(), ((Eval)obj).get());
	        }
	        @Override
	        public String toString(){
	            return mkString();
	        }
	        
	    }
	private static class Rec<T> {
        final PVectorX<Function<Object,Object>> fns;
        private final static Object VOID = new Object();
       
        Rec(PVectorX<Function<Object,Object>> s){
           fns =s;
        }
        
        private static Function<Object,Object> raw(Function<?,?> fn){
            return (Function<Object,Object>)fn;
        }
        static interface RecFunction extends Function<Object,Object>{
            
        }
        public PVectorX<Function<Object,Object>> steps(){
            return fns;
        }
        T apply(){
            Object input = VOID;
            for(Function<Object,Object> n : fns){
                DequeX<Function<Object,Object>> newFns = DequeX.of(n);
                while(newFns.size()>0){
                    Function<Object,Object> next =newFns.pop();
                    if(next instanceof RecFunction){
                        newFns.plusAll((List)((RecFunction)next).apply(VOID));
                    }
                    else 
                        input = next.apply(input);
                    
                }    
            }
            return (T)input;
        }
       
        public T get() {
            return apply();
        }
        
       
        
    }

	}
	
}
