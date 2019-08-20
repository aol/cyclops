package com.oath.cyclops.types;

import cyclops.control.Either;
import cyclops.control.LazyEither;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class UnwrappableTest {
    @Test
    public void unwrapIfInstanceHit() throws Exception {
       Object o = new MyUnWrappable().unwrapIfInstance(Either.class,()->"hello");
       assertThat(o,equalTo(LazyEither.left("hello")));
    }

    @Test
    public void unwrapIfInstanceMiss() throws Exception {
        Object o = new MyUnWrappable().unwrapIfInstance(String.class,()->"hello");
        assertThat(o,equalTo("hello"));
    }
    static class MyUnWrappable implements Unwrappable {
        @Override
        public <R> R unwrap() {
            return (R) LazyEither.left("hello");
        }
    }
}
