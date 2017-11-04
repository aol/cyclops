package cyclops.monads.anym.value;

import java.util.Optional;

import org.junit.Before;

import cyclops.monads.AnyM;

public class OptionalAnyMValueTest extends BaseAnyMValueTest<Witness.optional> {
    @Before
    public void setUp() throws Exception {
        just = AnyM.fromOptional(Optional.of(10));
        none = AnyM.fromOptional(Optional.empty());
    }

}
