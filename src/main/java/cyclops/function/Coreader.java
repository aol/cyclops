package cyclops.function;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

import java.util.function.Function;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Coreader<R, T> {

    private final T extract;
    private final R ask;


    public <B> Coreader<R,B> map(Function<? super T,? extends B> fn) {
        return new Coreader<R,B>( fn.apply(extract),ask);
    }

    public Coreader<R, Coreader<R, T>> nest() {
        return new Coreader<>(this,ask);
    }

    public <B> Coreader<R,B> coflatMap(Function<? super Coreader<R, T>,? extends B> fn) {
        return nest().map(fn);
    }

    public static <R,T> Coreader<R,T> coreader(T extract, R ask){
        return new Coreader<>(extract,ask);
    }
}