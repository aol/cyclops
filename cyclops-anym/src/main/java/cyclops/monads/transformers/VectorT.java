package cyclops.monads.transformers;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.oath.cyclops.types.persistent.PersistentCollection;
import com.oath.cyclops.types.persistent.PersistentList;
import cyclops.data.Seq;
import cyclops.data.Vector;
import cyclops.control.Maybe;
import com.oath.cyclops.types.traversable.IterableX;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import cyclops.monads.Witness.list;
import cyclops.monads.WitnessType;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;

import cyclops.function.Monoid;
import cyclops.reactive.ReactiveSeq;
import com.oath.cyclops.types.foldable.To;
import com.oath.cyclops.types.traversable.Traversable;
import com.oath.cyclops.anym.transformers.FoldableTransformerSeq;
import org.reactivestreams.Publisher;


public class VectorT<W extends WitnessType<W>,T> implements To<VectorT<W,T>>,
                                                          FoldableTransformerSeq<W,T> {

    final AnyM<W,Vector<T>> run;



    private VectorT(final AnyM<W,? extends Vector<T>> run) {
        this.run = AnyM.narrow(run);
    }


    public AnyM<W,Vector<T>> unwrap() {
        return run;
    }

    public <R> R unwrapTo(Function<? super AnyM<W,Vector<T>>,? extends R> fn) {
        return unwrap().to(fn);
    }


    @Override
    public VectorT<W,T> peek(final Consumer<? super T> peek) {
        return map(a -> {
            peek.accept(a);
            return a;
        });

    }


    @Override
    public VectorT<W,T> filter(final Predicate<? super T> test) {
        return of(run.map(seq -> seq.filter(test)));
    }


    @Override
    public <B> VectorT<W,B> map(final Function<? super T, ? extends B> f) {
        return of(run.map(o -> o.map(f)));
    }

    @Override
    public <B> VectorT<W,B> flatMap(final Function<? super T, ? extends Iterable<? extends B>> f) {
        return new VectorT<W,B>(
                               run.map(o -> o.concatMap(f)));

    }


    public <B> VectorT<W,B> flatMapT(final Function<? super T, VectorT<W,B>> f) {

        return of(run.map(list -> list.concatMap(a -> f.apply(a).run.stream())
                                      .concatMap(a -> a.stream())));
    }




    public static <W extends WitnessType<W>,A> VectorT<W,A> fromAnyM(final AnyM<W,A> anyM) {

        return of( anyM.map(Vector::of));
    }


    public static <W extends WitnessType<W>,A> VectorT<W,A> of(final AnyM<W,? extends Vector<A>> monads) {
        return new VectorT<>(
                              monads);
    }
    public static <W extends WitnessType<W>,A> VectorT<W,A> ofList(final AnyM<W,? extends PersistentList<A>> monads) {
        return new VectorT<>(
                              monads.map(Vector::fromIterable));
    }
    public static <A> VectorT<Witness.stream,A> fromStream(final Stream<? extends Vector<A>> nested) {
        return of(AnyM.fromStream(nested));
    }
    public static <A> VectorT<Witness.reactiveSeq,A> fromStream(final ReactiveSeq<? extends Vector<A>> nested) {
        return of(AnyM.fromStream(nested));
    }
    public static <A> VectorT<Witness.optional,A> fromOptional(final Optional<? extends Vector<A>> nested) {
        return of(AnyM.fromOptional(nested));
    }
    public static <A> VectorT<Witness.maybe,A> fromMaybe(final Maybe<? extends Vector<A>> nested) {
        return of(AnyM.fromMaybe(nested));
    }
    public static <A> VectorT<list,A> fromList(final List<? extends Vector<A>> nested) {
        return of(AnyM.fromList(nested));
    }
    public static <A> VectorT<Witness.set,A> fromSet(final Set<? extends Vector<A>> nested) {
        return of(AnyM.fromSet(nested));
    }


    @Override
    public String toString() {
        return String.format("VectorT[%s]",  run.unwrap().toString());

    }


    public <T> VectorT<W,T> unit(final T unit) {
        return of(run.unit(Vector.of(unit)));
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


    @Override
    public <R> VectorT<W,R> unitIterable(final Iterable<R> it) {
        return of(run.unitIterable(it)
                     .map(Vector::of));
    }

    @Override
    public <R> VectorT<W,R> empty() {
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
    public <T> VectorT<W,T> unitAnyM(final AnyM<W,Traversable<T>> traversable) {

        return of((AnyM) traversable.map(t -> Vector.fromIterable(t)));
    }

    @Override
    public AnyM<W,? extends IterableX<T>> transformerStream() {

        return run;
    }

    public static <W extends WitnessType<W>,T> VectorT<W,T> emptyList(W witness) {
        return of(witness.<W>adapter().unit(Vector.empty()));
    }

    @Override
    public boolean isSeqPresent() {
        return !run.isEmpty();
    }


    @Override
    public VectorT<W,T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {

        return (VectorT<W,T>) FoldableTransformerSeq.super.combine(predicate, op);
    }
    @Override
    public VectorT<W,T> combine(final Monoid<T> op, final BiPredicate<? super T, ? super T> predicate) {
        return (VectorT<W,T>)FoldableTransformerSeq.super.combine(op,predicate);
    }


    @Override
    public VectorT<W,T> cycle(final long times) {

        return (VectorT<W,T>) FoldableTransformerSeq.super.cycle(times);
    }

    @Override
    public VectorT<W,T> cycle(final Monoid<T> m, final long times) {

        return (VectorT<W,T>) FoldableTransformerSeq.super.cycle(m, times);
    }


    @Override
    public VectorT<W,T> cycleWhile(final Predicate<? super T> predicate) {

        return (VectorT<W,T>) FoldableTransformerSeq.super.cycleWhile(predicate);
    }


    @Override
    public VectorT<W,T> cycleUntil(final Predicate<? super T> predicate) {

        return (VectorT<W,T>) FoldableTransformerSeq.super.cycleUntil(predicate);
    }


    @Override
    public <U, R> VectorT<W,R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (VectorT<W,R>) FoldableTransformerSeq.super.zip(other, zipper);
    }


    @Override
    public <U, R> VectorT<W,R> zipWithStream(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (VectorT<W,R>) FoldableTransformerSeq.super.zipWithStream(other, zipper);
    }




    @Override
    public <U> VectorT<W,Tuple2<T, U>> zipWithStream(final Stream<? extends U> other) {

        return (VectorT) FoldableTransformerSeq.super.zipWithStream(other);
    }


    @Override
    public <U> VectorT<W,Tuple2<T, U>> zip(final Iterable<? extends U> other) {

        return (VectorT) FoldableTransformerSeq.super.zip(other);
    }


    @Override
    public <S, U> VectorT<W,Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {

        return (VectorT) FoldableTransformerSeq.super.zip3(second, third);
    }


    @Override
    public <T2, T3, T4> VectorT<W,Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
                                                              final Iterable<? extends T4> fourth) {

        return (VectorT) FoldableTransformerSeq.super.zip4(second, third, fourth);
    }


    @Override
    public VectorT<W,Tuple2<T, Long>> zipWithIndex() {

        return (VectorT<W,Tuple2<T, Long>>) FoldableTransformerSeq.super.zipWithIndex();
    }

    @Override
    public VectorT<W,Seq<T>> sliding(final int windowSize) {

        return (VectorT<W,Seq<T>>) FoldableTransformerSeq.super.sliding(windowSize);
    }


    @Override
    public VectorT<W,Seq<T>> sliding(final int windowSize, final int increment) {

        return (VectorT<W,Seq<T>>) FoldableTransformerSeq.super.sliding(windowSize, increment);
    }


    @Override
    public <C extends PersistentCollection<? super T>> VectorT<W,C> grouped(final int size, final Supplier<C> supplier) {

        return (VectorT<W,C>) FoldableTransformerSeq.super.grouped(size, supplier);
    }


    @Override
    public VectorT<W,Vector<T>> groupedUntil(final Predicate<? super T> predicate) {

        return (VectorT<W,Vector<T>>) FoldableTransformerSeq.super.groupedUntil(predicate);
    }


    @Override
    public VectorT<W,Vector<T>> groupedUntil(final BiPredicate<Vector<? super T>, ? super T> predicate) {

        return (VectorT<W,Vector<T>>) FoldableTransformerSeq.super.groupedUntil(predicate);
    }


    @Override
    public VectorT<W,Vector<T>> groupedWhile(final Predicate<? super T> predicate) {

        return (VectorT<W,Vector<T>>) FoldableTransformerSeq.super.groupedWhile(predicate);
    }


    @Override
    public <C extends PersistentCollection<? super T>> VectorT<W,C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (VectorT<W,C>) FoldableTransformerSeq.super.groupedWhile(predicate, factory);
    }


    @Override
    public <C extends PersistentCollection<? super T>> VectorT<W,C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (VectorT<W,C>) FoldableTransformerSeq.super.groupedUntil(predicate, factory);
    }


    @Override
    public VectorT<W,Vector<T>> grouped(final int groupSize) {

        return (VectorT<W,Vector<T>>) FoldableTransformerSeq.super.grouped(groupSize);
    }



    @Override
    public VectorT<W,T> distinct() {

        return (VectorT<W,T>) FoldableTransformerSeq.super.distinct();
    }


    @Override
    public VectorT<W,T> scanLeft(final Monoid<T> monoid) {

        return (VectorT<W,T>) FoldableTransformerSeq.super.scanLeft(monoid);
    }


    @Override
    public <U> VectorT<W,U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {

        return (VectorT<W,U>) FoldableTransformerSeq.super.scanLeft(seed, function);
    }


    @Override
    public VectorT<W,T> scanRight(final Monoid<T> monoid) {

        return (VectorT<W,T>) FoldableTransformerSeq.super.scanRight(monoid);
    }


    @Override
    public <U> VectorT<W,U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {

        return (VectorT<W,U>) FoldableTransformerSeq.super.scanRight(identity, combiner);
    }


    @Override
    public VectorT<W,T> sorted() {

        return (VectorT<W,T>) FoldableTransformerSeq.super.sorted();
    }


    @Override
    public VectorT<W,T> sorted(final Comparator<? super T> c) {

        return (VectorT<W,T>) FoldableTransformerSeq.super.sorted(c);
    }


    @Override
    public VectorT<W,T> takeWhile(final Predicate<? super T> p) {

        return (VectorT<W,T>) FoldableTransformerSeq.super.takeWhile(p);
    }


    @Override
    public VectorT<W,T> dropWhile(final Predicate<? super T> p) {

        return (VectorT<W,T>) FoldableTransformerSeq.super.dropWhile(p);
    }


    @Override
    public VectorT<W,T> takeUntil(final Predicate<? super T> p) {

        return (VectorT<W,T>) FoldableTransformerSeq.super.takeUntil(p);
    }


    @Override
    public VectorT<W,T> dropUntil(final Predicate<? super T> p) {

        return (VectorT<W,T>) FoldableTransformerSeq.super.dropUntil(p);
    }


    @Override
    public VectorT<W,T> dropRight(final int num) {

        return (VectorT<W,T>) FoldableTransformerSeq.super.dropRight(num);
    }

    @Override
    public VectorT<W,T> takeRight(final int num) {

        return (VectorT<W,T>) FoldableTransformerSeq.super.takeRight(num);
    }




    @Override
    public VectorT<W,T> intersperse(final T value) {

        return (VectorT<W,T>) FoldableTransformerSeq.super.intersperse(value);
    }


    @Override
    public VectorT<W,T> reverse() {

        return (VectorT<W,T>) FoldableTransformerSeq.super.reverse();
    }


    @Override
    public VectorT<W,T> shuffle() {

        return (VectorT<W,T>) FoldableTransformerSeq.super.shuffle();
    }


    @Override
    public VectorT<W,T> onEmpty(final T value) {

        return (VectorT<W,T>) FoldableTransformerSeq.super.onEmpty(value);
    }


    @Override
    public VectorT<W,T> onEmptyGet(final Supplier<? extends T> supplier) {

        return (VectorT<W,T>) FoldableTransformerSeq.super.onEmptyGet(supplier);
    }




    @Override
    public VectorT<W,T> shuffle(final Random random) {

        return (VectorT<W,T>) FoldableTransformerSeq.super.shuffle(random);
    }


    @Override
    public VectorT<W,T> slice(final long from, final long to) {

        return (VectorT<W,T>) FoldableTransformerSeq.super.slice(from, to);
    }


    @Override
    public <U extends Comparable<? super U>> VectorT<W,T> sorted(final Function<? super T, ? extends U> function) {
        return (VectorT) FoldableTransformerSeq.super.sorted(function);
    }

    @Override
    public int hashCode() {
        return run.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof VectorT) {
            return run.equals(((VectorT) o).run);
        }
        return false;
    }



    public <T2, R1, R2, R3, R> VectorT<W,R> forEach4M(Function<? super T, ? extends VectorT<W,R1>> value1,
                                                      BiFunction<? super T, ? super R1, ? extends VectorT<W,R2>> value2,
                                                      Function3<? super T, ? super R1, ? super R2, ? extends VectorT<W,R3>> value3,
                                                      Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return this.flatMapT(in->value1.apply(in)
                .flatMapT(in2-> value2.apply(in,in2)
                        .flatMapT(in3->value3.apply(in,in2,in3)
                                .map(in4->yieldingFunction.apply(in,in2,in3,in4)))));

    }
    public <T2, R1, R2, R3, R> VectorT<W,R> forEach4M(Function<? super T, ? extends VectorT<W,R1>> value1,
                                                      BiFunction<? super T, ? super R1, ? extends VectorT<W,R2>> value2,
                                                      Function3<? super T, ? super R1, ? super R2, ? extends VectorT<W,R3>> value3,
                                                      Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                      Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return this.flatMapT(in->value1.apply(in)
                .flatMapT(in2-> value2.apply(in,in2)
                        .flatMapT(in3->value3.apply(in,in2,in3)
                                .filter(in4->filterFunction.apply(in,in2,in3,in4))
                                .map(in4->yieldingFunction.apply(in,in2,in3,in4)))));

    }

    public <T2, R1, R2, R> VectorT<W,R> forEach3M(Function<? super T, ? extends VectorT<W,R1>> value1,
                                                  BiFunction<? super T, ? super R1, ? extends VectorT<W,R2>> value2,
                                                  Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return this.flatMapT(in->value1.apply(in).flatMapT(in2-> value2.apply(in,in2)
                .map(in3->yieldingFunction.apply(in,in2,in3))));

    }

    public <T2, R1, R2, R> VectorT<W,R> forEach3M(Function<? super T, ? extends VectorT<W,R1>> value1,
                                                  BiFunction<? super T, ? super R1, ? extends VectorT<W,R2>> value2,
                                                  Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                                                  Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return this.flatMapT(in->value1.apply(in).flatMapT(in2-> value2.apply(in,in2).filter(in3->filterFunction.apply(in,in2,in3))
                .map(in3->yieldingFunction.apply(in,in2,in3))));

    }
    public <R1, R> VectorT<W,R> forEach2M(Function<? super T, ? extends VectorT<W,R1>> value1,
                                          BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {


        return this.flatMapT(in->value1.apply(in)
                .map(in2->yieldingFunction.apply(in,in2)));
    }

    public <R1, R> VectorT<W,R> forEach2M(Function<? super T, ? extends VectorT<W,R1>> value1,
                                          BiFunction<? super T, ? super R1, Boolean> filterFunction,
                                          BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {


        return this.flatMapT(in->value1.apply(in)
                .filter(in2->filterFunction.apply(in,in2))
                .map(in2->yieldingFunction.apply(in,in2)));
    }


    @Override
    public VectorT<W,T> prependStream(Stream<? extends T> stream) {
        return (VectorT) FoldableTransformerSeq.super.prependStream(stream);
    }

    @Override
    public VectorT<W,T> appendAll(T... values) {
        return (VectorT) FoldableTransformerSeq.super.appendAll(values);
    }

    @Override
    public VectorT<W,T> append(T value) {
        return (VectorT) FoldableTransformerSeq.super.append(value);
    }

    @Override
    public VectorT<W,T> prepend(T value) {
        return (VectorT) FoldableTransformerSeq.super.prepend(value);
    }

    @Override
    public VectorT<W,T> prependAll(T... values) {
        return (VectorT) FoldableTransformerSeq.super.prependAll(values);
    }

    @Override
    public VectorT<W,T> insertAt(int pos, T... values) {
        return (VectorT) FoldableTransformerSeq.super.insertAt(pos,values);
    }

    @Override
    public VectorT<W,T> deleteBetween(int start, int end) {
        return (VectorT) FoldableTransformerSeq.super.deleteBetween(start,end);
    }

    @Override
    public VectorT<W,T> insertStreamAt(int pos, Stream<T> stream) {
        return (VectorT) FoldableTransformerSeq.super.insertStreamAt(pos,stream);
    }


  @Override
    public <T2, R> VectorT<W,R> zip(BiFunction<? super T, ? super T2, ? extends R> fn, Publisher<? extends T2> publisher) {
        return (VectorT) FoldableTransformerSeq.super.zip(fn, publisher);
    }

    @Override
    public <U> VectorT<W,Tuple2<T, U>> zipWithPublisher(Publisher<? extends U> other) {
        return (VectorT) FoldableTransformerSeq.super.zipWithPublisher(other);
    }

    @Override
    public <S, U, R> VectorT<W,R> zip3(Iterable<? extends S> second, Iterable<? extends U> third, Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (VectorT) FoldableTransformerSeq.super.zip3(second,third,fn3);
    }

    @Override
    public <T2, T3, T4, R> VectorT<W,R> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth, Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (VectorT) FoldableTransformerSeq.super.zip4(second,third,fourth,fn);
    }


    @Override
    public VectorT<W,T> removeStream(final Stream<? extends T> stream) {
        return (VectorT) FoldableTransformerSeq.super.removeStream(stream);
    }


    @Override
    public <U> VectorT<W,U> ofType(final Class<? extends U> type) {
        return (VectorT) FoldableTransformerSeq.super.ofType(type);
    }

    @Override
    public VectorT<W,T> removeAll(final Iterable<? extends T> it) {
        return (VectorT) FoldableTransformerSeq.super.removeAll(it);
    }


    @Override
    public VectorT<W,T> removeAll(final T... values) {
        return (VectorT) FoldableTransformerSeq.super.removeAll(values);
    }


    @Override
    public VectorT<W,T> filterNot(final Predicate<? super T> predicate) {
        return (VectorT) FoldableTransformerSeq.super.filterNot(predicate);
    }



    @Override
    public VectorT<W,T> retainAll(final Iterable<? extends T> it) {
        return (VectorT) FoldableTransformerSeq.super.retainAll(it);
    }

    @Override
    public VectorT<W,T> notNull() {
        return (VectorT) FoldableTransformerSeq.super.notNull();
    }

    @Override
    public VectorT<W,T> retainStream(final Stream<? extends T> stream) {
        return (VectorT) FoldableTransformerSeq.super.retainStream(stream);
    }

    @Override
    public VectorT<W,T> retainAll(final T... values) {
        return (VectorT) FoldableTransformerSeq.super.retainAll(values);
    }

    @Override
    public VectorT<W,T> drop(final long num) {
        return drop(num);
    }

    @Override
    public VectorT<W,T> take(final long num) {
        return take(num);
    }


}
