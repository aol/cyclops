package com.aol.cyclops.streams.anyM;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.internal.Monad;
import com.aol.cyclops.internal.monads.AnyMSeqImpl;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.types.anyM.AnyMSeq;
public class MonadBugTest {
	@Test
	 public void test() {
	    List<Integer> list = Arrays.asList(1,2,3);
	    Monad<Integer> m = Monad.of(list.stream());
	    AnyMSeq<Integer> any = m.anyMSeq();
	    AnyM<Integer> mapped = any.flatMap(e -> any.unit(e));
	    List<Integer> unwrapped = mapped.stream().toList();
	    assertEquals(list, unwrapped);
	}
}
