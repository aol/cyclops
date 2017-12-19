package cyclops.kinds;

import com.oath.cyclops.hkt.DataWitness;
import com.oath.cyclops.hkt.Higher;
import cyclops.arrow.Cokleisli;
import cyclops.arrow.Kleisli;
import cyclops.control.Future;
import cyclops.hkt.Active;
import cyclops.hkt.Coproduct;
import cyclops.hkt.Nested;
import cyclops.hkt.Product;
import cyclops.instances.jdk.CompletableFutureInstances;
import cyclops.typeclasses.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.*;

/**
 * Simulates Higher Kinded Types for CompletableFuture's
 *
 * CompletableFutureKind is a CompletableFuture and a Higher Kinded Type (Witness.completableFuture,T)
 *
 * @author johnmcclean
 *
 * @param <T> Data type stored within the CompletableFuture
 */

public interface CompletableFutureKind<T> extends Higher<DataWitness.completableFuture, T>, CompletionStage<T> {

  public static <W1,T> Nested<DataWitness.completableFuture,W1,T> nested(CompletableFuture<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){
    return Nested.of(CompletableFutureKind.widen(nested), CompletableFutureInstances.definitions(),def2);
  }
  public static <W1,T> Product<DataWitness.completableFuture,W1,T> product(CompletableFuture<T> f, Active<W1,T> active){
    return Product.of(allTypeclasses(f),active);
  }

  public static <W1,T> Coproduct<W1,DataWitness.completableFuture,T> coproduct(CompletableFuture<T> f, InstanceDefinitions<W1> def2){
    return Coproduct.right(CompletableFutureKind.widen(f),def2, CompletableFutureInstances.definitions());
  }
  public static <T> Active<DataWitness.completableFuture,T> allTypeclasses(CompletableFuture<T> f){
    return Active.of(CompletableFutureKind.widen(f), CompletableFutureInstances.definitions());
  }
  public static <W2,T,R> Nested<DataWitness.completableFuture,W2,R> mapM(CompletableFuture<T> f, Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
    CompletableFuture<Higher<W2, R>> x = f.thenApply(fn);
    return nested(x,defs);

  }
  public static  <T> Kleisli<DataWitness.completableFuture,CompletableFuture<T>,T> kindKleisli(){
    return Kleisli.of(CompletableFutureInstances.monad(), CompletableFutureKind::widen);
  }

  public static  <T> Cokleisli<DataWitness.completableFuture,T,CompletableFuture<T>> kindCokleisli(){
    return Cokleisli.of(CompletableFutureKind::narrowK);
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



  public static <U> CompletableFutureKind<U> supplyAsync(Supplier<U> supplier) {
    return widen(CompletableFuture.supplyAsync(supplier));
  }


  public static <U> CompletableFutureKind<U> supplyAsync(Supplier<U> supplier,
                                                         Executor executor) {
    return widen(CompletableFuture.supplyAsync(supplier,executor));
  }


  public static CompletableFutureKind<Void> runAsync(Runnable runnable) {
    return widen(CompletableFuture.runAsync(runnable));
  }


  public static CompletableFutureKind<Void> runAsync(Runnable runnable,
                                                     Executor executor) {
    return widen(CompletableFuture.runAsync(runnable,executor));
  }

  /**
   * Convert a CompletableFuture to a simulated HigherKindedType that captures CompletableFuture nature
   * and CompletableFuture element data type separately. Recover via @see CompletableFutureKind#narrow
   *
   * If the supplied CompletableFuture implements CompletableFutureKind it is returned already, otherwise it
   * is wrapped into a CompletableFuture implementation that does implement CompletableFutureKind
   *
   * @param completableFuture CompletableFuture to widen to a CompletableFutureKind
   * @return CompletableFutureKind encoding HKT info about CompletableFutures
   */
  public static <T> CompletableFutureKind<T> widen(final CompletionStage<T> completableFuture) {
    if (completableFuture instanceof CompletableFutureKind)
      return (CompletableFutureKind<T>) completableFuture;
    return new Box<>(
      completableFuture);
  }
  public static <T> CompletableFutureKind<T> fromFuture(final Future<T> completableFuture) {
    return widen(completableFuture.toCompletableFuture());
  }
  public static <T> CompletableFutureKind<T> narrowFuture(final Higher<DataWitness.future, T> future) {
    return fromFuture(Future.narrowK(future));
  }
  /**
   * Convert the raw Higher Kinded Type for CompletableFutureKind types into the CompletableFutureKind type definition class
   *
   * @param future HKT encoded list into a CompletableFutureKind
   * @return CompletableFutureKind
   */
  public static <T> CompletableFutureKind<T> narrow(final Higher<DataWitness.completableFuture, T> future) {
    return (CompletableFutureKind<T>)future;
  }

  /**
   * Convert the HigherKindedType definition for a CompletableFuture into
   *
   * @param completableFuture Type Constructor to convert back into narrowed type
   * @return CompletableFuture from Higher Kinded Type
   */
  public static <T> CompletableFuture<T> narrowK(final Higher<DataWitness.completableFuture, T> completableFuture) {
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

    public Active<DataWitness.completableFuture,T> allTypeclasses(){
      return Active.of(this, CompletableFutureInstances.definitions());
    }
    public <W2,R> Nested<DataWitness.completableFuture,W2,R> mapM(Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
      return Nested.of(thenApply(fn), CompletableFutureInstances.definitions(), defs);
    }
    @Override
    public <U> CompletableFutureKind<U> thenApply(final Function<? super T, ? extends U> fn) {
      return widen(boxed.thenApply(fn));
    }

    @Override
    public <U> CompletableFutureKind<U> thenApplyAsync(final Function<? super T, ? extends U> fn) {
      return widen(boxed.thenApplyAsync(fn));
    }

    @Override
    public <U> CompletableFutureKind<U> thenApplyAsync(final Function<? super T, ? extends U> fn,
                                                       final Executor executor) {
      return widen(boxed.thenApplyAsync(fn, executor));
    }

    @Override
    public CompletableFutureKind<Void> thenAccept(final Consumer<? super T> action) {
      return widen(boxed.thenAccept(action));
    }

    @Override
    public CompletableFutureKind<Void> thenAcceptAsync(final Consumer<? super T> action) {
      return widen(boxed.thenAcceptAsync(action));
    }

    @Override
    public CompletableFutureKind<Void> thenAcceptAsync(final Consumer<? super T> action, final Executor executor) {
      return widen(boxed.thenAcceptAsync(action, executor));
    }

    @Override
    public CompletableFutureKind<Void> thenRun(final Runnable action) {
      return widen(boxed.thenRun(action));
    }

    @Override
    public CompletableFutureKind<Void> thenRunAsync(final Runnable action) {
      return widen(boxed.thenRunAsync(action));
    }

    @Override
    public CompletableFutureKind<Void> thenRunAsync(final Runnable action, final Executor executor) {
      return widen(boxed.thenRunAsync(action, executor));
    }

    @Override
    public <U, V> CompletableFutureKind<V> thenCombine(final CompletionStage<? extends U> other,
                                                       final BiFunction<? super T, ? super U, ? extends V> fn) {
      return widen(boxed.thenCombine(other, fn));
    }

    @Override
    public <U, V> CompletableFutureKind<V> thenCombineAsync(final CompletionStage<? extends U> other,
                                                            final BiFunction<? super T, ? super U, ? extends V> fn) {
      return widen(boxed.thenCombineAsync(other, fn));
    }

    @Override
    public <U, V> CompletableFutureKind<V> thenCombineAsync(final CompletionStage<? extends U> other,
                                                            final BiFunction<? super T, ? super U, ? extends V> fn, final Executor executor) {
      return widen(boxed.thenCombineAsync(other, fn, executor));
    }

    @Override
    public <U> CompletableFutureKind<Void> thenAcceptBoth(final CompletionStage<? extends U> other,
                                                          final BiConsumer<? super T, ? super U> action) {
      return widen(boxed.thenAcceptBoth(other, action));
    }

    @Override
    public <U> CompletableFutureKind<Void> thenAcceptBothAsync(final CompletionStage<? extends U> other,
                                                               final BiConsumer<? super T, ? super U> action) {
      return widen(boxed.thenAcceptBothAsync(other, action));
    }

    @Override
    public <U> CompletableFutureKind<Void> thenAcceptBothAsync(final CompletionStage<? extends U> other,
                                                               final BiConsumer<? super T, ? super U> action, final Executor executor) {
      return widen(boxed.thenAcceptBothAsync(other, action, executor));
    }

    @Override
    public CompletableFutureKind<Void> runAfterBoth(final CompletionStage<?> other, final Runnable action) {
      return widen(boxed.runAfterBoth(other, action));
    }

    @Override
    public CompletableFutureKind<Void> runAfterBothAsync(final CompletionStage<?> other, final Runnable action) {
      return widen(boxed.runAfterBothAsync(other, action));
    }

    @Override
    public CompletableFutureKind<Void> runAfterBothAsync(final CompletionStage<?> other, final Runnable action,
                                                         final Executor executor) {
      return widen(boxed.runAfterBothAsync(other, action, executor));
    }

    @Override
    public <U> CompletableFutureKind<U> applyToEither(final CompletionStage<? extends T> other,
                                                      final Function<? super T, U> fn) {
      return widen(boxed.applyToEither(other, fn));
    }

    @Override
    public <U> CompletableFutureKind<U> applyToEitherAsync(final CompletionStage<? extends T> other,
                                                           final Function<? super T, U> fn) {
      return widen(boxed.applyToEitherAsync(other, fn));
    }

    @Override
    public <U> CompletableFutureKind<U> applyToEitherAsync(final CompletionStage<? extends T> other,
                                                           final Function<? super T, U> fn, final Executor executor) {
      return widen(boxed.applyToEitherAsync(other, fn, executor));
    }

    @Override
    public CompletableFutureKind<Void> acceptEither(final CompletionStage<? extends T> other,
                                                    final Consumer<? super T> action) {
      return widen(boxed.acceptEither(other, action));
    }

    @Override
    public CompletableFutureKind<Void> acceptEitherAsync(final CompletionStage<? extends T> other,
                                                         final Consumer<? super T> action) {
      return widen(boxed.acceptEitherAsync(other, action));
    }

    @Override
    public CompletableFutureKind<Void> acceptEitherAsync(final CompletionStage<? extends T> other,
                                                         final Consumer<? super T> action, final Executor executor) {
      return widen(boxed.acceptEitherAsync(other, action, executor));
    }

    @Override
    public CompletableFutureKind<Void> runAfterEither(final CompletionStage<?> other, final Runnable action) {
      return widen(boxed.runAfterEither(other, action));
    }

    @Override
    public CompletableFutureKind<Void> runAfterEitherAsync(final CompletionStage<?> other, final Runnable action) {
      return widen(boxed.runAfterEitherAsync(other, action));
    }

    @Override
    public CompletableFutureKind<Void> runAfterEitherAsync(final CompletionStage<?> other, final Runnable action,
                                                           final Executor executor) {
      return widen(boxed.runAfterEitherAsync(other, action, executor));
    }

    @Override
    public <U> CompletableFutureKind<U> thenCompose(final Function<? super T, ? extends CompletionStage<U>> fn) {
      return widen(boxed.thenCompose(fn));
    }

    @Override
    public <U> CompletableFutureKind<U> thenComposeAsync(final Function<? super T, ? extends CompletionStage<U>> fn) {
      return widen(boxed.thenComposeAsync(fn));
    }

    @Override
    public <U> CompletableFutureKind<U> thenComposeAsync(final Function<? super T, ? extends CompletionStage<U>> fn,
                                                         final Executor executor) {
      return widen(boxed.thenComposeAsync(fn, executor));
    }

    @Override
    public CompletableFutureKind<T> exceptionally(final Function<Throwable, ? extends T> fn) {
      return widen(boxed.exceptionally(fn));
    }

    @Override
    public CompletableFutureKind<T> whenComplete(final BiConsumer<? super T, ? super Throwable> action) {
      return widen(boxed.whenComplete(action));
    }

    @Override
    public CompletableFutureKind<T> whenCompleteAsync(final BiConsumer<? super T, ? super Throwable> action) {
      return widen(boxed.whenCompleteAsync(action));
    }

    @Override
    public CompletableFutureKind<T> whenCompleteAsync(final BiConsumer<? super T, ? super Throwable> action,
                                                      final Executor executor) {
      return widen(boxed.whenCompleteAsync(action, executor));
    }

    @Override
    public <U> CompletableFutureKind<U> handle(final BiFunction<? super T, Throwable, ? extends U> fn) {
      return widen(boxed.handle(fn));
    }

    @Override
    public <U> CompletableFutureKind<U> handleAsync(final BiFunction<? super T, Throwable, ? extends U> fn) {
      return widen(boxed.handleAsync(fn));
    }

    @Override
    public <U> CompletableFutureKind<U> handleAsync(final BiFunction<? super T, Throwable, ? extends U> fn,
                                                    final Executor executor) {
      return widen(boxed.handleAsync(fn, executor));
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
