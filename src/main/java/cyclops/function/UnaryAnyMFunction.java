package cyclops.function;

import cyclops.monads.AnyM;
import cyclops.monads.WitnessType;
import cyclops.monads.function.AnyMFn1;

import java.util.function.UnaryOperator;


public interface UnaryAnyMFunction<W extends WitnessType<W>,T> extends UnaryOperator<AnyM<W,T>>, AnyMFn1<W,T,T> {
}
