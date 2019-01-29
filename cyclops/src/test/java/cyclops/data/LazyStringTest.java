package cyclops.data;

import cyclops.control.Option;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

public class LazyStringTest {
    LazyString str = LazyString.of("hello world");
    LazyString strLines = LazyString.of("hello world\nboo hoo");
    LazyString caps = LazyString.of("HELLO WORLD");
    LazyString longStr = LazyString.of("the quick brown fox jumps over the dog");
    LazyString empty = LazyString.empty();

    LazySeq<Character> helloWorld = LazySeq.of('h','e','l','l','o',' ','w','o','r','l','d');


    @Test
    public void fromLazySeq() {
        assertThat(empty,equalTo(LazyString.fromLazySeq(LazySeq.empty())));
        assertThat(str,equalTo(LazyString.fromLazySeq(helloWorld)));
    }

    @Test
    public void fromIterable() {
        assertThat(empty,equalTo(LazyString.fromIterable(LazySeq.empty())));
        assertThat(str,equalTo(LazyString.fromIterable(helloWorld)));
    }

    @Test
    public void of() {
        assertThat(empty,equalTo(LazyString.of("")));
        assertThat(str,equalTo(LazyString.of("hello world")));
    }

    @Test
    public void collector() {
        LazyString hw = Stream.of('h','e','l','l','o', ' ', 'w','o','r','l','d').collect(LazyString.collector());
        LazyString empty2 = Stream.<Character>empty().collect(LazyString.collector());

        assertThat(str,equalTo(hw));
        assertThat(empty2,equalTo(empty));

    }

    @Test
    public void unitIterable() {
        assertThat(empty.unitIterable(str),equalTo(str));
        assertThat(empty.unitIterable(str.lazySeq()),equalTo(str));
    }
    @Test
    public void unitStream() {
        assertThat(empty.unitStream(str.stream()),equalTo(str));

    }

    @Test
    public void empty() {
        assertThat(LazyString.empty(),equalTo(empty));
    }

    @Test
    public void op() {
        assertThat(str.op(s->s.reverse()),equalTo(str.reverse()));
    }

    @Test
    public void substring() {
        assertThat(str.substring(6).toString(),equalTo("world"));
        assertThat(empty.substring(6).toString(),equalTo(""));
    }


    @Test
    public void substring1() {
        assertThat(str.substring(0,5).toString(),equalTo("hello"));
        assertThat(empty.substring(0,5).toString(),equalTo(""));
    }

    @Test
    public void toUpperCase() {
        assertThat(str.toUpperCase().toString(),equalTo("HELLO WORLD"));
        assertThat(empty.toUpperCase().toString(),equalTo(""));
    }

    @Test
    public void toLowerCase() {
        assertThat(caps.toLowerCase().toString(),equalTo("hello world"));
        assertThat(empty.toLowerCase().toString(),equalTo(""));
    }

    @Test
    public void words() {
        assertThat(str.words().map(ls->ls.toString()),equalTo(LazySeq.of("hello", "world")));
        assertThat(empty.words().map(ls->ls.toString()),equalTo(LazySeq.empty()));
    }

    @Test
    public void lines() {
        assertThat(str.lines().map(ls->ls.toString()),equalTo(LazySeq.of("hello world")));
        assertThat(strLines.lines().map(ls->ls.toString()),equalTo(LazySeq.of("hello world","boo hoo")));
        assertThat(empty.lines().map(ls->ls.toString()),equalTo(LazySeq.empty()));
    }

    @Test
    public void mapChar() {
        assertThat(str.mapChar(c->new Character((char)(c+1))),equalTo(LazyString.of("ifmmp!xpsme")));
        assertThat(empty.mapChar(c->new Character((char)(c+1))),equalTo(LazyString.of("")));
    }

    @Test
    public void flatMapChar() {
        assertThat(str.flatMapChar(c->LazyString.of(""+new Character((char)(c+1)))),equalTo(LazyString.of("ifmmp!xpsme")));
        assertThat(empty.flatMapChar(c->LazyString.of(""+new Character((char)(c+1)))),equalTo(LazyString.of("")));
    }

    @Test
    public void filter() {
       assertThat(str.filter(i->!Objects.equals(i,'h')).toString(),equalTo("ello world"));
       assertThat(empty.filter(i->!Objects.equals(i,'h')).toString(),equalTo(""));
    }

    @Test
    public void map() {
        assertThat(str.map(i -> i + "1").vector().join(),equalTo("h1e1l1l1o1 1w1o1r1l1d1"));
        assertThat(empty.map(i->i+"1").toString(),equalTo("[]"));
    }

    @Test
    public void flatMap() {
        assertThat(str.flatMap(i -> LazySeq.of(i + "1")).vector().join(),equalTo("h1e1l1l1o1 1w1o1r1l1d1"));
        assertThat(empty.flatMap(i-> LazySeq.of(i + "1")).toString(),equalTo("[]"));
    }

    @Test
    public void concatMap() {
        assertThat(str.concatMap(i -> LazySeq.of(i + "1")).vector().join(),equalTo("h1e1l1l1o1 1w1o1r1l1d1"));
        assertThat(empty.concatMap(i-> LazySeq.of(i + "1")).toString(),equalTo("[]"));
    }

    @Test
    public void mergeMap() {

        System.out.println(Vector.of("h1","e1","l1","l1","o1"," 1","w1","o1","r1","l1","d1"));
        assertThat(str.mergeMap(i -> LazySeq.of(i + "1")).vector(),hasItems("h1","e1","l1","l1","o1"," 1","w1","o1","r1","l1","d1"));
        assertThat(empty.mergeMap(i-> LazySeq.of(i + "1")).toString(),equalTo("[]"));
    }

    @Test
    public void mergeMap1() {
        assertThat(str.mergeMap(10,i -> LazySeq.of(i + "1")).vector(),hasItems("h1","e1","l1","l1","o1"," 1","w1","o1","r1","l1","d1"));
        assertThat(empty.mergeMap(10,i-> LazySeq.of(i + "1")).toString(),equalTo("[]"));
    }

    @Test
    public void fold() {
        assertThat(str.fold(s->"10",n->"20"),equalTo("10"));
        assertThat(empty.fold(s->"10",n->"20"),equalTo("20"));
    }

    @Test
    public void onEmpty() {
        assertThat(str.onEmpty('a'),equalTo(str));
        assertThat(empty.onEmpty('a'),equalTo(LazyString.of("a")));
    }

    @Test
    public void onEmptyGet() {
        assertThat(str.onEmptyGet(()->'a'),equalTo(str));
        assertThat(empty.onEmptyGet(()->'a'),equalTo(LazyString.of("a")));
    }

    @Test
    public void onEmptySwitch() {
        assertThat(str.onEmptySwitch(()->empty),equalTo(str));
        assertThat(empty.onEmptySwitch(()->str),equalTo(str));
    }

    @Test
    public void stream() {
        assertThat(str.stream().join(),equalTo("hello world"));
        assertThat(empty.stream().join(),equalTo(""));
    }

    @Test
    public void take() {

        assertThat(str.take(0).toString(), equalTo(""));
        assertThat(str.take(-1).toString(), equalTo(""));
        assertThat(str.take(1).toString(), equalTo("h"));
        assertThat(str.take(2).toString(), equalTo("he"));
        assertThat(str.take(2000).toString(), equalTo("hello world"));

        assertThat(empty.take(0).toString(), equalTo(""));
        assertThat(empty.take(-1).toString(), equalTo(""));
        assertThat(empty.take(1).toString(), equalTo(""));
        assertThat(empty.take(2).toString(), equalTo(""));
        assertThat(empty.take(2000).toString(), equalTo(""));

    }


    @Test
    public void emptyUnit() {
        assertThat(str.emptyUnit(),equalTo(empty));
    }

    @Test
    public void replaceFirst() {
        assertThat(str.replaceFirst('l','b').toString(),equalTo("heblo world"));
        assertThat(empty.replaceFirst('l','b').toString(),equalTo(""));
    }

    @Test
    public void removeFirst() {
        assertThat(str.removeFirst(i->i=='l').toString(),equalTo("helo world"));
        assertThat(empty.removeFirst(i->i=='l').toString(),equalTo(""));
    }

    @Test
    public void subList() {
        assertThat(str.subList(0,str.length()),equalTo(str));
        assertThat(str.subList(-1100,Integer.MAX_VALUE),equalTo(str));
        assertThat(str.subList(0,5).toString(),equalTo("hello"));
        assertThat(str.subList(6,11).toString(),equalTo("world"));

        assertThat(empty.subList(0,str.length()),equalTo(empty));
        assertThat(empty.subList(-1100,Integer.MAX_VALUE),equalTo(empty));
        assertThat(empty.subList(0,5).toString(),equalTo(""));
        assertThat(empty.subList(6,11).toString(),equalTo(""));
    }

    @Test
    public void filterNot() {
        assertThat(str.filterNot(i->i=='l').toString(),equalTo("heo word"));
    }

    @Test
    public void notNull() {

        assertThat(LazyString.fromIterable(Vector.of('h',null,'e')).notNull().toString(),equalTo("he"));
        assertThat(LazyString.fromLazySeq(LazySeq.of('h',null,'e')).notNull().toString(),equalTo("he"));

    }

    boolean called;
    @Test
    public void peek() {
        called = false;
        LazyString s = str.take(1).peek(i->{
            called = true;
            assertThat(i,equalTo('h'));

        });
        assertFalse(called);
        s.join();
        assertTrue(called);
    }

    @Test
    public void tailOrElse() {
        assertThat(str.tailOrElse(LazySeq.of('h')),equalTo(LazyString.of("ello world")));
        assertThat(empty.tailOrElse(LazySeq.of('h')),equalTo(LazyString.of("h")));
    }

    @Test
    public void removeStream() {

        assertThat(str.removeStream(Stream.of('a','b','c')),equalTo(str));
        assertThat(str.removeStream(Stream.of('a','b','c','h')).toString(),equalTo("ello world"));
        assertThat(str.removeStream(Stream.of('h','e','l','h')).toString(),equalTo("o word"));

        assertThat(empty.removeStream(Stream.of('a','b','c')),equalTo(empty));
        assertThat(empty.removeStream(Stream.of('a','b','c','h')).toString(),equalTo(""));
        assertThat(empty.removeStream(Stream.of('h','e','l','h')).toString(),equalTo(""));
    }

    @Test
    public void removeAt() {
        assertThat(str.removeAt(0),equalTo(LazyString.of("ello world")));
        assertThat(str.removeAt(1),equalTo(LazyString.of("hllo world")));
        assertThat(str.removeAt(10),equalTo(LazyString.of("hello worl")));
        assertThat(str.removeAt(-1),equalTo(str));
        assertThat(str.removeAt(100),equalTo(str));
        assertThat(str.removeAt(Integer.MAX_VALUE),equalTo(str));
        assertThat(str.removeAt(Integer.MAX_VALUE),equalTo(str));

        assertThat(empty.removeAt(0),equalTo(LazyString.of("")));
        assertThat(empty.removeAt(1),equalTo(LazyString.of("")));
        assertThat(empty.removeAt(10),equalTo(LazyString.of("")));
        assertThat(empty.removeAt(-1),equalTo(empty));
        assertThat(empty.removeAt(100),equalTo(empty));
        assertThat(empty.removeAt(Integer.MAX_VALUE),equalTo(empty));
        assertThat(empty.removeAt(Integer.MAX_VALUE),equalTo(empty));
    }

    @Test
    public void removeAll() {
        assertThat(str.removeAll(),equalTo(str));
        assertThat(str.removeAll('l'),equalTo(LazyString.of("heo word")));

        assertThat(empty.removeAll(),equalTo(empty));
        assertThat(empty.removeAll('l'),equalTo(empty));
    }

    @Test
    public void retainAll() {
        assertThat(str.retainAll(),equalTo(empty));
        assertThat(str.retainAll('l'),equalTo(LazyString.of("lll")));

        assertThat(empty.retainAll(),equalTo(empty));
        assertThat(empty.retainAll('l'),equalTo(empty));
    }

    @Test
    public void retainStream() {
        assertThat(str.retainStream(Stream.of('a','b','c')),equalTo(empty));
        assertThat(str.retainStream(Stream.of('a','b','c','h')).toString(),equalTo("h"));
        assertThat(str.retainStream(Stream.of('h','e','l','h')).toString(),equalTo("helll"));

        assertThat(empty.retainStream(Stream.of('a','b','c')),equalTo(empty));
        assertThat(empty.retainStream(Stream.of('a','b','c','h')).toString(),equalTo(""));
        assertThat(empty.retainStream(Stream.of('h','e','l','h')).toString(),equalTo(""));
    }

    @Test
    public void retainAll1() {
        assertThat(str.retainAll(Vector.empty()),equalTo(empty));
        assertThat(str.retainAll(Arrays.asList('l')),equalTo(LazyString.of("lll")));

        assertThat(empty.retainAll(Vector.empty()),equalTo(empty));
        assertThat(empty.retainAll(Vector.of('l')),equalTo(empty));
    }

    @Test
    public void distinct() {
        assertThat(empty.distinct(),equalTo(empty));
        assertThat(str.distinct(),equalTo(LazyString.of("helo wrd")));
    }

    @Test
    public void sorted() {
        assertThat(empty.sorted(),equalTo(empty));
        assertThat(str.sorted().toString(),equalTo(" dehllloorw"));
    }

    @Test
    public void sorted1() {
        assertThat(empty.sorted(),equalTo(empty));
        assertThat(str.sorted(new Comparator<Character>() {
            @Override
            public int compare(Character o1, Character o2) {
                return -o1.compareTo(o2);
            }
        }).toString(),equalTo("wroolllhed "));
    }

    @Test
    public void takeWhile() {
        assertThat(empty.takeWhile(i->true),equalTo(empty));
        assertThat(empty.takeWhile(i->false),equalTo(empty));
        assertThat(str.takeWhile(i->true),equalTo(str));
        assertThat(str.takeWhile(i->false),equalTo(empty));
    }

    @Test
    public void dropWhile() {
        assertThat(empty.dropWhile(i->true),equalTo(empty));
        assertThat(empty.dropWhile(i->false),equalTo(empty));
        assertThat(str.dropWhile(i->true),equalTo(empty));
        assertThat(str.dropWhile(i->false),equalTo(str));
    }

    @Test
    public void takeUntil() {
        assertThat(empty.takeUntil(i->true),equalTo(empty));
        assertThat(empty.takeUntil(i->false),equalTo(empty));
        assertThat(str.takeUntil(i->true),equalTo(empty));
        assertThat(str.takeUntil(i->false),equalTo(str));
    }

    @Test
    public void dropUntil() {
        assertThat(empty.dropUntil(i->true),equalTo(empty));
        assertThat(empty.dropUntil(i->false),equalTo(empty));
        assertThat(str.dropUntil(i->true),equalTo(str));
        assertThat(str.dropUntil(i->false),equalTo(empty));
    }


    @Test
    public void dropRight() {
        assertThat(empty.dropRight(2),equalTo(empty));
        assertThat(empty.dropRight(Integer.MAX_VALUE),equalTo(empty));
        assertThat(str.dropRight(Integer.MAX_VALUE),equalTo(empty));
        assertThat(str.dropRight(-2),equalTo(str));
        assertThat(str.dropRight(2).toString(),equalTo("hello wor"));

    }

    @Test
    public void takeRight() {
        assertThat(empty.takeRight(2),equalTo(empty));
        assertThat(empty.takeRight(Integer.MAX_VALUE),equalTo(empty));
        assertThat(str.takeRight(Integer.MAX_VALUE),equalTo(str));
        assertThat(str.takeRight(-2),equalTo(empty));
        assertThat(str.takeRight(2).toString(),equalTo("ld"));
    }

    @Test
    public void shuffle() {
        assertThat(empty.shuffle(),equalTo(empty));
        List<Character> list = str.shuffle().listView();
        System.out.println(list);
        assertThat(list,hasItems(str.lazySeq().listView().toArray(new Character[0])));
    }

    @Test
    public void shuffle1() {
        assertThat(empty.shuffle(new Random()),equalTo(empty));
        assertThat(str.shuffle(new Random()),hasItems(str.lazySeq().listView().toArray(new Character[0])));

    }

    @Test
    public void slice() {
        assertThat(empty.slice(0,100),equalTo(empty));
        assertThat(empty.slice(-100,100),equalTo(empty));
        assertThat(empty.slice(-100,Integer.MAX_VALUE),equalTo(empty));

        assertThat(str.slice(0,100),equalTo(str));
        assertThat(str.slice(-100,100),equalTo(str));
        assertThat(str.slice(-100,Integer.MAX_VALUE),equalTo(str));

        assertThat(str.slice(3,7).toString(),equalTo("lo w"));
    }

    @Test
    public void sorted2() {
        assertThat(empty.sorted(i->i.hashCode()),equalTo(empty));
        assertThat(str.sorted(i->i.hashCode()).toString(),equalTo(" dehllloorw"));
    }

    @Test
    public void prependStream() {
        assertThat(empty.prependStream(str.stream()),equalTo(str));
        assertThat(str.prependStream(empty.stream()),equalTo(str));
        assertThat(str.prependStream(str.stream()).toString(),equalTo("hello worldhello world"));
        assertThat(str.prependStream(LazyString.of("bob").stream()).toString(),equalTo("bobhello world"));
    }

    @Test
    public void appendAll() {
        assertThat(empty.appendAll(str),equalTo(str));
        assertThat(str.appendAll(empty),equalTo(str));
        assertThat(str.appendAll(str).toString(),equalTo("hello worldhello world"));
        assertThat(str.appendAll(LazyString.of("bob").stream()).toString(),equalTo("hello worldbob"));
    }

    @Test
    public void prependAll() {
        assertThat(empty.prependAll(str.stream()),equalTo(str));
        assertThat(str.prependAll(empty.stream()),equalTo(str));
        assertThat(str.prependAll(str.stream()).toString(),equalTo("hello worldhello world"));
        assertThat(str.prependAll(LazyString.of("bob").stream()).toString(),equalTo("bobhello world"));
    }


    @Test
    public void hashCodeTest(){
        assertThat(empty.hashCode(),equalTo(LazySeq.empty().hashCode()));
    }
    @Test
    public void deleteBetween() {
        assertThat(empty.deleteBetween(0,100),equalTo(empty));
        assertThat(empty.deleteBetween(-10000,10000),equalTo(empty));
        assertThat(empty.deleteBetween(-10000,Integer.MAX_VALUE),equalTo(empty));

        assertThat(str.deleteBetween(-10000,Integer.MAX_VALUE),equalTo(empty));
        assertThat(str.deleteBetween(0,100),equalTo(empty));
        assertThat(str.deleteBetween(-10000,10000),equalTo(empty));

        assertThat(str.deleteBetween(2,5),equalTo(LazyString.of("he world")));
        assertThat(str.deleteBetween(2,Integer.MAX_VALUE),equalTo(LazyString.of("he")));
    }

    @Test
    public void insertStreamAt() {
        assertThat(empty.insertStreamAt(0,Stream.of('a','b')),equalTo(LazyString.of("ab")));
        assertThat(empty.insertStreamAt(1,Stream.of('a','b')),equalTo(LazyString.of("ab")));
        assertThat(empty.insertStreamAt(Integer.MAX_VALUE,Stream.of('a','b')),equalTo(LazyString.of("ab")));
        assertThat(empty.insertStreamAt(-1,Stream.of('a','b')),equalTo(LazyString.of("ab")));
        assertThat(empty.insertStreamAt(-10000,Stream.of('a','b')),equalTo(LazyString.of("ab")));

        assertThat(str.insertStreamAt(0,Stream.of('a','b')),equalTo(LazyString.of("abhello world")));
        assertThat(str.insertStreamAt(1,Stream.of('a','b')),equalTo(LazyString.of("habello world")));
        assertThat(str.insertStreamAt(Integer.MAX_VALUE,Stream.of('a','b')),equalTo(LazyString.of("hello worldab")));
        assertThat(str.insertStreamAt(-1,Stream.of('a','b')),equalTo(LazyString.of("abhello world")));
        assertThat(str.insertStreamAt(-10000,Stream.of('a','b')),equalTo(LazyString.of("abhello world")));
    }

    @Test
    public void plusAll() {
        assertThat(empty.plusAll(str),equalTo(str));
        assertThat(str.plusAll(empty),equalTo(str));
        assertThat(str.plusAll(str).toString(),equalTo("hello worldhello world"));
        assertThat(str.plusAll(LazyString.of("bob")).toString(),equalTo("bobhello world"));

    }

    @Test
    public void plus() {
        assertThat(empty.plus('a'),equalTo(LazyString.of("a")));
        assertThat(str.plus('a'),equalTo(LazyString.of("ahello world")));
    }

    @Test
    public void removeValue() {
        assertThat(empty.removeValue('h'),equalTo(empty));
        assertThat(str.removeValue('x'),equalTo(str));
        assertThat(str.removeValue('l'),equalTo(LazyString.of("helo world")));
    }

    @Test
    public void removeAll1() {
        assertThat(empty.removeAll('h'),equalTo(empty));
        assertThat(str.removeAll('x'),equalTo(str));
        assertThat(str.removeAll('l'),equalTo(LazyString.of("heo word")));
        assertThat(str.removeAll('l','e'),equalTo(LazyString.of("ho word")));
        assertThat(str.removeAll('l','e','o'),equalTo(LazyString.of("h wrd")));

        assertThat(empty.removeAll(Seq.of('h')),equalTo(empty));
        assertThat(str.removeAll(Vector.of('x')),equalTo(str));
        assertThat(str.removeAll(Vector.of('l')),equalTo(LazyString.of("heo word")));
        assertThat(str.removeAll(Vector.of('l','e')),equalTo(LazyString.of("ho word")));
        assertThat(str.removeAll(Vector.of('l','e','o')),equalTo(LazyString.of("h wrd")));


    }

    @Test
    public void updateAt() {
        assertThat(empty.updateAt(0,'h'),equalTo(empty));
        assertThat(empty.updateAt(-100,'h'),equalTo(empty));
        assertThat(empty.updateAt(100,'h'),equalTo(empty));
        assertThat(empty.updateAt(Integer.MAX_VALUE,'h'),equalTo(empty));


        assertThat(str.updateAt(0,'x'),equalTo(LazyString.of("xello world")));
        assertThat(str.updateAt(1,'x'),equalTo(LazyString.of("hxllo world")));
        assertThat(str.updateAt(-100,'x'),equalTo(str));
        assertThat(str.updateAt(100,'x'),equalTo(str));
        assertThat(str.updateAt(Integer.MAX_VALUE,'x'),equalTo(str));
    }

    @Test
    public void insertAt1() {
        assertThat(empty.insertAt(0,'a'),equalTo(LazyString.of("a")));
        assertThat(empty.insertAt(1,'a'),equalTo(LazyString.of("a")));
        assertThat(empty.insertAt(Integer.MAX_VALUE,'a'),equalTo(LazyString.of("a")));
        assertThat(empty.insertAt(-1,'a'),equalTo(LazyString.of("a")));
        assertThat(empty.insertAt(-10000,'a'),equalTo(LazyString.of("a")));

        assertThat(str.insertAt(0,'a'),equalTo(LazyString.of("ahello world")));
        assertThat(str.insertAt(1,'a'),equalTo(LazyString.of("haello world")));
        assertThat(str.insertAt(Integer.MAX_VALUE,'a'),equalTo(LazyString.of("hello worlda")));
        assertThat(str.insertAt(-1,'a'),equalTo(LazyString.of("ahello world")));
        assertThat(str.insertAt(-10000,'a'),equalTo(LazyString.of("ahello world")));
    }

    @Test
    public void insertAt2() {
        assertThat(empty.insertAt(0,'a','b'),equalTo(LazyString.of("ab")));
        assertThat(empty.insertAt(1,'a','b'),equalTo(LazyString.of("ab")));
        assertThat(empty.insertAt(Integer.MAX_VALUE,'a','b'),equalTo(LazyString.of("ab")));
        assertThat(empty.insertAt(-1,'a','b'),equalTo(LazyString.of("ab")));
        assertThat(empty.insertAt(-10000,'a','b'),equalTo(LazyString.of("ab")));

        assertThat(str.insertAt(0,'a','b'),equalTo(LazyString.of("abhello world")));
        assertThat(str.insertAt(1,'a','b'),equalTo(LazyString.of("habello world")));
        assertThat(str.insertAt(Integer.MAX_VALUE,'a','b'),equalTo(LazyString.of("hello worldab")));
        assertThat(str.insertAt(-1,'a','b'),equalTo(LazyString.of("abhello world")));
        assertThat(str.insertAt(-10000,'a','b'),equalTo(LazyString.of("abhello world")));
    }
    @Test
    public void insertAt3() {
        assertThat(empty.insertAt(0,Seq.of('a','b')),equalTo(LazyString.of("ab")));
        assertThat(empty.insertAt(1,Seq.of('a','b')),equalTo(LazyString.of("ab")));
        assertThat(empty.insertAt(Integer.MAX_VALUE,Seq.of('a','b')),equalTo(LazyString.of("ab")));
        assertThat(empty.insertAt(-1,Seq.of('a','b')),equalTo(LazyString.of("ab")));
        assertThat(empty.insertAt(-10000,Seq.of('a','b')),equalTo(LazyString.of("ab")));

        assertThat(str.insertAt(0,Seq.of('a','b')),equalTo(LazyString.of("abhello world")));
        assertThat(str.insertAt(1,Seq.of('a','b')),equalTo(LazyString.of("habello world")));
        assertThat(str.insertAt(Integer.MAX_VALUE,Seq.of('a','b')),equalTo(LazyString.of("hello worldab")));
        assertThat(str.insertAt(-1,Seq.of('a','b')),equalTo(LazyString.of("abhello world")));
        assertThat(str.insertAt(-10000,Seq.of('a','b')),equalTo(LazyString.of("abhello world")));
    }

    @Test
    public void drop() {
        assertThat(empty.drop(-1),equalTo(empty));
        assertThat(empty.drop(-10000),equalTo(empty));
        assertThat(empty.drop(Integer.MAX_VALUE),equalTo(empty));
        assertThat(empty.drop(10),equalTo(empty));

        assertThat(str.drop(-1),equalTo(str));
        assertThat(str.drop(-10000),equalTo(str));
        assertThat(str.drop(Integer.MAX_VALUE),equalTo(empty));
        assertThat(str.drop(50),equalTo(empty));
        assertThat(str.drop(5),equalTo(LazyString.of(" world")));
    }

    @Test
    public void reverse() {
        assertThat(empty.reverse(),equalTo(empty));
        assertThat(str.reverse().toString(),equalTo("dlrow olleh"));
    }

    @Test
    public void get() {
        assertThat(empty.get(10),equalTo(Option.none()));
        assertThat(empty.get(Integer.MAX_VALUE),equalTo(Option.none()));
        assertThat(empty.get(-100),equalTo(Option.none()));

        assertThat(str.get(100),equalTo(Option.none()));
        assertThat(str.get(Integer.MAX_VALUE),equalTo(Option.none()));
        assertThat(str.get(-100),equalTo(Option.none()));
        assertThat(str.get(1),equalTo(Option.some('e')));
    }

    @Test
    public void getOrElse() {
        assertThat(empty.getOrElse(10,'x'),equalTo('x'));
        assertThat(empty.getOrElse(Integer.MAX_VALUE,'x'),equalTo('x'));
        assertThat(empty.getOrElse(-100,'x'),equalTo('x'));

        assertThat(str.getOrElse(100,'x'),equalTo('x'));
        assertThat(str.getOrElse(Integer.MAX_VALUE,'x'),equalTo('x'));
        assertThat(str.getOrElse(-100,'x'),equalTo('x'));
        assertThat(str.getOrElse(1,'x'),equalTo('e'));
    }

    @Test
    public void getOrElseGet() {
        assertThat(empty.getOrElseGet(10,()->'x'),equalTo('x'));
        assertThat(empty.getOrElseGet(Integer.MAX_VALUE,()->'x'),equalTo('x'));
        assertThat(empty.getOrElseGet(-100,()->'x'),equalTo('x'));

        assertThat(str.getOrElseGet(100,()->'x'),equalTo('x'));
        assertThat(str.getOrElseGet(Integer.MAX_VALUE,()->'x'),equalTo('x'));
        assertThat(str.getOrElseGet(-100,()->'x'),equalTo('x'));
        assertThat(str.getOrElseGet(1,()->'x'),equalTo('e'));
    }

    @Test
    public void prepend() {
        assertThat(empty.prepend('a'),equalTo(LazyString.of("a")));
        assertThat(str.prepend('a'),equalTo(LazyString.of("ahello world")));
    }

    @Test
    public void append() {
        assertThat(empty.append('a'),equalTo(LazyString.of("a")));
        assertThat(str.append('a'),equalTo(LazyString.of("hello worlda")));
    }

    @Test
    public void prependAll1() {
        assertThat(empty.prependAll(str),equalTo(str));
        assertThat(str.prependAll(empty),equalTo(str));
        assertThat(str.prependAll(str).toString(),equalTo("hello worldhello world"));
        assertThat(str.prependAll(LazyString.of("bob")).toString(),equalTo("bobhello world"));
    }

    @Test
    public void appendAll1() {
        assertThat(empty.appendAll(str),equalTo(str));
        assertThat(str.appendAll(empty),equalTo(str));
        assertThat(str.appendAll(str).toString(),equalTo("hello worldhello world"));
        assertThat(str.appendAll(LazyString.of("bob").stream()).toString(),equalTo("hello worldbob"));
    }

    @Test
    public void appendAll2() {
        assertThat(empty.appendAll(str.toArray(i->new Character[i])),equalTo(str));
        assertThat(str.appendAll(empty.toArray(i->new Character[i])),equalTo(str));
        assertThat(str.appendAll(str.toArray(i->new Character[i])).toString(),equalTo("hello worldhello world"));
        assertThat(str.appendAll(LazyString.of("bob").stream().toArray(i->new Character[i])).toString(),equalTo("hello worldbob"));
    }

    @Test
    public void prependAll2() {
        assertThat(empty.prependAll(str.stream().toArray(i->new Character[i])),equalTo(str));
        assertThat(str.prependAll(empty.stream().toArray(i->new Character[i])),equalTo(str));
        assertThat(str.prependAll(str.stream().toArray(i->new Character[i])).toString(),equalTo("hello worldhello world"));
        assertThat(str.prependAll(LazyString.of("bob").stream().toArray(i->new Character[i])).toString(),equalTo("bobhello world"));
    }

    @Test
    public void append1() {
        assertThat(empty.append("a"),equalTo(LazyString.of("a")));
        assertThat(str.append("a"),equalTo(LazyString.of("hello worlda")));
        assertThat(str.append("ab"),equalTo(LazyString.of("hello worldab")));
    }

    @Test
    public void size() {
        assertThat(empty.size(),equalTo(0));
        assertThat(str.size(),equalTo("hello world".length()));
    }

    @Test
    public void isEmpty() {
        assertThat(empty.isEmpty(),equalTo(true));
        assertThat(str.isEmpty(),equalTo(false));
    }

    @Test
    public void length() {
        assertThat(empty.length(),equalTo(0));
        assertThat(str.length(),equalTo("hello world".length()));
    }

    @Test
    public void toStringTest() {
        assertThat(empty.toString(),equalTo(""));
        assertThat(str.toString(),equalTo("hello world"));
    }
}
