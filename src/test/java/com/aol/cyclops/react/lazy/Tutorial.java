package com.aol.cyclops.react.lazy;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Ignore;
import org.junit.Test;
import org.pcollections.HashTreePMap;

import com.aol.cyclops.control.LazyReact;
import com.aol.cyclops.control.SimpleReact;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.react.async.Queue;
import com.aol.cyclops.react.async.factories.QueueFactories;
import com.aol.cyclops.react.exceptions.SimpleReactFailedStageException;
import com.aol.cyclops.react.stream.traits.LazyFutureStream;
import com.aol.cyclops.react.stream.traits.SimpleReactStream;
import com.aol.cyclops.react.threads.SequentialElasticPools;
import com.nurkiewicz.asyncretry.AsyncRetryExecutor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;


public class Tutorial {

	@SuppressWarnings("unchecked")
	@Test
	public void zipByResults() {

		LazyFutureStream<String> a = LazyReact.parallelCommonBuilder().react(
				() -> slowest(), () -> fast(), () -> slow());
		LazyFutureStream<Integer> b = LazyReact.sequentialCommonBuilder().of(
				1, 2, 3, 4, 5, 6);

		a.zip(b).peek(System.out::println);
		System.out.println("Not blocked!");
		

	}

	@SuppressWarnings("unchecked")
	@Test
	public void zipWithIndex() {

		LazyReact.sequentialCommonBuilder()
				.react(() -> slowest(), () -> fast(), () -> slow())
				.zipWithIndex().forEach(System.out::println);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void zipFuturesWithIndex() {

		LazyReact.parallelCommonBuilder()
				.react(() -> slowest(), () -> fast(), () -> slow())
				.zipWithIndex().forEach(System.out::println);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void combineLatest() {
		SimpleReact
				.parallelCommonBuilder()
				.react(() -> slowest(), () -> fast(), () -> slow())
				.combineLatest(
						SimpleReact.sequentialCommonBuilder().of(1, 2, 3, 4, 5,
								6)).forEach(System.out::println);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void withLatest() {
		SimpleReact
				.sequentialBuilder()
				.react(() -> slowest(), () -> fast(), () -> slow())
				.withLatest(
						SimpleReact.sequentialCommonBuilder().of(1, 2, 3, 4, 5,
								6)).forEach(System.out::println);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void zipByFutures() {

		LazyReact.parallelCommonBuilder()
				.react(() -> slowest(), () -> fast(), () -> slow())
				.flatMap(it -> it.chars().boxed()).forEach(System.out::println);

		LazyFutureStream<String> a = LazyReact.parallelCommonBuilder()
				.react(() -> slowest(), () -> fast(), () -> slow());
		LazyFutureStream<Integer> b = LazyReact.sequentialCommonBuilder()
				.of(1, 2, 3, 4, 5, 6);

		a.actOnFutures().zip(b).forEach(System.out::println);

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

		List<String> results = LazyReact.sequentialCommonBuilder()
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

		Map<Integer, LazyFutureStream<User>> sharded = LazyReact
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
		
		SimpleReactStream<String> stream1 =SimpleReact.sequentialCommonBuilder()
													.react(() -> loadFromDb())
													.then(this::convertToStandardFormat);

		SimpleReactStream<String> stream2 = SimpleReact.sequentialCommonBuilder()
													.react(() -> loadFromService1())
													.then(this::convertToStandardFormat);

		SimpleReactStream<String> stream3 = SimpleReact.sequentialCommonBuilder()
													.react(() -> loadFromService2())
													.then(this::convertToStandardFormat);

		SimpleReactStream.firstOf(stream1, stream2, stream3)
						.peek(System.out::println)
						.then(this::saveData)
						.block();
			
		
	}
	
	
	@Test
	public void anyOf(){
		


		LazyReact.parallelCommonBuilder().react(() -> loadFromDb(),() -> loadFromService1(),
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
		 SimpleReact.sequentialCommonBuilder().react(()->1,()->2,()->3)
		 									 .then(it->it+100)
		 									 .peek(System.out::println)
		 									 .allOf(c-> HashTreePMap.singleton("numbers",c))
		 									 .peek(System.out::println)
		 									 .block();
	}
	
	
	
	
	
	
	
	@Test
	public void testFilter(){
		LazyReact.sequentialCommonBuilder()
		.from(loadUserData())
		.filter(User::hasPurchased)
		.forEach(System.out::println);
	}
	
	@Test
	public void filterMapReduceFlatMap() {
		int totalVisits = 
				LazyReact.sequentialCommonBuilder()
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
		SimpleReactStream<Boolean> stoppingStream = SimpleReact
				.sequentialCommonBuilder().react(() -> 1000).then(this::sleep)
				.peek(System.out::println);
		System.out.println(SimpleReact.sequentialCommonBuilder()
				.from(IntStream.range(0, 1000000))
				// .peek(System.out::println)
				.skipUntil(stoppingStream).peek(System.out::println)
				.toList()
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
		LazyReact.sequentialCommonBuilder()
				.from(IntStream.range(0, 1000))
				.map(it -> it * 100).jitter(100l)
				.peek(System.out::println).runOnCurrent();
	}

	@Test
	public void fixedDelay() {

		LazyReact.sequentialCommonBuilder()
				.from(IntStream.range(0, 1000))
				.fixedDelay(1l, TimeUnit.MICROSECONDS).peek(System.out::println)
				.runOnCurrent();
	}

	@Test
	public void elasticPool() {

		List<String> files = Arrays.asList("/tmp/1.data", "/tmp/2.data");

		List<Status> data = SequentialElasticPools.lazyReact.react(er -> er
				.from(files).map(this::loadData)
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
		LazyReact.sequentialCommonBuilder()
				.iterateInfinitely(0, it -> it + 1)
				.limit(100)
				.debounce(100, TimeUnit.MILLISECONDS).peek(System.out::println)
				.runOnCurrent();
	}

	@Test
	public void onePerSecond() {

		LazyReact.sequentialCommonBuilder()
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

	volatile int count = 0;

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
		List<ListX<String>> collected = LazyReact
				.sequentialCommonBuilder().reactInfinitely(() -> status)
				.withQueueFactory(QueueFactories.boundedQueue(1))
				.onePer(1, TimeUnit.SECONDS).groupedByTime(10, TimeUnit.SECONDS)
				.limit(15).block();
		System.out.println(collected);
	}

	/**
	 * create a stream of time intervals in seconds
	 */
	@Test @Ignore
	public void secondsTimeInterval() {
		List<ListX<Integer>> collected = LazyReact
				.sequentialCommonBuilder().iterateInfinitely(0, it -> it + 1)
				//.limit(100)
				.withQueueFactory(QueueFactories.boundedQueue(1))
				.onePer(1, TimeUnit.MICROSECONDS).peek(System.out::println)
				.groupedByTime(10, TimeUnit.MICROSECONDS).peek(System.out::println)
				.limit(15).block();
		System.out.println(collected);
	}

	@Test
	@Ignore
	public void range() {
		List<ListX<Integer>> collected = LazyReact
				.sequentialCommonBuilder()
				.from(IntStream.range(0, 10)).batchBySize(5)
				.block();
		System.out.println(collected);
	}

	@Test
	@Ignore
	public void executeRestCallInPool() {
		boolean success = SequentialElasticPools.lazyReact.react(er -> er
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

		LazyReact
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
	public void filterAndLimit(){
		LazyFutureStream.of(1,2,3,4,5,6,7,8,9,10)
					.limit(6)
					.filter(i->i%2==0)
					.forEach(System.out::println);
	}
	@Test
	public void filterAndLimitInfinite(){
	
		LazyReact
				.parallelCommonBuilder()
					.iterateInfinitely("", last -> nextFile())
					.limit(100)
					.map(d->Arrays.asList())
					.filter(i-> !i.isEmpty())
					.forEach(System.out::println);
	}
	
	@Test
	public void testFilterAndFlatMapWithFilter(){
		count=0;
		LazyFutureStream.of(1,2,3).limit(2)
		.flatMap(a->Arrays.asList(10,20,30,40).stream())
		.limit(6)
		.forEach(next->count++);
		
		assertThat(count,equalTo(6));
	}
	@Test
	public void testFilterAndFlatMapWithFilterRunOnCurrent(){
		count=0;
		LazyFutureStream.of(1,2,3).limit(2)
		.flatMap(a->Arrays.asList(10,20,30,40).stream())
		.limit(6)
		.peek(next->count++)
		.runOnCurrent();
		
		assertThat(count,equalTo(6));
	}
	@Test
	public void testFilterAndFlatMapWithFilterList(){
		count=0;
		List list = LazyFutureStream.of(1,2,3).limit(2)
		.flatMap(a->Arrays.asList(10,20,30,40).stream())
		.limit(6)
		.toList();
		
		assertThat(list.size(),equalTo(6));
	}
	AtomicInteger count2 =new AtomicInteger(0);
	int count3 =0;
	volatile int otherCount;
	volatile int peek =0;
	@Test
	public void batchByTimeFiltered() {

		for(int x=0;x<10;x++){
			count2=new AtomicInteger(0);
			List<Collection<Map>> result = new ArrayList<>();
					LazyReact
					.parallelCommonBuilder()
					.iterateInfinitely("", last -> nextFile())
					.limit(1000)
					
					.map(this::readFileToString)
					.map(this::parseJson)
					.peek(i->System.out.println(++otherCount))
					//.filter(i->false)
					.groupedByTime(1, TimeUnit.MICROSECONDS)
					
					.peek(batch -> System.out.println("batched : " + batch + ":" + (++peek)))
				//	.filter(c->!c.isEmpty())
					.peek(batch->count3= count3+batch.size())
					.map(this::processOrders)
					//.toList();
					.forEach(next -> { //result.addAll((Collection)next); 
					count2.getAndAdd(next.size());});
		//	assertThat(size,equalTo(100));
			
			System.out.println("In flight count " + count3 + " :" + otherCount);
			System.out.println(result.size());
			System.out.println(result);
			System.out.println("x" +x);
			assertThat(count2.get(),equalTo(1000));

		}
	}
	@Test
	public void batchByTimeFilteredEager() {
		count2=new AtomicInteger(0);
		LazyReact
				.parallelCommonBuilder()
				.from(list100())
				.limit(100)
				
				.map(this::readFileToString)
				.map(this::parseJson)
				//.filter(i->false)
								.groupedByTime(1, TimeUnit.MICROSECONDS)
				
				.peek(batch -> System.out.println("batched : " + batch))
				.filter(c->!c.isEmpty())
				
				.map(this::processOrders)
				.forEach(next -> count2.getAndAdd(next.size()));
		
		assertThat(count2.get(),equalTo(100));
	}

	private List<String> list100() {
		List col = new ArrayList();
		for(int i=0;i<100;i++)
			col.add(""+i);
		return col;
	}
	@Test
	public void batchByTimeFilteredForEach() {
		count2=new AtomicInteger(0);
		LazyReact
				.parallelCommonBuilder()
				.iterateInfinitely("", last -> nextFile())
				.limit(100)
				
				.map(this::readFileToString)
				.map(this::parseJson)
				//.filter(i->false)
				
				.groupedByTime(1, TimeUnit.MICROSECONDS)
				
				.peek(batch -> System.out.println("batched : " + batch))
				.filter(c->!c.isEmpty())
				
				.map(this::processOrders)
				.toList().stream().forEach(next ->  count2.getAndAdd(next.size()));;
		
		assertThat(count2.get(),equalTo(100));
	}
	@Test
	public void batchByTimeFilteredForEachEager() {
		count2=new AtomicInteger(0);
		LazyReact
				.parallelCommonBuilder()
				.from(list100())
				.limit(100)
				
				.map(this::readFileToString)
				.map(this::parseJson)
				//.filter(i->false)
				
				.groupedByTime(1, TimeUnit.MICROSECONDS)
				
				.peek(batch -> System.out.println("batched : " + batch))
				.filter(c->!c.isEmpty())
				
				.map(this::processOrders)
				.toList().stream().forEach(next -> count2.getAndAdd(next.size()));
		
		assertThat(count2.get(),equalTo(100));
	}
	@Test
	public void batchByTime() {

		LazyReact
				.parallelCommonBuilder()
				.iterateInfinitely("", last -> nextFile())
				.limit(100)
				
				.map(this::readFileToString)
				.map(this::parseJson)
				
				.peek(next->System.out.println("Counter " +count2.incrementAndGet()))
				.groupedByTime(10, TimeUnit.MICROSECONDS)
				.peek(batch -> System.out.println("batched : " + batch))
				.filter(c->!c.isEmpty())
				
				.map(this::processOrders)
				.forEach(System.out::println);
			/**	
				.flatMap(Collection::stream)
				.peek(individual -> System.out.println("Flattened : "
						+ individual))
				.forEach(this::save);**/

	}

	@Test
	public void chunkSinceLastRead() {

		LazyReact
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
		return input.stream().map(m -> HashTreePMap.singleton("processed", m))
				.collect(Collectors.toList());
	}

	private Map parseJson(String json) {
		return HashTreePMap.<Object,Object>singleton("id", count++).plus( "type", "order").plus( "date",
				new Date());
	}

	private String readFileToString(String name) {
		return "";
	}

	private String nextFile() {

		return null;
	}
}
