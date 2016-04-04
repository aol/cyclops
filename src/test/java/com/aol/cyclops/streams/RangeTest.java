package com.aol.cyclops.streams;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import org.junit.Test;

import com.aol.cyclops.control.ReactiveSeq;

public class RangeTest {
    
    @Test
    public void reversedRange(){
       assertThat(ReactiveSeq.range(10, -10).count(),equalTo(20L));
    }
    @Test
    public void reversedRangeWithReverse(){
       assertThat(ReactiveSeq.range(10, -10).reverse().count(),equalTo(20L));
    }
    @Test
    public void reversedRangeLong(){
       assertThat(ReactiveSeq.rangeLong(10, -10).count(),equalTo(20L));
    }
    @Test
    public void reversedRangeLongWithReverse(){
       assertThat(ReactiveSeq.rangeLong(10, -10).reverse().count(),equalTo(20L));
    }
    @Test
    public void intStreamCompare0(){
       
        assertThat(IntStream.range(0,10).sum(),
                equalTo(ReactiveSeq.range(0,10).sum().get()));
    }
    @Test
    public void longStreamCompare0(){
        assertThat(LongStream.range(0,10).sum(),
                equalTo(ReactiveSeq.rangeLong(0,10).sum().get()));
    }
    @Test
    public void intStreamCompareReversed(){
        assertThat(IntStream.of(5,4,3,2,1,0,-1,-2,-3,-4,-5).sum(),
                equalTo(ReactiveSeq.range(-5,6).reverse().sum().get()));
    }
    @Test
    public void longStreamCompareReversed(){
        assertThat(LongStream.of(5,4,3,2,1,0,-1,-2,-3,-4,-5).sum(),
                equalTo(ReactiveSeq.rangeLong(-5,6).reverse().sum().get()));
    }
    @Test
    public void intStreamCompare(){
        assertThat(IntStream.range(-1,10).sum(),
                equalTo(ReactiveSeq.range(-1,10).sum().get()));
    }
    @Test
    public void longStreamCompare(){
        assertThat(LongStream.range(-1l,10l).sum(),
                equalTo(ReactiveSeq.rangeLong(-1l,10l).sum().get()));
    }
    @Test
    public void negative(){
        assertThat(ReactiveSeq.range(-1000,Integer.MAX_VALUE)
                .limit(100)
                .count(),equalTo(100L));
    }
    @Test
    public void negativeLong(){
        assertThat(ReactiveSeq.rangeLong(-1000L,Long.MAX_VALUE)
                .limit(100)
                .count(),equalTo(100L));
    }
	@Test
	public void limitRange() throws InterruptedException{
		
		assertThat(ReactiveSeq.range(0,Integer.MAX_VALUE)
				 .limit(100)
				 .count(),equalTo(100L));
	}
	@Test
	public void limitList() throws InterruptedException{
		
		List<Integer> list= new ArrayList<>();
		for(int i=0;i<1000;i++)
			list.add(i);
		assertThat(ReactiveSeq.fromList(list)
				 .limit(100)
				 .count(),equalTo(100L));
		
	}
	@Test
	public void limitArray() throws InterruptedException{
		
		List<Integer> list= new ArrayList<>();
		for(int i=0;i<1000;i++)
			list.add(i);
		assertThat(ReactiveSeq.of(list.toArray())
				 .limit(100)
				 .count(),equalTo(100L));
		
	}
	@Test
	public void skipArray() throws InterruptedException{
		
		List<Integer> list= new ArrayList<>();
		for(int i=0;i<1000;i++)
			list.add(i);
		assertThat(ReactiveSeq.of(list.toArray())
				 .skip(100)
				 .count(),equalTo(900L));
		
	}
	@Test
	public void skipRange() throws InterruptedException{
		
		assertThat(ReactiveSeq.range(0,1000)
				 .skip(100)
				 .count(),equalTo(900L));
	}
	@Test
	public void skipRangeReversed() throws InterruptedException{
		
		assertThat(ReactiveSeq.range(0,1000)
				 .skip(100).reverse()
				 .count(),equalTo(900L));
	}
	@Test
	public void skipList() throws InterruptedException{
		
		List<Integer> list= new ArrayList<>();
		for(int i=0;i<1000;i++)
			list.add(i);
		assertThat(ReactiveSeq.fromList(list)
				 .skip(100)
				 .count(),equalTo(900L));
		
	}
	@Test
	public void reversedOfArray() throws InterruptedException{
		List<Integer> list= new ArrayList<>();
		list.add(1);
		list.add(2);
		
		assertThat(ReactiveSeq.reversedOf(1,2)
							.toList(),
							equalTo(Arrays.asList(2,1)));
		
	}
	@Test
	public void reversedOfList() throws InterruptedException{
		List<Integer> list= new ArrayList<>();
		list.add(1);
		list.add(2);
		
		assertThat(ReactiveSeq.reversedListOf(list)
							.toList(),
							equalTo(Arrays.asList(2,1)));
		
	}
	
}
