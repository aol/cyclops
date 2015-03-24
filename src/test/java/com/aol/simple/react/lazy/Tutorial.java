package com.aol.simple.react.lazy;

import static java.util.Arrays.asList;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.Getter;

import org.junit.Ignore;
import org.junit.Test;

import com.aol.simple.react.async.QueueFactories;
import com.aol.simple.react.exceptions.SimpleReactFailedStageException;
import com.aol.simple.react.stream.eager.EagerFutureStream;
import com.aol.simple.react.stream.eager.EagerReact;
import com.aol.simple.react.stream.lazy.LazyFutureStream;
import com.aol.simple.react.stream.simple.SimpleReact;
import com.aol.simple.react.stream.traits.FutureStream;
import com.aol.simple.react.threads.ReactPool;
import com.aol.simple.react.threads.SequentialElasticPools;

@Ignore
public class Tutorial {

	
	@SuppressWarnings("unchecked")
	
	
	
	@Test
	public void zipByResults(){
		
		LazyFutureStream<String> a = LazyFutureStream.parallelBuilder(3).react(()->slowest(),()->fast(),()->slow());
		LazyFutureStream<Integer> b = LazyFutureStream.sequentialBuilder().of(1,2,3,4,5,6);
		
		a.zip(b).forEach(System.out::println);
		
		
		
		
	}
	
	
	
	
	
	@SuppressWarnings("unchecked")
	
	
	
	
	
	
	
	@Test
	public void zipByFutures(){
		
		EagerFutureStream<String> a = EagerFutureStream.parallelBuilder(3).react(()->slowest(),()->fast(),()->slow());
		EagerFutureStream<Integer> b = EagerFutureStream.sequentialBuilder().of(1,2,3,4,5,6);
		
		a.zipFutures(b).forEach(System.out::println);
		
		
		
		
	}
	
	
	
	private String slowest(){
		sleep(2500);
		return "slowestResult";
	}
	private String slow(){
		sleep(100);
		return "slowResult";
	}
	private String fast(){	
		return "fast";
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@Test
	public void gettingStarted(){
		
		List<String> results = new SimpleReact()
				.react(() -> readData("data1"), () -> readData("data2"))
				.onFail(RuntimeException.class, this::loadFromDb)
				.peek(System.out::println)
				.then(this::processData)
				.block();
				
	}
		
	private String readData(String name) {
		if(name.equals("data1"))
			throw new RuntimeException();
			
		else
			return "hello world from file!";
			
	}
	private String processData(String data){
		return "processed : " + data;
	}
	private String loadFromDb(SimpleReactFailedStageException e){
		return "hello world from DB!";
	}

	
	
	
	
	
	
	
	@Test
	public void skipUntil(){
		FutureStream<Boolean> stoppingStream = LazyFutureStream.sequentialBuilder()
														.react(()-> 1000)
														.then(this::sleep)
														.peek(System.out::println);
		System.out.println(LazyFutureStream.sequentialCommonBuilder()
						.fromPrimitiveStream(IntStream.range(0, 1000000))
						//.peek(System.out::println)
						.skipUntil(stoppingStream)
						.peek(System.out::println)
						.block().size());
	}
	
	
	
	private boolean sleep(int i) {
		
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
		
			e.printStackTrace();
		}
		return true;
		
	}


	
	
	
	
	
	
	
	
	
	
	
	@Test
	public void jitter(){
		LazyFutureStream.sequentialCommonBuilder()
						.fromPrimitiveStream(IntStream.range(0, 1000000))
						.map(it -> it*100)
						.jitter(10000000l)
						.peek(System.out::println)
						.runOnCurrent();
	}
	
	
	
	
	
	
	@Test
	public void fixedDelay(){
		
		LazyFutureStream.sequentialCommonBuilder()
						.fromPrimitiveStream(IntStream.range(0, 1000000))
						.fixedDelay(1l,TimeUnit.SECONDS)
						.peek(System.out::println)
						.runOnCurrent();
	}
	
	
	
	
	
	
	
	@Test
	public void elasticPool(){
		
		List<String> files = Arrays.asList("/tmp/1.data","/tmp/2.data");

        List<Status> data = SequentialElasticPools.lazyReact.react(er -> er.reactToCollection(files)
                .map(this::loadData)
                .peek(System.out::println)
                .map(this::saveData)
                .collect(Collectors.toList()));
        
        System.out.println("Loaded and saved " + data.size());
	}
	
	
	
	
	
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	
	
	
	@Test
	public void testRoundRobin(){
		
		EagerReact react1 = new EagerReact(new ForkJoinPool(4));
		EagerReact react2 =  new EagerReact(new ForkJoinPool(4));
		
		ReactPool<EagerReact> pool = ReactPool.boundedPool(asList(react1,react2));
		
		
		Supplier[] suppliers = { ()->"hello",()->"world" };
		
		pool.react( (er) -> er.react(suppliers)
						.peek(it-> System.out.println("Data is : " + it + " - " 
											+ " Reactor is : " + System.identityHashCode(er))));
		
		pool.react( (er) -> er.react(suppliers)
						.peek(it-> System.out.println("Data is : " + it + " - " 
											+ " Reactor is : " + System.identityHashCode(er))));
		
		
		
	}
	
	
	
	@Test
	public void add100(){
		
		
		new SimpleReact().of(1,2,3,4,5)
        				 .then(num -> num+100)
        				 .then(num -> Thread.currentThread().getId())
        				 .peek(System.out::println);
		
	}
	
	
	
	
	
	
	
	
	
	
	
	Stack<String> dataArray = new Stack(){{
		add( "{order:1000,{customer:604}}");
		add( "{order:1001,{customer:605}}");
	}};
	private String loadData(String file){
		sleep(1000);
		return dataArray.pop();
	}
	private Status saveData(String data){
		return new Status();
	}
	
	
	
	

	@Test
	public void debounce(){
		LazyFutureStream.sequentialCommonBuilder()
						.iterateInfinitely(0, it -> it + 1)
						.debounce(100, TimeUnit.MILLISECONDS)
						.peek(System.out::println)
						.runOnCurrent();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@Test
	public void onePerSecond(){
		
		
		
		
		LazyFutureStream.sequentialCommonBuilder()
						.iterateInfinitely(0, it -> it + 1)
						.onePer(1, TimeUnit.SECONDS)
						.map(seconds -> readStatus())
						.retry(this::saveStatus)
						.peek(System.out::println)
						.capture(Throwable::printStackTrace)
						.block();
		
	}
	
	
	private String saveStatus(Status s){
		if(count++%2==0)
			throw new RuntimeException();
	
		return "Status saved:"+s.getId();
	}

	int count =0;
	
	
	
	
	private Status readStatus() {
		return new Status();
	}
	static int nextId=1;
	@Getter
	class Status{
		long id = nextId++;
	}



	String status="ok";
	/**
	 * check status every second, batch every 10 secs
	 */
	@Test @Ignore
	public void onePerSecondAndBatch(){
		List<Collection<String>> collected = LazyFutureStream.sequentialCommonBuilder().reactInfinitely(()->status)
													.withQueueFactory(QueueFactories.boundedQueue(1))
													.onePer(1, TimeUnit.SECONDS)
													.batchByTime(10, TimeUnit.SECONDS)
													.limit(15)
													.block();
		System.out.println(collected);
	}
	/**
	 * create a stream of time intervals in seconds
	 */
	@Test 
	public void secondsTimeInterval(){
		List<Collection<Integer>> collected = LazyFutureStream.sequentialCommonBuilder().iterateInfinitely(0, it -> it+1)
													.withQueueFactory(QueueFactories.boundedQueue(1))
													.onePer(1, TimeUnit.SECONDS)
													.peek(System.out::println)
													.batchByTime(10, TimeUnit.SECONDS)
													.peek(System.out::println)
													.limit(15)
													.block();
		System.out.println(collected);
	}
	@Test @Ignore
	public void range(){
		List<Collection<Integer>> collected = LazyFutureStream.sequentialCommonBuilder()
														.fromPrimitiveStream(IntStream.range(0, 10))
														.batchBySize(5)
														.block();
		System.out.println(collected);
	}
	
	@Test @Ignore
	public void executeRestCallInPool(){
		boolean success  = SequentialElasticPools.eagerReact.react( er-> er.react(()->restGet())
														.map(Tutorial::transformData)
														.then(Tutorial::saveToDb)
														.first());
	}
	private static boolean saveToDb(Object o){
		return true;
	}
	private Object restGet() {
		// TODO Auto-generated method stub
		return null;
	}
	private static Object transformData(Object o) {
		// TODO Auto-generated method stub
		return null;
	}
}
