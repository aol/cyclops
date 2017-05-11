package cyclops.monads.function;

import cyclops.monads.AnyM;
import cyclops.monads.WitnessType;

import java.util.function.BinaryOperator;


public interface BinaryAnyMFn<W extends WitnessType<W>,T> extends BinaryOperator<AnyM<W,T>>, AnyMFn2<W,T,T,T> {
}
