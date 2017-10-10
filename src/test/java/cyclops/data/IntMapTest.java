package cyclops.data;



import cyclops.control.Option;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


public class IntMapTest {

    @Test
    public void appendPrependGet(){
        assertThat(IntMap.of(1,2,3).plus(4).get(3),equalTo(Option.some(4)));

    }
    @Test
    public void testSize() {
        assertThat(IntMap.of(1,2,3).size(),equalTo(3));
        assertThat(IntMap.of(1,2,3).plus(1).size(),equalTo(4));
    }
    @Test
    public void testCalcSize() {
        assertThat(IntMap.of(1,2,3).calcSize(),equalTo(3));
        assertThat(IntMap.of(1,2,3).plus(1).calcSize(),equalTo(4));
    }
/**
    @Test
    public void add10000AL(){
        //2717
        long start = System.currentTimeMillis();
        ArrayList<Integer> v = new ArrayList(1);
        v.add(1);
        for(int i=0;i<100_000_00;i++){
            v.add(i);
        }
        System.out.println(System.currentTimeMillis()-start);
        System.out.println(v.size());
    }
 **/

    @Test
    public void add10000(){
        //11040
        long start = System.currentTimeMillis();
        IntMap<Integer> v = IntMap.of(1);
        for(int i=0;i<100_000_00;i++){
            v =v.plus(i);
        }
        System.out.println(System.currentTimeMillis()-start);
        System.out.println(v.size());
    }
    /**
    @Test
    public void read10000(){
        //2197


        IntMap<Integer> v = IntMap.of(1);
        for(int i=0;i<100_000_00;i++){
            v =v.plus(i);
        }
        ArrayList<Integer> al = new ArrayList(v.size());
        long start = System.currentTimeMillis();

        for(int i=0;i<100_000_00;i++){
            al.add(v.getOrElse(i,-1));
        }
        System.out.println(System.currentTimeMillis()-start);
        System.out.println(al.size());
    }
    @Test
    public void read10000PCol(){
        //1032


        PVector<Integer> v = TreePVector.singleton(1);
        for(int i=0;i<100_000_00;i++){
            v =v.plus(i);
        }
        long start = System.currentTimeMillis();
        ArrayList<Integer> al = new ArrayList(v.size());
        for(int i=0;i<100_000_00;i++){
            al.add(v.get(i));
        }
        System.out.println(System.currentTimeMillis()-start);
        System.out.println(al.size());
    }
    @Test
    public void add10000PCol(){
        //13550
        long start = System.currentTimeMillis();
        PVector<Integer> v = TreePVector.singleton(1);
        for(int i=0;i<100_000_00;i++){
            v =v.plus(i);
        }
        System.out.println(System.currentTimeMillis()-start);
        System.out.println(v.size());
    }
**/

}