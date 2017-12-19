package cyclops.instances.jdk;

import com.oath.cyclops.hkt.DataWitness.optional;
import com.oath.cyclops.hkt.Higher;
import cyclops.companion.Optionals;
import cyclops.control.Either;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.function.Function3;
import cyclops.function.Monoid;
import cyclops.instances.control.MaybeInstances;
import cyclops.kinds.OptionalKind;
import cyclops.arrow.MonoidKs;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.instances.General;
import lombok.experimental.UtilityClass;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import cyclops.typeclasses.InstanceDefinitions;
import cyclops.typeclasses.Pure;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.arrow.MonoidK;
import cyclops.typeclasses.monad.*;
/**
 * Companion class for creating Type Class instances for working with Optionals
 * @author johnmccleanP
 *
 */
@UtilityClass
public  class OptionalInstances {
  public static InstanceDefinitions<optional> definitions(){
    return new InstanceDefinitions<optional>() {
      @Override
      public <T, R> Functor<optional> functor() {
        return OptionalInstances.functor();
      }

      @Override
      public <T> Pure<optional> unit() {
        return OptionalInstances.unit();
      }

      @Override
      public <T, R> Applicative<optional> applicative() {
        return OptionalInstances.applicative();
      }

      @Override
      public <T, R> Monad<optional> monad() {
        return OptionalInstances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<optional>> monadZero() {
        return Option.some(OptionalInstances.monadZero());
      }

      @Override
      public <T> Option<MonadPlus<optional>> monadPlus() {
        return Option.some(OptionalInstances.monadPlus());
      }

      @Override
      public <T> MonadRec<optional> monadRec() {
        return OptionalInstances.monadRec();
      }

      @Override
      public <T> Option<MonadPlus<optional>> monadPlus(MonoidK<optional> m) {
        return Option.some(OptionalInstances.monadPlus(m));
      }

      @Override
      public <C2, T> Traverse<optional> traverse() {
        return OptionalInstances.traverse();
      }

      @Override
      public <T> Foldable<optional> foldable() {
        return OptionalInstances.foldable();
      }

      @Override
      public <T> Option<Comonad<optional>> comonad() {
        return Maybe.just(OptionalInstances.comonad());
      }

      @Override
      public <T> Option<Unfoldable<optional>> unfoldable() {
        return Maybe.nothing();
      }
    };
  }

  /**
   *
   * Transform a list, mulitplying every element by 2
   *
   * <pre>
   * {@code
   *  OptionalKind<Integer> list = Optionals.functor().map(i->i*2, OptionalKind.widen(Arrays.asOptional(1,2,3));
   *
   *  //[2,4,6]
   *
   *
   * }
   * </pre>
   *
   * An example fluent api working with Optionals
   * <pre>
   * {@code
   *   OptionalKind<Integer> list = Optionals.unit()
  .unit("hello")
  .applyHKT(h->Optionals.functor().map((String v) ->v.length(), h))
  .convert(OptionalKind::narrowK3);
   *
   * }
   * </pre>
   *
   *
   * @return A functor for Optionals
   */
  public static <T,R>Functor<optional> functor(){
    BiFunction<OptionalKind<T>,Function<? super T, ? extends R>,OptionalKind<R>> map = OptionalInstances::map;
    return General.functor(map);
  }
  /**
   * <pre>
   * {@code
   * OptionalKind<String> list = Optionals.unit()
  .unit("hello")
  .convert(OptionalKind::narrowK3);

  //Arrays.asOptional("hello"))
   *
   * }
   * </pre>
   *
   *
   * @return A factory for Optionals
   */
  public static <T> Pure<optional> unit(){
    return General.<optional,T>unit(OptionalInstances::of);
  }
  /**
   *
   * <pre>
   * {@code
   * import static com.aol.cyclops.hkt.jdk.OptionalKind.widen;
   * import static com.aol.cyclops.util.function.Lambda.l1;
   * import static java.util.Arrays.asOptional;
   *
  Optionals.zippingApplicative()
  .ap(widen(asOptional(l1(this::multiplyByTwo))),widen(asOptional(1,2,3)));
   *
   * //[2,4,6]
   * }
   * </pre>
   *
   *
   * Example fluent API
   * <pre>
   * {@code
   * OptionalKind<Function<Integer,Integer>> listFn =Optionals.unit()
   *                                                  .unit(Lambda.l1((Integer i) ->i*2))
   *                                                  .convert(OptionalKind::narrowK3);

  OptionalKind<Integer> list = Optionals.unit()
  .unit("hello")
  .applyHKT(h->Optionals.functor().map((String v) ->v.length(), h))
  .applyHKT(h->Optionals.applicative().ap(listFn, h))
  .convert(OptionalKind::narrowK3);

  //Arrays.asOptional("hello".length()*2))
   *
   * }
   * </pre>
   *
   *
   * @return A zipper for Optionals
   */
  public static <T,R> Applicative<optional> applicative(){
    BiFunction<OptionalKind< Function<T, R>>,OptionalKind<T>,OptionalKind<R>> ap = OptionalInstances::ap;
    return General.applicative(functor(), unit(), ap);
  }
  /**
   *
   * <pre>
   * {@code
   * import static com.aol.cyclops.hkt.jdk.OptionalKind.widen;
   * OptionalKind<Integer> list  = Optionals.monad()
  .flatMap(i->widen(OptionalX.range(0,i)), widen(Arrays.asOptional(1,2,3)))
  .convert(OptionalKind::narrowK3);
   * }
   * </pre>
   *
   * Example fluent API
   * <pre>
   * {@code
   *    OptionalKind<Integer> list = Optionals.unit()
  .unit("hello")
  .applyHKT(h->Optionals.monad().flatMap((String v) ->Optionals.unit().unit(v.length()), h))
  .convert(OptionalKind::narrowK3);

  //Arrays.asOptional("hello".length())
   *
   * }
   * </pre>
   *
   * @return Type class with monad arrow for Optionals
   */
  public static <T,R> Monad<optional> monad(){

    BiFunction<Higher<optional,T>,Function<? super T, ? extends Higher<optional,R>>,Higher<optional,R>> flatMap = OptionalInstances::flatMap;
    return General.monad(applicative(), flatMap);
  }
  /**
   *
   * <pre>
   * {@code
   *  OptionalKind<String> list = Optionals.unit()
  .unit("hello")
  .applyHKT(h->Optionals.monadZero().filter((String t)->t.startsWith("he"), h))
  .convert(OptionalKind::narrowK3);

  //Arrays.asOptional("hello"));
   *
   * }
   * </pre>
   *
   *
   * @return A filterable monad (with default value)
   */
  public static <T,R> MonadZero<optional> monadZero(){

    return General.monadZero(monad(), OptionalKind.empty());
  }
  public static  MonadRec<optional> monadRec() {

    return new MonadRec<optional>(){


      @Override
      public <T, R> Higher<optional, R> tailRec(T initial, Function<? super T, ? extends Higher<optional, ? extends Either<T, R>>> fn) {
        Optional<R> x = Optionals.tailRec(initial, fn.andThen(a -> OptionalKind.narrowK(a)));
        return OptionalKind.widen(x);

      }
    };


  }
  /**
   *
   * <pre>
   * {@code
   *  OptionalKind<Integer> list = Optionals.<Integer>monadPlus()
  .plus(OptionalKind.widen(Arrays.asOptional()), OptionalKind.widen(Arrays.asOptional(10)))
  .convert(OptionalKind::narrowK3);
  //Arrays.asOptional(10))
   *
   * }
   * </pre>
   * @return Type class for combining Optionals by concatenation
   */
  public static <T> MonadPlus<optional> monadPlus(){

    return General.monadPlus(monadZero(), MonoidKs.firstPresentOptional());
  }
  /**
   *
   * <pre>
   * {@code
   *  Monoid<OptionalKind<Integer>> m = Monoid.of(OptionalKind.widen(Arrays.asOptional()), (a,b)->a.isEmpty() ? b : a);
  OptionalKind<Integer> list = Optionals.<Integer>monadPlus(m)
  .plus(OptionalKind.widen(Arrays.asOptional(5)), OptionalKind.widen(Arrays.asOptional(10)))
  .convert(OptionalKind::narrowK3);
  //Arrays.asOptional(5))
   *
   * }
   * </pre>
   *
   * @param m2 Monoid to use for combining Optionals
   * @return Type class for combining Optionals
   */
  public static <T> MonadPlus<optional> monadPlus(MonoidK<optional> m2){
    return General.monadPlus(monadZero(),m2);
  }

  /**
   * @return Type class for traversables with traverse / sequence operations
   */
  public static <C2,T> Traverse<optional> traverse(){

    return General.traverseByTraverse(applicative(), OptionalInstances::traverseA);
  }

  /**
   *
   * <pre>
   * {@code
   * int sum  = Optionals.foldable()
  .foldLeft(0, (a,b)->a+b, OptionalKind.widen(Arrays.asOptional(1,2,3,4)));

  //10
   *
   * }
   * </pre>
   *
   *
   * @return Type class for folding / reduction operations
   */
  public static <T,R> Foldable<optional> foldable(){
    BiFunction<Monoid<T>,Higher<optional,T>,T> foldRightFn =  (m, l)-> OptionalKind.narrow(l).orElse(m.zero());
    BiFunction<Monoid<T>,Higher<optional,T>,T> foldLeftFn = (m, l)-> OptionalKind.narrow(l).orElse(m.zero());
    Function3<Monoid<R>, Function<T, R>, Higher<optional, T>, R> foldMapFn = (m, f, l)->OptionalKind.narrowK(l).map(f).orElseGet(()->m.zero());
    return General.foldable(foldRightFn, foldLeftFn,foldMapFn);
  }
  public static <T> Comonad<optional> comonad(){
    Function<? super Higher<optional, T>, ? extends T> extractFn = maybe -> maybe.convert(OptionalKind::narrow).get();
    return General.comonad(functor(), unit(), extractFn);
  }

  private <T> OptionalKind<T> of(T value){
    return OptionalKind.widen(Optional.of(value));
  }
  private static <T,R> OptionalKind<R> ap(OptionalKind<Function< T, R>> lt, OptionalKind<T> list){
    return OptionalKind.widen(MaybeInstances.fromOptionalKind(lt).zip(MaybeInstances.fromOptionalKind(list), (a, b)->a.apply(b)).toOptional());

  }
  private static <T,R> Higher<optional,R> flatMap(Higher<optional,T> lt, Function<? super T, ? extends  Higher<optional,R>> fn){
    return OptionalKind.widen(OptionalKind.narrow(lt).flatMap(fn.andThen(OptionalKind::narrowK)));
  }
  private static <T,R> OptionalKind<R> map(OptionalKind<T> lt, Function<? super T, ? extends R> fn){
    return OptionalKind.narrow(lt).map(fn);
  }


  private static <C2,T,R> Higher<C2, Higher<optional, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn,
                                                                                Higher<optional, T> ds){
    Optional<T> opt = OptionalKind.narrowK(ds);
    return opt.isPresent() ?   applicative.map(OptionalKind::of, fn.apply(opt.get())) :
      applicative.unit(OptionalKind.empty());
  }

}

