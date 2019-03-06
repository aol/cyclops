package cyclops.futurestream;

import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import cyclops.streams.AbstractReactiveSeqTest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

public class FutureStreamReactiveSeqTest extends AbstractReactiveSeqTest {
    @Override
    public ReactiveSeq<Integer> of(Integer... values) {
        return LazyReact.sequentialBuilder()
                            .of(values);
    }

    @Override
    public ReactiveSeq<Integer> empty() {
        return FutureStream.builder()
                            .of();
    }
    @Test @Override
    public void recoverWithMiddleIterator(){

        Iterator<Integer> it = of(1, 2, 3).<Integer>map(i -> {
            if(i==2)
                throw new RuntimeException();
            return i;
        })
            .recoverWith(e-> Spouts.of(100,200,300))
            .iterator();

        List<Integer> result = new ArrayList<>();
        while(it.hasNext()){
            result.add(it.next());
        }



        assertThat(result,hasItems(100,200,300));
    }

    @Test @Override
    public void recoverWithMiddleList(){

        List<Integer> result = of(1, 2, 3).<Integer>map(i -> {
            if(i==2)
                throw new RuntimeException();
            return i;
        })
            .recoverWith(e->Spouts.of(100,200,300))
            .toList();




        assertThat(result,hasItems(100,200,300));
    }

}
