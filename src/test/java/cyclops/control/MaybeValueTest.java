package cyclops.control;

import com.aol.cyclops2.types.AbstractValueTest;
import com.aol.cyclops2.types.Value;

public class MaybeValueTest extends AbstractValueTest {

    @Override
    public <T> Value<T> of(T element) {
        return Maybe.of(element);
    }

}
