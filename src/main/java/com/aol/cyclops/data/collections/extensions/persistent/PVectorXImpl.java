package com.aol.cyclops.data.collections.extensions.persistent;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Random;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.pcollections.PVector;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.Reducers;
import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.data.collections.extensions.AbstractFluentCollectionX;
import com.aol.cyclops.data.collections.extensions.LazyFluentCollection;
import com.aol.cyclops.data.collections.extensions.standard.ListX;

import lombok.AllArgsConstructor;


@AllArgsConstructor
public class PVectorXImpl<T> extends AbstractFluentCollectionX<T> implements PVectorX<T> {
	
    private final LazyFluentCollection<T,PVector<T>> lazy;
    public PVectorXImpl(PVector<T> q){
        this.lazy = new PersistentLazyCollection<>(q,null,Reducers.toPVector());
    }
    private PVectorXImpl(Stream<T> stream){
        this.lazy = new PersistentLazyCollection<>(null,stream,Reducers.toPVector());
    }

	/**
	 * @param action
	 * @see java.lang.Iterable#forEach(java.util.function.Consumer)
	 */
	public void forEach(Consumer<? super T> action) {
		getVector().forEach(action);
	}

	/**
	 * @return
	 * @see org.pcollections.MapPSet#iterator()
	 */
	public Iterator<T> iterator() {
		return getVector().iterator();
	}

	/**
	 * @return
	 * @see org.pcollections.MapPSet#size()
	 */
	public int size() {
		return getVector().size();
	}

	/**
	 * @param e
	 * @return
	 * @see org.pcollections.MapPSet#contains(java.lang.Object)
	 */
	public boolean contains(Object e) {
		return getVector().contains(e);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.AbstractSet#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		return getVector().equals(o);
	}

	/**
	 * @param e
	 * @return
	 * @see org.pcollections.MapPSet#plus(java.lang.Object)
	 */
	public PVectorX<T> plus(T e) {
		return new PVectorXImpl<>(getVector().plus(e));
	}

	/**
	 * @param e
	 * @return
	 * @see org.pcollections.MapPSet#minus(java.lang.Object)
	 */
	public  PVectorX<T> minus(Object e) {
		return new PVectorXImpl<>(getVector().minus(e));
	}

	/**
	 * @param list
	 * @return
	 * @see org.pcollections.MapPSet#plusAll(java.util.Collection)
	 */
	public  PVectorX<T> plusAll(Collection<? extends T> list) {
		return  new PVectorXImpl<>(getVector().plusAll(list));
	}

	/**
	 * @param list
	 * @return
	 * @see org.pcollections.MapPSet#minusAll(java.util.Collection)
	 */
	public PVectorX<T> minusAll(Collection<?> list) {
		return  new PVectorXImpl<>(getVector().minusAll(list));
	}

	/**
	 * @return
	 * @see java.util.AbstractCollection#isEmpty()
	 */
	public boolean isEmpty() {
		return getVector().isEmpty();
	}

	/**
	 * @return
	 * @see java.util.AbstractSet#hashCode()
	 */
	public int hashCode() {
		return getVector().hashCode();
	}

	/**
	 * @return
	 * @see java.util.AbstractCollection#toArray()
	 */
	public Object[] toArray() {
		return getVector().toArray();
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractSet#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection<?> c) {
		return getVector().removeAll(c);
	}

	/**
	 * @param a
	 * @return
	 * @see java.util.AbstractCollection#toArray(java.lang.Object[])
	 */
	public <T> T[] toArray(T[] a) {
		return getVector().toArray(a);
	}

	/**
	 * @param e
	 * @return
	 * @see java.util.AbstractCollection#add(java.lang.Object)
	 */
	public boolean add(T e) {
		return getVector().add(e);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.AbstractCollection#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		return getVector().remove(o);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection<?> c) {
		return getVector().containsAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#addAll(java.util.Collection)
	 */
	@Deprecated
	public boolean addAll(Collection<? extends T> c) {
		return getVector().addAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#retainAll(java.util.Collection)
	 */
	@Deprecated
	public boolean retainAll(Collection<?> c) {
		return getVector().retainAll(c);
	}

	/**
	 * 
	 * @see java.util.AbstractCollection#clear()
	 */
	@Deprecated
	public void clear() {
		getVector().clear();
	}

	/**
	 * @return
	 * @see java.util.AbstractCollection#toString()
	 */
	public String toString() {
		return getVector().toString();
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
	 * @param i
	 * @param e
	 * @return
	 * @see org.pcollections.PStack#with(int, java.lang.Object)
	 */
	public PVectorX<T> with(int i, T e) {
		return new PVectorXImpl<>(getVector().with(i, e));
	}

	/**
	 * @param i
	 * @param e
	 * @return
	 * @see org.pcollections.PStack#plus(int, java.lang.Object)
	 */
	public PVectorX<T> plus(int i, T e) {
		return new PVectorXImpl<>(getVector().plus(i, e));
	}

	/**
	 * @param i
	 * @param list
	 * @return
	 * @see org.pcollections.PStack#plusAll(int, java.util.Collection)
	 */
	public PVectorX<T> plusAll(int i, Collection<? extends T> list) {
		return new PVectorXImpl<>(getVector().plusAll(i, list));
	}

	/**
	 * @param i
	 * @return
	 * @see org.pcollections.PStack#minus(int)
	 */
	public PVectorX<T> minus(int i) {
		return new PVectorXImpl<>(getVector().minus(i));
	}

	/**
	 * @param start
	 * @param end
	 * @return
	 * @see org.pcollections.PStack#subList(int, int)
	 */
	public PVectorX<T> subList(int start, int end) {
		return new PVectorXImpl<>(getVector().subList(start, end));
	}

	

	/**
	 * @param index
	 * @param c
	 * @return
	 * @deprecated
	 * @see org.pcollections.PSequence#addAll(int, java.util.Collection)
	 */
	public boolean addAll(int index, Collection<? extends T> c) {
		return getVector().addAll(index, c);
	}

	/**
	 * @param index
	 * @param element
	 * @return
	 * @deprecated
	 * @see org.pcollections.PSequence#set(int, java.lang.Object)
	 */
	public T set(int index, T element) {
		return getVector().set(index, element);
	}

	/**
	 * @param index
	 * @param element
	 * @deprecated
	 * @see org.pcollections.PSequence#add(int, java.lang.Object)
	 */
	public void add(int index, T element) {
		getVector().add(index, element);
	}

	/**
	 * @param index
	 * @return
	 * @deprecated
	 * @see org.pcollections.PSequence#remove(int)
	 */
	public T remove(int index) {
		return getVector().remove(index);
	}

	/**
	 * @param operator
	 * @see java.util.List#replaceAll(java.util.function.UnaryOperator)
	 */
	public  void replaceAll(UnaryOperator<T> operator) {
		getVector().replaceAll(operator);
	}

	/**
	 * @param filter
	 * @return
	 * @see java.util.Collection#removeIf(java.util.function.Predicate)
	 */
	public boolean removeIf(Predicate<? super T> filter) {
		return getVector().removeIf(filter);
	}

	/**
	 * @param c
	 * @see java.util.List#sort(java.util.Comparator)
	 */
	public void sort(Comparator<? super T> c) {
		getVector().sort(c);
	}

	/**
	 * @return
	 * @see java.util.Collection#spliterator()
	 */
	public Spliterator<T> spliterator() {
		return getVector().spliterator();
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.List#get(int)
	 */
	public T get(int index) {
		return getVector().get(index);
	}

	

	/**
	 * @return
	 * @see java.util.Collection#parallelStream()
	 */
	public Stream<T> parallelStream() {
		return getVector().parallelStream();
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.List#indexOf(java.lang.Object)
	 */
	public int indexOf(Object o) {
		return getVector().indexOf(o);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.List#lastIndexOf(java.lang.Object)
	 */
	public int lastIndexOf(Object o) {
		return getVector().lastIndexOf(o);
	}

	/**
	 * @return
	 * @see java.util.List#listIterator()
	 */
	public ListIterator<T> listIterator() {
		return getVector().listIterator();
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.List#listIterator(int)
	 */
	public ListIterator<T> listIterator(int index) {
		return getVector().listIterator(index);
	}

    private PVector<T> getVector() {
        return lazy.get();
    }

   
    @Override
    public <X> PVectorX<X> stream(Stream<X> stream){
        return new PVectorXImpl<X>(stream);
    }
    
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#stream()
     */
    @Override
    public ReactiveSeq<T> streamInternal() {
        return lazy.stream();
    }
   
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#stream()
     */
    @Override
    public ReactiveSeq<T> stream() {
        return ReactiveSeq.fromStream(lazy.get().stream());
    }
   
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    public PVectorX<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {
       
        return (PVectorX<T>)super.combine(predicate, op);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#reverse()
     */
    @Override
    public PVectorX<T> reverse() {
       
        return(PVectorX<T>)super.reverse();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#filter(java.util.function.Predicate)
     */
    @Override
    public PVectorX<T> filter(Predicate<? super T> pred) {
       
        return (PVectorX<T>)super.filter(pred);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#map(java.util.function.Function)
     */
    @Override
    public <R> PVectorX<R> map(Function<? super T, ? extends R> mapper) {
       
        return (PVectorX<R>)super.map(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#flatMap(java.util.function.Function)
     */
    @Override
    public <R> PVectorX<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
       return (PVectorX<R>)super.flatMap(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#limit(long)
     */
    @Override
    public PVectorX<T> limit(long num) {
       return (PVectorX<T>)super.limit(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#skip(long)
     */
    @Override
    public PVectorX<T> skip(long num) {
       return (PVectorX<T>)super.skip(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#takeRight(int)
     */
    @Override
    public PVectorX<T> takeRight(int num) {
       return (PVectorX<T>)super.takeRight(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#dropRight(int)
     */
    @Override
    public PVectorX<T> dropRight(int num) {
       return (PVectorX<T>)super.dropRight(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#takeWhile(java.util.function.Predicate)
     */
    @Override
    public PVectorX<T> takeWhile(Predicate<? super T> p) {
       return (PVectorX<T>)super.takeWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#dropWhile(java.util.function.Predicate)
     */
    @Override
    public PVectorX<T> dropWhile(Predicate<? super T> p) {
       return (PVectorX<T>)super.dropWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#takeUntil(java.util.function.Predicate)
     */
    @Override
    public PVectorX<T> takeUntil(Predicate<? super T> p) {
       return (PVectorX<T>)super.takeUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#dropUntil(java.util.function.Predicate)
     */
    @Override
    public PVectorX<T> dropUntil(Predicate<? super T> p) {
       return(PVectorX<T>)super.dropUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#trampoline(java.util.function.Function)
     */
    @Override
    public <R> PVectorX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
       return (PVectorX<R>)super.trampoline(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#slice(long, long)
     */
    @Override
    public PVectorX<T> slice(long from, long to) {
       return (PVectorX<T>)super.slice(from, to);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#grouped(int)
     */
    @Override
    public PVectorX<ListX<T>> grouped(int groupSize) {
       
        return (PVectorX<ListX<T>>)super.grouped(groupSize);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#grouped(java.util.function.Function, java.util.stream.Collector)
     */
    @Override
    public <K, A, D> PVectorX<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier,
            Collector<? super T, A, D> downstream) {
       
        return (PVectorX)super.grouped(classifier, downstream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#grouped(java.util.function.Function)
     */
    @Override
    public <K> PVectorX<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier) {
       
        return (PVectorX)super.grouped(classifier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#zip(java.lang.Iterable)
     */
    @Override
    public <U> PVectorX<Tuple2<T, U>> zip(Iterable<U> other) {
       
        return (PVectorX<Tuple2<T, U>>)super.zip(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    public <U, R> PVectorX<R> zip(Iterable<U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
       
        return (PVectorX<R>)super.zip(other, zipper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#sliding(int)
     */
    @Override
    public PVectorX<ListX<T>> sliding(int windowSize) {
       
        return (PVectorX<ListX<T>>)super.sliding(windowSize);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#sliding(int, int)
     */
    @Override
    public PVectorX<ListX<T>> sliding(int windowSize, int increment) {
       
        return (PVectorX<ListX<T>>)super.sliding(windowSize, increment);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#scanLeft(com.aol.cyclops.Monoid)
     */
    @Override
    public PVectorX<T> scanLeft(Monoid<T> monoid) {
       
        return (PVectorX<T>)super.scanLeft(monoid);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> PVectorX<U> scanLeft(U seed, BiFunction<U, ? super T, U> function) {
       
        return (PVectorX<U>) super.scanLeft(seed, function);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#scanRight(com.aol.cyclops.Monoid)
     */
    @Override
    public PVectorX<T> scanRight(Monoid<T> monoid) {
       
        return (PVectorX<T>)super.scanRight(monoid);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> PVectorX<U> scanRight(U identity, BiFunction<? super T, U, U> combiner) {
       
        return (PVectorX<U>)super.scanRight(identity, combiner);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#sorted(java.util.function.Function)
     */
    @Override
    public <U extends Comparable<? super U>> PVectorX<T> sorted(Function<? super T, ? extends U> function) {
       
        return (PVectorX<T>)super.sorted(function);
    }
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#plusLazy(java.lang.Object)
     */
    @Override
    public PVectorX<T> plusLazy(T e) {
       
        return (PVectorX<T>)super.plus(e);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#plusAllLazy(java.util.Collection)
     */
    @Override
    public PVectorX<T> plusAllLazy(Collection<? extends T> list) {
       
        return (PVectorX<T>)super.plusAll(list);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#minusLazy(java.lang.Object)
     */
    @Override
    public PVectorX<T> minusLazy(Object e) {
       
        return (PVectorX<T>)super.minus(e);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#minusAllLazy(java.util.Collection)
     */
    @Override
    public PVectorX<T> minusAllLazy(Collection<?> list) {
       
        return (PVectorX<T>)super.minusAll(list);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.MutableCollectionX#cycle(int)
     */
    @Override
    public PVectorX<T> cycle(int times) {
        
        return stream(this.stream().cycle(times));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.MutableCollectionX#cycle(com.aol.cyclops.sequence.Monoid, int)
     */
    @Override
    public PVectorX<T> cycle(Monoid<T> m, int times) {
        
        return stream(this.stream().cycle(m,times));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.MutableCollectionX#cycleWhile(java.util.function.Predicate)
     */
    @Override
    public PVectorX<T> cycleWhile(Predicate<? super T> predicate) {
        
        return stream(this.stream().cycleWhile(predicate));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.MutableCollectionX#cycleUntil(java.util.function.Predicate)
     */
    @Override
    public PVectorX<T> cycleUntil(Predicate<? super T> predicate) {
        
        return stream(this.stream().cycleUntil(predicate));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#zipStream(java.util.stream.Stream)
     */
    @Override
    public <U> PVectorX<Tuple2<T, U>> zipStream(Stream<U> other) {
       
        return (PVectorX<Tuple2<T, U>>)super.zipStream(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#zip(org.jooq.lambda.Seq)
     */
    @Override
    public <U> PVectorX<Tuple2<T, U>> zip(Seq<U> other) {
       
        return (PVectorX<Tuple2<T, U>>)super.zip(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <S, U> PVectorX<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third) {
       
        return (PVectorX)super.zip3(second, third);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <T2, T3, T4> PVectorX<Tuple4<T, T2, T3, T4>> zip4(Stream<T2> second, Stream<T3> third,
            Stream<T4> fourth) {
       
        return (PVectorX<Tuple4<T, T2, T3, T4>>)super.zip4(second, third, fourth);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#zipWithIndex()
     */
    @Override
    public PVectorX<Tuple2<T, Long>> zipWithIndex() {
       
        return (PVectorX<Tuple2<T, Long>>)super.zipWithIndex();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#distinct()
     */
    @Override
    public PVectorX<T> distinct() {
       
        return (PVectorX<T>)super.distinct();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#sorted()
     */
    @Override
    public PVectorX<T> sorted() {
       
        return (PVectorX<T>)super.sorted();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#sorted(java.util.Comparator)
     */
    @Override
    public PVectorX<T> sorted(Comparator<? super T> c) {
       
        return (PVectorX<T>)super.sorted(c);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#skipWhile(java.util.function.Predicate)
     */
    @Override
    public PVectorX<T> skipWhile(Predicate<? super T> p) {
       
        return (PVectorX<T>)super.skipWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#skipUntil(java.util.function.Predicate)
     */
    @Override
    public PVectorX<T> skipUntil(Predicate<? super T> p) {
       
        return (PVectorX<T>)super.skipUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#limitWhile(java.util.function.Predicate)
     */
    @Override
    public PVectorX<T> limitWhile(Predicate<? super T> p) {
       
        return (PVectorX<T>)super.limitWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#limitUntil(java.util.function.Predicate)
     */
    @Override
    public PVectorX<T> limitUntil(Predicate<? super T> p) {
       
        return (PVectorX<T>)super.limitUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#intersperse(java.lang.Object)
     */
    @Override
    public PVectorX<T> intersperse(T value) {
       
        return (PVectorX<T>)super.intersperse(value);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#shuffle()
     */
    @Override
    public PVectorX<T> shuffle() {
       
        return (PVectorX<T>)super.shuffle();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#skipLast(int)
     */
    @Override
    public PVectorX<T> skipLast(int num) {
       
        return (PVectorX<T>)super.skipLast(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#limitLast(int)
     */
    @Override
    public PVectorX<T> limitLast(int num) {
       
        return (PVectorX<T>)super.limitLast(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#onEmpty(java.lang.Object)
     */
    @Override
    public PVectorX<T> onEmpty(T value) {
       
        return (PVectorX<T>)super.onEmpty(value);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    public PVectorX<T> onEmptyGet(Supplier<T> supplier) {
       
        return (PVectorX<T>)super.onEmptyGet(supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    public <X extends Throwable> PVectorX<T> onEmptyThrow(Supplier<X> supplier) {
       
        return (PVectorX<T>)super.onEmptyThrow(supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#shuffle(java.util.Random)
     */
    @Override
    public PVectorX<T> shuffle(Random random) {
       
        return (PVectorX<T>)super.shuffle(random);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#ofType(java.lang.Class)
     */
    @Override
    public <U> PVectorX<U> ofType(Class<U> type) {
       
        return (PVectorX<U>)super.ofType(type);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#filterNot(java.util.function.Predicate)
     */
    @Override
    public PVectorX<T> filterNot(Predicate<? super T> fn) {
       
        return (PVectorX<T>)super.filterNot(fn);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#notNull()
     */
    @Override
    public PVectorX<T> notNull() {
       
        return (PVectorX<T>)super.notNull();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#removeAll(java.util.stream.Stream)
     */
    @Override
    public PVectorX<T> removeAll(Stream<T> stream) {
       
        return (PVectorX<T>)(super.removeAll(stream));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#removeAll(org.jooq.lambda.Seq)
     */
    @Override
    public PVectorX<T> removeAll(Seq<T> stream) {
       
        return (PVectorX<T>)super.removeAll(stream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#removeAll(java.lang.Iterable)
     */
    @Override
    public PVectorX<T> removeAll(Iterable<T> it) {
       
        return (PVectorX<T>)super.removeAll(it);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#removeAll(java.lang.Object[])
     */
    @Override
    public PVectorX<T> removeAll(T... values) {
       
        return (PVectorX<T>)super.removeAll(values);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#retainAll(java.lang.Iterable)
     */
    @Override
    public PVectorX<T> retainAll(Iterable<T> it) {
       
        return (PVectorX<T>)super.retainAll(it);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#retainAll(java.util.stream.Stream)
     */
    @Override
    public PVectorX<T> retainAll(Stream<T> stream) {
       
        return (PVectorX<T>)super.retainAll(stream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#retainAll(org.jooq.lambda.Seq)
     */
    @Override
    public PVectorX<T> retainAll(Seq<T> stream) {
       
        return (PVectorX<T>)super.retainAll(stream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#retainAll(java.lang.Object[])
     */
    @Override
    public PVectorX<T> retainAll(T... values) {
       
        return (PVectorX<T>)super.retainAll(values);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#cast(java.lang.Class)
     */
    @Override
    public <U> PVectorX<U> cast(Class<U> type) {
       
        return (PVectorX<U>)super.cast(type);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#patternMatch(java.util.function.Function, java.util.function.Supplier)
     */
    @Override
    public <R> PVectorX<R> patternMatch(Function<CheckValue1<T, R>, CheckValue1<T, R>> case1,
            Supplier<? extends R> otherwise) {
       
        return (PVectorX<R>)super.patternMatch(case1, otherwise);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#permutations()
     */
    @Override
    public PVectorX<ReactiveSeq<T>> permutations() {
       
        return (PVectorX<ReactiveSeq<T>>)super.permutations();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#combinations(int)
     */
    @Override
    public PVectorX<ReactiveSeq<T>> combinations(int size) {
       
        return (PVectorX<ReactiveSeq<T>>)super.combinations(size);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#combinations()
     */
    @Override
    public PVectorX<ReactiveSeq<T>> combinations() {
       
        return (PVectorX<ReactiveSeq<T>>)super.combinations();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#grouped(int, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> PVectorX<C> grouped(int size, Supplier<C> supplier) {
       
        return (PVectorX<C>)super.grouped(size, supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#groupedUntil(java.util.function.Predicate)
     */
    @Override
    public PVectorX<ListX<T>> groupedUntil(Predicate<? super T> predicate) {
       
        return (PVectorX<ListX<T>>)super.groupedUntil(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#groupedWhile(java.util.function.Predicate)
     */
    @Override
    public PVectorX<ListX<T>> groupedWhile(Predicate<? super T> predicate) {
       
        return (PVectorX<ListX<T>>)super.groupedWhile(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> PVectorX<C> groupedWhile(Predicate<? super T> predicate,
            Supplier<C> factory) {
        
        return (PVectorX<C>)super.groupedWhile(predicate, factory);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> PVectorX<C> groupedUntil(Predicate<? super T> predicate,
            Supplier<C> factory) {
        
        return (PVectorX<C>)super.groupedUntil(predicate, factory);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PVectorX#groupedStatefullyWhile(java.util.function.BiPredicate)
     */
    @Override
    public PVectorX<ListX<T>> groupedStatefullyWhile(BiPredicate<ListX<? super T>, ? super T> predicate) {
        
        return (PVectorX<ListX<T>>)super.groupedStatefullyWhile(predicate);
    }
    /** PVectorX methods **/

    /* Makes a defensive copy of this PVectorX replacing the value at i with the specified element
     *  (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.MutableSequenceX#with(int, java.lang.Object)
     */
    public PVectorX<T> withLazy(int i,T element){
        return stream( streamInternal().deleteBetween(i, i+1).insertAt(i,element) ) ;
    }
   
    @Override
    public <R> PVectorX<R> unit(Collection<R> col){
        return PVectorX.fromIterable(col);
    }
    @Override
    public  <R> PVectorX<R> unit(R value){
        return PVectorX.singleton(value);
    }
    @Override
    public <R> PVectorX<R> unitIterator(Iterator<R> it){
        return PVectorX.fromIterable(()->it);
    }
}
