package cyclops.typeclasses.cyclops;

import static cyclops.function.Lambda.l1;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.oath.cyclops.hkt.Higher;
import cyclops.collections.mutable.QueueX;
import cyclops.control.Maybe;
import cyclops.function.Function1;
import cyclops.function.Lambda;
import com.oath.cyclops.hkt.DataWitness.queue;
import cyclops.typeclasses.functions.MonoidKs;
import org.junit.Test;



public class QueuesTest {

    @Test
    public void unit(){

        QueueX<String> list = QueueX.QueueXInstances.unit()
                                     .unit("hello")
                                     .convert(QueueX::narrowK);

        assertThat(list.toArray(),equalTo(QueueX.of("hello").toArray()));
    }
    @Test
    public void functor(){

        QueueX<Integer> list = QueueX.QueueXInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> QueueX.QueueXInstances.functor().map((String v) ->v.length(), h))
                                     .convert(QueueX::narrowK);

        assertThat(list.toArray(),equalTo(QueueX.of("hello".length()).toArray()));
    }
    @Test
    public void apSimple(){
        QueueX.QueueXInstances.zippingApplicative()
            .ap(QueueX.of(l1(this::multiplyByTwo)),QueueX.of(1,2,3));
    }
    private int multiplyByTwo(int x){
        return x*2;
    }
    @Test
    public void applicative(){

        QueueX<Function1<Integer,Integer>> listFn = QueueX.QueueXInstances.unit().unit(Lambda.l1((Integer i) ->i*2)).convert(QueueX::narrowK);

        QueueX<Integer> list = QueueX.QueueXInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> QueueX.QueueXInstances.functor().map((String v) ->v.length(), h))
                                     .applyHKT(h-> QueueX.QueueXInstances.zippingApplicative().ap(listFn, h))
                                     .convert(QueueX::narrowK);

        assertThat(list.toArray(),equalTo(QueueX.of("hello".length()*2).toArray()));
    }
    @Test
    public void monadSimple(){
       QueueX<Integer> list  = QueueX.QueueXInstances.monad()
                                      .flatMap(i->QueueX.range(0,i), QueueX.of(1,2,3))
                                      .convert(QueueX::narrowK);
    }
    @Test
    public void monad(){

        QueueX<Integer> list = QueueX.QueueXInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> QueueX.QueueXInstances.monad().flatMap((String v) -> QueueX.QueueXInstances.unit().unit(v.length()), h))
                                     .convert(QueueX::narrowK);

        assertThat(list.toArray(),equalTo(QueueX.of("hello".length()).toArray()));
    }
    @Test
    public void monadZeroFilter(){

        QueueX<String> list = QueueX.QueueXInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> QueueX.QueueXInstances.monadZero().filter((String t)->t.startsWith("he"), h))
                                     .convert(QueueX::narrowK);

        assertThat(list.toArray(),equalTo(QueueX.of("hello").toArray()));
    }
    @Test
    public void monadZeroFilterOut(){

        QueueX<String> list = QueueX.QueueXInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> QueueX.QueueXInstances.monadZero().filter((String t)->!t.startsWith("he"), h))
                                     .convert(QueueX::narrowK);

        assertThat(list.toArray(),equalTo(QueueX.of().toArray()));
    }

    @Test
    public void monadPlus(){
        QueueX<Integer> list = QueueX.QueueXInstances.<Integer>monadPlus()
                                      .plus(QueueX.of(), QueueX.of(10))
                                      .convert(QueueX::narrowK);
        assertThat(list.toArray(),equalTo(QueueX.of(10).toArray()));
    }
    @Test
    public void monadPlusNonEmpty(){

        QueueX<Integer> list = QueueX.QueueXInstances.<Integer>monadPlus(MonoidKs.queueXConcat())
                                      .plus(QueueX.of(5),QueueX.of(10))
                                      .convert(QueueX::narrowK);
        assertThat(list.toArray(),equalTo(QueueX.of(5,10).toArray()));
    }
    @Test
    public void  foldLeft(){
        int sum  = QueueX.QueueXInstances.foldable()
                        .foldLeft(0, (a,b)->a+b, QueueX.of(1,2,3,4));

        assertThat(sum,equalTo(10));
    }
    @Test
    public void  foldRight(){
        int sum  = QueueX.QueueXInstances.foldable()
                        .foldRight(0, (a,b)->a+b, QueueX.of(1,2,3,4));

        assertThat(sum,equalTo(10));
    }
    @Test
    public void traverse(){
       Maybe<Higher<queue, Integer>> res = QueueX.QueueXInstances.traverse()
                                                           .traverseA(Maybe.MaybeInstances.applicative(), (Integer a)->Maybe.just(a*2), QueueX.of(1,2,3))
                                                            .convert(Maybe::narrowK);


       assertThat(res.map(h->QueueX.fromIterable(h.convert(QueueX::narrowK)).toList()),
                  equalTo(Maybe.just(QueueX.of(2,4,6).toList())));
    }

}
