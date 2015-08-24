package com.aol.simple.react.async.future;



import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;

import com.aol.cyclops.lambda.utils.ExceptionSoftener;
/*
 * @author John McClean
 * assumptions
 * 1. only on thread may join at a time
 * 2. only map / mapAsync/ exceptionally/ allOf and anyOf are neccessary
 */
@AllArgsConstructor
public class FastFuture<T> {

	@Getter
	private volatile boolean done=false;
	private boolean isFirstAsync=false;
	@Getter
	private boolean completedExceptionally=false;
	private Object result;
	
	@Wither
	ExecutionPipeline builder;
	final FinalPipeline pipeline;

	volatile int count=0;
	private final int max;
	
	
	Executor exec;
	
	public FastFuture(){
		max=0;
		this.builder = new ExecutionPipeline();
		this.pipeline = null;
	}
	
	private FastFuture(FinalPipeline pipeline,int max){
		this.max = max;
		this.builder = new ExecutionPipeline();
		this.pipeline = null;
	}
	
	public T join(){
		CompletableFuture<T> f;
		long spin=1;
		while(!done){
			LockSupport.parkNanos(spin++);
		}
		while(result==null){
			Thread.yield();
		}
		return (T)result;	
	}
	public static <T> FastFuture<T> completedFuture(T value){
		FastFuture<T> f = new FastFuture();
		f.result =value;
		return f;
	}
	
	public CompletableFuture<T> toCompletableFuture(){
		CompletableFuture<T> f = new CompletableFuture<>();
		this.peek(i->f.complete(i));
		return f;
		
	}
	public static <T>FastFuture<T> fromCompletableFuture(CompletableFuture<T> cf){
		FastFuture<T> f = new FastFuture<>();
		cf.thenAccept(i->f.set(i));
		return f;
	}
	
	
	
	public static <R> FastFuture<List<R>> allOf(FastFuture... futures){
		
		FastFuture allOf = new FastFuture(FinalPipeline.empty(),futures.length);
		
		for(FastFuture next : futures){
			next.peek(v->{ 
					allOf.count++;
					if(allOf.count==allOf.max){
						allOf.result =1;
						allOf.done();
					}
					
			});
		}
		return allOf;
	}
	public static <R> FastFuture<List<R>> anyOf(FastFuture... futures){
		
		FastFuture anyOf = new FastFuture();
		
		for(FastFuture next : futures){
			next.peek(v->anyOf.done=true);
		}
		return anyOf;
	}
	
	//Stream.of(values).map(v->fastFutures.next().set(v))
	public <R> R set(T result){
		return set(()->result);
	}
	public <R> R set(Supplier<T> result){
		try{
			final Object use = result.get();
			Function op = pipeline.functions[0];
			if(this.isFirstAsync){
				this.exec.execute(()->{
					set(()->(T)op.apply(use),1);
				});
				return (R)result;
			}else{
				return set(result,0);
			}
		}catch(Throwable t){
			completedExceptionally =true;
			ExceptionSoftener.singleton.factory.getInstance().throwSoftenedException(t);
		}
		return (R)result;
	}
	public <R> R set(Supplier<T> result,int index){
		try{
			Object current = result;
			Function op = pipeline.functions[index];
			current = op.apply(current);
			final Object use = current;
			if(index+1<pipeline.functions.length){
					this.exec.execute(()->{
						set(()->(T)op.apply(use),index+1);
					});
					return (R)result;
			}
			
			this.result = current;
			done();
			
			
		}catch(Throwable t){
			completedExceptionally =true;
			ExceptionSoftener.singleton.factory.getInstance().throwSoftenedException(t);
		}
		return (R)this.result;
	}
	private boolean done(){
		return this.done =true;
	}
	
	public void clearFast() {
		result =null;
		this.done=false;	
	}
	
	public void cancel(boolean cancel){
		
	}
	public <R> FastFuture<R> thenCompose(Function<T,CompletableFuture<R>> fn){
		return (FastFuture)this.withBuilder(builder.thenCompose((Function)fn));
	}
	public <R> FastFuture<R> thenComposeAsync(Function<T,CompletableFuture<R>> fn,Executor exec){
		return (FastFuture)this.withBuilder(builder.thenComposeAsync((Function)fn, exec));
	}
	public <R> FastFuture<R> thenApplyAsync(Function<T,R> fn,Executor exec){
		return (FastFuture)this.withBuilder(builder.thenApplyAsync(fn, exec));
	}
	public  FastFuture<T> peek(Consumer<T> c){
		this.builder = builder.peek(c);
		return this;
	}
	public <R> FastFuture<R> thenApply(Function<T,R> fn){
		return (FastFuture)this.withBuilder(builder.thenApply(fn));
	}
	public <X extends Throwable> FastFuture<T> exceptionally(Function<X,T> fn){
		return (FastFuture)this.withBuilder(builder.exceptionally(fn));
	}
	public <X extends Throwable> FastFuture<T> whenComplete(BiConsumer<T,X> fn){
		return (FastFuture)this.withBuilder(builder.whenComplete(fn));
	}
	public FastFuture<T> build() {
		return new FastFuture(this.builder.toFinalPipeline(),0);
	}
	
	
}
