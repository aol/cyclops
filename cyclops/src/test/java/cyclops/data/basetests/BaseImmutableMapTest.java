package cyclops.data.basetests;


import cyclops.data.Seq;
import cyclops.data.tuple.Tuple;
import cyclops.data.*;
import cyclops.control.Option;
import cyclops.data.ImmutableMap;
import cyclops.reactive.ReactiveSeq;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public abstract  class BaseImmutableMapTest {

    protected abstract <K,V> ImmutableMap<K,V> empty();
    protected abstract  <K,V> ImmutableMap<K,V> of(K k1,V v1);
    protected abstract <K,V> ImmutableMap<K,V> of(K k1,V v1,K k2, V v2);
    protected abstract ImmutableMap<String,Integer> fromMap(Map<String, Integer> hello);

  @Test
  public void removeMissingKey(){
    assertThat(of(1,"a",2,"b").remove(0),equalTo(of(1,"a",2,"b")));
    assertThat(of(1,"a",2,"b").removeAll(0),equalTo(of(1,"a",2,"b")));
    assertThat(of(1,"a",2,"b").remove(5),equalTo(of(1,"a",2,"b")));
    assertThat(of(1,"a",2,"b").removeAll(5),equalTo(of(1,"a",2,"b")));
  }
    @Test
    public void addRemove(){
      for(int i=0;i<100_00;i++) {
        ImmutableMap<ReactiveSeq<Integer>,Integer> map = empty();
        ReactiveSeq<Integer> s1 = ReactiveSeq.of(1);
        ReactiveSeq<Integer> s2 = ReactiveSeq.of(10);

        map = map.put(s1,2);
        map = map.put(s2,20);

        assertThat(map.size(), is(2));
        map = map.remove(s1);
        map = map.remove(s2);


        assertThat(map.size(), is(0));


      }
    }
    @Test
    public void emptyRemoveAbsent(){
      assertThat(empty().remove(10),equalTo(empty()));
    }
  @Test
  public void singleRemoveAbset(){
    assertThat(of(1,2).remove(10),equalTo(of(1,2)));
  }

    @Test
    public void toStringTest(){
        assertThat(of(1,3).toString(),equalTo("[{1=3}]"));
    }
    @Test
    public void toSeq(){
        ImmutableMap<String,Integer> maps = of("a",1,"b",2);
        Seq<String> strs = maps.toSeq(t->""+t._1()+t._2());
        assertThat(strs,equalTo(Seq.of("a1","b2")));
    }

    @Test
    public void onEmpty(){
        assertThat(empty().onEmpty(Tuple.tuple("hello",10)).get("hello"),equalTo(Option.some(10)));
    }
    @Test
    public void onEmptyGet(){
        assertThat(empty().onEmptyGet(()->Tuple.tuple("hello",10)).get("hello"),equalTo(Option.some(10)));
    }
    @Test
    public void onEmptyThrow(){

        assertTrue(empty().onEmptyTry(()->new RuntimeException("hello")).isFailure());
        assertFalse(of(1,2).onEmptyTry(()->new RuntimeException("hello")).isFailure());
    }
    @Test
    public void onEmptySwitch(){

        Map<String,Integer> m = new HashMap<>();
        m.put("hello",10);
        assertThat(this.<String,Integer>empty().onEmptySwitch(()-> fromMap(m)).get("hello"),equalTo(Option.some(10)));
    }



    @Test
    public void testOf() {
        assertThat(this.empty().javaMap(),equalTo(new java.util.HashMap()));
    }

    @Test
    public void testOfKV() {
        Map<String,Integer> map = new java.util.HashMap<>();
        map.put("key",1);
        assertThat(of("key",1).javaMap(),equalTo(map));
    }

    @Test
    public void testOfKVKV() {
        Map<String,Integer> map = new java.util.HashMap<>();
        map.put("1",1);
        map.put("2",2);
        assertThat(of("1",1,"2",2).javaMap(),equalTo(map));
    }



    @Test
    public void testMapKV() {
        Map<String,Integer> map = new java.util.HashMap<>();
        map.put("1",1);
        map.put("2",2);
        assertThat(of("1",1).put("2", 2).javaMap(),equalTo(map));
    }
    @Test
    public void testMapKVPutAll() {
        Map<String,Integer> map = new java.util.HashMap<>();
        map.put("1",1);
        map.put("2",2);
        map.put("3",3);
        map.put("4",4);
        map.put("5",5);
        map.put("6",6);
        Map<String,Integer> map2 = new java.util.HashMap<>();
        map2.put("1",1);
        map2.put("2",2);
        map2.put("3",3);
        map2.put("4",4);
        map2.put("5",5);
        map2.put("6",6);
        assertThat(of("1",1).putAll(cyclops.data.HashMap.fromMap(map2)).javaMap(),equalTo(map));
    }

    @Test
    public void testEmpty(){
        assertThat(empty().size(),equalTo(0));
    }
    @Test
    public void test(){
        ImmutableMap<Integer,Integer> map = empty();


        assertThat(map.put(10,10).size(),equalTo(1));

    }

    public <K,V> void putAndCompare(ImmutableMap<K,V> map){
        HashMap<K,V> jmap = new HashMap<>();

        jmap.putAll(map.javaMap());
        assertThat(map.javaMap(),equalTo(jmap));
    }


    @Test
    public void add3Entries(){
        ImmutableMap<Integer,Integer> map = empty();

        for(int i=0;i<3;i++){
            map = map.put(i,i*2);

        }
        assertThat(map.size(),equalTo(3));
        putAndCompare(map);
    }
    @Test
    public void add5Entries(){
        ImmutableMap<Integer,Integer> map = empty();
        for(int i=0;i<5;i++){
            map = map.put(i,i*2);
        }
        assertThat(map.size(),equalTo(5));
        putAndCompare(map);
    }
    @Test
    public void add10Entries(){
        ImmutableMap<Integer,Integer> map = empty();
        for(int i=0;i<10;i++){
            map = map.put(i,i*2);
        }
        assertThat(map.size(),equalTo(10));
        putAndCompare(map);
    }
    @Test
    public void add34Entries(){
        ImmutableMap<Integer,Integer> map = empty();
        for(int i=0;i<34;i++){
            map = map.put(i,i*2);
        }
        assertThat(map.size(),equalTo(34));
        putAndCompare(map);
    }
    @Test
    public void add80Entries(){
        ImmutableMap<Integer,Integer> map = empty();
        for(int i=0;i<80;i++){
            map = map.put(i,i*2);
        }
        assertThat(map.size(),equalTo(80));
        putAndCompare(map);
    }
    @Test
    public void streamTest(){
        ImmutableMap<Integer,Integer> map = empty();
        for(int i=0;i<500;i++){
            map = map.put(i,i*2);
        }
        assertThat(map.stream().toList().size(),equalTo(500));
    }
    @Test
    public void add500Entries(){
        ImmutableMap<Integer,Integer> map = empty();
        for(int i=0;i<500;i++){
            map = map.put(i,i*2);
        }

        assertThat(map.size(),equalTo(500));
        putAndCompare(map);

    }
    @Test
    public void add50000Entries(){
        ImmutableMap<Integer,Integer> map = empty();
        for(int i=0;i<50000;i++){
            map = map.put(i,i*2);
        }
        assertThat(map.size(),equalTo(50000));
        putAndCompare(map);
    }

    @Test
    public void map(){
        ImmutableMap<Integer,Integer> map = empty();
        for(int i=0;i<80;i++){
            map = map.put(i,i*2);
        }
        ImmutableMap<Integer,Integer> map2 =  map.bimap(k->k*2,v->v*10);
        assertThat(map2.stream().map(t->t._1()).sumInt(i->i),equalTo(map.stream().map(t->t._1()).sumInt(i->i)*2));
        assertThat(map2.stream().map(t->t._2()).sumInt(i->i),equalTo(map.stream().map(t->t._2()).sumInt(i->i)*10));

    }
    @Test
    public void flatMap(){
        ImmutableMap<Integer,Integer> map = empty();
        for(int i=0;i<80;i++){
            map = map.put(i,i*2);
        }
        ImmutableMap<Integer,Integer> map2 =  map.flatMap((k,v)-> of(k*2,v*10));
        assertThat(map2.stream().map(t->t._1()).sumInt(i->i),equalTo(map.stream().map(t->t._1()).sumInt(i->i)*2));
        assertThat(map2.stream().map(t->t._2()).sumInt(i->i),equalTo(map.stream().map(t->t._2()).sumInt(i->i)*10));

    }
  @Test
  public void viewTest(){
    Map<Integer,String> map = of(1,"hello",2,"world").mapView();
    Map<Integer,String> hashMap = of(1,"hello",2,"world").mapView();
    assertThat(map.size(),equalTo(2));
    assertThat(map,equalTo(hashMap));

    assertThat(map.put(1,"hello"),equalTo("hello"));

    assertThat(map.remove(1),equalTo("hello"));
    assertThat(map.remove((Object)1),equalTo("hello"));

  }
}
