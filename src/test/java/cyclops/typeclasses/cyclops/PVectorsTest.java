package cyclops.typeclasses.cyclops;

import static cyclops.function.Lambda.l1;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.aol.cyclops2.hkt.Higher;
import cyclops.collections.immutable.PVectorX;
import cyclops.control.Maybe;
import cyclops.function.Fn1;
import cyclops.function.Lambda;
import cyclops.function.Monoid;
import org.junit.Test;



public class PVectorsTest {

    @Test
    public void unit(){
        
        PVectorX<String> list = PVectorX.Instances.unit()
                                     .unit("hello")
                                     .convert(PVectorX::narrowK);
        
        assertThat(list,equalTo(PVectorX.of("hello")));
    }
    @Test
    public void functor(){
        
        PVectorX<Integer> list = PVectorX.Instances.unit()
                                     .unit("hello")
                                     .transform(h->PVectorX.Instances.functor().map((String v) ->v.length(), h))
                                     .convert(PVectorX::narrowK);
        
        assertThat(list,equalTo(PVectorX.of("hello".length())));
    }
    @Test
    public void apSimple(){
        PVectorX.Instances.zippingApplicative()
            .ap(PVectorX.of(l1(this::multiplyByTwo)),PVectorX.of(1,2,3));
    }
    private int multiplyByTwo(int x){
        return x*2;
    }
    @Test
    public void applicative(){
        
        PVectorX<Fn1<Integer,Integer>> listFn =PVectorX.Instances.unit().unit(Lambda.l1((Integer i) ->i*2)).convert(PVectorX::narrowK);
        
        PVectorX<Integer> list = PVectorX.Instances.unit()
                                     .unit("hello")
                                     .transform(h->PVectorX.Instances.functor().map((String v) ->v.length(), h))
                                     .transform(h->PVectorX.Instances.zippingApplicative().ap(listFn, h))
                                     .convert(PVectorX::narrowK);
        
        assertThat(list,equalTo(PVectorX.of("hello".length()*2)));
    }
    @Test
    public void monadSimple(){
       PVectorX<Integer> list  = PVectorX.Instances.monad()
                                      .flatMap(i->PVectorX.range(0,i), PVectorX.of(1,2,3))
                                      .convert(PVectorX::narrowK);
    }
    @Test
    public void monad(){
        
        PVectorX<Integer> list = PVectorX.Instances.unit()
                                     .unit("hello")
                                     .transform(h->PVectorX.Instances.monad().flatMap((String v) ->PVectorX.Instances.unit().unit(v.length()), h))
                                     .convert(PVectorX::narrowK);
        
        assertThat(list,equalTo(PVectorX.of("hello".length())));
    }
    @Test
    public void monadZeroFilter(){
        
        PVectorX<String> list = PVectorX.Instances.unit()
                                     .unit("hello")
                                     .transform(h->PVectorX.Instances.monadZero().filter((String t)->t.startsWith("he"), h))
                                     .convert(PVectorX::narrowK);
        
        assertThat(list,equalTo(PVectorX.of("hello")));
    }
    @Test
    public void monadZeroFilterOut(){
        
        PVectorX<String> list = PVectorX.Instances.unit()
                                     .unit("hello")
                                     .transform(h->PVectorX.Instances.monadZero().filter((String t)->!t.startsWith("he"), h))
                                     .convert(PVectorX::narrowK);
        
        assertThat(list,equalTo(PVectorX.empty()));
    }
    
    @Test
    public void monadPlus(){
        PVectorX<Integer> list = PVectorX.Instances.<Integer>monadPlus()
                                      .plus(PVectorX.empty(), PVectorX.of(10))
                                      .convert(PVectorX::narrowK);
        assertThat(list,equalTo(PVectorX.of(10)));
    }
    @Test
    public void monadPlusNonEmpty(){
        
        Monoid<PVectorX<Integer>> m = Monoid.of(PVectorX.empty(), (a, b)->a.isEmpty() ? b : a);
        PVectorX<Integer> list = PVectorX.Instances.<Integer>monadPlus(m)
                                      .plus(PVectorX.of(5), PVectorX.of(10))
                                      .convert(PVectorX::narrowK);
        assertThat(list,equalTo(PVectorX.of(5)));
    }
    @Test
    public void  foldLeft(){
        int sum  = PVectorX.Instances.foldable()
                        .foldLeft(0, (a,b)->a+b, PVectorX.of(1,2,3,4));
        
        assertThat(sum,equalTo(10));
    }
    @Test
    public void  foldRight(){
        int sum  = PVectorX.Instances.foldable()
                        .foldRight(0, (a,b)->a+b, PVectorX.of(1,2,3,4));
        
        assertThat(sum,equalTo(10));
    }
    
    @Test
    public void traverse(){
       Maybe<Higher<PVectorX.µ, Integer>> res = PVectorX.Instances.traverse()
                                                         .traverseA(Maybe.Instances.applicative(), (Integer a)->Maybe.just(a*2), PVectorX.of(1,2,3))
                                                         .convert(Maybe::narrowK);
       
       
       assertThat(res,equalTo(Maybe.just(PVectorX.of(2,4,6))));
    }
    
}
