package com.aol.cyclops.lambda.monads;

import static com.aol.cyclops.internal.AsGenericMonad.asMonad;
import static com.aol.cyclops.internal.AsGenericMonad.monad;
import static com.aol.cyclops.lambda.api.AsAnyM.*;
import static com.aol.cyclops.lambda.api.AsAnyMList.collectionToAnyMList;
import static com.aol.cyclops.lambda.api.AsAnyMList.completableFutureToAnyMList;
import static com.aol.cyclops.lambda.api.AsAnyMList.optionalToAnyMList;
import static com.aol.cyclops.lambda.api.AsAnyMList.streamToAnyMList;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import lombok.val;

import org.junit.Test;

import com.aol.cyclops.internal.AsGenericMonad;
import com.aol.cyclops.internal.Monad;
import com.aol.cyclops.lambda.api.AsAnyMList;
import com.aol.cyclops.lambda.api.AsAnyM;
import com.aol.cyclops.lambda.api.Monoid;
import com.aol.cyclops.lambda.api.Reducers;
import com.aol.cyclops.lambda.api.Streamable;
import com.aol.cyclops.streams.StreamUtils;


public class MonadTest {

	 Optional<Integer> value = Optional.of(42);
	 Monad<Optional<Integer>,Integer> monadicValue = monad(value);
	 Function<Optional<Integer>,Monad<Optional<Integer>,Integer>> monadOf = input ->monad(input);
	 Function<Optional<Integer>,Monad<Optional<Integer>,Integer>> f = input -> monad(Optional.of(input.get()*5));
	 Function<Optional<Integer>,Monad<Optional<Integer>,Integer>> g = input -> monad(Optional.of(input.get()*50));
	  /**
     * Monad law 1, Left Identity
     *
     * From LYAHFGG [1] above: 
     *   The first monad law states that if we take a value, put it in a default context 
     *   with return and then feed it to a function by using >>=, it’s the same as just 
     *   taking the value and applying the function to it
     */
	 @Test
    public void satisfiesLaw1LeftIdentity() {
        assertThat( monad(value).monadFlatMap(f),
            equalTo(f.apply(value) ));
    }
 
    /**
     * Monad law 2, Right Identity
     *
     * From LYAHFGG [1] above: 
     *   The second law states that if we have a monadic value and we use >>= to feed 
     *   it to return, the result is our original monadic value.
     */
    @Test
    public void satisfiesLaw2RightIdentity() {
         assertThat(monadicValue.monadFlatMap(monadOf),
            equalTo(monadicValue));
    }
 
    /**
     * Monad law 3, Associativity
     *
     * From LYAHFGG [1] above: 
     *   The final monad law says that when we have a chain of monadic function 
     *   applications with >>=, it shouldn’t matter how they’re nested.
     */
    @Test
    public void satisfiesLaw3Associativity() {
    	assertThat(monadicValue.monadFlatMap(f).monadFlatMap(g)
            ,equalTo(monadicValue.monadFlatMap( input ->f.apply(input).monadFlatMap(g))));
    }
	
	@Test
	public void test() {
		val list = MonadWrapper.<Stream<Integer>,List<Integer>>of(Stream.of(Arrays.asList(1,3)))
				.flatMap(Collection::stream).unwrap()
				.map(i->i*2)
				.peek(System.out::println)
				.collect(Collectors.toList());
		assertThat(Arrays.asList(2,6),equalTo(list));
	}
	@Test
	public void testMixed() {
		
		List<Integer> list = MonadWrapper.<Stream<Integer>,List<Integer>>of(Stream.of(Arrays.asList(1,3),null))
				.bind(Optional::ofNullable)
				.map(i->i.size())
				.peek(System.out::println)
				.<Integer>sequence()
				.toList();
		assertThat(Arrays.asList(2),equalTo(list));
	}
	int count;
	@Test
	public void testCycleWhile(){
		count =0;
		assertThat(MonadWrapper.<Stream<Integer>,Integer>of(Stream.of(1,2,2))
												.sequence().cycleWhile(next -> count++<6)
											.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,2,1,2,2)));
	}
	@Test
	public void testCycle(){
		assertThat(MonadWrapper.<Integer,Stream<Integer>>of(Stream.of(1,2,2))
											.sequence()
											.cycle(3)
											.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,2,1,2,2,1,2,2)));
	}
	@Test
	public void testCycleReduce(){
		assertThat(MonadWrapper.<Stream<Integer>,Integer>of(Stream.of(1,2,2))
											.<Integer>sequence()
											.cycle(Reducers.toCountInt(),3)
											.collect(Collectors.toList()),
											equalTo(Arrays.asList(3,3,3)));
	}
	@Test
	public void testCycleMonad(){
		
		assertThat(MonadWrapper.<Stream<Integer>,Integer>of(Stream.of(1,2))
											.sequence()
											.cycle(Optional.class,2)
											.collect(Collectors.toList()),
											equalTo(asList(Optional.of(1),Optional.of(2)
												,Optional.of(1),Optional.of(2)	)));
	}
	@Test
	public void testJoin(){
		assertThat(MonadWrapper.<Stream<Integer>,Integer>of(Stream.of(1,2,2)).map(b-> Stream.of(b)).flatten().sequence().toList(),equalTo(Arrays.asList(1,2,2)));
	}
	@Test
	public void testJoin2(){
		assertThat(MonadWrapper.<Stream<Integer>,Integer>of(Stream.of(asList(1,2),asList(2))).flatten().sequence().toList(),equalTo(Arrays.asList(1,2,2)));
	}
	
	@Test
	public void testToSet(){
		assertThat(MonadWrapper.<Stream<Integer>,Integer>of(Stream.of(1,2,2)).sequence().toSet().size(),equalTo(2));
	}
	@Test
	public void testToList(){
		assertThat(MonadWrapper.<Stream<Integer>,Integer>of(Stream.of(1,2,3)).sequence().toList(),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testCollect(){
		assertThat(MonadWrapper.<Stream<Integer>,Integer>of(Stream.of(1,2,3)).sequence().collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testToListFlatten(){
		assertThat(MonadWrapper.<Stream<Integer>,Integer>of(Stream.of(1,2,3,null)).bind(Optional::ofNullable).sequence().toList(),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testToListOptional(){
		assertThat(MonadWrapper.<Stream<Integer>,Integer>of(Optional.of(1)).sequence().toList(),equalTo(Arrays.asList(1)));
	}
	
	@Test
    public void testFold() {
		 
       Supplier<Monad<Stream<String>,String>> s = () -> AsGenericMonad.asMonad(Stream.of("a","b","c"));

        assertThat("cba",equalTo( s.get().<String>sequence().foldRight(Reducers.toString(""))));
        assertThat("abc",equalTo( s.get().<String>sequence().foldLeft(Reducers.toString(""))));
        assertThat( 3,equalTo( s.get().map(i->""+i.length()).sequence().foldRightMapToType(Reducers.toCountInt())));
        assertThat( 3,equalTo( s.get().map(i->""+i.length()).sequence().foldLeftMapToType(Reducers.toCountInt())));
      
    }
	
	@Test
	public void testLift(){
		
		
		List<String> result = AsGenericMonad.<Stream<String>,String>asMonad(Stream.of("input.file"))
								.map(getClass().getClassLoader()::getResource)
								.peek(System.out::println)
								.map(URL::getFile)
								.<Stream<String>,String>liftAndBind(File::new)
								.<String>sequence()
								.toList();
		
		assertThat(result,equalTo(Arrays.asList("hello","world")));
	}
	
	
	
	
	@Test
	public void testSequence(){
		
        List<Integer> list = IntStream.range(0, 100).boxed().collect(Collectors.toList());
        List<CompletableFuture<Integer>> futures = list
                .stream()
                .map(x -> CompletableFuture.supplyAsync(() -> x))
                .collect(Collectors.toList());
       
        
        AnyM<Stream<Integer>> futureList = AnyMonads.sequence(completableFutureToAnyMList(futures));
        
 
        List<Integer> collected = futureList.<CompletableFuture<List<Integer>>>unwrap().join();
        assertThat(collected.size(),equalTo( list.size()));
        
        for(Integer next : list){
        	assertThat(list.get(next),equalTo( collected.get(next)));
        }
        
	}
	@Test
	public void testSequenceList(){
		
        
       
        
        AnyM<Stream<Integer>> futureList = AnyMonads.sequence(collectionToAnyMList(asList(Arrays.asList(1,2),Arrays.asList(3,4))));
        
 
        assertThat(futureList.toSequence().toList(),equalTo(Arrays.asList(1,2,3,4)));
        
	}
	@Test
	public void testSequenceStream(){
	
        
       
        
        AnyM<Stream<Integer>> result = AnyMonads.sequence(streamToAnyMList(asList(Stream.of(1,2),Stream.of(3,4))));
        
 
       
        assertThat(result
        			  .toSequence()
        		      .toList(),
        				equalTo(Arrays.asList(1,2,3,4)));
        
	}
	@Test
	public void testSequenceOptional(){
		
        

        
        AnyM<Stream<Integer>> futureList = AnyMonads.sequence(optionalToAnyMList(asList(Optional.of(7),Optional.of(8),Optional.of(9))));
        
 
        assertThat(futureList.toSequence().toList(),equalTo(Arrays.asList(7,8,9)));
        
	}
	
	@Test
	public void testTraverse(){
		
        List<Integer> list = IntStream.range(0, 100).boxed().collect(Collectors.toList());
        List<CompletableFuture<Integer>> futures = list
                .stream()
                .map(x -> CompletableFuture.supplyAsync(() -> x))
                .collect(Collectors.toList());

       
        AnyM<List<String>> futureList = AnyMonads.traverse(completableFutureToAnyMList(futures), (Integer i) -> "hello" +i);
   
        List<String> collected = futureList.<CompletableFuture<List<String>>>unwrap().join();
        assertThat(collected.size(),equalTo( list.size()));
        
        for(Integer next : list){
        	assertThat("hello"+list.get(next),equalTo( collected.get(next)));
        }
        
	}
	
	
	@Test
	public void zipStream(){
		Stream<List<Integer>> zipped = monad(Stream.of(1,2,3)).<Integer>sequence().zip(Stream.of(2,3,4), 
													(a,b) -> Arrays.asList(a,b))
													.stream();
		
		
		List<Integer> zip = zipped.collect(Collectors.toList()).get(1);
		assertThat(zip.get(0),equalTo(2));
		assertThat(zip.get(1),equalTo(3));
		
	}
	
	
	@Test
	public void distinctOptional(){
		List<Integer> list = monad(Optional.of(Arrays.asList(1,2,2,2,5,6)))
											.<Stream<Integer>,Integer>streamedMonad()
											.<Integer>sequence()
											.distinct().collect(Collectors.toList());
		
		
		assertThat(list.size(),equalTo(4));
	}
	
	
	
	@Test
	public void aggregate(){
		List<Integer> result = monad(Stream.of(1,2,3,4)).<Integer>aggregate(monad(Optional.of(5))).<Integer>sequence().toList();
		
		assertThat(result,equalTo(Arrays.asList(1,2,3,4,5)));
	}
	@Test
	public void aggregate2(){
		List<Integer> result = monad(Optional.of(Arrays.asList(1,2,3,4)))
								.<Integer>aggregate(monad(CompletableFuture.completedFuture(5)))
								.anyM().<Integer>toSequence().toList();
		
		assertThat(result,equalTo(Arrays.asList(1,2,3,4,5)));
	}
	@Test
	public void aggregate3(){
		List<Integer> result = monad(Optional.of(Arrays.asList(1,2,3,4)))
								.<Integer>aggregate(monad(CompletableFuture.supplyAsync(()->Arrays.asList(5,6))))
								.anyM().<Integer>toSequence().toList();
		
		assertThat(result,equalTo(Arrays.asList(1,2,3,4,5,6)));
	}
	
	@Test
	public void testApplyM(){
	 AnyM<Integer> applied =monad(Stream.of(1,2,3)).applyM(monad(Streamable.of( (Integer a)->a+1 ,(Integer a) -> a*2))).anyM();
	
	 assertThat(applied.toSequence().toList(),equalTo(Arrays.asList(2, 2, 3, 4, 4, 6)));
	 
	}
	@Test
	public void testApplyMOptional(){
	 AnyM<Integer> applied =monad(Optional.of(2)).applyM(monad(Optional.of( (Integer a)->a+1)) ).anyM();
	
	 assertThat(applied.toSequence().toList(),equalTo(Arrays.asList(3)));
	 
	}
	@Test
	public void testApplyMOptionalEmpty(){
	 AnyM<Integer> applied =monad(Optional.of(2)).applyM(monad(Optional.empty())).<Integer>anyM();
	
	 assertThat(applied.toSequence().toList(),equalTo(Arrays.asList()));
	 
	}
	@Test
	public void testApplyMEmptyOptional(){
		AnyM<Integer> empty= 	monad(Optional.empty()).anyM();
		AnyM<Integer> applied =	empty.applyM(anyM(Optional.of((Integer a)->a+1)) );
	
		assertThat(applied.toSequence().toList(),equalTo(Arrays.asList()));
	 
	}

	@Test
	public void testSimpleFilter(){
	 AnyM<Stream<Integer>> applied =monad(Stream.of(1,2,3)).simpleFilter(monad(Streamable.of( (Integer a)->a>5 ,(Integer a) -> a<3))).anyM();
	
	// System.out.println(applied.toList());
	 assertThat(applied.map(s->s.collect(Collectors.toList())).asSequence().toList(),equalTo(Arrays.asList(Arrays.asList(1), Arrays.asList(2),Arrays.asList())));
	
	}
	@Test
	public void testSimpleFilterOptional(){
	 AnyM<Optional<Integer>> applied =monad(Optional.of(2)).simpleFilter(monad(Streamable.of( (Integer a)->a>5 ,(Integer a) -> a<3))).anyM();
	
	 assertThat(applied.toSequence().toList(),equalTo(Arrays.asList(2)));
	
	}
	
	@Test
	public void testReplicateM(){
		 AnyM<List<Integer>> applied =monad(Optional.of(2)).replicateM(5).anyM();
		 assertThat(applied.unwrap(),equalTo(Optional.of(Arrays.asList(2,2,2,2,2))));
	}
	@Test
	public void testReplicateMStream(){
		 AnyM<Integer> applied =monad(Stream.of(2,3,4)).replicateM(5).anyM();
		 assertThat(applied.toSequence().toList(),equalTo(Arrays.asList(2,3,4,2,3,4,2,3,4,2,3,4,2,3,4)));
	}
	
	@Test
	public void testSorted(){
		assertThat(monad(Stream.of(4,3,6,7)).anyM().asSequence().sorted().toList(),equalTo(Arrays.asList(3,4,6,7)));
	}
	@Test
	public void testSortedCompartor(){
		assertThat(monad(Stream.of(4,3,6,7)).<Integer>sequence().sorted((a,b) -> b-a).toList(),equalTo(Arrays.asList(7,6,4,3)));
	}
	@Test
	public void testSkip(){
		assertThat(monad(Stream.of(4,3,6,7)).sequence().skip(2).toList(),equalTo(Arrays.asList(6,7)));
	}
	@Test
	public void testSkipUntil(){
		assertThat(monad(Stream.of(4,3,6,7)).<Integer>sequence().skipUntil(i->i==6).toList(),equalTo(Arrays.asList(6,7)));
	}
	@Test
	public void testSkipWhile(){
		assertThat(monad(Stream.of(4,3,6,7)).<Integer>sequence().sorted().skipWhile(i->i<6).toList(),equalTo(Arrays.asList(6,7)));
	}
	@Test
	public void testLimit(){
		assertThat(monad(Stream.of(4,3,6,7)).<Integer>sequence().limit(2).toList(),equalTo(Arrays.asList(4,3)));
	}
	@Test
	public void testLimitUntil(){
		assertThat(monad(Stream.of(4,3,6,7)).<Integer>sequence().limitUntil(i->i==6).toList(),equalTo(Arrays.asList(4,3)));
	}
	@Test
	public void testLimitWhile(){
		assertThat(monad(Stream.of(4,3,6,7)).<Integer>sequence().sorted().limitWhile(i->i<6).toList(),equalTo(Arrays.asList(3,4)));
	}
	
	@Test
	public void testLiftMSimplex(){
		val lifted = AnyMonads.liftM((Integer a)->a+3);
		
		AnyM<Integer> result = lifted.apply(anyM(Optional.of(3)));
		
		assertThat(result.<Optional<Integer>>unwrap().get(),equalTo(6));
	}
	
	
	@Test
	public void testReduceM(){
		Monoid<Optional<Integer>> optionalAdd = Monoid.of(Optional.of(0), (a,b)-> Optional.of(a.get()+b.get()));
		
		assertThat(monad(Stream.of(2,8,3,1)).reduceM(optionalAdd).unwrap(),equalTo(Optional.of(14)));
	}
	
	
	@Test
	public void testLiftM2Simplex(){
		val lifted = AnyMonads.liftM2((Integer a,Integer b)->a+b);
		
		AnyM<Integer> result = lifted.apply(anyM(Optional.of(3)),anyM(Optional.of(4)));
		
		assertThat(result.<Optional<Integer>>unwrap().get(),equalTo(7));
	}
	@Test
	public void testLiftM2SimplexNull(){
		val lifted = AnyMonads.liftM2((Integer a,Integer b)->a+b);
		
		AnyM<Integer> result = lifted.apply(anyM(Optional.of(3)),anyM(Optional.ofNullable(null)));
		
		assertThat(result.<Optional<Integer>>unwrap().isPresent(),equalTo(false));
	}
	
	private Integer add(Integer a, Integer  b){
		return a+b;
	}
	@Test
	public void testLiftM2Mixed(){
		val lifted = AnyMonads.liftM2(this::add); 
		
		AnyM<Integer> result = lifted.apply(anyM(Optional.of(3)),anyM(Stream.of(4,6,7)));
		
		
		assertThat(result.<Optional<List<Integer>>>unwrap().get(),equalTo(Arrays.asList(7,9,10)));
	}
}
