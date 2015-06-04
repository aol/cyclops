package com.aol.cyclops.lambda.functions;

import static com.aol.cyclops.lambda.api.AsAnyM.anyM;
import static org.junit.Assert.*;

import java.util.Optional;

import lombok.val;

import org.junit.Test;

import com.aol.cyclops.functions.LiftMFunctions;
import com.aol.cyclops.lambda.api.AsAnyM;
import com.aol.cyclops.lambda.monads.AnyM;

import static org.hamcrest.Matchers.*;
public class LiftMFunctionsTest {

	public Integer one(String word){
		return word.length();
	}
	@Test
	public void testLiftM() {
		String name = null;
		assertThat(LiftMFunctions.liftM(this::one).apply(anyM(Optional.ofNullable(name))).unwrapMonad(),
				equalTo(Optional.empty()));
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
