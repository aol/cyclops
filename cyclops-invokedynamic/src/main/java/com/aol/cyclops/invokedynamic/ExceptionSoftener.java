package com.aol.cyclops.invokedynamic;


import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoublePredicate;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleToIntFunction;
import java.util.function.DoubleToLongFunction;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntSupplier;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.LongConsumer;
import java.util.function.LongPredicate;
import java.util.function.LongSupplier;
import java.util.function.LongToDoubleFunction;
import java.util.function.LongToIntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

import lombok.experimental.UtilityClass;

import org.jooq.lambda.fi.util.function.CheckedBiConsumer;
import org.jooq.lambda.fi.util.function.CheckedBiFunction;
import org.jooq.lambda.fi.util.function.CheckedBiPredicate;
import org.jooq.lambda.fi.util.function.CheckedBooleanSupplier;
import org.jooq.lambda.fi.util.function.CheckedConsumer;
import org.jooq.lambda.fi.util.function.CheckedDoubleConsumer;
import org.jooq.lambda.fi.util.function.CheckedDoubleFunction;
import org.jooq.lambda.fi.util.function.CheckedDoublePredicate;
import org.jooq.lambda.fi.util.function.CheckedDoubleSupplier;
import org.jooq.lambda.fi.util.function.CheckedDoubleToIntFunction;
import org.jooq.lambda.fi.util.function.CheckedDoubleToLongFunction;
import org.jooq.lambda.fi.util.function.CheckedFunction;
import org.jooq.lambda.fi.util.function.CheckedIntConsumer;
import org.jooq.lambda.fi.util.function.CheckedIntFunction;
import org.jooq.lambda.fi.util.function.CheckedIntPredicate;
import org.jooq.lambda.fi.util.function.CheckedIntSupplier;
import org.jooq.lambda.fi.util.function.CheckedIntToDoubleFunction;
import org.jooq.lambda.fi.util.function.CheckedIntToLongFunction;
import org.jooq.lambda.fi.util.function.CheckedLongConsumer;
import org.jooq.lambda.fi.util.function.CheckedLongFunction;
import org.jooq.lambda.fi.util.function.CheckedLongPredicate;
import org.jooq.lambda.fi.util.function.CheckedLongSupplier;
import org.jooq.lambda.fi.util.function.CheckedLongToDoubleFunction;
import org.jooq.lambda.fi.util.function.CheckedLongToIntFunction;
import org.jooq.lambda.fi.util.function.CheckedPredicate;
import org.jooq.lambda.fi.util.function.CheckedSupplier;
import org.junit.Test;

/**
 * Utility class for softening exceptions
 * 
 * Use throw throwSoftenedException
 * 
 * <pre>
 * {@code
 *  throw ExceptionSoftener.throwSoftenedException(new IOException("hello"));
 * }
 * </pre>
 * 
 * Or soften lambda's and method references
 * 
 * <pre>
 * {@code
 *   interface IO{
 *       static String load() throws IOException
 *   }
 *   
 *   Supplier<String> supplier = ExceptionSoftener.soften(IO::load);
 * }
 * </pre>
 * 
 * @author johnmcclean
 *
 */
@UtilityClass
public class ExceptionSoftener {

	
	/**
	 * Soften a Supplier that throws a ChecekdException into a plain old Supplier
	 * 
	 * <pre>
	 * {@code 
	 * 
	 * Supplier<String> supplier = ExceptionSoftener.soften(this::get);
	 * supplier.get();
	 * 
	 * private String get() throws IOException{
		return "hello";
	   }
	
	 * }
	 * </pre>
	 * 
	 * 
	 * @param s Supplier with CheckedException
	 * @return Supplier that throws the same exception, but doesn't need to declare it as a
	 *  checked Exception
	 */
	public static <T> Supplier<T> soften(CheckedSupplier<T> s ){
		return () -> {
			try {
				return s.get();
			} catch (Throwable e) {
				throw throwSoftenedException(e);
			}
		};
	}
	/**
	 * Soften a BooleanSuppler that throws a checked exception into one that still throws the exception, but doesn't need to declare it.
	 * 
	 * <pre>
	 * {@code 
	 
		assertThat(ExceptionSoftener.softenBooleanSupplier(()->true).getAsBoolean(),equalTo(true));
		
		//throws IOException
		ExceptionSoftener.softenBooleanSupplier(()->{throw new IOException();});
	
	
	 * }
	 * </pre>
	 * @param s CheckedBooleanSupplier to soften
	 * @return Plain old BooleanSupplier
	 */
	public static  BooleanSupplier softenBooleanSupplier(CheckedBooleanSupplier s ){
		return () -> {
			try {
				return s.getAsBoolean();
			} catch (Throwable e) {
				throw throwSoftenedException(e);
			}
		};
	}
	public static  IntSupplier softenIntSupplier(CheckedIntSupplier s ){
		return () -> {
			try {
				return s.getAsInt();
			} catch (Throwable e) {
				throw throwSoftenedException(e);
			}
		};
	}
	public static  LongSupplier softenLongSupplier(CheckedLongSupplier s ){
		return () -> {
			try {
				return s.getAsLong();
			} catch (Throwable e) {
				throw throwSoftenedException(e);
			}
		};
	}
	public static  DoubleSupplier softenDoubleSupplier(CheckedDoubleSupplier s ){
		return () -> {
			try {
				return s.getAsDouble();
			} catch (Throwable e) {
				throw throwSoftenedException(e);
			}
		};
	}
	public static <T,R> Function<T,R> soften(CheckedFunction<T,R> fn ){
		return t -> {
			try {
				return fn.apply(t);
			} catch (Throwable e) {
				throw throwSoftenedException(e);
			}
		};
	}
	public static <R> IntFunction<R> softenIntFunction(CheckedIntFunction<R> fn ){
		return t -> {
			try {
				return fn.apply(t);
			} catch (Throwable e) {
				throw throwSoftenedException(e);
			}
		};
	}
	public static <R> IntFunction<R> softenLongFunction(CheckedLongFunction<R> fn ){
		return t -> {
			try {
				return fn.apply(t);
			} catch (Throwable e) {
				throw throwSoftenedException(e);
			}
		};
	}
	public static <R> DoubleFunction<R> softenDoubleFunction(CheckedDoubleFunction<R> fn ){
		return t -> {
			try {
				return fn.apply(t);
			} catch (Throwable e) {
				throw throwSoftenedException(e);
			}
		};
	}

	public static  LongToDoubleFunction softenLongToDoubleFunction(CheckedLongToDoubleFunction fn ){
		return t -> {
			try {
				return fn.applyAsDouble(t);
			} catch (Throwable e) {
				throw throwSoftenedException(e);
			}
		};
	}

	public static LongToIntFunction softenLongToIntFunction(CheckedLongToIntFunction fn) {
		return t -> {
			try {
				return fn.applyAsInt(t);
			} catch (Throwable e) {
				throw throwSoftenedException(e);
			}
		};
	}

	public static IntToDoubleFunction softenIntToDoubleFunction(CheckedIntToDoubleFunction fn) {
		return t -> {
			try {
				return fn.applyAsDouble(t);
			} catch (Throwable e) {
				throw throwSoftenedException(e);
			}
		};
	}

	public static IntToLongFunction softenIntToLongFunction(CheckedIntToLongFunction fn) {
		return t -> {
			try {
				return fn.applyAsLong(t);
			} catch (Throwable e) {
				throw throwSoftenedException(e);
			}
		};
	}
	public static DoubleToIntFunction softenDoubleToIntFunction(CheckedDoubleToIntFunction fn) {
		return t -> {
			try {
				return fn.applyAsInt(t);
			} catch (Throwable e) {
				throw throwSoftenedException(e);
			}
		};
	}

	public static DoubleToLongFunction softenDoubleToLongFunction(CheckedDoubleToLongFunction fn) {
		return t -> {
			try {
				return fn.applyAsLong(t);
			} catch (Throwable e) {
				throw throwSoftenedException(e);
			}
		};
	}

	public static <T1, T2, R> BiFunction<T1, T2, R> soften(CheckedBiFunction<T1, T2, R> fn) {
		return (t1, t2) -> {
			try {
				return fn.apply(t1, t2);
			} catch (Throwable e) {
				throw throwSoftenedException(e);
			}
		};
	}
	public static <T> Predicate<T> soften(CheckedPredicate<T> fn ){
		return t -> {
			try {
				return fn.test(t);
			} catch (Throwable e) {
				throw throwSoftenedException(e);
			}
		};
	}
	public static  DoublePredicate softenDoublePredicate(CheckedDoublePredicate fn ){
		return t -> {
			try {
				return fn.test(t);
			} catch (Throwable e) {
				throw throwSoftenedException(e);
			}
		};
	}
	public static  IntPredicate softenIntPredicate(CheckedIntPredicate fn ){
		return t -> {
			try {
				return fn.test(t);
			} catch (Throwable e) {
				throw throwSoftenedException(e);
			}
		};
	}
	public static  LongPredicate softenIntPredicate(CheckedLongPredicate fn ){
		return t -> {
			try {
				return fn.test(t);
			} catch (Throwable e) {
				throw throwSoftenedException(e);
			}
		};
	}
	public static <T1,T2> BiPredicate<T1,T2> soften(CheckedBiPredicate<T1,T2> fn ){
		return (t1,t2) -> {
			try {
				return fn.test(t1,t2);
			} catch (Throwable e) {
				throw throwSoftenedException(e);
			}
		};
	}
	public static <T> Consumer<T> soften(CheckedConsumer<T> fn ){
		return t -> {
			try {
				 fn.accept(t);
			} catch (Throwable e) {
				throw throwSoftenedException(e);
			}
		};
	}
	public static DoubleConsumer softenDoubleConsumer(CheckedDoubleConsumer fn ){
		return t -> {
			try {
				 fn.accept(t);
			} catch (Throwable e) {
				throw throwSoftenedException(e);
			}
		};
	}
	public static IntConsumer softenIntConsumer(CheckedIntConsumer fn ){
		return t -> {
			try {
				 fn.accept(t);
			} catch (Throwable e) {
				throw throwSoftenedException(e);
			}
		};
	}
	public static LongConsumer softenLongConsumer(CheckedLongConsumer fn ){
		return t -> {
			try {
				 fn.accept(t);
			} catch (Throwable e) {
				throw throwSoftenedException(e);
			}
		};
	}
	public static <T1,T2> BiConsumer<T1,T2> soften(CheckedBiConsumer<T1,T2> fn ){
		return (t1,t2) -> {
			try {
				 fn.accept(t1,t2);
			} catch (Throwable e) {
				throw throwSoftenedException(e);
			}
		};
	}
	
	public static RuntimeException throwSoftenedException(final Throwable e) {
		throw ExceptionSoftener.<RuntimeException>uncheck(e);
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends Throwable> T uncheck(Throwable throwable) throws T {
		throw (T) throwable;
	}
			 
			
	

}

