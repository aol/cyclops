package com.aol.cyclops.validation;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import lombok.Value;

import org.junit.Test;

import fj.data.Validation;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static com.aol.cyclops.validation.Validator.of;
public class ValidationAccumulateUntilFailTest {

	@Value
	static class User { 
		int age;
		String email;
	}
	@Test
	public void testAccumulateUntilFailSuccess() {
		ValidationResults<String,String> results  = CumulativeValidator.of((User user)->user.age>18, "too young", "age ok")
												.add(Validator.of((User user)->user.email!=null, "user email null","email ok"))
												.accumulateUntilFail(new User(20,"email@email.com"));
	
		assertThat(results.getResults().size(),equalTo(2));
	}
	@Test
	public void testAccumulateUntilFailAtStart() {
		ValidationResults<String,String> results  = CumulativeValidator.of((User user)->user.age>18, "too young", "age ok")
												.add(Validator.of((User user)->user.email!=null, "user email null","email ok"))
												.accumulateUntilFail(new User(10,"email@email.com"));
	
		assertThat(results.getResults().size(),equalTo(1));
	}
	
	
}
