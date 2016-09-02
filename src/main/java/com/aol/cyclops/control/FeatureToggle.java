package com.aol.cyclops.control;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.reactivestreams.Publisher;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.types.Filterable;
import com.aol.cyclops.types.Functor;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.MonadicValue1;
import com.aol.cyclops.types.Value;
import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.types.applicative.ApplicativeFunctor;
import com.aol.cyclops.util.function.Curry;

/**
 * Switch interface for handling features that can be enabled or disabled.
 * 
 * @author johnmcclean
 *
 * @param <F>
 */
/**
 * @author johnmcclean
 *
 * @param <F>
 */
public interface FeatureToggle<F>
        extends Supplier<F>, MonadicValue1<F>, Filterable<F>, Functor<F>, ApplicativeFunctor<F>, Matchable.ValueAndOptionalMatcher<F> {

    /**
     * @return true if enabled
     */
    boolean isEnabled();

    /**
     * @return false if disabled
     * 
     */
    boolean isDisabled();
    /**
     * Narrow covariant type parameter
     * 
     * @param toggle Eval with covariant type parameter
     * @return Narrowed FeatureToggle
     */
    static <T> FeatureToggle<T> narrow(FeatureToggle<? extends T> toggle) {
        return (FeatureToggle<T>) toggle;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#cast(java.lang.Class)
     */
    @Override
    default <U> FeatureToggle<U> cast(Class<? extends U> type) {
        return (FeatureToggle<U>) ApplicativeFunctor.super.cast(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#trampoline(java.util.function.Function)
     */
    @Override
    default <R> FeatureToggle<R> trampoline(Function<? super F, ? extends Trampoline<? extends R>> mapper) {

        return (FeatureToggle<R>) ApplicativeFunctor.super.trampoline(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#ofType(java.lang.Class)
     */
    @Override
    default <U> FeatureToggle<U> ofType(Class<? extends U> type) {

        return (FeatureToggle<U>) Filterable.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#filterNot(java.util.function.Predicate)
     */
    @Override
    default FeatureToggle<F> filterNot(Predicate<? super F> fn) {

        return (FeatureToggle<F>) Filterable.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#notNull()
     */
    @Override
    default FeatureToggle<F> notNull() {

        return (FeatureToggle<F>) Filterable.super.notNull();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#unit(java.lang.Object)
     */
    default <T> FeatureToggle<T> unit(T unit) {
        return FeatureToggle.enable(unit);
    }

    /**
     * If this FeatureToggle is enabled the enabled function will be executed
     * If this FeatureToggle is disabled the disabled function will be executed
     * 
     * @param enabled Function to execute if enabled
     * @param disabled Function to execute if disabled
     * @return
     */
    <R> R visit(Function<? super F, ? extends R> enabled, Function<? super F, ? extends R> disabled);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#patternMatch(java.util.function.Function, java.util.function.Supplier)
     */
    @Override
    default <R> FeatureToggle<R> patternMatch(Function<CheckValue1<F, R>, CheckValue1<F, R>> case1, Supplier<? extends R> otherwise) {

        return (FeatureToggle<R>) ApplicativeFunctor.super.patternMatch(case1, otherwise);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toFeatureToggle()
     */
    default FeatureToggle<F> toFeatureToggle() {
        return this;
    }

    /**
     * @return This monad, wrapped as AnyM
     */
    public AnyMValue<F> anyM();

    /**
     * @return This monad, wrapped as AnyM of Disabled
     */
    public AnyM<F> anyMDisabled();

    /**
     * @return This monad, wrapped as AnyM of Enabled
     */
    public AnyM<F> anyMEnabled();

    F get();

    /**
     * Create a new enabled switch
     * 
     * @param f switch value
     * @return enabled switch
     */
    public static <F> Enabled<F> enable(F f) {
        return new Enabled<F>(
                              f);
    }

    /**
     * Create a new disabled switch
     * 
     * @param f switch value
     * @return disabled switch
     */
    public static <F> Disabled<F> disable(F f) {
        return new Disabled<F>(
                               f);
    }

    /**
     * 
     * 
     * @param from Create a switch with the same type
     * @param f but with this value (f)
     * @return new switch
     */
    public static <F> FeatureToggle<F> from(FeatureToggle<F> from, F f) {
        if (from.isEnabled())
            return enable(f);
        return disable(f);
    }

    /**
     * Peek at current switch value
     * 
     * @param consumer Consumer to provide current value to
     * @return This Switch
     */
    @Override
    default FeatureToggle<F> peek(Consumer<? super F> consumer) {
        if (this.isEnabled())
            consumer.accept(get());
        return this;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#nest()
     */
    @Override
    default FeatureToggle<MonadicValue<F>> nest() {
       
        return (FeatureToggle<MonadicValue<F>>)MonadicValue1.super.nest();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue1#coflatMap(java.util.function.Function)
     */
    @Override
    default <R> FeatureToggle<R> coflatMap(Function<? super MonadicValue<F>, R> mapper) {
       
        return (FeatureToggle<R>)MonadicValue1.super.coflatMap(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue1#combineEager(com.aol.cyclops.Monoid, com.aol.cyclops.types.MonadicValue)
     */
    @Override
    default FeatureToggle<F> combineEager(Monoid<F> monoid, MonadicValue<? extends F> v2) {
        
        return (FeatureToggle<F>)MonadicValue1.super.combineEager(monoid, v2);
    }

    /**
     * @param flatMapper Create a new Switch with provided function
     * @return switch from function
     */
    @Override
    default <X> FeatureToggle<X> flatMap(Function<? super F, ? extends MonadicValue<? extends X>> flatMapper) {
        if (isDisabled())
            return (FeatureToggle<X>) this;
        return narrow(flatMapper.apply(get())
                         .toFeatureToggle());
    }

    /**
     * @param map transform the value inside this Switch into new Switch object
     * @return new Switch with transformed value
     */
    @Override
    default <X> FeatureToggle<X> map(Function<? super F, ? extends X> map) {
        if (isDisabled())
            return (FeatureToggle<X>) this;
        return enable(map.apply(get()));
    }

    /**
     * Filter this Switch. If current value does not meet criteria,
     * a disabled Switch is returned
     * 
     * @param p Predicate to test for
     * @return Filtered switch
     */
    @Override
    default FeatureToggle<F> filter(Predicate<? super F> p) {
        if (isDisabled())
            return this;
        if (!p.test(get()))
            return FeatureToggle.disable(get());
        return this;
    }

    /**
     * Iterate over value in switch (single value, so one iteration)
     * @param consumer to provide value to.
     */
    @Override
    default void forEach(Consumer<? super F> consumer) {
        if (isDisabled())
            return;
        consumer.accept(get());
    }

    /**
     * @return transform this Switch into an enabled Switch
     */
    default Enabled<F> enable() {
        return new Enabled<F>(
                              get());
    }

    /**
     * @return transform this Switch into a disabled Switch
     */
    default Disabled<F> disable() {
        return new Disabled<F>(
                               get());
    }

    /**
     * @return flip this Switch
     */
    default FeatureToggle<F> flip() {

        if (isEnabled())
            return disable();
        else
            return enable();
    }

    /**
     * @return Optional.empty() if disabled, Optional containing current value if enabled
     */
    default Optional<F> optional() {
        return stream().findFirst();
    }

    /**
     * @return emty Stream if disabled, Stream with current value if enabled.
     */
    @Override
    default ReactiveSeq<F> stream() {
        if (isEnabled())
            return ReactiveSeq.of(get());
        else
            return ReactiveSeq.of();
    }

    @Override
    default Iterator<F> iterator() {

        return Matchable.ValueAndOptionalMatcher.super.iterator();
    }

    /**
     * An enabled switch
     * 
     * <pre>
     * 
     * Switch&lt;Data&gt; data = Switch.enabled(data);
     * 
     * data.map(this::load);  //data will be loaded because Switch is of type Enabled
     * 
     * </pre>
     * @author johnmcclean
     *
     * @param <F> Type of value Enabled Switch holds
     */
    @lombok.Value
    public static class Enabled<F> implements FeatureToggle<F> {

        private final F enabled;

        /**
         * @return This monad, wrapped as AnyM
         */
        public AnyMValue<F> anyM() {
            return AnyM.ofValue(this);
        }

        /**
         * @return This monad, wrapped as AnyM of Disabled
         */
        public AnyMValue<F> anyMDisabled() {
            return AnyM.ofValue(Optional.empty());
        }

        /**
         * @return This monad, wrapped as AnyM of Enabled
         */
        public AnyMValue<F> anyMEnabled() {
            return anyM();
        }

        /**
         * Create a new enabled switch
         * 
         * @param f switch value
         * @return enabled switch
         */
        public static <F> Enabled<F> of(F f) {
            return new Enabled<F>(
                                  f);
        }

        /**
         * Create a new enabled switch
         * 
         * @param f switch value
         * @return enabled switch
         */
        public static <F> AnyM<F> anyMOf(F f) {
            return new Enabled<F>(
                                  f).anyM();
        }

        /* 
         *	@return
         * @see com.aol.cyclops.enableswitch.Switch#get()
         */
        public F get() {
            return enabled;
        }

        /**
         * Constructs an Enabled Switch
         *
         * @param enabled The value of this Enabled Switch
         */
        Enabled(F enabled) {
            this.enabled = enabled;
        }

        /* 
         * @param obj to check equality with
         * @return whether objects are equal
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            return (obj == this) || (obj instanceof Enabled && Objects.equals(enabled, ((Enabled<?>) obj).enabled));
        }

        /* 
         *
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return Objects.hashCode(enabled);
        }

        /*  
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return String.format("Enabled(%s)", enabled);
        }

        /* 
         *	@return true - is Enabled
         * @see com.aol.cyclops.enableswitch.Switch#isEnabled()
         */
        @Override
        public final boolean isEnabled() {
            return true;
        }

        /* 
         *	@return false - is not Disabled
         * @see com.aol.cyclops.enableswitch.Switch#isDisabled()
         */
        @Override
        public final boolean isDisabled() {

            return false;
        }

        /* (non-Javadoc)
         * @see com.aol.cyclops.featuretoggle.FeatureToggle#when(java.util.function.Function, java.util.function.Supplier)
         */
        @Override
        public <R> R visit(Function<? super F, ? extends R> enabled, Function<? super F, ? extends R> disabled) {
            return enabled.apply(get());
        }
    }

    /**
     * An disabled switch
     * 
     * <pre>
     * 
     * Switch&lt;Data&gt; data = Switch.disabled(data);
     * 
     * data.map(this::load);  //data will NOT be loaded because Switch is of type Disabled
     * 
     * </pre>
     * @author johnmcclean
     *
     * @param <F> Type of value Enabled Switch holds
     */
    @lombok.Value
    public static class Disabled<F> implements FeatureToggle<F> {

        private final F disabled;

        public Enabled<F> enable() {
            return new Enabled<F>(
                                  disabled);
        }

        /**
         * Constructs a left.
         *
         * @param disabled The value of this Left
         */
        Disabled(F disabled) {
            this.disabled = disabled;
        }

        @Override
        public boolean isPresent() {
            return false;
        }

        /**
         * @return This monad, wrapped as AnyM
         */
        public AnyMValue<F> anyM() {
            return AnyM.fromOptional(Optional.empty());
        }

        /**
         * @return This monad, wrapped as AnyM of Disabled
         */
        public AnyM<F> anyMDisabled() {
            return AnyM.ofValue(this);
        }

        /**
         * @return This monad, wrapped as AnyM of Enabled
         */
        public AnyM<F> anyMEnabled() {
            return anyM();
        }

        /**
         * Create a new disabled switch
         * 
         * @param f switch value
         * @return disabled switch
         */
        public static <F> Disabled<F> of(F f) {
            return new Disabled<F>(
                                   f);
        }

        /**
         * Create a new disabled switch
         * 
         * @param f switch value
         * @return disabled switch
         */
        public static <F> AnyM<F> anyMOf(F f) {
            return new Disabled<F>(
                                   f).anyM();
        }

        /* 
         *	@return value of this Disabled
         * @see com.aol.cyclops.enableswitch.Switch#get()
         */
        public F get() {
            Optional.ofNullable(null)
                    .get();
            return null;
        }

        /* 
         *
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            return (obj == this) || (obj instanceof Disabled && Objects.equals(disabled, ((Disabled<?>) obj).disabled));
        }

        /* 
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return Objects.hashCode(disabled);
        }

        /* 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return String.format("Disabled(%s)", disabled);
        }

        /* 
         *	@return false - is NOT enabled
         * @see com.aol.cyclops.enableswitch.Switch#isEnabled()
         */
        @Override
        public final boolean isEnabled() {
            return false;
        }

        /* 
         *	@return true - is Disabled
         * @see com.aol.cyclops.enableswitch.Switch#isDisabled()
         */
        @Override
        public final boolean isDisabled() {
            return true;
        }

        /* (non-Javadoc)
         * @see com.aol.cyclops.featuretoggle.FeatureToggle#when(java.util.function.Function, java.util.function.Supplier)
         */
        @Override
        public <R> R visit(Function<? super F, ? extends R> enabled, Function<? super F, ? extends R> disabled) {
            return disabled.apply(get());
        }
    }

    /**
     * Apply a function across to values at once. If this Featue is disabled, or the supplied value represents null, none or disabled then a disabled Feature is returned.
     * Otherwise a Maybe with the function applied with this value and the supplied value is returned
     * 
     * @param app
     * @param fn
     * @return
     */
    @Override
    default <T2, R> FeatureToggle<R> combine(Value<? extends T2> app, BiFunction<? super F, ? super T2, ? extends R> fn) {

        return map(v -> Tuple.tuple(v, Curry.curry2(fn)
                                            .apply(v))).flatMap(tuple -> app.visit(i -> Maybe.just(tuple.v2.apply(i)), () -> Maybe.none()));
    }

    /**
     * Equivalent to ap, but accepts an Iterable and takes the first value only from that iterable.
     * 
     * @param app
     * @param fn
     * @return
     */
    @Override
    default <T2, R> FeatureToggle<R> zip(Iterable<? extends T2> app, BiFunction<? super F, ? super T2, ? extends R> fn) {

        return map(v -> Tuple.tuple(v, Curry.curry2(fn)
                                            .apply(v))).flatMap(tuple -> Maybe.fromIterable(app)
                                                                              .visit(i -> Maybe.just(tuple.v2.apply(i)), () -> Maybe.none()));
    }

    /**
     * Equivalent to ap, but accepts a Publisher and takes the first value only from that publisher.
     * 
     * @param app
     * @param fn
     * @return
     */
    @Override
    default <T2, R> FeatureToggle<R> zip(BiFunction<? super F, ? super T2, ? extends R> fn, Publisher<? extends T2> app) {
        return map(v -> Tuple.tuple(v, Curry.curry2(fn)
                                            .apply(v))).flatMap(tuple -> Maybe.fromPublisher(app)
                                                                              .visit(i -> Maybe.just(tuple.v2.apply(i)), () -> Maybe.none()));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(org.jooq.lambda.Seq, java.util.function.BiFunction)
     */
    @Override
    default <U, R> FeatureToggle<R> zip(Seq<? extends U> other, BiFunction<? super F, ? super U, ? extends R> zipper) {

        return (FeatureToggle<R>) MonadicValue.super.zip(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(java.util.stream.Stream, java.util.function.BiFunction)
     */
    @Override
    default <U, R> FeatureToggle<R> zip(Stream<? extends U> other, BiFunction<? super F, ? super U, ? extends R> zipper) {

        return (FeatureToggle<R>) MonadicValue.super.zip(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(java.util.stream.Stream)
     */
    @Override
    default <U> FeatureToggle<Tuple2<F, U>> zip(Stream<? extends U> other) {

        return (FeatureToggle) MonadicValue.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(org.jooq.lambda.Seq)
     */
    @Override
    default <U> FeatureToggle<Tuple2<F, U>> zip(Seq<? extends U> other) {

        return (FeatureToggle) MonadicValue.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(java.lang.Iterable)
     */
    @Override
    default <U> FeatureToggle<Tuple2<F, U>> zip(Iterable<? extends U> other) {

        return (FeatureToggle) MonadicValue.super.zip(other);
    }

}
