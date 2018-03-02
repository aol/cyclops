package cyclops.typeclasses;

import com.oath.cyclops.data.ReactiveWitness;
import com.oath.cyclops.data.ReactiveWitness.list;
import cyclops.control.Future;
import cyclops.reactive.collections.mutable.ListX;
import cyclops.companion.Monoids;
import com.oath.cyclops.hkt.DataWitness.future;

import com.oath.cyclops.hkt.DataWitness.optional;
import com.oath.cyclops.hkt.DataWitness.reactiveSeq;
import cyclops.hkt.Nested;
import cyclops.instances.control.FutureInstances;
import cyclops.instances.jdk.OptionalInstances;
import cyclops.instances.reactive.PublisherInstances;
import cyclops.instances.reactive.collections.mutable.ListXInstances;
import cyclops.kinds.OptionalKind;
import cyclops.reactive.ReactiveSeq;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

/**
 * Created by johnmcclean on 28/06/2017.
 */
public class NestedTest {
    Nested<list,optional,Integer> just = Nested.of(ListX.of(OptionalKind.of(2)), ListXInstances.definitions(), OptionalInstances.definitions());
    Nested<list,optional,Integer> doubled = Nested.of(ListX.of(OptionalKind.of(4)), ListXInstances.definitions(),OptionalInstances.definitions());


    Nested<future,optional,Integer> futureOptional = Nested.of(Future.ofResult(OptionalKind.of(4)), FutureInstances.definitions(),OptionalInstances.definitions());

    Nested<future,reactiveSeq,Integer> futureReactiveSeq = Nested.of(Future.ofResult(ReactiveSeq.of(4)), FutureInstances.definitions(), PublisherInstances.definitions());


    @Test
    public void foldLeft()  {
        assertThat(just.foldLeft(Monoids.intSum).applyHKT(ListX::narrowK), equalTo(ListX.of(2)));
    }

    @Test
    public void map()  {
        assertThat(just.map(i->i*2).toString(), equalTo(doubled.toString()));
    }

    int c=0;
    @Test
    public void peek() throws Exception {
        just.peek(i->c=i).toString();
        assertThat(c, equalTo(2));
    }



    @Test
    public void flatMap() throws Exception {
        assertThat(just.flatMap(i-> OptionalKind.of(i*2)).toString(), equalTo(doubled.toString()));
    }

    @Test
    public void sequence()  {
        assertThat(just.sequence().toString(), equalTo(Nested.of(OptionalKind.of(ListX.of(2)),OptionalInstances.definitions(), ListXInstances.definitions()).toString()));
    }

    @Test
    public void traverse() {
        assertThat(just.traverse(i->i*2).toString(), equalTo(Nested.of(OptionalKind.of(ListX.of(4)),OptionalInstances.definitions(), ListXInstances.definitions()).toString()));
    }


}
