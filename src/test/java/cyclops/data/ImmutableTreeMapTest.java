package cyclops.data;

import cyclops.collections.tuple.Tuple;
import cyclops.collections.tuple.Tuple2;
import cyclops.data.basetests.BaseImmutableMapTest;

import java.util.Map;
import java.util.stream.Stream;


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
}