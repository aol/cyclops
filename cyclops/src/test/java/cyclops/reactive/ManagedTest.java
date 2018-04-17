package cyclops.reactive;

import cyclops.control.Future;
import cyclops.control.Try;
import cyclops.data.Range;
import cyclops.reactive.Managed;
import cyclops.reactive.Spouts;
import lombok.val;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Executors;

import static cyclops.companion.Monoids.intSum;
import static cyclops.companion.Monoids.zipFutures;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class ManagedTest {

    boolean closed = false;
    Resource resource;
    private void close(){
        closed=  true;
    }
    @Before
    public void setup(){
        closed = false;
        resource = new Resource();
    }
    @Test
    public void release(){
        Managed.managed("hello", t->close())
              .map(s->s.length())
              .forEach(__->{},e->e.printStackTrace());
        assertTrue(closed);
    }
    @Test
    public void releaseAfterException(){
        Managed.managed("hello",t->close())
            .map(s->s.length())
            .map(i->{throw new RuntimeException();})
            .forEach(__->{},e->e.printStackTrace());
        assertTrue(closed);
    }

    static class Resource implements AutoCloseable{

        boolean open =true;
        @Override
        public void close() throws Exception {
            open =false;
        }
        public String use(){
            return "used";
        }
    }



    @Test
    public void acquireRelease(){
        assertThat(Managed.managed(resource)
               .map(r->r.use())
               .run().orElse("wrong"),equalTo("used"));

        assertThat(resource.open,equalTo(false));
    }

    @Test
    public void acquireFailed(){
        Managed.of(Spouts.<AutoCloseable>defer(()->{
            return Spouts.generate(()->{throw new RuntimeException();});
        })).map(a->"hello").run();

        Managed.of(Spouts.<AutoCloseable>generate(() -> {
            throw new RuntimeException();
        })).map(a -> "hello").run().printErr();

    }

    @Test
    public void test() throws InterruptedException {
       val squashed =  Range.range(1,5).lazySeq().map(this::resource).foldLeft(Managed.monoid(zipFutures(intSum)));
       val sum = squashed.map(s -> Future.of(()->{
           System.out.println("Got "+s);
           return s;
       }));
       sum.runAsync(Executors.newFixedThreadPool(1));
       Thread.sleep(1000);

    }


    public Managed<Future<Integer>> resource(int i){
         return Managed.managed(Future.of(() -> {
            System.out.println("Acquiring " + i);
            return i;
        }),f->f.peek(r->{
            try {
                System.out.println("Releasing "+ i);
            }catch(Exception e){

            }
         }));
    }

}
