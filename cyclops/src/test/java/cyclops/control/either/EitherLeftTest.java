package cyclops.control.either;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.FileNotFoundException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cyclops.control.Option;
import cyclops.control.Either;
import org.junit.Before;
import org.junit.Test;

public class EitherLeftTest {

	Either<FileNotFoundException,Integer> failure;
	FileNotFoundException error = new FileNotFoundException();
	@Before
	public void setup(){
		failure = Either.left(error);
	}


	@Test
	public void testOf() {
		assertNotNull(failure);
	}



	@Test
	public void testMap() {
		assertThat(failure.map(x->x+1),equalTo(failure));
	}

	@Test
	public void testFlatMap() {
		assertThat(failure.flatMap(x-> Either.right(10)),equalTo(failure));
	}

	@Test
	public void testFilter() {
		assertThat(failure.filter(x->x==10),equalTo(Option.none()));
	}

	

	
	@Test
	public void testOrElse() {
		assertThat(failure.orElse(10),equalTo(10));
	}

	@Test
	public void testOrElseGet() {
		assertThat(failure.orElseGet(()->10),equalTo(10));
	}

	@Test
	public void testToOptional() {
		assertThat(failure.toOptional(),equalTo(Optional.empty()));
	}

	@Test
	public void testToStream() {
		assertThat(failure.stream().collect(Collectors.toList()),
				equalTo(Stream.of().collect(Collectors.toList())));
	}

	@Test
	public void testIsSuccess() {
		assertThat(failure.isRight(),equalTo(false));
	}

	@Test
	public void testIsFailure() {
		assertThat(failure.isLeft(),equalTo(true));
	}

	Integer value = null;
	@Test
	public void testForeach() {
		
		failure.forEach(v -> value = v);
		assertThat(value,is(nullValue()));
	}

	
	Object errorCaptured;
	@Test
	public void testForeachFailed() {
		errorCaptured = null;
		failure.peekLeft(e -> errorCaptured =e);
		assertThat(error,equalTo(errorCaptured));
	}

	

}
