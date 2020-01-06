package cyclops.data;

import com.oath.cyclops.internal.stream.spliterators.IteratableSpliterator;
import com.oath.cyclops.types.persistent.PersistentCollection;
import com.oath.cyclops.types.persistent.PersistentIndexed;
import cyclops.function.Memoize;
import cyclops.function.Monoid;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import org.reactivestreams.Publisher;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class NonEmptyChain<T> extends Chain<T> implements ImmutableList.Some<T>{
    @Override
    public NonEmptyChain<T> appendAll(Iterable<? extends T> value) {
        Chain<? extends T> w = wrap(value);
        return w.isEmpty() ? this : append(this,w);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
    @Override
    public <R> Chain<R> flatMap(Function<? super T, ? extends ImmutableList<? extends R>> fn) {
        return wrap(ReactiveSeq.fromIterable(this).concatMap(fn));
    }

    public <R> NonEmptyChain<R> flatMapNEC(Function<? super T, ? extends NonEmptyChain<? extends R>> fn) {
        return new Chain.Wrap<R>(ReactiveSeq.fromIterable(this).concatMap(fn));
    }

    @Override
    public NonEmptyChain<T> prepend(T value) {
        return append(singleton(value),this);
    }
    @Override
    public NonEmptyChain<T> prependAll(Iterable< ? extends T> value) {
        return append(wrap(value),this);
    }
    @Override
    public NonEmptyChain<T> append(T value) {
        return append(this,singleton(value));
    }
    @Override
    public  <R> NonEmptyChain<R> map(Function<? super T, ? extends R> fn){
        return new Chain.Wrap<R>(ReactiveSeq.fromIterable(this).map(fn));
    }

    @Override
    public NonEmptyChain<T> concat(Chain<T> b) {
        return append(this,b);
    }

    @Override
    public NonEmptyChain<T> insertStreamAt(int pos, Stream<T> stream) {
        return (NonEmptyChain<T>)super.insertStreamAt(pos, stream);
    }

    @Override
    public NonEmptyChain<T> prependStream(Stream<? extends T> stream) {
        return (NonEmptyChain<T>)super.prependStream(stream);
    }

    @Override
    public NonEmptyChain<T> peek(Consumer<? super T> c) {
        return (NonEmptyChain<T>)super.peek(c);
    }

    @Override
    public NonEmptyChain<ReactiveSeq<T>> permutations() {
        return (NonEmptyChain<ReactiveSeq<T>>)super.permutations();
    }

    @Override
    public NonEmptyChain<ReactiveSeq<T>> combinations(int size) {
        return (NonEmptyChain<ReactiveSeq<T>>)super.combinations(size);
    }

    @Override
    public NonEmptyChain<ReactiveSeq<T>> combinations() {
        return (NonEmptyChain<ReactiveSeq<T>>)super.combinations();
    }

    @Override
    public NonEmptyChain<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {
        return (NonEmptyChain<T>)super.combine(predicate, op);
    }

    @Override
    public NonEmptyChain<T> combine(Monoid<T> op, BiPredicate<? super T, ? super T> predicate) {
        return (NonEmptyChain<T>)super.combine(op, predicate);
    }

    @Override
    public NonEmptyChain<T> cycle(long times) {
        return (NonEmptyChain<T>)super.cycle(times);
    }

    @Override
    public NonEmptyChain<T> cycle(Monoid<T> m, long times) {
        return (NonEmptyChain<T>)super.cycle(m, times);
    }

    @Override
    public NonEmptyChain<Seq<T>> sliding(int windowSize) {
        return (NonEmptyChain<Seq<T>>)super.sliding(windowSize);
    }

    @Override
    public NonEmptyChain<Seq<T>> sliding(int windowSize, int increment) {
        return (NonEmptyChain<Seq<T>>)super.sliding(windowSize, increment);
    }

    @Override
    public <C extends PersistentCollection<? super T>> NonEmptyChain<C> grouped(int size, Supplier<C> supplier) {
        return (NonEmptyChain<C>)super.grouped(size, supplier);
    }

    @Override
    public NonEmptyChain<Vector<T>> grouped(int groupSize) {
        return (NonEmptyChain<Vector<T>>)super.grouped(groupSize);
    }

    @Override
    public NonEmptyChain<T> distinct() {
        return (NonEmptyChain<T>)super.distinct();
    }

    @Override
    public NonEmptyChain<T> scanLeft(Monoid<T> monoid) {
        return (NonEmptyChain<T>)super.scanLeft(monoid);
    }

    @Override
    public <U> NonEmptyChain<U> scanLeft(U seed, BiFunction<? super U, ? super T, ? extends U> function) {
        return (NonEmptyChain<U>)super.scanLeft(seed, function);
    }

    @Override
    public NonEmptyChain<T> scanRight(Monoid<T> monoid) {
        return (NonEmptyChain<T>)super.scanRight(monoid);
    }

    @Override
    public <U> NonEmptyChain<U> scanRight(U identity, BiFunction<? super T, ? super U, ? extends U> combiner) {
        return (NonEmptyChain<U>)super.scanRight(identity, combiner);
    }

    @Override
    public NonEmptyChain<T> sorted() {
        return (NonEmptyChain<T>)super.sorted();
    }

    @Override
    public NonEmptyChain<T> sorted(Comparator<? super T> c) {
        return (NonEmptyChain<T>)super.sorted(c);
    }

    @Override
    public NonEmptyChain<T> intersperse(T value) {
        return (NonEmptyChain<T>)super.intersperse(value);
    }

    @Override
    public NonEmptyChain<T> shuffle() {
        return (NonEmptyChain<T>)super.shuffle();
    }

    @Override
    public NonEmptyChain<T> shuffle(Random random) {
        return (NonEmptyChain<T>)super.shuffle(random);
    }

    @Override
    public <U extends Comparable<? super U>> NonEmptyChain<T> sorted(Function<? super T, ? extends U> function) {
        return (NonEmptyChain<T>)super.sorted(function);
    }

    @Override
    public NonEmptyChain<T> prependAll(T... values) {
        return (NonEmptyChain<T>)super.prependAll(values);
    }

    @Override
    public NonEmptyChain<T> insertAt(int pos, T... values) {
        return (NonEmptyChain<T>)super.insertAt(pos, values);
    }

    @Override
    public NonEmptyChain<T> plusAll(Iterable<? extends T> list) {
        return (NonEmptyChain<T>)super.plusAll(list);
    }

    @Override
    public NonEmptyChain<T> plus(T value) {
        return super.plus(value);
    }

    @Override
    public NonEmptyChain<T> insertAt(int pos, Iterable<? extends T> values) {
        return (NonEmptyChain<T>)super.insertAt(pos, values);
    }

    @Override
    public NonEmptyChain<T> insertAt(int i, T value) {
        return (NonEmptyChain<T>)super.insertAt(i, value);
    }

    @Override
    public <R> R fold(Function<? super Some<T>, ? extends R> fn1, Function<? super None<T>, ? extends R> fn2) {
        return fn1.apply(this);
    }

    @Override
    public NonEmptyChain<T> onEmpty(T value) {
        return this;
    }

    @Override
    public NonEmptyChain<T> onEmptyGet(Supplier<? extends T> supplier) {
        return this;
    }


    private Supplier<Integer> hash;
    @Override
    public int hashCode() {
        if(hash==null){
            Supplier<Integer> local = Memoize.memoizeSupplier(()->{
                int hashCode = 1;
                for (T e : this)
                    hashCode = 31*hashCode + (e==null ? 0 : e.hashCode());
                return hashCode;
            });
            hash= local;
            return local.get();
        }
        return hash.get();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj==null)
            return false;
        if (obj == this)
            return true;
        if (obj instanceof Chain) {
            Chain<T> seq1 = this;
            Chain seq2 = (Chain) obj;
            if (seq1.size() != seq2.size()) {
                return false;
            }
        }
        if(obj instanceof PersistentIndexed) {
            return equalToIteration((Iterable)obj);
        }
        return false;
    }
    @Override
    public NonEmptyChain<T> reverse() {
        return new Wrap(this::reverseIterator);
    }
    @Override
    public String toString(){

        Iterator<T> it = iterator();

        StringBuffer b = new StringBuffer("[" + it.next());
        while(it.hasNext()){
            b.append(", "+it.next());
        }
        b.append("]");
        return b.toString();

    }
    @Override
    public Iterator<T> iterator() {
        return new ChainIterator<T>(this);
    }

    @Override
    public <T2, R> Chain<R> zip(BiFunction<? super T, ? super T2, ? extends R> fn, Publisher<? extends T2> publisher) {
        return wrap(Spouts.from(this).zip(fn,publisher));
    }

    @Override
    public Spliterator<T> spliterator() {
        return new IteratableSpliterator<>(this);
    }
}
