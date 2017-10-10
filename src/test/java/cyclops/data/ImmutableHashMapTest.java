package cyclops.data;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;


public class ImmutableHashMapTest {

    @Test
    public void testEmpty(){
        assertThat(HashMap.empty().size(),equalTo(0));
    }
    @Test
    public void test(){
        HashMap<Integer,Integer> map = HashMap.empty();

        assertThat(map.put(10,10).size(),equalTo(1));

    }


    @Test
    public void add3Entries(){
        HashMap<Integer,Integer> map = HashMap.empty();
        for(int i=0;i<3;i++){
            map = map.put(i,i*2);
        }
        assertThat(map.size(),equalTo(3));
    }
    @Test
    public void add5Entries(){
        HashMap<Integer,Integer> map = HashMap.empty();
        for(int i=0;i<5;i++){
            map = map.put(i,i*2);
        }
        assertThat(map.size(),equalTo(5));
    }
    @Test
    public void add10Entries(){
        HashMap<Integer,Integer> map = HashMap.empty();
        for(int i=0;i<10;i++){
            map = map.put(i,i*2);
        }
        assertThat(map.size(),equalTo(10));
    }
    @Test
    public void add34Entries(){
        HashMap<Integer,Integer> map = HashMap.empty();
        for(int i=0;i<34;i++){
            map = map.put(i,i*2);
        }
        assertThat(map.size(),equalTo(34));
    }
    @Test
    public void add80Entries(){
        HashMap<Integer,Integer> map = HashMap.empty();
        for(int i=0;i<80;i++){
            map = map.put(i,i*2);
        }
        assertThat(map.size(),equalTo(80));
    }
    @Test
    public void add500Entries(){
        HashMap<Integer,Integer> map = HashMap.empty();
        for(int i=0;i<500;i++){
            map = map.put(i,i*2);
        }
        assertThat(map.size(),equalTo(500));
    }
    @Test
    public void add50000Entries(){
        HashMap<Integer,Integer> map = HashMap.empty();
        for(int i=0;i<50000;i++){
            map = map.put(i,i*2);
        }
        assertThat(map.size(),equalTo(50000));
    }

}