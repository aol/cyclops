package cyclops.collections.mutable;

import com.aol.cyclops2.data.collections.extensions.lazy.LazyListX;
import com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX;
import com.aol.cyclops2.data.collections.extensions.standard.MutableSequenceX;
import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.util.ExceptionSoftener;
import cyclops.async.Future;
import cyclops.control.*;

import cyclops.typeclasses.*;
import com.aol.cyclops2.types.Zippable;
import com.aol.cyclops2.types.foldable.Evaluation;
import com.aol.cyclops2.types.recoverable.OnEmptySwitch;
import com.aol.cyclops2.types.foldable.To;
import com.aol.cyclops2.types.anyM.AnyMSeq;
import cyclops.collections.immutable.VectorX;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.function.Monoid;
import cyclops.monads.AnyM;
import cyclops.monads.WitnessType;
import cyclops.monads.transformers.ListT;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.instances.General;
import cyclops.typeclasses.monad.*;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import org.reactivestreams.Publisher;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cyclops.monads.Witness.list;

/**
 * An eXtended List type, that offers additional functional style operators such as bimap, filter and more
 * Can operate eagerly, lazily or reactively (async push)
 *
 * @author johnmcclean
 *
 * @param <T> the type of elements held in this ListX
 */
public interface ListX<T> extends To<ListX<T>>,
                                  List<T>,
                                  LazyCollectionX<T>,
                                  MutableSequenceX<T>,
                                  Comparable<T>,
                                  OnEmptySwitch<T, List<T>>,
                                  Higher<list,T> {


    default Maybe<T> headMaybe(){
        return headAndTail().headMaybe();
    }
    default T head(){
        return headAndTail().head();
    }
    default ListX<T> tail(){
        return headAndTail().tail().to().listX(Evaluation.LAZY);
    }

    @Override
    default Object[] toArray(){
        return LazyCollectionX.super.toArray();
    }

    @Override
    default <T1> T1[] toArray(T1[] a){
        return LazyCollectionX.super.toArray(a);
    }

    public static <T> ListX<T> defer(Supplier<ListX<T>> s){
        return ListX.of(s)
                    .map(Supplier::get)
                    .flatMap(l->l);
    }

    static <T> CompletableListX<T> completable(){
        return new CompletableListX<>();
    }

    static class CompletableListX<T> implements InvocationHandler{
        Future<ListX<T>> future = Future.future();
        public boolean complete(List<T> result){
            return future.complete(ListX.fromIterable(result));
        }

        public ListX<T> asListX(){
            ListX f = (ListX) Proxy.newProxyInstance(ListX.class.getClassLoader(),
                    new Class[] { ListX.class },
                    this);
            return f;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            //@TODO
            ListX<T> target = future.visit(l->l,t->{throw ExceptionSoftener.throwSoftenedException(t);});
            return method.invoke(target,args);
        }
    }

    ListX<T> lazy();
    ListX<T> eager();
    public static <W1,T> Nested<list,W1,T> nested(ListX<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){
        return Nested.of(nested, Instances.definitions(),def2);
    }
    default <W1> Product<list,W1,T> product(Active<W1,T> active){
        return Product.of(allTypeclasses(),active);
    }
    default <W1> Coproduct<W1,list,T> coproduct(InstanceDefinitions<W1> def2){
        return Coproduct.right(this,def2, Instances.definitions());
    }
    default Active<list,T> allTypeclasses(){
        return Active.of(this,Instances.definitions());
    }
    default <W2,R> Nested<list,W2,R> mapM(Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
        return Nested.of(map(fn), Instances.definitions(), defs);
    }
    /**
     * Convert the raw Higher Kinded Type for ListX types into the ListX type definition class
     *
     * @param list HKT encoded list into a ListX
     * @return ListX
     */
    public static <T> ListX<T> narrowK(final Higher<list, T> list) {
        return (ListX<T>)list;
    }
    static class Instances {

        public static InstanceDefinitions<list> definitions(){
            return new InstanceDefinitions<list>() {
                @Override
                public <T, R> Functor<list> functor() {
                    return Instances.functor();
                }

                @Override
                public <T> Pure<list> unit() {
                    return Instances.unit();
                }

                @Override
                public <T, R> Applicative<list> applicative() {
                    return Instances.zippingApplicative();
                }

                @Override
                public <T, R> Monad<list> monad() {
                    return Instances.monad();
                }

                @Override
                public <T, R> Maybe<MonadZero<list>> monadZero() {
                    return Maybe.just(Instances.monadZero());
                }

                @Override
                public <T> Maybe<MonadPlus<list>> monadPlus() {
                    return Maybe.just(Instances.monadPlus());
                }

                @Override
                public <T> MonadRec<list> monadRec() {
                    return Instances.monadRec();
                }

                @Override
                public <T> Maybe<MonadPlus<list>> monadPlus(Monoid<Higher<list, T>> m) {
                    return Maybe.just(Instances.monadPlus((Monoid)m));
                }

                @Override
                public <C2, T> Traverse<list> traverse() {
                    return Instances.traverse();
                }

                @Override
                public <T> Foldable<list> foldable() {
                    return Instances.foldable();
                }

                @Override
                public <T> Maybe<Comonad<list>> comonad() {
                    return Maybe.nothing();
                }
                @Override
                public <T> Maybe<Unfoldable<list>> unfoldable() {
                    return Maybe.just(Instances.unfoldable());
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
            BiFunction<ListX<T>,Function<? super T, ? extends R>,ListX<R>> map = Instances::map;
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
            return General.<list,T>unit(Instances::of);
        }
        /**
         *
         * <pre>
         * {@code
         * import static com.aol.cyclops2.hkt.jdk.ListX.widen;
         * import static com.aol.cyclops2.util.function.Lambda.l1;
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
            BiFunction<ListX< Function<T, R>>,ListX<T>,ListX<R>> ap = Instances::ap;
            return General.applicative(functor(), unit(), ap);
        }
        /**
         *
         * <pre>
         * {@code
         * import static com.aol.cyclops2.hkt.jdk.ListX.widen;
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
         * @return Type class with monad functions for Lists
         */
        public static <T,R> Monad<list> monad(){

            BiFunction<Higher<list,T>,Function<? super T, ? extends Higher<list,R>>,Higher<list,R>> flatMap = Instances::flatMap;
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
        /**
         * <pre>
         * {@code
         *  ListX<Integer> list = Lists.<Integer>monadPlus()
        .plus(ListX.widen(Arrays.asList()), ListX.widen(Arrays.asList(10)))
        .convert(ListX::narrowK3);
        //Arrays.asList(10))
         *
         * }
         * </pre>
         * @return Type class for combining Lists by concatenation
         */
        public static <T> MonadPlus<list> monadPlus(){
            Monoid<ListX<T>> m = Monoid.of(ListX.empty(), Instances::concat);
            Monoid<Higher<list,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }
        /**
         *
         * <pre>
         * {@code
         *  Monoid<ListX<Integer>> m = Monoid.of(ListX.widen(Arrays.asList()), (a,b)->a.isEmpty() ? b : a);
        ListX<Integer> list = Lists.<Integer>monadPlus(m)
        .plus(ListX.widen(Arrays.asList(5)), ListX.widen(Arrays.asList(10)))
        .convert(ListX::narrowK3);
        //Arrays.asList(5))
         *
         * }
         * </pre>
         *
         * @param m Monoid to use for combining Lists
         * @return Type class for combining Lists
         */
        public static <T> MonadPlus<list> monadPlus(Monoid<ListX<T>> m){
            Monoid<Higher<list,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }

        /**
         * @return Type class for traversables with traverse / sequence operations
         */
        public static <C2,T> Traverse<list> traverse(){
            BiFunction<Applicative<C2>,ListX<Higher<C2, T>>,Higher<C2, ListX<T>>> sequenceFn = (ap,list) -> {

                Higher<C2,ListX<T>> identity = ap.unit(ListX.empty());

                BiFunction<Higher<C2,ListX<T>>,Higher<C2,T>,Higher<C2,ListX<T>>> combineToList =   (acc,next) -> ap.apBiFn(ap.unit((a,b) -> { a.add(b); return a;}),acc,next);

                BinaryOperator<Higher<C2,ListX<T>>> combineLists = (a,b)-> ap.apBiFn(ap.unit((l1,l2)-> { l1.addAll(l2); return l1;}),a,b); ;

                return list.stream()
                        .reduce(identity,
                                combineToList,
                                combineLists);


            };
            BiFunction<Applicative<C2>,Higher<list,Higher<C2, T>>,Higher<C2, Higher<list,T>>> sequenceNarrow  =
                    (a,b) -> Instances.widen2(sequenceFn.apply(a, Instances.narrowK(b)));
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
        private static <T,R> Higher<list,R> flatMap( Higher<list,T> lt, Function<? super T, ? extends  Higher<list,R>> fn){
            return ListX.fromIterable(Instances.narrowK(lt)).flatMap(fn.andThen(Instances::narrowK));
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
        public static <T> Higher<list, T> widen( ListX<T> flux) {

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
    public static  <T> Kleisli<list,ListX<T>,T> kindKleisli(){
        return Kleisli.of(Instances.monad(),Instances::widen);
    }
    public static  <T> Cokleisli<list,T,ListX<T>> kindCokleisli(){
        return Cokleisli.of(Instances::narrowK);
    }

    default <W extends WitnessType<W>> ListT<W, T> liftM(W witness) {
        return ListT.of(witness.adapter().unit(this));
    }

    default AnyMSeq<list,T> anyM(){
        return AnyM.fromList(this);
    }


    /**
     * Create a ListX that contains the Integers between skip and take
     *
     * @param start
     *            Number of range to skip from
     * @param end
     *            Number for range to take at
     * @return Range ListX
     */
    public static ListX<Integer> range(final int start, final int end) {
        return ReactiveSeq.range(start, end).to()
                          .listX(Evaluation.LAZY);
    }

    /**
     * Create a ListX that contains the Longs between skip and take
     *
     * @param start
     *            Number of range to skip from
     * @param end
     *            Number for range to take at
     * @return Range ListX
     */
    public static ListX<Long> rangeLong(final long start, final long end) {
        return ReactiveSeq.rangeLong(start, end).to()
                          .listX(Evaluation.LAZY);
    }

    /**
     * Unfold a function into a ListX
     *
     * <pre>
     * {@code
     *  ListX.unfold(1,i->i<=6 ? Optional.of(Tuple.tuple(i,i+1)) : Optional.zero());
     *
     * //(1,2,3,4,5)
     *
     * }</pre>
     *
     * @param seed Initial value
     * @param unfolder Iteratively applied function, terminated by an zero Optional
     * @return ListX generated by unfolder function
     */
    static <U, T> ListX<T> unfold(final U seed, final Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        return ReactiveSeq.unfold(seed, unfolder)
                          .to()
                          .listX(Evaluation.LAZY);
    }
    /**
     * Generate a ListX from the provided value up to the provided limit number of times
     *
     * @param limit Max number of elements to generate
     * @param s Value for ListX elements
     * @return ListX generated from the provided Supplier
     */
    public static <T> ListX<T> fill(final long limit, final T s) {

        return ReactiveSeq.fill(s)
                          .limit(limit)
                          .to()
                          .listX(Evaluation.LAZY);
    }

    /**
     * Generate a ListX from the provided Supplier up to the provided limit number of times
     *
     * @param limit Max number of elements to generate
     * @param s Supplier to generate ListX elements
     * @return ListX generated from the provided Supplier
     */
    public static <T> ListX<T> generate(final long limit, final Supplier<T> s) {

        return ReactiveSeq.generate(s)
                          .limit(limit)
                          .to()
                          .listX(Evaluation.LAZY);
    }

    /**
     * Create a ListX by iterative application of a function to an initial element up to the supplied limit number of times
     *
     * @param limit Max number of elements to generate
     * @param seed Initial element
     * @param f Iteratively applied to each element to generate the next element
     * @return ListX generated by iterative application
     */
    public static <T> ListX<T> iterate(final long limit, final T seed, final UnaryOperator<T> f) {
        return ReactiveSeq.iterate(seed, f)
                          .limit(limit)
                          .to()
                          .listX(Evaluation.LAZY);

    }
    @Override
    default ListX<T> materialize() {
        return (ListX<T>)LazyCollectionX.super.materialize();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> ListX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
            Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return (ListX)LazyCollectionX.super.forEach4(stream1, stream2, stream3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> ListX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
            Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
            Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return (ListX)LazyCollectionX.super.forEach4(stream1, stream2, stream3, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> ListX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (ListX)LazyCollectionX.super.forEach3(stream1, stream2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> ListX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
            Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (ListX)LazyCollectionX.super.forEach3(stream1, stream2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> ListX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return (ListX)LazyCollectionX.super.forEach2(stream1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> ListX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, Boolean> filterFunction,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return (ListX)LazyCollectionX.super.forEach2(stream1, filterFunction, yieldingFunction);
    }



    /**
     * @return A JDK 8 Collector for converting Streams into ListX instances
     */
    static <T> Collector<T, ?, ListX<T>> listXCollector() {
        return Collectors.toCollection(() -> ListX.of());
    }

    /**
     * @return An Array List Collector
     */
    static <T> Collector<T, ?, List<T>> defaultCollector() {
        return Collectors.toCollection(() -> new ArrayList<>());
    }

    /**
     * @return Unmodifiable array list collector
     */
    static <T> Collector<T, ?, List<T>> immutableCollector() {
        return Collectors.collectingAndThen(defaultCollector(), (final List<T> d) -> Collections.unmodifiableList(d));

    }

    /**
     * @return Construct an zero ListX
     */
    public static <T> ListX<T> empty() {
        return fromIterable((List<T>) defaultCollector().supplier()
                                                        .get());
    }

    /**
     * Construct a ListX from the provided values
     *
     * <pre>
     * {@code
     *     ListX<Integer> deque = ListX.of(1,2,3,4);
     *
     * }</pre>
     *
     *
     *
     * @param values to construct a Deque from
     * @return DequeX
     */
    @SafeVarargs
    public static <T> ListX<T> of(final T... values) {

        return new LazyListX<T>(null,
                ReactiveSeq.of(values),
                defaultCollector());
    }
    public static <T> ListX<T> fromIterator(final Iterator<T> it) {
        return fromIterable(()->it);
    }

    public static <T> ListX<T> singleton(final T value) {
        return ListX.<T> of(value);
    }

    /**
     * Construct a ListX from an Publisher
     *
     * @param publisher
     *            to construct ListX from
     * @return ListX
     */
    public static <T> ListX<T> fromPublisher(final Publisher<? extends T> publisher) {
        return Spouts.from((Publisher<T>) publisher).to()
                          .listX(Evaluation.LAZY);
    }
    /**
     *
     * <pre>
     * {@code
     *  import static cyclops.stream.ReactiveSeq.range;
     *
     *  ListX<Integer> list = listX(range(10,20));
     *
     * }
     * </pre>
     * @param stream To create ListX from
     * @param <T> ListX generated from Stream
     * @return
     */
    public static <T> ListX<T> listX(ReactiveSeq<T> stream){
        return new LazyListX<T>(null,
                stream,
                defaultCollector());
    }

    ListX<T> type(Collector<T, ?, List<T>> collector);
    public static <T> ListX<T> fromIterable(final Iterable<T> it) {
        if (it instanceof ListX)
            return (ListX<T>) it;
        if (it instanceof List)
            return new LazyListX<T>(
                                    (List<T>) it, null,defaultCollector());

        return new LazyListX<T>(null,
                                ReactiveSeq.fromIterable(it),
                                           defaultCollector());
    }

    public static <T> ListX<T> fromIterable(final Collector<T, ?, List<T>> collector, final Iterable<T> it) {
        if (it instanceof ListX)
            return ((ListX<T>) it).withCollector(collector);
        if (it instanceof List)
            return new LazyListX<T>(
                                    (List<T>) it,null,collector);
        return new LazyListX<T>(null,
                                ReactiveSeq.fromIterable(it),
                                collector);
    }
    @Override
    default ListX<T> take(final long num) {

        return (ListX<T>) LazyCollectionX.super.limit(num);
    }
    @Override
    default ListX<T> drop(final long num) {

        return (ListX<T>) LazyCollectionX.super.skip(num);
    }
    ListX<T> withCollector(Collector<T, ?, List<T>> collector);

    /**
     * coflatMap pattern, can be used to perform maybe reductions / collections / folds and other terminal operations
     *
     * <pre>
     * {@code
     *
     *      ListX.of(1,2,3)
     *           .map(i->i*2)
     *           .coflatMap(s -> s.reduce(0,(a,b)->a+b))
     *
     *      //ListX[12]
     * }
     * </pre>
     *
     *
     * @param fn mapping function
     * @return Transformed List
     */
    default <R> ListX<R> coflatMap(Function<? super ListX<T>, ? extends R> fn){
        return fn.andThen(r ->  this.<R>unit(r))
                 .apply(this);
    }



    @Override
    default <R> ListX<R> unit(final Iterable<R> col) {
        return fromIterable(col);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Pure#unit(java.lang.Object)
     */
    @Override
    default <R> ListX<R> unit(final R value) {
        return singleton(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.IterableFunctor#unitIterable(java.util.Iterator)
     */
    @Override
    default <R> ListX<R> unitIterator(final Iterator<R> it) {
        return fromIterable(() -> it);
    }




    @Override
    default ReactiveSeq<T> stream() {

        return ReactiveSeq.fromIterable(this);
    }

    /**
     * @return A Collector to generate a List
     */
    public <T> Collector<T, ?, List<T>> getCollector();


    @Override
    default <T1> ListX<T1> from(final Iterable<T1> c) {
        return ListX.<T1> fromIterable(getCollector(), c);
    }

    @Override
    default <X> ListX<X> fromStream(final ReactiveSeq<X> stream) {
        return new LazyListX<>(null,ReactiveSeq.fromStream(stream), getCollector());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#reverse()
     */
    @Override
    default ListX<T> reverse() {

        return (ListX<T>) LazyCollectionX.super.reverse();
    }

    /**
     * Combine two adjacent elements in a ListX using the supplied BinaryOperator
     * This is a stateful grouping and reduction operation. The emitted of a combination may in turn be combined
     * with it's neighbor
     * <pre>
     * {@code
     *  ListX.of(1,1,2,3)
                   .combine((a, b)->a.equals(b),SemigroupK.intSum)
                   .to()
                   .listX(Evaluation.LAZY)

     *  //ListX(3,4)
     * }</pre>
     *
     * @param predicate Test to see if two neighbors should be joined
     * @param op Reducer to combine neighbors
     * @return Combined / Partially Reduced ListX
     */
    @Override
    default ListX<T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {
        return (ListX<T>) LazyCollectionX.super.combine(predicate, op);
    }

    @Override
    default ListX<T> combine(final Monoid<T> op, final BiPredicate<? super T, ? super T> predicate) {
        return (ListX<T>)LazyCollectionX.super.combine(op,predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#filter(java.util.function.Predicate)
     */
    @Override
    default ListX<T> filter(final Predicate<? super T> pred) {

        return (ListX<T>) LazyCollectionX.super.filter(pred);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#transform(java.util.function.Function)
     */
    @Override
    default <R> ListX<R> map(final Function<? super T, ? extends R> mapper) {

        return (ListX<R>) LazyCollectionX.super.<R> map(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#flatMap(java.util.function.Function)
     */
    @Override
    default <R> ListX<R> flatMap(final Function<? super T, ? extends Iterable<? extends R>> mapper) {

        return (ListX<R>) LazyCollectionX.super.<R> flatMap(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#limit(long)
     */
    @Override
    default ListX<T> limit(final long num) {

        return (ListX<T>) LazyCollectionX.super.limit(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#skip(long)
     */
    @Override
    default ListX<T> skip(final long num) {

        return (ListX<T>) LazyCollectionX.super.skip(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#takeRight(int)
     */
    @Override
    default ListX<T> takeRight(final int num) {
        return (ListX<T>) LazyCollectionX.super.takeRight(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#dropRight(int)
     */
    @Override
    default ListX<T> dropRight(final int num) {
        return (ListX<T>) LazyCollectionX.super.dropRight(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#takeWhile(java.util.function.Predicate)
     */
    @Override
    default ListX<T> takeWhile(final Predicate<? super T> p) {

        return (ListX<T>) LazyCollectionX.super.takeWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#dropWhile(java.util.function.Predicate)
     */
    @Override
    default ListX<T> dropWhile(final Predicate<? super T> p) {

        return (ListX<T>) LazyCollectionX.super.dropWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#takeUntil(java.util.function.Predicate)
     */
    @Override
    default ListX<T> takeUntil(final Predicate<? super T> p) {

        return (ListX<T>) LazyCollectionX.super.takeUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#dropUntil(java.util.function.Predicate)
     */
    @Override
    default ListX<T> dropUntil(final Predicate<? super T> p) {
        return (ListX<T>) LazyCollectionX.super.dropUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#trampoline(java.util.function.Function)
     */
    @Override
    default <R> ListX<R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (ListX<R>) LazyCollectionX.super.<R> trampoline(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#slice(long, long)
     */
    @Override
    default ListX<T> slice(final long from, final long to) {
        return (ListX<T>) LazyCollectionX.super.slice(from, to);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#sorted(java.util.function.Function)
     */
    @Override
    default <U extends Comparable<? super U>> ListX<T> sorted(final Function<? super T, ? extends U> function) {

        return (ListX<T>) LazyCollectionX.super.sorted(function);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#grouped(int)
     */
    @Override
    default ListX<ListX<T>> grouped(final int groupSize) {
        return (ListX<ListX<T>>) LazyCollectionX.super.grouped(groupSize);
    }


    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#zip(java.lang.Iterable)
     */
    @Override
    default <U> ListX<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return (ListX) LazyCollectionX.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> ListX<R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (ListX<R>) LazyCollectionX.super.zip(other, zipper);
    }


    @Override
    default <U, R> ListX<R> zipS(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (ListX<R>) LazyCollectionX.super.zipS(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#sliding(int)
     */
    @Override
    default ListX<VectorX<T>> sliding(final int windowSize) {
        return (ListX<VectorX<T>>) LazyCollectionX.super.sliding(windowSize);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#sliding(int, int)
     */
    @Override
    default ListX<VectorX<T>> sliding(final int windowSize, final int increment) {
        return (ListX<VectorX<T>>) LazyCollectionX.super.sliding(windowSize, increment);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#scanLeft(cyclops2.function.Monoid)
     */
    @Override
    default ListX<T> scanLeft(final Monoid<T> monoid) {
        return (ListX<T>) LazyCollectionX.super.scanLeft(monoid);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    default <U> ListX<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {
        return (ListX<U>) LazyCollectionX.super.scanLeft(seed, function);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#scanRight(cyclops2.function.Monoid)
     */
    @Override
    default ListX<T> scanRight(final Monoid<T> monoid) {
        return (ListX<T>) LazyCollectionX.super.scanRight(monoid);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    default <U> ListX<U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {
        return (ListX<U>) LazyCollectionX.super.scanRight(identity, combiner);
    }

    /* Makes a defensive copy of this ListX replacing the value at i with the specified element
     *  (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableSequenceX#with(int, java.lang.Object)
     */
    @Override
    default ListX<T> insertAt(final int i, final T element) {
        return from(stream()
                            .insertAt(i, element));
    }

    /* (non-Javadoc)
     * @see java.util.List#subList(int, int)
     */
    @Override
    public ListX<T> subList(int start, int end);

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#plus(java.lang.Object)
     */
    @Override
    default ListX<T> plus(final T e) {
        add(e);
        return this;
    }


    @Override
    default ListX<T> plusAll(final Iterable<? extends T> list) {
        for(T next : list)
            add(next);
        return this;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.MutableSequenceX#removeAt(int)
     */
    @Override
    default ListX<T> removeAt(final int pos) {
        remove(pos);
        return this;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#removeValue(java.lang.Object)
     */
    @Override
    default ListX<T> removeValue(final Object e) {
        this.remove(e);
        return this;
    }


    @Override
    default ListX<T> removeAll(final Iterable<? extends T> list) {
        for(T next : list){
            this.removeValue(next);
        }
        return this;
    }



    @Override
    default ListX<T> insertAt(final int i, final Iterable<? extends T> list) {
        return fromStream(stream().insertAt(i,list));
    }

    @Override
    int size();

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.FluentCollectionX#plusInOrder(java.lang.Object)
     */
    @Override
    default ListX<T> plusInOrder(final T e) {

        return (ListX<T>) MutableSequenceX.super.plusInOrder(e);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.CollectionX#peek(java.util.function.Consumer)
     */
    @Override
    default ListX<T> peek(final Consumer<? super T> c) {

        return (ListX<T>) LazyCollectionX.super.peek(c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#cycle(int)
     */
    @Override
    default ListX<T> cycle(final long times) {

        return (ListX<T>) LazyCollectionX.super.cycle(times);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#cycle(com.aol.cyclops2.sequence.Monoid, int)
     */
    @Override
    default ListX<T> cycle(final Monoid<T> m, final long times) {

        return (ListX<T>) LazyCollectionX.super.cycle(m, times);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#cycleWhile(java.util.function.Predicate)
     */
    @Override
    default ListX<T> cycleWhile(final Predicate<? super T> predicate) {

        return (ListX<T>) LazyCollectionX.super.cycleWhile(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#cycleUntil(java.util.function.Predicate)
     */
    @Override
    default ListX<T> cycleUntil(final Predicate<? super T> predicate) {
        return (ListX<T>) LazyCollectionX.super.cycleUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#zip(java.util.stream.Stream)
     */
    @Override
    default <U> ListX<Tuple2<T, U>> zipS(final Stream<? extends U> other) {

        return (ListX) LazyCollectionX.super.zipS(other);
    }


    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <S, U> ListX<Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {

        return (ListX) LazyCollectionX.super.zip3(second, third);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <T2, T3, T4> ListX<Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
            final Iterable<? extends T4> fourth) {

        return (ListX) LazyCollectionX.super.zip4(second, third, fourth);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#zipWithIndex()
     */
    @Override
    default ListX<Tuple2<T, Long>> zipWithIndex() {

        return (ListX<Tuple2<T, Long>>) LazyCollectionX.super.zipWithIndex();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#sorted()
     */
    @Override
    default ListX<T> sorted() {

        return (ListX<T>) LazyCollectionX.super.sorted();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#sorted(java.util.Comparator)
     */
    @Override
    default ListX<T> sorted(final Comparator<? super T> c) {

        return (ListX<T>) LazyCollectionX.super.sorted(c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#skipWhile(java.util.function.Predicate)
     */
    @Override
    default ListX<T> skipWhile(final Predicate<? super T> p) {

        return (ListX<T>) LazyCollectionX.super.skipWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#skipUntil(java.util.function.Predicate)
     */
    @Override
    default ListX<T> skipUntil(final Predicate<? super T> p) {

        return (ListX<T>) LazyCollectionX.super.skipUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#shuffle()
     */
    @Override
    default ListX<T> shuffle() {

        return (ListX<T>) LazyCollectionX.super.shuffle();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#skipLast(int)
     */
    @Override
    default ListX<T> skipLast(final int num) {

        return (ListX<T>) LazyCollectionX.super.skipLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#shuffle(java.util.Random)
     */
    @Override
    default ListX<T> shuffle(final Random random) {

        return (ListX<T>) LazyCollectionX.super.shuffle(random);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#permutations()
     */
    @Override
    default ListX<ReactiveSeq<T>> permutations() {

        return (ListX<ReactiveSeq<T>>) LazyCollectionX.super.permutations();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#combinations(int)
     */
    @Override
    default ListX<ReactiveSeq<T>> combinations(final int size) {

        return (ListX<ReactiveSeq<T>>) LazyCollectionX.super.combinations(size);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#combinations()
     */
    @Override
    default ListX<ReactiveSeq<T>> combinations() {

        return (ListX<ReactiveSeq<T>>) LazyCollectionX.super.combinations();
    }


    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#distinct()
     */
    @Override
    default ListX<T> distinct() {

        return (ListX<T>) LazyCollectionX.super.distinct();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#limitWhile(java.util.function.Predicate)
     */
    @Override
    default ListX<T> limitWhile(final Predicate<? super T> p) {

        return (ListX<T>) LazyCollectionX.super.limitWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#limitUntil(java.util.function.Predicate)
     */
    @Override
    default ListX<T> limitUntil(final Predicate<? super T> p) {

        return (ListX<T>) LazyCollectionX.super.limitUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#intersperse(java.lang.Object)
     */
    @Override
    default ListX<T> intersperse(final T value) {

        return (ListX<T>) LazyCollectionX.super.intersperse(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#limitLast(int)
     */
    @Override
    default ListX<T> limitLast(final int num) {

        return (ListX<T>) LazyCollectionX.super.limitLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#onEmpty(java.lang.Object)
     */
    @Override
    default ListX<T> onEmpty(final T value) {

        return (ListX<T>) LazyCollectionX.super.onEmpty(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default ListX<T> onEmptyGet(final Supplier<? extends T> supplier) {

        return (ListX<T>) LazyCollectionX.super.onEmptyGet(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#onEmptyError(java.util.function.Supplier)
     */
    @Override
    default <X extends Throwable> ListX<T> onEmptyError(final Supplier<? extends X> supplier) {

        return (ListX<T>) LazyCollectionX.super.onEmptyError(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#ofType(java.lang.Class)
     */
    @Override
    default <U> ListX<U> ofType(final Class<? extends U> type) {

        return (ListX<U>) LazyCollectionX.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#filterNot(java.util.function.Predicate)
     */
    @Override
    default ListX<T> filterNot(final Predicate<? super T> fn) {

        return (ListX<T>) LazyCollectionX.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#notNull()
     */
    @Override
    default ListX<T> notNull() {

        return (ListX<T>) LazyCollectionX.super.notNull();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#removeAll(java.util.stream.Stream)
     */
    @Override
    default ListX<T> removeAllS(final Stream<? extends T> stream) {

        return (ListX<T>) LazyCollectionX.super.removeAllS(stream);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#removeAll(java.lang.Iterable)
     */
    @Override
    default ListX<T> removeAllI(final Iterable<? extends T> it) {

        return (ListX<T>) LazyCollectionX.super.removeAllI(it);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#removeAll(java.lang.Object[])
     */
    @Override
    default ListX<T> removeAll(final T... values) {

        return (ListX<T>) LazyCollectionX.super.removeAll(values);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#retainAllI(java.lang.Iterable)
     */
    @Override
    default ListX<T> retainAllI(final Iterable<? extends T> it) {

        return (ListX<T>) LazyCollectionX.super.retainAllI(it);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#retainAllI(java.util.stream.Stream)
     */
    @Override
    default ListX<T> retainAllS(final Stream<? extends T> seq) {

        return (ListX<T>) LazyCollectionX.super.retainAllS(seq);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#retainAllI(java.lang.Object[])
     */
    @Override
    default ListX<T> retainAll(final T... values) {

        return (ListX<T>) LazyCollectionX.super.retainAll(values);
    }

    /* (non-Javadoc)
    * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#grouped(int, java.util.function.Supplier)
    */
    @Override
    default <C extends Collection<? super T>> ListX<C> grouped(final int size, final Supplier<C> supplier) {

        return (ListX<C>) LazyCollectionX.super.grouped(size, supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#groupedUntil(java.util.function.Predicate)
     */
    @Override
    default ListX<ListX<T>> groupedUntil(final Predicate<? super T> predicate) {

        return (ListX<ListX<T>>) LazyCollectionX.super.groupedUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#groupedWhile(java.util.function.Predicate)
     */
    @Override
    default ListX<ListX<T>> groupedWhile(final Predicate<? super T> predicate) {

        return (ListX<ListX<T>>) LazyCollectionX.super.groupedWhile(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> ListX<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (ListX<C>) LazyCollectionX.super.groupedWhile(predicate, factory);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> ListX<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (ListX<C>) LazyCollectionX.super.groupedUntil(predicate, factory);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX#groupedStatefullyUntil(java.util.function.BiPredicate)
     */
    @Override
    default ListX<ListX<T>> groupedStatefullyUntil(final BiPredicate<ListX<? super T>, ? super T> predicate) {

        return (ListX<ListX<T>>) LazyCollectionX.super.groupedStatefullyUntil(predicate);
    }





    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.recoverable.OnEmptySwitch#onEmptySwitch(java.util.function.Supplier)
     */
    @Override
    default ListX<T> onEmptySwitch(final Supplier<? extends List<T>> supplier) {
        if (isEmpty())
            return ListX.fromIterable(supplier.get());
        return this;
    }

    @Override
    default int compareTo(final T o) {
        if (o instanceof List) {
            final List l = (List) o;
            if (this.size() == l.size()) {
                final Iterator i1 = iterator();
                final Iterator i2 = l.iterator();
                if (i1.hasNext()) {
                    if (i2.hasNext()) {
                        final int comp = Comparator.<Comparable> naturalOrder()
                                .compare((Comparable) i1.next(), (Comparable) i2.next());
                        if (comp != 0)
                            return comp;
                    }
                    return 1;
                } else {
                    if (i2.hasNext())
                        return -1;
                    else
                        return 0;
                }
            }
            return this.size() - ((List) o).size();
        } else
            return 1;

    }


    @Override
    default <R> ListX<R> retry(final Function<? super T, ? extends R> fn) {
        return (ListX<R>)LazyCollectionX.super.retry(fn);
    }

    @Override
    default <R> ListX<R> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (ListX<R>)LazyCollectionX.super.retry(fn,retries,delay,timeUnit);
    }

    @Override
    default <R> ListX<R> flatMapS(Function<? super T, ? extends Stream<? extends R>> fn) {
        return (ListX<R>)LazyCollectionX.super.flatMapS(fn);
    }

    @Override
    default <R> ListX<R> flatMapP(Function<? super T, ? extends Publisher<? extends R>> fn) {
        return (ListX<R>)LazyCollectionX.super.flatMapP(fn);
    }

    @Override
    default ListX<T> prependS(Stream<? extends T> stream) {
        return (ListX<T>)LazyCollectionX.super.prependS(stream);
    }

    @Override
    default ListX<T> append(T... values) {
        return (ListX<T>)LazyCollectionX.super.append(values);
    }

    @Override
    default ListX<T> append(T value) {
        return (ListX<T>)LazyCollectionX.super.append(value);
    }

    @Override
    default ListX<T> prepend(T value) {
        return (ListX<T>)LazyCollectionX.super.prepend(value);
    }

    @Override
    default ListX<T> prependAll(T... values) {
        return (ListX<T>)LazyCollectionX.super.prependAll(values);
    }

    @Override
    default ListX<T> insertAt(int pos, T... values) {
        return (ListX<T>)LazyCollectionX.super.insertAt(pos,values);
    }

    @Override
    default ListX<T> deleteBetween(int start, int end) {
        return (ListX<T>)LazyCollectionX.super.deleteBetween(start,end);
    }

    @Override
    default ListX<T> insertAtS(int pos, Stream<T> stream) {
        return (ListX<T>)LazyCollectionX.super.insertAtS(pos,stream);
    }

    @Override
    default ListX<T> recover(final Function<? super Throwable, ? extends T> fn) {
        return (ListX<T>)LazyCollectionX.super.recover(fn);
    }

    @Override
    default <EX extends Throwable> ListX<T> recover(Class<EX> exceptionClass, final Function<? super EX, ? extends T> fn) {
        return (ListX<T>)LazyCollectionX.super.recover(exceptionClass,fn);
    }

    @Override
    default ListX<T> plusLoop(int max, IntFunction<T> value) {
        return (ListX<T>)LazyCollectionX.super.plusLoop(max,value);
    }

    @Override
    default ListX<T> plusLoop(Supplier<Option<T>> supplier) {
        return (ListX<T>)LazyCollectionX.super.plusLoop(supplier);
    }


    /**
     * Narrow a covariant List
     *
     * <pre>
     * {@code
     * ListX<? extends Fruit> list = ListX.of(apple,bannana);
     * ListX<Fruit> fruitList = ListX.narrow(list);
     * }
     * </pre>
     *
     * @param listX to narrow generic type
     * @return ListX with narrowed type
     */
    public static <T> ListX<T> narrow(final ListX<? extends T> listX) {
        return (ListX<T>) listX;
    }

    @Override
    default ListX<T> zip(BinaryOperator<Zippable<T>> combiner, final Zippable<T> app) {
        return (ListX<T>)LazyCollectionX.super.zip(combiner,app);
    }

    @Override
    default <R> ListX<R> zipWith(Iterable<Function<? super T, ? extends R>> fn) {
        return (ListX<R>)LazyCollectionX.super.zipWith(fn);
    }

    @Override
    default <R> ListX<R> zipWithS(Stream<Function<? super T, ? extends R>> fn) {
        return (ListX<R>)LazyCollectionX.super.zipWithS(fn);
    }

    @Override
    default <R> ListX<R> zipWithP(Publisher<Function<? super T, ? extends R>> fn) {
        return (ListX<R>)LazyCollectionX.super.zipWithP(fn);
    }

    @Override
    default <T2, R> ListX<R> zipP(final Publisher<? extends T2> publisher, final BiFunction<? super T, ? super T2, ? extends R> fn) {
        return (ListX<R>)LazyCollectionX.super.zipP(publisher,fn);
    }



    @Override
    default <U> ListX<Tuple2<T, U>> zipP(final Publisher<? extends U> other) {
        return (ListX)LazyCollectionX.super.zipP(other);
    }


    @Override
    default <S, U, R> ListX<R> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third, final Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (ListX<R>)LazyCollectionX.super.zip3(second,third,fn3);
    }

    @Override
    default <T2, T3, T4, R> ListX<R> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (ListX<R>)LazyCollectionX.super.zip4(second,third,fourth,fn);
    }
    public static  <T,R> ListX<R> tailRec(T initial, Function<? super T, ? extends Iterable<? extends Either<T, R>>> fn) {
        ListX<Either<T, R>> lazy = ListX.of(Either.left(initial));
        ListX<Either<T, R>> next = lazy.eager();
        boolean newValue[] = {true};
        for(;;){

            next = next.flatMap(e -> e.visit(s -> {
                        newValue[0]=true;
                        return fn.apply(s); },
                    p -> {
                        newValue[0]=false;
                        return ListX.of(e);
                    }));
            if(!newValue[0])
                break;

        }
        return Either.sequenceRight(next).orElse(ListX.empty());
    }
}
