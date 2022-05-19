package cyclops.data;

import com.oath.cyclops.types.persistent.PersistentBag;
import cyclops.reactive.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.reactivestreams.Publisher;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Bag<T> implements ImmutableSet<T>,
                                PersistentBag<T>,
                                Serializable {

    private static final long serialVersionUID = 1L;
    private final HashMap<T,Integer> map;
    private final int size;

    public static <T> Bag<T> empty() {
        return new Bag<>(HashMap.empty(), 0);
    }

    public static <T> Bag<T> singleton(T value) {
        return Bag.<T>empty().plus(value);
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
    public static <T> Bag<T> fromIterable(Iterable<? extends T> values){
        return ReactiveSeq.fromIterable(values).foldLeft(empty(),(a,b)->a.plus(b));
    }


    public int instances(T type){
        return map.getOrElse(type,0);
    }
    public int size() {
        return size;
    }

    @Override
    public Bag<T> add(T value) {
        return plus(value);
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public <R> Bag<R> map(Function<? super T, ? extends R> fn) {
        return fromStream(stream().map(fn));
    }

    @Override
    public <R> Bag<R> flatMap(Function<? super T, ? extends ImmutableSet<? extends R>> fn) {
        return fromStream(stream().flatMap(fn.andThen(s->s.stream())));
    }

    @Override
    public <R> Bag<R> concatMap(Function<? super T, ? extends Iterable<? extends R>> fn) {
        return fromStream(stream().concatMap(fn));
    }

    @Override
    public <R> Bag<R> mergeMap(Function<? super T, ? extends Publisher<? extends R>> fn) {
      return fromStream(stream().mergeMap(fn));
    }

    @Override
    public <R> Bag<R> mergeMap(int maxConcurecy, Function<? super T, ? extends Publisher<? extends R>> fn) {
      return fromStream(stream().mergeMap(maxConcurecy,fn));
    }

    @Override
    public <U> Bag<U> unitIterable(Iterable<U> it) {
        return fromIterable(it);
    }

    @Override
    public Bag<T> filter(Predicate<? super T> predicate) {
        return fromStream(stream().filter(predicate));
    }

    @Override
    public <R> Bag<R> unitStream(Stream<R> stream) {
        return fromStream(stream);
    }



    @Override
    public boolean containsValue(final T e) {
        return map.get(e).isPresent();
    }

    public Bag<T> plus(final T value) {
        return new Bag<>(map.put(value, map.get(value).orElse(0)+1), size+1);
    }

    @Override
    public Bag<T> plusAll(Iterable<? extends T> list) {
        Bag<T> res = this;
        for(T next : list){
            res = res.plus(next);
        }
        return res;
    }



    @Override
    public Bag<T> removeAll(Iterable<? extends T> list) {
        Bag<T> res = this;
        for(T next : list){
            res = res.removeValue(next);
        }
        return res;
    }



    @Override
    public Bag<T> removeValue(final T value) {
        int n = map.get(value).orElse(0);
        if(n==0)
            return this;
        if(n==1)
            return new Bag<>(map.remove(value), size-1);

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
        if (o == null)
            return false;
        if(o instanceof ImmutableSet) {
            ImmutableSet bag = (ImmutableSet) o;
            return equalToIteration(bag);
        }
        if(o instanceof PersistentBag) {
            PersistentBag bag = (PersistentBag) o;
            return equalToIteration(bag);
        }
        return false;
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
