package cyclops.data;

import com.oath.cyclops.hkt.DataWitness;
import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.matching.Deconstruct;
import com.oath.cyclops.types.persistent.PersistentCollection;
import cyclops.data.tuple.Tuple2;
import cyclops.function.Monoid;
import cyclops.reactive.ReactiveSeq;

import java.util.Comparator;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface NonEmptyImmutableList<T> extends Deconstruct.Deconstruct2<T,ImmutableList<T>>, ImmutableList<T>, ImmutableList.Some<T>{
    NonEmptyImmutableList<T> prepend(T value);

    @Override
    default NonEmptyImmutableList<T> replaceFirst(T currentElement, T newElement){
        return  (NonEmptyImmutableList<T> )ImmutableList.Some.super.replaceFirst(currentElement,newElement);
    }

    @Override
    NonEmptyImmutableList<T> insertAt(int pos, T... values);

    @Override
    NonEmptyImmutableList<T> plusAll(Iterable<? extends T> list);

    @Override
    NonEmptyImmutableList<T> plus(T value);

    @Override
    NonEmptyImmutableList<T> updateAt(int pos, T value);

    @Override
    NonEmptyImmutableList<T> insertAt(int pos, Iterable<? extends T> values);

    @Override
    NonEmptyImmutableList<T> insertAt(int i, T value);

    @Override
    NonEmptyImmutableList<T> prependAll(Iterable<? extends T> value);

    @Override
    NonEmptyImmutableList<T> append(T value);

    @Override
    NonEmptyImmutableList<T> appendAll(Iterable<? extends T> value);

    @Override
    NonEmptyImmutableList<T> reverse();


    <R> NonEmptyImmutableList<R> map(Function<? super T, ? extends R> fn);

    @Override
    NonEmptyImmutableList<T> peek(Consumer<? super T> c);

    @Override
    NonEmptyImmutableList<T> onEmpty(T value);

    @Override
    NonEmptyImmutableList<T> onEmptyGet(Supplier<? extends T> supplier);

    @Override
    NonEmptyImmutableList<ReactiveSeq<T>> permutations();

    @Override
    NonEmptyImmutableList<ReactiveSeq<T>> combinations(int size);

    @Override
    NonEmptyImmutableList<ReactiveSeq<T>> combinations();

    @Override
    NonEmptyImmutableList<Tuple2<T, Long>> zipWithIndex();

    @Override
    NonEmptyImmutableList<Seq<T>> sliding(int windowSize);

    @Override
    NonEmptyImmutableList<Seq<T>> sliding(int windowSize, int increment);

    @Override
    <C extends PersistentCollection<? super T>> NonEmptyImmutableList<C> grouped(int size, Supplier<C> supplier);

    @Override
    NonEmptyImmutableList<Vector<T>> groupedUntil(Predicate<? super T> predicate);

    @Override
    NonEmptyImmutableList<Vector<T>> groupedUntil(BiPredicate<Vector<? super T>, ? super T> predicate);

    @Override
    NonEmptyImmutableList<Vector<T>> groupedWhile(Predicate<? super T> predicate);

    @Override
    <C extends PersistentCollection<? super T>> NonEmptyImmutableList<C> groupedWhile(Predicate<? super T> predicate, Supplier<C> factory);

    @Override
    <C extends PersistentCollection<? super T>> NonEmptyImmutableList<C> groupedUntil(Predicate<? super T> predicate, Supplier<C> factory);

    @Override
    NonEmptyImmutableList<Vector<T>> grouped(int groupSize);

    @Override
    NonEmptyImmutableList<T> distinct();

    @Override
    NonEmptyImmutableList<T> scanLeft(Monoid<T> monoid);

    @Override
    <U> NonEmptyImmutableList<U> scanLeft(U seed, BiFunction<? super U, ? super T, ? extends U> function);

    @Override
    NonEmptyImmutableList<T> scanRight(Monoid<T> monoid);

    @Override
    <U> NonEmptyImmutableList<U> scanRight(U identity, BiFunction<? super T, ? super U, ? extends U> combiner);

    @Override
    NonEmptyImmutableList<T> sorted();

    @Override
    NonEmptyImmutableList<T> sorted(Comparator<? super T> c);

    @Override
    NonEmptyImmutableList<T> intersperse(T value);

    @Override
    NonEmptyImmutableList<T> shuffle();

    @Override
    NonEmptyImmutableList<T> shuffle(Random random);

    @Override
    NonEmptyImmutableList<T> prependStream(Stream<? extends T> stream);

    @Override
    NonEmptyImmutableList<T> appendAll(T... values);

    @Override
    NonEmptyImmutableList<T> prependAll(T... values);

    @Override
    NonEmptyImmutableList<T> insertStreamAt(int pos, Stream<T> stream);

    @Override
    <U extends Comparable<? super U>> NonEmptyImmutableList<T> sorted(Function<? super T, ? extends U> function);
}
