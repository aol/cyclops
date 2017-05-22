package com.aol.cyclops2.types;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.aol.cyclops2.types.stream.ConvertableSequence;

public abstract class AbstractConvertableSequenceTest {
    public abstract <T> ConvertableSequence<T> of(T...elements);
    public abstract <T> ConvertableSequence<T> empty();
    @Test
    public void emptyConvert(){

        assertFalse(empty().optional().isPresent());
        assertFalse(empty().listX().size()>0);
        assertFalse(empty().dequeX().size()>0);
        assertFalse(empty().linkedListX().size()>0);
        assertFalse(empty().queueX().size()>0);
        assertFalse(empty().vectorX().size()>0);
        assertFalse(empty().persistentQueueX().size()>0);
        assertFalse(empty().setX().size()>0);
        assertFalse(empty().sortedSetX().size()>0);
        assertFalse(empty().orderedSetX().size()>0);
        assertFalse(empty().bagX().size()>0);
        assertFalse(empty().persistentMapX(t->t, t->t).size()>0);
        assertFalse(empty().mapX(t->t,t->t).size()>0);
        assertFalse(empty().streamable().size()>0);
        
        
    }
    @Test
    public void presentConvert(){

        assertTrue(of(1).optional().isPresent());
        assertTrue(of(1).listX().size()>0);
        assertTrue(of(1).dequeX().size()>0);
        assertTrue(of(1).linkedListX().size()>0);
        assertTrue(of(1).queueX().size()>0);
        assertTrue(of(1).vectorX().size()>0);
        assertTrue(of(1).queueX().size()>0);
        assertTrue(of(1).setX().size()>0);
        assertTrue(of(1).sortedSetX().size()>0);
        assertTrue(of(1).orderedSetX().size()>0);
        assertTrue(of(1).bagX().size()>0);
        assertTrue(of(1).persistentMapX(t->t, t->t).size()>0);
        assertTrue(of(1).mapX(t->t,t->t).size()>0);
        assertTrue(of(1).streamable().size()>0);
        
        
    }

    
}
