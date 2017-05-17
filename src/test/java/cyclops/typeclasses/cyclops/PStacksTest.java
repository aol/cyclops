package cyclops.typeclasses.cyclops;

import static cyclops.function.Lambda.l1;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.aol.cyclops2.hkt.Higher;
import cyclops.collections.immutable.PStackX;
import cyclops.control.lazy.Maybe;
import cyclops.function.Fn1;
import cyclops.function.Lambda;
import cyclops.function.Monoid;
import org.junit.Test;


public class PStacksTest {

    @Test
    public void unit(){
        
        PStackX<String> list = PStackX.Instances.unit()
                                     .unit("hello")
                                     .convert(PStackX::narrowK);
        
        assertThat(list,equalTo(PStackX.of("hello")));
    }
    @Test
    public void functor(){
        
        PStackX<Integer> list = PStackX.Instances.unit()
                                     .unit("hello")
                                     .apply(h->PStackX.Instances.functor().map((String v) ->v.length(), h))
                                     .convert(PStackX::narrowK);
        
        assertThat(list,equalTo(PStackX.of("hello".length())));
    }
    @Test
    public void apSimple(){
        PStackX.Instances.zippingApplicative()
            .ap(PStackX.of(l1(this::multiplyByTwo)),PStackX.of(1,2,3));
    }
    private int multiplyByTwo(int x){
        return x*2;
    }
    @Test
    public void applicative(){
        
        PStackX<Fn1<Integer,Integer>> listFn =PStackX.Instances.unit().unit(Lambda.l1((Integer i) ->i*2)).convert(PStackX::narrowK);
        
        PStackX<Integer> list = PStackX.Instances.unit()
                                     .unit("hello")
                                     .apply(h->PStackX.Instances.functor().map((String v) ->v.length(), h))
                                     .apply(h->PStackX.Instances.zippingApplicative().ap(listFn, h))
                                     .convert(PStackX::narrowK);
        
        assertThat(list,equalTo(PStackX.of("hello".length()*2)));
    }
    @Test
    public void monadSimple(){
       PStackX<Integer> list  = PStackX.Instances.monad()
                                      .flatMap(i->PStackX.range(0,i), PStackX.of(1,2,3))
                                      .convert(PStackX::narrowK);
    }
    @Test
    public void monad(){
        
        PStackX<Integer> list = PStackX.Instances.unit()
                                     .unit("hello")
                                     .apply(h->PStackX.Instances.monad().flatMap((String v) ->PStackX.Instances.unit().unit(v.length()), h))
                                     .convert(PStackX::narrowK);
        
        assertThat(list,equalTo(PStackX.of("hello".length())));
    }
    @Test
    public void monadZeroFilter(){
        
        PStackX<String> list = PStackX.Instances.unit()
                                     .unit("hello")
                                     .apply(h->PStackX.Instances.monadZero().filter((String t)->t.startsWith("he"), h))
                                     .convert(PStackX::narrowK);
        
        assertThat(list,equalTo(PStackX.of("hello")));
    }
    @Test
    public void monadZeroFilterOut(){
        
        PStackX<String> list = PStackX.Instances.unit()
                                     .unit("hello")
                                     .apply(h->PStackX.Instances.monadZero().filter((String t)->!t.startsWith("he"), h))
                                     .convert(PStackX::narrowK);
        
        assertThat(list,equalTo(PStackX.empty()));
    }
    
    @Test
    public void monadPlus(){
        PStackX<Integer> list = PStackX.Instances.<Integer>monadPlus()
                                      .plus(PStackX.empty(), PStackX.of(10))
                                      .convert(PStackX::narrowK);
        assertThat(list,equalTo(PStackX.of(10)));
    }
    @Test
    public void monadPlusNonEmpty(){
        
        Monoid<PStackX<Integer>> m = Monoid.of(PStackX.empty(), (a, b)->a.isEmpty() ? b : a);
        PStackX<Integer> list = PStackX.Instances.<Integer>monadPlus(m)
                                      .plus(PStackX.of(5), PStackX.of(10))
                                      .convert(PStackX::narrowK);
        assertThat(list,equalTo(PStackX.of(5)));
    }
    @Test
    public void  foldLeft(){
        int sum  = PStackX.Instances.foldable()
                        .foldLeft(0, (a,b)->a+b, PStackX.of(1,2,3,4));
        
        assertThat(sum,equalTo(10));
    }
    @Test
    public void  foldRight(){
        int sum  = PStackX.Instances.foldable()
                        .foldRight(0, (a,b)->a+b, PStackX.of(1,2,3,4));
        
        assertThat(sum,equalTo(10));
    }
    
    @Test
    public void traverse(){
       Maybe<Higher<PStackX.µ, Integer>> res = PStackX.Instances.traverse()
                                                         .traverseA(Maybe.Instances.applicative(), (Integer a)->Maybe.just(a*2), PStackX.of(1,2,3))
                                                         .convert(Maybe::narrowK);
       
       
       assertThat(res,equalTo(Maybe.just(PStackX.of(2,4,6))));
    }
    
}
