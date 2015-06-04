package com.aol.cyclops.enableswitch;

import static com.aol.cyclops.lambda.api.AsAnyM.anyM;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import lombok.val;

import org.junit.Test;

import com.aol.cyclops.lambda.monads.AnyM;
import com.aol.cyclops.lambda.monads.MonadFunctions;

public class LiftTest {

	private Integer add(Integer a, Integer b){
		return a+b;
	}
	
	@Test
	public void testLift(){
		val add = MonadFunctions.liftM2(this::add);
		
		AnyM<Integer> result = add.apply(anyM(Switch.enable(2)), anyM(Switch.enable(3)));
		assertThat(result.<Switch<Integer>>unwrapMonad().get(),equalTo(5));
	}
	
	
	
	
	public void testLiftDisabled(){
		val divide = MonadFunctions.liftM2(this::add);
		
		AnyM<Integer> result = divide.apply(anyM(Switch.enable(2)), anyM(Switch.disable(4)));
		assertThat(result.<Switch<Integer>>unwrapMonad().isDisabled(),equalTo(true));
	}
	
	
	
}
