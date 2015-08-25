package com.aol.simple.react.async.future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import lombok.ToString;

import org.pcollections.ConsPStack;
import org.pcollections.PStack;
@ToString
public class ExecutionPipeline {
	private final PStack<Function> functionList;
	private final PStack<Executor> execList;
	
	ExecutionPipeline(PStack<Function> functionList,PStack<Executor> execList ){
		this.functionList= functionList;
		this.execList = execList;
	}
	public ExecutionPipeline(){
		functionList= ConsPStack.empty();
		execList = ConsPStack.empty();
	}
	public <T> ExecutionPipeline peek(Consumer<T> c){
		this.<T,Object>thenApply(i->{c.accept(i); return i;});
		return this;
	}
	public  <T,R> ExecutionPipeline thenApplyAsync(Function<T,R> fn,Executor exec){
		
		return new ExecutionPipeline(functionList.plus(fn),execList.plus(exec));
		
	}
	public<T,R> ExecutionPipeline thenComposeAsync(Function<Object,CompletableFuture<?>> fn,Executor exec){
		
		return new ExecutionPipeline(functionList.plus(t-> fn.apply(t).join()),execList.plus(exec));
	}
	
	public<T,R> ExecutionPipeline thenCompose(Function<T,CompletableFuture<R>> fn){
		Function<T,R> unpacked= t-> fn.apply(t).join();
		if(functionList.size()>0){
			Function before = functionList.get(functionList.size()-1);
			PStack<Function> removed = functionList.minus(functionList.size()-1);
			return new ExecutionPipeline(removed.plus( unpacked.compose(before)), execList);
		}else{
			return new ExecutionPipeline(functionList.plus( unpacked), execList);
			
		}
	
	}
	public<T,R> ExecutionPipeline thenApply(Function<T,R> fn){
		if(functionList.size()>0){
			Function before = functionList.get(functionList.size()-1);
			PStack<Function> removed = functionList.minus(functionList.size()-1);
			return new ExecutionPipeline(removed.plus( fn.compose(before)), execList);
		}else{
			return new ExecutionPipeline(functionList.plus( fn), execList);
		}
		
	}
	public <X extends Throwable,T> ExecutionPipeline exceptionally(Function<X,T> fn){
		Function before = functionList.get(functionList.size()-1);
		PStack<Function> removed = functionList.minus(functionList.size()-1);
		
		return new ExecutionPipeline(removed.plus( t-> {
			try{
				return before.apply(t);
			}catch(Throwable e){
				return fn.apply((X)e);
			}
		}),execList);
		
	}
	public <X extends Throwable,T> ExecutionPipeline whenComplete(BiConsumer<T,X> fn){
		
		Function before = functionList.get(functionList.size()-1);
		PStack<Function> removed = functionList.minus(functionList.size()-1);
		return new ExecutionPipeline(removed.plus( t-> {
			T res = null;
			X ex= null;
			try{
				res= (T)before.apply(t);
			}catch(Throwable e){
				ex =(X)e;
			}
			fn.accept(res,ex);
			if(ex!=null)
				throw (RuntimeException)ex;
			return res;
		}),execList);
		
		
	}
	
	public FinalPipeline toFinalPipeline(){
		return new FinalPipeline(functionList.toArray(new Function[0]),
				execList.toArray(new Executor[0]));
	}
	public static ExecutionPipeline empty() {
		ExecutionPipeline pipeline = new ExecutionPipeline();
		
		return pipeline;
	}
}
