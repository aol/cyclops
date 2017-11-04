package cyclops.function.checked;

public interface CheckedBiFunction<T1,T2,R> {
    public R apply(T1 t1,T2 t2) throws Throwable;
}
