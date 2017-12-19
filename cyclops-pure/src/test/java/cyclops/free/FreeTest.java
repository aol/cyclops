package cyclops.free;


import static cyclops.free.CharToy.bell;
import static cyclops.free.CharToy.done;
import static cyclops.free.CharToy.output;
import static cyclops.kinds.SupplierKind.λK;

import cyclops.control.Either;
import cyclops.function.Function0;
import com.oath.cyclops.hkt.DataWitness.supplier;
import cyclops.data.tuple.Tuple2;
import cyclops.instances.jdk.SupplierInstances;
import cyclops.kinds.SupplierKind;
import org.junit.Test;

import cyclops.free.CharToy.*;
import static cyclops.function.Lambda.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public final class FreeTest {
    private static Free<supplier, Long> fibonacci(long i){
        return fibonacci(i,1,0);
    }

    private static Free<supplier, Long> fibonacci(long n, long a, long b) {
        return n == 0 ? Free.done(b) : λK( ()->fibonacci(n-1, a+b, a))
                                        .kindTo(SupplierKind::suspend)
                                        .flatMap(i->λK( ()->fibonacci(n-1, a+b, a))
                                                .kindTo(SupplierKind::suspend));
    }
    static Free<supplier, Long> fib(final Long n){

        if(n < 2){
            return Free.done(2L);
        }else{
            return λK(()->fib(n-1))
                            .kindTo(SupplierKind::suspend)
                            .flatMap(x->λK(()->fib(n-2))
                                    .kindTo(SupplierKind::suspend)
                                    .map(y->x+y));
        }
    }

    @Test
    public void testFib(){

        long time = System.currentTimeMillis();
        assertThat(1597l,equalTo(SupplierKind.run(fibonacci(17L))));
        System.out.println("Taken "  +(System.currentTimeMillis()-time));
    }

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



        Free<µ, Void> one =                   output('A')
                .forEach4(__ ->               bell(),
                         (__, ___) ->         output('B'),
                         (__, ___, ____) ->   done());

        Free<µ, Void> two =                   output('C')
                .forEach4(__ ->               bell(),
                         (__, ___) ->         output('D'),
                         (__, ___, ____) ->   done());


        assertThat(expected,equalTo(
                                interleaveProgram(one,two)));
    }

    static <R> String interleaveProgram(Free<CharToy.µ,R> program1,Free<CharToy.µ,R> program2){

        Tuple2<Either<CharToy<Free<µ, R>>, R>, Either<CharToy<Free<µ, R>>, R>> tuple = Free.product(CharToy.functor, program1, CharToy::narrowK, program2, CharToy::narrowK);
        Either<CharToy<Free<µ, R>>, R> a = tuple._1();
        Either<CharToy<Free<µ, R>>, R> b = tuple._2();




        String one =a.visit(
                        r ->   r.match()
                                .visit(o->interleaveOutput(o,program2),
                                        FreeTest::handleBell,
                                        FreeTest::handleDone)
                        ,
                        FreeTest::handleReturn
                    );
        String two = b.visit(
                r ->   r.match()
                        .visit(o->interleaveOutput1(o,program1),
                                FreeTest::handleBell,
                                FreeTest::handleDone)
                ,
                FreeTest::handleReturn
        );
        return one +two;

    }

    static <R> String showProgram(Free<CharToy.µ,R> program){


        return program.resume(CharToy.functor, CharToy::narrowK)
                .visit(
                        r ->   r.match()
                                .visit(FreeTest::handleOutput,
                                       FreeTest::handleBell,
                                       FreeTest::handleDone)
                        ,
                        FreeTest::handleReturn
                );

    }
    static <R> String handleReturn(R r){
        return "return " + r + "\n";
    }
    static <R> String handleOutput(CharOutput<Free<CharToy.µ,R>> output){
        return output.visit((a, next) -> "emitted " + a + "\n" + showProgram(next));
    }

    static <R> String handleBell(CharBell<Free<CharToy.µ, R>> bell){
       return bell.visit(next -> "bell " + "\n" + showProgram(next));
    }

    static <T> String handleDone(CharDone<T> done){
        return "done\n";
    }

    static <R> String interleaveOutput(CharOutput<Free<CharToy.µ,R>> output,Free<CharToy.µ,R> program2){
        System.out.println("Running interA");
        return output.visit((a, next) -> "emitted " + a + "\n" + interleaveProgram(next,program2));
    }

    static <R> String interleaveOutput1(CharOutput<Free<CharToy.µ,R>> output,Free<CharToy.µ,R> program1){
        System.out.println("Running interB");
        return output.visit((a, next) -> "emitted " + a + "\n" + interleaveProgram(program1,next));
    }


}
