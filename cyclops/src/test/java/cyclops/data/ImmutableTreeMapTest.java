package cyclops.data;

import cyclops.companion.Comparators;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.data.basetests.BaseImmutableMapTest;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;


public class ImmutableTreeMapTest extends BaseImmutableMapTest {


    @Override
    protected <K, V> ImmutableMap<K, V> empty() {
        return TreeMap.empty(Comparators.naturalOrderIdentityComparator());
    }

    @Override
    protected <K, V> ImmutableMap<K, V> of(K k1, V v1) {
        return TreeMap.of(Comparators.naturalOrderIdentityComparator(),k1,v1);
    }

    @Override
    protected <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2) {
        return TreeMap.of(Comparators.naturalOrderIdentityComparator(),k1,v1,k2,v2);
    }

    @Override
    protected ImmutableMap<String, Integer> fromMap(Map<String, Integer> map) {
        Stream<Tuple2<String, Integer>> s = map.entrySet().stream().map(e -> Tuple.tuple(e.getKey(), e.getValue()));
        TreeMap<String, Integer> x = TreeMap.fromStream(s,Comparators.naturalOrderIdentityComparator());
        return x;
    }
    @Test @Ignore
    public void add50000Entries(){
        ImmutableMap<Integer,Integer> map = empty();
        for(int i=0;i<50000;i++){
            map = map.put(i,i*2);
        }
        assertThat(map.size(),equalTo(50000));
        putAndCompare(map);
    }

}
