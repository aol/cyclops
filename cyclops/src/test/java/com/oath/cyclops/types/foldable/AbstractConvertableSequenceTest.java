package com.oath.cyclops.types.foldable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.oath.cyclops.types.foldable.ConvertableSequence;
import org.junit.Test;

import java.util.Comparator;

public abstract class AbstractConvertableSequenceTest {
    public abstract <T> ConvertableSequence<T> of(T...elements);
    public abstract <T> ConvertableSequence<T> empty();
    @Test
    public void emptyConvert(){

        assertFalse(empty().option().isPresent());
        assertFalse(empty().seq().size()>0);
        assertFalse(empty().lazySeq().size()>0);
        assertFalse(empty().vector().size()>0);
        assertFalse(empty().bankersQueue().size()>0);
        assertFalse(empty().hashSet().size()>0);
        assertFalse(empty().treeSet((Comparator)Comparator.naturalOrder()).size()>0);
        assertFalse(empty().hashMap(t->t,t->t).size()>0);




    }
    @Test
    public void presentConvert(){

        assertTrue(of(1).option().isPresent());

        assertTrue(of(1).seq().size()>0);
        assertTrue(of(1).lazySeq().size()>0);
        assertTrue(of(1).bankersQueue().size()>0);
        assertTrue(of(1).vector().size()>0);
        assertTrue(of(1).hashSet().size()>0);
        assertTrue(of(1).treeSet(Comparator.naturalOrder()).size()>0);
        assertTrue(of(1).bag().size()>0);
        assertTrue(of(1).hashMap(t->t, t->t).size()>0);

    }

    @Test
    public void lazyString(){
        assertThat(of(1,2).lazyString().toString(),equalTo(of(1,2).stream().join(", ")));
    }
    @Test
    public void lazyStringEmpty(){
        assertThat(empty().lazyString().toString(),equalTo(empty().stream().join(",")));
    }


}
