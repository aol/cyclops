package cyclops.streams.push.asyncreactivestreams;

import cyclops.companion.Streams;
import cyclops.collections.ListX;
import cyclops.control.Maybe;
import cyclops.function.Monoid;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Spouts;
import org.hamcrest.Matchers;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;


//see BaseSequentialSeqTest for in order tests
public  class AsyncRSReactiveStreamXTest {
 	int ITERATIONS = 1;
	ReactiveSeq<Integer> empty;
	ReactiveSeq<Integer> nonEmpty;

	@Before
	public void setup(){
		empty = of();
		nonEmpty = of(1);
	}

	protected <U> ReactiveSeq<U> of(U... array){
		return Spouts.from(Flux.just(array).subscribeOn(Schedulers.fromExecutor(ForkJoinPool.commonPool())));

	}
	
	

    @Test
    public void flatMapPublisher() throws InterruptedException{
		//of(1,2,3)
		//		.flatMapP(i->Maybe.of(i)).printOut();

        assertThat(of(1,2,3)
                        .flatMapP(i->Maybe.of(i))
                        .toListX(),Matchers.hasItems(1,2,3));
        
        
    }
    

    private void sleep2(int time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

	
	protected Object value() {
		
		return "jello";
	}
	private int value2() {
		
		return 200;
	}
	
	
	@Test
	public void batchBySize(){
		System.out.println(of(1,2,3,4,5,6).grouped(3).collect(Collectors.toList()));
		assertThat(of(1,2,3,4,5,6).grouped(3).collect(Collectors.toList()).size(),is(2));
	}
	

	

	
	@Test
	public void limitWhileTest(){
		
		List<Integer> list = new ArrayList<>();
		while(list.size()==0){
			list = of(1,2,3,4,5,6).limitWhile(it -> it<4)
						.peek(it -> System.out.println(it)).collect(Collectors.toList());
	
		}
		assertThat(Arrays.asList(1,2,3,4,5,6),hasItem(list.get(0)));
		
		
		
		
	}

    @Test
    public void testScanLeftStringConcat() {
        assertThat(of("a", "b", "c").scanLeft("", String::concat).toList().size(),
        		is(4));
    }
    @Test
    public void testScanLeftSum() {
    	assertThat(of("a", "ab", "abc").map(str->str.length()).scanLeft(0, (u, t) -> u + t).toList().size(), 
    			is(asList(0, 1, 3, 6).size()));
    }
    @Test
    public void testScanRightStringConcatMonoid() {
        assertThat(of("a", "b", "c").scanRight(Monoid.of("", String::concat)).toList().size(),
            is(asList("", "c", "bc", "abc").size()));
    }
    @Test
    public void testScanRightStringConcat() {
        assertThat(of("a", "b", "c").scanRight("", String::concat).toList().size(),
            is(asList("", "c", "bc", "abc").size()));
    }
    @Test
    public void testScanRightSum() {
    	assertThat(of("a", "ab", "abc").map(str->str.length()).scanRight(0, (t, u) -> u + t).toList().size(),
            is(asList(0, 3, 5, 6).size()));

        
    }
	@Test
	public void cycleIterateIterable(){
		Iterator<Integer> it = ReactiveSeq.fromIterable(Arrays.asList(1)).stream().cycle(2).iterator();
		List<Integer> list2 = new ArrayList<>();
		while(it.hasNext())
			list2.add(it.next());
		assertThat(list2,equalTo(ListX.of(1,1)));
	}
    @Test
	public void cycleIterate(){
		Iterator<Integer> it = of(1).stream().cycle(2).iterator();
		List<Integer> list2 = new ArrayList<>();
		while(it.hasNext())
			list2.add(it.next());
		assertThat(list2,equalTo(ListX.of(1,1)));
	}
	@Test
	public void cycleIterate2(){
		Iterator<Integer> it = of(1,2).stream().cycle(2).iterator();
		List<Integer> list2 = new ArrayList<>();
		while(it.hasNext())
			list2.add(it.next());
		assertThat(list2,equalTo(ListX.of(1,2,1,2)));
	}
    

    @Test
    public void testReverse() {

        assertThat( of(1, 2, 3).reverse().toList(), equalTo(asList(3, 2, 1)));
    }
    @Test
    public void testReverseList() {
    	
        assertThat( Spouts.fromIterable(Arrays.asList(10,400,2,-1))
        				.reverse().toList(), equalTo(asList(-1, 2, 400,10)));
    }
    @Test
    public void testReverseListLimit() {
    	
        assertThat( Spouts.fromIterable(Arrays.asList(10,400,2,-1)).limit(2)
        				.reverse().toList(), equalTo(asList(400, 10)));
    }
    @Test
    public void testReverseRange() {
    	
        assertThat( ReactiveSeq.range(0,10)
        				.reverse().toList(), equalTo(asList(10,9,8,7,6,5,4,3,2,1)));
    }
	@Test
	public void testCycleLong() {


		assertEquals(asList(1, 2, 1, 2, 1, 2), Streams.oneShotStream(Stream.of(1, 2)).cycle(3).toListX());
		assertEquals(asList(1, 2, 3, 1, 2, 3), Streams.oneShotStream(Stream.of(1, 2,3)).cycle(2).toListX());
	}
	@Test
	public void onEmptySwitchEmpty(){
	   for(int i=0;i<ITERATIONS;i++){
	       System.out.println("Iteration " + i);
            assertThat(of()
                            .onEmptySwitch(() -> of(1, 2, 3))
                            .toList(),
                    equalTo(Arrays.asList(1, 2, 3)));
        }

	}
    @Test
    public void flatMap(){
        for(int i=0;i<ITERATIONS;i++){
            System.out.println("Iteration " + i);
            assertThat(of(1)
                            .flatMap(in -> of(1, 2, 3))
                            .toList(),
                    equalTo(Arrays.asList(1, 2, 3)));
        }

    }
	@Test
	public void flatMapSynchronous(){
		for(int i=0;i<ITERATIONS;i++){
			System.out.println("***************************Iteration " + i);
            System.out.println("***************************Iteration " + i);
            System.out.println("***************************Iteration " + i);
            System.out.println("***************************Iteration " + i);
            System.out.println("***************************Iteration " + i);
            System.out.println("***************************Iteration " + i);
            System.out.println("***************************Iteration " + i);
            System.out.println("***************************Iteration " + i);

			assertThat(ReactiveSeq.fromPublisher(of(1)
							.flatMap(in -> of(1, 2, 3)))
							.toList(),
					equalTo(Arrays.asList(1, 2, 3)));
		}

	}
    @Test
    public void flatMapPSynchronous(){
        for(int i=0;i<ITERATIONS;i++){
            System.out.println("Iteration " + i);
            assertThat(ReactiveSeq.fromPublisher(of(1)
                            .flatMapP(in -> of(1, 2, 3)))
                            .toList(),
                    equalTo(Arrays.asList(1, 2, 3)));
        }

    }
    @Test
    public void flatMapForEach(){
        for(int i=0;i<ITERATIONS;i++){
            AtomicInteger count = new AtomicInteger(0);
            System.out.println("Iteration " + i);
            of(1)
                    .flatMap(in -> of(1, 2, 3))
                    .forEach(e->count.incrementAndGet());
            assertThat(count.get(),
                    equalTo(3));
        }

    }
    @Test
    public void onEmptySwitchGet(){
        for(int i=0;i<ITERATIONS;i++){
            System.out.println("Iteration " + i);
            assertThat(of()
                            .onEmptyGet(() -> 1)
                            .toList(),
                    equalTo(Arrays.asList(1)));
        }

    }
	private int sleep(Integer i) {
		try {
			Thread.currentThread().sleep(i);
		} catch (InterruptedException e) {

		}
		return i;
	}
	@Test
	public void skipTime(){
		List<Integer> result = of(1,2,3,4,5,6)
				.peek(i->sleep(i*100))
				.skip(1000,TimeUnit.MILLISECONDS)
				.toList();

        assertThat(result.size(), Matchers.isOneOf(3,4));
		assertThat(result,hasItems(4,5,6));
	}
	@Test
	public void limitTime(){
		List<Integer> result = of(1,2,3,4,5,6)
				.peek(i->sleep(i*100))
				.limit(1000, TimeUnit.MILLISECONDS)
				.toList();


		assertThat(result,equalTo(Arrays.asList(1,2,3)));
	}
    @Test
	public void skipUntil(){
		assertEquals(asList(3, 4, 5), of(1, 2, 3, 4, 5).skipUntil(i -> i % 3 == 0).toList());
	}
	@Test
    public void simpleZip(){
        of(1,2,3)
                .zip(of(100,200))
                .forEach(System.out::println);
    }
	@Test
	public void zip2of(){

		List<Tuple2<Integer,Integer>> list =of(1,2,3,4,5,6)
				                            .zip(of(100,200,300,400).stream())
				                            .toListX();

		System.out.println(list);

		List<Integer> right = list.stream().map(t -> t.v2).collect(Collectors.toList());
		assertThat(right,hasItem(100));
		assertThat(right,hasItem(200));
		assertThat(right,hasItem(300));
		assertThat(right,hasItem(400));

		List<Integer> left = list.stream().map(t -> t.v1).collect(Collectors.toList());
		System.out.println(left);
		assertThat(Arrays.asList(1,2,3,4,5,6),hasItem(left.get(0)));

	}
	@Test
	public void cast(){
        AtomicReference<Throwable> error = new AtomicReference<>(null);
        AtomicBoolean complete=  new AtomicBoolean(false);
		of(1,2,3).cast(String.class).forEach(System.out::println,e->error.set(e),()->complete.set(true));
		while(!complete.get()){
            LockSupport.parkNanos(100l);
        }
		System.out.println(error.get());
		assertTrue(error.get() instanceof ClassCastException);
	}
	@Test(expected=ClassCastException.class)
	public void castList(){

		of(1,2,3).cast(String.class).toList();

	}
    @Test
	public void dropRight(){
		System.out.println(of(1,2,3).skipLast(1).toList());
		assertThat(of(1,2,3).dropRight(1).toList(),hasItems(1,2));
	}
	@Test
	public void dropRight2(){
		assertThat(of(1,2,3).dropRight(2).toList(),hasItems(1));
	}
	@Test
	public void skipLast1(){
		assertThat(of(1,2,3).skipLast(1).toList(),hasItems(1,2));
	}
	@Test
	public void testSkipLast(){
		assertThat(of(1,2,3,4,5)
				.skipLast(2)
				.toListX(),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testSkipLastForEach(){
		List<Integer> list = new ArrayList();
		of(1,2,3,4,5).skipLast(2)
				.forEach(n->{list.add(n);});
		assertThat(list,equalTo(Arrays.asList(1,2,3)));
	}
    @Test
    public void testCycle() {

    	   assertEquals(asList(1, 1, 1, 1, 1,1),of(1).cycle().limit(6).toList());
      
    }
    
    @Test
    public void testIterable() {
        List<Integer> list = of(1, 2, 3).toCollection(LinkedList::new);

        for (Integer i :of(1, 2, 3)) {
            assertThat(list,hasItem(i));
        }
    }
	
	@Test
	public void testDuplicate(){
		 Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).duplicate();
		 assertTrue(copies.v1.anyMatch(i->i==2));
		 assertTrue(copies.v2.anyMatch(i->i==2));
	}
	@Test
	public void testTriplicate(){
		 Tuple3<ReactiveSeq<Integer>, ReactiveSeq<Integer>, ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).triplicate();
		 assertTrue(copies.v1.anyMatch(i->i==2));
		 assertTrue(copies.v2.anyMatch(i->i==2));
		 assertTrue(copies.v3.anyMatch(i->i==2));
	}
	
	@Test
	public void testQuadriplicate(){
		 Tuple4<ReactiveSeq<Integer>, ReactiveSeq<Integer>, ReactiveSeq<Integer>,ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).quadruplicate();
		 assertTrue(copies.v1.anyMatch(i->i==2));
		 assertTrue(copies.v2.anyMatch(i->i==2));
		 assertTrue(copies.v3.anyMatch(i->i==2));
		 assertTrue(copies.v4.anyMatch(i->i==2));
	}

	@Test
	public void testDuplicateFilter(){
		 Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).duplicate();
		 assertTrue(copies.v1.filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies.v2.filter(i->i%2==0).toList().size()==3);
	} 
	@Test
	public void testTriplicateFilter(){
		Tuple3<ReactiveSeq<Integer>, ReactiveSeq<Integer>, ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).triplicate();
		 assertTrue(copies.v1.filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies.v2.filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies.v3.filter(i->i%2==0).toList().size()==3);
	} 
	@Test
	public void testQuadriplicateFilter(){
		 Tuple4<ReactiveSeq<Integer>, ReactiveSeq<Integer>, ReactiveSeq<Integer>,ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).quadruplicate();
		 assertTrue(copies.v1.filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies.v2.filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies.v3.filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies.v4.filter(i->i%2==0).toList().size()==3);
	}
	@Test
	public void testDuplicateLimit(){
		 Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).duplicate();
		 assertTrue(copies.v1.limit(3).toList().size()==3);
		 assertTrue(copies.v2.limit(3).toList().size()==3);
	} 
	@Test
	public void testTriplicateLimit(){
		Tuple3<ReactiveSeq<Integer>, ReactiveSeq<Integer>, ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).triplicate();
		 assertTrue(copies.v1.limit(3).toList().size()==3);
		 assertTrue(copies.v2.limit(3).toList().size()==3);
		 assertTrue(copies.v3.limit(3).toList().size()==3);
	} 
	@Test
	public void testQuadriplicateLimit(){
		 Tuple4<ReactiveSeq<Integer>, ReactiveSeq<Integer>, ReactiveSeq<Integer>,ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).quadruplicate();
		 assertTrue(copies.v1.limit(3).toList().size()==3);
		 assertTrue(copies.v2.limit(3).toList().size()==3);
		 assertTrue(copies.v3.limit(3).toList().size()==3);
		 assertTrue(copies.v4.limit(3).toList().size()==3);
	}
	    @Test(expected=ClassCastException.class)
	    public void testCastException() {
	    	of(1, "a", 2, "b", 3, null)
	    			.peek(it ->System.out.println(it))
	    			.cast(Integer.class)
	    				.peek(it ->System.out.println(it)).toList();
	    		
	    }

	public void prepend(){
		List<String> result = 	of(1,2,3).prepend(100,200,300)
				.map(it ->it+"!!").collect(Collectors.toList());

		assertThat(result,equalTo(Arrays.asList("100!!","200!!","300!!","1!!","2!!","3!!")));
	}
	@Test
	public void append(){
		List<String> result = 	of(1,2,3).append(100,200,300)
				.map(it ->it+"!!").collect(Collectors.toList());

		assertThat(result,equalTo(Arrays.asList("1!!","2!!","3!!","100!!","200!!","300!!")));
	}
	@Test
	public void concatStreams(){
		List<String> result = 	of(1,2,3).appendS(of(100,200,300))
				.map(it ->it+"!!").collect(Collectors.toList());

		assertThat(result,equalTo(Arrays.asList("1!!","2!!","3!!","100!!","200!!","300!!")));
	}
	@Test
	public void shuffle(){

		assertEquals(3, of(1, 2, 3).shuffle().toListX().size());
	}
	@Test
	public void shuffleRandom(){
		Random r = new Random();
		assertEquals(3, of(1, 2, 3).shuffle(r).toListX().size());
	}


	@Test
	public void prependStreams(){
		List<String> result = 	of(1,2,3).prependS(of(100,200,300))
				.map(it ->it+"!!").collect(Collectors.toList());

		assertThat(result,equalTo(Arrays.asList("100!!","200!!","300!!","1!!","2!!","3!!")));
	}
	   

		
	    @Test
	    public void testGroupByEager() {
	        Map<Integer, ListX<Integer>> map1 =of(1, 2, 3, 4).groupBy(i -> i % 2);
	       
	        assertThat(map1.get(0),hasItem(2));
	        assertThat(map1.get(0),hasItem(4));
	        assertThat(map1.get(1),hasItem(1));
	        assertThat(map1.get(1),hasItem(3));
	        
	        assertEquals(2, map1.size());

	     
	    }
	    

	    @Test
	    public void testJoin() {
	        assertEquals("123".length(),of(1, 2, 3).join().length());
	        assertEquals("1, 2, 3".length(), of(1, 2, 3).join(", ").length());
	        assertEquals("^1|2|3$".length(), of(1, 2, 3).join("|", "^", "$").length());
	        
	      
	    }

	    
	   
	  

	   
	    @Test
	    public void testSkipWhile() {
	        Supplier<ReactiveSeq<Integer>> s = () -> of(1, 2, 3, 4, 5);

	        assertTrue(s.get().skipWhile(i -> false).toList().containsAll(asList(1, 2, 3, 4, 5)));
	      
	        assertEquals(asList(), s.get().skipWhile(i -> true).toList());
	    }

	    @Test
	    public void testSkipUntil() {
	        Supplier<ReactiveSeq<Integer>> s = () -> of(1, 2, 3, 4, 5);

	        assertEquals(asList(), s.get().skipUntil(i -> false).toList());
	        assertTrue(s.get().skipUntil(i -> true).toList().containsAll(asList(1, 2, 3, 4, 5)));
		  }

	    @Test(expected= NullPointerException.class)
	    public void testSkipUntilWithNulls() {
	        Supplier<ReactiveSeq<Integer>> s = () -> of(1, 2, null, 3, 4, 5);
	       
	        assertTrue(s.get().skipUntil(i -> true).toList().containsAll(asList(1, 2, null, 3, 4, 5)));
	    }

	    @Test
	    public void testLimitWhile() {
	        Supplier<ReactiveSeq<Integer>> s = () -> of(1, 2, 3, 4, 5);

	        assertEquals(asList(), s.get().limitWhile(i -> false).toList());
	        assertTrue( s.get().limitWhile(i -> i < 3).toList().size()!=5);       
	        assertTrue(s.get().limitWhile(i -> true).toList().containsAll(asList(1, 2, 3, 4, 5)));
	    }

	    @Test
	    public void testLimitUntil() {
	        

	        assertTrue(of(1, 2, 3, 4, 5).limitUntil(i -> false).toList().containsAll(asList(1, 2, 3, 4, 5)));
	        assertFalse(of(1, 2, 3, 4, 5).limitUntil(i -> i % 3 == 0).toList().size()==5);
	        
	        assertEquals(asList(), of(1, 2, 3, 4, 5).limitUntil(i -> true).toList());
	    }

	    @Test(expected = NullPointerException.class)
	    public void testLimitUntilWithNulls() {
	       
	    	System.out.println(of(1, 2, null, 3, 4, 5).limitUntil(i -> false).toList());
	        assertTrue(of(1, 2, null, 3, 4, 5).limitUntil(i -> false).toList().containsAll(asList(1, 2, null, 3, 4, 5)));
	    }

	    

	    @Test
	    public void testMinByMaxBy() {
	        Supplier<ReactiveSeq<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);

	        assertEquals(1, (int) s.get().maxBy(t -> Math.abs(t - 5)).get());
	        assertEquals(5, (int) s.get().minBy(t -> Math.abs(t - 5)).get());

	        assertEquals(6, (int) s.get().maxBy(t -> "" + t).get());
	        assertEquals(1, (int) s.get().minBy(t -> "" + t).get());
	    }

	   
	   

	    @Test
	    public void testFoldLeft() {
	    	for(int i=0;i<100;i++){
		        Supplier<ReactiveSeq<String>> s = () -> of("a", "b", "c");
	
		        assertTrue(s.get().reduce("", String::concat).contains("a"));
		        assertTrue(s.get().reduce("", String::concat).contains("b"));
		        assertTrue(s.get().reduce("", String::concat).contains("c"));
		       
		        assertEquals(3, (int) s.get().map(str->str.length()).foldLeft(0, (u, t) -> u + t));
	
		        
		        assertEquals(3, (int) s.get().map(str->str.length()).foldRight(0, (t, u) -> u + t));
	    	}
	    }
	    
	    @Test
	    public void testFoldRight(){
	    	 	Supplier<ReactiveSeq<String>> s = () -> of("a", "b", "c");

		        assertTrue(s.get().foldRight("", String::concat).contains("a"));
		        assertTrue(s.get().foldRight("", String::concat).contains("b"));
		        assertTrue(s.get().foldRight("", String::concat).contains("c"));
		        assertEquals(3, (int) s.get().map(str->str.length())
		        					.foldRight(0, (t, u) -> u + t));
	    }
	    
	   
	    //tests converted from lazy-seq suite
	    @Test
		public void flattenEmpty() throws Exception {
				assertTrue(this.<ReactiveSeq<Integer>>of().to(ReactiveSeq::flatten).toList().isEmpty());
		}

		@Test
		public void flatten() throws Exception {
			assertThat(of(Arrays.asList(1,2)).to(ReactiveSeq::flattenI).toList().size(),equalTo(asList(1,  2).size()));
		}

		

		@Test
		public void flattenEmptyStream() throws Exception {
			
			assertThat(this.<Integer>of(1,2,3,4,5,5,6,8,9,10).limit(10).collect(Collectors.toList()).size(),
											equalTo(asList(2, 3, 4, 5, 6, 7, 0, 0, 0, 0).size()));
		}
		
		
	
}