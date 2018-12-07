package cyclops.data;

import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.data.basetests.BaseImmutableMapTest;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.Assert.assertThat;


public class ImmutableHashMapTest extends BaseImmutableMapTest {


    @Override
    protected <K, V> ImmutableMap<K, V> empty() {
        return HashMap.empty();
    }

    @Override
    protected <K, V> ImmutableMap<K, V> of(K k1, V v1) {
        return HashMap.of(k1,v1);
    }

    @Override
    protected <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2) {
        return HashMap.of(k1,v1,k2,v2);
    }

    @Override
    protected ImmutableMap<String, Integer> fromMap(Map<String, Integer> map) {
        Stream<Tuple2<String, Integer>> s = map.entrySet().stream().map(e -> Tuple.tuple(e.getKey(), e.getValue()));
        HashMap<String, Integer> x = HashMap.fromStream(s);
        return x;
    }
}
