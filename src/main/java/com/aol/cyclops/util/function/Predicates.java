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
    public static <T> ADTPredicateBuilder<T> type(final Class<T> type) {
        return new ADTPredicateBuilder<>(
                                         type);
    }

    @NoArgsConstructor
    public static class PredicateBuilder1<T1> implements Predicate<Matchable.MTuple1<T1>> {
        Predicate predicate;

        public PredicateBuilder1(final MTuple1<Predicate<? super T1>> when) {
            predicate = new ADTPredicateBuilder<>(
                                                  Matchable.MTuple2.class).isGuard(when.getMatchable()
                                                                                       .v1());

        }

        @Override
        public boolean test(final MTuple1<T1> t) {
            return predicate.test(t);
        }
    }

    
    /**
     * Return a predicate builder for deconstructing a tuple. Any Object can be mapped to a nested hierarchy of Tuples,
     * decons can be used to perform structural pattern matching over Java objects (once the Object to tuple mapping has been 
     * created).
     * 
     * Supports structural pattern matching on a Tuple of a single field.
     * decons deconstructs the Tuple so each field can be matched independently.
     * 
     * Example : Given a class with § fields, we can provide a method to create a Matchable Tuple containing those fields
     * <pre>
     * {@code 
         * static class Address{
            int house;
         
            
            
            public MTuple1<Integer> match(){
                return Matchables.supplier(()->house);
            }
        }
        }
        </pre>
     * 
     * decons can be used to match against those fields directly
     * <pre>
     * {@code
     *  Predicates.decons(when(10)).test(new Address(10).match()));
        //true
       }
       </pre>
       And in the pattern matching API
     * <pre>
     * {@code
      String result =  new Customer("test",new Address(10))
                                .match()
                                .on$_2()
                                .matches(c->c.is(when(decons(when(10))),then("hello")), otherwise("miss"))
                                .get();
      
       //"hello"
     * 
     * }
     * </pre>
     * 
     * 
     * @param when 1 Predicates to match against (in the returned builder)
     * @return Builder that matches values provided to it, with the Predicated provided to decons
     */
    public static <T1, T2, T3> PredicateBuilder1<T1> decons(final MTuple1<Predicate<? super T1>> when) {

        return new PredicateBuilder1<T1>(
                                         when);
    }

    @NoArgsConstructor
    public static class PredicateBuilder2<T1, T2> implements Predicate<Matchable.MTuple2<T1, T2>> {
        Predicate predicate;

        public PredicateBuilder2(final MTuple2<Predicate<? super T1>, Predicate<? super T2>> when) {
            predicate = new ADTPredicateBuilder<>(
                                                  Matchable.MTuple2.class).isGuard(when.getMatchable()
                                                                                       .v1(),
                                                                                   when.getMatchable()
                                                                                       .v2());

        }

        @Override
        public boolean test(final MTuple2<T1, T2> t) {
            return predicate.test(t);
        }
    }
    /**
     * Return a predicate builder for deconstructing a tuple. Any Object can be mapped to a nested hierarchy of Tuples,
     * decons can be used to perform structural pattern matching over Java objects (once the Object to tuple mapping has been 
     * created).
     * 
     * Supports structural pattern matching on a Tuple of two fields.
     * decons deconstructs the Tuple so each field can be matched independently.
     * 
     * Example : Given a class with 2 fields, we can provide a method to create a Matchable Tuple containing those fields
     * <pre>
     * {@code 
         * static class Address{
            int house;
            String street;
            
            
            public MTuple2<Integer,String> match(){
                return Matchables.supplier2(()->house,()->street);
            }
        }
        }
        </pre>
     * 
     * decons can be used to match against those fields directly
     * <pre>
     * {@code
     *  Predicates.decons(when(10,"hello")).test(new Address(10,"hello").match()));
        //true
       }
       </pre>
       And in the pattern matching API
     * <pre>
     * {@code
      String result =  new Customer("test",new Address(10,"hello"))
                                .match()
                                .on$_2()
                                .matches(c->c.is(when(decons(when(10,"hello"))),then("hello")), otherwise("miss"))
                                .get();
      
       //"hello"
     * 
     * }
     * </pre>
     * 
     * 
     * @param when 2 Predicates to match against (in the returned builder)
     * @return Builder that matches values provided to it, with the Predicated provided to decons
     */
    public static <T1, T2, T3> PredicateBuilder2<T1, T2> decons(final MTuple2<Predicate<? super T1>, Predicate<? super T2>> when) {

        return new PredicateBuilder2<T1, T2>(
                                             when);
    }

    @NoArgsConstructor
    public static class PredicateBuilder3<T1, T2, T3> implements Predicate<Matchable.MTuple3<T1, T2, T3>> {
        Predicate predicate;

        public PredicateBuilder3(final MTuple3<Predicate<? super T1>, Predicate<? super T2>, Predicate<? super T3>> when) {
            predicate = new ADTPredicateBuilder<>(
                                                  Matchable.MTuple3.class).isGuard(when.getMatchable()
                                                                                       .v1(),
                                                                                   when.getMatchable()
                                                                                       .v2(),
                                                                                   when.getMatchable()
                                                                                       .v3());

        }

        @Override
        public boolean test(final MTuple3<T1, T2, T3> t) {
            return predicate.test(t);
        }
    }

    /**
     * Return a predicate builder for deconstructing a tuple. Any Object can be mapped to a nested hierarchy of Tuples,
     * decons can be used to perform structural pattern matching over Java objects (once the Object to tuple mapping has been 
     * created). 
     * Supports structural pattern matching on a Tuple of three fields.
     * decons deconstructs the Tuple so each field can be matched independently.
     * 
     * Example : Given a class with 3 fields, we can provide a method to create a Matchable Tuple containing those fields
     * <pre>
     * {@code 
         * static class Address{
            int house;
            String street;
            String city;
            
            public MTuple3<Integer,String,String> match(){
                return Matchables.supplier3(()->house,()->street,()->city);
            }
        }
        }
        </pre>
     * 
     * decons can be used to match against those fields directly
     * <pre>
     * {@code
     *  Predicates.decons(when(10,"hello","my city")).test(new Address(10,"hello","my city").match()));
        //true
       }
       </pre>
       And in the pattern matching API
     * <pre>
     * {@code
      String result =  new Customer("test",new Address(10,"hello","my city"))
                                .match()
                                .on$_2()
                                .matches(c->c.is(when(decons(when(10,"hello","my city"))),then("hello")), otherwise("miss")).get();
      
       //"hello"
     * 
     * }
     * </pre>
     * 
     * 
     * @param when 3 Predicates to match against (in the returned builder)
     * @return Builder that matches values provided to it, with the Predicated provided to decons
     */
    public static <T1, T2, T3> PredicateBuilder3<T1, T2, T3> decons(
            final MTuple3<Predicate<? super T1>, Predicate<? super T2>, Predicate<? super T3>> when) {

        return new PredicateBuilder3<T1, T2, T3>(
                                                 when);
    }

    @NoArgsConstructor
    public static class PredicateBuilder4<T1, T2, T3, T4> implements Predicate<Matchable.MTuple4<T1, T2, T3, T4>> {
        Predicate predicate;

        public PredicateBuilder4(final MTuple4<Predicate<? super T1>, Predicate<? super T2>, Predicate<? super T3>, Predicate<? super T4>> when) {
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
        public boolean test(final MTuple4<T1, T2, T3, T4> t) {
            return predicate.test(t);
        }
    }
    /**
     * Return a predicate builder for deconstructing a tuple. Any Object can be mapped to a nested hierarchy of Tuples,
     * decons can be used to perform structural pattern matching over Java objects (once the Object to tuple mapping has been 
     * created). 
     * Supports structural pattern matching on a Tuple of four fields.
     * decons deconstructs the Tuple so each field can be matched independently.
     * 
     * Example : Given a class with 4 fields, we can provide a method to create a Matchable Tuple containing those fields
     * <pre>
     * {@code 
         * static class Address{
            int house;
            String street;
            String city;
            String country;
            
            public MTuple4<Integer,String,String> match(){
                return Matchables.supplier4(()->house,()->street,()->city,()->country);
            }
        }
        }
        </pre>
     * 
     * decons can be used to match against those fields directly
     * <pre>
     * {@code
     *  Predicates.decons(when(10,"hello","my city","Faroe Islands")).test(new Address(10,"hello","my city","Faroe Islands").match()));
        //true
       }
       </pre>
       And in the pattern matching API
     * <pre>
     * {@code
      String result =  new Customer("test",new Address(10,"hello","my city","Faroe Islands"))
                                .match()
                                .on$_2()
                                .matches(c->c.is(when(decons(when(10,"hello","my city","Faroe Islands"))),then("hello")), otherwise("miss"))
                                .get();
      
       //"hello"
     * 
     * }
     * </pre>
     * 
     * 
     * @param when 4 Predicates to match against (in the returned builder)
     * @return Builder that matches values provided to it, with the Predicated provided to decons
     */
    public static <T1, T2, T3, T4> PredicateBuilder4<T1, T2, T3, T4> decons(
            final MTuple4<Predicate<? super T1>, Predicate<? super T2>, Predicate<? super T3>, Predicate<? super T4>> when) {

        return new PredicateBuilder4<T1, T2, T3, T4>(
                                                     when);
    }

    @NoArgsConstructor
    public static class PredicateBuilder5<T1, T2, T3, T4, T5> implements Predicate<Matchable.MTuple5<T1, T2, T3, T4, T5>> {
        Predicate predicate;

        public PredicateBuilder5(
                final MTuple5<Predicate<? super T1>, Predicate<? super T2>, Predicate<? super T3>, Predicate<? super T4>, Predicate<? super T5>> when) {
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
        public boolean test(final MTuple5<T1, T2, T3, T4, T5> t) {
            return predicate.test(t);
        }
    }
    /**
     * Return a predicate builder for deconstructing a tuple. Any Object can be mapped to a nested hierarchy of Tuples,
     * decons can be used to perform structural pattern matching over Java objects (once the Object to tuple mapping has been 
     * created). 
     * Supports structural pattern matching on a Tuple of five fields.
     * decons deconstructs the Tuple so each field can be matched independently.
     * 
     * Example : Given a class with 5 fields, we can provide a method to create a Matchable Tuple containing those fields
     * <pre>
     * {@code 
         * static class Address{
            int house;
            String street;
            String city;
            String country;
            String zip;
            
            public MTuple5<Integer,String,String> match(){
                return Matchables.supplier5(()->house,()->street,()->city,()->country,()->zip);
            }
        }
        }
        </pre>
     * 
     * decons can be used to match against those fields directly
     * <pre>
     * {@code
     *  Predicates.decons(when(10,"hello","my city","Faroe Islands")).test(new Address(10,"hello","my city","Faroe Islands","CR25").match()));
        //true
       }
       </pre>
       And in the pattern matching API
     * <pre>
     * {@code
      String result =  new Customer("test",new Address(10,"hello","my city","Faroe Islands","CR25"))
                                .match()
                                .on$_2()
                                .matches(c->c.is(when(decons(when(10,"hello","my city","Faroe Islands","CR25"))),then("hello")), otherwise("miss"))
                                .get();
      
       //"hello"
     * 
     * }
     * </pre>
     * 
     * 
     * @param when 5 Predicates to match against (in the returned builder)
     * @return Builder that matches values provided to it, with the Predicated provided to decons
     */
    public static <T1, T2, T3, T4, T5> PredicateBuilder5<T1, T2, T3, T4, T5> decons(
            final MTuple5<Predicate<? super T1>, Predicate<? super T2>, Predicate<? super T3>, Predicate<? super T4>, Predicate<? super T5>> when) {

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
    public static <V> Predicate<V> has(final V... values) {
        return new ADTPredicateBuilder<Object>(
                                               Object.class).hasGuard(values);
    }

    @SafeVarargs
    public static <V> Predicate<V> hasWhere(final Predicate<V>... values) {
        return new ADTPredicateBuilder.InternalADTPredicateBuilder<Object>(
                                                                           Object.class).hasWhere(values);
    }

    @SafeVarargs
    public static <V> Predicate<V> is(final V... values) {
        return new ADTPredicateBuilder<Object>(
                                               Object.class).<V> isGuard(values);
    }

    @SafeVarargs
    public static <V> Predicate<V> isWhere(final Predicate<V>... values) {
        return new ADTPredicateBuilder.InternalADTPredicateBuilder<Object>(
                                                                           Object.class).isWhere(values);
    }

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
