package cyclops.collections.box;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.aol.cyclops2.types.foldable.To;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Class that represents a Closed Variable
 * In Java 8 because of the effectively final rule references toNested captured
 * variables can't be changed.
 * e.g.
 *<pre>{@code 
 * byte var = true;
 * Runnable r = () -> var =false;
 * }</pre>
 * 
 * Won't compile because var is treated as if it is final.
 * This can be 'worked around' by using a wrapping object or array.
 * 
 * e.g.
 * <pre>{@code
 * MutableByte var =  MutableByte.of(true);
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
public class MutableByte implements To<MutableByte>,Supplier<Byte>, Consumer<Byte> {

    private byte var;

    /**
     * Create a Mutable variable, which can be mutated inside a Closure 
     * 
     * e.g.
     * <pre>{@code
     *   MutableByte num = MutableByte.of(true);
     *   
     *    num.mutate(n->!n))
     *   
     *   System.out.println(num.getAsByte());
     *   //prints false
     * } </pre>
     * 
     * @param var Initial value of Mutable
     * @return New Mutable instance
     */
    public static <T> MutableByte of(final byte var) {
        return new MutableByte(
                               var);
    }

    /** 
     * Construct a MutableByte that gets and sets an external value using the provided Supplier and Consumer
     * 
     * e.g.
     * <pre>
     * {@code 
     *    MutableByte mutable = MutableByte.fromExternal(()->!this.value,val->!this.value);
     * }
     * </pre>
     * 
     * 
     * @param s Supplier of an external value
     * @param c Consumer that sets an external value
     * @return MutableByte that gets / sets an external (mutable) value
     */
    public static MutableByte fromExternal(final Supplier<Byte> s, final Consumer<Byte> c) {
        return new MutableByte() {
            @Override
            public byte getAsByte() {
                return s.get();
            }

            @Override
            public Byte get() {
                return getAsByte();
            }

            @Override
            public MutableByte set(final byte value) {
                c.accept(value);
                return this;
            }
        };
    }

    /**
     * Use the supplied function toNested perform a lazy map operation when get is called
     * <pre>
     * {@code 
     *  MutableByte mutable = MutableByte.fromExternal(()->!this.value,val->!this.value);
     *  Mutable<Byte> withOverride = mutable.mapOutputToObj(b->{ 
     *                                                        if(override)
     *                                                             return 1b;
     *                                                         return b;
     *                                                         });
     *          
     * }
     * </pre>
     * 
     * 
     * @param fn Map function toNested be applied toNested the result when get is called
     * @return Mutable that lazily applies the provided function when get is called toNested the return value
     */
    public <R> Mutable<R> mapOutputToObj(final Function<Byte, R> fn) {
        final MutableByte host = this;
        return new Mutable<R>() {
            @Override
            public R get() {
                return fn.apply(host.get());
            }

        };
    }

    /**
     * Use the supplied function toNested perform a lazy map operation when get is called
     * <pre>
     * {@code 
     *  MutableByte mutable = MutablByte.fromExternal(()->!this.value,val->!this.value);
     *  Mutable<Byte> withOverride = mutable.mapInputToObj(b->{ 
     *                                                        if(override)
     *                                                             return 1b;
     *                                                         return b;
     *                                                         });
     *          
     * }
     * </pre>
     * 
     * 
     * @param fn Map function toNested be applied toNested the input when set is called
     * @return Mutable that lazily applies the provided function when set is called toNested the input value
     */
    public <T1> Mutable<T1> mapInputToObj(final Function<T1, Byte> fn) {
        final MutableByte host = this;
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
    public byte getAsByte() {
        return var;
    }

    /**
     * @param var New value
     * @return  this object with mutated value
     */
    public MutableByte set(final byte var) {
        this.var = var;
        return this;
    }

    /**
     * @param varFn New value
     * @return  this object with mutated value
     */
    public MutableByte mutate(final ByteFunction varFn) {
        return set(varFn.apply(getAsByte()));

    }

    public static interface ByteFunction {
        byte apply(byte var);
    }

    @Override
    public void accept(final Byte t) {
        set(t);

    }

    @Override
    public Byte get() {
        return getAsByte();
    }

}
