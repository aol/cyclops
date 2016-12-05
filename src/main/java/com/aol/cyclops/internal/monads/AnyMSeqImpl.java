package com.aol.cyclops.internal.monads;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.internal.Monad;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.types.anyM.WitnessType;
import com.aol.cyclops.types.extensability.Comprehender;

public class AnyMSeqImpl<W extends WitnessType,T> extends BaseAnyMImpl<W,T>implements AnyMSeq<W,T> {

    protected AnyMSeqImpl(final Monad<T> monad, final Class initialType, Comprehender<T> adapter) {
        super(monad, initialType,adapter);

    }
    
    @Override
    public AnyMSeq<W,T> fromIterable(Iterable<T> t){
        return new MonadWrapper(adapter().fromIterator(t.iterator()),initialType,adapter).anyMSeq();
    }

    /**
    public static <W extends WitnessType,T> AnyMSeqImpl<W,T> from(final AnyMValue<W,T> value) {
        final AnyMValueImpl<T> impl = (AnyMValueImpl<T>) value;
        return new AnyMSeqImpl<W,T>(
                                  impl.monad, impl.initialType);
    }

**/
    private <T> AnyMSeqImpl<W,T> with(final Monad<T> anyM) {

        return new AnyMSeqImpl<>(
                                 anyM, initialType);
    }

    private <T> AnyMSeqImpl<W,T> with(final AnyM<W,T> anyM) {

        return (AnyMSeqImpl<W,T>) anyM;
    }

    @Override
    public AnyMSeq<W,T> peek(final Consumer<? super T> c) {
        return with(super.peekInternal(c));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.IterableFunctor#unitIterator(java.util.Iterator)
     */
    @Override
    public <U> AnyMSeq<W,U> unitIterator(final Iterator<U> it) {
        return fromIterable(() -> it);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.super.AnyMSeq#emptyUnit()
     */
    @Override
    public <T> AnyMSeq<W,T> emptyUnit() {
        return new AnyMSeqImpl(
                               monad.empty(), initialType,adapter);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.super.AnyMSeq#stream()
     */
    @Override
    public ReactiveSeq<T> stream() {
        return super.asSequence();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.super.AnyMSeq#unwrap()
     */
    @Override
    public <R> R unwrap() {
        return (R) super.unwrap();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.super.AnyMSeq#filter(java.util.function.Predicate)
     */
    @Override
    public AnyMSeq<W,T> filter(final Predicate<? super T> p) {
        return with(super.filterInternal(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.super.AnyMSeq#map(java.util.function.Function)
     */
    @Override
    public <R> AnyMSeq<W,R> map(final Function<? super T, ? extends R> fn) {
        return with(super.mapInternal(fn));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.super.AnyMSeq#bind(java.util.function.Function)
     */
    @Override
    public <R> AnyMSeq<W,R> bind(final Function<? super T, ?> fn) {
        return with(super.bindInternal(fn));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.super.AnyMSeq#flatten()
     */
    @Override
    public <T1> AnyMSeq<W,T1> flatten() {
        return with(super.flattenInternal());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.super.AnyMSeq#aggregate(com.aol.cyclops.control.AnyM)
     */
    @Override
    public AnyMSeq<W,List<T>> aggregate(final AnyM<W,T> next) {
        return with(super.aggregate(next));
    }


    /* (non-Javadoc)
     * @see com.aol.cyclops.types.super.AnyMSeq#flatMap(java.util.function.Function)
     */
    @Override
    public <R> AnyMSeq<W,R> flatMap(final Function<? super T, ? extends AnyM<? extends R>> fn) {
        return with(super.flatMapInternal(fn));

    }

    @Override
    public <R> AnyMSeq<W,R> flatMapFirst(final Function<? super T, ? extends Iterable<? extends R>> fn) {
        return with(super.flatMapInternal(fn.andThen(it -> fromIterable(it))));

    }

    @Override
    public <R> AnyMSeq<W,R> flatMapFirstPublisher(final Function<? super T, ? extends Publisher<? extends R>> fn) {
        return with(super.flatMapInternal(fn.andThen(it -> fromPublisher(it))));

    }

    @Override
    public Xor<AnyMValue<W,T>, AnyMSeq<W,T>> matchable() {
        return Xor.primary(this);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.super.AnyMSeq#unit(java.lang.Object)
     */
    @Override
    public <T> AnyMSeq<W,T> unit(final T value) {
        return AnyM.ofSeq(monad.unit(value));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.super.AnyMSeq#empty()
     */
    @Override
    public <T> AnyMSeq<W,T> empty() {
        return with(new AnyMSeqImpl(
                                    monad.empty(), initialType));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.super.AnyMSeq#replicateM(int)
     */
    @Override
    public AnyMSeq<W,T> replicateM(final int times) {
        return monad.replicateM(times)
                    .anyMSeq();
    }

    @Override
    public <R> AnyMSeq<W,R> applyM(final AnyM<W,Function<? super T, ? extends R>> fn) {
        return monad.applyM(((AnyMSeqImpl<W,Function<? super T, ? extends R>>) fn).monad())
                    .anyMSeq();

    }

    @Override
    public <NT> ReactiveSeq<NT> toReactiveSeq(final Function<? super T, ? extends Stream<? extends NT>> fn) {
        return super.toReactiveSeq(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.stream.reactive.ReactiveStreamsTerminalOperations#forEachX(long, java.util.function.Consumer)
     */
    @Override
    public <X extends Throwable> Subscription forEachX(final long numberOfElements, final Consumer<? super T> consumer) {
        return this.stream()
                   .forEachX(numberOfElements, consumer);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.stream.reactive.ReactiveStreamsTerminalOperations#forEachXWithError(long, java.util.function.Consumer, java.util.function.Consumer)
     */
    @Override
    public <X extends Throwable> Subscription forEachXWithError(final long numberOfElements, final Consumer<? super T> consumer,
            final Consumer<? super Throwable> consumerError) {
        return this.stream()
                   .forEachXWithError(numberOfElements, consumer, consumerError);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.stream.reactive.ReactiveStreamsTerminalOperations#forEachXEvents(long, java.util.function.Consumer, java.util.function.Consumer, java.lang.Runnable)
     */
    @Override
    public <X extends Throwable> Subscription forEachXEvents(final long numberOfElements, final Consumer<? super T> consumer,
            final Consumer<? super Throwable> consumerError, final Runnable onComplete) {
        return this.stream()
                   .forEachXEvents(numberOfElements, consumer, consumerError, onComplete);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.stream.reactive.ReactiveStreamsTerminalOperations#forEachWithError(java.util.function.Consumer, java.util.function.Consumer)
     */
    @Override
    public <X extends Throwable> void forEachWithError(final Consumer<? super T> consumerElement, final Consumer<? super Throwable> consumerError) {
        this.stream()
            .forEachWithError(consumerElement, consumerError);

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.stream.reactive.ReactiveStreamsTerminalOperations#forEachEvent(java.util.function.Consumer, java.util.function.Consumer, java.lang.Runnable)
     */
    @Override
    public <X extends Throwable> void forEachEvent(final Consumer<? super T> consumerElement, final Consumer<? super Throwable> consumerError,
            final Runnable onComplete) {
        this.stream()
            .forEachEvent(consumerElement, consumerError, onComplete);

    }

    @Override
    public String toString() {
        return String.format("AnyMSeq[%s]", monad);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(unwrap());
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof AnyMSeq))
            return false;
        return unwrap().equals(((AnyMSeq) o).unwrap());
    }

}
