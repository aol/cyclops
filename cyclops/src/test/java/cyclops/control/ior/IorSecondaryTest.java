package cyclops.control.ior;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cyclops.control.Ior;
import cyclops.control.Option;
import org.junit.Before;
import org.junit.Test;

public class IorSecondaryTest {

	Ior<FileNotFoundException,Integer> failure;
	FileNotFoundException error = new FileNotFoundException();
	@Before
	public void setup(){
		failure = Ior.left(error);
	}
	@Test
    public void bimap(){

        Ior<RuntimeException,Integer> mapped = failure.bimap(e->new RuntimeException(), d->d+1);
        assertTrue(mapped.isLeft());
        assertThat(mapped.swap().orElse(null),instanceOf(RuntimeException.class));
    }
    Throwable capT;
    int capInt=0;
    @Test
    public void bipeek(){
       capT =null;
       capInt=0;
         failure.bipeek(e->capT=e, d->capInt=d);
        assertThat(capInt,equalTo(0));
        assertThat(capT,instanceOf(FileNotFoundException.class));
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
		assertThat(failure.flatMap(x->Ior.right(10)),equalTo(failure));
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
