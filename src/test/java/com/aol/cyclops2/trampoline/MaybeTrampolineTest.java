package com.aol.cyclops2.trampoline;

import cyclops.control.Eval;
import cyclops.control.Maybe;
import org.junit.Test;


public class MaybeTrampolineTest {
    @Test
    public void bounce() {
        even(Maybe.just(200000)).toTrampoline().bounce();
    }
    @Test
    public void odd(){
        System.out.println(even(Maybe.just(200000)).toTrampoline()
                .zip(odd1(Maybe.just(200000)).toTrampoline()).get());


        //use zip to interleave execution of even and odd algorithms!
        even(Maybe.just(200000))
                  .toTrampoline()
                  .zip(odd1(Maybe.just(200000))
                                 .toTrampoline()).get();





    }
    public Maybe<String> odd(Maybe<Integer> n )  {
        System.out.println("A");
        return n.flatMap(x->even(Maybe.just(x-1)));
    }
    public Maybe<String> even(Maybe<Integer> n )  {
        return n.flatMap(x->{
            return x<=0 ? Maybe.just("done") : odd(Maybe.just(x-1));
        });
    }
    public Maybe<String> odd1(Maybe<Integer> n )  {
        System.out.println("B");
        return n.flatMap(x->even1(Maybe.just(x-1)));
    }
    public Maybe<String> even1(Maybe<Integer> n )  {

        return n.flatMap(x->{
            return x<=0 ? Maybe.just("done") : odd1(Maybe.just(x-1));
        });
    }
}
