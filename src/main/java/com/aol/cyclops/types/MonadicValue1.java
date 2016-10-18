package com.aol.cyclops.types;

import java.util.function.Function;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.types.anyM.AnyMValue;
import java.util.Iterator;
import org.reactivestreams.Publisher;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.types.stream.reactive.ValueSubscriber;

public interface MonadicValue1<T> extends MonadicValue<T> {
    public <T> MonadicValue1<T> unit(T unit);

    <R> MonadicValue<R> map(Function<? super T, ? extends R> fn);

    default AnyMValue<T> anyM() {
        return AnyM.ofValue(this);
    }

    default <R> MonadicValue<R> coflatMap(Function<? super MonadicValue<T>, R> mapper) {
        return mapper.andThen(r -> unit(r))
                     .apply(this);
    }

    /**
     * Eagerly combine two MonadicValues using the supplied monoid (@see ApplicativeFunctor for type appropraite i.e. lazy / async alternatives)
     * 
     * <pre>
     * {@code 
     * 
     *  Monoid<Integer> add = Monoid.of(1,Semigroups.intSum);
     *  Maybe.of(10).combineEager(add,Maybe.none());
     *  //Maybe[10]
     *  
     *  Maybe.none().combineEager(add,Maybe.of(10));
     *  //Maybe[10]
     *  
     *  Maybe.none().combineEager(add,Maybe.none());
     *  //Maybe.none()
     *  
     *  Maybe.of(10).combineEager(add,Maybe.of(10));
     *  //Maybe[20]
     *  
     *  Monoid<Integer> firstNonNull = Monoid.of(null , Semigroups.firstNonNull());
     *  Maybe.of(10).combineEager(firstNonNull,Maybe.of(10));
     *  //Maybe[10]
     * }</pre>
     * 
     * @param monoid
     * @param v2
     * @return
     */
    default MonadicValue1<T> combineEager(Monoid<T> monoid, MonadicValue<? extends T> v2) {
        return unit(this.<T> flatMap(t1 -> v2.map(t2 -> monoid.combiner()
                                                              .apply(t1, t2)))
                        .orElseGet(() -> this.orElseGet(() -> monoid.zero())));
    }

    <R> MonadicValue<R> flatMap(Function<? super T, ? extends MonadicValue<? extends R>> mapper);
    
	default <R> MonadicValue<R> flatMapIterable(Function<? super T, ? extends Iterable<? extends R>> mapper) {
		return this.flatMap(a -> {
			Iterator<? extends R> it = mapper.apply(a).iterator();
			if (it.hasNext()) {
				return unit(it.next());
			}
			return Maybe.none();
		});
	}

	default <R> MonadicValue<R> flatMapPublisher(Function<? super T, ? extends Publisher<? extends R>> mapper) {
		return this.flatMap(a -> {
			Publisher<? extends R> publisher = mapper.apply(a);
			ValueSubscriber<R> sub = ValueSubscriber.subscriber();
			publisher.subscribe(sub);
			Maybe<R> maybe = sub.toMaybe();
			return unit(maybe.get());
		});
	}
}
