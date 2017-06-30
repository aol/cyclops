package cyclops.typeclasses.cyclops;

import static cyclops.function.Lambda.l1;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.aol.cyclops2.hkt.Higher;
import cyclops.control.Maybe;
import cyclops.function.Fn1;
import cyclops.function.Lambda;
import cyclops.function.Monoid;
import cyclops.monads.Witness;
import cyclops.monads.Witness.maybe;
import org.junit.Test;



public class MaybesTest {

    @Test
    public void unit(){
        
        Maybe<String> opt = Maybe.Instances.unit()
                                            .unit("hello")
                                            .convert(Maybe::narrowK);
        
        assertThat(opt,equalTo(Maybe.of("hello")));
    }
    @Test
    public void functor(){
        
        Maybe<Integer> opt = Maybe.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h->Maybe.Instances.functor().map((String v) ->v.length(), h))
                                     .convert(Maybe::narrowK);
        
        assertThat(opt,equalTo(Maybe.of("hello".length())));
    }
    @Test
    public void apSimple(){
        Maybe.Instances.applicative()
            .ap(Maybe.of(l1(this::multiplyByTwo)),Maybe.of(1));
    }
    private int multiplyByTwo(int x){
        return x*2;
    }
    @Test
    public void applicative(){
        
        Maybe<Fn1<Integer,Integer>> optFn =Maybe.Instances.unit().unit(Lambda.l1((Integer i) ->i*2)).convert(Maybe::narrowK);
        
        Maybe<Integer> opt = Maybe.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h->Maybe.Instances.functor().map((String v) ->v.length(), h))
                                     .applyHKT(h->Maybe.Instances.applicative().ap(optFn, h))
                                     .convert(Maybe::narrowK);
        
        assertThat(opt,equalTo(Maybe.of("hello".length()*2)));
    }
    @Test
    public void monadSimple(){
       Maybe<Integer> opt  = Maybe.Instances.monad()
                                            .<Integer,Integer>flatMap(i->Maybe.of(i*2), Maybe.of(3))
                                            .convert(Maybe::narrowK);
    }
    @Test
    public void monad(){
        
        Maybe<Integer> opt = Maybe.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h->Maybe.Instances.monad().flatMap((String v) ->Maybe.Instances.unit().unit(v.length()), h))
                                     .convert(Maybe::narrowK);
        
        assertThat(opt,equalTo(Maybe.of("hello".length())));
    }
    @Test
    public void monadZeroFilter(){
        
        Maybe<String> opt = Maybe.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h->Maybe.Instances.monadZero().filter((String t)->t.startsWith("he"), h))
                                     .convert(Maybe::narrowK);
        
        assertThat(opt,equalTo(Maybe.of("hello")));
    }
    @Test
    public void monadZeroFilterOut(){
        
        Maybe<String> opt = Maybe.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h->Maybe.Instances.monadZero().filter((String t)->!t.startsWith("he"), h))
                                     .convert(Maybe::narrowK);
        
        assertThat(opt,equalTo(Maybe.none()));
    }
    
    @Test
    public void monadPlus(){
        Maybe<Integer> opt = Maybe.Instances.<Integer>monadPlus()
                                      .plus(Maybe.none(), Maybe.of(10))
                                      .convert(Maybe::narrowK);
        assertThat(opt,equalTo(Maybe.of(10)));
    }
    @Test
    public void monadPlusNonEmpty(){
        
        Monoid<Maybe<Integer>> m = Monoid.of(Maybe.none(), (a, b)->a.isPresent() ? b : a);
        Maybe<Integer> opt = Maybe.Instances.<Integer>monadPlus(m)
                                      .plus(Maybe.of(5), Maybe.of(10))
                                      .convert(Maybe::narrowK);
        assertThat(opt,equalTo(Maybe.of(10)));
    }
    @Test
    public void  foldLeft(){
        int sum  = Maybe.Instances.foldable()
                        .foldLeft(0, (a,b)->a+b, Maybe.of(4));
        
        assertThat(sum,equalTo(4));
    }
    @Test
    public void  foldRight(){
        int sum  = Maybe.Instances.foldable()
                        .foldRight(0, (a,b)->a+b,Maybe.of(1));
        
        assertThat(sum,equalTo(1));
    }
    @Test
    public void traverse(){
       Maybe<Higher<maybe, Integer>> res = Maybe.Instances.traverse()
                                                          .traverseA(Maybe.Instances.applicative(), (Integer a)->Maybe.just(a*2), Maybe.just(1))
                                                          .convert(Maybe::narrowK);
       
       
       assertThat(res.map(h->h.convert(Maybe::narrowK).get()),
                  equalTo(Maybe.just(Maybe.just(2).get())));
    }
    
}
