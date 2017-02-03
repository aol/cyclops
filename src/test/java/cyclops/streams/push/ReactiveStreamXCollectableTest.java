package cyclops.streams.push;

import com.aol.cyclops2.react.lazy.sequence.CollectableTest;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Spouts;
import org.jooq.lambda.Collectable;

public class ReactiveStreamXCollectableTest extends CollectableTest {

    @Override
    public <T> Collectable<T> of(T... values) {
       return Spouts.of(values).collectable();
    }

}
