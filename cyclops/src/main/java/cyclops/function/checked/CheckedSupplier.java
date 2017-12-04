package cyclops.function.checked;

public interface CheckedSupplier<T> {
    public T get() throws Throwable;
}
