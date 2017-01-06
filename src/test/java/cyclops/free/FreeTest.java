package cyclops.free;


import static cyclops.function.Fn0.SupplierKind;

import cyclops.function.Fn0;
import org.junit.Test;

import cyclops.free.CharToy.*;
import static cyclops.function.Lambda.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public final class FreeTest {
    private static Free<SupplierKind.µ, Long> fibonacci(long i){
        return fibonacci(i,1,0);
    }
    private static Free<SupplierKind.µ, Long> fibonacci(long n, long a, long b) {
        return n == 0 ? Free.done(b) : λK( ()->fibonacci(n-1, a+b, a))
                                        .kindTo(Fn0::suspend)
                                        .flatMap(i->λK( ()->fibonacci(n-1, a+b, a))
                                                .kindTo(Fn0::suspend));
    }
    static Free<SupplierKind.µ, Long> fib(final Long n){

        if(n < 2){
            return Free.done(2L);
        }else{
            return λK(()->fib(n-1))
                            .kindTo(Fn0::suspend)
                            .flatMap(x->λK(()->fib(n-2))
                                    .kindTo(Fn0::suspend)
                                    .map(y->x+y));
        }
    }

    @Test
    public void testFib(){

        long time = System.currentTimeMillis();
        assertThat(1597l,equalTo(Fn0.run(fibonacci(17L))));
        System.out.println("Taken "  +(System.currentTimeMillis()-time));
    }

    @Test
    public void interpreter(){
        String expected = "output A\n" +
                "bell \n" +
                "output B\n" +
                "done\n";


        assertThat(expected,equalTo(
                showProgram(CharToy.output('A')
                                   .forEach4(unit1 -> CharToy.bell(),
                                             (unit1,unit2) -> CharToy.output('B'),
                                             (u1,u2,u3)->CharToy.done()))));
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
        return output.fold((a, next) -> "output " + a + "\n" + showProgram(next));
    }

    static <R> String handleBell(CharBell<Free<CharToy.µ, R>> bell){
       return bell.fold(next -> "bell " + "\n" + showProgram(next));
    }

    static <T> String handleDone(CharDone<T> done){
        return "done\n";
    }
}