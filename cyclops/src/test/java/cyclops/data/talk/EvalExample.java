package cyclops.data.talk;

import cyclops.control.Eval;

public class EvalExample {

    static int count;

    public static void main(String[] args){

        Eval<Integer> lazy = Eval.later(()->count++)
                                 .map(i->i*2);


        System.out.println("doubled (1) " + lazy.get());
        System.out.println("count (1) " + count);


        System.out.println("doubled (2) " + lazy.get());
        System.out.println("count (2) " + count);

    }
}
