package cyclops.function.checked;

public interface CheckedLongPredicate {
    public boolean test(long test) throws Throwable;
}
