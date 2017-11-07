package cyclops.monads.anym.value;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import cyclops.monads.Witness.either;
import org.junit.Before;
import org.junit.Test;

import cyclops.monads.AnyM;
import cyclops.control.Either;
import com.oath.cyclops.util.box.Mutable;

public class XorAnyMValueTest extends BaseAnyMValueTest<either> {
    @Before
    public void setUp() throws Exception {
        just = AnyM.fromEither(Either.right(10));
        none = AnyM.fromEither(Either.left(null));
    }
    @Test
    public void testPeek() {
        Mutable<Integer> capture = Mutable.of(null);
        just = just.peek(c->capture.set(c));


        just.get();
        assertThat(capture.get(),equalTo(10));
    }
}
