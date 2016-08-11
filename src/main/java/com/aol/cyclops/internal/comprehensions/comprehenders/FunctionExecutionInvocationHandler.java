package com.aol.cyclops.internal.comprehensions.comprehenders;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.function.Function;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
public class FunctionExecutionInvocationHandler implements InvocationHandler {

    @Setter
    private Function function;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("toString".equals(method.getName()) && method.getParameterCount() == 0)
            return function.toString();
        if ("hashCode".equals(method.getName()) && method.getParameterCount() == 0)
            return function.hashCode();
        if ("equals".equals(method.getName()) && method.getParameterCount() == 1)
            return function.equals(args[0]);
        return function.apply(args[0]);
    }
}
