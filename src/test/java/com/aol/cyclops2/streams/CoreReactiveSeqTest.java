package com.aol.cyclops2.streams;

import static cyclops.stream.ReactiveSeq.of;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cyclops.companion.Streams;
import cyclops.async.QueueFactories;
import cyclops.async.adapters.Topic;
import cyclops.collections.mutable.ListX;
import org.hamcrest.CoreMatchers;
import cyclops.collections.tuple.Tuple;
import cyclops.collections.tuple.Tuple2;
import cyclops.collections.tuple.Tuple3;
import cyclops.collections.tuple.Tuple4;
import org.junit.Before;
import org.junit.Test;

import cyclops.function.Monoid;
import cyclops.async.LazyReact;
import cyclops.control.lazy.Maybe;
import cyclops.stream.ReactiveSeq;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;


//see BaseSequentialSeqTest for in order tests
public  class CoreReactiveSeqTest {
    public static Executor ex =  Executors.newFixedThreadPool(10);
    public static final LazyReact r = new LazyReact(10,10);
	
	ReactiveSeq<Integer> empty;
	ReactiveSeq<Integer> nonEmpty;

	@Before
	public void setup(){
		empty = of();
		nonEmpty = of(1);
	}
    @Test
    public void enqueued(){
        assertThat(ReactiveSeq.enqueued(sub->{
            sub.onNext(1);
            sub.onNext(2);
            sub.onComplete();
        }).toList(), CoreMatchers.equalTo(ListX.of(1,2)));
    }
    @Test
    public void subscribeErrorOnComplete(){
        List<Integer> result = new ArrayList<>();
        AtomicBoolean onComplete = new AtomicBoolean(false);
        Subscription s= of(1,2,3).forEachSubscribe(i->result.add(i), e->e.printStackTrace(),()->onComplete.set(true));

        assertThat(onComplete.get(),equalTo(false));
        s.request(1l);
        assertThat(result.size(),equalTo(1));
        assertThat(onComplete.get(),equalTo(false));
        s.request(1l);
        assertThat(result.size(),equalTo(2));
        assertThat(onComplete.get(),equalTo(false));
        s.request(1l);
        assertThat(result.size(),equalTo(3));
        assertThat(result,hasItems(1,2,3));
        s.request(1l);
        assertThat(onComplete.get(),equalTo(true));
    }

	@Test
    public void publishToAndMerge(){
	    cyclops.async.adapters.Queue<Integer> queue = QueueFactories.<Integer>boundedNonBlockingQueue(10)
                                            .build();

        Thread t=  new Thread( ()-> {

            while(true) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                    System.out.println("Closing!");
                    queue.close();

            }
        });
        t.start();
	    assertThat(ReactiveSeq.of(1,2,3)
                             .publishTo(queue)
                             .peek(System.out::println)
                             .merge(queue)
                             .toListX(),equalTo(ListX.of(1,1,2,2,3,3)));
    }

    @Test
    public void parallelFanOut(){
        assertThat(ReactiveSeq.of(1,2,3,4)
                .parallelFanOut(ForkJoinPool.commonPool(), s1->s1.filter(i->i%2==0).map(i->i*2),
                        s2->s2.filter(i->i%2!=0).map(i->i*100))
                .toListX(),equalTo(ListX.of(4,100,8,300)));

        assertThat(ReactiveSeq.of(1,2,3,4)
                .parallelFanOutZipIn(ForkJoinPool.commonPool(), s1->s1.filter(i->i%2==0).map(i->i*2),
                        s2->s2.filter(i->i%2!=0).map(i->i*100),(a,b)->a+b)
                .toListX(),equalTo(ListX.of(104,308)));
    }
    @Test
    public void mergePTest(){
        ListX<Integer> list = ReactiveSeq.of(3,6,9).mergeP(ReactiveSeq.of(2,4,8),ReactiveSeq.of(1,5,7)).toListX();
        assertThat(list,hasItems(1,2,3,4,5,6,7,8,9));
        assertThat(list.size(),equalTo(9));
    }
    @Test
    public void duplicateTest(){
        Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> tuples = ReactiveSeq.of(1, 2, 3, 4, 5, 6, 7, 8, 9).duplicate();

        Tuple2<Iterator<Integer>, Iterator<Integer>> its = tuples.map1(s -> s.iterator())
                .map2(s -> s.iterator());

        List<Integer> result = new ArrayList<>();
        while(its._1().hasNext() || its._2().hasNext() ){
            if(its._1().hasNext()){
                result.add(its._1().next());
            }
            if(its._2().hasNext()){
                result.add(its._2().next());
            }

        }

        System.out.println(result);

        assertThat(result,hasItems(1,2,3,4,5,6,7,8,9));
        assertThat(result.size(),equalTo(18));
    }
    @Test
    public void duplicatePropertiesTest(){
        for(int i=0;i<100;i++) {
            Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> tuples = ReactiveSeq.range(0,i).duplicate();

            Tuple2<Iterator<Integer>, Iterator<Integer>> its = tuples.map1(s -> s.iterator())
                    .map2(s -> s.iterator());

            List<Integer> result = new ArrayList<>();
            while (its._1().hasNext() || its._2().hasNext()) {
                if (its._1().hasNext()) {
                    result.add(its._1().next());
                }
                if (its._2().hasNext()) {
                    result.add(its._2().next());
                }

            }

            System.out.println(result);
            for(int x=0;x<i;x++) {
                assertThat(result, hasItem(x));

            }
            assertThat(result.size(), equalTo(i*2));
        }
    }
    @Test
    public void bufferingCopierTest(){
        for(int i=0;i<10;i++) {
            for(int k=1;k<5;k++) {
                System.out.println (" Length : " + i + " - copies " + k);
                ListX<Iterable<Integer>> list = Streams.toBufferingCopier(ListX.range(0, i), k);
                ListX<Integer> result = list.map(it -> ReactiveSeq.fromIterable(it))
                        .flatMapS(s -> s);

                for (int x = 0; x < i; x++) {
                    assertThat("Failed on " + i + " and " + k,result, hasItem(x));

                }
                assertThat("Failed on " + i + " and " + k,result.size(), equalTo(i * k));
            }

        }
    }
    @Test
    public void bufferingCopierTriplicateCompare(){


        ListX<Iterable<Integer>> list = Streams.toBufferingCopier(ReactiveSeq.of(0, 1), 3);
        Tuple3<Iterator<Integer>, Iterator<Integer>, Iterator<Integer>> its = Tuple.tuple(list.get(0).iterator(),
                list.get(1).iterator(),
                list.get(2).iterator());

        List<Integer> result = new ArrayList<>();
        while (its._1().hasNext() || its._2().hasNext() || its._3().hasNext()) {
            if (its._1().hasNext()) {
                result.add(its._1().next() +100);
            }
            if (its._2().hasNext()) {
                result.add(its._2().next()+200);
            }
            if (its._3().hasNext()) {
                result.add(its._3().next()+300);
            }
        }

        System.out.println(result);

        assertThat(result.size(), equalTo(2 * 3));



    }
    @Test
    public void triplicateBug(){
        Tuple3<ReactiveSeq<Integer>, ReactiveSeq<Integer>,ReactiveSeq<Integer>> tuples = ReactiveSeq.of(0,1).triplicate();

        Tuple3<Iterator<Integer>, Iterator<Integer>, Iterator<Integer>> its = tuples.map1(s -> s.iterator())
                .map2(s -> s.iterator())
                .map3(s -> s.iterator());

        List<Integer> result = new ArrayList<>();
        while(its._1().hasNext() || its._2().hasNext() || its._3().hasNext()){
            if(its._1().hasNext()){
                result.add(its._1().next());
            }
            if(its._2().hasNext()){
                result.add(its._2().next());
            }
            if(its._3().hasNext()){
                result.add(its._3().next());
            }
        }

        System.out.println(result);
        for(int x=0;x<2;x++) {
            assertThat(result, hasItem(x));

        }
        assertThat(result.size(), equalTo(2*3));
    }
    @Test
    public void triplicatePropertiesTest(){
        for(int i=0;i<100;i++) {
            Tuple3<ReactiveSeq<Integer>, ReactiveSeq<Integer>,ReactiveSeq<Integer>> tuples = ReactiveSeq.range(0,i).triplicate();

            Tuple3<Iterator<Integer>, Iterator<Integer>, Iterator<Integer>> its = tuples.map1(s -> s.iterator())
                    .map2(s -> s.iterator())
                    .map3(s -> s.iterator());

            List<Integer> result = new ArrayList<>();
            while(its._1().hasNext() || its._2().hasNext() || its._3().hasNext()){
                if(its._1().hasNext()){
                    result.add(its._1().next());
                }
                if(its._2().hasNext()){
                    result.add(its._2().next());
                }
                if(its._3().hasNext()){
                    result.add(its._3().next());
                }
            }

            System.out.println(result);
            for(int x=0;x<i;x++) {
                assertThat(result, hasItem(x));

            }
            assertThat(result.size(), equalTo(i*3));
        }
    }
    @Test
    public void triplicateTest(){
        Tuple3<ReactiveSeq<Integer>, ReactiveSeq<Integer>, ReactiveSeq<Integer>> tuples = ReactiveSeq.of(1, 2, 3, 4, 5, 6, 7, 8, 9).triplicate();

        Tuple3<Iterator<Integer>, Iterator<Integer>, Iterator<Integer>> its = tuples.map1(s -> s.iterator())
                .map2(s -> s.iterator())
                .map3(s -> s.iterator());

        List<Integer> result = new ArrayList<>();
        while(its._1().hasNext() || its._2().hasNext() || its._3().hasNext()){
            if(its._1().hasNext()){
                result.add(its._1().next());
            }
            if(its._2().hasNext()){
                result.add(its._2().next());
            }
            if(its._3().hasNext()){
                result.add(its._3().next());
            }
        }

        System.out.println(result);

        assertThat(result,hasItems(1,2,3,4,5,6,7,8,9));
        assertThat(result.size(),equalTo(27));
    }
    @Test
    public void triplicateFanOut(){


        assertThat(ReactiveSeq.of(1,2,3,4,5,6,7,8,9)
                .fanOut(s1->s1.peek(System.out::println).filter(i->i%3==0).map(i->i*2),
                        s2->s2.filter(i->i%3==1).map(i->i*100),
                        s3->s3.filter(i->i%3==2).map(i->i*1000))
                .toListX(),equalTo(ListX.of(6, 100, 2000, 12, 400, 5000, 18, 700, 8000)));

    }
    @Test
    public void fanOut(){

        assertThat(ReactiveSeq.of(1,2,3,4)
                   .fanOut(s1->s1.filter(i->i%2==0).map(i->i*2),
                           s2->s2.filter(i->i%2!=0).map(i->i*100))
                   .toListX(),equalTo(ListX.of(4,100,8,300)));
        assertThat(ReactiveSeq.of(1,2,3,4,5,6,7,8,9)
                .fanOut(s1->s1.filter(i->i%3==0).map(i->i*2),
                        s2->s2.filter(i->i%3==1).map(i->i*100),
                        s3->s3.filter(i->i%3==2).map(i->i*1000))
                .toListX(),equalTo(ListX.of(6, 100, 2000, 12, 400, 5000, 18, 700, 8000)));
        assertThat(ReactiveSeq.of(1,2,3,4,5,6,7,8,9,10,11,12)
                             .fanOut(s1->s1.filter(i->i%4==0).map(i->i*2),
                                     s2->s2.filter(i->i%4==1).map(i->i*100),
                                     s3->s3.filter(i->i%4==2).map(i->i*1000),
                                     s4->s4.filter(i->i%4==3).map(i->i*10000))
                .toListX(),equalTo(ListX.of(8, 100, 2000, 30000, 16, 500, 6000, 70000, 24, 900, 10000, 110000)));
    }
    @Test
    public void parallelFanOut2(){

        assertThat(ReactiveSeq.of(1,2,3,4)
                .parallelFanOut(ForkJoinPool.commonPool(),s1->s1.filter(i->i%2==0).map(i->i*2),
                        s2->s2.filter(i->i%2!=0).map(i->i*100))
                .toListX(),equalTo(ListX.of(4,100,8,300)));
        assertThat(ReactiveSeq.of(1,2,3,4,5,6,7,8,9)
                .parallelFanOut(ForkJoinPool.commonPool(),s1->s1.filter(i->i%3==0).map(i->i*2),
                        s2->s2.filter(i->i%3==1).map(i->i*100),
                        s3->s3.filter(i->i%3==2).map(i->i*1000))
                .toListX(),equalTo(ListX.of(6, 100, 2000, 12, 400, 5000, 18, 700, 8000)));
        assertThat(ReactiveSeq.of(1,2,3,4,5,6,7,8,9,10,11,12)
                .parallelFanOut(ForkJoinPool.commonPool(),s1->s1.filter(i->i%4==0).map(i->i*2),
                        s2->s2.filter(i->i%4==1).map(i->i*100),
                        s3->s3.filter(i->i%4==2).map(i->i*1000),
                        s4->s4.filter(i->i%4==3).map(i->i*10000))
                .toListX(),equalTo(ListX.of(8, 100, 2000, 30000, 16, 500, 6000, 70000, 24, 900, 10000, 110000)));
    }
    @Test
    public void iteratePred(){

        assertThat(ReactiveSeq.iterate(0,i->i<10,i->i+1)
                    .toListX().size(),equalTo(10));
    }
	@Test
    public void broadcastTest(){
	    Topic<Integer> topic = ReactiveSeq.of(1,2,3)
                                          .broadcast();


        ReactiveSeq<Integer> stream1 = topic.stream();
        ReactiveSeq<Integer> stream2 = topic.stream();
	    assertThat(stream1.toListX(),equalTo(ListX.of(1,2,3)));
        assertThat(stream2.stream().toListX(),equalTo(ListX.of(1,2,3)));

    }
    @Test
    public void broadcastThreads() throws InterruptedException {
        Topic<Integer> topic = ReactiveSeq.range(0,100_000)
                                          .broadcast();

       Thread t=  new Thread( ()-> {
            ReactiveSeq<Integer> stream2 = topic.stream();
            assertThat(stream2.takeRight(1).singleUnsafe(), equalTo(99_999));
        });
       t.start();

        ReactiveSeq<Integer> stream1 = topic.stream();

        assertThat(stream1.takeRight(1).singleUnsafe(),equalTo(99_999));

       t.join();
    }



    @Test
	public void ambTest(){
        for(int i=0;i<10;i++) {
            assertThat(ReactiveSeq.of(1, 2, 3).ambWith(Flux.just(10, 20, 30)).toListX(), isOneOf(ListX.of(1, 2, 3), ListX.of(10, 20, 30)));
        }
	}
	

    @Test
    public void flatMapPublisher() throws InterruptedException{

        assertThat(of(1,2,3)
                        .flatMapP(i->Maybe.of(i))
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
		Iterator<Integer> it = ReactiveSeq.of(1).stream().cycle(2).iterator();
		List<Integer> list2 = new ArrayList<>();
		while(it.hasNext())
			list2.add(it.next());
		assertThat(list2,equalTo(ListX.of(1,1)));
	}
	@Test
	public void cycleIterate2(){
		Iterator<Integer> it = ReactiveSeq.of(1,2).stream().cycle(2).iterator();
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
    	
        assertThat( ReactiveSeq.fromList(Arrays.asList(10,400,2,-1))
        				.reverse().toList(), equalTo(asList(-1, 2, 400,10)));
    }
    @Test
    public void testReverseListLimit() {
    	
        assertThat( ReactiveSeq.fromList(Arrays.asList(10,400,2,-1)).limit(2)
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
		assertThat(of()
						.onEmptySwitch(()->of(1,2,3))
						.toList(),
				equalTo(Arrays.asList(1,2,3)));

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


		assertThat(result,equalTo(Arrays.asList(4,5,6)));
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
	public void zip2of(){

		List<Tuple2<Integer,Integer>> list =of(1,2,3,4,5,6)
				.zip(of(100,200,300,400).stream())
				.toListX();


		List<Integer> right = list.stream().map(t -> t._2()).collect(Collectors.toList());
		assertThat(right,hasItem(100));
		assertThat(right,hasItem(200));
		assertThat(right,hasItem(300));
		assertThat(right,hasItem(400));

		List<Integer> left = list.stream().map(t -> t._1()).collect(Collectors.toList());
		System.out.println(left);
		assertThat(Arrays.asList(1,2,3,4,5,6),hasItem(left.get(0)));

	}
	@Test(expected=ClassCastException.class)
	public void cast(){
		assertThat(of(1,2,3).cast(String.class).toList(),equalTo(ListX.empty()));
        of(1,2,3).cast(String.class).printOut();
	}
	@Test(expected=ClassCastException.class)
	public void castList(){
		of(1,2,3).cast(String.class).toList();

	}
    @Test
	public void dropRight(){
		assertThat(of(1,2,3).dropRight(1).toList(),hasItems(1,2));
	}
	@Test
	public void skipLast1(){
		assertThat(of(1,2,3).skipLast(1).toList(),hasItems(1,2));
	}
	@Test
	public void testSkipLast(){
		assertThat(ReactiveSeq.of(1,2,3,4,5)
				.skipLast(2)
				.toListX(),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testSkipLastForEach(){
		List<Integer> list = new ArrayList();
		ReactiveSeq.of(1,2,3,4,5).skipLast(2)
				.forEach(n->{list.add(n);});
		assertThat(list,equalTo(Arrays.asList(1,2,3)));
	}
    @Test
    public void testCycle() {

    	   assertEquals(asList(1, 1, 1, 1, 1,1),of(1).cycle().limit(6).toList());
      
    }
    
    @Test
    public void testIterable() {
        List<Integer> list = of(1, 2, 3).to().collection(LinkedList::new);

        for (Integer i :of(1, 2, 3)) {
            assertThat(list,hasItem(i));
        }
    }
	
	@Test
	public void testDuplicate(){
		 Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).duplicate();
		 assertTrue(copies._1().anyMatch(i->i==2));
		 assertTrue(copies._2().anyMatch(i->i==2));
	}
	@Test
	public void testTriplicate(){
		 Tuple3<ReactiveSeq<Integer>, ReactiveSeq<Integer>, ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).triplicate();
		 assertTrue(copies._1().anyMatch(i->i==2));
		 assertTrue(copies._2().anyMatch(i->i==2));
		 assertTrue(copies._3().anyMatch(i->i==2));
	}
	
	@Test
	public void testQuadriplicate(){
		 Tuple4<ReactiveSeq<Integer>, ReactiveSeq<Integer>, ReactiveSeq<Integer>,ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).quadruplicate();
		 assertTrue(copies._1().anyMatch(i->i==2));
		 assertTrue(copies._2().anyMatch(i->i==2));
		 assertTrue(copies._3().anyMatch(i->i==2));
		 assertTrue(copies._4().anyMatch(i->i==2));
	}

	@Test
	public void testDuplicateFilter(){
		 Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).duplicate();
		 assertTrue(copies._1().filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies._2().filter(i->i%2==0).toList().size()==3);
	} 
	@Test
	public void testTriplicateFilter(){
		Tuple3<ReactiveSeq<Integer>, ReactiveSeq<Integer>, ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).triplicate();
		 assertTrue(copies._1().filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies._2().filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies._3().filter(i->i%2==0).toList().size()==3);
	} 
	@Test
	public void testQuadriplicateFilter(){
		 Tuple4<ReactiveSeq<Integer>, ReactiveSeq<Integer>, ReactiveSeq<Integer>,ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).quadruplicate();
		 assertTrue(copies._1().filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies._2().filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies._3().filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies._4().filter(i->i%2==0).toList().size()==3);
	}
	@Test
	public void testDuplicateLimit(){
		 Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).duplicate();
		 assertTrue(copies._1().limit(3).toList().size()==3);
		 assertTrue(copies._2().limit(3).toList().size()==3);
	} 
	@Test
	public void testTriplicateLimit(){
		Tuple3<ReactiveSeq<Integer>, ReactiveSeq<Integer>, ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).triplicate();
		 assertTrue(copies._1().limit(3).toList().size()==3);
		 assertTrue(copies._2().limit(3).toList().size()==3);
		 assertTrue(copies._3().limit(3).toList().size()==3);
	} 
	@Test
	public void testQuadriplicateLimit(){
		 Tuple4<ReactiveSeq<Integer>, ReactiveSeq<Integer>, ReactiveSeq<Integer>,ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).quadruplicate();
		 assertTrue(copies._1().limit(3).toList().size()==3);
		 assertTrue(copies._2().limit(3).toList().size()==3);
		 assertTrue(copies._3().limit(3).toList().size()==3);
		 assertTrue(copies._4().limit(3).toList().size()==3);
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

		assertEquals(3, ReactiveSeq.of(1, 2, 3).shuffle().toListX().size());
	}
	@Test
	public void shuffleRandom(){
		Random r = new Random();
		assertEquals(3, ReactiveSeq.of(1, 2, 3).shuffle(r).toListX().size());
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

	    @Test
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

	    @Test
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
				assertTrue(ReactiveSeq.<ReactiveSeq<Integer>>of().to(ReactiveSeq::flatten).toList().isEmpty());
		}

		@Test
		public void flatten() throws Exception {
			assertThat(ReactiveSeq.of(Arrays.asList(1,2)).to(ReactiveSeq::flattenI).toList().size(),equalTo(asList(1,  2).size()));
		}

		

		@Test
		public void flattenEmptyStream() throws Exception {
			
			assertThat(ReactiveSeq.<Integer>of(1,2,3,4,5,5,6,8,9,10).limit(10).collect(Collectors.toList()).size(),
											equalTo(asList(2, 3, 4, 5, 6, 7, 0, 0, 0, 0).size()));
		}
		
		
	
}