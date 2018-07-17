package cyclops.data;

import cyclops.companion.Comparators;
import cyclops.data.base.RedBlackTree;
import org.junit.Test;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RBTTest {

    <T> RedBlackTree.Tree<T,T> empty() {
        return RedBlackTree.empty(Comparators.naturalOrderIdentityComparator());
    }

    <T> RedBlackTree.Tree<T,T> of(T... values) {
        RedBlackTree.Tree<T, T> x = empty();
        for(T next : values){
            x = RedBlackTree.rootIsBlack(x.plus(next,next));
        }
        return x;
    }

    @Test
    public void emptyProperties(){
        assertTrue(empty().isBlack());
        assertTrue(empty().isEmpty());
        assertTrue(empty().size()==0);
        assertThat(empty().getOrElse(1,-1),equalTo(-1));
    }
    @Test
    public void oneProperties(){
        assertTrue(of(1).isBlack());
        assertFalse(of(1).isEmpty());
        assertTrue(of(1).size()==1);
        assertThat(of(1).getOrElse(1,-1),equalTo(1));
    }


    @Test
    public void balanceCheckNew() {

        RedBlackTree.Tree<Integer,Integer> tree = empty();
        assertThat(tree.tree(),equalTo("{LEAF}"));
        assertThat(tree.size(),equalTo(0));

        tree = RedBlackTree.rootIsBlack(tree.plus(100,100));
        assertThat(tree.tree(),equalTo("{BLACK:100}"));
        assertThat(tree.size(),equalTo(1));

        tree = RedBlackTree.rootIsBlack(tree.plus(5,5));
        assertThat(tree.tree(),equalTo("{BLACK:100 {RED:5}}"));
        assertThat(tree.size(),equalTo(2));

        tree = RedBlackTree.rootIsBlack(tree.plus(-5,-5));
        assertThat(tree.tree(),equalTo("{BLACK:5 {BLACK:-5} {BLACK:100}}"));
        assertThat(tree.size(),equalTo(3));

        tree = RedBlackTree.rootIsBlack(tree.plus(7,7));
        assertThat(tree.tree(),equalTo("{BLACK:5 {BLACK:-5} {BLACK:100 {RED:7}}}"));
        assertThat(tree.size(),equalTo(4));

        tree = RedBlackTree.rootIsBlack(tree.plus(-7,-7));
        assertThat(tree.tree(),equalTo("{BLACK:5 {BLACK:-5 {RED:-7}} {BLACK:100 {RED:7}}}"));
        assertThat(tree.size(),equalTo(5));

        tree = RedBlackTree.rootIsBlack(tree.plus(100,100));
        assertThat(tree.tree(),equalTo("{BLACK:5 {BLACK:-5 {RED:-7}} {BLACK:100 {RED:7}}}"));
        assertThat(tree.size(),equalTo(5));

        tree = RedBlackTree.rootIsBlack(tree.plus(101,101));
        assertThat(tree.tree(),equalTo("{BLACK:5 {BLACK:-5 {RED:-7}} {BLACK:100 {RED:7} {RED:101}}}"));
        assertThat(tree.size(),equalTo(6));

        tree = RedBlackTree.rootIsBlack(tree.plus(102,102));
        assertThat(tree.tree(),equalTo("{BLACK:5 {BLACK:-5 {RED:-7}} {RED:101 {BLACK:100 {RED:7}} {BLACK:102}}}"));
        assertThat(tree.size(),equalTo(7));

        tree = RedBlackTree.rootIsBlack(tree.plus(-1,-1));
        assertThat(tree.tree(),equalTo("{BLACK:5 {BLACK:-5 {RED:-7} {RED:-1}} {RED:101 {BLACK:100 {RED:7}} {BLACK:102}}}"));
        assertThat(tree.size(),equalTo(8));

        tree = RedBlackTree.rootIsBlack(tree.plus(-2,-2));
        assertThat(tree.tree(),equalTo("{BLACK:5 {RED:-2 {BLACK:-5 {RED:-7}} {BLACK:-1}} {RED:101 {BLACK:100 {RED:7}} {BLACK:102}}}"));
        assertThat(tree.size(),equalTo(9));

        tree = RedBlackTree.rootIsBlack(tree.plus(-3,-3));
        assertThat(tree.tree(),equalTo("{BLACK:5 {RED:-2 {BLACK:-5 {RED:-7} {RED:-3}} {BLACK:-1}} {RED:101 {BLACK:100 {RED:7}} {BLACK:102}}}"));
        assertThat(tree.size(),equalTo(10));

        tree = RedBlackTree.rootIsBlack(tree.minus(-3));
        assertThat(tree.tree(),equalTo("{BLACK:5 {RED:-2 {BLACK:-5 {RED:-7}} {BLACK:-1}} {RED:101 {BLACK:100 {RED:7}} {BLACK:102}}}"));
        assertThat(tree.size(),equalTo(9));

        tree = RedBlackTree.rootIsBlack(tree.minus(-3));
        assertThat(tree.tree(),equalTo("{BLACK:5 {RED:-2 {BLACK:-5 {RED:-7}} {BLACK:-1}} {RED:101 {BLACK:100 {RED:7}} {BLACK:102}}}"));
        assertThat(tree.size(),equalTo(9));

        tree = RedBlackTree.rootIsBlack(tree.minus(102));
        assertThat(tree.tree(),equalTo("{BLACK:5 {RED:-2 {BLACK:-5 {RED:-7}} {BLACK:-1}} {RED:101 {BLACK:100 {RED:7}}}}"));
        assertThat(tree.size(),equalTo(8));

    }
    @Test
    public void balanceCheckTo6() {
        RedBlackTree.Tree<Integer,Integer> tree = empty();
        assertThat(tree.tree(),equalTo("{LEAF}"));
        assertThat(tree.size(),equalTo(0));

        tree = RedBlackTree.rootIsBlack(tree.plus(0,0));
        assertThat(tree.tree(),equalTo("{BLACK:0}"));
        assertThat(tree.size(),equalTo(1));

        tree = RedBlackTree.rootIsBlack(tree.plus(1,1));
        assertThat(tree.tree(),equalTo("{BLACK:0 {RED:1}}"));
        assertThat(tree.size(),equalTo(2));

        tree = RedBlackTree.rootIsBlack(tree.plus(2,2));
        assertThat(tree.tree(),equalTo("{BLACK:1 {BLACK:0} {BLACK:2}}"));
        assertThat(tree.size(),equalTo(3));

        tree = RedBlackTree.rootIsBlack(tree.plus(3,3));
        assertThat(tree.tree(),equalTo("{BLACK:1 {BLACK:0} {BLACK:2 {RED:3}}}"));
        assertThat(tree.size(),equalTo(4));

        tree = RedBlackTree.rootIsBlack(tree.plus(4,4));
        assertThat(tree.tree(),equalTo("{BLACK:1 {BLACK:0} {RED:3 {BLACK:2} {BLACK:4}}}"));
        assertThat(tree.size(),equalTo(5));

        tree = RedBlackTree.rootIsBlack(tree.plus(5,5));
        assertThat(tree.tree(),equalTo("{BLACK:1 {BLACK:0} {RED:3 {BLACK:2} {BLACK:4 {RED:5}}}}"));
        assertThat(tree.size(),equalTo(6));

        tree = RedBlackTree.rootIsBlack(tree.plus(6,6));
        assertThat(tree.tree(),equalTo("{BLACK:3 {BLACK:1 {BLACK:0} {BLACK:2}} {BLACK:5 {BLACK:4} {BLACK:6}}}"));
        assertThat(tree.size(),equalTo(7));
    }



}
