package com.aol.cyclops.monad.functions;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import lombok.val;

import org.junit.Test;

import com.aol.cyclops.monad.AnyM;
import com.aol.cyclops.sequence.streamable.Streamable;
public class LiftMFunctionsTest {

	public Integer one(String word){
		return word.length();
	}
	@Test
	public void testLiftM() {
		String name = null;
		assertThat(LiftMFunctions.liftM(this::one).apply(AnyM.fromOptional(Optional.ofNullable(name))).unwrap(),
				equalTo(Optional.empty()));
	}
	@Test
	public void testLiftM2Simplex(){
		val lifted = LiftMFunctions.liftM2((Integer a,Integer b)->a+b);
		
		AnyM<Integer> result = lifted.apply(AnyM.fromOptional(Optional.of(3)),AnyM.fromOptional(Optional.of(4)));
		
		assertThat(result.<Optional<Integer>>unwrap().get(),equalTo(7));
	}
	// + (Just 1) Nothing = Nothing
	@Test
	public void testLiftM2SimplexNull(){
		val lifted = LiftMFunctions.liftM2((Integer a,Integer b)->a+b);
		
		AnyM<Integer> result = lifted.apply(AnyM.fromOptional(Optional.of(3)),AnyM.fromOptional(Optional.ofNullable(null)));
		
		assertThat(result.<Optional<Integer>>unwrap().isPresent(),equalTo(false));
	}
	
	//+ [0,1] [0,2] = [0,2,1,3]
	@Test
	public void liftM2(){
		Streamable<String> stream1 = Streamable.of("ALL UPPER","MiXed Case");
		Streamable<String> stream2 = Streamable.of("MixedCase","all lower");
		
		
		AnyM<String> responses = LiftMFunctions.liftM2(this::response).apply(AnyM.fromStreamable(stream1), 
				AnyM.fromStreamable(stream2));
		
		
		assertThat(responses.toSequence().toList(),equalTo(Arrays.asList("all upper::MIXEDCASE", 
				"all upper::ALL LOWER", "mixed case::MIXEDCASE", "mixed case::ALL LOWER")));
		
	}
	@Test
	public void liftM2AnyM(){
		Streamable<String> stream1 = Streamable.of("ALL UPPER","MiXed Case");
		Streamable<String> stream2 = Streamable.of("MixedCase","all lower");
		
		
	
		AnyM<String> responses = AnyM.liftM2(this::response).apply(AnyM.fromStreamable(stream1), AnyM.fromStreamable(stream2));
		
		assertThat(responses.toSequence().toList(),equalTo(Arrays.asList("all upper::MIXEDCASE", 
				"all upper::ALL LOWER", "mixed case::MIXEDCASE", "mixed case::ALL LOWER")));
		
	}
	
	@Test
	public void lift2Stream(){
		Stream<String> stream1 = Stream.of("ALL UPPER","MiXed Case");
		Streamable<String> stream2 = Streamable.of("MixedCase","all lower");
		
		
		AnyM<String> responses = LiftMFunctions.liftM2(this::response).apply(AnyM.fromStream(stream1),
				AnyM.fromStreamable(stream2));
		
		assertThat(responses.toSequence().toList(),equalTo(Arrays.asList("all upper::MIXEDCASE", 
				"all upper::ALL LOWER", "mixed case::MIXEDCASE", "mixed case::ALL LOWER")));
		
	}
	
	public String response(String input1, String input2){
		return input1.toLowerCase() + "::" + input2.toUpperCase();
	}


}
