package cyclops.control.either;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cyclops.control.Option;
import cyclops.control.Either;
import org.junit.Before;
import org.junit.Test;

public class EitherRightTest {

	Either<FileNotFoundException,Integer> success;
	final Integer value = 10;
	
	
	public Either<FileNotFoundException,String> load(String filename){
		return Either.right("test-data");
	}
	
	public void process(){
		
		Either<FileNotFoundException,String> attempt = load("data");
		
		attempt.map(String::toUpperCase)
				.peek(System.out::println);
	
	}
	
	@Before
	public void setup(){
		success = Either.right(10);
	}



	@Test
	public void testGet() {
		assertThat(success.get(),equalTo(Option.some(value)));
	}

	@Test
	public void testOf() {
		assertThat(value,not(nullValue()));
	}

	@Test
	public void testMap() {
		assertThat(success.map(x->x+1),equalTo(Either.right(value+1)));
	}

	@Test
	public void testFlatMap() {
		assertThat(success.flatMap(x-> Either.right(x+1)),equalTo(Either.right(value+1)));
	}

	@Test
	public void testFilter() {
		assertThat(success.filter(x->x>5),equalTo(Option.some(value)));
	}
	@Test
	public void testFilterFail() {
		assertThat(success.filter(x->x>15),equalTo(Option.none()));
	}

	

	@Test
	public void testOrElse() {
		assertThat(success.orElse(30),equalTo(value));
	}

	@Test
	public void testOrElseGet() {
		assertThat(success.orElseGet(()->30),equalTo(value));
	}

	@Test
	public void testToOptional() {
		assertThat(success.toOptional(),equalTo(Optional.of(value)));
	}

	@Test
	public void testToStream() {
		assertThat(success.stream().collect(Collectors.toList()),
				equalTo(Stream.of(value).collect(Collectors.toList())));
	}

	@Test
	public void testIsSuccess() {
		assertTrue(success.isRight());
	}

	@Test
	public void testIsFailure() {
		assertFalse(success.isLeft());
	}
	Integer valueCaptured = null;
	@Test
	public void testForeach() {
		success.forEach(v -> valueCaptured = v);
		assertThat(valueCaptured,is(10));
	}
	Exception errorCaptured;
	

}
