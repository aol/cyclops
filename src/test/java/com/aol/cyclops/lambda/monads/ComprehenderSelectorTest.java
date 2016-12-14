package com.aol.cyclops.lambda.monads;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.BaseStream;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.internal.comprehensions.comprehenders.ListComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.StreamAdapter;

public class ComprehenderSelectorTest {

    @Test
    public void listXTest(){
        assertThat(new ComprehenderSelector().selectComprehender(
                ListX.of(1,2,3)),instanceOf(ListComprehender.class));
    }
	@Test
	public void testSelectComprehenderClass() {
		assertThat(new ComprehenderSelector().selectComprehender(MyStream.class),instanceOf(StreamAdapter.class));
	}

	@Test
	public void testSelectComprehenderClassStream() {
		Stream stream = Arrays.asList(1,2,3).stream();
		assertThat(new ComprehenderSelector().selectComprehender(stream.getClass()),instanceOf(StreamAdapter.class));
	}

	@Test
	public void testSelectComprehenderObject() {
		assertThat(new ComprehenderSelector().selectComprehender(new MyStream()),instanceOf(StreamAdapter.class));
	}
	@Test
	public void testSelectComprehenderClassObject() {
		Stream stream = Arrays.asList(1,2,3).stream();
		assertThat(new ComprehenderSelector().selectComprehender(stream),instanceOf(StreamAdapter.class));
	}
	static class MyStream implements Stream{

		@Override
		public Iterator iterator() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Spliterator spliterator() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isParallel() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public BaseStream sequential() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BaseStream parallel() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BaseStream unordered() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BaseStream onClose(Runnable closeHandler) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void close() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Stream filter(Predicate predicate) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Stream map(Function mapper) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IntStream mapToInt(ToIntFunction mapper) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public LongStream mapToLong(ToLongFunction mapper) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public DoubleStream mapToDouble(ToDoubleFunction mapper) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Stream flatMap(Function mapper) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IntStream flatMapToInt(Function mapper) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public LongStream flatMapToLong(Function mapper) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public DoubleStream flatMapToDouble(Function mapper) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Stream distinct() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Stream sorted() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Stream sorted(Comparator comparator) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Stream peek(Consumer action) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Stream limit(long maxSize) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Stream skip(long n) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void forEach(Consumer action) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void forEachOrdered(Consumer action) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Object[] toArray() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object[] toArray(IntFunction generator) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object reduce(Object identity, BinaryOperator accumulator) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Optional reduce(BinaryOperator accumulator) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object reduce(Object identity, BiFunction accumulator,
				BinaryOperator combiner) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object collect(Supplier supplier, BiConsumer accumulator,
				BiConsumer combiner) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object collect(Collector collector) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Optional min(Comparator comparator) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Optional max(Comparator comparator) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long count() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public boolean anyMatch(Predicate predicate) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean allMatch(Predicate predicate) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean noneMatch(Predicate predicate) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Optional findFirst() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Optional findAny() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}

}
