package com.aol.cyclops.sequence.traits.lazy;



import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;

import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.jooq.lambda.tuple.Tuple5;
import org.jooq.lambda.tuple.Tuple6;
import org.jooq.lambda.tuple.Tuple7;
import org.jooq.lambda.tuple.Tuple8;

import com.aol.cyclops.collections.extensions.standard.ListX;
import com.aol.cyclops.collections.extensions.standard.MapX;
import com.aol.cyclops.collections.extensions.standard.SetX;
import com.aol.cyclops.control.Eval;


public interface LazyCollectable<T>{

	
	 /**
     * Collect this collectable into 2 {@link Collector}s.
     */
    default <R1, R2, A1, A2> Eval<Tuple2<R1, R2>> collect(
        Collector<? super T, A1, R1> collector1,
        Collector<? super T, A2, R2> collector2
    ) {
        return collect(Tuple.collectors(collector1, collector2));
    }

    /**
     * Collect this collectable into 3 {@link Collector}s.
     */
    default <R1, R2, R3, A1, A2, A3> Eval<Tuple3<R1, R2, R3>> collect(
        Collector<? super T, A1, R1> collector1,
        Collector<? super T, A2, R2> collector2,
        Collector<? super T, A3, R3> collector3
    ) {
        return collect(Tuple.collectors(collector1, collector2, collector3));
    }

    /**
     * Collect this collectable into 4 {@link Collector}s.
     */
    default <R1, R2, R3, R4, A1, A2, A3, A4> Eval<Tuple4<R1, R2, R3, R4>> collect(
        Collector<? super T, A1, R1> collector1,
        Collector<? super T, A2, R2> collector2,
        Collector<? super T, A3, R3> collector3,
        Collector<? super T, A4, R4> collector4
    ) {
        return collect(Tuple.collectors(collector1, collector2, collector3, collector4));
    }

    /**
     * Collect this collectable into 5 {@link Collector}s.
     */
    default <R1, R2, R3, R4, R5, A1, A2, A3, A4, A5> Eval<Tuple5<R1, R2, R3, R4, R5>> collect(
        Collector<? super T, A1, R1> collector1,
        Collector<? super T, A2, R2> collector2,
        Collector<? super T, A3, R3> collector3,
        Collector<? super T, A4, R4> collector4,
        Collector<? super T, A5, R5> collector5
    ) {
        return collect(Tuple.collectors(collector1, collector2, collector3, collector4, collector5));
    }

    /**
     * Collect this collectable into 6 {@link Collector}s.
     */
    default <R1, R2, R3, R4, R5, R6, A1, A2, A3, A4, A5, A6> Eval<Tuple6<R1, R2, R3, R4, R5, R6>> collect(
        Collector<? super T, A1, R1> collector1,
        Collector<? super T, A2, R2> collector2,
        Collector<? super T, A3, R3> collector3,
        Collector<? super T, A4, R4> collector4,
        Collector<? super T, A5, R5> collector5,
        Collector<? super T, A6, R6> collector6
    ) {
        return collect(Tuple.collectors(collector1, collector2, collector3, collector4, collector5, collector6));
    }

    /**
     * Collect this collectable into 7 {@link Collector}s.
     */
    default <R1, R2, R3, R4, R5, R6, R7, A1, A2, A3, A4, A5, A6, A7> Eval<Tuple7<R1, R2, R3, R4, R5, R6, R7>> collect(
        Collector<? super T, A1, R1> collector1,
        Collector<? super T, A2, R2> collector2,
        Collector<? super T, A3, R3> collector3,
        Collector<? super T, A4, R4> collector4,
        Collector<? super T, A5, R5> collector5,
        Collector<? super T, A6, R6> collector6,
        Collector<? super T, A7, R7> collector7
    ) {
        return collect(Tuple.collectors(collector1, collector2, collector3, collector4, collector5, collector6, collector7));
    }

    /**
     * Collect this collectable into 8 {@link Collector}s.
     */
    default <R1, R2, R3, R4, R5, R6, R7, R8, A1, A2, A3, A4, A5, A6, A7, A8> Eval<Tuple8<R1, R2, R3, R4, R5, R6, R7, R8>> collect(
        Collector<? super T, A1, R1> collector1,
        Collector<? super T, A2, R2> collector2,
        Collector<? super T, A3, R3> collector3,
        Collector<? super T, A4, R4> collector4,
        Collector<? super T, A5, R5> collector5,
        Collector<? super T, A6, R6> collector6,
        Collector<? super T, A7, R7> collector7,
        Collector<? super T, A8, R8> collector8
    ) {
        return collect(Tuple.collectors(collector1, collector2, collector3, collector4, collector5, collector6, collector7, collector8));
    }

	public <R, A> Eval<R> collect(Collector<? super T, A, R> collector);

	
	public Eval<Long> count();

	
	public Eval<Long> countDistinct();

	
	public <U> Eval<Long> countDistinctBy(Function<? super T, ? extends U> function);

	
	public Eval<Optional<T>> mode() ;

	
	public Eval<Optional<T>> sum() ;

	
	public <U> Eval<Optional<U>> sum(Function<? super T, ? extends U> function) ;

	
	public Eval<Integer> sumInt(ToIntFunction<? super T> function);

	
	public Eval<Long> sumLong(ToLongFunction<? super T> function);

	
	public Eval<Double> sumDouble(ToDoubleFunction<? super T> function);

	
	public Eval<Optional<T>> avg() ;

	
	public <U> Eval<Optional<U>> avg(Function<? super T, ? extends U> function) ;

	
	
	public Eval<Optional<T>> min() ;

	
	public Eval<Optional<T>> min(Comparator<? super T> comparator) ;

	
	public <U extends Comparable<? super U>> Eval<Optional<U>> min(Function<? super T, ? extends U> function) ;

	
	public <U> Eval<Optional<U>> min(Function<? super T, ? extends U> function, Comparator<? super U> comparator) ;

	
	public <U extends Comparable<? super U>> Eval<Optional<T>> minBy(Function<? super T, ? extends U> function) ;

	
	public <U> Eval<Optional<T>> minBy(Function<? super T, ? extends U> function, Comparator<? super U> comparator) ;

	
	public Eval<Optional<T>> max() ;

	
	public Eval<Optional<T>> max(Comparator<? super T> comparator) ;

	
	public <U extends Comparable<? super U>> Eval<Optional<U>> max(Function<? super T, ? extends U> function) ;

	
	public <U> Eval<Optional<U>> max(Function<? super T, ? extends U> function, Comparator<? super U> comparator) ;

	
	public <U extends Comparable<? super U>> Eval<Optional<T>> maxBy(Function<? super T, ? extends U> function) ;

	
	public <U> Eval<Optional<T>> maxBy(Function<? super T, ? extends U> function, Comparator<? super U> comparator) ;

	
	public Eval<Optional<T>> median() ;

	
	public Eval<Optional<T>> median(Comparator<? super T> comparator) ;

	
	public <U extends Comparable<? super U>> Eval<Optional<T>> medianBy(Function<? super T, ? extends U> function) ;

	
	public <U> Eval<Optional<T>> medianBy(Function<? super T, ? extends U> function, Comparator<? super U> comparator) ;

	
	public Eval<Optional<T>> percentile(double percentile) ;

	
	public Eval<Optional<T>> percentile(double percentile, Comparator<? super T> comparator) ;

	
	public <U extends Comparable<? super U>> Eval<Optional<T>> percentileBy(double percentile, Function<? super T, ? extends U> function) ;

	
	public <U> Eval<Optional<T>> percentileBy(double percentile, Function<? super T, ? extends U> function, Comparator<? super U> comparator) ;

	
	public Eval<Boolean> allMatch(Predicate<? super T> predicate);

	
	public Eval<Boolean> anyMatch(Predicate<? super T> predicate);

	
	public Eval<Boolean> noneMatch(Predicate<? super T> predicate);

	
	public Eval<ListX<T>> toList() ;

	
	public <L extends List<T>> Eval<L> toList(Supplier<L> factory) ;

	
	public Eval<SetX<T>> toSet() ;

	
	public <S extends Set<T>> Eval<S> toSet(Supplier<S> factory) ;

	
	public <C extends Collection<T>> Eval<C> toCollection(Supplier<C> factory) ;

	
	public <K, V> Eval<MapX<K, V>> toMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) ;

	
	

}
