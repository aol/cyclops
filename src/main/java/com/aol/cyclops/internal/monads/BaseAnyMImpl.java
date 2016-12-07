package com.aol.cyclops.internal.monads;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.internal.Monad;
import com.aol.cyclops.internal.comprehensions.comprehenders.MaterializedList;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.types.anyM.WitnessType;
import com.aol.cyclops.types.extensability.Comprehender;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * 
 * Wrapper for Any Monad type
 * @see AnyMonads companion class for static helper methods
 * 
 * @author johnmcclean
 *
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseAnyMImpl<W extends WitnessType,T> {

    protected final Monad<T> monad;
    protected final Class initialType;
    protected final Comprehender<T> adapter;

    public Comprehender<T> adapter(){
        return adapter;
    }
    public <R> R unwrap() {
        return (R) monad.unwrap();

    }
/**
    protected <R> AnyM<R> fromIterable(final Iterable<R> it) {
        if (it instanceof AnyM)
            return (AnyM<R>) it;
        return AnyM.fromIterable(it);
    }

    protected <R> AnyM<R> fromPublisher(final Publisher<R> it) {
        if (it instanceof AnyM)
            return (AnyM<R>) it;
        return AnyM.fromPublisher(it);
    }
**/
    public Monad monad() {
        return monad;
    }

    protected Monad<T> filterInternal(final Predicate<? super T> fn) {
        return monad.filter(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Functor#map(java.util.function.Function)
     */
    protected <R> Monad<R> mapInternal(final Function<? super T, ? extends R> fn) {
        return monad.map(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Functor#peek(java.util.function.Consumer)
     */
    protected Monad<T> peekInternal(final Consumer<? super T> c) {
        return (Monad<T>)monad.peek(c);
    }

    /**
     * Perform a looser typed flatMap / bind operation
     * The return type can be another type other than the host type
     * 
     * @param fn flatMap function
     * @return flatMapped monad
    */
    protected <R> Monad<R> bindInternal(final Function<? super T, ?> fn) {
        return monad.<R> bind(fn);

    }

    protected <R> Monad<R> flatMapInternal(final Function<? super T, ? extends AnyM<W,? extends R>> fn) {
        try {
            return monad.bind(in -> fn.apply(in)
                                      .unwrap())
                        .map(this::takeFirst);
        } catch (final GotoAsEmpty e) {
            return (Monad) monad.empty();
        }
    }

    private static class GotoAsEmpty extends RuntimeException {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        @Override
        public synchronized Throwable fillInStackTrace() {
            return null;
        }

    }

    private <T> T takeFirst(final Object o) {
        if (o instanceof MaterializedList) {
            if (((List) o).size() == 0)
                throw new GotoAsEmpty();
            return (T) ((List) o).get(0);
        }
        return (T) o;
    }

    /**
     * join / flatten one level of a nested hierarchy
     * 
     * @return Flattened / joined one level
     */
    protected <T1> Monad<T1> flattenInternal() {
        return monad.flatten();

    }

    abstract Xor<AnyMValue<W,T>, AnyMSeq<W,T>> matchable();

    /**
     * Aggregate the contents of this Monad and the supplied Monad 
     * 
     * <pre>{@code 
     * 
     * List<Integer> result = anyM(Stream.of(1,2,3,4))
     * 							.aggregate(anyM(Optional.of(5)))
     * 							.asSequence()
     * 							.toList();
    	
    	assertThat(result,equalTo(Arrays.asList(1,2,3,4,5)));
    	}</pre>
     * 
     * @param next Monad to aggregate content with
     * @return Aggregated Monad
     */
    protected AnyM<W,List<T>> aggregate(final AnyM<W,T> next) {

        return unit(Stream.concat(matchable().visit(value -> value.toSequence(), seq -> seq.stream()), next.matchable()
                                                                                                           .visit(value -> value.toSequence(),
                                                                                                                  seq -> seq.stream()))
                          .collect(Collectors.toList()));

    }

    public void forEach(final Consumer<? super T> action) {
        asSequence().forEach(action);
    }

    /**
     * Sequence the contents of a Monad.  e.g.
     * Turn an <pre>
     * 	{@code Optional<List<Integer>>  into Stream<Integer> }</pre>
     * 
     * <pre>{@code
     * List<Integer> list = anyM(Optional.of(Arrays.asList(1,2,3,4,5,6)))
    										.<Integer>toSequence(c->c.stream())
    										.collect(Collectors.toList());
    	
    	
    	assertThat(list,hasItems(1,2,3,4,5,6));
    	
     * 
     * }</pre>
     * 
     * @return A Sequence that wraps a Stream
     */
    public <NT> ReactiveSeq<NT> toReactiveSeq(final Function<? super T, ? extends Stream<? extends NT>> fn) {
        return monad.flatMapToStream((Function) fn)
                    .sequence();
    }

    /**
     *  <pre>{@code Optional<List<Integer>>  into Stream<Integer> }</pre>
     * Less type safe equivalent, but may be more accessible than toSequence(fn) i.e. 
     * <pre>
     * {@code 
     *    toSequence(Function<T,Stream<NT>> fn)
     *   }
     *   </pre>
     *  <pre>{@code
     * List<Integer> list = anyM(Optional.of(Arrays.asList(1,2,3,4,5,6)))
    										.<Integer>toSequence()
    										.collect(Collectors.toList());
    	
    	
    	
     * 
     * }</pre>
    
     * @return A Sequence that wraps a Stream
     */
    public <T> ReactiveSeq<T> toSequence() {
        return monad.streamedMonad()
                    .sequence();
    }

    /**
     * Wrap this Monad's contents as a Sequence without disaggreating it. .e.
     *  <pre>{@code Optional<List<Integer>>  into Stream<List<Integer>> }</pre>
     * If the underlying monad is a Stream it is returned
     * Otherwise we flatMap the underlying monad to a Stream type
     */
    public ReactiveSeq<T> asSequence() {
        return monad.sequence();

    }

   										

    public abstract <T> AnyM<W,T> unit(T value);

    public abstract <T> AnyM<W,T> empty();

  

    public ReactiveSeq<T> stream() {
        //	if(this.monad.unwrap() instanceof Stream){
        return asSequence();
        //	}
        //return this.<T>toSequence();
    }

    @Override
    public String toString() {
        return String.format("AnyM(%s)", monad);
    }

    public T get() {
        return monad.get();
    }

}