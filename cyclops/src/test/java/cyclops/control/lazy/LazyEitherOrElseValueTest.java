package cyclops.control.lazy;

import com.oath.cyclops.types.OrElseValue;
import cyclops.control.AbstractOrElseValueTest;
import cyclops.control.LazyEither;
import cyclops.control.Maybe;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class LazyEitherOrElseValueTest extends AbstractOrElseValueTest {
    @Override
    public OrElseValue<Integer, OrElseValue<Integer, ?>> of(int value) {
        return (OrElseValue) LazyEither.right(value);
    }

    @Override
    public OrElseValue<Integer, OrElseValue<Integer, ?>> empty() {
        return (OrElseValue) LazyEither.left(null);
    }

    @Override
    public boolean isLazy() {
        return true;
    }

}
