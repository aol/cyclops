package com.aol.cyclops.types;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.aol.cyclops.types.stream.ConvertableSequence;

public abstract class AbstractConvertableSequenceTest {
    public abstract <T> ConvertableSequence<T> of(T...elements);
    public abstract <T> ConvertableSequence<T> empty();
    @Test
    public void emptyConvert(){

        assertFalse(empty().toOptional().isPresent());
        assertFalse(empty().toListX().size()>0);
        assertFalse(empty().toDequeX().size()>0);
        assertFalse(empty().toPStackX().size()>0);
        assertFalse(empty().toQueueX().size()>0);
        assertFalse(empty().toPVectorX().size()>0);
        assertFalse(empty().toPQueueX().size()>0);
        assertFalse(empty().toSetX().size()>0);
        assertFalse(empty().toSortedSetX().size()>0);
        assertFalse(empty().toPOrderedSetX().size()>0);
        assertFalse(empty().toPBagX().size()>0);
        assertFalse(empty().toPMapX(t->t,t->t).size()>0);
        assertFalse(empty().toMapX(t->t,t->t).size()>0);
        assertFalse(empty().toStreamable().size()>0);
        
        
    }
    @Test
    public void presentConvert(){

        assertTrue(of(1).toOptional().isPresent());
        assertTrue(of(1).toListX().size()>0);
        assertTrue(of(1).toDequeX().size()>0);
        assertTrue(of(1).toPStackX().size()>0);
        assertTrue(of(1).toQueueX().size()>0);
        assertTrue(of(1).toPVectorX().size()>0);
        assertTrue(of(1).toPQueueX().size()>0);
        assertTrue(of(1).toSetX().size()>0);
        assertTrue(of(1).toSortedSetX().size()>0);
        assertTrue(of(1).toPOrderedSetX().size()>0);
        assertTrue(of(1).toPBagX().size()>0);
        assertTrue(of(1).toPMapX(t->t,t->t).size()>0);
        assertTrue(of(1).toMapX(t->t,t->t).size()>0);
        assertTrue(of(1).toStreamable().size()>0);
        
        
    }

    
}
