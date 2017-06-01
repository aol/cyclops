package cyclops.function;

import java.util.function.BooleanSupplier;

public interface BooleanFn0 extends BooleanSupplier {

    default BooleanFn0 before(Runnable r){
        return ()->{
            r.run();
            return getAsBoolean();
        };
    }
    default BooleanFn0 after(Runnable r){
        return ()->{

            boolean res = getAsBoolean();
            r.run();
            return res;
        };
    }

}