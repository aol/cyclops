package cyclops.typeclasses;

import cyclops.companion.Streams;
import cyclops.companion.Streams.StreamKind;
import cyclops.control.lazy.Maybe;
import cyclops.monads.Witness.maybe;
import cyclops.monads.Witness.stream;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;


public class CoproductTest {

    Coproduct<stream,maybe,Integer> just = Coproduct.just(10, Streams.Instances.definitions());

    @Test
    public void map(){
        assertThat(just.map(i->i*2),equalTo(Coproduct.just(20,Streams.Instances.definitions())));
    }
    @Test
    public void filter(){
        assertThat(just.filter(i->i<10),equalTo(Coproduct.none(Streams.Instances.definitions())));
    }
    @Test
    public void filterTrue(){
        assertThat(just.filter(i->i<11),equalTo(just));
    }

    @Test
    public void visit(){
        assertThat(just.visit(s-> StreamKind.narrowK(s).count(), m-> Maybe.narrowK(m).toOptional().get()),equalTo(10));
    }
}