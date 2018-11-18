package cyclops.data;


import com.oath.cyclops.hkt.Higher;
import cyclops.control.Option;
import com.oath.cyclops.hkt.DataWitness.lazyString;
import cyclops.reactive.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.reactivestreams.Publisher;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class LazyString implements ImmutableList<Character>,Higher<lazyString,Character>, Serializable {
    private static final long serialVersionUID = 1L;
    private final LazySeq<Character> string;

    private static final LazyString Nil = fromLazySeq(LazySeq.empty());
    public static LazyString fromLazySeq(LazySeq<Character> string){
        return new LazyString(string);
    }
    public static LazyString fromIterable(Iterable<Character> string){
        return new LazyString(LazySeq.fromIterable(string));
    }
    public static LazyString of(CharSequence seq){
        return fromLazySeq(LazySeq.fromStream( seq.chars().mapToObj(i -> (char) i)));
    }

    static Collector<Character, List<Character>, LazyString> collector() {
        Collector<Character, ?, List<Character>> c  = Collectors.toList();
        return Collectors.<Character, List<Character>, Iterable<Character>,LazyString>collectingAndThen((Collector)c,LazyString::fromIterable);
    }

    @Override
    public<R> LazySeq<R> unitIterable(Iterable<R> it){
        if(it instanceof LazySeq){
            return (LazySeq<R>)it;
        }
        return LazySeq.fromIterable(it);
    }
    public static LazyString empty(){
        return Nil;
    }

    public LazyString op(Function<? super LazySeq<Character>, ? extends LazySeq<Character>> custom){
        return fromLazySeq(custom.apply(string));
    }

    public LazyString substring(int start){
        return drop(start);
    }
    public LazyString substring(int start, int end){
        return drop(start).take(end-start);
    }
    public LazyString toUpperCase(){
        return fromLazySeq(string.map(c->c.toString().toUpperCase().charAt(0)));
    }
    public LazyString toLowerCase(){
        return fromLazySeq(string.map(c->c.toString().toLowerCase().charAt(0)));
    }
    public LazySeq<LazyString> words() {
        return string.split(t -> t.equals(' ')).map(l-> fromLazySeq(l));
    }
    public LazySeq<LazyString> lines() {
        return string.split(t -> t.equals('\n')).map(l-> fromLazySeq(l));
    }
    public LazyString mapChar(Function<Character,Character> fn){
        return fromLazySeq(string.map(fn));
    }
    public LazyString flatMapChar(Function<Character,LazyString> fn){
        return fromLazySeq(string.flatMap(fn.andThen(s->s.string)));
    }

    @Override
    public LazyString filter(Predicate<? super Character> predicate) {
        return fromLazySeq(string.filter(predicate));
    }

    @Override
    public <R> ImmutableList<R> map(Function<? super Character, ? extends R> fn) {
        return string.map(fn);
    }

    @Override
    public <R> ImmutableList<R> flatMap(Function<? super Character, ? extends ImmutableList<? extends R>> fn) {
        return  string.flatMap(fn);
    }

    @Override
    public <R> ImmutableList<R> concatMap(Function<? super Character, ? extends Iterable<? extends R>> fn) {
        return  string.concatMap(fn);
    }

    @Override
    public <R> ImmutableList<R> mergeMap(Function<? super Character, ? extends Publisher<? extends R>> fn) {
      return string.mergeMap(fn);
    }

    @Override
    public <R> ImmutableList<R> mergeMap(int maxConcurecy, Function<? super Character, ? extends Publisher<? extends R>> fn) {
      return string.mergeMap(maxConcurecy,fn);
    }

  @Override
    public <R> R fold(Function<? super Some<Character>, ? extends R> fn1, Function<? super None<Character>, ? extends R> fn2) {
        return string.fold(fn1,fn2);
    }

    @Override
    public LazyString onEmpty(Character value) {
        return fromLazySeq(string.onEmpty(value));
    }

    @Override
    public LazyString onEmptyGet(Supplier<? extends Character> supplier) {
        return fromLazySeq(string.onEmptyGet(supplier));
    }


    @Override
    public ImmutableList<Character> onEmptySwitch(Supplier<? extends ImmutableList<Character>> supplier) {
        return string.onEmptySwitch(supplier);
    }

    public ReactiveSeq<Character> stream(){
        return string.stream();
    }
    public LazyString take(final long n) {
        return fromLazySeq(string.take(n));

    }

    @Override
    public <R> ImmutableList<R> unitStream(Stream<R> stream) {
        return LazySeq.fromStream(stream);
    }

    @Override
    public LazyString emptyUnit() {
        return empty();
    }

    @Override
    public LazyString replaceFirst(Character currentElement, Character newElement) {
        return fromLazySeq(string.replaceFirst(currentElement,newElement));
    }

    @Override
    public LazyString removeFirst(Predicate<? super Character> pred) {
        return fromLazySeq(string.removeFirst(pred));
    }

    @Override
    public LazyString subList(int start, int end) {
        return fromLazySeq(string.subList(start,end));
    }


    @Override
    public LazyString filterNot(Predicate<? super Character> predicate) {
        return fromLazySeq(string.filterNot(predicate));
    }

    @Override
    public LazyString notNull() {
        return fromLazySeq(string.notNull());
    }

    @Override
    public LazyString peek(Consumer<? super Character> c) {
        return fromLazySeq(string.peek(c));
    }

    @Override
    public LazyString tailOrElse(ImmutableList<Character> tail) {
        return fromLazySeq(string.tailOrElse(LazySeq.fromIterable(tail)));
    }

    @Override
    public LazyString removeStream(Stream<? extends Character> stream) {
        return fromLazySeq(string.removeStream(stream));
    }

    @Override
    public LazyString removeAt(long pos) {
        return fromLazySeq(string.removeAt(pos));
    }

    @Override
    public LazyString removeAll(Character... values) {
        return fromLazySeq(string.removeAll(values));
    }

    @Override
    public LazyString retainAll(Iterable<? extends Character> it) {
        return fromLazySeq(string.retainAll(it));
    }

    @Override
    public LazyString retainStream(Stream<? extends Character> stream) {
        return fromLazySeq(string.retainStream(stream));
    }

    @Override
    public LazyString retainAll(Character... values) {
        return fromLazySeq(string.retainAll(values));
    }

    @Override
    public LazyString distinct() {
        return fromLazySeq(string.distinct());
    }

    @Override
    public LazyString sorted() {
        return fromLazySeq(string.sorted());
    }

    @Override
    public LazyString sorted(Comparator<? super Character> c) {
        return fromLazySeq(string.sorted(c));
    }

    @Override
    public LazyString takeWhile(Predicate<? super Character> p) {
        return fromLazySeq(string.takeWhile(p));
    }

    @Override
    public LazyString dropWhile(Predicate<? super Character> p) {
        return fromLazySeq(string.dropWhile(p));
    }

    @Override
    public LazyString takeUntil(Predicate<? super Character> p) {
        return fromLazySeq(string.takeUntil(p));
    }

    @Override
    public LazyString dropUntil(Predicate<? super Character> p) {
        return fromLazySeq(string.dropUntil(p));
    }

    @Override
    public LazyString dropRight(int num) {
        return fromLazySeq(string.dropRight(num));
    }

    @Override
    public LazyString takeRight(int num) {
        return fromLazySeq(string.takeRight(num));
    }

    @Override
    public LazyString shuffle() {
        return fromLazySeq(string.shuffle());
    }

    @Override
    public LazyString shuffle(Random random) {
        return fromLazySeq(string.shuffle(random));
    }

    @Override
    public LazyString slice(long from, long to) {
        return fromLazySeq(string.slice(from,to));
    }

    @Override
    public <U extends Comparable<? super U>> LazyString sorted(Function<? super Character, ? extends U> function) {
        return fromLazySeq(string.sorted(function));
    }

    @Override
    public LazyString prependStream(Stream<? extends Character> stream) {
        return fromLazySeq(string.prependStream(stream));
    }

    @Override
    public LazyString appendAll(Character... values) {
        return fromLazySeq(string.appendAll(values));
    }

    @Override
    public LazyString prependAll(Character... values) {
        return fromLazySeq(string.prependAll(values));
    }

    @Override
    public LazyString insertAt(int pos, Character... values) {
        return fromLazySeq(string.insertAt(pos,values));
    }

    @Override
    public LazyString deleteBetween(int start, int end) {
        return fromLazySeq(string.deleteBetween(start,end));
    }

    @Override
    public LazyString insertStreamAt(int pos, Stream<Character> stream) {
        return fromLazySeq(string.insertStreamAt(pos,stream));
    }



    @Override
    public LazyString plusAll(Iterable<? extends Character> list) {
        return fromLazySeq(string.plusAll(list));
    }

    @Override
    public LazyString plus(Character value) {
        return fromLazySeq(string.plus(value));
    }

    @Override
    public LazyString removeValue(Character value) {
        return fromLazySeq(string.removeValue(value));
    }


    @Override
    public LazyString removeAll(Iterable<? extends Character> value) {
        return fromLazySeq(string.removeAll(value));
    }

    @Override
    public LazyString updateAt(int pos, Character value) {
        return fromLazySeq(string.updateAt(pos,value));
    }

    @Override
    public LazyString insertAt(int pos, Iterable<? extends Character> values) {
        return fromLazySeq(string.insertAt(pos,values));
    }

    @Override
    public LazyString insertAt(int i, Character value) {
        return fromLazySeq(string.insertAt(i,value));
    }

    public LazyString  drop(final long num) {
        return fromLazySeq(string.drop(num));
    }
    public LazyString  reverse() {
        return fromLazySeq(string.reverse());
    }
    public Option<Character> get(int pos){
        return string.get(pos);
    }

    @Override
    public Character getOrElse(int pos, Character alt) {
        return string.getOrElse(pos,alt);
    }

    @Override
    public Character getOrElseGet(int pos, Supplier<? extends Character> alt) {
        return string.getOrElseGet(pos,alt);
    }

    public LazyString prepend(Character value){
        return fromLazySeq(string.prepend(value));
    }

    @Override
    public LazyString append(Character value) {
        return fromLazySeq(string.append(value));
    }

    @Override
    public LazyString prependAll(Iterable<? extends Character> value) {
        return fromLazySeq(string.prependAll(value)) ;
    }


    @Override
    public LazyString appendAll(Iterable<? extends Character> value) {
        return fromLazySeq(string.appendAll(value)) ;
    }

    public LazyString prependAll(LazyString value){
        return fromLazySeq(string.prependAll(value.string));
    }
    public LazyString append(String s){
        return fromLazySeq(string.appendAll(LazySeq.fromStream( s.chars().mapToObj(i -> (char) i))));
    }
    public int size(){
        return length();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public int length(){
        return string.size();
    }
    public String toString(){
        return string.stream().join("");
    }

}
