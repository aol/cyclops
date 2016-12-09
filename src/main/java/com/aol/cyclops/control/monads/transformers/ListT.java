package com.aol.cyclops.control.monads.transformers;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.collections.extensions.FluentSequenceX;
import com.aol.cyclops.data.collections.extensions.persistent.PStackX;
import com.aol.cyclops.data.collections.extensions.persistent.PVectorX;
import com.aol.cyclops.data.collections.extensions.standard.DequeX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.IterableFoldable;
import com.aol.cyclops.types.To;
import com.aol.cyclops.types.Traversable;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.WitnessType;
import com.aol.cyclops.types.anyM.transformers.FoldableTransformerSeq;
import com.aol.cyclops.types.stream.CyclopsCollectable;

/**
 * Monad Transformer for Java Lists nested within Sequential or non-scalar data types (e.g. Lists, Streams etc)
 * 
 * ListT allows the deeply wrapped List to be manipulating within it's nested /contained context
 * @author johnmcclean
 *
 * @param <T> Type of data stored inside the nested Lists
 */
public class ListT<W extends WitnessType<W>,T> implements To<ListT<W,T>>,
                                                          FoldableTransformerSeq<W,T> {

    final AnyM<W,FluentSequenceX<T>> run;

    
    
    private ListT(final AnyM<W,? extends FluentSequenceX<T>> run) {
        this.run = AnyM.narrow(run);
    }
    
   
    /**
     * FluentSequenceX types that are not one of ListX, PStackX, PVectorX are converted to ListX types
     * 
     * @param listXFn
     * @param pListFn
     * @param vectorFn
     * @return
     */
    public <R> AnyM<W,R> visit(Function<? super ListX<T>,? extends R> listXFn,
                       Function<? super PStackX<T>, ? extends R> pListFn,
                       Function<? super PVectorX<T>, ? extends R> vectorFn){
        
        return this.transformerStream().map(t->{
           if(t instanceof PStackX){
               return pListFn.apply((PStackX)t);
           }
           else if(t instanceof PVectorX){
               return vectorFn.apply((PVectorX)t);
           }
           else if(t instanceof ListX){
               return listXFn.apply((ListX)t);
           }
           return listXFn.apply(ListX.fromIterable(t));
        });
    }

    public <R> AnyM<W, R> visit(Function<? super ListX<T>, ? extends R> listXFn,
                                Function<? super DequeX<T>, ? extends R> dequeXFn,
                                    Function<? super PStackX<T>, ? extends R> pListFn, 
                                    Function<? super PVectorX<T>, ? extends R> vectorFn) {

        return this.transformerStream()
                   .map(t -> {
                       if (t instanceof PStackX) {
                           return pListFn.apply((PStackX) t);
                       } else if (t instanceof PVectorX) {
                           return vectorFn.apply((PVectorX) t);
                       } else if (t instanceof DequeX) {
                           return dequeXFn.apply((DequeX) t);
                       } else if (t instanceof ListX) {
                           return listXFn.apply((ListX) t);
                       }
                       return listXFn.apply(ListX.fromIterable(t));
                   });
    }

    public <R> AnyM<W, R> visit(Function<? super ListX<T>, ? extends R> listXFn,
                                Function<? super PVectorX<T>, ? extends R> vectorFn) {

        return this.transformerStream()
                   .map(t -> {
                       if (t instanceof PVectorX) {
                           return vectorFn.apply((PVectorX) t);
                       } else if (t instanceof ListX) {
                           return listXFn.apply((ListX) t);
                       }
                       return listXFn.apply(ListX.fromIterable(t));
                   });
    }

    /**
     * @return The wrapped AnyM
     */
    public AnyM<W,FluentSequenceX<T>> unwrap() {
        return run;
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
     * @return ListT that applies the map function to the wrapped List
     */
    @Override
    public <B> ListT<W,B> map(final Function<? super T, ? extends B> f) {
        return of(run.map(o -> o.map(f)));
    }

    @Override
    public <B> ListT<W,B> flatMap(final Function<? super T, ? extends Iterable<? extends B>> f) {
        return new ListT<W,B>(
                               run.map(o -> o.flatMap(f)));

    }

    /**
     * Flat Map the wrapped List
      * <pre>
     * {@code 
     *  ListT.of(AnyM.fromStream(Arrays.asList(10))
     *             .flatMap(t->List.empty();
     *  
     *  
     *  //ListT<AnyM<Stream<List.empty>>>
     * }
     * </pre>
     * @param f FlatMap function
     * @return ListT that applies the flatMap function to the wrapped List
     */
    public <B> ListT<W,B> flatMapT(final Function<? super T, ListT<W,B>> f) {

        return of(run.map(list -> list.flatMap(a -> f.apply(a).run.stream())
                                      .flatMap(a -> a.stream())));
    }

    

   

    /**
     * Construct an ListT from an AnyM that contains a monad type that contains type other than List
     * The values in the underlying monad will be mapped to List<A>
     * 
     * @param anyM AnyM that doesn't contain a monad wrapping an List
     * @return ListT
     */
    public static <W extends WitnessType<W>,A> ListT<W,A> fromAnyM(final AnyM<W,A> anyM) {
        return of(anyM.map(ListX::of));
    }
    
    /**
     * Construct an ListT from an AnyM that wraps a monad containing  Lists
     * 
     * @param monads AnyM that contains a monad wrapping an List
     * @return ListT
     */
    public static <W extends WitnessType<W>,A> ListT<W,A> of(final AnyM<W,? extends FluentSequenceX<A>> monads) {
        return new ListT<>(
                              monads);
    }
    public static <W extends WitnessType<W>,A> ListT<W,A> ofList(final AnyM<W,? extends List<A>> monads) {
        return new ListT<>(
                              monads.map(ListX::fromIterable));
    }

    

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("ListT[%s]", run);

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Unit#unit(java.lang.Object)
     */
    public <T> ListT<W,T> unit(final T unit) {
        return of(run.unit(ListX.of(unit)));
    }

    @Override
    public ReactiveSeq<T> stream() {
        return run.stream()
                  .flatMapIterable(e -> e);
    }

    @Override
    public Iterator<T> iterator() {
        return stream().iterator();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.stream.CyclopsCollectable#collectable()
     
    @Override
    public Collectable<T> collectable() {
       return this;
    } */
    @Override
    public <R> ListT<W,R> unitIterator(final Iterator<R> it) {  
        return of(run.unitIterator(it)
                     .map(i -> ListX.of(i)));
    }

    @Override
    public <R> ListT<W,R> empty() {
        return of(run.empty());
    }

    @Override
    public AnyM<W,? extends IterableFoldable<T>> nestedFoldables() {
        return run;

    }

    @Override
    public AnyM<W,? extends CyclopsCollectable<T>> nestedCollectables() {
        return run;

    }

    @Override
    public <T> ListT<W,T> unitAnyM(final AnyM<W,Traversable<T>> traversable) {

        return of((AnyMSeq) traversable.map(t -> ListX.fromIterable(t)));
    }

    @Override
    public AnyM<W,? extends Traversable<T>> transformerStream() {

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
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    public ListT<W,T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {

        return (ListT<W,T>) FoldableTransformerSeq.super.combine(predicate, op);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#cycle(int)
     */
    @Override
    public ListT<W,T> cycle(final int times) {

        return (ListT<W,T>) FoldableTransformerSeq.super.cycle(times);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#cycle(com.aol.cyclops.Monoid, int)
     */
    @Override
    public ListT<W,T> cycle(final Monoid<T> m, final int times) {

        return (ListT<W,T>) FoldableTransformerSeq.super.cycle(m, times);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#cycleWhile(java.util.function.Predicate)
     */
    @Override
    public ListT<W,T> cycleWhile(final Predicate<? super T> predicate) {

        return (ListT<W,T>) FoldableTransformerSeq.super.cycleWhile(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#cycleUntil(java.util.function.Predicate)
     */
    @Override
    public ListT<W,T> cycleUntil(final Predicate<? super T> predicate) {

        return (ListT<W,T>) FoldableTransformerSeq.super.cycleUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    public <U, R> ListT<W,R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (ListT<W,R>) FoldableTransformerSeq.super.zip(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.ListT#zip(java.util.stream.Stream, java.util.function.BiFunction)
     */
    @Override
    public <U, R> ListT<W,R> zip(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (ListT<W,R>) FoldableTransformerSeq.super.zip(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.ListT#zip(org.jooq.lambda.Seq, java.util.function.BiFunction)
     */
    @Override
    public <U, R> ListT<W,R> zip(final Seq<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (ListT<W,R>) FoldableTransformerSeq.super.zip(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#zipStream(java.util.stream.Stream)
     */
    @Override
    public <U> ListT<W,Tuple2<T, U>> zip(final Stream<? extends U> other) {

        return (ListT) FoldableTransformerSeq.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.ListT#zip(java.lang.Iterable)
     */
    @Override
    public <U> ListT<W,Tuple2<T, U>> zip(final Iterable<? extends U> other) {

        return (ListT) FoldableTransformerSeq.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#zip(org.jooq.lambda.Seq)
     */
    @Override
    public <U> ListT<W,Tuple2<T, U>> zip(final Seq<? extends U> other) {

        return (ListT) FoldableTransformerSeq.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <S, U> ListT<W,Tuple3<T, S, U>> zip3(final Stream<? extends S> second, final Stream<? extends U> third) {

        return (ListT) FoldableTransformerSeq.super.zip3(second, third);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <T2, T3, T4> ListT<W,Tuple4<T, T2, T3, T4>> zip4(final Stream<? extends T2> second, final Stream<? extends T3> third,
            final Stream<? extends T4> fourth) {

        return (ListT) FoldableTransformerSeq.super.zip4(second, third, fourth);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#zipWithIndex()
     */
    @Override
    public ListT<W,Tuple2<T, Long>> zipWithIndex() {

        return (ListT<W,Tuple2<T, Long>>) FoldableTransformerSeq.super.zipWithIndex();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#sliding(int)
     */
    @Override
    public ListT<W,ListX<T>> sliding(final int windowSize) {

        return (ListT<W,ListX<T>>) FoldableTransformerSeq.super.sliding(windowSize);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#sliding(int, int)
     */
    @Override
    public ListT<W,ListX<T>> sliding(final int windowSize, final int increment) {

        return (ListT<W,ListX<T>>) FoldableTransformerSeq.super.sliding(windowSize, increment);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#grouped(int, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> ListT<W,C> grouped(final int size, final Supplier<C> supplier) {

        return (ListT<W,C>) FoldableTransformerSeq.super.grouped(size, supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#groupedUntil(java.util.function.Predicate)
     */
    @Override
    public ListT<W,ListX<T>> groupedUntil(final Predicate<? super T> predicate) {

        return (ListT<W,ListX<T>>) FoldableTransformerSeq.super.groupedUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#groupedStatefullyUntil(java.util.function.BiPredicate)
     */
    @Override
    public ListT<W,ListX<T>> groupedStatefullyUntil(final BiPredicate<ListX<? super T>, ? super T> predicate) {

        return (ListT<W,ListX<T>>) FoldableTransformerSeq.super.groupedStatefullyUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#groupedWhile(java.util.function.Predicate)
     */
    @Override
    public ListT<W,ListX<T>> groupedWhile(final Predicate<? super T> predicate) {

        return (ListT<W,ListX<T>>) FoldableTransformerSeq.super.groupedWhile(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> ListT<W,C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (ListT<W,C>) FoldableTransformerSeq.super.groupedWhile(predicate, factory);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> ListT<W,C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (ListT<W,C>) FoldableTransformerSeq.super.groupedUntil(predicate, factory);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#grouped(int)
     */
    @Override
    public ListT<W,ListX<T>> grouped(final int groupSize) {

        return (ListT<W,ListX<T>>) FoldableTransformerSeq.super.grouped(groupSize);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#grouped(java.util.function.Function, java.util.stream.Collector)
     */
    @Override
    public <K, A, D> ListT<W,Tuple2<K, D>> grouped(final Function<? super T, ? extends K> classifier, final Collector<? super T, A, D> downstream) {

        return (ListT) FoldableTransformerSeq.super.grouped(classifier, downstream);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#grouped(java.util.function.Function)
     */
    @Override
    public <K> ListT<W,Tuple2<K, Seq<T>>> grouped(final Function<? super T, ? extends K> classifier) {

        return (ListT) FoldableTransformerSeq.super.grouped(classifier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#distinct()
     */
    @Override
    public ListT<W,T> distinct() {

        return (ListT<W,T>) FoldableTransformerSeq.super.distinct();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#scanLeft(com.aol.cyclops.Monoid)
     */
    @Override
    public ListT<W,T> scanLeft(final Monoid<T> monoid) {

        return (ListT<W,T>) FoldableTransformerSeq.super.scanLeft(monoid);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> ListT<W,U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {

        return (ListT<W,U>) FoldableTransformerSeq.super.scanLeft(seed, function);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#scanRight(com.aol.cyclops.Monoid)
     */
    @Override
    public ListT<W,T> scanRight(final Monoid<T> monoid) {

        return (ListT<W,T>) FoldableTransformerSeq.super.scanRight(monoid);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> ListT<W,U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {

        return (ListT<W,U>) FoldableTransformerSeq.super.scanRight(identity, combiner);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#sorted()
     */
    @Override
    public ListT<W,T> sorted() {

        return (ListT<W,T>) FoldableTransformerSeq.super.sorted();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#sorted(java.util.Comparator)
     */
    @Override
    public ListT<W,T> sorted(final Comparator<? super T> c) {

        return (ListT<W,T>) FoldableTransformerSeq.super.sorted(c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#takeWhile(java.util.function.Predicate)
     */
    @Override
    public ListT<W,T> takeWhile(final Predicate<? super T> p) {

        return (ListT<W,T>) FoldableTransformerSeq.super.takeWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#dropWhile(java.util.function.Predicate)
     */
    @Override
    public ListT<W,T> dropWhile(final Predicate<? super T> p) {

        return (ListT<W,T>) FoldableTransformerSeq.super.dropWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#takeUntil(java.util.function.Predicate)
     */
    @Override
    public ListT<W,T> takeUntil(final Predicate<? super T> p) {

        return (ListT<W,T>) FoldableTransformerSeq.super.takeUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#dropUntil(java.util.function.Predicate)
     */
    @Override
    public ListT<W,T> dropUntil(final Predicate<? super T> p) {

        return (ListT<W,T>) FoldableTransformerSeq.super.dropUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#dropRight(int)
     */
    @Override
    public ListT<W,T> dropRight(final int num) {

        return (ListT<W,T>) FoldableTransformerSeq.super.dropRight(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#takeRight(int)
     */
    @Override
    public ListT<W,T> takeRight(final int num) {

        return (ListT<W,T>) FoldableTransformerSeq.super.takeRight(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#skip(long)
     */
    @Override
    public ListT<W,T> skip(final long num) {

        return (ListT<W,T>) FoldableTransformerSeq.super.skip(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#skipWhile(java.util.function.Predicate)
     */
    @Override
    public ListT<W,T> skipWhile(final Predicate<? super T> p) {

        return (ListT<W,T>) FoldableTransformerSeq.super.skipWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#skipUntil(java.util.function.Predicate)
     */
    @Override
    public ListT<W,T> skipUntil(final Predicate<? super T> p) {

        return (ListT<W,T>) FoldableTransformerSeq.super.skipUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#limit(long)
     */
    @Override
    public ListT<W,T> limit(final long num) {

        return (ListT<W,T>) FoldableTransformerSeq.super.limit(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#limitWhile(java.util.function.Predicate)
     */
    @Override
    public ListT<W,T> limitWhile(final Predicate<? super T> p) {

        return (ListT<W,T>) FoldableTransformerSeq.super.limitWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#limitUntil(java.util.function.Predicate)
     */
    @Override
    public ListT<W,T> limitUntil(final Predicate<? super T> p) {

        return (ListT<W,T>) FoldableTransformerSeq.super.limitUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#intersperse(java.lang.Object)
     */
    @Override
    public ListT<W,T> intersperse(final T value) {

        return (ListT<W,T>) FoldableTransformerSeq.super.intersperse(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#reverse()
     */
    @Override
    public ListT<W,T> reverse() {

        return (ListT<W,T>) FoldableTransformerSeq.super.reverse();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#shuffle()
     */
    @Override
    public ListT<W,T> shuffle() {

        return (ListT<W,T>) FoldableTransformerSeq.super.shuffle();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#skipLast(int)
     */
    @Override
    public ListT<W,T> skipLast(final int num) {

        return (ListT<W,T>) FoldableTransformerSeq.super.skipLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#limitLast(int)
     */
    @Override
    public ListT<W,T> limitLast(final int num) {

        return (ListT<W,T>) FoldableTransformerSeq.super.limitLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#onEmpty(java.lang.Object)
     */
    @Override
    public ListT<W,T> onEmpty(final T value) {

        return (ListT<W,T>) FoldableTransformerSeq.super.onEmpty(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    public ListT<W,T> onEmptyGet(final Supplier<? extends T> supplier) {

        return (ListT<W,T>) FoldableTransformerSeq.super.onEmptyGet(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    public <X extends Throwable> ListT<W,T> onEmptyThrow(final Supplier<? extends X> supplier) {

        return (ListT<W,T>) FoldableTransformerSeq.super.onEmptyThrow(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#shuffle(java.util.Random)
     */
    @Override
    public ListT<W,T> shuffle(final Random random) {

        return (ListT<W,T>) FoldableTransformerSeq.super.shuffle(random);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#slice(long, long)
     */
    @Override
    public ListT<W,T> slice(final long from, final long to) {

        return (ListT<W,T>) FoldableTransformerSeq.super.slice(from, to);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#sorted(java.util.function.Function)
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

}