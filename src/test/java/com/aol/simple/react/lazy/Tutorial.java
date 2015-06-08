package com.aol.simple.react.lazy;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import org.junit.Ignore;
import org.junit.Test;

import com.aol.simple.react.async.Queue;
import com.aol.simple.react.async.QueueFactories;
import com.aol.simple.react.exceptions.SimpleReactFailedStageException;
import com.aol.simple.react.stream.simple.SimpleReact;
import com.aol.simple.react.stream.traits.EagerFutureStream;
import com.aol.simple.react.stream.traits.FutureStream;
import com.aol.simple.react.stream.traits.LazyFutureStream;
import com.aol.simple.react.threads.SequentialElasticPools;
import com.google.common.collect.ImmutableMap;
import com.nurkiewicz.asyncretry.AsyncRetryExecutor;


public class Tutorial {

	@SuppressWarnings("unchecked")
	@Test
	public void zipByResults() {

		EagerFutureStream<String> a = EagerFutureStream.parallelCommonBuilder().react(
				() -> slowest(), () -> fast(), () -> slow());
		EagerFutureStream<Integer> b = EagerFutureStream.sequentialCommonBuilder().of(
				1, 2, 3, 4, 5, 6);

		a.zip(b).peek(System.out::println);
		System.out.println("Not blocked!");
		

	}

	@SuppressWarnings("unchecked")
	@Test
	public void zipWithIndex() {

		LazyFutureStream.sequentialCommonBuilder()
				.react(() -> slowest(), () -> fast(), () -> slow())
				.zipWithIndex().forEach(System.out::println);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void zipFuturesWithIndex() {

		EagerFutureStream.parallelCommonBuilder()
				.react(() -> slowest(), () -> fast(), () -> slow())
				.zipFuturesWithIndex().forEach(System.out::println);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void combineLatest() {
		LazyFutureStream
				.parallelCommonBuilder()
				.react(() -> slowest(), () -> fast(), () -> slow())
				.combineLatest(
						LazyFutureStream.sequentialCommonBuilder().of(1, 2, 3, 4, 5,
								6)).forEach(System.out::println);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void withLatest() {
		LazyFutureStream
				.sequentialBuilder()
				.react(() -> slowest(), () -> fast(), () -> slow())
				.withLatest(
						LazyFutureStream.sequentialCommonBuilder().of(1, 2, 3, 4, 5,
								6)).forEach(System.out::println);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void zipByFutures() {

		LazyFutureStream.parallelCommonBuilder()
				.react(() -> slowest(), () -> fast(), () -> slow())
				.flatMap(it -> it.chars().boxed()).forEach(System.out::println);

		EagerFutureStream<String> a = EagerFutureStream.parallelCommonBuilder()
				.react(() -> slowest(), () -> fast(), () -> slow());
		EagerFutureStream<Integer> b = EagerFutureStream.sequentialCommonBuilder()
				.of(1, 2, 3, 4, 5, 6);

		a.zipFutures(b).forEach(System.out::println);

	}

	private String slowest() {
		sleep(250);
		return "slowestResult";
	}

	private String slow() {
		sleep(10);
		return "slowResult";
	}

	private String fast() {
		return "fast";
	}

	@Test
	public void errorHandling() {
		AsyncRetryExecutor retrier = new AsyncRetryExecutor(
				Executors.newScheduledThreadPool(Runtime.getRuntime()
						.availableProcessors())).retryOn(Throwable.class)
				.withMaxDelay(1_000). // 1 seconds
				withUniformJitter(). // add between +/- 100 ms randomly
				withMaxRetries(1);

		List<String> results = LazyFutureStream.sequentialCommonBuilder()
				.withRetrier(retrier)
				.react(() -> "new event1", () -> "new event2")
				.retry(this::unreliable).onFail(e -> "default")
				.peek(System.out::println).capture(Throwable::printStackTrace)
				.block();

		assertThat(results.size(), equalTo(2));

	}

	private String unreliable(Object o) {
		throw new RuntimeException();

	}

	@SuppressWarnings("unchecked")
	@Test
	public void shard() {
		Map<Integer, Queue<User>> shards = new HashMap<>();
		shards.put(0, new Queue<>());
		shards.put(1, new Queue<>());
		shards.put(2, new Queue<>());

		Map<Integer, LazyFutureStream<User>> sharded = LazyFutureStream
				.sequentialCommonBuilder().react(() -> loadUserData())
				.flatMap(Collection::stream)
				.shard(shards, user -> user.getUserId() % 3);

		System.out.println("First shard");
		sharded.get(0).forEach(System.out::println);

		System.out.println("Second shard");
		sharded.get(1).forEach(System.out::println);

		System.out.println("Third shard");
		sharded.get(2).forEach(System.out::println);
	}

	@Test
	public void firstOf(){
		
		EagerFutureStream<String> stream1 =EagerFutureStream.sequentialCommonBuilder()
													.react(() -> loadFromDb())
													.map(this::convertToStandardFormat);

		EagerFutureStream<String> stream2 = EagerFutureStream.sequentialCommonBuilder()
													.react(() -> loadFromService1())
													.map(this::convertToStandardFormat);

		EagerFutureStream<String> stream3 = EagerFutureStream.sequentialCommonBuilder()
													.react(() -> loadFromService2())
													.map(this::convertToStandardFormat);

		EagerFutureStream.firstOf(stream1, stream2, stream3)
						.peek(System.out::println)
						.map(this::saveData)
						.runOnCurrent();
			
		
	}
	
	
	@Test
	public void anyOf(){
		


		LazyFutureStream.parallelCommonBuilder().react(() -> loadFromDb(),() -> loadFromService1(),
														() -> loadFromService2())
						.map(this::convertToStandardFormat)
						.peek(System.out::println)
						.map(this::saveData)
						.block();
			
		
	}
	
	private String convertToStandardFormat(String input){
		if(count++%2==0){
			System.out.println("sleeping!" + input);
			sleep(1000);
		}
		return "converted " + input;
	}
	private String loadFromDb(){
		
		return "from db";
	}
	private String loadFromService1(){
		
		return "from service1";
	}
	private String loadFromService2(){
		return "from service2";
	}


	@Test
	public void allOf(){
		 LazyFutureStream.sequentialCommonBuilder().react(()->1,()->2,()->3)
		 									 .map(it->it+100)
		 									 .peek(System.out::println)
		 									 .allOf(c-> ImmutableMap.of("numbers",c))
		 									 .peek(System.out::println)
		 									 .block();
	}
	
	
	
	
	
	
	
	@Test
	public void testFilter(){
		LazyFutureStream.sequentialCommonBuilder()
		.of(loadUserData())
		.filter(User::hasPurchased)
		.forEach(System.out::println);
	}
	
	@Test
	public void filterMapReduceFlatMap() {
		int totalVisits = 
				LazyFutureStream.sequentialCommonBuilder()
											.react(() -> loadUserData())
											.flatMap(Collection::stream)
											.filter(User::hasPurchased)
											.peek(System.out::println)
											.map(User::getTotalVisits)
											.peek(System.out::println)
											.reduce(0, (acc, next) -> acc + next);

		System.out.println("Total visits is : " + totalVisits);
	}

	@AllArgsConstructor
	@ToString
	@Getter
	class User {
		boolean purchased;
		int totalVisits;
		final int userId = count++;

		public boolean hasPurchased() {
			return purchased;
		}
	}

	private Collection<User> loadUserData() {
		return Arrays.asList(new User(true, 102), new User(false, 501),
				new User(true, 14), new User(true, 23), new User(false, 3),
				new User(true, 531), new User(false, 56));
	}

	@Test
	public void gettingStarted() {

		List<String> results = new SimpleReact()
				.react(() -> readData("data1"), () -> readData("data2"))
				.onFail(RuntimeException.class, this::loadFromDb)
				.peek(System.out::println).then(this::processData).block();

	}

	private String readData(String name) {
		if (name.equals("data1"))
			throw new RuntimeException();

		else
			return "hello world from file!";

	}

	private String processData(String data) {
		return "processed : " + data;
	}

	private String loadFromDb(SimpleReactFailedStageException e) {
		return "hello world from DB!";
	}

	@Test
	public void skipUntil() {
		FutureStream<Boolean> stoppingStream = LazyFutureStream
				.sequentialCommonBuilder().react(() -> 1000).then(this::sleep)
				.peek(System.out::println);
		System.out.println(EagerFutureStream.sequentialCommonBuilder()
				.of(IntStream.range(0, 1000000))
				// .peek(System.out::println)
				.skipUntil(stoppingStream).peek(System.out::println).block()
				.size());
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
	public void jitter() {
		LazyFutureStream.sequentialCommonBuilder()
				.of(IntStream.range(0, 1000))
				.map(it -> it * 100).jitter(100l)
				.peek(System.out::println).runOnCurrent();
	}

	@Test
	public void fixedDelay() {

		LazyFutureStream.sequentialCommonBuilder()
				.of(IntStream.range(0, 1000))
				.fixedDelay(1l, TimeUnit.MICROSECONDS).peek(System.out::println)
				.runOnCurrent();
	}

	@Test
	public void elasticPool() {

		List<String> files = Arrays.asList("/tmp/1.data", "/tmp/2.data");

		List<Status> data = SequentialElasticPools.lazyReact.react(er -> er
				.of(files).map(this::loadData)
				.peek(System.out::println).map(this::saveData)
				.collect(Collectors.toList()));

		System.out.println("Loaded and saved " + data.size());
	}

	
	@Test
	public void add100() {

		new SimpleReact().of(1, 2, 3, 4, 5).then(num -> num + 100)
				.then(num -> Thread.currentThread().getId())
				.peek(System.out::println);

	}

	Stack<String> dataArray = new Stack() {
		{
			add("{order:1000,{customer:604}}");
			add("{order:1001,{customer:605}}");
		}
	};

	private String loadData(String file) {
		sleep(1000);
		return dataArray.pop();
	}

	private Status saveData(String data) {
		return new Status();
	}

	@Test
	public void debounce() {
		LazyFutureStream.sequentialCommonBuilder()
				.iterateInfinitely(0, it -> it + 1)
				.limit(100)
				.debounce(100, TimeUnit.MILLISECONDS).peek(System.out::println)
				.runOnCurrent();
	}

	@Test
	public void onePerSecond() {

		LazyFutureStream.sequentialCommonBuilder()
				.iterateInfinitely(0, it -> it + 1)
				.limit(100)
				.onePer(1, TimeUnit.MICROSECONDS)
				.map(seconds -> readStatus()).retry(this::saveStatus)
				.peek(System.out::println).capture(Throwable::printStackTrace)
				.block();

	}

	private String saveStatus(Status s) {
		//if (count++ % 2 == 0)
		//	throw new RuntimeException();

		return "Status saved:" + s.getId();
	}

	int count = 0;

	private Status readStatus() {
		return new Status();
	}

	static int nextId = 1;

	@Getter
	class Status {
		long id = nextId++;
	}

	String status = "ok";

	/**
	 * check status every second, batch every 10 secs
	 */
	@Test
	@Ignore
	public void onePerSecondAndBatch() {
		List<Collection<String>> collected = LazyFutureStream
				.sequentialCommonBuilder().reactInfinitely(() -> status)
				.withQueueFactory(QueueFactories.boundedQueue(1))
				.onePer(1, TimeUnit.SECONDS).batchByTime(10, TimeUnit.SECONDS)
				.limit(15).block();
		System.out.println(collected);
	}

	/**
	 * create a stream of time intervals in seconds
	 */
	@Test @Ignore
	public void secondsTimeInterval() {
		List<Collection<Integer>> collected = LazyFutureStream
				.sequentialCommonBuilder().iterateInfinitely(0, it -> it + 1)
				//.limit(100)
				.withQueueFactory(QueueFactories.boundedQueue(1))
				.onePer(1, TimeUnit.MICROSECONDS).peek(System.out::println)
				.batchByTime(10, TimeUnit.MICROSECONDS).peek(System.out::println)
				.limit(15).block();
		System.out.println(collected);
	}

	@Test
	@Ignore
	public void range() {
		List<Collection<Integer>> collected = LazyFutureStream
				.sequentialCommonBuilder()
				.of(IntStream.range(0, 10)).batchBySize(5)
				.block();
		System.out.println(collected);
	}

	@Test
	@Ignore
	public void executeRestCallInPool() {
		boolean success = SequentialElasticPools.eagerReact.react(er -> er
				.react(() -> restGet()).map(Tutorial::transformData)
				.then(Tutorial::saveToDb).first());
	}

	private static boolean saveToDb(Object o) {
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

	@Test
	public void batchBySize() {

		LazyFutureStream
				.parallelCommonBuilder()
				.iterateInfinitely("", last -> nextFile())
				.limit(100)
				.map(this::readFileToString)
				.map(this::parseJson)
				.batchBySize(10)
				.onePer(1, TimeUnit.MICROSECONDS)
				.peek(batch -> System.out.println("batched : " + batch))
				.map(this::processOrders)
				.flatMap(Collection::stream)
				.peek(individual -> System.out.println("Flattened : "
						+ individual)).forEach(this::save);

	}

	@Test
	public void batchByTime() {

		LazyFutureStream
				.parallelCommonBuilder()
				.iterateInfinitely("", last -> nextFile())
				.limit(100)
				.map(this::readFileToString)
				.map(this::parseJson)
				.batchByTime(1, TimeUnit.MICROSECONDS)
				.peek(batch -> System.out.println("batched : " + batch))
				.map(this::processOrders)
				.flatMap(Collection::stream)
				.peek(individual -> System.out.println("Flattened : "
						+ individual)).forEach(this::save);

	}

	@Test
	public void chunkSinceLastRead() {

		LazyFutureStream
				.parallelCommonBuilder()
				.iterateInfinitely("", last -> nextFile())
				.limit(100)
				.map(this::readFileToString)
				.map(this::parseJson)
				.chunkSinceLastRead()
				.peek(batch -> System.out.println("batched : " + batch))
				.map(this::processOrders)
				.flatMap(Collection::stream)
				.peek(individual -> System.out.println("Flattened : "
						+ individual)).forEach(this::save);

	}

	private void save(Map map) {

	}

	private Collection<Map> processOrders(Collection<Map> input) {
		sleep(100);
		return input.stream().map(m -> ImmutableMap.of("processed", m))
				.collect(Collectors.toList());
	}

	private Map parseJson(String json) {
		return ImmutableMap.of("id", count++, "type", "order", "date",
				new Date());
	}

	private String readFileToString(String name) {
		return "";
	}

	private String nextFile() {

		return null;
	}
}
