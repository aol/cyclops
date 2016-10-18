package com.aol.cyclops.internal.comprehensions.comprehenders;

import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.control.FeatureToggle;
import com.aol.cyclops.control.FeatureToggle.Enabled;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.extensability.ValueComprehender;

/**
 * @author johnmcclean
 *
 *  Behaviour in cross-type flatMap is to create an empty instance for Disabled Switches, but always pass Enabled values on
 */
public class FeatureToggleComprehender implements ValueComprehender<FeatureToggle<Object>> {

    @Override
    public Object filter(final FeatureToggle t, final Predicate p) {
        return t.filter(p);
    }

    @Override
    public Object map(final FeatureToggle t, final Function fn) {
        return t.map(fn);
    }

    @Override
    public FeatureToggle flatMap(final FeatureToggle t, final Function fn) {
        return t.flatMap(fn);
    }

    @Override
    public boolean instanceOfT(final Object apply) {
        return apply instanceof FeatureToggle;
    }

    @Override
    public FeatureToggle of(final Object o) {
        return FeatureToggle.enable(o);
    }

    @Override
    public FeatureToggle empty() {
        return FeatureToggle.disable(null);
    }

    @Override
    public Class getTargetClass() {
        return FeatureToggle.class;
    }

    @Override
    public Object resolveForCrossTypeFlatMap(final Comprehender comp, final FeatureToggle<Object> apply) {
        return apply instanceof Enabled ? comp.of(apply.get()) : comp.empty();

    }

}
