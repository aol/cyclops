package com.aol.cyclops.internal.invokedynamic;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aol.cyclops.util.ExceptionSoftener;

public class InvokeDynamic {
    private static volatile Map<Method, CallSite> callSites = new ConcurrentHashMap<>();

    private static volatile Map<String, Map<Class, List<Method>>> generalMethods = new ConcurrentHashMap<>();

    public <T> Optional<T> execute(List<String> methodNames, Object obj, Object... args) {
        return (Optional) methodNames.stream()
                                     .map(s -> execute(s, obj, args))
                                     .filter(Optional::isPresent)
                                     .findFirst()
                                     .flatMap(i -> i);
    }

    public <T> Optional<T> execute(String methodName, Object obj, Object... args) {
        Class clazz = obj instanceof Class ? (Class) obj : obj.getClass();
        Map<Class, List<Method>> methods = generalMethods.computeIfAbsent(methodName, k -> new ConcurrentHashMap<>());
        List<Method> om = methods.computeIfAbsent(clazz, c -> Stream.of(c.getMethods())
                                                                    .filter(method -> methodName.equals(method.getName()))
                                                                    .filter(method -> method.getParameterCount() == args.length)
                                                                    .map(m2 -> {
                                                                        m2.setAccessible(true);
                                                                        return m2;
                                                                    })
                                                                    .collect(Collectors.toList()));

        if (om.size() > 0) {
            return obj instanceof Class ? Optional.of((T) executeStaticMethod(om.get(0), (Class) obj, args))
                    : Optional.of((T) executeMethod(om.get(0), obj, args));
        }
        return Optional.empty();
    }

    private Object executeStaticMethod(Method m, Class type, Object... args) {
        try {

            MethodHandle mh = this.callSites.computeIfAbsent(m, (m2) -> {
                try {
                    return new ConstantCallSite(
                                                MethodHandles.publicLookup()
                                                             .unreflect(m2));
                } catch (Exception e) {
                    throw ExceptionSoftener.throwSoftenedException(e);

                }

            })
                                            .dynamicInvoker();

            if (args.length == 0)
                return mh.invoke();
            if (args.length == 1)
                return mh.invoke(args[0]);
            if (args.length == 2)
                return mh.invoke(args[0], args[1]);

        } catch (Throwable e) {
            throw ExceptionSoftener.throwSoftenedException(e);
        } finally {

        }
        return null;
    }

    public Object executeMethod(Method m, Object obj, Object... args) {
        try {

            MethodHandle mh = this.callSites.computeIfAbsent(m, (m2) -> {
                try {
                    return new ConstantCallSite(
                                                MethodHandles.publicLookup()
                                                             .unreflect(m2));
                } catch (Exception e) {
                    throw ExceptionSoftener.throwSoftenedException(e);

                }

            })
                                            .dynamicInvoker();
            if (args.length == 0)
                return mh.invoke(obj);
            if (args.length == 1)
                return mh.invoke(obj, args[0]);
            if (args.length == 2)
                return mh.invoke(obj, args[0], args[1]);

        } catch (Throwable e) {
            throw ExceptionSoftener.throwSoftenedException(e);
        } finally {

        }
        return null;
    }

}