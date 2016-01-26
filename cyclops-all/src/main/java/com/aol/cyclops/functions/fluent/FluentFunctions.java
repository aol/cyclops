package com.aol.cyclops.functions.fluent;


import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;

import org.jooq.lambda.fi.util.function.CheckedBiConsumer;
import org.jooq.lambda.fi.util.function.CheckedBiFunction;
import org.jooq.lambda.fi.util.function.CheckedConsumer;
import org.jooq.lambda.fi.util.function.CheckedFunction;
import org.jooq.lambda.fi.util.function.CheckedSupplier;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;

import com.aol.cyclops.closures.mutable.MutableInt;
import com.aol.cyclops.functions.TriFunction;
import com.aol.cyclops.functions.caching.Cacheable;
import com.aol.cyclops.functions.caching.Memoize;
import com.aol.cyclops.functions.currying.Curry;
import com.aol.cyclops.functions.currying.PartialApplicator;
import com.aol.cyclops.invokedynamic.CheckedTriFunction;
import com.aol.cyclops.invokedynamic.ExceptionSoftener;
import com.aol.cyclops.matcher.builders.CheckValues;
import com.aol.cyclops.matcher.recursive.Matchable;
import com.aol.cyclops.monad.AnyM;
import com.aol.cyclops.monad.functions.LiftMFunctions;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.trycatch.Try;
public class FluentFunctions {

	public static <R> FluentFunctions.s<R> ofChecked(CheckedSupplier<R> fn){
		return FluentFunctions.of(ExceptionSoftener.softenSupplier(fn));
	}
	public static <R> FluentFunctions.s<R> of(Supplier<R> fn){
		return new s<>(fn);
	}
	public static <T,R> FluentFunctions.f1<T,R> ofChecked(CheckedFunction<T,R> fn){
		return FluentFunctions.of(ExceptionSoftener.softenFunction(fn));
	}
	public static <T,R> FluentFunctions.f1<T,R> of(Function<T,R> fn){
		return new f1<>(fn);
	}
	public static <T1,T2,R> FluentFunctions.f2<T1,T2,R> ofChecked(CheckedBiFunction<T1,T2,R> fn){
		return FluentFunctions.of(ExceptionSoftener.softenBiFunction(fn));
	}
	public static <T1,T2,R> FluentFunctions.f2<T1,T2,R> of(BiFunction<T1,T2,R> fn){
		return new f2<>(fn);
	}
	public static <T1,T2,T3,R> FluentFunctions.f3<T1,T2,T3,R> ofChecked(CheckedTriFunction<T1,T2,T3,R> fn){
		
		return new f3<>(softenTriFunction(fn));
	}
	public static <T1,T2,T3,R> FluentFunctions.f3<T1,T2,T3,R> of(TriFunction<T1,T2,T3,R> fn){
		return new f3<>(fn);
	}
	
	public static <T> FluentFunctions.f1<T,T> expression(Consumer<T> fn){
		return FluentFunctions.of(t->{
			fn.accept(t);
			return t;
		});
	}
	public static <T> FluentFunctions.f1<T,T> checkedExpression(CheckedConsumer<T> fn){
		final Consumer<T> toUse = ExceptionSoftener.softenConsumer(fn);
		return FluentFunctions.of(t->{
			toUse.accept(t);
			return t;
		});
	}
	public static <T1,T2> FluentFunctions.f2<T1,T2,Tuple2<T1,T2>> expression(BiConsumer<T1,T2> fn){
		return FluentFunctions.of((t1,t2)->{
			fn.accept(t1,t2);
			return Tuple.tuple(t1,t2);
		});
	}
	public static <T1,T2> FluentFunctions.f2<T1,T2,Tuple2<T1,T2>> checkedExpression(CheckedBiConsumer<T1,T2> fn){
		final BiConsumer<T1,T2> toUse = ExceptionSoftener.softenBiConsumer(fn);
		return FluentFunctions.of((t1,t2)->{
			toUse.accept(t1,t2);
			return Tuple.tuple(t1,t2);
		});
	}
	public static <T1, T2,T3, R> TriFunction<T1, T2, T3,R> softenTriFunction(CheckedTriFunction<T1, T2,T3, R> fn) {
		return (t1, t2,t3) -> {
			try {
				return fn.apply(t1, t2,t3);
			} catch (Throwable e) {
				throw ExceptionSoftener.throwSoftenedException(e);
			}
		};
	}
	
	@Wither(AccessLevel.PRIVATE)
	@AllArgsConstructor
	public static class s<R> implements Supplier<R>{
		private final Supplier<R> fn;
		private final String name;
		
		public s(Supplier<R> fn){
			this.name = null;
			this.fn = fn;
		}
		
		@Override
		public R get(){
			return fn.get();
		}
		
		
		public s<R> before(Runnable r){
			return withFn(()->{
				r.run();
				return fn.get();
				}
			  );
		}
		public s<R> after(Consumer<R> action){
			return withFn(()->{
				final R result = fn.get();
				action.accept(result);
				return result;
				}
			  );
		}
		
		public s<R> around(Function<Advice0<R>,R> around){
			return withFn(()->around.apply(new Advice0<R>(fn)));
		}
		
		public s<R> memoize(){
			return withFn(Memoize.memoizeSupplier(fn));
		}
		public s<R> memoize(Cacheable<R> cache){
			return withFn(Memoize.memoizeSupplier(fn,cache));
		}
		
		public s<R> name(String name){
			return this.withName(name);
		}
		private String handleNameStart(){
			return name==null ? "(fluent-supplier-" : "("+name+"-";
				
		}
		private String handleNameEnd(){
			return ")";
				
		}
		public s<R> println(){
			return log(s->System.out.println(s),t->t.printStackTrace());
			
		}	
		public s<R> log(Consumer<String> logger,Consumer<Throwable> error){
			return FluentFunctions.of(()->{
				try{
					R result = fn.get();
					logger.accept(handleNameStart()+"Result["+result+"]"+handleNameEnd());
					return result;
				}catch(Throwable t){
					error.accept(t);
					throw ExceptionSoftener.throwSoftenedException(t);
				}
			});
		}	

		public <X extends Throwable> s<R> recover(Class<X> type,Supplier<R> onError){
			return FluentFunctions.of(()->{
				try{
					return fn.get();
				}catch(Throwable t){
					if(type.isAssignableFrom(t.getClass())){
						return onError.get();	
					}
					throw ExceptionSoftener.throwSoftenedException(t);
					
				}
			});
			
		}
		public s<R> retry(int times,int backoffStartTime){
			return FluentFunctions.of(() -> {
				int count = times;
				MutableInt sleep =MutableInt.of(backoffStartTime);
				Throwable exception=null;
				while(count-->0){
					try{
						return fn.get();
					}catch(Throwable e){
						exception = e;
					}
					ExceptionSoftener.softenRunnable(()->Thread.sleep(sleep.get()));
					
					sleep.mutate(s->s*2);
				}
				throw ExceptionSoftener.throwSoftenedException(exception);
				
			});
			
		}
		
		public <R1 >s<R1> matches(Function<CheckValues<R,R1>,CheckValues<R,R1>> case1){
			return FluentFunctions.of(()->Matchable.of(fn.get()).matches(case1));
		}
		public <R1 >s<R1> matches(Function<CheckValues<R,R1>,CheckValues<R,R1>> case1,
									Function<CheckValues<R,R1>,CheckValues<R,R1>> case2){
			
			return FluentFunctions.of(()->Matchable.of(fn.get()).matches(case1,case2));
		}
		public <R1 >s<R1> matches(Function<CheckValues<R,R1>,CheckValues<R,R1>> case1,
				Function<CheckValues<R,R1>,CheckValues<R,R1>> case2,Function<CheckValues<R,R1>,CheckValues<R,R1>> case3){

			return FluentFunctions.of(()->Matchable.of(fn.get()).matches(case1,case2,case3));
		}
		public <R1 >s<R1> matches(Function<CheckValues<R,R1>,CheckValues<R,R1>> case1,
				Function<CheckValues<R,R1>,CheckValues<R,R1>> case2,
				Function<CheckValues<R,R1>,CheckValues<R,R1>> case3,
				Function<CheckValues<R,R1>,CheckValues<R,R1>> case4){

			return FluentFunctions.of(()->Matchable.of(fn.get()).matches(case1,case2,case3,case4));
		}
		public <R1 >s<R1> matches(Function<CheckValues<R,R1>,CheckValues<R,R1>> case1,
				Function<CheckValues<R,R1>,CheckValues<R,R1>> case2,
				Function<CheckValues<R,R1>,CheckValues<R,R1>> case3,
				Function<CheckValues<R,R1>,CheckValues<R,R1>> case4,
				Function<CheckValues<R,R1>,CheckValues<R,R1>> case5){

			return FluentFunctions.of(()->Matchable.of(fn.get()).matches(case1,case2,case3,case4,case5));
		}
			
		
		public SequenceM<R> generate(){
			return SequenceM.generate(fn);
		}
		
		public s<Optional<R>> lift(){
			return new s<>(() -> Optional.ofNullable(fn.get()));
		}
		public <X extends Throwable> s<Try<R,X>> liftTry(Class<X>... classes){
			return FluentFunctions.of(() -> Try.withCatch(()->fn.get(),classes));
		}
		
		public s<AnyM<R>> liftM(){
			return new s<>(()-> AnyM.ofNullable(fn.get()));
		}
		
		public s<CompletableFuture<R>> liftAsync(Executor ex){
			return FluentFunctions.of(()->CompletableFuture.supplyAsync(fn,ex));
		}
		public CompletableFuture<s<R>> async(Executor ex){
			return CompletableFuture.supplyAsync(()->FluentFunctions.of(fn),ex);
		}
		
		
	}
	
	@Wither(AccessLevel.PRIVATE)
	@AllArgsConstructor
	public static class f1<T,R> implements Function<T,R>{
		private final Function<T,R> fn;
		private final String name;
		
		public f1(Function<T,R> fn){
			this.name = null;
			this.fn = fn;
		}
		@Override
		public R apply(T t) {
			return fn.apply(t);
		}
		
		public f1<T,R> before(Consumer<T> action){
			return withFn(t->{
				action.accept(t);
				return fn.apply(t);
				}
			  );
		}

		public f1<T,R> after(BiConsumer<T,R> action){
			return withFn(t->{
				
				final R result = fn.apply(t);
				action.accept(t,result);
				 return result;
				}
			  );
		}
		
		public f1<T,R> around(Function<Advice1<T,R>,R> around){
			return withFn(t->around.apply(new Advice1<T,R>(t,fn)));
		}
		
		public s<R> partiallyApply(T param){
			return new s<>(PartialApplicator.partial(param,fn));
		}
		public f1<T,R> memoize(){
			return withFn(Memoize.memoizeFunction(fn));
		}
		public f1<T,R> memoize(Cacheable<R> cache){
			return withFn(Memoize.memoizeFunction(fn));
		}
		public f1<T,R> name(String name){
			return this.withName(name);
		}
		private String handleNameStart(){
			return name==null ? "(fluent-function-" : "("+name+"-";
				
		}
		private String handleNameEnd(){
			return ")";
				
		}
		public f1<T,R> log(Consumer<String> logger,Consumer<Throwable> error){
			return FluentFunctions.of(t1->{
				
				try{
					logger.accept(handleNameStart()+"Parameter["+t1+"]"+handleNameEnd());
					R result = fn.apply(t1);
					logger.accept(handleNameStart()+"Result["+result+"]"+handleNameEnd());
					return result;
				}catch(Throwable t){
					error.accept(t);
					throw ExceptionSoftener.throwSoftenedException(t);
				}
				
			});
		}	
		public f1<T,R> println(){
			return log(s->System.out.println(s),t->t.printStackTrace());
		}	
		public <X extends Throwable> f1<T,R> recover(Class<X> type,Function<T,R> onError){
			return FluentFunctions.of(t1->{
				try{
					return fn.apply(t1);
				}catch(Throwable t){
					if(type.isAssignableFrom(t.getClass())){
						return onError.apply(t1);
						
					}
					throw ExceptionSoftener.throwSoftenedException(t);
					
				}
			});
		}
		public f1<T,R> retry(int times,int backoffStartTime){
			return FluentFunctions.of(t -> {
				int count = times;
				MutableInt sleep =MutableInt.of(backoffStartTime);
				Throwable exception=null;
				while(count-->0){
					try{
						return fn.apply(t);
					}catch(Throwable e){
						exception = e;
					}
					ExceptionSoftener.softenRunnable(()->Thread.sleep(sleep.get()));
					
					sleep.mutate(s->s*2);
				}
				throw ExceptionSoftener.throwSoftenedException(exception);
				
			});
			
		}
		public <R1> f1<T,R1> matches(Function<CheckValues<R,R1>,CheckValues<R,R1>> case1){
			return FluentFunctions.of(t->Matchable.of(fn.apply(t)).matches(case1));
		}
		public <R1> f1<T,R1> matches(Function<CheckValues<R,R1>,CheckValues<R,R1>> case1,
									Function<CheckValues<R,R1>,CheckValues<R,R1>> case2){
			
			return FluentFunctions.of(t->Matchable.of(fn.apply(t)).matches(case1,case2));
		}
		public <R1> f1<T,R1> matches(Function<CheckValues<R,R1>,CheckValues<R,R1>> case1,
				Function<CheckValues<R,R1>,CheckValues<R,R1>> case2,Function<CheckValues<R,R1>,CheckValues<R,R1>> case3){

			return FluentFunctions.of(t->Matchable.of(fn.apply(t)).matches(case1,case2,case3));
		}
		public <R1> f1<T,R1> matches(Function<CheckValues<R,R1>,CheckValues<R,R1>> case1,
				Function<CheckValues<R,R1>,CheckValues<R,R1>> case2,
				Function<CheckValues<R,R1>,CheckValues<R,R1>> case3,
				Function<CheckValues<R,R1>,CheckValues<R,R1>> case4){

			return FluentFunctions.of(t->Matchable.of(fn.apply(t)).matches(case1,case2,case3,case4));
		}
		public <R1> f1<T,R1> matches(Function<CheckValues<R,R1>,CheckValues<R,R1>> case1,
				Function<CheckValues<R,R1>,CheckValues<R,R1>> case2,
				Function<CheckValues<R,R1>,CheckValues<R,R1>> case3,
				Function<CheckValues<R,R1>,CheckValues<R,R1>> case4,
				Function<CheckValues<R,R1>,CheckValues<R,R1>> case5){

			return FluentFunctions.of(t->Matchable.of(fn.apply(t)).matches(case1,case2,case3,case4,case5));
		}
		public SequenceM<R> iterate(T seed,Function<R,T> mapToType){
			return SequenceM.iterate(fn.apply(seed),t->fn.compose(mapToType).apply(t));
		}
		public SequenceM<R> generate(T input){
			return SequenceM.generate(()->fn.apply(input));
		}
		public f1<Optional<T>,Optional<R>> lift(){
			return new f1<>(opt -> opt.map(t->fn.apply(t)));
		}
		public <X extends Throwable> f1<T,Try<R,X>> liftTry(Class<X>... classes){
			return FluentFunctions.of((t1) -> Try.withCatch(()->fn.apply(t1),classes));
		}
		public f1<AnyM<T>,AnyM<R>> liftM(){
			return FluentFunctions.of(LiftMFunctions.liftM(fn));
		}
		public f1<T,CompletableFuture<R>> liftAsync(Executor ex){
			return FluentFunctions.of(t->CompletableFuture.supplyAsync(()->fn.apply(t),ex));
		}
		public CompletableFuture<f1<T,R>> async(Executor ex){
			return CompletableFuture.supplyAsync(()->FluentFunctions.of(fn),ex);
		}
		
	}
	@Wither(AccessLevel.PRIVATE)
	@AllArgsConstructor
	public static class f2<T1,T2,R> implements BiFunction<T1,T2,R>{
		BiFunction<T1,T2,R> fn;
		private final String name;
		
		public f2(BiFunction<T1,T2,R> fn){
			this.name = null;
			this.fn = fn;
		}
		@Override
		public R apply(T1 t1,T2 t2) {
			return fn.apply(t1,t2);
		}
		
		public f2<T1,T2,R> around(Function<Advice2<T1,T2,R>,R> around){
			return withFn((t1,t2)->around.apply(new Advice2<>(t1,t2,fn)));
		}
		
		public f1<T2,R> partiallyApply(T1 param){
			return new f1<>(PartialApplicator.partial2(param,fn));
		}
		public s<R> partiallyApply(T1 param1,T2 param2){
			return new s<>(PartialApplicator.partial2(param1,param2,fn));
		}
		public f1<T1,Function<T2,R>> curry(){
			return new f1<>(Curry.curry2(fn));
		}
		public f2<T1,T2,R> memoize(){
			return withFn(Memoize.memoizeBiFunction(fn));
		}
		public f2<T1,T2,R> memoize(Cacheable<R> cache){
			return withFn(Memoize.memoizeBiFunction(fn));
		}
		public f2<T1,T2,R> name(String name){
			return this.withName(name);
		}
		private String handleNameStart(){
			return name==null ? "(fluent-function-" : "("+name+"-";
				
		}
		private String handleNameEnd(){
			return ")";
				
		}
		public f2<T1,T2,R> log(Consumer<String> logger,Consumer<Throwable> error){
			return FluentFunctions.of((t1,t2)->{
				try{
					logger.accept(handleNameStart()+"Parameters["+t1+","+t2+"]"+handleNameEnd());
					R result = fn.apply(t1,t2);
					logger.accept(handleNameStart()+"Result["+result+"]"+handleNameEnd());
					return result;
				}catch(Throwable t){
					error.accept(t);
					throw ExceptionSoftener.throwSoftenedException(t);
				}
			});
		}	
		public f2<T1,T2,R> println(){
			return log(s->System.out.println(s),t->t.printStackTrace());
		}	
		
		public <X extends Throwable> f2<T1,T2,R> recover(Class<X> type,BiFunction<T1,T2,R> onError){
			return FluentFunctions.of((t1,t2)->{
				try{
					return fn.apply(t1,t2);
				}catch(Throwable t){
					if(type.isAssignableFrom(t.getClass())){
						return onError.apply(t1,t2);
						
					}
					throw ExceptionSoftener.throwSoftenedException(t);
					
				}
			});
			
		}
		public f2<T1,T2,R> retry(int times,int backoffStartTime){
			return FluentFunctions.of((t1,t2) -> {
				int count = times;
				MutableInt sleep =MutableInt.of(backoffStartTime);
				Throwable exception=null;
				while(count-->0){
					try{
						return fn.apply(t1,t2);
					}catch(Throwable e){
						exception = e;
					}
					ExceptionSoftener.softenRunnable(()->Thread.sleep(sleep.get()));
					
					sleep.mutate(s->s*2);
				}
				throw ExceptionSoftener.throwSoftenedException(exception);
				
			});
			
		}
		public <R1> f2<T1,T2,R1> matches(Function<CheckValues<R,R1>,CheckValues<R,R1>> case1){
			return FluentFunctions.of((t1,t2)->Matchable.of(fn.apply(t1,t2)).matches(case1));
		}
		public <R1> f2<T1,T2,R1> matches(Function<CheckValues<R,R1>,CheckValues<R,R1>> case1,
									Function<CheckValues<R,R1>,CheckValues<R,R1>> case2){
			
			return FluentFunctions.of((t1,t2)->Matchable.of(fn.apply(t1,t2)).matches(case1,case2));
		}
		public <R1> f2<T1,T2,R1> matches(Function<CheckValues<R,R1>,CheckValues<R,R1>> case1,
				Function<CheckValues<R,R1>,CheckValues<R,R1>> case2,Function<CheckValues<R,R1>,CheckValues<R,R1>> case3){

			return FluentFunctions.of((t1,t2)->Matchable.of(fn.apply(t1,t2)).matches(case1,case2,case3));
		}
		public <R1> f2<T1,T2,R1> matches(Function<CheckValues<R,R1>,CheckValues<R,R1>> case1,
				Function<CheckValues<R,R1>,CheckValues<R,R1>> case2,
				Function<CheckValues<R,R1>,CheckValues<R,R1>> case3,
				Function<CheckValues<R,R1>,CheckValues<R,R1>> case4){

			return FluentFunctions.of((t1,t2)->Matchable.of(fn.apply(t1,t2)).matches(case1,case2,case3,case4));
		}
		public <R1> f2<T1,T2,R1> matches(Function<CheckValues<R,R1>,CheckValues<R,R1>> case1,
				Function<CheckValues<R,R1>,CheckValues<R,R1>> case2,
				Function<CheckValues<R,R1>,CheckValues<R,R1>> case3,
				Function<CheckValues<R,R1>,CheckValues<R,R1>> case4,
				Function<CheckValues<R,R1>,CheckValues<R,R1>> case5){

			return FluentFunctions.of((t1,t2)->Matchable.of(fn.apply(t1,t2)).matches(case1,case2,case3,case4,case5));
		}
		
		public SequenceM<R> iterate(T1 seed1,T2 seed2,Function<R,Tuple2<T1,T2>> mapToType){
			return SequenceM.iterate(fn.apply(seed1,seed2),t->{ 
				Tuple2<T1,T2> tuple =mapToType.apply(t);
				return fn.apply(tuple.v1,tuple.v2);
			});
		}
		public SequenceM<R> generate(T1 input1,T2 input2){
			return SequenceM.generate(()->fn.apply(input1,input2));
		}
		
		public f2<Optional<T1>,Optional<T2>,Optional<R>> lift(){
			return new f2<>((opt1,opt2) -> opt1.flatMap(t1-> opt2.map(t2->fn.apply(t1,t2))));
		}
		public <X extends Throwable> f2<T1,T2,Try<R,X>> liftTry(Class<X>... classes){
			return FluentFunctions.of((t1,t2) -> Try.withCatch(()->fn.apply(t1,t2),classes));
		}
		
		public f2<AnyM<T1>,AnyM<T2>,AnyM<R>> liftM(){
			return FluentFunctions.of(LiftMFunctions.liftM2(fn));
		}
		public f2<T1,T2,CompletableFuture<R>> liftAsync(Executor ex){
			return FluentFunctions.of((t1,t2)->CompletableFuture.supplyAsync(()->fn.apply(t1,t2),ex));
		}
		public CompletableFuture<f2<T1,T2,R>> async(Executor ex){
			return CompletableFuture.supplyAsync(()->FluentFunctions.of(fn),ex);
		}
		
	}
	@Wither(AccessLevel.PRIVATE)
	@AllArgsConstructor
	public static class f3<T1,T2,T3,R> implements TriFunction<T1,T2,T3,R>{
		private  final TriFunction<T1,T2,T3,R> fn;
		private final String name;
		
		public f3(TriFunction<T1,T2,T3,R> fn){
			this.name = null;
			this.fn = fn;
		}
		
		@Override
		public R apply(T1 t1,T2 t2,T3 t3) {
			return fn.apply(t1,t2,t3);
		}
		
		public f3<T1,T2,T3,R> around(Function<Advice3<T1,T2,T3,R>,R> around){
			return withFn((t1,t2,t3)->around.apply(new Advice3<>(t1,t2,t3,fn)));
		}
		public f2<T2,T3,R> partiallyApply(T1 param){
			return new f2<>(PartialApplicator.partial3(param,fn));
		}
		public f1<T3,R> partiallyApply(T1 param1,T2 param2){
			return new f1<>(PartialApplicator.partial3(param1,param2,fn));
		}
		public s<R> partiallyApply(T1 param1,T2 param2,T3 param3){
			return new s<>(PartialApplicator.partial3(param1,param2,param3,fn));
		}
		public f1<T1,Function<T2,Function<T3,R>>> curry(){
			return new f1<>(Curry.curry3(fn));
		}
		public f3<T1,T2,T3,R> memoize(){
			return withFn(Memoize.memoizeTriFunction(fn));
		}
		public f3<T1,T2,T3,R> memoize(Cacheable<R> cache){
			return withFn(Memoize.memoizeTriFunction(fn));
		}
		public f3<T1,T2,T3,R> name(String name){
			return this.withName(name);
		}
		private String handleNameStart(){
			return name==null ? "(fluent-function-" : "("+name+"-";
				
		}
		private String handleNameEnd(){
			return ")";
				
		}
		public f3<T1,T2,T3,R> log(Consumer<String> logger,Consumer<Throwable> error){
			return FluentFunctions.of((t1,t2,t3)->{
				try{
					logger.accept(handleNameStart()+"Parameters["+t1+","+t2+","+t3+"]"+handleNameEnd());
					R result = fn.apply(t1,t2,t3);
					logger.accept(handleNameStart()+"Result["+result+"]"+handleNameEnd());
					return result;
				}catch(Throwable t){
					error.accept(t);
					throw ExceptionSoftener.throwSoftenedException(t);
				}
			});
		}	
		public f3<T1,T2,T3,R> println(){
			return log(s->System.out.println(s),t->t.printStackTrace());
		}	
		
		public <X extends Throwable> f3<T1,T2,T3,R> recover(Class<X> type,TriFunction<T1,T2,T3,R> onError){
			return FluentFunctions.of((t1,t2,t3)->{
				try{
					return fn.apply(t1,t2,t3);
				}catch(Throwable t){
					if(type.isAssignableFrom(t.getClass())){
						return onError.apply(t1,t2,t3);
						
					}
					throw ExceptionSoftener.throwSoftenedException(t);
					
				}
			});
			
		}
		public f3<T1,T2,T3,R> retry(int times,int backoffStartTime){
			return FluentFunctions.of((t1,t2,t3) -> {
				int count = times;
				MutableInt sleep =MutableInt.of(backoffStartTime);
				Throwable exception=null;
				while(count-->0){
					try{
						return fn.apply(t1,t2,t3);
					}catch(Throwable e){
						exception = e;
					}
					ExceptionSoftener.softenRunnable(()->Thread.sleep(sleep.get()));
					
					sleep.mutate(s->s*2);
				}
				throw ExceptionSoftener.throwSoftenedException(exception);
				
			});
			
		}
		public <R1> f3<T1,T2,T3,R1> matches(Function<CheckValues<R,R1>,CheckValues<R,R1>> case1){
			return FluentFunctions.of((t1,t2,t3)->Matchable.of(fn.apply(t1,t2,t3)).matches(case1));
		}
		public <R1> f2<T1,T2,R1> matches(Function<CheckValues<R,R1>,CheckValues<R,R1>> case1,
									Function<CheckValues<R,R1>,CheckValues<R,R1>> case2){
			
			return FluentFunctions.of((t1,t2)->Matchable.of(fn.apply(t1,t2)).matches(case1,case2));
		}
		public <R1> f3<T1,T2,T3,R1> matches(Function<CheckValues<R,R1>,CheckValues<R,R1>> case1,
				Function<CheckValues<R,R1>,CheckValues<R,R1>> case2,Function<CheckValues<R,R1>,CheckValues<R,R1>> case3){

			return FluentFunctions.of((t1,t2,t3)->Matchable.of(fn.apply(t1,t2,t3)).matches(case1,case2,case3));
		}
		public <R1> f3<T1,T2,T3,R1> matches(Function<CheckValues<R,R1>,CheckValues<R,R1>> case1,
				Function<CheckValues<R,R1>,CheckValues<R,R1>> case2,
				Function<CheckValues<R,R1>,CheckValues<R,R1>> case3,
				Function<CheckValues<R,R1>,CheckValues<R,R1>> case4){

			return FluentFunctions.of((t1,t2,t3)->Matchable.of(fn.apply(t1,t2,t3)).matches(case1,case2,case3,case4));
		}
		public <R1> f3<T1,T2,T3,R1> matches(Function<CheckValues<R,R1>,CheckValues<R,R1>> case1,
				Function<CheckValues<R,R1>,CheckValues<R,R1>> case2,
				Function<CheckValues<R,R1>,CheckValues<R,R1>> case3,
				Function<CheckValues<R,R1>,CheckValues<R,R1>> case4,
				Function<CheckValues<R,R1>,CheckValues<R,R1>> case5){

			return FluentFunctions.of((t1,t2,t3)->Matchable.of(fn.apply(t1,t2,t3)).matches(case1,case2,case3,case4,case5));
		}
		
		public SequenceM<R> iterate(T1 seed1,T2 seed2,T3 seed3,Function<R,Tuple3<T1,T2,T3>> mapToType){
			return SequenceM.iterate(fn.apply(seed1,seed2,seed3),t->{ 
				Tuple3<T1,T2,T3> tuple =mapToType.apply(t);
				return fn.apply(tuple.v1,tuple.v2,tuple.v3);
			});
		}
		public SequenceM<R> generate(T1 input1,T2 input2,T3 input3){
			return SequenceM.generate(()->fn.apply(input1,input2,input3));
		}
		
		public f3<Optional<T1>,Optional<T2>,Optional<T3>,Optional<R>> lift(){
			return new f3<>((opt1,opt2,opt3) -> opt1.flatMap(t1-> opt2.flatMap(t2-> opt3.map(t3->fn.apply(t1,t2,t3)))));
		}
		public <X extends Throwable> f3<T1,T2,T3,Try<R,X>> liftTry(Class<X>... classes){
			return FluentFunctions.of((t1,t2,t3) -> Try.withCatch(()->fn.apply(t1,t2,t3),classes));
		}
		
		public f3<AnyM<T1>,AnyM<T2>,AnyM<T3>,AnyM<R>> liftM(){
			return FluentFunctions.of(LiftMFunctions.liftM3(fn));
		}
		public f3<T1,T2,T3,CompletableFuture<R>> liftAsync(Executor ex){
			return FluentFunctions.of((t1,t2,t3)->CompletableFuture.supplyAsync(()->fn.apply(t1,t2,t3),ex));
		}
		public CompletableFuture<f3<T1,T2,T3,R>> async(Executor ex){
			return CompletableFuture.supplyAsync(()->FluentFunctions.of(fn),ex);
		}
		
	}
	
	@AllArgsConstructor
	public static class Advice0<R>{
		
		private final Supplier<R> fn;
		
		public R proceed(){
			return fn.get();
		}
		
	}
	
	@AllArgsConstructor
	public static class Advice1<T,R>{
		public final T param;
		private final Function<T,R> fn;
		
		public R proceed(){
			return fn.apply(param);
		}
		public R proceed(T param){
			return fn.apply(param);
		}
	}
	@AllArgsConstructor
	public static class Advice2<T1,T2,R>{
		public final T1 param1;
		public final T2 param2;
		private final BiFunction<T1,T2,R> fn;
		
		public R proceed(){
			return fn.apply(param1,param2);
		}
		public R proceed(T1 param1, T2 param2){
			return fn.apply(param1,param2);
		}
		public R proceed1(T1 param){
			return fn.apply(param,param2);
		}
		public R proceed2(T2 param){
			return fn.apply(param1,param);
		}
	}
	@AllArgsConstructor
	public static class Advice3<T1,T2,T3,R>{
		public final T1 param1;
		public final T2 param2;
		public final T3 param3;
		private final TriFunction<T1,T2,T3,R> fn;
		
		public R proceed(){
			return fn.apply(param1,param2,param3);
		}
		public R proceed(T1 param1, T2 param2,T3 param3){
			return fn.apply(param1,param2,param3);
		}
		public R proceed1(T1 param){
			return fn.apply(param,param2,param3);
		}
		public R proceed2(T2 param){
			return fn.apply(param1,param,param3);
		}
		public R proceed3(T3 param){
			return fn.apply(param1,param2,param);
		}
	}
}
