package cyclops.function.checked;

public interface CheckedDoubleFunction<R> {
    public R apply(double t) throws Throwable;
}
