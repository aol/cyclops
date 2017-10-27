package cyclops.function.checked;

public interface CheckedPredicate<T> {
    public boolean test(T test) throws Throwable;
}
