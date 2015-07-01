package com.aol.cyclops.featuretoggle;

import static com.aol.cyclops.lambda.api.AsAnyM.anyM;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import lombok.val;

import org.junit.Test;

import com.aol.cyclops.featuretoggle.FeatureToggle;
import com.aol.cyclops.lambda.monads.AnyM;
import com.aol.cyclops.lambda.monads.MonadFunctions;
import com.aol.cyclops.lambda.monads.Monads;

public class LiftTest {

	private Integer add(Integer a, Integer b){
		return a+b;
	}
	
	@Test
	public void testLift(){
		
		val add = Monads.liftM2(this::add);
		
		AnyM<Integer> result = add.apply(anyM(FeatureToggle.enable(2)), anyM(FeatureToggle.enable(3)));
		assertThat(result.<FeatureToggle<Integer>>unwrap().get(),equalTo(5));
	}
	
	
	
	
	public void testLiftDisabled(){
		val divide = Monads.liftM2(this::add);
		
		AnyM<Integer> result = divide.apply(anyM(FeatureToggle.enable(2)), anyM(FeatureToggle.disable(4)));
		assertThat(result.<FeatureToggle<Integer>>unwrap().isDisabled(),equalTo(true));
	}
	
	
	
}
