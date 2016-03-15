package com.aol.cyclops.internal.react.async.future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import com.aol.cyclops.util.function.Cacheable;
import com.aol.cyclops.util.function.Memoize;

import lombok.AllArgsConstructor;
import lombok.experimental.Wither;

@AllArgsConstructor
@Wither
public class PipelineBuilder {
	
	private final ExecutionPipeline builder;
	private final boolean autoOptimise; //fan out then syncrhonous there after
	private final Executor optimisingExec;
	private final boolean autoMemoize;
	private final Cacheable memoizeFactory;
	public PipelineBuilder(){
		builder = new ExecutionPipeline();
		autoOptimise=false;
		optimisingExec=null;
		autoMemoize=false;
		this.memoizeFactory=null;
	}
	
	private <T,R> Function<T,R> memoize(Function<T,R> fn){
		
		if(!this.autoMemoize)
			return fn;
		if(memoizeFactory==null)
			return Memoize.memoizeFunction(fn);
		return Memoize.memoizeFunction(fn, memoizeFactory);
	}
	public PipelineBuilder(boolean autoOptimise,Executor optimisingExec
		,boolean autoMemoize,Cacheable memoizeFactory ){
		builder = new ExecutionPipeline();
		this.autoOptimise=autoOptimise;
		this.optimisingExec=optimisingExec;
		this.autoMemoize=autoMemoize;
		this.memoizeFactory= memoizeFactory;
	}
	public <T,R> PipelineBuilder thenCompose(Function<? super T,CompletableFuture<? extends R>> fn){
		if(autoOptimise && builder.functionListSize()==0) 
			return thenComposeAsync(fn,optimisingExec);
		return this.withBuilder(builder.thenCompose((Function)memoize(fn)));
	}
	public <T,R> PipelineBuilder thenComposeAsync(Function<? super T,CompletableFuture<? extends R>> fn,Executor exec){
		if(autoOptimise){//if we already have a function present, compose with that
			if(builder.functionListSize()>0)
				return thenCompose(fn);
		}
		
		return this.withBuilder(builder.thenComposeAsync((Function)memoize(fn), exec));
		
	}
	public <T,R> PipelineBuilder thenApplyAsync(Function<T,R> fn,Executor exec){
		if(autoOptimise){//if we already have a function present, compose with that
			if(builder.functionListSize()>0)
				return thenApply(fn);
		}
		return this.withBuilder(builder.thenApplyAsync(memoize(fn), exec));
		
		
	}
	public  <T> PipelineBuilder peek(Consumer<? super T> c){
		
		return this.withBuilder( builder.peek(c));
		
	}
	public <T,R> PipelineBuilder thenApply(Function<? super T,? extends R> fn){
		if(autoOptimise && builder.functionListSize()==0)
			return this.withBuilder(builder.thenApplyAsync(memoize(fn),  optimisingExec));
		return  this.withBuilder(builder.thenApply(memoize(fn)));

	}
	
	public <X extends Throwable,T>  PipelineBuilder exceptionally(Function<? super X,? extends T> fn){
		
		return this.withBuilder(builder.exceptionally(fn));
	}
	public <T,X extends Throwable> PipelineBuilder whenComplete(BiConsumer<? super T,? super X> fn){
		return this.withBuilder(builder.whenComplete(fn));
	}
	public <T> FastFuture<T> build() {
		
		return new FastFuture<T>(this.builder.toFinalPipeline(),0);
	}
	public PipelineBuilder onFail(Consumer<Throwable> onFail) {
		return this.withBuilder(builder.onFail(onFail));
	}
	public boolean isSequential() {
		return this.builder.isSequential();
	}
}