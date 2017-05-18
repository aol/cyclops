package cyclops.collections.immutable;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.jooq.lambda.tuple.Tuple2;

import cyclops.function.Reducer;
import cyclops.companion.Reducers;

public class PMapXs {

    public static <K, V> PMapX<K, V> of() {
        return PMapX.fromMap(new HashMap<>());
    }

    public static <K, V> PMapX<K, V> of(final K key, final V value) {
        return PMapX.fromMap(new Builder<K, V>(
                                               key, value).build());
    }

    public static <K, V> PMapX<K, V> of(final K key, final V value, final K key1, final V value1) {
        return PMapX.fromMap(new Builder<K, V>(
                                               key, value).put(key1, value1)
                                                          .build());
    }

    public static <K, V> PMapX<K, V> of(final K key, final V value, final K key1, final V value1, final K key2, final V value2) {
        return PMapX.fromMap(new Builder<K, V>(
                                               key, value).put(key, value, key1, value1, key2, value2)
                                                          .build());
    }

    public static <K, V> PMapX<K, V> of(final K key, final V value, final K key1, final V value1, final K key2, final V value2, final K key3,
            final V value3) {
        return PMapX.fromMap(new Builder<K, V>(
                                               key, value).put(key, value, key1, value1, key2, value2, key3, value3)
                                                          .build());
    }

    public static <K, V> PMapX<K, V> toPMapX(final Stream<Tuple2<K, V>> stream) {
        return (PMapX<K, V>) toPMapX().mapReduce(stream);
    }

    public static <K, V> Reducer<PMapX<K, V>> toPMapX() {
        return Reducers.toPMapX();
    }

    public static <K, V> Builder<K, V> from(final Map<K, V> map) {
        return new Builder<K, V>(
                                 map);
    }

    public static <K, V> Builder<K, V> map(final K key, final V value) {
        return new Builder<K, V>(
                                 key, value);
    }

    public static <K, V> Builder<K, V> map(final K key, final V value, final K key1, final V value1) {
        return new Builder<K, V>(
                                 key, value).put(key1, value1);
    }

    public static <K, V> Builder<K, V> map(final K key, final V value, final K key1, final V value1, final K key2, final V value2) {
        return new Builder<K, V>(
                                 key, value).put(key, value, key1, value1, key2, value2);
    }

    public static <K, V> Builder<K, V> map(final K key, final V value, final K key1, final V value1, final K key2, final V value2, final K key3,
            final V value3) {
        return new Builder<K, V>(
                                 key, value).put(key, value, key1, value1, key2, value2, key3, value3);
    }

    public static final class Builder<K, V> {
        private final Map<K, V> build;

        public Builder(final K key, final V value) {
            build = new HashMap<K, V>();
            build.put(key, value);
        }

        public Builder(final Map<K, V> map) {
            build = new HashMap<K, V>(
                                      map);

        }

        public Builder<K, V> putAll(final Map<K, V> map) {
            build.putAll(map);
            return this;
        }

        public Builder<K, V> put(final K key, final V value) {
            build.put(key, value);
            return this;
        }

        public Builder<K, V> put(final K key, final V value, final K key1, final V value1) {
            build.put(key, value);
            build.put(key1, value1);
            return this;
        }

        public Builder<K, V> put(final K key, final V value, final K key1, final V value1, final K key2, final V value2) {
            build.put(key, value);
            build.put(key1, value1);
            build.put(key2, value2);
            return this;
        }

        public Builder<K, V> put(final K key, final V value, final K key1, final V value1, final K key2, final V value2, final K key3,
                final V value3) {
            build.put(key, value);
            build.put(key1, value1);
            build.put(key2, value2);
            build.put(key3, value3);
            return this;
        }

        public PMapX<K, V> build() {
            return PMapX.fromMap(build);
        }
    }

}
