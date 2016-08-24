package com.aol.cyclops.util.function;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.aol.cyclops.control.Matchable;
import com.aol.cyclops.control.Matchable.MTuple1;
import com.aol.cyclops.control.Matchable.MTuple2;
import com.aol.cyclops.control.Matchable.MTuple3;
import com.aol.cyclops.control.Matchable.MTuple4;
import com.aol.cyclops.control.Matchable.MTuple5;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.internal.matcher2.ADTPredicateBuilder;
import com.aol.cyclops.types.Value;

import lombok.NoArgsConstructor;


/**
 * 
 * Placeholder class for Predicates useful for pattern matching, filtering etc.
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
    public static <T> Predicate<T> p(Predicate<T> p) {
        return p;
    }

    /**
     * @return A Predicate that checks if it's input is an Optional with a value
     */
    public static <T> Predicate<T> optionalPresent() {
        return t -> (t instanceof Optional) ? ((Optional) t).isPresent() : false;
    }

    /**
     * @return A Predicate that checks if it's input is a cyclops-react Value (which also contains a present value)
     */
    public static <T> Predicate<T> valuePresent() {
        return t -> (t instanceof Value) ? ((Value) t).toMaybe()
                                                      .isPresent()
                : false;
    }

    /**
     * @return A Predicate that checks if it's input is an Iterable with at least one value
     */
    public static <T> Predicate<T> iterablePresent() {
        return t -> (t instanceof Iterable) ? ((Iterable) t).iterator()
                                                            .hasNext()
                : false;
    }

    /**
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
     * @param value Value to check for equality
     * @return Predicate that checks for equality with the supplied value
     */
    public static <T> Predicate<T> some(T value) {
        return eq(value);
    }

  

    /**
     * wildcard predicate
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
     * @param c Class type to match against
     * @return Predicate that mathes against type
     */
    public static final <Y> Predicate<Y> any(Class<Y> c) {
        return a -> a.getClass()
                     .isAssignableFrom(c);
    };

    /**
     * Recursively decompose and match against case classes of specified type.
     * 
     * <pre>
     * {@code
     *      List<String> result = of(new MyCase2(1,2),new MyCase2(3,4))
                                                      .patternMatch(
                                                              c->c.is(when(new MyCase2(1,2)),then("one"))
                                                                   .is(when(new MyCase2(3,4)),then("two"))
                                                                   .is(when(new MyCase2(3,5)),then("three"))
                                                                   .is(when(Predicates.type(MyCase.class).isGuard(3,4)),then(()->"two"))
                                                                   ,Matchable.otherwise("n/a")
                                                              )
                                                      .toListX();
            //Arrays.asList("one","two");
     * 
     * }
     * </pre>
     * 
     * 
     * @param type Classs type to decompose
     * @return Predicate builder that can decompose classes of specified type
     */
    public static <T> ADTPredicateBuilder<T> type(Class<T> type) {
        return new ADTPredicateBuilder<>(
                                         type);
    }

    @NoArgsConstructor
    public static class PredicateBuilder1<T1> implements Predicate<Matchable.MTuple1<T1>> {
        Predicate predicate;

        public PredicateBuilder1(MTuple1<Predicate<? super T1>> when) {
            predicate = new ADTPredicateBuilder<>(
                                                  Matchable.MTuple2.class).isGuard(when.getMatchable()
                                                                                       .v1());

        }

        @Override
        public boolean test(MTuple1<T1> t) {
            return predicate.test(t);
        }
    }

    /**
     * A return a predicate builder for deconstructing a tuple. Any Object can be mapped to a nested hierarchy of Tuples,
     * decons can be used to perform structural pattern matching over Java objects (once the Object to tuple mapping has been 
     * created).
     * 
     * @param when
     * @return
     */
    public static <T1, T2, T3> PredicateBuilder1<T1> decons(MTuple1<Predicate<? super T1>> when) {

        return new PredicateBuilder1<T1>(
                                         when);
    }

    @NoArgsConstructor
    public static class PredicateBuilder2<T1, T2> implements Predicate<Matchable.MTuple2<T1, T2>> {
        Predicate predicate;

        public PredicateBuilder2(MTuple2<Predicate<? super T1>, Predicate<? super T2>> when) {
            predicate = new ADTPredicateBuilder<>(
                                                  Matchable.MTuple2.class).isGuard(when.getMatchable()
                                                                                       .v1(),
                                                                                   when.getMatchable()
                                                                                       .v2());

        }

        @Override
        public boolean test(MTuple2<T1, T2> t) {
            return predicate.test(t);
        }
    }

    public static <T1, T2, T3> PredicateBuilder2<T1, T2> decons(MTuple2<Predicate<? super T1>, Predicate<? super T2>> when) {

        return new PredicateBuilder2<T1, T2>(
                                             when);
    }

    @NoArgsConstructor
    public static class PredicateBuilder3<T1, T2, T3> implements Predicate<Matchable.MTuple3<T1, T2, T3>> {
        Predicate predicate;

        public PredicateBuilder3(MTuple3<Predicate<? super T1>, Predicate<? super T2>, Predicate<? super T3>> when) {
            predicate = new ADTPredicateBuilder<>(
                                                  Matchable.MTuple3.class).isGuard(when.getMatchable()
                                                                                       .v1(),
                                                                                   when.getMatchable()
                                                                                       .v2(),
                                                                                   when.getMatchable()
                                                                                       .v3());

        }

        @Override
        public boolean test(MTuple3<T1, T2, T3> t) {
            return predicate.test(t);
        }
    }

    public static <T1, T2, T3> PredicateBuilder3<T1, T2, T3> decons(
            MTuple3<Predicate<? super T1>, Predicate<? super T2>, Predicate<? super T3>> when) {

        return new PredicateBuilder3<T1, T2, T3>(
                                                 when);
    }

    @NoArgsConstructor
    public static class PredicateBuilder4<T1, T2, T3, T4> implements Predicate<Matchable.MTuple4<T1, T2, T3, T4>> {
        Predicate predicate;

        public PredicateBuilder4(MTuple4<Predicate<? super T1>, Predicate<? super T2>, Predicate<? super T3>, Predicate<? super T4>> when) {
            predicate = new ADTPredicateBuilder<>(
                                                  Matchable.MTuple4.class).isGuard(when.getMatchable()
                                                                                       .v1(),
                                                                                   when.getMatchable()
                                                                                       .v2(),
                                                                                   when.getMatchable()
                                                                                       .v3(),
                                                                                   when.getMatchable()
                                                                                       .v4());

        }

        @Override
        public boolean test(MTuple4<T1, T2, T3, T4> t) {
            return predicate.test(t);
        }
    }

    public static <T1, T2, T3, T4> PredicateBuilder4<T1, T2, T3, T4> decons(
            MTuple4<Predicate<? super T1>, Predicate<? super T2>, Predicate<? super T3>, Predicate<? super T4>> when) {

        return new PredicateBuilder4<T1, T2, T3, T4>(
                                                     when);
    }

    @NoArgsConstructor
    public static class PredicateBuilder5<T1, T2, T3, T4, T5> implements Predicate<Matchable.MTuple5<T1, T2, T3, T4, T5>> {
        Predicate predicate;

        public PredicateBuilder5(
                MTuple5<Predicate<? super T1>, Predicate<? super T2>, Predicate<? super T3>, Predicate<? super T4>, Predicate<? super T5>> when) {
            predicate = new ADTPredicateBuilder<>(
                                                  Matchable.MTuple5.class).isGuard(when.getMatchable()
                                                                                       .v1(),
                                                                                   when.getMatchable()
                                                                                       .v2(),
                                                                                   when.getMatchable()
                                                                                       .v3(),
                                                                                   when.getMatchable()
                                                                                       .v4());

        }

        @Override
        public boolean test(MTuple5<T1, T2, T3, T4, T5> t) {
            return predicate.test(t);
        }
    }

    public static <T1, T2, T3, T4, T5> PredicateBuilder5<T1, T2, T3, T4, T5> decons(
            MTuple5<Predicate<? super T1>, Predicate<? super T2>, Predicate<? super T3>, Predicate<? super T4>, Predicate<? super T5>> when) {

        return new PredicateBuilder5<T1, T2, T3, T4, T5>(
                                                         when);
    }

    /**
     * Recursively compose an Object without specifying a type
     * 
     * <pre>
     * {@code 
     * return Matching.<Expression>whenValues().isType( (Add<Const,Mult> a)-> new Const(1))
    								.with(__,type(Mult.class).with(__,new Const(0)))
    			.whenValues().isType( (Add<Mult,Const> a)-> new Const(0)).with(type(Mult.class).with(__,new Const(0)),__)
    			.whenValues().isType( (Add<Add,Const> a)-> new Const(-100)).with(with(__,new Const(2)),__)
    			
    			
    			.apply(e).orElse(new Const(-1));
     * 
     * }
     * </pre>
     * 
     * @param values To match against
     * @return Predicate builder that can decompose Case class and match against specified values
     */
    @SafeVarargs
    public static <V> Predicate<V> has(V... values) {
        return new ADTPredicateBuilder<Object>(
                                               Object.class).hasGuard(values);
    }

    @SafeVarargs
    public static <V> Predicate<V> hasWhere(Predicate<V>... values) {
        return (Predicate<V>) new ADTPredicateBuilder.InternalADTPredicateBuilder<Object>(
                                                                                          Object.class).hasWhere(values);
    }

    @SafeVarargs
    public static <V> Predicate<V> is(V... values) {
        return new ADTPredicateBuilder<Object>(
                                               Object.class).<V> isGuard(values);
    }

    @SafeVarargs
    public static <V> Predicate<V> isWhere(Predicate<V>... values) {
        return (Predicate<V>) new ADTPredicateBuilder.InternalADTPredicateBuilder<Object>(
                                                                                          Object.class).isWhere(values);
    }

    /**
     * Check for universal equality (Object#equals)
     * @param value
     * @return
     */
    public static <V> Predicate<V> eq(V value) {

        return test -> Objects.equals(test, value);
    }

    /**
     * test for equivalence
     *  null eqv to absent, embedded value equivalency, non-values converted to values before testing
     *.
     * <pre>
     * {@code 
     * 
     *  Stream.of(Maybe.of(2)).filter(eqv(Maybe.of(2))).forEach(System.out::println);
     *   Stream.of(2).filter(eqv(Maybe.of(2))).forEach(System.out::println);
     * }</pre>
     * 
     * @param value
     * @return
     */
    public static <V> Predicate<Value<? super V>> eqv(Value<? super V> value) {

        return test -> test == null ? (value == null ? true : !value.toMaybe()
                                                                    .isPresent())
                : test.toMaybe()
                      .equals(value.toMaybe());

    }

    public static <V> Predicate<Iterable<? super V>> eqvIterable(Iterable<? super V> iterable) {

        return test -> test == null ? (iterable == null ? true : ListX.fromIterable(iterable)
                                                                      .isEmpty())
                : ListX.fromIterable(test)
                       .equals(ListX.fromIterable(iterable));

    }

    public static <V> Predicate<V> eqv2(Value<? super V> value) {

        return test -> test == null ? (value == null ? true : !value.toMaybe()
                                                                    .isPresent())
                : Maybe.ofNullable(test)
                       .equals(value.toMaybe());

    }

    public static <T1> Predicate<T1> not(Predicate<T1> p) {
        return p.negate();
    }

    @SafeVarargs
    public static <T1> Predicate<T1> in(T1... values){
    return test -> Arrays.asList(values)
                         .contains(test);
}

public static <T1 extends Comparable<T1>> Predicate<? super T1> greaterThan(T1 v) {
        return test -> test.compareTo(v) > 0;
    }

    public static <T1 extends Comparable<T1>> Predicate<? super T1> greaterThanOrEquals(T1 v) {
        return test -> test.compareTo(v) >= 0;
    }

    public static <T1 extends Comparable<T1>> Predicate<? super T1> lessThan(T1 v) {
        return test -> test.compareTo(v) < 0;
    }

    public static <T1 extends Comparable<T1>> Predicate<? super T1> lessThanOrEquals(T1 v) {
        return test -> test.compareTo(v) <= 0;
    }

    public static <T1 extends Comparable<T1>> Predicate<? super T1> equals(T1 v) {
        return test -> test.compareTo(v) == 0;
    }

    public static <T1> Predicate<? super T1> nullValue() {
        return test -> test == null;
    }

    public static <T1> Predicate<Collection<? super T1>> hasItems(Collection<T1> items) {
        return test -> ReactiveSeq.fromIterable(items)
                                  .map(i -> test.contains(i))
                                  .allMatch(v -> v);
    }

    public static <T1> Predicate<Collection<? super T1>> hasItems(Stream<T1> items) {
        return test -> items.map(i -> test.contains(i))
                            .allMatch(v -> v);
    }

    @SafeVarargs
    public static <T1> Predicate<Collection<? super T1>> hasItems(T1... items){
    return test -> ReactiveSeq.of(items)
                              .map(i -> test.contains(i))
                              .allMatch(v -> v);
}

@SafeVarargs
public static <T1> Predicate<Iterable<? super T1>> startsWith(T1... items) {
    return test -> ReactiveSeq.fromIterable(test)
                              .startsWithIterable(ReactiveSeq.of(items));

}

@SafeVarargs
public static <T1> Predicate<Iterable<? super T1>> endsWith(T1... items) {

    return test -> ReactiveSeq.fromIterable(test)
                              .endsWithIterable(ReactiveSeq.of(items));

}

public static <T1> Predicate<? super T1> instanceOf(Class<?> clazz) {

        return test -> clazz.isAssignableFrom(test.getClass());
    }

    @SafeVarargs
    public static <T1> Predicate<? super T1> allOf(Predicate<? super T1>... preds) {
        return test -> ReactiveSeq.of(preds)
                                  .map(t -> t.test(test))
                                  .allMatch(r -> r);
    }

    @SafeVarargs
    public static <T1> Predicate<? super T1> anyOf(Predicate<? super T1>... preds) {
        return test -> ReactiveSeq.of(preds)
                                  .map(t -> t.test(test))
                                  .anyMatch(r -> r);
    }

    @SafeVarargs
    public static <T1> Predicate<? super T1> noneOf(Predicate<? super T1>... preds) {
        return test -> ReactiveSeq.of(preds)
                                  .map(t -> t.test(test))
                                  .noneMatch(r -> r);
    }

    @SafeVarargs
    public static <T1> Predicate<? super T1> xOf(int x, Predicate<? super T1>... preds) {
        return test -> ReactiveSeq.of(preds)
                                  .map(t -> t.test(test))
                                  .xMatch(x, r -> r);
    }

}
