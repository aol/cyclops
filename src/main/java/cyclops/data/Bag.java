package cyclops.data;

import cyclops.stream.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.Iterator;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Bag<T> implements Iterable<T>{

    private final HashMap<T,Integer> map;
    private final int size;

    public static <T> Bag<T> empty() {
        return new Bag<>(HashMap.empty(), 0);
    }



    public int size() {
        return size;
    }

    public boolean contains(final T e) {
        return map.get(e).isPresent();
    }

    public Bag<T> plus(final T value) {
        return new Bag<>(map.put(value, map.get(value).orElse(0)+1), size+1);
    }


    public Bag<T> minus(final T value) {
        int n = map.get(value).orElse(0);
        if(n==0)
            return this;
        if(n==1)
            return new Bag<>(map.minus(value), size-1);

        return new Bag<>(map.put(value, n-1), size-1);
    }

    public ReactiveSeq<T> stream(){
        return ReactiveSeq.fromIterable(()->map.iterator()).flatMap(t-> ReactiveSeq.of(t._1()).cycle(t._2()));
    }


    @Override
    public Iterator<T> iterator() {
        return stream().iterator();
    }
}
