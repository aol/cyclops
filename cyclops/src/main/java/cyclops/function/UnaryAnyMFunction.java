package cyclops.function;

import cyclops.monads.AnyM;
import cyclops.monads.DataWitnessType;
import cyclops.monads.function.AnyMFunction1;

import java.util.function.UnaryOperator;


public interface UnaryAnyMFunction<W extends WitnessType<W>,T> extends UnaryOperator<AnyM<W,T>>, AnyMFunction1<W,T,T> {
}
