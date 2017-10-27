package cyclops.function.checked;

public interface CheckedIntFunction<R> {
    public R apply(int t) throws Throwable;
}
