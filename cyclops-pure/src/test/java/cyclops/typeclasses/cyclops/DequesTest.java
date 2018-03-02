package cyclops.typeclasses.cyclops;

import static cyclops.function.Lambda.l1;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.oath.cyclops.data.ReactiveWitness;
import com.oath.cyclops.data.ReactiveWitness.deque;
import com.oath.cyclops.hkt.Higher;
import cyclops.reactive.collections.mutable.DequeX;
import cyclops.control.Maybe;
import cyclops.function.Function1;
import cyclops.function.Lambda;
import cyclops.instances.control.MaybeInstances;
import cyclops.instances.reactive.collections.mutable.DequeXInstances;
import cyclops.arrow.MonoidKs;
import org.junit.Test;



public class DequesTest {

    @Test
    public void unit(){

        DequeX<String> list = DequeXInstances.unit()
                                     .unit("hello")
                                     .convert(DequeX::narrowK);

        assertThat(list.toArray(),equalTo(DequeX.of("hello").toArray()));
    }
    @Test
    public void functor(){

        DequeX<Integer> list = DequeXInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> DequeXInstances.functor().map((String v) ->v.length(), h))
                                     .convert(DequeX::narrowK);

        assertThat(list.toArray(),equalTo(DequeX.of("hello".length()).toArray()));
    }
    @Test
    public void apSimple(){
        DequeXInstances.zippingApplicative()
            .ap(DequeX.of(l1(this::multiplyByTwo)),DequeX.of(1,2,3));
    }
    private int multiplyByTwo(int x){
        return x*2;
    }
    @Test
    public void applicative(){

        DequeX<Function1<Integer,Integer>> listFn = DequeXInstances.unit().unit(Lambda.l1((Integer i) ->i*2)).convert(DequeX::narrowK);

        DequeX<Integer> list = DequeXInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> DequeXInstances.functor().map((String v) ->v.length(), h))
                                     .applyHKT(h-> DequeXInstances.zippingApplicative().ap(listFn, h))
                                     .convert(DequeX::narrowK);

        assertThat(list.toArray(),equalTo(DequeX.of("hello".length()*2).toArray()));
    }
    @Test
    public void monadSimple(){
       DequeX<Integer> list  = DequeXInstances.monad()
                                      .flatMap(i->DequeX.range(0,i), DequeX.of(1,2,3))
                                      .convert(DequeX::narrowK);
    }
    @Test
    public void monad(){

        DequeX<Integer> list = DequeXInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> DequeXInstances.monad().flatMap((String v) -> DequeXInstances.unit().unit(v.length()), h))
                                     .convert(DequeX::narrowK);

        assertThat(list.toArray(),equalTo(DequeX.of("hello".length()).toArray()));
    }
    @Test
    public void monadZeroFilter(){

        DequeX<String> list = DequeXInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> DequeXInstances.monadZero().filter((String t)->t.startsWith("he"), h))
                                     .convert(DequeX::narrowK);

        assertThat(list.toArray(),equalTo(DequeX.of("hello").toArray()));
    }
    @Test
    public void monadZeroFilterOut(){

        DequeX<String> list = DequeXInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> DequeXInstances.monadZero().filter((String t)->!t.startsWith("he"), h))
                                     .convert(DequeX::narrowK);

        assertThat(list.toArray(),equalTo(DequeX.of().toArray()));
    }

    @Test
    public void monadPlus(){
        DequeX<Integer> list = DequeXInstances.<Integer>monadPlus()
                                      .plus(DequeX.of(), DequeX.of(10))
                                      .convert(DequeX::narrowK);
        assertThat(list.toArray(),equalTo(DequeX.of(10).toArray()));
    }
    @Test
    public void monadPlusNonEmpty(){


        DequeX<Integer> list = DequeXInstances.<Integer>monadPlus(MonoidKs.dequeXConcat())
                                      .plus(DequeX.of(5), DequeX.of(10))
                                      .convert(DequeX::narrowK);
        assertThat(list.toArray(),equalTo(DequeX.of(5,10).toArray()));
    }
    @Test
    public void  foldLeft(){
        int sum  = DequeXInstances.foldable()
                        .foldLeft(0, (a,b)->a+b, DequeX.of(1,2,3,4));

        assertThat(sum,equalTo(10));
    }
    @Test
    public void  foldRight(){
        int sum  = DequeXInstances.foldable()
                        .foldRight(0, (a,b)->a+b, DequeX.of(1,2,3,4));

        assertThat(sum,equalTo(10));
    }
    @Test
    public void traverse(){
       Maybe<Higher<deque, Integer>> res = DequeXInstances.traverse()
                                                           .traverseA(MaybeInstances.applicative(), (Integer a)->Maybe.just(a*2), DequeX.of(1,2,3))
                                                            .convert(Maybe::narrowK);


       assertThat(res.map(h->DequeX.fromIterable(h.convert(DequeX::narrowK)).toList()),
                  equalTo(Maybe.just(DequeX.of(2,4,6).toList())));
    }

}
