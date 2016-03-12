package com.aol.cyclops;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.Matchable;
import com.aol.cyclops.control.Matchable.MTuple2;
import com.aol.cyclops.control.Matchable.MTuple3;
import com.aol.cyclops.control.Matchable.MTuple4;
import com.aol.cyclops.control.Matchable.MTuple5;
import com.aol.cyclops.control.Matchable.MXor;
import com.aol.cyclops.control.Matchable.MatchableIterable;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Try;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.types.stream.HeadAndTail;
import com.aol.cyclops.util.ExceptionSoftener;

public class Matchables {
    
    public static <T1> MXor<T1,Throwable> future(CompletableFuture<T1> future){
        return ()-> FutureW.of(future).toXor().swap();
    }
    public static <T1> MXor<T1,Throwable> future(FutureW<T1> future){
        return ()-> future.toXor().swap();
    }
   
    public static <T1,X extends Throwable> MXor<T1,X> tryMatch(Try<T1,X> match){
        return ()-> match.toXor().swap();
    }
    public static <T> Matchable.MatchableOptional<T> maybe(Maybe<T> opt){
        return opt;
    }
    public static <T> Matchable.MatchableOptional<T> optional(Optional<T> opt){
        return Maybe.fromOptional(opt);
    }
 
    public static <T> MXor<AnyMValue<T>,AnyMSeq<T>> anyM(AnyM<T> anyM){
        return ()-> anyM instanceof AnyMValue ?  Xor.secondary((AnyMValue<T>)anyM) : Xor.primary((AnyMSeq<T>)anyM);
    }
	public static<X extends Throwable> MTuple4<Class,String,Throwable,MatchableIterable<StackTraceElement>> throwable(X t){
		return Matchable.from(()->(Class)t.getClass(),
							  ()->t.getMessage(),
							  ()->t.getCause(),
							  ()->Matchable.fromIterable(Arrays.asList(t.getStackTrace())));
	}
	
	/**
	 * Break an URL down into
	 * protocol, host, port, path, query
	 * 
	 * @param url
	 * @return
	 */
	public static MTuple5<String,String,Integer,String,String> url(URL url){
		return Matchable.from(()->url.getProtocol(),
							  ()->url.getHost(),
							  ()->url.getPort(),
							  ()->url.getPath(),
							  ()->url.getQuery());
	}
	public static Matchable.AutoCloseableMatchableIterable<String> lines(BufferedReader in){
	
		return new Matchable.AutoCloseableMatchableIterable<>(in,()->in.lines().iterator());	
	}
	public static Matchable.AutoCloseableMatchableIterable<String> lines(URL url){
		
		BufferedReader in = ExceptionSoftener.softenSupplier(()->new BufferedReader(
															new InputStreamReader(
															url.openStream()))).get();
		return new Matchable.AutoCloseableMatchableIterable<>(in,()->in.lines().iterator());	
	}
	/**
	 * Pattern match on the contents of a File
	 * <pre>
	 * {@code 
	 * String result = Matchables.lines(new File(file))
                                 .on$12___()
                                 .matches(c->c.is(when("hello","world"),then("correct")), otherwise("miss")).get();
                                  
       }
       </pre>
	 * 
	 * @param f File to match against
	 * @return Matcher
	 */
	public static Matchable.AutoCloseableMatchableIterable<String> lines(File f){
			Stream<String> stream = ExceptionSoftener.softenSupplier(()->Files.lines(Paths.get( ((File)f).getAbsolutePath()))).get();
		return new Matchable.AutoCloseableMatchableIterable<>(stream ,()->stream.iterator() );	
	}
	public static MatchableIterable<String> words(CharSequence seq){
		return Matchable.fromIterable(Arrays.asList(seq.toString().split(" ")));
	}
	public static MatchableIterable<Character> chars(CharSequence seq){
		return Matchable.fromCharSequence(seq);
	}
	public static <ST,PT> MXor<ST,PT> xor(Xor<ST,PT> xor){
	    return ()->xor;
	}
	public static <T> MTuple2<Maybe<T>,ListX<T>> headAndTail(Collection<T> col){
		HeadAndTail<T> ht = CollectionX.fromCollection(col).headAndTail();
		return Matchable.from(()->ht.headMaybe(),()->ht.tail().toListX());
	}
	public static <K,V> ReactiveSeq<MTuple2<K,V>> keysAndValues(Map<K,V> map){
		return ReactiveSeq.fromIterable(map.entrySet()).map(entry ->
		                    (MTuple2<K,V>)Matchable.from(()->entry.getKey(),()->entry.getValue()));
	}
	public static MTuple3<Integer,Integer,Integer> dateDDMMYYYY(Date date){
		Date input = new Date();
		LocalDate local = input.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		return localDateDDMMYYYY(local);
	}
	public static MTuple3<Integer,Integer,Integer> dateMMDDYYYY(Date date){
		Date input = new Date();
		LocalDate local = input.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		return localDateMMDDYYYY(local);
	}
	public static MTuple3<Integer,Integer,Integer> localDateDDMMYYYY(LocalDate date){
		return Matchable.from(()->date.getDayOfMonth(),()->date.getMonth().getValue(),()->date.getYear());
	}
	public static MTuple3<Integer,Integer,Integer> localDateMMDDYYYY(LocalDate date){
		return Matchable.from(()->date.getMonth().getValue(),()->date.getDayOfMonth(),()->date.getYear());
	}
	public static MTuple3<Integer,Integer,Integer> dateHMS(Date date){
		Date input = new Date();
		LocalTime local = input.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
		return localTimeHMS(local);
	}
	public static MTuple3<Integer,Integer,Integer> localTimeHMS(LocalTime time){
		return Matchable.from(()->time.getHour(),()->time.getMinute(),()->time.getSecond());
	}
}
