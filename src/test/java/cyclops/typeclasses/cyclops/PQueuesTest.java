package cyclops.typeclasses.cyclops;

import static cyclops.function.Lambda.l1;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.aol.cyclops2.hkt.Higher;
import cyclops.collections.immutable.PQueueX;
import cyclops.control.Maybe;
import cyclops.function.Fn1;
import cyclops.function.Monoid;
import org.junit.Test;



public class PQueuesTest {

    @Test
    public void unit(){
        
        PQueueX<String> list = PQueueX.Instances.unit()
                                     .unit("hello")
                                     .convert(PQueueX::narrowK);
        
        assertThat(list.toArray(),equalTo(PQueueX.of("hello").toArray()));
    }
    @Test
    public void functor(){
        
        PQueueX<Integer> list = PQueueX.Instances.unit()
                                     .unit("hello")
                                     .apply(h->PQueueX.Instances.functor().map((String v) ->v.length(), h))
                                     .convert(PQueueX::narrowK);
        
        assertThat(list.toArray(),equalTo(PQueueX.of("hello".length()).toArray()));
    }
    @Test
    public void apSimple(){
        PQueueX.Instances.zippingApplicative()
            .ap(PQueueX.of(l1(this::multiplyByTwo)),PQueueX.of(1,2,3));
    }
    private int multiplyByTwo(int x){
        return x*2;
    }
    @Test
    public void applicative(){
        
        PQueueX<Fn1<Integer,Integer>> listFn =PQueueX.Instances.unit().unit(l1((Integer i) ->i*2)).convert(PQueueX::narrowK);
        
        PQueueX<Integer> list = PQueueX.Instances.unit()
                                     .unit("hello")
                                     .apply(h->PQueueX.Instances.functor().map((String v) ->v.length(), h))
                                     .apply(h->PQueueX.Instances.zippingApplicative().ap(listFn, h))
                                     .convert(PQueueX::narrowK);
        
        assertThat(list.toArray(),equalTo(PQueueX.of("hello".length()*2).toArray()));
    }
    @Test
    public void monadSimple(){
       PQueueX<Integer> list  = PQueueX.Instances.monad()
                                      .flatMap(i->PQueueX.range(0,i), PQueueX.of(1,2,3))
                                      .convert(PQueueX::narrowK);
    }
    @Test
    public void monad(){
        
        PQueueX<Integer> list = PQueueX.Instances.unit()
                                     .unit("hello")
                                     .apply(h->PQueueX.Instances.monad().flatMap((String v) ->PQueueX.Instances.unit().unit(v.length()), h))
                                     .convert(PQueueX::narrowK);
        
        assertThat(list.toArray(),equalTo(PQueueX.of("hello".length()).toArray()));
    }
    @Test
    public void monadZeroFilter(){
        
        PQueueX<String> list = PQueueX.Instances.unit()
                                     .unit("hello")
                                     .apply(h->PQueueX.Instances.monadZero().filter((String t)->t.startsWith("he"), h))
                                     .convert(PQueueX::narrowK);
        
        assertThat(list.toArray(),equalTo(PQueueX.of("hello").toArray()));
    }
    @Test
    public void monadZeroFilterOut(){
        
        PQueueX<String> list = PQueueX.Instances.unit()
                                     .unit("hello")
                                     .apply(h->PQueueX.Instances.monadZero().filter((String t)->!t.startsWith("he"), h))
                                     .convert(PQueueX::narrowK);
        
        assertThat(list.toArray(),equalTo(PQueueX.empty().toArray()));
    }
    
    @Test
    public void monadPlus(){
        PQueueX<Integer> list = PQueueX.Instances.<Integer>monadPlus()
                                      .plus(PQueueX.empty(), PQueueX.of(10))
                                      .convert(PQueueX::narrowK);
        assertThat(list.toArray(),equalTo(PQueueX.of(10).toArray()));
    }
    @Test
    public void monadPlusNonEmpty(){
        
        Monoid<PQueueX<Integer>> m = Monoid.of(PQueueX.empty(), (a, b)->a.isEmpty() ? b : a);
        PQueueX<Integer> list = PQueueX.Instances.<Integer>monadPlus(m)
                                      .plus(PQueueX.of(5), PQueueX.of(10))
                                      .convert(PQueueX::narrowK);
        assertThat(list.toArray(),equalTo(PQueueX.of(5).toArray()));
    }
    @Test
    public void  foldLeft(){
        int sum  = PQueueX.Instances.foldable()
                        .foldLeft(0, (a,b)->a+b, PQueueX.of(1,2,3,4));
        
        assertThat(sum,equalTo(10));
    }
    @Test
    public void  foldRight(){
        int sum  = PQueueX.Instances.foldable()
                        .foldRight(0, (a,b)->a+b, PQueueX.of(1,2,3,4));
        
        assertThat(sum,equalTo(10));
    }
    
    @Test
    public void traverse(){
       Maybe<Higher<PQueueX.µ, Integer>> res = PQueueX.Instances.traverse()
                                                         .traverseA(Maybe.Instances.applicative(), (Integer a)->Maybe.just(a*2), PQueueX.of(1,2,3))
                                                         .convert(Maybe::narrowK);
       
       
       assertThat(res.map(q->PQueueX.narrowK(q)
                                       .toArray()).get(),equalTo(Maybe.just(PQueueX.of(2,4,6).toArray()).get()));
    }
    
}
