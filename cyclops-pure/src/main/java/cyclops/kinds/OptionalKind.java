package cyclops.kinds;

import com.oath.cyclops.hkt.DataWitness;
import com.oath.cyclops.hkt.DataWitness.optional;
import com.oath.cyclops.hkt.Higher;
import cyclops.companion.Optionals;
import cyclops.instances.jdk.OptionalInstances;
import cyclops.typeclasses.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
/* Simulates Higher Kinded Types for Optional's
  *
  * OptionalKind is a Optional and a Higher Kinded Type (optional,T)
  *
  * @author johnmcclean
  *
  * @param <T> Data type stored within the Optional
  */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public  final class OptionalKind<T> implements Higher<optional, T> {

  public static  <T> Kleisli<optional,Optional<T>,T> kindKleisli(){
    return Kleisli.of(OptionalInstances.monad(), OptionalKind::widen);
  }

  public static  <T> Cokleisli<optional,T,Optional<T>> kindCokleisli(){
    return Cokleisli.of(OptionalKind::narrowK);
  }


  public static <W1,T> Nested<optional,W1,T> nested(Optional<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){
    return Nested.of(OptionalKind.widen(nested), OptionalInstances.definitions(),def2);
  }
  public <W1,T> Product<optional,W1,T> product(Optional<T> f, Active<W1,T> active){
    return Product.of(allTypeclasses(f),active);
  }

  public static <W1,T> Coproduct<W1,optional,T> coproduct(Optional<T> f, InstanceDefinitions<W1> def2){
    return Coproduct.right(OptionalKind.widen(f),def2, OptionalInstances.definitions());
  }
  public static <T> Active<optional,T> allTypeclasses(Optional<T> f){
    return Active.of(OptionalKind.widen(f), OptionalInstances.definitions());
  }
  public <W2,T,R> Nested<optional,W2,R> mapM(Optional<T> f, Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
    Optional<Higher<W2, R>> x = f.map(fn);
    return nested(x,defs);

  }

  private final Optional<T> boxed;


  /**
   * @return An HKT encoded zero Optional
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
  public static <T> OptionalKind<T> ofNullable(T value) {
    return widen(Optional.ofNullable(value));
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
  public static <T> OptionalKind<T> narrow(final Higher<optional, T> future) {
    return (OptionalKind<T>)future;
  }
  /**
   * Convert the HigherKindedType definition for a Optional into
   *
   * @param Optional Type Constructor to convert back into narrowed type
   * @return Optional from Higher Kinded Type
   */
  public static <T> Optional<T> narrowK(final Higher<optional, T> Optional) {
    //has to be an OptionalKind as only OptionalKind can implement Higher<optional, T>
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
  public OptionalKind<T> filter(Predicate<? super T> predicate) {
    return widen(boxed.filter(predicate));
  }
  public <U> OptionalKind<U> map(Function<? super T, ? extends U> mapper) {
    return widen(boxed.map(mapper));
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

  public Active<optional,T> allTypeclasses(){
    return Active.of(this, OptionalInstances.definitions());
  }

  public <W2,R> Nested<optional,W2,R> mapM(Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
    return Nested.of(map(fn), OptionalInstances.definitions(), defs);
  }
}
