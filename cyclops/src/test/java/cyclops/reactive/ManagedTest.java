package cyclops.reactive;

import cyclops.control.Future;
import cyclops.control.Try;
import cyclops.data.Range;
import cyclops.data.Seq;
import cyclops.data.tuple.Tuple;
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
    public void sequence(){
        assertThat(Managed.Comprehensions.forEach(resource(1),t->resource(t.getOrElse(0)+1)).run().map(f->f.orElse(-1)).orElse(-1),equalTo(2));
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
    @Test
    public void traverse(){
        Managed<Seq<Future<String>>> writers = Managed.traverse(Seq.of("a", "b", "c"),this::acquireNamed);
       // Try<ReactiveSeq<Future<String>>, Throwable> m = writers.run();
        writers.run();//.stream().forEach(System.out::println);
    }
    @Test
    public void traverse2(){
        Managed<Seq<Future<String>>> writers = Managed.traverse(Seq.of("a"),this::acquireNamed);
        // Try<ReactiveSeq<Future<String>>, Throwable> m = writers.run();
        writers.run();//.stream().forEach(System.out::println);
    }

    @Test
    public void sequenced(){
        Managed.sequence(Seq.of(acquireNamed("a"),acquireNamed("c"))).run();
    }
    @Test
    public void traverse3() {
        acquireNamed("hello").run();
    }
    public Managed<Future<String>> acquireNamed(String name){
        return Managed.managed(Future.of(() -> {
            System.out.println("Acquiring " + name);
            return name;
        }),f->f.peek(r->{
            try {
                System.out.println("Releasing "+ name);
            }catch(Exception e){
                e.printStackTrace();

            }
        }));
    }

    @Test
    public void zip(){
        acquireNamed("left").zip(acquireNamed("right"), Tuple::tuple).flatMap(f->acquireNamed(f.toString()+"hello")).zip(acquireNamed("another"),Tuple::tuple).run();
    }
    @Test
    public void zipToList(){
        acquireNamed("a").map(Seq::of).zip(acquireNamed("b"),(a,b)->a.appendAll(b)).run();
    }
    @Test
    public void flatMap() throws InterruptedException {
        Try<Future<String>, Throwable> t = acquireNamed("hello")
                                                .map(a->{System.out.println("UsingX "+a.getOrElse("")); return a;})
                                                .flatMap(i -> acquireNamed(i.getOrElse("")+ " world")
                                                .map(b->{System.out.println("UsingY "+b.getOrElse("")); return b;}))
                                                .run();
        System.out.println(t);
        Thread.sleep(1000);
    }

    @Test
    public void flatMap2() throws InterruptedException {
        Try<Future<String>, Throwable> t = acquireNamed("hello").flatMap(i -> acquireNamed(i.getOrElse("")+ " world").flatMap(f->acquireNamed(f.getOrElse("")+"dude"))).run();
        System.out.println(t);
        Thread.sleep(1000);
    }

    @Test
    public void checkOpenComp(){
        Managed.Comprehensions.forEach(Managed.of(()->new Resource()),r-> {
          if(r.open)
              return Managed.managed(new Resource());
            throw new RuntimeException("boo!");
        }).run().printOut();
    }
    @Test
    public void checkOpenFlatMap(){
        Managed.of(()->new Resource()).flatMap(r-> {
            if(r.open)
                return Managed.managed(new Resource());
            throw new RuntimeException("boo!");
        }).run().printOut();
    }
    @Test
    public void map(){
        acquireNamed("hello").map(i->i+" world").run();
    }

}
