package cyclops.instances.control;

import com.oath.cyclops.hkt.DataWitness.eval;
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
import cyclops.typeclasses.*;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.arrow.MonoidK;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.instances.General;
import cyclops.typeclasses.monad.*;
import lombok.experimental.UtilityClass;

import java.util.function.BiFunction;
import java.util.function.Function;

import static cyclops.control.Eval.narrowK;

/**
 * Companion class for creating Type Class instances for working with Evals
 * @author johnmcclean
 *
 */
@UtilityClass
public class EvalInstances {

  public static  <T> Kleisli<eval,Eval<T>,T> kindKleisli(){
    return Kleisli.of(EvalInstances.monad(), Eval::widen);
  }

  public static  <T> Cokleisli<eval,T,Eval<T>> kindCokleisli(){
    return Cokleisli.of(Eval::narrowK);
  }
  public static <W1,T> Nested<eval,W1,T> nested(Eval<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){
    return Nested.of(nested, EvalInstances.definitions(),def2);
  }
  public static <W1,T> Product<eval,W1,T> product(Eval<T> ev, Active<W1,T> active){
    return Product.of(allTypeclasses(ev),active);
  }
  public static  <W1,T> Coproduct<W1,eval,T> coproduct(Eval<T> ev, InstanceDefinitions<W1> def2){
    return Coproduct.right(ev,def2, EvalInstances.definitions());
  }
  public static <T> Active<eval,T> allTypeclasses(Eval<T> ev){
    return Active.of(ev, EvalInstances.definitions());
  }

  public static  <W2,R,T> Nested<eval,W2,R> mapM(Eval<T> ev,Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
    return Nested.of(ev.map(fn), EvalInstances.definitions(), defs);
  }

  public static InstanceDefinitions<eval> definitions(){
    return new InstanceDefinitions<eval>() {
      @Override
      public <T, R> Functor<eval> functor() {
        return EvalInstances.functor();
      }

      @Override
      public <T> Pure<eval> unit() {
        return EvalInstances.unit();
      }

      @Override
      public <T, R> Applicative<eval> applicative() {
        return EvalInstances.applicative();
      }

      @Override
      public <T, R> Monad<eval> monad() {
        return EvalInstances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<eval>> monadZero() {
        return Option.some(EvalInstances.monadZero());
      }

      @Override
      public <T> Option<MonadPlus<eval>> monadPlus() {
        return Maybe.nothing();
      }

      @Override
      public <T> MonadRec<eval> monadRec() {
        return EvalInstances.monadRec();
      }

      @Override
      public <T> Option<MonadPlus<eval>> monadPlus(MonoidK<eval> m) {
        return Maybe.nothing();
      }

      @Override
      public <C2, T>Traverse<eval> traverse() {
        return EvalInstances.traverse();
      }

      @Override
      public <T> Foldable<eval> foldable() {
        return EvalInstances.foldable();
      }

      @Override
      public <T> Option<Comonad<eval>> comonad() {
        return Maybe.just(EvalInstances.comonad());
      }

      @Override
      public <T> Option<Unfoldable<eval>> unfoldable() {
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
   *  Eval<Integer> list = Evals.functor().map(i->i*2, Eval.widen(Arrays.asEval(1,2,3));
   *
   *  //[2,4,6]
   *
   *
   * }
   * </pre>
   *
   * An example fluent api working with Evals
   * <pre>
   * {@code
   *   Eval<Integer> list = Evals.unit()
  .unit("hello")
  .applyHKT(h->Evals.functor().map((String v) ->v.length(), h))
  .convert(Eval::narrowK3);
   *
   * }
   * </pre>
   *
   *
   * @return A functor for Evals
   */
  public static <T,R>Functor<eval> functor(){
    BiFunction<Eval<T>,Function<? super T, ? extends R>,Eval<R>> map = EvalInstances::map;
    return General.functor(map);
  }

  /**
   * <pre>
   * {@code
   * Eval<String> list = Evals.unit()
  .unit("hello")
  .convert(Eval::narrowK3);

  //Arrays.asEval("hello"))
   *
   * }
   * </pre>
   *
   *
   * @return A factory for Evals
   */
  public static <T> Pure<eval> unit(){
    return General.<eval,T>unit(EvalInstances::of);
  }
  /**
   *
   * <pre>
   * {@code
   * import static com.aol.cyclops.hkt.jdk.Eval.widen;
   * import static com.aol.cyclops.util.function.Lambda.l1;
   * import static java.util.Arrays.asEval;
   *
  Evals.zippingApplicative()
  .ap(widen(asEval(l1(this::multiplyByTwo))),widen(asEval(1,2,3)));
   *
   * //[2,4,6]
   * }
   * </pre>
   *
   *
   * Example fluent API
   * <pre>
   * {@code
   * Eval<Function<Integer,Integer>> listFn =Evals.unit()
   *                                                  .unit(Lambda.l1((Integer i) ->i*2))
   *                                                  .convert(Eval::narrowK3);

  Eval<Integer> list = Evals.unit()
  .unit("hello")
  .applyHKT(h->Evals.functor().map((String v) ->v.length(), h))
  .applyHKT(h->Evals.applicative().ap(listFn, h))
  .convert(Eval::narrowK3);

  //Arrays.asEval("hello".length()*2))
   *
   * }
   * </pre>
   *
   *
   * @return A zipper for Evals
   */
  public static <T,R> Applicative<eval> applicative(){
    BiFunction<Eval< Function<T, R>>,Eval<T>,Eval<R>> ap = EvalInstances::ap;
    return General.applicative(functor(), unit(), ap);
  }
  /**
   *
   * <pre>
   * {@code
   * import static com.aol.cyclops.hkt.jdk.Eval.widen;
   * Eval<Integer> list  = Evals.monad()
  .flatMap(i->widen(EvalX.range(0,i)), widen(Arrays.asEval(1,2,3)))
  .convert(Eval::narrowK3);
   * }
   * </pre>
   *
   * Example fluent API
   * <pre>
   * {@code
   *    Eval<Integer> list = Evals.unit()
  .unit("hello")
  .applyHKT(h->Evals.monad().flatMap((String v) ->Evals.unit().unit(v.length()), h))
  .convert(Eval::narrowK3);

  //Arrays.asEval("hello".length())
   *
   * }
   * </pre>
   *
   * @return Type class with monad arrow for Evals
   */
  public static <T,R> Monad<eval> monad(){

    BiFunction<Higher<eval,T>,Function<? super T, ? extends Higher<eval,R>>,Higher<eval,R>> flatMap = EvalInstances::flatMap;
    return General.monad(applicative(), flatMap);
  }
  /**
   *
   * <pre>
   * {@code
   *  Eval<String> list = Evals.unit()
  .unit("hello")
  .applyHKT(h->Evals.monadZero().filter((String t)->t.startsWith("he"), h))
  .convert(Eval::narrowK3);

  //Arrays.asEval("hello"));
   *
   * }
   * </pre>
   *
   *
   * @return A filterable monad (with default value)
   */
  public static <T,R> MonadZero<eval> monadZero(){

    return General.monadZero(monad(), Eval.now(null));
  }
  public static <T,R> MonadRec<eval> monadRec(){

    return new MonadRec<eval>(){

      @Override
      public <T, R> Higher<eval, R> tailRec(T initial, Function<? super T, ? extends Higher<eval, ? extends Either<T, R>>> fn) {
        return Eval.tailRec(initial,fn.andThen(Eval::narrowK));
      }
    };
  }



  /**
   * @return Type class for traversables with traverse / sequence operations
   */
  public static <C2,T> Traverse<eval> traverse(){

    return General.traverseByTraverse(applicative(), EvalInstances::traverseA);
  }

  /**
   *
   * <pre>
   * {@code
   * int sum  = Evals.foldable()
  .foldLeft(0, (a,b)->a+b, Eval.widen(Arrays.asEval(1,2,3,4)));

  //10
   *
   * }
   * </pre>
   *
   *
   * @return Type class for folding / reduction operations
   */
  public static <T,R> Foldable<eval> foldable(){
    BiFunction<Monoid<T>,Higher<eval,T>,T> foldRightFn =  (m, l)-> narrowK(l).orElse(m.zero());
    BiFunction<Monoid<T>,Higher<eval,T>,T> foldLeftFn = (m, l)-> narrowK(l).orElse(m.zero());
    Function3<Monoid<R>, Function<T, R>, Higher<eval, T>, R> foldMapFn = (m, f, l)-> narrowK(l).map(f).fold(m);
    return General.foldable(foldRightFn, foldLeftFn,foldMapFn);
  }

  public static <T> Comonad<eval> comonad(){
    Function<? super Higher<eval, T>, ? extends T> extractFn = maybe -> maybe.convert(Eval::narrowK).get();
    return General.comonad(functor(), unit(), extractFn);
  }
  private <T> Eval<T> of(T value){
    return Eval.now(value);
  }
  private static <T,R> Eval<R> ap(Eval<Function< T, R>> lt,  Eval<T> maybe){
    return lt.zip(maybe, (a,b)->a.apply(b));

  }
  private static <T,R> Higher<eval,R> flatMap(Higher<eval,T> lt, Function<? super T, ? extends  Higher<eval,R>> fn){
    return narrowK(lt).flatMap(fn.andThen(Eval::narrowK));
  }
  private static <T,R> Eval<R> map(Eval<T> lt, Function<? super T, ? extends R> fn){
    return  lt.map(fn);
  }


  private static <C2,T,R> Higher<C2, Higher<eval, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn,
                                                                            Higher<eval, T> ds){
    Eval<T> eval = narrowK(ds);
    return applicative.map(Eval::now, fn.apply(eval.get()));
  }

}
