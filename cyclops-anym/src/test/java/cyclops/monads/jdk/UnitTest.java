package cyclops.monads.jdk;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Optional;

import cyclops.reactive.collections.mutable.ListX;
import cyclops.monads.Witness;
import cyclops.monads.Witness.optional;
import org.junit.Test;

import cyclops.monads.AnyM;

public class UnitTest {
	@Test
	public void unitOptional() {
	    AnyM<optional,Integer> empty = AnyM.fromOptional(Optional.empty());
	    AnyM<optional,Integer> unit = empty.unit(1);
	    Optional<Integer> unwrapped = unit.unwrap();
	    assertEquals(Integer.valueOf(1), unwrapped.get());
	}

	@Test
	public void unitList() {
	    AnyM<Witness.list,Integer> empty = AnyM.fromList(ListX.empty());
	    AnyM<Witness.list,Integer> unit = empty.unit(1);
	    List<Integer> unwrapped = unit.stream().toList();
	    assertEquals(Integer.valueOf(1), unwrapped.get(0));
	}
}
