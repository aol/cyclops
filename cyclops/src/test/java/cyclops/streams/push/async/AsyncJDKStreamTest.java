package cyclops.streams.push.async;


import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;

public class AsyncJDKStreamTest {
	protected <U> ReactiveSeq<U> of(U... array){

		return Spouts.async(s->{
			Thread t = new Thread(()-> {
				for (U next : array) {
					s.onNext(next);
				}
				s.onComplete();
			});
			t.start();
		});
	}
    protected ReactiveSeq<Integer> range(int start, int end){

        return Spouts.async(s->{
            Thread t = new Thread(()-> {

                for (int i =start;i<end;i++) {
                    s.onNext(i);
                }
                s.onComplete();
            });
            t.start();
        });
    }
    protected <U> ReactiveSeq<U> rs(U... array){
        return Spouts.from(Flux.just(array).subscribeOn(Schedulers.fromExecutor(ForkJoinPool.commonPool())));

    }
    protected <U> Flux<U> flux(U... array){
        return Flux.just(array).subscribeOn(Schedulers.fromExecutor(ForkJoinPool.commonPool()));

    }
	@Test
	public void testDistinctReactiveSeq(){

		List<String> d = of("Java", "C").distinct(n -> n + ":" + n).toList();
		assertThat(d.size(),equalTo(2));
	}

	@Test
	public void testDistinctReactiveSeqMultipleDuplicates(){
		List<String> d = of("Java", "C", "Java", "Java","java", "java").distinct(n -> n + ":" + n).toList();
		System.out.println(d);
		assertThat(d.size(),equalTo(3));
	}
    @Test
    public void fluxConcatMap(){
        System.out.println(Flux.just(1,2,3).concatMap(i->Flux.just(i+100,200))
            .collect(Collectors.toList()).block());
    }
    @Test
    public void flatMapPub1(){
	    for(int l=0;l<1000;l++) {
		    System.out.println("************Iteration " + l);
		    System.out.println("************Iteration " + l);
		    System.out.println("************Iteration " + l);

		    assertThat(Arrays.asList(5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7).size(),
				    equalTo(this.rs(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
						    .mergeMap(i -> rs(i, i * 2, i * 4)
								    .mergeMap(x -> rs(5, 6, 7)))
						    .toList().size()));

	    }
    }
    @Test
    public void flatMapPub(){
        for(int l=0;l<100_000;l++) {
            System.out.println("************Iteration " + l);
            System.out.println("************Iteration " + l);
            System.out.println("************Iteration " + l);

           assertThat(this.rs(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                    .mergeMap(i -> rs(i, i * 2, i * 4)
                            .mergeMap(x -> rs(5, 6, 7)))
                    .toList(),equalTo(Arrays.asList(5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7)));

        }
    }
	@Test
	public void flatMapP(){
        for(int l=0;l<1_000;l++) {
            System.out.println("************Iteration " + l);
            System.out.println("************Iteration " + l);
            System.out.println("************Iteration " + l);
            System.out.println(this.rs(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                    .mergeMap(i -> rs(i, i * 2, i * 4)
                            .mergeMap(x -> rs(5, 6, 7)))
                    .toList());
        }
    }
    @Test
    public void flatMapP3(){
        System.out.println(this.rs(1,2)
                .mergeMap(i->rs(i,i*2,i*4)
                        .mergeMap(x->rs(5,6,7)
                        .mergeMap(y->rs(2,3,4))))
                .toList());

        assertThat(this.rs(1,2)
          .mergeMap(i->rs(i,i*2,i*4)
            .mergeMap(x->rs(5,6,7)
              .mergeMap(y->rs(2,3,4))))
          .toList(),equalTo(Arrays.asList(2, 3, 4, 2, 3, 4, 2, 3, 4, 2, 3, 4, 2, 3, 4,
          2, 3, 4, 2, 3, 4, 2, 3, 4, 2, 3, 4, 2, 3, 4, 2, 3, 4, 2, 3, 4, 2, 3, 4, 2, 3, 4, 2, 3, 4, 2, 3, 4, 2, 3, 4, 2, 3, 4)));
    }
    @Test
    public void flatMapP2(){
        for(int l=0;l<1_000;l++) {
            System.out.println("************Iteration " + l);
            System.out.println("************Iteration " + l);
            System.out.println("************Iteration " + l);
            System.out.println(this.rs("1", "2")
                    .mergeMap(i -> rs(1, 2,3))
                      .mergeMap(x -> rs('a','b'))
                    .toList());
        }
    }
    @Test
    public void flatMapP2a(){
        for(int l=0;l<1_000;l++) {
            System.out.println("************Iteration " + l);
            System.out.println("************Iteration " + l);
            System.out.println("************Iteration " + l);
            System.out.println(this.rs("1", "2","3")
                    .mergeMap(i -> rs(1, 2,3,4,5))
                    .mergeMap(x -> rs('a','b'))
                    .toList());
        }
    }
    @Test
    public void flatMapFlux(){
        System.out.println(this.flux(1,2,3,4,5,6,7,8,9,10)
                .flatMap(i->flux(i,i*2,i*4)
                        .flatMap(x->flux(5,6,7),2),2)
                .collect(Collectors.toList()).block());
    }

	@Test
	public void testAnyMatch(){
		assertThat(of(1,2,3,4,5).anyMatch(it-> it.equals(3)),equalTo(true));
	}
	@Test
	public void testAllMatch(){
		assertThat(of(1,2,3,4,5).allMatch(it-> it>0 && it <6),equalTo(true));
	}
	@Test
	public void testNoneMatch(){
		assertThat(of(1,2,3,4,5).noneMatch(it-> it==5000),equalTo(true));
	}


	@Test
	public void testAnyMatchFalse(){
		assertThat(of(1,2,3,4,5).anyMatch(it-> it.equals(8)),equalTo(false));
	}
	@Test
	public void testAnyMatchGrouping(){
		assertThat(of(1,2,3,4,5).grouped(2).flatMap(s->s.stream()).anyMatch(it-> it.equals(1)),equalTo(true));
	}
	@Test
	public void testAllMatchFalse(){
		assertThat(of(1,2,3,4,5).allMatch(it-> it<0 && it >6),equalTo(false));
	}
	@Test
	public void testFlatMap(){
		assertThat(of( asList("1","10"), asList("2"),asList("3"),asList("4")).flatMapStream( list -> list.stream() ).collect(Collectors.toList()
						),hasItem("10"));
	}
    @Test
    public void testFlatMap2(){

	     System.out.println(of( asList("1","10"), asList("2"),asList("3"),asList("4")).flatMap( list -> list.stream() ).collect(Collectors.toList()));

        assertThat(of( asList("1","10"), asList("2"),asList("3"),asList("4")).flatMap( list -> list.stream() ).collect(Collectors.toList()
        ),hasItem("10"));
    }

	@Test
	public void testMapReduce(){
		assertThat(of(1,2,3,4,5).map(it -> it*100).reduce( (acc,next) -> acc+next).get(),equalTo(1500));
	}
	@Test
	public void testMapReduceSeed(){
		assertThat(of(1,2,3,4,5).map(it -> it*100).reduce( 50,(acc,next) -> acc+next),equalTo(1550));
	}


	@Test
	public void testMapReduceCombiner(){
		assertThat(of(1,2,3,4,5).map(it -> it*100).reduce( 0,
                (acc, next) -> acc+next,
                Integer::sum),equalTo(1500));
	}
	@Test
	public void testFindFirst(){
		assertThat(Arrays.asList(1,2,3),hasItem(of(1,2,3,4,5).filter(it -> it <3).findFirst().get()));
	}
	@Test
	public void testFindAny(){
		assertThat(Arrays.asList(1,2,3),hasItem(of(1,2,3,4,5).filter(it -> it <3).findAny().get()));
	}
	@Test
	public void testDistinct(){
		assertThat(of(1,1,1,2,1).distinct().collect(Collectors.toList()).size(),equalTo(2));
		assertThat(of(1,1,1,2,1).distinct().collect(Collectors.toList()),hasItem(1));
		assertThat(of(1,1,1,2,1).distinct().collect(Collectors.toList()),hasItem(2));
	}

	@Test
	public void testLimit(){
		assertThat(of(1,2,3,4,5).limit(2).collect(Collectors.toList()).size(),equalTo(2));
	}
	@Test
	public void testSkip(){
		assertThat(of(1,2,3,4,5).skip(2).collect(Collectors.toList()).size(),equalTo(3));
	}
	@Test
    public void testTake(){
        assertThat(of(1,2,3,4,5).take(2).collect(Collectors.toList()).size(),equalTo(2));
    }
    @Test
    public void testDrop(){
        assertThat(of(1,2,3,4,5).drop(2).collect(Collectors.toList()).size(),equalTo(3));
    }
	@Test
	public void testMax(){
		assertThat(of(1,2,3,4,5).maximum((t1, t2) -> t1-t2).orElse(-100),equalTo(5));
	}
	@Test
	public void testMin(){
		assertThat(of(1,2,3,4,5).minimum((t1, t2) -> t1-t2).orElse(-100),equalTo(1));
	}

	@Test
	public void testMapToInt(){
		assertThat(of("1","2","3","4").mapToInt(it -> Integer.valueOf(it)).max().getAsInt(),equalTo(4));

	}

	@Test
	public void mapToLong() {
		assertThat(of("1","2","3","4").mapToLong(it -> Long.valueOf(it)).max().getAsLong(),equalTo(4l));
	}

	@Test
	public void mapToDouble() {
		assertThat(of("1","2","3","4").mapToDouble(it -> Double.valueOf(it)).max().getAsDouble(),equalTo(4d));
	}


	@Test
	public void flatMapToInt() {
		assertThat(of( asList("1","10"), asList("2"),asList("3"),asList("4"))
				.flatMapToInt(list ->list.stream()
						.mapToInt(Integer::valueOf)).max().getAsInt(),equalTo(10));
	}


	@Test
	public void flatMapToLong() {
		assertThat(of( asList("1","10"), asList("2"),asList("3"),asList("4"))
				.flatMapToLong(list ->list.stream().mapToLong(Long::valueOf)).max().getAsLong(),equalTo(10l));

	}


	@Test
	public void flatMapToDouble(){

		assertThat(of( asList("1","10"),
				asList("2"),asList("3"),asList("4"))
				.flatMapToDouble(list ->list.stream()
						.mapToDouble(Double::valueOf))
						.max().getAsDouble(),equalTo(10d));
	}

	@Test
	public void sorted() {
		assertThat(of(1,5,3,4,2).sorted().collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3,4,5)));
	}
	@Test
	public void sortedComparator() {
		assertThat(of(1,5,3,4,2).sorted((t1,t2) -> t2-t1).collect(Collectors.toList()),equalTo(Arrays.asList(5,4,3,2,1)));
	}
	@Test
	public void forEach() throws InterruptedException {
		List<Integer> list = new ArrayList<>();
		of(1,5,3,4,2).forEach(it-> list.add(it));
		Thread.sleep(500l);
		assertThat(list,hasItem(1));
		assertThat(list,hasItem(2));
		assertThat(list,hasItem(3));
		assertThat(list,hasItem(4));
		assertThat(list,hasItem(5));

	}
	@Test
	public void forEachOrderedx() {
		List<Integer> list = new ArrayList<>();
		of(1,5,3,4,2).forEachOrdered(it-> list.add(it));
		assertThat(list,hasItem(1));
		assertThat(list,hasItem(2));
		assertThat(list,hasItem(3));
		assertThat(list,hasItem(4));
		assertThat(list,hasItem(5));

	}

	@Test
	public void testToArray() {
		assertThat( Arrays.asList(1,2,3,4,5),hasItem(of(1,5,3,4,2).toArray()[0]));
	}
	@Test
	public void testToArrayGenerator() {
		assertThat( Arrays.asList(1,2,3,4,5),hasItem(of(1,5,3,4,2).toArray(it->new Integer[it])[0]));
	}

	@Test
	public void testCount(){
		assertThat(of(1,5,3,4,2).count(),equalTo(5L));
	}

	@Test
	public void collectSBB(){

		List<Integer> list = of(1,2,3,4,5).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
		assertThat(list.size(),equalTo(5));
	}
	@Test
	public void collect(){
		assertThat(of(1,2,3,4,5).collect(Collectors.toList()).size(),equalTo(5));
		assertThat(of(1,1,1,2).collect(Collectors.toSet()).size(),equalTo(2));
	}
	@Test
	public void testFilter(){
		assertThat(of(1,1,1,2).filter(it -> it==1).collect(Collectors.toList()).size(),equalTo(3));
	}
	@Test
	public void testMap(){
		assertThat(of(1).map(it->it+100).collect(Collectors.toList()).get(0),equalTo(101));
	}
	Object val;
	@Test
	public void testPeek(){
		val = null;
		of(1).map(it->it+100).peek(it -> val=it).collect(Collectors.toList());
		assertThat(val,equalTo(101));
	}


    @Test
    public void rangeMaxConcurrencyM32() {
        List<Integer> list =range(0, 1_000_000).mergeMap(32,Flux::just).toList();

        assertThat(list.size(), CoreMatchers.equalTo(1_000_000));
    }
    @Test
    public void rangeMaxConcurrencyM64() {
        List<Integer> list = range(0, 1_000_000).mergeMap(64,Flux::just).toList();

        assertThat(list.size(), CoreMatchers.equalTo(1_000_000));
    }
}
