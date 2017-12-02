package com.oath.cyclops.util.box;

import com.oath.cyclops.types.foldable.To;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Class that represents a Closed Variable
 * In Java 8 because of the effectively final rule references to captured
 * variables can't be changed.
 * e.g.
 *<pre>{@code
 * boolean var = true;
 * Runnable r = () -> var =false;
 * }</pre>
 *
 * Won't compile because var is treated as if it is final.
 * This can be 'worked around' by using a wrapping object or array.
 *
 * e.g.
 * <pre>{@code
 * MutableBoolean var =  MutableBoolean.of(true);
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
public class MutableBoolean implements To<MutableBoolean>,BooleanSupplier, Consumer<Boolean>, Supplier<Boolean> {


    private boolean var;

    /**
     * Create a Mutable variable, which can be mutated inside a Closure
     *
     * e.g.
     * <pre>{@code
     *   MutableBoolean num = MutableBoolean.of(true);
     *
     *    num.mutate(n->!n))
     *
     *   System.out.println(num.getAsBoolean());
     *   //prints false
     * } </pre>
     *
     * @param var Initial value of Mutable
     * @return New Mutable instance
     */
    public static <T> MutableBoolean of(final boolean var) {
        return new MutableBoolean(
                                  var);
    }

    /**
     * Construct a MutableBoolean that gets and sets an external value using the provided Supplier and Consumer
     *
     * e.g.
     * <pre>
     * {@code
     *    MutableBoolean mutable = MutableBoolean.fromExternal(()->!this.value,val->!this.value);
     * }
     * </pre>
     *
     *
     * @param s Supplier of an external value
     * @param c Consumer that sets an external value
     * @return MutableBoolean that gets / sets an external (mutable) value
     */
    public static MutableBoolean fromExternal(final BooleanSupplier s, final Consumer<Boolean> c) {
        return new MutableBoolean() {
            @Override
            public boolean getAsBoolean() {
                return s.getAsBoolean();
            }

            @Override
            public Boolean get() {
                return getAsBoolean();
            }

            @Override
            public MutableBoolean set(final boolean value) {
                c.accept(value);
                return this;
            }
        };
    }

    /**
     * Use the supplied function to perform a lazy transform operation when getValue is called
     * <pre>
     * {@code
     *  MutableBoolean mutable = MutableBoolean.fromExternal(()->!this.value,val->!this.value);
     *  Mutable<Boolean> withOverride = mutable.mapOutput(b->{
     *                                                        if(override)
     *                                                             return true;
     *                                                         return b;
     *                                                         });
     *
     * }
     * </pre>
     *
     *
     * @param fn Map function to be applied to the result when getValue is called
     * @return Mutable that lazily applies the provided function when getValue is called to the return value
     */
    public <R> Mutable<R> mapOutputToObj(final Function<Boolean, R> fn) {
        final MutableBoolean host = this;
        return new Mutable<R>() {
            @Override
            public R get() {
                return fn.apply(host.get());
            }

        };
    }

    /**
     * Use the supplied function to perform a lazy transform operation when getValue is called
     * <pre>
     * {@code
     *  MutableBoolean mutable = MutableBoolean.fromExternal(()->!this.value,val->!this.value);
     *  Mutable<Boolean> withOverride = mutable.mapInput(b->{
     *                                                        if(override)
     *                                                             return true;
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
    public <T1> Mutable<T1> mapInputToObj(final Function<T1, Boolean> fn) {
        final MutableBoolean host = this;
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
    public boolean getAsBoolean() {
        return var;
    }

    /**
     * @param var New value
     * @return  this object with mutated value
     */
    public MutableBoolean set(final boolean var) {
        this.var = var;
        return this;
    }

    /**
     * @param varFn New value
     * @return  this object with mutated value
     */
    public MutableBoolean mutate(final BooleanFunction varFn) {
        return set(varFn.apply(getAsBoolean()));

    }

    public static interface BooleanFunction {
        boolean apply(boolean var);
    }

    @Override
    public void accept(final Boolean t) {
        set(t);

    }


    public Boolean get() {
        return getAsBoolean();
    }

}
