package com.aol.cyclops.control.anym.transformers;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Test;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.monads.transformers.SetT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.SetX;
import com.aol.cyclops.functions.collections.extensions.AbstractAnyMSeqTest;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.stream.reactive.SeqSubscriber;
public class SetTOptionalTest extends AbstractAnyMSeqTest{

	@Override
	public <T> AnyMSeq<T> of(T... values) {
		return AnyM.fromSetT(SetT.fromOptional(Optional.of(SetX.of(values))));
	}
	@Test
    public void stream(){
       
        assertThat(of(1,2,3).stream().collect(Collectors.toList()),hasItems(1,2,3));
    }
	
	@Test
	public void publisher(){
	    SeqSubscriber<Integer> sub = SeqSubscriber.subscriber();
	    of(1,2,3).subscribe(sub);
	    sub.forEach(System.out::println);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.functions.collections.extensions.AbstractCollectionXTest#empty()
	 */
	@Override
	public <T> AnyMSeq<T> empty() {
		return AnyM.fromIterable(ListX.empty());
	}
	@Test
	public void when(){
		
		String res=	AnyM.fromIterable(ListX.of(1,2,3)).visit((x,xs)->
								xs.join(x.visit(some-> (int)some>2? "hello" : "world",()->"boo!"))
					);
		assertThat(res,equalTo("2world3"));
	}
	@Test
	public void whenGreaterThan2(){
		String res=	AnyM.fromIterable(ListX.of(5,2,3)).visit((x,xs)->
								xs.join(x.visit(some-> (int)some>2? "hello" : "world",()->"boo!"))
					);
		assertThat(res,equalTo("2hello3"));
	}
	@Test
	public void when2(){
		
		Integer res =	AnyM.fromIterable(ListX.of(1,2,3)).visit((x,xs)->{
						
								System.out.println(x.isPresent());
								System.out.println(x.get());
								return x.get();
								});
		System.out.println(res);
	}
	@Test
	public void whenNilOrNot(){
		String res1=	AnyM.fromIterable(ListX.of(1,2,3)).visit((x,xs)-> x.visit(some-> (int)some>2? "hello" : "world",()->"EMPTY"));
	}
	@Test
	public void whenNilOrNotJoinWithFirstElement(){
		
		
		String res=	AnyM.fromIterable(ListX.of(1,2,3)).visit((x,xs)-> x.visit(some-> xs.join((int)some>2? "hello" : "world"),()->"EMPTY"));
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

