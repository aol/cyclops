package cyclops.function.checked;

public interface CheckedFunction<T,R> {
    public R apply(T t) throws Throwable;
}
