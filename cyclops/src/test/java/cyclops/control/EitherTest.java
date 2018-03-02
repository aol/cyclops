package cyclops.control;

import cyclops.data.LazySeq;
import cyclops.companion.Monoids;
import cyclops.companion.Semigroups;

import cyclops.data.Vector;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
public class EitherTest {

	private String concat(String a,String b){
		return a+b;
	}
	static class Base{ }
    static class One extends Base{ }
    static class Two extends Base{}
    @Test
    public void visitAny(){

        Either<One,Two> test = Either.right(new Two());
        test.to(Either::applyAny).apply(b->b.toString());
        Either.right(10).to(Either::consumeAny).accept(System.out::println);
        Either.right(10).to(e-> Either.visitAny(System.out::println,e));
        Object value = Either.right(10).to(e-> Either.visitAny(e, x->x));
        assertThat(value,equalTo(10));
    }



	@Test
	public void test2() {



		assertThat(Either.accumulateLeft(Monoids.stringConcat, Arrays.asList(Either.left("failed1"),
													Either.left("failed2"),
													Either.right("success"))
													).orElse(":"),equalTo("failed1failed2"));

	}






	@Test
	public void applicative(){
	    Either<String,String> fail1 =  Either.left("failed1");
	    Either<String,String> result = fail1.combine(Either.left("failed2"), Semigroups.stringConcat,(a, b)->a+b);
	    assertThat(result.leftOrElse(":"),equalTo("failed2failed1"));
	}
	@Test
    public void applicativeColleciton(){
        Either<String,String> fail1 =  Either.left("failed1");
        Either<LazySeq<String>,String> result = fail1.lazySeq().combine(Either.left("failed2").lazySeq(), Semigroups.lazySeqConcat(),(a, b)->a+b);
        assertThat(result.leftOrElse(LazySeq.empty()),equalTo(LazySeq.of("failed2","failed1")));
    }
	@Test
    public void applicativeLazySeq(){
        Either<String,String> fail1 =  Either.left("failed1");
        Either<LazySeq<String>,String> result = fail1.combineToLazySeq(Either.<String,String>left("failed2"),(a, b)->a+b);
        assertThat(result.leftOrElse(LazySeq.empty()),equalTo(LazySeq.of("failed2","failed1")));
    }
    @Test
    public void applicativeVector(){
        Either<String,String> fail1 =  Either.left("failed1");
        Either<Vector<String>,String> result = fail1.combineToVector(Either.<String,String>left("failed2"),(a, b)->a+b);
        assertThat(result.leftOrElse(Vector.empty()),equalTo(Vector.of("failed2","failed1")));
    }


}
