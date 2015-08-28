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
	private final PStack<Function> firstRecover;
	
	ExecutionPipeline(PStack<Function> functionList,PStack<Executor> execList ){
		this.functionList= functionList;
		this.execList = execList;
		firstRecover = ConsPStack.empty();
	}
	ExecutionPipeline(PStack<Function> functionList,PStack<Executor> execList,PStack<Function> firstRecover ){
		this.functionList= functionList;
		this.execList = execList;
		this.firstRecover = firstRecover;
	}
	public ExecutionPipeline(){
		functionList= ConsPStack.empty();
		execList = ConsPStack.empty();
		firstRecover= ConsPStack.empty();
	}
	public <T> ExecutionPipeline peek(Consumer<T> c){
		return this.<T,Object>thenApply(i->{c.accept(i); return i;});
		
	}
	
	public  <T,R> ExecutionPipeline thenApplyAsync(Function<T,R> fn,Executor exec){
		
		return new ExecutionPipeline(addFn(fn),addExec(exec));
		
	}
	public<T,R> ExecutionPipeline thenComposeAsync(Function<Object,CompletableFuture<?>> fn,Executor exec){
		
		return new ExecutionPipeline(addFn(t-> fn.apply(t).join()),addExec(exec));
	}
	
	public<T,R> ExecutionPipeline thenCompose(Function<T,CompletableFuture<R>> fn){
		Function<T,R> unpacked= t-> fn.apply(t).join();
		return new ExecutionPipeline(swapFn(unpacked),execList.size()==0?execList.plus(null)  : execList);

	}
	public<T,R> ExecutionPipeline thenApply(Function<T,R> fn){
		return new ExecutionPipeline(swapComposeFn(fn),execList.size()==0?execList.plus(null)  : execList);
	}
	public <X extends Throwable,T> ExecutionPipeline exceptionally(Function<X,T> fn){
		if(functionList.size()>0){
			Function before = functionList.get(functionList.size()-1);
			Function except = t-> {
				try{
					return before.apply(t);
				}catch(Throwable e){
					return fn.apply((X)e);
				}
			};
			
			return new ExecutionPipeline(swapFn(except),execList);
		}
		
		return new ExecutionPipeline(functionList,execList,addFirstRecovery(fn));
	
		
	}
	public <X extends Throwable,T> ExecutionPipeline whenComplete(BiConsumer<T,X> fn){
		
		Function before = functionList.get(functionList.size()-1);
	
		Function except = t-> {
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
		};
		
		return new ExecutionPipeline(swapFn(except),execList);
	}
	
	public FinalPipeline toFinalPipeline(){
		return new FinalPipeline(functionList.toArray(new Function[0]),
				execList.toArray(new Executor[0]), this.firstRecover.toArray(new Function[0]));
	}
	public static ExecutionPipeline empty() {
		ExecutionPipeline pipeline = new ExecutionPipeline();
		
		return pipeline;
	}
	private PStack<Executor> addExec(Executor exec){
		if(execList.size()==0)
			return execList.plus(exec);
		return execList.plus(execList.size(),exec);
	}
	private PStack<Function> addFirstRecovery(Function fn){
		if(firstRecover.size()==0)
			return firstRecover.plus(fn);
		
		return firstRecover.plus(firstRecover.size(),fn);
	}
	private PStack<Function> addFn(Function fn){
		if(functionList.size()==0)
			return functionList.plus(fn);
		
		return functionList.plus(functionList.size(),fn);
	}
	
	private PStack<Function> swapFn(Function fn){
		if(functionList.size()==0)
			return functionList.plus(fn);
		Function before = functionList.get(functionList.size()-1);
		PStack<Function> removed = functionList.minus(functionList.size()-1);
		return removed.plus(removed.size(),fn);
		//return removed.plus(removed.size(),fn.compose(before));
	}
	private PStack<Function> swapComposeFn(Function fn){
		if(functionList.size()==0)
			return functionList.plus(fn);
		Function before = functionList.get(functionList.size()-1);
		PStack<Function> removed = functionList.minus(functionList.size()-1);
		return removed.plus(removed.size(),fn.compose(before));
	}
	int functionListSize(){
		return this.functionList.size();
	}
}
