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
import org.pcollections.PStack;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.Reducers;
import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.data.collections.extensions.AbstractFluentCollectionX;
import com.aol.cyclops.data.collections.extensions.LazyFluentCollection;
import com.aol.cyclops.data.collections.extensions.standard.ListX;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;


@AllArgsConstructor
public class PStackXImpl<T> extends AbstractFluentCollectionX<T> implements PStackX<T> {
	@Wither
	private final LazyFluentCollection<T,PStack<T>> lazy;
    public PStackXImpl(PStack<T> set,boolean efficientOps){
        this.lazy = new PersistentLazyCollection<>(set,null,Reducers.toPStack());
        this.efficientOps=efficientOps;
    }
    private PStackXImpl(Stream<T> stream,boolean efficientOps){
        this.lazy = new PersistentLazyCollection<>(null,stream,Reducers.toPStack());
        this.efficientOps=efficientOps;
    }
	@Wither @Getter
	private final boolean efficientOps;

	public PStackX<T> efficientOpsOn(){
		return this.withEfficientOps(true);
	}
	public PStackX<T> efficientOpsOff(){
		return this.withEfficientOps(false);
	}
	/**
	 * @param action
	 * @see java.lang.Iterable#forEach(java.util.function.Consumer)
	 */
	public void forEach(Consumer<? super T> action) {
		getStack().forEach(action);
	}

	/**
	 * @return
	 * @see org.pcollections.MapPSet#iterator()
	 */
	public Iterator<T> iterator() {
		return getStack().iterator();
	}

	/**
	 * @return
	 * @see org.pcollections.MapPSet#size()
	 */
	public int size() {
		return getStack().size();
	}

	/**
	 * @param e
	 * @return
	 * @see org.pcollections.MapPSet#contains(java.lang.Object)
	 */
	public boolean contains(Object e) {
		return getStack().contains(e);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.AbstractSet#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		return getStack().equals(o);
	}

	/**
	 * @param e
	 * @return
	 * @see org.pcollections.MapPSet#plus(java.lang.Object)
	 */
	public PStackX<T> plus(T e) {
		return this.withStack(getStack().plus(e));
	}

	/**
	 * @param e
	 * @return
	 * @see org.pcollections.MapPSet#minus(java.lang.Object)
	 */
	public  PStackX<T> minus(Object e) {
		return  this.withStack(getStack().minus(e));
	}

	/**
	 * @param list
	 * @return
	 * @see org.pcollections.MapPSet#plusAll(java.util.Collection)
	 */
	public  PStackX<T> plusAll(Collection<? extends T> list) {
		return   this.withStack(getStack().plusAll(list));
	}

	/**
	 * @param list
	 * @return
	 * @see org.pcollections.MapPSet#minusAll(java.util.Collection)
	 */
	public PStackX<T> minusAll(Collection<?> list) {
		return   this.withStack(getStack().minusAll(list));
	}

	/**
	 * @return
	 * @see java.util.AbstractCollection#isEmpty()
	 */
	public boolean isEmpty() {
		return getStack().isEmpty();
	}

	/**
	 * @return
	 * @see java.util.AbstractSet#hashCode()
	 */
	public int hashCode() {
		return getStack().hashCode();
	}

	/**
	 * @return
	 * @see java.util.AbstractCollection#toArray()
	 */
	public Object[] toArray() {
		return getStack().toArray();
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractSet#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection<?> c) {
		return getStack().removeAll(c);
	}

	/**
	 * @param a
	 * @return
	 * @see java.util.AbstractCollection#toArray(java.lang.Object[])
	 */
	public <T> T[] toArray(T[] a) {
		return getStack().toArray(a);
	}

	/**
	 * @param e
	 * @return
	 * @see java.util.AbstractCollection#add(java.lang.Object)
	 */
	public boolean add(T e) {
		return getStack().add(e);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.AbstractCollection#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		return getStack().remove(o);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection<?> c) {
		return getStack().containsAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#addAll(java.util.Collection)
	 */
	@Deprecated
	public boolean addAll(Collection<? extends T> c) {
		return getStack().addAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#retainAll(java.util.Collection)
	 */
	@Deprecated
	public boolean retainAll(Collection<?> c) {
		return getStack().retainAll(c);
	}

	/**
	 * 
	 * @see java.util.AbstractCollection#clear()
	 */
	@Deprecated
	public void clear() {
		getStack().clear();
	}

	/**
	 * @return
	 * @see java.util.AbstractCollection#toString()
	 */
	public String toString() {
		return getStack().toString();
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
	public PStackX<T> with(int i, T e) {
		return  this.withStack(getStack().with(i, e));
	}

	/**
	 * @param i
	 * @param e
	 * @return
	 * @see org.pcollections.PStack#plus(int, java.lang.Object)
	 */
	public PStackX<T> plus(int i, T e) {
		return this.withStack(getStack().plus(i, e));
	}

	/**
	 * @param i
	 * @param list
	 * @return
	 * @see org.pcollections.PStack#plusAll(int, java.util.Collection)
	 */
	public PStackX<T> plusAll(int i, Collection<? extends T> list) {
		return  this.withStack(getStack().plusAll(i, list));
	}

	/**
	 * @param i
	 * @return
	 * @see org.pcollections.PStack#minus(int)
	 */
	public PStackX<T> minus(int i) {
		return  this.withStack(getStack().minus(i));
	}

	/**
	 * @param start
	 * @param end
	 * @return
	 * @see org.pcollections.PStack#subList(int, int)
	 */
	public PStackX<T> subList(int start, int end) {
		return  this.withStack(getStack().subList(start, end));
	}

	/**
	 * @param start
	 * @return
	 * @see org.pcollections.PStack#subList(int)
	 */
	public PStackX<T> subList(int start) {
		return  this.withStack(getStack().subList(start));
	}

	
	/**
	 * @param index
	 * @param c
	 * @return
	 * @deprecated
	 * @see org.pcollections.PSequence#addAll(int, java.util.Collection)
	 */
	public boolean addAll(int index, Collection<? extends T> c) {
		return getStack().addAll(index, c);
	}

	/**
	 * @param index
	 * @param element
	 * @return
	 * @deprecated
	 * @see org.pcollections.PSequence#set(int, java.lang.Object)
	 */
	public T set(int index, T element) {
		return getStack().set(index, element);
	}

	/**
	 * @param index
	 * @param element
	 * @deprecated
	 * @see org.pcollections.PSequence#add(int, java.lang.Object)
	 */
	public void add(int index, T element) {
		getStack().add(index, element);
	}

	/**
	 * @param index
	 * @return
	 * @deprecated
	 * @see org.pcollections.PSequence#remove(int)
	 */
	public T remove(int index) {
		return getStack().remove(index);
	}

	/**
	 * @param operator
	 * @see java.util.List#replaceAll(java.util.function.UnaryOperator)
	 */
	public  void replaceAll(UnaryOperator<T> operator) {
		getStack().replaceAll(operator);
	}

	/**
	 * @param filter
	 * @return
	 * @see java.util.Collection#removeIf(java.util.function.Predicate)
	 */
	public boolean removeIf(Predicate<? super T> filter) {
		return getStack().removeIf(filter);
	}

	/**
	 * @param c
	 * @see java.util.List#sort(java.util.Comparator)
	 */
	public void sort(Comparator<? super T> c) {
		getStack().sort(c);
	}

	/**
	 * @return
	 * @see java.util.Collection#spliterator()
	 */
	public Spliterator<T> spliterator() {
		return getStack().spliterator();
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.List#get(int)
	 */
	public T get(int index) {
		return getStack().get(index);
	}

	

	/**
	 * @return
	 * @see java.util.Collection#parallelStream()
	 */
	public Stream<T> parallelStream() {
		return getStack().parallelStream();
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.List#indexOf(java.lang.Object)
	 */
	public int indexOf(Object o) {
		return getStack().indexOf(o);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.List#lastIndexOf(java.lang.Object)
	 */
	public int lastIndexOf(Object o) {
		return getStack().lastIndexOf(o);
	}

	/**
	 * @return
	 * @see java.util.List#listIterator()
	 */
	public ListIterator<T> listIterator() {
		return getStack().listIterator();
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.List#listIterator(int)
	 */
	public ListIterator<T> listIterator(int index) {
		return getStack().listIterator(index);
	}
    private PStack<T> getStack() {
        return lazy.get();
    }
    
    @Override
    public <X> PStackX<X> stream(Stream<X> stream){
        return new PStackXImpl<X>(stream,this.efficientOps);
    }

    public PStackX<T> withStack(PStack<T> stack){
        return new PStackXImpl<>(stack,this.efficientOps);
    }
    
    

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#stream()
     */
    @Override
    public ReactiveSeq<T> streamInternal() {
        return lazy.stream();
    }
   
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#stream()
     */
    @Override
    public ReactiveSeq<T> stream() {
        return ReactiveSeq.fromStream(lazy.get().stream());
    }
   
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    public PStackX<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {
       
        return (PStackX<T>)super.combine(predicate, op);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#reverse()
     */
    @Override
    public PStackX<T> reverse() {
       
        return(PStackX<T>)super.reverse();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#filter(java.util.function.Predicate)
     */
    @Override
    public PStackX<T> filter(Predicate<? super T> pred) {
       
        return (PStackX<T>)super.filter(pred);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#map(java.util.function.Function)
     */
    @Override
    public <R> PStackX<R> map(Function<? super T, ? extends R> mapper) {
       
        return (PStackX<R>)super.map(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#flatMap(java.util.function.Function)
     */
    @Override
    public <R> PStackX<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
       return (PStackX<R>)super.flatMap(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#limit(long)
     */
    @Override
    public PStackX<T> limit(long num) {
       return (PStackX<T>)super.limit(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#skip(long)
     */
    @Override
    public PStackX<T> skip(long num) {
       return (PStackX<T>)super.skip(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#takeRight(int)
     */
    @Override
    public PStackX<T> takeRight(int num) {
       return (PStackX<T>)super.takeRight(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#dropRight(int)
     */
    @Override
    public PStackX<T> dropRight(int num) {
       return (PStackX<T>)super.dropRight(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#takeWhile(java.util.function.Predicate)
     */
    @Override
    public PStackX<T> takeWhile(Predicate<? super T> p) {
       return (PStackX<T>)super.takeWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#dropWhile(java.util.function.Predicate)
     */
    @Override
    public PStackX<T> dropWhile(Predicate<? super T> p) {
       return (PStackX<T>)super.dropWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#takeUntil(java.util.function.Predicate)
     */
    @Override
    public PStackX<T> takeUntil(Predicate<? super T> p) {
       return (PStackX<T>)super.takeUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#dropUntil(java.util.function.Predicate)
     */
    @Override
    public PStackX<T> dropUntil(Predicate<? super T> p) {
       return(PStackX<T>)super.dropUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#trampoline(java.util.function.Function)
     */
    @Override
    public <R> PStackX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
       return (PStackX<R>)super.trampoline(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#slice(long, long)
     */
    @Override
    public PStackX<T> slice(long from, long to) {
       return (PStackX<T>)super.slice(from, to);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#grouped(int)
     */
    @Override
    public PStackX<ListX<T>> grouped(int groupSize) {
       
        return (PStackX<ListX<T>>)super.grouped(groupSize);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#grouped(java.util.function.Function, java.util.stream.Collector)
     */
    @Override
    public <K, A, D> PStackX<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier,
            Collector<? super T, A, D> downstream) {
       
        return (PStackX)super.grouped(classifier, downstream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#grouped(java.util.function.Function)
     */
    @Override
    public <K> PStackX<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier) {
       
        return (PStackX)super.grouped(classifier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#zip(java.lang.Iterable)
     */
    @Override
    public <U> PStackX<Tuple2<T, U>> zip(Iterable<U> other) {
       
        return (PStackX<Tuple2<T, U>>)super.zip(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    public <U, R> PStackX<R> zip(Iterable<U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
       
        return (PStackX<R>)super.zip(other, zipper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#sliding(int)
     */
    @Override
    public PStackX<ListX<T>> sliding(int windowSize) {
       
        return (PStackX<ListX<T>>)super.sliding(windowSize);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#sliding(int, int)
     */
    @Override
    public PStackX<ListX<T>> sliding(int windowSize, int increment) {
       
        return (PStackX<ListX<T>>)super.sliding(windowSize, increment);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#scanLeft(com.aol.cyclops.Monoid)
     */
    @Override
    public PStackX<T> scanLeft(Monoid<T> monoid) {
       
        return (PStackX<T>)super.scanLeft(monoid);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> PStackX<U> scanLeft(U seed, BiFunction<U, ? super T, U> function) {
       
        return (PStackX<U>) super.scanLeft(seed, function);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#scanRight(com.aol.cyclops.Monoid)
     */
    @Override
    public PStackX<T> scanRight(Monoid<T> monoid) {
       
        return (PStackX<T>)super.scanRight(monoid);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> PStackX<U> scanRight(U identity, BiFunction<? super T, U, U> combiner) {
       
        return (PStackX<U>)super.scanRight(identity, combiner);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#sorted(java.util.function.Function)
     */
    @Override
    public <U extends Comparable<? super U>> PStackX<T> sorted(Function<? super T, ? extends U> function) {
       
        return (PStackX<T>)super.sorted(function);
    }
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#plusLazy(java.lang.Object)
     */
    @Override
    public PStackX<T> plusLazy(T e) {
       
        return (PStackX<T>)super.plus(e);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#plusAllLazy(java.util.Collection)
     */
    @Override
    public PStackX<T> plusAllLazy(Collection<? extends T> list) {
       
        return (PStackX<T>)super.plusAll(list);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#minusLazy(java.lang.Object)
     */
    @Override
    public PStackX<T> minusLazy(Object e) {
       
        return (PStackX<T>)super.minus(e);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#minusAllLazy(java.util.Collection)
     */
    @Override
    public PStackX<T> minusAllLazy(Collection<?> list) {
       
        return (PStackX<T>)super.minusAll(list);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.MutableCollectionX#cycle(int)
     */
    @Override
    public PStackX<T> cycle(int times) {
        
        return stream(this.stream().cycle(times));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.MutableCollectionX#cycle(com.aol.cyclops.sequence.Monoid, int)
     */
    @Override
    public PStackX<T> cycle(Monoid<T> m, int times) {
        
        return stream(this.stream().cycle(m,times));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.MutableCollectionX#cycleWhile(java.util.function.Predicate)
     */
    @Override
    public PStackX<T> cycleWhile(Predicate<? super T> predicate) {
        
        return stream(this.stream().cycleWhile(predicate));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.MutableCollectionX#cycleUntil(java.util.function.Predicate)
     */
    @Override
    public PStackX<T> cycleUntil(Predicate<? super T> predicate) {
        
        return stream(this.stream().cycleUntil(predicate));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#zipStream(java.util.stream.Stream)
     */
    @Override
    public <U> PStackX<Tuple2<T, U>> zipStream(Stream<U> other) {
       
        return (PStackX<Tuple2<T, U>>)super.zipStream(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#zip(org.jooq.lambda.Seq)
     */
    @Override
    public <U> PStackX<Tuple2<T, U>> zip(Seq<U> other) {
       
        return (PStackX<Tuple2<T, U>>)super.zip(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <S, U> PStackX<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third) {
       
        return (PStackX)super.zip3(second, third);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <T2, T3, T4> PStackX<Tuple4<T, T2, T3, T4>> zip4(Stream<T2> second, Stream<T3> third,
            Stream<T4> fourth) {
       
        return (PStackX<Tuple4<T, T2, T3, T4>>)super.zip4(second, third, fourth);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#zipWithIndex()
     */
    @Override
    public PStackX<Tuple2<T, Long>> zipWithIndex() {
       
        return (PStackX<Tuple2<T, Long>>)super.zipWithIndex();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#distinct()
     */
    @Override
    public PStackX<T> distinct() {
       
        return (PStackX<T>)super.distinct();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#sorted()
     */
    @Override
    public PStackX<T> sorted() {
       
        return (PStackX<T>)super.sorted();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#sorted(java.util.Comparator)
     */
    @Override
    public PStackX<T> sorted(Comparator<? super T> c) {
       
        return (PStackX<T>)super.sorted(c);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#skipWhile(java.util.function.Predicate)
     */
    @Override
    public PStackX<T> skipWhile(Predicate<? super T> p) {
       
        return (PStackX<T>)super.skipWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#skipUntil(java.util.function.Predicate)
     */
    @Override
    public PStackX<T> skipUntil(Predicate<? super T> p) {
       
        return (PStackX<T>)super.skipUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#limitWhile(java.util.function.Predicate)
     */
    @Override
    public PStackX<T> limitWhile(Predicate<? super T> p) {
       
        return (PStackX<T>)super.limitWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#limitUntil(java.util.function.Predicate)
     */
    @Override
    public PStackX<T> limitUntil(Predicate<? super T> p) {
       
        return (PStackX<T>)super.limitUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#intersperse(java.lang.Object)
     */
    @Override
    public PStackX<T> intersperse(T value) {
       
        return (PStackX<T>)super.intersperse(value);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#shuffle()
     */
    @Override
    public PStackX<T> shuffle() {
       
        return (PStackX<T>)super.shuffle();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#skipLast(int)
     */
    @Override
    public PStackX<T> skipLast(int num) {
       
        return (PStackX<T>)super.skipLast(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#limitLast(int)
     */
    @Override
    public PStackX<T> limitLast(int num) {
       
        return (PStackX<T>)super.limitLast(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#onEmpty(java.lang.Object)
     */
    @Override
    public PStackX<T> onEmpty(T value) {
       
        return (PStackX<T>)super.onEmpty(value);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    public PStackX<T> onEmptyGet(Supplier<T> supplier) {
       
        return (PStackX<T>)super.onEmptyGet(supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    public <X extends Throwable> PStackX<T> onEmptyThrow(Supplier<X> supplier) {
       
        return (PStackX<T>)super.onEmptyThrow(supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#shuffle(java.util.Random)
     */
    @Override
    public PStackX<T> shuffle(Random random) {
       
        return (PStackX<T>)super.shuffle(random);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#ofType(java.lang.Class)
     */
    @Override
    public <U> PStackX<U> ofType(Class<U> type) {
       
        return (PStackX<U>)super.ofType(type);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#filterNot(java.util.function.Predicate)
     */
    @Override
    public PStackX<T> filterNot(Predicate<? super T> fn) {
       
        return (PStackX<T>)super.filterNot(fn);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#notNull()
     */
    @Override
    public PStackX<T> notNull() {
       
        return (PStackX<T>)super.notNull();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#removeAll(java.util.stream.Stream)
     */
    @Override
    public PStackX<T> removeAll(Stream<T> stream) {
       
        return (PStackX<T>)(super.removeAll(stream));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#removeAll(org.jooq.lambda.Seq)
     */
    @Override
    public PStackX<T> removeAll(Seq<T> stream) {
       
        return (PStackX<T>)super.removeAll(stream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#removeAll(java.lang.Iterable)
     */
    @Override
    public PStackX<T> removeAll(Iterable<T> it) {
       
        return (PStackX<T>)super.removeAll(it);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#removeAll(java.lang.Object[])
     */
    @Override
    public PStackX<T> removeAll(T... values) {
       
        return (PStackX<T>)super.removeAll(values);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#retainAll(java.lang.Iterable)
     */
    @Override
    public PStackX<T> retainAll(Iterable<T> it) {
       
        return (PStackX<T>)super.retainAll(it);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#retainAll(java.util.stream.Stream)
     */
    @Override
    public PStackX<T> retainAll(Stream<T> stream) {
       
        return (PStackX<T>)super.retainAll(stream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#retainAll(org.jooq.lambda.Seq)
     */
    @Override
    public PStackX<T> retainAll(Seq<T> stream) {
       
        return (PStackX<T>)super.retainAll(stream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#retainAll(java.lang.Object[])
     */
    @Override
    public PStackX<T> retainAll(T... values) {
       
        return (PStackX<T>)super.retainAll(values);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#cast(java.lang.Class)
     */
    @Override
    public <U> PStackX<U> cast(Class<U> type) {
       
        return (PStackX<U>)super.cast(type);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#patternMatch(java.util.function.Function, java.util.function.Supplier)
     */
    @Override
    public <R> PStackX<R> patternMatch(Function<CheckValue1<T, R>, CheckValue1<T, R>> case1,
            Supplier<? extends R> otherwise) {
       
        return (PStackX<R>)super.patternMatch(case1, otherwise);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#permutations()
     */
    @Override
    public PStackX<ReactiveSeq<T>> permutations() {
       
        return (PStackX<ReactiveSeq<T>>)super.permutations();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#combinations(int)
     */
    @Override
    public PStackX<ReactiveSeq<T>> combinations(int size) {
       
        return (PStackX<ReactiveSeq<T>>)super.combinations(size);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#combinations()
     */
    @Override
    public PStackX<ReactiveSeq<T>> combinations() {
       
        return (PStackX<ReactiveSeq<T>>)super.combinations();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#grouped(int, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> PStackX<C> grouped(int size, Supplier<C> supplier) {
       
        return (PStackX<C>)super.grouped(size, supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#groupedUntil(java.util.function.Predicate)
     */
    @Override
    public PStackX<ListX<T>> groupedUntil(Predicate<? super T> predicate) {
       
        return (PStackX<ListX<T>>)super.groupedUntil(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#groupedWhile(java.util.function.Predicate)
     */
    @Override
    public PStackX<ListX<T>> groupedWhile(Predicate<? super T> predicate) {
       
        return (PStackX<ListX<T>>)super.groupedWhile(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> PStackX<C> groupedWhile(Predicate<? super T> predicate,
            Supplier<C> factory) {
        
        return (PStackX<C>)super.groupedWhile(predicate, factory);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> PStackX<C> groupedUntil(Predicate<? super T> predicate,
            Supplier<C> factory) {
        
        return (PStackX<C>)super.groupedUntil(predicate, factory);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PStackX#groupedStatefullyWhile(java.util.function.BiPredicate)
     */
    @Override
    public PStackX<ListX<T>> groupedStatefullyWhile(BiPredicate<ListX<? super T>, ? super T> predicate) {
        
        return (PStackX<ListX<T>>)super.groupedStatefullyWhile(predicate);
    }
    /** PStackX methods **/

    /* Makes a defensive copy of this PStackX replacing the value at i with the specified element
     *  (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.MutableSequenceX#with(int, java.lang.Object)
     */
    public PStackX<T> withLazy(int i,T element){
        return stream( streamInternal().deleteBetween(i, i+1).insertAt(i,element) ) ;
    }
    @Override
    public <R> PStackX<R> emptyUnit(){
        if(isEfficientOps())
            return PStackX.<R>empty();
        return PStackX.<R>empty().efficientOpsOff();
    }
    @Override
    public <R> PStackX<R> unit(Collection<R> col){
        if(isEfficientOps())
            return PStackX.fromCollection(col);
        return PStackX.fromCollection(col).efficientOpsOff();
    }
    @Override
    public  <R> PStackX<R> unit(R value){
        if(isEfficientOps())
            return PStackX.singleton(value);
        return PStackX.singleton(value).efficientOpsOff();
       
    }
    @Override
    public <R> PStackX<R> unitIterator(Iterator<R> it){
        return PStackX.fromIterable(()->it);
    }
    
}
