package cyclops.futurestream.react.lazy;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import com.oath.cyclops.ReactiveConvertableSequence;
import cyclops.futurestream.LazyReact;
import org.junit.Test;

public class ToOptionalCompletableFutureTest {

	@Test
	public void toCompletableFuture(){
		assertThat(LazyReact.sequentialBuilder().of(1,2,3,4)
						.toCompletableFuture()
						.join(),equalTo(Arrays.asList(1,2,3,4)));

	}
	@Test
	public void toOptional(){
		assertThat(LazyReact.sequentialBuilder().of(1,2,3,4).to(ReactiveConvertableSequence::converter)
						.optional()
						.get(),equalTo(Arrays.asList(1,2,3,4)));

	}
	@Test
	public void toOptionalEmpty(){
		assertFalse(LazyReact.sequentialBuilder().of().to(ReactiveConvertableSequence::converter)
						.optional().isPresent());

	}
}
