package com.aol.cyclops.internal.comprehensions.comprehenders;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.internal.invokedynamic.InvokeDynamic;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.extensability.ValueComprehender;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class InvokeDynamicComprehender implements ValueComprehender {
    @Override
    public Class getTargetClass() {
        return null;
    }

    Optional<Class> type;

    private static volatile Map<Class, Method> mapMethod = new ConcurrentHashMap<>();
    private static volatile Map<Class, Method> flatMapMethod = new ConcurrentHashMap<>();
    private static volatile Map<Class, ListX<Method>> filterMethod = new ConcurrentHashMap<>();

    @Override
    public Object filter(final Object t, final Predicate p) {
        final Class clazz = t.getClass();
        final ListX<Method> m = filterMethod.computeIfAbsent(clazz, c -> ReactiveSeq.of(c.getMethods())
                                                                                    .filter(method -> "filter".equals(method.getName()))
                                                                                    .filter(method -> method.getParameterCount() == 1)
                                                                                    .toListX()
                                                                                    .map(m2 -> {
                                                                                        m2.setAccessible(true);
                                                                                        return m2;
                                                                                    }));
        if (m.size() == 0)
            return ValueComprehender.super.filter(t, p);
        for (final Method next : m) {

            final Class z = next.getParameterTypes()[0];
            if (z.isInterface()) {

                Object target = p;
                if (!z.isAssignableFrom(Predicate.class)) {
                    target = Proxy.newProxyInstance(InvokeDynamicComprehender.class.getClassLoader(), new Class[] { z },
                                                    new FunctionExecutionInvocationHandler(
                                                                                           input -> p.test(input)));
                }

                return new InvokeDynamic().executeMethod(next, t, target);
            }
        }
        return ValueComprehender.super.filter(t, p);

    }

    @Override
    public Object map(final Object t, final Function fn) {

        final Class clazz = t.getClass();

        final Method m = mapMethod.computeIfAbsent(clazz, c -> Stream.of(c.getMethods())
                                                                     .filter(method -> "map".equals(method.getName())
                                                                             || "transform".equals(method.getName()))
                                                                     .filter(method -> method.getParameterCount() == 1)
                                                                     .findFirst()
                                                                     .map(m2 -> {
                                                                         m2.setAccessible(true);
                                                                         return m2;
                                                                     })
                                                                     .get());

        return execute(t, fn, m);

    }

    private Object execute(final Object t, final Function fn, final Method m) {
        final Class z = m.getParameterTypes()[0];
        Object target = fn;
        if (!z.isAssignableFrom(Function.class)) {
            target = Proxy.newProxyInstance(InvokeDynamicComprehender.class.getClassLoader(), new Class[] { z },
                                            new FunctionExecutionInvocationHandler(
                                                                                   input -> fn.apply(input)));
        }

        return new InvokeDynamic().execute(m.getName(), t, target)
                                  .get();
    }

    @Override
    public Object flatMap(final Object t, final Function fn) {
        final Class clazz = t.getClass();
        final Method m = flatMapMethod.computeIfAbsent(clazz, c -> Stream.of(c.getMethods())
                                                                         .filter(method -> "flatMap".equals(method.getName())
                                                                                 || "bind".equals(method.getName())
                                                                                 || "transformAndConcat".equals(method.getName()))
                                                                         .filter(method -> method.getParameterCount() == 1)
                                                                         .findFirst()
                                                                         .get());

        return execute(t, fn, m);
    }

    private boolean isAssignableFrom(final Class t, final Object apply) {
        if (apply.getClass()
                 .isAssignableFrom(t))
            return true;

        if (apply.getClass()
                 .getSuperclass() == Object.class)
            return false;

        return apply.getClass()
                    .getSuperclass()
                    .isAssignableFrom(t);
    }

    @Override
    public boolean instanceOfT(final Object apply) {
        return type.map(t -> isAssignableFrom(t, apply))
                   .orElse(true);
    }

    @Override
    public Object of(final Object o) {

        final InvokeDynamic dyn = new InvokeDynamic();
        final Optional ob = dyn.execute(Arrays.asList("of", "singleton", "some", "right", "success", "primary"), type.get(), o);

        return ob.get();

    }

    @Override
    public Object empty() {

        final InvokeDynamic dyn = new InvokeDynamic();
        final Optional o = dyn.execute(Arrays.asList("empty", "of", "cons", "none", "nil", "left", "failure", "secondary"), type.get());
        return o.get();
    }

    @Override
    public Object resolveForCrossTypeFlatMap(final Comprehender comp, final Object apply) {
        final InvokeDynamic dyn = new InvokeDynamic();
        try {
            final Optional o = dyn.execute(Arrays.asList("get", "join"), apply);
            if (o.isPresent()) //extraction method exists?
                return comp.of(o.get());
            else //no? let's just wrap the value in the appropriate monad type, this allows flatten() to work

                return comp.of(apply);
        } catch (final Throwable t) {
            //error extracting from extraciton method? return empty
        }
        return comp.empty();

    }

}
