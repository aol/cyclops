package cyclops.free;


import cyclops.function.Fn0;
import cyclops.free.Free;
import org.junit.Test;

import static cyclops.function.Lambda.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public final class FreeTest {
    private static Free<Fn0.µ, Long> fibonacci(long i){
        return fibonacci(i,1,0);
    }
    private static Free<Fn0.µ, Long> fibonacci(long n, long a, long b) {
        return n == 0 ? Free.done(b) : λ( ()->fibonacci(n-1, a+b, a))
                                        .fnTo(Fn0::suspend)
                                        .flatMap(i->λ( ()->fibonacci(n-1, a+b, a))
                                                .fnTo(Fn0::suspend));
    }
    static Free<Fn0.µ, Long> fib(final Long n){

        if(n < 2){
            return Free.done(2L);
        }else{
            return λ(()->fib(n-1))
                            .fnTo(Fn0::suspend)
                            .flatMap(x->λ(()->fib(n-2))
                                    .fnTo(Fn0::suspend)
                                    .map(y->x+y));
        }
    }

    @Test
    public void testFib(){

        long time = System.currentTimeMillis();
        assertThat(1597,equalTo(Fn0.run(fibonacci(17L))));
        System.out.println("Taken "  +(System.currentTimeMillis()-time));
    }

    @Test
    public void interpreter(){
        String expected = "output A\n" +
                "bell \n" +
                "output B\n" +
                "done\n";
        assertThat(expected,equalTo(
                showProgram(CharToy.output('A').forEach4(unit1 -> CharToy.bell(),
                                                            (unit1,unit2) -> CharToy.output('B'),
                                                            (u1,u2,u3)->CharToy.done()))));
    }


    static <R> String showProgram(Free<CharToy.µ, R> program){
        return
                program.resume(CharToy.functor).visit(
                        r ->
                                ((CharToy<Free<CharToy.µ, R>>)r).fold(
                                        (a, next) -> "output " + a + "\n" + showProgram(next),
                                        (next -> "bell " + "\n" + showProgram(next)),
                                        "done\n"
                                )
                        ,
                        r ->
                                "return " + r + "\n"
                );
    }
}