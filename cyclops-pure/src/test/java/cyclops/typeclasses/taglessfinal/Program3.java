package cyclops.typeclasses.taglessfinal;

import com.oath.cyclops.hkt.Higher;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.function.NaturalTransformation;
import cyclops.typeclasses.Do;
import cyclops.typeclasses.monad.Monad;
import cyclops.typeclasses.taglessfinal.Cases.Account;
import lombok.AllArgsConstructor;

import java.util.function.Function;

import static cyclops.function.Function2._0;
import static cyclops.function.Function2._1;
import static cyclops.function.Function2._2;
import static cyclops.function.Function3.__1;
import static cyclops.function.Function3.__3;
import static cyclops.function.Function4.___13;
import static cyclops.function.Function4.___4;
import static cyclops.function.Function5.____24;

@AllArgsConstructor
public class Program3<W,W2> {

    private final Monad<W> monad;
    private final AccountAlgebra<W> accountService;
    private final LogAlgebra<W2> logService;
    private final NaturalTransformation<W2,W> nt;

    private final Account to;
    private final Account from;

    public <R> R transfer(double amount,Function<Higher<W, Tuple2<Account,Account>>,R> fn){

        return  Do.forEach(monad)
                    ._of(amount)
                    .__(this::debit)
                    .__(_2(this::logBalance))
                    .__(__1(this::credit))
                    .__(___4(this::logBalance))
                    .yield(____24(Tuple::tuple))
                    .fold(fn);

    }
    private Higher<W, Void> logBalance(Account a) {
        return  logService.info("Account balance " + a.getBalance())
                          .convert(nt.asFunction());
    }
    private Higher<W,Account> debit(double amount){
        return accountService.debit(from,amount);
    }
    private Higher<W,Account> credit(double amount){
        return accountService.credit(to,amount);
    }
}
