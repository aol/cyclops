package cyclops;

import com.oath.cyclops.types.foldable.ConvertableSequence;
import org.junit.Test;

import java.util.Comparator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

        assertTrue(of(1).option().isPresent());

        assertTrue(of(1).seq().size()>0);
        assertTrue(of(1).lazySeq().size()>0);
        assertTrue(of(1).bankersQueue().size()>0);
        assertTrue(of(1).vector().size()>0);
        assertTrue(of(1).hashSet().size()>0);
        assertTrue(of(1).treeSet(Comparator.naturalOrder()).size()>0);
        assertTrue(of(1).streamable().size()>0);
        assertTrue(of(1).bag().size()>0);
        assertTrue(of(1).hashMap(t->t, t->t).size()>0);

    }


}
