package com.aol.cyclops.internal.invokedynamic;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;

public class ReflectionCache {
    private final static Map<Class, List<Field>> fields = new ConcurrentHashMap<>();

    private final static Map<Class, Optional<Method>> unapplyMethods = new ConcurrentHashMap<>();

    public static List<Field> getFields(final Class class1) {
        return getFieldData(class1).stream()
                                   .collect(Collectors.<Field> toList());

    }

    public static Map<String, Field> getFieldMap(final Class class1) {
        return getFieldData(class1).stream()
                                   .collect(Collectors.toMap(f -> f.getName(), f -> f));

    }

    private static List<Field> getFieldData(final Class class1) {
        return fields.computeIfAbsent(class1, cl -> {
            return Seq.iterate(class1, c -> c.getSuperclass())
                      .limitWhile(c -> c != Object.class)
                      .reverse()
                      .flatMap(c -> Stream.of(c.getDeclaredFields()))
                      .filter(f -> !Modifier.isStatic(f.getModifiers()))
                      .map(f -> {
                f.setAccessible(true);
                return f;
            })
                      .collect(Collectors.toList());
        });

    }

    public static Optional<Method> getUnapplyMethod(final Class c) {

        return unapplyMethods.computeIfAbsent(c, cl -> {
            try {
                return Optional.of(cl.getMethod("unapply"));
            } catch (final NoSuchMethodException e) {
                return Optional.empty();
            }
        });

    }

}
