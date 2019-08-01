package cyclops.control.maybe;

import com.oath.cyclops.types.AbstractValueTest;
import com.oath.cyclops.types.Value;
import cyclops.control.Maybe;

public class MaybeValueTest extends AbstractValueTest {

    @Override
    public <T> Value<T> of(T element) {
        return Maybe.of(element);
    }

}
