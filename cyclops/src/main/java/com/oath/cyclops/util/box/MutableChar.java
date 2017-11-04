package com.oath.cyclops.util.box;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.oath.cyclops.types.foldable.To;

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
 * char var = true;
 * Runnable r = () -> var =false;
 * }</pre>
 *
 * Won't compile because var is treated as if it is final.
 * This can be 'worked around' by using a wrapping object or array.
 *
 * e.g.
 * <pre>{@code
 * MutableChar var =  MutableChar.of(true);
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
public class MutableChar implements To<MutableChar>,Supplier<Character>, Consumer<Character> {

    private char var;

    /**
     * Create a Mutable variable, which can be mutated inside a Closure
     *
     * e.g.
     * <pre>{@code
     *   MutableChar char = MutableChar.of('c');
     *
     *    char.mutate(n->'d'))
     *
     *   System.out.println(num.getAsChar());
     *   //prints d
     * } </pre>
     *
     * @param var Initial value of Mutable
     * @return New Mutable instance
     */
    public static <T> MutableChar of(final char var) {
        return new MutableChar(
                               var);
    }

    /**
     * Construct a MutableChar that gets and sets an external value using the provided Supplier and Consumer
     *
     * e.g.
     * <pre>
     * {@code
     *    MutableChar mutable = MutableChar.fromExternal(()->!this.value,val->!this.value);
     * }
     * </pre>
     *
     *
     * @param s Supplier of an external value
     * @param c Consumer that sets an external value
     * @return MutableChar that gets / sets an external (mutable) value
     */
    public static MutableChar fromExternal(final Supplier<Character> s, final Consumer<Character> c) {
        return new MutableChar() {
            @Override
            public char getAsChar() {
                return s.get();
            }

            @Override
            public Character get() {
                return getAsChar();
            }

            @Override
            public MutableChar set(final char value) {
                c.accept(value);
                return this;
            }
        };
    }

    /**
     * Use the supplied function to perform a lazy transform operation when getValue is called
     * <pre>
     * {@code
     *  MutableChar mutable = MutableChar.fromExternal(()->!this.value,val->!this.value);
     *  Mutable<Character> withOverride = mutable.mapOutput(b->{
     *                                                        if(override)
     *                                                             return 'a';
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
    public <R> Mutable<R> mapOutputToObj(final Function<Character, R> fn) {
        final MutableChar host = this;
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
     *  MutableChar mutable = MutableChar.fromExternal(()->!this.value,val->!this.value);
     *  Mutable<Character> withOverride = mutable.mapInput(b->{
     *                                                        if(override)
     *                                                             return 'v';
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
    public <T1> Mutable<T1> mapInputToObj(final Function<T1, Character> fn) {
        final MutableChar host = this;
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
    public char getAsChar() {
        return var;
    }

    /**
     * @param var New value
     * @return  this object with mutated value
     */
    public MutableChar set(final char var) {
        this.var = var;
        return this;
    }

    /**
     * @param varFn New value
     * @return  this object with mutated value
     */
    public MutableChar mutate(final CharFunction varFn) {
        return set(varFn.apply(get()));

    }

    public static interface CharFunction {
        char apply(char var);
    }

    @Override
    public void accept(final Character t) {
        set(t);

    }

    @Override
    public Character get() {
        return getAsChar();
    }

}
