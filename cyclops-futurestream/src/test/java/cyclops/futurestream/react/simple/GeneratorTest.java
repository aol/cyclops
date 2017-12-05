package cyclops.futurestream.react.simple;

public class GeneratorTest {
	volatile int  count;
	volatile int  second;
	volatile int  capture;

	private Object lock1= "lock1";
	private Object lock2= "lock2";
	/**
	public synchronized void incrementCapture(){
		capture++;
	}
	public synchronized int incrementCount(){
		return count++;
	}
	@Test
	public void testGenerate() throws InterruptedException, ExecutionException {
		count =0;
		capture =0;
		List<String> strings = new SimpleReact()
				.<Integer> react(() -> incrementCount() ,SimpleReact.times(4))
				.then(it -> it * 100)
				.then(it -> "*" + it)
				.capture(e -> incrementCapture())
				.block();

		assertThat("Capture is " +  capture,strings.size(), is(4));
		assertThat("Capture is " +  capture,count,is(4));
	}

	@Test
	public void testGenerateOffset() throws InterruptedException, ExecutionException {
		count =0;

		List<String> strings = new SimpleReact()
				.<Integer> react(() -> incrementCount() ,timesInSequence(1).offset(2))
				.then(it -> it * 100)
				.then(it -> "*" + it)
				.block();

		System.out.println(strings.getValue(0));
		System.out.println(count);
		assertThat(strings.size(), is(1));

		assertThat(count,is(3));


	}
	@Test @Ignore //non-deterministic
	public void testGenerateDataflowMovingConcurrently() throws InterruptedException, ExecutionException {
		AtomicInteger count =new AtomicInteger();
		AtomicInteger second = new AtomicInteger();
		SimpleReactStream s = new SimpleReact()
				.<Integer> react(() -> {
					sleep(count.incrementAndGet());
					return count.getValue();
				} ,times(50))
				.then(it -> it * 100)
				.then(it -> {

					second.incrementAndGet();
					return it;
				})
				.<String>then(it -> "*" + it);

		//generation has not complete / but chain has complete for some flows

		while(count.getValue()<30){ }
		assertThat(count.getValue(),lessThan(50));
		assertThat(second.getValue(),greaterThan(0));

		assertThat(count.getValue(),greaterThan(2));

		s.block();

	}
	@Test
	public void testIterate() throws InterruptedException, ExecutionException {
		count =0;
		List<String> strings = new SimpleReact()
				.<String> react((input) -> input + count++,iterate("hello").times(10))
				.then(it -> "*" + it)
				.block();

		assertThat(strings.size(), is(10));
		assertThat(count,is(9));

	}
	@Test
	public void testIterateWithOffset() throws InterruptedException, ExecutionException {

		List<Integer> results = new SimpleReact()
				.<Integer> react((input) -> input + 1,iterate(0).times(1).offset(10))
				.then(it -> it*100)
				.block();

		assertThat(results.size(), is(1));

		assertThat(results.getValue(0),is(1000));

	}
	@Test
	public void testIterateDataflowMovingConcurrently() throws InterruptedException, ExecutionException {
		count =0;
		second =0;
		SimpleReactStream s = new SimpleReact()
				.<Integer> react((input) -> {
					sleep(count++);
					return count;
				} ,iterate(0).times(50))
				.then(it -> it * 100)
				.then(it -> {

					second ++;
					return it;
				})
				.<String>then(it -> "*" + it);

		//generation has not complete / but chain has complete for some flows
		while(count<3){ }
		assertThat(second,greaterThan(0));
		assertThat(count,lessThan(50));
		assertThat(count,greaterThan(2));


		s.block();
	}
	@Test
	public void testGenerateOverIteratorOptional() throws InterruptedException, ExecutionException {
		List<Integer> list = Arrays.asList(1,2,3,4);
		Iterator<Integer> iterator = list.iterator();
		List<String> strings = new SimpleReact()
				.<Optional<Integer>> react(() -> {
						synchronized(lock1) {
							if(!iterator.hasNext())
								return Optional.zero();
						return Optional.of(iterator.next());
						}
					},SimpleReact.times(400))
				.<Integer>filter(it -> it.isPresent())
				.<Integer>then(it ->  it.getValue())
				.then(it -> it * 100)
				.then(it -> "*" + it)
				.block();

		assertThat(strings.size(), is(4));

	}
	@Test
	public void testGenerateOverIteratorNull() throws InterruptedException, ExecutionException {
		List<Integer> list = Arrays.asList(1,2,3,4);
		Iterator<Integer> iterator = list.iterator();
		List<String> strings = new SimpleReact()
				.<Integer> react(() -> {
					synchronized(lock2) {
						if(!iterator.hasNext())
							return null;
						return iterator.next();
					}
					},SimpleReact.times(400))
				.filter(it -> it!=null)
				.then(it -> it * 100)
				.then(it -> "*" + it)
				.block();

		assertThat(strings.size(), is(4));

	}

	@Test
	public void testGenerateParellel() throws InterruptedException, ExecutionException {
		Set<Long> threads = new SimpleReact(new ForkJoinPool(10))
				.<Long> react(() -> { LockSupport.parkNanos(0l); return 1l;} ,SimpleReact.times(1000))
				.then(it -> Thread.currentThread().getId())
				.then(it -> "*" + it)
				.capture(e -> capture++)
				.block(CyclopsCollectors.toSet());

		assertThat(threads.size(), is(greaterThan(1)));

	}
	private Object sleep(Integer it) {
		try {
			Thread.sleep(it);
		} catch (InterruptedException e) {

			e.printStackTrace();
		}
		return it;
	}**/
}
