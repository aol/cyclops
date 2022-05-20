package cyclops.futurestream;


import cyclops.reactive.Spouts;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class FutureStreamReactiveSeqTest extends AbstractReactiveSeqTest {
    @Override
    public FutureStream<Integer> of(Integer... values) {
        return LazyReact.sequentialBuilder()
                            .of(values);
    }

    @Override
    public FutureStream<Integer> empty() {
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

    @Test
    public void onErrorList(){
        AtomicInteger count = new AtomicInteger(0);

        try {
            of(1, 2, 3)/**.filter(i->false)**/
            .map(i -> {
                throw new RuntimeException();
            })
                .onError(e -> count.incrementAndGet())
                .toList();
            fail("exception  expected");
        }catch(Exception e){

        }


        assertThat(count.get(),equalTo(3));

    }
    @Test
    public void onErrorIterator(){
        AtomicInteger count = new AtomicInteger(0);

        try {
            Iterator<Integer> it = of(1, 2, 3).<Integer>map(i -> {
                throw new RuntimeException();
            })
                .onError(e -> count.incrementAndGet())
                .iterator();
            while(it.hasNext()){
                System.out.println(it.next());
            }
            fail("exception  expected");
        }catch(Exception e){


        }


        assertThat(count.get(),equalTo(3));

    }


}
