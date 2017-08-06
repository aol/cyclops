package cyclops.collections.box;

import com.aol.cyclops2.types.functor.Transformable;
import cyclops.stream.ReactiveSeq;
import cyclops.control.Trampoline;
import com.aol.cyclops2.types.foldable.To;
import com.aol.cyclops2.types.Zippable;
import lombok.ToString;

import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A class that represents an 'immutable' value that is generated inside a lambda
 * expression, but is accessible outside it
 * 
 * It will only allow it's value to be set once. Unfortunately the compiler won't be
 * able to tell if setOnce is called more than once
 * 
 * example usage
 * 
 * <pre>
 * {@code
 * public static <T> Supplier<T> memoiseSupplier(Supplier<T> s){
		LazyImmutable<T> maybe = LazyImmutable.def();
		return () -> maybe.computeIfAbsent(s);
	}
 * }</pre>
 * 
 * Has map and flatMap methods, but is not a Monad (see example usage above for why, it is the initial mutation that is valuable).
 * 
 * @author johnmcclean
 *
 * @param <T>
 */
@ToString

public class LazyImmutable<T> implements To<LazyImmutable<T>>,Supplier<T>, Consumer<T>, Transformable<T>, Zippable<T> {
    private final static Object UNSET = new Object();
    private final AtomicReference value = new AtomicReference<>(
                                                                UNSET);
    private final AtomicBoolean set = new AtomicBoolean(
                                                        false);

    public LazyImmutable() {
    }

    /**
     * @return Current value
     */
    @Override
    public T get() {
        return (T) value.get();
    }

    /**
     * Create an intermediate unbound (or unitialised) ImmutableClosedValue)
     *
     * @return unitialised ImmutableClosedValue
     */
    public static <T> LazyImmutable<T> unbound() {
        return new LazyImmutable<>();
    }

    /**
     * @param value Create an initialised ImmutableClosedValue with specified value
     * @return Initialised ImmutableClosedValue
     */
    public static <T> LazyImmutable<T> of(final T value) {
        final LazyImmutable<T> v = new LazyImmutable<>();
        v.setOnce(value);
        return v;
    }

    /**
     * @return a defined, but unitialised LazyImmutable
     */
    public static <T> LazyImmutable<T> def() {
        return new LazyImmutable<>();
    }

    /**
     * Map the value stored in this Immutable Closed Value from one Value to another
     * If this is an unitiatilised ImmutableClosedValue, an uninitialised closed value will be returned instead
     * 
     * @param fn Mapper function
     * @return new ImmutableClosedValue with new mapped value 
     */
    @Override
    public <R> LazyImmutable<R> map(final Function<? super T, ? extends R> fn) {
        final T val = get();
        if (val == UNSET)
            return (LazyImmutable<R>) this;
        else
            return LazyImmutable.of(fn.apply(val));
    }



    /**
     * FlatMap the value stored in Immutable Closed Value from one Value to another
     *  If this is an unitiatilised ImmutableClosedValue, an uninitialised closed value will be returned instead
     * 
     * @param fn  Flat Mapper function
     * @return new ImmutableClosedValue with new mapped value 
     */
    public <R> LazyImmutable<? extends R> flatMap(final Function<? super T, ? extends LazyImmutable<? extends R>> fn) {

        final T val = get();
        if (val == UNSET)
            return (LazyImmutable<R>) this;
        else
            return fn.apply(val);
    }

    /**
     * 
     * Set the value of this ImmutableClosedValue
     * If it has already been set will throw an exception
     * 
     * @param val Value to set to
     * @return Current set Value
     */
    public LazyImmutable<T> setOnce(final T val) {
        this.value.compareAndSet(UNSET, val);
        set.set(true);
        return this;

    }

    private T setOnceFromSupplier(final Supplier<T> lazy) {

        this.value.compareAndSet(UNSET, lazy.get());
        return (T) this.value.get();

    }

    /**
     * Get the current value or set if it has not been set yet
     * 
     * @param lazy Supplier to generate new value
     * @return Current value
     */
    public T computeIfAbsent(final Supplier<T> lazy) {
        final T val = get();
        if (val == UNSET)
            return setOnceFromSupplier(lazy);

        return val;

    }

    /**
     * @return true if this LazyImmutable value has been set
     */
    public boolean isSet() {
        return this.set.get();
    }

    /* (non-Javadoc)
     * @see java.util.function.Consumer#accept(java.lang.Object)
     */
    @Override
    public void accept(final T t) {
        setOnce(t);

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Value#reactiveStream()
     */
    public ReactiveSeq<T> stream() {
        return ReactiveSeq.generate(this)
                          .limit(1);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.control.Matchable.ValueAndOptionalMatcher#iterator()
     */
    @Override
    public Iterator<T> iterator() {
        return stream().iterator();
    }




    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Transformable#cast(java.lang.Class)
     */
    @Override
    public <U> LazyImmutable<U> cast(final Class<? extends U> type) {

        return (LazyImmutable<U>) Zippable.super.cast(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Transformable#peek(java.util.function.Consumer)
     */
    @Override
    public LazyImmutable<T> peek(final Consumer<? super T> c) {

        return (LazyImmutable<T>) Zippable.super.peek(c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Transformable#trampoline(java.util.function.Function)
     */
    @Override
    public <R> LazyImmutable<R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {

        return (LazyImmutable<R>) Zippable.super.trampoline(mapper);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return value.get()
                    .hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof LazyImmutable))
            return false;
        return Objects.equals(this.value.get(), ((LazyImmutable) obj).value.get());
    }

}
