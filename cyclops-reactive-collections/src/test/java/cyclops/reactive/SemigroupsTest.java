package cyclops.reactive;

import cyclops.ReactiveSemigroups;
import cyclops.companion.Semigroups;
import cyclops.data.Vector;
import cyclops.function.Semigroup;
import cyclops.reactive.collections.immutable.*;
import cyclops.reactive.collections.mutable.*;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class SemigroupsTest {



    @Test
    public void testListXConcat() {
        ListX<Integer>  list1= ListX.of(1,2,3);


        ListX<Integer> list2 = ListX.of(4,5,6);


        ListX<Integer> result = ReactiveSemigroups.<Integer>listXConcat().apply(list1, list2);
        assertThat(result,equalTo(Arrays.asList(1,2,3,4,5,6)));
    }

    @Test
    public void testSetXConcat() {
        SetX<Integer> one =SetX.of(1,2,3);


        SetX<Integer> two = SetX.of(4,5,6);


        SetX<Integer> result = ReactiveSemigroups.<Integer>setXConcat().apply(one,two);
        assertThat(result,equalTo(SetX.of(1,2,3,4,5,6)));
    }

    @Test
    public void testSortedSetXConcat() {
        SortedSetX<Integer> one =SortedSetX.of(1,2,3);


        SortedSetX<Integer> two = SortedSetX.of(4,5,6);


        SortedSetX<Integer> result = ReactiveSemigroups.<Integer>sortedSetXConcat().apply(one,two);
        assertThat(result,equalTo(SetX.of(1,2,3,4,5,6)));
    }

    @Test
    public void testQueueXConcat() {
        QueueX<Integer> one =QueueX.of(1,2,3);


        QueueX<Integer> two = QueueX.of(4,5,6);


        QueueX<Integer> result = ReactiveSemigroups.<Integer>queueXConcat().apply(one,two);
        assertThat(result.toList(),equalTo(ListX.of(1,2,3,4,5,6)));
    }

    @Test
    public void testDequeXConcat() {
        DequeX<Integer> one =DequeX.of(1,2,3);


        DequeX<Integer> two = DequeX.of(4,5,6);


        DequeX<Integer> result = ReactiveSemigroups.<Integer>dequeXConcat().apply(one,two);
        assertThat(result.toList(),equalTo(DequeX.of(1,2,3,4,5,6).toList()));
    }

    @Test
    public void testPStackXConcat() {
        LinkedListX<Integer> one = LinkedListX.of(1,2,3);


        LinkedListX<Integer> two = LinkedListX.of(4,5,6);


        LinkedListX<Integer> result = ReactiveSemigroups.<Integer>linkedListXConcat().apply(one,two);
        assertThat(result,equalTo(LinkedListX.of(4,5,6,1,2,3)));
    }
    @Test
    public void testPVectorXConcat() {
        VectorX<Integer> one = VectorX.of(1,2,3);


        VectorX<Integer> two = VectorX.of(4,5,6);


        VectorX<Integer> result = ReactiveSemigroups.<Integer>vectorXConcat().apply(one,two);
        assertThat(result,equalTo(VectorX.of(1,2,3,4,5,6)));
    }

    @Test
    public void testPSetXConcat() {
        PersistentSetX<Integer> one = PersistentSetX.of(1,2,3);


        PersistentSetX<Integer> two = PersistentSetX.of(4,5,6);


        PersistentSetX<Integer> result = ReactiveSemigroups.<Integer>persistentSetXConcat().apply(one,two);
        assertThat(result,equalTo(PersistentSetX.of(1,2,3,4,5,6)));
    }

    @Test
    public void testPOrderedSetXConcat() {
        OrderedSetX<Integer> one = OrderedSetX.of(1,2,3);


        OrderedSetX<Integer> two = OrderedSetX.of(4,5,6);


        OrderedSetX<Integer> result = ReactiveSemigroups.<Integer>orderedSetXConcat().apply(one,two);
        assertThat(result,equalTo(OrderedSetX.of(1,2,3,4,5,6)));
    }

    @Test
    public void testPQueueXConcat() {
        PersistentQueueX<Integer> one = PersistentQueueX.of(1,2,3);


        PersistentQueueX<Integer> two = PersistentQueueX.of(4,5,6);


        PersistentQueueX<Integer> result = ReactiveSemigroups.<Integer>persistentQueueXConcat().apply(one,two);
        assertThat(result.toList(),equalTo(PersistentQueueX.of(1,2,3,4,5,6).toList()));
    }

    @Test
    public void testPBagXConcat() {
        BagX<Integer> one = BagX.of(1,2,3);


        BagX<Integer> two = BagX.of(4,5,6);


        BagX<Integer> result = ReactiveSemigroups.<Integer>bagXConcat().apply(one,two);
        assertThat(result,equalTo(BagX.of(1,2,3,4,5,6)));
    }
    @Test
    public void testCollectionConcatListX() {
        ListX<Integer> in1 = ListX.of(1,2);
        ListX<Integer> in2 = ListX.of(3,4);
        Semigroup<ListX<Integer>> listX = ReactiveSemigroups.collectionXConcat();

        ListX<Integer> list =listX.apply(in1, in2);

        assertThat(list,equalTo(Arrays.asList(1,2,3,4)));
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
