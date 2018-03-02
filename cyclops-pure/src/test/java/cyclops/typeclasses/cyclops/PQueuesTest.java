package cyclops.typeclasses.cyclops;

import static cyclops.function.Lambda.l1;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.oath.cyclops.data.ReactiveWitness;
import com.oath.cyclops.data.ReactiveWitness.persistentQueueX;
import com.oath.cyclops.hkt.Higher;
import cyclops.reactive.collections.immutable.PersistentQueueX;
import cyclops.control.Maybe;
import cyclops.function.Function1;

import cyclops.arrow.MonoidKs;
import cyclops.instances.control.MaybeInstances;
import cyclops.instances.reactive.collections.immutable.PersistentQueueXInstances;
import org.junit.Test;



public class PQueuesTest {

    @Test
    public void unit(){

        PersistentQueueX<String> list = PersistentQueueXInstances.unit()
                                     .unit("hello")
                                     .convert(PersistentQueueX::narrowK);

        assertThat(list.toArray(),equalTo(PersistentQueueX.of("hello").toArray()));
    }
    @Test
    public void functor(){

        PersistentQueueX<Integer> list = PersistentQueueXInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> PersistentQueueXInstances.functor().map((String v) ->v.length(), h))
                                     .convert(PersistentQueueX::narrowK);

        assertThat(list.toArray(),equalTo(PersistentQueueX.of("hello".length()).toArray()));
    }
    @Test
    public void apSimple(){
        PersistentQueueXInstances.zippingApplicative()
            .ap(PersistentQueueX.of(l1(this::multiplyByTwo)), PersistentQueueX.of(1,2,3));
    }
    private int multiplyByTwo(int x){
        return x*2;
    }
    @Test
    public void applicative(){

        PersistentQueueX<Function1<Integer,Integer>> listFn = PersistentQueueXInstances.unit().unit(l1((Integer i) ->i*2)).convert(PersistentQueueX::narrowK);

        PersistentQueueX<Integer> list = PersistentQueueXInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> PersistentQueueXInstances.functor().map((String v) ->v.length(), h))
                                     .applyHKT(h-> PersistentQueueXInstances.zippingApplicative().ap(listFn, h))
                                     .convert(PersistentQueueX::narrowK);

        assertThat(list.toArray(),equalTo(PersistentQueueX.of("hello".length()*2).toArray()));
    }
    @Test
    public void monadSimple(){
       PersistentQueueX<Integer> list  = PersistentQueueXInstances.monad()
                                      .flatMap(i-> PersistentQueueX.range(0,i), PersistentQueueX.of(1,2,3))
                                      .convert(PersistentQueueX::narrowK);
    }
    @Test
    public void monad(){

        PersistentQueueX<Integer> list = PersistentQueueXInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> PersistentQueueXInstances.monad().flatMap((String v) -> PersistentQueueXInstances.unit().unit(v.length()), h))
                                     .convert(PersistentQueueX::narrowK);

        assertThat(list.toArray(),equalTo(PersistentQueueX.of("hello".length()).toArray()));
    }
    @Test
    public void monadZeroFilter(){

        PersistentQueueX<String> list = PersistentQueueXInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> PersistentQueueXInstances.monadZero().filter((String t)->t.startsWith("he"), h))
                                     .convert(PersistentQueueX::narrowK);

        assertThat(list.toArray(),equalTo(PersistentQueueX.of("hello").toArray()));
    }
    @Test
    public void monadZeroFilterOut(){

        PersistentQueueX<String> list = PersistentQueueXInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> PersistentQueueXInstances.monadZero().filter((String t)->!t.startsWith("he"), h))
                                     .convert(PersistentQueueX::narrowK);

        assertThat(list.toArray(),equalTo(PersistentQueueX.empty().toArray()));
    }

    @Test
    public void monadPlus(){
        PersistentQueueX<Integer> list = PersistentQueueXInstances.<Integer>monadPlus()
                                      .plus(PersistentQueueX.empty(), PersistentQueueX.of(10))
                                      .convert(PersistentQueueX::narrowK);
        assertThat(list.toArray(),equalTo(PersistentQueueX.of(10).toArray()));
    }
    @Test
    public void monadPlusNonEmpty(){


        PersistentQueueX<Integer> list = PersistentQueueXInstances.<Integer>monadPlus(MonoidKs.persistentQueueXConcat())
                                      .plus(PersistentQueueX.of(5), PersistentQueueX.of(10))
                                      .convert(PersistentQueueX::narrowK);
        assertThat(list.toArray(),equalTo(PersistentQueueX.of(5,10).toArray()));
    }
    @Test
    public void  foldLeft(){
        int sum  = PersistentQueueXInstances.foldable()
                        .foldLeft(0, (a,b)->a+b, PersistentQueueX.of(1,2,3,4));

        assertThat(sum,equalTo(10));
    }
    @Test
    public void  foldRight(){
        int sum  = PersistentQueueXInstances.foldable()
                        .foldRight(0, (a,b)->a+b, PersistentQueueX.of(1,2,3,4));

        assertThat(sum,equalTo(10));
    }

    @Test
    public void traverse(){
       Maybe<Higher<persistentQueueX, Integer>> res = PersistentQueueXInstances.traverse()
                                                         .traverseA(MaybeInstances.applicative(), (Integer a)->Maybe.just(a*2), PersistentQueueX.of(1,2,3))
                                                         .convert(Maybe::narrowK);


       assertThat(res.map(q-> PersistentQueueX.narrowK(q)
                                       .toArray()).orElse(null),equalTo(Maybe.just(PersistentQueueX.of(2,4,6).toArray()).orElse(null)));
    }

}
