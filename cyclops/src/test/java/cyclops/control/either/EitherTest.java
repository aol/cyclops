package cyclops.control.either;

import cyclops.control.Either;
import cyclops.data.LazySeq;
import cyclops.companion.Monoids;
import cyclops.companion.Semigroups;

import cyclops.data.Seq;
import cyclops.data.Vector;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import org.junit.Test;

import java.util.Arrays;

import static cyclops.control.Either.left;
import static cyclops.control.Either.right;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
public class EitherTest {

	private String concat(String a,String b){
		return a+b;
	}
	public static class Base{ }
    public static class One extends Base{ }
    public static class Two extends Base{}

    @Test
    public void partitionEithersOrdered(){

        Seq<Either<Integer,String>> s = Seq.of(right("hello"),left(1),right("world"),left(2));
        Tuple2<Vector<Integer>, Vector<String>> v = Either.partitionEithers(s);
        Vector<Integer> ints = v._1();
        Vector<String> strs = v._2();
        System.out.println("ints " + ints);
        System.out.println("strs " + strs);
        System.out.println(v);
        assertThat(Either.partitionEithers(s),equalTo(Tuple.tuple(Seq.of(1,2),Seq.of("hello","world"))));
    }
    @Test
    public void partitionEithersVector(){
        Vector<Either<Integer,String>> s = Vector.of(right("hello"),left(1),right("world"),left(2));
        assertThat(Either.partitionEithers(s),equalTo(Tuple.tuple(Seq.of(1,2),Seq.of("hello","world"))));
    }
    @Test
    public void partitionEithersEmpty(){
        Vector<Either<Integer,String>> s = Vector.of();
        assertThat(Either.partitionEithers(s),equalTo(Tuple.tuple(Seq.of(),Seq.of())));
    }

    @Test
    public void leftEithersOrdered(){
        Seq<Either<Integer,String>> s = Seq.of(right("hello"),left(1),right("world"),left(2));

        assertThat(Either.lefts(s),equalTo(Seq.of(1,2)));
    }
    @Test
    public void leftsEithersVector(){
        Vector<Either<Integer,String>> s = Vector.of(right("hello"),left(1),right("world"),left(2));
        assertThat(Either.lefts(s),equalTo(Seq.of(1,2)));
    }
    @Test
    public void leftsEithersEmpty(){
        Vector<Either<Integer,String>> s = Vector.of();
        assertThat(Either.lefts(s),equalTo(Seq.of()));
    }

    @Test
    public void rightsEithersOrdered(){
        Seq<Either<Integer,String>> s = Seq.of(right("hello"),left(1),right("world"),left(2));

        assertThat(Either.rights(s),equalTo(Seq.of("hello","world")));
    }
    @Test
    public void rightsEithersVector(){
        Vector<Either<Integer,String>> s = Vector.of(right("hello"),left(1),right("world"),left(2));
        assertThat(Either.rights(s),equalTo(Seq.of("hello","world")));
    }
    @Test
    public void rightsEithersEmpty(){
        Vector<Either<Integer,String>> s = Vector.of();
        assertThat(Either.rights(s),equalTo(Seq.of()));
    }

    @Test
    public void visitAny(){

        Either<One,Two> test = right(new Two());
        test.to(Either::applyAny).apply(b->b.toString());
        right(10).to(Either::consumeAny).accept(System.out::println);
        right(10).to(e-> Either.visitAny(System.out::println,e));
        Object value = right(10).to(e-> Either.visitAny(e, x->x));
        assertThat(value,equalTo(10));
    }



	@Test
	public void test2() {



		assertThat(Either.accumulateLeft(Monoids.stringConcat, Arrays.asList(left("failed1"),
													left("failed2"),
													right("success"))
													).orElse(":"),equalTo("failed1failed2"));

	}






	@Test
	public void applicative(){
	    Either<String,String> fail1 =  left("failed1");
	    Either<String,String> result = fail1.combine(left("failed2"), Semigroups.stringConcat,(a, b)->a+b);
	    assertThat(result.leftOrElse(":"),equalTo("failed2failed1"));
	}
	@Test
    public void applicativeColleciton(){
        Either<String,String> fail1 =  left("failed1");
        Either<LazySeq<String>,String> result = fail1.lazySeq().combine(left("failed2").lazySeq(), Semigroups.lazySeqConcat(),(a, b)->a+b);
        assertThat(result.leftOrElse(LazySeq.empty()),equalTo(LazySeq.of("failed2","failed1")));
    }
	@Test
    public void applicativeLazySeq(){
        Either<String,String> fail1 =  left("failed1");
        Either<LazySeq<String>,String> result = fail1.combineToLazySeq(Either.<String,String>left("failed2"),(a, b)->a+b);
        assertThat(result.leftOrElse(LazySeq.empty()),equalTo(LazySeq.of("failed2","failed1")));
    }
    @Test
    public void applicativeVector(){
        Either<String,String> fail1 =  left("failed1");
        Either<Vector<String>,String> result = fail1.combineToVector(Either.<String,String>left("failed2"),(a, b)->a+b);
        assertThat(result.leftOrElse(Vector.empty()),equalTo(Vector.of("failed2","failed1")));
    }


}
