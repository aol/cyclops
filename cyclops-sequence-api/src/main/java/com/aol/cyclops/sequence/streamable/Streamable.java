package com.aol.cyclops.sequence.streamable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;






import com.aol.cyclops.invokedynamic.InvokeDynamic;
import com.aol.cyclops.objects.AsDecomposable;
import com.aol.cyclops.sequence.ReversedIterator;
import com.aol.cyclops.sequence.SeqUtils;
import com.aol.cyclops.sequence.SequenceM;

/**
 * Represents something that can generate a Stream, repeatedly
 * 
 * @author johnmcclean
 *
 * @param <T> Data type for Stream
 */
public interface Streamable<T> extends Iterable<T>{

	default Iterator<T> iterator(){
		return stream().iterator();
	}
	default  Object getStreamable(){
		return this;
	}
	default SequenceM<T> reveresedSequenceM(){
		return SequenceM.fromStream(reveresedStream());
	}
	/**
	 * @return SequenceM from this Streamable
	 */
	default SequenceM<T> sequenceM(){
		return SequenceM.fromStream(stream());
	}
	default Stream<T> reveresedStream(){
		Object streamable = getStreamable();
		if(streamable instanceof List){
			return StreamSupport.stream(new ReversedIterator((List)streamable).spliterator(),false);
		}
		if(streamable instanceof Object[]){
			List arrayList = Arrays.asList((Object[])streamable);
			return StreamSupport.stream(new ReversedIterator(arrayList).spliterator(),false);
		}
		return SeqUtils.reverse(stream());
	}
	default boolean isEmpty(){
		return this.sequenceM().isEmpty();
	}
	/**
	 * @return New Stream
	 */
	default Stream<T> stream(){
		Object streamable = getStreamable();
		if(streamable instanceof Stream)
			return (Stream)streamable;
		if(streamable instanceof Iterable)
			return StreamSupport.stream(((Iterable)streamable).spliterator(), false);
		return  new InvokeDynamic().stream(streamable).orElseGet( ()->
								(Stream)StreamSupport.stream(AsDecomposable.asDecomposable(streamable)
												.unapply()
												.spliterator(),
													false));
	}
	
	/**
	 * (Lazily) Construct a Streamable from a Stream.
	 * 
	 * @param stream to construct Streamable from
	 * @return Streamable
	 */
	public static <T> Streamable<T> fromStream(Stream<T> stream){
		return AsStreamable.fromStream(stream);
	}
	/**
	 * (Lazily) Construct a Streamable from an Iterable.
	 * 
	 * @param iterable to construct Streamable from
	 * @return Streamable
	 */
	public static <T> Streamable<T> fromIterable(Iterable<T> iterable){
		return AsStreamable.fromIterable(iterable);
	}

	
	
	/**
	 * Construct a Streamable that returns a Stream
	 * 
	 * @param values to construct Streamable from
	 * @return Streamable
	 */
	public static<T> Streamable<T> of(T... values){
		
		return new Streamable<T>(){
			public Stream<T> stream(){
				return Stream.of(values);
			}
			public Object getStreamable(){
				return values;
			}
		};
	}
	public static <T> Streamable<T> empty(){
		return of();
	}
	default Streamable<T> tail(){
		return Streamable.fromStream(sequenceM().headAndTail().tail());
	}
	default T head(){
		return sequenceM().headAndTail().head();
	}
	
	default Streamable<T> appendAll(Streamable<T> t){
		return Streamable.fromStream(this.sequenceM().appendStream(t.sequenceM()));
	}
	default Streamable<T> remove(T t){
		return Streamable.fromStream( sequenceM().remove(t));
	}
	default Streamable<T> prepend(T t){
		return Streamable.fromStream( sequenceM().prepend(t));
	}
	default Streamable<T> distinct(){
		return Streamable.fromStream( sequenceM().distinct());
	}
	default <U> U foldLeft(U seed, BiFunction<U, ? super T, U> function) {
        return sequenceM().foldLeft(seed, function);
    }
	 default <U> U foldRight(U seed, BiFunction<? super T, U, U> function)  {
        return sequenceM().foldRight(seed, function);
    }
	default <R> Streamable<R> map(Function<? super T,? extends R> fn){
		return Streamable.fromStream(sequenceM().map(fn));
	}
	default  Streamable<T> peek(Consumer<? super T> fn){
		return Streamable.fromStream(sequenceM().peek(fn));
	}
	default  Streamable<T> filter(Predicate<? super T> fn){
		return Streamable.fromStream(sequenceM().filter(fn));
	}
	default <R> Streamable<R> flatMap(Function<? super T,Streamable<? extends R>> fn){
		return Streamable.fromStream(sequenceM().flatMap(i->fn.apply(i).sequenceM()));
	}
	default List<T> toList(){
		if(getStreamable() instanceof List)
			return (List)getStreamable();
		return sequenceM().toList();
	}
	default <C extends Collection<T>> C toCollection(Supplier<C> collectionFactory){
		
		return sequenceM().toCollection(collectionFactory);
	}
	
    default Streamable<Streamable<T>> permutations() {
        if (isEmpty()) {
            return Streamable.empty();
        } else {
            final Streamable<T> tail = tail();
            if (tail.isEmpty()) {
                return Streamable.of(this);
            } else {
                final Streamable<Streamable<T>> zero = Streamable.empty();
                return distinct().foldLeft(zero, (xs, x) -> {
                    final Function<Streamable<T>, Streamable<T>> prepend = l -> l.prepend(x);
                    return xs.appendAll(remove(x).permutations().map(prepend));
                });
            }
        }
    }
    
    
}
