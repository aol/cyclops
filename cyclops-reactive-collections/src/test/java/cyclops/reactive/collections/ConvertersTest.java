package cyclops.reactive.collections;

import com.oath.cyclops.types.persistent.*;
import cyclops.companion.MapXs;
import cyclops.companion.PersistentMapXs;
import org.junit.Test;


import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;

/**
 * Created by johnmcclean on 17/05/2017.
 */
public class ConvertersTest {
    @Test
    public void convert(){

        LinkedList<Integer> list1 = Arrays.asList(1,2,3).to(MapXs.Converters::LinkedList);
        ArrayList<Integer> list2 = Arrays.asList(1,2,3).to(MapXs.Converters::ArrayList);


        assertThat(list1,equalTo(list2));
        assertThat(list1,equalTo(Arrays.asList(1,2,3)));

        PersistentList<Integer> pstack = LinkedArrays.asList(1,2,3).to(MapXs.Converters::PStack);
        PersistentList<Integer> pvector = VectorX.of(1,2,3).to(MapXs.Converters::PVector);
        PersistentSet<Integer> pset = PersistentSetX.of(1,2,3).to(MapXs.Converters::PSet);
        PersistentSortedSet<Integer> pOrderedSet = OrderedSetX.of(1,2,3).to(MapXs.Converters::POrderedSet);
        PersistentBag<Integer> pBag = BagX.of(1,2,3).to(MapXs.Converters::PBag);
        PersistentQueue<Integer> pQueue = PersistentQueueX.of(1,2,3).to(MapXs.Converters::PQueue);
        PersistentMap<Integer,Integer> pMap = PersistentMapXs.of(1,2).to(MapXs.Converters::PMap);

        HashSet<Integer> set = SetX.of(1,2,3).to(MapXs.Converters::HashSet);
        ArrayDeque<Integer> deque = DequeX.of(1,2,3).to(MapXs.Converters::ArrayDeque);
        ArrayBlockingQueue<Integer> queue = QueueX.of(1,2,3).to(MapXs.Converters::ArrayBlockingQueue);
        TreeSet<Integer> tset = SortedSetX.of(1,2,3).to(MapXs.Converters::TreeSet);
        HashMap<Integer,Integer> map = MapXs.of(1,2).to(MapXs.Converters::HashMap);

        assertThat(pstack,equalTo(pvector));
        assertThat(pset,equalTo(pOrderedSet));
        assertThat(pset,equalTo(pBag.stream().to().persistentSetX()));
        assertThat(pQueue.stream().collect(Collectors.toList()), equalTo(list1));

        assertThat(map,not(equalTo(pMap)));
        assertThat(pMap,not(equalTo(map)));
        assertThat(set,equalTo(tset));
        assertThat(deque.stream().collect(Collectors.toList()),equalTo(list1));
        assertThat(queue.stream().collect(Collectors.toList()),equalTo(list1));

    }
}
