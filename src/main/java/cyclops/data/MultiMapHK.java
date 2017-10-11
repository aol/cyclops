package cyclops.data;


import com.aol.cyclops2.hkt.Higher;
import cyclops.control.Option;
import cyclops.typeclasses.Pure;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import cyclops.data.tuple.Tuple2;

import java.util.function.Function;

/*
 Higher kinded multimap
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MultiMapHK<W,K,V> {
    private final HashMap<K,Higher<W,V>> multiMap;
    private final Appender<W,V> appender;
    private final Pure<W> pure;

    public static <W,K,V> MultiMapHK<W,K,V> empty(Appender<W,V> appender, Pure<W> pure){
        return new MultiMapHK<>(HashMap.empty(),appender,pure);
    }

    public MultiMapHK<W,K, V> put(K key, V value) {
        Higher<W,V> hkt = multiMap.get(key).map(v->appender.append(v,value)).orElseGet(()->pure.unit(value));
        return new MultiMapHK<>(multiMap.put(key,hkt),appender,pure);
    }
    public <R> Option<R> get(K key, Function<? super Higher<W,V>,? extends R> decoder){
        return multiMap.get(key).map(decoder);
    }
    public Option<Higher<W,V>> get(K key){
        return multiMap.get(key);
    }

    public boolean containsKey(K key){
        return multiMap.containsKey(key);
    }

    public boolean contains(Tuple2<K,Higher<W,V>>  keyAndValue){
        return multiMap.contains(keyAndValue);
    }
    @FunctionalInterface
    public interface Appender<W,V>{
        public Higher<W,V> append(Higher<W, V> container, V value);
    }
}
