package cyclops.typeclasses;

import cyclops.async.Future;
import cyclops.collectionx.mutable.ListX;
import cyclops.companion.Monoids;
import cyclops.companion.Optionals;
import cyclops.companion.Optionals.OptionalKind;
import cyclops.control.anym.Witness.future;
import cyclops.control.anym.Witness.list;
import cyclops.control.anym.Witness.optional;
import cyclops.control.anym.Witness.reactiveSeq;
import cyclops.reactive.ReactiveSeq;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

/**
 * Created by johnmcclean on 28/06/2017.
 */
public class NestedTest {
    Nested<list,optional,Integer> just = Nested.of(ListX.of(OptionalKind.of(2)),ListX.Instances.definitions(),Optionals.Instances.definitions());
    Nested<list,optional,Integer> doubled = Nested.of(ListX.of(OptionalKind.of(4)),ListX.Instances.definitions(),Optionals.Instances.definitions());


    Nested<future,optional,Integer> futureOptional = Nested.of(Future.ofResult(OptionalKind.of(4)),Future.Instances.definitions(),Optionals.Instances.definitions());

    Nested<future,reactiveSeq,Integer> futureReactiveSeq = Nested.of(Future.ofResult(ReactiveSeq.of(4)),Future.Instances.definitions(),ReactiveSeq.Instances.definitions());


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
        assertThat(just.sequence().toString(), equalTo(Nested.of(OptionalKind.of(ListX.of(2)),Optionals.Instances.definitions(),ListX.Instances.definitions()).toString()));
    }

    @Test
    public void traverse() {
        assertThat(just.traverse(i->i*2).toString(), equalTo(Nested.of(OptionalKind.of(ListX.of(4)),Optionals.Instances.definitions(),ListX.Instances.definitions()).toString()));
    }


}