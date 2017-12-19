package cyclops.typeclasses.jdk;

import static cyclops.companion.Optionals.OptionalKind.widen;
import static cyclops.function.Lambda.l1;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Optional;

import com.oath.cyclops.hkt.Higher;
import cyclops.companion.Optionals.OptionalKind;
import cyclops.control.Maybe;
import cyclops.function.Function1;
import cyclops.function.Lambda;

import com.oath.cyclops.hkt.DataWitness.optional;
import cyclops.arrow.MonoidKs;
import cyclops.typeclasses.functor.Functor;
import org.junit.Test;



public class OptionalsTest {

    @Test
    public void unit(){

        OptionalKind<String> opt = OptionalInstances.unit()
                                            .unit("hello")
                                            .convert(OptionalKind::narrow);

        assertThat(opt,equalTo(Optional.of("hello")));
    }
    @Test
    public void functor(){

        OptionalKind<Integer> opt = OptionalInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h->OptionalInstances.functor().map((String v) ->v.length(), h))
                                     .convert(OptionalKind::narrow);

        assertThat(opt,equalTo(Optional.of("hello".length())));
    }
    @Test
    public void apSimple(){
        OptionalInstances.applicative()
            .ap(widen(Optional.of(l1(this::multiplyByTwo))),widen(Optional.of(1)));
    }
    private int multiplyByTwo(int x){
        return x*2;
    }
    @Test
    public void applicative(){

        OptionalKind<Function1<Integer,Integer>> optFn =OptionalInstances.unit().unit(Lambda.l1((Integer i) ->i*2)).convert(OptionalKind::narrow);

        OptionalKind<Integer> opt = OptionalInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h->OptionalInstances.functor().map((String v) ->v.length(), h))
                                     .applyHKT(h->OptionalInstances.applicative().ap(optFn, h))
                                     .convert(OptionalKind::narrow);

        assertThat(opt,equalTo(Optional.of("hello".length()*2)));
    }
    @Test
    public void monadSimple(){
       OptionalKind<Integer> opt  = OptionalInstances.monad()
                                            .<Integer,Integer>flatMap(i->widen(Optional.of(i*2)), widen(Optional.of(3)))
                                            .convert(OptionalKind::narrow);
    }

    @Test
    public void functorSimple(){

        Functor<optional> functor = OptionalInstances.functor();
        Higher<optional, Integer> hkt = functor.map(i -> i * 2, OptionalKind.widen(Optional.of(3)));
        Optional<Integer> opt = OptionalKind.narrowK(hkt);
    }

    @Test
    public void monad(){

        OptionalKind<Integer> opt = OptionalInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h->OptionalInstances.monad().flatMap((String v) ->OptionalInstances.unit().unit(v.length()), h))
                                     .convert(OptionalKind::narrow);

        assertThat(opt,equalTo(Optional.of("hello".length())));
    }
    @Test
    public void monadZeroFilter(){

        OptionalKind<String> opt = OptionalInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h->OptionalInstances.monadZero().filter((String t)->t.startsWith("he"), h))
                                     .convert(OptionalKind::narrow);

        assertThat(opt,equalTo(Optional.of("hello")));
    }
    @Test
    public void monadZeroFilterOut(){

        OptionalKind<String> opt = OptionalInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h->OptionalInstances.monadZero().filter((String t)->!t.startsWith("he"), h))
                                     .convert(OptionalKind::narrow);

        assertThat(opt,equalTo(Optional.empty()));
    }

    @Test
    public void monadPlus(){
        OptionalKind<Integer> opt = OptionalInstances.<Integer>monadPlus()
                                      .plus(widen(Optional.empty()), widen(Optional.of(10)))
                                      .convert(OptionalKind::narrow);
        assertThat(opt,equalTo(Optional.of(10)));
    }
    @Test
    public void monadPlusNonEmpty(){

        OptionalKind<Integer> opt = OptionalInstances.<Integer>monadPlus(MonoidKs.firstPresentOptional())
                                      .plus(widen(Optional.of(5)), widen(Optional.of(10)))
                                      .convert(OptionalKind::narrow);
        assertThat(opt,equalTo(Optional.of(5)));
    }
    @Test
    public void  foldLeft(){
        int sum  = OptionalInstances.foldable()
                        .foldLeft(0, (a,b)->a+b, widen(Optional.of(4)));

        assertThat(sum,equalTo(4));
    }
    @Test
    public void  foldRight(){
        int sum  = OptionalInstances.foldable()
                        .foldRight(0, (a,b)->a+b, widen(Optional.of(1)));

        assertThat(sum,equalTo(1));
    }
    @Test
    public void traverse(){
       Maybe<Higher<optional, Integer>> res = OptionalInstances.traverse()
                                                                         .traverseA(MaybeInstances.applicative(), (Integer a)-> Maybe.just(a*2), OptionalKind.of(1))
                                                                         .convert(Maybe::narrowK);


       assertThat(res,equalTo(Maybe.just(Optional.of(2))));
    }

}
