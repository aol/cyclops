package com.aol.cyclops.streams.tailrec;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;

import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.streams.tailrec.IteratorModule.RecursiveOperator;

@Wither
@AllArgsConstructor
public class TailRecIt<T> {

	
	@Getter
	private final Stream<T> stream;
	
	public TailRecIt(Iterator<T> split){
		stream = stream(split);
	}
	
	public Iterator<T> iterator(){
		return stream.iterator();
	}
	public static<T> TailRecIt<T> iterate(final T seed, final UnaryOperator<T> f){
		return new TailRecIt<T>(Stream.iterate(seed, f));
	}
	public void forEach(Consumer<? super T> consumer){
		SequenceM.fromIterator(stream.iterator()).forEach(consumer);
	}
	
	public <R> TailRecIt<R> headTail(BiFunction<? super T, ? super TailRecIt<T>, ? extends TailRecIt<R>> mapper) {
        

        return new TailRecIt<>( stream(new RecursiveOperator<T, R>(stream.iterator(), mapper)));
        
    }
	public TCO<T> tailRec(){
		return new TCO<>(stream);
	}
	public TailRecIt<T> prepend(T value){
		 return new TailRecIt<>(Stream.concat(Stream.of(value), stream));
	    //    return this.withStream(stream(new SpliteratorModule.PrependSpliterator<T>(stream.spliterator(), value)));
	}
	
	public TailRecIt<T> filter(Predicate<? super T> predicate) {
	        return withStream(stream.filter(predicate));
	}
	
	private <U> Stream<U> stream(Iterator<U> iterator){
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator,  Spliterator.ORDERED),false);
	}
}
