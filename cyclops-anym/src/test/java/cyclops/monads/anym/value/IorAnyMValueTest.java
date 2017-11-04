package cyclops.monads.anym.value;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.oath.cyclops.util.box.Mutable;
import org.junit.Before;
import org.junit.Test;

import cyclops.monads.AnyM;
import cyclops.control.Ior;

public class IorAnyMValueTest extends BaseAnyMValueTest<Witness.ior> {
    @Before
    public void setUp() throws Exception {
        just = AnyM.fromIor(Ior.right(10));
        none = AnyM.fromIor(Ior.left(null));
    }
    @Test
    public void testPeek() {
        Mutable<Integer> capture = Mutable.of(null);

        just = just.peek(c->capture.set(c));


        just.get();
        assertThat(capture.get(),equalTo(10));
    }
}
