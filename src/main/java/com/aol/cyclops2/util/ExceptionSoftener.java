package com.aol.cyclops2.util;

import java.util.concurrent.Callable;
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
import java.util.function.LongFunction;
import java.util.function.LongPredicate;
import java.util.function.LongSupplier;
import java.util.function.LongToDoubleFunction;
import java.util.function.LongToIntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;


import cyclops.control.Try;
import cyclops.control.Try.CheckedRunnable;
import cyclops.function.checked.*;
import lombok.experimental.UtilityClass;

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
     * Soften a Runnable that throws a ChecekdException into a plain old Runnable
     * 
     * <pre>
     * {@code 
     * 
     * Runnable runnable = ExceptionSoftener.softenRunnable(this::run);
     * runnable.run() //thows IOException but doesn't need to declare it
     * 
     * private void  run() throws IOException{
    	 throw new IOException();
       }
    	ExceptionSoftener.softenRunnable(()->Thread.sleep(1000));
     * }
     * </pre>
     * 
     * 
     * @param s Supplier with CheckedException
     * @return Supplier that throws the same exception, but doesn't need to declare it as a
     *  checked Exception
     */
    public static Runnable softenRunnable(final CheckedRunnable s) {
        return () -> {
            try {
                s.run();
            } catch (final Throwable e) {
                throw throwSoftenedException(e);
            }
        };
    }

    /**
     * Soften a Supplier that throws a ChecekdException into a plain old Supplier
     * 
     * <pre>
     * {@code 
     * 
     * Supplier<String> supplier = ExceptionSoftener.softenSupplier(this::get);
     * supplier.get(); //thows IOException but doesn't need to declare it
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
    public static <T> Supplier<T> softenSupplier(final CheckedSupplier<T> s) {
        return () -> {
            try {
                return s.get();
            } catch (final Throwable e) {
                throw throwSoftenedException(e);
            }
        };
    }

    /**
     * Soften a Callable that throws a ChecekdException into a Supplier
     * 
     * <pre>
     * {@code 
     * 
     * Supplier<String> supplier = ExceptionSoftener.softenCallable(this);
     * supplier.get(); //thows IOException but doesn't need to declare it
     * 
     * public String call() throws IOException{
    	return "hello";
       }
    
     * }
     * </pre>
     * 
     * 
     * @param s Callable with CheckedException
     * @return Supplier that throws the same exception, but doesn't need to declare it as a
     *  checked Exception
     */
    public static <T> Supplier<T> softenCallable(final Callable<T> s) {
        return () -> {
            try {
                return s.call();
            } catch (final Throwable e) {
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
    	
    	
    	BooleanSupplier supplier = ExceptionSoftener.softenBooleanSupplier(()->{throw new IOException();});
    	
    	supplier.get() //throws IOException but doesn't need to declare it
    
    
     * }
     * </pre>
     * @param s CheckedBooleanSupplier to soften
     * @return Plain old BooleanSupplier
     */
    public static BooleanSupplier softenBooleanSupplier(final CheckedBooleanSupplier s) {
        return () -> {
            try {
                return s.getAsBoolean();
            } catch (final Throwable e) {
                throw throwSoftenedException(e);
            }
        };
    }

    /**
     * Soften a CheckedIntSupplier to an IntSupplier that doesn't need to declare any checked exceptions thrown
     * e.g.
     * <pre>
     * {@code
     *  IntSupplier supplier =   ExceptionSoftener.softenIntSupplier(()->{throw new IOException();})
     *  
     *  supplier.getAsInt();//throws IOException but doesn't need to declare it
     *  
     *  
     *  //as a method reference
     *  ExceptionSoftener.softenIntSupplier(this::getInt);
     * }
     * </pre>
     * 
     * @param s CheckedIntSupplier to soften
     * @return IntSupplier that can throw checked exceptions
     */
    public static IntSupplier softenIntSupplier(final CheckedIntSupplier s) {
        return () -> {
            try {
                return s.getAsInt();
            } catch (final Throwable e) {
                throw throwSoftenedException(e);
            }
        };
    }

    /**
     * Soften a CheckedLongSupplier to an LongSupplier that doesn't need to declare any checked exceptions thrown
     * e.g.
     * <pre>
     * {@code
     *  LongSupplier supplier =   ExceptionSoftener.softenLongSupplier(()->{throw new IOException();})
     *  
     *  supplier.getAsLong();//throws IOException but doesn't need to declare it
     *  
     *  
     *  //as a method reference
     *  ExceptionSoftener.softenLongSupplier(this::getLong);
     * }
     * </pre>
     * 
     * @param s CheckedLongSupplier to soften
     * @return LongSupplier that can throw checked exceptions
     */
    public static LongSupplier softenLongSupplier(final CheckedLongSupplier s) {
        return () -> {
            try {
                return s.getAsLong();
            } catch (final Throwable e) {
                throw throwSoftenedException(e);
            }
        };
    }

    /**
     * Soften a CheckedDoubleSupplier to an DoubleSupplier that doesn't need to declare any checked exceptions thrown
     * e.g.
     * <pre>
     * {@code
     *  DoubleSupplier supplier =   ExceptionSoftener.softenDoubleSupplier(()->{throw new IOException();})
     *  
     *  supplier.getAsDouble();//throws IOException but doesn't need to declare it
     *  
     *  
     *  //as a method reference
     *  ExceptionSoftener.softenDoubleSupplier(this::getDouble);
     * }
     * </pre>
     * 
     * @param s CheckedDoubleSupplier to soften
     * @return DoubleSupplier that can throw checked exceptions
     */
    public static DoubleSupplier softenDoubleSupplier(final CheckedDoubleSupplier s) {
        return () -> {
            try {
                return s.getAsDouble();
            } catch (final Throwable e) {
                throw throwSoftenedException(e);
            }
        };
    }

    /**
     * Soften a CheckedFunction that can throw Checked Exceptions to a standard Function that can also throw Checked Exceptions (without declaring them)
     * 
     * e.g.
     * 
     * <pre>
     * {@code 
     * Data loaded = ExceptionSoftener.softenFunction(this::load).applyHKT(fileName);
    
    	public Data load(String file) throws IOException
     * </pre>
     * 
     * @param fn CheckedFunction to be converted to a standard Function
     * @return Function that can throw checked Exceptions
     */
    public static <T, R> Function<T, R> softenFunction(final CheckedFunction<T, R> fn) {
        return t -> {
            try {
                return fn.apply(t);
            } catch (final Throwable e) {
                throw throwSoftenedException(e);
            }
        };
    }

    /**
     * Soften a CheckedIntFunction that can throw Checked Exceptions to a standard IntFunction that can also throw Checked Exceptions (without declaring them)
     * 
     * e.g.
     * 
     * <pre>
     * {@code 
     *  int loaded = ExceptionSoftener.softenFunction(this::load).applyHKT(id);
    
    	public int load(int it) throws IOException
     * </pre>
     * 
     * @param fn CheckedIntFunction to be converted to a standard IntFunction
     * @return IntFunction that can throw checked Exceptions
     */
    public static <R> IntFunction<R> softenIntFunction(final CheckedIntFunction<R> fn) {
        return t -> {
            try {
                return fn.apply(t);
            } catch (final Throwable e) {
                throw throwSoftenedException(e);
            }
        };
    }

    /**
     * Soften a CheckedLongFunction that can throw Checked Exceptions to a standard LongFunction that can also throw Checked Exceptions (without declaring them)
     * 
     * e.g.
     * 
     * <pre>
     * {@code 
     *  long loaded = ExceptionSoftener.softenFunction(this::load).applyHKT(id);
    
    	public long load(long it) throws IOException
     * </pre>
     * 
     * @param fn CheckedLongFunction to be converted to a standard LongFunction
     * @return LongFunction that can throw checked Exceptions
     */
    public static <R> LongFunction<R> softenLongFunction(final CheckedLongFunction<R> fn) {
        return t -> {
            try {
                return fn.apply(t);
            } catch (final Throwable e) {
                throw throwSoftenedException(e);
            }
        };
    }

    /**
     * Soften a CheckedDoubleFunction that can throw Checked Exceptions to a standard DoubleFunction that can also throw Checked Exceptions (without declaring them)
     * 
     * e.g.
     * 
     * <pre>
     * {@code 
     *  double loaded = ExceptionSoftener.softenFunction(this::load).applyHKT(id);
    
    	public double load(double it) throws IOException
     * </pre>
     * 
     * @param fn CheckedDoubleFunction to be converted to a standard DoubleFunction
     * @return DoubleFunction that can throw checked Exceptions
     */
    public static <R> DoubleFunction<R> softenDoubleFunction(final CheckedDoubleFunction<R> fn) {
        return t -> {
            try {
                return fn.apply(t);
            } catch (final Throwable e) {
                throw throwSoftenedException(e);
            }
        };
    }


    /**
     * 	Soften a CheckedBiFunction that can throw Checked Exceptions to a standard BiFunction that can also throw Checked Exceptions (without declaring them)
     * 
    
     * <pre>
     * {@code
     * 
    	
    	ExceptionSoftener.softenBiFunction(this::loadDir).applyHKT(".core","/tmp/dir");
     * 
     *  public String loadDir(String fileExt,String dir) throws IOException
     *  
     *  }
     *  </pre>
     * @param fn CheckedBiLongFunction to be converted to a standard BiFunction
     * @return BiFunction that can throw checked Exceptions
     */
    public static <T1, T2, R> BiFunction<T1, T2, R> softenBiFunction(final CheckedBiFunction<T1, T2, R> fn) {
        return (t1, t2) -> {
            try {
                return fn.apply(t1, t2);
            } catch (final Throwable e) {
                throw throwSoftenedException(e);
            }
        };
    }

    /**
     * Soften a CheckedPredicate that can throw Checked Exceptions to a standard Predicate that can also throw Checked Exceptions (without declaring them)
     * 
     * e.g.
     * 
     * <pre>
     * {@code 
     *  boolean loaded = ExceptionSoftener.softenPredicate(this::exists).test(id);
    
    	public boolean exists(Double id) throws IOException
     * </pre>
     * 
     * @param fn CheckedPredicate to be converted to a standard Predicate
     * @return Predicate that can throw checked Exceptions
     */
    public static <T> Predicate<T> softenPredicate(final CheckedPredicate<T> fn) {
        return t -> {
            try {
                return fn.test(t);
            } catch (final Throwable e) {
                throw throwSoftenedException(e);
            }
        };
    }

    /**
     * Soften a CheckedDoublePredicate that can throw Checked Exceptions to a standard DoublePredicate that can also throw Checked Exceptions (without declaring them)
     * 
     * e.g.
     * 
     * <pre>
     * {@code 
     *  boolean loaded = ExceptionSoftener.softenDoublePredicate(this::exists).test(id);
    
    	public boolean exists(double id) throws IOException
     * </pre>
     * 
     * @param fn CheckedDoublePredicate to be converted to a standard DoublePredicate
     * @return DoublePredicate that can throw checked Exceptions
     */
    public static DoublePredicate softenDoublePredicate(final CheckedDoublePredicate fn) {
        return t -> {
            try {
                return fn.test(t);
            } catch (final Throwable e) {
                throw throwSoftenedException(e);
            }
        };
    }

    /**
     * Soften a CheckedIntPredicate that can throw Checked Exceptions to a standard IntPredicate that can also throw Checked Exceptions (without declaring them)
     * 
     * e.g.
     * 
     * <pre>
     * {@code 
     *  boolean loaded = ExceptionSoftener.softenIntPredicate(this::exists).test(id);
    
    	public boolean exists(int id) throws IOException
     * </pre>
     * 
     * @param fn CheckedIntPredicate to be converted to a standard IntPredicate
     * @return IntPredicate that can throw checked Exceptions
     */
    public static IntPredicate softenIntPredicate(final CheckedIntPredicate fn) {
        return t -> {
            try {
                return fn.test(t);
            } catch (final Throwable e) {
                throw throwSoftenedException(e);
            }
        };
    }

    /**
     * Soften a CheckedLongPredicate that can throw Checked Exceptions to a standard LongPredicate that can also throw Checked Exceptions (without declaring them)
     * 
     * e.g.
     * 
     * <pre>
     * {@code 
     *  boolean loaded = ExceptionSoftener.softenLongPredicate(this::exists).test(id);
    
    	public boolean exists(long id) throws IOException
     * </pre>
     * 
     * @param fn CheckedLongPredicate to be converted to a standard LongPredicate
     * @return LongPredicate that can throw checked Exceptions
     */
    public static LongPredicate softenLongPredicate(final CheckedLongPredicate fn) {
        return t -> {
            try {
                return fn.test(t);
            } catch (final Throwable e) {
                throw throwSoftenedException(e);
            }
        };
    }

    /**
     * Soften a CheckedBiPredicate that can throw Checked Exceptions to a standard BiPredicate that can also throw Checked Exceptions (without declaring them)
     * 
     * e.g.
     * 
     * <pre>
     * {@code 
     *  boolean loaded = ExceptionSoftener.softenBiPredicate(this::exists).test(id,"db");
    
    	public boolean exists(int id, String context) throws IOException
     * </pre>
     * 
     * @param fn CheckedBiPredicate to be converted to a standard BiPredicate
     * @return BiPredicate that can throw checked Exceptions
     */
    public static <T1, T2> BiPredicate<T1, T2> softenBiPredicate(final CheckedBiPredicate<T1, T2> fn) {
        return (t1, t2) -> {
            try {
                return fn.test(t1, t2);
            } catch (final Throwable e) {
                throw throwSoftenedException(e);
            }
        };
    }

    /**
     * Soften a CheckedConsumer that can throw Checked Exceptions to a standard Consumer that can also throw Checked Exceptions (without declaring them)
     * 
     * e.g.
     * 
     * <pre>
     * {@code 
     *  ExceptionSoftener.softenConsumer(this::save).accept(data);
    
    	public void save(Data data) throws IOException
     * </pre>
     * 
     * @param fn CheckedConsumer to be converted to a standard Consumer
     * @return Consumer that can throw checked Exceptions
     */
    public static <T> Consumer<T> softenConsumer(final CheckedConsumer<T> fn) {
        return t -> {
            try {
                fn.accept(t);
            } catch (final Throwable e) {
                throw throwSoftenedException(e);
            }
        };
    }

    /**
     * Soften a CheckedDoubleConsumer that can throw Checked Exceptions to a standard DoubleConsumer that can also throw Checked Exceptions (without declaring them)
     * 
     * e.g.
     * 
     * <pre>
     * {@code 
     *  ExceptionSoftener.softenDoubleConsumer(this::save).accept(data);
    
    	public void save(double data) throws IOException
     * </pre>
     * 
     * @param fn CheckedDoubleConsumer to be converted to a standard DoubleConsumer
     * @return DoubleConsumer that can throw checked Exceptions
     */
    public static DoubleConsumer softenDoubleConsumer(final CheckedDoubleConsumer fn) {
        return t -> {
            try {
                fn.accept(t);
            } catch (final Throwable e) {
                throw throwSoftenedException(e);
            }
        };
    }

    /**
     * Soften a CheckedIntConsumer that can throw Checked Exceptions to a standard IntConsumer that can also throw Checked Exceptions (without declaring them)
     * 
     * e.g.
     * 
     * <pre>
     * {@code 
     *  ExceptionSoftener.softenIntConsumer(this::save).accept(data);
    
    	public void save(int data) throws IOException
     * </pre>
     * 
     * @param fn CheckedIntConsumer to be converted to a standard IntConsumer
     * @return IntConsumer that can throw checked Exceptions
     */
    public static IntConsumer softenIntConsumer(final CheckedIntConsumer fn) {
        return t -> {
            try {
                fn.accept(t);
            } catch (final Throwable e) {
                throw throwSoftenedException(e);
            }
        };
    }

    /**
     * Soften a CheckedLongConsumer that can throw Checked Exceptions to a standard LongConsumer that can also throw Checked Exceptions (without declaring them)
     * 
     * e.g.
     * 
     * <pre>
     * {@code 
     *  ExceptionSoftener.softenLongConsumer(this::save).accept(data);
    
    	public void save(long data) throws IOException
     * </pre>
     * 
     * @param fn CheckedLongConsumer to be converted to a standard LongConsumer
     * @return LongConsumer that can throw checked Exceptions
     */
    public static LongConsumer softenLongConsumer(final CheckedLongConsumer fn) {
        return t -> {
            try {
                fn.accept(t);
            } catch (final Throwable e) {
                throw throwSoftenedException(e);
            }
        };
    }

    /**
     * Soften a CheckedBiConsumer that can throw Checked Exceptions to a standard BiConsumer that can also throw Checked Exceptions (without declaring them)
     * 
     * e.g.
     * 
     * <pre>
     * {@code 
     *  ExceptionSoftener.softenBiConsumer(this::save).accept(data,System.currentTimeMillis());
    
    	public void save(Data data,long timestamp) throws IOException
     * </pre>
     * 
     * @param fn CheckedBiConsumer to be converted to a standard BiConsumer
     * @return BiConsumer that can throw checked Exceptions
     */
    public static <T1, T2> BiConsumer<T1, T2> softenBiConsumer(final CheckedBiConsumer<T1, T2> fn) {
        return (t1, t2) -> {
            try {
                fn.accept(t1, t2);
            } catch (final Throwable e) {
                throw throwSoftenedException(e);
            }
        };
    }

    /**
     * Convert any throwable into an unchecked exception. The original exception will stay as is,
     * this simply tricks the Java compiler into thinking the specified throwable is an unchecked exception.
     * There is no need to wrap your checked Exceptions inside RuntimeExceptions to propagate them without having to declare them.
     * 
     * e.g.
     * 
     * <pre>
     * {@code 
     * 
     * //IOException does not need to be declared
     * 
     *  public Data load(String input) {
     *   try{
     *   
     *   
     *   }catch(IOException e) {
     *   
     *       throw ExceptionSoftener.throwSoftenedException(e);
     *    }
     * 
     * }
     * }
     * </pre>
     * 
     * @param e
     * @return
     */
    public static RuntimeException throwSoftenedException(final Throwable e) {
        throw ExceptionSoftener.<RuntimeException> uncheck(e);
    }

    /**
     * Throw the exception as upwards if the predicate holds, otherwise do nothing
     * 
     * @param e Exception
     * @param p Predicate to check exception should be thrown or not
     */
    public static <X extends Throwable> void throwIf(final X e, final Predicate<X> p) {
        if (p.test(e))
            throw ExceptionSoftener.<RuntimeException> uncheck(e);
    }

    /**
     * Throw the exception as upwards if the predicate holds, otherwise pass to the handler
     * 
     * @param e  Exception
     * @param p Predicate to check exception should be thrown or not
     * @param handler Handles exceptions that should not be thrown
     */
    public static <X extends Throwable> void throwOrHandle(final X e, final Predicate<X> p, final Consumer<X> handler) {
        if (p.test(e))
            throw ExceptionSoftener.<RuntimeException> uncheck(e);
        else
            handler.accept(e);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> T uncheck(final Throwable throwable) throws T {
        throw (T) throwable;
    }

}
