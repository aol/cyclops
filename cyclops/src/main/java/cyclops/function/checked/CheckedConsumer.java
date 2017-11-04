package cyclops.function.checked;

public interface CheckedConsumer<T> {
    public void accept(T a) throws Throwable;
}
