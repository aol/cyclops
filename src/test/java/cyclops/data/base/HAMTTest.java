package cyclops.data.base;

import cyclops.control.Option;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class HAMTTest {
    Integer one = 1;
    Integer minusOne = -1;
    Integer two = 2;
    @Test
    public void empty() throws Exception {
        HAMT.Node<Integer, Integer> node = HAMT.<Integer, Integer>empty();
        assertThat(node.size(),equalTo(0));
    }
    @Test
    public void put1() throws Exception {
        HAMT.Node<Integer, Integer> node = HAMT.<Integer, Integer>empty().plus(0,one.hashCode(),one,one);
        assertThat(node.get(0,one.hashCode(),one),equalTo(Option.some(1)));
    }
    @Test
    public void putMany() throws Exception {
        HAMT.Node<Integer, Integer> node = HAMT.<Integer, Integer>empty();
        for(int i=0;i<1026;i++) {

            Integer next = i;
            node = node.plus(0, next.hashCode(), next, next);
            assertThat(node.toString(),node.get(0,next.hashCode(),next),equalTo(Option.some(next)));
            node = node.plus(0,minusOne.hashCode(),minusOne,minusOne);
        }
        System.out.println(node);
        node = node.plus(0,minusOne.hashCode(),minusOne,minusOne);
        assertTrue(node.get(0,minusOne.hashCode(),minusOne).isPresent());
        System.out.println(node);
    }
    @Test
    public void put31() throws Exception {
        HAMT.Node<Integer, Integer> node = HAMT.<Integer, Integer>empty();
        for(int i=0;i<32;i++) {

            Integer next = i;
            node = node.plus(0, next.hashCode(), next, next);
            assertThat(node.toString(),node.get(0,next.hashCode(),next),equalTo(Option.some(next)));
            if(i==10)
                node = node.plus(0,minusOne.hashCode(),minusOne,minusOne);
        }
        System.out.println(node);
        node = node.plus(0,minusOne.hashCode(),minusOne,minusOne);
        assertTrue(node.get(0,minusOne.hashCode(),minusOne).isPresent());
        System.out.println(node);
    }

    @Test
    public void problemBitsetNode(){
        HAMT.Node<Integer, Integer>[] nodes = new HAMT.Node[2];
        nodes[0] = new HAMT.ValueNode<>(-1,-1,-1);
        nodes[1] = new HAMT.ValueNode<>(31,31,31);
        new HAMT.BitsetNode<Integer,Integer>( new Long(Long.parseLong("10000000000000000000000000000001", 2)).intValue(),
                                            2,nodes);
    }

}