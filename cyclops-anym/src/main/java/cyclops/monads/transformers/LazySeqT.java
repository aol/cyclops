package cyclops.monads.transformers;

import com.oath.anym.transformers.FoldableTransformerSeq;
import com.oath.cyclops.types.foldable.To;
import com.oath.cyclops.types.persistent.PersistentCollection;
import com.oath.cyclops.types.persistent.PersistentList;
import com.oath.cyclops.types.traversable.IterableX;
import com.oath.cyclops.types.traversable.Traversable;
import cyclops.control.Maybe;
import cyclops.data.LazySeq;
import cyclops.data.Seq;
import cyclops.data.Vector;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.function.Monoid;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import cyclops.monads.Witness.list;
import cyclops.monads.WitnessType;
import cyclops.reactive.ReactiveSeq;
import org.reactivestreams.Publisher;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Stream;


public class LazySeqT<W extends WitnessType<W>,T> implements To<LazySeqT<W,T>>,
                                                          FoldableTransformerSeq<W,T> {

    final AnyM<W,LazySeq<T>> run;



    private LazySeqT(final AnyM<W,? extends LazySeq<T>> run) {
        this.run = AnyM.narrow(run);
    }





    public AnyM<W,LazySeq<T>> unwrap() {
        return run;
    }

    public <R> R unwrapTo(Function<? super AnyM<W,LazySeq<T>>,? extends R> fn) {
        return unwrap().to(fn);
    }


    @Override
    public LazySeqT<W,T> peek(final Consumer<? super T> peek) {
        return map(a -> {
            peek.accept(a);
            return a;
        });

    }


    @Override
    public LazySeqT<W,T> filter(final Predicate<? super T> test) {
        return of(run.map(seq -> seq.filter(test)));
    }


    @Override
    public <B> LazySeqT<W,B> map(final Function<? super T, ? extends B> f) {
        return of(run.map(o -> o.map(f)));
    }

    @Override
    public <B> LazySeqT<W,B> flatMap(final Function<? super T, ? extends Iterable<? extends B>> f) {
        return new LazySeqT<W,B>(
                               run.map(o -> o.concatMap(f)));

    }


    public <B> LazySeqT<W,B> flatMapT(final Function<? super T, LazySeqT<W,B>> f) {

        return of(run.map(list -> list.concatMap(a -> f.apply(a).run.stream())
                                      .concatMap(a -> a.stream())));
    }




    public static <W extends WitnessType<W>,A> LazySeqT<W,A> fromAnyM(final AnyM<W,A> anyM) {

        return of( anyM.map(LazySeq::of));
    }


    public static <W extends WitnessType<W>,A> LazySeqT<W,A> of(final AnyM<W,? extends LazySeq<A>> monads) {
        return new LazySeqT<>(
                              monads);
    }
    public static <W extends WitnessType<W>,A> LazySeqT<W,A> ofList(final AnyM<W,? extends PersistentList<A>> monads) {
        return new LazySeqT<>(
                              monads.map(LazySeq::fromIterable));
    }
    public static <A> LazySeqT<Witness.stream,A> fromStream(final Stream<? extends LazySeq<A>> nested) {
        return of(AnyM.fromStream(nested));
    }
    public static <A> LazySeqT<Witness.reactiveSeq,A> fromStream(final ReactiveSeq<? extends LazySeq<A>> nested) {
        return of(AnyM.fromStream(nested));
    }
    public static <A> LazySeqT<Witness.optional,A> fromOptional(final Optional<? extends LazySeq<A>> nested) {
        return of(AnyM.fromOptional(nested));
    }
    public static <A> LazySeqT<Witness.maybe,A> fromMaybe(final Maybe<? extends LazySeq<A>> nested) {
        return of(AnyM.fromMaybe(nested));
    }
    public static <A> LazySeqT<list,A> fromList(final List<? extends LazySeq<A>> nested) {
        return of(AnyM.fromList(nested));
    }
    public static <A> LazySeqT<Witness.set,A> fromSet(final Set<? extends LazySeq<A>> nested) {
        return of(AnyM.fromSet(nested));
    }


    @Override
    public String toString() {
        return String.format("LazySeqT[%s]",  run.unwrap().toString());

    }


    public <T> LazySeqT<W,T> unit(final T unit) {
        return of(run.unit(LazySeq.of(unit)));
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
    public <R> LazySeqT<W,R> unitIterator(final Iterator<R> it) {
        return of(run.unitIterator(it)
                     .map(LazySeq::of));
    }

    @Override
    public <R> LazySeqT<W,R> empty() {
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
    public <T> LazySeqT<W,T> unitAnyM(final AnyM<W,Traversable<T>> traversable) {

        return of((AnyM) traversable.map(t -> LazySeq.fromIterable(t)));
    }

    @Override
    public AnyM<W,? extends IterableX<T>> transformerStream() {

        return run;
    }

    public static <W extends WitnessType<W>,T> LazySeqT<W,T> emptyList(W witness) {
        return of(witness.<W>adapter().unit(LazySeq.empty()));
    }

    @Override
    public boolean isSeqPresent() {
        return !run.isEmpty();
    }


    @Override
    public LazySeqT<W,T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {

        return (LazySeqT<W,T>) FoldableTransformerSeq.super.combine(predicate, op);
    }
    @Override
    public LazySeqT<W,T> combine(final Monoid<T> op, final BiPredicate<? super T, ? super T> predicate) {
        return (LazySeqT<W,T>)FoldableTransformerSeq.super.combine(op,predicate);
    }


    @Override
    public LazySeqT<W,T> cycle(final long times) {

        return (LazySeqT<W,T>) FoldableTransformerSeq.super.cycle(times);
    }

    @Override
    public LazySeqT<W,T> cycle(final Monoid<T> m, final long times) {

        return (LazySeqT<W,T>) FoldableTransformerSeq.super.cycle(m, times);
    }


    @Override
    public LazySeqT<W,T> cycleWhile(final Predicate<? super T> predicate) {

        return (LazySeqT<W,T>) FoldableTransformerSeq.super.cycleWhile(predicate);
    }


    @Override
    public LazySeqT<W,T> cycleUntil(final Predicate<? super T> predicate) {

        return (LazySeqT<W,T>) FoldableTransformerSeq.super.cycleUntil(predicate);
    }


    @Override
    public <U, R> LazySeqT<W,R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (LazySeqT<W,R>) FoldableTransformerSeq.super.zip(other, zipper);
    }


    @Override
    public <U, R> LazySeqT<W,R> zipWithStream(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (LazySeqT<W,R>) FoldableTransformerSeq.super.zipWithStream(other, zipper);
    }




    @Override
    public <U> LazySeqT<W,Tuple2<T, U>> zipWithStream(final Stream<? extends U> other) {

        return (LazySeqT) FoldableTransformerSeq.super.zipWithStream(other);
    }


    @Override
    public <U> LazySeqT<W,Tuple2<T, U>> zip(final Iterable<? extends U> other) {

        return (LazySeqT) FoldableTransformerSeq.super.zip(other);
    }


    @Override
    public <S, U> LazySeqT<W,Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {

        return (LazySeqT) FoldableTransformerSeq.super.zip3(second, third);
    }


    @Override
    public <T2, T3, T4> LazySeqT<W,Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
                                                               final Iterable<? extends T4> fourth) {

        return (LazySeqT) FoldableTransformerSeq.super.zip4(second, third, fourth);
    }


    @Override
    public LazySeqT<W,Tuple2<T, Long>> zipWithIndex() {

        return (LazySeqT<W,Tuple2<T, Long>>) FoldableTransformerSeq.super.zipWithIndex();
    }

    @Override
    public LazySeqT<W,Seq<T>> sliding(final int windowSize) {

        return (LazySeqT<W,Seq<T>>) FoldableTransformerSeq.super.sliding(windowSize);
    }


    @Override
    public LazySeqT<W,Seq<T>> sliding(final int windowSize, final int increment) {

        return (LazySeqT<W,Seq<T>>) FoldableTransformerSeq.super.sliding(windowSize, increment);
    }


    @Override
    public <C extends PersistentCollection<? super T>> LazySeqT<W,C> grouped(final int size, final Supplier<C> supplier) {

        return (LazySeqT<W,C>) FoldableTransformerSeq.super.grouped(size, supplier);
    }


    @Override
    public LazySeqT<W,Vector<T>> groupedUntil(final Predicate<? super T> predicate) {

        return (LazySeqT<W,Vector<T>>) FoldableTransformerSeq.super.groupedUntil(predicate);
    }


    @Override
    public LazySeqT<W,Vector<T>> groupedUntil(final BiPredicate<Vector<? super T>, ? super T> predicate) {

        return (LazySeqT<W,Vector<T>>) FoldableTransformerSeq.super.groupedUntil(predicate);
    }


    @Override
    public LazySeqT<W,Vector<T>> groupedWhile(final Predicate<? super T> predicate) {

        return (LazySeqT<W,Vector<T>>) FoldableTransformerSeq.super.groupedWhile(predicate);
    }


    @Override
    public <C extends PersistentCollection<? super T>> LazySeqT<W,C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (LazySeqT<W,C>) FoldableTransformerSeq.super.groupedWhile(predicate, factory);
    }


    @Override
    public <C extends PersistentCollection<? super T>> LazySeqT<W,C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (LazySeqT<W,C>) FoldableTransformerSeq.super.groupedUntil(predicate, factory);
    }


    @Override
    public LazySeqT<W,Vector<T>> grouped(final int groupSize) {

        return (LazySeqT<W,Vector<T>>) FoldableTransformerSeq.super.grouped(groupSize);
    }



    @Override
    public LazySeqT<W,T> distinct() {

        return (LazySeqT<W,T>) FoldableTransformerSeq.super.distinct();
    }


    @Override
    public LazySeqT<W,T> scanLeft(final Monoid<T> monoid) {

        return (LazySeqT<W,T>) FoldableTransformerSeq.super.scanLeft(monoid);
    }


    @Override
    public <U> LazySeqT<W,U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {

        return (LazySeqT<W,U>) FoldableTransformerSeq.super.scanLeft(seed, function);
    }


    @Override
    public LazySeqT<W,T> scanRight(final Monoid<T> monoid) {

        return (LazySeqT<W,T>) FoldableTransformerSeq.super.scanRight(monoid);
    }


    @Override
    public <U> LazySeqT<W,U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {

        return (LazySeqT<W,U>) FoldableTransformerSeq.super.scanRight(identity, combiner);
    }


    @Override
    public LazySeqT<W,T> sorted() {

        return (LazySeqT<W,T>) FoldableTransformerSeq.super.sorted();
    }


    @Override
    public LazySeqT<W,T> sorted(final Comparator<? super T> c) {

        return (LazySeqT<W,T>) FoldableTransformerSeq.super.sorted(c);
    }


    @Override
    public LazySeqT<W,T> takeWhile(final Predicate<? super T> p) {

        return (LazySeqT<W,T>) FoldableTransformerSeq.super.takeWhile(p);
    }


    @Override
    public LazySeqT<W,T> dropWhile(final Predicate<? super T> p) {

        return (LazySeqT<W,T>) FoldableTransformerSeq.super.dropWhile(p);
    }


    @Override
    public LazySeqT<W,T> takeUntil(final Predicate<? super T> p) {

        return (LazySeqT<W,T>) FoldableTransformerSeq.super.takeUntil(p);
    }


    @Override
    public LazySeqT<W,T> dropUntil(final Predicate<? super T> p) {

        return (LazySeqT<W,T>) FoldableTransformerSeq.super.dropUntil(p);
    }


    @Override
    public LazySeqT<W,T> dropRight(final int num) {

        return (LazySeqT<W,T>) FoldableTransformerSeq.super.dropRight(num);
    }

    @Override
    public LazySeqT<W,T> takeRight(final int num) {

        return (LazySeqT<W,T>) FoldableTransformerSeq.super.takeRight(num);
    }


    @Override
    public LazySeqT<W,T> skip(final long num) {

        return (LazySeqT<W,T>) FoldableTransformerSeq.super.skip(num);
    }


    @Override
    public LazySeqT<W,T> skipWhile(final Predicate<? super T> p) {

        return (LazySeqT<W,T>) FoldableTransformerSeq.super.skipWhile(p);
    }


    @Override
    public LazySeqT<W,T> skipUntil(final Predicate<? super T> p) {

        return (LazySeqT<W,T>) FoldableTransformerSeq.super.skipUntil(p);
    }


    @Override
    public LazySeqT<W,T> limit(final long num) {

        return (LazySeqT<W,T>) FoldableTransformerSeq.super.limit(num);
    }


    @Override
    public LazySeqT<W,T> limitWhile(final Predicate<? super T> p) {

        return (LazySeqT<W,T>) FoldableTransformerSeq.super.limitWhile(p);
    }


    @Override
    public LazySeqT<W,T> limitUntil(final Predicate<? super T> p) {

        return (LazySeqT<W,T>) FoldableTransformerSeq.super.limitUntil(p);
    }


    @Override
    public LazySeqT<W,T> intersperse(final T value) {

        return (LazySeqT<W,T>) FoldableTransformerSeq.super.intersperse(value);
    }


    @Override
    public LazySeqT<W,T> reverse() {

        return (LazySeqT<W,T>) FoldableTransformerSeq.super.reverse();
    }


    @Override
    public LazySeqT<W,T> shuffle() {

        return (LazySeqT<W,T>) FoldableTransformerSeq.super.shuffle();
    }


    @Override
    public LazySeqT<W,T> skipLast(final int num) {

        return (LazySeqT<W,T>) FoldableTransformerSeq.super.skipLast(num);
    }


    @Override
    public LazySeqT<W,T> limitLast(final int num) {

        return (LazySeqT<W,T>) FoldableTransformerSeq.super.limitLast(num);
    }


    @Override
    public LazySeqT<W,T> onEmpty(final T value) {

        return (LazySeqT<W,T>) FoldableTransformerSeq.super.onEmpty(value);
    }


    @Override
    public LazySeqT<W,T> onEmptyGet(final Supplier<? extends T> supplier) {

        return (LazySeqT<W,T>) FoldableTransformerSeq.super.onEmptyGet(supplier);
    }




    @Override
    public LazySeqT<W,T> shuffle(final Random random) {

        return (LazySeqT<W,T>) FoldableTransformerSeq.super.shuffle(random);
    }


    @Override
    public LazySeqT<W,T> slice(final long from, final long to) {

        return (LazySeqT<W,T>) FoldableTransformerSeq.super.slice(from, to);
    }


    @Override
    public <U extends Comparable<? super U>> LazySeqT<W,T> sorted(final Function<? super T, ? extends U> function) {
        return (LazySeqT) FoldableTransformerSeq.super.sorted(function);
    }

    @Override
    public int hashCode() {
        return run.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof LazySeqT) {
            return run.equals(((LazySeqT) o).run);
        }
        return false;
    }



    public <T2, R1, R2, R3, R> LazySeqT<W,R> forEach4M(Function<? super T, ? extends LazySeqT<W,R1>> value1,
                                                       BiFunction<? super T, ? super R1, ? extends LazySeqT<W,R2>> value2,
                                                       Function3<? super T, ? super R1, ? super R2, ? extends LazySeqT<W,R3>> value3,
                                                       Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return this.flatMapT(in->value1.apply(in)
                .flatMapT(in2-> value2.apply(in,in2)
                        .flatMapT(in3->value3.apply(in,in2,in3)
                                .map(in4->yieldingFunction.apply(in,in2,in3,in4)))));

    }
    public <T2, R1, R2, R3, R> LazySeqT<W,R> forEach4M(Function<? super T, ? extends LazySeqT<W,R1>> value1,
                                                       BiFunction<? super T, ? super R1, ? extends LazySeqT<W,R2>> value2,
                                                       Function3<? super T, ? super R1, ? super R2, ? extends LazySeqT<W,R3>> value3,
                                                       Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                       Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return this.flatMapT(in->value1.apply(in)
                .flatMapT(in2-> value2.apply(in,in2)
                        .flatMapT(in3->value3.apply(in,in2,in3)
                                .filter(in4->filterFunction.apply(in,in2,in3,in4))
                                .map(in4->yieldingFunction.apply(in,in2,in3,in4)))));

    }

    public <T2, R1, R2, R> LazySeqT<W,R> forEach3M(Function<? super T, ? extends LazySeqT<W,R1>> value1,
                                                   BiFunction<? super T, ? super R1, ? extends LazySeqT<W,R2>> value2,
                                                   Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return this.flatMapT(in->value1.apply(in).flatMapT(in2-> value2.apply(in,in2)
                .map(in3->yieldingFunction.apply(in,in2,in3))));

    }

    public <T2, R1, R2, R> LazySeqT<W,R> forEach3M(Function<? super T, ? extends LazySeqT<W,R1>> value1,
                                                   BiFunction<? super T, ? super R1, ? extends LazySeqT<W,R2>> value2,
                                                   Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                                                   Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return this.flatMapT(in->value1.apply(in).flatMapT(in2-> value2.apply(in,in2).filter(in3->filterFunction.apply(in,in2,in3))
                .map(in3->yieldingFunction.apply(in,in2,in3))));

    }
    public <R1, R> LazySeqT<W,R> forEach2M(Function<? super T, ? extends LazySeqT<W,R1>> value1,
                                           BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {


        return this.flatMapT(in->value1.apply(in)
                .map(in2->yieldingFunction.apply(in,in2)));
    }

    public <R1, R> LazySeqT<W,R> forEach2M(Function<? super T, ? extends LazySeqT<W,R1>> value1,
                                           BiFunction<? super T, ? super R1, Boolean> filterFunction,
                                           BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {


        return this.flatMapT(in->value1.apply(in)
                .filter(in2->filterFunction.apply(in,in2))
                .map(in2->yieldingFunction.apply(in,in2)));
    }


    @Override
    public LazySeqT<W,T> prependStream(Stream<? extends T> stream) {
        return (LazySeqT) FoldableTransformerSeq.super.prependStream(stream);
    }

    @Override
    public LazySeqT<W,T> appendAll(T... values) {
        return (LazySeqT) FoldableTransformerSeq.super.appendAll(values);
    }

    @Override
    public LazySeqT<W,T> appendAll(T value) {
        return (LazySeqT) FoldableTransformerSeq.super.appendAll(value);
    }

    @Override
    public LazySeqT<W,T> prepend(T value) {
        return (LazySeqT) FoldableTransformerSeq.super.prepend(value);
    }

    @Override
    public LazySeqT<W,T> prependAll(T... values) {
        return (LazySeqT) FoldableTransformerSeq.super.prependAll(values);
    }

    @Override
    public LazySeqT<W,T> insertAt(int pos, T... values) {
        return (LazySeqT) FoldableTransformerSeq.super.insertAt(pos,values);
    }

    @Override
    public LazySeqT<W,T> deleteBetween(int start, int end) {
        return (LazySeqT) FoldableTransformerSeq.super.deleteBetween(start,end);
    }

    @Override
    public LazySeqT<W,T> insertStreamAt(int pos, Stream<T> stream) {
        return (LazySeqT) FoldableTransformerSeq.super.insertStreamAt(pos,stream);
    }


  @Override
    public <T2, R> LazySeqT<W,R> zip(BiFunction<? super T, ? super T2, ? extends R> fn, Publisher<? extends T2> publisher) {
        return (LazySeqT) FoldableTransformerSeq.super.zip(fn, publisher);
    }

    @Override
    public <U> LazySeqT<W,Tuple2<T, U>> zipWithPublisher(Publisher<? extends U> other) {
        return (LazySeqT) FoldableTransformerSeq.super.zipWithPublisher(other);
    }

    @Override
    public <S, U, R> LazySeqT<W,R> zip3(Iterable<? extends S> second, Iterable<? extends U> third, Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (LazySeqT) FoldableTransformerSeq.super.zip3(second,third,fn3);
    }

    @Override
    public <T2, T3, T4, R> LazySeqT<W,R> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth, Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (LazySeqT) FoldableTransformerSeq.super.zip4(second,third,fourth,fn);
    }


    @Override
    public LazySeqT<W,T> removeStream(final Stream<? extends T> stream) {
        return (LazySeqT) FoldableTransformerSeq.super.removeStream(stream);
    }


    @Override
    public <U> LazySeqT<W,U> ofType(final Class<? extends U> type) {
        return (LazySeqT) FoldableTransformerSeq.super.ofType(type);
    }

    @Override
    public LazySeqT<W,T> removeAll(final Iterable<? extends T> it) {
        return (LazySeqT) FoldableTransformerSeq.super.removeAll(it);
    }


    @Override
    public LazySeqT<W,T> removeAll(final T... values) {
        return (LazySeqT) FoldableTransformerSeq.super.removeAll(values);
    }


    @Override
    public LazySeqT<W,T> filterNot(final Predicate<? super T> predicate) {
        return (LazySeqT) FoldableTransformerSeq.super.filterNot(predicate);
    }



    @Override
    public LazySeqT<W,T> retainAll(final Iterable<? extends T> it) {
        return (LazySeqT) FoldableTransformerSeq.super.retainAll(it);
    }

    @Override
    public LazySeqT<W,T> notNull() {
        return (LazySeqT) FoldableTransformerSeq.super.notNull();
    }

    @Override
    public LazySeqT<W,T> retainStream(final Stream<? extends T> stream) {
        return (LazySeqT) FoldableTransformerSeq.super.retainStream(stream);
    }

    @Override
    public LazySeqT<W,T> retainAll(final T... values) {
        return (LazySeqT) FoldableTransformerSeq.super.retainAll(values);
    }




    @Override
    public <R> LazySeqT<W,R> retry(final Function<? super T, ? extends R> fn) {
        return (LazySeqT) FoldableTransformerSeq.super.retry(fn);
    }

    @Override
    public <R> LazySeqT<W,R> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (LazySeqT) FoldableTransformerSeq.super.retry(fn,retries,delay,timeUnit);
    }



    @Override
    public LazySeqT<W,T> drop(final long num) {
        return skip(num);
    }

    @Override
    public LazySeqT<W,T> take(final long num) {
        return limit(num);
    }


    @Override
    public LazySeqT<W,T> recover(final Function<? super Throwable, ? extends T> fn) {
        return (LazySeqT) FoldableTransformerSeq.super.recover(fn);
    }

    @Override
    public <EX extends Throwable> LazySeqT<W,T> recover(Class<EX> exceptionClass, final Function<? super EX, ? extends T> fn) {
        return (LazySeqT) FoldableTransformerSeq.super.recover(exceptionClass,fn);
    }

}
