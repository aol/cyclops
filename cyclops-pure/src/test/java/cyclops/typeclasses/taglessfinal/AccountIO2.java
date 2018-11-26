package cyclops.typeclasses.taglessfinal;

import com.oath.cyclops.hkt.DataWitness;
import com.oath.cyclops.hkt.DataWitness.io;
import cyclops.instances.reactive.IOInstances;
import cyclops.typeclasses.Do;
import cyclops.typeclasses.taglessfinal.Cases.Account;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AccountIO2 implements AccountAlgebra2<io> {
    StoreIO<Long, Account> storeIO;
    @Override
    public StoreAlgebra<io, Long, Account> store() {
        return storeIO;
    }

    @Override
    public Do<io> forEach() {
        return Do.forEach(IOInstances::monad);
    }
}
