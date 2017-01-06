package com.aol.cyclops2;

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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cyclops.function.Semigroup;
import cyclops.Semigroups;
import org.jooq.lambda.Seq;
import org.junit.Test;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

import cyclops.stream.ReactiveSeq;
import cyclops.collections.immutable.PBagX;
import cyclops.collections.immutable.POrderedSetX;
import cyclops.collections.immutable.PQueueX;
import cyclops.collections.immutable.PSetX;
import cyclops.collections.immutable.PStackX;
import cyclops.collections.immutable.PVectorX;
import cyclops.collections.DequeX;
import cyclops.collections.ListX;
import cyclops.collections.QueueX;
import cyclops.collections.SetX;
import cyclops.collections.SortedSetX;
public class SemigroupsTest {


    @Test
    public void testMutableListConcat() {
        List<Integer> list1 = new ArrayList<>();
        list1.add(1);
        list1.add(2);
        list1.add(3);
        
        List<Integer> list2 = new ArrayList<>();
        list2.add(4);
        list2.add(5);
        list2.add(6);
        
        List<Integer> result = Semigroups.<Integer>mutableListConcat().apply(list1, list2);
        assertThat(result,equalTo(Arrays.asList(1,2,3,4,5,6)));

    }

    @Test
    public void testMutableSetConcat() {
        Set<Integer> one = new HashSet<>();
        one.add(1);
        one.add(2);
        one.add(3);
        
        Set<Integer> two = new HashSet<>();
        two.add(4);
        two.add(5);
        two.add(6);
        
        Set<Integer> result = Semigroups.<Integer>mutableSetConcat().apply(one, two);
        assertThat(result,equalTo(SetX.of(1,2,3,4,5,6)));
    }

    @Test
    public void testMutableSortedSetConcat() {
        SortedSet<Integer> one = new TreeSet<>();
        one.add(1);
        one.add(2);
        one.add(3);
        
        SortedSet<Integer> two = new TreeSet<>();
        two.add(4);
        two.add(5);
        two.add(6);
        
        SortedSet<Integer> result = Semigroups.<Integer>mutableSortedSetConcat().apply(one, two);
        assertThat(result,equalTo(SortedSetX.of(1,2,3,4,5,6)));
    }

    @Test
    public void testMutableQueueConcat() {
        Queue<Integer> one = new LinkedBlockingQueue<>(100);
        one.add(1);
        one.add(2);
        one.add(3);
        
        Queue<Integer> two = new LinkedBlockingQueue<>(100);
        two.add(4);
        two.add(5);
        two.add(6);
        
        Queue<Integer> result = Semigroups.<Integer>mutableQueueConcat().apply(one, two);
        assertThat(result.stream().collect(Collectors.toList()),equalTo(ListX.of(1,2,3,4,5,6)));
    }

    @Test
    public void testMutableDequeConcat() {
        Deque<Integer> one = new LinkedList<>();
        one.add(1);
        one.add(2);
        one.add(3);
        
        Deque<Integer> two = new LinkedList<>();
        two.add(4);
        two.add(5);
        two.add(6);
        
        Deque<Integer> result = Semigroups.<Integer>mutableDequeConcat().apply(one, two);
        assertThat(result.stream().collect(Collectors.toList()),equalTo(ListX.of(1,2,3,4,5,6)));
    }

    @Test
    public void testListXConcat() {
        ListX<Integer>  list1= ListX.of(1,2,3);
       
        
        ListX<Integer> list2 = ListX.of(4,5,6);
       
        
        ListX<Integer> result = Semigroups.<Integer>listXConcat().apply(list1, list2);
        assertThat(result,equalTo(Arrays.asList(1,2,3,4,5,6)));
    }

    @Test
    public void testSetXConcat() {
        SetX<Integer> one =SetX.of(1,2,3);
       
        
        SetX<Integer> two = SetX.of(4,5,6);
       
        
        SetX<Integer> result = Semigroups.<Integer>setXConcat().apply(one,two);
        assertThat(result,equalTo(SetX.of(1,2,3,4,5,6)));
    }

    @Test
    public void testSortedSetXConcat() {
        SortedSetX<Integer> one =SortedSetX.of(1,2,3);
       
        
        SortedSetX<Integer> two = SortedSetX.of(4,5,6);
       
        
        SortedSetX<Integer> result = Semigroups.<Integer>sortedSetXConcat().apply(one,two);
        assertThat(result,equalTo(SetX.of(1,2,3,4,5,6)));
    }

    @Test
    public void testQueueXConcat() {
        QueueX<Integer> one =QueueX.of(1,2,3);
       
        
        QueueX<Integer> two = QueueX.of(4,5,6);
       
        
        QueueX<Integer> result = Semigroups.<Integer>queueXConcat().apply(one,two);
        assertThat(result.toList(),equalTo(ListX.of(1,2,3,4,5,6)));
    }

    @Test
    public void testDequeXConcat() {
        DequeX<Integer> one =DequeX.of(1,2,3);
       
        
        DequeX<Integer> two = DequeX.of(4,5,6);
       
        
        DequeX<Integer> result = Semigroups.<Integer>dequeXConcat().apply(one,two);
        assertThat(result.toList(),equalTo(DequeX.of(1,2,3,4,5,6).toList()));
    }

    @Test
    public void testPStackXConcat() {
        PStackX<Integer> one =PStackX.of(1,2,3);
       
        
        PStackX<Integer> two = PStackX.of(4,5,6);
       
        
        PStackX<Integer> result = Semigroups.<Integer>pStackXConcat().apply(one,two);
        assertThat(result,equalTo(PStackX.of(6,5,4,1,2,3)));
    }
    @Test
    public void testPVectorXConcat() {
        PVectorX<Integer> one =PVectorX.of(1,2,3);
       
        
        PVectorX<Integer> two = PVectorX.of(4,5,6);
       
        
        PVectorX<Integer> result = Semigroups.<Integer>pVectorXConcat().apply(one,two);
        assertThat(result,equalTo(PVectorX.of(1,2,3,4,5,6)));
    }

    @Test
    public void testPSetXConcat() {
        PSetX<Integer> one =PSetX.of(1,2,3);
       
        
        PSetX<Integer> two = PSetX.of(4,5,6);
       
        
        PSetX<Integer> result = Semigroups.<Integer>pSetXConcat().apply(one,two);
        assertThat(result,equalTo(PSetX.of(1,2,3,4,5,6)));
    }

    @Test
    public void testPOrderedSetXConcat() {
        POrderedSetX<Integer> one =POrderedSetX.of(1,2,3);
       
        
        POrderedSetX<Integer> two = POrderedSetX.of(4,5,6);
       
        
        POrderedSetX<Integer> result = Semigroups.<Integer>pOrderedSetXConcat().apply(one,two);
        assertThat(result,equalTo(POrderedSetX.of(1,2,3,4,5,6)));
    }

    @Test
    public void testPQueueXConcat() {
        PQueueX<Integer> one =PQueueX.of(1,2,3);
       
        
        PQueueX<Integer> two = PQueueX.of(4,5,6);
       
        
        PQueueX<Integer> result = Semigroups.<Integer>pQueueXConcat().apply(one,two);
        assertThat(result.toList(),equalTo(PQueueX.of(1,2,3,4,5,6).toList()));
    }

    @Test
    public void testPBagXConcat() {
        PBagX<Integer> one =PBagX.of(1,2,3);
       
        
        PBagX<Integer> two = PBagX.of(4,5,6);
       
        
        PBagX<Integer> result = Semigroups.<Integer>pBagXConcat().apply(one,two);
        assertThat(result,equalTo(PBagX.of(1,2,3,4,5,6)));
    }
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
