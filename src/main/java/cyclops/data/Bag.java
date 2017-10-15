package cyclops.data;

import com.aol.cyclops2.types.traversable.IterableX;
import cyclops.reactive.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Bag<T> implements IterableX<T>, Serializable {

    private static final long serialVersionUID = 1L;
    private final HashMap<T,Integer> map;
    private final int size;

    public static <T> Bag<T> empty() {
        return new Bag<>(HashMap.empty(), 0);
    }

    public static <T> Bag<T> of(T... values){
        Bag<T> res = empty();
        for(T next : values){
            res = res.plus(next);
        }
        return res;
    }

    public static <T> Bag<T> fromStream(Stream<T> values){
        return ReactiveSeq.fromStream(values).foldLeft(empty(),(a,b)->a.plus(b));
    }


    public int instances(T type){
        return map.getOrElse(type,0);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bag<?> bag = (Bag<?>) o;
        System.out.println(Objects.equals(map, bag.map));
        return size == bag.size &&
                Objects.equals(map, bag.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map, size);
    }

    @Override
    public String toString() {
        return map.mkString();
    }
}
