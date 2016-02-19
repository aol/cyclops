package com.aol.cyclops.types.futurestream;

import static com.aol.cyclops.types.futurestream.NullValue.NULL;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import com.aol.cyclops.internal.react.stream.CloseableIterator;

public class LazyFutureStreamFunctions {

	/**
	 * Zip two streams into one.
	 * <p>
	 * <code>
	 * // (tuple(1, "a"), tuple(2, "b"), tuple(3, "c"))
	 * Seq.of(1, 2, 3).zip(Seq.of("a", "b", "c"))
	 * </code>
	 */
	static <T1, T2> Seq<Tuple2<T1, T2>> zip(Stream<T1> left, Stream<T2> right) {
		return zip(left, right, Tuple::tuple);
	}

	/**
	 * Zip two streams into one using a {@link BiFunction} to produce resulting
	 * values.
	 * <p>
	 * <code>
	 * // ("1:a", "2:b", "3:c")
	 * Seq.of(1, 2, 3).zip(Seq.of("a", "b", "c"), (i, s) -&gt; i + ":" + s)
	 * </code>
	 */
	static <T1, T2, R> Seq<R> zip(Stream<T1> left, Stream<T2> right,
			BiFunction<? super T1,? super T2,? extends R> zipper) {
		final Iterator<T1> it1 = left.iterator();
		final Iterator<T2> it2 = right.iterator();

		class Zip implements Iterator<R> {
			@Override
			public boolean hasNext() {
				if (!it1.hasNext()) {
					close(it2);
				}
				if (!it2.hasNext()) {
					close(it1);
				}
				return it1.hasNext() && it2.hasNext();
			}

			@Override
			public R next() {
				return zipper.apply(it1.next(), it2.next());
			}
		}

		return Seq.seq(new Zip()).onClose(()->{left.close(); right.close();});
	}

	static void close(Iterator it) {

		if (it instanceof CloseableIterator) {
			((CloseableIterator) it).close();
		}
	}

	/**
	 * Returns a stream limited to all elements for which a predicate evaluates
	 * to <code>true</code>.
	 * <p>
	 * <code>
	 * // (1, 2)
	 * Seq.of(1, 2, 3, 4, 5).limitWhile(i -&gt; i &lt; 3)
	 * </code>
	 */
	static <T> Seq<T> limitWhile(Stream<T> stream,
			Predicate<? super T> predicate) {
		return limitUntil(stream, predicate.negate());
	}



	/**
	 * Returns a stream ed to all elements for which a predicate evaluates to
	 * <code>true</code>.
	 * <p>
	 * <code>
	 * // (1, 2)
	 * Seq.of(1, 2, 3, 4, 5).limitUntil(i -&gt; i == 3)
	 * </code>
	 */
	@SuppressWarnings("unchecked")
	static <T> Seq<T> limitUntil(Stream<T> stream,
			Predicate<? super T> predicate) {
		final Iterator<T> it = stream.iterator();

		class LimitUntil implements Iterator<T> {
			T next = (T) NULL;
			boolean test = false;

			void test() {
				if (!test && next == NULL && it.hasNext()) {
					next = it.next();

					if (test = predicate.test(next)) {
						next = (T) NULL;
						close(it); // need to close any open queues
					}
				}
			}

			@Override
			public boolean hasNext() {
				test();
				return next != NULL;
			}

			@Override
			public T next() {
				if (next == NULL)
					throw new NoSuchElementException();

				try {
					return next;
				} finally {
					next = (T) NULL;
				}
			}
		}

		return Seq.seq(new LimitUntil()).onClose(()->{stream.close();});
	}
	
}
