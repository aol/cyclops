package com.aol.cyclops.streams.tailrec;

import java.util.Spliterator;
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
import com.aol.cyclops.streams.tailrec.SpliteratorModule.RecursiveOperator;

@Wither
@AllArgsConstructor
public class TailRec<T> {

	
	@Getter
	private final Stream<T> stream;
	
	public TailRec(Spliterator<T> split){
		stream = stream(split);
	}
	
	public static<T> TailRec<T> iterate(final T seed, final UnaryOperator<T> f){
		return new TailRec<T>(Stream.iterate(seed, f));
	}
	public void forEach(Consumer<? super T> consumer){
		SequenceM.fromIterator(stream.iterator()).forEach(consumer);
	}
	
	public <R> TailRec<R> headTail(BiFunction<? super T, ? super TailRec<T>, ? extends TailRec<R>> mapper) {
        

        return new TailRec<>( stream(new RecursiveOperator<T, R>(stream.spliterator(), mapper)));
        
    }
	public TCO<T> tailRec(){
		return new TCO<>(stream);
	}
	public TailRec<T> prepend(T value){
		 return new TailRec<>(Stream.concat(Stream.of(value), stream));
	    //    return this.withStream(stream(new SpliteratorModule.PrependSpliterator<T>(stream.spliterator(), value)));
	}
	
	public TailRec<T> filter(Predicate<? super T> predicate) {
	        return withStream(stream.filter(predicate));
	}
	
	private <U> Stream<U> stream(Spliterator<U> spliterator){
		return StreamSupport.stream(spliterator, false);
	}
}
