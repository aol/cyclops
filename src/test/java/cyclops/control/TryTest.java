package cyclops.control;

import cyclops.async.Future;
import cyclops.async.LazyReact;
import cyclops.function.Monoid;
import cyclops.companion.Semigroups;
import com.aol.cyclops2.util.box.Mutable;
import cyclops.collections.mutable.ListX;
import cyclops.companion.Streams;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;



public class TryTest {

	Try<Integer,RuntimeException> just;
	Try<Integer,RuntimeException> none;
	RuntimeException exception = new RuntimeException();
	@Before
	public void setUp() throws Exception {
		just = Try.success(10);
		none = Try.failure(exception);

		just.toEither(-5000).mapLeft(x-> new Exception()).toTry(Exception.class);
	}


	@Test
    public void recover(){

        final String result = Try.withCatch(() -> "takeOne", RuntimeException.class)
                .recoverFlatMap(__ -> Try.<String,RuntimeException>success("ignored")
                        .retry(i->"retry"))
                .orElse("boo!");
        Try.withCatch(() -> "hello", RuntimeException.class)
           .recover(()->"world");
	}





	@Test
    public void nest(){
       assertThat(just.nest().map(m->m.toOptional().get()),equalTo(just));
       assertThat(none.nest().map(m->m.get()),equalTo(none));
    }
    @Test
    public void coFlatMap(){
        assertThat(just.coflatMap(m-> m.isPresent()? m.toOptional().get() : 50),equalTo(just));
        assertThat(none.coflatMap(m-> m.isPresent()? m.toOptional().get() : 50),equalTo(Try.success(50)));
    }


	@Test
	public void testToMaybe() {
		assertThat(just.toMaybe(),equalTo(Maybe.of(10)));
		assertThat(none.toMaybe(),equalTo(Maybe.nothing()));
	}

	private int add1(int i){
		return i+1;
	}

	@Test
	public void testOfT() {
		assertThat(Ior.right(1),equalTo(Ior.right(1)));
	}








	@Test
	public void testUnitT() {
		assertThat(just.unit(20),equalTo(Try.success(20)));
	}



	@Test
	public void testisPrimary() {
		assertTrue(just.isSuccess());
		assertFalse(none.isSuccess());
	}


	@Test
	public void testMapFunctionOfQsuperTQextendsR() {
		assertThat(just.map(i->i+5),equalTo(Try.success(15)));
		assertThat(none.map(i->i+5).toEither(),equalTo(Either.left(exception)));
	}

	@Test
	public void testFlatMap() {
		assertThat(just.flatMap(i->Try.success(i+5)),equalTo(Try.success(15)));
		assertThat(none.flatMap(i->Try.success(i+5)),equalTo(Try.failure(exception)));
	}

	@Test
	public void testWhenFunctionOfQsuperTQextendsRSupplierOfQextendsR() {
		assertThat(just.visit(i->i+1,()->20),equalTo(11));
		assertThat(none.visit(i->i+1,()->20),equalTo(20));
	}



	@Test
	public void testStream() {
		assertThat(just.stream().toListX(),equalTo(ListX.of(10)));
		assertThat(none.stream().toListX(),equalTo(ListX.of()));
	}

	@Test
	public void testOfSupplierOfT() {

	}

	@Test
    public void testConvertTo() {
        Stream<Integer> toStream = just.visit(m->Stream.of(m),()->Stream.of());
        assertThat(toStream.collect(Collectors.toList()),equalTo(ListX.of(10)));
    }

    @Test
    public void testConvertToAsync() {
        Future<Stream<Integer>> async = Future.of(()->just.visit(f->Stream.of((int)f),()->Stream.of()));

        assertThat(async.orElse(Stream.empty()).collect(Collectors.toList()),equalTo(ListX.of(10)));
    }


	@Test
	public void testIterate() {
		assertThat(just.asSupplier(-100).iterate(i->i+1).limit(10).sumInt(i->i),equalTo(145));
	}

	@Test
	public void testGenerate() {
		assertThat(just.asSupplier(-100).generate().limit(10).sumInt(i->i),equalTo(100));
	}




	@Test
	public void testToXor() {
		assertThat(just.toEither(-5000),equalTo(Either.right(10)));

	}
	@Test
	public void testToXorNone(){
		Either<RuntimeException,Integer> xor = none.toEither();
		assertTrue(xor.isLeft());
		assertThat(xor,equalTo(Either.left(exception)));

	}


	@Test
	public void testToXorSecondary() {
		assertThat(just.toEither(-5000).swap(),equalTo(Either.left(10)));
	}

	@Test
	public void testToXorSecondaryNone(){

		Either<Integer,RuntimeException> xorNone = none.toEither().swap();
		assertThat(xorNone,equalTo(Either.right(exception)));

	}
	@Test
	public void testToTry() {
		assertTrue(none.toTry().isFailure());
		assertThat(just.toTry(),equalTo(Try.success(10)));
	}

	@Test
	public void testToTryClassOfXArray() {
		assertTrue(none.toTry(Throwable.class).isFailure());
	}

	@Test
	public void testToIor() {
		assertThat(just.toIor(),equalTo(Ior.right(10)));

	}
	@Test
	public void testToIorNone(){
		Ior<RuntimeException,Integer> ior = none.toIor();
		assertTrue(ior.isLeft());
        assertThat(ior,equalTo(Ior.left(exception)));

	}


	@Test
	public void testToIorSecondary() {
		assertThat(just.toIor().swap(),equalTo(Ior.left(10)));
	}


	@Test
	public void testToIorSecondaryNone(){
	    Ior<Integer,RuntimeException> ior = none.toIor().swap();
        assertTrue(ior.isRight());
        assertThat(ior,equalTo(Ior.right(exception)));

	}





	@Test
	public void testMkString() {
		assertThat(just.mkString(),equalTo("Success[10]"));
		assertThat(none.mkString(),equalTo("Failure["+exception+"]"));
	}
	LazyReact react = new LazyReact();


	@Test
	public void testGet() {
		assertThat(just.get(),equalTo(Option.some(10)));
	}


	@Test
	public void testFilter() {
		assertFalse(just.filter(i->i<5).isPresent());
		assertTrue(just.filter(i->i>5).isPresent());
		assertFalse(none.filter(i->i<5).isPresent());
		assertFalse(none.filter(i->i>5).isPresent());

	}

	@Test
	public void testOfType() {
		assertFalse(just.ofType(String.class).isPresent());
		assertTrue(just.ofType(Integer.class).isPresent());
		assertFalse(none.ofType(String.class).isPresent());
		assertFalse(none.ofType(Integer.class).isPresent());
	}

	@Test
	public void testFilterNot() {
		assertTrue(just.filterNot(i->i<5).isPresent());
		assertFalse(just.filterNot(i->i>5).isPresent());
		assertFalse(none.filterNot(i->i<5).isPresent());
		assertFalse(none.filterNot(i->i>5).isPresent());
	}

	@Test
	public void testNotNull() {
		assertTrue(just.notNull().isPresent());
		assertFalse(none.notNull().isPresent());

	}


	private int add(int a, int b){
		return a+b;
	}


	private int add3(int a, int b, int c){
		return a+b+c;
	}

	private int add4(int a, int b, int c,int d){
		return a+b+c+d;
	}

	private int add5(int a, int b, int c,int d,int e){
		return a+b+c+d+e;
	}




	@Test
	public void testFoldRightMonoidOfT() {
		assertThat(just.fold(Monoid.of(1,Semigroups.intMult)),equalTo(10));
	}





	@Test
	public void testWhenFunctionOfQsuperMaybeOfTQextendsR() {
		assertThat(just.visit(s->"hello", ()->"world"),equalTo("hello"));
		assertThat(none.visit(s->"hello", ()->"world"),equalTo("world"));
	}


	@Test
	public void testOrElseGet() {
		assertThat(none.orElseGet(()->2),equalTo(2));
		assertThat(just.orElseGet(()->2),equalTo(10));
	}

	@Test
	public void testToOptional() {
		assertFalse(none.toOptional().isPresent());
		assertTrue(just.toOptional().isPresent());
		assertThat(just.toOptional(),equalTo(Optional.of(10)));
	}

	@Test
	public void testToStream() {
		assertThat(none.stream().collect(Collectors.toList()).size(),equalTo(0));
		assertThat(just.stream().collect(Collectors.toList()).size(),equalTo(1));

	}


	@Test
	public void testOrElse() {
		assertThat(none.orElse(20),equalTo(20));
		assertThat(just.orElse(20),equalTo(10));
	}



	Executor exec = Executors.newFixedThreadPool(1);


	@Test
	public void testIterator1() {
		assertThat(Streams.stream(just.iterator()).collect(Collectors.toList()),
				equalTo(Arrays.asList(10)));
	}

	@Test
	public void testForEach() {
		Mutable<Integer> capture = Mutable.of(null);
		 none.forEach(c->capture.set(c));
		assertNull(capture.get());
		just.forEach(c->capture.set(c));
		assertThat(capture.get(),equalTo(10));
	}

	@Test
	public void testSpliterator() {
		assertThat(StreamSupport.stream(just.spliterator(),false).collect(Collectors.toList()),
				equalTo(Arrays.asList(10)));
	}


	@Test
	public void testMapFunctionOfQsuperTQextendsR1() {
		assertThat(just.map(i->i+5),equalTo(Try.success(15)));
	}

	@Test
	public void testPeek() {
		Mutable<Integer> capture = Mutable.of(null);
		just = just.peek(c->capture.set(c));

		assertThat(capture.get(),equalTo(10));
	}

	private Trampoline<Integer> sum(int times, int sum){
		return times ==0 ?  Trampoline.done(sum) : Trampoline.more(()->sum(times-1,sum+times));
	}
	@Test
	public void testTrampoline() {
		assertThat(just.trampoline(n ->sum(10,n)).toEither(),equalTo(Either.right(65)));
	}



	@Test
	public void testUnitT1() {
		assertThat(none.unit(10),equalTo(just));
	}

}
