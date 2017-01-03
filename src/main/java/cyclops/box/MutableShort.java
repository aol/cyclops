package cyclops.box;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.aol.cyclops2.types.To;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Class that represents a Closed Variable
 * In Java 8 because of the effectively final rule references to captured
 * variables can't be changed.
 * e.g.
 *<pre>{@code 
 * short var = true;
 * Runnable r = () -> var =false;
 * }</pre>
 * 
 * Won't compile because var is treated as if it is final.
 * This can be 'worked around' by using a wrapping object or array.
 * 
 * e.g.
 * <pre>{@code
 * MutableShort var =  MutableShort.of(true);
 * Runnable r = () -> var.set(false);
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
public class MutableShort implements To<MutableShort>, Supplier<Short>, Consumer<Short> {

    private short var;

    /**
     * Create a Mutable variable, which can be mutated inside a Closure 
     * 
     * e.g.
     * <pre>{@code
     *   MutableShort num = MutableShort.of(true);
     *   
     *    num.mutate(n->!n))
     *   
     *   System.out.println(num.getAsShort());
     *   //prints false
     * } </pre>
     * 
     * @param var Initial value of Mutable
     * @return New Mutable instance
     */
    public static <T> MutableShort of(final short var) {
        return new MutableShort(
                                var);
    }

    /** 
     * Construct a MutableShort that gets and sets an external value using the provided Supplier and Consumer
     * 
     * e.g.
     * <pre>
     * {@code 
     *    MutableShort mutable = MutableShort.fromExternal(()->!this.value,val->!this.value);
     * }
     * </pre>
     * 
     * 
     * @param s Supplier of an external value
     * @param c Consumer that sets an external value
     * @return MutableShort that gets / sets an external (mutable) value
     */
    public static MutableShort fromExternal(final Supplier<Short> s, final Consumer<Short> c) {
        return new MutableShort() {
            @Override
            public short getAsShort() {
                return s.get();
            }

            @Override
            public Short get() {
                return getAsShort();
            }

            @Override
            public MutableShort set(final short value) {
                c.accept(value);
                return this;
            }
        };
    }

    /**
     * Use the supplied function to perform a lazy map operation when get is called 
     * <pre>
     * {@code 
     *  MutableShort mutable = MutableShort.fromExternal(()->!this.value,val->!this.value);
     *  Mutable<Short> withOverride = mutable.mapOutputToObj(b->{ 
     *                                                        if(override)
     *                                                             return 3s;
     *                                                         return b;
     *                                                         });
     *          
     * }
     * </pre>
     * 
     * 
     * @param fn Map function to be applied to the result when get is called
     * @return Mutable that lazily applies the provided function when get is called to the return value
     */
    public <R> Mutable<R> mapOutputToObj(final Function<Short, R> fn) {
        final MutableShort host = this;
        return new Mutable<R>() {
            @Override
            public R get() {
                return fn.apply(host.get());
            }

        };
    }

    /**
     * Use the supplied function to perform a lazy map operation when get is called 
     * <pre>
     * {@code 
     *  MutableShort mutable = MutableShort.fromExternal(()->!this.value,val->!this.value);
     *  Mutable<Short> withOverride = mutable.mapInputToObj(b->{ 
     *                                                        if(override)
     *                                                             return 1s;
     *                                                         return b;
     *                                                         });
     *          
     * }
     * </pre>
     * 
     * 
     * @param fn Map function to be applied to the input when set is called
     * @return Mutable that lazily applies the provided function when set is called to the input value
     */
    public <T1> Mutable<T1> mapInputToObj(final Function<T1, Short> fn) {
        final MutableShort host = this;
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
    public short getAsShort() {
        return var;
    }

    /**
     * @param var New value
     * @return  this object with mutated value
     */
    public MutableShort set(final short var) {
        this.var = var;
        return this;
    }

    /**
     * @param varFn New value
     * @return  this object with mutated value
     */
    public MutableShort mutate(final ShortFunction varFn) {
        return set(varFn.apply(get()));

    }

    public static interface ShortFunction {
        short apply(short var);
    }

    @Override
    public Short get() {
        return getAsShort();
    }

    @Override
    public void accept(final Short t) {
        set(t);

    }

}
