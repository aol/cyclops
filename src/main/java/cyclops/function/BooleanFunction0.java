package cyclops.function;

import java.util.function.BooleanSupplier;

public interface BooleanFunction0 extends BooleanSupplier {

    default BooleanFunction0 before(Runnable r){
        return ()->{
            r.run();
            return getAsBoolean();
        };
    }
    default BooleanFunction0 after(Runnable r){
        return ()->{

            boolean res = getAsBoolean();
            r.run();
            return res;
        };
    }

}