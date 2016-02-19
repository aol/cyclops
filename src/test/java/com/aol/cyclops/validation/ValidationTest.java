package com.aol.cyclops.validation;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.aol.cyclops.control.Validator;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.util.validation.ValidationResults;

import lombok.Value;
public class ValidationTest {

	@Value
	static class User { 
		int age;
		String email;
	}
	@Test
	public void testAccumulate() {
		ValidationResults<String,String> results  = Validator.of((User user)->user.age>18, "too young", "age ok")
												.isValid(user->user.email!=null, "user email null","email ok")
												.accumulate(new User(10,"email@email.com"));
	
		assertThat(results.getResults().size(),equalTo(2));
	}
	
	@Test
	public void testAccumulateFJ() {
		User user = new User(10,"email@email.com");
		ValidationResults<String,String> results  = Validator.of(emailOk(user))
														.add(ageOk(user))
														.accumulate();
	
		System.out.println(results);
	}
	

	public Xor<String,String> emailOk(User u){
	    if(u.email!=null)
	       return Xor.primary("email ok");
	     return Xor.secondary("no email");
	 }
	public Xor<String,String> ageOk(User u){
	    if(u.age>18)
	        return Xor.primary("age ok");
	    return Xor.secondary("too young");
	 }

}
