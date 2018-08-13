package cyclops.monads.transformers;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.oath.cyclops.types.functor.ReactiveTransformable;
import com.oath.cyclops.types.persistent.PersistentCollection;
import com.oath.cyclops.types.traversable.RecoverableTraversable;
import cyclops.data.Seq;
import cyclops.data.Vector;
import cyclops.control.Maybe;
import com.oath.cyclops.types.traversable.IterableX;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import cyclops.monads.WitnessType;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;

import cyclops.function.Monoid;
import cyclops.reactive.ReactiveSeq;
import com.oath.cyclops.data.collections.extensions.IndexedSequenceX;
import cyclops.reactive.collections.mutable.ListX;
import com.oath.cyclops.types.foldable.To;
import com.oath.cyclops.types.traversable.Traversable;
import com.oath.cyclops.anym.transformers.FoldableTransformerSeq;
import org.reactivestreams.Publisher;

/**
 * Monad Transformer for Java Lists
 *
 * ListT allows the deeply wrapped List to be manipulating within it's nest /contained context
 *
 * <pre>
 *     {@code
 *       ListT<optional,Integer> streamT = ListT.ofList(AnyM.fromOptional(Optional.of(Arrays.asList(10))));
 *       AnyM<optional, IndexedSequenceX<Integer>> anyM = listT.unwrap();
 *
         Optional<IndexedSequenceX<Integer>> opt = Witness.optional(anyM);
         Optional<LinkedList<Integer>> list = opt.map(s -> s.toX(Converters::LinkedList));
 *     }
 *
 *
 * </pre>
 *
 * @author johnmcclean
 *
 * @param <T> Type of data stored inside the nest Lists
 */
public class ListT<W extends WitnessType<W>,T> implements To<ListT<W,T>>,
                                                           RecoverableTraversable<T>,
                                                            ReactiveTransformable<T>,
                                                          FoldableTransformerSeq<W,T> {

    final AnyM<W,IndexedSequenceX<T>> run;



    private ListT(final AnyM<W,? extends IndexedSequenceX<T>> run) {
        this.run = AnyM.narrow(run);
    }




    /**
     * @return The wrapped AnyM
     */
    public AnyM<W,IndexedSequenceX<T>> unwrap() {
        return run;
    }
    public <R> R unwrapTo(Function<? super AnyM<W,IndexedSequenceX<T>>,? extends R> fn) {
        return unwrap().to(fn);
    }

    /**
     * Peek at the current value of the List
     * <pre>
     * {@code
     *    ListT.of(AnyM.fromStream(Arrays.asList(10))
     *             .peek(System.out::println);
     *
     *     //prints 10
     * }
     * </pre>
     *
     * @param peek  Consumer to accept current value of List
     * @return ListT with peek call
     */
    @Override
    public ListT<W,T> peek(final Consumer<? super T> peek) {
        return map(a -> {
            peek.accept(a);
            return a;
        });

    }

    /**
     * Filter the wrapped List
     * <pre>
     * {@code
     *    ListT.of(AnyM.fromStream(Arrays.asList(10,11))
     *             .filter(t->t!=10);
     *
     *     //ListT<AnyM<Stream<List[11]>>>
     * }
     * </pre>
     * @param test Predicate to filter the wrapped List
     * @return ListT that applies the provided filter
     */
    @Override
    public ListT<W,T> filter(final Predicate<? super T> test) {
        return of(run.map(seq -> seq.filter(test)));
    }

    /**
     * Map the wrapped List
     *
     * <pre>
     * {@code
     *  ListT.of(AnyM.fromStream(Arrays.asList(10))
     *             .map(t->t=t+1);
     *
     *
     *  //ListT<AnyM<Stream<List[11]>>>
     * }
     * </pre>
     *
     * @param f Mapping function for the wrapped List
     * @return ListT that applies the transform function to the wrapped List
     */
    @Override
    public <B> ListT<W,B> map(final Function<? super T, ? extends B> f) {
        return of(run.map(o -> o.map(f)));
    }

    @Override
    public <B> ListT<W,B> flatMap(final Function<? super T, ? extends Iterable<? extends B>> f) {
        return new ListT<W,B>(
                               run.map(o -> o.concatMap(f)));

    }

    /**
     * Flat Map the wrapped List
      * <pre>
     * {@code
     *  ListT.of(AnyM.fromStream(Arrays.asList(10))
     *             .flatMap(t->List.zero();
     *
     *
     *  //ListT<AnyM<Stream<List.zero>>>
     * }
     * </pre>
     * @param f FlatMap function
     * @return ListT that applies the flatMap function to the wrapped List
     */
    public <B> ListT<W,B> flatMapT(final Function<? super T, ListT<W,B>> f) {

        return of(run.map(list -> list.concatMap(a -> f.apply(a).run.stream())
                                      .concatMap(a -> a.stream())));
    }





    /**
     * Construct an ListT from an AnyM that contains a monad type that contains type other than List
     * The values in the underlying monad will be mapped to List<A>
     *
     * @param anyM AnyM that doesn't contain a monad wrapping an List
     * @return ListT
     */
    public static <W extends WitnessType<W>,A> ListT<W,A> fromAnyM(final AnyM<W,A> anyM) {
        AnyM<W, ListX<A>> y = anyM.map(i->ListX.of(i));
        return of(y);
    }

    /**
     * Construct an ListT from an AnyM that wraps a monad containing  Lists
     *
     * @param monads AnyM that contains a monad wrapping an List
     * @return ListT
     */
    public static <W extends WitnessType<W>,A> ListT<W,A> of(final AnyM<W,? extends IndexedSequenceX<A>> monads) {
        return new ListT<>(
                              monads);
    }
    public static <W extends WitnessType<W>,A> ListT<W,A> ofList(final AnyM<W,? extends List<A>> monads) {
        return new ListT<>(
                              monads.map(ListX::fromIterable));
    }
    public static <A> ListT<Witness.stream,A> fromStream(final Stream<? extends IndexedSequenceX<A>> nested) {
        return of(AnyM.fromStream(nested));
    }
    public static <A> ListT<Witness.reactiveSeq,A> fromStream(final ReactiveSeq<? extends IndexedSequenceX<A>> nested) {
        return of(AnyM.fromStream(nested));
    }
    public static <A> ListT<Witness.optional,A> fromOptional(final Optional<? extends IndexedSequenceX<A>> nested) {
        return of(AnyM.fromOptional(nested));
    }
    public static <A> ListT<Witness.maybe,A> fromMaybe(final Maybe<? extends IndexedSequenceX<A>> nested) {
        return of(AnyM.fromMaybe(nested));
    }
    public static <A> ListT<Witness.list,A> fromList(final List<? extends IndexedSequenceX<A>> nested) {
        return of(AnyM.fromList(nested));
    }
    public static <A> ListT<Witness.set,A> fromSet(final Set<? extends IndexedSequenceX<A>> nested) {
        return of(AnyM.fromSet(nested));
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("ListT[%s]",  run.unwrap().toString());

    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.Pure#unit(java.lang.Object)
     */
    public <T> ListT<W,T> unit(final T unit) {
        return of(run.unit(ListX.of(unit)));
    }

    @Override
    public ReactiveSeq<T> stream() {
        return run.stream()
                  .concatMap(e -> e);
    }

    @Override
    public Iterator<T> iterator() {
        return stream().iterator();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.stream.CyclopsCollectable#collectors()

    @Override
    public Collectable<T> collectors() {
       return this;
    } */
    @Override
    public <R> ListT<W,R> unitIterable(final Iterable<R> it) {
        return of(run.unitIterable(it)
                     .map(i -> ListX.of(i)));
    }

    @Override
    public <R> ListT<W,R> empty() {
        return of(run.empty());
    }

    @Override
    public AnyM<W,? extends IterableX<T>> nestedFoldables() {
        return run;

    }

    @Override
    public AnyM<W,? extends IterableX<T>> nestedCollectables() {
        return run;

    }

    @Override
    public <T> ListT<W,T> unitAnyM(final AnyM<W,Traversable<T>> traversable) {

        return of((AnyM) traversable.map(t -> ListX.fromIterable(t)));
    }

    @Override
    public AnyM<W,? extends IterableX<T>> transformerStream() {

        return run;
    }

    public static <W extends WitnessType<W>,T> ListT<W,T> emptyList(W witness) {
        return of(witness.<W>adapter().unit(ListX.empty()));
    }

    @Override
    public boolean isSeqPresent() {
        return !run.isEmpty();
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    public ListT<W,T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {

        return (ListT<W,T>) FoldableTransformerSeq.super.combine(predicate, op);
    }
    @Override
    public ListT<W,T> combine(final Monoid<T> op, final BiPredicate<? super T, ? super T> predicate) {
        return (ListT<W,T>)FoldableTransformerSeq.super.combine(op,predicate);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#cycle(int)
     */
    @Override
    public ListT<W,T> cycle(final long times) {

        return (ListT<W,T>) FoldableTransformerSeq.super.cycle(times);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#cycle(cyclops2.function.Monoid, int)
     */
    @Override
    public ListT<W,T> cycle(final Monoid<T> m, final long times) {

        return (ListT<W,T>) FoldableTransformerSeq.super.cycle(m, times);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#cycleWhile(java.util.function.Predicate)
     */
    @Override
    public ListT<W,T> cycleWhile(final Predicate<? super T> predicate) {

        return (ListT<W,T>) FoldableTransformerSeq.super.cycleWhile(predicate);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#cycleUntil(java.util.function.Predicate)
     */
    @Override
    public ListT<W,T> cycleUntil(final Predicate<? super T> predicate) {

        return (ListT<W,T>) FoldableTransformerSeq.super.cycleUntil(predicate);
    }


    @Override
    public <U, R> ListT<W,R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (ListT<W,R>) FoldableTransformerSeq.super.zip(other, zipper);
    }


    @Override
    public <U, R> ListT<W,R> zipWithStream(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (ListT<W,R>) FoldableTransformerSeq.super.zipWithStream(other, zipper);
    }




    @Override
    public <U> ListT<W,Tuple2<T, U>> zipWithStream(final Stream<? extends U> other) {

        return (ListT) FoldableTransformerSeq.super.zipWithStream(other);
    }


    @Override
    public <U> ListT<W,Tuple2<T, U>> zip(final Iterable<? extends U> other) {

        return (ListT) FoldableTransformerSeq.super.zip(other);
    }



    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <S, U> ListT<W,Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {

        return (ListT) FoldableTransformerSeq.super.zip3(second, third);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <T2, T3, T4> ListT<W,Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
            final Iterable<? extends T4> fourth) {

        return (ListT) FoldableTransformerSeq.super.zip4(second, third, fourth);
    }


    @Override
    public ListT<W,Tuple2<T, Long>> zipWithIndex() {

        return (ListT<W,Tuple2<T, Long>>) FoldableTransformerSeq.super.zipWithIndex();
    }

    @Override
    public ListT<W,Seq<T>> sliding(final int windowSize) {

        return (ListT<W,Seq<T>>) FoldableTransformerSeq.super.sliding(windowSize);
    }


    @Override
    public ListT<W,Seq<T>> sliding(final int windowSize, final int increment) {

        return (ListT<W,Seq<T>>) FoldableTransformerSeq.super.sliding(windowSize, increment);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#grouped(int, java.util.function.Supplier)
     */
    @Override
    public <C extends PersistentCollection<? super T>> ListT<W,C> grouped(final int size, final Supplier<C> supplier) {

        return (ListT<W,C>) FoldableTransformerSeq.super.grouped(size, supplier);
    }


    @Override
    public ListT<W,Vector<T>> groupedUntil(final Predicate<? super T> predicate) {

        return (ListT<W,Vector<T>>) FoldableTransformerSeq.super.groupedUntil(predicate);
    }


    @Override
    public ListT<W,Vector<T>> groupedUntil(final BiPredicate<Vector<? super T>, ? super T> predicate) {

        return (ListT<W,Vector<T>>) FoldableTransformerSeq.super.groupedUntil(predicate);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#groupedWhile(java.util.function.Predicate)
     */
    @Override
    public ListT<W,Vector<T>> groupedWhile(final Predicate<? super T> predicate) {

        return (ListT<W,Vector<T>>) FoldableTransformerSeq.super.groupedWhile(predicate);
    }


    @Override
    public <C extends PersistentCollection<? super T>> ListT<W,C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (ListT<W,C>) FoldableTransformerSeq.super.groupedWhile(predicate, factory);
    }


    @Override
    public <C extends PersistentCollection<? super T>> ListT<W,C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (ListT<W,C>) FoldableTransformerSeq.super.groupedUntil(predicate, factory);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#grouped(int)
     */
    @Override
    public ListT<W,Vector<T>> grouped(final int groupSize) {

        return (ListT<W,Vector<T>>) FoldableTransformerSeq.super.grouped(groupSize);
    }


    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#distinct()
     */
    @Override
    public ListT<W,T> distinct() {

        return (ListT<W,T>) FoldableTransformerSeq.super.distinct();
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#scanLeft(cyclops2.function.Monoid)
     */
    @Override
    public ListT<W,T> scanLeft(final Monoid<T> monoid) {

        return (ListT<W,T>) FoldableTransformerSeq.super.scanLeft(monoid);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> ListT<W,U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {

        return (ListT<W,U>) FoldableTransformerSeq.super.scanLeft(seed, function);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#scanRight(cyclops2.function.Monoid)
     */
    @Override
    public ListT<W,T> scanRight(final Monoid<T> monoid) {

        return (ListT<W,T>) FoldableTransformerSeq.super.scanRight(monoid);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> ListT<W,U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {

        return (ListT<W,U>) FoldableTransformerSeq.super.scanRight(identity, combiner);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#sorted()
     */
    @Override
    public ListT<W,T> sorted() {

        return (ListT<W,T>) FoldableTransformerSeq.super.sorted();
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#sorted(java.util.Comparator)
     */
    @Override
    public ListT<W,T> sorted(final Comparator<? super T> c) {

        return (ListT<W,T>) FoldableTransformerSeq.super.sorted(c);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#takeWhile(java.util.function.Predicate)
     */
    @Override
    public ListT<W,T> takeWhile(final Predicate<? super T> p) {

        return (ListT<W,T>) FoldableTransformerSeq.super.takeWhile(p);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#dropWhile(java.util.function.Predicate)
     */
    @Override
    public ListT<W,T> dropWhile(final Predicate<? super T> p) {

        return (ListT<W,T>) FoldableTransformerSeq.super.dropWhile(p);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#takeUntil(java.util.function.Predicate)
     */
    @Override
    public ListT<W,T> takeUntil(final Predicate<? super T> p) {

        return (ListT<W,T>) FoldableTransformerSeq.super.takeUntil(p);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#dropUntil(java.util.function.Predicate)
     */
    @Override
    public ListT<W,T> dropUntil(final Predicate<? super T> p) {

        return (ListT<W,T>) FoldableTransformerSeq.super.dropUntil(p);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#dropRight(int)
     */
    @Override
    public ListT<W,T> dropRight(final int num) {

        return (ListT<W,T>) FoldableTransformerSeq.super.dropRight(num);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#takeRight(int)
     */
    @Override
    public ListT<W,T> takeRight(final int num) {

        return (ListT<W,T>) FoldableTransformerSeq.super.takeRight(num);
    }




    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#intersperse(java.lang.Object)
     */
    @Override
    public ListT<W,T> intersperse(final T value) {

        return (ListT<W,T>) FoldableTransformerSeq.super.intersperse(value);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#reverse()
     */
    @Override
    public ListT<W,T> reverse() {

        return (ListT<W,T>) FoldableTransformerSeq.super.reverse();
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#shuffle()
     */
    @Override
    public ListT<W,T> shuffle() {

        return (ListT<W,T>) FoldableTransformerSeq.super.shuffle();
    }


    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#onEmpty(java.lang.Object)
     */
    @Override
    public ListT<W,T> onEmpty(final T value) {

        return (ListT<W,T>) FoldableTransformerSeq.super.onEmpty(value);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    public ListT<W,T> onEmptyGet(final Supplier<? extends T> supplier) {

        return (ListT<W,T>) FoldableTransformerSeq.super.onEmptyGet(supplier);
    }



    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#shuffle(java.util.Random)
     */
    @Override
    public ListT<W,T> shuffle(final Random random) {

        return (ListT<W,T>) FoldableTransformerSeq.super.shuffle(random);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#slice(long, long)
     */
    @Override
    public ListT<W,T> slice(final long from, final long to) {

        return (ListT<W,T>) FoldableTransformerSeq.super.slice(from, to);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ListT#sorted(java.util.function.Function)
     */
    @Override
    public <U extends Comparable<? super U>> ListT<W,T> sorted(final Function<? super T, ? extends U> function) {
        return (ListT) FoldableTransformerSeq.super.sorted(function);
    }

    @Override
    public int hashCode() {
        return run.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof ListT) {
            return run.equals(((ListT) o).run);
        }
        return false;
    }



    public <T2, R1, R2, R3, R> ListT<W,R> forEach4M(Function<? super T, ? extends ListT<W,R1>> value1,
                                                    BiFunction<? super T, ? super R1, ? extends ListT<W,R2>> value2,
                                                    Function3<? super T, ? super R1, ? super R2, ? extends ListT<W,R3>> value3,
                                                    Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return this.flatMapT(in->value1.apply(in)
                .flatMapT(in2-> value2.apply(in,in2)
                        .flatMapT(in3->value3.apply(in,in2,in3)
                                .map(in4->yieldingFunction.apply(in,in2,in3,in4)))));

    }
    public <T2, R1, R2, R3, R> ListT<W,R> forEach4M(Function<? super T, ? extends ListT<W,R1>> value1,
                                                    BiFunction<? super T, ? super R1, ? extends ListT<W,R2>> value2,
                                                    Function3<? super T, ? super R1, ? super R2, ? extends ListT<W,R3>> value3,
                                                    Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                    Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return this.flatMapT(in->value1.apply(in)
                .flatMapT(in2-> value2.apply(in,in2)
                        .flatMapT(in3->value3.apply(in,in2,in3)
                                .filter(in4->filterFunction.apply(in,in2,in3,in4))
                                .map(in4->yieldingFunction.apply(in,in2,in3,in4)))));

    }

    public <T2, R1, R2, R> ListT<W,R> forEach3M(Function<? super T, ? extends ListT<W,R1>> value1,
                                                BiFunction<? super T, ? super R1, ? extends ListT<W,R2>> value2,
                                                Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return this.flatMapT(in->value1.apply(in).flatMapT(in2-> value2.apply(in,in2)
                .map(in3->yieldingFunction.apply(in,in2,in3))));

    }

    public <T2, R1, R2, R> ListT<W,R> forEach3M(Function<? super T, ? extends ListT<W,R1>> value1,
                                                BiFunction<? super T, ? super R1, ? extends ListT<W,R2>> value2,
                                                Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                                                Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return this.flatMapT(in->value1.apply(in).flatMapT(in2-> value2.apply(in,in2).filter(in3->filterFunction.apply(in,in2,in3))
                .map(in3->yieldingFunction.apply(in,in2,in3))));

    }
    public <R1, R> ListT<W,R> forEach2M(Function<? super T, ? extends ListT<W,R1>> value1,
                                        BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {


        return this.flatMapT(in->value1.apply(in)
                .map(in2->yieldingFunction.apply(in,in2)));
    }

    public <R1, R> ListT<W,R> forEach2M(Function<? super T, ? extends ListT<W,R1>> value1,
                                        BiFunction<? super T, ? super R1, Boolean> filterFunction,
                                        BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {


        return this.flatMapT(in->value1.apply(in)
                .filter(in2->filterFunction.apply(in,in2))
                .map(in2->yieldingFunction.apply(in,in2)));
    }


    @Override
    public ListT<W,T> prependStream(Stream<? extends T> stream) {
        return (ListT) FoldableTransformerSeq.super.prependStream(stream);
    }

    @Override
    public ListT<W,T> appendAll(T... values) {
        return (ListT) FoldableTransformerSeq.super.appendAll(values);
    }

    @Override
    public ListT<W,T> append(T value) {
        return (ListT) FoldableTransformerSeq.super.append(value);
    }

    @Override
    public ListT<W,T> prepend(T value) {
        return (ListT) FoldableTransformerSeq.super.prepend(value);
    }

    @Override
    public ListT<W,T> prependAll(T... values) {
        return (ListT) FoldableTransformerSeq.super.prependAll(values);
    }

    @Override
    public ListT<W,T> insertAt(int pos, T... values) {
        return (ListT) FoldableTransformerSeq.super.insertAt(pos,values);
    }

    @Override
    public ListT<W,T> deleteBetween(int start, int end) {
        return (ListT) FoldableTransformerSeq.super.deleteBetween(start,end);
    }

    @Override
    public ListT<W,T> insertStreamAt(int pos, Stream<T> stream) {
        return (ListT) FoldableTransformerSeq.super.insertStreamAt(pos,stream);
    }


  @Override
    public <T2, R> ListT<W,R> zip(BiFunction<? super T, ? super T2, ? extends R> fn, Publisher<? extends T2> publisher) {
        return (ListT) FoldableTransformerSeq.super.zip(fn, publisher);
    }

    @Override
    public <U> ListT<W,Tuple2<T, U>> zipWithPublisher(Publisher<? extends U> other) {
        return (ListT) FoldableTransformerSeq.super.zipWithPublisher(other);
    }

    @Override
    public <S, U, R> ListT<W,R> zip3(Iterable<? extends S> second, Iterable<? extends U> third, Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (ListT) FoldableTransformerSeq.super.zip3(second,third,fn3);
    }

    @Override
    public <T2, T3, T4, R> ListT<W,R> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth, Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (ListT) FoldableTransformerSeq.super.zip4(second,third,fourth,fn);
    }


    @Override
    public ListT<W,T> removeStream(final Stream<? extends T> stream) {
        return (ListT) FoldableTransformerSeq.super.removeStream(stream);
    }


    @Override
    public <U> ListT<W,U> ofType(final Class<? extends U> type) {
        return (ListT) FoldableTransformerSeq.super.ofType(type);
    }

    @Override
    public ListT<W,T> removeAll(final Iterable<? extends T> it) {
        return (ListT) FoldableTransformerSeq.super.removeAll(it);
    }


    @Override
    public ListT<W,T> removeAll(final T... values) {
        return (ListT) FoldableTransformerSeq.super.removeAll(values);
    }


    @Override
    public ListT<W,T> filterNot(final Predicate<? super T> predicate) {
        return (ListT) FoldableTransformerSeq.super.filterNot(predicate);
    }



    @Override
    public ListT<W,T> retainAll(final Iterable<? extends T> it) {
        return (ListT) FoldableTransformerSeq.super.retainAll(it);
    }

    @Override
    public ListT<W,T> notNull() {
        return (ListT) FoldableTransformerSeq.super.notNull();
    }

    @Override
    public ListT<W,T> retainStream(final Stream<? extends T> stream) {
        return (ListT) FoldableTransformerSeq.super.retainStream(stream);
    }

    @Override
    public ListT<W,T> retainAll(final T... values) {
        return (ListT) FoldableTransformerSeq.super.retainAll(values);
    }




    @Override
    public <R> ListT<W,R> retry(final Function<? super T, ? extends R> fn) {
        return (ListT) ReactiveTransformable.super.retry(fn);
    }

    @Override
    public <R> ListT<W,R> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (ListT) ReactiveTransformable.super.retry(fn,retries,delay,timeUnit);
    }



    @Override
    public ListT<W,T> drop(final long num) {
        return drop(num);
    }

    @Override
    public ListT<W,T> take(final long num) {
        return take(num);
    }


    @Override
    public ListT<W,T> recover(final Function<? super Throwable, ? extends T> fn) {
        AnyM<W, Traversable<T>> zipped = transformerStream().map(s -> s.stream().recover(fn));
        return unitAnyM(zipped);
    }

    @Override
    public <EX extends Throwable> ListT<W,T> recover(Class<EX> exceptionClass, final Function<? super EX, ? extends T> fn) {
        AnyM<W, Traversable<T>> zipped = transformerStream().map(s -> s.stream().recover(exceptionClass,fn));
        return unitAnyM(zipped);
    }

}
