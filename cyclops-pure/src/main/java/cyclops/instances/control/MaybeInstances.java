package cyclops.instances.control;

import com.oath.cyclops.hkt.DataWitness;
import com.oath.cyclops.hkt.DataWitness.option;
import com.oath.cyclops.hkt.Higher;
import cyclops.arrow.Cokleisli;
import cyclops.arrow.Kleisli;
import cyclops.control.Either;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.function.Function3;
import cyclops.function.Monoid;
import cyclops.hkt.Active;
import cyclops.hkt.Coproduct;
import cyclops.hkt.Nested;
import cyclops.hkt.Product;
import cyclops.kinds.OptionalKind;
import cyclops.typeclasses.*;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.arrow.MonoidK;
import cyclops.arrow.MonoidKs;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.instances.General;
import cyclops.typeclasses.monad.*;
import cyclops.transformers.Transformer;
import cyclops.transformers.TransformerFactory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

import java.util.function.BiFunction;
import java.util.function.Function;

import static cyclops.control.Maybe.narrowK;

/**
 * Companion class for creating Type Class instances for working with Maybes
 * @author johnmcclean
 *
 */
@UtilityClass
public class MaybeInstances {
  public static <T> Maybe<T> fromOptionalKind(final OptionalKind<T> opt){
    return fromOptional(OptionalKind.narrow(opt));
  }
  public static  <T> Kleisli<option,Maybe<T>,T> kindKleisli(){
    return Kleisli.of(MaybeInstances.monad(), Maybe::widen);
  }

  public static <T> Cokleisli<option,T,Maybe<T>> kindCokleisli(){
    return Cokleisli.of(Maybe::narrowK);
  }
  public static <W1,T> Nested<option,W1,T> nested(Maybe<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){

    return Nested.of(nested, MaybeInstances.definitions(),def2);
  }
  public static <W1,T> Product<option,W1,T> product(Maybe<T> m, Active<W1,T> active){
    return Product.of(allTypeclasses(m),active);
  }
  public static <W1,T> Coproduct<W1,option,T> coproduct(Maybe<T> m, InstanceDefinitions<W1> def2){
    return Coproduct.right(m,def2, MaybeInstances.definitions());
  }
  public static <T> Active<option,T> allTypeclasses(Maybe<T> m){
    return Active.of(m, MaybeInstances.definitions());
  }
  public static <W2,R,T> Nested<option,W2,R> mapM(Maybe<T> m,Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
    return Nested.of(m.map(fn), MaybeInstances.definitions(), defs);
  }


  /**
   * Construct an equivalent Maybe from the Supplied Optional
   * <pre>
   * {@code
   *   MaybeType<Integer> some = MaybeType.fromOptional(Optional.of(10));
   *   //Maybe[10], Some[10]
   *
   *   MaybeType<Integer> none = MaybeType.fromOptional(Optional.zero());
   *   //Maybe.zero, None[]
   * }
   * </pre>
   *
   * @param optional Optional to construct Maybe from
   * @return Maybe created from Optional
   */
  public static <T> Maybe<T> fromOptional(Higher<DataWitness.optional,T> optional){
    return   Maybe.fromOptional(OptionalKind.narrowK(optional));

  }

  public static InstanceDefinitions<option> definitions(){
    return new InstanceDefinitions<option>() {
      @Override
      public <T, R> Functor<option> functor() {
        return MaybeInstances.functor();
      }

      @Override
      public <T> Pure<option> unit() {
        return MaybeInstances.unit();
      }

      @Override
      public <T, R> Applicative<option> applicative() {
        return MaybeInstances.applicative();
      }

      @Override
      public <T, R> Monad<option> monad() {
        return MaybeInstances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<option>> monadZero() {
        return Option.some(MaybeInstances.monadZero());
      }

      @Override
      public <T> Option<MonadPlus<option>> monadPlus() {
        return Option.some(MaybeInstances.monadPlus());
      }

      @Override
      public <T> MonadRec<option> monadRec() {
        return MaybeInstances.monadRec();
      }

      @Override
      public <T> Option<MonadPlus<option>> monadPlus(MonoidK<option> m) {
        return Option.some(MaybeInstances.monadPlus(m));
      }

      @Override
      public <C2,T> Traverse<option> traverse() {
        return MaybeInstances.traverse();
      }


      @Override
      public <T> Foldable<option> foldable() {
        return MaybeInstances.foldable();
      }

      @Override
      public <T> Option<Comonad<option>> comonad() {
        return Maybe.nothing();
      }

      @Override
      public <T> Option<Unfoldable<option>> unfoldable() {
        return Maybe.nothing();
      }
    };
  }

  /**
   *
   * Transform a maybe, mulitplying every element by 2
   *
   * <pre>
   * {@code
   *  Maybe<Integer> maybe = Maybes.functor().map(i->i*2, Maybe.widen(Maybe.just(1));
   *
   *  //[2]
   *
   *
   * }
   * </pre>
   *
   * An example fluent api working with Maybes
   * <pre>
   * {@code
   *   Maybe<Integer> maybe = Maybes.unit()
  .unit("hello")
  .applyHKT(h->Maybes.functor().map((String v) ->v.length(), h))
  .convert(Maybe::narrowK3);
   *
   * }
   * </pre>
   *
   *
   * @return A functor for Maybes
   */
  public static <T,R>Functor<option> functor(){
    BiFunction<Maybe<T>,Function<? super T, ? extends R>,Maybe<R>> map = MaybeInstances::map;
    return General.functor(map);
  }
  /**
   * <pre>
   * {@code
   * Maybe<String> maybe = Maybes.unit()
  .unit("hello")
  .convert(Maybe::narrowK3);

  //Maybe.just("hello"))
   *
   * }
   * </pre>
   *
   *
   * @return A factory for Maybes
   */
  public static <T> Pure<option> unit(){
    return General.<option,T>unit(MaybeInstances::of);
  }
  /**
   *
   * <pre>
   * {@code
   * import static com.aol.cyclops.hkt.jdk.Maybe.widen;
   * import static com.aol.cyclops.util.function.Lambda.l1;
   * import static java.util.Maybe.just;
   *
  Maybes.zippingApplicative()
  .ap(widen(asMaybe(l1(this::multiplyByTwo))),widen(asMaybe(1,2,3)));
   *
   * //[2,4,6]
   * }
   * </pre>
   *
   *
   * Example fluent API
   * <pre>
   * {@code
   * Maybe<Function<Integer,Integer>> maybeFn =Maybes.unit()
   *                                                  .unit(Lambda.l1((Integer i) ->i*2))
   *                                                  .convert(Maybe::narrowK3);

  Maybe<Integer> maybe = Maybes.unit()
  .unit("hello")
  .applyHKT(h->Maybes.functor().map((String v) ->v.length(), h))
  .applyHKT(h->Maybes.applicative().ap(maybeFn, h))
  .convert(Maybe::narrowK3);

  //Maybe.just("hello".length()*2))
   *
   * }
   * </pre>
   *
   *
   * @return A zipper for Maybes
   */
  public static <T,R> Applicative<option> applicative(){
    BiFunction<Maybe< Function<T, R>>,Maybe<T>,Maybe<R>> ap = MaybeInstances::ap;
    return General.applicative(functor(), unit(), ap);
  }
  /**
   *
   * <pre>
   * {@code
   * import static com.aol.cyclops.hkt.jdk.Maybe.widen;
   * Maybe<Integer> maybe  = Maybes.monad()
  .flatMap(i->widen(MaybeX.range(0,i)), widen(Maybe.just(1,2,3)))
  .convert(Maybe::narrowK3);
   * }
   * </pre>
   *
   * Example fluent API
   * <pre>
   * {@code
   *    Maybe<Integer> maybe = Maybes.unit()
  .unit("hello")
  .applyHKT(h->Maybes.monad().flatMap((String v) ->Maybes.unit().unit(v.length()), h))
  .convert(Maybe::narrowK3);

  //Maybe.just("hello".length())
   *
   * }
   * </pre>
   *
   * @return Type class with monad arrow for Maybes
   */
  public static <T,R> Monad<option> monad(){

    BiFunction<Higher<option,T>,Function<? super T, ? extends Higher<option,R>>,Higher<option,R>> flatMap = MaybeInstances::flatMap;
    return General.monad(applicative(), flatMap);
  }
  public static <T,R> MonadRec<option> monadRec(){

    return new MonadRec<option>(){

      @Override
      public <T, R> Higher<option, R> tailRec(T initial, Function<? super T, ? extends Higher<option, ? extends Either<T, R>>> fn) {
        return Maybe.tailRec(initial,fn.andThen(Maybe::narrowK));
      }
    };
  }
  /**
   *
   * <pre>
   * {@code
   *  Maybe<String> maybe = Maybes.unit()
  .unit("hello")
  .applyHKT(h->Maybes.monadZero().filter((String t)->t.startsWith("he"), h))
  .convert(Maybe::narrowK3);

  //Maybe.just("hello"));
   *
   * }
   * </pre>
   *
   *
   * @return A filterable monad (with default value)
   */
  public static <T,R> MonadZero<option> monadZero(){

    return General.monadZero(monad(), Maybe.nothing());
  }
  /**
   * <pre>
   * {@code
   *  Maybe<Integer> maybe = Maybes.<Integer>monadPlus()
  .plus(Maybe.widen(Maybe.just()), Maybe.widen(Maybe.just(10)))
  .convert(Maybe::narrowK3);
  //Maybe.just(10))
   *
   * }
   * </pre>
   * @return Type class for combining Maybes by concatenation
   */
  public static <T> MonadPlus<option> monadPlus(){

    return General.monadPlus(monadZero(), MonoidKs.firstPresentOption());
  }
  /**
   *
   * <pre>
   * {@code
   *  Monoid<Maybe<Integer>> m = Monoid.of(Maybe.widen(Maybe.just()), (a,b)->a.isEmpty() ? b : a);
  Maybe<Integer> maybe = Maybes.<Integer>monadPlus(m)
  .plus(Maybe.just(5), Maybe.just(10))
  .convert(Maybe::narrowK3);
  //Maybe[5]
   *
   * }
   * </pre>
   *
   * @param m Monoid to use for combining Maybes
   * @return Type class for combining Maybes
   */
  public static <T> MonadPlus<option> monadPlus(MonoidK<option> m){

    return General.monadPlus(monadZero(),m);
  }

  /**
   * @return Type class for traversables with traverse / sequence operations
   */
  public static <C2,T> Traverse<option> traverse(){

    return General.traverseByTraverse(applicative(), MaybeInstances::traverseA);
  }

  /**
   *
   * <pre>
   * {@code
   * int sum  = Maybes.foldable()
  .foldLeft(0, (a,b)->a+b, Maybe.widen(Maybe.just(1)));

  //1
   *
   * }
   * </pre>
   *
   *
   * @return Type class for folding / reduction operations
   */
  public static <T,R> Foldable<option> foldable(){
    BiFunction<Monoid<T>,Higher<option,T>,T> foldRightFn =  (m, l)-> narrowK(l).orElse(m.zero());
    BiFunction<Monoid<T>,Higher<option,T>,T> foldLeftFn = (m, l)-> narrowK(l).orElse(m.zero());
    Function3<Monoid<R>, Function<T, R>, Higher<option, T>, R> foldMapFn = (m, f, l)-> narrowK(l).map(f).fold(m);
    return General.foldable(foldRightFn, foldLeftFn,foldMapFn);
  }



  private <T> Maybe<T> of(T value){
    return Maybe.of(value);
  }
  private static <T,R> Maybe<R> ap(Maybe<Function< T, R>> lt,  Maybe<T> maybe){
    return lt.zip(maybe, (a,b)->a.apply(b)).toMaybe();

  }
  private static <T,R> Higher<option,R> flatMap(Higher<option,T> lt, Function<? super T, ? extends  Higher<option,R>> fn){
    return narrowK(lt).flatMap(fn.andThen(Maybe::narrowK));
  }
  private static <T,R> Maybe<R> map(Maybe<T> lt, Function<? super T, ? extends R> fn){
    return lt.map(fn);

  }


  private static <C2,T,R> Higher<C2, Higher<option, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn,
                                                                              Higher<option, T> ds){

    Maybe<T> maybe = narrowK(ds);
    Higher<C2, Maybe<R>> res = maybe.visit(some-> applicative.map(m->Maybe.of(m), fn.apply(some)),
      ()->applicative.unit(Maybe.<R>nothing()));

    return Maybe.widen2(res);
  }

}
