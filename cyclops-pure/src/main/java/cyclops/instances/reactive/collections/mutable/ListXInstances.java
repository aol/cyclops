package cyclops.instances.reactive.collections.mutable;

import static com.oath.cyclops.data.ReactiveWitness.*;

import com.oath.cyclops.hkt.Higher;
import cyclops.arrow.Cokleisli;
import cyclops.arrow.Kleisli;
import cyclops.reactive.collections.mutable.ListX;
import cyclops.control.Either;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import cyclops.function.Monoid;
import cyclops.hkt.Active;
import cyclops.hkt.Coproduct;
import cyclops.hkt.Nested;
import cyclops.hkt.Product;
import cyclops.reactive.ReactiveSeq;
import cyclops.typeclasses.*;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.arrow.MonoidK;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.instances.General;
import cyclops.typeclasses.monad.*;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;

@UtilityClass
public class ListXInstances {
  public static  <T> Kleisli<list,ListX<T>,T> kindKleisli(){
    return Kleisli.of(ListXInstances.monad(), ListXInstances::widen);
  }
  public static  <T> Cokleisli<list,T,ListX<T>> kindCokleisli(){
    return Cokleisli.of(ListXInstances::narrowK);
  }
  public static <W1,T> Nested<list,W1,T> nested(ListX<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){
    return Nested.of(nested, ListXInstances.definitions(),def2);
  }
  public static  <W1,T> Product<list,W1,T> product(ListX<T> l, Active<W1,T> active){
    return Product.of(allTypeclasses(l),active);
  }
  public static  <W1,T> Coproduct<W1,list,T> coproduct(ListX<T> l, InstanceDefinitions<W1> def2){
    return Coproduct.right(l,def2, ListXInstances.definitions());
  }
  public static  <T> Active<list,T> allTypeclasses(ListX<T> l){
    return Active.of(l, ListXInstances.definitions());
  }
  public static  <W2,R,T> Nested<list,W2,R> mapM(ListX<T> l,Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
    return Nested.of(l.map(fn), ListXInstances.definitions(), defs);
  }
  public static InstanceDefinitions<list> definitions(){
    return new InstanceDefinitions<list>() {
      @Override
      public <T, R> Functor<list> functor() {
        return ListXInstances.functor();
      }

      @Override
      public <T> Pure<list> unit() {
        return ListXInstances.unit();
      }

      @Override
      public <T, R> Applicative<list> applicative() {
        return ListXInstances.zippingApplicative();
      }

      @Override
      public <T, R> Monad<list> monad() {
        return ListXInstances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<list>> monadZero() {
        return Option.some(ListXInstances.monadZero());
      }

      @Override
      public <T> Option<MonadPlus<list>> monadPlus() {
        return Option.some(ListXInstances.monadPlus());
      }

      @Override
      public <T> MonadRec<list> monadRec() {
        return ListXInstances.monadRec();
      }

      @Override
      public <T> Option<MonadPlus<list>> monadPlus(MonoidK<list> m) {
        MonadPlus<list> x = ListXInstances.monadPlus(m);
        return Maybe.just(x);
      }

      @Override
      public <C2, T> Traverse<list> traverse() {
        return ListXInstances.traverse();
      }

      @Override
      public <T> Foldable<list> foldable() {
        return ListXInstances.foldable();
      }

      @Override
      public <T> Option<Comonad<list>> comonad() {
        return Maybe.nothing();
      }
      @Override
      public <T> Option<Unfoldable<list>> unfoldable() {
        return Option.some(ListXInstances.unfoldable());
      }
    };

  }
  public static Unfoldable<list> unfoldable(){
    return new Unfoldable<list>() {
      @Override
      public <R, T> Higher<list, R> unfold(T b, Function<? super T, Option<Tuple2<R, T>>> fn) {
        return ListX.unfold(b,fn);
      }
    };
  }
  /**
   *
   * Transform a list, mulitplying every element by 2
   *
   * <pre>
   * {@code
   *  ListX<Integer> list = Lists.functor().map(i->i*2, ListX.widen(Arrays.asList(1,2,3));
   *
   *  //[2,4,6]
   *
   *
   * }
   * </pre>
   *
   * An example fluent api working with Lists
   * <pre>
   * {@code
   *   ListX<Integer> list = Lists.unit()
  .unit("hello")
  .applyHKT(h->Lists.functor().map((String v) ->v.length(), h))
  .convert(ListX::narrowK3);
   *
   * }
   * </pre>
   *
   *
   * @return A functor for Lists
   */
  public static <T,R>Functor<list> functor(){
    BiFunction<ListX<T>,Function<? super T, ? extends R>,ListX<R>> map = ListXInstances::map;
    return General.functor(map);
  }
  /**
   * <pre>
   * {@code
   * ListX<String> list = Lists.unit()
  .unit("hello")
  .convert(ListX::narrowK3);

  //Arrays.asList("hello"))
   *
   * }
   * </pre>
   *
   *
   * @return A factory for Lists
   */
  public static <T> Pure<list> unit(){
    return General.<list,T>unit(ListXInstances::of);
  }
  /**
   *
   * <pre>
   * {@code
   * import static com.oath.cyclops.hkt.jdk.ListX.widen;
   * import static com.oath.cyclops.util.function.Lambda.l1;
   * import static java.util.Arrays.asList;
   *
  Lists.zippingApplicative()
  .ap(widen(asList(l1(this::multiplyByTwo))),widen(asList(1,2,3)));
   *
   * //[2,4,6]
   * }
   * </pre>
   *
   *
   * Example fluent API
   * <pre>
   * {@code
   * ListX<Function<Integer,Integer>> listFn =Lists.unit()
   *                                                  .unit(Lambda.l1((Integer i) ->i*2))
   *                                                  .convert(ListX::narrowK3);

  ListX<Integer> list = Lists.unit()
  .unit("hello")
  .applyHKT(h->Lists.functor().map((String v) ->v.length(), h))
  .applyHKT(h->Lists.zippingApplicative().ap(listFn, h))
  .convert(ListX::narrowK3);

  //Arrays.asList("hello".length()*2))
   *
   * }
   * </pre>
   *
   *
   * @return A zipper for Lists
   */
  public static <T,R> Applicative<list> zippingApplicative(){
    BiFunction<ListX< Function<T, R>>,ListX<T>,ListX<R>> ap = ListXInstances::ap;
    return General.applicative(functor(), unit(), ap);
  }
  /**
   *
   * <pre>
   * {@code
   * import static com.oath.cyclops.hkt.jdk.ListX.widen;
   * ListX<Integer> list  = Lists.monad()
  .flatMap(i->widen(ListX.range(0,i)), widen(Arrays.asList(1,2,3)))
  .convert(ListX::narrowK3);
   * }
   * </pre>
   *
   * Example fluent API
   * <pre>
   * {@code
   *    ListX<Integer> list = Lists.unit()
  .unit("hello")
  .applyHKT(h->Lists.monad().flatMap((String v) ->Lists.unit().unit(v.length()), h))
  .convert(ListX::narrowK3);

  //Arrays.asList("hello".length())
   *
   * }
   * </pre>
   *
   * @return Type class with monad arrow for Lists
   */
  public static <T,R> Monad<list> monad(){

    BiFunction<Higher<list,T>,Function<? super T, ? extends Higher<list,R>>,Higher<list,R>> flatMap = ListXInstances::flatMap;
    return General.monad(zippingApplicative(), flatMap);
  }
  public static <T,R> MonadRec<list> monadRec(){

    return new MonadRec<list>(){
      @Override
      public <T, R> Higher<list, R> tailRec(T initial, Function<? super T, ? extends Higher<list,? extends Either<T, R>>> fn) {
        return ListX.tailRec(initial,fn.andThen(ListX::narrowK));
      }
    };
  }

  /**
   *
   * <pre>
   * {@code
   *  ListX<String> list = Lists.unit()
  .unit("hello")
  .applyHKT(h->Lists.monadZero().filter((String t)->t.startsWith("he"), h))
  .convert(ListX::narrowK3);

  //Arrays.asList("hello"));
   *
   * }
   * </pre>
   *
   *
   * @return A filterable monad (with default value)
   */
  public static <T,R> MonadZero<list> monadZero(){

    return General.monadZero(monad(), ListX.empty());
  }

  public static <T> MonadPlus<list> monadPlus(){

    MonoidK<list> m = new MonoidK<list>() {
      @Override
      public <T> Higher<list, T> zero() {
        return ListX.empty();
      }

      @Override
      public <T> Higher<list, T> apply(Higher<list, T> t1, Higher<list, T> t2) {
        return ListXInstances.concat(narrowK(t1),narrowK(t2));
      }
    };
    return General.monadPlus(monadZero(),m);
  }

  public static <T> MonadPlus<list> monadPlus(MonoidK<list> m){

    return General.monadPlus(monadZero(),m);
  }

  /**
   * @return Type class for traversables with traverse / sequence operations
   */
  public static <C2,T> Traverse<list> traverse(){
    BiFunction<Applicative<C2>,ListX<Higher<C2, T>>,Higher<C2, ListX<T>>> sequenceFn = (ap,list) -> {

      Higher<C2,ListX<T>> identity = ap.unit(ListX.empty());

      BiFunction<Higher<C2,ListX<T>>,Higher<C2,T>,Higher<C2,ListX<T>>> combineToList =   (acc,next) -> ap.apBiFn(ap.unit((a,b) -> { a.add(b); return a;}),acc,next);

      BinaryOperator<Higher<C2,ListX<T>>> combineLists = (a, b)-> ap.apBiFn(ap.unit((l1, l2)-> { l1.addAll(l2); return l1;}),a,b); ;

      return list.stream()
        .reduce(identity,
          combineToList,
          combineLists);


    };
    BiFunction<Applicative<C2>,Higher<list,Higher<C2, T>>,Higher<C2, Higher<list,T>>> sequenceNarrow  =
      (a,b) -> ListXInstances.widen2(sequenceFn.apply(a, ListXInstances.narrowK(b)));
    return General.traverse(zippingApplicative(), sequenceNarrow);
  }

  /**
   *
   * <pre>
   * {@code
   * int sum  = Lists.foldable()
  .foldLeft(0, (a,b)->a+b, ListX.widen(Arrays.asList(1,2,3,4)));

  //10
   *
   * }
   * </pre>
   *
   *
   * @return Type class for folding / reduction operations
   */
  public static <T> Foldable<list> foldable(){
    return new Foldable<list>() {
      @Override
      public <T> T foldRight(Monoid<T> monoid, Higher<list, T> ds) {
        return  ListX.fromIterable(narrow(ds)).foldRight(monoid);
      }

      @Override
      public <T> T foldLeft(Monoid<T> monoid, Higher<list, T> ds) {
        return  ListX.fromIterable(narrow(ds)).foldLeft(monoid);
      }

      @Override
      public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<list, T> nestedA) {
        return narrow(nestedA).<R>map(fn).foldLeft(mb);


      }
    };

  }

  private static  <T> ListX<T> concat(List<T> l1, List<T> l2){
    return ListX.listX(ReactiveSeq.fromStream(Stream.concat(l1.stream(),l2.stream())));
  }
  private static <T> ListX<T> of(T value){
    return ListX.of(value);
  }
  private static <T,R> ListX<R> ap(ListX<Function< T, R>> lt,  ListX<T> list){
    return ListX.fromIterable(lt).zip(list,(a,b)->a.apply(b));
  }
  private static <T,R> Higher<list,R> flatMap(Higher<list,T> lt, Function<? super T, ? extends  Higher<list,R>> fn){
    return narrowK(lt).concatMap(fn.andThen(ListXInstances::narrowK));
  }
  private static <T,R> ListX<R> map(ListX<T> lt, Function<? super T, ? extends R> fn){
    return ListX.fromIterable(lt).map(fn);
  }



  /**
   * Widen a ListType nest inside another HKT encoded type
   *
   * @param flux HTK encoded type containing  a List to widen
   * @return HKT encoded type with a widened List
   */
  public static <C2, T> Higher<C2, Higher<list, T>> widen2(Higher<C2, ListX<T>> flux) {
    // a functor could be used (if C2 is a functor / one exists for C2 type)
    // instead of casting
    // cast seems safer as Higher<list,T> must be a ListX
    return (Higher) flux;
  }
  public static <T> Higher<list, T> widen(ListX<T> flux) {

    return flux;
  }



  /**
   * Convert the raw Higher Kinded Type for ListType types into the ListType type definition class
   *
   * @param future HKT encoded list into a ListType
   * @return ListType
   */
  public static <T> ListX<T> narrowK(final Higher<list, T> future) {
    return (ListX<T>) future;
  }


  /**
   * Convert the HigherKindedType definition for a List into
   *
   * @param  completableList Type Constructor to convert back into narrowed type
   * @return List from Higher Kinded Type
   */
  public static <T> ListX<T> narrow(final Higher<list, T> completableList) {

    return ((ListX<T>) completableList);//.narrow();

  }
}
