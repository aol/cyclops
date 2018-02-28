package cyclops.monads.jdk;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.oath.cyclops.ReactiveConvertableSequence;
import org.junit.Test;

import cyclops.monads.AnyM;
import cyclops.reactive.collections.mutable.ListX;

public class UnwrapTest {

	@Test
	public void unwrap(){
		Stream<String> stream = AnyM.streamOf("hello","world").stream().stream();
		assertThat(stream.collect(Collectors.toList()),equalTo(Arrays.asList("hello","world")));
	}

	@Test
	public void unwrapOptional(){
		Optional<ListX<String>> stream = AnyM.streamOf("hello","world")
											.stream().to(ReactiveConvertableSequence::converter)
											.optional();
		assertThat(stream.get(),equalTo(Arrays.asList("hello","world")));
	}

}
