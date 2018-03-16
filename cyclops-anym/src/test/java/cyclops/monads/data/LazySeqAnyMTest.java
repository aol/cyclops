package cyclops.monads.data;

import com.oath.cyclops.anym.AnyMSeq;
import cyclops.companion.Reducers;
import cyclops.data.LazySeq;
import cyclops.monads.AnyM;
import cyclops.monads.Witness.lazySeq;
import cyclops.monads.collections.AbstractAnyMSeqOrderedDependentTest;
import org.junit.Ignore;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class LazySeqAnyMTest extends AbstractAnyMSeqOrderedDependentTest<lazySeq> {

	@Override
	public <T> AnyMSeq<lazySeq,T> of(T... values) {
		return AnyM.fromLazySeq(LazySeq.of(values));
	}

	@Override
	public <T> AnyMSeq<lazySeq,T> empty() {
		return AnyM.fromLazySeq(LazySeq.empty());
	}

  @Test
  public void when(){
    String res=of(1,2,3).visit((x,xs)->
                                xs.join(x>2? "hello" : "world"),()->"boo!");

        assertThat(res,equalTo("2world3"));
    }
	@Test
    public void whenGreaterThan2(){
        String res= of(5,2,3).visit((x,xs)->
                                xs.join(x>2? "hello" : "world"),()->"boo!");

        assertThat(res,equalTo("2hello3"));
    }
    @Test
    public void when2(){

        Integer res =   of(1,2,3).visit((x,xs)->x,()->10);
        System.out.println(res);
    }
    @Test
    public void whenNilOrNot(){
        String res1=    of(1,2,3).visit((x,xs)-> x>2? "hello" : "world",()->"EMPTY");
    }
    @Test
    public void whenNilOrNotJoinWithFirstElement(){


        String res= of(1,2,3).visit((x,xs)-> xs.join(x>2? "hello" : "world"),()->"EMPTY");
        assertThat(res,equalTo("2world3"));
    }

    @Test @Ignore //only works for lazy data types
    public void testRecover1(){

     }
    @Test  @Ignore //only works for lazy data types
    public void testRecover2(){
    }
    @Test
    public void testScanRightSumMonoid() {
      assertThat(of("a", "ab", "abc").peek(System.out::println)
        .map(str -> str.length())
        .peek(System.out::println)
        .scanRight(Reducers.toTotalInt()).toList(), is(asList(6,5,3,0)));

    }
}

