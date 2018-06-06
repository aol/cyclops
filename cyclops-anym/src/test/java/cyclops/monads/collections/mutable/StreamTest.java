package cyclops.monads.collections.mutable;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.stream.Stream;

import com.oath.cyclops.anym.AnyMSeq;
import com.oath.cyclops.ReactiveConvertableSequence;
import cyclops.reactive.collections.mutable.ListX;
import cyclops.monads.Witness.stream;
import cyclops.monads.collections.AbstractAnyMSeqOrderedDependentTest;
import org.junit.Test;

import cyclops.monads.AnyM;

public class StreamTest extends AbstractAnyMSeqOrderedDependentTest<stream> {

	@Override
	public <T> AnyMSeq<stream,T> of(T... values) {
		return AnyM.fromStream(Stream.of(values));
	}

	@Override
	public <T> AnyMSeq<stream,T> empty() {
		return AnyM.fromStream(Stream.empty());
	}

	int count = 0;
    @Test
    public void testCycleUntil2() {
        count =0;
        System.out.println("Cycle until!");
        count =0;
        ListX<Integer> b= of(1, 2, 3).peek(System.out::println)
                            .cycleUntil(next->count++==6).to(ReactiveConvertableSequence::converter).listX();
        System.out.println("2 " + b);

    }

}

