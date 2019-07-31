package cyclops.control.lazy;

import com.oath.cyclops.types.OrElseValue;
import cyclops.control.AbstractOrElseValueTest;
import cyclops.control.LazyEither3;
import cyclops.control.LazyEither5;

public class LazyEither5OrElseValueTest extends AbstractOrElseValueTest {
    @Override
    public OrElseValue<Integer, OrElseValue<Integer, ?>> of(int value) {
        return (OrElseValue) LazyEither5.right(value);
    }

    @Override
    public OrElseValue<Integer, OrElseValue<Integer, ?>> empty() {
        return (OrElseValue) LazyEither5.left1(null);
    }

    @Override
    public boolean isLazy() {
        return true;
    }

}
