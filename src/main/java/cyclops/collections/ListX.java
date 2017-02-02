package cyclops.collections;

import com.aol.cyclops2.data.collections.extensions.FluentCollectionX;
import com.aol.cyclops2.data.collections.extensions.lazy.LazyListX;
import com.aol.cyclops2.data.collections.extensions.standard.MutableCollectionX;
import com.aol.cyclops2.data.collections.extensions.standard.MutableSequenceX;
import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.types.OnEmptySwitch;
import com.aol.cyclops2.types.To;
import com.aol.cyclops2.types.Unit;
import com.aol.cyclops2.types.anyM.AnyMSeq;
import cyclops.collections.immutable.PVectorX;
import cyclops.control.Trampoline;
import cyclops.function.Fn3;
import cyclops.function.Fn4;
import cyclops.function.Monoid;
import cyclops.monads.AnyM;
import cyclops.monads.WitnessType;
import cyclops.monads.transformers.ListT;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Spouts;
import cyclops.typeclasses.Pure;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.instances.General;
import cyclops.typeclasses.monad.*;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Publisher;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cyclops.monads.Witness.list;

/**
 * An eXtended List type, that offers additional eagerly executed functional style operators such as bimap, filter and more
 * 
 * @author johnmcclean
 *
 * @param <T> the type of elements held in this collection
 */
public interface ListX<T> extends To<ListX<T>>,
                                  List<T>,
                                  MutableCollectionX<T>,
                                  MutableSequenceX<T>,
                                  Comparable<T>,
                                  OnEmptySwitch<T, List<T>>,
                                  Higher<ListX.µ,T> {

    public static class µ {
    }

    /**
     * Convert the raw Higher Kinded Type for ListX types into the ListX type definition class
     *
     * @param deque HKT encoded list into a ListX
     * @return ListX
     */
    public static <T> ListX<T> narrowK(final Higher<ListX.µ, T> list) {
        return (ListX<T>)list;
    }
    static class Instances {
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
        .transform(h->Lists.functor().map((String v) ->v.length(), h))
        .convert(ListX::narrowK);
         *
         * }
         * </pre>
         *
         *
         * @return A functor for Lists
         */
        public static <T,R>Functor<µ> functor(){
            BiFunction<ListX<T>,Function<? super T, ? extends R>,ListX<R>> map = Instances::map;
            return General.functor(map);
        }
        /**
         * <pre>
         * {@code
         * ListX<String> list = Lists.unit()
        .unit("hello")
        .convert(ListX::narrowK);

        //Arrays.asList("hello"))
         *
         * }
         * </pre>
         *
         *
         * @return A factory for Lists
         */
        public static <T> Pure<µ> unit(){
            return General.<ListX.µ,T>unit(Instances::of);
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
         *                                                  .convert(ListX::narrowK);

        ListX<Integer> list = Lists.unit()
        .unit("hello")
        .transform(h->Lists.functor().map((String v) ->v.length(), h))
        .transform(h->Lists.zippingApplicative().ap(listFn, h))
        .convert(ListX::narrowK);

        //Arrays.asList("hello".length()*2))
         *
         * }
         * </pre>
         *
         *
         * @return A zipper for Lists
         */
        public static <T,R> Applicative<µ> zippingApplicative(){
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
        .convert(ListX::narrowK);
         * }
         * </pre>
         *
         * Example fluent API
         * <pre>
         * {@code
         *    ListX<Integer> list = Lists.unit()
        .unit("hello")
        .transform(h->Lists.monad().flatMap((String v) ->Lists.unit().unit(v.length()), h))
        .convert(ListX::narrowK);

        //Arrays.asList("hello".length())
         *
         * }
         * </pre>
         *
         * @return Type class with monad functions for Lists
         */
        public static <T,R> Monad<µ> monad(){

            BiFunction<Higher<ListX.µ,T>,Function<? super T, ? extends Higher<ListX.µ,R>>,Higher<ListX.µ,R>> flatMap = Instances::flatMap;
            return General.monad(zippingApplicative(), flatMap);
        }
        /**
         *
         * <pre>
         * {@code
         *  ListX<String> list = Lists.unit()
        .unit("hello")
        .transform(h->Lists.monadZero().filter((String t)->t.startsWith("he"), h))
        .convert(ListX::narrowK);

        //Arrays.asList("hello"));
         *
         * }
         * </pre>
         *
         *
         * @return A filterable monad (with default value)
         */
        public static <T,R> MonadZero<µ> monadZero(){

            return General.monadZero(monad(), ListX.empty());
        }
        /**
         * <pre>
         * {@code
         *  ListX<Integer> list = Lists.<Integer>monadPlus()
        .plus(ListX.widen(Arrays.asList()), ListX.widen(Arrays.asList(10)))
        .convert(ListX::narrowK);
        //Arrays.asList(10))
         *
         * }
         * </pre>
         * @return Type class for combining Lists by concatenation
         */
        public static <T> MonadPlus<µ> monadPlus(){
            Monoid<ListX<T>> m = Monoid.of(ListX.empty(), Instances::concat);
            Monoid<Higher<ListX.µ,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }
        /**
         *
         * <pre>
         * {@code
         *  Monoid<ListX<Integer>> m = Monoid.of(ListX.widen(Arrays.asList()), (a,b)->a.isEmpty() ? b : a);
        ListX<Integer> list = Lists.<Integer>monadPlus(m)
        .plus(ListX.widen(Arrays.asList(5)), ListX.widen(Arrays.asList(10)))
        .convert(ListX::narrowK);
        //Arrays.asList(5))
         *
         * }
         * </pre>
         *
         * @param m Monoid to use for combining Lists
         * @return Type class for combining Lists
         */
        public static <T> MonadPlus<ListX.µ> monadPlus(Monoid<ListX<T>> m){
            Monoid<Higher<ListX.µ,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }

        /**
         * @return Type class for traversables with traverse / sequence operations
         */
        public static <C2,T> Traverse<µ> traverse(){
            BiFunction<Applicative<C2>,ListX<Higher<C2, T>>,Higher<C2, ListX<T>>> sequenceFn = (ap,list) -> {

                Higher<C2,ListX<T>> identity = ap.unit(ListX.empty());

                BiFunction<Higher<C2,ListX<T>>,Higher<C2,T>,Higher<C2,ListX<T>>> combineToList =   (acc,next) -> ap.apBiFn(ap.unit((a,b) -> { a.add(b); return a;}),acc,next);

                BinaryOperator<Higher<C2,ListX<T>>> combineLists = (a,b)-> ap.apBiFn(ap.unit((l1,l2)-> { l1.addAll(l2); return l1;}),a,b); ;

                return list.stream()
                        .reduce(identity,
                                combineToList,
                                combineLists);


            };
            BiFunction<Applicative<C2>,Higher<ListX.µ,Higher<C2, T>>,Higher<C2, Higher<ListX.µ,T>>> sequenceNarrow  =
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
        public static <T> Foldable<µ> foldable(){
            BiFunction<Monoid<T>,Higher<ListX.µ,T>,T> foldRightFn =  (m,l)-> ListX.fromIterable(narrow(l)).foldRight(m);
            BiFunction<Monoid<T>,Higher<ListX.µ,T>,T> foldLeftFn = (m,l)-> ListX.fromIterable(narrow(l)).reduce(m);
            return General.foldable(foldRightFn, foldLeftFn);
        }

        private static  <T> ListX<T> concat(List<T> l1, List<T> l2){
            return ListX.fromStreamS(Stream.concat(l1.stream(),l2.stream()));
        }
        private static <T> ListX<T> of(T value){
            return ListX.of(value);
        }
        private static <T,R> ListX<R> ap(ListX<Function< T, R>> lt,  ListX<T> list){
            return ListX.fromIterable(lt).zip(list,(a,b)->a.apply(b));
        }
        private static <T,R> Higher<ListX.µ,R> flatMap( Higher<ListX.µ,T> lt, Function<? super T, ? extends  Higher<ListX.µ,R>> fn){
            return ListX.fromIterable(Instances.narrowK(lt)).flatMap(fn.andThen(Instances::narrowK));
        }
        private static <T,R> ListX<R> map(ListX<T> lt, Function<? super T, ? extends R> fn){
            return ListX.fromIterable(lt).map(fn);
        }



        /**
         * Widen a ListType nested inside another HKT encoded type
         *
         * @param flux HTK encoded type containing  a List to widen
         * @return HKT encoded type with a widened List
         */
        public static <C2, T> Higher<C2, Higher<ListX.µ, T>> widen2(Higher<C2, ListX<T>> flux) {
            // a functor could be used (if C2 is a functor / one exists for C2 type)
            // instead of casting
            // cast seems safer as Higher<ListX.µ,T> must be a ListX
            return (Higher) flux;
        }



        /**
         * Convert the raw Higher Kinded Type for ListType types into the ListType type definition class
         *
         * @param future HKT encoded list into a ListType
         * @return ListType
         */
        public static <T> ListX<T> narrowK(final Higher<ListX.µ, T> future) {
            return (ListX<T>) future;
        }

        /**
         * Convert the HigherKindedType definition for a List into
         *
         * @param List Type Constructor to convert back into narrowed type
         * @return List from Higher Kinded Type
         */
        public static <T> ListX<T> narrow(final Higher<ListX.µ, T> completableList) {

            return ((ListX<T>) completableList);//.narrow();

        }
    }

    default <W extends WitnessType<W>> ListT<W, T> liftM(W witness) {
        return ListT.of(witness.adapter().unit(this));
    }

    default AnyMSeq<list,T> anyM(){
        return AnyM.fromList(this);
    }
   
    public static <T> ListX<T> fromStreamS(Stream<T> s){
        return ReactiveSeq.fromStream(s).toListX();
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
        return ReactiveSeq.range(start, end)
                          .toListX();
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
        return ReactiveSeq.rangeLong(start, end)
                          .toListX();
    }

    /**
     * Unfold a function into a ListX
     * 
     * <pre>
     * {@code 
     *  ListX.unfold(1,i->i<=6 ? Optional.of(Tuple.tuple(i,i+1)) : Optional.empty());
     * 
     * //(1,2,3,4,5)
     * 
     * }</pre>
     * 
     * @param seed Initial value 
     * @param unfolder Iteratively applied function, terminated by an empty Optional
     * @return ListX generated by unfolder function
     */
    static <U, T> ListX<T> unfold(final U seed, final Function<? super U, Optional<Tuple2<T, U>>> unfolder) {
        return ReactiveSeq.unfold(seed, unfolder)
                          .toListX();
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
                          .toListX();
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
                          .toListX();
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
                          .toListX();

    }
    @Override
    default ListX<T> materialize() {
        return (ListX<T>)MutableCollectionX.super.materialize();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> ListX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Fn3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
            Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return (ListX)MutableCollectionX.super.forEach4(stream1, stream2, stream3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> ListX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Fn3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
            Fn4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
            Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return (ListX)MutableCollectionX.super.forEach4(stream1, stream2, stream3, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> ListX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        
        return (ListX)MutableCollectionX.super.forEach3(stream1, stream2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> ListX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Fn3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
            Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        
        return (ListX)MutableCollectionX.super.forEach3(stream1, stream2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> ListX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        
        return (ListX)MutableCollectionX.super.forEach2(stream1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> ListX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, Boolean> filterFunction,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        
        return (ListX)MutableCollectionX.super.forEach2(stream1, filterFunction, yieldingFunction);
    }
    
    
    /* (non-Javadoc)
     * @see com.aol.cyclops2.sequence.traits.ConvertableSequence#toListX()
     */
    @Override
    default ListX<T> toListX() {
        return this;
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
     * @return Construct an empty ListX
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

        final List<T> res = (List<T>) defaultCollector().supplier()
                .get();
        for (final T v : values)
            res.add(v);

        return fromIterable(res);
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
        return Spouts.from((Publisher<T>) publisher)
                          .toListX();
    }

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

        return (ListX<T>) MutableCollectionX.super.limit(num);
    }
    @Override
    default ListX<T> drop(final long num) {

        return (ListX<T>) MutableCollectionX.super.skip(num);
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
    

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.FluentCollectionX#unit(java.util.Collection)
     */
    @Override
    default <R> ListX<R> unit(final Collection<R> col) {
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



    /* (non-Javadoc)
     * @see java.util.Collection#reactiveStream()
     */
    @Override
    default ReactiveSeq<T> stream() {

        return ReactiveSeq.fromIterable(this);
    }

    /**
     * @return A Collector to generate a List
     */
    public <T> Collector<T, ?, List<T>> getCollector();

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#from(java.util.Collection)
     */
    @Override
    default <T1> ListX<T1> from(final Collection<T1> c) {
        return ListX.<T1> fromIterable(getCollector(), c);
    }

    @Override
    default <X> ListX<X> fromStream(final Stream<X> stream) {
        return new LazyListX<>(null,ReactiveSeq.fromStream(stream), getCollector());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#reverse()
     */
    @Override
    default ListX<T> reverse() {

        return (ListX<T>) MutableCollectionX.super.reverse();
    }

    /**
     * Combine two adjacent elements in a ListX using the supplied BinaryOperator
     * This is a stateful grouping and reduction operation. The emitted of a combination may in turn be combined
     * with it's neighbor
     * <pre>
     * {@code 
     *  ListX.of(1,1,2,3)
                   .combine((a, b)->a.equals(b),Semigroups.intSum)
                   .toListX()
                   
     *  //ListX(3,4) 
     * }</pre>
     * 
     * @param predicate Test to see if two neighbors should be joined
     * @param op Reducer to combine neighbors
     * @return Combined / Partially Reduced ListX
     */
    @Override
    default ListX<T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {
        return (ListX<T>) MutableCollectionX.super.combine(predicate, op);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#filter(java.util.function.Predicate)
     */
    @Override
    default ListX<T> filter(final Predicate<? super T> pred) {

        return (ListX<T>) MutableCollectionX.super.filter(pred);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#map(java.util.function.Function)
     */
    @Override
    default <R> ListX<R> map(final Function<? super T, ? extends R> mapper) {

        return (ListX<R>) MutableCollectionX.super.<R> map(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#flatMap(java.util.function.Function)
     */
    @Override
    default <R> ListX<R> flatMap(final Function<? super T, ? extends Iterable<? extends R>> mapper) {

        return (ListX<R>) MutableCollectionX.super.<R> flatMap(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#limit(long)
     */
    @Override
    default ListX<T> limit(final long num) {

        return (ListX<T>) MutableCollectionX.super.limit(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#skip(long)
     */
    @Override
    default ListX<T> skip(final long num) {

        return (ListX<T>) MutableCollectionX.super.skip(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.MutableCollectionX#takeRight(int)
     */
    @Override
    default ListX<T> takeRight(final int num) {
        return (ListX<T>) MutableCollectionX.super.takeRight(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.MutableCollectionX#dropRight(int)
     */
    @Override
    default ListX<T> dropRight(final int num) {
        return (ListX<T>) MutableCollectionX.super.dropRight(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#takeWhile(java.util.function.Predicate)
     */
    @Override
    default ListX<T> takeWhile(final Predicate<? super T> p) {

        return (ListX<T>) MutableCollectionX.super.takeWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#dropWhile(java.util.function.Predicate)
     */
    @Override
    default ListX<T> dropWhile(final Predicate<? super T> p) {

        return (ListX<T>) MutableCollectionX.super.dropWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#takeUntil(java.util.function.Predicate)
     */
    @Override
    default ListX<T> takeUntil(final Predicate<? super T> p) {

        return (ListX<T>) MutableCollectionX.super.takeUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#dropUntil(java.util.function.Predicate)
     */
    @Override
    default ListX<T> dropUntil(final Predicate<? super T> p) {
        return (ListX<T>) MutableCollectionX.super.dropUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#trampoline(java.util.function.Function)
     */
    @Override
    default <R> ListX<R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (ListX<R>) MutableCollectionX.super.<R> trampoline(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#slice(long, long)
     */
    @Override
    default ListX<T> slice(final long from, final long to) {
        return (ListX<T>) MutableCollectionX.super.slice(from, to);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#sorted(java.util.function.Function)
     */
    @Override
    default <U extends Comparable<? super U>> ListX<T> sorted(final Function<? super T, ? extends U> function) {

        return (ListX<T>) MutableCollectionX.super.sorted(function);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.MutableCollectionX#grouped(int)
     */
    @Override
    default ListX<ListX<T>> grouped(final int groupSize) {
        return (ListX<ListX<T>>) MutableCollectionX.super.grouped(groupSize);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.MutableCollectionX#grouped(java.util.function.Function, java.util.reactiveStream.Collector)
     */
    @Override
    default <K, A, D> ListX<Tuple2<K, D>> grouped(final Function<? super T, ? extends K> classifier, final Collector<? super T, A, D> downstream) {
        return (ListX) MutableCollectionX.super.grouped(classifier, downstream);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.MutableCollectionX#grouped(java.util.function.Function)
     */
    @Override
    default <K> ListX<Tuple2<K, ReactiveSeq<T>>> grouped(final Function<? super T, ? extends K> classifier) {
        return (ListX) MutableCollectionX.super.grouped(classifier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.MutableCollectionX#zip(java.lang.Iterable)
     */
    @Override
    default <U> ListX<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return (ListX) MutableCollectionX.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> ListX<R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (ListX<R>) MutableCollectionX.super.zip(other, zipper);
    }


    @Override
    default <U, R> ListX<R> zipS(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (ListX<R>) MutableCollectionX.super.zipS(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.MutableCollectionX#sliding(int)
     */
    @Override
    default ListX<PVectorX<T>> sliding(final int windowSize) {
        return (ListX<PVectorX<T>>) MutableCollectionX.super.sliding(windowSize);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.MutableCollectionX#sliding(int, int)
     */
    @Override
    default ListX<PVectorX<T>> sliding(final int windowSize, final int increment) {
        return (ListX<PVectorX<T>>) MutableCollectionX.super.sliding(windowSize, increment);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.MutableCollectionX#scanLeft(cyclops2.function.Monoid)
     */
    @Override
    default ListX<T> scanLeft(final Monoid<T> monoid) {
        return (ListX<T>) MutableCollectionX.super.scanLeft(monoid);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.MutableCollectionX#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    default <U> ListX<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {
        return (ListX<U>) MutableCollectionX.super.scanLeft(seed, function);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.MutableCollectionX#scanRight(cyclops2.function.Monoid)
     */
    @Override
    default ListX<T> scanRight(final Monoid<T> monoid) {
        return (ListX<T>) MutableCollectionX.super.scanRight(monoid);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.MutableCollectionX#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    default <U> ListX<U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {
        return (ListX<U>) MutableCollectionX.super.scanRight(identity, combiner);
    }

    /* Makes a defensive copy of this ListX replacing the value at i with the specified element
     *  (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableSequenceX#with(int, java.lang.Object)
     */
    @Override
    default ListX<T> with(final int i, final T element) {
        return from(stream().deleteBetween(i, i + 1)
                            .insertAt(i, element)
                            .collect(getCollector()));
    }

    /* (non-Javadoc)
     * @see java.util.List#subList(int, int)
     */
    @Override
    public ListX<T> subList(int start, int end);

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.MutableCollectionX#plus(java.lang.Object)
     */
    @Override
    default ListX<T> plus(final T e) {
        add(e);
        return this;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.MutableCollectionX#plusAll(java.util.Collection)
     */
    @Override
    default ListX<T> plusAll(final Collection<? extends T> list) {
        addAll(list);
        return this;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.MutableSequenceX#minus(int)
     */
    @Override
    default ListX<T> minus(final int pos) {
        remove(pos);
        return this;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.MutableCollectionX#minus(java.lang.Object)
     */
    @Override
    default ListX<T> minus(final Object e) {
        remove(e);
        return this;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.MutableCollectionX#minusAll(java.util.Collection)
     */
    @Override
    default ListX<T> minusAll(final Collection<?> list) {
        removeAll(list);
        return this;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.MutableSequenceX#plus(int, java.lang.Object)
     */
    @Override
    default ListX<T> plus(final int i, final T e) {
        add(i, e);
        return this;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.MutableSequenceX#plusAll(int, java.util.Collection)
     */
    @Override
    default ListX<T> plusAll(final int i, final Collection<? extends T> list) {
        addAll(i, list);
        return this;
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

        return (ListX<T>) MutableCollectionX.super.peek(c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#cycle(int)
     */
    @Override
    default ListX<T> cycle(final long times) {

        return (ListX<T>) MutableCollectionX.super.cycle(times);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#cycle(com.aol.cyclops2.sequence.Monoid, int)
     */
    @Override
    default ListX<T> cycle(final Monoid<T> m, final long times) {

        return (ListX<T>) MutableCollectionX.super.cycle(m, times);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#cycleWhile(java.util.function.Predicate)
     */
    @Override
    default ListX<T> cycleWhile(final Predicate<? super T> predicate) {

        return (ListX<T>) MutableCollectionX.super.cycleWhile(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#cycleUntil(java.util.function.Predicate)
     */
    @Override
    default ListX<T> cycleUntil(final Predicate<? super T> predicate) {
        return (ListX<T>) MutableCollectionX.super.cycleUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#zip(java.util.reactiveStream.Stream)
     */
    @Override
    default <U> ListX<Tuple2<T, U>> zipS(final Stream<? extends U> other) {

        return (ListX) MutableCollectionX.super.zipS(other);
    }


    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#zip3(java.util.reactiveStream.Stream, java.util.reactiveStream.Stream)
     */
    @Override
    default <S, U> ListX<Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {

        return (ListX) MutableCollectionX.super.zip3(second, third);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#zip4(java.util.reactiveStream.Stream, java.util.reactiveStream.Stream, java.util.reactiveStream.Stream)
     */
    @Override
    default <T2, T3, T4> ListX<Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
            final Iterable<? extends T4> fourth) {

        return (ListX) MutableCollectionX.super.zip4(second, third, fourth);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#zipWithIndex()
     */
    @Override
    default ListX<Tuple2<T, Long>> zipWithIndex() {

        return (ListX<Tuple2<T, Long>>) MutableCollectionX.super.zipWithIndex();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#sorted()
     */
    @Override
    default ListX<T> sorted() {

        return (ListX<T>) MutableCollectionX.super.sorted();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#sorted(java.util.Comparator)
     */
    @Override
    default ListX<T> sorted(final Comparator<? super T> c) {

        return (ListX<T>) MutableCollectionX.super.sorted(c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#skipWhile(java.util.function.Predicate)
     */
    @Override
    default ListX<T> skipWhile(final Predicate<? super T> p) {

        return (ListX<T>) MutableCollectionX.super.skipWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#skipUntil(java.util.function.Predicate)
     */
    @Override
    default ListX<T> skipUntil(final Predicate<? super T> p) {

        return (ListX<T>) MutableCollectionX.super.skipUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#shuffle()
     */
    @Override
    default ListX<T> shuffle() {

        return (ListX<T>) MutableCollectionX.super.shuffle();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#skipLast(int)
     */
    @Override
    default ListX<T> skipLast(final int num) {

        return (ListX<T>) MutableCollectionX.super.skipLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#shuffle(java.util.Random)
     */
    @Override
    default ListX<T> shuffle(final Random random) {

        return (ListX<T>) MutableCollectionX.super.shuffle(random);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#permutations()
     */
    @Override
    default ListX<ReactiveSeq<T>> permutations() {

        return (ListX<ReactiveSeq<T>>) MutableCollectionX.super.permutations();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#combinations(int)
     */
    @Override
    default ListX<ReactiveSeq<T>> combinations(final int size) {

        return (ListX<ReactiveSeq<T>>) MutableCollectionX.super.combinations(size);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#combinations()
     */
    @Override
    default ListX<ReactiveSeq<T>> combinations() {

        return (ListX<ReactiveSeq<T>>) MutableCollectionX.super.combinations();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Transformable#cast(java.lang.Class)
     */
    @Override
    default <U> ListX<U> cast(final Class<? extends U> type) {

        return (ListX<U>) MutableCollectionX.super.cast(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#distinct()
     */
    @Override
    default ListX<T> distinct() {

        return (ListX<T>) MutableCollectionX.super.distinct();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#limitWhile(java.util.function.Predicate)
     */
    @Override
    default ListX<T> limitWhile(final Predicate<? super T> p) {

        return (ListX<T>) MutableCollectionX.super.limitWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#limitUntil(java.util.function.Predicate)
     */
    @Override
    default ListX<T> limitUntil(final Predicate<? super T> p) {

        return (ListX<T>) MutableCollectionX.super.limitUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#intersperse(java.lang.Object)
     */
    @Override
    default ListX<T> intersperse(final T value) {

        return (ListX<T>) MutableCollectionX.super.intersperse(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#limitLast(int)
     */
    @Override
    default ListX<T> limitLast(final int num) {

        return (ListX<T>) MutableCollectionX.super.limitLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#onEmpty(java.lang.Object)
     */
    @Override
    default ListX<T> onEmpty(final T value) {

        return (ListX<T>) MutableCollectionX.super.onEmpty(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default ListX<T> onEmptyGet(final Supplier<? extends T> supplier) {

        return (ListX<T>) MutableCollectionX.super.onEmptyGet(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    default <X extends Throwable> ListX<T> onEmptyThrow(final Supplier<? extends X> supplier) {

        return (ListX<T>) MutableCollectionX.super.onEmptyThrow(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#ofType(java.lang.Class)
     */
    @Override
    default <U> ListX<U> ofType(final Class<? extends U> type) {

        return (ListX<U>) MutableCollectionX.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#filterNot(java.util.function.Predicate)
     */
    @Override
    default ListX<T> filterNot(final Predicate<? super T> fn) {

        return (ListX<T>) MutableCollectionX.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#notNull()
     */
    @Override
    default ListX<T> notNull() {

        return (ListX<T>) MutableCollectionX.super.notNull();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#removeAllS(java.util.reactiveStream.Stream)
     */
    @Override
    default ListX<T> removeAllS(final Stream<? extends T> stream) {

        return (ListX<T>) MutableCollectionX.super.removeAllS(stream);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#removeAllS(java.lang.Iterable)
     */
    @Override
    default ListX<T> removeAllS(final Iterable<? extends T> it) {

        return (ListX<T>) MutableCollectionX.super.removeAllS(it);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#removeAllS(java.lang.Object[])
     */
    @Override
    default ListX<T> removeAllS(final T... values) {

        return (ListX<T>) MutableCollectionX.super.removeAllS(values);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#retainAllS(java.lang.Iterable)
     */
    @Override
    default ListX<T> retainAllS(final Iterable<? extends T> it) {

        return (ListX<T>) MutableCollectionX.super.retainAllS(it);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#retainAllS(java.util.reactiveStream.Stream)
     */
    @Override
    default ListX<T> retainAllS(final Stream<? extends T> seq) {

        return (ListX<T>) MutableCollectionX.super.retainAllS(seq);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#retainAllS(java.lang.Object[])
     */
    @Override
    default ListX<T> retainAllS(final T... values) {

        return (ListX<T>) MutableCollectionX.super.retainAllS(values);
    }

    /* (non-Javadoc)
    * @see com.aol.cyclops2.data.collections.extensions.standard.MutableCollectionX#grouped(int, java.util.function.Supplier)
    */
    @Override
    default <C extends Collection<? super T>> ListX<C> grouped(final int size, final Supplier<C> supplier) {

        return (ListX<C>) MutableCollectionX.super.grouped(size, supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.MutableCollectionX#groupedUntil(java.util.function.Predicate)
     */
    @Override
    default ListX<ListX<T>> groupedUntil(final Predicate<? super T> predicate) {

        return (ListX<ListX<T>>) MutableCollectionX.super.groupedUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.MutableCollectionX#groupedWhile(java.util.function.Predicate)
     */
    @Override
    default ListX<ListX<T>> groupedWhile(final Predicate<? super T> predicate) {

        return (ListX<ListX<T>>) MutableCollectionX.super.groupedWhile(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.MutableCollectionX#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> ListX<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (ListX<C>) MutableCollectionX.super.groupedWhile(predicate, factory);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.MutableCollectionX#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> ListX<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (ListX<C>) MutableCollectionX.super.groupedUntil(predicate, factory);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.standard.MutableCollectionX#groupedStatefullyUntil(java.util.function.BiPredicate)
     */
    @Override
    default ListX<ListX<T>> groupedStatefullyUntil(final BiPredicate<ListX<? super T>, ? super T> predicate) {

        return (ListX<ListX<T>>) MutableCollectionX.super.groupedStatefullyUntil(predicate);
    }





    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.OnEmptySwitch#onEmptySwitch(java.util.function.Supplier)
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
        return (ListX<R>)MutableCollectionX.super.retry(fn);
    }

    @Override
    default <R> ListX<R> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (ListX<R>)MutableCollectionX.super.retry(fn);
    }

    @Override
    default <R> ListX<R> flatMapS(Function<? super T, ? extends Stream<? extends R>> fn) {
        return (ListX<R>)MutableCollectionX.super.flatMapS(fn);
    }

    @Override
    default <R> ListX<R> flatMapP(Function<? super T, ? extends Publisher<? extends R>> fn) {
        return (ListX<R>)MutableCollectionX.super.flatMapP(fn);
    }

    @Override
    default ListX<T> prependS(Stream<? extends T> stream) {
        return (ListX<T>)MutableCollectionX.super.prependS(stream);
    }

    @Override
    default ListX<T> append(T... values) {
        return (ListX<T>)MutableCollectionX.super.append(values);
    }

    @Override
    default ListX<T> append(T value) {
        return (ListX<T>)MutableCollectionX.super.append(value);
    }

    @Override
    default ListX<T> prepend(T value) {
        return (ListX<T>)MutableCollectionX.super.prepend(value);
    }

    @Override
    default ListX<T> prepend(T... values) {
        return (ListX<T>)MutableCollectionX.super.prepend(values);
    }

    @Override
    default ListX<T> insertAt(int pos, T... values) {
        return (ListX<T>)MutableCollectionX.super.insertAt(pos,values);
    }

    @Override
    default ListX<T> deleteBetween(int start, int end) {
        return (ListX<T>)MutableCollectionX.super.deleteBetween(start,end);
    }

    @Override
    default ListX<T> insertAtS(int pos, Stream<T> stream) {
        return (ListX<T>)MutableCollectionX.super.insertAtS(pos,stream);
    }

    @Override
    default ListX<T> recover(final Function<? super Throwable, ? extends T> fn) {
        return (ListX<T>)MutableCollectionX.super.recover(fn);
    }

    @Override
    default <EX extends Throwable> ListX<T> recover(Class<EX> exceptionClass, final Function<? super EX, ? extends T> fn) {
        return (ListX<T>)MutableCollectionX.super.recover(exceptionClass,fn);
    }

    @Override
    default ListX<T> plusLoop(int max, IntFunction<T> value) {
        return (ListX<T>)MutableCollectionX.super.plusLoop(max,value);
    }

    @Override
    default ListX<T> plusLoop(Supplier<Optional<T>> supplier) {
        return (ListX<T>)MutableCollectionX.super.plusLoop(supplier);
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

}
