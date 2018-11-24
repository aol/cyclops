package cyclops.typeclasses.taglessfinal;

import com.oath.cyclops.hkt.Higher;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.typeclasses.Do;
import cyclops.typeclasses.monad.Monad;
import cyclops.typeclasses.taglessfinal.Cases.Account;
import lombok.AllArgsConstructor;

import java.util.function.Function;

import static cyclops.function.Function1.lazy;
import static cyclops.function.Function2._1;
import static cyclops.function.Function3.__23;

@AllArgsConstructor
public class Program<W> {

    private final Monad<W> monad;
    private final AccountAlgebra<W> accountService;
    private final Account to;
    private final Account from;


    public Higher<W, Tuple2<Account,Account>> transfer(double amount) {

        return Do.forEach(monad)
                 ._of(amount)
                 .__(this::debit)
                 .__(_1(this::credit))
                 .yield(__23(Tuple::tuple))
                 .unwrap();
    }


    public <R> R transfer(double amount, Function<Higher<W, Tuple2<Account,Account>>,R> fn) {

        return Do.forEach(monad)
                ._of(amount)
                .__(this::debit)
                .__(_1(this::credit))
                .yield(__23(Tuple::tuple))
                .fold(fn);
    }

    private Higher<W,Account> debit(double amount){
        return accountService.debit(from,amount);
    }
    private Higher<W,Account> credit(double amount){
        return accountService.credit(to,amount);
    }

}
