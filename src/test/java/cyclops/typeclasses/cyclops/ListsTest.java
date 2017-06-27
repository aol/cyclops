package cyclops.typeclasses.cyclops;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import com.aol.cyclops2.hkt.Higher;
import cyclops.collections.mutable.ListX;
import cyclops.control.Maybe;
import cyclops.function.Fn1;
import cyclops.function.Lambda;
import cyclops.function.Monoid;
import org.junit.Test;



public class ListsTest {

    @Test
    public void unit(){
        
        ListX<String> list = ListX.Instances.unit()
                                     .unit("hello")
                                     .convert(ListX::narrowK);
        
        assertThat(list,equalTo(Arrays.asList("hello")));
    }
    @Test
    public void functor(){
        
        ListX<Integer> list = ListX.Instances.unit()
                                     .unit("hello")
                                     .apply(h->ListX.Instances.functor().map((String v) ->v.length(), h))
                                     .convert(ListX::narrowK);
        
        assertThat(list,equalTo(Arrays.asList("hello".length())));
    }
    @Test
    public void apSimple(){
        ListX.Instances.zippingApplicative()
            .ap(ListX.of(Lambda.l1(this::multiplyByTwo)),ListX.of(1,2,3));
    }
    private int multiplyByTwo(int x){
        return x*2;
    }
    @Test
    public void applicative(){
        
        ListX<Fn1<Integer,Integer>> listFn =ListX.Instances.unit().unit(Lambda.l1((Integer i) ->i*2)).convert(ListX::narrowK);
        
        ListX<Integer> list = ListX.Instances.unit()
                                     .unit("hello")
                                     .apply(h->ListX.Instances.functor().map((String v) ->v.length(), h))
                                     .apply(h->ListX.Instances.zippingApplicative().ap(listFn, h))
                                     .convert(ListX::narrowK);
        
        assertThat(list,equalTo(Arrays.asList("hello".length()*2)));
    }
    @Test
    public void monadSimple(){
       ListX<Integer> list  = ListX.Instances.monad()
                                      .flatMap(i->ListX.range(0,i),ListX.of(1,2,3))
                                      .convert(ListX::narrowK);
    }
    @Test
    public void monad(){
        
        ListX<Integer> list = ListX.Instances.unit()
                                     .unit("hello")
                                     .apply(h->ListX.Instances.monad().flatMap((String v) ->ListX.Instances.unit().unit(v.length()), h))
                                     .convert(ListX::narrowK);
        
        assertThat(list,equalTo(Arrays.asList("hello".length())));
    }
    @Test
    public void monadZeroFilter(){
        
        ListX<String> list = ListX.Instances.unit()
                                     .unit("hello")
                                     .apply(h->ListX.Instances.monadZero().filter((String t)->t.startsWith("he"), h))
                                     .convert(ListX::narrowK);
        
        assertThat(list,equalTo(Arrays.asList("hello")));
    }
    @Test
    public void monadZeroFilterOut(){
        
        ListX<String> list = ListX.Instances.unit()
                                     .unit("hello")
                                     .apply(h->ListX.Instances.monadZero().filter((String t)->!t.startsWith("he"), h))
                                     .convert(ListX::narrowK);
        
        assertThat(list,equalTo(Arrays.asList()));
    }
    
    @Test
    public void monadPlus(){
        ListX<Integer> list = ListX.Instances.<Integer>monadPlus()
                                      .plus(ListX.of(), ListX.of(10))
                                      .convert(ListX::narrowK);
        assertThat(list,equalTo(Arrays.asList(10)));
    }
    @Test
    public void monadPlusNonEmpty(){
        
        Monoid<ListX<Integer>> m = Monoid.of(ListX.empty(), (a, b)->a.isEmpty() ? b : a);
        ListX<Integer> list = ListX.Instances.<Integer>monadPlus(m)
                                      .plus(ListX.of(5), ListX.of(10))
                                      .convert(ListX::narrowK);
        assertThat(list,equalTo(Arrays.asList(5)));
    }
    @Test
    public void  foldLeft(){
        int sum  = ListX.Instances.foldable()
                        .foldLeft(0, (a,b)->a+b, ListX.of(1,2,3,4));
        
        assertThat(sum,equalTo(10));
    }
    @Test
    public void  foldRight(){
        int sum  = ListX.Instances.foldable()
                        .foldRight(0, (a,b)->a+b, ListX.of(1,2,3,4));
        
        assertThat(sum,equalTo(10));
    }
    
    @Test
    public void traverse(){
       Maybe<Higher<ListX.Mu, Integer>> res = ListX.Instances.traverse()
                                                         .traverseA(Maybe.Instances.applicative(), (Integer a)->Maybe.just(a*2), ListX.of(1,2,3))
                                                         .convert(Maybe::narrowK);
       
       
       assertThat(res,equalTo(Maybe.just(ListX.of(2,4,6))));
    }
    
}
