package cyclops.typeclasses.cyclops;

import static cyclops.function.Lambda.l1;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.aol.cyclops2.hkt.Higher;
import cyclops.collectionx.mutable.QueueX;
import cyclops.control.lazy.Maybe;
import cyclops.function.Function1;
import cyclops.function.Lambda;
import cyclops.function.Monoid;
import cyclops.control.anym.Witness.queue;
import org.junit.Test;



public class QueuesTest {

    @Test
    public void unit(){
        
        QueueX<String> list = QueueX.Instances.unit()
                                     .unit("hello")
                                     .convert(QueueX::narrowK);
        
        assertThat(list.toArray(),equalTo(QueueX.of("hello").toArray()));
    }
    @Test
    public void functor(){
        
        QueueX<Integer> list = QueueX.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h->QueueX.Instances.functor().map((String v) ->v.length(), h))
                                     .convert(QueueX::narrowK);
        
        assertThat(list.toArray(),equalTo(QueueX.of("hello".length()).toArray()));
    }
    @Test
    public void apSimple(){
        QueueX.Instances.zippingApplicative()
            .ap(QueueX.of(l1(this::multiplyByTwo)),QueueX.of(1,2,3));
    }
    private int multiplyByTwo(int x){
        return x*2;
    }
    @Test
    public void applicative(){
        
        QueueX<Function1<Integer,Integer>> listFn =QueueX.Instances.unit().unit(Lambda.l1((Integer i) ->i*2)).convert(QueueX::narrowK);
        
        QueueX<Integer> list = QueueX.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h->QueueX.Instances.functor().map((String v) ->v.length(), h))
                                     .applyHKT(h->QueueX.Instances.zippingApplicative().ap(listFn, h))
                                     .convert(QueueX::narrowK);
        
        assertThat(list.toArray(),equalTo(QueueX.of("hello".length()*2).toArray()));
    }
    @Test
    public void monadSimple(){
       QueueX<Integer> list  = QueueX.Instances.monad()
                                      .flatMap(i->QueueX.range(0,i), QueueX.of(1,2,3))
                                      .convert(QueueX::narrowK);
    }
    @Test
    public void monad(){
        
        QueueX<Integer> list = QueueX.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h->QueueX.Instances.monad().flatMap((String v) ->QueueX.Instances.unit().unit(v.length()), h))
                                     .convert(QueueX::narrowK);
        
        assertThat(list.toArray(),equalTo(QueueX.of("hello".length()).toArray()));
    }
    @Test
    public void monadZeroFilter(){
        
        QueueX<String> list = QueueX.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h->QueueX.Instances.monadZero().filter((String t)->t.startsWith("he"), h))
                                     .convert(QueueX::narrowK);
        
        assertThat(list.toArray(),equalTo(QueueX.of("hello").toArray()));
    }
    @Test
    public void monadZeroFilterOut(){
        
        QueueX<String> list = QueueX.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h->QueueX.Instances.monadZero().filter((String t)->!t.startsWith("he"), h))
                                     .convert(QueueX::narrowK);
        
        assertThat(list.toArray(),equalTo(QueueX.of().toArray()));
    }
    
    @Test
    public void monadPlus(){
        QueueX<Integer> list = QueueX.Instances.<Integer>monadPlus()
                                      .plus(QueueX.of(), QueueX.of(10))
                                      .convert(QueueX::narrowK);
        assertThat(list.toArray(),equalTo(QueueX.of(10).toArray()));
    }
    @Test
    public void monadPlusNonEmpty(){
        
        Monoid<QueueX<Integer>> m = Monoid.of(QueueX.of(), (a, b)->a.isEmpty() ? b : a);
        QueueX<Integer> list = QueueX.Instances.<Integer>monadPlus(m)
                                      .plus(QueueX.of(5),QueueX.of(10))
                                      .convert(QueueX::narrowK);
        assertThat(list.toArray(),equalTo(QueueX.of(5).toArray()));
    }
    @Test
    public void  foldLeft(){
        int sum  = QueueX.Instances.foldable()
                        .foldLeft(0, (a,b)->a+b, QueueX.of(1,2,3,4));
        
        assertThat(sum,equalTo(10));
    }
    @Test
    public void  foldRight(){
        int sum  = QueueX.Instances.foldable()
                        .foldRight(0, (a,b)->a+b, QueueX.of(1,2,3,4));
        
        assertThat(sum,equalTo(10));
    }
    @Test
    public void traverse(){
       Maybe<Higher<queue, Integer>> res = QueueX.Instances.traverse()
                                                           .traverseA(Maybe.Instances.applicative(), (Integer a)->Maybe.just(a*2), QueueX.of(1,2,3))
                                                            .convert(Maybe::narrowK);
       
       
       assertThat(res.map(h->QueueX.fromIterable(h.convert(QueueX::narrowK)).toList()),
                  equalTo(Maybe.just(QueueX.of(2,4,6).toList())));
    }
    
}
