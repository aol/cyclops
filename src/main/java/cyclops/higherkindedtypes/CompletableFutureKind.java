package cyclops.higherkindedtypes;

import java.util.concurrent.CompletionStage;

        import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
        import java.util.function.BiConsumer;
        import java.util.function.BiFunction;
        import java.util.function.Consumer;
        import java.util.function.Function;


import com.aol.cyclops2.hkt.Higher;
import lombok.AccessLevel;
        import lombok.AllArgsConstructor;

/**
 * Simulates Higher Kinded Types for CompletableFuture's
 *
 * CompletableFutureKind is a CompletableFuture and a Higher Kinded Type (CompletableFutureKind.µ,T)
 *
 * @author johnmcclean
 *
 * @param <T> Data type stored within the CompletableFuture
 */

public interface CompletableFutureKind<T> extends Higher<CompletableFutureKind.µ, T>, CompletionStage<T> {

    /**
     * Witness type
     *
     * @author johnmcclean
     *
     */
    public static class µ {
    }

    /**
     * Construct a HKT encoded completed CompletableFuture
     *
     * @param value To encode inside a HKT encoded CompletableFuture
     * @return Completed HKT encoded CompletableFuture
     */
    public static <T> CompletableFutureKind<T> completedFuture(T value){
        return widen(CompletableFuture.completedFuture(value));
    }

    /**
     * Convert a CompletableFuture to a simulated HigherKindedType that captures CompletableFuture nature
     * and CompletableFuture element data type separately. Recover via @see CompletableFutureKind#narrow
     *
     * If the supplied CompletableFuture implements CompletableFutureKind it is returned already, otherwise it
     * is wrapped into a CompletableFuture implementation that does implement CompletableFutureKind
     *
     * @param CompletableFuture CompletableFuture to widen to a CompletableFutureKind
     * @return CompletableFutureKind encoding HKT info about CompletableFutures
     */
    public static <T> CompletableFutureKind<T> widen(final CompletionStage<T> completableFuture) {
        if (completableFuture instanceof CompletableFutureKind)
            return (CompletableFutureKind<T>) completableFuture;
        return new Box<>(
                completableFuture);
    }

    /**
     * Convert the raw Higher Kinded Type for CompletableFutureKind types into the CompletableFutureKind type definition class
     *
     * @param future HKT encoded list into a CompletableFutureKind
     * @return CompletableFutureKind
     */
    public static <T> CompletableFutureKind<T> narrowK(final Higher<CompletableFutureKind.µ, T> future) {
        return (CompletableFutureKind<T>)future;
    }

    /**
     * Convert the HigherKindedType definition for a CompletableFuture into
     *
     * @param CompletableFuture Type Constructor to convert back into narrowed type
     * @return CompletableFuture from Higher Kinded Type
     */
    public static <T> CompletableFuture<T> narrow(final Higher<CompletableFutureKind.µ, T> completableFuture) {
        if (completableFuture instanceof CompletionStage) {
            final CompletionStage<T> ft = (CompletionStage<T>) completableFuture;
            return CompletableFuture.completedFuture(1)
                    .thenCompose(f -> ft);
        }
        // this code should be unreachable due to HKT type checker
        final Box<T> type = (Box<T>) completableFuture;
        final CompletionStage<T> stage = type.narrow();
        return CompletableFuture.completedFuture(1)
                .thenCompose(f -> stage);

    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static final class Box<T> implements CompletableFutureKind<T> {

        private final CompletionStage<T> boxed;

        /**
         * @return wrapped CompletableFuture
         */
        public CompletionStage<T> narrow() {
            return boxed;
        }

        @Override
        public <U> CompletionStage<U> thenApply(final Function<? super T, ? extends U> fn) {
            return boxed.thenApply(fn);
        }

        @Override
        public <U> CompletionStage<U> thenApplyAsync(final Function<? super T, ? extends U> fn) {
            return boxed.thenApplyAsync(fn);
        }

        @Override
        public <U> CompletionStage<U> thenApplyAsync(final Function<? super T, ? extends U> fn,
                                                     final Executor executor) {
            return boxed.thenApplyAsync(fn, executor);
        }

        @Override
        public CompletionStage<Void> thenAccept(final Consumer<? super T> action) {
            return boxed.thenAccept(action);
        }

        @Override
        public CompletionStage<Void> thenAcceptAsync(final Consumer<? super T> action) {
            return boxed.thenAcceptAsync(action);
        }

        @Override
        public CompletionStage<Void> thenAcceptAsync(final Consumer<? super T> action, final Executor executor) {
            return boxed.thenAcceptAsync(action, executor);
        }

        @Override
        public CompletionStage<Void> thenRun(final Runnable action) {
            return boxed.thenRun(action);
        }

        @Override
        public CompletionStage<Void> thenRunAsync(final Runnable action) {
            return boxed.thenRunAsync(action);
        }

        @Override
        public CompletionStage<Void> thenRunAsync(final Runnable action, final Executor executor) {
            return boxed.thenRunAsync(action, executor);
        }

        @Override
        public <U, V> CompletionStage<V> thenCombine(final CompletionStage<? extends U> other,
                                                     final BiFunction<? super T, ? super U, ? extends V> fn) {
            return boxed.thenCombine(other, fn);
        }

        @Override
        public <U, V> CompletionStage<V> thenCombineAsync(final CompletionStage<? extends U> other,
                                                          final BiFunction<? super T, ? super U, ? extends V> fn) {
            return boxed.thenCombineAsync(other, fn);
        }

        @Override
        public <U, V> CompletionStage<V> thenCombineAsync(final CompletionStage<? extends U> other,
                                                          final BiFunction<? super T, ? super U, ? extends V> fn, final Executor executor) {
            return boxed.thenCombineAsync(other, fn, executor);
        }

        @Override
        public <U> CompletionStage<Void> thenAcceptBoth(final CompletionStage<? extends U> other,
                                                        final BiConsumer<? super T, ? super U> action) {
            return boxed.thenAcceptBoth(other, action);
        }

        @Override
        public <U> CompletionStage<Void> thenAcceptBothAsync(final CompletionStage<? extends U> other,
                                                             final BiConsumer<? super T, ? super U> action) {
            return boxed.thenAcceptBothAsync(other, action);
        }

        @Override
        public <U> CompletionStage<Void> thenAcceptBothAsync(final CompletionStage<? extends U> other,
                                                             final BiConsumer<? super T, ? super U> action, final Executor executor) {
            return boxed.thenAcceptBothAsync(other, action, executor);
        }

        @Override
        public CompletionStage<Void> runAfterBoth(final CompletionStage<?> other, final Runnable action) {
            return boxed.runAfterBoth(other, action);
        }

        @Override
        public CompletionStage<Void> runAfterBothAsync(final CompletionStage<?> other, final Runnable action) {
            return boxed.runAfterBothAsync(other, action);
        }

        @Override
        public CompletionStage<Void> runAfterBothAsync(final CompletionStage<?> other, final Runnable action,
                                                       final Executor executor) {
            return boxed.runAfterBothAsync(other, action, executor);
        }

        @Override
        public <U> CompletionStage<U> applyToEither(final CompletionStage<? extends T> other,
                                                    final Function<? super T, U> fn) {
            return boxed.applyToEither(other, fn);
        }

        @Override
        public <U> CompletionStage<U> applyToEitherAsync(final CompletionStage<? extends T> other,
                                                         final Function<? super T, U> fn) {
            return boxed.applyToEitherAsync(other, fn);
        }

        @Override
        public <U> CompletionStage<U> applyToEitherAsync(final CompletionStage<? extends T> other,
                                                         final Function<? super T, U> fn, final Executor executor) {
            return boxed.applyToEitherAsync(other, fn, executor);
        }

        @Override
        public CompletionStage<Void> acceptEither(final CompletionStage<? extends T> other,
                                                  final Consumer<? super T> action) {
            return boxed.acceptEither(other, action);
        }

        @Override
        public CompletionStage<Void> acceptEitherAsync(final CompletionStage<? extends T> other,
                                                       final Consumer<? super T> action) {
            return boxed.acceptEitherAsync(other, action);
        }

        @Override
        public CompletionStage<Void> acceptEitherAsync(final CompletionStage<? extends T> other,
                                                       final Consumer<? super T> action, final Executor executor) {
            return boxed.acceptEitherAsync(other, action, executor);
        }

        @Override
        public CompletionStage<Void> runAfterEither(final CompletionStage<?> other, final Runnable action) {
            return boxed.runAfterEither(other, action);
        }

        @Override
        public CompletionStage<Void> runAfterEitherAsync(final CompletionStage<?> other, final Runnable action) {
            return boxed.runAfterEitherAsync(other, action);
        }

        @Override
        public CompletionStage<Void> runAfterEitherAsync(final CompletionStage<?> other, final Runnable action,
                                                         final Executor executor) {
            return boxed.runAfterEitherAsync(other, action, executor);
        }

        @Override
        public <U> CompletionStage<U> thenCompose(final Function<? super T, ? extends CompletionStage<U>> fn) {
            return boxed.thenCompose(fn);
        }

        @Override
        public <U> CompletionStage<U> thenComposeAsync(final Function<? super T, ? extends CompletionStage<U>> fn) {
            return boxed.thenComposeAsync(fn);
        }

        @Override
        public <U> CompletionStage<U> thenComposeAsync(final Function<? super T, ? extends CompletionStage<U>> fn,
                                                       final Executor executor) {
            return boxed.thenComposeAsync(fn, executor);
        }

        @Override
        public CompletionStage<T> exceptionally(final Function<Throwable, ? extends T> fn) {
            return boxed.exceptionally(fn);
        }

        @Override
        public CompletionStage<T> whenComplete(final BiConsumer<? super T, ? super Throwable> action) {
            return boxed.whenComplete(action);
        }

        @Override
        public CompletionStage<T> whenCompleteAsync(final BiConsumer<? super T, ? super Throwable> action) {
            return boxed.whenCompleteAsync(action);
        }

        @Override
        public CompletionStage<T> whenCompleteAsync(final BiConsumer<? super T, ? super Throwable> action,
                                                    final Executor executor) {
            return boxed.whenCompleteAsync(action, executor);
        }

        @Override
        public <U> CompletionStage<U> handle(final BiFunction<? super T, Throwable, ? extends U> fn) {
            return boxed.handle(fn);
        }

        @Override
        public <U> CompletionStage<U> handleAsync(final BiFunction<? super T, Throwable, ? extends U> fn) {
            return boxed.handleAsync(fn);
        }

        @Override
        public <U> CompletionStage<U> handleAsync(final BiFunction<? super T, Throwable, ? extends U> fn,
                                                  final Executor executor) {
            return boxed.handleAsync(fn, executor);
        }

        @Override
        public CompletableFuture<T> toCompletableFuture() {
            return boxed.toCompletableFuture();
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "CompletableFutureKind [" + boxed + "]";
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((boxed == null) ? 0 : boxed.hashCode());
            return result;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (!(obj instanceof  CompletionStage))
                return false;
            CompletionStage other = ( CompletionStage) obj;
            if (boxed == null) {
                if (other != null)
                    return false;
            } else if (!boxed.equals(other))
                return false;
            return true;
        }


    }
}