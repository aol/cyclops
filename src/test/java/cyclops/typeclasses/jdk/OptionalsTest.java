package com.aol.cyclops.hkt.instances.jdk;

import static cyclops.function.Lambda.l1;
import static cyclops.higherkindedtypes.OptionalKind.widen;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Optional;

import com.aol.cyclops2.hkt.Higher;
import cyclops.Optionals;
import cyclops.control.Maybe;
import cyclops.function.Fn1;
import cyclops.function.Lambda;
import cyclops.function.Monoid;
import cyclops.higherkindedtypes.OptionalKind;
import org.junit.Test;



public class OptionalsTest {

    @Test
    public void unit(){
        
        OptionalKind<String> opt = Optionals.Instances.unit()
                                            .unit("hello")
                                            .convert(OptionalKind::narrow);
        
        assertThat(opt,equalTo(Optional.of("hello")));
    }
    @Test
    public void functor(){
        
        OptionalKind<Integer> opt = Optionals.Instances.unit()
                                     .unit("hello")
                                     .apply(h->Optionals.Instances.functor().map((String v) ->v.length(), h))
                                     .convert(OptionalKind::narrow);
        
        assertThat(opt,equalTo(Optional.of("hello".length())));
    }
    @Test
    public void apSimple(){
        Optionals.Instances.applicative()
            .ap(widen(Optional.of(l1(this::multiplyByTwo))),widen(Optional.of(1)));
    }
    private int multiplyByTwo(int x){
        return x*2;
    }
    @Test
    public void applicative(){
        
        OptionalKind<Fn1<Integer,Integer>> optFn =Optionals.Instances.unit().unit(Lambda.l1((Integer i) ->i*2)).convert(OptionalKind::narrow);
        
        OptionalKind<Integer> opt = Optionals.Instances.unit()
                                     .unit("hello")
                                     .apply(h->Optionals.Instances.functor().map((String v) ->v.length(), h))
                                     .apply(h->Optionals.Instances.applicative().ap(optFn, h))
                                     .convert(OptionalKind::narrow);
        
        assertThat(opt,equalTo(Optional.of("hello".length()*2)));
    }
    @Test
    public void monadSimple(){
       OptionalKind<Integer> opt  = Optionals.Instances.monad()
                                            .<Integer,Integer>flatMap(i->widen(Optional.of(i*2)), widen(Optional.of(3)))
                                            .convert(OptionalKind::narrow);
    }
    @Test
    public void monad(){
        
        OptionalKind<Integer> opt = Optionals.Instances.unit()
                                     .unit("hello")
                                     .apply(h->Optionals.Instances.monad().flatMap((String v) ->Optionals.Instances.unit().unit(v.length()), h))
                                     .convert(OptionalKind::narrow);
        
        assertThat(opt,equalTo(Optional.of("hello".length())));
    }
    @Test
    public void monadZeroFilter(){
        
        OptionalKind<String> opt = Optionals.Instances.unit()
                                     .unit("hello")
                                     .apply(h->Optionals.Instances.monadZero().filter((String t)->t.startsWith("he"), h))
                                     .convert(OptionalKind::narrow);
        
        assertThat(opt,equalTo(Optional.of("hello")));
    }
    @Test
    public void monadZeroFilterOut(){
        
        OptionalKind<String> opt = Optionals.Instances.unit()
                                     .unit("hello")
                                     .apply(h->Optionals.Instances.monadZero().filter((String t)->!t.startsWith("he"), h))
                                     .convert(OptionalKind::narrow);
        
        assertThat(opt,equalTo(Optional.empty()));
    }
    
    @Test
    public void monadPlus(){
        OptionalKind<Integer> opt = Optionals.Instances.<Integer>monadPlus()
                                      .plus(widen(Optional.empty()), widen(Optional.of(10)))
                                      .convert(OptionalKind::narrow);
        assertThat(opt,equalTo(Optional.of(10)));
    }
    @Test
    public void monadPlusNonEmpty(){
        
        Monoid<OptionalKind<Integer>> m = Monoid.of(widen(Optional.empty()), (a, b)->a.isPresent() ? b : a);
        OptionalKind<Integer> opt = Optionals.Instances.<Integer>monadPlus(m)
                                      .plus(widen(Optional.of(5)), widen(Optional.of(10)))
                                      .convert(OptionalKind::narrow);
        assertThat(opt,equalTo(Optional.of(10)));
    }
    @Test
    public void  foldLeft(){
        int sum  = Optionals.Instances.foldable()
                        .foldLeft(0, (a,b)->a+b, widen(Optional.of(4)));
        
        assertThat(sum,equalTo(4));
    }
    @Test
    public void  foldRight(){
        int sum  = Optionals.Instances.foldable()
                        .foldRight(0, (a,b)->a+b, widen(Optional.of(1)));
        
        assertThat(sum,equalTo(1));
    }
    @Test
    public void traverse(){
       Maybe<Higher<OptionalKind.µ, Integer>> res = Optionals.Instances.traverse()
                                                                         .traverseA(Maybe.Instances.applicative(), (Integer a)-> Maybe.just(a*2), OptionalKind.of(1))
                                                                         .convert(Maybe::narrowK);
       
       
       assertThat(res,equalTo(Maybe.just(Optional.of(2))));
    }
    
}
