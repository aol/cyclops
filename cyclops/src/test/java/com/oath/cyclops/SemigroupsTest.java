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
        assertThat(result,equalTo(new HashSet<>(Arrays.asList(1,2,3,4,5,6))));
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
        assertThat(result,equalTo(new TreeSet<>(Arrays.asList(1,2,3,4,5,6))));
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
        assertThat(result.stream().collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3,4,5,6)));
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
        assertThat(result.stream().collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3,4,5,6)));
    }



    @Test
    public void testCollectionConcatListX2() {
        assertThat(Semigroups.collectionConcat().apply(Arrays.asList(4,5,6),Arrays.asList(1,3,4)),equalTo(Arrays.asList(1,3,4,4,5,6)));
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
        assertThat(combiner.apply(list.toList(),Arrays.<Integer>asList(4,5,6)),equalTo(Arrays.asList(1,2,4,4,5,6)));
    }
    @Test
    public void testCollectionConcatPVector2() {
        Vector<Integer> list = Vector.empty();
        list= list.plus(1);
        list = list.plus(2);
        list = list.plus(4);
        Semigroup<List<Integer>> combiner= Semigroups.collectionConcat();
        assertThat(combiner.apply(Arrays.asList(4,5,6),list.toList()),equalTo(Arrays.asList(1,2,4,4,5,6)));
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
        assertThat(Semigroups.combineReactiveSeq().apply(ReactiveSeq.of(1,2,3),ReactiveSeq.of(4,5,6)).toList(),equalTo(Arrays.asList(1,2,3,4,5,6)));
    }



    @Test
    public void testCombineStream() {
        assertThat(Semigroups.combineStream().apply(Stream.of(1,2,3),Stream.of(4,5,6)).collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3,4,5,6)));
    }

}
