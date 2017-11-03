package cyclops.typeclasses.cyclops;

import static cyclops.function.Lambda.l1;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.oath.cyclops.hkt.Higher;
import cyclops.collections.immutable.LinkedListX;
import cyclops.control.Maybe;
import cyclops.function.Function1;
import cyclops.function.Lambda;
import cyclops.function.Monoid;
import cyclops.monads.Witness.linkedListX;
import cyclops.typeclasses.functions.MonoidKs;
import org.junit.Test;


public class PStacksTest {

    @Test
    public void unit(){

        LinkedListX<String> list = LinkedListX.Instances.unit()
                                     .unit("hello")
                                     .convert(LinkedListX::narrowK);

        assertThat(list,equalTo(LinkedListX.of("hello")));
    }
    @Test
    public void functor(){

        LinkedListX<Integer> list = LinkedListX.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> LinkedListX.Instances.functor().map((String v) ->v.length(), h))
                                     .convert(LinkedListX::narrowK);

        assertThat(list,equalTo(LinkedListX.of("hello".length())));
    }
    @Test
    public void apSimple(){
        LinkedListX.Instances.zippingApplicative()
            .ap(LinkedListX.of(l1(this::multiplyByTwo)), LinkedListX.of(1,2,3));
    }
    private int multiplyByTwo(int x){
        return x*2;
    }
    @Test
    public void applicative(){

        LinkedListX<Function1<Integer,Integer>> listFn = LinkedListX.Instances.unit().unit(Lambda.l1((Integer i) ->i*2)).convert(LinkedListX::narrowK);

        LinkedListX<Integer> list = LinkedListX.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> LinkedListX.Instances.functor().map((String v) ->v.length(), h))
                                     .applyHKT(h-> LinkedListX.Instances.zippingApplicative().ap(listFn, h))
                                     .convert(LinkedListX::narrowK);

        assertThat(list,equalTo(LinkedListX.of("hello".length()*2)));
    }
    @Test
    public void monadSimple(){
       LinkedListX<Integer> list  = LinkedListX.Instances.monad()
                                      .flatMap(i-> LinkedListX.range(0,i), LinkedListX.of(1,2,3))
                                      .convert(LinkedListX::narrowK);
    }
    @Test
    public void monad(){

        LinkedListX<Integer> list = LinkedListX.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> LinkedListX.Instances.monad().flatMap((String v) -> LinkedListX.Instances.unit().unit(v.length()), h))
                                     .convert(LinkedListX::narrowK);

        assertThat(list,equalTo(LinkedListX.of("hello".length())));
    }
    @Test
    public void monadZeroFilter(){

        LinkedListX<String> list = LinkedListX.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> LinkedListX.Instances.monadZero().filter((String t)->t.startsWith("he"), h))
                                     .convert(LinkedListX::narrowK);

        assertThat(list,equalTo(LinkedListX.of("hello")));
    }
    @Test
    public void monadZeroFilterOut(){

        LinkedListX<String> list = LinkedListX.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> LinkedListX.Instances.monadZero().filter((String t)->!t.startsWith("he"), h))
                                     .convert(LinkedListX::narrowK);

        assertThat(list,equalTo(LinkedListX.empty()));
    }

    @Test
    public void monadPlus(){
        LinkedListX<Integer> list = LinkedListX.Instances.<Integer>monadPlus()
                                      .plus(LinkedListX.empty(), LinkedListX.of(10))
                                      .convert(LinkedListX::narrowK);
        assertThat(list,equalTo(LinkedListX.of(10)));
    }
    @Test
    public void monadPlusNonEmpty(){

        LinkedListX<Integer> list = LinkedListX.Instances.<Integer>monadPlus(MonoidKs.linkedListXConcat())
                                      .plus(LinkedListX.of(5), LinkedListX.of(10))
                                      .convert(LinkedListX::narrowK);
        assertThat(list,equalTo(LinkedListX.of(5)));
    }
    @Test
    public void  foldLeft(){
        int sum  = LinkedListX.Instances.foldable()
                        .foldLeft(0, (a,b)->a+b, LinkedListX.of(1,2,3,4));

        assertThat(sum,equalTo(10));
    }
    @Test
    public void  foldRight(){
        int sum  = LinkedListX.Instances.foldable()
                        .foldRight(0, (a,b)->a+b, LinkedListX.of(1,2,3,4));

        assertThat(sum,equalTo(10));
    }

    @Test
    public void traverse(){
       Maybe<Higher<linkedListX, Integer>> res = LinkedListX.Instances.traverse()
                                                         .traverseA(Maybe.Instances.applicative(), (Integer a)->Maybe.just(a*2), LinkedListX.of(1,2,3))
                                                         .convert(Maybe::narrowK);


       System.out.println("Res " + res);
       assertThat(res,equalTo(Maybe.just(LinkedListX.of(2,4,6))));
    }

}
