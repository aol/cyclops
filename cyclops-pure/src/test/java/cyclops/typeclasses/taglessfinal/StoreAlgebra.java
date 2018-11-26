package cyclops.typeclasses.taglessfinal;

import com.oath.cyclops.hkt.Higher;
import cyclops.control.Option;

public interface StoreAlgebra<W,K,V> {
    Higher<W, Option<V>> get(K key);
    Higher<W, Void> put(K key, V value);

}
