package cyclops.data;

import org.junit.Test;

import static cyclops.data.Chain.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class ChainTest {

    @Test
    public void single(){
         Chain<Integer> ten = singleton(10);
         assertThat(ten,equalTo(Seq.of(10)));
    }
}
