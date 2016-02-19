package com.aol.cyclops.react.lazy;


import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.Test;

import com.aol.cyclops.control.LazyReact;
import com.aol.cyclops.types.futurestream.LazyFutureStream;

public class LazyTest {
	@Test
	public void onePerSecond() {

		long start = System.currentTimeMillis();
				LazyFutureStream.iterate(0, it -> it + 1)
				.limit(3)
				.onePer(1, TimeUnit.SECONDS)
				.map(seconds -> "hello!")
				.peek(System.out::println)
				.toList();
				
	 assertTrue(System.currentTimeMillis()-start>1900);

	}
	@Test
	public void subStream(){
		List<Integer> list = LazyFutureStream.of(1,2,3,4,5,6).subStream(1,3).toList();
		assertThat(list,equalTo(Arrays.asList(2,3)));
	}
	@Test
    public void emptyPermutations() {
        assertThat(LazyFutureStream.of().permutations().map(s->s.toList()).toList(),equalTo(Arrays.asList()));
    }

    @Test
    public void permuations3() {
    	System.out.println(LazyFutureStream.of(1, 2, 3).permutations().map(s->s.toList()).toList());
        assertThat(LazyFutureStream.of(1, 2, 3).permutations().map(s->s.toList()).toList(),
        		equalTo(LazyFutureStream.of(LazyFutureStream.of(1, 2, 3),
        		LazyFutureStream.of(1, 3, 2), LazyFutureStream.of(2, 1, 3), LazyFutureStream.of(2, 3, 1), LazyFutureStream.of(3, 1, 2), LazyFutureStream.of(3, 2, 1)).map(s->s.toList()).toList()));
    }
    
    @Test
    public void emptyAllCombinations() {
        assertThat(LazyFutureStream.of().combinations().map(s->s.toList()).toList(),equalTo(Arrays.asList(Arrays.asList())));
    }

    @Test
    public void allCombinations3() {
        assertThat(LazyFutureStream.of(1, 2, 3).combinations().map(s->s.toList()).toList(),equalTo(Arrays.asList(Arrays.asList(), Arrays.asList(1), Arrays.asList(2),
        		Arrays.asList(3), Arrays.asList(1, 2), Arrays.asList(1, 3), Arrays.asList(2, 3), Arrays.asList(1, 2, 3))));
    }

  

    @Test
    public void emptyCombinations() {
        assertThat(LazyFutureStream.of().combinations(2).toList(),equalTo(Arrays.asList()));
    }

    @Test
    public void combinations2() {
        assertThat(LazyFutureStream.of(1, 2, 3).combinations(2).map(s->s.toList()).toList(),
                equalTo(Arrays.asList(Arrays.asList(1, 2), Arrays.asList(1, 3), Arrays.asList(2, 3))));
    }
	@Test
	public void onEmptySwitchEmpty(){
		assertThat(LazyFutureStream.of()
							.onEmptySwitch(()->LazyFutureStream.of(1,2,3))
							.toList(),
							equalTo(Arrays.asList(1,2,3)));
				
	}
	@Test
	public void onEmptySwitch(){
		assertThat(LazyFutureStream.of(4,5,6)
							.onEmptySwitch(()->LazyFutureStream.of(1,2,3))
							.toList(),
							equalTo(Arrays.asList(4,5,6)));
				
	}
	
	@Test
	public void elapsedIsPositive(){
		
		
		assertTrue(LazyFutureStream.of(1,2,3,4,5).elapsed().noneMatch(t->t.v2<0));
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
						.react(()->slow(),()->1,()->2)
						.peek(System.out::println)
						.convertToSimpleReact()
						.allOf(list->list)
						.block().size(),is(1));
						
	}

	@Test
	public void convertToEagerAndBack(){
		
		
		
		
		assertThat(LazyReact.parallelCommonBuilder()
						.react(()->slow(),()->1,()->2)
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
		.react(() -> 2, () -> 1, () -> 2)
		
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
		assertThat(LazyReact.sequentialBuilder().react(() -> 1).map(list -> 1 + 2)
				.block(),equalTo(Arrays.asList(3)));
	}
	@Test
	public void lazyReactParAndConc() {
		assertThat(new LazyReact(2,2).react(() -> 1).map(list -> 1 + 2)
				.block(),equalTo(Arrays.asList(3)));
	}

	@Test
	public void lazyParallel() {
		assertThat(LazyReact.parallelBuilder().react(() -> 1).map(list -> 1 + 2)
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
