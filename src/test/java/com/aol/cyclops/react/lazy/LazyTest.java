package com.aol.cyclops.react.lazy;


import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import cyclops.stream.FutureStream;
import org.junit.Test;

import cyclops.Semigroups;
import cyclops.async.LazyReact;
import cyclops.collections.ListX;

public class LazyTest {

	@Test
	public void testParallel(){

		assertThat(new LazyReact().range(0,1000)
				           .parallel(s->s.map(i->i*2))
				.count(),equalTo(1000L));
	}
    @Test
    public void combineNoOrder(){
        assertThat(LazyReact.parallelCommonBuilder().of(1,2,3)
                   .combine((a, b)->a.equals(b),Semigroups.intSum)
                   .toListX(),equalTo(ListX.of(1,2,3))); 
                   
    }
    @Test
    public void combine(){
        
        assertThat(LazyReact.parallelCommonBuilder().of(1,2,3)
                   .combine((a, b)->true,Semigroups.intSum).toListX(),
                   equalTo(Arrays.asList(6))); 
                   
    }
	@Test
	public void onePerSecond() {

		long start = System.currentTimeMillis();
				FutureStream.iterate(0, it -> it + 1)
				.limit(3)
				.onePer(1, TimeUnit.SECONDS)
				.map(seconds -> "hello!")
				.peek(System.out::println)
				.toList();
				
	 assertTrue(System.currentTimeMillis()-start>1900);

	}
	@Test
	public void subStream(){
		List<Integer> list = FutureStream.of(1,2,3,4,5,6).subStream(1,3).toList();
		assertThat(list,equalTo(Arrays.asList(2,3)));
	}
	@Test
    public void emptyPermutations() {
        assertThat(FutureStream.of().permutations().map(s->s.toList()).toList(),equalTo(Arrays.asList()));
    }

    @Test
    public void permuations3() {
    	System.out.println(FutureStream.of(1, 2, 3).permutations().map(s->s.toList()).toList());
        assertThat(FutureStream.of(1, 2, 3).permutations().map(s->s.toList()).toList(),
        		equalTo(FutureStream.of(FutureStream.of(1, 2, 3),
        		FutureStream.of(1, 3, 2), FutureStream.of(2, 1, 3), FutureStream.of(2, 3, 1), FutureStream.of(3, 1, 2), FutureStream.of(3, 2, 1)).map(s->s.toList()).toList()));
    }
    
    @Test
    public void emptyAllCombinations() {
        assertThat(FutureStream.of().combinations().map(s->s.toList()).toList(),equalTo(Arrays.asList(Arrays.asList())));
    }

    @Test
    public void allCombinations3() {
        assertThat(FutureStream.of(1, 2, 3).combinations().map(s->s.toList()).toList(),equalTo(Arrays.asList(Arrays.asList(), Arrays.asList(1), Arrays.asList(2),
        		Arrays.asList(3), Arrays.asList(1, 2), Arrays.asList(1, 3), Arrays.asList(2, 3), Arrays.asList(1, 2, 3))));
    }

  

    @Test
    public void emptyCombinations() {
        assertThat(FutureStream.of().combinations(2).toList(),equalTo(Arrays.asList()));
    }

    @Test
    public void combinations2() {
        assertThat(FutureStream.of(1, 2, 3).combinations(2).map(s->s.toList()).toList(),
                equalTo(Arrays.asList(Arrays.asList(1, 2), Arrays.asList(1, 3), Arrays.asList(2, 3))));
    }
	@Test
	public void onEmptySwitchEmpty(){
		assertThat(FutureStream.of()
							.onEmptySwitch(()-> FutureStream.of(1,2,3))
							.toList(),
							equalTo(Arrays.asList(1,2,3)));
				
	}
	@Test
	public void onEmptySwitch(){
		assertThat(FutureStream.of(4,5,6)
							.onEmptySwitch(()-> FutureStream.of(1,2,3))
							.toList(),
							equalTo(Arrays.asList(4,5,6)));
				
	}
	
	@Test
	public void elapsedIsPositive(){
		
		
		assertTrue(FutureStream.of(1,2,3,4,5).elapsed().noneMatch(t->t.v2<0));
	}


	int slow(){
		try {
			Thread.sleep(150);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 3;
	}
	
	@Test
	public void convertToEager(){
		
		
		
		
		assertThat(LazyReact.parallelCommonBuilder()
						.ofAsync(()->slow(),()->1,()->2)
						.peek(System.out::println)
						.convertToSimpleReact()
						.allOf(list->list)
						.block().size(),is(1));
						
	}

	@Test
	public void convertToEagerAndBack(){
		
		
		
		
		assertThat(LazyReact.parallelCommonBuilder()
						.ofAsync(()->slow(),()->1,()->2)
						.peek(System.out::println)
						.zipWithIndex()
						.convertToSimpleReact()
						.peek(System.out::println)
						.convertToLazyStream()
						.map(it->slow())
						.peek(System.out::println)
						.block().size(),is(3));
						
	}
	
	@Test
	public void zipWithIndexApi(){
		LazyReact.parallelCommonBuilder()
		.ofAsync(() -> 2, () -> 1, () -> 2)
		
		.zipWithIndex()
		.peek(System.out::println)
		.map(it -> {
			if (it.v1 == 1) {
				sleep(1000);
				return -1;
			}
			return it.v1 + 100;
		})
		.peek(System.out::println)
		.forEach(System.out::println);
	}
	@Test 
	public void debounce() {
		System.out.println(LazyReact.sequentialCommonBuilder()
				.from(IntStream.range(0, 1000000))
				.limit(100)
				.debounce(100, TimeUnit.MILLISECONDS)
				.peek(System.out::println)
				.block().size());
	}

	@Test
	public void iterateTest(){
	    
    	    assertThat(new LazyReact().iterate(1, i->i+1)
                    .limit(5)
                    .peek(System.out::println)
                    .toListX().size(),equalTo(5));
	    
	}
	@Test
    public void iterateTest2(){
	    LazyReact react = new LazyReact(1,1);
        for(int x=0;x<5000;x++)
	    {
            assertThat( react
                            .iterate(1, i->i+1)
                            .limit(5)
                            .reduce(Semigroups.intSum).get(),equalTo(15));
        }
    }

	@Test
	public void generateTest(){
	   assertThat( new LazyReact().generate(()->"hello")
	                   .limit(5)
	                   .reduce(Semigroups.stringConcat).get(),equalTo("hellohellohellohellohello"));
	    
	}
	@Test
    public void generateAsyncTest(){
       assertThat( new LazyReact().generateAsync(()->"hello")
                       .limit(5)
                       .reduce(Semigroups.stringConcat).get(),equalTo("hellohellohellohellohello"));
        
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
	public void lazyReactStream() {
		assertThat(LazyReact.sequentialBuilder().ofAsync(() -> 1).map(list -> 1 + 2)
				.block(),equalTo(Arrays.asList(3)));
	}
	@Test
	public void lazyReactParAndConc() {
		assertThat(new LazyReact(2,2).ofAsync(() -> 1).map(list -> 1 + 2)
				.block(),equalTo(Arrays.asList(3)));
	}

	@Test
	public void lazyParallel() {
		assertThat(LazyReact.parallelBuilder().ofAsync(() -> 1).map(list -> 1 + 2)
				.block(),equalTo(Arrays.asList(3)));
	}

	@Test
	public void lazyReactStreamList() {
		assertThat(LazyReact.sequentialBuilder().react(asList(() -> 1))
				.map(list -> 1 + 2).block(),equalTo(Arrays.asList(3)));
	}

	@Test
	public void lazyParallelList() {
		assertThat(LazyReact.parallelBuilder().react(asList(() -> 1))
				.map(list -> 1 + 2).block(),equalTo(Arrays.asList(3)));
	}
}
