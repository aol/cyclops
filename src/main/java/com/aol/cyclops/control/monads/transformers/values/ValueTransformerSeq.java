package com.aol.cyclops.control.monads.transformers.values;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.Reducer;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.Ior;
import com.aol.cyclops.control.LazyReact;
import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.SimpleReact;
import com.aol.cyclops.control.Streamable;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.control.Try;
import com.aol.cyclops.control.Validator;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.data.LazyImmutable;
import com.aol.cyclops.data.Mutable;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.persistent.PBagX;
import com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX;
import com.aol.cyclops.data.collections.extensions.persistent.PQueueX;
import com.aol.cyclops.data.collections.extensions.persistent.PSetX;
import com.aol.cyclops.data.collections.extensions.persistent.PStackX;
import com.aol.cyclops.data.collections.extensions.persistent.PVectorX;
import com.aol.cyclops.data.collections.extensions.standard.DequeX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.MapX;
import com.aol.cyclops.data.collections.extensions.standard.QueueX;
import com.aol.cyclops.data.collections.extensions.standard.SetX;
import com.aol.cyclops.data.collections.extensions.standard.SortedSetX;
import com.aol.cyclops.types.Combiner;
import com.aol.cyclops.types.Filterable;
import com.aol.cyclops.types.Foldable;
import com.aol.cyclops.types.Functor;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.Value;
import com.aol.cyclops.types.Zippable;
import com.aol.cyclops.types.anyM.WitnessType;
import com.aol.cyclops.types.futurestream.LazyFutureStream;
import com.aol.cyclops.types.futurestream.SimpleReactStream;
import com.aol.cyclops.types.stream.HotStream;
import com.aol.cyclops.types.stream.reactive.ValueSubscriber;
import com.aol.cyclops.util.function.QuadFunction;
import com.aol.cyclops.util.function.TriFunction;

public interface ValueTransformerSeq<W extends WitnessType,T> extends Publisher<T> {
    public <R> ValueTransformerSeq<W,R> empty();
    public <R> ValueTransformerSeq<W,R> flatMap(final Function<? super T, ? extends MonadicValue<? extends R>> f);
    AnyM<W,? extends MonadicValue<T>> transformerStream();
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#combine(com.aol.cyclops.types.Value, java.util.function.BiFunction)
     */
    default <T2, R> AnyM<W,? extends MonadicValue<R>> combine(Value<? extends T2> app,
            BiFunction<? super T, ? super T2, ? extends R> fn) {
       
        return this.transformerStream().map(v->v.combine(app, fn));
    }
    
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#subscribe(org.reactivestreams.Subscriber)
     */
   @Override
    default void subscribe(final Subscriber<? super T> s) {

        stream().subscribe(s);

    }

   

   // <T> TransformerSeq<W,T> unitStream(ReactiveSeq<T> traversable);
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Combiner#combine(java.util.function.BinaryOperator, com.aol.cyclops.types.Combiner)
     */
   
    default  AnyM<W,? extends Combiner<T>> combine(BinaryOperator<Combiner<T>> combiner, Combiner<T> app) {
        return this.transformerStream().map(v->v.combine(combiner, app));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#iterator()
     */
   
    default AnyM<W,? extends Iterator<T>> iterator() {
        
        return this.transformerStream().map(v->v.iterator());
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#test(java.lang.Object)
     */
   
    default AnyM<W,Boolean> test(T t) {
        
        return this.transformerStream().map(v->v.test(t));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#newSubscriber()
     */
   
    default AnyM<W,? extends ValueSubscriber<T>> newSubscriber() {
        return this.transformerStream().map(v->v.newSubscriber());
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#stream()
     */
   //Return StreamT
    default AnyM<W,? extends ReactiveSeq<T>> stream() {
        return this.transformerStream().map(v->v.stream());
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#unapply()
     */
   
    default AnyM<W,ListX<?>> unapply() {
        return this.transformerStream().map(v->v.unapply());
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#iterate(java.util.function.UnaryOperator)
     */
  //Return StreamT
    default AnyM<W,? extends ReactiveSeq<T>> iterate(UnaryOperator<T> fn) {
        
        return this.transformerStream().map(v->v.iterate(fn));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#generate()
     */
    //Return StreamT
    default AnyM<W,? extends ReactiveSeq<T>> generate() {
        
        return this.transformerStream().map(v->v.generate());
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#mapReduce(com.aol.cyclops.Reducer)
     */
   
    default <E> AnyM<W,? extends E> mapReduce(Reducer<E> monoid) {
        
        return this.transformerStream().map(v->v.mapReduce(monoid));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#fold(com.aol.cyclops.Monoid)
     */
   
    default AnyM<W,? extends T> fold(Monoid<T> monoid) {
        return this.transformerStream().map(v->v.fold(monoid));
     
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#fold(java.lang.Object, java.util.function.BinaryOperator)
     */
   
    default T fold(T identity, BinaryOperator<T> accumulator) {
        
        return MonadicValue.super.fold(identity, accumulator);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toLazyImmutable()
     */
   
    default LazyImmutable<T> toLazyImmutable() {
        
        return MonadicValue.super.toLazyImmutable();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toMutable()
     */
   
    default Mutable<T> toMutable() {
        
        return MonadicValue.super.toMutable();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toXor()
     */
   
    default Xor<?, T> toXor() {
        
        return MonadicValue.super.toXor();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toXor(java.lang.Object)
     */
   
    default <ST> Xor<ST, T> toXor(ST secondary) {
        
        return MonadicValue.super.toXor(secondary);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toTry(java.lang.Throwable)
     */
   
    default <X extends Throwable> Try<T, X> toTry(X throwable) {
        
        return MonadicValue.super.toTry(throwable);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toTry()
     */
   
    default Try<T, Throwable> toTry() {
        
        return MonadicValue.super.toTry();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toTry(java.lang.Class[])
     */
   
    default <X extends Throwable> Try<T, X> toTry(Class<X>... classes) {
        
        return MonadicValue.super.toTry(classes);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toIor()
     */
   
    default Ior<?, T> toIor() {
        
        return MonadicValue.super.toIor();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toEvalNow()
     */
   
    default Eval<T> toEvalNow() {
        
        return MonadicValue.super.toEvalNow();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toEvalLater()
     */
   
    default Eval<T> toEvalLater() {
        
        return MonadicValue.super.toEvalLater();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toEvalAlways()
     */
   
    default Eval<T> toEvalAlways() {
        
        return MonadicValue.super.toEvalAlways();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toMaybe()
     */
   
    default Maybe<T> toMaybe() {
        
        return MonadicValue.super.toMaybe();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toListX()
     */
   
    default ListX<T> toListX() {
        
        return MonadicValue.super.toListX();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toSetX()
     */
   
    default SetX<T> toSetX() {
        
        return MonadicValue.super.toSetX();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toSortedSetX()
     */
   
    default SortedSetX<T> toSortedSetX() {
        
        return MonadicValue.super.toSortedSetX();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toQueueX()
     */
   
    default QueueX<T> toQueueX() {
        
        return MonadicValue.super.toQueueX();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toDequeX()
     */
   
    default DequeX<T> toDequeX() {
        
        return MonadicValue.super.toDequeX();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toPStackX()
     */
   
    default PStackX<T> toPStackX() {
        
        return MonadicValue.super.toPStackX();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toPVectorX()
     */
   
    default PVectorX<T> toPVectorX() {
        
        return MonadicValue.super.toPVectorX();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toPQueueX()
     */
   
    default PQueueX<T> toPQueueX() {
        
        return MonadicValue.super.toPQueueX();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toPSetX()
     */
   
    default PSetX<T> toPSetX() {
        
        return MonadicValue.super.toPSetX();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toPOrderedSetX()
     */
   
    default POrderedSetX<T> toPOrderedSetX() {
        
        return MonadicValue.super.toPOrderedSetX();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toPBagX()
     */
   
    default PBagX<T> toPBagX() {
        
        return MonadicValue.super.toPBagX();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#mkString()
     */
   
    default String mkString() {
        
        return MonadicValue.super.mkString();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toFutureStream(com.aol.cyclops.control.LazyReact)
     */
   
    default LazyFutureStream<T> toFutureStream(LazyReact reactor) {
        
        return MonadicValue.super.toFutureStream(reactor);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toFutureStream()
     */
   
    default LazyFutureStream<T> toFutureStream() {
        
        return MonadicValue.super.toFutureStream();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toSimpleReact(com.aol.cyclops.control.SimpleReact)
     */
   
    default SimpleReactStream<T> toSimpleReact(SimpleReact reactor) {
        
        return MonadicValue.super.toSimpleReact(reactor);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toSimpleReact()
     */
   
    default SimpleReactStream<T> toSimpleReact() {
        
        return MonadicValue.super.toSimpleReact();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#collect(java.util.stream.Collector)
     */
   
    default <R, A> R collect(Collector<? super T, A, R> collector) {
        
        return MonadicValue.super.collect(collector);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toList()
     */
   
    default List<T> toList() {
        
        return MonadicValue.super.toList();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
   
    default <T2, R> Zippable<R> zip(Iterable<? extends T2> iterable,
            BiFunction<? super T, ? super T2, ? extends R> fn) {
        
        return MonadicValue.super.zip(iterable, fn);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(java.util.function.BiFunction, org.reactivestreams.Publisher)
     */
   
    default <T2, R> Zippable<R> zip(BiFunction<? super T, ? super T2, ? extends R> fn,
            Publisher<? extends T2> publisher) {
        
        return MonadicValue.super.zip(fn, publisher);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(org.jooq.lambda.Seq, java.util.function.BiFunction)
     */
   
    default <U, R> Zippable<R> zip(Seq<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        
        return MonadicValue.super.zip(other, zipper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(java.util.stream.Stream, java.util.function.BiFunction)
     */
   
    default <U, R> Zippable<R> zip(Stream<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        
        return MonadicValue.super.zip(other, zipper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(java.util.stream.Stream)
     */
   
    default <U> Zippable<Tuple2<T, U>> zip(Stream<? extends U> other) {
        
        return MonadicValue.super.zip(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(org.jooq.lambda.Seq)
     */
   
    default <U> Zippable<Tuple2<T, U>> zip(Seq<? extends U> other) {
        
        return MonadicValue.super.zip(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(java.lang.Iterable)
     */
   
    default <U> Zippable<Tuple2<T, U>> zip(Iterable<? extends U> other) {
        
        return MonadicValue.super.zip(other);
    }
    /* (non-Javadoc)
     * @see java.util.function.Predicate#and(java.util.function.Predicate)
     */
   
    default Predicate<T> and(Predicate<? super T> other) {
        
        return MonadicValue.super.and(other);
    }
    /* (non-Javadoc)
     * @see java.util.function.Predicate#negate()
     */
   
    default Predicate<T> negate() {
        
        return MonadicValue.super.negate();
    }
    /* (non-Javadoc)
     * @see java.util.function.Predicate#or(java.util.function.Predicate)
     */
   
    default Predicate<T> or(Predicate<? super T> other) {
        
        return MonadicValue.super.or(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#cast(java.lang.Class)
     */
   
    default <U> Functor<U> cast(Class<? extends U> type) {
        
        return MonadicValue.super.cast(type);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#peek(java.util.function.Consumer)
     */
   
    default Functor<T> peek(Consumer<? super T> c) {
        
        return MonadicValue.super.peek(c);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#trampoline(java.util.function.Function)
     */
   
    default <R> Functor<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        
        return MonadicValue.super.trampoline(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#patternMatch(java.util.function.Function, java.util.function.Supplier)
     */
   
    default <R> Functor<R> patternMatch(Function<CheckValue1<T, R>, CheckValue1<T, R>> case1,
            Supplier<? extends R> otherwise) {
        
        return MonadicValue.super.patternMatch(case1, otherwise);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#ofType(java.lang.Class)
     */
   
    default <U> Filterable<U> ofType(Class<? extends U> type) {
        
        return MonadicValue.super.ofType(type);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#filterNot(java.util.function.Predicate)
     */
   
    default Filterable<T> filterNot(Predicate<? super T> predicate) {
        
        return MonadicValue.super.filterNot(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#notNull()
     */
   
    default Filterable<T> notNull() {
        
        return MonadicValue.super.notNull();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#foldable()
     */
   
    default Foldable<T> foldable() {
        
        return MonadicValue.super.foldable();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#mapReduce(java.util.function.Function, com.aol.cyclops.Monoid)
     */
   
    default <R> R mapReduce(Function<? super T, ? extends R> mapper, Monoid<R> reducer) {
        
        return MonadicValue.super.mapReduce(mapper, reducer);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#reduce(com.aol.cyclops.Monoid)
     */
   
    default T reduce(Monoid<T> reducer) {
        
        return MonadicValue.super.reduce(reducer);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#reduce(java.util.function.BinaryOperator)
     */
   
    default Optional<T> reduce(BinaryOperator<T> accumulator) {
        
        return MonadicValue.super.reduce(accumulator);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#reduce(java.lang.Object, java.util.function.BinaryOperator)
     */
   
    default T reduce(T identity, BinaryOperator<T> accumulator) {
        
        return MonadicValue.super.reduce(identity, accumulator);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#reduce(java.lang.Object, java.util.function.BiFunction)
     */
   
    default <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator) {
        
        return MonadicValue.super.reduce(identity, accumulator);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#reduce(java.lang.Object, java.util.function.BiFunction, java.util.function.BinaryOperator)
     */
   
    default <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
        
        return MonadicValue.super.reduce(identity, accumulator, combiner);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#reduce(java.util.stream.Stream)
     */
   
    default ListX<T> reduce(Stream<? extends Monoid<T>> reducers) {
        
        return MonadicValue.super.reduce(reducers);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#reduce(java.lang.Iterable)
     */
   
    default ListX<T> reduce(Iterable<? extends Monoid<T>> reducers) {
        
        return MonadicValue.super.reduce(reducers);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#foldRight(com.aol.cyclops.Monoid)
     */
   
    default T foldRight(Monoid<T> reducer) {
        
        return MonadicValue.super.foldRight(reducer);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#foldRight(java.lang.Object, java.util.function.BinaryOperator)
     */
   
    default T foldRight(T identity, BinaryOperator<T> accumulator) {
        
        return MonadicValue.super.foldRight(identity, accumulator);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#foldRight(java.lang.Object, java.util.function.BiFunction)
     */
   
    default <U> U foldRight(U identity, BiFunction<? super T, ? super U, ? extends U> accumulator) {
        
        return MonadicValue.super.foldRight(identity, accumulator);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#foldRightMapToType(com.aol.cyclops.Reducer)
     */
   
    default <T> T foldRightMapToType(Reducer<T> reducer) {
        
        return MonadicValue.super.foldRightMapToType(reducer);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#join()
     */
   
    default String join() {
        
        return MonadicValue.super.join();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#join(java.lang.String)
     */
   
    default String join(String sep) {
        
        return MonadicValue.super.join(sep);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#join(java.lang.String, java.lang.String, java.lang.String)
     */
   
    default String join(String sep, String start, String end) {
        
        return MonadicValue.super.join(sep, start, end);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#print(java.io.PrintStream)
     */
   
    default void print(PrintStream str) {
        
        MonadicValue.super.print(str);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#print(java.io.PrintWriter)
     */
   
    default void print(PrintWriter writer) {
        
        MonadicValue.super.print(writer);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#printOut()
     */
   
    default void printOut() {
        
        MonadicValue.super.printOut();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#printErr()
     */
   
    default void printErr() {
        
        MonadicValue.super.printErr();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#groupBy(java.util.function.Function)
     */
   
    default <K> MapX<K, List<T>> groupBy(Function<? super T, ? extends K> classifier) {
        
        return MonadicValue.super.groupBy(classifier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#findFirst()
     */
   
    default Optional<T> findFirst() {
        
        return MonadicValue.super.findFirst();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#findAny()
     */
   
    default Optional<T> findAny() {
        
        return MonadicValue.super.findAny();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#startsWithIterable(java.lang.Iterable)
     */
   
    default boolean startsWithIterable(Iterable<T> iterable) {
        
        return MonadicValue.super.startsWithIterable(iterable);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#startsWith(java.util.stream.Stream)
     */
   
    default boolean startsWith(Stream<T> stream) {
        
        return MonadicValue.super.startsWith(stream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#endsWithIterable(java.lang.Iterable)
     */
   
    default boolean endsWithIterable(Iterable<T> iterable) {
        
        return MonadicValue.super.endsWithIterable(iterable);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#endsWith(java.util.stream.Stream)
     */
   
    default boolean endsWith(Stream<T> stream) {
        
        return MonadicValue.super.endsWith(stream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#toLazyCollection()
     */
   
    default CollectionX<T> toLazyCollection() {
        
        return MonadicValue.super.toLazyCollection();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#toConcurrentLazyCollection()
     */
   
    default CollectionX<T> toConcurrentLazyCollection() {
        
        return MonadicValue.super.toConcurrentLazyCollection();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#toConcurrentLazyStreamable()
     */
   
    default Streamable<T> toConcurrentLazyStreamable() {
        
        return MonadicValue.super.toConcurrentLazyStreamable();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#firstValue()
     */
   
    default T firstValue() {
        
        return MonadicValue.super.firstValue();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#single()
     */
   
    default T single() {
        
        return MonadicValue.super.single();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#single(java.util.function.Predicate)
     */
   
    default T single(Predicate<? super T> predicate) {
        
        return MonadicValue.super.single(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#singleOptional()
     */
   
    default Optional<T> singleOptional() {
        
        return MonadicValue.super.singleOptional();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#get(long)
     */
   
    default Optional<T> get(long index) {
        
        return MonadicValue.super.get(index);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#schedule(java.lang.String, java.util.concurrent.ScheduledExecutorService)
     */
   
    default HotStream<T> schedule(String cron, ScheduledExecutorService ex) {
        
        return MonadicValue.super.schedule(cron, ex);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#scheduleFixedDelay(long, java.util.concurrent.ScheduledExecutorService)
     */
   
    default HotStream<T> scheduleFixedDelay(long delay, ScheduledExecutorService ex) {
        
        return MonadicValue.super.scheduleFixedDelay(delay, ex);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#scheduleFixedRate(long, java.util.concurrent.ScheduledExecutorService)
     */
   
    default HotStream<T> scheduleFixedRate(long rate, ScheduledExecutorService ex) {
        
        return MonadicValue.super.scheduleFixedRate(rate, ex);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#validate(com.aol.cyclops.control.Validator)
     */
   
    default <S, F> Ior<ReactiveSeq<F>, ReactiveSeq<S>> validate(Validator<T, S, F> validator) {
        
        return MonadicValue.super.validate(validator);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#xMatch(int, java.util.function.Predicate)
     */
   
    default boolean xMatch(int num, Predicate<? super T> c) {
        
        return MonadicValue.super.xMatch(num, c);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Convertable#collect(java.util.function.Supplier, java.util.function.BiConsumer, java.util.function.BiConsumer)
     */
   
    default <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
        
        return MonadicValue.super.collect(supplier, accumulator, combiner);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Convertable#visit(java.util.function.Function, java.util.function.Supplier)
     */
   
    default <R> R visit(Function<? super T, ? extends R> present, Supplier<? extends R> absent) {
        
        return MonadicValue.super.visit(present, absent);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Convertable#isPresent()
     */
   
    default boolean isPresent() {
        
        return MonadicValue.super.isPresent();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Convertable#orElseGet(java.util.function.Supplier)
     */
   
    default T orElseGet(Supplier<? extends T> value) {
        
        return MonadicValue.super.orElseGet(value);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Convertable#toOptional()
     */
   
    default Optional<T> toOptional() {
        
        return MonadicValue.super.toOptional();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Convertable#toStream()
     */
   
    default Stream<T> toStream() {
        
        return MonadicValue.super.toStream();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Convertable#toAtomicReference()
     */
   
    default AtomicReference<T> toAtomicReference() {
        
        return MonadicValue.super.toAtomicReference();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Convertable#toOptionalAtomicReference()
     */
   
    default Optional<AtomicReference<T>> toOptionalAtomicReference() {
        
        return MonadicValue.super.toOptionalAtomicReference();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Convertable#orElse(java.lang.Object)
     */
   
    default T orElse(T value) {
        
        return MonadicValue.super.orElse(value);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Convertable#orElseThrow(java.util.function.Supplier)
     */
   
    default <X extends Throwable> T orElseThrow(Supplier<? extends X> ex) throws X {
        
        return MonadicValue.super.orElseThrow(ex);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Convertable#toFutureW()
     */
   
    default FutureW<T> toFutureW() {
        
        return MonadicValue.super.toFutureW();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Convertable#toFutureWAsync()
     */
   
    default FutureW<T> toFutureWAsync() {
        
        return MonadicValue.super.toFutureWAsync();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Convertable#toFutureWAsync(java.util.concurrent.Executor)
     */
   
    default FutureW<T> toFutureWAsync(Executor ex) {
        
        return MonadicValue.super.toFutureWAsync(ex);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Convertable#toCompletableFuture()
     */
   
    default CompletableFuture<T> toCompletableFuture() {
        
        return MonadicValue.super.toCompletableFuture();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Convertable#toCompletableFutureAsync()
     */
   
    default CompletableFuture<T> toCompletableFutureAsync() {
        
        return MonadicValue.super.toCompletableFutureAsync();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Convertable#toCompletableFutureAsync(java.util.concurrent.Executor)
     */
   
    default CompletableFuture<T> toCompletableFutureAsync(Executor exec) {
        
        return MonadicValue.super.toCompletableFutureAsync(exec);
    }
    /* (non-Javadoc)
     * @see java.util.function.Supplier#get()
     */
   
    default T get() {
        
        return null;
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#filter(java.util.function.Predicate)
     */
   
    default MonadicValue<T> filter(Predicate<? super T> predicate) {
        
        return null;
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#unit(java.lang.Object)
     */
   
    default <T> MonadicValue<T> unit(T unit) {
        
        return null;
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#map(java.util.function.Function)
     */
   
    default <R> MonadicValue<R> map(Function<? super T, ? extends R> fn) {
        
        return null;
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#coflatMap(java.util.function.Function)
     */
   
    default <R> MonadicValue<R> coflatMap(Function<? super MonadicValue<T>, R> mapper) {
        
        return MonadicValue.super.coflatMap(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#nest()
     */
   
    default MonadicValue<MonadicValue<T>> nest() {
        
        return MonadicValue.super.nest();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops.util.function.TriFunction, com.aol.cyclops.util.function.QuadFunction)
     */
   
    default <T2, R1, R2, R3, R> MonadicValue<R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            TriFunction<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
            QuadFunction<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return MonadicValue.super.forEach4(value1, value2, value3, yieldingFunction);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops.util.function.TriFunction, com.aol.cyclops.util.function.QuadFunction, com.aol.cyclops.util.function.QuadFunction)
     */
   
    default <T2, R1, R2, R3, R> MonadicValue<R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            TriFunction<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
            QuadFunction<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
            QuadFunction<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return MonadicValue.super.forEach4(value1, value2, value3, filterFunction, yieldingFunction);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops.util.function.TriFunction)
     */
   
    default <T2, R1, R2, R> MonadicValue<R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            TriFunction<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        
        return MonadicValue.super.forEach3(value1, value2, yieldingFunction);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops.util.function.TriFunction, com.aol.cyclops.util.function.TriFunction)
     */
   
    default <T2, R1, R2, R> MonadicValue<R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            TriFunction<? super T, ? super R1, ? super R2, Boolean> filterFunction,
            TriFunction<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        
        return MonadicValue.super.forEach3(value1, value2, filterFunction, yieldingFunction);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
   
    default <R1, R> MonadicValue<R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        
        return MonadicValue.super.forEach2(value1, yieldingFunction);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
   
    default <R1, R> MonadicValue<R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, Boolean> filterFunction,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        
        return MonadicValue.super.forEach2(value1, filterFunction, yieldingFunction);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#combineEager(com.aol.cyclops.Monoid, com.aol.cyclops.types.MonadicValue)
     */
   
    default MonadicValue<T> combineEager(Monoid<T> monoid, MonadicValue<? extends T> v2) {
        
        return MonadicValue.super.combineEager(monoid, v2);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#flatMapIterable(java.util.function.Function)
     */
   
    default <R> MonadicValue<R> flatMapIterable(Function<? super T, ? extends Iterable<? extends R>> mapper) {
        
        return MonadicValue.super.flatMapIterable(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#flatMapPublisher(java.util.function.Function)
     */
   
    default <R> MonadicValue<R> flatMapPublisher(Function<? super T, ? extends Publisher<? extends R>> mapper) {
        
        return MonadicValue.super.flatMapPublisher(mapper);
    }

    

}
