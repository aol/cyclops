package cyclops.control.either;

import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.Value;
import com.aol.cyclops.types.Zippable;
import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.types.stream.reactive.ValueSubscriber;
import cyclops.Semigroups;
import cyclops.Streams;
import cyclops.collections.ListX;
import cyclops.collections.immutable.PStackX;
import cyclops.control.*;
import cyclops.function.*;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import cyclops.stream.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.reactivestreams.Publisher;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * A totally Lazy Either implementation with tail call optimization for map and flatMap operators.
 * 
 * 'Right' (or primary type) biased disjunct union. Often called Either, but in a generics heavy Java world Either is half the length of Either.
 * 
 *  No 'projections' are provided, swap() and secondaryXXXX alternative methods can be used instead.
 *  
 *  Either is used to represent values that can be one of two states (for example a validation result, either everything is ok - or we have an error).
 *  It can be used to avoid a common design anti-pattern where an Object has two fields one of which is always null (or worse, both are defined as Optionals).
 *  
 *  <pre>
 *  {@code 
 *     
 *     public class Member{
 *           Either<SeniorTeam,JuniorTeam> team;      
 *     }
 *     
 *     Rather than
 *     
 *     public class Member{
 *           @Setter
 *           SeniorTeam seniorTeam = null;
 *           @Setter
 *           JuniorTeam juniorTeam = null;      
 *     }
 *  }
 *  </pre>
 *  
 *  Either's have two states
 *  Right : Most methods operate naturally on the primary type, if it is present. If it is not, nothing happens.
 *  Left : Most methods do nothing to the secondary type if it is present. 
 *              To operate on the Left type first call swap() or use secondary analogs of the main operators.
 *  
 *  Instantiating an Either - Right
 *  <pre>
 *  {@code 
 *      Either.right("hello").map(v->v+" world") 
 *    //Either.right["hello world"]
 *  }
 *  </pre>
 *  
 *  Instantiating an Either - Left
 *  <pre>
 *  {@code 
 *      Either.left("hello").map(v->v+" world") 
 *    //Either.seconary["hello"]
 *  }
 *  </pre>
 *  
 *  Either can operate (via map/flatMap) as a Functor / Monad and via combine as an ApplicativeFunctor
 *  
 *   Values can be accumulated via 
 *  <pre>
 *  {@code 
 *  Xor.accumulateLeft(ListX.of(Either.left("failed1"),
                                                    Either.left("failed2"),
                                                    Either.right("success")),
                                                    Semigroups.stringConcat)
 *  
 *  //failed1failed2
 *  
 *   Either<String,String> fail1 = Either.left("failed1");
     fail1.swap().combine((a,b)->a+b)
                 .combine(Either.left("failed2").swap())
                 .combine(Either.<String,String>primary("success").swap())
 *  
 *  //failed1failed2
 *  }
 *  </pre>
 * 
 * 
 * For Inclusive Ors @see Ior
 * 
 * @author johnmcclean
 *
 * @param <ST> Left type
 * @param <PT> Right type
 */
public interface Either<ST, PT> extends Xor<ST,PT> {

    static <LT1,RT> Either<LT1,RT> fromMonadicValue(MonadicValue<RT> mv2){
        if(mv2 instanceof Either){
            return (Either)mv2;
        }
        return mv2.toOptional().isPresent()? Either.right(mv2.get()) : Either.left(null);

    }




    /**
     *  Turn a collection of Eithers into a single Either with Lists of values.
     *  
     * <pre>
     * {@code 
     * 
     * Either<String,Integer> just  = Either.right(10);
       Either<String,Integer> none = Either.left("none");
        
        
     * Either<ListX<String>,ListX<Integer>> xors =Either.sequence(ListX.of(just,none,Either.right(1)));
       //Eitehr.right(ListX.of(10,1)));
     * 
     * }</pre>
     *
     * 
     * 
     * @param Either Either to sequence
     * @return Either Sequenced
     */
    public static <LT1, PT> Either<ListX<LT1>,ListX<PT>> sequenceRight(final CollectionX<Either<LT1, PT>> xors) {
        Objects.requireNonNull(xors);
        return AnyM.sequence(xors.stream().filter(Either::isRight).map(AnyM::fromEither).toListX(),Witness.either.INSTANCE)
                .to(Witness::either);
    }
    public static <LT1, PT> Either<ListX<LT1>,ListX<PT>> sequenceLeft(final CollectionX<Either<LT1, PT>> xors) {
        Objects.requireNonNull(xors);
        Either<ListX<PT>,ListX<LT1>> res = AnyM.sequence(xors.stream()
                                 .filter(Either::isRight)
                                 .map(i->AnyM.fromEither(i.swap()))
                                 .toListX(),
                                Witness.either.INSTANCE)
                    .to(Witness::either);
        return res.swap();
    }
    /**
     * Traverse a Collection of Either producting an Either3 with a ListX, applying the transformation function to every
     * element in the list
     * 
     * @param xors Eithers to sequence and transform
     * @param fn Transformation function
     * @return An Either with a transformed list
     */
    public static <LT1, PT,R> Either<ListX<LT1>,ListX<R>> traverseRight(final CollectionX<Either<LT1, PT>> xors, Function<? super PT, ? extends R> fn) {
        return  sequenceRight(xors).map(l->l.map(fn));
    }
    public static <LT1, PT,R> Either<ListX<R>,ListX<PT>> traverseLeft(final CollectionX<Either<LT1, PT>> xors, Function<? super LT1, ? extends R> fn) {
        return  sequenceLeft(xors).secondaryMap(l->l.map(fn));
    }
   

    /**
     *  Accumulate the results only from those Either3 which have a Right type present, using the supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see com.aol.cyclops.Monoids }.
     * 
     * <pre>
     * {@code 
     * Either3<String,String,Integer> just  = Either3.right(10);
       Either3<String,String,Integer> none = Either3.left("none");
     *  
     *  Either3<ListX<String>,ListX<String>,Integer> xors = Either3.accumulatePrimary(Monoids.intSum,ListX.of(just,none,Either3.right(1)));
        //Either3.right(11);
     * 
     * }
     * </pre>
     * 
     * 
     * 
     * @param xors Collection of Eithers to accumulate primary values
     * @param reducer  Reducer to accumulate results
     * @return  Either populated with the accumulate primary operation
     */
    public static <LT1, RT> Either<ListX<LT1>, RT> accumulate(final Monoid<RT> reducer, final CollectionX<Either<LT1, RT>> xors) {
        return sequenceRight(xors).map(s -> s.reduce(reducer));
    }
    
    public static <LT, B, RT> Either<LT,RT> rightEval(final Eval<RT> right) {
        return new Right<>(
                           right);
    }

    public static <LT, B, RT> Either<LT, RT> leftEval(final Eval<LT> left) {
        return new Left<>(
                          left);
    }

    
    /**
     * Static method useful as a method reference for fluent consumption of any value type stored in this Either 
     * (will capture the lowest common type)
     * 
     * <pre>
     * {@code 
     * 
     *   myEither.to(Either::consumeAny)
                 .accept(System.out::println);
     * }
     * </pre>
     * 
     * @param either Either to consume value for
     * @return Consumer we can apply to consume value
     */
    static <X, LT extends X, M extends X, RT extends X>  Consumer<Consumer<? super X>> consumeAny(Either<LT, RT> either){
        return in->visitAny(in,either);
    }
    
    static <X, LT extends X, M extends X, RT extends X,R>  Function<Function<? super X, R>,R> applyAny(Either<LT, RT> either){
        return in->visitAny(either,in);
    }

    static <X, PT extends X, ST extends X,R> R visitAny(Either<ST, PT> either, Function<? super X, ? extends R> fn){
        return either.visit(fn, fn);
    }
 
    static <X, LT extends X, RT extends X> X visitAny(Consumer<? super X> c, Either<LT, RT> either){
        Function<? super X, X> fn = x ->{
            c.accept(x);
            return x;
        };
        return visitAny(either,fn);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.Xor#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops.util.function.TriFunction, com.aol.cyclops.util.function.QuadFunction)
     */
    @Override
    default <T2, R1, R2, R3, R> Either<ST, R> forEach4(Function<? super PT, ? extends MonadicValue<R1>> value1,
                                                       BiFunction<? super PT, ? super R1, ? extends MonadicValue<R2>> value2,
                                                       Fn3<? super PT, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
                                                       Fn4<? super PT, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return (Either<ST, R>)Xor.super.forEach4(value1, value2, value3, yieldingFunction);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.Xor#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops.util.function.TriFunction, com.aol.cyclops.util.function.QuadFunction, com.aol.cyclops.util.function.QuadFunction)
     */
    @Override
    default <T2, R1, R2, R3, R> Either<ST, R> forEach4(Function<? super PT, ? extends MonadicValue<R1>> value1,
                                                       BiFunction<? super PT, ? super R1, ? extends MonadicValue<R2>> value2,
                                                       Fn3<? super PT, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
                                                       Fn4<? super PT, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                       Fn4<? super PT, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return (Either<ST, R>)Xor.super.forEach4(value1, value2, value3, filterFunction, yieldingFunction);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.Xor#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops.util.function.TriFunction)
     */
    @Override
    default <T2, R1, R2, R> Either<ST, R> forEach3(Function<? super PT, ? extends MonadicValue<R1>> value1,
                                                   BiFunction<? super PT, ? super R1, ? extends MonadicValue<R2>> value2,
                                                   Fn3<? super PT, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        
        return (Either<ST, R>)Xor.super.forEach3(value1, value2, yieldingFunction);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.Xor#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops.util.function.TriFunction, com.aol.cyclops.util.function.TriFunction)
     */
    @Override
    default <T2, R1, R2, R> Either<ST, R> forEach3(Function<? super PT, ? extends MonadicValue<R1>> value1,
                                                   BiFunction<? super PT, ? super R1, ? extends MonadicValue<R2>> value2,
                                                   Fn3<? super PT, ? super R1, ? super R2, Boolean> filterFunction,
                                                   Fn3<? super PT, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        
        return (Either<ST, R>)Xor.super.forEach3(value1, value2, filterFunction, yieldingFunction);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.Xor#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> Either<ST, R> forEach2(Function<? super PT, ? extends MonadicValue<R1>> value1,
                                           BiFunction<? super PT, ? super R1, ? extends R> yieldingFunction) {
        
        return (Either<ST, R>)Xor.super.forEach2(value1, yieldingFunction);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.Xor#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> Either<ST, R> forEach2(Function<? super PT, ? extends MonadicValue<R1>> value1,
                                           BiFunction<? super PT, ? super R1, Boolean> filterFunction,
                                           BiFunction<? super PT, ? super R1, ? extends R> yieldingFunction) {
        
        return (Either<ST, R>)Xor.super.forEach2(value1, filterFunction, yieldingFunction);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.Xor#combineToList(com.aol.cyclops.control.Xor, java.util.function.BiFunction)
     */
    @Override
    default <T2, R> Either<PStackX<ST>, R> combineToList(Xor<ST, ? extends T2> app,
                                                         BiFunction<? super PT, ? super T2, ? extends R> fn) {
        
        return (Either<PStackX<ST>, R>)Xor.super.combineToList(app, fn);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.Xor#combine(com.aol.cyclops.control.Xor, java.util.function.BinaryOperator, java.util.function.BiFunction)
     */
    @Override
    default <T2, R> Either<ST, R> combine(Xor<? extends ST, ? extends T2> app, BinaryOperator<ST> semigroup,
                                          BiFunction<? super PT, ? super T2, ? extends R> fn) {
        
        return (Either<ST, R>)Xor.super.combine(app, semigroup, fn);
    }
    /**
     * Lazily construct a Right Either from the supplied publisher
     * <pre>
     * {@code 
     *   ReactiveSeq<Integer> stream =  ReactiveSeq.of(1,2,3);
        
         Either<Throwable,Integer> future = Either.fromPublisher(stream);
        
         //Either[1]
     * 
     * }
     * </pre>
     * @param pub Publisher to construct an Either from
     * @return Either constructed from the supplied Publisher
     */
    public static <T> Either<Throwable, T> fromPublisher(final Publisher<T> pub) {
        final ValueSubscriber<T> sub = ValueSubscriber.subscriber();
        pub.subscribe(sub);
        return Either.rightEval(sub.toEvalLater());
    }

    /**
     * Construct a Right Either from the supplied Iterable
     * <pre>
     * {@code 
     *   List<Integer> list =  Arrays.asList(1,2,3);
        
         Either<Throwable,Integer> future = Either.fromIterable(list);
        
         //Either[1]
     * 
     * }
     * </pre> 
     * @param iterable Iterable to construct an Either from
     * @return Either constructed from the supplied Iterable
     */
    public static <ST, T> Either<ST, T> fromIterable(final Iterable<T> iterable) {

        final Iterator<T> it = iterable.iterator();
        return it.hasNext() ? Either.right( it.next()) : Either.left(null);
    }

    /**
     * Create an instance of the secondary type. Most methods are biased to the primary type,
     * so you will need to use swap() or secondaryXXXX to manipulate the wrapped value
     * 
     * <pre>
     * {@code 
     *   Either.<Integer,Integer>left(10).map(i->i+1);
     *   //Either.left[10]
     *    
     *    Either.<Integer,Integer>left(10).swap().map(i->i+1);
     *    //Either.right[11]
     * }
     * </pre>
     * 
     * 
     * @param value to wrap
     * @return Left instance of Either
     */
    public static <ST, PT> Either<ST, PT> left(final ST value) {
        return new Left<>(Eval.later(()-> value));
    }

    /**
     * Create an instance of the primary type. Most methods are biased to the primary type,
     * which means, for example, that the map method operates on the primary type but does nothing on secondary Eithers
     * 
     * <pre>
     * {@code 
     *   Either.<Integer,Integer>right(10).map(i->i+1);
     *   //Either.right[11]
     *    
     *   
     * }
     * </pre>
     * 
     * 
     * @param value To construct an Either from
     * @return Right type instanceof Either
     */
    public static <ST, PT> Either<ST, PT> right(final PT value) {
        
        return new Right<ST,PT>(Eval.later(()->
                           value));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.MonadicValue#fromEither5()
     */

    default AnyM<Witness.either,PT> anyMEither() {
        return AnyM.fromEither(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.Applicative#combine(java.util.function.
     * BinaryOperator, com.aol.cyclops.types.Applicative)
     */
    @Override
    default Either<ST, PT> zip(BinaryOperator<Zippable<PT>> combiner, Zippable<PT> app) {

        return (Either<ST, PT>) Xor.super.zip(combiner, app);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.types.MonadicValue#flatMapIe(java.util.function.
     * Function)
     */
    @Override
    default <R> Either<ST, R> flatMapIe(Function<? super PT, ? extends Iterable<? extends R>> mapper) {
        return (Either<ST, R>) Xor.super.flatMapIe(mapper);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.types.MonadicValue#flatMapP(java.util.function.
     * Function)
     */
    @Override
    default <R> Either<ST, R> flatMapP(Function<? super PT, ? extends Publisher<? extends R>> mapper) {
        return (Either<ST, R>) Xor.super.flatMapP(mapper);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.types.MonadicValue#coflatMap(java.util.function.Function)
     */
    @Override
    default <R> Either<ST, R> coflatMap(final Function<? super MonadicValue<PT>, R> mapper) {
        return (Either<ST, R>) Xor.super.coflatMap(mapper);
    }

    // cojoin
    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.MonadicValue#nest()
     */
    @Override
    default Either<ST, MonadicValue<PT>> nest() {
        return this.map(t -> unit(t));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.MonadicValue#combine(com.aol.cyclops.Monoid,
     * com.aol.cyclops.types.MonadicValue)
     */ 
    @Override
    default Either<ST, PT> combineEager(final Monoid<PT> monoid, final MonadicValue<? extends PT> v2) {
        return (Either<ST, PT>) Xor.super.combineEager(monoid, v2);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.MonadicValue#unit(java.lang.Object)
     */
    @Override
    default <T> Either<ST, T> unit(final T unit) {
        return Either.right(unit);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.Convertable#toOptional()
     */
    @Override
    default Optional<PT> toOptional() {
        return isRight() ? Optional.ofNullable(get()) : Optional.empty();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.types.Filterable#filter(java.util.function.Predicate)
     */
    @Override
    Either<ST, PT> filter(Predicate<? super PT> test);

    /**
     * If this Either contains the Left type, map it's value so that it contains the Right type 
     * 
     * 
     * @param fn Function to map secondary type to primary
     * @return Either with secondary type mapped to primary
     */
    Either<ST, PT> secondaryToPrimayMap(Function<? super ST, ? extends PT> fn);

    /**
     * Always map the Left type of this Either if it is present using the provided transformation function
     * 
     * @param fn Transformation function for Left types
     * @return Either with Left type transformed
     */
    <R> Either<R, PT> secondaryMap(Function<? super ST, ? extends R> fn);

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.MonadicValue#map(java.util.function.Function)
     */
    @Override
    <R> Either<ST, R> map(Function<? super PT, ? extends R> fn);

    /**
     * Peek at the Left type value if present
     * 
     * @param action Consumer to peek at the Left type value
     * @return Either with the same values as before
     */
    Either<ST, PT> secondaryPeek(Consumer<? super ST> action);

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.Functor#peek(java.util.function.Consumer)
     */
    @Override
    Either<ST, PT> peek(Consumer<? super PT> action);

    /**
     * Swap types so operations directly affect the current (pre-swap) Left type
     *<pre>
     *  {@code 
     *    
     *    Either.left("hello")
     *       .map(v->v+" world") 
     *    //Either.seconary["hello"]
     *    
     *    Either.left("hello")
     *       .swap()
     *       .map(v->v+" world") 
     *       .swap()
     *    //Either.seconary["hello world"]
     *  }
     *  </pre>
     * 
     * 
     * @return Swap the primary and secondary types, allowing operations directly on what was the Left type
     */
    Either<PT, ST> swap();

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.Value#toIor()
     */
    @Override
    Ior<ST, PT> toIor();

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.Convertable#isPresent()
     */
    @Override
    default boolean isPresent() {
        return isRight();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.Value#toEither()
     */
    @Override
    default Xor<ST, PT> toXor() {
        return visit(Xor::secondary, Xor::primary);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.Value#toEither(java.lang.Object)
     */
    @Override
    default <ST2> Xor<ST2, PT> toXor(final ST2 secondary) {
        return visit(s -> Xor.secondary(secondary), p -> Xor.primary(p));
    }

     


    default boolean isSecondary(){
        return isLeft();
    }
    default boolean isPrimary(){
        return isRight();
    }
   

   
    /**
     * Visitor pattern for this Ior.
     * Execute the secondary function if this Either contains an element of the secondary type
     * Execute the primary function if this Either contains an element of the primary type
     * 
     * 
     * <pre>
     * {@code 
     *  Either.right(10)
     *     .visit(secondary->"no", primary->"yes")
     *  //Either["yes"]
        
        Either.left(90)
           .visit(secondary->"no", primary->"yes")
        //Either["no"]
         
    
     * 
     * }
     * </pre>
     * 
     * @param secondary Function to execute if this is a Left Either
     * @param primary Function to execute if this is a Right Ior
     * @param both Function to execute if this Ior contains both types
     * @return Result of executing the appropriate function
     */
    <R> R visit(Function<? super ST, ? extends R> secondary, Function<? super PT, ? extends R> primary);

    @Deprecated // use bimap instead
    default <R1, R2> Either<R1, R2> mapBoth(final Function<? super ST, ? extends R1> secondary,
                                            final Function<? super PT, ? extends R2> primary) {
        if (isLeft())
            return (Either<R1, R2>) swap().map(secondary)
                                          .swap();
        return (Either<R1, R2>) map(primary);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.BiFunctor#bimap(java.util.function.Function,
     * java.util.function.Function)
     */
    @Override
    default <R1, R2> Either<R1, R2> bimap(Function<? super ST, ? extends R1> secondary,
                                          Function<? super PT, ? extends R2> primary) {
        if (isLeft())
            return (Either<R1, R2>) swap().map(secondary)
                                          .swap();
        return (Either<R1, R2>) map(primary);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.BiFunctor#bipeek(java.util.function.Consumer,
     * java.util.function.Consumer)
     */
    @Override
    default Either<ST, PT> bipeek(Consumer<? super ST> c1, Consumer<? super PT> c2) {

        return (Either<ST, PT>) Xor.super.bipeek(c1, c2);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.BiFunctor#bicast(java.lang.Class,
     * java.lang.Class)
     */
    @Override
    default <U1, U2> Either<U1, U2> bicast(Class<U1> type1, Class<U2> type2) {

        return (Either<U1, U2>) Xor.super.bicast(type1, type2);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.types.BiFunctor#bitrampoline(java.util.function.Function,
     * java.util.function.Function)
     */
    @Override
    default <R1, R2> Either<R1, R2> bitrampoline(Function<? super ST, ? extends Trampoline<? extends R1>> mapper1,
                                                 Function<? super PT, ? extends Trampoline<? extends R2>> mapper2) {

        return (Either<R1, R2>) Xor.super.bitrampoline(mapper1, mapper2);
    }




    /*
     * (non-Javadoc)
     * 
     * @see java.util.function.Supplier#get()
     */
    @Override
    PT get();

    /**
     * @return A Value containing the secondary Value if present
     */
    Value<ST> secondaryValue();

    /**
     * @return The Left Value if present, otherwise null
     */
    ST secondaryGet();

    /**
     * @return The Left value wrapped in an Optional if present, otherwise an empty Optional
     */
    Optional<ST> secondaryToOptional();

    /**
     * @return A Stream containing the secondary value if present, otherwise an empty Stream
     */
    ReactiveSeq<ST> secondaryToStream();

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.types.MonadicValue#flatMap(java.util.function.Function)
     */
    @Override
    <RT1> Either<ST, RT1> flatMap(
            Function<? super PT, ? extends MonadicValue<? extends RT1>> mapper);

    
    
    /**
     * Perform a flatMap operation on the Left type
     * 
     * @param mapper Flattening transformation function
     * @return Either containing the value inside the result of the transformation function as the Left value, if the Left type was present
     */
    <LT1> Either<LT1, PT> secondaryFlatMap(Function<? super ST, ? extends Xor<LT1, PT>> mapper);

    
    /**
     * A flatMap operation that keeps the Left and Right types the same
     * 
     * @param fn Transformation function
     * @return Either
     */
    Either<ST, PT> secondaryToPrimayFlatMap(Function<? super ST, ? extends Xor<ST, PT>> fn);

    @Deprecated // use bipeek
    void peek(Consumer<? super ST> stAction, Consumer<? super PT> ptAction);

    /**
     * @return True if this is a primary Either
     */
    public boolean isRight();

    /**
     * @return True if this is a secondary Either
     */
    public boolean isLeft();

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.types.applicative.ApplicativeFunctor#ap(com.aol.cyclops.
     * types.Value, java.util.function.BiFunction)
     */
    @Override
    <T2, R> Either<ST, R> combine(Value<? extends T2> app, BiFunction<? super PT, ? super T2, ? extends R> fn);

    /**
     * @return An Either with the secondary type converted to a persistent list, for use with accumulating app function  {@link Either#combine(Either,BiFunction)}
     */
    default Either<PStackX<ST>, PT> list() {
        return secondaryMap(PStackX::of);
    }

    /**
     * Accumulate secondarys into a PStackX (extended Persistent List) and Right with the supplied combiner function
     * Right accumulation only occurs if all phases are primary
     * 
     * @param app Value to combine with
     * @param fn Combiner function for primary values
     * @return Combined Either
     */
    default <T2, R> Either<PStackX<ST>, R> combineToList(final Either<ST, ? extends T2> app,
                                                         final BiFunction<? super PT, ? super T2, ? extends R> fn) {
        return list().combine(app.list(), Semigroups.collectionXConcat(), fn);
    }

    /**
     * Accumulate secondary values with the provided BinaryOperator / Semigroup {@link Semigroups}
     * Right accumulation only occurs if all phases are primary
     * 
     * <pre>
     * {@code 
     *  Either<String,String> fail1 =  Either.left("failed1");
        Either<PStackX<String>,String> result = fail1.list().combine(Either.left("failed2").list(), Semigroups.collectionConcat(),(a,b)->a+b);
        
        //Left of [PStackX.of("failed1","failed2")))]
     * }
     * </pre>
     * 
     * @param app Value to combine with
     * @param semigroup to combine secondary types
     * @param fn To combine primary types
     * @return Combined Either
     */

    default <T2, R> Either<ST, R> combine(final Either<? extends ST, ? extends T2> app,
                                          final BinaryOperator<ST> semigroup, final BiFunction<? super PT, ? super T2, ? extends R> fn) {
        return this.visit(secondary -> app.visit(s2 -> Either.left(semigroup.apply(s2, secondary)),
                                                 p2 -> Either.left(secondary)),
                          primary -> app.visit(s2 -> Either.left(s2),
                                               p2 -> Either.right(fn.apply(primary, p2))));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.applicative.ApplicativeFunctor#zip(java.lang.
     * Iterable, java.util.function.BiFunction)
     */
    @Override
    default <T2, R> Either<ST, R> zip(final Iterable<? extends T2> app,
                                      final BiFunction<? super PT, ? super T2, ? extends R> fn) {
        return map(v -> Tuple.tuple(v, Curry.curry2(fn)
                                            .apply(v))).flatMap(tuple -> Either.fromIterable(app)
                                                                               .visit(i -> Either.right(tuple.v2.apply(i)),
                                                                                      () -> Either.left(null)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.applicative.ApplicativeFunctor#zip(java.util.
     * function.BiFunction, org.reactivestreams.Publisher)
     */
    @Override
    default <T2, R> Either<ST, R> zipP(final Publisher<? extends T2> app,final BiFunction<? super PT, ? super T2, ? extends R> fn) {
        return map(v -> Tuple.tuple(v, Curry.curry2(fn)
                                            .apply(v))).flatMap(tuple -> Either.fromPublisher(app)
                                                                               .visit(i -> Either.right(tuple.v2.apply(i)),
                                                                                      () -> Either.left(null)));
    }



    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.Zippable#zip(java.util.stream.Stream,
     * java.util.function.BiFunction)
     */
    @Override
    default <U, R> Either<ST, R> zipS(final Stream<? extends U> other,
                                     final BiFunction<? super PT, ? super U, ? extends R> zipper) {

        return (Either<ST, R>) Xor.super.zipS(other, zipper);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.Zippable#zip(java.util.stream.Stream)
     */
    @Override
    default <U> Either<ST, Tuple2<PT, U>> zipS(final Stream<? extends U> other) {

        return (Either) Xor.super.zipS(other);
    }



    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.Zippable#zip(java.lang.Iterable)
     */
    @Override
    default <U> Either<ST, Tuple2<PT, U>> zip(final Iterable<? extends U> other) {

        return (Either) Xor.super.zip(other);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.lambda.monads.Filterable#ofType(java.lang.Class)
     */
    @Override
    default <U> Either<ST, U> ofType(final Class<? extends U> type) {

        return (Either<ST, U>) Xor.super.ofType(type);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.lambda.monads.Filterable#filterNot(java.util.function.
     * Predicate)
     */
    @Override
    default Either<ST, PT> filterNot(final Predicate<? super PT> fn) {

        return (Either<ST, PT>) Xor.super.filterNot(fn);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.lambda.monads.Filterable#notNull()
     */
    @Override
    default Either<ST, PT> notNull() {

        return (Either<ST, PT>) Xor.super.notNull();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.lambda.monads.Functor#cast(java.lang.Class)
     */
    @Override
    default <U> Either<ST, U> cast(final Class<? extends U> type) {

        return (Either<ST, U>) Xor.super.cast(type);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.lambda.monads.Functor#trampoline(java.util.function.
     * Function)
     */
    @Override
    default <R> Either<ST, R> trampoline(final Function<? super PT, ? extends Trampoline<? extends R>> mapper) {

        return (Either<ST, R>) Xor.super.trampoline(mapper);
    }

    static <ST, PT> Either<ST, PT> narrow(final Either<? extends ST, ? extends PT> broad) {
        return (Either<ST, PT>) broad;
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static final class Lazy<ST, PT> implements Either<ST, PT> {

        private final Eval<Either<ST, PT>> lazy;

        private static <ST, PT> Lazy<ST, PT> lazy(Eval<Either<ST, PT>> lazy) {
            return new Lazy<>(
                              lazy);
        }
        
       
        public Either<ST, PT> resolve() {
          return lazy.get()
                       .visit(Either::left, Either::right);
        }
        @Override
        public <R> Either<ST, R> map(final Function<? super PT, ? extends R> mapper) {
          
            return lazy(Eval.later( () -> resolve().map(mapper)));
         
        }
        
        private <PT> Either<ST, PT> toEither(MonadicValue<? extends PT> value) {
            return value.visit(p -> Either.right(p), () -> Either.left(null));
        }

        @Override
        public <RT1> Either<ST, RT1> flatMap(
                final Function<? super PT, ? extends MonadicValue<? extends RT1>> mapper) {

           
            return lazy(Eval.later( () -> resolve().flatMap(mapper)));
         
        }

        @Override
        public Either<ST, PT> filter(final Predicate<? super PT> test) {
            return flatMap(t -> test.test(t) ? this : Either.left(null));
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.aol.cyclops.sum.types.Either#secondaryValue()
         */
        @Override
        public Value<ST> secondaryValue() {
            return trampoline()
                       .secondaryValue();
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.aol.cyclops.sum.types.Either#secondaryToPrimayMap(java.util.
         * function.Function)
         */
        @Override
        public Either<ST, PT> secondaryToPrimayMap(Function<? super ST, ? extends PT> fn) {
            return lazy(Eval.later(() ->  resolve()
                                             .secondaryToPrimayMap(fn)));

        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.aol.cyclops.sum.types.Either#secondaryMap(java.util.function.
         * Function)
         */
        @Override
        public <R> Either<R, PT> secondaryMap(Function<? super ST, ? extends R> fn) {
            return lazy(Eval.later(() -> resolve().secondaryMap(fn)));
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.aol.cyclops.sum.types.Either#secondaryPeek(java.util.function.
         * Consumer)
         */
        @Override
        public Either<ST, PT> secondaryPeek(Consumer<? super ST> action) {
            return lazy(Eval.later(() -> resolve().secondaryPeek(action)));
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.aol.cyclops.sum.types.Either#peek(java.util.function.Consumer)
         */
        @Override
        public Either<ST, PT> peek(Consumer<? super PT> action) {
            return lazy(Eval.later(() -> resolve().peek(action)));
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.aol.cyclops.sum.types.Either#swap()
         */
        @Override
        public Either<PT, ST> swap() {
            return lazy(Eval.later(() ->  resolve()
                                             .swap()));
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.aol.cyclops.sum.types.Either#toIor()
         */
        @Override
        public Ior<ST, PT> toIor() {
            return trampoline()
                       .toIor();
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.aol.cyclops.sum.types.Either#visit(java.util.function.Function,
         * java.util.function.Function)
         */
        @Override
        public <R> R visit(Function<? super ST, ? extends R> secondary, Function<? super PT, ? extends R> primary) {
            return trampoline()
                       .visit(secondary, primary);
        } 
        private Either<ST,PT> trampoline(){
            Either<ST,PT> maybe = lazy.get();
            while (maybe instanceof Lazy) {
                maybe = ((Lazy<ST,PT>) maybe).lazy.get();
            }
            return maybe;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.aol.cyclops.sum.types.Either#get()
         */
        @Override
        public PT get() {

            return trampoline()
                       .get();
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.aol.cyclops.sum.types.Either#secondaryGet()
         */
        @Override
        public ST secondaryGet() {
            return trampoline()
                       .secondaryGet();
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.aol.cyclops.sum.types.Either#secondaryToOptional()
         */
        @Override
        public Optional<ST> secondaryToOptional() {
            return trampoline()
                       .secondaryToOptional();
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.aol.cyclops.sum.types.Either#secondaryToStream()
         */
        @Override
        public ReactiveSeq<ST> secondaryToStream() {
            return ReactiveSeq.generate(() -> trampoline()
                                                  .secondaryToStream())
                              .flatMap(Function.identity());
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.aol.cyclops.sum.types.Either#secondaryFlatMap(java.util.function.
         * Function)
         */
        @Override
        public <LT1> Either<LT1, PT> secondaryFlatMap(Function<? super ST, ? extends Xor<LT1, PT>> mapper) {
            return lazy(Eval.later(() -> resolve()
                                             .secondaryFlatMap(mapper)));
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.aol.cyclops.sum.types.Either#secondaryToPrimayFlatMap(java.util.
         * function.Function)
         */
        @Override
        public Either<ST, PT> secondaryToPrimayFlatMap(Function<? super ST, ? extends Xor<ST, PT>> fn) {
            return lazy(Eval.later(() -> resolve().secondaryToPrimayFlatMap(fn)));
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.aol.cyclops.sum.types.Either#peek(java.util.function.Consumer,
         * java.util.function.Consumer)
         */
        @Override
        public void peek(Consumer<? super ST> stAction, Consumer<? super PT> ptAction) {
            trampoline()
                .peek(stAction, ptAction);

        }

        /*
         * (non-Javadoc)
         * 
         * @see com.aol.cyclops.sum.types.Either#isRight()
         */
        @Override
        public boolean isRight() {
            return trampoline()
                       .isRight();
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.aol.cyclops.sum.types.Either#isLeft()
         */
        @Override
        public boolean isLeft() {
            return trampoline()
                       .isLeft();
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.aol.cyclops.sum.types.Either#combine(com.aol.cyclops.types.Value,
         * java.util.function.BiFunction)
         */
        @Override
        public <T2, R> Either<ST, R> combine(Value<? extends T2> app,
                BiFunction<? super PT, ? super T2, ? extends R> fn) {
            return lazy(Eval.later(() -> trampoline()
                                             .combine(app, fn)));
        }


        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return trampoline().hashCode();
            
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(final Object obj) {

            return trampoline().equals(obj);
        }
        @Override
        public String toString(){
            return trampoline().toString();
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class Right<ST, PT> implements Either<ST, PT> {
        private final Eval<PT> value;

        @Override
        public Either<ST, PT> secondaryToPrimayMap(final Function<? super ST, ? extends PT> fn) {
            return this;
        }

        @Override
        public <R> Either<R, PT> secondaryMap(final Function<? super ST, ? extends R> fn) {
            return (Either<R, PT>) this;
        }

        @Override
        public <R> Either<ST, R> map(final Function<? super PT, ? extends R> fn) {
            return new Right<ST, R>(
                                    value.map(fn));
        }

        @Override
        public Either<ST, PT> secondaryPeek(final Consumer<? super ST> action) {
            return this;
        }

        @Override
        public Either<ST, PT> peek(final Consumer<? super PT> action) {
            return map(i -> {
                action.accept(i);
                return i;
            });

        }

        @Override
        public Either<ST, PT> filter(final Predicate<? super PT> test) {

            return flatMap(i -> test.test(i) ? this : new Left<ST,PT>(
                                                               Eval.now(null)));

        }

        @Override
        public Either<PT, ST> swap() {
            return new Left<PT, ST>(
                                    value);
        }

        @Override
        public PT get() {
            return value.get();
        }

        @Override
        public ST secondaryGet() {
            return null;
        }

        @Override
        public Optional<ST> secondaryToOptional() {
            return Optional.empty();
        }

        @Override
        public ReactiveSeq<ST> secondaryToStream() {
            return ReactiveSeq.empty();
        }

        @Override
        public < RT1> Either<ST, RT1> flatMap(
                final Function<? super PT, ? extends MonadicValue<? extends RT1>> mapper) {

            Eval<? extends Either<? extends ST,  ? extends RT1>> ret = value.map(mapper.andThen(Either::fromMonadicValue));
          
            
           final Eval<Either<ST, RT1>> e3 =  (Eval<Either<ST,  RT1>>)ret;
           return new Lazy<>(
                             e3);

        }

        @Override
        public <LT1> Either<LT1, PT> secondaryFlatMap(
                final Function<? super ST, ? extends Xor<LT1, PT>> mapper) {
            return (Either<LT1, PT>) this;
        }

        @Override
        public Either<ST, PT> secondaryToPrimayFlatMap(final Function<? super ST, ? extends Xor<ST, PT>> fn) {
            return this;
        }

        @Override
        public void peek(final Consumer<? super ST> stAction, final Consumer<? super PT> ptAction) {
            ptAction.accept(value.get());
        }

        @Override
        public boolean isRight() {
            return true;
        }

        @Override
        public boolean isLeft() {
            return false;
        }

        @Override
        public Value<ST> secondaryValue() {
            return Value.of(() -> null);
        }

        @Override
        public String toString() {
            return mkString();
        }

        @Override
        public String mkString() {
            return "Either.right[" + value.get() + "]";
        }

        @Override
        public Ior<ST, PT> toIor() {
            return Ior.primary(value.get());
        }

        @Override
        public <R> R visit(final Function<? super ST, ? extends R> secondary,
                final Function<? super PT, ? extends R> primary) {
            return primary.apply(value.get());
        }



        /*
         * (non-Javadoc)
         * 
         * @see com.aol.cyclops.types.applicative.ApplicativeFunctor#ap(com.aol.
         * cyclops.types.Value, java.util.function.BiFunction)
         */
        @Override
        public <T2, R> Either<ST, R> combine(final Value<? extends T2> app,
                final BiFunction<? super PT, ? super T2, ? extends R> fn) {
            return app.toXor()
                      .visit(s -> Either.left(null), f -> Either.right(fn.apply(get(), app.get())));
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return value.get().hashCode();
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            
           
            if(obj instanceof Lazy){
                return ((Lazy)obj).equals(this);
            }
            if(obj instanceof Primary){
                return value.equals(((Primary)obj).get());
            }
            if (getClass() != obj.getClass())
                return false;
            Right other = (Right) obj;
            if (value == null) {
                if (other.value != null)
                    return false;
            } else if (!value.equals(other.value))
                return false;
            return true;
        }
        
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class Left<ST, PT> implements Either<ST, PT> {
        private final Eval<ST> value;

        @Override
        public boolean isLeft() {
            return true;
        }

        @Override
        public boolean isRight() {
            return false;
        }



        @Override
        public Either<ST, PT> secondaryToPrimayMap(final Function<? super ST, ? extends PT> fn) {
            return new Right<ST, PT>(
                                     value.map(fn));
        }

        @Override
        public <R> Either<R, PT> secondaryMap(final Function<? super ST, ? extends R> fn) {
            return new Left<R, PT>(
                                   value.map(fn));
        }

        @Override
        public <R> Either<ST, R> map(final Function<? super PT, ? extends R> fn) {
            return (Either<ST, R>) this;
        }

        @Override
        public Either<ST, PT> secondaryPeek(final Consumer<? super ST> action) {
            return secondaryMap((Function) FluentFunctions.expression(action));
        }

        @Override
        public Either<ST, PT> peek(final Consumer<? super PT> action) {
            return this;
        }

        @Override
        public Either<ST, PT> filter(final Predicate<? super PT> test) {
            return this;
        }

        @Override
        public Either<PT, ST> swap() {
            return new Right<PT, ST>(
                                     value);
        }

        @Override
        public PT get() {
            throw new NoSuchElementException();
        }

        @Override
        public ST secondaryGet() {
            return value.get();
        }

        @Override
        public Optional<ST> secondaryToOptional() {
            return Optional.ofNullable(value.get());
        }

        @Override
        public ReactiveSeq<ST> secondaryToStream() {
            return ReactiveSeq.fromStream(Streams.optionalToStream(secondaryToOptional()));
        }

        @Override
        public <RT1> Either<ST, RT1> flatMap(
                final Function<? super PT, ? extends MonadicValue<? extends RT1>> mapper) {
            return (Either<ST, RT1>) this;
        }

        @Override
        public <LT1> Either<LT1, PT> secondaryFlatMap(
                final Function<? super ST, ? extends Xor<LT1, PT>> mapper) {
            Eval<? extends MonadicValue<? extends PT>> ret = value.map(mapper);
            Eval<? extends Either<? extends LT1,  ? extends PT>> et = ret.map(Either::fromMonadicValue);
            
           final Eval<Either<LT1, PT>> e3 =  (Eval<Either<LT1,  PT>>)et;
           return new Lazy<>(
                             e3);
           
        }

        @Override
        public Either<ST, PT> secondaryToPrimayFlatMap(final Function<? super ST, ? extends Xor<ST, PT>> fn) {
            return new Lazy<ST, PT>(
                    Eval.now(this)).secondaryToPrimayFlatMap(fn);
        }

        @Override
        public void peek(final Consumer<? super ST> stAction, final Consumer<? super PT> ptAction) {
            stAction.accept(value.get());

        }

        @Override
        public <R> R visit(final Function<? super ST, ? extends R> secondary,
                final Function<? super PT, ? extends R> primary) {
            return secondary.apply(value.get());
        }

        @Override
        public Maybe<PT> toMaybe() {
            return Maybe.none();
        }

        @Override
        public Optional<PT> toOptional() {
            return Optional.empty();
        }

        @Override
        public Value<ST> secondaryValue() {
            return value;
        }

        @Override
        public String toString() {
            return mkString();
        }

        @Override
        public String mkString() {
            return "Either.left[" + value.get() + "]";
        }



        @Override
        public Ior<ST, PT> toIor() {
            return Ior.secondary(value.get());
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.aol.cyclops.types.applicative.ApplicativeFunctor#ap(com.aol.
         * cyclops.types.Value, java.util.function.BiFunction)
         */
        @Override
        public <T2, R> Either<ST, R> combine(final Value<? extends T2> app,
                final BiFunction<? super PT, ? super T2, ? extends R> fn) {
            return (Either<ST, R>) this;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return value.hashCode();
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if(obj instanceof Lazy){
                return ((Lazy)obj).equals(this);
            }
            if(obj instanceof Secondary){
                return value.equals(((Primary)obj).get());
            }
            if (getClass() != obj.getClass())
                return false;
            Left other = (Left) obj;
            if (value == null) {
                if (other.value != null)
                    return false;
            } else if (!value.equals(other.value))
                return false;
            return true;
        }

        
        
    }

}