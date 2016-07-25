package com.aol.cyclops;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.junit.Test;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
public class SemigroupsTest {

    @Test
    public void testCollectionConcatListX() {
        ListX<Integer> in1 = ListX.of(1,2);
        ListX<Integer> in2 = ListX.of(3,4);
        Semigroup<ListX<Integer>> listX = Semigroups.collectionXConcat();
        
        ListX<Integer> list =listX.apply(in1, in2);
        
        assertThat(Semigroups.collectionConcat().apply(ListX.of(1,3,4),Arrays.asList(4,5,6)),equalTo(Arrays.asList(1,3,4,4,5,6)));
    }
    @Test
    public void testCollectionConcatListX2() {
        assertThat(Semigroups.collectionConcat().apply(Arrays.asList(4,5,6),ListX.of(1,3,4)),equalTo(Arrays.asList(1,3,4,4,5,6)));
    }
    @Test
    public void testCollectionConcatArrayList() {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(4);
        Semigroup<List<Integer>> combiner= Semigroups.collectionConcat();
        assertThat(combiner.apply(list,Arrays.asList(4,5,6)),equalTo(Arrays.asList(1,2,4,4,5,6)));
    }
    @Test
    public void testCollectionConcatPVector() {
        PVector<Integer> list = TreePVector.empty();
        list= list.plus(1);
        list = list.plus(2);
        list = list.plus(4);
        Semigroup<List<Integer>> combiner= Semigroups.collectionConcat();
        assertThat(combiner.apply(list,Arrays.<Integer>asList(4,5,6)),equalTo(Arrays.asList(1,2,4,4,5,6)));
    }
    @Test
    public void testCollectionConcatPVector2() {
        PVector<Integer> list = TreePVector.empty();
        list= list.plus(1);
        list = list.plus(2);
        list = list.plus(4);
        Semigroup<List<Integer>> combiner= Semigroups.collectionConcat();
        assertThat(combiner.apply(Arrays.asList(4,5,6),list),equalTo(Arrays.asList(1,2,4,4,5,6)));
    }
    
    @Test
    public void testMutableCollectionConcatArrayList() {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(4);
        Semigroup<List<Integer>> combiner= Semigroups.mutableCollectionConcat();
        assertThat(combiner.apply(list,Arrays.asList(4,5,6)),equalTo(Arrays.asList(1,2,4,4,5,6)));
    }

    @Test
    public void testCombineReactiveSeq() {
        assertThat(Semigroups.combineReactiveSeq().apply(ReactiveSeq.of(1,2,3),ReactiveSeq.of(4,5,6)).toList(),equalTo(ListX.of(1,2,3,4,5,6)));
    }

    @Test
    public void testCombineSeq() {
        assertThat(Semigroups.combineSeq().apply(Seq.of(1,2,3),Seq.of(4,5,6)).toList(),equalTo(ListX.of(1,2,3,4,5,6)));
    }

    @Test
    public void testCombineStream() {
        assertThat(Semigroups.combineStream().apply(Stream.of(1,2,3),Stream.of(4,5,6)).collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3,4,5,6)));
    }

}
