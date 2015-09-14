package com.aol.cyclops.validation;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.aol.cyclops.validation.ValidationTest.User;

import fj.data.Validation;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;

/**
 * A Cumulative Validator, that processes all validation steps and accumulates all results & errors
 * 
 * Supports Lazy Validation (natively via isValid and add(Validator) )
 * And Eager Validation via FunctionalJava
 * 
 * @author johnmcclean
 *
 * @param <T> Input type
 * @param <R> Return type
 * @param <E> Error type
 */
@AllArgsConstructor
@Wither
public class CumulativeValidator<T,R,E> {

	private final Validator<T,R,E> validation;
	private final CumulativeValidator<T,R,E> next;
	public  CumulativeValidator<T,R,E> isValid(Predicate<T> valid, E error, R result){
		return new CumulativeValidator<>(Validator.of(valid, error, result),null);
	}
	
	/**
	 * Accumulate validation results. For Eager Validation
	 * Generally not suited for Lazy Validation.
	 * Null will be used as input to lazy validation steps.
	 * 
	 * User user = new User(10,"email@email.com");
		ValidationResults<String,String> results  = CumulativeValidator.of(emailOk(user))
														.add(ageOk(user))
														.accumulate();
	
		System.out.println(results);
	 * 
	 * 
	 * @return Validation Results
	 */
	public ValidationResults<R,E> accumulate(){
		return accumulate(null);
	}
	/**
	 * Accumulate validation results
	 * <pre>
	 * {@code 
	 * ValidationResults<String,String> results  = CumulativeValidator.of((User user)->user.age>18, "too young", "age ok")
												.isValid(user->user.email!=null, "user email null","email ok")
												.accumulate(new User(10,"email@email.com"));
	
		assertThat(results.getResults().size(),equalTo(2));
	 * 
	 * }
	 * </pre>
	 * @param input Value to perform validation checks with
	 * @return Validation Results
	 */
	public ValidationResults<R,E> accumulate(T input){
		List<ValidationResult<R,E>> results = new ArrayList<>();
		final ValidationResult<R,E> result;
		if(validation.isValid(input))
			result = SuccessfulResult.success(validation.result);
		else
			result = FailedResult.fail(validation.error);
		
		results.add(result);
		if(next!=null)
			results.addAll(next.accumulate(input).getResults());
		return new ValidationResults<R,E>(results);
		
	}
	/**
	 * Run the accumulation until first fail
	 * <pre>
	 * {@code 
	 * ValidationResults<String,String> results  = CumulativeValidator.of((User user)->user.age>18, "too young", "age ok")
												.add(Validator.of((User user)->user.email!=null, "user email null","email ok"))
												.accumulateUntilFail(new User(10,"email@email.com"));
	
		assertThat(results.getResults().size(),equalTo(1));
	 * 
	 * }
	 * </pre>
	 * @param input Value to perform validation checks with
	 * @return Validation Results
	 */
	public ValidationResults<R,E> accumulateUntilFail(T input){
		List<ValidationResult<R,E>> results = new ArrayList<>();
		final ValidationResult<R,E> result;
		if(validation.isValid(input))
			result = SuccessfulResult.success(validation.result);
		else{
			result = FailedResult.fail(validation.error);
			results.add(result);
			return  new ValidationResults<R,E>(results);
		}
		
		results.add(result);
		if(next!=null)
			results.addAll(next.accumulate(input).getResults());
		return new ValidationResults<R,E>(results);
		
	}
	/**
	 * Add a FunctionalJava Validation to this CumulativeValidation
	 *  <pre>
	 * {@code 
	 * 
	 *  User user = new User(10,"email@email.com");
		ValidationResults<String,String> results  = CumulativeValidator.of(emailOk(user))
														.add(user.age>18 ? Validation.success("age ok") : Validation.fail("too young"))
														.accumulate();
	
		
	 * }</pre>
	 * @param validation FunctionalJava Validation to add
	 * @return CumulativeValidator that includes the new validation (or set of validations)
	 */
	public CumulativeValidator<T,R,E> add(Validation<E,R> validation){
		if(next==null)
           		return this.withNext(new CumulativeValidator<T,R,E>(Validator.convert(validation),null));
		else
			return next.add(Validator.convert(validation));
	}
	
	/**
	 * Add another Validator instance to this validation
	 * <pre>
	 * {@code 
	 * ValidationResults<String,String> results  = CumulativeValidator.of((User user)->user.age>18, "too young", "age ok")
												.add(Validator.of((User user)->user.email!=null, "user email null","email ok"))
												.accumulate(new User(10,"email@email.com"));
	
		assertThat(results.getResults().size(),equalTo(2));
	 * 
	 * 
	 * }
	 * </pre>
	 * 
	 * @param validation Validator instance
	 * @return CumulativeValidator that includes the new validation (or set of validations)
	 */
	public CumulativeValidator<T,R,E> add(Validator<T,R,E> validation){
		if(next==null)
           		return this.withNext(new CumulativeValidator<T,R,E>(validation,null));
		else
			return next.add(validation);
	}
	
	/**
	 * Static factory method
	 * Construct a CumulativeValidator that checks for the supplied condition returns the supplied
	 * error if it doesn't hold or the supplied result if it does. Further validation steps may be added
	 * to the returned validator
	 * 
	 * <pre>
	 * {@code 
	 * 	ValidationResults<String,String> results  = CumulativeValidator.of((User user)->user.age>18, "too young", "age ok")
												.isValid(user->user.email!=null, "user email null","email ok")
												.accumulate(new User(10,"email@email.com"));
	
		assertThat(results.getResults().size(),equalTo(2));
	 * 
	 * }</pre>
	 * 
	 * @param valid Validation Predicate to check
	 * @param error Error to return if validation predicate fails
	 * @param result Result to return if validation predicate succeeds
	 * @return
	 */
	public static <T,R,E> CumulativeValidator<T,R,E> of(Predicate<T> valid, E error, R result){
		return new CumulativeValidator<>(Validator.of(valid, error, result),null);
	}
	
	/**
	 * Static factory method
	 * Construct a CumulativeValidator from the supplied FunctionalJava validation object
	 * <pre>
	 * {@code 
	 * 
	 *  User user = new User(10,"email@email.com");
		ValidationResults<String,String> results  = CumulativeValidator.of(emailOk(user))
														.add(ageOk(user))
														.accumulate();
	
		
	 * 
	 * public Validation<String,String> emailOk(User u){
	    if(u.email!=null)
	       return Validation.success("email ok");
	     return Validation.fail("no email");
	 }
	 public Validation<String,String> ageOk(User u){
	    if(u.age>18)
	        return Validation.success("age ok");
	    return Validation.fail("too young");
	 }
	 * }
	 * </pre>
	 * 
	 * 
	 * @param validation FuncitonalJava validation
	 * @return
	 */
	public static <T,R,E> CumulativeValidator<T,R,E> of(Validation<E,R> validation){
		return new CumulativeValidator<>(Validator.convert(validation),null);
	}

}

