package cyclops.monads.transformers;

import com.oath.cyclops.anym.transformers.FoldableTransformerSeq;
import com.oath.cyclops.types.foldable.To;
import com.oath.cyclops.types.persistent.PersistentCollection;
import com.oath.cyclops.types.persistent.PersistentList;
import com.oath.cyclops.types.traversable.IterableX;
import com.oath.cyclops.types.traversable.Traversable;
import cyclops.control.Maybe;
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
import java.util.function.*;
import java.util.stream.Stream;


public class SeqT<W extends WitnessType<W>,T> implements To<SeqT<W,T>>,
                                                          FoldableTransformerSeq<W,T> {

    final AnyM<W,Seq<T>> run;



    private SeqT(final AnyM<W,? extends Seq<T>> run) {
        this.run = AnyM.narrow(run);
    }





    public AnyM<W,Seq<T>> unwrap() {
        return run;
    }

    public <R> R unwrapTo(Function<? super AnyM<W,Seq<T>>,? extends R> fn) {
        return unwrap().to(fn);
    }


    @Override
    public SeqT<W,T> peek(final Consumer<? super T> peek) {
        return map(a -> {
            peek.accept(a);
            return a;
        });

    }


    @Override
    public SeqT<W,T> filter(final Predicate<? super T> test) {
        return of(run.map(seq -> seq.filter(test)));
    }


    @Override
    public <B> SeqT<W,B> map(final Function<? super T, ? extends B> f) {
        return of(run.map(o -> o.map(f)));
    }

    @Override
    public <B> SeqT<W,B> flatMap(final Function<? super T, ? extends Iterable<? extends B>> f) {
        return new SeqT<W,B>(
                               run.map(o -> o.concatMap(f)));

    }


    public <B> SeqT<W,B> flatMapT(final Function<? super T, SeqT<W,B>> f) {

        return of(run.map(list -> list.concatMap(a -> f.apply(a).run.stream())
                                      .concatMap(a -> a.stream())));
    }




    public static <W extends WitnessType<W>,A> SeqT<W,A> fromAnyM(final AnyM<W,A> anyM) {

        return of( anyM.map(Seq::of));
    }


    public static <W extends WitnessType<W>,A> SeqT<W,A> of(final AnyM<W,? extends Seq<A>> monads) {
        return new SeqT<>(
                              monads);
    }
    public static <W extends WitnessType<W>,A> SeqT<W,A> ofList(final AnyM<W,? extends PersistentList<A>> monads) {
        return new SeqT<>(
                              monads.map(Seq::fromIterable));
    }
    public static <A> SeqT<Witness.stream,A> fromStream(final Stream<? extends Seq<A>> nested) {
        return of(AnyM.fromStream(nested));
    }
    public static <A> SeqT<Witness.reactiveSeq,A> fromStream(final ReactiveSeq<? extends Seq<A>> nested) {
        return of(AnyM.fromStream(nested));
    }
    public static <A> SeqT<Witness.optional,A> fromOptional(final Optional<? extends Seq<A>> nested) {
        return of(AnyM.fromOptional(nested));
    }
    public static <A> SeqT<Witness.maybe,A> fromMaybe(final Maybe<? extends Seq<A>> nested) {
        return of(AnyM.fromMaybe(nested));
    }
    public static <A> SeqT<list,A> fromList(final List<? extends Seq<A>> nested) {
        return of(AnyM.fromList(nested));
    }
    public static <A> SeqT<Witness.set,A> fromSet(final Set<? extends Seq<A>> nested) {
        return of(AnyM.fromSet(nested));
    }


    @Override
    public String toString() {
        return String.format("SeqT[%s]",  run.unwrap().toString());

    }


    public <T> SeqT<W,T> unit(final T unit) {
        return of(run.unit(Seq.of(unit)));
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
    public <R> SeqT<W,R> unitIterable(final Iterable<R> it) {
        return of(run.unitIterable(it)
                     .map(Seq::of));
    }

    @Override
    public <R> SeqT<W,R> empty() {
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
    public <T> SeqT<W,T> unitAnyM(final AnyM<W,Traversable<T>> traversable) {

        return of((AnyM) traversable.map(t -> Seq.fromIterable(t)));
    }

    @Override
    public AnyM<W,? extends IterableX<T>> transformerStream() {

        return run;
    }

    public static <W extends WitnessType<W>,T> SeqT<W,T> emptyList(W witness) {
        return of(witness.<W>adapter().unit(Seq.empty()));
    }

    @Override
    public boolean isSeqPresent() {
        return !run.isEmpty();
    }


    @Override
    public SeqT<W,T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {

        return (SeqT<W,T>) FoldableTransformerSeq.super.combine(predicate, op);
    }
    @Override
    public SeqT<W,T> combine(final Monoid<T> op, final BiPredicate<? super T, ? super T> predicate) {
        return (SeqT<W,T>)FoldableTransformerSeq.super.combine(op,predicate);
    }


    @Override
    public SeqT<W,T> cycle(final long times) {

        return (SeqT<W,T>) FoldableTransformerSeq.super.cycle(times);
    }

    @Override
    public SeqT<W,T> cycle(final Monoid<T> m, final long times) {

        return (SeqT<W,T>) FoldableTransformerSeq.super.cycle(m, times);
    }


    @Override
    public SeqT<W,T> cycleWhile(final Predicate<? super T> predicate) {

        return (SeqT<W,T>) FoldableTransformerSeq.super.cycleWhile(predicate);
    }


    @Override
    public SeqT<W,T> cycleUntil(final Predicate<? super T> predicate) {

        return (SeqT<W,T>) FoldableTransformerSeq.super.cycleUntil(predicate);
    }


    @Override
    public <U, R> SeqT<W,R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (SeqT<W,R>) FoldableTransformerSeq.super.zip(other, zipper);
    }


    @Override
    public <U, R> SeqT<W,R> zipWithStream(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (SeqT<W,R>) FoldableTransformerSeq.super.zipWithStream(other, zipper);
    }




    @Override
    public <U> SeqT<W,Tuple2<T, U>> zipWithStream(final Stream<? extends U> other) {

        return (SeqT) FoldableTransformerSeq.super.zipWithStream(other);
    }


    @Override
    public <U> SeqT<W,Tuple2<T, U>> zip(final Iterable<? extends U> other) {

        return (SeqT) FoldableTransformerSeq.super.zip(other);
    }


    @Override
    public <S, U> SeqT<W,Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {

        return (SeqT) FoldableTransformerSeq.super.zip3(second, third);
    }


    @Override
    public <T2, T3, T4> SeqT<W,Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
                                                           final Iterable<? extends T4> fourth) {

        return (SeqT) FoldableTransformerSeq.super.zip4(second, third, fourth);
    }


    @Override
    public SeqT<W,Tuple2<T, Long>> zipWithIndex() {

        return (SeqT<W,Tuple2<T, Long>>) FoldableTransformerSeq.super.zipWithIndex();
    }

    @Override
    public SeqT<W,Seq<T>> sliding(final int windowSize) {

        return (SeqT<W,Seq<T>>) FoldableTransformerSeq.super.sliding(windowSize);
    }


    @Override
    public SeqT<W,Seq<T>> sliding(final int windowSize, final int increment) {

        return (SeqT<W,Seq<T>>) FoldableTransformerSeq.super.sliding(windowSize, increment);
    }


    @Override
    public <C extends PersistentCollection<? super T>> SeqT<W,C> grouped(final int size, final Supplier<C> supplier) {

        return (SeqT<W,C>) FoldableTransformerSeq.super.grouped(size, supplier);
    }


    @Override
    public SeqT<W,Vector<T>> groupedUntil(final Predicate<? super T> predicate) {

        return (SeqT<W,Vector<T>>) FoldableTransformerSeq.super.groupedUntil(predicate);
    }


    @Override
    public SeqT<W,Vector<T>> groupedUntil(final BiPredicate<Vector<? super T>, ? super T> predicate) {

        return (SeqT<W,Vector<T>>) FoldableTransformerSeq.super.groupedUntil(predicate);
    }


    @Override
    public SeqT<W,Vector<T>> groupedWhile(final Predicate<? super T> predicate) {

        return (SeqT<W,Vector<T>>) FoldableTransformerSeq.super.groupedWhile(predicate);
    }


    @Override
    public <C extends PersistentCollection<? super T>> SeqT<W,C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (SeqT<W,C>) FoldableTransformerSeq.super.groupedWhile(predicate, factory);
    }


    @Override
    public <C extends PersistentCollection<? super T>> SeqT<W,C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (SeqT<W,C>) FoldableTransformerSeq.super.groupedUntil(predicate, factory);
    }


    @Override
    public SeqT<W,Vector<T>> grouped(final int groupSize) {

        return (SeqT<W,Vector<T>>) FoldableTransformerSeq.super.grouped(groupSize);
    }



    @Override
    public SeqT<W,T> distinct() {

        return (SeqT<W,T>) FoldableTransformerSeq.super.distinct();
    }


    @Override
    public SeqT<W,T> scanLeft(final Monoid<T> monoid) {

        return (SeqT<W,T>) FoldableTransformerSeq.super.scanLeft(monoid);
    }


    @Override
    public <U> SeqT<W,U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {

        return (SeqT<W,U>) FoldableTransformerSeq.super.scanLeft(seed, function);
    }


    @Override
    public SeqT<W,T> scanRight(final Monoid<T> monoid) {

        return (SeqT<W,T>) FoldableTransformerSeq.super.scanRight(monoid);
    }


    @Override
    public <U> SeqT<W,U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {

        return (SeqT<W,U>) FoldableTransformerSeq.super.scanRight(identity, combiner);
    }


    @Override
    public SeqT<W,T> sorted() {

        return (SeqT<W,T>) FoldableTransformerSeq.super.sorted();
    }


    @Override
    public SeqT<W,T> sorted(final Comparator<? super T> c) {

        return (SeqT<W,T>) FoldableTransformerSeq.super.sorted(c);
    }


    @Override
    public SeqT<W,T> takeWhile(final Predicate<? super T> p) {

        return (SeqT<W,T>) FoldableTransformerSeq.super.takeWhile(p);
    }


    @Override
    public SeqT<W,T> dropWhile(final Predicate<? super T> p) {

        return (SeqT<W,T>) FoldableTransformerSeq.super.dropWhile(p);
    }


    @Override
    public SeqT<W,T> takeUntil(final Predicate<? super T> p) {

        return (SeqT<W,T>) FoldableTransformerSeq.super.takeUntil(p);
    }


    @Override
    public SeqT<W,T> dropUntil(final Predicate<? super T> p) {

        return (SeqT<W,T>) FoldableTransformerSeq.super.dropUntil(p);
    }


    @Override
    public SeqT<W,T> dropRight(final int num) {

        return (SeqT<W,T>) FoldableTransformerSeq.super.dropRight(num);
    }

    @Override
    public SeqT<W,T> takeRight(final int num) {

        return (SeqT<W,T>) FoldableTransformerSeq.super.takeRight(num);
    }




    @Override
    public SeqT<W,T> intersperse(final T value) {

        return (SeqT<W,T>) FoldableTransformerSeq.super.intersperse(value);
    }


    @Override
    public SeqT<W,T> reverse() {

        return (SeqT<W,T>) FoldableTransformerSeq.super.reverse();
    }


    @Override
    public SeqT<W,T> shuffle() {

        return (SeqT<W,T>) FoldableTransformerSeq.super.shuffle();
    }


    @Override
    public SeqT<W,T> onEmpty(final T value) {

        return (SeqT<W,T>) FoldableTransformerSeq.super.onEmpty(value);
    }


    @Override
    public SeqT<W,T> onEmptyGet(final Supplier<? extends T> supplier) {

        return (SeqT<W,T>) FoldableTransformerSeq.super.onEmptyGet(supplier);
    }




    @Override
    public SeqT<W,T> shuffle(final Random random) {

        return (SeqT<W,T>) FoldableTransformerSeq.super.shuffle(random);
    }


    @Override
    public SeqT<W,T> slice(final long from, final long to) {

        return (SeqT<W,T>) FoldableTransformerSeq.super.slice(from, to);
    }


    @Override
    public <U extends Comparable<? super U>> SeqT<W,T> sorted(final Function<? super T, ? extends U> function) {
        return (SeqT) FoldableTransformerSeq.super.sorted(function);
    }

    @Override
    public int hashCode() {
        return run.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof SeqT) {
            return run.equals(((SeqT) o).run);
        }
        return false;
    }



    public <T2, R1, R2, R3, R> SeqT<W,R> forEach4M(Function<? super T, ? extends SeqT<W,R1>> value1,
                                                   BiFunction<? super T, ? super R1, ? extends SeqT<W,R2>> value2,
                                                   Function3<? super T, ? super R1, ? super R2, ? extends SeqT<W,R3>> value3,
                                                   Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return this.flatMapT(in->value1.apply(in)
                .flatMapT(in2-> value2.apply(in,in2)
                        .flatMapT(in3->value3.apply(in,in2,in3)
                                .map(in4->yieldingFunction.apply(in,in2,in3,in4)))));

    }
    public <T2, R1, R2, R3, R> SeqT<W,R> forEach4M(Function<? super T, ? extends SeqT<W,R1>> value1,
                                                   BiFunction<? super T, ? super R1, ? extends SeqT<W,R2>> value2,
                                                   Function3<? super T, ? super R1, ? super R2, ? extends SeqT<W,R3>> value3,
                                                   Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                   Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return this.flatMapT(in->value1.apply(in)
                .flatMapT(in2-> value2.apply(in,in2)
                        .flatMapT(in3->value3.apply(in,in2,in3)
                                .filter(in4->filterFunction.apply(in,in2,in3,in4))
                                .map(in4->yieldingFunction.apply(in,in2,in3,in4)))));

    }

    public <T2, R1, R2, R> SeqT<W,R> forEach3M(Function<? super T, ? extends SeqT<W,R1>> value1,
                                               BiFunction<? super T, ? super R1, ? extends SeqT<W,R2>> value2,
                                               Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return this.flatMapT(in->value1.apply(in).flatMapT(in2-> value2.apply(in,in2)
                .map(in3->yieldingFunction.apply(in,in2,in3))));

    }

    public <T2, R1, R2, R> SeqT<W,R> forEach3M(Function<? super T, ? extends SeqT<W,R1>> value1,
                                               BiFunction<? super T, ? super R1, ? extends SeqT<W,R2>> value2,
                                               Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                                               Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return this.flatMapT(in->value1.apply(in).flatMapT(in2-> value2.apply(in,in2).filter(in3->filterFunction.apply(in,in2,in3))
                .map(in3->yieldingFunction.apply(in,in2,in3))));

    }
    public <R1, R> SeqT<W,R> forEach2M(Function<? super T, ? extends SeqT<W,R1>> value1,
                                       BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {


        return this.flatMapT(in->value1.apply(in)
                .map(in2->yieldingFunction.apply(in,in2)));
    }

    public <R1, R> SeqT<W,R> forEach2M(Function<? super T, ? extends SeqT<W,R1>> value1,
                                       BiFunction<? super T, ? super R1, Boolean> filterFunction,
                                       BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {


        return this.flatMapT(in->value1.apply(in)
                .filter(in2->filterFunction.apply(in,in2))
                .map(in2->yieldingFunction.apply(in,in2)));
    }


    @Override
    public SeqT<W,T> prependStream(Stream<? extends T> stream) {
        return (SeqT) FoldableTransformerSeq.super.prependStream(stream);
    }

    @Override
    public SeqT<W,T> appendAll(T... values) {
        return (SeqT) FoldableTransformerSeq.super.appendAll(values);
    }

    @Override
    public SeqT<W,T> append(T value) {
        return (SeqT) FoldableTransformerSeq.super.append(value);
    }

    @Override
    public SeqT<W,T> prepend(T value) {
        return (SeqT) FoldableTransformerSeq.super.prepend(value);
    }

    @Override
    public SeqT<W,T> prependAll(T... values) {
        return (SeqT) FoldableTransformerSeq.super.prependAll(values);
    }

    @Override
    public SeqT<W,T> insertAt(int pos, T... values) {
        return (SeqT) FoldableTransformerSeq.super.insertAt(pos,values);
    }

    @Override
    public SeqT<W,T> deleteBetween(int start, int end) {
        return (SeqT) FoldableTransformerSeq.super.deleteBetween(start,end);
    }

    @Override
    public SeqT<W,T> insertStreamAt(int pos, Stream<T> stream) {
        return (SeqT) FoldableTransformerSeq.super.insertStreamAt(pos,stream);
    }


  @Override
    public <T2, R> SeqT<W,R> zip(BiFunction<? super T, ? super T2, ? extends R> fn, Publisher<? extends T2> publisher) {
        return (SeqT) FoldableTransformerSeq.super.zip(fn, publisher);
    }

    @Override
    public <U> SeqT<W,Tuple2<T, U>> zipWithPublisher(Publisher<? extends U> other) {
        return (SeqT) FoldableTransformerSeq.super.zipWithPublisher(other);
    }

    @Override
    public <S, U, R> SeqT<W,R> zip3(Iterable<? extends S> second, Iterable<? extends U> third, Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (SeqT) FoldableTransformerSeq.super.zip3(second,third,fn3);
    }

    @Override
    public <T2, T3, T4, R> SeqT<W,R> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth, Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (SeqT) FoldableTransformerSeq.super.zip4(second,third,fourth,fn);
    }


    @Override
    public SeqT<W,T> removeStream(final Stream<? extends T> stream) {
        return (SeqT) FoldableTransformerSeq.super.removeStream(stream);
    }


    @Override
    public <U> SeqT<W,U> ofType(final Class<? extends U> type) {
        return (SeqT) FoldableTransformerSeq.super.ofType(type);
    }

    @Override
    public SeqT<W,T> removeAll(final Iterable<? extends T> it) {
        return (SeqT) FoldableTransformerSeq.super.removeAll(it);
    }


    @Override
    public SeqT<W,T> removeAll(final T... values) {
        return (SeqT) FoldableTransformerSeq.super.removeAll(values);
    }


    @Override
    public SeqT<W,T> filterNot(final Predicate<? super T> predicate) {
        return (SeqT) FoldableTransformerSeq.super.filterNot(predicate);
    }



    @Override
    public SeqT<W,T> retainAll(final Iterable<? extends T> it) {
        return (SeqT) FoldableTransformerSeq.super.retainAll(it);
    }

    @Override
    public SeqT<W,T> notNull() {
        return (SeqT) FoldableTransformerSeq.super.notNull();
    }

    @Override
    public SeqT<W,T> retainStream(final Stream<? extends T> stream) {
        return (SeqT) FoldableTransformerSeq.super.retainStream(stream);
    }

    @Override
    public SeqT<W,T> retainAll(final T... values) {
        return (SeqT) FoldableTransformerSeq.super.retainAll(values);
    }


    @Override
    public SeqT<W,T> drop(final long num) {
        return drop(num);
    }

    @Override
    public SeqT<W,T> take(final long num) {
        return take(num);
    }



}
