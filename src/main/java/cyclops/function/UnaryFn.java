package cyclops.function;

import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;


public interface UnaryFn<T> extends UnaryOperator<T>, Fn1<T,T> {
}
