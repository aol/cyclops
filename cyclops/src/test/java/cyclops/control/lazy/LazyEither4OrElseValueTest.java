package cyclops.control.lazy;

import com.oath.cyclops.types.OrElseValue;
import cyclops.control.AbstractOrElseValueTest;
import cyclops.control.LazyEither4;
import cyclops.control.LazyEither5;

public class LazyEither4OrElseValueTest extends AbstractOrElseValueTest {
    @Override
    public OrElseValue<Integer, OrElseValue<Integer, ?>> of(int value) {
        return (OrElseValue) LazyEither4.right(value);
    }

    @Override
    public OrElseValue<Integer, OrElseValue<Integer, ?>> empty1() {
        return (OrElseValue) LazyEither4.left1(null);
    }

    @Override
    public OrElseValue<Integer, OrElseValue<Integer, ?>> empty2() {
        return (OrElseValue) LazyEither4.left2(null);
    }
    @Override
    public OrElseValue<Integer, OrElseValue<Integer, ?>> empty3() {
        return (OrElseValue) LazyEither4.left3(null);
    }

    @Override
    public OrElseValue<Integer, OrElseValue<Integer, ?>> empty4() {
        return (OrElseValue) LazyEither4.left3(null);
    }

    @Override
    public boolean isLazy() {
        return true;
    }

}
