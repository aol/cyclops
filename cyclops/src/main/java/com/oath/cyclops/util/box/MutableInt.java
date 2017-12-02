package com.oath.cyclops.util.box;

import com.oath.cyclops.types.foldable.To;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.OptionalInt;
import java.util.function.*;
import java.util.stream.IntStream;

/**
 * Class that represents a Closed Variable
 * In Java 8 because of the effectively final rule references to captured
 * variables can't be changed.
 * e.g.
 *<pre>{@code
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
public class MutableInt implements To<MutableInt>,IntSupplier, IntConsumer, Supplier<Integer> {

    private int var;

    /**
     * Create a Mutable variable, which can be mutated inside a Closure
     *
     * e.g.
     * <pre>{@code
     *   MutableInt num = MutableInt.of(20);
     *
     *   Stream.of(1,2,3,4).map(i->i*10).peek(i-> num.mutate(n->n+i)).foreach(System.out::println);
     *
     *   System.out.println(num.getValue());
     *   //prints 120
     * } </pre>
     *
     * @param var Initial value of Mutable
     * @return New Mutable instance
     */
    public static MutableInt of(final int var) {
        return new MutableInt(
                              var);
    }

    /**
     * Construct a MutableInt that gets and sets an external value using the provided Supplier and Consumer
     *
     * e.g.
     * <pre>
     * {@code
     *    MutableInt mutable = MutableInt.fromExternal(()->!this.value,val->!this.value);
     * }
     * </pre>
     *
     *
     * @param s Supplier of an external value
     * @param c Consumer that sets an external value
     * @return MutableInt that gets / sets an external (mutable) value
     */
    public static MutableInt fromExternal(final IntSupplier s, final IntConsumer c) {
        return new MutableInt() {
            @Override
            public int getAsInt() {
                return s.getAsInt();
            }

            @Override
            public Integer get() {
                return getAsInt();
            }

            @Override
            public MutableInt set(final int value) {
                c.accept(value);
                return this;
            }
        };
    }

    /**
     * Use the supplied function to perform a lazy transform operation when getValue is called
     * <pre>
     * {@code
     *  MutableInt mutable = MutableInt.fromExternal(()->!this.value,val->!this.value);
     *  Mutable<Int> withOverride = mutable.mapOutputToObj(b->{
     *                                                        if(override)
     *                                                             return 10.0;
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
    public <R> Mutable<R> mapOutputToObj(final Function<Integer, R> fn) {
        final MutableInt host = this;
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
     *  MutableInt mutable = MutableInt.fromExternal(()->!this.value,val->!this.value);
     *  Mutable<Int> withOverride = mutable.mapInputToObj(b->{
     *                                                        if(override)
     *                                                             return 10;
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
    public <T1> Mutable<T1> mapInputToObj(final Function<T1, Integer> fn) {
        final MutableInt host = this;
        return new Mutable<T1>() {
            @Override
            public Mutable<T1> set(final T1 value) {
                host.set(fn.apply(value));
                return this;
            }

        };
    }

    /**
     * Use the supplied function to perform a lazy transform operation when getValue is called
     * <pre>
     * {@code
     *  MutableInt mutable = MutableInt.fromExternal(()->!this.value,val->!this.value);
     *  MutableInt withOverride = mutable.mapOutput(b->{
     *                                                        if(override)
     *                                                             return 10;
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
    public MutableInt mapOutput(final IntUnaryOperator fn) {
        final MutableInt host = this;
        return new MutableInt() {
            @Override
            public int getAsInt() {
                return fn.applyAsInt(host.getAsInt());
            }

        };
    }

    /**
     * Use the supplied function to perform a lazy transform operation when getValue is called
     * <pre>
     * {@code
     *  MutableInt mutable = MutableInt.fromExternal(()->!this.value,val->!this.value);
     *  MutableInt withOverride = mutable.mapInput(b->{
     *                                                        if(override)
     *                                                             return 10;
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
    public MutableInt mapInput(final IntUnaryOperator fn) {
        final MutableInt host = this;
        return new MutableInt() {
            @Override
            public MutableInt set(final int value) {
                host.set(fn.applyAsInt(value));
                return this;
            }

        };
    }

    /**
     * @return Current value
     */
    @Override
    public int getAsInt() {
        return var;
    }

    /**
     * @param var New value
     * @return  this object with mutated value
     */
    public MutableInt set(final int var) {
        this.var = var;
        return this;
    }

    /**
     * @param varFn New value
     * @return  this object with mutated value
     */
    public MutableInt mutate(final IntFunction<Integer> varFn) {
        var = varFn.apply(var);
        return this;
    }

    public OptionalInt toOptionalInt() {
        return OptionalInt.of(var);
    }

    public IntStream toIntStream() {
        return IntStream.of(var);
    }

    @Override
    public Integer get() {
        return getAsInt();
    }

    @Override
    public void accept(final int value) {
        set(value);

    }
}
