package cyclops.monads.function;

import cyclops.monads.AnyM;

import cyclops.monads.WitnessType;
import cyclops.monads.function.AnyMFunction1;

import java.util.function.UnaryOperator;


public interface UnaryAnyMFunction<W extends WitnessType<W>,T> extends UnaryOperator<AnyM<W,T>>, AnyMFunction1<W,T,T> {
}
