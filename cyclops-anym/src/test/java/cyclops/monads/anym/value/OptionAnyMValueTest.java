package cyclops.monads.anym.value;

import com.oath.cyclops.util.box.Mutable;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import cyclops.monads.Witness.maybe;
import cyclops.monads.Witness.option;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class OptionAnyMValueTest extends BaseAnyMValueTest<option> {
    @Before
    public void setUp() throws Exception {
        just = AnyM.fromOption(Option.of(10));
        none = AnyM.fromOption(Option.none());
    }
    @Test
    public void testPeek() {
        Mutable<Integer> capture = Mutable.of(null);
        just = just.peek(c->capture.set(c));
        assertNotNull(capture.get());

        just.get();
        assertThat(capture.get(),equalTo(10));
    }
}
