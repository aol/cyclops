package cyclops.function.checked;

public interface CheckedBooleanSupplier {
    public Boolean getAsBoolean() throws Throwable;
}
