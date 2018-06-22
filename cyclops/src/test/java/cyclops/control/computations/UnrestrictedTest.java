package cyclops.control.computations;


import cyclops.control.Unrestricted;
import cyclops.control.Either;
import cyclops.control.computations.ToyLanguage.Bell;
import cyclops.control.computations.ToyLanguage.Done;
import cyclops.control.computations.ToyLanguage.Output;

import cyclops.data.tuple.Tuple2;
import org.junit.Test;

import static cyclops.control.computations.ToyLanguage.bell;
import static cyclops.control.computations.ToyLanguage.output;
import static cyclops.control.computations.ToyLanguage.done;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public final class UnrestrictedTest {

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


        Unrestricted<Void> one = output('A').forEach4(__ -> bell(),
                                                                        (__, ___) -> output('B'),
                                                                         (__, ___, ____) -> done());

        Unrestricted<Void> two =                   output('C')
                .forEach4(__ ->               bell(),
                         (__, ___) ->         output('D'),
                         (__, ___, ____) ->   done());


        assertThat(expected,equalTo(
                                interleaveProgram(one,two)));
    }

    static <R> String interleaveProgram(Unrestricted<R> program1, Unrestricted<R> program2){

        Tuple2<Either<ToyLanguage<Unrestricted<R>>, R>, Either<ToyLanguage<Unrestricted<R>>, R>> tuple = Unrestricted.product(program1,ToyLanguage.decoder() ,program2,ToyLanguage.decoder());
        Either<ToyLanguage<Unrestricted<R>>, R> a = tuple._1();
        Either<ToyLanguage<Unrestricted<R>>, R> b = tuple._2();




        String one =a.fold(
                        r ->   r.match()
                                .fold(o->interleaveOutput(o,program2),
                                        UnrestrictedTest::handleBell,
                                        UnrestrictedTest::handleDone)
                        ,
                        UnrestrictedTest::handleReturn
                    );
        String two = b.fold(
                r ->   r.match()
                        .fold(o->interleaveOutput1(o,program1),
                                UnrestrictedTest::handleBell,
                                UnrestrictedTest::handleDone)
                ,
                UnrestrictedTest::handleReturn
        );
        return one +two;

    }

    static <R> String showProgram(Unrestricted<R> program){


        return program.resume(ToyLanguage.decoder())
                .fold(
                        r ->    r.match()
                                .fold(UnrestrictedTest::handleOutput,
                                       UnrestrictedTest::handleBell,
                                       UnrestrictedTest::handleDone)
                        ,
                        UnrestrictedTest::handleReturn
                );

    }
    static <R> String handleReturn(R r){
        return "return " + r + "\n";
    }
    static <R> String handleOutput(Output<Unrestricted<R>> output){
        return output.visit((a, next) -> "emitted " + a + "\n" + showProgram(next));
    }

    static <R> String handleBell(Bell<Unrestricted<R>> bell){
       return bell.visit(next -> "bell " + "\n" + showProgram(next));
    }

    static <T> String handleDone(Done<T> done){
        return "done\n";
    }

    static <R> String interleaveOutput(Output<Unrestricted<R>> output, Unrestricted<R> program2){
        System.out.println("Running interA");
        return output.visit((a, next) -> "emitted " + a + "\n" + interleaveProgram(next,program2));
    }

    static <R> String interleaveOutput1(Output<Unrestricted<R>> output, Unrestricted<R> program1){
        System.out.println("Running interB");
        return output.visit((a, next) -> "emitted " + a + "\n" + interleaveProgram(program1,next));
    }


}
