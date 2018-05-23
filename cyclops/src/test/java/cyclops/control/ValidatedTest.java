package cyclops.control;

import cyclops.companion.Monoids;
import cyclops.data.NonEmptyList;
import cyclops.data.Seq;
import cyclops.reactive.Spouts;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;

public class ValidatedTest {

    Validated<String,Integer> valid = Validated.valid(10);
    Validated<String,Integer> invalid = Validated.invalid("failed");
    Validated<Throwable,Integer> async = Validated.fromPublisher(Spouts.of(10));

    Integer v = 0;
    @Test
    public void map(){
        assertThat(valid.map(i->i+1),equalTo(Validated.valid(11)));
        assertThat(invalid.map(i->i+1),equalTo(Validated.invalid("failed")));
        assertThat(async.map(i->i+1),equalTo(Validated.valid(11)));
    }
    @Test
    public void bimap(){
        assertThat(valid.bimap(s->s,i->i+1),equalTo(Validated.valid(11)));
        assertThat(invalid.bimap(s->s+"hello",i->i+1),equalTo(Validated.invalid("failedhello")));
        assertThat(async.bimap(s->s,i->i+1),equalTo(Validated.valid(11)));
    }

    @Test
    public void toStringTest(){
        assertThat(valid.toString(),equalTo("Valid[10]"));
        assertThat(invalid.toString(),equalTo("Invalid[failed]"));

        assertThat(invalid.sequence(Seq.of(Validated.invalid("boo!"))).toString(),equalTo("Invalid[failed,boo!]"));

        assertThat(Validated.fromPublisher(Future.ofResult(10)).toString(),equalTo("Valid[10]"));
    }


    @Test
    public void peek(){
        valid.peek(a->v=a).isValid();
        assertThat(v,equalTo(10));

        v= 0;
        invalid.peek(a->v=a).isValid();
        assertThat(v,equalTo(0));

        async.peek(a->v=a).isValid();
        assertThat(v,equalTo(10));

        v= 0;
    }



    @Test
    public void combine(){
        assertThat(valid.combine((a,b)->a+b,Validated.valid(100)).orElse(-1),equalTo(110));
        assertThat(valid.combine((a,b)->a+b,invalid).orElseInvalid("boo!"),equalTo(NonEmptyList.of("failed")));
        assertThat(invalid.combine((a,b)->a+b,Validated.valid(100)).orElseInvalid("boo!"),equalTo(NonEmptyList.of("failed")));
        assertThat(invalid.combine((a,b)->a+b,Validated.invalid("boo!")).orElseInvalid("oops!"),equalTo(NonEmptyList.of("failed","boo!")));
        assertThat(async.combine((a,b)->a+b,Validated.valid(100)).orElse(-1),equalTo(110));
    }

    @Test
    public void foldInvalid(){
        String result = invalid.foldInvalidLeft("",(a,b)->a+b);
        assertThat(result,equalTo("failed"));

        String result2 = valid.foldInvalidLeft("",(a,b)->a+b);
        assertThat(result2,equalTo(""));

        String result3 = async.foldInvalidLeft("",(a,b)->a+b);
        assertThat(result3,equalTo(""));
    }

    @Test
    public void sequence(){
        Validated<String, Seq<Integer>> res = valid.sequence(Seq.of(invalid));
        assertTrue(res.isInvalid());

        String str = invalid.sequence(Seq.of(Validated.invalid("boo!"))).foldInvalidLeft(Monoids.stringConcat);
        assertThat(str,equalTo("failedboo!"));
    }

    @Test
    public void traverse(){
        String str = invalid.traverse(Seq.of(Validated.invalid("boo!")),i->i+1).foldInvalidLeft(Monoids.stringConcat);
        assertThat(str,equalTo("failedboo!"));
        Validated<String, Seq<Integer>> res = valid.traverse(Seq.of(Validated.valid(10)),i->i+1);
        assertThat(res,equalTo(Validated.valid(Seq.of(11,11))));
    }

    @Test
    public void orElseUse(){
        assertThat(invalid.orElseUse(()->valid),equalTo(valid));
        assertThat(valid.orElseUse(()->Validated.valid(100)),equalTo(valid));
        assertThat(invalid.orElseUse(()->invalid),equalTo(invalid));
    }
    @Test
    public void orElseUseAccumulating(){
        assertThat(invalid.orElseUseAccumulating(()->valid),equalTo(valid));
        assertThat(valid.orElseUseAccumulating(()->Validated.valid(100)),equalTo(valid));
        assertThat(invalid.orElseUseAccumulating(()->invalid), not(equalTo(invalid)));
        assertThat(invalid.orElseUseAccumulating(()->invalid), equalTo(invalid.sequence(Seq.of(invalid))));
    }

    @Test
    public void async(){
        Future<Integer> f = Future.future();

        Validated<Throwable,Integer> v = Validated.fromPublisher(f);
        Validated<Throwable, Integer> v2 = v.map(i -> i + 10);

        new Thread(()-> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            f.complete(100);
        }).start();

        assertThat(v2.orElse(-1),equalTo(110));
    }

    @Test
    public void visit(){
        assertThat(valid.visit(i->i,()->-1),equalTo(10));
        assertThat(async.visit(i->i,()->-1),equalTo(10));
        assertThat(invalid.visit(i->i,()->-1),equalTo(-1));
    }

}
