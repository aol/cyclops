package cyclops.typeclasses;

import cyclops.companion.Streams;
import cyclops.control.Maybe;
import com.oath.cyclops.hkt.DataWitness.option;
import com.oath.cyclops.hkt.DataWitness.stream;
import cyclops.instances.jdk.StreamInstances;
import cyclops.kinds.StreamKind;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;


public class CoproductTest {

    Coproduct<stream,option,Integer> just = Coproduct.just(10, StreamInstances.definitions());

    @Test
    public void map(){
        assertThat(just.map(i->i*2),equalTo(Coproduct.just(20, StreamInstances.definitions())));
    }
    @Test
    public void filter(){
        assertThat(just.filter(i->i<10),equalTo(Coproduct.none(StreamInstances.definitions())));
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
