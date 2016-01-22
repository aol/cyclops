package com.aol.cyclops.javaslang.reactivestreams;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import com.aol.cyclops.invokedynamic.ExceptionSoftener;

import javaslang.Lazy;
import javaslang.collection.Stream;
import javaslang.collection.Stream.Cons;
import lombok.AllArgsConstructor;


public class LazyStream <T>{

	private final Lazy<Stream<T>> stream;
	
	public LazyStream(Lazy<Stream<T>> stream){
		this.stream=stream;
	}
	
	public <U> LazyStream<U> map(Function<? super T, ? extends U> mapper) {
	        Objects.requireNonNull(mapper, "mapper is null");
	       
	       return new LazyStream<>(stream.map(s->s.map(mapper)));
	 }
	
	public void forEach(Consumer<T> c){
		stream.get().forEach(c);
	}
	public LazyStream<T> recover(Function<Throwable, ? extends T> fn) {
		
		return new LazyStream<T>(stream.map(st ->  {
		Iterator<T> it = st.iterator();
		
		Class type =Throwable.class;
		return Stream.ofAll(()->new Iterator<T>(){
			
			@Override
			public boolean hasNext() {
				try{
					return it.hasNext();
				}catch(Throwable t){
					
					t.printStackTrace();
					return true;
					
				}
				
			}
			@Override
			public T next() {
				try{
					return it.next();
				}catch(Throwable t){
					if(type.isAssignableFrom(t.getClass())){
						return fn.apply(t);
						
					}
					throw ExceptionSoftener.throwSoftenedException(t);
					
				}
				
				
			}
			
		});
		}));
	}
		
	

}
