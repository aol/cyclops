package cyclops.collections.persistent.anyM;

import com.oath.cyclops.types.anyM.AnyMSeq;
import cyclops.collections.AbstractAnyMSeqOrderedDependentTest;
import cyclops.collections.immutable.PersistentQueueX;
import cyclops.collections.mutable.ListX;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class PersistentQueueXTest extends AbstractAnyMSeqOrderedDependentTest<Witness.persistentQueueX>{

	@Override
	public <T> AnyMSeq<Witness.persistentQueueX,T> of(T... values) {
		return AnyM.fromPersistentQueueX(PersistentQueueX.of(values));
	}
	/* (non-Javadoc)
	 * @see com.oath.cyclops.function.collections.extensions.AbstractCollectionXTest#zero()
	 */
	@Override
	public <T> AnyMSeq<Witness.persistentQueueX,T> empty() {
		return AnyM.fromPersistentQueueX(PersistentQueueX.empty());
	}
	@Test
    public void when(){

        String res= AnyM.fromList(ListX.of(1,2,3)).visit((x,xs)->
                                xs.join(x>2? "hello" : "world"),()->"boo!");

        assertThat(res,equalTo("2world3"));
    }
	@Test
    public void whenGreaterThan2(){
        String res= of(5,2,3).visit((x,xs)->
                                xs.join(x>2? "hello" : "world"),()->"boo!");

        assertThat(res,equalTo("2hello3"));
    }
    @Test
    public void when2(){

        Integer res =   of(1,2,3).visit((x,xs)->x,()->10);
        System.out.println(res);
    }
    @Test
    public void whenNilOrNot(){
        String res1=    of(1,2,3).visit((x,xs)-> x>2? "hello" : "world",()->"EMPTY");
    }
    @Test
    public void whenNilOrNotJoinWithFirstElement(){


        String res= of(1,2,3).visit((x,xs)-> xs.join(x>2? "hello" : "world"),()->"EMPTY");
        assertThat(res,equalTo("2world3"));
    }
	/**
	 *
		Eval e;
		//int cost = ReactiveSeq.of(1,2).when((head,tail)-> head.when(h-> (int)h>5, h-> 0 )
		//		.flatMap(h-> head.when());

		ht.headMaybe().when(some-> Matchable.of(some).matches(
											c->c.hasValues(1,2,3).then(i->"hello world"),
											c->c.hasValues('b','b','c').then(i->"boo!")
									),()->"hello");
									**/


}

