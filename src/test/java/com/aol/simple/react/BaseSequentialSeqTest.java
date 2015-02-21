package com.aol.simple.react;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.aol.simple.react.stream.api.FutureStream;
import com.aol.simple.react.stream.eager.EagerFutureStream;

public abstract class BaseSequentialSeqTest {
	abstract <U> FutureStream<U> of(U... array);
	FutureStream<Integer> empty;
	FutureStream<Integer> nonEmpty;

	@Before
	public void setup(){
		empty = of();
		nonEmpty = of(1);
	}
	@Test
	public void zip(){
		List<Tuple2<Integer,Integer>> list =
				of(1,2,3,4,5,6).zip(of(100,200,300,400))
												.peek(it -> System.out.println(it)).collect(Collectors.toList());
		
		List<Integer> right = list.stream().map(t -> t.v2).collect(Collectors.toList());
		assertThat(right,hasItem(100));
		assertThat(right,hasItem(200));
		assertThat(right,hasItem(300));
		assertThat(right,hasItem(400));
		
		List<Integer> left = list.stream().map(t -> t.v1).collect(Collectors.toList());
		assertThat(Arrays.asList(1,2,3,4,5,6),hasItem(left.get(0)));
		
		
	}
	
	@Test
	public void zip2of(){
		List<Tuple2<Integer,Integer>> list =of(1,2,3,4,5,6).zip(of(100,200,300,400)).peek(it -> System.out.println(it)).collect(Collectors.toList());
	
		List<Integer> right = list.stream().map(t -> t.v2).collect(Collectors.toList());
		assertThat(right,hasItem(100));
		assertThat(right,hasItem(200));
		assertThat(right,hasItem(300));
		assertThat(right,hasItem(400));
		
		List<Integer> left = list.stream().map(t -> t.v1).collect(Collectors.toList());
		assertThat(Arrays.asList(1,2,3,4,5,6),hasItem(left.get(0)));

	}
	@Test
	public void zipInOrder(){
		List<Tuple2<Integer,Integer>> list =  EagerFutureStream.parallel(1,2,3,4,5,6).sorted()
													.zip( EagerFutureStream.parallel(100,200,300,400).sorted())
													.collect(Collectors.toList());
		
		assertThat(list.get(0).v1,is(1));
		assertThat(list.get(0).v2,is(100));
		
		
	}

	@Test
	public void zipEmpty() throws Exception {
		
		
		final Seq<Integer> zipped = empty.zip(empty, (a, b) -> a + b);
		assertTrue(zipped.collect(Collectors.toList()).isEmpty());
	}

	@Test
	public void shouldReturnEmptySeqWhenZipEmptyWithNonEmpty() throws Exception {
		
		
		
		final Seq<Integer> zipped = empty.zip(nonEmpty, (a, b) -> a + b);
		assertTrue(zipped.collect(Collectors.toList()).isEmpty());
	}

	@Test
	public void shouldReturnEmptySeqWhenZipNonEmptyWithEmpty() throws Exception {
		
		
		final Seq<Integer> zipped = nonEmpty.zip(empty, (a, b) -> a + b);

		
		assertTrue(zipped.collect(Collectors.toList()).isEmpty());
	}

	@Test
	public void shouldZipTwoFiniteSequencesOfSameSize() throws Exception {
		
		final FutureStream<String> first = of("A", "B", "C");
		final FutureStream<Integer> second = of(1, 2, 3);

		
		final Seq<String> zipped = first.zip(second, (a, b) -> a + b);

		
		assertThat(zipped.collect(Collectors.toList()),equalTo(asList("A1", "B2", "C3")));
	}

	

	@Test
	public void shouldTrimSecondFixedSeqIfLonger() throws Exception {
		final FutureStream<String> first = of("A", "B", "C");
		final FutureStream<Integer> second = of(1, 2, 3, 4);

		
		final Seq<String> zipped = first.zip(second, (a, b) -> a + b);

		assertThat(zipped.collect(Collectors.toList()),equalTo(asList("A1", "B2", "C3")));
	}

	@Test
	public void shouldTrimFirstFixedSeqIfLonger() throws Exception {
		final FutureStream<String> first = of("A", "B", "C","D");
		final FutureStream<Integer> second = of(1, 2, 3);
		final Seq<String> zipped = first.zip(second, (a, b) -> a + b);

		
		assertThat(zipped.collect(Collectors.toList()),equalTo(asList("A1", "B2", "C3")));
	}

	@Test
	public void limitWhileTest(){
		List<Integer> list = of(1,2,3,4,5,6).sorted().limitWhile(it -> it<4).peek(it -> System.out.println(it)).collect(Collectors.toList());
	
		assertThat(list,hasItem(1));
		assertThat(list,hasItem(2));
		assertThat(list,hasItem(3));
		
		
		
	}

    @Test
    public void testScanLeftStringConcat() {
        assertThat(of("a", "b", "c").sorted().scanLeft("", String::concat).toList(),
        		is(asList("", "a", "ab", "abc")));
    }
    @Test
    public void testScanLeftSum() {
    	assertThat(of("a", "ab", "abc").sorted().scanLeft(0, (u, t) -> u + t.length()).toList(), 
    			is(asList(0, 1, 3, 6)));
    }

    @Test
    public void testScanRightStringConcat() {
        assertThat(of("a", "b", "c").sorted().scanRight("", String::concat).toList(),
            is(asList("", "c", "bc", "abc")));
    }
    @Test
    public void testScanRightSum() {
    	assertThat(of("a", "ab", "abc").sorted().scanRight(0, (t, u) -> u + t.length()).toList(),
            is(asList(0, 3, 5, 6)));

        
    }

    

    @Test
    public void testReverse() {
        assertThat( of(1, 2, 3).sorted().reverse().toList(), is(asList(3, 2, 1)));
    }

    @Test
    public void testShuffle() {
        Supplier<Seq<Integer>> s = () ->of(1, 2, 3);

        assertEquals(3, s.get().shuffle().toList().size());
        assertThat(s.get().shuffle().toList(), hasItems(1, 2, 3));

        
    }

    @Test
    public void testCycle() {
        assertEquals(asList(1, 2, 1, 2, 1, 2),of(1, 2).sorted().cycle().limit(6).toList());
        assertEquals(asList(1, 2, 3, 1, 2, 3), of(1, 2, 3).sorted().cycle().limit(6).toList());
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
		 Tuple2<Seq<Integer>, Seq<Integer>> copies =of(1,2,3,4,5,6).duplicate();
		 assertTrue(copies.v1.anyMatch(i->i==2));
		 assertTrue(copies.v2.anyMatch(i->i==2));
	}
	
	

	 Throwable ex;
	    @Test
	    public void testCastException() {
	    	ex = null;
	    	of(1, "a", 2, "b", 3, null).capture(e-> ex =e)
	    			.peek(it ->System.out.println(it))
	    			.cast(Integer.class)
	    				.peek(it ->System.out.println(it)).toList();
	    	
	    	assertThat(ex.getCause().getClass(),equalTo(ClassCastException.class));
	    }
	    @Test
	    public void testCastExceptionOnFail() {
	    	ex = null;
	    	of(1, "a", 2, "b", 3, null)//.capture(e-> {e.printStackTrace();ex =e;})
	    			//.peek(it ->System.out.println(it))
	    			.cast(Integer.class)
	    			.<Object>onFail(e -> {
	    				System.out.println("**"+e.getValue());
	    				return (String)e.getValue();
	    				
	    			})
	    			.peek(it ->System.out.println(it)).toList();
	    	
	    	assertThat(ex,is(nullValue()));
	    }

	   

		
	    @Test
	    public void testGroupByEager() {
	        Map<Integer, List<Integer>> map1 =of(1, 2, 3, 4).groupBy(i -> i % 2);
	        assertEquals(asList(2, 4), map1.get(0));
	        assertEquals(asList(1, 3), map1.get(1));
	        assertEquals(2, map1.size());

	     
	    }
	    

	    @Test
	    public void testJoin() {
	        assertEquals("123",of(1, 2, 3).sorted().join());
	        assertEquals("1, 2, 3", of(1, 2, 3).sorted().join(", "));
	        assertEquals("^1|2|3$", of(1, 2, 3).sorted().join("|", "^", "$"));
	    }

	    
	    @Test @Ignore //failing!
	    public void testOptional() {
	        assertEquals(asList(1),of(Optional.of(1)).toList());
	        assertEquals(asList(), of(Optional.empty()).toList());
	    }
	    @Test
	    public void testZipDifferingLength() {
	        List<Tuple2<Integer, String>> list = of(1, 2).zip(of("a", "b", "c", "d")).toList();

	        assertEquals(2, list.size());
	        assertTrue(asList(1,2).contains( list.get(0).v1));
	        assertTrue(""+list.get(1).v2,asList(1,2).contains( list.get(1).v1)); 
	        assertTrue(asList("a", "b", "c", "d").contains( list.get(0).v2));
	        assertTrue(asList("a", "b", "c", "d").contains( list.get(1).v2));
	       
	        
	    }

	    @Test
	    public void testZipWithIndex() {
	        assertEquals(asList(), of().zipWithIndex().toList());
	        assertEquals(asList(tuple("a", 0L)), of("a").zipWithIndex().toList());
	        assertEquals(asList(tuple("a", 0L), tuple("b", 1L)), of("a", "b").zipWithIndex().toList());
	        assertEquals(asList(tuple("a", 0L), tuple("b", 1L), tuple("c", 2L)), of("a", "b", "c").zipWithIndex().toList());
	    }

	   
	    @Test
	    public void testSkipWhile() {
	    	 Supplier<Seq<Integer>> s = () -> Seq.of(1, 2, 3, 4, 5);

	         assertEquals(asList(1, 2, 3, 4, 5), s.get().skipWhile(i -> false).toList());
	         assertEquals(asList(3, 4, 5), s.get().skipWhile(i -> i % 3 != 0).toList());
	         assertEquals(asList(3, 4, 5), s.get().skipWhile(i -> i < 3).toList());
	         assertEquals(asList(4, 5), s.get().skipWhile(i -> i < 4).toList());
	         assertEquals(asList(), s.get().skipWhile(i -> true).toList());
	    }

	    @Test
	    public void testSkipUntil() {
	    	Supplier<Seq<Integer>> s = () -> Seq.of(1, 2, 3, 4, 5);

	        assertEquals(asList(), s.get().skipUntil(i -> false).toList());
	        assertEquals(asList(3, 4, 5), s.get().skipUntil(i -> i % 3 == 0).toList());
	        assertEquals(asList(3, 4, 5), s.get().skipUntil(i -> i == 3).toList());
	        assertEquals(asList(4, 5), s.get().skipUntil(i -> i == 4).toList());
	        assertEquals(asList(1, 2, 3, 4, 5), s.get().skipUntil(i -> true).toList());
		  }

	    @Test
	    public void testSkipUntilWithNulls() {
	    	 Supplier<Seq<Integer>> s = () -> Seq.of(1, 2, null, 3, 4, 5);

	         assertEquals(asList(1, 2, null, 3, 4, 5), s.get().skipUntil(i -> true).toList());
	    }

	    @Test
	    public void testLimitWhile() {
	    	 Supplier<Seq<Integer>> s = () -> Seq.of(1, 2, 3, 4, 5);

	         assertEquals(asList(), s.get().limitWhile(i -> false).toList());
	         assertEquals(asList(1, 2), s.get().limitWhile(i -> i % 3 != 0).toList());
	         assertEquals(asList(1, 2), s.get().limitWhile(i -> i < 3).toList());
	         assertEquals(asList(1, 2, 3), s.get().limitWhile(i -> i < 4).toList());
	         assertEquals(asList(1, 2, 3, 4, 5), s.get().limitWhile(i -> true).toList());
	    }

	    @Test
	    public void testLimitUntil() {
	    	 assertEquals(asList(1, 2, 3, 4, 5),of(1, 2, 3, 4, 5).limitUntil(i -> false).toList());
	         assertEquals(asList(1, 2), of(1, 2, 3, 4, 5).limitUntil(i -> i % 3 == 0).toList());
	         assertEquals(asList(1, 2), of(1, 2, 3, 4, 5).limitUntil(i -> i == 3).toList());
	         assertEquals(asList(1, 2, 3), of(1, 2, 3, 4, 5).limitUntil(i -> i == 4).toList());
	         assertEquals(asList(), of(1, 2, 3, 4, 5).limitUntil(i -> true).toList());

	        
	        
	        assertEquals(asList(), of(1, 2, 3, 4, 5).limitUntil(i -> true).toList());
	    }

	    @Test
	    public void testLimitUntilWithNulls() {
	       

	        assertThat(of(1, 2, null, 3, 4, 5).limitUntil(i -> false).toList(),equalTo(asList(1, 2, null, 3, 4, 5)));
	    }

	    @Test
	    public void testPartition() {
	        Supplier<Seq<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);

	        assertEquals(asList(1, 3, 5), s.get().partition(i -> i % 2 != 0).v1.toList());
	        assertEquals(asList(2, 4, 6), s.get().partition(i -> i % 2 != 0).v2.toList());

	        assertEquals(asList(2, 4, 6), s.get().partition(i -> i % 2 == 0).v1.toList());
	        assertEquals(asList(1, 3, 5), s.get().partition(i -> i % 2 == 0).v2.toList());

	        assertEquals(asList(1, 2, 3), s.get().partition(i -> i <= 3).v1.toList());
	        assertEquals(asList(4, 5, 6), s.get().partition(i -> i <= 3).v2.toList());

	        assertEquals(asList(1, 2, 3, 4, 5, 6), s.get().partition(i -> true).v1.toList());
	        assertEquals(asList(), s.get().partition(i -> true).v2.toList());

	        assertEquals(asList(), s.get().partition(i -> false).v1.toList());
	        assertEquals(asList(1, 2, 3, 4, 5, 6), s.get().partition(i -> false).v2.toList());
	    }

	    @Test
	    public void testSplitAt() {
	        Supplier<Seq<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);

	        assertEquals(asList(), s.get().splitAt(0).v1.toList());
	        assertEquals(asList(1, 2, 3, 4, 5, 6), s.get().splitAt(0).v2.toList());

	        assertEquals(asList(1), s.get().splitAt(1).v1.toList());
	        assertEquals(asList(2, 3, 4, 5, 6), s.get().splitAt(1).v2.toList());

	        assertEquals(asList(1, 2, 3), s.get().splitAt(3).v1.toList());
	        assertEquals(asList(4, 5, 6), s.get().splitAt(3).v2.toList());

	        assertEquals(asList(1, 2, 3, 4, 5, 6), s.get().splitAt(6).v1.toList());
	        assertEquals(asList(), s.get().splitAt(6).v2.toList());

	        assertEquals(asList(1, 2, 3, 4, 5, 6), s.get().splitAt(7).v1.toList());
	        assertEquals(asList(), s.get().splitAt(7).v2.toList());
	    }

	    @Test
	    public void testSplitAtHead() {
	        assertEquals(Optional.empty(), of().splitAtHead().v1);
	        assertEquals(asList(), of().splitAtHead().v2.toList());

	        assertEquals(Optional.of(1), of(1).splitAtHead().v1);
	        assertEquals(asList(), of(1).splitAtHead().v2.toList());

	        assertEquals(Optional.of(1), of(1, 2).splitAtHead().v1);
	        assertEquals(asList(2), of(1, 2).splitAtHead().v2.toList());

	        assertEquals(Optional.of(1), of(1, 2, 3).splitAtHead().v1);
	        assertEquals(Optional.of(2), of(1, 2, 3).splitAtHead().v2.splitAtHead().v1);
	        assertEquals(Optional.of(3), of(1, 2, 3).splitAtHead().v2.splitAtHead().v2.splitAtHead().v1);
	        assertEquals(asList(2, 3), of(1, 2, 3).splitAtHead().v2.toList());
	        assertEquals(asList(3), of(1, 2, 3).splitAtHead().v2.splitAtHead().v2.toList());
	        assertEquals(asList(), of(1, 2, 3).splitAtHead().v2.splitAtHead().v2.splitAtHead().v2.toList());
	    }

	    @Test
	    public void testMinByMaxBy() {
	        Supplier<Seq<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);

	        assertEquals(1, (int) s.get().maxBy(t -> Math.abs(t - 5)).get());
	        assertEquals(5, (int) s.get().minBy(t -> Math.abs(t - 5)).get());

	        assertEquals(6, (int) s.get().maxBy(t -> "" + t).get());
	        assertEquals(1, (int) s.get().minBy(t -> "" + t).get());
	    }

	    @Test
	    public void testUnzip() {
	        Supplier<Seq<Tuple2<Integer, String>>> s = () -> of(tuple(1, "a"), tuple(2, "b"), tuple(3, "c"));

	        Tuple2<Seq<Integer>, Seq<String>> u1 = Seq.unzip(s.get());
	        assertThat(u1.v1.toList(),equalTo(asList(1, 2, 3)));
	        assertThat(u1.v2.toList(),equalTo(asList("a", "b", "c")));

	        Tuple2<Seq<Integer>, Seq<String>> u2 = Seq.unzip(s.get(), v1 -> -v1, v2 -> v2 + "!");
	        assertThat(u2.v1.toList(),equalTo(asList(-1, -2, -3)));
	        assertThat(u2.v2.toList(),equalTo(asList("a!", "b!", "c!")));

	        Tuple2<Seq<Integer>, Seq<String>> u3 = Seq.unzip(s.get(), t -> tuple(-t.v1, t.v2 + "!"));
	        assertThat(u3.v1.toList(),equalTo(asList(-1, -2, -3)));
	        assertThat(u3.v2.toList(),equalTo(asList("a!", "b!", "c!")));

	        Tuple2<Seq<Integer>, Seq<String>> u4 = Seq.unzip(s.get(), (t1, t2) -> tuple(-t1, t2 + "!"));
	        assertThat(u4.v1.toList(),equalTo(asList(-1, -2, -3)));
	        assertThat(u4.v2.toList(),equalTo(asList("a!", "b!", "c!")));
	    }
	   

	    @Test
	    public void testFoldLeft() {
	        Supplier<Seq<String>> s = () -> of("a", "b", "c");

	        assertTrue(s.get().foldLeft("", String::concat).contains("a"));
	        assertTrue(s.get().foldLeft("", String::concat).contains("b"));
	        assertTrue(s.get().foldLeft("", String::concat).contains("c"));
	       
	        assertEquals(3, (int) s.get().foldLeft(0, (u, t) -> u + t.length()));

	        
	        assertEquals(3, (int) s.get().foldRight(0, (t, u) -> u + t.length()));
	        assertEquals("-a-b-c", s.get().foldLeft(new StringBuilder(), (u, t) -> u.append("-").append(t)).toString());
	    }
	    
	    @Test
	    public void testFoldRight(){
	    	 	Supplier<Seq<String>> s = () -> of("a", "b", "c");

		        assertTrue(s.get().foldRight("", String::concat).contains("a"));
		        assertTrue(s.get().foldRight("", String::concat).contains("b"));
		        assertTrue(s.get().foldRight("", String::concat).contains("c"));
		        assertEquals(3, (int) s.get().foldRight(0, (t, u) -> u + t.length()));
		        
	    }
	    
	    @Test
	    public void testFoldLeftStringBuilder() {
	        Supplier<Seq<String>> s = () -> of("a", "b", "c");

	        
	        assertTrue(s.get().foldLeft(new StringBuilder(), (u, t) -> u.append("-").append(t)).toString().contains("a"));
	        assertTrue(s.get().foldLeft(new StringBuilder(), (u, t) -> u.append("-").append(t)).toString().contains("b"));
	        assertTrue(s.get().foldLeft(new StringBuilder(), (u, t) -> u.append("-").append(t)).toString().contains("c"));
	        assertTrue(s.get().foldLeft(new StringBuilder(), (u, t) -> u.append("-").append(t)).toString().contains("-"));
	        
	        
	        assertEquals(3, (int) s.get().foldLeft(0, (u, t) -> u + t.length()));

	       
	    }

	    @Test
	    public void testFoldRighttringBuilder() {
	        Supplier<Seq<String>> s = () -> of("a", "b", "c");

	        
	        assertTrue(s.get().foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).toString().contains("a"));
	        assertTrue(s.get().foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).toString().contains("b"));
	        assertTrue(s.get().foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).toString().contains("c"));
	        assertTrue(s.get().foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).toString().contains("-"));
	        assertEquals("-c-b-a", s.get().foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).toString());
	           
	    }

	  

	    
	
	protected Object sleep(int i) {
		try {
			Thread.currentThread().sleep(i);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return i;
	}
	
}
