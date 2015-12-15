package com.aol.cyclops.featuretoggle;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import lombok.val;

import org.junit.Test;

import com.aol.cyclops.lambda.monads.AnyMonads;
import com.aol.cyclops.monad.AnyM;

public class LiftTest {

	private Integer add(Integer a, Integer b){
		return a+b;
	}
	
	@Test
	public void testLift(){
		
		val add = AnyMonads.liftM2(this::add);
		
		AnyM<Integer> result = add.apply(AnyM.ofMonad(FeatureToggle.enable(2)), AnyM.ofMonad(FeatureToggle.enable(3)));
		assertThat(result.<FeatureToggle<Integer>>unwrap().get(),equalTo(5));
	}
	
	
	
	
	public void testLiftDisabled(){
		val divide = AnyMonads.liftM2(this::add);
		
		AnyM<Integer> result = divide.apply(AnyM.ofMonad(FeatureToggle.enable(2)), AnyM.ofMonad(FeatureToggle.disable(4)));
		assertThat(result.<FeatureToggle<Integer>>unwrap().isDisabled(),equalTo(true));
	}
	
	
	
}
