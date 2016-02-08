package com.aol.cyclops.lambda.monads;
import static com.aol.cyclops.internal.AsGenericMonad.monad;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import lombok.val;

import org.junit.Test;

import com.aol.cyclops.collections.extensions.standard.ListX;
import com.aol.cyclops.internal.Monad;
import com.aol.cyclops.monad.AnyM;
import com.aol.cyclops.monad.AnyMonads;
import com.aol.cyclops.sequence.SequenceM;


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
            ,equalTo((Monad)monadicValue.monadFlatMap( input ->f.apply(input).monadFlatMap(g))));
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
	public void testSequence(){
		
        List<Integer> list = IntStream.range(0, 100).boxed().collect(Collectors.toList());
        List<CompletableFuture<Integer>> futures = list
                .stream()
                .map(x -> CompletableFuture.supplyAsync(() -> x))
                .collect(Collectors.toList());
       
        
        AnyM<SequenceM<Integer>> futureList = AnyM.sequence(AnyM.listFromCompletableFuture(futures));
        
 
        List<Integer> collected = futureList.<CompletableFuture<List<Integer>>>unwrap().join();
        assertThat(collected.size(),equalTo( list.size()));
        
        for(Integer next : list){
        	assertThat(list.get(next),equalTo( collected.get(next)));
        }
        
	}
	
	
	@Test
	public void testTraverse(){
		
        List<Integer> list = IntStream.range(0, 100).boxed().collect(Collectors.toList());
        List<CompletableFuture<Integer>> futures = list
                .stream()
                .map(x -> CompletableFuture.supplyAsync(() -> x))
                .collect(Collectors.toList());

       
        AnyM<ListX<String>> futureList = AnyM.traverse(AnyM.listFromCompletableFuture(futures), (Integer i) -> "hello" +i);
   
        List<String> collected = futureList.<CompletableFuture<List<String>>>unwrap().join();
        assertThat(collected.size(),equalTo( list.size()));
        
        for(Integer next : list){
        	assertThat("hello"+list.get(next),equalTo( collected.get(next)));
        }
        
	}
	
	
	
	@Test
	public void testReplicateM(){
		 AnyM<List<Integer>> applied =monad(Optional.of(2)).replicateM(5).anyM();
		 assertThat(applied.unwrap(),equalTo(Optional.of(Arrays.asList(2,2,2,2,2))));
	}

	
	@Test
	public void testLiftMSimplex(){
		val lifted = AnyM.liftM((Integer a)->a+3);
		
		AnyM<Integer> result = lifted.apply(AnyM.fromOptional(Optional.of(3)));
		
		assertThat(result.<Optional<Integer>>unwrap().get(),equalTo(6));
	}
	
	
	
	@Test
	public void testLiftM2Simplex(){
		val lifted = AnyM.liftM2((Integer a,Integer b)->a+b);
		
		AnyM<Integer> result = lifted.apply(AnyM.fromOptional(Optional.of(3)),AnyM.fromOptional(Optional.of(4)));
		
		assertThat(result.<Optional<Integer>>unwrap().get(),equalTo(7));
	}
	@Test
	public void testLiftM2SimplexNull(){
		val lifted = AnyM.liftM2((Integer a,Integer b)->a+b);
		
		AnyM<Integer> result = lifted.apply(AnyM.fromOptional(Optional.of(3)),AnyM.fromOptional(Optional.ofNullable(null)));
		
		assertThat(result.<Optional<Integer>>unwrap().isPresent(),equalTo(false));
	}
	
	private Integer add(Integer a, Integer  b){
		return a+b;
	}
	@Test
	public void testLiftM2Mixed(){
		val lifted = AnyM.liftM2(this::add); 
		
		AnyM<Integer> result = lifted.apply(AnyM.fromOptional(Optional.of(3)),AnyM.fromStream(Stream.of(4,6,7)));
		
		
		assertThat(result.<Optional<List<Integer>>>unwrap().get(),equalTo(Arrays.asList(7,9,10)));
	}
}
