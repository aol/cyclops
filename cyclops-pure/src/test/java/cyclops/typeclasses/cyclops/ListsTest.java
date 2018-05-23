package cyclops.typeclasses.cyclops;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import com.oath.cyclops.data.ReactiveWitness;
import com.oath.cyclops.data.ReactiveWitness.list;
import com.oath.cyclops.hkt.DataWitness;
import com.oath.cyclops.hkt.DataWitness.nonEmptyList;
import com.oath.cyclops.hkt.DataWitness.vector;
import com.oath.cyclops.hkt.Higher;
import cyclops.data.NonEmptyList;
import cyclops.data.Seq;
import cyclops.data.Vector;
import cyclops.instances.data.VectorInstances;
import cyclops.reactive.collections.mutable.ListX;
import cyclops.control.Maybe;
import cyclops.function.Function1;
import cyclops.function.Lambda;
import cyclops.arrow.MonoidKs;
import cyclops.instances.control.MaybeInstances;
import cyclops.instances.reactive.collections.mutable.ListXInstances;
import cyclops.typeclasses.functor.Functor;
import org.junit.Test;



public class ListsTest {

    @Test
    public void nelTest(){
        Higher<vector, Seq<Integer>> hktInts = Vector.of(Seq.of(10));
        Functor<vector> functor = VectorInstances.functor();
        Higher<vector, Seq<String>> hktStrings = functor.map(s -> s.map(i -> i.toString()),hktInts);
      }
    @Test
    public void unit(){

        ListX<String> list = ListXInstances.unit()
                                     .unit("hello")
                                     .convert(ListX::narrowK);

        assertThat(list,equalTo(Arrays.asList("hello")));
    }
    @Test
    public void functor(){

        ListX<Integer> list = ListXInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> ListXInstances.functor().map((String v) ->v.length(), h))
                                     .convert(ListX::narrowK);

        assertThat(list,equalTo(Arrays.asList("hello".length())));
    }
    @Test
    public void apSimple(){
        ListXInstances.zippingApplicative()
            .ap(ListX.of(Lambda.l1(this::multiplyByTwo)),ListX.of(1,2,3));
    }
    private int multiplyByTwo(int x){
        return x*2;
    }
    @Test
    public void applicative(){

        ListX<Function1<Integer,Integer>> listFn = ListXInstances.unit().unit(Lambda.l1((Integer i) ->i*2)).convert(ListX::narrowK);

        ListX<Integer> list = ListXInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> ListXInstances.functor().map((String v) ->v.length(), h))
                                     .applyHKT(h-> ListXInstances.zippingApplicative().ap(listFn, h))
                                     .convert(ListX::narrowK);

        assertThat(list,equalTo(Arrays.asList("hello".length()*2)));
    }
    @Test
    public void monadSimple(){

       ListX<Integer> list  = ListXInstances.monad()
                                      .flatMap(i->ListX.range(0,i),ListX.of(1,2,3))
                                      .convert(ListX::narrowK);
    }
    @Test
    public void functorSimple(){

        Functor<list> functor = ListXInstances.functor();
        Higher<list, Integer> hkt = functor.map(i -> i * 2, ListX.of(1, 2, 3));
        ListX<Integer> list =  hkt.convert(ListX::narrowK);

    }
    @Test
    public void monad(){

        ListX<Integer> list = ListXInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> ListXInstances.monad().flatMap((String v) -> ListXInstances.unit().unit(v.length()), h))
                                     .convert(ListX::narrowK);

        assertThat(list,equalTo(Arrays.asList("hello".length())));
    }
    @Test
    public void monadZeroFilter(){

        ListX<String> list = ListXInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> ListXInstances.monadZero().filter((String t)->t.startsWith("he"), h))
                                     .convert(ListX::narrowK);

        assertThat(list,equalTo(Arrays.asList("hello")));
    }
    @Test
    public void monadZeroFilterOut(){

        ListX<String> list = ListXInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> ListXInstances.monadZero().filter((String t)->!t.startsWith("he"), h))
                                     .convert(ListX::narrowK);

        assertThat(list,equalTo(Arrays.asList()));
    }

    @Test
    public void monadPlus(){
        ListX<Integer> list = ListXInstances.<Integer>monadPlus()
                                      .plus(ListX.of(), ListX.of(10))
                                      .convert(ListX::narrowK);
        assertThat(list,equalTo(Arrays.asList(10)));
    }
    @Test
    public void monadPlusNonEmpty(){

        ListX<Integer> list = ListXInstances.<Integer>monadPlus(MonoidKs.listXConcat())
                                      .plus(ListX.of(5), ListX.of(10))
                                      .convert(ListX::narrowK);
        assertThat(list,equalTo(Arrays.asList(5,10)));
    }
    @Test
    public void  foldLeft(){
        int sum  = ListXInstances.foldable()
                        .foldLeft(0, (a,b)->a+b, ListX.of(1,2,3,4));

        assertThat(sum,equalTo(10));
    }
    @Test
    public void  foldRight(){
        int sum  = ListXInstances.foldable()
                        .foldRight(0, (a,b)->a+b, ListX.of(1,2,3,4));

        assertThat(sum,equalTo(10));
    }

    @Test
    public void traverse(){
       Maybe<Higher<list, Integer>> res = ListXInstances.traverse()
                                                         .traverseA(MaybeInstances.applicative(), (Integer a)->Maybe.just(a*2), ListX.of(1,2,3))
                                                         .convert(Maybe::narrowK);


       assertThat(res,equalTo(Maybe.just(ListX.of(2,4,6))));
    }

}
