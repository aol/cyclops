package cyclops.control.lazy;

import com.oath.cyclops.types.OrElseValue;
import cyclops.control.AbstractOrElseValueTest;
import cyclops.control.LazyEither;
import cyclops.control.LazyEither3;

import static org.junit.Assert.assertTrue;

public class LazyEither3OrElseValueTest extends AbstractOrElseValueTest {
    @Override
    public OrElseValue<Integer, OrElseValue<Integer, ?>> of(int value) {
        return (OrElseValue) LazyEither3.right(value);
    }

    @Override
    public OrElseValue<Integer, OrElseValue<Integer, ?>> empty() {
        return (OrElseValue) LazyEither3.left1(null);
    }

    @Override
    public boolean isLazy() {
        return true;
    }

}
