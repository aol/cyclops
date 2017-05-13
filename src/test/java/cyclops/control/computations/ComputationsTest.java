package cyclops.control.computations;


import cyclops.control.Computations;
import cyclops.control.Xor;
import cyclops.control.computations.ToyLanguage.Bell;
import cyclops.control.computations.ToyLanguage.Done;
import cyclops.control.computations.ToyLanguage.Output;
import cyclops.function.Fn0;

import org.jooq.lambda.tuple.Tuple2;
import org.junit.Test;

import static cyclops.control.computations.ToyLanguage.bell;
import static cyclops.control.computations.ToyLanguage.output;
import static cyclops.control.computations.ToyLanguage.done;
import static cyclops.function.Fn0.SupplierKind;
import static cyclops.function.Lambda.λK;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public final class ComputationsTest {

    @Test
    public void interpreter(){
        String expected = "emitted A\n" +
                "bell \n" +
                "emitted B\n" +
                "done\n";


        assertThat(expected,equalTo(
                showProgram(output('A')
                                   .forEach4(unit1 -> bell(),
                                             (unit1,unit2) -> output('B'),
                                             (u1,u2,u3)-> done()))));
    }
    @Test
    public void interpreterInterleave(){
        String expected = "emitted A\nbell \nemitted B\ndone\nemitted C\nbell \nemitted B\ndone\nbell \nemitted D\ndone\nemitted C\nemitted A\nbell \nemitted B\ndone\nbell \nemitted D\ndone\nbell \nemitted D\ndone\n";


        Computations<Void> one = output('A').forEach4(__ -> bell(),
                                                                        (__, ___) -> output('B'),
                                                                         (__, ___, ____) -> done());

        Computations<Void> two =                   output('C')
                .forEach4(__ ->               bell(),
                         (__, ___) ->         output('D'),
                         (__, ___, ____) ->   done());


        assertThat(expected,equalTo(
                                interleaveProgram(one,two)));
    }

    static <R> String interleaveProgram(Computations<R> program1,Computations<R> program2){

        Tuple2<Xor<ToyLanguage<Computations<R>>, R>, Xor<ToyLanguage<Computations<R>>, R>> tuple = Computations.product(program1,ToyLanguage.decoder() ,program2,ToyLanguage.decoder());
        Xor<ToyLanguage<Computations<R>>, R> a = tuple.v1;
        Xor<ToyLanguage<Computations<R>>, R> b = tuple.v2;




        String one =a.visit(
                        r ->   r.match()
                                .visit(o->interleaveOutput(o,program2),
                                        ComputationsTest::handleBell,
                                        ComputationsTest::handleDone)
                        ,
                        ComputationsTest::handleReturn
                    );
        String two = b.visit(
                r ->   r.match()
                        .visit(o->interleaveOutput1(o,program1),
                                ComputationsTest::handleBell,
                                ComputationsTest::handleDone)
                ,
                ComputationsTest::handleReturn
        );
        return one +two;

    }

    static <R> String showProgram(Computations<R> program){


        return program.resume(ToyLanguage.decoder())
                .visit(
                        r ->    r.match()
                                .visit(ComputationsTest::handleOutput,
                                       ComputationsTest::handleBell,
                                       ComputationsTest::handleDone)
                        ,
                        ComputationsTest::handleReturn
                );

    }
    static <R> String handleReturn(R r){
        return "return " + r + "\n";
    }
    static <R> String handleOutput(Output<Computations<R>> output){
        return output.visit((a, next) -> "emitted " + a + "\n" + showProgram(next));
    }

    static <R> String handleBell(Bell<Computations<R>> bell){
       return bell.visit(next -> "bell " + "\n" + showProgram(next));
    }

    static <T> String handleDone(Done<T> done){
        return "done\n";
    }

    static <R> String interleaveOutput(Output<Computations<R>> output,Computations<R> program2){
        System.out.println("Running interA");
        return output.visit((a, next) -> "emitted " + a + "\n" + interleaveProgram(next,program2));
    }

    static <R> String interleaveOutput1(Output<Computations<R>> output,Computations<R> program1){
        System.out.println("Running interB");
        return output.visit((a, next) -> "emitted " + a + "\n" + interleaveProgram(program1,next));
    }


}