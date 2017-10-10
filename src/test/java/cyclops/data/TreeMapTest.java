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

        map.plus(10,"boo!")
                .plus(20,"world").minus(10).stream().printOut();

        System.out.println(map.plus(10,"boo!").get(10).orElse(null));
    }
}