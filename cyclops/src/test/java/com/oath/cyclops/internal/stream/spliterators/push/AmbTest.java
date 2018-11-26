package com.oath.cyclops.internal.stream.spliterators.push;

import cyclops.control.Future;
import cyclops.data.Vector;
import cyclops.reactive.Spouts;
import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class AmbTest {

    @Test
    public void race(){
        AtomicReference<Vector<Integer>> data = new AtomicReference(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Spouts.amb(Spouts.range(1, 10), Spouts.range(11, 20)).forEach(z->{
            assertFalse(complete.get());
            data.updateAndGet(s->s.plus(z));
        },e->{
            error.set(e);
        },()->{
            complete.set(true);
        });

        assertThat("Values " +  data,data.get(), hasItems(11, 12, 13, 14, 15, 16, 17, 18, 19));
        Assert.assertThat(complete.get(), IsEqual.equalTo(true));
        assertNull(error.get());
    }
    @Test
    public void raceBackpressure() {
        AtomicReference<Vector<Integer>> data = new AtomicReference(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Spouts.amb(Spouts.range(1, 10), Spouts.range(11, 20)).forEach(3,z->{
            assertFalse(complete.get());
            data.updateAndGet(s->s.plus(z));
        },e->{
            error.set(e);
        },()->{
            complete.set(true);
        });

        assertThat("Values " +  data,data.get(), hasItems(11, 12, 13));
        Assert.assertThat(complete.get(), IsEqual.equalTo(false));
        assertNull(error.get());
    }
    @Test
    public void raceFuture(){
        AtomicReference<Vector<Integer>> data = new AtomicReference(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Spouts.amb(Spouts.range(1, 10), Future.future()).forEach(z->{
            assertFalse(complete.get());
            data.updateAndGet(s->s.plus(z));
        },e->{
            error.set(e);
        },()->{
            complete.set(true);
        });

        assertThat("Values " +  data,data.get(), hasItems(1, 2, 3, 4, 5, 6, 7, 8, 9));
        Assert.assertThat(complete.get(), IsEqual.equalTo(true));
        assertNull(error.get());
    }
    @Test
    public void raceError(){
        AtomicReference<Vector<Integer>> data = new AtomicReference(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Spouts.amb(Future.ofError(new RuntimeException()), Future.<Integer>future()).forEach(z->{
            assertFalse(complete.get());
            data.updateAndGet(s->s.plus(z));
        },e->{
            error.set(e);
        },()->{
            complete.set(true);
        });

        assertThat("Values " +  data,data.get().size(), equalTo(0));
        Assert.assertThat(complete.get(), IsEqual.equalTo(true));
        assertThat(error.get(),instanceOf(RuntimeException.class));
    }

}
