package cyclops.function.checked;

public interface CheckedBiPredicate<T1,T2> {
    public boolean test(T1 t1,T2 t2) throws Throwable;
}
