package com.aol.simple.react.async.future;



import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import uk.co.real_logic.agrona.concurrent.ManyToOneConcurrentArrayQueue;
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
	private Object result = UNSET;
	private Object exception = UNSET;
	private static UnSet UNSET = new UnSet();
	static class UnSet{}
	@Wither
	public ExecutionPipeline builder;
	FinalPipeline pipeline;

	volatile int count=0;
	private final int max;
	
	private Function doAfter;
	Executor exec;
	
	public FastFuture(){
		max=0;
		this.builder = new ExecutionPipeline();
		this.pipeline = null;
	}
	private T result(){
		while(result==UNSET){
			Thread.yield();
		}
		return (T)result;	
	}
	private Throwable exception(){
		while(exception==UNSET){
			Thread.yield();
		}
		return (Throwable)exception;	
	}
	
	private FastFuture(FinalPipeline pipeline,int max){
		this.max = max;
		this.builder = new ExecutionPipeline();
		this.pipeline = pipeline;
	}
	
	public T join(){
		System.out.println("joining! " + done + " : " + result);
		long spin=1;
		while(!done){
			LockSupport.parkNanos(spin++);
		}
		return result();
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
		System.out.println("All off! " + futures.length);
		FastFuture allOf = new FastFuture(FinalPipeline.empty(),futures.length);
		
		
		for(FastFuture next : futures){
			next.peek(v->{ 
					allOf.count++;
					System.out.println(allOf.count++);
					
					if(allOf.count==allOf.max){
						List res = new ArrayList(futures.length);
						for(FastFuture resNext : futures)
							res.add(resNext.result());
						allOf.result =res;
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
		System.out.println("Setting!");
		return set(()->result);
	}
	public <R> R set(Supplier<T> result){
		try{
			
			final Object use = result.get();
			if(pipeline.functions.length==0){
				done();
				this.result = (T)use;
				return (R)this.result;
			}
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
			exception = t;
			completedExceptionally =true;
			
			ExceptionSoftener.singleton.factory.getInstance().throwSoftenedException(t);
		}
		return (R)result;
	}
	public <R> R set(Supplier<T> result,int index){
		try{
			
			Object current = result.get();
			
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
			exception = t;
			completedExceptionally =true;
			ExceptionSoftener.singleton.factory.getInstance().throwSoftenedException(t);
		}
		return (R)this.result;
	}
	private boolean done(){
		System.out.println("done!");
		this.completedExceptionally=false;
		return this.done =true;
		
	}
	
	public void clearFast() {
		result =UNSET;
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
		if(done){
			done=false;
			Runnable command = ()-> set((T)fn.apply((T)result()));
			exec.execute( command);
		}
		return (FastFuture)this.withBuilder(builder.thenApplyAsync(fn, exec));
	}
	public  FastFuture<T> peek(Consumer<T> c){
		if(done){
			
			c.accept((T)result());
		}
		this.builder = builder.peek(c);
		return this;
	}
	public <R> FastFuture<R> thenApply(Function<T,R> fn){
		
		System.out.println("then apply " + done + " : " + result);
		return (FastFuture)this.withBuilder(builder.thenApply(fn));
	}
	public <X extends Throwable> FastFuture<T> exceptionally(Function<X,T> fn){
		if(pipeline!=null){
			doAfter= fn;
		}
		if(done && completedExceptionally ){
			try{
				result = doAfter.apply((X)exception());
				doAfter = null;
				done();
			}catch(Throwable t){
				exception = t;
			}
			return this;
		}else if(done){
			return this;
		}
		return (FastFuture)this.withBuilder(builder.exceptionally(fn));
	}
	public <X extends Throwable> FastFuture<T> whenComplete(BiConsumer<T,X> fn){
		return (FastFuture)this.withBuilder(builder.whenComplete(fn));
	}
	public FastFuture<T> build() {
		return new FastFuture(this.builder.toFinalPipeline(),0);
	}
	
	
}
