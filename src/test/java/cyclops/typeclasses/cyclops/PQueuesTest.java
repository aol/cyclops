package cyclops.typeclasses.cyclops;

import static cyclops.function.Lambda.l1;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.aol.cyclops2.hkt.Higher;
import cyclops.collectionx.immutable.PersistentQueueX;
import cyclops.control.lazy.Maybe;
import cyclops.function.Function1;
import cyclops.function.Monoid;
import cyclops.control.anym.Witness.persistentQueueX;
import org.junit.Test;



public class PQueuesTest {

    @Test
    public void unit(){
        
        PersistentQueueX<String> list = PersistentQueueX.Instances.unit()
                                     .unit("hello")
                                     .convert(PersistentQueueX::narrowK);
        
        assertThat(list.toArray(),equalTo(PersistentQueueX.of("hello").toArray()));
    }
    @Test
    public void functor(){
        
        PersistentQueueX<Integer> list = PersistentQueueX.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> PersistentQueueX.Instances.functor().map((String v) ->v.length(), h))
                                     .convert(PersistentQueueX::narrowK);
        
        assertThat(list.toArray(),equalTo(PersistentQueueX.of("hello".length()).toArray()));
    }
    @Test
    public void apSimple(){
        PersistentQueueX.Instances.zippingApplicative()
            .ap(PersistentQueueX.of(l1(this::multiplyByTwo)), PersistentQueueX.of(1,2,3));
    }
    private int multiplyByTwo(int x){
        return x*2;
    }
    @Test
    public void applicative(){
        
        PersistentQueueX<Function1<Integer,Integer>> listFn = PersistentQueueX.Instances.unit().unit(l1((Integer i) ->i*2)).convert(PersistentQueueX::narrowK);
        
        PersistentQueueX<Integer> list = PersistentQueueX.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> PersistentQueueX.Instances.functor().map((String v) ->v.length(), h))
                                     .applyHKT(h-> PersistentQueueX.Instances.zippingApplicative().ap(listFn, h))
                                     .convert(PersistentQueueX::narrowK);
        
        assertThat(list.toArray(),equalTo(PersistentQueueX.of("hello".length()*2).toArray()));
    }
    @Test
    public void monadSimple(){
       PersistentQueueX<Integer> list  = PersistentQueueX.Instances.monad()
                                      .flatMap(i-> PersistentQueueX.range(0,i), PersistentQueueX.of(1,2,3))
                                      .convert(PersistentQueueX::narrowK);
    }
    @Test
    public void monad(){
        
        PersistentQueueX<Integer> list = PersistentQueueX.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> PersistentQueueX.Instances.monad().flatMap((String v) -> PersistentQueueX.Instances.unit().unit(v.length()), h))
                                     .convert(PersistentQueueX::narrowK);
        
        assertThat(list.toArray(),equalTo(PersistentQueueX.of("hello".length()).toArray()));
    }
    @Test
    public void monadZeroFilter(){
        
        PersistentQueueX<String> list = PersistentQueueX.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> PersistentQueueX.Instances.monadZero().filter((String t)->t.startsWith("he"), h))
                                     .convert(PersistentQueueX::narrowK);
        
        assertThat(list.toArray(),equalTo(PersistentQueueX.of("hello").toArray()));
    }
    @Test
    public void monadZeroFilterOut(){
        
        PersistentQueueX<String> list = PersistentQueueX.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> PersistentQueueX.Instances.monadZero().filter((String t)->!t.startsWith("he"), h))
                                     .convert(PersistentQueueX::narrowK);
        
        assertThat(list.toArray(),equalTo(PersistentQueueX.empty().toArray()));
    }
    
    @Test
    public void monadPlus(){
        PersistentQueueX<Integer> list = PersistentQueueX.Instances.<Integer>monadPlus()
                                      .plus(PersistentQueueX.empty(), PersistentQueueX.of(10))
                                      .convert(PersistentQueueX::narrowK);
        assertThat(list.toArray(),equalTo(PersistentQueueX.of(10).toArray()));
    }
    @Test
    public void monadPlusNonEmpty(){
        
        Monoid<PersistentQueueX<Integer>> m = Monoid.of(PersistentQueueX.empty(), (a, b)->a.isEmpty() ? b : a);
        PersistentQueueX<Integer> list = PersistentQueueX.Instances.<Integer>monadPlus(m)
                                      .plus(PersistentQueueX.of(5), PersistentQueueX.of(10))
                                      .convert(PersistentQueueX::narrowK);
        assertThat(list.toArray(),equalTo(PersistentQueueX.of(5).toArray()));
    }
    @Test
    public void  foldLeft(){
        int sum  = PersistentQueueX.Instances.foldable()
                        .foldLeft(0, (a,b)->a+b, PersistentQueueX.of(1,2,3,4));
        
        assertThat(sum,equalTo(10));
    }
    @Test
    public void  foldRight(){
        int sum  = PersistentQueueX.Instances.foldable()
                        .foldRight(0, (a,b)->a+b, PersistentQueueX.of(1,2,3,4));
        
        assertThat(sum,equalTo(10));
    }
    
    @Test
    public void traverse(){
       Maybe<Higher<persistentQueueX, Integer>> res = PersistentQueueX.Instances.traverse()
                                                         .traverseA(Maybe.Instances.applicative(), (Integer a)->Maybe.just(a*2), PersistentQueueX.of(1,2,3))
                                                         .convert(Maybe::narrowK);


       assertThat(res.map(q-> PersistentQueueX.narrowK(q)
                                       .toArray()).orElse(null),equalTo(Maybe.just(PersistentQueueX.of(2,4,6).toArray()).orElse(null)));
    }
    
}
