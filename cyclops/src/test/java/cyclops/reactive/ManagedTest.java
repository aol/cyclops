package cyclops.reactive;

import cyclops.control.Future;
import cyclops.control.Try;
import cyclops.data.Range;
import cyclops.data.Seq;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.reactive.Managed;
import cyclops.reactive.Spouts;
import lombok.val;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Executors;
import java.util.function.Function;

import static cyclops.companion.Monoids.intSum;
import static cyclops.companion.Monoids.zipFutures;
import static cyclops.data.tuple.Tuple.tuple;
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
        Try<String, Throwable> t = Managed.of(Spouts.<AutoCloseable>defer(() -> {
            return Spouts.generate(() -> {
                throw new RuntimeException();
            });
        })).map(a -> "hello").run();

        assertFalse(t.isSuccess());
        Managed.of(Spouts.<AutoCloseable>generate(() -> {
            throw new RuntimeException();
        })).map(a -> "hello").run().printErr();

    }

    @Test
    public void test() throws InterruptedException {
        Managed<Future<Integer>> squashed = Range.range(1, 5).lazySeq().map(this::resource).foldLeft(Managed.monoid(zipFutures(intSum)));
        Managed<Future<Future<Integer>>> sum = squashed.map(s -> Future.of(() -> {
            System.out.println("Got " + s);
            return s;
        }));
       Try<Future<Future<Integer>>, Throwable> t = sum.runAsync(Executors.newFixedThreadPool(1));
       Thread.sleep(1000);
       assertThat(t.map(f->f.flatMap(Function.identity())).map(f->f.orElse(-1)).orElse(-1),equalTo(15));
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
        Managed<Seq<Future<String>>> writers = Managed.traverse(Seq.of("a", "b", "c"),ManagedTest::acquireNamed);
        Try<Seq<Future<String>>, Throwable> t = writers.run();

        assertTrue(t.isSuccess());
        assertThat(t.map(s->s.map(f->f.orElse("-1")).join(",")).orElse("-"),equalTo("A,B,C"));
    }
    @Test
    public void traverse2(){
        Managed<Seq<Future<String>>> writers = Managed.traverse(Seq.of("a"),ManagedTest::acquireNamed);

        Try<Seq<Future<String>>, Throwable> t = writers.run();
        System.out.println(t);
        assertTrue(t.isSuccess());
        assertThat(t.orElse(null).map(f->f.orElse("-1")),equalTo(Seq.of("A")));
    }

    @Test
    public void sequenced(){
        Try<Seq<Future<String>>, Throwable> t = Managed.sequence(Seq.of(acquireNamed("a"), acquireNamed("c"))).run();

        System.out.println(t);
        assertTrue(t.isSuccess());

        assertThat(t.map(s->s.map(f->f.orElse("-")).join(",")).orElse("-"),equalTo("A,C"));
    }
    @Test
    public void traverse3() {
        Try<Future<String>, Throwable> t = acquireNamed("hello").run();
        assertThat(t.isSuccess(), equalTo(true));
        assertThat(t.map(f->f.orElse("world")).orElse("world"),equalTo("HELLO"));
    }
    public static Managed<Future<String>> acquireNamed(String name){
        return Managed.managed(Future.of(() -> {
            System.out.println("Acquiring " + name);
            return name.toUpperCase();
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
        Try<Tuple2<Future<String>, Future<String>>, Throwable> t = acquireNamed("left")
                                                                            .zip(acquireNamed("right"), Tuple::tuple)
                                                                            .flatMap(f -> acquireNamed(f.toString() + "hello"))
                                                                            .zip(acquireNamed("another"), Tuple::tuple)
                                                                            .run();
        System.out.println(t);
        assertTrue(t.isSuccess());
        Try<Tuple2<String, String>, Throwable> r = t.map(t2 -> tuple(t2._1().orElse(""), t2._2().orElse("")));
        assertThat(r.orElse(tuple(null,null)),equalTo(tuple("[FUTURE[LEFT],FUTURE[RIGHT]]HELLO","ANOTHER")));
    }
    @Test
    public void zipToList(){
        Try<Seq<Future<String>>, Throwable> t = acquireNamed("a").map(Seq::of).zip(acquireNamed("b"), (a, b) -> a.appendAll(b)).run();

        System.out.println(t);
        assertTrue(t.isSuccess());
        assertThat(t.orElse(Seq.of()).map(f->f.orElse("")),equalTo(Seq.of("A","B")));

    }
    @Test
    public void flatMap() throws InterruptedException {
        Try<Future<String>, Throwable> t = acquireNamed("hello")
                                                .map(a->{System.out.println("UsingX "+a.getOrElse("")); return a;})
                                                .flatMap(i -> acquireNamed(i.getOrElse("")+ " world")
                                                .map(b->{System.out.println("UsingY "+b.getOrElse("")); return b;}))
                                                .run();
        System.out.println(t);
        assertTrue(t.isSuccess());
        assertThat(t.orElse(null).orElse("-1"),equalTo("HELLO WORLD"));

    }

    @Test
    public void flatMap2() throws InterruptedException {
        Try<Future<String>, Throwable> t = acquireNamed("hello").flatMap(i -> acquireNamed(i.getOrElse("")+ " world").flatMap(f->acquireNamed(f.getOrElse("")+"dude"))).run();
        assertTrue(t.isSuccess());
        assertThat(t.orElse(null).orElse("-1"),equalTo("hello worlddude".toUpperCase()));

    }

    @Test
    public void checkOpenComp(){
        Try<Resource, Throwable> t = Managed.Comprehensions.forEach(Managed.of(() -> new Resource()), r -> {
            if (r.open)
                return Managed.managed(new Resource());
            throw new RuntimeException("boo!");
        }).run();

        assertTrue(t.isSuccess());
    }
    @Test
    public void checkOpenFlatMap(){
        Try<Resource, Throwable> t = Managed.of(() -> new Resource()).flatMap(r -> {
            if (r.open)
                return Managed.managed(new Resource());
            throw new RuntimeException("boo!");
        }).run();

        assertTrue(t.isSuccess());
    }
    @Test
    public void map(){
        Try<String, Throwable> t = acquireNamed("hello").map(i -> i.getOrElse("") + " world").run();
        assertThat(t,equalTo(Try.success("HELLO world")));
    }

}
