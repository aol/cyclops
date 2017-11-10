package cyclops.control.trampoline;

import cyclops.control.Eval;
import org.junit.Test;

import static org.junit.Assert.assertThat;


public class EvalTrampolineTest {
    @Test
    public void bounce() {
        even(Eval.now(200000)).toTrampoline().bounce();
    }
    @Test
    public void odd(){
        System.out.println(even(Eval.now(200000)).toTrampoline()
                              .zip(odd1(Eval.now(200000)).toTrampoline()).get());
    }
    public Eval<String> odd(Eval<Integer> n )  {
        System.out.println("A");
        return n.flatMap(x->even(Eval.now(x-1)));
    }
    public Eval<String> even(Eval<Integer> n )  {
      //  System.out.println("A");
        return n.flatMap(x->{
            return x<=0 ? Eval.now("done") : odd(Eval.now(x-1));
        });
    }
    public Eval<String> odd1(Eval<Integer> n )  {
        System.out.println("B");
        return n.flatMap(x->even1(Eval.now(x-1)));
    }
    public Eval<String> even1(Eval<Integer> n )  {
       // System.out.println("B");
        return n.flatMap(x->{
            return x<=0 ? Eval.now("done") : odd1(Eval.now(x-1));
        });
    }
}
