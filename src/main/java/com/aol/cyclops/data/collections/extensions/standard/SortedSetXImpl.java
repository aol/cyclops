package com.aol.cyclops.data.collections.extensions.standard;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.Random;
import java.util.SortedSet;
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
public class SortedSetXImpl<T> extends AbstractMutableCollectionX<T> implements SortedSetX<T> {
	
    private final LazyCollection<T,SortedSet<T>> lazy;
	@Getter
	private final Collector<T,?,SortedSet<T>> collector;

    public SortedSetXImpl(SortedSet<T> SortedSet, Collector<T, ?, SortedSet<T>> collector) {
        this.lazy = new LazyCollection<>(SortedSet, null, collector);
        this.collector = collector;
    }

    public SortedSetXImpl(SortedSet<T> SortedSet) {

        this.collector = SortedSetX.defaultCollector();
        this.lazy = new LazyCollection<T, SortedSet<T>>(SortedSet, null, collector);
    }

    private SortedSetXImpl(Stream<T> stream) {

        this.collector = SortedSetX.defaultCollector();
        this.lazy = new LazyCollection<>(null, stream, collector);
    }

    public SortedSetXImpl() {
        this.collector = SortedSetX.defaultCollector();
        this.lazy = new LazyCollection<>((SortedSet) this.collector.supplier().get(), null, collector);
    }

	/**
	 * @param action
	 * @see java.lang.Iterable#forEach(java.util.function.Consumer)
	 */
	public void forEach(Consumer<? super T> action) {
		getSet().forEach(action);
	}

	/**
	 * @return
	 * @see org.pcollections.MapPSet#iterator()
	 */
	public Iterator<T> iterator() {
		return getSet().iterator();
	}

	/**
	 * @return
	 * @see org.pcollections.MapPSet#size()
	 */
	public int size() {
		return getSet().size();
	}

	/**
	 * @param e
	 * @return
	 * @see org.pcollections.MapPSet#contains(java.lang.Object)
	 */
	public boolean contains(Object e) {
		return getSet().contains(e);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.AbstractSet#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		return getSet().equals(o);
	}



	/**
	 * @return
	 * @see java.util.AbstractCollection#isEmpty()
	 */
	public boolean isEmpty() {
		return getSet().isEmpty();
	}

	/**
	 * @return
	 * @see java.util.AbstractSet#hashCode()
	 */
	public int hashCode() {
		return getSet().hashCode();
	}

	/**
	 * @return
	 * @see java.util.AbstractCollection#toArray()
	 */
	public Object[] toArray() {
		return getSet().toArray();
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractSet#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection<?> c) {
		return getSet().removeAll(c);
	}

	/**
	 * @param a
	 * @return
	 * @see java.util.AbstractCollection#toArray(java.lang.Object[])
	 */
	public <T> T[] toArray(T[] a) {
		return getSet().toArray(a);
	}

	/**
	 * @param e
	 * @return
	 * @see java.util.AbstractCollection#add(java.lang.Object)
	 */
	public boolean add(T e) {
		return getSet().add(e);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.AbstractCollection#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		return getSet().remove(o);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection<?> c) {
		return getSet().containsAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection<? extends T> c) {
		return getSet().addAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#retainAll(java.util.Collection)
	 */
	public boolean retainAll(Collection<?> c) {
		return getSet().retainAll(c);
	}

	/**
	 * 
	 * @see java.util.AbstractCollection#clear()
	 */
	public void clear() {
		getSet().clear();
	}

	/**
	 * @return
	 * @see java.util.AbstractCollection#toString()
	 */
	public String toString() {
		return getSet().toString();
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
	 * @return
	 * @see java.util.SortedSet#comparator()
	 */
	public Comparator<? super T> comparator() {
		return getSet().comparator();
	}
	/**
	 * @param fromElement
	 * @param toElement
	 * @return
	 * @see java.util.SortedSet#subSet(java.lang.Object, java.lang.Object)
	 */
	public SortedSetX<T> subSet(T fromElement, T toElement) {
		return new SortedSetXImpl<>(getSet().subSet(fromElement, toElement),this.collector);
	}
	/**
	 * @param toElement
	 * @return
	 * @see java.util.SortedSet#headSet(java.lang.Object)
	 */
	public SortedSetX<T> headSet(T toElement) {
		return new SortedSetXImpl<>(getSet().headSet(toElement),this.collector);
	}
	/**
	 * @param fromElement
	 * @return
	 * @see java.util.SortedSet#tailSet(java.lang.Object)
	 */
	public SortedSet<T> tailSet(T fromElement) {
		return new SortedSetXImpl<>(getSet().tailSet(fromElement),this.collector);
	}
	/**
	 * @return
	 * @see java.util.SortedSet#first()
	 */
	public T first() {
		return getSet().first();
	}
	/**
	 * @return
	 * @see java.util.SortedSet#last()
	 */
	public T last() {
		return getSet().last();
	}
	/**
	 * @return
	 * @see java.util.SortedSet#spliterator()
	 */
	public Spliterator<T> spliterator() {
		return getSet().spliterator();
	}
	/**
	 * @param filter
	 * @return
	 * @see java.util.Collection#removeIf(java.util.function.Predicate)
	 */
	public  boolean removeIf(Predicate<? super T> filter) {
		return getSet().removeIf(filter);
	}
	
	/**
	 * @return
	 * @see java.util.Collection#parallelStream()
	 */
	public Stream<T> parallelStream() {
		return getSet().parallelStream();
	}
    private SortedSet<T> getSet() {
        return lazy.get();
    }
    
    @Override
    public <X> SortedSetX<X> stream(Stream<X> stream){
        return new SortedSetXImpl<X>(stream);
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
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    public SortedSetX<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {
       
        return (SortedSetX<T>)super.combine(predicate, op);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#reverse()
     */
    @Override
    public SortedSetX<T> reverse() {
       
        return(SortedSetX<T>)super.reverse();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#filter(java.util.function.Predicate)
     */
    @Override
    public SortedSetX<T> filter(Predicate<? super T> pred) {
       
        return (SortedSetX<T>)super.filter(pred);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#map(java.util.function.Function)
     */
    @Override
    public <R> SortedSetX<R> map(Function<? super T, ? extends R> mapper) {
       
        return (SortedSetX<R>)super.map(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#flatMap(java.util.function.Function)
     */
    @Override
    public <R> SortedSetX<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
       return (SortedSetX<R>)super.flatMap(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#limit(long)
     */
    @Override
    public SortedSetX<T> limit(long num) {
       return (SortedSetX<T>)super.limit(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#skip(long)
     */
    @Override
    public SortedSetX<T> skip(long num) {
       return (SortedSetX<T>)super.skip(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#takeRight(int)
     */
    @Override
    public SortedSetX<T> takeRight(int num) {
       return (SortedSetX<T>)super.takeRight(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#dropRight(int)
     */
    @Override
    public SortedSetX<T> dropRight(int num) {
       return (SortedSetX<T>)super.dropRight(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#takeWhile(java.util.function.Predicate)
     */
    @Override
    public SortedSetX<T> takeWhile(Predicate<? super T> p) {
       return (SortedSetX<T>)super.takeWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#dropWhile(java.util.function.Predicate)
     */
    @Override
    public SortedSetX<T> dropWhile(Predicate<? super T> p) {
       return (SortedSetX<T>)super.dropWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#takeUntil(java.util.function.Predicate)
     */
    @Override
    public SortedSetX<T> takeUntil(Predicate<? super T> p) {
       return (SortedSetX<T>)super.takeUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#dropUntil(java.util.function.Predicate)
     */
    @Override
    public SortedSetX<T> dropUntil(Predicate<? super T> p) {
       return(SortedSetX<T>)super.dropUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#trampoline(java.util.function.Function)
     */
    @Override
    public <R> SortedSetX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
       return (SortedSetX<R>)super.trampoline(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#slice(long, long)
     */
    @Override
    public SortedSetX<T> slice(long from, long to) {
       return (SortedSetX<T>)super.slice(from, to);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#grouped(int)
     */
    @Override
    public SortedSetX<ListX<T>> grouped(int groupSize) {
       
        return (SortedSetX<ListX<T>>)super.grouped(groupSize);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#grouped(java.util.function.Function, java.util.stream.Collector)
     */
    @Override
    public <K, A, D> SortedSetX<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier,
            Collector<? super T, A, D> downstream) {
       
        return (SortedSetX)super.grouped(classifier, downstream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#grouped(java.util.function.Function)
     */
    @Override
    public <K> SortedSetX<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier) {
       
        return (SortedSetX)super.grouped(classifier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#zip(java.lang.Iterable)
     */
    @Override
    public <U> SortedSetX<Tuple2<T, U>> zip(Iterable<U> other) {
       
        return (SortedSetX<Tuple2<T, U>>)super.zip(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    public <U, R> SortedSetX<R> zip(Iterable<U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
       
        return (SortedSetX<R>)super.zip(other, zipper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#sliding(int)
     */
    @Override
    public SortedSetX<ListX<T>> sliding(int windowSize) {
       
        return (SortedSetX<ListX<T>>)super.sliding(windowSize);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#sliding(int, int)
     */
    @Override
    public SortedSetX<ListX<T>> sliding(int windowSize, int increment) {
       
        return (SortedSetX<ListX<T>>)super.sliding(windowSize, increment);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#scanLeft(com.aol.cyclops.Monoid)
     */
    @Override
    public SortedSetX<T> scanLeft(Monoid<T> monoid) {
       
        return (SortedSetX<T>)super.scanLeft(monoid);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> SortedSetX<U> scanLeft(U seed, BiFunction<U, ? super T, U> function) {
       
        return (SortedSetX<U>) super.scanLeft(seed, function);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#scanRight(com.aol.cyclops.Monoid)
     */
    @Override
    public SortedSetX<T> scanRight(Monoid<T> monoid) {
       
        return (SortedSetX<T>)super.scanRight(monoid);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> SortedSetX<U> scanRight(U identity, BiFunction<? super T, U, U> combiner) {
       
        return (SortedSetX<U>)super.scanRight(identity, combiner);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#sorted(java.util.function.Function)
     */
    @Override
    public <U extends Comparable<? super U>> SortedSetX<T> sorted(Function<? super T, ? extends U> function) {
       
        return (SortedSetX<T>)super.sorted(function);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#plus(java.lang.Object)
     */
    @Override
    public SortedSetX<T> plus(T e) {
       
        return (SortedSetX<T>)super.plus(e);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#plusAll(java.util.Collection)
     */
    @Override
    public SortedSetX<T> plusAll(Collection<? extends T> list) {
       
        return (SortedSetX<T>)super.plusAll(list);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#minus(java.lang.Object)
     */
    @Override
    public SortedSetX<T> minus(Object e) {
       
        return (SortedSetX<T>)super.minus(e);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#minusAll(java.util.Collection)
     */
    @Override
    public SortedSetX<T> minusAll(Collection<?> list) {
       
        return (SortedSetX<T>)super.minusAll(list);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#plusLazy(java.lang.Object)
     */
    @Override
    public SortedSetX<T> plusLazy(T e) {
       
        return (SortedSetX<T>)super.plus(e);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#plusAllLazy(java.util.Collection)
     */
    @Override
    public SortedSetX<T> plusAllLazy(Collection<? extends T> list) {
       
        return (SortedSetX<T>)super.plusAll(list);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#minusLazy(java.lang.Object)
     */
    @Override
    public SortedSetX<T> minusLazy(Object e) {
       
        return (SortedSetX<T>)super.minus(e);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#minusAllLazy(java.util.Collection)
     */
    @Override
    public SortedSetX<T> minusAllLazy(Collection<?> list) {
       
        return (SortedSetX<T>)super.minusAll(list);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#cycle(int)
     */
    @Override
    public ListX<T> cycle(int times) {
        
        return new ListXImpl<T>(this.stream().cycle(times));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#cycle(com.aol.cyclops.sequence.Monoid, int)
     */
    @Override
    public ListX<T> cycle(Monoid<T> m, int times) {
        
        return new ListXImpl<T>(this.stream().cycle(m,times));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#cycleWhile(java.util.function.Predicate)
     */
    @Override
    public ListX<T> cycleWhile(Predicate<? super T> predicate) {
        
        return new ListXImpl<T>(this.stream().cycleWhile(predicate));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#cycleUntil(java.util.function.Predicate)
     */
    @Override
    public ListX<T> cycleUntil(Predicate<? super T> predicate) {
        
        return new ListXImpl<T>(this.stream().cycleUntil(predicate));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#zipStream(java.util.stream.Stream)
     */
    @Override
    public <U> SortedSetX<Tuple2<T, U>> zipStream(Stream<U> other) {
       
        return (SortedSetX<Tuple2<T, U>>)super.zipStream(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#zip(org.jooq.lambda.Seq)
     */
    @Override
    public <U> SortedSetX<Tuple2<T, U>> zip(Seq<U> other) {
       
        return (SortedSetX<Tuple2<T, U>>)super.zip(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <S, U> SortedSetX<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third) {
       
        return (SortedSetX)super.zip3(second, third);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <T2, T3, T4> SortedSetX<Tuple4<T, T2, T3, T4>> zip4(Stream<T2> second, Stream<T3> third,
            Stream<T4> fourth) {
       
        return (SortedSetX<Tuple4<T, T2, T3, T4>>)super.zip4(second, third, fourth);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#zipWithIndex()
     */
    @Override
    public SortedSetX<Tuple2<T, Long>> zipWithIndex() {
       
        return (SortedSetX<Tuple2<T, Long>>)super.zipWithIndex();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#distinct()
     */
    @Override
    public SortedSetX<T> distinct() {
       
        return (SortedSetX<T>)super.distinct();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#sorted()
     */
    @Override
    public SortedSetX<T> sorted() {
       
        return (SortedSetX<T>)super.sorted();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#sorted(java.util.Comparator)
     */
    @Override
    public SortedSetX<T> sorted(Comparator<? super T> c) {
       
        return (SortedSetX<T>)super.sorted(c);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#skipWhile(java.util.function.Predicate)
     */
    @Override
    public SortedSetX<T> skipWhile(Predicate<? super T> p) {
       
        return (SortedSetX<T>)super.skipWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#skipUntil(java.util.function.Predicate)
     */
    @Override
    public SortedSetX<T> skipUntil(Predicate<? super T> p) {
       
        return (SortedSetX<T>)super.skipUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#limitWhile(java.util.function.Predicate)
     */
    @Override
    public SortedSetX<T> limitWhile(Predicate<? super T> p) {
       
        return (SortedSetX<T>)super.limitWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#limitUntil(java.util.function.Predicate)
     */
    @Override
    public SortedSetX<T> limitUntil(Predicate<? super T> p) {
       
        return (SortedSetX<T>)super.limitUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#intersperse(java.lang.Object)
     */
    @Override
    public SortedSetX<T> intersperse(T value) {
       
        return (SortedSetX<T>)super.intersperse(value);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#shuffle()
     */
    @Override
    public SortedSetX<T> shuffle() {
       
        return (SortedSetX<T>)super.shuffle();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#skipLast(int)
     */
    @Override
    public SortedSetX<T> skipLast(int num) {
       
        return (SortedSetX<T>)super.skipLast(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#limitLast(int)
     */
    @Override
    public SortedSetX<T> limitLast(int num) {
       
        return (SortedSetX<T>)super.limitLast(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#onEmpty(java.lang.Object)
     */
    @Override
    public SortedSetX<T> onEmpty(T value) {
       
        return (SortedSetX<T>)super.onEmpty(value);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    public SortedSetX<T> onEmptyGet(Supplier<T> supplier) {
       
        return (SortedSetX<T>)super.onEmptyGet(supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    public <X extends Throwable> SortedSetX<T> onEmptyThrow(Supplier<X> supplier) {
       
        return (SortedSetX<T>)super.onEmptyThrow(supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#shuffle(java.util.Random)
     */
    @Override
    public SortedSetX<T> shuffle(Random random) {
       
        return (SortedSetX<T>)super.shuffle(random);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#ofType(java.lang.Class)
     */
    @Override
    public <U> SortedSetX<U> ofType(Class<U> type) {
       
        return (SortedSetX<U>)super.ofType(type);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#filterNot(java.util.function.Predicate)
     */
    @Override
    public SortedSetX<T> filterNot(Predicate<? super T> fn) {
       
        return (SortedSetX<T>)super.filterNot(fn);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#notNull()
     */
    @Override
    public SortedSetX<T> notNull() {
       
        return (SortedSetX<T>)super.notNull();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#removeAll(java.util.stream.Stream)
     */
    @Override
    public SortedSetX<T> removeAll(Stream<T> stream) {
       
        return (SortedSetX<T>)(super.removeAll(stream));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#removeAll(org.jooq.lambda.Seq)
     */
    @Override
    public SortedSetX<T> removeAll(Seq<T> stream) {
       
        return (SortedSetX<T>)super.removeAll(stream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#removeAll(java.lang.Iterable)
     */
    @Override
    public SortedSetX<T> removeAll(Iterable<T> it) {
       
        return (SortedSetX<T>)super.removeAll(it);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#removeAll(java.lang.Object[])
     */
    @Override
    public SortedSetX<T> removeAll(T... values) {
       
        return (SortedSetX<T>)super.removeAll(values);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#retainAll(java.lang.Iterable)
     */
    @Override
    public SortedSetX<T> retainAll(Iterable<T> it) {
       
        return (SortedSetX<T>)super.retainAll(it);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#retainAll(java.util.stream.Stream)
     */
    @Override
    public SortedSetX<T> retainAll(Stream<T> stream) {
       
        return (SortedSetX<T>)super.retainAll(stream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#retainAll(org.jooq.lambda.Seq)
     */
    @Override
    public SortedSetX<T> retainAll(Seq<T> stream) {
       
        return (SortedSetX<T>)super.retainAll(stream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#retainAll(java.lang.Object[])
     */
    @Override
    public SortedSetX<T> retainAll(T... values) {
       
        return (SortedSetX<T>)super.retainAll(values);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#cast(java.lang.Class)
     */
    @Override
    public <U> SortedSetX<U> cast(Class<U> type) {
       
        return (SortedSetX<U>)super.cast(type);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#patternMatch(java.util.function.Function, java.util.function.Supplier)
     */
    @Override
    public <R> SortedSetX<R> patternMatch(Function<CheckValue1<T, R>, CheckValue1<T, R>> case1,
            Supplier<? extends R> otherwise) {
       
        return (SortedSetX<R>)super.patternMatch(case1, otherwise);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#grouped(int, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> SortedSetX<C> grouped(int size, Supplier<C> supplier) {
       
        return (SortedSetX<C>)super.grouped(size, supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#groupedUntil(java.util.function.Predicate)
     */
    @Override
    public SortedSetX<ListX<T>> groupedUntil(Predicate<? super T> predicate) {
       
        return (SortedSetX<ListX<T>>)super.groupedUntil(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#groupedWhile(java.util.function.Predicate)
     */
    @Override
    public SortedSetX<ListX<T>> groupedWhile(Predicate<? super T> predicate) {
       
        return (SortedSetX<ListX<T>>)super.groupedWhile(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> SortedSetX<C> groupedWhile(Predicate<? super T> predicate,
            Supplier<C> factory) {
        
        return (SortedSetX<C>)super.groupedWhile(predicate, factory);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> SortedSetX<C> groupedUntil(Predicate<? super T> predicate,
            Supplier<C> factory) {
        
        return (SortedSetX<C>)super.groupedUntil(predicate, factory);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SortedSetX#groupedStatefullyWhile(java.util.function.BiPredicate)
     */
    @Override
    public SortedSetX<ListX<T>> groupedStatefullyWhile(BiPredicate<ListX<? super T>, ? super T> predicate) {
        
        return (SortedSetX<ListX<T>>)super.groupedStatefullyWhile(predicate);
    }
    /** SortedSetX methods **/

    /* Makes a defensive copy of this SortedSetX replacing the value at i with the specified element
     *  (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableSequenceX#with(int, java.lang.Object)
     */
    public SortedSetX<T> with(int i,T element){
        return stream( streamInternal().deleteBetween(i, i+1).insertAt(i,element) ) ;
    }
   
    @Override
    public <R> SortedSetX<R> unit(Collection<R> col){
        return SortedSetX.fromIterable(col);
    }
    @Override
    public  <R> SortedSetX<R> unit(R value){
        return SortedSetX.singleton(value);
    }
    @Override
    public <R> SortedSetX<R> unitIterator(Iterator<R> it){
        return SortedSetX.fromIterable(()->it);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.ExtendedTraversable#permutations()
     */
    @Override
    public SortedSetX<ReactiveSeq<T>> permutations() {
        return stream(stream().permutations().map(Comparables::comparable));
        
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.ExtendedTraversable#combinations(int)
     */
    @Override
    public SortedSetX<ReactiveSeq<T>> combinations(int size) {
        return stream(stream().combinations(size).map(Comparables::comparable));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.ExtendedTraversable#combinations()
     */
    @Override
    public SortedSetX<ReactiveSeq<T>> combinations() {
        return stream(stream().combinations().map(Comparables::comparable));
    }
   
    static class Comparables{
        
        static <T, R extends ReactiveSeq<T> & Comparable<T>> R comparable(Seq<T> seq){
            return comparable(ReactiveSeq.fromStream(seq));
        }
        
        @SuppressWarnings("unchecked")
        
        static <T, R extends ReactiveSeq<T> & Comparable<T>> R comparable(ReactiveSeq<T> seq){
            Method compareTo = Stream.of(Comparable.class.getMethods()).filter(m->m.getName().equals("compareTo"))
                        .findFirst().get();
           
            return (R) Proxy.newProxyInstance(SortedSetX.class
                    .getClassLoader(), new Class[]{ReactiveSeq.class, Comparable.class},
                    (proxy,method,args)->{
                        if(compareTo.equals(method))
                           return Objects.compare(System.identityHashCode(seq), System.identityHashCode(args[0]),Comparator.naturalOrder() );
                        else
                           return method.invoke(seq,args);
                    });
            
        }
    }
}
