package cyclops.function;

import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.util.SimpleTimer;
import cyclops.control.Maybe;
import com.oath.cyclops.hkt.DataWitness.predicate;
import cyclops.reactive.ReactiveSeq;
import cyclops.collections.mutable.ListX;
import com.oath.cyclops.types.Value;
import cyclops.typeclasses.functor.ContravariantFunctor;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 *
 * Predicates for filtering and Pattern matching
 *
 * e.g.
 * <pre>
 * {@code
 *  import static cyclops2.function.Predicates.greaterThan;
 *
 *  Stream.of(1,2,3,100,200,300)
 *        .filter(greaterThan(10));
 *
 * //Stream[100,200,300]
 * }
 * </pre>
 *
 *
 * @author johnmcclean
 *
 */
public class Predicates {
    public static <T1,T2> BiPredicate<T1,T2> when(BiPredicate<T1,T2> pred){
        return pred;
    }
    public static <T1,T2> BiPredicate<T1,T2> true2(){
        return (a,b)->true;
    }
    public static <T1,T2> BiPredicate<T1,T2> and(Predicate<? super T1> p1, Predicate<? super T2> p2){
        return (a,b)->p1.test(a) && p2.test(b);
    }
    public static <T1,T2> BiPredicate<T1,T2> _1(Predicate<? super T1> p1){
        return (a,b)->p1.test(a);
    }

    public static <T1,T2> BiPredicate<T1,T2> first(Predicate<? super T1> p1, Predicate<? super T2> p2){
        return (a,b)->p1.test(a) && !p2.test(b);
    }
    public static <T1,T2> BiPredicate<T1,T2> second(Predicate<? super T1> p1, Predicate<? super T2> p2){
        return (a,b)->!p1.test(a) && p2.test(b);
    }
    public static <T1,T2> BiPredicate<T1,T2> _2(Predicate<? super T2> p2){
        return (a,b)->p2.test(b);
    }
    public static <T1,T2> BiPredicate<T1,T2> or(Predicate<? super T1> p1, Predicate<? super T2> p2){
        return (a,b)->p1.test(a) || p2.test(b);
    }
    public static <T1,T2> BiPredicate<T1,T2> xor(Predicate<? super T1> p1, Predicate<? super T2> p2){
        return (a,b)->p1.test(a) ^ p2.test(b);
    }

    /**
     * Method for point-free Predicate definition (helps with lambda type inferencing)
     *
     * e.g.
     * <pre>
     * {@code
     *    Predicate<Integer> pred = i->i>10;
     *
     *    Predicates.<Integer>p(i->i>10).and(not(p(i<100));
     * }
     * </pre>
     *
     * @param p Supplied predicate (normally as a lambda expression)
     * @return Predicate
     */
    public static <T> Predicate<T> p(final Predicate<T> p) {
        return p;
    }

    /**
     *
     * <pre>
     * {@code
     *      import static cyclops2.function.Predicates.optionalPresent;
     *
     *      ListX.of(Optional.ofNullable(null),Optional.of(1),null)
     *            .filter(optionalPresent());
     *
     *       //ListX[Optional[1]]
     * }
     * </pre>
     *
     * @return A Predicate that checks if it's input is an Optional with a value
     */

    public static <T> Predicate<T> optionalPresent() {
        return t -> t instanceof Optional ? ((Optional) t).isPresent() : false;
    }

    /**
     * <pre>
     * {@code
     *      import static cyclops2.function.Predicates.valuePresent;
     *
     *      ListX.of(Maybe.ofNullable(null),Maybe.just(1),null)
     *            .filter(valuePresent());
     *
     *       //ListX[Maybe[1]]
     * }
     * </pre>
     *  @return A Predicate that checks if it's input is a cyclops2-react Value (which also contains a present value)
     */
    public static <T> Predicate<T> valuePresent() {
        return t -> t instanceof Value ? ((Value) t).toMaybe()
                                                    .isPresent()
                : false;
    }

    /**
     * <pre>
     * {@code
     *      import static cyclops2.function.Predicates.valuePresent;
     *
     *      ListX.of(Arrays.asList(),Arrays.asList(1),null)
     *            .filter(iterablePresent());
     *
     *       //ListX[List[1]]
     * }
     * </pre>
     * @return A Predicate that checks if it's input is an Iterable with at least one value
     */
    public static <T> Predicate<T> iterablePresent() {
        return t -> t instanceof Iterable ? ((Iterable) t).iterator()
                                                          .hasNext()
                : false;
    }

    /**
     *
     * <pre>
     * {@code
     *      import static cyclops2.function.Predicates.some;
     *
     *      ListX.of(Arrays.asList(),Arrays.asList(1),null, Optional.zero(),Maybe.none())
     *            .filter(some());
     *
     *       //ListX[List[1]]
     * }
     * </pre>
     *
     * @return A predicate that checks for a values presence (i.e. for standard values that they are non-null,
     *  for Optionals that they are present, for cyclops2-react values that they are present and for Iterables that they are
     *  non-null).
     */
    public static <T> Predicate<T> some() {
        return Predicates.<T> p(t -> t != null)
                         .and(not(optionalPresent()))
                         .and(not(valuePresent()))
                         .and(not(iterablePresent()));
    }

    /**
     * Alias for eq (results in nicer pattern matching dsl).
     * Returns a Predicate that checks for equality between the supplied value and the predicates input parameter
     *
     * <pre>
     * {@code
     *
     * import static cyclops2.function.Predicates.some

     *
     * Eval<Integer> result = Xors.future(Future.ofResult(1))
                                         .matches(c-> c.is( when(some(1)), applyHKT(10)), c->c.is(when(instanceOf(RuntimeException.class)), applyHKT(2)),otherwise(3));

       //Eval[10]
     *
     *
     * }
     * </pre>
     *
     *
     * @param value Value to check for equality
     * @return Predicate that checks for equality with the supplied value
     */
    public static <T> Predicate<T> some(final T value) {
        return eq(value);
    }

    /**
     * wildcard predicate
     *
     * <pre>
     * {@code
     *  import static cyclops2.function.Predicates.__
     *
     *  Eval<String> result = Xors.listOfValues(1,new MyCase(4,5,6))
                                .matches(c->c.is(when(__,Predicates.has(4,5,6)),transform("rec")),otherwise("n/a"));

        //Eval["rec"]
     *
     * }
     * </pr>
     *
     *
     */
    public static final Predicate __ = test -> true;

    /**
     * @see Predicates#__
     * @return A Wildcard predicate, always returns true
     *
     */
    public static final <Y> Predicate<Y> any() {
        return __;
    };
    public static final <Y> Predicate<Y> none() {
        return t->false;
    };

    /**
     * MatchType against any object that is an instance of supplied type
     *
     * <pre>
     * {@code
     *  import static com.oath.cyclops.control.Matchable.whenGuard;
     *  import static com.oath.cyclops.control.Matchable.otherwise;
        import static com.oath.cyclops.control.Matchable.transform;
     *  import static cyclops2.function.Predicates.eq;
     *  import static cyclops2.function.Predicates.any;
     *
     *  Matchable.of(Arrays.asList(1,2,3))
                    .matches(c->c.is(whenGuard(eq(1),any(Integer.class),eq(4)),transform("2")),otherwise("45"));
     *
     * }
     * </pre>
     *
     *
     * @param c Class type to fold against
     * @return Predicate that mathes against type
     */
    public static final <Y> Predicate<Y> any(final Class<Y> c) {
        return a -> a.getClass()
                     .isAssignableFrom(c);
    };




    /**
     * Check for universal equality (Object#equals)
     *
     * Filtering example
     * <pre>
     * {@code
     *  ReactiveSeq.of(1,2,3).filter(anyOf(not(eq(2)),in(1,10,20)));
     *  //ReactiveSeq[1]
     *
     * }
     * </pre>
     *
     * Pattern Matching Example
     *
     *  <pre>
     *  {@code
     *   Eval<String> url = Xors.url(new URL("http://www.aol.com/path?q=hello"))
                                     .on$12_45()
                                     .matches(c->c.is(when(eq("http"),in("www.aol.com","aol.com"),any(),not(eq("q=hello!"))), transform("correct")),otherwise("miss"));

        //Eval.now("correct");
     *
     *
     *  }
     *  </pre>
     *
     * @param value Value to check equality of
     * @return Predicate for equality
     */
    public static <V> Predicate<V> eq(final V value) {

        return test -> Objects.equals(test, value);
    }
    public static <T1> Predicate<T1> not(final Predicate<T1> p) {
        return p.negate();
    }


    /**
     *  Test for equivalence
     *  null eqv to absent, embedded value equivalency, non-values converted to values before testing
     *.
     * <pre>
     * {@code
     *
     *   Stream.of(Maybe.of(2))
     *         .filter(eqv(Maybe.of(2)))
     *         .forEach(System.out::println);
     *
     *   //Maybe[2]
     *
     *   Stream.of(2)
     *         .filter(eqv(Maybe.of(2)))
     *         .forEach(System.out::println);
     *
     *   //2      (passes filter as equivalent to Maybe[2])
     * }</pre>
     *
     * @param value
     * @return
     */
    public static <V> Predicate<Value<? super V>> eqv(final Value<? super V> value) {

        return test -> test == null ? value == null ? true : !value.toMaybe()
                                                                   .isPresent()
                : test.toMaybe()
                      .equals(value.toMaybe());

    }

    public static <V> Predicate<Iterable<? super V>> eqvIterable(final Iterable<? super V> iterable) {

        return test -> test == null ? iterable == null ? true : ListX.fromIterable(iterable)
                                                                     .isEmpty()
                : ListX.fromIterable(test)
                       .equals(ListX.fromIterable(iterable));

    }

    public static <V> Predicate<V> eqv2(final Value<? super V> value) {

        return test -> test == null ? value == null ? true : !value.toMaybe()
                                                                   .isPresent()
                : Maybe.ofNullable(test)
                       .equals(value.toMaybe());

    }


    public static <T1> Predicate<T1> and(Predicate<? super T1>... preds)
    {
        Predicate<T1> current=  (Predicate<T1>)preds[0];
        for(int i=1;i<preds.length;i++){
            current = current.and((Predicate<T1>)preds[i]);
        }
        return current;
    }
    public static <T1> Predicate<T1> or(Predicate<? super T1>... preds)
    {
        Predicate<T1> current =  (Predicate<T1>)preds[0];
        for(int i=1;i<preds.length;i++){
            current = current.or((Predicate<T1>)preds[i]);
        }
        return current;
    }


    @SafeVarargs
    public static <T1> Predicate<T1> in(final T1... values){

    return test -> Arrays.asList(values)
                         .contains(test);
}

    public static <T1 extends Comparable<T1>> Predicate<? super T1> greaterThan(final T1 v) {
        return test -> test.compareTo(v) > 0;
    }

    public static <T1 extends Comparable<T1>> Predicate<? super T1> greaterThanOrEquals(final T1 v) {
        return test -> test.compareTo(v) >= 0;
    }

    public static <T1 extends Comparable<T1>> Predicate<? super T1> lessThan(final T1 v) {
        return test -> test.compareTo(v) < 0;
    }

    public static <T1 extends Comparable<T1>> Predicate<? super T1> lessThanOrEquals(final T1 v) {
        return test -> test.compareTo(v) <= 0;
    }
    public static <T1 extends Comparable<T1>> Predicate<? super T1> equal(final T1 v) {
        return test -> test.compareTo(v) == 0;
    }
    @Deprecated //collides with Object#equals use equal instead
    public static <T1 extends Comparable<T1>> Predicate<? super T1> equals(final T1 v) {
        return equal(v);
    }

    public static <T1> Predicate<? super T1> nullValue() {
        return test -> test == null;
    }

    public static <T1> Predicate<Collection<? super T1>> hasItems(final Collection<T1> items) {
        return test -> ReactiveSeq.fromIterable(items)
                                  .map(i -> test.contains(i))
                                  .allMatch(v -> v);
    }

    public static <T1> Predicate<Collection<? super T1>> hasItems(final Stream<T1> items) {
        return test -> items.map(i -> test.contains(i))
                            .allMatch(v -> v);
    }

    @SafeVarargs
    public static <T1> Predicate<Collection<? super T1>> hasItems(final T1... items){
    return test -> ReactiveSeq.of(items)
                              .map(i -> test.contains(i))
                              .allMatch(v -> v);
}

@SafeVarargs
public static <T1> Predicate<Iterable<? super T1>> startsWith(final T1... items) {
    return test -> ReactiveSeq.fromIterable(test)
                              .startsWithIterable(ReactiveSeq.of(items));

}

@SafeVarargs
public static <T1> Predicate<Iterable<? super T1>> endsWith(final T1... items) {

    return test -> ReactiveSeq.fromIterable(test)
                              .endsWithIterable(ReactiveSeq.of(items));

}

public static <T1> Predicate<? super T1> instanceOf(final Class<?> clazz) {

        return test -> clazz.isAssignableFrom(test.getClass());
    }

    @SafeVarargs
    public static <T1> Predicate<? super T1> allOf(final Predicate<? super T1>... preds) {
        Predicate<T1> current=  (Predicate<T1>)preds[0];
        for(int i=1;i<preds.length;i++){
            current = current.and((Predicate<T1>)preds[i]);
        }
        return current;
    }

    @SafeVarargs
    public static <T1> Predicate<? super T1> anyOf(final Predicate<? super T1>... preds) {
        Predicate<T1> current =  (Predicate<T1>)preds[0];
        for(int i=1;i<preds.length;i++){
            current = current.or((Predicate<T1>)preds[i]);
        }
        return current;
    }

    @SafeVarargs
    public static <T1> Predicate<? super T1> noneOf(final Predicate<? super T1>... preds) {
        return allOf(preds).negate();
    }

    @SafeVarargs
    public static <T1> Predicate<? super T1> xOf(final int x, final Predicate<? super T1>... preds) {
        return test -> ReactiveSeq.of(preds)
                                  .map(t -> t.test(test))
                                  .xMatch(x, r -> r);
    }

    /**
     * Samples a dataset by only returning true when the modulus of the event count divided by the rate is 0
     * e.g.
     * To select every second member of a Stream, filter with Predicates.sample(2) - for every thid member filter with Predicates.sample(3) and so on.
     * To take 1% of a Stream use Predicates.sample(100)
     *
     * @param rate Every x element to include in your sample
     * @param <T> Data type to sample
     * @return Sampling Predicate
     */
    public static <T> Predicate<T> sample(int rate){
        return new Sample<T>(rate);
    }
    public static <T> Predicate<T> sample(long time,TimeUnit unit){
        return new TimeSample<T>(unit.toNanos(time));
    }
    private static class TimeSample<T> implements Predicate<T>{
        private final AtomicReference<SimpleTimer> timer = new AtomicReference(null);
        private final  long nanos;


        public TimeSample(long nanos) {
            this.nanos = nanos;
        }

        @Override
        public boolean test(T t) {
            if(timer.get()==null){
                timer.set(new SimpleTimer());
                return true;
            }
            if(timer.get().getElapsedNanoseconds() > nanos){
                timer.set(null);
                return true;
            }
            return false;
        }

    }

    private static class Sample<T> implements Predicate<T>{
        private final int rate;

        private AtomicInteger count = new AtomicInteger(0);

        public Sample(int rate) {
            this.rate = rate;
        }

        @Override
        public boolean test(T t) {
            return count.incrementAndGet() % rate ==0;
        }

    }

    static <T1> Predicate<T1> inSet(Set<T1> set) {
        return set::contains;
    }
    static interface PredicateKind<T> extends Higher<predicate,T>,Predicate<T>{

        public static <T> PredicateKind<T> narrow(Higher<predicate,T> ds){
            return (PredicateKind<T>)ds;
        }
    }
    public static class Instances{

        public ContravariantFunctor<predicate> contravariantFunctor(){
            return new ContravariantFunctor<predicate>() {
                @Override
                public <T, R> Higher<predicate, R> contramap(Function<? super R, ? extends T> fn, Higher<predicate, T> ds) {
                    PredicateKind<R> r = in->PredicateKind.narrow(ds).test(fn.apply(in));
                    return r;
                }
            };
        }
    }
}
