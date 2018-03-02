package cyclops.companion;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cyclops.data.IntMap;
import cyclops.data.LazySeq;
import cyclops.data.Seq;
import cyclops.data.Vector;
import cyclops.function.Semigroup;
import cyclops.companion.Semigroups;

import org.junit.Test;


import cyclops.reactive.ReactiveSeq;

public class SemigroupsTest {


    @Test
    public void testCollectionConcatPVector() {
        cyclops.data.Vector<Integer> list = cyclops.data.Vector.empty();
        list= list.plus(1);
        list = list.plus(2);
        list = list.plus(4);
        Semigroup<Vector<Integer>> combiner= Semigroups.vectorConcat();
        assertThat(combiner.apply(list,Vector.of(4,5,6)).toList(),equalTo(Arrays.asList(1,2,4,4,5,6)));
    }
    @Test
    public void testCollectionConcatSeq() {
        Seq<Integer> list = Seq.of(1,2,4);
        Semigroup<Seq<Integer>> combiner= Semigroups.seqConcat();
        assertThat(combiner.apply(list,Seq.of(4,5,6)).toList(),equalTo(Arrays.asList(1,2,4,4,5,6)));
    }
    @Test
    public void testCollectionConcatLazySeq() {
        LazySeq<Integer> list = LazySeq.of(1,2,4);
        Semigroup<LazySeq<Integer>> combiner= Semigroups.lazySeqConcat();
        assertThat(combiner.apply(list,LazySeq.of(4,5,6)).toList(),equalTo(Arrays.asList(1,2,4,4,5,6)));
    }
    @Test
    public void testCollectionConcatIntMap() {
        IntMap<Integer> list = IntMap.of(1,2,4);
        Semigroup<IntMap<Integer>> combiner= Semigroups.intMapConcat();
        assertThat(combiner.apply(list,IntMap.of(4,5,6)).toList(),equalTo(Arrays.asList(1,2,4,4,5,6)));
    }
    @Test
    public void testCollectionConcatPVector2() {
        cyclops.data.Vector<Integer> list = cyclops.data.Vector.empty();
        list= list.plus(1);
        list = list.plus(2);
        list = list.plus(4);
        Semigroup<Vector<Integer>> combiner= Semigroups.vectorConcat();
        assertThat(combiner.apply(Vector.of(4,5,6),list).toList(),equalTo(Arrays.asList(4,5,6,1,2,4)));
    }

    @Test
    public void testCombineReactiveSeq() {
        assertThat(Semigroups.combineReactiveSeq().apply(ReactiveSeq.of(1,2,3),ReactiveSeq.of(4,5,6)).toList(),equalTo(Arrays.asList(1,2,3,4,5,6)));
    }



    @Test
    public void testCombineStream() {
        assertThat(Semigroups.combineStream().apply(Stream.of(1,2,3),Stream.of(4,5,6)).collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3,4,5,6)));
    }

}
