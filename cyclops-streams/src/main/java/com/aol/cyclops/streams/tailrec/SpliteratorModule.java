package com.aol.cyclops.streams.tailrec;

import static org.jooq.lambda.tuple.Tuple.tuple;

import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import com.aol.cyclops.closures.mutable.Mutable;
import com.aol.cyclops.functions.inference.Lambda;
import com.aol.cyclops.trampoline.Trampoline;

public interface SpliteratorModule {

	static interface StackFreeingSpliterator<T> {

		Tuple2<Boolean, ? extends Spliterator<T>> advance(Consumer<? super T> action);

	}

	static class TCOPrependOperator<T> extends AbstractSpliterator<T> implements StackFreeingSpliterator<T> {

		private final T[] values;
		private final Spliterator<T> split;

		public TCOPrependOperator(Spliterator<T> split, T... values) {
			super(Long.MAX_VALUE, ORDERED);
			this.split = split;
			this.values = values;

		}

		@Override
		public boolean tryAdvance(Consumer<? super T> action) {
			throw new UnsupportedOperationException();
		}

		private int pos;

		@Override
		public Tuple2<Boolean, ? extends Spliterator<T>> advance(Consumer<? super T> action) {
			return pos >= values.length ? tuple(true, split) : Lambda.s(() -> {
				action.accept(values[pos++]);
				return tuple(true, this);
			}).get();

		}
	}

	static final class RecursiveOperator<T, U> extends AbstractSpliterator<U> implements StackFreeingSpliterator<U> {

		private final BiFunction<? super T, ? super TailRec<T>, ? extends TailRec<U>> mapper;

		private final Spliterator<T> split;
		private Spliterator<U> active;

		public RecursiveOperator(Spliterator<T> split, BiFunction<? super T, ? super TailRec<T>, ? extends TailRec<U>> mapper) {
			super(Long.MAX_VALUE, ORDERED);
			this.split = split;
			this.mapper = mapper;

		}

		private boolean isFinished;

		@Override
		public Tuple2<Boolean, Spliterator<U>> advance(Consumer<? super U> action) {
			return isFinished ? tuple(false, active) : tuple(isFinished = true, active = initializeActive());
		}

		@Override
		public boolean tryAdvance(Consumer<? super U> action) {
			if (isFinished)
				return false;
			active = initializeActive();
			Tuple2<Boolean, ? extends Spliterator<U>> more = iterativelyAdvance(active, action).result();

			return more.v1 ? (active = more.v2) != null : !(isFinished = true);
		}

		private Spliterator<U> initializeActive() {
			if (active != null)
				return active;
			Mutable<T> head = Mutable.of(null);
			iterativelyAdvance(split, head).result();
			return mapper.apply(head.get(), new TailRec<>(split)).getStream().spliterator();

		}

		private <T1> Trampoline<Tuple2<Boolean, ? extends Spliterator<T1>>> handleStackFreeSpliterator(StackFreeingSpliterator<T1> current,
				Consumer<? super T1> action) {
			Tuple2<Boolean, ? extends Spliterator<T1>> advance = current.advance(action);
			return isFinishedOrSameSpliterator(current, advance) ? Trampoline.done(Tuple.tuple(true, advance.v2)) : 
																	Trampoline.more(() -> iterativelyAdvance(advance.v2, action));
		}

		private <T1> boolean isFinishedOrSameSpliterator(StackFreeingSpliterator<T1> current, Tuple2<Boolean, ? extends Spliterator<T1>> advance) {
			return !advance.v1 || advance.v2 == current;
		}

		@SuppressWarnings("unchecked")
		private <T1> Trampoline<Tuple2<Boolean, ? extends Spliterator<T1>>> iterativelyAdvance(final Spliterator<T1> current, final Consumer<? super T1> action) {

			return isStackFreeingSpliterator(current) ? handleStackFreeSpliterator((StackFreeingSpliterator<T1>) current, action) : 
														Trampoline.done(Tuple.tuple(current.tryAdvance(action), current));

		}

		private <T1> boolean isStackFreeingSpliterator(final Spliterator<T1> target) {
			return target instanceof StackFreeingSpliterator;
		}

	}
}
