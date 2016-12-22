package cyclops.function;

import cyclops.monads.AnyM;
import cyclops.monads.WitnessType;

import java.util.function.UnaryOperator;


public interface UnaryAnyMFn<W extends WitnessType<W>,T> extends UnaryOperator<AnyM<W,T>>, AnyMFn1<W,T,T> {
}
