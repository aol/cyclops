package com.aol.cyclops.streams.operators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aol.cyclops.collections.extensions.standard.ListX;
import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.streams.StreamUtils;

import lombok.Value;
@Value
public class MultiReduceOperator<R> {

	Stream<R> stream;
	public ListX<R> reduce(Iterable<? extends Monoid<R>> reducers){
		Monoid<R> m = new Monoid(){
			public List zero(){
				return StreamUtils.stream(reducers).map(r->r.zero()).collect(ListX.toListX());
			}
			public BiFunction<List,List,List> combiner(){
				return (c1,c2) -> { 
					List l= new ArrayList<>();
					int i =0;
					for(Monoid next : reducers){
						l.add(next.combiner().apply(c1.get(i),c2.get(0)));
						i++;
					}
					
					
					return l;
				};
			}
			
		
			public Stream mapToType(Stream stream){
				return (Stream) stream.map(value->Arrays.asList(value));
			}
		};
		return (ListX)m.mapReduce(stream);
	}
}
