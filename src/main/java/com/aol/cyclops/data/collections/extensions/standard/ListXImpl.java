package com.aol.cyclops.data.collections.extensions.standard;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
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

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.data.collections.extensions.CollectionX;

import lombok.AllArgsConstructor;
import lombok.Getter;



public class ListXImpl<T> extends AbstractMutableCollectionX<T> implements ListX<T> {
	
	private final LazyCollection<T,List<T>> lazy;
	@Getter
	private final Collector<T,?,List<T>> collector;
	
	public ListXImpl(List<T> list,Collector<T,?,List<T>> collector){
	    this.lazy = new LazyCollection<>(list,null,collector);
	    this.collector=  collector;
	}
	
	public ListXImpl(List<T> list){
		
		this.collector = ListX.defaultCollector();
		this.lazy = new LazyCollection<T,List<T>>(list,null,collector);
	}
	private ListXImpl(Stream<T> stream){
        
        this.collector = ListX.defaultCollector();
        this.lazy = new LazyCollection<>(null,stream,collector);
    }
	public ListXImpl(){
		this.collector = ListX.defaultCollector();
		this.lazy = new LazyCollection<>((List)this.collector.supplier().get(),null,collector);
	}
	
	/**
	 * @param action
	 * @see java.lang.Iterable#forEach(java.util.function.Consumer)
	 */
	public void forEach(Consumer<? super T> action) {
		getList().forEach(action);
	}

	/**
	 * @return
	 * @see org.pcollections.MapPSet#iterator()
	 */
	public Iterator<T> iterator() {
		return getList().iterator();
	}

	/**
	 * @return
	 * @see org.pcollections.MapPSet#size()
	 */
	public int size() {
		return getList().size();
	}

	/**
	 * @param e
	 * @return
	 * @see org.pcollections.MapPSet#contains(java.lang.Object)
	 */
	public boolean contains(Object e) {
		return getList().contains(e);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.AbstractSet#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		return getList().equals(o);
	}



	/**
	 * @return
	 * @see java.util.AbstractCollection#isEmpty()
	 */
	public boolean isEmpty() {
		return getList().isEmpty();
	}

	/**
	 * @return
	 * @see java.util.AbstractSet#hashCode()
	 */
	public int hashCode() {
		return getList().hashCode();
	}

	/**
	 * @return
	 * @see java.util.AbstractCollection#toArray()
	 */
	public Object[] toArray() {
		return getList().toArray();
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractSet#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection<?> c) {
		return getList().removeAll(c);
	}

	/**
	 * @param a
	 * @return
	 * @see java.util.AbstractCollection#toArray(java.lang.Object[])
	 */
	public <T> T[] toArray(T[] a) {
		return getList().toArray(a);
	}

	/**
	 * @param e
	 * @return
	 * @see java.util.AbstractCollection#add(java.lang.Object)
	 */
	public boolean add(T e) {
		return getList().add(e);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.AbstractCollection#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		return getList().remove(o);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection<?> c) {
		return getList().containsAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection<? extends T> c) {
		return getList().addAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#retainAll(java.util.Collection)
	 */
	public boolean retainAll(Collection<?> c) {
		return getList().retainAll(c);
	}

	/**
	 * 
	 * @see java.util.AbstractCollection#clear()
	 */
	public void clear() {
		getList().clear();
	}

	/**
	 * @return
	 * @see java.util.AbstractCollection#toString()
	 */
	public String toString() {
		return getList().toString();
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
	 * @param index
	 * @param c
	 * @return
	 * @see java.util.List#addAll(int, java.util.Collection)
	 */
	public boolean addAll(int index, Collection<? extends T> c) {
		return getList().addAll(index, c);
	}
	/**
	 * @param operator
	 * @see java.util.List#replaceAll(java.util.function.UnaryOperator)
	 */
	public void replaceAll(UnaryOperator<T> operator) {
		getList().replaceAll(operator);
	}
	/**
	 * @param filter
	 * @return
	 * @see java.util.Collection#removeIf(java.util.function.Predicate)
	 */
	public  boolean removeIf(Predicate<? super T> filter) {
		return getList().removeIf(filter);
	}
	/**
	 * @param c
	 * @see java.util.List#sort(java.util.Comparator)
	 */
	public  void sort(Comparator<? super T> c) {
		getList().sort(c);
	}
	/**
	 * @param index
	 * @return
	 * @see java.util.List#get(int)
	 */
	public T get(int index) {
		return getList().get(index);
	}
	/**
	 * @param index
	 * @param element
	 * @return
	 * @see java.util.List#set(int, java.lang.Object)
	 */
	public T set(int index, T element) {
		return getList().set(index, element);
	}
	/**
	 * @param index
	 * @param element
	 * @see java.util.List#add(int, java.lang.Object)
	 */
	public void add(int index, T element) {
		getList().add(index, element);
	}
	
	/**
	 * @param index
	 * @return
	 * @see java.util.List#remove(int)
	 */
	public T remove(int index) {
		return getList().remove(index);
	}
	/**
	 * @return
	 * @see java.util.Collection#parallelStream()
	 */
	public  Stream<T> parallelStream() {
		return getList().parallelStream();
	}
	/**
	 * @param o
	 * @return
	 * @see java.util.List#indexOf(java.lang.Object)
	 */
	public int indexOf(Object o) {
		return getList().indexOf(o);
	}
	/**
	 * @param o
	 * @return
	 * @see java.util.List#lastIndexOf(java.lang.Object)
	 */
	public int lastIndexOf(Object o) {
		return getList().lastIndexOf(o);
	}
	/**
	 * @return
	 * @see java.util.List#listIterator()
	 */
	public ListIterator<T> listIterator() {
		return getList().listIterator();
	}
	/**
	 * @param index
	 * @return
	 * @see java.util.List#listIterator(int)
	 */
	public ListIterator<T> listIterator(int index) {
		return getList().listIterator(index);
	}
	/**
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 * @see java.util.List#subList(int, int)
	 */
	public ListX<T> subList(int fromIndex, int toIndex) {
		return new ListXImpl<T>(getList().subList(fromIndex, toIndex),getCollector());
	}
	/**
	 * @return
	 * @see java.util.List#spliterator()
	 */
	public Spliterator<T> spliterator() {
		return getList().spliterator();
	}
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(T o) {
		if(o instanceof List){
			List l = (List)o;
			if(this.size()==l.size()){
				Iterator i1 = iterator();
				Iterator i2 = l.iterator();
				if(i1.hasNext()){
					if(i2.hasNext()){
						int comp = Comparator.<Comparable>naturalOrder().compare((Comparable)i1.next(), (Comparable)i2.next());
						if(comp!=0)
							return comp;
					}
					return 1;
				}
				else{
					if(i2.hasNext())
						return -1;
					else
						return 0;
				}
			}
			return this.size() - ((List)o).size();
		}
		else
			return 1;
			
			
	}
    private List<T> getList() {
        return lazy.get();
    }
    @Override
	public <X> ListX<X> stream(Stream<X> stream){
	    return new ListXImpl<X>(stream);
	}
    @Override
    public CollectionX<T> immutable(){
        return new ListXImpl<>(Collections.unmodifiableList(getList()),ListX.immutableCollector());
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#stream()
     */
    @Override
    public ReactiveSeq<T> stream() {
        return ReactiveSeq.fromStream(lazy.get().stream());
    }
    @Override
    public ReactiveSeq<T> streamInternal() {
        return lazy.stream();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    public ListX<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {
       
        return (ListX<T>)super.combine(predicate, op);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#reverse()
     */
    @Override
    public ListX<T> reverse() {
       
        return(ListX<T>)super.reverse();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#filter(java.util.function.Predicate)
     */
    @Override
    public ListX<T> filter(Predicate<? super T> pred) {
       
        return (ListX<T>)super.filter(pred);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#map(java.util.function.Function)
     */
    @Override
    public <R> ListX<R> map(Function<? super T, ? extends R> mapper) {
       
        return (ListX<R>)super.map(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#flatMap(java.util.function.Function)
     */
    @Override
    public <R> ListX<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
       return (ListX<R>)super.flatMap(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#limit(long)
     */
    @Override
    public ListX<T> limit(long num) {
       return (ListX<T>)super.limit(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#skip(long)
     */
    @Override
    public ListX<T> skip(long num) {
       return (ListX<T>)super.skip(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#takeRight(int)
     */
    @Override
    public ListX<T> takeRight(int num) {
       return (ListX<T>)super.takeRight(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#dropRight(int)
     */
    @Override
    public ListX<T> dropRight(int num) {
       return (ListX<T>)super.dropRight(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#takeWhile(java.util.function.Predicate)
     */
    @Override
    public ListX<T> takeWhile(Predicate<? super T> p) {
       return (ListX<T>)super.takeWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#dropWhile(java.util.function.Predicate)
     */
    @Override
    public ListX<T> dropWhile(Predicate<? super T> p) {
       return (ListX<T>)super.dropWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#takeUntil(java.util.function.Predicate)
     */
    @Override
    public ListX<T> takeUntil(Predicate<? super T> p) {
       return (ListX<T>)super.takeUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#dropUntil(java.util.function.Predicate)
     */
    @Override
    public ListX<T> dropUntil(Predicate<? super T> p) {
       return(ListX<T>)super.dropUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#trampoline(java.util.function.Function)
     */
    @Override
    public <R> ListX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
       return (ListX<R>)super.trampoline(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#slice(long, long)
     */
    @Override
    public ListX<T> slice(long from, long to) {
       return (ListX<T>)super.slice(from, to);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#grouped(int)
     */
    @Override
    public ListX<ListX<T>> grouped(int groupSize) {
       
        return (ListX<ListX<T>>)super.grouped(groupSize);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#grouped(java.util.function.Function, java.util.stream.Collector)
     */
    @Override
    public <K, A, D> ListX<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier,
            Collector<? super T, A, D> downstream) {
       
        return (ListX)super.grouped(classifier, downstream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#grouped(java.util.function.Function)
     */
    @Override
    public <K> ListX<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier) {
       
        return (ListX)super.grouped(classifier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#zip(java.lang.Iterable)
     */
    @Override
    public <U> ListX<Tuple2<T, U>> zip(Iterable<U> other) {
       
        return (ListX<Tuple2<T, U>>)super.zip(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    public <U, R> ListX<R> zip(Iterable<U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
       
        return (ListX<R>)super.zip(other, zipper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#sliding(int)
     */
    @Override
    public ListX<ListX<T>> sliding(int windowSize) {
       
        return (ListX<ListX<T>>)super.sliding(windowSize);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#sliding(int, int)
     */
    @Override
    public ListX<ListX<T>> sliding(int windowSize, int increment) {
       
        return (ListX<ListX<T>>)super.sliding(windowSize, increment);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#scanLeft(com.aol.cyclops.Monoid)
     */
    @Override
    public ListX<T> scanLeft(Monoid<T> monoid) {
       
        return (ListX<T>)super.scanLeft(monoid);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> ListX<U> scanLeft(U seed, BiFunction<U, ? super T, U> function) {
       
        return (ListX<U>) super.scanLeft(seed, function);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#scanRight(com.aol.cyclops.Monoid)
     */
    @Override
    public ListX<T> scanRight(Monoid<T> monoid) {
       
        return (ListX<T>)super.scanRight(monoid);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> ListX<U> scanRight(U identity, BiFunction<? super T, U, U> combiner) {
       
        return (ListX<U>)super.scanRight(identity, combiner);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#sorted(java.util.function.Function)
     */
    @Override
    public <U extends Comparable<? super U>> ListX<T> sorted(Function<? super T, ? extends U> function) {
       
        return (ListX<T>)super.sorted(function);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#plus(java.lang.Object)
     */
    @Override
    public ListX<T> plus(T e) {
       
        return (ListX<T>)super.plus(e);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#plusAll(java.util.Collection)
     */
    @Override
    public ListX<T> plusAll(Collection<? extends T> list) {
       
        return (ListX<T>)super.plusAll(list);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#minus(java.lang.Object)
     */
    @Override
    public ListX<T> minus(Object e) {
       
        return (ListX<T>)super.minus(e);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#minusAll(java.util.Collection)
     */
    @Override
    public ListX<T> minusAll(Collection<?> list) {
       
        return (ListX<T>)super.minusAll(list);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#plusLazy(java.lang.Object)
     */
    @Override
    public ListX<T> plusLazy(T e) {
       
        return (ListX<T>)super.plus(e);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#plusAllLazy(java.util.Collection)
     */
    @Override
    public ListX<T> plusAllLazy(Collection<? extends T> list) {
       
        return (ListX<T>)super.plusAll(list);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#minusLazy(java.lang.Object)
     */
    @Override
    public ListX<T> minusLazy(Object e) {
       
        return (ListX<T>)super.minus(e);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#minusAllLazy(java.util.Collection)
     */
    @Override
    public ListX<T> minusAllLazy(Collection<?> list) {
       
        return (ListX<T>)super.minusAll(list);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#cycle(int)
     */
    @Override
    public ListX<T> cycle(int times) {
       
        return (ListX<T>)super.cycle(times);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#cycle(com.aol.cyclops.Monoid, int)
     */
    @Override
    public ListX<T> cycle(Monoid<T> m, int times) {
       
        return (ListX<T>)super.cycle(m, times);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#cycleWhile(java.util.function.Predicate)
     */
    @Override
    public ListX<T> cycleWhile(Predicate<? super T> predicate) {
       
        return (ListX<T>)super.cycleWhile(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#cycleUntil(java.util.function.Predicate)
     */
    @Override
    public ListX<T> cycleUntil(Predicate<? super T> predicate) {
       
        return (ListX<T>)super.cycleUntil(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#zipStream(java.util.stream.Stream)
     */
    @Override
    public <U> ListX<Tuple2<T, U>> zipStream(Stream<U> other) {
       
        return (ListX<Tuple2<T, U>>)super.zipStream(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#zip(org.jooq.lambda.Seq)
     */
    @Override
    public <U> ListX<Tuple2<T, U>> zip(Seq<U> other) {
       
        return (ListX<Tuple2<T, U>>)super.zip(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <S, U> ListX<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third) {
       
        return (ListX)super.zip3(second, third);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <T2, T3, T4> ListX<Tuple4<T, T2, T3, T4>> zip4(Stream<T2> second, Stream<T3> third,
            Stream<T4> fourth) {
       
        return (ListX<Tuple4<T, T2, T3, T4>>)super.zip4(second, third, fourth);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#zipWithIndex()
     */
    @Override
    public ListX<Tuple2<T, Long>> zipWithIndex() {
       
        return (ListX<Tuple2<T, Long>>)super.zipWithIndex();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#distinct()
     */
    @Override
    public ListX<T> distinct() {
       
        return (ListX<T>)super.distinct();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#sorted()
     */
    @Override
    public ListX<T> sorted() {
       
        return (ListX<T>)super.sorted();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#sorted(java.util.Comparator)
     */
    @Override
    public ListX<T> sorted(Comparator<? super T> c) {
       
        return (ListX<T>)super.sorted(c);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#skipWhile(java.util.function.Predicate)
     */
    @Override
    public ListX<T> skipWhile(Predicate<? super T> p) {
       
        return (ListX<T>)super.skipWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#skipUntil(java.util.function.Predicate)
     */
    @Override
    public ListX<T> skipUntil(Predicate<? super T> p) {
       
        return (ListX<T>)super.skipUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#limitWhile(java.util.function.Predicate)
     */
    @Override
    public ListX<T> limitWhile(Predicate<? super T> p) {
       
        return (ListX<T>)super.limitWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#limitUntil(java.util.function.Predicate)
     */
    @Override
    public ListX<T> limitUntil(Predicate<? super T> p) {
       
        return (ListX<T>)super.limitUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#intersperse(java.lang.Object)
     */
    @Override
    public ListX<T> intersperse(T value) {
       
        return (ListX<T>)super.intersperse(value);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#shuffle()
     */
    @Override
    public ListX<T> shuffle() {
       
        return (ListX<T>)super.shuffle();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#skipLast(int)
     */
    @Override
    public ListX<T> skipLast(int num) {
       
        return (ListX<T>)super.skipLast(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#limitLast(int)
     */
    @Override
    public ListX<T> limitLast(int num) {
       
        return (ListX<T>)super.limitLast(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#onEmpty(java.lang.Object)
     */
    @Override
    public ListX<T> onEmpty(T value) {
       
        return (ListX<T>)super.onEmpty(value);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    public ListX<T> onEmptyGet(Supplier<T> supplier) {
       
        return (ListX<T>)super.onEmptyGet(supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    public <X extends Throwable> ListX<T> onEmptyThrow(Supplier<X> supplier) {
       
        return (ListX<T>)super.onEmptyThrow(supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#shuffle(java.util.Random)
     */
    @Override
    public ListX<T> shuffle(Random random) {
       
        return (ListX<T>)super.shuffle(random);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#ofType(java.lang.Class)
     */
    @Override
    public <U> ListX<U> ofType(Class<U> type) {
       
        return (ListX<U>)super.ofType(type);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#filterNot(java.util.function.Predicate)
     */
    @Override
    public ListX<T> filterNot(Predicate<? super T> fn) {
       
        return (ListX<T>)super.filterNot(fn);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#notNull()
     */
    @Override
    public ListX<T> notNull() {
       
        return (ListX<T>)super.notNull();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#removeAll(java.util.stream.Stream)
     */
    @Override
    public ListX<T> removeAll(Stream<T> stream) {
       
        return (ListX<T>)(super.removeAll(stream));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#removeAll(org.jooq.lambda.Seq)
     */
    @Override
    public ListX<T> removeAll(Seq<T> stream) {
       
        return (ListX<T>)super.removeAll(stream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#removeAll(java.lang.Iterable)
     */
    @Override
    public ListX<T> removeAll(Iterable<T> it) {
       
        return (ListX<T>)super.removeAll(it);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#removeAll(java.lang.Object[])
     */
    @Override
    public ListX<T> removeAll(T... values) {
       
        return (ListX<T>)super.removeAll(values);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#retainAll(java.lang.Iterable)
     */
    @Override
    public ListX<T> retainAll(Iterable<T> it) {
       
        return (ListX<T>)super.retainAll(it);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#retainAll(java.util.stream.Stream)
     */
    @Override
    public ListX<T> retainAll(Stream<T> stream) {
       
        return (ListX<T>)super.retainAll(stream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#retainAll(org.jooq.lambda.Seq)
     */
    @Override
    public ListX<T> retainAll(Seq<T> stream) {
       
        return (ListX<T>)super.retainAll(stream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#retainAll(java.lang.Object[])
     */
    @Override
    public ListX<T> retainAll(T... values) {
       
        return (ListX<T>)super.retainAll(values);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#cast(java.lang.Class)
     */
    @Override
    public <U> ListX<U> cast(Class<U> type) {
       
        return (ListX<U>)super.cast(type);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#patternMatch(java.util.function.Function, java.util.function.Supplier)
     */
    @Override
    public <R> ListX<R> patternMatch(Function<CheckValue1<T, R>, CheckValue1<T, R>> case1,
            Supplier<? extends R> otherwise) {
       
        return (ListX<R>)super.patternMatch(case1, otherwise);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#permutations()
     */
    @Override
    public ListX<ReactiveSeq<T>> permutations() {
       
        return (ListX<ReactiveSeq<T>>)super.permutations();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#combinations(int)
     */
    @Override
    public ListX<ReactiveSeq<T>> combinations(int size) {
       
        return (ListX<ReactiveSeq<T>>)super.combinations(size);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#combinations()
     */
    @Override
    public ListX<ReactiveSeq<T>> combinations() {
       
        return (ListX<ReactiveSeq<T>>)super.combinations();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#grouped(int, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> ListX<C> grouped(int size, Supplier<C> supplier) {
       
        return (ListX<C>)super.grouped(size, supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#groupedUntil(java.util.function.Predicate)
     */
    @Override
    public ListX<ListX<T>> groupedUntil(Predicate<? super T> predicate) {
       
        return (ListX<ListX<T>>)super.groupedUntil(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#groupedWhile(java.util.function.Predicate)
     */
    @Override
    public ListX<ListX<T>> groupedWhile(Predicate<? super T> predicate) {
       
        return (ListX<ListX<T>>)super.groupedWhile(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> ListX<C> groupedWhile(Predicate<? super T> predicate,
            Supplier<C> factory) {
        
        return (ListX<C>)super.groupedWhile(predicate, factory);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> ListX<C> groupedUntil(Predicate<? super T> predicate,
            Supplier<C> factory) {
        
        return (ListX<C>)super.groupedUntil(predicate, factory);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.ListX#groupedStatefullyWhile(java.util.function.BiPredicate)
     */
    @Override
    public ListX<ListX<T>> groupedStatefullyWhile(BiPredicate<ListX<? super T>, ? super T> predicate) {
        
        return (ListX<ListX<T>>)super.groupedStatefullyWhile(predicate);
    }
    /** ListX methods **/

    /* Makes a defensive copy of this ListX replacing the value at i with the specified element
     *  (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableSequenceX#with(int, java.lang.Object)
     */
    public ListX<T> with(int i,T element){
        return stream( streamInternal().deleteBetween(i, i+1).insertAt(i,element) ) ;
    }
    @Override
    public ListX<T> minus(int pos){
        remove(pos);
        return this;
    }
    @Override
    public ListX<T> plus(int i, T e){
        add(i,e);
        return this;
    }
    
    @Override
    public ListX<T> plusAll(int i, Collection<? extends T> list){
        addAll(i,list);
        return this;
    }
    
    @Override
    public <R> ListX<R> unit(Collection<R> col){
        return ListX.fromIterable(col);
    }
    @Override
    public  <R> ListX<R> unit(R value){
        return ListX.singleton(value);
    }
    @Override
    public <R> ListX<R> unitIterator(Iterator<R> it){
        return ListX.fromIterable(()->it);
    }

}
