package com.aol.cyclops.sequence;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aol.cyclops.sequence.streamable.Streamable;

public class SeqUtils {
	/**
	 * Reverse a Stream
	 * 
	 * <pre>
	 * {@code 
	 * assertThat(StreamUtils.reverse(Stream.of(1,2,3)).collect(Collectors.toList())
				,equalTo(Arrays.asList(3,2,1)));
	 * }
	 * </pre>
	 * 
	 * @param stream Stream to reverse
	 * @return Reversed stream
	 */
	public static <U> Stream<U> reverse(Stream<U> stream){
		return reversedStream(stream.collect(Collectors.toList()));
	}
	/**
	 * Create a reversed Stream from a List
	 * <pre>
	 * {@code 
	 * StreamUtils.reversedStream(asList(1,2,3))
				.map(i->i*100)
				.forEach(System.out::println);
		
		
		assertThat(StreamUtils.reversedStream(Arrays.asList(1,2,3)).collect(Collectors.toList())
				,equalTo(Arrays.asList(3,2,1)));
	 * 
	 * }
	 * </pre>
	 * 
	 * @param list List to create a reversed Stream from
	 * @return Reversed Stream
	 */
	public static <U> Stream<U> reversedStream(List<U> list){
		return new ReversedIterator<>(list).stream();
	}
	
	/**
	 * Create a Stream that finitely cycles the provided Streamable, provided number of times
	 * 
	 * <pre>
	 * {@code 
	 * assertThat(StreamUtils.cycle(3,Streamable.of(1,2,2))
								.collect(Collectors.toList()),
									equalTo(Arrays.asList(1,2,2,1,2,2,1,2,2)));
	 * }
	 * </pre>
	 * @param s Streamable to cycle
	 * @return New cycling stream
	 */
	public static <U> Stream<U> cycle(int times,Streamable<U> s){
		return Stream.iterate(s.stream(),s1-> s.stream()).limit(times).flatMap(Function.identity());
	}
	 /**
	   * Projects an immutable collection of this stream. Initial iteration over the collection is not thread safe 
	   * (can't be performed by multiple threads concurrently) subsequent iterations are.
	   *
	   * @return An immutable collection of this stream.
	   */
	  public static final <A> Collection<A> toLazyCollection(Stream<A> stream) {
		  	return toLazyCollection(stream.iterator());
	  }	
	  public static final <A> Collection<A> toLazyCollection(Iterator<A> iterator){
		  return toLazyCollection(iterator,false);
	  }
	  /**
	   * Lazily constructs a Collection from specified Stream. Collections iterator may be safely used
	   * concurrently by multiple threads.
	 * @param stream
	 * @return
	 */
	public static final <A> Collection<A> toConcurrentLazyCollection(Stream<A> stream) {
		  	return toConcurrentLazyCollection(stream.iterator());
	  }	
	  public static final <A> Collection<A> toConcurrentLazyCollection(Iterator<A> iterator){
		  return toLazyCollection(iterator,true);
	  }
	 private static final <A> Collection<A> toLazyCollection(Iterator<A> iterator,boolean concurrent) {
	    return new AbstractCollection<A>() {
	    	
	    @Override  
	    public boolean equals(Object o){
	    	  if(o==null)
	    		  return false;
	    	  if(! (o instanceof Collection))
	    		  return false;
	    	  Collection<A> c = (Collection)o;
	    	  Iterator<A> it1 = iterator();
	    	  Iterator<A> it2 = c.iterator();
	    	  while(it1.hasNext()){
	    		  if(!it2.hasNext())
	    			  return false;
	    		  if(!Objects.equals(it1.next(),it2.next()))
	    			  return false;
	    	  }
	    	  if(it2.hasNext())
	    		  return false;
	    	  return true;
	      }
	      @Override  
	      public int hashCode(){
	    	  Iterator<A> it1 = iterator();
	    	  List<A> arrayList= new ArrayList<>();
	    	  while(it1.hasNext()){
	    		  arrayList.add(it1.next());
	    	  }
	    	  return Objects.hashCode(arrayList.toArray());
	      }
	      List<A> data =new ArrayList<>();
	     
	      volatile boolean complete=false;
	      
	      Object lock = new Object();
	      ReentrantLock rlock = new ReentrantLock();
	      public Iterator<A> iterator() {
	    	  if(complete)
	    		  return data.iterator();
	    	  return new Iterator<A>(){
	    		int current = -1;
				@Override
				public boolean hasNext() {
					
					if(concurrent){
						
						rlock.lock();
					}
					try{
						
						if(current==data.size()-1 && !complete){
							boolean result = iterator.hasNext();
							complete = !result;
							
							return result;
						}
						if(current+1<data.size()){
							
							return true;
						}
						return false;
					}finally{
						if(concurrent)
							rlock.unlock();
					}
				}

					@Override
					public A next() {
						
						if (concurrent) {

							rlock.lock();
						}
						try {
							if (current < data.size() && !complete) {
								if(iterator.hasNext())
									data.add(iterator.next());

								return data.get(++current);
							}
							current++;	
							return data.get(current);
						} finally {
							
							if (concurrent)
								rlock.unlock();
						}

					}

				};
	        
	      }

	      public int size() {
	    	  if(complete)
	    		  return data.size();
	    	  Iterator it = iterator();
	    	  while(it.hasNext())
	    		  it.next();
	    	  
	    	  return data.size();
	      }
	    };
	  }
}
