package cyclops.function.checked;

public interface CheckedLongSupplier {
    public long getAsLong() throws Throwable;
}
