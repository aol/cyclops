package com.nurkiewicz.lazyseq;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.*;
import java.util.stream.*;

/**
 * @author Tomasz Nurkiewicz
 * @since 5/8/13, 9:09 PM
 */
class LazySeqStream<E> implements Stream<E> {

	private final LazySeq<E> underlying;

	LazySeqStream(LazySeq<E> underlying) {
		this.underlying = underlying;
	}

	@Override
	public Stream<E> filter(Predicate<? super E> predicate) {
		return underlying.filter(predicate).stream();
	}

	@Override
	public <R> Stream<R> map(Function<? super E, ? extends R> mapper) {
		return (Stream)underlying.map(mapper).stream();
	}

	@Override
	public <R> Stream<R> flatMap(Function<? super E, ? extends Stream<? extends R>> mapper) {
		return underlying.
				flatMap(e -> mapper.apply(e).collect(Collectors.<R>toList())).
				stream();
	}

	@Override
	public Stream<E> limit(long maxSize) {
		return underlying.limit(maxSize).stream();
	}

	@Override
	public Stream<E> skip(long n) {
		return underlying.drop(n).stream();
	}

	@Override
	public void forEach(Consumer<? super E> action) {
		underlying.forEach(action);
	}

	@Override
	public void forEachOrdered(Consumer<? super E> action) {
		underlying.forEach(action);
	}

	@Override
	public Object[] toArray() {
		final Object[] array = new Object[underlying.size()];
		copyToArray(array);
		return array;
	}

	@Override
	public <A> A[] toArray(IntFunction<A[]> generator) {
		final A[] array = generator.apply(underlying.size());
		copyToArray(array);
		return array;
	}

	private void copyToArray(Object[] array) {
		LazySeq<E> cur = underlying;
		for (int i = 0; i < array.length; ++i) {
			array[i] = cur.head();
			cur = cur.tail();
		}
	}

	@Override
	public E reduce(E identity, BinaryOperator<E> accumulator) {
		return underlying.reduce(identity, accumulator);
	}

	@Override
	public Optional<E> reduce(BinaryOperator<E> accumulator) {
		return underlying.reduce(accumulator);
	}

	@Override
	public <U> U reduce(U identity, BiFunction<U, ? super E, U> accumulator, BinaryOperator<U> combiner) {
		return underlying.reduce(identity, accumulator);
	}

	@Override
	public Optional<E> min(Comparator<? super E> comparator) {
		return underlying.min(comparator);
	}

	@Override
	public Optional<E> max(Comparator<? super E> comparator) {
		return underlying.max(comparator);
	}

	@Override
	public long count() {
		return underlying.size();
	}

	@Override
	public boolean anyMatch(Predicate<? super E> predicate) {
		return underlying.anyMatch(predicate);
	}

	@Override
	public boolean allMatch(Predicate<? super E> predicate) {
		return underlying.allMatch(predicate);
	}

	@Override
	public boolean noneMatch(Predicate<? super E> predicate) {
		return underlying.noneMatch(predicate);
	}

	@Override
	public Optional<E> findFirst() {
		return underlying.headOption();
	}

	@Override
	public Optional<E> findAny() {
		return underlying.headOption();
	}

	@Override
	public Iterator<E> iterator() {
		return underlying.iterator();
	}

	@Override
	public Spliterator<E> spliterator() {
		return Spliterators.spliteratorUnknownSize(this.iterator(),Spliterator.ORDERED);
	}

	@Override
	public boolean isParallel() {
		return false;
	}

	@Override
	public Stream<E> sequential() {
		return this;
	}

	@Override
	public Stream<E> parallel() {
		return this;
	}

	@Override
	public Stream<E> unordered() {
		return this;
	}

	@Override
	public IntStream mapToInt(ToIntFunction<? super E> mapper) {
		throw new UnsupportedOperationException("Not yet implemented: mapToInt");
	}

	@Override
	public LongStream mapToLong(ToLongFunction<? super E> mapper) {
		throw new UnsupportedOperationException("Not yet implemented: mapToLong");
	}

	@Override
	public DoubleStream mapToDouble(ToDoubleFunction<? super E> mapper) {
		throw new UnsupportedOperationException("Not yet implemented: mapToDouble");
	}

	@Override
	public IntStream flatMapToInt(Function<? super E, ? extends IntStream> mapper) {
		throw new UnsupportedOperationException("Not yet implemented: flatMapToInt");
	}

	@Override
	public LongStream flatMapToLong(Function<? super E, ? extends LongStream> mapper) {
		throw new UnsupportedOperationException("Not yet implemented: flatMapToLong");
	}

	@Override
	public DoubleStream flatMapToDouble(Function<? super E, ? extends DoubleStream> mapper) {
		throw new UnsupportedOperationException("Not yet implemented: flatMapToDouble");
	}

	@Override
	public Stream<E> distinct() {
		return underlying.distinct().stream();
	}

	@Override
	public Stream<E> sorted() {
		return underlying.sorted().stream();
	}

	@Override
	public Stream<E> sorted(Comparator<? super E> comparator) {
		return underlying.sorted(comparator).stream();
	}

	@Override
	public Stream<E> peek(Consumer<? super E> consumer) {
		return underlying.map(e -> {
			consumer.accept(e);
			return e;
		}).stream();
	}

	@Override
	public <R> R collect(Supplier<R> resultFactory, BiConsumer<R, ? super E> accumulator, BiConsumer<R, R> combiner) {
		R result = resultFactory.get();
		for (E element : underlying) {
			accumulator.accept(result, element);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R, A> R collect(Collector<? super E, A, R> collector) {
		if (collector instanceof DummyLazySeqCollector) {
			return (R) underlying;
		}
		A result = collector.supplier().get();
		for (E element : underlying) {
			collector.accumulator().accept(result, element);
		}
		return collector.finisher().apply(result);
	}

	@Override
	public Stream<E> onClose(Runnable closeHandler) {
		return this;
	}

	@Override
	public void close() {
		
	}
}
