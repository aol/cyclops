package cyclops.data;

import cyclops.collections.tuple.Tuple;
import cyclops.collections.tuple.Tuple2;
import cyclops.data.basetests.BaseImmutableMapTest;

import java.util.Map;
import java.util.stream.Stream;


public class ImmutableTrieMapTest extends BaseImmutableMapTest {


    @Override
    protected <K, V> ImmutableMap<K, V> empty() {
        return TrieMap.empty();
    }

    @Override
    protected <K, V> ImmutableMap<K, V> of(K k1, V v1) {
        return TrieMap.of(k1,v1);
    }

    @Override
    protected <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2) {
        return TrieMap.of(k1,v1,k2,v2);
    }

    @Override
    protected ImmutableMap<String, Integer> fromMap(Map<String, Integer> map) {
        Stream<Tuple2<String, Integer>> s = map.entrySet().stream().map(e -> Tuple.tuple(e.getKey(), e.getValue()));
        TrieMap<String, Integer> x = TrieMap.fromStream(s);
        return x;
    }
}