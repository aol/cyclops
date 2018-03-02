package cyclops.kinds;

import com.oath.cyclops.hkt.DataWitness.supplier;
import com.oath.cyclops.hkt.Higher;
import cyclops.control.Future;
import cyclops.free.Free;
import cyclops.function.Function0;
import cyclops.instances.jdk.SupplierInstances;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.collections.immutable.LinkedListX;
import cyclops.reactive.collections.immutable.VectorX;
import cyclops.reactive.collections.mutable.ListX;

import java.util.function.Function;
import java.util.function.Supplier;

@FunctionalInterface
public interface SupplierKind<R> extends Function0<R>, Higher<supplier,R> {

  public static <T> SupplierKind<T> λK(final SupplierKind<T> supplier) {
    return supplier;
  }
  public static  <R> Free<supplier, R> free(Supplier<R> s){
    return suspend(() -> Free.done(s.get()));
  }
  public static <A> Free<supplier, A> suspend(final SupplierKind<Free<supplier, A>> f){
    return Free.suspend(f);
  }
  public static <A> A run(final Free<supplier, A> f){
    return f.go(a -> ((Function0<Free<supplier, A>>)a).apply(), SupplierInstances.functor);
  }
  default <R1> R1 kindTo(Function<? super SupplierKind<R>,? extends R1> reduce){
    return reduce.apply(this);
  }
  default <V> SupplierKind<V> apply(final Supplier<? extends Function<? super R,? extends V>> applicative) {
    return () -> applicative.get().apply(this.apply());
  }
  default <R1> SupplierKind<R1> mapFn(final Function<? super R,? extends R1 > f){
    return () -> f.apply(this.apply());
  }
  default <R1> SupplierKind<R1> flatMapFn(final Function<? super R, ? extends Supplier<? extends R1>> f) {
    return () -> f.apply(apply()).get();
  }
  default <R1> SupplierKind<R1> coflatMapFn(final Function<? super Supplier<? super R>, ? extends  R1> f) {
    return () -> f.apply(this);
  }
  default Free<supplier, R> free(){
    return suspend(() -> Free.done(get()));
  }
  default SupplierKind<ReactiveSeq<R>> liftStream() {
    return () -> ReactiveSeq.of(apply());
  }

  default SupplierKind<Future<R>> liftFuture() {
    return () -> Future.ofResult(apply());
  }

  default SupplierKind<ListX<R>> liftList() {
    return () -> ListX.of(apply());
  }


  default SupplierKind<LinkedListX<R>> liftPStack() {
    return () -> LinkedListX.of(apply());
  }

  default SupplierKind<VectorX<R>> liftPVector() {
    return () -> VectorX.of(apply());
  }

}
