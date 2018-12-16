package cyclops.control;

import cyclops.control.Eval.CompletableEval;
import cyclops.data.Seq;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.reactive.ReactiveSeq;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.util.NoSuchElementException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static cyclops.control.Eval.eval;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
public class EvalTest {

    int times = 0;
    @Before
    public void setup(){
        times = 0;
    }

    @Test(expected = NoSuchElementException.class)
     public void fromFuture(){
      Future<Integer> f = Future.ofError(new NoSuchElementException());
      Eval.fromPublisher(f).get();

    }

    @Test
    public void toTry(){
        Future<Integer> f = Future.ofError(new NoSuchElementException());
        Try<Integer, Throwable> t = Eval.fromPublisher(f).toTry();
        System.out.println(t);
        assertThat(t.isFailure(),equalTo(true));
        assertThat(t.failureGet().orElse(null),instanceOf(NoSuchElementException.class));
    }
    @Test
    public void fromFuture2(){
        AtomicReference<Throwable> error = new AtomicReference<>(null);
        Future<Integer> f = Future.ofError(new NoSuchElementException());
        Future.fromPublisher(Eval.fromPublisher(f)).recover(e->{
            error.set(e.getCause());return -1;
        });

        assertThat(error.get(),instanceOf(NoSuchElementException.class));
    }
    @Test
    public void fromFuture3(){
        AtomicReference<Throwable> error = new AtomicReference<>(null);
        Future<Integer> f = Future.ofError(new NoSuchElementException());
        Eval.fromPublisher(f).toFuture().recover(e->{
            error.set(e.getCause());return -1;
        });

        assertThat(error.get(),instanceOf(NoSuchElementException.class));
    }
    @Test
    public void fromFuture5(){
        AtomicReference<Throwable> error = new AtomicReference<>(null);
        Future<Integer> f = Future.ofError(new NoSuchElementException());
        Future<Integer> res = Future.of(Eval.fromPublisher(f), Executors.newCachedThreadPool()).recover(e->{
            error.set(e.getCause());return -1;
        });
        System.out.println(res.get());

        assertThat(error.get(),instanceOf(NoSuchElementException.class));
    }

    int i=0;
    @Test
    public void restartUntil(){
        i=0;
        assertThat(Eval.always(()->++i)
                .restartUntil(n->n>500000)
                .get(),equalTo(500001));
    }
    @Test(expected = NoSuchElementException.class)
    public void restartUntilAsync() throws InterruptedException {
        i=0;
        CompletableEval<Integer,Integer> async = Eval.eval();
        Thread t = new Thread(()->async.complete(1));


        t.start();


        t.join();



        assertThat(async.peek(System.out::println)
            .restartUntil(n->n>500000)
            .get(),equalTo(500001));

    }
    @Test
    public void restartUntilAsyncPassing() throws InterruptedException {
        i=0;
        CompletableEval<Integer,Integer> async = Eval.eval();
        Thread t = new Thread(()->async.complete(1));


        t.start();


        t.join();



        assertThat(async.peek(System.out::println)
            .restartUntil(n->n>0)
            .get(),equalTo(1));

    }

    @Test
    public void onError(){

        assertThat(Eval.now(100)
            .map(i->{throw new RuntimeException();})
            .recover(i->120)
            .get(),equalTo(120));


    }
    @Test
    public void testForEach() throws InterruptedException {
        count =0;
        AtomicInteger result = new AtomicInteger(-1);
        AtomicInteger values = new AtomicInteger(0);
        AtomicLong processingThread = new AtomicLong(-1l);
        long mainThread = Thread.currentThread().getId();
        CompletableEval<Integer,Integer> async = Eval.eval();
        Eval<Integer> res= async
            .map(i -> {

                System.out.println("Count " + count);
                count++;
                if(count<1000)
                    throw new RuntimeException();
                return count;
            }).peek(i->System.out.println("T "+ Thread.currentThread().getId()))
            .peek(i->processingThread.set(Thread.currentThread().getId()))
            .onErrorRestart(100000);

        Thread t = new Thread(()->async.complete(1));
        System.out.println(res.getClass());
        res.forEach(c->{
            values.incrementAndGet();
            result.set(c);
        });

        t.start();


        t.join();
        assertThat(res.get(),equalTo(count));
        assertThat(res.get(),equalTo(1000));
        assertThat(mainThread, not(equalTo(processingThread.get())));
        assertThat(-1, not(equalTo(processingThread.get())));
        assertThat(values.get(),equalTo(1));
        assertThat(result.get(),equalTo(1000));

    }
    @Test
    public void testForEachWithErrorNoErrors() throws InterruptedException {
        count =0;
        AtomicReference<Throwable> error = new AtomicReference<>(null);
        AtomicInteger result = new AtomicInteger(-1);
        AtomicInteger values = new AtomicInteger(0);
        AtomicLong processingThread = new AtomicLong(-1l);
        long mainThread = Thread.currentThread().getId();
        CompletableEval<Integer,Integer> async = Eval.eval();
        Eval<Integer> res= async
            .map(i -> {

                System.out.println("Count " + count);
                count++;
                if(count<1000)
                    throw new RuntimeException();
                return count;
            }).peek(i->System.out.println("T "+ Thread.currentThread().getId()))
            .peek(i->processingThread.set(Thread.currentThread().getId()))
            .onErrorRestart(100000);

        Thread t = new Thread(()->async.complete(1));
        res.forEach(c->{
            values.incrementAndGet();
            result.set(c);
        },e->{
            error.set(e);
        });

        t.start();


        t.join();
        assertThat(res.get(),equalTo(count));
        assertThat(res.get(),equalTo(1000));
        assertThat(mainThread, not(equalTo(processingThread.get())));
        assertThat(-1, not(equalTo(processingThread.get())));
        assertThat(values.get(),equalTo(1));
        assertThat(result.get(),equalTo(1000));
        assertThat(error.get(),equalTo(null));

    }
    @Test
    public void testForEachWithOnComplete() throws InterruptedException {
        count =0;
        AtomicReference<Throwable> error = new AtomicReference<>(null);
        AtomicInteger result = new AtomicInteger(-1);
        AtomicInteger values = new AtomicInteger(0);
        AtomicLong processingThread = new AtomicLong(-1l);
        AtomicBoolean onComplete = new AtomicBoolean(false);
        long mainThread = Thread.currentThread().getId();
        CompletableEval<Integer,Integer> async = Eval.eval();
        Eval<Integer> res= async
            .map(i -> {

                System.out.println("Count " + count);
                count++;
                if(count<10000)
                    throw new RuntimeException();
                return count;
            }).peek(i->System.out.println("T "+ Thread.currentThread().getId()))
            .peek(i->processingThread.set(Thread.currentThread().getId()))
            .onErrorRestart(100000);

        Thread t = new Thread(()->async.complete(1));
        res.forEach(c->{
            values.incrementAndGet();
            result.set(c);
        },e->{
            error.set(e);
        },()->onComplete.set(true));
        assertThat(onComplete.get(),equalTo(false));
        t.start();


        t.join();
        assertThat(res.get(),equalTo(count));
        assertThat(res.get(),equalTo(10000));
        assertThat(mainThread, not(equalTo(processingThread.get())));
        assertThat(-1, not(equalTo(processingThread.get())));
        assertThat(values.get(),equalTo(1));
        assertThat(result.get(),equalTo(10000));
        assertThat(error.get(),equalTo(null));
        assertThat(onComplete.get(),equalTo(true));

    }
    @Test
    public void testForEachWithOnCompleteWithErrors() throws InterruptedException {
        count =0;
        AtomicReference<Throwable> error = new AtomicReference<>(null);
        AtomicInteger result = new AtomicInteger(-1);
        AtomicInteger values = new AtomicInteger(0);
        AtomicLong processingThread = new AtomicLong(-1l);
        AtomicBoolean onComplete = new AtomicBoolean(false);
        long mainThread = Thread.currentThread().getId();
        CompletableEval<Integer,Integer> async = Eval.eval();
        Eval<Integer> res= async .peek(i->processingThread.set(Thread.currentThread().getId()))
            .map(i -> {

                System.out.println("Count " + count);
                count++;
                if(count<1000)
                    throw new RuntimeException();
                return count;
            }).peek(i->System.out.println("T "+ Thread.currentThread().getId()));



        Thread t = new Thread(()->async.complete(1));
        res.forEach(c->{
            values.incrementAndGet();
            result.set(c);
        },e->{
            error.set(e);
        },()->onComplete.set(true));
        assertThat(onComplete.get(),equalTo(false));
        t.start();


        t.join();
        assertThat(mainThread, not(equalTo(processingThread.get())));
        assertThat(-1, not(equalTo(processingThread.get())));
        assertThat(values.get(),equalTo(0));
        assertThat(result.get(),equalTo(-1));
        assertThat(error.get(),instanceOf(RuntimeException.class));
        assertThat(onComplete.get(),equalTo(false));

    }
    @Test
    public void testForEachWithErrors() throws InterruptedException {
        count =0;
        AtomicReference<Throwable> error = new AtomicReference<>(null);
        AtomicInteger result = new AtomicInteger(-1);
        AtomicInteger values = new AtomicInteger(0);
        AtomicLong processingThread = new AtomicLong(-1l);
        long mainThread = Thread.currentThread().getId();
        CompletableEval<Integer,Integer> async = Eval.eval();
        Eval<Integer> res= async .peek(i->processingThread.set(Thread.currentThread().getId()))
            .map(i -> {

                System.out.println("Count " + count);
                count++;
                if(count<1000)
                    throw new RuntimeException();
                return count;
            }).peek(i->System.out.println("T "+ Thread.currentThread().getId()));



        Thread t = new Thread(()->async.complete(1));
        res.forEach(c->{
            values.incrementAndGet();
            result.set(c);
        },e->{
            error.set(e);
        });

        t.start();


        t.join();

        assertThat(mainThread, not(equalTo(processingThread.get())));
        assertThat(-1, not(equalTo(processingThread.get())));
        assertThat(values.get(),equalTo(0));
        assertThat(result.get(),equalTo(-1));
        assertThat(error.get(),instanceOf(RuntimeException.class));

    }


    int count = 0;
    @Test
    public void onErrorRestart(){
        count =0;
        int res = Eval.now(10)
            .map(i -> {
                count++;
                if(count<1000)
                     throw new RuntimeException();
                return count;
        }).onErrorRestart(1000000).get();

        assertThat(res,equalTo(count));
        assertThat(res,equalTo(1000));
    }
    @Test
    public void onErrorRestartAsync() throws InterruptedException {
        count =0;
        AtomicLong processingThread = new AtomicLong(-1l);
        long mainThread = Thread.currentThread().getId();
        CompletableEval<Integer,Integer> async = Eval.eval();
        Eval<Integer> res= async
            .map(i -> {

                System.out.println("Count " + count);
                count++;
                if(count<1000)
                    throw new RuntimeException();
                return count;
            }).peek(i->System.out.println("T "+ Thread.currentThread().getId()))
            .peek(i->processingThread.set(Thread.currentThread().getId()))
            .onErrorRestart(100000);
        Thread t = new Thread(()->async.complete(1));
        res.forEach(e->{});
        t.start();


        t.join();
        assertThat(res.get(),equalTo(count));
        assertThat(res.get(),equalTo(1000));
        assertThat(mainThread, not(equalTo(processingThread.get())));
        assertThat(-1, not(equalTo(processingThread.get())));
    }
    @Test
    public void recoverAsync() throws InterruptedException {
        CompletableEval<Integer,Integer> async = Eval.eval();
        long mainThread = Thread.currentThread().getId();
        AtomicLong processingThread = new AtomicLong(-1l);
        System.out.println("Main "+ Thread.currentThread().getId());
        Eval<Integer> error = async.<Integer>map(i -> {
            throw new RuntimeException();
        })
            .recover(Throwable.class,i -> 120)
            .peek(i->System.out.println("T "+ Thread.currentThread().getId()))
            .peek(i->processingThread.set(Thread.currentThread().getId()))
            .peek(System.out::println);

        Thread t = new Thread(()->async.complete(10));
        error.forEach(e->{});
        t.start();


        t.join();

       // error.get();
        Thread.sleep(100);
        assertThat(mainThread, not(equalTo(processingThread.get())));
        assertThat(-1, not(equalTo(processingThread.get())));

    }
    @Test
    public void recoverWithAsync() throws InterruptedException {
        CompletableEval<Integer,Integer> async = Eval.eval();
        long mainThread = Thread.currentThread().getId();
        AtomicLong processingThread = new AtomicLong(-1l);
        System.out.println("Main "+ Thread.currentThread().getId());
        Eval<Integer> error = async.<Integer>map(i -> {
            throw new RuntimeException();
        })
            .recoverWith(Throwable.class,i -> Eval.now(120))
            .peek(i->System.out.println("T "+ Thread.currentThread().getId()))
            .peek(i->processingThread.set(Thread.currentThread().getId()))
            .peek(System.out::println);

        Thread t = new Thread(()->async.complete(10));
        error.forEach(e->{});
        t.start();


        t.join();

        // error.get();
        Thread.sleep(100);
        assertThat(mainThread, not(equalTo(processingThread.get())));
        assertThat(-1, not(equalTo(processingThread.get())));

    }
    Seq<String> order;
    @Test
    public void interleave(){
        order = Seq.empty();
        Eval<Integer> algorithm1 = loop(50000,Eval.now(5));
        Eval<Integer> algorithm2 = loop2(50000,Eval.now(5));

        //interleaved execution via Zip!
        Tuple2<Integer, Integer> result = algorithm1.zip(algorithm2,Tuple::tuple).get();

        System.out.println(result);

        assertThat(order.getOrElse(0,"null"),equalTo("b"));
        assertThat(order.getOrElse(1,"null"),equalTo("a"));
        assertThat(order.getOrElse(2,"null"),equalTo("b"));
        assertThat(order.getOrElse(3,"null"),equalTo("a"));
    }

    @Test
    public void interleave3(){
        order = Seq.empty();
        Eval<Integer> algorithm1 = loop(50000,Eval.now(5));
        Eval<Integer> algorithm2 = loop2(50000,Eval.now(5));
        Eval<Integer> algorithm3 = loop3(50000,Eval.now(5));

        //interleaved execution via Zip!
        Tuple3<Integer, Integer,Integer> result = algorithm1.zip(algorithm2,algorithm3,Tuple::tuple).get();

        System.out.println(result);

        assertThat(order.getOrElse(0,"null"),equalTo("c"));
        assertThat(order.getOrElse(1,"null"),equalTo("b"));
        assertThat(order.getOrElse(2,"null"),equalTo("a"));
        assertThat(order.getOrElse(3,"null"),equalTo("c"));
        assertThat(order.getOrElse(1,"null"),equalTo("b"));
        assertThat(order.getOrElse(2,"null"),equalTo("a"));
    }
    Eval<Integer> loop3(int times,Eval<Integer> sum){

        System.out.println("Loop-C " + times + " : " + sum);
        order = order.prepend("c");
        if(times==0)
            return sum;
        else
            return sum.flatMap(s->loop3(times-1,Eval.now(s+times)));
    }
    Eval<Integer> loop2(int times,Eval<Integer> sum){

        System.out.println("Loop-B " + times + " : " + sum);
        order = order.prepend("b");
        if(times==0)
            return sum;
        else
            return sum.flatMap(s->loop2(times-1,Eval.now(s+times)));
    }

    Eval<Integer> loop(int times,Eval<Integer> sum){
        System.out.println("Loop-A " + times + " : " + sum);
        order = order.prepend("a");
        if(times==0)
            return sum;
        else
            return sum.flatMap(s->loop(times-1,Eval.now(s+times)));
    }
    Supplier<Integer> loopSupplier(int times, int sum){
        System.out.println("Loop-Supplier " + times + " : " + sum);

        if(times==0)
            return ()->sum;
        else
            return eval(()->sum).flatMap(s->eval(loopSupplier(times-1,s+times)));
    }

    @Test
    public void supplier(){

        Supplier<Integer> algorithm1 = loopSupplier(50000,5);


        System.out.println(algorithm1.get());


    }

    @Test
    public void streamUntil(){
       assertThat(Eval.always(()->times++)
                        .peek(System.out::println)
                        .streamUntil(i->i>10).count(),equalTo(11L));
    }
    @Test
    public void streamUntilTime(){
        assertThat(Eval.always(()->times++)
            .peek(System.out::println)
            .streamUntil(1000, TimeUnit.MILLISECONDS).count(),greaterThan(10L));
    }
    @Test
    public void streamWhile(){
        assertThat(Eval.always(()->times++)
            .peek(System.out::println)
            .streamWhile(i->i<10).count(),equalTo(10L));
    }
	@Test
    public void lazyBugNow(){
        Eval.now("50")
                .filter(it -> {
                    times++;
                    System.out.println("filter");
                    return true;
                })
                .orElseGet(() -> "bad");
        assertThat(times,equalTo(1));
    }
    @Test
    public void lazyBugAlways(){
        Eval.always(()->"50")
                .filter(it -> {
                    times++;
                    System.out.println("filter");
                    return true;
                })
                .orElseGet(() -> "bad");
        assertThat(times,equalTo(1));
    }
    @Test
    public void lazyBugLater(){
        Eval.later(()->"50")
                .filter(it -> {
                    times++;
                    System.out.println("filter");
                    return true;
                })
                .orElseGet(() -> "bad");
        assertThat(times,equalTo(1));
    }

    @Test
    public void laterExample(){

      Eval<Integer> delayed =   Eval.later(()->loadData())
                                    .map(this::process);


    }

    @Test
    public void completableTest(){
        CompletableEval<Integer,Integer> completable = eval();
        Eval<Integer> mapped = completable.map(i->i*2)
                                          .flatMap(i->Eval.later(()->i+1));

        completable.complete(5);
        System.out.println(mapped.getClass());
        mapped.printOut();
        assertThat(mapped.get(),equalTo(11));


    }

    @Test
    public void reactive(){
        Eval<Integer> react = Eval.fromPublisher(Flux.create(s -> {
            new Thread(() -> {
                try {
                    Thread.sleep(1000l);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                s.next(1);
                s.complete();
            }).start();
        }));
        react.map(i->i*2)
                .flatMap(i->Eval.now(i*3))
                .forEach(System.out::println);


    }
    @Test
	public void coeval(){
		Future<Eval<Integer>> input = Future.future();
    	Eval<Integer> reactive = Eval.coeval(input);

    	reactive.forEach(System.out::println);

    	input.complete(Eval.now(10));
	}

    @Test
    public void testZip(){
        assertThat(Eval.now(10).zipWith(Eval.now(20),(a, b)->a+b).get(),equalTo(30));
        assertThat(Eval.now(10).zipWith((a, b)->a+b, Eval.now(20)).get(),equalTo(30));
        assertThat(Eval.now(10).zipWith(ReactiveSeq.of(20),(a, b)->a+b).get(),equalTo(30));

    }

    private String loadData(){
        return "data";
    }
    private Integer process(String process){
        return 2;
    }
    @Test
    public void oddCompletable() throws InterruptedException {

        AtomicBoolean complete = new AtomicBoolean(false);

        CompletableEval<Integer, Integer> ce = Eval.eval();
        Eval<String> even = even(ce);

        Thread t = new Thread(()->ce.complete(200000));

        even.forEach(System.out::println,System.out::println,()->complete.set(true));

        t.start();


        t.join();
        while(!complete.get()){
            Thread.sleep(100);
        }
    }
    @Test
    public void odd(){
        System.out.println(even(Eval.now(200000)).get());
    }
    public Eval<String> odd(Eval<Integer> n )  {

       return n.flatMap(x->even(Eval.now(x-1)));
    }
    public Eval<String> even(Eval<Integer> n )  {
        return n.flatMap(x->{
            return x<=0 ? Eval.now("done") : odd(Eval.now(x-1));
        });
     }

	@Test
	public void now(){
		assertThat(Eval.now(1).map(i->i+2).get(),equalTo(3));
	}
	@Test
	public void later(){
		assertThat(Eval.later(()->1).map(i->i+2).get(),equalTo(3));
	}

	@Test
	public void laterCaches(){
		count = 0;
		Eval<Integer> eval = Eval.later(()->{
			count++;
			return 1;
		});
		eval.map(i->i+2).get();
		eval.map(i->i+2).get();
		assertThat(eval.map(i->i+2).get(),equalTo(3));
		assertThat(count,equalTo(1));
	}
	@Test
	public void always(){
		assertThat(Eval.always(()->1).map(i->i+2).get(),equalTo(3));
	}
	@Test
	public void alwaysDoesNotCache(){
		count = 0;
		Eval<Integer> eval = Eval.always(()->{
			count++;
			return 1;
		});
		eval.map(i->i+2).get();
		eval.map(i->i+2).get();
		assertThat(eval.map(i->i+2).get(),equalTo(3));
		assertThat(count,equalTo(3));
	}
	@Test
	public void nowFlatMap(){
		assertThat(Eval.now(1).map(i->i+2).flatMap(i->Eval.later(()->i*3)).get(),equalTo(9));
	}
	@Test
	public void laterFlatMap(){
		assertThat(Eval.later(()->1).map(i->i+2)
						.flatMap(i->Eval.now(i*3)).get(),equalTo(9));
	}
	@Test
	public void alwaysFlatMap(){
		assertThat(Eval.always(()->1).map(i->i+2)
						.flatMap(i->Eval.now(i*3)).get(),equalTo(9));
	}
	public int addOne(Integer i){
		return i+1;
	}
	public int add(Integer i, Integer b){
		return i+b;
	}
	public String concat(String a, String b, String c){
		return a+b+c;
	}
	public String concat4(String a, String b, String c,String d){
		return a+b+c+d;
	}
	public String concat5(String a, String b, String c,String d,String e){
		return a+b+c+d+e;
	}



    @Test
    public void asyncZip(){
        Eval.CompletableEval<Integer,Integer> cm =  Eval.eval();
        System.out.println("Test ");
        Eval<Tuple2<Integer, Integer>> m = cm.zipWith(Tuple::tuple, Eval.now(10));
        System.out.println("Blocked ? ");
        assertThat(m.getClass(),equalTo(Eval.Module.FutureAlways.class));
    }

}
