package cyclops.function.checked;

public interface CheckedLongFunction<R> {
    public R apply(long t) throws Throwable;
}
