package cyclops.higherkindedtypes;

import com.aol.cyclops2.hkt.Higher;

        import java.util.Optional;
import java.util.function.*;


import cyclops.Monoids;
import cyclops.control.Maybe;
import cyclops.function.Monoid;
import cyclops.typeclasses.Pure;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.instances.General;
import cyclops.typeclasses.monad.*;
import lombok.AccessLevel;
        import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

/**
 * Simulates Higher Kinded Types for Optional's
 *
 * OptionalKind is a Optional and a Higher Kinded Type (OptionalKind.µ,T)
 *
 * @author johnmcclean
 *
 * @param <T> Data type stored within the Optional
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class OptionalKind<T> implements Higher<OptionalKind.µ, T> {
    private final Optional<T> boxed;

    /**
     * Witness type
     *
     * @author johnmcclean
     *
     */
    public static class µ {
    }
    /**
     * @return An HKT encoded empty Optional
     */
    public static <T> OptionalKind<T> empty() {
        return widen(Optional.empty());
    }
    /**
     * @param value Value to embed in an Optional
     * @return An HKT encoded Optional
     */
    public static <T> OptionalKind<T> of(T value) {
        return widen(Optional.of(value));
    }
    /**
     * Convert a Optional to a simulated HigherKindedType that captures Optional nature
     * and Optional element data type separately. Recover via @see OptionalKind#narrow
     *
     * If the supplied Optional implements OptionalKind it is returned already, otherwise it
     * is wrapped into a Optional implementation that does implement OptionalKind
     *
     * @param Optional Optional to widen to a OptionalKind
     * @return OptionalKind encoding HKT info about Optionals
     */
    public static <T> OptionalKind<T> widen(final Optional<T> Optional) {

        return new OptionalKind<T>(Optional);
    }
    /**
     * Convert the raw Higher Kinded Type for OptionalKind types into the OptionalKind type definition class
     *
     * @param future HKT encoded list into a OptionalKind
     * @return OptionalKind
     */
    public static <T> OptionalKind<T> narrow(final Higher<OptionalKind.µ, T> future) {
        return (OptionalKind<T>)future;
    }
    /**
     * Convert the HigherKindedType definition for a Optional into
     *
     * @param Optional Type Constructor to convert back into narrowed type
     * @return Optional from Higher Kinded Type
     */
    public static <T> Optional<T> narrowK(final Higher<OptionalKind.µ, T> Optional) {
        //has to be an OptionalKind as only OptionalKind can implement Higher<OptionalKind.µ, T>
        return ((OptionalKind<T>)Optional).boxed;

    }
    public boolean isPresent() {
        return boxed.isPresent();
    }
    public T get() {
        return boxed.get();
    }
    public void ifPresent(Consumer<? super T> consumer) {
        boxed.ifPresent(consumer);
    }
    public Optional<T> filter(Predicate<? super T> predicate) {
        return boxed.filter(predicate);
    }
    public <U> Optional<U> map(Function<? super T, ? extends U> mapper) {
        return boxed.map(mapper);
    }
    public <U> Optional<U> flatMap(Function<? super T, Optional<U>> mapper) {
        return boxed.flatMap(mapper);
    }
    public T orElse(T other) {
        return boxed.orElse(other);
    }
    public T orElseGet(Supplier<? extends T> other) {
        return boxed.orElseGet(other);
    }
    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        return boxed.orElseThrow(exceptionSupplier);
    }
    public boolean equals(Object obj) {
        return boxed.equals(obj);
    }
    public int hashCode() {
        return boxed.hashCode();
    }
    public String toString() {
        return boxed.toString();
    }



}