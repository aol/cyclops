package cyclops.function;

import java.util.function.UnaryOperator;


public interface UnaryFn<T> extends UnaryOperator<T>, Function1<T,T> {
}
