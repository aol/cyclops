package com.aol.simple.react.eager;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.lambda.tuple.Tuple2;
import org.junit.Test;

import com.aol.simple.react.base.BaseSeqTest;
import com.aol.simple.react.stream.eager.EagerReact;
import com.aol.simple.react.stream.simple.SimpleReact;
import com.aol.simple.react.stream.traits.EagerFutureStream;

public class EagerSeqTest extends BaseSeqTest {
 
	@Override
	protected <U> EagerFutureStream<U> of(U... array) {
		return EagerFutureStream.parallel(array);
	}
	@Override
	protected <U> EagerFutureStream<U> ofThread(U... array) {
		return EagerFutureStream.freeThread(array);
	}
	
	@Override
	protected <U> EagerFutureStream<U> react(Supplier<U>... array) {
		return EagerReact.parallelBuilder().react(array);
		
	}
	
	@Test
	public void combine(){
		
		assertThat(of(1,2,3,4,5,6).combineLatest(of(3)).collect(Collectors.toList()).size(),greaterThan(5));
	}
	@Test
	public void combineLatest(){
		
		assertThat(of(1,2,3,4,5,6).combineLatest(react(()->3,()->value())).collect(Collectors.toList()).size(),greaterThan(5));
	}
	@Test
	public void combineValues(){
		for(int i=0;i<1000;i++){
			Stream<Tuple2<Integer,Integer>> s = of(1,2,3,4,5,6).combineLatest(of(3));
			List<Tuple2<Integer,Integer>> list = s.collect(Collectors.toList());
			System.out.println(i + " : " +  list);
			
		//	assertTrue(of(1,2,3,4,5,6).combineLatest(of(3)).anyMatch(it-> it.v2==null));
			
			assertTrue(of(1,2,3,4,5,6).combineLatest(of(3)).anyMatch(it-> (it.v1 == null ? -1 : it.v1)==1));
			assertTrue(list.stream().anyMatch(it-> (it.v1 == null ? -1 : it.v1)==2));
			assertTrue(of(1,2,3,4,5,6).combineLatest(of(3)).anyMatch(it-> (it.v1 == null ? -1 : it.v1)==3));
			assertTrue(of(1,2,3,4,5,6).combineLatest(of(3)).anyMatch(it-> (it.v1 == null ? -1 : it.v1)==4));
			assertTrue(of(1,2,3,4,5,6).combineLatest(of(3)).anyMatch(it-> (it.v1 == null ? -1 : it.v1)==5));
			assertTrue(of(1,2,3,4,5,6).combineLatest(of(3)).anyMatch(it-> (it.v1 == null ? -1 : it.v1)==6));
		}
	}
	@Test
	public void withLatest(){
		
		assertThat(of(1,2,3,4,5,6).withLatest(of(30,40,50,60,70,80,90,100,110,120,140))
				.collect(Collectors.toList()).size(),is(6));
		
	}
	/**
	@Test
	public void mergeMultipleMixed(){
		assertThat(react(()->1,()->2).merge(new SimpleReact().react(()->-1,()->-2),
						new EagerReact().react(()->100,()->200)).toList().size(),equalTo(6));
	}**/
	@Test
	public void withLatestValues(){
		assertTrue(of(1,2,3,4,5,6).withLatest(of(30,40,50,60,70,80,90,100,110,120,140)).anyMatch(it-> it.v2==null));
		//assertTrue(of(1,2,3,4,5,6).combine(of(3)).oneMatch(it-> it.v2==3));
		assertTrue(of(1,2,3,4,5,6).withLatest(of(3)).anyMatch(it-> it.v1==1));
		assertTrue(of(1,2,3,4,5,6).withLatest(of(3)).anyMatch(it-> it.v1==2));
		assertTrue(of(1,2,3,4,5,6).withLatest(of(3)).anyMatch(it-> it.v1==3));
		assertTrue(of(1,2,3,4,5,6).withLatest(of(3)).anyMatch(it-> it.v1==4));
		assertTrue(of(1,2,3,4,5,6).withLatest(of(3)).anyMatch(it-> it.v1==5));
		assertTrue(of(1,2,3,4,5,6).withLatest(of(3)).anyMatch(it-> it.v1==6));
	}
	
	@Test
	public void takeUntil(){
		System.out.println(react(()->1,()->2,()->3,()->4,()->value2())
				.takeUntil(react(()->value())).collect(Collectors.toList()));
		assertTrue(react(()->1,()->2,()->3,()->4,()->value2()).takeUntil(react(()->value())).noneMatch(it-> it==200));
		assertTrue(react(()->1,()->2,()->3,()->4,()->value2()).takeUntil(react(()->value())).anyMatch(it-> it==1));
	}
	@Test(expected=UnsupportedOperationException.class)
    public void testCycle() {
    	  of(1).cycle().limit(6).toList();
      
    }
	@Test
	public void mergeMultiple(){
		assertThat(EagerFutureStream.mergeMultiple( react(()->1,()->2), react(()->'a',()->'b'),
						react(()->100,()->200)).toList().size(),equalTo(6));
	}
	@Test
	public void skipUntil(){
		System.out.println(react(()->1,()->2,()->3,()->4,()->value2())
				.skipUntil(react(()->value())).collect(Collectors.toList()));
		assertTrue(react(()->1,()->2,()->3,()->4,()->value2()).skipUntil(react(()->value())).allMatch(it-> it==200));
		assertThat(react(()->1,()->2,()->3,()->4,()->value2()).skipUntil(react(()->value())).count(),is(1l));
	}
	@Test
	public void batchBySize(){
		System.out.println(of(1,2,3,4,5,6).batchBySize(3).collect(Collectors.toList()));
		for(int i=0;i<5000;i++)
			assertThat(of(1,2,3,4,5,6).batchBySize(3).collect(Collectors.toList()).size(),is(2));
	}
	@Test
	public void batchBySize2(){
		System.out.println(react(()->1,()->2,()->3,()->4,()->{sleep(100);return 5;},()->{sleep(110);return 6;}).batchBySize(3).collect(Collectors.toList()));
		for(int i=0;i<50;i++)
			assertThat(react(()->1,()->2,()->3,()->4,()->{sleep(100);return 5;},()->{sleep(110);return 6;}).batchBySize(3).collect(Collectors.toList()).size(),is(2));
	}
	@Test
	public void batchByTime2(){
		for(int i=0;i<5;i++){
			System.out.println(i);
			assertThat(react(()->1,()->2,()->3,()->4,()->{sleep(100);return 5;},()->{sleep(110);return 6;})
							.batchByTime(30,TimeUnit.MILLISECONDS)
							.toList()
							.get(0)
							,not(hasItem(6)));
		}
	}
	@Test
	public void batchSinceLastReadIterator() throws InterruptedException{
		Iterator<Collection<Object>> it = react(()->1,()->2,()->3,()->4,()->5,()->value()).chunkLastReadIterator();
		Thread.sleep(50);
		List<Collection> cols = new ArrayList<>();
		while(it.hasNext()){
			
			cols.add(it.next());
		}
		
		assertThat(cols.get(0).size(),greaterThan(1));
		cols.remove(0);
		Collection withJello = cols.stream().filter(col -> col.contains("jello")).findFirst().get();
		
	
		
	}
	@Test
	public void batchSinceLastRead() throws InterruptedException{
		
			
			List<Collection> cols = react(()->1,()->2,()->3,()->4,()->5,()->value()).chunkSinceLastRead().peek(it->{sleep(50);}).collect(Collectors.toList());
			
			System.out.println(cols);
			assertThat(cols.size(),greaterThan(1));
			cols.remove(0);
			Collection withJello = cols.stream().filter(col -> col.contains("jello")).findFirst().get();
		
		
	
		
	}

	@Test
	public void testOfType() {
		assertTrue(
				of(1, "a", 2, "b", 3, null).ofType(Integer.class).toList().containsAll(asList(1, 2, 3)));
		assertTrue( of(1, "a", 2, "b", 3, null)
				.ofType(Serializable.class).toList().containsAll(asList(1, "a", 2, "b", 3)));
	}

	@Test
	public void testCastPast() {
		assertTrue(
				of(1, "a", 2, "b", 3, null).capture(e -> e.printStackTrace())
						.cast(Serializable.class).toList().containsAll(
								asList(1, "a", 2, "b", 3, null)));

	}
	
	@Test
	public void testLimitFutures(){
		assertThat(of(1,2,3,4,5).limitFutures(2).collect(Collectors.toList()).size(),is(2));
		
	}	
	@Test
	public void testSkipFutures(){
		assertThat(of(1,2,3,4,5).skipFutures(2).collect(Collectors.toList()).size(),is(3));
	}
	@Test
	public void testSliceFutures(){
		assertThat(of(1,2,3,4,5).sliceFutures(3,4).collect(Collectors.toList()).size(),is(1));
	}
	@Test
	public void testSplitFuturesAt(){
		assertThat(of(1,2,3,4,5).splitAtFutures(2).v1.block().size(),is(asList(1,2).size()));
	}
	@Test
	public void testSplitFuturesAt2(){
		assertThat(sortedList(of(1,2,3,4,5).splitAt(2)
											.v2
											.collect(Collectors.toList())).size(),
											is(asList(3,4,5).size()));
	}

	
	@Test
	public void testZipWithFutures(){
		Stream stream = of("a","b");
		List<Tuple2<Integer,String>> result = of(1,2).zipFutures(stream).block();
		
		assertThat(result.size(),is(asList(tuple(1,"a"),tuple(2,"b")).size()));
	}
	

	@Test
	public void testZipFuturesWithIndex(){
		
		List<Tuple2<String,Long>> result  = of("a","b").zipFuturesWithIndex().block();
		
		assertThat(result.size(),is(asList(tuple("a",0l),tuple("b",1l)).size()));
	}
	@Test
	public void duplicateFutures(){
		List<String> list = of("a","b").duplicateFutures().v1.block();
		assertThat(sortedList(list),is(asList("a","b")));
	}
	private <T> List<T> sortedList(List<T> list) {
		return list.stream().sorted().collect(Collectors.toList());
	}

	@Test
	public void duplicateFutures2(){
		List<String> list = of("a","b").duplicateFutures().v2.block();
		assertThat(sortedList(list),is(asList("a","b")));
	}
	

	/*
	 * Default behaviour is to operate on the outputs rather than the inputs where that can be done in a non-blocking way
	 */
	
	
}
