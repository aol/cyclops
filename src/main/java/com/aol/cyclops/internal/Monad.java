package com.aol.cyclops.internal;

import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Streamable;
import com.aol.cyclops.data.Mutable;
import com.aol.cyclops.internal.comprehensions.comprehenders.StreamComprehender;
import com.aol.cyclops.internal.monads.ComprehenderSelector;
import com.aol.cyclops.internal.monads.MonadWrapper;
import com.aol.cyclops.internal.stream.SeqUtils;
import com.aol.cyclops.types.Filterable;
import com.aol.cyclops.types.Functor;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.types.anyM.Witness;
import com.aol.cyclops.types.anyM.WitnessType;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.extensability.ValueComprehender;

/**
 * An interoperability Trait that encapsulates java Monad implementations.
 * 
 * A generalised view into Any Monad (that implements flatMap or bind and accepts any function definition
 * with an arity of 1). Operates as a  Monad Monad (yes two Monads in a row! - or a Monad that encapsulates and operates on Monads).
 * 
 * NB the intended use case is to wrap already existant Monad-like objects from diverse sources, to improve
 * interoperability - it's not intended for use as an interface to be implemented on a Monad class.
 * 
 * @author johnmcclean
 *
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public interface Monad<T> extends Functor<T>, Filterable<T> {

    public <T> Monad<T> withMonad(Object invoke);

    

    /**
     * Transform the contents of a Monad into a Monad wrapping a Stream e.g.
     * Turn an <pre>{@code Optional<List<Integer>>  into Stream<Integer> }</pre>
     * 
     * <pre>{@code
     * List<List<Integer>> list = monad(Optional.of(Arrays.asList(1,2,3,4,5,6)))
    										.<Stream<Integer>,Integer>streamedMonad()
    										.grouped(3)
    										.collect(Collectors.toList());
    	
    	
    	assertThat(list.get(0),hasItems(1,2,3));
    	assertThat(list.get(1),hasItems(4,5,6));
     * 
     * }</pre>
     * 
     * 
     * @return A Monad that wraps a Stream
     */
    default <NT> Monad<NT> streamedMonad() {
        final Stream stream = Stream.of(1);
        final Monad r = this.<T> withMonad((Stream) new ComprehenderSelector().selectComprehender(stream,adapter())
                                                                              .executeflatMap(stream, i -> unwrap()));
        return r.bind(e -> e);
    }

    /**
     * Unwrap this Monad into a Stream.
     * If the underlying monad is a Stream it is returned
     * Otherwise we flatMap the underlying monad to a Stream type
     */
    default Stream<T> stream() {
        if (unwrap() instanceof Stream)
            return (Stream) unwrap();
        if (unwrap() instanceof Iterable)
            return StreamSupport.stream(((Iterable) unwrap()).spliterator(), false);
        final Stream stream = Stream.of(1);
        return (Stream) withMonad((Stream) new ComprehenderSelector().selectComprehender(stream,adapter())
                                                                     .executeflatMap(stream, i -> unwrap())).unwrap();

    }

    /**
     * Convert to a Stream with the values repeated specified times
     * 
     * @param times Times values should be repeated within a Stream
     * @return Stream with values repeated
     */
    default Monad<T> cycle(final int times) {

        return fromStream(SeqUtils.cycle(times, Streamable.fromStream(stream())));

    }


    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Filterable#filter(java.util.function.Predicate)
     */
    default Monad<T> filter(final Predicate<? super T> fn) {
        
        final Object filterable = unwrap();
        if (filterable instanceof Filterable) {
            return withMonad((T) ((Filterable) filterable).filter(fn));
        }
        final T result = (T) new ComprehenderSelector().selectComprehender(unwrap(),adapter())
                                                       .filter(filterable, fn);
        return withMonad(result);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Functor#map(java.util.function.Function)
     */
    @Override
    default <R> Monad<R> map(final Function<? super T, ? extends R> fn) {
        final Object functor = unwrap();
        if (functor instanceof Functor) {
            final Functor f = (Functor) functor;

            return withMonad((R) f.map(fn));
        }
        final Object value = new ComprehenderSelector().selectComprehender(unwrap(),adapter())
                                                       .map(unwrap(), fn);

        return withMonad((R) value);

    }
   

    /**
     * Perform a looser typed flatMap / bind operation
     * The return type can be another type other than the host type
     * 
     * @param fn flatMap function
     * @return flatMapped monad
     */
    default <R> Monad<R> bind(final Function<? super T, ?> fn) {
        return withMonad(new ComprehenderSelector().selectComprehender(unwrap(),adapter())
                                                   .executeflatMap(unwrap(), fn));

    }
    public Comprehender<?> adapter();
    /**
     * Perform a bind operation (@see #bind) but also lift the return value into a Monad using configured
     * MonadicConverters
     * 
     * @param fn flatMap function
     * @return flatMapped monad
     */
    default <R> Monad<R> liftAndBind(final Function<? super T, ?> fn) {
        return withMonad(new ComprehenderSelector().selectComprehender(unwrap(),adapter())
                                                   .liftAndFlatMap(unwrap(), fn));

    }

    /**
     * join / flatten one level of a nested hierarchy
     * 
     * @return Flattened / joined one level
     */
    default <T1> Monad<T1> flatten() {
        return this.<T1> bind(t -> t instanceof AnyM ? (T1) ((AnyM) t).unwrap() : (T1) t);
    }

    default <R> Monad<R> flatMapToStream(final Function<Object, ? extends Stream<? extends R>> fn) {

        final Stream stream = Stream.of(1);
        final Monad r = this.<T> withMonad((Stream) new ComprehenderSelector().selectComprehender(stream,adapter())
                                                                              .executeflatMap(stream, i -> unwrap()));
        return r.bind(e -> e);

    }

    /**
     * Generate a new instance of the underlying monad with given value
     * 
     * @param value  to construct new instance with
     * @return new instance of underlying Monad
     */
    default <T> Object unit(final T value) {
        return new ComprehenderSelector().selectComprehender(unwrap(),adapter())
                                         .of(value);
    }

    default T get() {
        final Mutable<T> captured = Mutable.of(null);

        final Comprehender c = new ComprehenderSelector().selectComprehender(unwrap(),adapter());
        c.resolveForCrossTypeFlatMap(new ValueComprehender() {

            /* (non-Javadoc)
             * @see com.aol.cyclops.lambda.api.Comprehender#of(java.lang.Object)
             */
            @Override
            public Object of(final Object o) {
                return captured.set((T) o);
            }

            /* (non-Javadoc)
             * @see com.aol.cyclops.lambda.api.Comprehender#empty()
             */
            @Override
            public Object empty() {
                throw new NoSuchElementException();
            }

            @Override
            public Object map(final Object t, final Function fn) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Object flatMap(final Object t, final Function fn) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Class getTargetClass() {
                // TODO Auto-generated method stub
                return null;
            }

        }, unwrap());
        return captured.get();
    }


    Object unwrap();

    public <W extends WitnessType,T> AnyMValue<W,T> anyMValue();

    public <W extends WitnessType,T> AnyMSeq<W,T> anyMSeq();

    public <T> ReactiveSeq<T> sequence();

    /**
    public static <T> Monad<T> of(final Object o) {
        return new MonadWrapper(
                                o);
    }
**/
    default Monad<T> empty() {
        return (Monad) new ComprehenderSelector().selectComprehender(unwrap(),adapter())
                                                 .empty();
    }

    static <T> Monad<T> fromStream(final Stream<T> monad) {
        return new MonadWrapper<Witness.stream,T>(
                                  monad,(Comprehender<T>)StreamComprehender.INSTANCE);
    }

    /**
     * Apply function/s inside supplied Monad to data in current Monad
     * 
     * e.g. with Streams
     * <pre>
     * {@code 
     * 
     * AnyM<Integer> applied = AsAnyM.anyM(Stream.of(1,2,3))
     *                               .applyM(AsAnyM.anyM(Streamable.of( (Integer a)->a+1 ,(Integer a) -> a*2)));
    
        assertThat(applied.toSequence().toList(),equalTo(Arrays.asList(2, 2, 3, 4, 4, 6)));
     }</pre>
     * 
     * with Optionals 
     * 
     * <pre>{@code
     * 
     *  AnyM<Integer> applied = AsAnyM.anyM(Optional.of(2))
     *                                .applyM( AsAnyM.anyM(Optional.of( (Integer a)->a+1)) );
        
        assertThat(applied.toSequence().toList(),equalTo(Arrays.asList(3)));
        }
     * </pre>
     */
    default <R> Monad<R> applyM(final Monad<Function<? super T, ? extends R>> fn) {

        return (Monad) this.bind(v -> fn.map(innerFn -> innerFn.apply(v))
                                        .unwrap());

    }

    /**
     * 
     * Replicate given Monad
     * 
     * <pre>{@code 
     *  
     *   AnyM<Integer> applied =AsAnyM.anyM(Optional.of(2))
     *                                .replicateM(5);
     *                                
         assertThat(applied.unwrap(),equalTo(Optional.of(Arrays.asList(2,2,2,2,2))));
         
         }</pre>
     * 
     * 
     * @param times number of times to replicate
     * @return Replicated Monad
     */
    default <R> Monad<R> replicateM(final int times) {

        //This only works because of cross-type resolution which we are removing
        return (Monad) new MonadWrapper<>(
                                          unit(1),adapter()).flatten()
                                                  .bind(v -> cycle(times).unwrap());
    }

    /**
     * Perform a reduction where NT is a (native) Monad type
     * e.g. 
     * <pre>{@code 
     * Monoid<Optional<Integer>> optionalAdd = Monoid.of(Optional.of(0), (a,b)-> Optional.of(a.get()+b.get()));
        
        assertThat(AsAnyM.anyM(Stream.of(2,8,3,1)).reduceM(optionalAdd).unwrap(),equalTo(Optional.of(14)));
        }</pre>
     * 
     * 
    
    default <R> Monad<R> reduceM(final Monoid<R> reducer) {
        //  List(2, 8, 3, 1).foldLeftM(0) {binSmalls} -> Optional(14)
        //  convert to list Optionals

        return new MonadWrapper<>(
                                  Monad.fromStream(stream())
                                       .map(value -> new ComprehenderSelector().selectComprehender(reducer.zero()
                                                                                                          .getClass(),adapter())
                                                                               .of(value))
                                       .sequence()
                                       .reduce((Monoid) reducer),adapter());
    }
 */
}
