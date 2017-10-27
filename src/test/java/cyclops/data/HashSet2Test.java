package cyclops.data;

import cyclops.reactive.ReactiveSeq;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class HashSet2Test {
    @Test
    public void duplicates(){
        assertThat(HashSet.of(1,2,1,2,1,2).size(),equalTo(2));
    }
    @Test
    public void nines(){
   /**     HashSet<Integer> hs = HashSet.<Integer>empty().plus(9).plus(-10).plus(8)
                .plus(-9);
        System.out.println("-9?"+hs.containsValue(-9));
    **/

        System.out.println(HashSet.<Long>empty().plus(9l).plus(-10l)
                .plus(8l));
              //  .plus(-9l));
    }
    @Test
    public void reversedRangeLongWithReverse(){
        HashSet<Long> s = HashSet.rangeLong(10, -10);
     //   HashSet<Long> s = (HashSet)HashSet.rangeLong(10, -10).reverse();
       //System.out.println(HashSet.fromStream(ReactiveSeq.rangeLong(10,-10).reverse()));

        System.out.println("******************");
        System.out.println("******************");

        System.out.println(s.reverse());
   /**     System.out.println(((HashSet)s.reverse()).getMap());
        assertThat(HashSet.rangeLong(10, -10).reverse().count(),equalTo(20L));
         **/
    }
}

//[0,-1,1,-2,2,-3,3,-4,4,-5,5,-6,6,-7,7,-8,-9,8,9,-10,9,-10]
//[-1,0,-2,1,-3,2,-4,3,-5,4,-6,5,-7,6,-8,7,-9,8,-10,9]