package com.aol.cyclops.control;

import java.util.function.Predicate;

import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple3;

import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.util.validation.ValidationResult;
import com.aol.cyclops.util.validation.ValidationResult.FailedResult;
import com.aol.cyclops.util.validation.ValidationResult.SuccessfulResult;
import com.aol.cyclops.util.validation.ValidationResults;

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
public class Validator<T, R, E> {

    private final Tuple3<Predicate<? super T>, R, E> validation;
    private final Validator<T, R, E> next;

    /**
     * Add another validation to this cumulative validator
     * <pre>
     * {@code
     * CumulativeValidator<User,String,String> validator = CumulativeValidator.of((User user)->user.age>18, "too young", "age ok")
     * 	ValidationResults<String,String> results  = validator.isValid(user->user.email!=null, "user email null","email ok")
    											.accumulate(new User(10,"email@email.com"));
    
    	assertThat(results.getResults().size(),equalTo(2));
    	}
     * 
     * }</pre>
     * 
     * 
     * @param valid Validation Predicate to check
     * @param error Error to return if validation predicate fails
     * @param result Result to return if validation predicate succeeds
     * @return
     */
    public Validator<T, R, E> isValid(final Predicate<? super T> valid, final E error, final R result) {
        return add(Tuple.tuple(valid, result, error));
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
    public ValidationResults<R, E> accumulate() {
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
    public ValidationResults<R, E> accumulate(final T input) {
        final ListX<ValidationResult<R, E>> results = ListX.empty();
        final ValidationResult<R, E> result;
        if (isValid(input))
            result = SuccessfulResult.success(this.validation.v2);
        else
            result = FailedResult.fail(validation.v3);

        results.add(result);
        if (next != null)
            results.addAll(next.accumulate(input)
                               .getResults());
        return new ValidationResults<R, E>(
                                           results);

    }

    /**
     * @param input to test validation against
     * @return true if valid
     */
    private boolean isValid(final T input) {
        return validation.v1.test(input);
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
    public ValidationResults<R, E> accumulateUntilFail(final T input) {
        final ListX<ValidationResult<R, E>> results = ListX.empty();
        final ValidationResult<R, E> result;
        if (isValid(input))
            result = SuccessfulResult.success(validation.v2);
        else {
            result = FailedResult.fail(validation.v3);
            results.add(result);
            return new ValidationResults<R, E>(
                                               results);
        }

        results.add(result);
        if (next != null)
            results.addAll(next.accumulate(input)
                               .getResults());
        return new ValidationResults<R, E>(
                                           results);

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
    public Validator<T, R, E> add(final Xor<E, R> validation) {
        if (next == null)
            return this.withNext(new Validator<T, R, E>(
                                                        convert(validation), null));
        else
            return next.add(convert(validation));
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
    public Validator<T, R, E> add(final Validator<T, R, E> validator) {
        if (next == null)
            return this.withNext(validator);
        else
            return next.add(validation);
    }

    private Validator<T, R, E> add(final Tuple3<Predicate<? super T>, R, E> validation) {
        if (next == null)
            return this.withNext(new Validator<T, R, E>(
                                                        validation, null));
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
    public static <T, R, E> Validator<T, R, E> of(final Predicate<? super T> valid, final E error, final R result) {
        return new Validator<>(
                               Tuple.tuple(valid, result, error), null);
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
    public static <T, R, E> Validator<T, R, E> of(final Xor<E, R> validation) {
        return new Validator<>(
                               convert(validation), null);
    }

    /*
     * Convert a Xor Validation result to a Cyclops Validator instance
     * 
     * @param validation FunctionalJava Validation result to convert
     * @return Cyclops Validator
     */
    private static <T, R, E> Tuple3<Predicate<? super T>, R, E> convert(final Xor<E, R> validation) {
        if (validation.isPrimary())
            return Tuple.tuple(t -> true, validation.<R> get(), null);

        return Tuple.tuple(t -> false, null, validation.swap()
                                                       .get());
    }

}
