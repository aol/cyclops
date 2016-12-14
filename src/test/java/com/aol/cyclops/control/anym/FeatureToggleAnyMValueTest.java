package com.aol.cyclops.control.anym;

import org.junit.Before;

import com.aol.cyclops.control.AnyM;

public class FeatureToggleAnyMValueTest extends BaseAnyMValueTest {
    @Before
    public void setUp() throws Exception {
        just = AnyM.fromFeatureToggle(FeatureToggle.enable(10));
        none = AnyM.fromFeatureToggle(FeatureToggle.disable(20));
    }
    
}
