package com.aol.cyclops.control;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hamcrest.Matcher;
import org.jooq.lambda.function.Function1;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple1;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.jooq.lambda.tuple.Tuple5;

import com.aol.cyclops.internal.matcher2.ADTPredicateBuilder;
import com.aol.cyclops.internal.matcher2.MatchableCase;
import com.aol.cyclops.internal.matcher2.MatchingInstance;
import com.aol.cyclops.internal.matcher2.PatternMatcher;
import com.aol.cyclops.internal.matcher2.SeqUtils;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.types.Decomposable;
import com.aol.cyclops.types.Value;
import com.aol.cyclops.util.function.QuadFunction;
import com.aol.cyclops.util.function.QuintFunction;
import com.aol.cyclops.util.function.TriFunction;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * Matchable
 * 
 * todo - add AsMatchable.asMatchable
 * 
 * @author johnmcclean
 *
 */
public interface Matchable<TYPE>{
	
	public static <T,R> Supplier<? extends R> then(R value){
		return ()-> value;
	}
	public static <T,R> Supplier<? extends R> then(Supplier<? extends R> fn){
		return fn;
	}
	public static <R> Supplier<? extends R> otherwise(R value){
		return ()-> value;
	}
	public static <R> Supplier<? extends R> otherwise(Supplier<? extends R> s){
		return  s;
	}
	@SafeVarargs
	public static <T,V>  Iterable<Predicate<? super T>> whenGuard(V... values){
		return (Iterable)()->ReactiveSeq.of(values)
						  			.map(v->ADTPredicateBuilder.convertToPredicateTyped(v))
						  			.iterator();
	}
	
	//when arity 1
	public static <T1> MatchableTuple1<Predicate<? super T1>> when(T1 t1){
		
		return ()->Tuple.tuple(test -> Objects.equals(test,t1));
	}
	public static <T1> MatchableTuple1<Predicate<? super T1>> when(Predicate<? super T1> t1){
		
		return ()->Tuple.tuple(t1);
	}
	public static <T1> MatchableTuple1<Predicate<? super T1>> when(Matcher<? super T1> t1){
		return ()->Tuple.tuple(test -> t1.matches(test));
	}
	//when arity 2
	
	public static <T1,T2>  MTuple2<Predicate<? super T1>,Predicate<? super T2>> when(T1 t1,T2 t2){
		
		return ()->Tuple.tuple(test -> Objects.equals(test,t1),test -> Objects.equals(test,t2));
	}
	public static <T1,T2,T3> MTuple2<Predicate<? super T1>,Predicate<? super T2>> when(Predicate<? super T1> t1,Predicate<? super T2> t2){
		
		return ()->Tuple.tuple(t1,t2);
	}
	public static <T1,T2,T3> MTuple2<Predicate<? super T1>,Predicate<? super T2>> when(Matcher<? super T1> t1,Matcher<? super T2> t2){
		
		return ()->Tuple.tuple(test -> t1.matches(test),test -> t2.matches(test));
	}
	//when arity 3
	public static <T1,T2,T3> MTuple3<Predicate<? super T1>,Predicate<? super T2>,Predicate<? super T3>> when(T1 t1,T2 t2,T3 t3){
		
		return ()->Tuple.tuple(test -> Objects.equals(test,t1),test -> Objects.equals(test,t2),test -> Objects.equals(test,t3));
	}
	public static <T1,T2,T3> MTuple3<Predicate<? super T1>,Predicate<? super T2>,Predicate<? super T3>> when(Predicate<? super T1> t1,Predicate<? super T2> t2,Predicate<? super T3> t3){
		
		return ()->Tuple.tuple(t1,t2,t3);
	}
	public static <T1,T2,T3> MTuple3<Predicate<? super T1>,Predicate<? super T2>,Predicate<? super T3>> when(Matcher<? super T1> t1,Matcher<? super T2> t2,Matcher<? super T3> t3){
		
		return ()->Tuple.tuple(test -> t1.matches(test),test -> t2.matches(test),test -> t3.matches(test));
	}
	//when arity 4
	public static <T1,T2,T3,T4> MTuple4<Predicate<? super T1>,Predicate<? super T2>,Predicate<? super T3>,Predicate<? super T4>> when(T1 t1,T2 t2,T3 t3,T4 t4){
		
		return ()->Tuple.tuple(test -> Objects.equals(test,t1),test -> Objects.equals(test,t2),test -> Objects.equals(test,t3),test -> Objects.equals(test,t4));
	}
	public static  <T1,T2,T3,T4> MTuple4<Predicate<? super T1>,Predicate<? super T2>,Predicate<? super T3>,Predicate<? super T4>> when(Predicate<? super T1> t1,Predicate<? super T2> t2,Predicate<? super T3> t3,Predicate<? super T4> t4){
		
		return ()->Tuple.tuple(t1,t2,t3,t4);
	}
	public static  <T1,T2,T3,T4> MTuple4<Predicate<? super T1>,Predicate<? super T2>,Predicate<? super T3>,Predicate<? super T4>> when(Matcher<? super T1> t1,
																						Matcher<? super T2> t2,
																						Matcher<? super T3> t3,
																						Matcher<? super T4> t4){
		
		return ()->Tuple.tuple(test -> t1.matches(test),test -> t2.matches(test),test -> t3.matches(test),test -> t4.matches(test));
	}
	//when arity 5
	public static <T1,T2,T3,T4,T5> MTuple5<Predicate<? super T1>,Predicate<? super T2>,Predicate<? super T3>,Predicate<? super T4>,Predicate<? super T5>> when(T1 t1,T2 t2,T3 t3,T4 t4, T5 t5){
		
		return ()->Tuple.tuple(test -> Objects.equals(test,t1),test -> Objects.equals(test,t2),test -> Objects.equals(test,t3),test -> Objects.equals(test,t4),test -> Objects.equals(test,t5));
	}
	public static  <T1,T2,T3,T4,T5> MTuple5<Predicate<? super T1>,Predicate<? super T2>,Predicate<? super T3>,Predicate<? super T4>,Predicate<? super T5>> when(Predicate<? super T1> t1,Predicate<? super T2> t2,
																							Predicate<? super T3> t3,
																							Predicate<? super T4> t4,
																							Predicate<? super T5> t5){
		
		return ()->Tuple.tuple(t1,t2,t3,t4,t5);
	}
	public static  <T1,T2,T3,T4,T5> MTuple5<Predicate<? super T1>,Predicate<? super T2>,Predicate<? super T3>,Predicate<? super T4>,Predicate<? super T5>> when(Matcher<? super T1> t1,
																						Matcher<? super T2> t2,
																						Matcher<? super T3> t3,
																						Matcher<? super T4> t4,
																						Matcher<? super T5> t5){
		
		return ()->Tuple.tuple(test -> t1.matches(test),test -> t2.matches(test),test -> t3.matches(test),test -> t4.matches(test),test -> t5.matches(test));
	}	
	
	public static interface MatchSelf<TYPE> extends Matchable<TYPE>{
		default Object getMatchable(){
			return this;
		}
	}
	
	/**
	 * @return matchable
	 */
	Object getMatchable();
	
	/*
	 * Match against the values inside the matchable with a single case
	 * 
	 * <pre>
	 * {@code
	 * int result = Matchable.of(Optional.of(1))
								.matches(c->c.hasValues(1).then(i->2));
		//2						
	 * }</pre>
	 * 
	 * Note, it is possible to continue to chain cases within a single case, but cleaner
	 * to use the appropriate overloaded matches method that accepts two (or more) cases.
	 * 
	 * @param fn1 Describes the matching case
	 * @return Result - this method requires a match or an NoSuchElement exception is thrown
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <R> Eval<R>  matches(Function<CheckValues<TYPE,R>,CheckValues<TYPE, R>> fn1,Supplier<? extends R> otherwise){
		return Eval.later(()->(R) new MatchingInstance(new MatchableCase( fn1.apply( (CheckValues)
				new MatchableCase(new PatternMatcher()).withType(getMatchable().getClass())).getPatternMatcher()))
					.match(getMatchable()).orElseGet(otherwise));
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <R> Maybe<R>  mayMatch(Function<CheckValues<TYPE,R>,CheckValues<TYPE,R>> fn1){
		return  new MatchingInstance(new MatchableCase( fn1.apply( (CheckValues)
				new MatchableCase(new PatternMatcher()).withType(getMatchable().getClass())).getPatternMatcher()))
					.match(getMatchable());
	}
	
	
	/**
	 * Create a new matchable that will match on the fields of the provided Object
	 * 
	 * @param o Object to match on it's fields
	 * @return new Matchable
	 */
	public static<T> Matchable<T> of(T o){
		if(o instanceof Stream)
			return of((Stream)o);
		if(o instanceof Optional)
			return of((Optional)o);
		if(o instanceof CompletableFuture)
			return of((CompletableFuture)o);
		if(o instanceof Value)
			return of((Value)o);
		if(o instanceof Iterable)
			return of((Iterable)o);
		return AsMatchable.asMatchable(o);
	}
	public static<T> Matchable<T> of(Iterable<T> o){
		return AsMatchable.asMatchable(o);
	}
	public static<T> MatchableIterable<T> ofIterable(Iterable<T> o){
		return ()->o.iterator();
	}
	public static<T> Matchable<T> of(Optional<T> o){
		return AsMatchable.asMatchable(o);
	}
	public static<T> Matchable<T> of(CompletableFuture<T> o){
		return of(FutureW.of(o));
	}
	public static<T> Matchable<T> of(Value<T> o){
		return AsMatchable.asMatchable(o.toOptional());
	}
	public static<T> Matchable<Optional<T>> fromOptional(Optional<T> o){
		return AsMatchable.asMatchable(o);
	}
	public static<T> Matchable<Value<T>> fromValue(Value<T> o){
		return AsMatchable.asMatchable(o);
	}
	/**
	 * Create a new matchable that will match on the fields of the provided Stream
	 * 
	 * @param o Object to match on it's fields
	 * @return new Matchable
	 */
	public static <T> Matchable<T> of(Stream<T> o){
		return AsMatchable.asMatchable(o.collect(Collectors.toList()));
	}
	/**
	 * Create a new matchable that will match on the fields of the provided Decomposable
	 * 
	 * @param o Decomposable to match on it's fields
	 * @return new Matchable
	 */
	public static <T extends Decomposable> Matchable<T> ofDecomposable(Decomposable o){
		return AsMatchable.asMatchable(o);
	}
	
	/**
	 * Create a matchable that matches on the provided Objects
	 * 
	 * @param o Objects to match on
	 * @return new Matchable
	 */
	public static <T>  Matchable<T> listOfValues(T... o){
		return AsMatchable.asMatchable(Arrays.asList(o));
	}
	public static <T> MatchableIterable<T> fromIterable(Iterable<T> it){
		return ()->it;
	}
	
	public static <TYPE, T1 extends TYPE> MatchableTuple1<T1> from(Supplier<T1> s1){
		
		return ()-> Tuple.tuple(s1.get());
	}
	public static <TYPE, T1 extends TYPE> MatchableTuple1<T1> from(Tuple1<T1> t2){
		return ()-> t2;
	}
	public static <TYPE, T1 extends TYPE, T2 extends TYPE> MTuple2<T1,T2> from(Supplier<T1> s1, Supplier<T2> s2){
		return ()-> Tuple.tuple(s1.get(),s2.get());
	}
	public static <TYPE, T1 extends TYPE, T2 extends TYPE> MTuple2<T1,T2> from(Tuple2<T1,T2> t2){
		return ()-> t2;
	}
	public static <TYPE, T1 extends TYPE, T2 extends TYPE,T3 extends TYPE> MTuple3<T1,T2,T3> from(Tuple3<T1,T2,T3> t3){
		return ()-> t3;
	}
	public static <TYPE, T1 extends TYPE, T2 extends TYPE,T3 extends TYPE> MTuple3<T1,T2,T3> from(Supplier<T1> s1, Supplier<T2> s2, Supplier<T3> s3){
		return()-> Tuple.tuple(s1.get(),s2.get(),s3.get());
	}
	public static <TYPE, T1 extends TYPE, T2 extends TYPE,T3 extends TYPE,T4 extends TYPE> MTuple4<T1,T2,T3,T4> from(Tuple4<T1,T2,T3,T4> t4){
		return ()-> t4;
	}
	public static <TYPE, T1 extends TYPE, T2 extends TYPE,T3 extends TYPE,T4 extends TYPE> MTuple4<T1,T2,T3,T4> from(Supplier<T1> s1, Supplier<T2> s2, 
												Supplier<T3> s3,Supplier<T4> s4){
		return()-> Tuple.tuple(s1.get(),s2.get(),s3.get(),s4.get());
	}
	public static <TYPE, T1 extends TYPE, T2 extends TYPE,T3 extends TYPE,T4 extends TYPE,T5 extends TYPE> MTuple5<T1,T2,T3,T4,T5> from(Tuple5<T1,T2,T3,T4,T5> t5){
		return ()-> t5;
	}
	public static <TYPE, T1 extends TYPE, T2 extends TYPE,T3 extends TYPE,T4 extends TYPE,T5 extends TYPE> MTuple5<T1,T2,T3,T4,T5> from(Supplier<T1> s1, Supplier<T2> s2, 
												Supplier<T3> s3,Supplier<T4> s4,Supplier<T5> s5){
		return()-> Tuple.tuple(s1.get(),s2.get(),s3.get(),s4.get(),s5.get());
	}
	
	public static  MatchableIterable<Character> fromCharSequence(CharSequence chars){
		Iterable<Character> it = ()->chars.chars().boxed().map(i ->Character.toChars(i)[0]).iterator();
		return ()-> it;
	}
	
	public static interface MatchableIterable<TYPE> extends Matchable<TYPE>{
		
		default <R> Eval<R> visitSeq(BiFunction<? super Maybe<TYPE>,? super ReactiveSeq<TYPE>,? extends R> match ){
			@SuppressWarnings("unchecked")
			Iterable<TYPE> it = (Iterable<TYPE>)getMatchable();
			return Eval.later(()->ReactiveSeq.fromIterable(it).visit(match));	
		}	
		default <R> Eval<R> visit(BiFunction<? super Maybe<TYPE>,? super MatchableIterable<TYPE>,? extends R> match ){
			@SuppressWarnings("unchecked")
			Iterable<TYPE> it = (Iterable<TYPE>)getMatchable();
			
			return 	Eval.later(()->{ 
				Iterator<TYPE> iter = it.iterator();
				Maybe<TYPE> head = Maybe.ofNullable( (iter.hasNext() ? iter.next() : null));
				
				MatchableIterable<TYPE> matchable = ()->iter;
				return match.apply(head,matchable);
			});
		}
		default Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> toTuple5(Object o){
			Iterator it = ((Iterable)o).iterator();
			return Tuple.tuple((TYPE)(it.hasNext() ?it.next():null),
					(TYPE)(it.hasNext() ?it.next():null),
					(TYPE)(it.hasNext() ?it.next():null),
					(TYPE)(it.hasNext() ?it.next():null),
					(TYPE)(it.hasNext() ?it.next():null));
		}
		default MatchableTuple1<TYPE> on$1____(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple1<TYPE>(it.v1);
		}
		default MatchableTuple1<TYPE> on$_2___(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple1<TYPE>(it.v2);
		}
		default MatchableTuple1<TYPE> on$__3__(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple1<TYPE>(it.v3);
		}
		default MatchableTuple1<TYPE> on$___4_(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple1<TYPE>(it.v4);
		}
		default MatchableTuple1<TYPE> on$____5(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple1<TYPE>(it.v5);
		}
		default MatchableTuple2<TYPE,TYPE,TYPE> on$12___(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple2<TYPE,TYPE>(it.v1,it.v2);
		}
		default MatchableTuple2<TYPE,TYPE,TYPE> on$1_3__(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple2<TYPE,TYPE>(it.v1,it.v3);
		}
		default MatchableTuple2<TYPE,TYPE,TYPE> on$1__4_(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple2<TYPE,TYPE>(it.v1,it.v4);
		}
		default MatchableTuple2<TYPE,TYPE,TYPE> on$1___5(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple2<TYPE,TYPE>(it.v1,it.v5);
		}
		default MatchableTuple2<TYPE,TYPE,TYPE> on$_23__(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple2<TYPE,TYPE>(it.v2,it.v3);
		}
		default MatchableTuple2<TYPE,TYPE,TYPE> on$_2_4_(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple2<TYPE,TYPE>(it.v2,it.v4);
		}
		default MatchableTuple2<TYPE,TYPE,TYPE> on$_2__5(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple2<TYPE,TYPE>(it.v2,it.v5);
		}
		default MatchableTuple2<TYPE,TYPE,TYPE> on$__34_(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple2<TYPE,TYPE>(it.v3,it.v4);
		}
		default MatchableTuple2<TYPE,TYPE,TYPE> on$__3_5(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple2<TYPE,TYPE>(it.v3,it.v5);
		}
		default MatchableTuple2<TYPE,TYPE,TYPE> on$___45(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple2<TYPE,TYPE>(it.v4,it.v5);
		}
		
		default MatchableTuple3<TYPE,TYPE,TYPE,TYPE> on$123__(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple3<TYPE,TYPE,TYPE>(it.v1,it.v2,it.v3);
		}
		default MatchableTuple3<TYPE,TYPE,TYPE,TYPE> on$12_4_(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple3<TYPE,TYPE,TYPE>(it.v1,it.v2,it.v4);
		}
		default MatchableTuple3<TYPE,TYPE,TYPE,TYPE> on$12__5(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple3<TYPE,TYPE,TYPE>(it.v1,it.v2,it.v5);
		}
		default MatchableTuple3<TYPE,TYPE,TYPE,TYPE> on$1_34_(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple3<TYPE,TYPE,TYPE>(it.v1,it.v3,it.v4);
		}
		default MatchableTuple3<TYPE,TYPE,TYPE,TYPE> on$1_3_5(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple3<TYPE,TYPE,TYPE>(it.v1,it.v3,it.v5);
		}
		default MatchableTuple3<TYPE,TYPE,TYPE,TYPE> on$1__45(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple3<TYPE,TYPE,TYPE>(it.v1,it.v4,it.v5);
		}
	
		default MatchableTuple3<TYPE,TYPE,TYPE,TYPE> on$_234_(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple3<TYPE,TYPE,TYPE>(it.v2,it.v3,it.v4);
		}
		default MatchableTuple3<TYPE,TYPE,TYPE,TYPE> on$_23_5(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple3<TYPE,TYPE,TYPE>(it.v2,it.v3,it.v5);
		}
		default MatchableTuple3<TYPE,TYPE,TYPE,TYPE> on$__345(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple3<TYPE,TYPE,TYPE>(it.v3,it.v4,it.v5);
		}
		default MatchableTuple4<TYPE,TYPE,TYPE,TYPE,TYPE> on$1234_(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple4<TYPE,TYPE,TYPE,TYPE>(it.v1,it.v2,it.v3,it.v4);
		}
		default MatchableTuple4<TYPE,TYPE,TYPE,TYPE,TYPE> on$123_5(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple4<TYPE,TYPE,TYPE,TYPE>(it.v1,it.v2,it.v3,it.v5);
		}
		default MatchableTuple4<TYPE,TYPE,TYPE,TYPE,TYPE> on$12_45(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple4<TYPE,TYPE,TYPE,TYPE>(it.v1,it.v2,it.v4,it.v5);
		}
		default MatchableTuple4<TYPE,TYPE,TYPE,TYPE,TYPE> on$1_345(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple4<TYPE,TYPE,TYPE,TYPE>(it.v1,it.v3,it.v4,it.v5);
		}
		default MatchableTuple4<TYPE,TYPE,TYPE,TYPE,TYPE> on$_2345(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple4<TYPE,TYPE,TYPE,TYPE>(it.v2,it.v3,it.v4,it.v5);
		}
		
	}
	public static interface MTuple1<T1> extends MatchableTuple1<T1>{
		default <U1> MTuple1<U1> map1(Function1<? super T1, ? extends U1> function) {
	        return ()->getMatchable().map1(function);
	    }
	}
	public static interface MatchableTuple1<T1> extends Iterable<T1>{
		Tuple1<T1> getMatchable();
		default T1 v1(){
			return getMatchable().v1;
		}
		default int compareTo(MatchableTuple1<T1> other) {
			return getMatchable().compareTo(other.getMatchable());
		}
		default  Iterator<T1> iterator(){
			return Arrays.asList(getMatchable().v1).iterator();
		}
		default <R> R visit(Function<? super T1,? extends R> some,Supplier<? extends R> none ){
			@SuppressWarnings("unchecked")
			Tuple1<T1> it = (Tuple1<T1>)getMatchable();
			
			return  Maybe.ofNullable(it.v1).visit(some, none);
		}	
		@SuppressWarnings({ "rawtypes", "unchecked" })
		default <R> Maybe<R>  mayMatch(Function<CheckValue1<T1,R>,CheckValue1<T1,R>> fn1){
			return  new MatchingInstance(new MatchableCase( fn1.apply( (CheckValue1)
					new MatchableCase(new PatternMatcher()).withType1(getMatchable().getClass())).getPatternMatcher()))
						.match(getMatchable());
		}
		@SuppressWarnings({ "rawtypes", "unchecked" })
		default <R> Eval<R>  matches(Function<CheckValue1<T1,R>,CheckValue1<T1,R>> fn1,Supplier<? extends R> s){
			return  Eval.later(()->(R)new MatchingInstance(new MatchableCase( fn1.apply( (CheckValue1)
					new MatchableCase(new PatternMatcher()).withType1(getMatchable().getClass())).getPatternMatcher()))
						.match(getMatchable()).orElseGet(s));
		}
		
		
		
	}
	public static interface MTuple2<T1,T2> extends MatchableTuple2<Object,T1,T2>{
		default <U1> MTuple2<U1, T2> map1(Function1<? super T1, ? extends U1> function) {
	        return ()->getMatchable().map1(function);
	    }
		
	   default <U2> MTuple2<T1, U2> map2(Function1<? super T2, ? extends U2> function) {
	        return ()->getMatchable().map2(function);
	    }
	}
	public static interface MatchableTuple2<TYPE,T1 extends TYPE,T2 extends TYPE> extends Iterable<TYPE> {
		Tuple2<T1,T2> getMatchable();
		
		default T1 v1(){
			return getMatchable().v1;
		}
		default T2 v2(){
			
			return getMatchable().v2;
		}
		default int compareTo(MatchableTuple2<?,T1, T2> other) {
			return getMatchable().compareTo(other.getMatchable());
		}
		

	    
		
		default  Iterator<TYPE> iterator(){
			return Arrays.asList(getMatchable().v1,getMatchable().v2).iterator();
		}
		default <R> R visit(BiFunction<? super Maybe<T1>,? super Maybe<T2>,? extends R> match ){
			@SuppressWarnings("unchecked")
			Tuple2<T1,T2> it = (Tuple2<T1,T2>)getMatchable();
			return  match.apply(Maybe.ofNullable(it.v1), Maybe.ofNullable(it.v2));
		}	
		@SuppressWarnings({ "rawtypes", "unchecked" })
		default <R> Eval<R>  matches(Function<CheckValue2<T1,T2,R>,CheckValue2<T1,T2,R>> fn1,Supplier<R> otherwise){
			return Eval.later(()-> (R)new MatchingInstance(new MatchableCase( fn1.apply( (CheckValue2)
					new MatchableCase(new PatternMatcher()).withType2(getMatchable().getClass())).getPatternMatcher()))
						.match(getMatchable()).orElseGet(otherwise));
		}
		@SuppressWarnings({ "rawtypes", "unchecked" })
		default <R> Maybe<R>  mayMatch(Function<CheckValue2<T1, T2,R>,CheckValue2<T1,T2,R>> fn1){
			return new MatchingInstance(new MatchableCase( fn1.apply( (CheckValue2)
					new MatchableCase(new PatternMatcher()).withType2(getMatchable().getClass())).getPatternMatcher()))
						.match(getMatchable());
		}
		
		default MatchableTuple1<T1> on$1_(){
			Tuple2<T1,T2> it = (Tuple2<T1,T2>)getMatchable();
			return ()->new Tuple1<T1>(it.v1);
		}
		default MatchableTuple1<T2> on$_2(){
			Tuple2<T1,T2> it = (Tuple2<T1,T2>)getMatchable();
			return ()->new Tuple1<T2>(it.v2);
		}
		
	}
	@FunctionalInterface
	public static interface MTuple3<T1,T2,T3> extends MatchableTuple3<Object,T1,T2,T3>{
		
	   default <U1> MTuple3<U1, T2, T3> map1(Function1<? super T1, ? extends U1> function) {
	        return ()->getMatchable().map1(function);
	    }

	    
	   default <U2> MTuple3<T1, U2, T3> map2(Function1<? super T2, ? extends U2> function) {
	    	 return ()->getMatchable().map2(function);
	    }

	    
	   default <U3> MTuple3<T1, T2, U3> map3(Function1<? super T3, ? extends U3> function) {
		   return ()->getMatchable().map3(function);
	    }
	}
	@FunctionalInterface
	public static interface MatchableTuple3<TYPE,T1 extends TYPE,T2 extends TYPE,T3 extends TYPE> {
		Tuple3<T1,T2,T3> getMatchable();
		default T1 v1(){
			return getMatchable().v1;
		}
		default T2 v2(){
			return getMatchable().v2;
		}
		default T3 v3(){
			return getMatchable().v3;
		}
		default int compareTo(MatchableTuple3<?,T1, T2,T3> other) {
			
			return getMatchable().compareTo(other.getMatchable());
		}
		default  Iterator<TYPE> iterator(){
			return Arrays.asList(v1(),v2(),v3()).iterator();
		}
		
		default <R> Eval<R> visit(TriFunction<? super Maybe<T1>,? super Maybe<T2>,? super Maybe<T3>,? extends R> match ){
			@SuppressWarnings("unchecked")
			Tuple3<T1,T2,T3> it = (Tuple3<T1,T2,T3>)getMatchable();
			return  Eval.later(()->match.apply(Maybe.ofNullable(it.v1), Maybe.ofNullable(it.v2),Maybe.ofNullable(it.v3)));
		}	
		@SuppressWarnings({ "rawtypes", "unchecked" })
		default <R> Eval<R>  matches(Function<CheckValue3<T1,T2,T3,R>,CheckValue3<T1,T2,T3,R>> fn1,Supplier<R> otherwise){
			return Eval.later(()-> (R)new MatchingInstance(new MatchableCase( fn1.apply( (CheckValue3)
					new MatchableCase(new PatternMatcher()).withType3(getMatchable().getClass())).getPatternMatcher()))
						.match(getMatchable()).orElseGet(otherwise));
		}
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		default <R> Maybe<R>  mayMatch(Function<CheckValue3<T1,T2,T3,R>,CheckValue3<T1, T2,T3,R>> fn1){
			return  new MatchingInstance(new MatchableCase( fn1.apply( (CheckValue3)
					new MatchableCase(new PatternMatcher()).withType3(getMatchable().getClass())).getPatternMatcher()))
						.match(getMatchable());
		}
		
		
		default MatchableTuple1<T1> on$1__(){
			Tuple3<T1,T2,T3> it = (Tuple3<T1,T2,T3>)getMatchable();
			return ()->new Tuple1<T1>(it.v1);
		}
		default MatchableTuple1<T2> on$_2_(){
			Tuple3<T1,T2,T3> it = (Tuple3<T1,T2,T3>)getMatchable();
			return ()->new Tuple1<T2>(it.v2);
		}
		default MatchableTuple1<T3> on$__3(){
			Tuple3<T1,T2,T3> it = (Tuple3<T1,T2,T3>)getMatchable();
			return ()->new Tuple1<T3>(it.v3);
		}
		default MatchableTuple2<TYPE,T1,T2> on$12_(){
			Tuple3<T1,T2,T3> it = (Tuple3<T1,T2,T3>)getMatchable();
			return ()->new Tuple2<T1,T2>(it.v1,it.v2);
		}
		default MatchableTuple2<TYPE,T1,T3> on$1_3(){
			Tuple3<T1,T2,T3> it = (Tuple3<T1,T2,T3>)getMatchable();
			return ()->new Tuple2<T1,T3>(it.v1,it.v3);
		}
		default MatchableTuple2<TYPE,T2,T3> on$_23(){
			Tuple3<T1,T2,T3> it = (Tuple3<T1,T2,T3>)getMatchable();
			return ()->new Tuple2<T2,T3>(it.v2,it.v3);
		}
	}
	public static interface MTuple4<T1,T2,T3,T4> extends MatchableTuple4<Object,T1,T2,T3,T4>{
		 default <U1> MTuple4<U1, T2, T3, T4> map1(Function1<? super T1, ? extends U1> function) {
		        return ()->getMatchable().map1(function);
		    }

		 default <U2> MTuple4<T1, U2, T3, T4> map2(Function1<? super T2, ? extends U2> function) {
			 return ()->getMatchable().map2(function);
		    }

		    
		 default <U3> MTuple4<T1, T2, U3, T4> map3(Function1<? super T3, ? extends U3> function) {
			 return ()->getMatchable().map3(function);
		    }

		   
		 default <U4> MTuple4<T1, T2, T3, U4> map4(Function1<? super T4, ? extends U4> function) {
			 return ()->getMatchable().map4(function);
		    }		
	}
	public static interface MatchableTuple4<TYPE,T1 extends TYPE,T2 extends TYPE,T3 extends TYPE,T4 extends TYPE> extends Iterable<TYPE>{
		Tuple4<T1,T2,T3,T4> getMatchable();
		default T1 v1(){
			return getMatchable().v1;
		}
		default T2 v2(){
			return getMatchable().v2;
		}
		default T3 v3(){
			return getMatchable().v3;
		}
		default T4 v4(){
			return getMatchable().v4;
		}
		default int compareTo(MatchableTuple4<?,T1, T2,T3,T4> other) {
			return getMatchable().compareTo(other.getMatchable());
		}
		
		default Iterator<TYPE> iterator(){
			return Arrays.asList(v1(),v2(),v3(),v4()).iterator();
		}
		default <R> Eval<R> visit(QuadFunction<? super Maybe<T1>,? super Maybe<T2>,? super Maybe<T3>,? super Maybe<T4>,? extends R> match ){
			@SuppressWarnings("unchecked")
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return  Eval.later(()->match.apply(Maybe.ofNullable(it.v1), Maybe.ofNullable(it.v2),Maybe.ofNullable(it.v3),Maybe.ofNullable(it.v4)));
		}
		@SuppressWarnings({ "rawtypes", "unchecked" })
		default <R> Eval<R>  matches(Function<CheckValue4<T1,T2,T3,T4,R>,CheckValue4<T1,T2,T3,T4,R>> fn1,Supplier<R> otherwise){
			return Eval.later(()-> (R)new MatchingInstance(new MatchableCase( fn1.apply( (CheckValue4)
					new MatchableCase(new PatternMatcher()).withType4(getMatchable().getClass())).getPatternMatcher()))
						.match(getMatchable()).orElseGet(otherwise));
		}
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		default <R> Maybe<R>  mayMatch(Function<CheckValue4<T1,T2, T3,T4,R>,CheckValue4<T1,T2,T3,T4,R>> fn1){
			return  new MatchingInstance(new MatchableCase( fn1.apply( (CheckValue4)
					new MatchableCase(new PatternMatcher()).withType4(getMatchable().getClass())).getPatternMatcher()))
						.match(getMatchable());
		}
		default MatchableTuple1<T1> on$1___(){
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return ()->new Tuple1<T1>(it.v1);
		}
		default MatchableTuple1<T2> on$_2__(){
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return ()->new Tuple1<T2>(it.v2);
		}
		default MatchableTuple1<T3> on$__3_(){
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return ()->new Tuple1<T3>(it.v3);
		}
		default MatchableTuple1<T4> on$___4(){
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return ()->new Tuple1<T4>(it.v4);
		}
		default MatchableTuple2<TYPE,T1,T2> on$12__(){
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return ()->new Tuple2<T1,T2>(it.v1,it.v2);
		}
		default MatchableTuple2<TYPE,T1,T3> on$1_3_(){
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return ()->new Tuple2<T1,T3>(it.v1,it.v3);
		}
		default MatchableTuple2<TYPE,T1,T4> on$1__4(){
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return ()->new Tuple2<T1,T4>(it.v1,it.v4);
		}
		default MatchableTuple2<TYPE,T2,T3> on$_23_(){
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return ()->new Tuple2<T2,T3>(it.v2,it.v3);
		}
		default MatchableTuple2<TYPE,T2,T4> on$_2_4(){
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return ()->new Tuple2<T2,T4>(it.v2,it.v4);
		}
		default MatchableTuple2<TYPE,T3,T4> on$__34(){
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return ()->new Tuple2<T3,T4>(it.v3,it.v4);
		}
		
		default MatchableTuple3<TYPE,T1,T2,T3> on$123_(){
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return ()->new Tuple3<T1,T2,T3>(it.v1,it.v2,it.v3);
		}
		default MatchableTuple3<TYPE,T1,T2,T4> on$12_4(){
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return ()->new Tuple3<T1,T2,T4>(it.v1,it.v2,it.v4);
		}
		default MatchableTuple3<TYPE,T1,T3,T4> on$1_34(){
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return ()->new Tuple3<T1,T3,T4>(it.v1,it.v3,it.v4);
		}
	
		default MatchableTuple3<TYPE,T2,T3,T4> on$_234(){
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return ()->new Tuple3<T2,T3,T4>(it.v2,it.v3,it.v4);
		}

		
		
	}
	public static interface MTuple5<T1,T2,T3,T4,T5> extends MatchableTuple5<Object,T1,T2,T3,T4,T5>{
		
	    default <U1> MTuple5<U1, T2, T3, T4, T5> map1(Function1<? super T1, ? extends U1> function) {
	        return ()-> getMatchable().map1(function);
	    }

	   
	    default <U2> MTuple5<T1, U2, T3, T4, T5> map2(Function1<? super T2, ? extends U2> function) {
	        return  ()-> getMatchable().map2(function);
	    }

	    default  <U3> MTuple5<T1, T2, U3, T4, T5> map3(Function1<? super T3, ? extends U3> function) {
	        return  ()-> getMatchable().map3(function);
	    }

	    default  <U4> MTuple5<T1, T2, T3, U4, T5> map4(Function1<? super T4, ? extends U4> function) {
	        return ()-> getMatchable().map4(function);
	    }

	    default  <U5> MTuple5<T1, T2, T3, T4, U5> map5(Function1<? super T5, ? extends U5> function) {
	        return  ()-> getMatchable().map5(function);
	    }
	}
	public static interface MatchableTuple5<TYPE,T1 extends TYPE,T2 extends TYPE,T3 extends TYPE,T4 extends TYPE,T5 extends TYPE> {
		Tuple5<T1,T2,T3,T4,T5> getMatchable();
		default T1 v1(){
			return getMatchable().v1;
		}
		default T2 v2(){
			return getMatchable().v2;
		}
		default T3 v3(){
			return getMatchable().v3;
		}
		default T4 v4(){
			return getMatchable().v4;
		}
		default T5 v5(){
			return getMatchable().v5;
		}
		default int compareTo(MatchableTuple5<?,T1, T2,T3,T4,T5> other) {
			
			return getMatchable().compareTo(other.getMatchable());
		}
		default <R> Eval<R> visit(QuintFunction<? super Maybe<T1>,? super Maybe<T2>,? super Maybe<T3>,? super Maybe<T4>,? super Maybe<T5>,? extends R> match ){
			@SuppressWarnings("unchecked")
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return  Eval.later(()->match.apply(Maybe.ofNullable(it.v1), Maybe.ofNullable(it.v2),Maybe.ofNullable(it.v3),Maybe.ofNullable(it.v4),Maybe.ofNullable(it.v5)));
		}	
		@SuppressWarnings({ "rawtypes", "unchecked" })
		default <R> Eval<R>  matches(Function<CheckValue5<T1,T2,T3,T4,T5,R>,CheckValue5<T1,T2,T3,T4,T5,R>> fn1,Supplier<R> otherwise){
			return Eval.later(()-> (R)new MatchingInstance(new MatchableCase( fn1.apply( (CheckValue5)
					new MatchableCase(new PatternMatcher()).withType5(getMatchable().getClass())).getPatternMatcher()))
						.match(getMatchable()).orElseGet(otherwise));
		}
		@SuppressWarnings({ "rawtypes", "unchecked" })
		default <R> Maybe<R>  mayMatch(Function<CheckValue5<? super T1,? super T2,? super T3,? super T4,? super T5,R>,CheckValue5<? super T1,? super T2,? super T3,? super T4,? super T5,R>> fn1){
			return  new MatchingInstance(new MatchableCase( fn1.apply( (CheckValue5)
					new MatchableCase(new PatternMatcher()).withType5(getMatchable().getClass())).getPatternMatcher()))
						.match(getMatchable());
		}
		default MatchableTuple1<T1> on$1____(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple1<T1>(it.v1);
		}
		default MatchableTuple1<T2> on$_2___(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple1<T2>(it.v2);
		}
		default MatchableTuple1<T3> on$__3__(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple1<T3>(it.v3);
		}
		default MatchableTuple1<T4> on$___4_(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple1<T4>(it.v4);
		}
		default MatchableTuple1<T5> on$____5(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple1<T5>(it.v5);
		}
		default MatchableTuple2<TYPE,T1,T2> on$12___(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple2<T1,T2>(it.v1,it.v2);
		}
		default MatchableTuple2<TYPE,T1,T3> on$1_3__(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple2<T1,T3>(it.v1,it.v3);
		}
		default MatchableTuple2<TYPE,T1,T4> on$1__4_(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple2<T1,T4>(it.v1,it.v4);
		}
		default MatchableTuple2<TYPE,T1,T5> on$1___5(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple2<T1,T5>(it.v1,it.v5);
		}
		default MatchableTuple2<TYPE,T2,T3> on$_23__(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple2<T2,T3>(it.v2,it.v3);
		}
		default MatchableTuple2<TYPE,T2,T4> on$_2_4_(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple2<T2,T4>(it.v2,it.v4);
		}
		default MatchableTuple2<TYPE,T2,T5> on$_2__5(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple2<T2,T5>(it.v2,it.v5);
		}
		default MatchableTuple2<TYPE,T3,T4> on$__34_(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple2<T3,T4>(it.v3,it.v4);
		}
		default MatchableTuple2<TYPE,T3,T5> on$__3_5(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple2<T3,T5>(it.v3,it.v5);
		}
		default MatchableTuple2<TYPE,T4,T5> on$___45(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple2<T4,T5>(it.v4,it.v5);
		}
		
		default MatchableTuple3<TYPE,T1,T2,T3> on$123__(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple3<T1,T2,T3>(it.v1,it.v2,it.v3);
		}
		default MatchableTuple3<TYPE,T1,T2,T4> on$12_4_(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple3<T1,T2,T4>(it.v1,it.v2,it.v4);
		}
		default MatchableTuple3<TYPE,T1,T2,T5> on$12__5(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple3<T1,T2,T5>(it.v1,it.v2,it.v5);
		}
		default MatchableTuple3<TYPE,T1,T3,T4> on$1_34_(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple3<T1,T3,T4>(it.v1,it.v3,it.v4);
		}
		default MatchableTuple3<TYPE,T1,T3,T5> on$1_3_5(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple3<T1,T3,T5>(it.v1,it.v3,it.v5);
		}
		default MatchableTuple3<TYPE,T1,T4,T5> on$1__45(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple3<T1,T4,T5>(it.v1,it.v4,it.v5);
		}
	
		default MatchableTuple3<TYPE,T2,T3,T4> on$_234_(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple3<T2,T3,T4>(it.v2,it.v3,it.v4);
		}
		default MatchableTuple3<TYPE,T2,T3,T5> on$_23_5(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple3<T2,T3,T5>(it.v2,it.v3,it.v5);
		}
		default MatchableTuple3<TYPE,T3,T4,T5> on$__345(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple3<T3,T4,T5>(it.v3,it.v4,it.v5);
		}
		default MatchableTuple4<TYPE,T1,T2,T3,T4> on$1234_(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple4<T1,T2,T3,T4>(it.v1,it.v2,it.v3,it.v4);
		}
		default MatchableTuple4<TYPE,T1,T2,T3,T5> on$123_5(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple4<T1,T2,T3,T5>(it.v1,it.v2,it.v3,it.v5);
		}
		default MatchableTuple4<TYPE,T1,T2,T4,T5> on$12_45(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple4<T1,T2,T4,T5>(it.v1,it.v2,it.v4,it.v5);
		}
		default MatchableTuple4<TYPE,T1,T3,T4,T5> on$1_345(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple4<T1,T3,T4,T5>(it.v1,it.v3,it.v4,it.v5);
		}
		default MatchableTuple4<TYPE,T2,T3,T4,T5> on$_2345(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple4<T2,T3,T4,T5>(it.v2,it.v3,it.v4,it.v5);
		}
	}
	public class AsMatchable {
		
		
		/**
		 * Coerce / wrap an Object as a Matchable instance
		 * This adds match / _match methods for pattern matching against the object
		 * 
		 * @param toCoerce Object to convert into a Matchable
		 * @return Matchable that adds functionality to the supplied object
		 */
		public static  Matchable asMatchable(Object toCoerce){
			return new CoercedMatchable(toCoerce);
		}
		
		@AllArgsConstructor
		public static class CoercedMatchable<T> implements Matchable{
			private final Object matchable;

			@Override
			public Object getMatchable(){
				return matchable;
			}
			
		}
	}
	@AllArgsConstructor(access=AccessLevel.PUBLIC)
	public static class CheckValue1<T,R> {
		private final Class<T> clazz;
		protected final MatchableCase<R> simplerCase;

		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public final CheckValue1<T,R> is(Tuple1<Predicate<? super T>> values,Supplier<? extends R> result) {		
			return isWhere(result,values.v1);
		}
		
		 @SuppressWarnings({ "rawtypes", "unchecked" })
		private final  CheckValue1<T,R> isWhere(Supplier<? extends R> result,Predicate<? super T> value){
			Predicate predicate = it -> Optional.of(it)
					.map(v -> v.getClass().isAssignableFrom(clazz))
					.orElse(false);
			// add wildcard support
			
			Predicate<T>[] predicates = ReactiveSeq.of(value)
					.map(nextValue -> simplerCase.convertToPredicate(nextValue)).toListX().plus(i->SeqUtils.EMPTY==i)
					.toArray(new Predicate[0]);

		
			return new CheckValue1(clazz,new MatchableCase(this.getPatternMatcher().inCaseOfManyType(predicate,i->result.get(),
					predicates)));
		}
		
		
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public final <V> CheckValue1<T,R> isEmpty(Supplier<? extends R> result) {

			Predicate predicate = it -> Maybe.ofNullable(it)
					.map(v -> v.getClass().isAssignableFrom(clazz))
					.orElse(false);
			// add wildcard support
			
			Predicate<V>[] predicates = new Predicate[]{i->i==SeqUtils.EMPTY};

			return new CheckValue1(clazz,new MatchableCase(this.getPatternMatcher().inCaseOfManyType(predicate,i->result.get(),
					predicates)));
		}


		PatternMatcher getPatternMatcher() {
			return simplerCase.getPatternMatcher();
		}
	}
	@AllArgsConstructor(access=AccessLevel.PUBLIC)
	public static class CheckValue2<T1,T2,R> {
		private final Class<Tuple2<T1,T2>> clazz;
		protected final MatchableCase<R> simplerCase;

		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public final CheckValue2<T1,T2,R> is(MTuple2<Predicate<? super T1>,Predicate<? super T2>> values,Supplier<? extends R> result) {		
			return isWhere(result,values.v1(),values.v2());
		}
		
		 @SuppressWarnings({ "rawtypes", "unchecked" })
		private final  CheckValue2<T1,T2,R> isWhere(Supplier<? extends R> result,Predicate<? super T1> value1,Predicate<? super T2> value2){
			Predicate predicate = it -> Optional.of(it)
					.map(v -> v.getClass().isAssignableFrom(clazz))
					.orElse(false);
			// add wildcard support
			
			Predicate[] predicates = ReactiveSeq.of(value1,value2)
					.map(nextValue -> simplerCase.convertToPredicate(nextValue)).toListX().plus(i->SeqUtils.EMPTY==i)
					.toArray(new Predicate[0]);

		
			return new CheckValue2(clazz,new MatchableCase(this.getPatternMatcher().inCaseOfManyType(predicate,i->result.get(),
					predicates)));
		}
		
		
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public final CheckValue2<T1,T2,R> isEmpty(Supplier<? extends R> result) {

			Predicate predicate = it -> Maybe.ofNullable(it)
					.map(v -> v.getClass().isAssignableFrom(clazz))
					.orElse(false);
			// add wildcard support
			
			Predicate[] predicates = new Predicate[]{i->i==SeqUtils.EMPTY};

			return new CheckValue2(clazz,new MatchableCase(this.getPatternMatcher().inCaseOfManyType(predicate,i->result.get(),
					predicates)));
		}


		PatternMatcher getPatternMatcher() {
			return simplerCase.getPatternMatcher();
		}
	}
	@AllArgsConstructor(access=AccessLevel.PUBLIC)
	public static class CheckValue3<T1,T2,T3,R> {
		private final Class<Tuple3<T1,T2,T3>> clazz;
		protected final MatchableCase<R> simplerCase;

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public final CheckValue3<T1,T2,T3,R> is(MTuple3<Predicate<? super T1>,Predicate<? super T2>,Predicate<? super T3>> values,Supplier<? extends R> result) {		
			return isWhere(result,values.v1(),values.v2(),values.v3());
		}
		
		 @SuppressWarnings({ "rawtypes", "unchecked" })
		private final  CheckValue3<T1,T2,T3,R> isWhere(Supplier<? extends R> result,Predicate<? super T1> value1,Predicate<? super T2> value2,Predicate<? super T3> value3){
			Predicate predicate = it -> Optional.of(it)
					.map(v -> v.getClass().isAssignableFrom(clazz))
					.orElse(false);
			// add wildcard support
			
			Predicate[] predicates = ReactiveSeq.of(value1,value2,value3)
					.map(nextValue -> simplerCase.convertToPredicate(nextValue)).toListX().plus(i->SeqUtils.EMPTY==i)
					.toArray(new Predicate[0]);

		
			return new CheckValue3(clazz,new MatchableCase(this.getPatternMatcher().inCaseOfManyType(predicate,i->result.get(),
					predicates)));
		}
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public final CheckValue3<T1,T2,T3,R> isEmpty(Supplier<? extends R> result) {

			Predicate predicate = it -> Maybe.ofNullable(it)
					.map(v -> v.getClass().isAssignableFrom(clazz))
					.orElse(false);
			// add wildcard support
			
			Predicate[] predicates = new Predicate[]{i->i==SeqUtils.EMPTY};

			return new CheckValue3(clazz,new MatchableCase(this.getPatternMatcher().inCaseOfManyType(predicate,i->result.get(),
					predicates)));
		}


		PatternMatcher getPatternMatcher() {
			return simplerCase.getPatternMatcher();
		}
	}
	@AllArgsConstructor(access=AccessLevel.PUBLIC)
	public static class CheckValue4<T1,T2,T3,T4,R> {
		private final Class<Tuple4<T1,T2,T3,T4>> clazz;
		protected final MatchableCase<R> simplerCase;

		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public final CheckValue4<T1,T2,T3,T4,R> is(Tuple4<Predicate<? super T1>,Predicate<? super T2>,Predicate<? super T3>,Predicate<? super T4>> values,Supplier<? extends R> result) {		
			return isWhere(result,values.v1,values.v2,values.v3,values.v4);
		}
		 @SuppressWarnings({ "rawtypes", "unchecked" })
		private final  CheckValue4<T1,T2,T3,T4,R> isWhere(Supplier<? extends R> result,Predicate<? super T1> value1,
																								Predicate<? super T2> value2,
																								Predicate<? super T3> value3,
																								Predicate<? super T4> value4){
			Predicate predicate = it -> Optional.of(it)
					.map(v -> v.getClass().isAssignableFrom(clazz))
					.orElse(false);
			// add wildcard support
			
			Predicate[] predicates = ReactiveSeq.of(value1,value2,value3,value4)
					.map(nextValue -> simplerCase.convertToPredicate(nextValue)).toListX().plus(i->SeqUtils.EMPTY==i)
					.toArray(new Predicate[0]);

		
			return new CheckValue4(clazz,new MatchableCase(this.getPatternMatcher().inCaseOfManyType(predicate,i->result.get(),
					predicates)));
		}
		
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public final CheckValue4<T1,T2,T3,T4,R> isEmpty(Supplier<? extends R> result) {

			Predicate predicate = it -> Maybe.ofNullable(it)
					.map(v -> v.getClass().isAssignableFrom(clazz))
					.orElse(false);
			// add wildcard support
			
			Predicate[] predicates = new Predicate[]{i->i==SeqUtils.EMPTY};

			return new CheckValue4(clazz,new MatchableCase(this.getPatternMatcher().inCaseOfManyType(predicate,i->result.get(),
					predicates)));
		}


		PatternMatcher getPatternMatcher() {
			return simplerCase.getPatternMatcher();
		}
	}
	@AllArgsConstructor(access=AccessLevel.PUBLIC)
	public static class CheckValue5<T1,T2,T3,T4,T5,R> {
		private final Class<Tuple5<T1,T2,T3,T4,T5>> clazz;
		protected final MatchableCase<R> simplerCase;

		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public final CheckValue5<T1,T2,T3,T4,T5,R> is(Tuple5<Predicate<? super T1>,Predicate<? super T2>,
																Predicate<? super T3>,Predicate<? super T4>,
																Predicate<? super T5>> values,Supplier<? extends R> result) {		
			return isWhere(result,values.v1,values.v2,values.v3,values.v4,values.v5);
		}
		 @SuppressWarnings({ "rawtypes", "unchecked" })
		private final  CheckValue5<T1,T2,T3,T4,T5,R> isWhere(Supplier<? extends R> result,Predicate<? super T1> value1,
																								Predicate<? super T2> value2,
																								Predicate<? super T3> value3,
																								Predicate<? super T4> value4,
																								Predicate<? super T5> value5){
			Predicate predicate = it -> Optional.of(it)
					.map(v -> v.getClass().isAssignableFrom(clazz))
					.orElse(false);
			// add wildcard support
			
			Predicate[] predicates = ReactiveSeq.of(value1,value2,value3,value4)
					.map(nextValue -> simplerCase.convertToPredicate(nextValue)).toListX().plus(i->SeqUtils.EMPTY==i)
					.toArray(new Predicate[0]);

		
			return new CheckValue5(clazz,new MatchableCase(this.getPatternMatcher().inCaseOfManyType(predicate,i->result.get(),
					predicates)));
		}
		
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public final CheckValue4<T1,T2,T3,T4,R> isEmpty(Supplier<? extends R> result) {

			Predicate predicate = it -> Maybe.ofNullable(it)
					.map(v -> v.getClass().isAssignableFrom(clazz))
					.orElse(false);
			// add wildcard support
			
			Predicate[] predicates = new Predicate[]{i->i==SeqUtils.EMPTY};

			return new CheckValue4(clazz,new MatchableCase(this.getPatternMatcher().inCaseOfManyType(predicate,i->result.get(),
					predicates)));
		}


		PatternMatcher getPatternMatcher() {
			return simplerCase.getPatternMatcher();
		}
	}
	@AllArgsConstructor(access=AccessLevel.PUBLIC)
	public static class CheckValues<T,R> {
		private final Class<T> clazz;
		protected final MatchableCase<R> simplerCase;

		
		
		public final CheckValues<T,R> is(Iterable<Predicate<? super T>> values,Supplier<? extends R> result) {		
			return isWhere(result,values);
		}
		public final CheckValues<T,R> is(MTuple1<Predicate<? super T>> values,Supplier<? extends R> result) {		
			return isWhere(result,(Iterable)values);
		}
		public final CheckValues<T,R> is(MTuple2<Predicate<? super T>,Predicate<? super T>> values,Supplier<? extends R> result) {		
			return isWhere(result,(Iterable)values);
		}
		public final CheckValues<T,R> is(MTuple3<Predicate<? super T>,Predicate<? super T>,Predicate<? super T>> values,Supplier<? extends R> result) {		
			return isWhere(result,(Iterable)values);
		}
		public final CheckValues<T,R> is(MTuple4<Predicate<? super T>,Predicate<? super T>,Predicate<? super T>,Predicate<? super T>> values,Supplier<? extends R> result) {		
			return isWhere(result,(Iterable)values);
		}
		public final CheckValues<T,R> is(MTuple5<Predicate<? super T>,Predicate<? super T>,Predicate<? super T>,Predicate<? super T>,Predicate<? super T>> values,Supplier<? extends R> result) {		
			return isWhere(result,(Iterable)values);
		}
		
		public final CheckValues<T,R> has(Iterable<Predicate<? super T>> values,Supplier<? extends R> result) {		
			return hasWhere(result,values);
		}
		public final CheckValues<T,R> has(MTuple1<Predicate<? super T>> values,Supplier<? extends R> result) {		
			return hasWhere(result,(Iterable)values);
		}
		public final CheckValues<T,R> has(MTuple2<Predicate<? super T>,Predicate<? super T>> values,Supplier<? extends R> result) {		
			return hasWhere(result,(Iterable)values);
		}
		public final CheckValues<T,R> has(MTuple3<Predicate<? super T>,Predicate<? super T>,Predicate<? super T>> values,Supplier<? extends R> result) {		
			return hasWhere(result,(Iterable)values);
		}
		public final CheckValues<T,R> has(MTuple4<Predicate<? super T>,Predicate<? super T>,Predicate<? super T>,Predicate<? super T>> values,Supplier<? extends R> result) {		
			return hasWhere(result,(Iterable)values);
		}
		public final CheckValues<T,R> has(MTuple5<Predicate<? super T>,Predicate<? super T>,Predicate<? super T>,Predicate<? super T>,Predicate<? super T>> values,Supplier<? extends R> result) {		
			return hasWhere(result,(Iterable)values);
		}
		
		
		 @SuppressWarnings({ "rawtypes", "unchecked" })
		private final  CheckValues<T,R> isWhere(Supplier<? extends R> result,Iterable<Predicate<? super T>> values){
			Predicate predicate = it -> Optional.of(it)
					.map(v -> v.getClass().isAssignableFrom(clazz))
					.orElse(false);
			// add wildcard support
			
			Predicate<T>[] predicates = ReactiveSeq.fromIterable(values)
					.map(nextValue -> simplerCase.convertToPredicate(nextValue)).toListX().plus(i->SeqUtils.EMPTY==i)
					.toArray(new Predicate[0]);

		
			return new MatchableCase(this.getPatternMatcher().inCaseOfManyType(predicate, i->result.get(),
					predicates)).withType(clazz);
		}
		 @SuppressWarnings({ "rawtypes", "unchecked" })
		public final <V> CheckValues<T,R> hasWhere(Supplier<? extends R> result,Iterable<Predicate<? super T>> values) {
		
			Predicate predicate = it -> Optional.of(it)
					.map(v -> v.getClass().isAssignableFrom(clazz))
					.orElse(false);
			// add wildcard support
			
			Predicate<V>[] predicates = ReactiveSeq.fromIterable(values)
					.map(nextValue -> simplerCase.convertToPredicate(nextValue)).toList()
					.toArray(new Predicate[0]);

			return new MatchableCase(this.getPatternMatcher().inCaseOfManyType(predicate, i->result.get(),
					predicates)).withType(clazz);
		}
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public final <V> CheckValues<T,R> isEmpty(Supplier<? extends R> result) {

			Predicate predicate = it -> Maybe.ofNullable(it)
					.map(v -> v.getClass().isAssignableFrom(clazz))
					.orElse(false);
			// add wildcard support
			
			Predicate<V>[] predicates = new Predicate[]{i->i==SeqUtils.EMPTY};

			return new MatchableCase(this.getPatternMatcher().inCaseOfManyType(predicate, i->result.get(),
					predicates)).withType(clazz);
		}


		PatternMatcher getPatternMatcher() {
			return simplerCase.getPatternMatcher();
		}
	}
	
}
