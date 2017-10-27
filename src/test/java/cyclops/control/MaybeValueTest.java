package cyclops.control;

import com.oath.cyclops.types.AbstractValueTest;
import com.oath.cyclops.types.Value;

public class MaybeValueTest extends AbstractValueTest {

    @Override
    public <T> Value<T> of(T element) {
        return Maybe.of(element);
    }

}
