package cyclops.data;


import com.oath.cyclops.hkt.Higher;
import cyclops.control.Option;
import com.oath.cyclops.hkt.DataWitness.lazyString;
import cyclops.reactive.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.reactivestreams.Publisher;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
    public ImmutableList<Character> onEmpty(Character value) {
        return string.onEmpty(value);
    }

    @Override
    public ImmutableList<Character> onEmptyGet(Supplier<? extends Character> supplier) {
        return string.onEmptyGet(supplier);
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
    public LazyString appendAll(Character value) {
        return fromLazySeq(string.appendAll(value)) ;
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
