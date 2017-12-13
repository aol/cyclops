package cyclops.streams.push.asyncreactivestreams;

import cyclops.collections.mutable.ListX;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;


public class AsyncRSFlatMapPTest {
    protected <U> ReactiveSeq<U> of(U... array){
        return Spouts.from(Flux.just(array).subscribeOn(Schedulers.fromExecutor(ForkJoinPool.commonPool())));

    }
    Executor ex = Executors.newFixedThreadPool(4);
    protected <U> ReactiveSeq<U> of2(U... array){
        return Spouts.from(Flux.just(array).subscribeOn(Schedulers.fromExecutor(ForkJoinPool.commonPool())));

    }
    protected <U> Flux<U> flux(U... array){
        return Flux.just(array).subscribeOn(Schedulers.fromExecutor(ForkJoinPool.commonPool()));

    }
    @Test
    public void simple(){
        of(1,2,3).mergeMap(1, i->flux(5,6,7)).peek(e->System.out.println("e " + e)).printOut();
    }

    @Test
    public void fluxTest(){
       List<String> list = flux(1,2,3,4)
                                .flatMap(i->flux(10,20,30).flatMap(j->flux("hello"+j,"world"),4),3)
                                .collect(Collectors.toList()).block();

       System.out.println(list);
    }
    @Test
    public void fluxList(){
        for(int l=0;l<1000;l++) {
            System.out.println("Starting!");
            List<Integer> it = this.flux(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                    .flatMap(i -> flux(i, i * 2, i * 4, 5, 6, 7, 8, 9)
                            .flatMap(x -> of2(5, 6, 7, 7, 8, 9),4),4)
                            .collect(Collectors.toList()).block();
            System.out.println("Size =  " + it.size());
        }
    }
    @Test
    public void nestedSync(){
        System.out.println(of(1, 2, 3)
                .mergeMap(2, i -> Spouts.of(5,6).mergeMap(2, k->Spouts.of(8,9))).toListX());
    }
    @Test
    public void failing(){
        System.out.println(Spouts.of(1, 2, 3)
                .mergeMap(2, i -> Spouts.of(5,6)).toListX());
        System.out.println(of(1, 2, 3)
                .mergeMap(2, i -> of(5,6)).toListX());
    }
    @Test
    public void flatMapPList(){
        for(int l=0;l<1_000;l++) {
            System.out.println("Starting!");
            ListX<Integer> it = this.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                    .mergeMap(4, i -> of2(i, i * 2, i * 4, 5, 6, 7, 8, 9)
                            .mergeMap(4, x -> of2(5, 6, 7, 7, 8, 9)))
                    .toListX();
            System.out.println("Size =  " + it.size());
        }
    }

    @Test
    public void flatMapPIt(){
        for(int l=0;l<1_000;l++) {
            Iterator<Integer> it = this.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                    .mergeMap( 4, i -> of2(i, i * 2, i * 4, 5, 6, 7, 8, 9)
                            .mergeMap( 4, x -> of2(5, 6, 7, 7, 8, 9)))
                    .iterator();
            assertThat("Iteration " + l,ListX.fromIterator(it).size(),equalTo(480));
        }
    }
    @Test
    public void flatMapPIterate(){
        for(int l=0;l<10;l++) {
            System.out.println("Iteration " + l);
            Iterator<Integer> it = this.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                    .mergeMap(i -> of2(i, i * 2, i * 4, 5, 6, 7, 8, 9)
                            .mergeMap(x -> of2(5, 6, 7, 7, 8, 9)))
                    .iterator();
            List<Integer> result = new ArrayList<>(480);
            while(it.hasNext()){
                result.add(it.next());
            }
            assertThat("Iteration " + l,result.size(),equalTo(480));
        }
    }
    @Test
    public void flatMapPIterateNoConc(){
        // int l = 0;
        for(int l=0;l<1_000;l++)
        {
            System.out.println("Iteration " + l);
            Iterator<Integer> it = this.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                    .mergeMap(i -> of2(i, i * 2, i * 4, 5, 6, 7, 8, 9)
                            .mergeMap(x -> of2(5, 6, 7, 7, 8, 9)))
                    .iterator();
            List<Integer> result = new ArrayList<>(480);
            while(it.hasNext()){
                result.add(it.next());
            }
            assertThat("Iteration " + l,result.size(),equalTo(480));
        }
    }
    @Test
    public void flatMapPubIteration(){
        Iterator<Integer> it = of(1,2,3,4)
                .mergeMap(i->of(5,6)
                        .mergeMap(k->of(k)))
                .iterator();

        assertThat(ListX.fromIterator(it).size(),equalTo(8));
    }
    @Test
    public void flatMapPub1(){
        for(int l=0;l<1_000;l++) {
            System.out.println("************Iteration " + l);
            System.out.println("************Iteration " + l);
            System.out.println("************Iteration " + l);

            assertThat(ListX.of(5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7).size(),
                    equalTo(this.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                            .mergeMap(i -> of(i, i * 2, i * 4)
                                    .mergeMap(x -> of(5, 6, 7)))
                            .toListX().size()));

        }
    }
    @Test
    public void flatMapPub(){
        for(int l=0;l<1_000;l++) {
            System.out.println("************Iteration " + l);
            System.out.println("************Iteration " + l);
            System.out.println("************Iteration " + l);

            System.out.println(this.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                    .mergeMap(i -> of(i, i * 2, i * 4)
                            .mergeMap(x -> of(5, 6, 7)))
                    .toListX());

        }
    }
    @Test
    public void flatMapP(){
        for(int l=0;l<1_000;l++) {
            System.out.println("************Iteration " + l);
            System.out.println("************Iteration " + l);
            System.out.println("************Iteration " + l);
            System.out.println(this.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                    .mergeMap(i -> of(i, i * 2, i * 4)
                            .mergeMap(x -> of(5, 6, 7)))
                    .toListX());
        }
    }
    @Test
    public void flatMapPConc(){
        for(int l=0;l<1_000;l++) {
            System.out.println("************Iteration " + l);
            System.out.println("************Iteration " + l);
            System.out.println("************Iteration " + l);
            System.out.println(this.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                    .mergeMap(i -> of2(i, i * 2, i * 4,5,6,7,8,9)
                            .mergeMap(x -> of2(5, 6, 7,7,8,9)))
                    .toListX());
        }
    }
    @Test
    public void flatMapP3(){
        System.out.println(this.of(1,2)
                .mergeMap(i->of(i,i*2,i*4)
                        .mergeMap(x->of(5,6,7)
                                .mergeMap(y->of(2,3,4))))
                .toListX());
    }
    @Test
    public void flatMapP2(){
        for(int l=0;l<1_000;l++) {
            System.out.println("************Iteration " + l);
            System.out.println("************Iteration " + l);
            System.out.println("************Iteration " + l);
            System.out.println(this.of("1", "2")
                    .mergeMap(i -> of(1, 2,3))
                    .mergeMap(x -> of('a','b'))
                    .toListX());
        }
    }
    @Test
    public void flatMapP2a(){
        for(int l=0;l<1_000;l++) {
            System.out.println("************Iteration " + l);
            System.out.println("************Iteration " + l);
            System.out.println("************Iteration " + l);
            System.out.println(this.of("1", "2","3")
                    .mergeMap(i -> of(1, 2,3,4,5))
                    .mergeMap(x -> of('a','b'))
                    .toListX());
        }
    }
}
