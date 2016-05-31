package com.aol.cyclops;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

import com.aol.cyclops.control.AnyM;

public class Matchers {
 
    public static <T> Matcher<AnyM<T>> equivalent(AnyM<T> anyM){
        return new Matcher<AnyM<T>>(){

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
