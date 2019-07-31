package cyclops.control.maybe;

import com.oath.cyclops.types.OrElseValue;
import cyclops.control.AbstractOrElseValueTest;
import cyclops.control.Maybe;
import cyclops.control.Option;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class MaybeOrElseValueTest extends AbstractOrElseValueTest {
    @Override
    public OrElseValue<Integer, OrElseValue<Integer, ?>> of(int value) {
        return (OrElseValue) Maybe.<Integer>just(value);
    }

    @Override
    public OrElseValue<Integer, OrElseValue<Integer, ?>> empty1() {
        return (OrElseValue)Maybe.<Integer>nothing();
    }

    @Override
    public OrElseValue<Integer, OrElseValue<Integer, ?>> empty2() {
        return (OrElseValue)Maybe.<Integer>nothing();
    }

    @Override
    public OrElseValue<Integer, OrElseValue<Integer, ?>> empty3() {
        return (OrElseValue)Maybe.<Integer>nothing();
    }

    @Override
    public OrElseValue<Integer, OrElseValue<Integer, ?>> empty4() {
        return (OrElseValue)Maybe.<Integer>nothing();
    }

    @Override
    public boolean isLazy() {
        return true;
    }
}
