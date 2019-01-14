package cyclops.data;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsInAnyOrder;
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
    }

    @Test
    public void retainAll() {
    }

    @Test
    public void retainStream() {
    }

    @Test
    public void retainAll1() {
    }

    @Test
    public void distinct() {
    }

    @Test
    public void sorted() {
    }

    @Test
    public void sorted1() {
    }

    @Test
    public void takeWhile() {
    }

    @Test
    public void dropWhile() {
    }

    @Test
    public void takeUntil() {
    }

    @Test
    public void dropUntil() {
    }

    @Test
    public void dropRight() {
    }

    @Test
    public void takeRight() {
    }

    @Test
    public void shuffle() {
    }

    @Test
    public void shuffle1() {
    }

    @Test
    public void slice() {
    }

    @Test
    public void sorted2() {
    }

    @Test
    public void prependStream() {
    }

    @Test
    public void appendAll() {
    }

    @Test
    public void prependAll() {
    }

    @Test
    public void insertAt() {
    }

    @Test
    public void deleteBetween() {
    }

    @Test
    public void insertStreamAt() {
    }

    @Test
    public void plusAll() {
    }

    @Test
    public void plus() {
    }

    @Test
    public void removeValue() {
    }

    @Test
    public void removeAll1() {
    }

    @Test
    public void updateAt() {
    }

    @Test
    public void insertAt1() {
    }

    @Test
    public void insertAt2() {
    }

    @Test
    public void drop() {
    }

    @Test
    public void reverse() {
    }

    @Test
    public void get() {
    }

    @Test
    public void getOrElse() {
    }

    @Test
    public void getOrElseGet() {
    }

    @Test
    public void prepend() {
    }

    @Test
    public void append() {
    }

    @Test
    public void prependAll1() {
    }

    @Test
    public void appendAll1() {
    }

    @Test
    public void prependAll2() {
    }

    @Test
    public void append1() {
    }

    @Test
    public void size() {
    }

    @Test
    public void isEmpty() {
    }

    @Test
    public void length() {
    }

    @Test
    public void toStringTest() {
    }
}
