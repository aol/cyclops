package cyclops.typeclasses.taglessfinal;

import com.oath.cyclops.hkt.Higher;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.typeclasses.Do;
import cyclops.typeclasses.monad.Monad;
import cyclops.typeclasses.taglessfinal.Cases.Account;
import lombok.AllArgsConstructor;

import java.util.function.Function;

import static cyclops.function.Function2._1;
import static cyclops.function.Function3.__1;
import static cyclops.function.Function3.__2;
import static cyclops.function.Function3.__23;

@AllArgsConstructor
public class ProgramStore<W> {

    private final Monad<W> monad;
    private final AccountAlgebra2<W> accountService;
    private final Account to;
    private final Account from;

    public <R> R transfer(double amount, Function<Higher<W, Tuple2<Option<Account>,Option<Account>>>,R> fn){

        return Do.forEach(monad)
                 ._of(amount)
                 .__(this::debit)
                 .__(_1(this::credit))
                 .yield(__23(Tuple::tuple))
                 .fold(fn);
    }

    private Higher<W,Option<Account>> debit(double amount){
        return accountService.debit(from, amount);
    }
    private Higher<W,Option<Account>> credit(double amount){
        return accountService.credit(to,amount);
    }
}
