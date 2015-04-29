package com.aol.cyclops.matcher;


import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;



@AllArgsConstructor(access=AccessLevel.PACKAGE)
public final class EmptyCase<T,R> implements Case<T,R,Function<T,R>>{
	@Getter
	private final Tuple2<Predicate<T>,Function<T,R>> pattern = Tuple.<Predicate<T>,Function<T,R>>tuple(t->false,input->null);
	@Getter
	private final boolean empty =true;
	
	@Override
	public  Optional<R> match(T value){
		return Optional.empty();
	}

}
