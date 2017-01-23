package cyclops.streams.push.asyncreactivestreams;

import com.aol.cyclops2.react.lazy.sequence.CollectableTest;
import cyclops.stream.Spouts;
import org.jooq.lambda.Collectable;
import reactor.core.publisher.Flux;

public class AsyncRSCollectableTest extends CollectableTest {


    public <T> Collectable<T> of(T... values){

        return Spouts.<T>reactive(s->{
            Thread t = new Thread(()-> {
                Flux.just(values).subscribe(s);

            });
            t.start();
        }).collectable();
    }

}
