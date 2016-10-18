package com.aol.cyclops.internal.comprehensions.donotation;

import java.util.function.Function;

import org.pcollections.PStack;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

import com.aol.cyclops.data.Mutable;
import com.aol.cyclops.internal.comprehensions.ComprehensionsModule.ComprehensionData;
import com.aol.cyclops.internal.comprehensions.ComprehensionsModule.Foreach;
import com.aol.cyclops.internal.comprehensions.donotation.DoBuilderModule.Assignment;
import com.aol.cyclops.internal.comprehensions.donotation.DoBuilderModule.Entry;
import com.aol.cyclops.internal.comprehensions.donotation.DoBuilderModule.Guard;
import com.aol.cyclops.types.Unwrapable;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class DoComp {

    private PStack<Entry> assigned;
    private final Class orgType;

    protected PStack<Entry> addToAssigned(final Function f) {
        return getAssigned().plus(getAssigned().size(), createEntry(f));
    }

    protected Entry createEntry(final Function f) {
        return new Entry(
                         "$$monad" + getAssigned().size(), new Assignment(
                                                                          f));
    }

    protected <T> T yieldInternal(final Function f) {
        return Foreach.<T> foreach(c -> (T) build(c, f));

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Object handleNext(final Entry e, final ComprehensionData c, final PVector<String> newList) {

        if (e.getValue() instanceof Guard) {

            final Function f = ((Guard) e.getValue()).getF();
            c.filter(() -> {

                return unwrapNestedFunction(c, f, newList);

            });

        } else if (e.getValue() instanceof Assignment) {

            final Function f = ((Assignment) e.getValue()).getF();
            c.$(e.getKey(), () -> {

                return unwrapNestedFunction(c, f, newList);

            });

        } else
            c.$(e.getKey(), handleUnwrappable(e.getValue()));

        return null;
    }

    private Object handleUnwrappable(final Object o) {
        if (o instanceof Unwrapable)
            return ((Unwrapable) o).unwrap();
        return o;
    }

    private Object build(final ComprehensionData c, final Function f) {
        final Mutable<PVector<String>> vars = new Mutable<>(
                                                            TreePVector.empty());
        getAssigned().stream()
                     .forEach(e -> addToVar(e, vars, handleNext(e, c, vars.get())));
        new Mutable<>(
                      f);

        return c.yield(() -> {
            return unwrapNestedFunction(c, f, vars.get());

        });

    }

    private Object unwrapNestedFunction(final ComprehensionData c, final Function f, final PVector<String> vars) {
        Function next = f;
        Object result = null;
        for (final String e : vars) {

            result = next.apply(c.$(e));
            if (result instanceof Function) {
                next = (Function) result;
            }

        }
        if (result instanceof Unwrapable)
            return ((Unwrapable) result).unwrap();
        return result;
    }

    private Object addToVar(final Entry e, final Mutable<PVector<String>> vars, final Object handleNext) {
        if (!(e.getValue() instanceof Guard)) {
            final PVector<String> vector = vars.get();
            vars.set(vector.plus(vector.size(), e.getKey()));
        }
        return handleNext;
    }

    protected PStack<Entry> getAssigned() {
        return assigned;
    }

    protected void setAssigned(final PStack<Entry> assigned) {
        this.assigned = assigned;
    }

    protected Class getOrgType() {
        return orgType;
    }

}