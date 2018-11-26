package cyclops.typeclasses.taglessfinal;

import com.oath.cyclops.hkt.DataWitness;
import com.oath.cyclops.hkt.DataWitness.io;
import com.oath.cyclops.hkt.Higher;
import cyclops.control.Option;
import cyclops.data.HashMap;
import cyclops.reactive.IO;

import java.util.Map;

public class StoreIO<K,V> implements StoreAlgebra<io,K,V> {
    private  HashMap<K,V> map = HashMap.empty();
    @Override
    public Higher<io, Option<V>> get(K key) {
        return IO.of(map.get(key));
    }

    @Override
    public Higher<io, Void> put(K key, V value) {
        map = map.put(key,value);

        return IO.of((Void)null);
    }
}
