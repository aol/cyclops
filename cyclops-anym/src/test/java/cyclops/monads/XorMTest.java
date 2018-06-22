package cyclops.monads;

import cyclops.monads.Witness.maybe;
import cyclops.monads.Witness.stream;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;


public class XorMTest {

    XorM<stream,maybe,Integer> just = XorM.just(10);

    @Test
    public void map(){
        assertThat(just.map(i->i*2),equalTo(XorM.just(20)));
    }
    @Test
    public void filter(){
        assertThat(just.filter(i->i<10),equalTo(XorM.none()));
    }
    @Test
    public void filterTrue(){
        assertThat(just.filter(i->i<11),equalTo(just));
    }

    @Test
    public void visit(){
        assertThat(just.fold(s->Witness.stream(s).count(), m->Witness.maybe(m).toOptional().get()),equalTo(10));
    }
}
