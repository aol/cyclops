package com.aol.cyclops.control;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.BaseStream;

import org.pcollections.ConsPStack;
import org.reactivestreams.Publisher;

import com.aol.cyclops.internal.comprehensions.donotation.DoComp0;
import com.aol.cyclops.internal.comprehensions.donotation.DoComp1;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.util.function.QuadFunction;
import com.aol.cyclops.util.function.TriFunction;

/**
 * For comprehensions
 * <pre>
 * {@code 
 *    For.Publishers.each2(Flux.range(1,10), 
                            i-> ReactiveSeq.iterate(i,a->a+1).limit(10),
                            Tuple::tuple)
                           .toListX();
 * }
 * </pre>
 * 
 * @author johnmcclean
 *
 */
public class For {

    public interface Publishers {

        /**
         * Perform a four level nested internal iteration over the provided Publishers
         * NB - avoid using traverse once Stream types as any parameter other than the first! (e.g. ReactiveSeq)
         * 
         * <pre>
         * {@code
         * each4(Flux.range(1,10), 
                  a-> ReactiveSeq.iterate(a,i->i+1).limit(10),
                  (a,b) -> Maybe.<Integer>of(a+b),
                  (a,b,c) -> Mono.<Integer>just(a+b+c),
                        Tuple::tuple)
                        .toListX()
         * 
         * }
         * </pre>
         * 
         * @param publisher top level publisher
         * @param publisher2 Nested publisher
         * @param publisher3 Nested publisher
         * @param publisher4 Nested publisher
         * @param yieldingFunction  Generates a result per combination
         * @return A sequential monad of the same type as the top level publisher, AnyMSeq also implements Publisher
         */
        static <T1, T2, T3, R1, R2, R3, R> AnyMSeq<R> each4(Publisher<? extends T1> publisher,
                Function<? super T1, ? extends Publisher<R1>> publisher2, BiFunction<? super T1, ? super R1, ? extends Publisher<R2>> publisher3,
                TriFunction<? super T1, ? super R1, ? super R2, ? extends Publisher<R3>> publisher4,
                QuadFunction<? super T1, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

            return For.publisher(publisher)
                      .publisher(a -> publisher2.apply(a))
                      .publisher(a -> b -> publisher3.apply(a, b))
                      .publisher(a -> b -> c -> publisher4.apply(a, b, c))
                      .yield4(yieldingFunction);

        }

        /**
         * Perform a four level nested internal iteration over the provided Publishers
         * NB - avoid using traverse once Stream types as any parameter other than the first! (e.g. ReactiveSeq)
         * 
         * <pre>
         * {@code 
         *  each4(Flux.range(1,10), 
                            a-> ReactiveSeq.iterate(a,i->i+1).limit(10),
                            (a,b) -> Maybe.<Integer>of(a+b),
                            (a,b,c) -> Mono.<Integer>just(a+b+c),
                            (a,b,c,d) -> a+b+c+d <100,
                                Tuple::tuple)
                            .toListX()
         * 
         * }
         * </pre>
         * 
         * @param publisher top level publisher
         * @param publisher2 Nested publisher
         * @param publisher3 Nested publisher
         * @param publisher4 Nested publisher
         * @param filterFunction A filtering function, keeps values where the predicate holds
         * @param yieldingFunction Generates a result per combination
         * @return A sequential monad of the same type as the top level publisher, AnyMSeq also implements Publisher
         */
        static <T1, T2, T3, R1, R2, R3, R> AnyMSeq<R> each4(Publisher<? extends T1> publisher,
                Function<? super T1, ? extends Publisher<R1>> publisher2, BiFunction<? super T1, ? super R1, ? extends Publisher<R2>> publisher3,
                TriFunction<? super T1, ? super R1, ? super R2, ? extends Publisher<R3>> publisher4,
                QuadFunction<? super T1, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                QuadFunction<? super T1, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

            return For.publisher(publisher)
                      .publisher(a -> publisher2.apply(a))
                      .publisher(a -> b -> publisher3.apply(a, b))
                      .publisher(a -> b -> c -> publisher4.apply(a, b, c))
                      .filter(a -> b -> c -> d -> filterFunction.apply(a, b, c, d))
                      .yield4(yieldingFunction);

        }

        /**
         * Perform a three level nested internal iteration over the provided Publishers
         * 
         * NB - avoid using traverse once Stream types as any parameter other than the first! (e.g. ReactiveSeq)
         * 
         * <pre>
         * {@code 
         * each3(Flux.range(1,10), 
                            a-> ReactiveSeq.iterate(a,i->i+1).limit(10),
                            (a,b) -> Maybe.<Integer>of(a+b),
                            Tuple::tuple).toListX();
         * 
         * }
         * </pre>
         * @param publisher top level publisher
         * @param publisher1 Nested publisher
         * @param publisher2 Nested publisher
         * @param yieldingFunction  Generates a result per combination
         * @return A sequential monad of the same type as the top level publisher, AnyMSeq also implements Publisher
         */
        static <T1, T2, R1, R2, R> AnyMSeq<R> each3(Publisher<? extends T1> publisher, Function<? super T1, ? extends Publisher<R1>> publisher1,
                BiFunction<? super T1, ? super R1, ? extends Publisher<R2>> publisher2,
                TriFunction<? super T1, ? super R1, ? super R2, ? extends R> yieldingFunction) {

            return For.publisher(publisher)
                      .publisher(a -> publisher1.apply(a))
                      .publisher(a -> b -> publisher2.apply(a, b))
                      .yield3(yieldingFunction);

        }

        /**
         * Perform a three level nested internal iteration over the provided Publishers
         * NB - avoid using traverse once Stream types as any parameter other than the first! (e.g. ReactiveSeq)
         * 
         * <pre>
         * {@code 
         * each3(Flux.range(1,10), 
                   a-> ReactiveSeq.iterate(a,i->i+1).limit(10),
                   (a,b) -> Maybe.<Integer>of(a+b),
                   (a,b,c) ->a+b+c<10,
                   Tuple::tuple).toListX();
         * }
         * </pre>
         * 
         * 
         * @param publisher top level publisher
         * @param publisher2 Nested publisher
         * @param publisher3 Nested publisher
         * @param filterFunction A filtering function
         * @param yieldingFunction  Generates a result per combination
         * @return A sequential monad of the same type as the top level publisher, AnyMSeq also implements Publisher
         */
        static <T1, T2, R1, R2, R> AnyMSeq<R> each3(Publisher<? extends T1> publisher, Function<? super T1, ? extends Publisher<R1>> publisher2,
                BiFunction<? super T1, ? super R1, ? extends Publisher<R2>> publisher3,
                TriFunction<? super T1, ? super R1, ? super R2, Boolean> filterFunction,
                TriFunction<? super T1, ? super R1, ? super R2, ? extends R> yieldingFunction) {

            return For.publisher(publisher)
                      .publisher(t -> publisher2.apply(t))
                      .publisher(a -> b -> publisher3.apply(a, b))
                      .filter(a -> b -> c -> filterFunction.apply(a, b, c))
                      .yield3(yieldingFunction);

        }

        /**
         * Perform a two level nested internal iteration over the provided Publishers
         * 
         * NB - avoid using traverse once Stream types as any parameter other than the first! (e.g. ReactiveSeq)
         * 
         * <pre>
         * {@code 
         *   each2(Flux.range(1,10), 
                   i-> ReactiveSeq.iterate(i,a->a+1).limit(10),
                   Tuple::tuple)
                   .toListX();
         * }
         * </pre>
         * 
         * @param publisher Top level publisher
         * @param publisher2 Nested publisher
         * @param yieldingFunction Generates a result per combination
         * @return  A sequential monad of the same type as the top level publisher, AnyMSeq also implements Publisher
         */
        static <T, R1, R> AnyMSeq<R> each2(Publisher<? extends T> publisher, Function<? super T, ? extends Publisher<R1>> publisher2,
                BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

            return For.publisher(publisher)
                      .publisher(t -> publisher2.apply(t))
                      .yield2(yieldingFunction);

        }

        /**
         * Perform a two level nested internal iteration over the provided Publishers
         * 
         * NB - avoid using traverse once Stream types as any parameter other than the first! (e.g. ReactiveSeq)
         * <pre>
         * {@code 
         *  each2(Flux.range(1,10), 
                          i-> ReactiveSeq.iterate(i,a->a+1).limit(10),
                          (a,b)->a+b<10,
                          Tuple::tuple).toListX();
         * }
         * </pre>
         * 
         * @param publisher  Top level publisher
         * @param publisher2 Nested publisher
         * @param filterFunction A filtering function
         * @param yieldingFunction Generates a result per combination
         * @return A sequential monad of the same type as the top level publisher, AnyMSeq also implements Publisher
         */
        static <T, R1, R> AnyMSeq<R> each2(Publisher<? extends T> publisher, Function<? super T, ? extends Publisher<R1>> publisher2,
                BiFunction<? super T, ? super R1, Boolean> filterFunction, BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

            return For.publisher(publisher)
                      .publisher(t -> publisher2.apply(t))
                      .filter(a -> b -> filterFunction.apply(a, b))
                      .yield2(yieldingFunction);

        }
    }

    public interface Values {

        /**
         * Perform a four level nested internal iteration over the provided MonadicValues
         * 
         * @param stream1
         *            Nested Stream to iterate over
         
         * @param yieldingFunction
         *            Function with pointers to the current element from both
         *            Streams that generates the new elements
         * @returnAnyM with elements generated via nested iteration
         */
        static <T1, T2, T3, R1, R2, R3, R> AnyMValue<R> each4(MonadicValue<? extends T1> monadicValue,
                Function<? super T1, ? extends MonadicValue<R1>> value2, BiFunction<? super T1, ? super R1, ? extends MonadicValue<R2>> value3,
                TriFunction<? super T1, ? super R1, ? super R2, ? extends MonadicValue<R3>> value4,
                QuadFunction<? super T1, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

            return AnyM.ofValue(For.iterable(monadicValue)
                                   .iterable(a -> value2.apply(a))
                                   .iterable(a -> b -> value3.apply(a, b))
                                   .iterable(a -> b -> c -> value4.apply(a, b, c))
                                   .yield4(yieldingFunction)
                                   .unwrap());

        }

        /**
         * Perform a four level nested internal iteration over the provided MonadicValues
         * 
         * @param stream1
         *            Nested Stream to iterate over
         * @param filterFunction
         *            Filter to apply over elements before passing non-filtered
         *            values to the yielding function
         * @param yieldingFunction
         *            Function with pointers to the current element from both
         *            Streams that generates the new elements
         * @returnAnyM with elements generated via nested iteration
         */
        static <T1, T2, T3, R1, R2, R3, R> AnyMValue<R> each4(MonadicValue<? extends T1> monadicValue,
                Function<? super T1, ? extends MonadicValue<R1>> value2, BiFunction<? super T1, ? super R1, ? extends MonadicValue<R2>> value3,
                TriFunction<? super T1, ? super R1, ? super R2, ? extends MonadicValue<R3>> value4,
                QuadFunction<? super T1, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                QuadFunction<? super T1, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

            return AnyM.ofValue(For.iterable(monadicValue)
                                   .iterable(a -> value2.apply(a))
                                   .iterable(a -> b -> value3.apply(a, b))
                                   .iterable(a -> b -> c -> value4.apply(a, b, c))
                                   .filter(a -> b -> c -> d -> filterFunction.apply(a, b, c, d))
                                   .yield4(yieldingFunction)
                                   .unwrap());

        }

        /**
         * Perform a three level nested internal iteration over the provided MonadicValues
         * 
         * @param stream1
         *            Nested Stream to iterate over
        
         * @param yieldingFunction
         *            Function with pointers to the current element from both
         *            Streams that generates the new elements
         * @returnAnyM with elements generated via nested iteration
         */
        static <T1, T2, R1, R2, R> AnyMValue<R> each3(MonadicValue<? extends T1> monadicValue,
                Function<? super T1, ? extends MonadicValue<R1>> value2, BiFunction<? super T1, ? super R1, ? extends MonadicValue<R2>> value3,
                TriFunction<? super T1, ? super R1, ? super R2, ? extends R> yieldingFunction) {

            return AnyM.ofValue(For.iterable(monadicValue)
                                   .iterable(a -> value2.apply(a))
                                   .iterable(a -> b -> value3.apply(a, b))
                                   .yield3(yieldingFunction)
                                   .unwrap());

        }

        /**
         * Perform a three level nested internal iteration over the provided MonadicValues
         * 
         * @param stream1
         *            Nested Stream to iterate over
         * @param filterFunction
         *            Filter to apply over elements before passing non-filtered
         *            values to the yielding function
         * @param yieldingFunction
         *            Function with pointers to the current element from both
         *            Streams that generates the new elements
         * @return AnyM with elements generated via nested iteration
         */
        static <T1, T2, R1, R2, R> AnyMValue<R> each3(MonadicValue<? extends T1> monadicValue,
                Function<? super T1, ? extends MonadicValue<R1>> value2, BiFunction<? super T1, ? super R1, ? extends MonadicValue<R2>> value3,
                TriFunction<? super T1, ? super R1, ? super R2, Boolean> filterFunction,
                TriFunction<? super T1, ? super R1, ? super R2, ? extends R> yieldingFunction) {

            return AnyM.ofValue(For.iterable(monadicValue)
                                   .iterable(a -> value2.apply(a))
                                   .iterable(a -> b -> value3.apply(a, b))
                                   .filter(a -> b -> c -> filterFunction.apply(a, b, c))
                                   .yield3(yieldingFunction)
                                   .unwrap());

        }

        /**
         * Perform a two level nested internal iteration over the provided MonadicValues
         * 
         * @param stream1
         *            Nested Stream to iterate over
         * @param filterFunction
         *            Filter to apply over elements before passing non-filtered
         *            values to the yielding function
         * @return AnyM with elements generated via nested iteration
         */
       
        static <T, R1, R> AnyMValue<R> each2(MonadicValue<? extends T> monadicValue, Function<? super T, MonadicValue<R1>> value2,
                BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

            return AnyM.ofValue(For.iterable(monadicValue)
                                   .iterable(a -> value2.apply(a))
                                   .yield2(yieldingFunction)
                                   .unwrap());

        }

        /**
         * Perform a two level nested internal iteration over the provided MonadicValues
         * 
         * @param stream1
         *            Nested Stream to iterate over
         * @param filterFunction
         *            Filter to apply over elements before passing non-filtered
         *            values to the yielding function
         * @param yieldingFunction
         *            Function with pointers to the current element from both
         *            Streams that generates the new elements
         * @return AnyM with elements generated via nested iteration
         */
        static <T, R1, R> AnyMValue<R> each2(MonadicValue<? extends T> monadicValue, Function<? super T, ? extends MonadicValue<R1>> value2,
                BiFunction<? super T, ? super R1, Boolean> filterFunction, BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

            return AnyM.ofValue(For.iterable(monadicValue)
                                   .iterable(a -> value2.apply(a))
                                   .filter(a -> b -> filterFunction.apply(a, b))
                                   .yield2(yieldingFunction)
                                   .unwrap());

        }

    }

    public static <T1> DoComp1<T1> reader(Reader<?, T1> reader) {
        return new DoComp0(
                           ConsPStack.empty()).reader(reader);
    }

    /**
     * Add a Iterable as next nested level in the comprehension
     * 
     * 
     * 
     * <pre>{@code   Do.add(iterable)
     				   .filter( -> i1>5)
    			  	   .yield( -> );
    						
    	}</pre>
     * 
     * 
     * @param o Defines next level in comprehension
     * @return Next stage in for comprehension builder
     */
    public static <T1> DoComp1<T1> iterable(Iterable<T1> o) {
        return new DoComp0(
                           ConsPStack.empty()).iterable(o);
    }

    public static <T1> DoComp1<T1> publisher(Publisher<T1> o) {
        return new DoComp0(
                           ConsPStack.empty()).publisher(o);
    }

    /**
     * Add a Stream as next nested level in the comprehension
     * 
     * 
     * 
     * <pre>{@code   Do.add(stream)
     				   .filter( -> i1>5)
    			  	   .yield( -> );
    						
    	}</pre>
     * 
     * 
     * @param o Defines next level in comprehension
     * @return Next stage in for comprehension builder
     */
    public static <T1> DoComp1<T1> stream(BaseStream<T1, ?> o) {
        return new DoComp0(
                           ConsPStack.empty()).stream(o);
    }

    /**
     * Add a Optional as next nested level in the comprehension
     * 
     * 
     * 
     * <pre>{@code   Do.add(optional)
     				   .filter( -> i1>5)
    			  	   .yield( -> );
    						
    	}</pre>
     * 
     * 
     * @param o Defines next level in comprehension
     * @return Next stage in for comprehension builder
     */
    public static <T1> DoComp1<T1> optional(Optional<T1> o) {
        return new DoComp0(
                           ConsPStack.empty()).optional(o);
    }

    /**
     * Add a CompletableFuture as next nested level in the comprehension
     * 
     * 
     * 
     * <pre>{@code   Do.add(completableFuture)
     				   .filter( -> i1>5)
    			  	   .yield( -> );
    						
    	}</pre>
     * 
     * 
     * @param o Defines next level in comprehension
     * @return Next stage in for comprehension builder
     */
    public static <T1> DoComp1<T1> future(CompletableFuture<T1> o) {
        return new DoComp0(
                           ConsPStack.empty()).future(o);
    }

    /**
     * Add a AnyM as next nested level in the comprehension
     * 
     * 
     * 
     * <pre>{@code   Do.add(anyM)
     				   .filter( -> i1>5)
    			  	   .yield( -> );
    						
    	}</pre>
     * 
     * 
     * @param o Defines next level in comprehension
     * @return Next stage in for comprehension builder
     */
    public static <T1> DoComp1<T1> anyM(AnyM<T1> o) {
        return new DoComp0(
                           ConsPStack.empty()).anyM(o);
    }

}
