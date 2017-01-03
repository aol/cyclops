package com.aol.cyclops2.functions.collections.extensions.persistent;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.jooq.lambda.tuple.Tuple;
import org.junit.Test;

import cyclops.collections.immutable.PBagX;
import cyclops.collections.immutable.PMapX;
import cyclops.collections.immutable.PMapXs;
import cyclops.collections.immutable.POrderedSetX;
import cyclops.collections.immutable.PQueueX;
import cyclops.collections.immutable.PSetX;
import cyclops.collections.immutable.PStackX;
import cyclops.collections.immutable.PVectorX;
import cyclops.collections.ListX;
import cyclops.collections.MapXs;
public class PMapXsTest {
    @Test
    public void toPStackX(){
        PMapX<String,Integer> maps = PMapXs.of("a",1,"b",2);
        PStackX<String> strs = maps.toPStackX(t->""+t.v1+t.v2);
        assertThat(strs.reverse(),equalTo(ListX.of("a1","b2")));
    }
    @Test
    public void toPSetX(){
        PMapX<String,Integer> maps = PMapXs.of("a",1,"b",2);
        PSetX<String> strs = maps.toPSetX(t->""+t.v1+t.v2);
        assertThat(strs,equalTo(PSetX.of("a1","b2")));
    }
    @Test
    public void toPOrderedSetX(){
        PMapX<String,Integer> maps = PMapXs.of("a",1,"b",2);
        POrderedSetX<String> strs = maps.toPOrderedSetX(t->""+t.v1+t.v2);
        assertThat(strs,equalTo(POrderedSetX.of("a1","b2")));
    }
    @Test
    public void toPBagX(){
        PMapX<String,Integer> maps = PMapXs.of("a",1,"b",2);
        PBagX<String> strs = maps.toPBagX(t->""+t.v1+t.v2);
        assertThat(strs,equalTo(PBagX.of("a1","b2")));
    }
    @Test
    public void toPQueueX(){
        PMapX<String,Integer> maps = PMapXs.of("a",1,"b",2);
        PQueueX<String> strs = maps.toPQueueX(t->""+t.v1+t.v2);
        assertThat(strs.toList(),equalTo(PQueueX.of("a1","b2").toList()));
    }
    @Test
    public void toPVectorX(){
        PMapX<String,Integer> maps = PMapXs.of("a",1,"b",2);
        PVectorX<String> strs = maps.toPVectorX(t->""+t.v1+t.v2);
        assertThat(strs,equalTo(ListX.of("a1","b2")));
    }
    
    @Test
    public void onEmpty(){
        assertThat(PMapX.empty().onEmpty(Tuple.tuple("hello",10)).get("hello"),equalTo(10));
    }
    @Test
    public void onEmptyGet(){
        assertThat(PMapX.empty().onEmptyGet(()->Tuple.tuple("hello",10)).get("hello"),equalTo(10));
    }
    @Test(expected=RuntimeException.class)
    public void onEmptyThrow(){
       PMapX.empty().onEmptyThrow(()->new RuntimeException("hello"));
    }
    @Test
    public void onEmptySwitch(){
       
        assertThat(PMapX.<String,Integer>empty().onEmptySwitch(()->PMapX.fromMap(MapXs.of("hello",10))).get("hello"),equalTo(10));
    }
    @Test
    public void testOf() {
        assertThat(PMapXs.of(),equalTo(new HashMap()));
    }

    @Test
    public void testOfKV() {
        Map<String,Integer> map = new HashMap<>();
        map.put("key",1);
        assertThat(PMapXs.of("key",1),equalTo(map));
    }

    @Test
    public void testOfKVKV() {
        Map<String,Integer> map = new HashMap<>();
        map.put("1",1);
        map.put("2",2);
        assertThat(PMapXs.of("1",1,"2",2),equalTo(map));
    }

    @Test
    public void testOfKVKVKV() {
        Map<String,Integer> map = new HashMap<>();
        map.put("1",1);
        map.put("2",2);
        map.put("3",3);
        assertThat(PMapXs.of("1",1,"2",2,"3",3),equalTo(map));
    }

    @Test
    public void testOfKVKVKVKV() {
        Map<String,Integer> map = new HashMap<>();
        map.put("1",1);
        map.put("2",2);
        map.put("3",3);
        map.put("4",4);
        assertThat(PMapXs.of("1",1,"2",2,"3",3,"4",4),equalTo(map));
    }

    @Test
    public void testFrom() {
        Map<String,Integer> map = new HashMap<>();
        map.put("1",1);
        map.put("2",2);
        map.put("3",3);
        map.put("4",4);
        assertThat(PMapXs.from(map).build(),equalTo(map));
    }

    @Test
    public void testMapKV() {
        Map<String,Integer> map = new HashMap<>();
        map.put("1",1);
        map.put("2",2);
        assertThat(PMapXs.map("1",1).put("2", 2).build(),equalTo(map));
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
        assertThat(PMapXs.map("1",1).putAll(map2).build(),equalTo(map));
    }

    @Test
    public void testMapKVKV() {
        Map<String,Integer> map = new HashMap<>();
        map.put("1",1);
        map.put("2",2);
        map.put("3",3);
        map.put("4",4);
        assertThat(PMapXs.map("1",1,"2", 2).put("3", 3,"4",4).build(),equalTo(map));
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
        assertThat(PMapXs.map("1",1,"2", 2,"3", 3).put("4",4,"5",5,"6",6).build(),equalTo(map));
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
        assertThat(PMapXs.map("1",1,"2", 2,"3", 3,"4",4).put("5",5,"6",6,"7",7,"8",8).build(),equalTo(map));
    }

}
