package com.aol.cyclops;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.types.anyM.WitnessType;

public class Matchers {
 
    public static <W extends WitnessType<W>,T> Matcher<AnyM<W,T>> equivalent(AnyM<W,T> anyM){
        return new Matcher<AnyM<W,T>>(){

            @Override
            public void describeTo(Description description) {
             
                
            }

            @Override
            public boolean matches(Object item) {
               return anyM.eqv((AnyM)item);
            }

            @Override
            public void describeMismatch(Object item, Description mismatchDescription) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void _dont_implement_Matcher___instead_extend_BaseMatcher_() {
                // TODO Auto-generated method stub
                
            }
            
        };
    }
}
