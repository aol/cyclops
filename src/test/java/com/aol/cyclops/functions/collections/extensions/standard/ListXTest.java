package com.aol.cyclops.functions.collections.extensions.standard;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.concurrent.Executors;

import org.junit.Test;

import com.aol.cyclops.control.LazyReact;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.data.collections.extensions.FluentCollectionX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.functions.collections.extensions.CollectionXTestsWithNulls;
import com.aol.cyclops.types.stream.reactive.FlatMapConfig;
public class ListXTest extends CollectionXTestsWithNulls{

	@Override
	public <T> FluentCollectionX<T> of(T... values) {
		return ListX.of(values);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.functions.collections.extensions.AbstractCollectionXTest#empty()
	 */
	@Override
	public <T> FluentCollectionX<T> empty() {
		return ListX.empty();
	}
	@Test
	public void flatMapPublisher() throws InterruptedException{
	    assertThat(ListX.of(1,2,3)
	                    .flatMapPublisher(i->Maybe.of(i))
	                    .toListX(),equalTo(Arrays.asList(1,2,3)));
	    
	}
	@Test
    public void flatMapPublisherWithAsync() throws InterruptedException{
        assertThat(ListX.of(1,2,3)
                        .flatMapPublisher(i->Maybe.of(i),FlatMapConfig.unbounded(Executors.newFixedThreadPool(1)))
                        .toListX(),equalTo(Arrays.asList(1,2,3)));
        
    }
	private void sleep2(int time){
	    try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}
	@Test
    public void flatMapPublisherAsync() throws InterruptedException{
       LazyReact r = new LazyReact(10,10);
       
       
      assertThat(ListX.of(3,2,1)
               .flatMapPublisher(i-> r.generate(()->i).peek(a->sleep2(a*100)).limit(5).async())
               .toListX().size(),equalTo(15));
    }
	@Test
	public void when(){
		
		String res=	ListX.of(1,2,3).visit((x,xs)->
								xs.join(x.visit(some-> (int)some>2? "hello" : "world",()->"boo!"))
					);
		assertThat(res,equalTo("2world3"));
	}
	@Test
	public void whenGreaterThan2(){
		String res=	ListX.of(5,2,3).visit((x,xs)->
								xs.join(x.visit(some-> (int)some>2? "hello" : "world",()->"boo!"))
					);
		assertThat(res,equalTo("2hello3"));
	}
	@Test
	public void when2(){
		
		Integer res =	ListX.of(1,2,3).visit((x,xs)->{
						
								System.out.println(x.isPresent());
								System.out.println(x.get());
								return x.get();
								});
		System.out.println(res);
	}
	@Test
	public void whenNilOrNot(){
		String res1=	ListX.of(1,2,3).visit((x,xs)-> x.visit(some-> (int)some>2? "hello" : "world",()->"EMPTY"));
	}
	@Test
	public void whenNilOrNotJoinWithFirstElement(){
		
		
		String res=	ListX.of(1,2,3).visit((x,xs)-> x.visit(some-> xs.join((int)some>2? "hello" : "world"),()->"EMPTY"));
		assertThat(res,equalTo("2world3"));
	}
	
	/**
	 *
		Eval e;
		//int cost = ReactiveSeq.of(1,2).when((head,tail)-> head.when(h-> (int)h>5, h-> 0 )
		//		.flatMap(h-> head.when());
		
		ht.headMaybe().when(some-> Matchable.of(some).matches(
											c->c.hasValues(1,2,3).then(i->"hello world"),
											c->c.hasValues('b','b','c').then(i->"boo!")
									),()->"hello");
									**/
	 

}

