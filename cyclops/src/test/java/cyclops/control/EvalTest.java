package cyclops.control;

import cyclops.control.Eval.CompletableEval;
import cyclops.data.Seq;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.reactive.ReactiveSeq;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static cyclops.control.Eval.eval;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
public class EvalTest {

    int times = 0;
    @Before
    public void setup(){
        times = 0;
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
	int count = 0;
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





}
