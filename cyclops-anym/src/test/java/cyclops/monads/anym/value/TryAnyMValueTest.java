package cyclops.monads.anym.value;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.oath.cyclops.util.box.Mutable;
import cyclops.monads.Witness;
import cyclops.monads.Witness.tryType;
import org.junit.Before;
import org.junit.Test;

import cyclops.monads.AnyM;
import cyclops.control.Try;

import java.util.NoSuchElementException;

public class TryAnyMValueTest extends BaseAnyMValueTest<tryType> {
    @Before
    public void setUp() throws Exception {
        just = AnyM.fromTry(Try.success(10));
        none = AnyM.fromTry(Try.failure(new NoSuchElementException()));
    }
    @Test
    public void testPeek() {
        Mutable<Integer> capture = Mutable.of(null);
        just = just.peek(c->capture.set(c));


        just.get();
        assertThat(capture.get(),equalTo(10));
    }
}
