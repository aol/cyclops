package com.oath.cyclops.streams;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import cyclops.collections.mutable.ListX;
import org.junit.Test;

import cyclops.reactive.ReactiveSeq;

public class RangeTest {


	@Test
    public void evenOnly(){
	   assertThat(ReactiveSeq.range(0,2,10).sumInt(i->i),equalTo(20));
    }
    @Test
    public void evenOnlyLong(){
        assertThat(ReactiveSeq.rangeLong(0,2,10).sumLong(i->i),equalTo(20L));
    }

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
    public void reversedRangeInt(){
        assertThat(ReactiveSeq.range(10, -10).count(),equalTo(20L));
    }
    @Test
    public void reversedRangeLongWithReverse(){
       assertThat(ReactiveSeq.rangeLong(10, -10).reverse().count(),equalTo(20L));
    }
    @Test
    public void intStreamCompare0(){

        assertThat(IntStream.range(0,10).sum(),
                equalTo(ReactiveSeq.range(0,10).sumInt(i->i)));
    }
    @Test
    public void longStreamCompare0(){
        assertThat(LongStream.range(0,10).sum(),
                equalTo(ReactiveSeq.rangeLong(0,10).sumLong(i->i)));
    }
    @Test
    public void intStreamCompareReversed(){


        assertThat(11,
                equalTo(ReactiveSeq.range(-5,6).reverse().sumInt(i->i)));

    }
    @Test
    public void longStreamCompareReversed(){
        assertThat(11L,
                equalTo(ReactiveSeq.rangeLong(-5,6).reverse().sumLong(i->i)));
    }
    @Test
    public void intStreamCompare(){
        assertThat(IntStream.range(-1,10).sum(),
                equalTo(ReactiveSeq.range(-1,10).sumInt(i->i)));
    }
    @Test
    public void longStreamCompare(){
        assertThat(LongStream.range(-1l,10l).sum(),
                equalTo(ReactiveSeq.rangeLong(-1l,10l).sumLong(i->i)));
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
	public void rangeLong(){
		assertThat(ReactiveSeq.rangeLong(0,Long.MAX_VALUE)
				.limit(2).toListX(),equalTo(ListX.of(0l,1l)));
	}
	@Test
	public void rangeLongReversed(){
		assertThat(ReactiveSeq.rangeLong(0,Long.MAX_VALUE).reverse()
				.limit(2).toListX(),equalTo(ListX.of(9223372036854775807l, 9223372036854775806l)));
	}
    @Test
    public void rangeLongReversedSkip(){
        assertThat(ReactiveSeq.rangeLong(0,5).reverse()
                .skip(3).toListX(),equalTo(ListX.of(2l,1l)));
    }
    @Test
    public void rangeLongSkip(){
        assertThat(ReactiveSeq.rangeLong(0,5)
                .skip(3).toListX(),equalTo(ListX.of(3l,4l)));
    }
	@Test
	public void rangeInt(){
		assertThat(ReactiveSeq.range(0,Integer.MAX_VALUE)
				.limit(2).toListX(),equalTo(ListX.of(0,1)));
	}
	@Test
	public void rangeIntReversed(){
		assertThat(ReactiveSeq.range(0,Integer.MAX_VALUE).reverse()
				.limit(2).toListX(),equalTo(ListX.of(2147483647, 2147483646)));
	}
    @Test
    public void rangeIntReversedSkip2(){
        assertThat(ReactiveSeq.range(0,5).reverse()
                .skip(3).toListX(),equalTo(ListX.of(2,1)));
    }

    @Test
    public void rangeIntSkip2(){
        assertThat(ReactiveSeq.range(0,5)
                .skip(3).toListX(),equalTo(ListX.of(3,4)));
    }

    @Test
    public void take2Reversed(){
        ReactiveSeq.range(0,Integer.MAX_VALUE).reverse().limit(2).printOut();
        assertThat(ReactiveSeq.range(0,Integer.MAX_VALUE).reverse().limit(2).toListX(),equalTo(ListX.of(2147483647, 2147483646)));
    }
    @Test
    public void rangeIntReversedSkip(){

        assertThat(ReactiveSeq.range(0,Integer.MAX_VALUE).reverse()
                .limit(10).skip(8).toListX(),equalTo(ListX.of(2147483639, 2147483638)));
    }

    @Test
    public void rangeIntSkip(){

        assertThat(ReactiveSeq.range(0,Integer.MAX_VALUE)
                .limit(10).skip(8).toListX(),equalTo(ListX.of(8, 9)));
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
    public void skipRangeLong() throws InterruptedException{

        assertThat(ReactiveSeq.rangeLong(0,1000)
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
