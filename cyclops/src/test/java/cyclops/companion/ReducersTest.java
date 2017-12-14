package cyclops.companion;

import cyclops.data.Bag;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class ReducersTest {
    @Test
    public void toBagX() throws Exception {
        assertThat(Reducers.toPersistentBag().mapReduce(Stream.of(1,2,3)),equalTo(Bag.of(1,2,3)));
        assertThat(Bag.empty().plus(10),equalTo(Bag.of(10)));
        assertThat(Bag.empty().plusAll(Arrays.asList(10)),equalTo(Bag.of(10)));
        assertThat(Bag.empty().plus(5).plusAll(Arrays.asList(10,20)),equalTo(Bag.of(5,10,20)));
    }

}
