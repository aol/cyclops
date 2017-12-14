package com.oath.cyclops.trycatch;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.function.Consumer;

import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import org.junit.Test;

import cyclops.control.Try;
public class TryTest {

	@Test
	public void test(){
		Try<Integer,RuntimeException> tryM;

		Try.runWithCatch(this::exceptional,IOException.class)
					.onFail(System.out::println).get();
	}

	private Consumer<IOException> extractFromMemoryCace() {
		// TODO Auto-generated method stub
		return null;
	}
	@Test
	public void ongoing(){
        Try<Object, Throwable> t = Try.success(2, RuntimeException.class).map(a -> {
            throw new RuntimeException();});
            System.out.println(t.toString());

            assertTrue(Try.success(2, RuntimeException.class)
                    .map(i -> {
                        throw new RuntimeException();
                    }).isFailure());

        };

    public int throwsEx() throws Exception{
	    return 0;
    }
    @Test
    public void testExcept(){

        assertThat(Try.withCatch(()-> throwsEx())
                .map(i->i+" woo!")
                .onFail(System.out::println)
                .flatMap(e->Try.withCatch(()-> throwsEx()))
                .orElse(1),is(0));

        Try.withCatch(() -> throwsEx())
                .onFail(e -> {})
                .flatMap(v -> Try.withCatch(()-> throwsEx()));
    }

    @Test
	public void test2(){
		assertThat(Try.withCatch(()-> exceptional2())
						.map(i->i+" woo!")
						.onFail(System.out::println)

						.orElse("default"),is("hello world woo!"));


	}

	@Test
	public void catchExceptonsWithRun(){
		assertThat(Try.withCatch(()-> exceptional2(),RuntimeException.class)
			.onFail(System.out::println)
			.map(i->i+"woo!")
			.toOptional()
			.orElse("hello world"),is("hello worldwoo!"));
	}

	@Test
	public void testTryWithResources(){

		assertThat(Try.withResources(() -> new BufferedReader(new FileReader("file.txt")),
      this::read,
      IOException.class,NullPointerException.class, FileNotFoundException.class).toFailedOptional().get(),instanceOf((Class)FileNotFoundException.class));




	}

	@Test
  public void mapOrCatch(){
	  IOException local = new IOException();
	  assertThat(Try.success(10)
                   .mapOrCatch(i->i+1,IOException.class),equalTo(Try.success(11)));
    assertThat(Try.success(10)
              .mapOrCatch(i->{throw local;},IOException.class),equalTo(Try.failure(local)));

  }
  @Test(expected = RuntimeException.class)
  public void mapOrCatchEx(){
    Try.success(10)
      .mapOrCatch(i->{throw new RuntimeException();});
    fail("exception expected");

  }
  @Test
  public void flatMapOrCatch(){
    IOException local = new IOException();
    assertThat(Try.success(10)
      .flatMapOrCatch(i->Try.success(i+1),IOException.class),equalTo(Try.success(11)));
    assertThat(Try.success(10)
      .flatMapOrCatch(i->{throw local;},IOException.class),equalTo(Try.failure(local)));

  }
  @Test(expected = RuntimeException.class)
  public void flatMapOrCatchEx(){
    Try.success(10)
      .flatMapOrCatch(i->{throw new RuntimeException();});
    fail("exception expected");

  }
	public void testMultipleResources(){

		Try t2 = Try.withResources(()->new BufferedReader(new FileReader("file.txt")),
                               ()->new FileReader("hello"),
				                        this::read2,FileNotFoundException.class,IOException.class);

	}

	private String read2(BufferedReader br,FileReader fr) throws IOException{
		String line = br.readLine();
		return null;
	}
	private String read(BufferedReader br) throws IOException{
		StringBuilder sb = new StringBuilder();
        String line = br.readLine();

        while (line != null) {
            sb.append(line);
            sb.append(System.lineSeparator());
            line = br.readLine();
        }
        String everything = sb.toString();
        return everything;
	}

	private void exceptional() throws IOException{
		throw new IOException();
	}
	private String exceptional2() throws RuntimeException{
		return "hello world";
	}
}
