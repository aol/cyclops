package com.aol.cyclops2.functions.collections.extensions.persistent;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import cyclops.collections.immutable.*;
import cyclops.companion.PersistentMapXs;
import org.jooq.lambda.tuple.Tuple;
import org.junit.Test;

import cyclops.collections.immutable.OrderedSetX;
import cyclops.collections.mutable.ListX;
import cyclops.companion.MapXs;
public class PMapXsTest {
    @Test
    public void toStringTest(){
        assertThat(PersistentMapXs.of(1,3).toString(),equalTo("PersistentMapX[{1=3}]"));
    }
    @Test
    public void toPStackX(){
        PersistentMapX<String,Integer> maps = PersistentMapXs.of("a",1,"b",2);
        LinkedListX<String> strs = maps.toLinkedListX(t->""+t.v1+t.v2);
        assertThat(strs,equalTo(ListX.of("a1","b2")));
    }
    @Test
    public void toPSetX(){
        PersistentMapX<String,Integer> maps = PersistentMapXs.of("a",1,"b",2);
        PersistentSetX<String> strs = maps.toPersistentSetX(t->""+t.v1+t.v2);
        assertThat(strs,equalTo(PersistentSetX.of("a1","b2")));
    }
    @Test
    public void toPOrderedSetX(){
        PersistentMapX<String,Integer> maps = PersistentMapXs.of("a",1,"b",2);
        OrderedSetX<String> strs = maps.toOrderedSetX(t->""+t.v1+t.v2);
        assertThat(strs,equalTo(OrderedSetX.of("a1","b2")));
    }
    @Test
    public void toPBagX(){
        PersistentMapX<String,Integer> maps = PersistentMapXs.of("a",1,"b",2);
        BagX<String> strs = maps.toBagX(t->""+t.v1+t.v2);
        assertThat(strs,equalTo(BagX.of("a1","b2")));
    }
    @Test
    public void toPQueueX(){
        PersistentMapX<String,Integer> maps = PersistentMapXs.of("a",1,"b",2);
        PersistentQueueX<String> strs = maps.toPersistentQueueX(t->""+t.v1+t.v2);
        assertThat(strs.toList(),equalTo(PersistentQueueX.of("a1","b2").toList()));
    }
    @Test
    public void toPVectorX(){
        PersistentMapX<String,Integer> maps = PersistentMapXs.of("a",1,"b",2);
        VectorX<String> strs = maps.toVectorX(t->""+t.v1+t.v2);
        assertThat(strs,equalTo(ListX.of("a1","b2")));
    }
    
    @Test
    public void onEmpty(){
        assertThat(PersistentMapX.empty().onEmpty(Tuple.tuple("hello",10)).get("hello"),equalTo(10));
    }
    @Test
    public void onEmptyGet(){
        assertThat(PersistentMapX.empty().onEmptyGet(()->Tuple.tuple("hello",10)).get("hello"),equalTo(10));
    }
    @Test(expected=RuntimeException.class)
    public void onEmptyThrow(){
       PersistentMapX.empty().onEmptyThrow(()->new RuntimeException("hello"));
    }
    @Test
    public void onEmptySwitch(){
       
        assertThat(PersistentMapX.<String,Integer>empty().onEmptySwitch(()-> PersistentMapX.fromMap(MapXs.of("hello",10))).get("hello"),equalTo(10));
    }
    @Test
    public void testOf() {
        assertThat(PersistentMapXs.of(),equalTo(new HashMap()));
    }

    @Test
    public void testOfKV() {
        Map<String,Integer> map = new HashMap<>();
        map.put("key",1);
        assertThat(PersistentMapXs.of("key",1),equalTo(map));
    }

    @Test
    public void testOfKVKV() {
        Map<String,Integer> map = new HashMap<>();
        map.put("1",1);
        map.put("2",2);
        assertThat(PersistentMapXs.of("1",1,"2",2),equalTo(map));
    }

    @Test
    public void testOfKVKVKV() {
        Map<String,Integer> map = new HashMap<>();
        map.put("1",1);
        map.put("2",2);
        map.put("3",3);
        assertThat(PersistentMapXs.of("1",1,"2",2,"3",3),equalTo(map));
    }

    @Test
    public void testOfKVKVKVKV() {
        Map<String,Integer> map = new HashMap<>();
        map.put("1",1);
        map.put("2",2);
        map.put("3",3);
        map.put("4",4);
        assertThat(PersistentMapXs.of("1",1,"2",2,"3",3,"4",4),equalTo(map));
    }

    @Test
    public void testFrom() {
        Map<String,Integer> map = new HashMap<>();
        map.put("1",1);
        map.put("2",2);
        map.put("3",3);
        map.put("4",4);
        assertThat(PersistentMapXs.from(map).build(),equalTo(map));
    }

    @Test
    public void testMapKV() {
        Map<String,Integer> map = new HashMap<>();
        map.put("1",1);
        map.put("2",2);
        assertThat(PersistentMapXs.map("1",1).put("2", 2).build(),equalTo(map));
    }
    @Test
    public void testMapKVPutAll() {
        Map<String,Integer> map = new HashMap<>();
        map.put("1",1);
        map.put("2",2);
        map.put("3",3);
        map.put("4",4);
        map.put("5",5);
        map.put("6",6);
        Map<String,Integer> map2 = new HashMap<>();
        map2.put("1",1);
        map2.put("2",2);
        map2.put("3",3);
        map2.put("4",4);
        map2.put("5",5);
        map2.put("6",6);
        assertThat(PersistentMapXs.map("1",1).putAll(map2).build(),equalTo(map));
    }

    @Test
    public void testMapKVKV() {
        Map<String,Integer> map = new HashMap<>();
        map.put("1",1);
        map.put("2",2);
        map.put("3",3);
        map.put("4",4);
        assertThat(PersistentMapXs.map("1",1,"2", 2).put("3", 3,"4",4).build(),equalTo(map));
    }

    @Test
    public void testMapKVKVKV() {
        Map<String,Integer> map = new HashMap<>();
        map.put("1",1);
        map.put("2",2);
        map.put("3",3);
        map.put("4",4);
        map.put("5",5);
        map.put("6",6);
        assertThat(PersistentMapXs.map("1",1,"2", 2,"3", 3).put("4",4,"5",5,"6",6).build(),equalTo(map));
    }

    @Test
    public void testMapKVKVKVKV() {
        Map<String,Integer> map = new HashMap<>();
        map.put("1",1);
        map.put("2",2);
        map.put("3",3);
        map.put("4",4);
        map.put("5",5);
        map.put("6",6);
        map.put("7",7);
        map.put("8",8);
        assertThat(PersistentMapXs.map("1",1,"2", 2,"3", 3,"4",4).put("5",5,"6",6,"7",7,"8",8).build(),equalTo(map));
    }

}
