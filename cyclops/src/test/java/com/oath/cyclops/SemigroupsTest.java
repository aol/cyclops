package com.oath.cyclops;

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

import cyclops.data.Vector;
import cyclops.function.Semigroup;
import cyclops.companion.Semigroups;

import org.junit.Test;


import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.collections.mutable.DequeX;
import cyclops.reactive.collections.mutable.ListX;
import cyclops.reactive.collections.mutable.QueueX;
import cyclops.reactive.collections.mutable.SetX;
import cyclops.reactive.collections.mutable.SortedSetX;
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
        LinkedListX<Integer> one = LinkedListX.of(1,2,3);


        LinkedListX<Integer> two = LinkedListX.of(4,5,6);


        LinkedListX<Integer> result = Semigroups.<Integer>linkedListXConcat().apply(one,two);
        assertThat(result,equalTo(LinkedListX.of(4,5,6,1,2,3)));
    }
    @Test
    public void testPVectorXConcat() {
        VectorX<Integer> one = VectorX.of(1,2,3);


        VectorX<Integer> two = VectorX.of(4,5,6);


        VectorX<Integer> result = Semigroups.<Integer>vectorXConcat().apply(one,two);
        assertThat(result,equalTo(VectorX.of(1,2,3,4,5,6)));
    }

    @Test
    public void testPSetXConcat() {
        PersistentSetX<Integer> one = PersistentSetX.of(1,2,3);


        PersistentSetX<Integer> two = PersistentSetX.of(4,5,6);


        PersistentSetX<Integer> result = Semigroups.<Integer>persistentSetXConcat().apply(one,two);
        assertThat(result,equalTo(PersistentSetX.of(1,2,3,4,5,6)));
    }

    @Test
    public void testPOrderedSetXConcat() {
        OrderedSetX<Integer> one = OrderedSetX.of(1,2,3);


        OrderedSetX<Integer> two = OrderedSetX.of(4,5,6);


        OrderedSetX<Integer> result = Semigroups.<Integer>orderedSetXConcat().apply(one,two);
        assertThat(result,equalTo(OrderedSetX.of(1,2,3,4,5,6)));
    }

    @Test
    public void testPQueueXConcat() {
        PersistentQueueX<Integer> one = PersistentQueueX.of(1,2,3);


        PersistentQueueX<Integer> two = PersistentQueueX.of(4,5,6);


        PersistentQueueX<Integer> result = Semigroups.<Integer>persistentQueueXConcat().apply(one,two);
        assertThat(result.toList(),equalTo(PersistentQueueX.of(1,2,3,4,5,6).toList()));
    }

    @Test
    public void testPBagXConcat() {
        BagX<Integer> one = BagX.of(1,2,3);


        BagX<Integer> two = BagX.of(4,5,6);


        BagX<Integer> result = Semigroups.<Integer>bagXConcat().apply(one,two);
        assertThat(result,equalTo(BagX.of(1,2,3,4,5,6)));
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
        Vector<Integer> list = Vector.empty();
        list= list.plus(1);
        list = list.plus(2);
        list = list.plus(4);
        Semigroup<List<Integer>> combiner= Semigroups.collectionConcat();
        assertThat(combiner.apply(list.toListX(),Arrays.<Integer>asList(4,5,6)),equalTo(Arrays.asList(1,2,4,4,5,6)));
    }
    @Test
    public void testCollectionConcatPVector2() {
        Vector<Integer> list = Vector.empty();
        list= list.plus(1);
        list = list.plus(2);
        list = list.plus(4);
        Semigroup<List<Integer>> combiner= Semigroups.collectionConcat();
        assertThat(combiner.apply(Arrays.asList(4,5,6),list.toListX()),equalTo(Arrays.asList(1,2,4,4,5,6)));
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
    public void testCombineStream() {
        assertThat(Semigroups.combineStream().apply(Stream.of(1,2,3),Stream.of(4,5,6)).collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3,4,5,6)));
    }

}
