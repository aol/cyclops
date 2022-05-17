package cyclops.data;

import com.oath.cyclops.types.persistent.PersistentMap;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import lombok.AllArgsConstructor;
import lombok.ToString;
import org.hamcrest.MatcherAssert;
import org.junit.Ignore;
import org.junit.Test;


import java.util.ArrayList;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class HashMapTest {

    @Test
    public void plusSize(){
        assertThat(TrieMap.empty().put("hello","world").size(),equalTo(1));
    }

    @Test
    public void stream(){

    }


    @Test @Ignore //long running test
    public void read100_000_00(){

        //6247
        HashMap<Integer,Integer> v = HashMap.empty();

        for(int i=0;i<100_000_00;i++){
            v =v.put(i,i);
        }
        ArrayList<Integer> al = new ArrayList(v.size());
        long start = System.currentTimeMillis();
        for(int i=0;i<100_000_00;i++){
            Integer next = v.getOrElse(i,null);
            assertNotNull(next);
            al.add(next);
        }

        System.out.println(System.currentTimeMillis()-start);
        assertThat(v.size(),equalTo(100_000_00));
        assertThat(al.size(),equalTo(100_000_00));
    }
    @Test @Ignore //long running test
    public void read100_000_00Stream(){

        //6247
        HashMap<Integer,Integer> v = HashMap.empty();

        for(int i=0;i<100_000_00;i++){
            v =v.put(i,i);
        }
        for(int i =0;i<100_000_00;i=i+2){
            v=v.remove(i);
        }
        for(int i=0;i<100_000_00;i++){
            v =v.put(i,i);
        }
        ArrayList<Integer> al = new ArrayList(v.size());
        long start = System.currentTimeMillis();
        for(Integer i : v.stream().map(Tuple2::_1)){
            Integer next = v.getOrElse(i,null);
            assertNotNull(next);
            al.add(next);
        }

        System.out.println(System.currentTimeMillis()-start);
        assertThat(v.size(),equalTo(100_000_00));
        assertThat(al.size(),equalTo(100_000_00));
    }
    @Test
    public void read100_000_00PC(){
        //2181

        PersistentMap<Integer,Integer> v = HashMap.empty();
        for(int i=0;i<100_000_00;i++){
            v =v.put(i,i);
        }
        ArrayList<Integer> al = new ArrayList(v.size());
        long start = System.currentTimeMillis();
        for(int i=0;i<100_000_00;i++){
            al.add(v.getOrElse(i,null));
        }

        System.out.println(System.currentTimeMillis()-start);
        System.out.println(v.size());
    }

    @Test
    public void add10000PCol(){
        //26792
        long start = System.currentTimeMillis();
        PersistentMap<Integer,Integer> v = HashMap.empty();
        for(int i=0;i<100_000_00;i++){
            v =v.put(i,i);
        }
        System.out.println(System.currentTimeMillis()-start);
        System.out.println(v.size());
    }

  @Test
  public void removeMissingKey(){
    MatcherAssert.assertThat(HashMap.of(1,"a",2,"b").removeAll(0),equalTo(HashMap.of(1,"a",2,"b")));
  }



}

