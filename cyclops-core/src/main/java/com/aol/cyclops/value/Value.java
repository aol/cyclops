package com.aol.cyclops.value;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import com.aol.cyclops.closures.Convertable;
import com.aol.cyclops.collections.extensions.persistent.PBagX;
import com.aol.cyclops.collections.extensions.persistent.POrderedSetX;
import com.aol.cyclops.collections.extensions.persistent.PQueueX;
import com.aol.cyclops.collections.extensions.persistent.PSetX;
import com.aol.cyclops.collections.extensions.persistent.PStackX;
import com.aol.cyclops.collections.extensions.persistent.PVectorX;
import com.aol.cyclops.collections.extensions.standard.DequeX;
import com.aol.cyclops.collections.extensions.standard.ListX;
import com.aol.cyclops.collections.extensions.standard.QueueX;
import com.aol.cyclops.collections.extensions.standard.SetX;
import com.aol.cyclops.collections.extensions.standard.SortedSetX;
import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.Ior;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.lambda.monads.Foldable;
import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.trycatch.Failure;
import com.aol.cyclops.trycatch.Success;
import com.aol.cyclops.trycatch.Try;


import lombok.AllArgsConstructor;

public interface Value<T> extends Supplier<T>, Foldable<T>, ValueObject, Convertable<T> {

	 public static <T> Value<T> of(Supplier<T> supplier){
		 return new ValueImpl<>(supplier);
	 }
	 @AllArgsConstructor
	 public static class ValueImpl<T> implements Value<T>{
		 private final Supplier<T> delegate;
		 
		 public T get(){
			 return delegate.get();
		 }

		

		@Override
		public Iterator<T> iterator() {
			return stream().iterator();
		}
	 }
	 
	default SequenceM<T> stream() {
			return SequenceM.of(get()).filter(v -> v != null);
	}
	 /**
	  * @return matchable
	  */
	 default Object getMatchable(){
		return get();
	 }
	 default <I extends Iterable<?>> I unapply(){
		 return (I)Arrays.asList(get());
	 }
	 default SequenceM<T> iterate(UnaryOperator<T> fn){
			return SequenceM.iterate(get(),fn);
	 }
	 default SequenceM<T> generate(){
		return SequenceM.generate(this);
	 }
	 default <E> E mapReduce(Monoid<E> monoid){
		 return monoid.mapReduce(toStream());
	 }
	 default T fold(Monoid<T> monoid){
		 return monoid.reduce(toStream());
	 }
	 default  T fold(T identity,BinaryOperator<T> accumulator){
		return accumulator.apply(identity, get());
	 }
	 
	 default <ST>  Xor<ST,T> toXor(){
		 return Xor.primary(get());
	 }
	 default <PT>  Xor<T,PT> toXorSecondary(){
		 return Xor.secondary(get());
	 }
	 default Try<T,NoSuchElementException> toTry(){
		 Optional<T> opt = toOptional();
		 if(opt.isPresent())
			 return Success.of(opt.get());
		 return Failure.of(new NoSuchElementException());
	 }
	 default <X extends Throwable> Try<T,X> toTry(Class<X>... classes){
		 return Try.withCatch( ()->get(),classes);
	 }
	 default <ST>  Ior<ST,T> toIor(){
		 return Ior.primary(get());
	 }
	 default <PT>  Ior<T,PT> toIorSecondary(){
		 return Ior.secondary(get());
	 }
	 default Eval<T> toEvalNow(){
		 return Eval.now(get());
	 }
	 default Eval<T> toEvalLater(){
		 return Eval.later(this);
	 }
	 default Eval<T> toEvalAlways(){
		 return Eval.always(this);
	 }
	 default Maybe<T> toMaybe(){
		 return Maybe.fromOptional(toOptional());
	 }
	 default ListX<T> toListX(){
		 return ListX.fromIterable(toList());
	 }
	 default SetX<T> toSetX(){
		 return SetX.fromIterable(toList());
	 }
	 default SortedSetX<T> toSortedSetX(){
		 return SortedSetX.fromIterable(toList());
	 }
	 default QueueX<T> toQueueX(){
		 return QueueX.fromIterable(toList());
	 }
	 default DequeX<T> toDequeX(){
		 return DequeX.fromIterable(toList());
	 }
	 default PStackX<T> toPStackX(){
		 return PStackX.fromCollection(toList());
	 }
	 default PVectorX<T> toPVectorX(){
		 return PVectorX.fromCollection(toList());
	 }
	 default PQueueX<T> toPQueueX(){
		 return PQueueX.fromCollection(toList());
	 }
	 default PSetX<T> toPSetX(){
		 return PSetX.fromCollection(toList());
	 }
	 default POrderedSetX<T> toPOrderedSetX(){
		 return POrderedSetX.fromCollection(toList());
	 }
	 default PBagX<T> toPBagX(){
		 return PBagX.fromCollection(toList());
	 }
	 
	 
}
