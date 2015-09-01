package com.aol.simple.react.async.future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import lombok.AllArgsConstructor;
import lombok.experimental.Wither;

@AllArgsConstructor
@Wither
public class PipelineBuilder {
	
	private final ExecutionPipeline builder;
	private final boolean autoOptimise; //fan out then syncrhonous there after
	private final Executor optimisingExec;
	public PipelineBuilder(){
		builder = new ExecutionPipeline();
		autoOptimise=false;
		optimisingExec=null;
	}
	public PipelineBuilder(boolean autoOptimise,Executor optimisingExec){
		builder = new ExecutionPipeline();
		this.autoOptimise=autoOptimise;
		this.optimisingExec=optimisingExec;
	}
	public <T,R> PipelineBuilder thenCompose(Function<T,CompletableFuture<R>> fn){
		if(autoOptimise && builder.functionListSize()==0) 
			return thenComposeAsync(fn,optimisingExec);
		return this.withBuilder(builder.thenCompose((Function)fn));
	}
	public <T,R> PipelineBuilder thenComposeAsync(Function<T,CompletableFuture<R>> fn,Executor exec){
		if(autoOptimise){//if we already have a function present, compose with that
			if(builder.functionListSize()>0)
				return thenCompose(fn);
		}
		
		return this.withBuilder(builder.thenComposeAsync((Function)fn, exec));
		
	}
	public <T,R> PipelineBuilder thenApplyAsync(Function<T,R> fn,Executor exec){
		if(autoOptimise){//if we already have a function present, compose with that
			if(builder.functionListSize()>0)
				return thenApply(fn);
		}
		return this.withBuilder(builder.thenApplyAsync(fn, exec));
		
		
	}
	public  <T> PipelineBuilder peek(Consumer<T> c){
		
		return this.withBuilder( builder.peek(c));
		
	}
	public <T,R> PipelineBuilder thenApply(Function<T,R> fn){
		if(autoOptimise && builder.functionListSize()==0)
			return this.withBuilder(builder.thenApplyAsync(fn,  optimisingExec));
		return  this.withBuilder(builder.thenApply(fn));

	}
	
	public <X extends Throwable,T>  PipelineBuilder exceptionally(Function<X,T> fn){
		
		return this.withBuilder(builder.exceptionally(fn));
	}
	public <T,X extends Throwable> PipelineBuilder whenComplete(BiConsumer<T,X> fn){
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