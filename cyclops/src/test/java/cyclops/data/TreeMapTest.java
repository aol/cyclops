package cyclops.data;

import cyclops.companion.PersistentMapXs;
import org.junit.Test;

import java.util.Comparator;

/**
 * Created by johnmcclean on 02/09/2017.
 */
public class TreeMapTest {

    @Test
    public void put(){
        TreeMap<Integer,String> map = TreeMap.fromMap(Comparator.naturalOrder(), PersistentMapXs.of(1,"hello",2,"world"));

        map.stream().printOut();

        map.put(10,"boo!")
                .put(20,"world").remove(10).stream().printOut();

        System.out.println(map.put(10,"boo!").elementAt(10).orElse(null));
    }
}