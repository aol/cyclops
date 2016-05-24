package com.aol.cyclops.util;

import static com.aol.cyclops.control.AnyM.fromCompletableFuture;
import static java.util.concurrent.CompletableFuture.completedFuture;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;

import com.aol.cyclops.Reducer;
import com.aol.cyclops.Semigroup;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;

public class CompletableFutures {

	public static <T> CompletableFuture<ListX<T>> sequence(CollectionX<CompletableFuture<T>> fts){
	    return sequence(fts.stream()).thenApply(s->s.toListX());
	}
	
	public static <T> CompletableFuture<ReactiveSeq<T>> sequence(Stream<CompletableFuture<T>> fts){
	    return AnyM.sequence(fts.map(f->fromCompletableFuture(f)),
                ()->AnyM.fromCompletableFuture(completedFuture(Stream.<T>empty())))
	                .map(s->ReactiveSeq.fromStream(s))
	                .unwrap();
       
    }
    
	public static <T,R> CompletableFuture<R> accumulate(CollectionX<CompletableFuture<T>> fts,Reducer<R> reducer){
		return sequence(fts).thenApply(s->s.mapReduce(reducer));
	}
	public static <T,R> CompletableFuture<R> accumulate(CollectionX<CompletableFuture<T>> fts,Function<? super T, R> mapper,Semigroup<R> reducer){
		return sequence(fts).thenApply(s->s.map(mapper).reduce(reducer.reducer()).get());
	}
	/**
	
	public static void main(String[] args){
	//   System.out.println(CompletableFutures.cfSequence(Stream.of(CompletableFuture.completedFuture(1),CompletableFuture.completedFuture(2), 
	 //                           new CompletableFuture<Integer>())));
	//    System.out.println(CompletableFutures.listSequence(Stream.of(Arrays.asList(1),Arrays.asList(8))).get(0).collect(Collectors.toList()));
	   System.out.println(CompletableFutures.listSequence(Stream.of(Arrays.asList(1,2,3),Arrays.asList(8), Arrays.asList(10,20))).map(s->s.collect(Collectors.toList())));
	   
	    
	}
	
	public static <T> CompletableFuture<Stream<T>> sequence4(Stream<CompletableFuture<T>> fts){
        return generic(fts.map(f->AnyM.fromCompletableFuture(f))).unwrap();
    }
	public static <T> CompletableFuture<Stream<T>> cfSequence(Stream<CompletableFuture<T>> fts){
        return genericSequence(fts.map(f->fromCompletableFuture(f)),
                    ()->AnyM.fromCompletableFuture(completedFuture(Stream.<T>empty()))).unwrap();
    }
	
	public static <T> ListX<Stream<T>> listSequence(Stream<List<T>> fts){
	   
        List<Stream<T>> list = genericSequence(fts.map(l->AnyM.fromList(l)),
                    ()->AnyM.fromList(Arrays.asList(Stream.<T>empty()))).unwrap();
        return ListX.fromIterable(list);
    }
	public static void listTExamples(){
	    
	    
	    ReactiveSeq<List<Integer>> stream = ReactiveSeq.of(ListX.of(1,2,3),ListX.of(10,20,30));
        
        ListTSeq<Integer> list = ListT.fromStream(stream);
        
        ListTSeq<Integer> doubled = list.map(i->i*2);
        
        ReactiveSeq<Integer> sums = doubled.reduce(0, (acc,next)-> acc+next).stream();
        //[12,120]
        
        
        
        
        
        sums.allMatch(i->true);
	}
	
	public static <T> ListT<Stream<T>> listTSequence(Stream<ListT<T>> fts){
	
	    return genericSequence(fts.map(l->AnyM.fromListT(l)),
                    ()->AnyM.fromListT(ListT.fromIterable(Arrays.asList(Arrays.asList(Stream.<T>empty()))))).unwrap();
       
    }
	/**

	public static <T> AnyM<Stream<T>> genericSequence(Stream<AnyM<T>> source, Supplier<AnyM<Stream<T>>> unitEmpty) {
	    return  source.reduce(unitEmpty.get(),
                        (fl, fo) ->   fl.flatMapFirst(a->{
                                 Streamable<T> streamable= Streamable.fromStream(a);
                                 return fo.map(b->Stream.concat(streamable.stream(),Stream.of(b)));
                               })
                        ,
                        (fa, fb) -> fa.flatMapFirst(a-> fb.map(b-> Stream.concat(a,b))));
    }
  
	public static <T> AnyM<Stream<T>> generic(Stream<AnyM<T>> source) {
      
        
        return  (AnyM<Stream<T>>) source
                .reduce((AnyM<Stream<T>>)AnyM.fromCompletableFuture(CompletableFuture.completedFuture(Stream.<T>empty())),
                        (fl, fo) ->{ 
                           
                            AnyM<Stream<String>> any = fo.map(i->Stream.of("hello"));
                            Function<Stream<T>,AnyM<Stream<T>>> f = (Stream<T> a)->fo.map((T b)->Stream.concat(a,Stream.of(b)));
                            AnyM<Stream<T>> stream =  fl.flatMapFirst(f);
                            return stream;
                        },
                        (fa, fb) -> fa.flatMapFirst(a-> fb.map(b-> Stream.concat(a,b))));
    }

     **/
}
