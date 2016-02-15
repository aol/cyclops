package com.aol.cyclops.control;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hamcrest.Matcher;
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
import com.aol.cyclops.util.function.Predicates;
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
	
	public static <T,R> Function<? super T,? extends R> Then(Function<? super T,? extends R> fn){
		return fn;
	}
	//when arity 1
	public static <T1> Tuple1<Predicate<? super T1>> When(T1 t1){
		
		return Tuple.tuple(test -> Objects.equals(test,t1));
	}
	public static <T1,T2,T3> Tuple1<Predicate<? super T1>> When(Predicate<? super T1> t1){
		
		return Tuple.tuple(t1);
	}
	public static <T1,T2,T3> Tuple1<Predicate<? super T1>> When(Matcher<? super T1> t1){
		return Tuple.tuple(test -> t1.matches(test));
	}
	//when arity 2
	public static <T1,T2> Tuple2<Predicate<? super T1>,Predicate<? super T2>> When(T1 t1,T2 t2){
		
		return Tuple.tuple(test -> Objects.equals(test,t1),test -> Objects.equals(test,t2));
	}
	public static <T1,T2,T3> Tuple2<Predicate<? super T1>,Predicate<? super T2>> When(Predicate<? super T1> t1,Predicate<? super T2> t2){
		
		return Tuple.tuple(t1,t2);
	}
	public static <T1,T2,T3> Tuple2<Predicate<? super T1>,Predicate<? super T2>> When(Matcher<? super T1> t1,Matcher<? super T2> t2){
		
		return Tuple.tuple(test -> t1.matches(test),test -> t2.matches(test));
	}
	//when arity 3
	public static <T1,T2,T3> Tuple3<Predicate<? super T1>,Predicate<? super T2>,Predicate<? super T3>> When(T1 t1,T2 t2,T3 t3){
		
		return Tuple.tuple(test -> Objects.equals(test,t1),test -> Objects.equals(test,t2),test -> Objects.equals(test,t3));
	}
	public static <T1,T2,T3> Tuple3<Predicate<? super T1>,Predicate<? super T2>,Predicate<? super T3>> When(Predicate<? super T1> t1,Predicate<? super T2> t2,Predicate<? super T3> t3){
		
		return Tuple.tuple(t1,t2,t3);
	}
	public static <T1,T2,T3> Tuple3<Predicate<? super T1>,Predicate<? super T2>,Predicate<? super T3>> When(Matcher<? super T1> t1,Matcher<? super T2> t2,Matcher<? super T3> t3){
		
		return Tuple.tuple(test -> t1.matches(test),test -> t2.matches(test),test -> t3.matches(test));
	}
	//when arity 4
	public static <T1,T2,T3,T4> Tuple4<Predicate<? super T1>,Predicate<? super T2>,Predicate<? super T3>,Predicate<? super T4>> When(T1 t1,T2 t2,T3 t3,T4 t4){
		
		return Tuple.tuple(test -> Objects.equals(test,t1),test -> Objects.equals(test,t2),test -> Objects.equals(test,t3),test -> Objects.equals(test,t4));
	}
	public static  <T1,T2,T3,T4> Tuple4<Predicate<? super T1>,Predicate<? super T2>,Predicate<? super T3>,Predicate<? super T4>> When(Predicate<? super T1> t1,Predicate<? super T2> t2,Predicate<? super T3> t3,Predicate<? super T4> t4){
		
		return Tuple.tuple(t1,t2,t3,t4);
	}
	public static  <T1,T2,T3,T4> Tuple4<Predicate<? super T1>,Predicate<? super T2>,Predicate<? super T3>,Predicate<? super T4>> When(Matcher<? super T1> t1,
																						Matcher<? super T2> t2,
																						Matcher<? super T3> t3,
																						Matcher<? super T4> t4){
		
		return Tuple.tuple(test -> t1.matches(test),test -> t2.matches(test),test -> t3.matches(test),test -> t4.matches(test));
	}
	//when arity 5
	public static <T1,T2,T3,T4,T5> Tuple5<Predicate<? super T1>,Predicate<? super T2>,Predicate<? super T3>,Predicate<? super T4>,Predicate<? super T5>> When(T1 t1,T2 t2,T3 t3,T4 t4, T5 t5){
		
		return Tuple.tuple(test -> Objects.equals(test,t1),test -> Objects.equals(test,t2),test -> Objects.equals(test,t3),test -> Objects.equals(test,t4),test -> Objects.equals(test,t5));
	}
	public static  <T1,T2,T3,T4,T5> Tuple5<Predicate<? super T1>,Predicate<? super T2>,Predicate<? super T3>,Predicate<? super T4>,Predicate<? super T5>> When(Predicate<? super T1> t1,Predicate<? super T2> t2,
																							Predicate<? super T3> t3,
																							Predicate<? super T4> t4,
																							Predicate<? super T5> t5){
		
		return Tuple.tuple(t1,t2,t3,t4,t5);
	}
	public static  <T1,T2,T3,T4,T5> Tuple5<Predicate<? super T1>,Predicate<? super T2>,Predicate<? super T3>,Predicate<? super T4>,Predicate<? super T5>> When(Matcher<? super T1> t1,
																						Matcher<? super T2> t2,
																						Matcher<? super T3> t3,
																						Matcher<? super T4> t4,
																						Matcher<? super T5> t5){
		
		return Tuple.tuple(test -> t1.matches(test),test -> t2.matches(test),test -> t3.matches(test),test -> t4.matches(test),test -> t5.matches(test));
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
	default <R> Eval<R>  matches(Function<CheckValues<? super TYPE,R>,CheckValues<? super TYPE, R>> fn1){
		return Eval.later(()->(R) new MatchingInstance(new MatchableCase( fn1.apply( (CheckValues)
				new MatchableCase(new PatternMatcher()).withType(getMatchable().getClass())).getPatternMatcher()))
					.match(getMatchable()).get());
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <R> Maybe<R>  mayMatch(Function<CheckValues<? super TYPE,R>,CheckValues<? super TYPE,R>> fn1){
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
			return ofStream((Stream)o);
		return AsMatchable.asMatchable(o);
	}
	/**
	 * Create a new matchable that will match on the fields of the provided Stream
	 * 
	 * @param o Object to match on it's fields
	 * @return new Matchable
	 */
	public static <T> Matchable<T> ofStream(Stream<T> o){
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
	public static <TYPE, T1 extends TYPE, T2 extends TYPE> MatchableTuple2<T1,T2> from(Supplier<T1> s1, Supplier<T2> s2){
		return ()-> Tuple.tuple(s1.get(),s2.get());
	}
	public static <TYPE, T1 extends TYPE, T2 extends TYPE> MatchableTuple2<T1,T2> from(Tuple2<T1,T2> t2){
		return ()-> t2;
	}
	public static <TYPE, T1 extends TYPE, T2 extends TYPE,T3 extends TYPE> MatchableTuple3<T1,T2,T3> from(Tuple3<T1,T2,T3> t3){
		return ()-> t3;
	}
	public static <TYPE, T1 extends TYPE, T2 extends TYPE,T3 extends TYPE> MatchableTuple3<T1,T2,T3> from(Supplier<T1> s1, Supplier<T2> s2, Supplier<T3> s3){
		return()-> Tuple.tuple(s1.get(),s2.get(),s3.get());
	}
	public static <TYPE, T1 extends TYPE, T2 extends TYPE,T3 extends TYPE,T4 extends TYPE> MatchableTuple4<T1,T2,T3,T4> from(Tuple4<T1,T2,T3,T4> t4){
		return ()-> t4;
	}
	public static <TYPE, T1 extends TYPE, T2 extends TYPE,T3 extends TYPE,T4 extends TYPE> MatchableTuple4<T1,T2,T3,T4> from(Supplier<T1> s1, Supplier<T2> s2, 
												Supplier<T3> s3,Supplier<T4> s4){
		return()-> Tuple.tuple(s1.get(),s2.get(),s3.get(),s4.get());
	}
	public static <TYPE, T1 extends TYPE, T2 extends TYPE,T3 extends TYPE,T4 extends TYPE,T5 extends TYPE> MatchableTuple5<T1,T2,T3,T4,T5> from(Tuple5<T1,T2,T3,T4,T5> t5){
		return ()-> t5;
	}
	public static <TYPE, T1 extends TYPE, T2 extends TYPE,T3 extends TYPE,T4 extends TYPE,T5 extends TYPE> MatchableTuple5<T1,T2,T3,T4,T5> from(Supplier<T1> s1, Supplier<T2> s2, 
												Supplier<T3> s3,Supplier<T4> s4,Supplier<T5> s5){
		return()-> Tuple.tuple(s1.get(),s2.get(),s3.get(),s4.get(),s5.get());
	}
	
	public static  MatchableIterable<Character> fromCharSequence(CharSequence chars){
		Iterable<Character> it = ()->chars.chars().boxed().map(i ->Character.toChars(i)[0]).iterator();
		return ()-> it;
	}
	
	public static interface MatchableIterable<TYPE> extends Matchable<TYPE>{
		
		default <R> Eval<R> visit(BiFunction<? super Maybe<TYPE>,? super ReactiveSeq<TYPE>,? extends R> match ){
			@SuppressWarnings("unchecked")
			Iterable<TYPE> it = (Iterable<TYPE>)getMatchable();
			return Eval.later(()->ReactiveSeq.fromIterable(it).visit(match));	
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
		default MatchableTuple2<TYPE,TYPE> on$12___(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple2<TYPE,TYPE>(it.v1,it.v2);
		}
		default MatchableTuple2<TYPE,TYPE> on$1_3__(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple2<TYPE,TYPE>(it.v1,it.v3);
		}
		default MatchableTuple2<TYPE,TYPE> on$1__4_(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple2<TYPE,TYPE>(it.v1,it.v4);
		}
		default MatchableTuple2<TYPE,TYPE> on$1___5(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple2<TYPE,TYPE>(it.v1,it.v5);
		}
		default MatchableTuple2<TYPE,TYPE> on$_23__(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple2<TYPE,TYPE>(it.v2,it.v3);
		}
		default MatchableTuple2<TYPE,TYPE> on$_2_4_(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple2<TYPE,TYPE>(it.v2,it.v4);
		}
		default MatchableTuple2<TYPE,TYPE> on$_2__5(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple2<TYPE,TYPE>(it.v2,it.v5);
		}
		default MatchableTuple2<TYPE,TYPE> on$__34_(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple2<TYPE,TYPE>(it.v3,it.v4);
		}
		default MatchableTuple2<TYPE,TYPE> on$__3_5(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple2<TYPE,TYPE>(it.v3,it.v5);
		}
		default MatchableTuple2<TYPE,TYPE> on$___45(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple2<TYPE,TYPE>(it.v4,it.v5);
		}
		
		default MatchableTuple3<TYPE,TYPE,TYPE> on$123__(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple3<TYPE,TYPE,TYPE>(it.v1,it.v2,it.v3);
		}
		default MatchableTuple3<TYPE,TYPE,TYPE> on$12_4_(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple3<TYPE,TYPE,TYPE>(it.v1,it.v2,it.v4);
		}
		default MatchableTuple3<TYPE,TYPE,TYPE> on$12__5(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple3<TYPE,TYPE,TYPE>(it.v1,it.v2,it.v5);
		}
		default MatchableTuple3<TYPE,TYPE,TYPE> on$1_34_(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple3<TYPE,TYPE,TYPE>(it.v1,it.v3,it.v4);
		}
		default MatchableTuple3<TYPE,TYPE,TYPE> on$1_3_5(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple3<TYPE,TYPE,TYPE>(it.v1,it.v3,it.v5);
		}
		default MatchableTuple3<TYPE,TYPE,TYPE> on$1__45(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple3<TYPE,TYPE,TYPE>(it.v1,it.v4,it.v5);
		}
	
		default MatchableTuple3<TYPE,TYPE,TYPE> on$_234_(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple3<TYPE,TYPE,TYPE>(it.v2,it.v3,it.v4);
		}
		default MatchableTuple3<TYPE,TYPE,TYPE> on$_23_5(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple3<TYPE,TYPE,TYPE>(it.v2,it.v3,it.v5);
		}
		default MatchableTuple3<TYPE,TYPE,TYPE> on$__345(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple3<TYPE,TYPE,TYPE>(it.v3,it.v4,it.v5);
		}
		default MatchableTuple4<TYPE,TYPE,TYPE,TYPE> on$1234_(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple4<TYPE,TYPE,TYPE,TYPE>(it.v1,it.v2,it.v3,it.v4);
		}
		default MatchableTuple4<TYPE,TYPE,TYPE,TYPE> on$123_5(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple4<TYPE,TYPE,TYPE,TYPE>(it.v1,it.v2,it.v3,it.v5);
		}
		default MatchableTuple4<TYPE,TYPE,TYPE,TYPE> on$12_45(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple4<TYPE,TYPE,TYPE,TYPE>(it.v1,it.v2,it.v4,it.v5);
		}
		default MatchableTuple4<TYPE,TYPE,TYPE,TYPE> on$1_345(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple4<TYPE,TYPE,TYPE,TYPE>(it.v1,it.v3,it.v4,it.v5);
		}
		default MatchableTuple4<TYPE,TYPE,TYPE,TYPE> on$_2345(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple4<TYPE,TYPE,TYPE,TYPE>(it.v2,it.v3,it.v4,it.v5);
		}
		
	}
	public static interface MatchableTuple1<T1>{
		Tuple1<T1> getMatchable();
		default T1 v1(){
			return getMatchable().v1;
		}
		
		default <R> R visit(Function<? super T1,? extends R> some,Supplier<? extends R> none ){
			@SuppressWarnings("unchecked")
			Tuple1<T1> it = (Tuple1<T1>)getMatchable();
			
			return  Maybe.ofNullable(it.v1).visit(some, none);
		}	
		@SuppressWarnings({ "rawtypes", "unchecked" })
		default <R> Maybe<R>  mayMatch(Function<CheckValue1<? super T1,R>,CheckValue1<? super T1,R>> fn1){
			return  new MatchingInstance(new MatchableCase( fn1.apply( (CheckValue1)
					new MatchableCase(new PatternMatcher()).withType1(getMatchable().getClass())).getPatternMatcher()))
						.match(getMatchable());
		}
		
		
		
	}
	
	public static interface MatchableTuple2<T1,T2> {
		Tuple2<T1,T2> getMatchable();
		
		default T1 v1(){
			return getMatchable().v1;
		}
		default T2 v2(){
			return getMatchable().v2;
		}
		
		default <R> R visit(BiFunction<? super Maybe<T1>,? super Maybe<T2>,? extends R> match ){
			@SuppressWarnings("unchecked")
			Tuple2<T1,T2> it = (Tuple2<T1,T2>)getMatchable();
			return  match.apply(Maybe.ofNullable(it.v1), Maybe.ofNullable(it.v2));
		}	
		@SuppressWarnings({ "rawtypes", "unchecked" })
		default <R> Maybe<R>  mayMatch(Function<CheckValue2<? super T1,? super T2,R>,CheckValue2<? super T1,? super T2,R>> fn1){
			return  new MatchingInstance(new MatchableCase( fn1.apply( (CheckValue2)
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
	
	public static interface MatchableTuple3<T1,T2,T3> {
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
		
		default <R> Eval<R> visit(TriFunction<? super Maybe<T1>,? super Maybe<T2>,? super Maybe<T3>,? extends R> match ){
			@SuppressWarnings("unchecked")
			Tuple3<T1,T2,T3> it = (Tuple3<T1,T2,T3>)getMatchable();
			return  Eval.later(()->match.apply(Maybe.ofNullable(it.v1), Maybe.ofNullable(it.v2),Maybe.ofNullable(it.v3)));
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
		default MatchableTuple2<T1,T2> on$12_(){
			Tuple3<T1,T2,T3> it = (Tuple3<T1,T2,T3>)getMatchable();
			return ()->new Tuple2<T1,T2>(it.v1,it.v2);
		}
		default MatchableTuple2<T1,T3> on$1_3(){
			Tuple3<T1,T2,T3> it = (Tuple3<T1,T2,T3>)getMatchable();
			return ()->new Tuple2<T1,T3>(it.v1,it.v3);
		}
		default MatchableTuple2<T2,T3> on$_23(){
			Tuple3<T1,T2,T3> it = (Tuple3<T1,T2,T3>)getMatchable();
			return ()->new Tuple2<T2,T3>(it.v2,it.v3);
		}
	}
	public static interface MatchableTuple4<T1,T2,T3,T4> {
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
		default <R> Eval<R> visit(QuadFunction<? super Maybe<T1>,? super Maybe<T2>,? super Maybe<T3>,? super Maybe<T4>,? extends R> match ){
			@SuppressWarnings("unchecked")
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return  Eval.later(()->match.apply(Maybe.ofNullable(it.v1), Maybe.ofNullable(it.v2),Maybe.ofNullable(it.v3),Maybe.ofNullable(it.v4)));
		}
		@SuppressWarnings({ "rawtypes", "unchecked" })
		default <R> Maybe<R>  mayMatch(Function<CheckValue4<? super T1,? super T2,? super T3,? super T4,R>,CheckValue4<? super T1,? super T2,? super T3,? super T4,R>> fn1){
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
		default MatchableTuple2<T1,T2> on$12__(){
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return ()->new Tuple2<T1,T2>(it.v1,it.v2);
		}
		default MatchableTuple2<T1,T3> on$1_3_(){
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return ()->new Tuple2<T1,T3>(it.v1,it.v3);
		}
		default MatchableTuple2<T1,T4> on$1__4(){
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return ()->new Tuple2<T1,T4>(it.v1,it.v4);
		}
		default MatchableTuple2<T2,T3> on$_23_(){
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return ()->new Tuple2<T2,T3>(it.v2,it.v3);
		}
		default MatchableTuple2<T2,T4> on$_2_4(){
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return ()->new Tuple2<T2,T4>(it.v2,it.v4);
		}
		default MatchableTuple2<T3,T4> on$__34(){
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return ()->new Tuple2<T3,T4>(it.v3,it.v4);
		}
		
		default MatchableTuple3<T1,T2,T3> on$123_(){
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return ()->new Tuple3<T1,T2,T3>(it.v1,it.v2,it.v3);
		}
		default MatchableTuple3<T1,T2,T4> on$12_4(){
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return ()->new Tuple3<T1,T2,T4>(it.v1,it.v2,it.v4);
		}
		default MatchableTuple3<T1,T3,T4> on$1_34(){
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return ()->new Tuple3<T1,T3,T4>(it.v1,it.v3,it.v4);
		}
	
		default MatchableTuple3<T2,T3,T4> on$_234(){
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return ()->new Tuple3<T2,T3,T4>(it.v2,it.v3,it.v4);
		}

		
		
	}
	public static interface MatchableTuple5<T1,T2,T3,T4,T5> {
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
		default <R> Eval<R> visit(QuintFunction<? super Maybe<T1>,? super Maybe<T2>,? super Maybe<T3>,? super Maybe<T4>,? super Maybe<T5>,? extends R> match ){
			@SuppressWarnings("unchecked")
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return  Eval.later(()->match.apply(Maybe.ofNullable(it.v1), Maybe.ofNullable(it.v2),Maybe.ofNullable(it.v3),Maybe.ofNullable(it.v4),Maybe.ofNullable(it.v5)));
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
		default MatchableTuple2<T1,T2> on$12___(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple2<T1,T2>(it.v1,it.v2);
		}
		default MatchableTuple2<T1,T3> on$1_3__(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple2<T1,T3>(it.v1,it.v3);
		}
		default MatchableTuple2<T1,T4> on$1__4_(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple2<T1,T4>(it.v1,it.v4);
		}
		default MatchableTuple2<T1,T5> on$1___5(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple2<T1,T5>(it.v1,it.v5);
		}
		default MatchableTuple2<T2,T3> on$_23__(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple2<T2,T3>(it.v2,it.v3);
		}
		default MatchableTuple2<T2,T4> on$_2_4_(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple2<T2,T4>(it.v2,it.v4);
		}
		default MatchableTuple2<T2,T5> on$_2__5(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple2<T2,T5>(it.v2,it.v5);
		}
		default MatchableTuple2<T3,T4> on$__34_(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple2<T3,T4>(it.v3,it.v4);
		}
		default MatchableTuple2<T3,T5> on$__3_5(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple2<T3,T5>(it.v3,it.v5);
		}
		default MatchableTuple2<T4,T5> on$___45(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple2<T4,T5>(it.v4,it.v5);
		}
		
		default MatchableTuple3<T1,T2,T3> on$123__(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple3<T1,T2,T3>(it.v1,it.v2,it.v3);
		}
		default MatchableTuple3<T1,T2,T4> on$12_4_(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple3<T1,T2,T4>(it.v1,it.v2,it.v4);
		}
		default MatchableTuple3<T1,T2,T5> on$12__5(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple3<T1,T2,T5>(it.v1,it.v2,it.v5);
		}
		default MatchableTuple3<T1,T3,T4> on$1_34_(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple3<T1,T3,T4>(it.v1,it.v3,it.v4);
		}
		default MatchableTuple3<T1,T3,T5> on$1_3_5(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple3<T1,T3,T5>(it.v1,it.v3,it.v5);
		}
		default MatchableTuple3<T1,T4,T5> on$1__45(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple3<T1,T4,T5>(it.v1,it.v4,it.v5);
		}
	
		default MatchableTuple3<T2,T3,T4> on$_234_(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple3<T2,T3,T4>(it.v2,it.v3,it.v4);
		}
		default MatchableTuple3<T2,T3,T5> on$_23_5(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple3<T2,T3,T5>(it.v2,it.v3,it.v5);
		}
		default MatchableTuple3<T3,T4,T5> on$__345(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple3<T3,T4,T5>(it.v3,it.v4,it.v5);
		}
		default MatchableTuple4<T1,T2,T3,T4> on$1234_(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple4<T1,T2,T3,T4>(it.v1,it.v2,it.v3,it.v4);
		}
		default MatchableTuple4<T1,T2,T3,T5> on$123_5(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple4<T1,T2,T3,T5>(it.v1,it.v2,it.v3,it.v5);
		}
		default MatchableTuple4<T1,T2,T4,T5> on$12_45(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple4<T1,T2,T4,T5>(it.v1,it.v2,it.v4,it.v5);
		}
		default MatchableTuple4<T1,T3,T4,T5> on$1_345(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple4<T1,T3,T4,T5>(it.v1,it.v3,it.v4,it.v5);
		}
		default MatchableTuple4<T2,T3,T4,T5> on$_2345(){
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
		public final CheckValue1<T,R> is(Tuple1<Predicate<? super T>> values,Function<? super Tuple1<T>,? extends R> result) {		
			return isWhere(result,values.v1);
		}
		
		 @SuppressWarnings({ "rawtypes", "unchecked" })
		private final  CheckValue1<T,R> isWhere(Function<? super Tuple1<T>,? extends R> result,Predicate<? super T> value){
			Predicate predicate = it -> Optional.of(it)
					.map(v -> v.getClass().isAssignableFrom(clazz))
					.orElse(false);
			// add wildcard support
			
			Predicate<T>[] predicates = ReactiveSeq.of(value)
					.map(nextValue -> simplerCase.convertToPredicate(nextValue)).toListX().plus(i->SeqUtils.EMPTY==i)
					.toArray(new Predicate[0]);

		
			return new CheckValue1(clazz,new MatchableCase(this.getPatternMatcher().inCaseOfManyType(predicate,result,
					predicates)));
		}
		
		
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public final <V> CheckValue1<T,R> isEmpty(Function<? super T,? extends R> result) {

			Predicate predicate = it -> Maybe.ofNullable(it)
					.map(v -> v.getClass().isAssignableFrom(clazz))
					.orElse(false);
			// add wildcard support
			
			Predicate<V>[] predicates = new Predicate[]{i->i==SeqUtils.EMPTY};

			return new CheckValue1(clazz,new MatchableCase(this.getPatternMatcher().inCaseOfManyType(predicate,result,
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
		public final CheckValue2<T1,T2,R> is(Tuple2<Predicate<? super T1>,Predicate<? super T2>> values,Function<? super Tuple2<T1,T2>,? extends R> result) {		
			return isWhere(result,values.v1,values.v2);
		}
		
		 @SuppressWarnings({ "rawtypes", "unchecked" })
		private final  CheckValue2<T1,T2,R> isWhere(Function<? super Tuple2<T1,T2>,? extends R> result,Predicate<? super T1> value1,Predicate<? super T2> value2){
			Predicate predicate = it -> Optional.of(it)
					.map(v -> v.getClass().isAssignableFrom(clazz))
					.orElse(false);
			// add wildcard support
			
			Predicate[] predicates = ReactiveSeq.of(value1,value2)
					.map(nextValue -> simplerCase.convertToPredicate(nextValue)).toListX().plus(i->SeqUtils.EMPTY==i)
					.toArray(new Predicate[0]);

		
			return new CheckValue2(clazz,new MatchableCase(this.getPatternMatcher().inCaseOfManyType(predicate,result,
					predicates)));
		}
		
		
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public final CheckValue2<T1,T2,R> isEmpty(Function<? super Tuple2<T1,T2>,? extends R> result) {

			Predicate predicate = it -> Maybe.ofNullable(it)
					.map(v -> v.getClass().isAssignableFrom(clazz))
					.orElse(false);
			// add wildcard support
			
			Predicate[] predicates = new Predicate[]{i->i==SeqUtils.EMPTY};

			return new CheckValue2(clazz,new MatchableCase(this.getPatternMatcher().inCaseOfManyType(predicate,result,
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
		public final CheckValue3<T1,T2,T3,R> is(Tuple3<Predicate<? super T1>,Predicate<? super T2>,Predicate<? super T3>> values,Function<? super Tuple3<T1,T2,T3>,? extends R> result) {		
			return isWhere(result,values.v1,values.v2,values.v3);
		}
		
		 @SuppressWarnings({ "rawtypes", "unchecked" })
		private final  CheckValue3<T1,T2,T3,R> isWhere(Function<? super Tuple3<T1,T2,T3>,? extends R> result,Predicate<? super T1> value1,Predicate<? super T2> value2,Predicate<? super T3> value3){
			Predicate predicate = it -> Optional.of(it)
					.map(v -> v.getClass().isAssignableFrom(clazz))
					.orElse(false);
			// add wildcard support
			
			Predicate[] predicates = ReactiveSeq.of(value1,value2,value3)
					.map(nextValue -> simplerCase.convertToPredicate(nextValue)).toListX().plus(i->SeqUtils.EMPTY==i)
					.toArray(new Predicate[0]);

		
			return new CheckValue3(clazz,new MatchableCase(this.getPatternMatcher().inCaseOfManyType(predicate,result,
					predicates)));
		}
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public final CheckValue3<T1,T2,T3,R> isEmpty(Function<? super Tuple3<T1,T2,T3>,? extends R> result) {

			Predicate predicate = it -> Maybe.ofNullable(it)
					.map(v -> v.getClass().isAssignableFrom(clazz))
					.orElse(false);
			// add wildcard support
			
			Predicate[] predicates = new Predicate[]{i->i==SeqUtils.EMPTY};

			return new CheckValue3(clazz,new MatchableCase(this.getPatternMatcher().inCaseOfManyType(predicate,result,
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
		public final CheckValue4<T1,T2,T3,T4,R> is(Tuple4<Predicate<? super T1>,Predicate<? super T2>,Predicate<? super T3>,Predicate<? super T4>> values,Function<? super Tuple4<T1,T2,T3,T4>,? extends R> result) {		
			return isWhere(result,values.v1,values.v2,values.v3,values.v4);
		}
		 @SuppressWarnings({ "rawtypes", "unchecked" })
		private final  CheckValue4<T1,T2,T3,T4,R> isWhere(Function<? super Tuple4<T1,T2,T3,T4>,? extends R> result,Predicate<? super T1> value1,
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

		
			return new CheckValue4(clazz,new MatchableCase(this.getPatternMatcher().inCaseOfManyType(predicate,result,
					predicates)));
		}
		
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public final CheckValue4<T1,T2,T3,T4,R> isEmpty(Function<? super Tuple4<T1,T2,T3,T4>,? extends R> result) {

			Predicate predicate = it -> Maybe.ofNullable(it)
					.map(v -> v.getClass().isAssignableFrom(clazz))
					.orElse(false);
			// add wildcard support
			
			Predicate[] predicates = new Predicate[]{i->i==SeqUtils.EMPTY};

			return new CheckValue4(clazz,new MatchableCase(this.getPatternMatcher().inCaseOfManyType(predicate,result,
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
																Predicate<? super T5>> values,Function<? super Tuple5<T1,T2,T3,T4,T5>,? extends R> result) {		
			return isWhere(result,values.v1,values.v2,values.v3,values.v4,values.v5);
		}
		 @SuppressWarnings({ "rawtypes", "unchecked" })
		private final  CheckValue5<T1,T2,T3,T4,T5,R> isWhere(Function<? super Tuple5<T1,T2,T3,T4,T5>,? extends R> result,Predicate<? super T1> value1,
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

		
			return new CheckValue5(clazz,new MatchableCase(this.getPatternMatcher().inCaseOfManyType(predicate,result,
					predicates)));
		}
		
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public final CheckValue4<T1,T2,T3,T4,R> isEmpty(Function<? super Tuple4<T1,T2,T3,T4>,? extends R> result) {

			Predicate predicate = it -> Maybe.ofNullable(it)
					.map(v -> v.getClass().isAssignableFrom(clazz))
					.orElse(false);
			// add wildcard support
			
			Predicate[] predicates = new Predicate[]{i->i==SeqUtils.EMPTY};

			return new CheckValue4(clazz,new MatchableCase(this.getPatternMatcher().inCaseOfManyType(predicate,result,
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

		/**
		 * 
		 * Provide a comparison value, JDK 8 Predicate, or Hamcrest Matcher  for each Element to match on.
		 * 
		 * Further &amp; recursively unwrap any element by Predicates.type(ELEMENT_TYPE.class).with(V... values)
		 * 
		 * @see Predicates#type
		 * 
		 * @param values Matching rules for each element in the decomposed / unapplied user input
		 * @return Pattern Matcher builder with completed Case added to it
		 */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public final <V>CheckValues<T,R> values(Function<? super T,? extends R> result,V... values) {		
			Predicate predicate = it -> Optional.of(it)
					.map(v -> v.getClass().isAssignableFrom(clazz))
					.orElse(false);
			// add wildcard support
			
			Predicate<V>[] predicates = ReactiveSeq.of(values)
					.map(nextValue -> simplerCase.convertToPredicate(nextValue)).toList()
					.toArray(new Predicate[0]);

			//return new _LastStep<R,V,T>(clazz,predicate,predicates,this.getPatternMatcher());
			return new MatchableCase(this.getPatternMatcher().inCaseOfManyType(predicate,result,
					predicates)).withType(clazz);
		}
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public final CheckValues<T,R> is(Function<? super T,? extends R> result,T value) {		
			Predicate predicate = it -> Optional.of(it)
					.map(v -> v.getClass().isAssignableFrom(clazz))
					.orElse(false);
			// add wildcard support
			
			Predicate<T>[] predicates = ReactiveSeq.of(value)
					.map(nextValue -> simplerCase.convertToPredicate(nextValue)).toListX().plus(i->SeqUtils.EMPTY==i)
					.toArray(new Predicate[0]);

			//return new _LastStep<R,V,T>(clazz,predicate,predicates,this.getPatternMatcher());
			return new MatchableCase(this.getPatternMatcher().inCaseOfManyType(predicate,result,
					predicates)).withType(clazz);
		}
		@SafeVarargs @SuppressWarnings({ "rawtypes", "unchecked" })
		public final <V> CheckValues<T,R> just(Function<? super T,? extends R> result,V... values) {	
			Predicate predicate = it -> Optional.of(it)
					.map(v -> v.getClass().isAssignableFrom(clazz))
					.orElse(false);
			// add wildcard support
			
			@SuppressWarnings("unchecked")
			Predicate<V>[] predicates = ReactiveSeq.of(values)
					.map(nextValue -> simplerCase.convertToPredicate(nextValue)).toListX().plus(i->SeqUtils.EMPTY==i)
					.toArray(new Predicate[0]);

			return new MatchableCase(this.getPatternMatcher().inCaseOfManyType(predicate, result,
					predicates)).withType(clazz);
		}
		@SafeVarargs @SuppressWarnings({ "rawtypes", "unchecked" })
		public final <V> CheckValues<T,R> justWhere(Function<? super T,? extends R> result,Predicate<V>... values){
			Predicate predicate = it -> Optional.of(it)
					.map(v -> v.getClass().isAssignableFrom(clazz))
					.orElse(false);
			// add wildcard support
			
			Predicate<V>[] predicates = ReactiveSeq.of(values)
					.map(nextValue -> simplerCase.convertToPredicate(nextValue)).toListX().plus(i->SeqUtils.EMPTY==i)
					.toArray(new Predicate[0]);

		
			return new MatchableCase(this.getPatternMatcher().inCaseOfManyType(predicate, result,
					predicates)).withType(clazz);
		}
		 @SuppressWarnings({ "rawtypes", "unchecked" })
		public final  CheckValues<T,R> isWhere(Function<? super T,? extends R> result,Predicate<T> value){
			Predicate predicate = it -> Optional.of(it)
					.map(v -> v.getClass().isAssignableFrom(clazz))
					.orElse(false);
			// add wildcard support
			
			Predicate<T>[] predicates = ReactiveSeq.of(value)
					.map(nextValue -> simplerCase.convertToPredicate(nextValue)).toListX().plus(i->SeqUtils.EMPTY==i)
					.toArray(new Predicate[0]);

		
			return new MatchableCase(this.getPatternMatcher().inCaseOfManyType(predicate, result,
					predicates)).withType(clazz);
		}
		@SafeVarargs @SuppressWarnings({ "rawtypes", "unchecked" })
		public final <V> CheckValues<T,R> where(Function<? super T,? extends R> result,Predicate<V>... values) {
		
			Predicate predicate = it -> Optional.of(it)
					.map(v -> v.getClass().isAssignableFrom(clazz))
					.orElse(false);
			// add wildcard support
			
			Predicate<V>[] predicates = ReactiveSeq.of(values)
					.map(nextValue -> simplerCase.convertToPredicate(nextValue)).toList()
					.toArray(new Predicate[0]);

			return new MatchableCase(this.getPatternMatcher().inCaseOfManyType(predicate, result,
					predicates)).withType(clazz);
		}
		@SafeVarargs @SuppressWarnings({ "rawtypes", "unchecked" })
		public final <V> CheckValues<T,R> justMatch( Function<? super T,? extends R> result,Matcher<V>... values){
			Predicate predicate = it -> Optional.of(it)
					.map(v -> v.getClass().isAssignableFrom(clazz))
					.orElse(false);
			// add wildcard support
			
			Predicate<V>[] predicates = ReactiveSeq.of(values)
					.map(nextValue -> simplerCase.convertToPredicate(nextValue)).toListX().plus(i->SeqUtils.EMPTY==i)
					.toArray(new Predicate[0]);

			return new MatchableCase(this.getPatternMatcher().inCaseOfManyType(predicate, result,
					predicates)).withType(clazz);
		}
		 @SuppressWarnings({ "rawtypes", "unchecked" })
		public final <V> CheckValues<T,R> isMatch( Function<? super T,? extends R> result,Matcher<V> value){
			Predicate predicate = it -> Optional.of(it)
					.map(v -> v.getClass().isAssignableFrom(clazz))
					.orElse(false);
			// add wildcard support
			
			Predicate<V>[] predicates = ReactiveSeq.of(value)
					.map(nextValue -> simplerCase.convertToPredicate(nextValue)).toListX().plus(i->SeqUtils.EMPTY==i)
					.toArray(new Predicate[0]);

			return new MatchableCase(this.getPatternMatcher().inCaseOfManyType(predicate, result,
					predicates)).withType(clazz);
		}
		@SafeVarargs @SuppressWarnings({ "rawtypes", "unchecked" })
		public final <V> CheckValues<T,R> match(Function<? super T,? extends R> result,Matcher<V>... values) {
			
			Predicate predicate = it -> Optional.of(it)
												.map(v -> v.getClass().isAssignableFrom(clazz))
												.orElse(false);
			// add wildcard support
			
			Predicate<V>[] predicates = ReactiveSeq.of(values)
												.map(nextValue -> simplerCase.convertToPredicate(nextValue)).toList()
												.toArray(new Predicate[0]);

			return new MatchableCase(this.getPatternMatcher().inCaseOfManyType(predicate, result,
					predicates)).withType(clazz);
		}
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public final <V> CheckValues<T,R> isEmpty(Function<? super T,? extends R> result) {

			Predicate predicate = it -> Maybe.ofNullable(it)
					.map(v -> v.getClass().isAssignableFrom(clazz))
					.orElse(false);
			// add wildcard support
			
			Predicate<V>[] predicates = new Predicate[]{i->i==SeqUtils.EMPTY};

			return new MatchableCase(this.getPatternMatcher().inCaseOfManyType(predicate, result,
					predicates)).withType(clazz);
		}


		PatternMatcher getPatternMatcher() {
			return simplerCase.getPatternMatcher();
		}
	}


}
