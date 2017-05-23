package cyclops.collections.box;

import com.aol.cyclops2.types.foldable.To;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Class that represents a Closed Variable
 * In Java 8 because of the effectively final rule references toNested captured
 * variables can't be changed.
 * e.g.
 *<pre>
 * {@code 
 * String var = "hello";
 * Runnable r = () -> var ="world";
 * }</pre>
 * 
 * Won't compile because var is treated as if it is final.
 * This can be 'worked around' by using a wrapping object or array.
 * 
 * e.g.
 * <pre>{@code
 * Mutable<String> var =  Mutable.of("hello");
 * Runnable r = () -> var.set("world");
 * }</pre>
 * 
 * @author johnmcclean
 *
 * @param <T> Type held inside closed var
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class Mutable<T> implements To<Mutable<T>>,Supplier<T>, Consumer<T>,Iterable<T> {

    private T var;

    @Override
    public Iterator<T> iterator() {
        return var!=null ? Arrays.asList(var).iterator() : Arrays.<T>asList().iterator();
    }

    /**
     * Create a Mutable variable, which can be mutated inside a Closure 
     * 
     * e.g.
     * <pre>{@code
     *   Mutable<Integer> num = Mutable.of(20);
     *   
     *   Stream.of(1,2,3,4).map(i->i*10).peek(i-> num.mutate(n->n+i)).foreach(System.out::println);
     *   
     *   System.out.println(num.get());
     *   //prints 120
     * } </pre>
     * 
     * @param var Initial value of Mutable
     * @return New Mutable instance
     */
    public static <T> Mutable<T> of(final T var) {
        return new Mutable<T>(
                              var);
    }

    /** 
     * Construct a Mutable that gets and sets an external value using the provided Supplier and Consumer
     * 
     * e.g.
     * <pre>
     * {@code 
     *    Mutable<Integer> mutable = Mutable.from(()->this.value*2,val->this.value=val);
     * }
     * </pre>
     * 
     * 
     * @param s Supplier of an external value
     * @param c Consumer that sets an external value
     * @return Mutable that gets / sets an external (mutable) value
     */
    public static <T> Mutable<T> fromExternal(final Supplier<T> s, final Consumer<T> c) {
        return new Mutable<T>() {
            @Override
            public T get() {
                return s.get();
            }

            @Override
            public Mutable<T> set(final T value) {
                c.accept(value);
                return this;
            }
        };
    }

    public <R> Mutable<R> mapOutput(final Function<T, R> fn) {
        final Mutable<T> host = this;
        return new Mutable<R>() {
            @Override
            public R get() {
                return fn.apply(host.get());
            }

        };
    }

    public <T1> Mutable<T1> mapInput(final Function<T1, T> fn) {
        final Mutable<T> host = this;
        return new Mutable<T1>() {
            @Override
            public Mutable<T1> set(final T1 value) {
                host.set(fn.apply(value));
                return this;
            }

        };
    }

    /**
     * @return Current value
     */
    @Override
    public T get() {
        return var;
    }

    /**
     * @param var New value
     * @return  this object with mutated value
     */
    public Mutable<T> set(final T var) {
        this.var = var;
        return this;
    }

    /**
     * @param varFn New value
     * @return  this object with mutated value
     */
    public Mutable<T> mutate(final Function<T, T> varFn) {
        return set(varFn.apply(get()));
    }

    /* (non-Javadoc)
     * @see java.util.function.Consumer#accept(java.lang.Object)
     */
    @Override
    public void accept(final T t) {
        set(t);

    }



}
