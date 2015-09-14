package com.aol.cyclops.validation;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import lombok.Value;

import org.junit.Test;

import fj.data.Validation;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static com.aol.cyclops.validation.Validator.of;
public class ValidationTest {

	@Value
	static class User { 
		int age;
		String email;
	}
	@Test
	public void testAccumulate() {
		ValidationResults<String,String> results  = CumulativeValidator.of((User user)->user.age>18, "too young", "age ok")
												.isValid(user->user.email!=null, "user email null","email ok")
												.accumulate(new User(10,"email@email.com"));
	
		assertThat(results.getResults().size(),equalTo(2));
	}
	
	@Test
	public void testAccumulateFJ() {
		User user = new User(10,"email@email.com");
		ValidationResults<String,String> results  = CumulativeValidator.of(emailOk(user))
														.add(ageOk(user))
														.accumulate();
	
		System.out.println(results);
	}
	

	public Validation<String,String> emailOk(User u){
	    if(u.email!=null)
	       return Validation.success("email ok");
	     return Validation.fail("no email");
	 }
	public Validation<String,String> ageOk(User u){
	    if(u.age>18)
	        return Validation.success("age ok");
	    return Validation.fail("too young");
	 }

}
