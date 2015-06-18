package com.aol.cyclops.lambda.functions;

import static com.aol.cyclops.lambda.api.AsAnyM.anyM;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import lombok.val;

import org.junit.Test;

import com.aol.cyclops.functions.LiftMFunctions;
import com.aol.cyclops.lambda.api.AsAnyM;
import com.aol.cyclops.lambda.api.Streamable;
import com.aol.cyclops.lambda.monads.AnyM;
import com.aol.cyclops.lambda.monads.Monads;

import static org.hamcrest.Matchers.*;
public class LiftMFunctionsTest {

	public Integer one(String word){
		return word.length();
	}
	@Test
	public void testLiftM() {
		String name = null;
		assertThat(LiftMFunctions.liftM(this::one).apply(anyM(Optional.ofNullable(name))).unwrap(),
				equalTo(Optional.empty()));
	}
	@Test
	public void testLiftM2Simplex(){
		val lifted = LiftMFunctions.liftM2((Integer a,Integer b)->a+b);
		
		AnyM<Integer> result = lifted.apply(anyM(Optional.of(3)),anyM(Optional.of(4)));
		
		assertThat(result.<Optional<Integer>>unwrap().get(),equalTo(7));
	}
	// + (Just 1) Nothing = Nothing
	@Test
	public void testLiftM2SimplexNull(){
		val lifted = LiftMFunctions.liftM2((Integer a,Integer b)->a+b);
		
		AnyM<Integer> result = lifted.apply(anyM(Optional.of(3)),anyM(Optional.ofNullable(null)));
		
		assertThat(result.<Optional<Integer>>unwrap().isPresent(),equalTo(false));
	}
	
	//+ [0,1] [0,2] = [0,2,1,3]
	@Test
	public void liftM2(){
		Streamable<String> stream1 = Streamable.of("ALL UPPER","MiXed Case");
		Streamable<String> stream2 = Streamable.of("MixedCase","all lower");
		
		
		AnyM<String> responses = LiftMFunctions.liftM2(this::response).apply(anyM(stream1), anyM(stream2));
		
		assertThat(responses.toList(),equalTo(Arrays.asList("all upper::MIXEDCASE", 
				"all upper::ALL LOWER", "mixed case::MIXEDCASE", "mixed case::ALL LOWER")));
		
	}
	
	@Test
	public void lift2Stream(){
		Stream<String> stream1 = Stream.of("ALL UPPER","MiXed Case");
		Streamable<String> stream2 = Streamable.of("MixedCase","all lower");
		
		
		AnyM<String> responses = LiftMFunctions.liftM2(this::response).apply(anyM(stream1), anyM(stream2));
		
		assertThat(responses.toList(),equalTo(Arrays.asList("all upper::MIXEDCASE", 
				"all upper::ALL LOWER", "mixed case::MIXEDCASE", "mixed case::ALL LOWER")));
		
	}
	
	public String response(String input1, String input2){
		return input1.toLowerCase() + "::" + input2.toUpperCase();
	}
/**
	@Test
	public void testLiftM2BiFunctionOfU1U2R() {
		fail("Not yet implemented");
	}

	@Test
	public void testLiftM3TriFunctionOfU1U2U3R() {
		fail("Not yet implemented");
	}

	@Test
	public void testLiftM4QuadFunctionOfU1U2U3U4R() {
		fail("Not yet implemented");
	}

	@Test
	public void testLiftM5QuintFunctionOfU1U2U3U4U5R() {
		fail("Not yet implemented");
	}

	@Test
	public void testLiftM2FunctionOfU1FunctionOfU2R() {
		fail("Not yet implemented");
	}

	@Test
	public void testLiftM3FunctionOfU1FunctionOfU2FunctionOfU3R() {
		fail("Not yet implemented");
	}

	@Test
	public void testLiftM4FunctionOfU1FunctionOfU2FunctionOfU3FunctionOfU4R() {
		fail("Not yet implemented");
	}

	@Test
	public void testLiftM5FunctionOfU1FunctionOfU2FunctionOfU3FunctionOfU4FunctionOfU5R() {
		fail("Not yet implemented");
	}
	**/

}
