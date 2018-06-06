package cyclops.monads.collections.mutable;


import com.oath.cyclops.anym.AnyMSeq;
import com.oath.cyclops.ReactiveConvertableSequence;
import cyclops.reactive.collections.mutable.SortedSetX;
import cyclops.monads.AnyM;

import cyclops.data.tuple.Tuple2;
import cyclops.monads.Witness.sortedSet;
import cyclops.monads.collections.AbstractAnyMSeqTest;
import org.junit.Test;

import java.util.List;

import static java.util.Comparator.comparing;
import static org.hamcrest.Matchers.equalTo;
import static cyclops.data.tuple.Tuple.tuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class SortedSetXTest extends AbstractAnyMSeqTest<sortedSet> {

	@Override
	public <T> AnyMSeq<sortedSet,T> of(T... values) {
		return AnyM.fromSortedSet(SortedSetX.of(values));
	}
    @Test
    public void testSorted() {


        AnyMSeq<sortedSet,Tuple2<Integer, Integer>> t1 = of(tuple(2, 2), tuple(1, 1));

        List<Tuple2<Integer, Integer>> s1 = t1.sorted().to(ReactiveConvertableSequence::converter).listX().sorted();
        assertEquals(tuple(1, 1), s1.get(0));
        assertEquals(tuple(2, 2), s1.get(1));

        AnyMSeq<sortedSet,Tuple2<Integer, String>> t2 = of(tuple(2, "two"), tuple(1, "replaceWith"));
        List<Tuple2<Integer, String>> s2 = t2.sorted(comparing(t -> t._1())).to(ReactiveConvertableSequence::converter).listX().sorted();
        assertEquals(tuple(1, "replaceWith"), s2.get(0));
        assertEquals(tuple(2, "two"), s2.get(1));

        AnyMSeq<sortedSet,Tuple2<Integer, String>> t3 = of(tuple(2, "two"), tuple(1, "replaceWith"));
        List<Tuple2<Integer, String>> s3 = t3.sorted(t -> t._1()).to(ReactiveConvertableSequence::converter).listX().sorted();
        assertEquals(tuple(1, "replaceWith"), s3.get(0));
        assertEquals(tuple(2, "two"), s3.get(1));
    }
	@Test
	public void testRecover1(){
		assertThat(of(1,2,3).map(e->{throw new RuntimeException();}).recover(e->"hello").join(" "),equalTo("hello"));
	}
	@Test
	public void testRecover2(){
		assertThat(of(1,2,3).map(e->{throw new RuntimeException();}).recover(RuntimeException.class,e->"hello").join(" "),equalTo("hello"));
	}
	@Override
	public <T> AnyMSeq<sortedSet,T> empty() {
		return AnyM.fromSortedSet(SortedSetX.empty());
	}



}
