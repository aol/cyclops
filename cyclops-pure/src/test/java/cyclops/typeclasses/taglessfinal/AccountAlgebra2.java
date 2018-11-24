package cyclops.typeclasses.taglessfinal;

import com.oath.cyclops.hkt.Higher;
import cyclops.control.Option;
import cyclops.typeclasses.Do;
import cyclops.typeclasses.taglessfinal.Cases.Account;

public interface AccountAlgebra2<W> {

    StoreAlgebra<W,Long,Account> store();
    Do<W> forEach();

    default Higher<W,Option<Account>> debit(Account account, double amount){
       return forEach().__(store().get(account.getId()))
                       .__(a -> store().put(account.getId(), account.debit(amount)))
                       .yield((a, b) -> a)
                        .unwrap();

    }
    default Higher<W, Option<Account>> credit(Account account, double amount){
        return forEach().__(store().get(account.getId()))
                        .__(a -> store().put(account.getId(), account.credit(amount)))
                        .yield((a, b) -> a)
                        .unwrap();
    }

}
