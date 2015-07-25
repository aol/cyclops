package com.aol.cyclops.functionaljava.comprehenders;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;









import com.aol.cyclops.lambda.api.Comprehender;
import com.google.common.base.Objects;
import com.nurkiewicz.lazyseq.LazySeq;

import fj.data.Stream;

public class StreamComprehender implements Comprehender<Stream> {

	@Override
	public Object map(Stream t, Function fn) {
		return t.map(s -> fn.apply(s));
	}
	@Override
	public Object executeflatMap(Stream t, Function fn){
		return flatMap(t,input -> unwrapOtherMonadTypes(this,fn.apply(input)));
	}
	@Override
	public Object flatMap(Stream t, Function fn) {
		return t.bind(s->fn.apply(s));
	}

	@Override
	public Stream of(Object o) {
		return Stream.single(o);
	}

	@Override
	public Stream empty() {
		return Stream.nil();
	}

	@Override
	public Class getTargetClass() {
		return Stream.class;
	}
	static Stream unwrapOtherMonadTypes(Comprehender<Stream> comp,Object apply){
		if(apply instanceof java.util.stream.Stream)
			return Stream.iteratorStream( ((java.util.stream.Stream)apply).iterator());
		if(apply instanceof Iterable)
			return Stream.iterableStream( ((Iterable)apply));
		if(apply instanceof LazySeq){
			return Stream.iteratorStream(((LazySeq)apply).iterator());
		}
		
		
		return Comprehender.unwrapOtherMonadTypes(comp,apply);
		
	}
	public Object resolveForCrossTypeFlatMap(Comprehender comp,Stream apply){
		return comp.of(toCollection(apply));
	}
	 /**
	   * Projects an immutable collection of this stream.
	   *
	   * @return An immutable collection of this stream.
	   */
	  public final <A> Collection<A> toCollection(Stream stream) {
	    return new AbstractCollection<A>() {
	    @Override  
	    public boolean equals(Object o){
	    	  if(o==null)
	    		  return false;
	    	  if(! (o instanceof Collection))
	    		  return false;
	    	  Collection c = (Collection)o;
	    	  Iterator it1 = iterator();
	    	  Iterator it2 = c.iterator();
	    	  while(it1.hasNext()){
	    		  if(!it2.hasNext())
	    			  return false;
	    		  if(!Objects.equal(it1.next(),it2.next()))
	    			  return false;
	    	  }
	    	  if(it2.hasNext())
	    		  return false;
	    	  return true;
	      }
	      @Override  
	      public int hashCode(){
	    	  Iterator it1 = iterator();
	    	  List arrayList= new ArrayList();
	    	  while(it1.hasNext()){
	    		  arrayList.add(it1.next());
	    	  }
	    	  return Objects.hashCode(arrayList.toArray());
	      }
	      public Iterator iterator() {
	        return new Iterator<A>() {
	          private Stream<A> xs = stream;

	          public boolean hasNext() {
	            return xs.isNotEmpty();
	          }

	          public A next() {
	            if (xs.isEmpty())
	              throw new NoSuchElementException();
	            else {
	              final A a = xs.head();
	              xs = xs.tail()._1();
	              return a;
	            }
	          }

	          public void remove() {
	            throw new UnsupportedOperationException();
	          }
	        };
	      }

	      public int size() {
	        return stream.length();
	      }
	    };
	  }
}
