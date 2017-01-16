package cyclops.streams.push;

import cyclops.collections.ListX;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Spouts;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class ReactiveRangeTest {
    
    @Test
    public void reversedRange(){
       Spouts.range(10, -10).printOut();
       assertThat(Spouts.range(10, -10).count(),equalTo(20L));
    }
    @Test
    public void reversedRangeWithReverse(){
       assertThat(Spouts.range(10, -10).reverse().count(),equalTo(20L));
    }
    @Test
    public void reversedRangeLong(){
       assertThat(Spouts.rangeLong(10, -10).count(),equalTo(20L));
    }
    @Test
    public void reversedRangeInt(){
        assertThat(Spouts.range(10, -10).count(),equalTo(20L));
    }
    @Test
    public void reversedRangeLongWithReverse(){
       assertThat(Spouts.rangeLong(10, -10).reverse().count(),equalTo(20L));
    }
    @Test
    public void intStreamCompare0(){
       
        assertThat(IntStream.range(0,10).sum(),
                equalTo(Spouts.range(0,10).sumInt(i->i)));
    }
    @Test
    public void longStreamCompare0(){
        assertThat(LongStream.range(0,10).sum(),
                equalTo(Spouts.rangeLong(0,10).sumLong(i->i)));
    }
    @Test
    public void intStreamCompareReversed(){


        assertThat(11,
                equalTo(Spouts.range(-5,6).reverse().sumInt(i->i)));

    }
    @Test
    public void longStreamCompareReversed(){
        assertThat(11L,
                equalTo(Spouts.rangeLong(-5,6).reverse().sumLong(i->i)));
    }

    @Test
    public void intStreamCompare(){

    	        assertThat(IntStream.range(-1,10).sum(),
                equalTo(Spouts.range(-1,10).sumInt(i->i)));

    }
    @Test
    public void longStreamCompare(){
        assertThat(LongStream.range(-1l,10l).sum(),
                equalTo(Spouts.rangeLong(-1l,10l).sumLong(i->i)));
    }
    @Test
    public void negative(){
        assertThat(Spouts.range(-1000,Integer.MAX_VALUE)
                .limit(100)
                .count(),equalTo(100L));
    }
    @Test
    public void negativeLong(){
        assertThat(Spouts.rangeLong(-1000L,Long.MAX_VALUE)
                .limit(100)
                .count(),equalTo(100L));
    }
	@Test
	public void limitRange() throws InterruptedException{
		
		assertThat(Spouts.range(0,Integer.MAX_VALUE)
				 .limit(100)
				 .count(),equalTo(100L));
	}
	@Test
	public void limitList() throws InterruptedException{
		
		List<Integer> list= new ArrayList<>();
		for(int i=0;i<1000;i++)
			list.add(i);
		assertThat(Spouts.fromIterable(list)
				 .limit(100)
				 .count(),equalTo(100L));
		
	}

	@Test
	public void rangeLong(){
		assertThat(Spouts.rangeLong(0,Long.MAX_VALUE)
				.limit(2).toListX(),equalTo(ListX.of(0l,1l)));
	}
	@Test
	public void rangeLongReversed(){
		assertThat(Spouts.rangeLong(0,Long.MAX_VALUE).reverse()
				.limit(2).toListX(),equalTo(ListX.of(9223372036854775807l, 9223372036854775806l)));
	}
    @Test
    public void rangeLongReversedSkip(){
        assertThat(Spouts.rangeLong(0,5).reverse()
                .skip(3).toListX(),equalTo(ListX.of(2l,1l)));
    }
    @Test
    public void rangeLongSkip(){
        assertThat(Spouts.rangeLong(0,5)
                .skip(3).toListX(),equalTo(ListX.of(3l,4l)));
    }
	@Test
	public void rangeInt(){
		assertThat(Spouts.range(0,Integer.MAX_VALUE)
				.limit(2).toListX(),equalTo(ListX.of(0,1)));
	}
	@Test
	public void rangeIntReversed(){
		assertThat(Spouts.range(0,10).reverse()
				.limit(2).toListX(),equalTo(ListX.of(9, 8)));
	}
    @Test
    public void rangeIntReversedSkip2(){
        assertThat(Spouts.range(0,5).reverse()
                .skip(3).toListX(),equalTo(ListX.of(2,1)));
    }

    @Test
    public void rangeIntSkip2(){
        assertThat(Spouts.range(0,5)
                .skip(3).toListX(),equalTo(ListX.of(3,4)));
    }

    @Test
    public void take2Reversed(){

        assertThat(Spouts.range(0,10).reverse().limit(2).toListX(),equalTo(ListX.of(9, 8)));
    }
    @Test
    public void rangeIntReversedSkip(){

        assertThat(Spouts.range(0,10).reverse()
                .limit(10).skip(8).toListX(),equalTo(ListX.of(1, 0)));
    }

    @Test
    public void rangeIntSkip(){

        assertThat(Spouts.range(0,Integer.MAX_VALUE)
                .limit(10).skip(8).toListX(),equalTo(ListX.of(8, 9)));
    }
	@Test
	public void limitArray() throws InterruptedException{
		
		List<Integer> list= new ArrayList<>();
		for(int i=0;i<1000;i++)
			list.add(i);
		assertThat(Spouts.of(list.toArray())
				 .limit(100)
				 .count(),equalTo(100L));
		
	}
	@Test
	public void skipArray() throws InterruptedException{
		
		List<Integer> list= new ArrayList<>();
		for(int i=0;i<1000;i++)
			list.add(i);
		assertThat(Spouts.of(list.toArray())
				 .skip(100)
				 .count(),equalTo(900L));
		
	}
	@Test
	public void skipRange() throws InterruptedException{
		
		assertThat(Spouts.range(0,1000)
				 .skip(100)
				 .count(),equalTo(900L));
	}
    @Test
    public void skipRangeLong() throws InterruptedException{

        assertThat(Spouts.rangeLong(0,1000)
                .skip(100)
                .count(),equalTo(900L));
    }
	@Test
	public void skipRangeReversed() throws InterruptedException{
		
		assertThat(Spouts.range(0,1000)
				 .skip(100).reverse()
				 .count(),equalTo(900L));
	}
	@Test
	public void skipList() throws InterruptedException{
		
		List<Integer> list= new ArrayList<>();
		for(int i=0;i<1000;i++)
			list.add(i);
		assertThat(Spouts.fromIterable(list)
				 .skip(100)
				 .count(),equalTo(900L));
		
	}
	@Test
	public void reversedOfArray() throws InterruptedException{
		List<Integer> list= new ArrayList<>();
		list.add(1);
		list.add(2);
		
		assertThat(Spouts.of(1,2).reverse()
							.toList(),
							equalTo(Arrays.asList(2,1)));
		
	}
	@Test
	public void reversedOfList() throws InterruptedException{
		List<Integer> list= new ArrayList<>();
		list.add(1);
		list.add(2);
		
		assertThat(Spouts.fromIterable(list).reverse()
							.toList(),
							equalTo(Arrays.asList(2,1)));
		
	}
	
}
