package com.aol.cyclops.util.function;

import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.Value;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * 
 * Predicates for filtering and Pattern matching
 * 
 * e.g. 
 * <pre>
 * {@code 
 *  import static com.aol.cyclops.util.function.Predicates.greaterThan;
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
     *      import static com.aol.cyclops.util.function.Predicates.optionalPresent;
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
     *      import static com.aol.cyclops.util.function.Predicates.valuePresent;
     *      
     *      ListX.of(Maybe.ofNullable(null),Maybe.just(1),null)
     *            .filter(valuePresent());
     *            
     *       //ListX[Maybe[1]]      
     * }
     * </pre>     
     *  @return A Predicate that checks if it's input is a cyclops-react Value (which also contains a present value)
     */
    public static <T> Predicate<T> valuePresent() {
        return t -> t instanceof Value ? ((Value) t).toMaybe()
                                                    .isPresent()
                : false;
    }

    /**
     * <pre>
     * {@code 
     *      import static com.aol.cyclops.util.function.Predicates.valuePresent;
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
     *      import static com.aol.cyclops.util.function.Predicates.some;
     *      
     *      ListX.of(Arrays.asList(),Arrays.asList(1),null, Optional.empty(),Maybe.none())
     *            .filter(some());
     *            
     *       //ListX[List[1]]      
     * }
     * </pre>   
     * 
     * @return A predicate that checks for a values presence (i.e. for standard values that they are non-null, 
     *  for Optionals that they are present, for cyclops-react values that they are present and for Iterables that they are
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
     * import static com.aol.cyclops.util.function.Predicates.some

     * 
     * Eval<Integer> result = Matchables.future(FutureW.ofResult(1))
                                         .matches(c-> c.is( when(some(1)), then(10)), c->c.is(when(instanceOf(RuntimeException.class)), then(2)),otherwise(3));
        
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
     *  import static com.aol.cyclops.util.function.Predicates.__
     * 
     *  Eval<String> result = Matchables.listOfValues(1,new MyCase(4,5,6))
                                .matches(c->c.is(when(__,Predicates.has(4,5,6)),then("rec")),otherwise("n/a"));
        
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

    /**
     * Match against any object that is an instance of supplied type
     * 
     * <pre>
     * {@code 
     *  import static com.aol.cyclops.control.Matchable.whenGuard;
     *  import static com.aol.cyclops.control.Matchable.otherwise;
        import static com.aol.cyclops.control.Matchable.then;
     *  import static com.aol.cyclops.util.function.Predicates.eq;
     *  import static com.aol.cyclops.util.function.Predicates.any;
     *  
     *  Matchable.of(Arrays.asList(1,2,3))
                    .matches(c->c.is(whenGuard(eq(1),any(Integer.class),eq(4)),then("2")),otherwise("45"));
     * 
     * }
     * </pre>
     * 
     * 
     * @param c Class type to match against
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
     *   Eval<String> url = Matchables.url(new URL("http://www.aol.com/path?q=hello"))
                                     .on$12_45()
                                     .matches(c->c.is(when(eq("http"),in("www.aol.com","aol.com"),any(),not(eq("q=hello!"))), then("correct")),otherwise("miss"));
       
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

    public static <T1> Predicate<T1> not(final Predicate<T1> p) {
        return p.negate();
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
        return test -> ReactiveSeq.of(preds)
                                  .map(t -> t.test(test))
                                  .allMatch(r -> r);
    }

    @SafeVarargs
    public static <T1> Predicate<? super T1> anyOf(final Predicate<? super T1>... preds) {
        return test -> ReactiveSeq.of(preds)
                                  .map(t -> t.test(test))
                                  .anyMatch(r -> r);
    }

    @SafeVarargs
    public static <T1> Predicate<? super T1> noneOf(final Predicate<? super T1>... preds) {
        return test -> ReactiveSeq.of(preds)
                                  .map(t -> t.test(test))
                                  .noneMatch(r -> r);
    }

    @SafeVarargs
    public static <T1> Predicate<? super T1> xOf(final int x, final Predicate<? super T1>... preds) {
        return test -> ReactiveSeq.of(preds)
                                  .map(t -> t.test(test))
                                  .xMatch(x, r -> r);
    }

}
