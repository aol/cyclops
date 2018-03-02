package cyclops.companion;

import cyclops.function.Semigroup;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class BiFunctionsTest {

    static <T> List<T> asList(T... values){
        List<T> l = new ArrayList<>();
        for(T next : values){
            l.add(next);
        }
        return l;
    }
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

        List<Integer> result = BiFunctions.<Integer>mutableListConcat().apply(list1, list2);
        assertThat(result,equalTo(asList(1,2,3,4,5,6)));

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

        Set<Integer> result = BiFunctions.<Integer>mutableSetConcat().apply(one, two);
        assertThat(result,equalTo(new HashSet<>(asList(1,2,3,4,5,6))));
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

        SortedSet<Integer> result = BiFunctions.<Integer>mutableSortedSetConcat().apply(one, two);
        assertThat(result,equalTo(new TreeSet<>(asList(1,2,3,4,5,6))));
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

        Queue<Integer> result = BiFunctions.<Integer>mutableQueueConcat().apply(one, two);
        assertThat(result.stream().collect(Collectors.toList()),equalTo(asList(1,2,3,4,5,6)));
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

        Deque<Integer> result = BiFunctions.<Integer>mutableDequeConcat().apply(one, two);
        assertThat(result.stream().collect(Collectors.toList()),equalTo(asList(1,2,3,4,5,6)));
    }



    @Test
    public void testCollectionConcatListX2() {
        assertThat(BiFunctions.collectionConcat().apply(asList(4,5,6), asList(1,3,4)),equalTo(asList(4,5,6,1,3,4)));
    }
    @Test
    public void testCollectionConcatArrayList() {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(4);
        BinaryOperator<List<Integer>> combiner= BiFunctions.collectionConcat();
        assertThat(combiner.apply(list, asList(4,5,6)),equalTo(asList(1,2,4,4,5,6)));
    }


    @Test
    public void testMutableCollectionConcatArrayList() {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(4);
        BinaryOperator<List<Integer>> combiner= BiFunctions.mutableCollectionConcat();
        assertThat(combiner.apply(list, asList(4,5,6)),equalTo(asList(1,2,4,4,5,6)));
    }
}
