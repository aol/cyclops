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
    public OrElseValue<Integer, OrElseValue<Integer, ?>> empty() {
        return (OrElseValue)Maybe.<Integer>nothing();
    }

}
