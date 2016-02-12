package com.aol.cyclops.internal.stream.operators;

import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Value;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;

import com.aol.cyclops.util.stream.Streamable;

@Value
public class MultiCollectOperator<T> {
	Stream<T> stream;
	public  List collect(Streamable<Collector> collectors){
	
		
		final Supplier supplier =  ()-> collectors.stream().map(c->c.supplier().get()).collect(Collectors.toList());
		final BiConsumer accumulator = (acc,next) -> {  Seq.seq(collectors.stream().iterator()).<Object,Tuple2<Collector,Object>>zip(
																Seq.seq((List)acc),(a,b)->new Tuple2<Collector,Object>(a,b))
													
													.forEach( t -> t.v1().accumulator().accept(t.v2(),next));
		};
		final BinaryOperator combiner = (t1,t2)->  {
			Iterator t1It = ((Iterable)t1).iterator();
			Iterator t2It =  ((Iterable)t2).iterator();
			return collectors.stream().map(c->c.combiner().apply(t1It.next(),t2It.next())).collect(Collectors.toList());
		};
		Function finisher = t1 -> {
			Iterator t1It = ((Iterable)t1).iterator();
			return collectors.stream().map(c->c.finisher().apply(t1It.next())).collect(Collectors.toList());
		};
		 Collector col = Collector.of( supplier,accumulator , combiner,finisher);
			
		 return (List)stream.collect(col);
	}
}
