package com.aol.cyclops.data.collections.extensions.standard;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Queue;
import java.util.Random;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Trampoline;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
public class QueueXImpl<T> extends AbstractMutableCollectionX<T> implements QueueX<T> {
	
    private final LazyCollection<T,Queue<T>> lazy;
	@Getter
	private final Collector<T,?,Queue<T>> collector;
	
    public QueueXImpl(Queue<T> Queue, Collector<T, ?, Queue<T>> collector) {
        this.lazy = new LazyCollection<>(Queue, null, collector);
        this.collector = collector;
    }

    public QueueXImpl(Queue<T> Queue) {

        this.collector = QueueX.defaultCollector();
        this.lazy = new LazyCollection<T, Queue<T>>(Queue, null, collector);
    }

    private QueueXImpl(Stream<T> stream) {

        this.collector = QueueX.defaultCollector();
        this.lazy = new LazyCollection<>(null, stream, collector);
    }

    public QueueXImpl() {
        this.collector = QueueX.defaultCollector();
        this.lazy = new LazyCollection<>((Queue) this.collector.supplier().get(), null, collector);
    }

	/**
	 * @param action
	 * @see java.lang.Iterable#forEach(java.util.function.Consumer)
	 */
	public void forEach(Consumer<? super T> action) {
		getQueue().forEach(action);
	}

	/**
	 * @return
	 * @see org.pcollections.MapPSet#iterator()
	 */
	public Iterator<T> iterator() {
		return getQueue().iterator();
	}

	/**
	 * @return
	 * @see org.pcollections.MapPSet#size()
	 */
	public int size() {
		return getQueue().size();
	}

	/**
	 * @param e
	 * @return
	 * @see org.pcollections.MapPSet#contains(java.lang.Object)
	 */
	public boolean contains(Object e) {
		return getQueue().contains(e);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.AbstractSet#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		return getQueue().equals(o);
	}



	/**
	 * @return
	 * @see java.util.AbstractCollection#isEmpty()
	 */
	public boolean isEmpty() {
		return getQueue().isEmpty();
	}

	/**
	 * @return
	 * @see java.util.AbstractSet#hashCode()
	 */
	public int hashCode() {
		return getQueue().hashCode();
	}

	/**
	 * @return
	 * @see java.util.AbstractCollection#toArray()
	 */
	public Object[] toArray() {
		return getQueue().toArray();
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractSet#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection<?> c) {
		return getQueue().removeAll(c);
	}

	/**
	 * @param a
	 * @return
	 * @see java.util.AbstractCollection#toArray(java.lang.Object[])
	 */
	public <T> T[] toArray(T[] a) {
		return getQueue().toArray(a);
	}

	/**
	 * @param e
	 * @return
	 * @see java.util.AbstractCollection#add(java.lang.Object)
	 */
	public boolean add(T e) {
		return getQueue().add(e);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.AbstractCollection#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		return getQueue().remove(o);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection<?> c) {
		return getQueue().containsAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection<? extends T> c) {
		return getQueue().addAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#retainAll(java.util.Collection)
	 */
	public boolean retainAll(Collection<?> c) {
		return getQueue().retainAll(c);
	}

	/**
	 * 
	 * @see java.util.AbstractCollection#clear()
	 */
	public void clear() {
		getQueue().clear();
	}

	/**
	 * @return
	 * @see java.util.AbstractCollection#toString()
	 */
	public String toString() {
		return getQueue().toString();
	}

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Collectable#collect(java.util.stream.Collector)
	 */
	@Override
	public <R, A> R collect(Collector<? super T, A, R> collector) {
		return stream().collect(collector);
	}

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Collectable#count()
	 */
	@Override
	public long count() {
		return this.size();
	}
	
	/**
	 * @param filter
	 * @return
	 * @see java.util.Collection#removeIf(java.util.function.Predicate)
	 */
	public  boolean removeIf(Predicate<? super T> filter) {
		return getQueue().removeIf(filter);
	}
	
	
	/**
	 * @return
	 * @see java.util.Collection#parallelStream()
	 */
	public  Stream<T> parallelStream() {
		return getQueue().parallelStream();
	}
	
	/**
	 * @return
	 * @see java.util.List#spliterator()
	 */
	public Spliterator<T> spliterator() {
		return getQueue().spliterator();
	}
	/**
	 * @param e
	 * @return
	 * @see java.util.Queue#offer(java.lang.Object)
	 */
	public boolean offer(T e) {
		return getQueue().offer(e);
	}
	/**
	 * @return
	 * @see java.util.Queue#remove()
	 */
	public T remove() {
		return getQueue().remove();
	}
	/**
	 * @return
	 * @see java.util.Queue#poll()
	 */
	public T poll() {
		return getQueue().poll();
	}
	/**
	 * @return
	 * @see java.util.Queue#element()
	 */
	public T element() {
		return getQueue().element();
	}
	/**
	 * @return
	 * @see java.util.Queue#peek()
	 */
	public T peek() {
		return getQueue().peek();
	}
    private Queue<T> getQueue() {
        return lazy.get();
    }
	
   
    @Override
    public <X> QueueX<X> stream(Stream<X> stream){
        return new QueueXImpl<X>(stream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#stream()
     */
    @Override
    public ReactiveSeq<T> streamInternal() {
        return lazy.stream();
    }
   
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#stream()
     */
    @Override
    public ReactiveSeq<T> stream() {
        return ReactiveSeq.fromStream(lazy.get().stream());
    }
   
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    public QueueX<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {
       
        return (QueueX<T>)super.combine(predicate, op);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#reverse()
     */
    @Override
    public QueueX<T> reverse() {
       
        return(QueueX<T>)super.reverse();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#filter(java.util.function.Predicate)
     */
    @Override
    public QueueX<T> filter(Predicate<? super T> pred) {
       
        return (QueueX<T>)super.filter(pred);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#map(java.util.function.Function)
     */
    @Override
    public <R> QueueX<R> map(Function<? super T, ? extends R> mapper) {
       
        return (QueueX<R>)super.map(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#flatMap(java.util.function.Function)
     */
    @Override
    public <R> QueueX<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
       return (QueueX<R>)super.flatMap(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#limit(long)
     */
    @Override
    public QueueX<T> limit(long num) {
       return (QueueX<T>)super.limit(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#skip(long)
     */
    @Override
    public QueueX<T> skip(long num) {
       return (QueueX<T>)super.skip(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#takeRight(int)
     */
    @Override
    public QueueX<T> takeRight(int num) {
       return (QueueX<T>)super.takeRight(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#dropRight(int)
     */
    @Override
    public QueueX<T> dropRight(int num) {
       return (QueueX<T>)super.dropRight(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#takeWhile(java.util.function.Predicate)
     */
    @Override
    public QueueX<T> takeWhile(Predicate<? super T> p) {
       return (QueueX<T>)super.takeWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#dropWhile(java.util.function.Predicate)
     */
    @Override
    public QueueX<T> dropWhile(Predicate<? super T> p) {
       return (QueueX<T>)super.dropWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#takeUntil(java.util.function.Predicate)
     */
    @Override
    public QueueX<T> takeUntil(Predicate<? super T> p) {
       return (QueueX<T>)super.takeUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#dropUntil(java.util.function.Predicate)
     */
    @Override
    public QueueX<T> dropUntil(Predicate<? super T> p) {
       return(QueueX<T>)super.dropUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#trampoline(java.util.function.Function)
     */
    @Override
    public <R> QueueX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
       return (QueueX<R>)super.trampoline(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#slice(long, long)
     */
    @Override
    public QueueX<T> slice(long from, long to) {
       return (QueueX<T>)super.slice(from, to);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#grouped(int)
     */
    @Override
    public QueueX<ListX<T>> grouped(int groupSize) {
       
        return (QueueX<ListX<T>>)super.grouped(groupSize);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#grouped(java.util.function.Function, java.util.stream.Collector)
     */
    @Override
    public <K, A, D> QueueX<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier,
            Collector<? super T, A, D> downstream) {
       
        return (QueueX)super.grouped(classifier, downstream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#grouped(java.util.function.Function)
     */
    @Override
    public <K> QueueX<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier) {
       
        return (QueueX)super.grouped(classifier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#zip(java.lang.Iterable)
     */
    @Override
    public <U> QueueX<Tuple2<T, U>> zip(Iterable<U> other) {
       
        return (QueueX<Tuple2<T, U>>)super.zip(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    public <U, R> QueueX<R> zip(Iterable<U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
       
        return (QueueX<R>)super.zip(other, zipper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#sliding(int)
     */
    @Override
    public QueueX<ListX<T>> sliding(int windowSize) {
       
        return (QueueX<ListX<T>>)super.sliding(windowSize);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#sliding(int, int)
     */
    @Override
    public QueueX<ListX<T>> sliding(int windowSize, int increment) {
       
        return (QueueX<ListX<T>>)super.sliding(windowSize, increment);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#scanLeft(com.aol.cyclops.Monoid)
     */
    @Override
    public QueueX<T> scanLeft(Monoid<T> monoid) {
       
        return (QueueX<T>)super.scanLeft(monoid);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> QueueX<U> scanLeft(U seed, BiFunction<U, ? super T, U> function) {
       
        return (QueueX<U>) super.scanLeft(seed, function);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#scanRight(com.aol.cyclops.Monoid)
     */
    @Override
    public QueueX<T> scanRight(Monoid<T> monoid) {
       
        return (QueueX<T>)super.scanRight(monoid);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> QueueX<U> scanRight(U identity, BiFunction<? super T, U, U> combiner) {
       
        return (QueueX<U>)super.scanRight(identity, combiner);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#sorted(java.util.function.Function)
     */
    @Override
    public <U extends Comparable<? super U>> QueueX<T> sorted(Function<? super T, ? extends U> function) {
       
        return (QueueX<T>)super.sorted(function);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#plus(java.lang.Object)
     */
    @Override
    public QueueX<T> plus(T e) {
       
        return (QueueX<T>)super.plus(e);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#plusAll(java.util.Collection)
     */
    @Override
    public QueueX<T> plusAll(Collection<? extends T> list) {
       
        return (QueueX<T>)super.plusAll(list);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#minus(java.lang.Object)
     */
    @Override
    public QueueX<T> minus(Object e) {
       
        return (QueueX<T>)super.minus(e);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#minusAll(java.util.Collection)
     */
    @Override
    public QueueX<T> minusAll(Collection<?> list) {
       
        return (QueueX<T>)super.minusAll(list);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#plusLazy(java.lang.Object)
     */
    @Override
    public QueueX<T> plusLazy(T e) {
       
        return (QueueX<T>)super.plus(e);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#plusAllLazy(java.util.Collection)
     */
    @Override
    public QueueX<T> plusAllLazy(Collection<? extends T> list) {
       
        return (QueueX<T>)super.plusAll(list);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#minusLazy(java.lang.Object)
     */
    @Override
    public QueueX<T> minusLazy(Object e) {
       
        return (QueueX<T>)super.minus(e);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#minusAllLazy(java.util.Collection)
     */
    @Override
    public QueueX<T> minusAllLazy(Collection<?> list) {
       
        return (QueueX<T>)super.minusAll(list);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#cycle(int)
     */
    @Override
    public QueueX<T> cycle(int times) {
       
        return (QueueX<T>)super.cycle(times);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#cycle(com.aol.cyclops.Monoid, int)
     */
    @Override
    public QueueX<T> cycle(Monoid<T> m, int times) {
       
        return (QueueX<T>)super.cycle(m, times);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#cycleWhile(java.util.function.Predicate)
     */
    @Override
    public QueueX<T> cycleWhile(Predicate<? super T> predicate) {
       
        return (QueueX<T>)super.cycleWhile(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#cycleUntil(java.util.function.Predicate)
     */
    @Override
    public QueueX<T> cycleUntil(Predicate<? super T> predicate) {
       
        return (QueueX<T>)super.cycleUntil(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#zipStream(java.util.stream.Stream)
     */
    @Override
    public <U> QueueX<Tuple2<T, U>> zipStream(Stream<U> other) {
       
        return (QueueX<Tuple2<T, U>>)super.zipStream(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#zip(org.jooq.lambda.Seq)
     */
    @Override
    public <U> QueueX<Tuple2<T, U>> zip(Seq<U> other) {
       
        return (QueueX<Tuple2<T, U>>)super.zip(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <S, U> QueueX<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third) {
       
        return (QueueX)super.zip3(second, third);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <T2, T3, T4> QueueX<Tuple4<T, T2, T3, T4>> zip4(Stream<T2> second, Stream<T3> third,
            Stream<T4> fourth) {
       
        return (QueueX<Tuple4<T, T2, T3, T4>>)super.zip4(second, third, fourth);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#zipWithIndex()
     */
    @Override
    public QueueX<Tuple2<T, Long>> zipWithIndex() {
       
        return (QueueX<Tuple2<T, Long>>)super.zipWithIndex();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#distinct()
     */
    @Override
    public QueueX<T> distinct() {
       
        return (QueueX<T>)super.distinct();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#sorted()
     */
    @Override
    public QueueX<T> sorted() {
       
        return (QueueX<T>)super.sorted();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#sorted(java.util.Comparator)
     */
    @Override
    public QueueX<T> sorted(Comparator<? super T> c) {
       
        return (QueueX<T>)super.sorted(c);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#skipWhile(java.util.function.Predicate)
     */
    @Override
    public QueueX<T> skipWhile(Predicate<? super T> p) {
       
        return (QueueX<T>)super.skipWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#skipUntil(java.util.function.Predicate)
     */
    @Override
    public QueueX<T> skipUntil(Predicate<? super T> p) {
       
        return (QueueX<T>)super.skipUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#limitWhile(java.util.function.Predicate)
     */
    @Override
    public QueueX<T> limitWhile(Predicate<? super T> p) {
       
        return (QueueX<T>)super.limitWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#limitUntil(java.util.function.Predicate)
     */
    @Override
    public QueueX<T> limitUntil(Predicate<? super T> p) {
       
        return (QueueX<T>)super.limitUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#intersperse(java.lang.Object)
     */
    @Override
    public QueueX<T> intersperse(T value) {
       
        return (QueueX<T>)super.intersperse(value);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#shuffle()
     */
    @Override
    public QueueX<T> shuffle() {
       
        return (QueueX<T>)super.shuffle();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#skipLast(int)
     */
    @Override
    public QueueX<T> skipLast(int num) {
       
        return (QueueX<T>)super.skipLast(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#limitLast(int)
     */
    @Override
    public QueueX<T> limitLast(int num) {
       
        return (QueueX<T>)super.limitLast(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#onEmpty(java.lang.Object)
     */
    @Override
    public QueueX<T> onEmpty(T value) {
       
        return (QueueX<T>)super.onEmpty(value);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    public QueueX<T> onEmptyGet(Supplier<T> supplier) {
       
        return (QueueX<T>)super.onEmptyGet(supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    public <X extends Throwable> QueueX<T> onEmptyThrow(Supplier<X> supplier) {
       
        return (QueueX<T>)super.onEmptyThrow(supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#shuffle(java.util.Random)
     */
    @Override
    public QueueX<T> shuffle(Random random) {
       
        return (QueueX<T>)super.shuffle(random);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#ofType(java.lang.Class)
     */
    @Override
    public <U> QueueX<U> ofType(Class<U> type) {
       
        return (QueueX<U>)super.ofType(type);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#filterNot(java.util.function.Predicate)
     */
    @Override
    public QueueX<T> filterNot(Predicate<? super T> fn) {
       
        return (QueueX<T>)super.filterNot(fn);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#notNull()
     */
    @Override
    public QueueX<T> notNull() {
       
        return (QueueX<T>)super.notNull();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#removeAll(java.util.stream.Stream)
     */
    @Override
    public QueueX<T> removeAll(Stream<T> stream) {
       
        return (QueueX<T>)(super.removeAll(stream));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#removeAll(org.jooq.lambda.Seq)
     */
    @Override
    public QueueX<T> removeAll(Seq<T> stream) {
       
        return (QueueX<T>)super.removeAll(stream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#removeAll(java.lang.Iterable)
     */
    @Override
    public QueueX<T> removeAll(Iterable<T> it) {
       
        return (QueueX<T>)super.removeAll(it);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#removeAll(java.lang.Object[])
     */
    @Override
    public QueueX<T> removeAll(T... values) {
       
        return (QueueX<T>)super.removeAll(values);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#retainAll(java.lang.Iterable)
     */
    @Override
    public QueueX<T> retainAll(Iterable<T> it) {
       
        return (QueueX<T>)super.retainAll(it);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#retainAll(java.util.stream.Stream)
     */
    @Override
    public QueueX<T> retainAll(Stream<T> stream) {
       
        return (QueueX<T>)super.retainAll(stream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#retainAll(org.jooq.lambda.Seq)
     */
    @Override
    public QueueX<T> retainAll(Seq<T> stream) {
       
        return (QueueX<T>)super.retainAll(stream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#retainAll(java.lang.Object[])
     */
    @Override
    public QueueX<T> retainAll(T... values) {
       
        return (QueueX<T>)super.retainAll(values);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#cast(java.lang.Class)
     */
    @Override
    public <U> QueueX<U> cast(Class<U> type) {
       
        return (QueueX<U>)super.cast(type);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#patternMatch(java.util.function.Function, java.util.function.Supplier)
     */
    @Override
    public <R> QueueX<R> patternMatch(Function<CheckValue1<T, R>, CheckValue1<T, R>> case1,
            Supplier<? extends R> otherwise) {
       
        return (QueueX<R>)super.patternMatch(case1, otherwise);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#permutations()
     */
    @Override
    public QueueX<ReactiveSeq<T>> permutations() {
       
        return (QueueX<ReactiveSeq<T>>)super.permutations();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#combinations(int)
     */
    @Override
    public QueueX<ReactiveSeq<T>> combinations(int size) {
       
        return (QueueX<ReactiveSeq<T>>)super.combinations(size);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#combinations()
     */
    @Override
    public QueueX<ReactiveSeq<T>> combinations() {
       
        return (QueueX<ReactiveSeq<T>>)super.combinations();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#grouped(int, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> QueueX<C> grouped(int size, Supplier<C> supplier) {
       
        return (QueueX<C>)super.grouped(size, supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#groupedUntil(java.util.function.Predicate)
     */
    @Override
    public QueueX<ListX<T>> groupedUntil(Predicate<? super T> predicate) {
       
        return (QueueX<ListX<T>>)super.groupedUntil(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#groupedWhile(java.util.function.Predicate)
     */
    @Override
    public QueueX<ListX<T>> groupedWhile(Predicate<? super T> predicate) {
       
        return (QueueX<ListX<T>>)super.groupedWhile(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> QueueX<C> groupedWhile(Predicate<? super T> predicate,
            Supplier<C> factory) {
        
        return (QueueX<C>)super.groupedWhile(predicate, factory);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> QueueX<C> groupedUntil(Predicate<? super T> predicate,
            Supplier<C> factory) {
        
        return (QueueX<C>)super.groupedUntil(predicate, factory);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.QueueX#groupedStatefullyWhile(java.util.function.BiPredicate)
     */
    @Override
    public QueueX<ListX<T>> groupedStatefullyWhile(BiPredicate<ListX<? super T>, ? super T> predicate) {
        
        return (QueueX<ListX<T>>)super.groupedStatefullyWhile(predicate);
    }
    /** QueueX methods **/

    /* Makes a defensive copy of this QueueX replacing the value at i with the specified element
     *  (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableSequenceX#with(int, java.lang.Object)
     */
    public QueueX<T> with(int i,T element){
        return stream( streamInternal().deleteBetween(i, i+1).insertAt(i,element) ) ;
    }
   
    @Override
    public <R> QueueX<R> unit(Collection<R> col){
        return QueueX.fromIterable(col);
    }
    @Override
    public  <R> QueueX<R> unit(R value){
        return QueueX.singleton(value);
    }
    @Override
    public <R> QueueX<R> unitIterator(Iterator<R> it){
        return QueueX.fromIterable(()->it);
    }
}
