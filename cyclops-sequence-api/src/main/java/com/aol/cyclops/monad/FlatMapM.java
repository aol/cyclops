package com.aol.cyclops.monad;

import java.io.BufferedReader;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.BaseStream;

import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.sequence.streamable.Streamable;

public interface FlatMapM<T> {
	/**
	 * Convenience method to allow method reference support, when flatMap return type is a Stream.
	 * 
	 * AnyM follows the javaslang modified 'monad' laws https://gist.github.com/danieldietrich/71be006b355d6fbc0584
	 * In particular left-identity becomes
	 * Left identity: unit(a).flatMap(f) ≡ select(f.apply(a))
	 * Or in plain English, if your flatMap function returns multiple values (such as flatMap by Stream) but the current Monad only can only hold one value,
	 * only the first value is accepted.
	 * 
	 * Example 1 : multi-values are supported (AnyM wraps a Stream, List, Set etc)
	 * <pre>
	 * {@code 
	 *   AnyM<Integer> anyM = AnyM.fromStream(Stream.of(1,2,3)).flatMap(i->Stream.of(i+1,i+2));
	 *   
	 *   //AnyM[Stream[2,3,3,4,4,5]]
	 * }
	 * </pre>
	 * Example 2 : multi-values are not supported (AnyM wraps a Stream, List, Set etc)
	 * <pre>
	 * {@code 
	 *   AnyM<Integer> anyM = AnyM.fromOptional(Optional.of(1)).flatMap(i->Stream.of(i+1,i+2));
	 *   
	 *   //AnyM[Optional[2]]
	 * }
	 * </pre>
	 * 
	 * @param fn flatMap function
	 * @return flatMapped AnyM
	 */
	 <R> AnyM<R> flatMapStream(Function<? super T,BaseStream<? extends R,?>> fn);
	 /**
		 * Convenience method to allow method reference support, when flatMap return type is a Stream.
		 * 
		 * AnyM follows the javaslang modified 'monad' laws https://gist.github.com/danieldietrich/71be006b355d6fbc0584
		 * In particular left-identity becomes
		 * Left identity: unit(a).flatMap(f) ≡ select(f.apply(a))
		 * Or in plain English, if your flatMap function returns multiple values (such as flatMap by Stream) but the current Monad only can only hold one value,
		 * only the first value is accepted.
		 * 
		 * Example 1 : multi-values are supported (AnyM wraps a Stream, List, Set etc)
		 * <pre>
		 * {@code 
		 *   AnyM<Integer> anyM = AnyM.fromStream(Stream.of(1,2,3)).flatMap(i->Streamable.of(i+1,i+2));
		 *   
		 *   //AnyM[Stream[2,3,3,4,4,5]]
		 * }
		 * </pre>
		 * Example 2 : multi-values are not supported (AnyM wraps a Stream, List, Set etc)
		 * <pre>
		 * {@code 
		 *   AnyM<Integer> anyM = AnyM.fromOptional(Optional.of(1)).flatMap(i->Streamable.of(i+1,i+2));
		 *   
		 *   //AnyM[Optional[2]]
		 * }
		 * </pre>
		 * 
		 * @param fn flatMap function
		 * @return flatMapped AnyM
		 */
	 <R> AnyM<R> flatMapStreamable(Function<? super T,Streamable<R>> fn) ;
	 /**
		 * Convenience method to allow method reference support, when flatMap return type is a Stream.
		 * 
		 * AnyM follows the javaslang modified 'monad' laws https://gist.github.com/danieldietrich/71be006b355d6fbc0584
		 * In particular left-identity becomes
		 * Left identity: unit(a).flatMap(f) ≡ select(f.apply(a))
		 * Or in plain English, if your flatMap function returns multiple values (such as flatMap by Stream) but the current Monad only can only hold one value,
		 * only the first value is accepted.
		 * 
		 * Example 1 : multi-values are supported (AnyM wraps a Stream, List, Set etc)
		 * <pre>
		 * {@code 
		 *   AnyM<Integer> anyM = AnyM.fromStream(Stream.of(1,2,3)).flatMap(i->Arrays.asList(i+1,i+2));
		 *   
		 *   //AnyM[Stream[2,3,3,4,4,5]]
		 * }
		 * </pre>
		 * Example 2 : multi-values are not supported (AnyM wraps a Stream, List, Set etc)
		 * <pre>
		 * {@code 
		 *   AnyM<Integer> anyM = AnyM.fromOptional(Optional.of(1)).flatMap(i->Arrays.asList(i+1,i+2));
		 *   
		 *   //AnyM[Optional[2]]
		 * }
		 * </pre>
		 * 
		 * @param fn flatMap function
		 * @return flatMapped AnyM
		 */
	 <R> AnyM<R> flatMapCollection(Function<? super T,Collection<? extends R>> fn);
	/**
	 * Convenience method to allow method reference support, when flatMap return type is a Optional
	 * 
	 * <pre>
	 * {@code
	 *      List<Integer> list = Arrays.asList(1,2,3,4,null,6);
	 *      AnyM<Integer> anyM = AnyM.fromIterable(list);
	 *      //AnyM[1,2,3,4,null,6]
	 *      AnyM<Integer> removeNulls = anyM.flatMapOptional(Optional::ofNullable);
	 *      //AnyM[1,2,3,4,6]
	 * }
	 * </pre>
	 * 
	 * @param fn flatMap function
	 * @return flatMapped AnyM
	 */
	 <R> AnyM<R> flatMapOptional(Function<? super T,Optional<? extends R>> fn) ;
	 <R> AnyM<R> flatMapCompletableFuture(Function<? super T,CompletableFuture<? extends R>> fn);
	 /**
		 * Convenience method to allow method reference support, when flatMap return type is a Stream.
		 * 
		 * AnyM follows the javaslang modified 'monad' laws https://gist.github.com/danieldietrich/71be006b355d6fbc0584
		 * In particular left-identity becomes
		 * Left identity: unit(a).flatMap(f) ≡ select(f.apply(a))
		 * Or in plain English, if your flatMap function returns multiple values (such as flatMap by Stream) but the current Monad only can only hold one value,
		 * only the first value is accepted.
		 * 
		 * Example 1 : multi-values are supported (AnyM wraps a Stream, List, Set etc)
		 * <pre>
		 * {@code 
		 *   AnyM<Integer> anyM = AnyM.fromStream(Stream.of(1,2,3)).flatMap(i->SequenceM.of(i+1,i+2));
		 *   
		 *   //AnyM[Stream[2,3,3,4,4,5]]
		 * }
		 * </pre>
		 * Example 2 : multi-values are not supported (AnyM wraps a Stream, List, Set etc)
		 * <pre>
		 * {@code 
		 *   AnyM<Integer> anyM = AnyM.fromOptional(Optional.of(1)).flatMap(i->SequenceM.of(i+1,i+2));
		 *   
		 *   //AnyM[Optional[2]]
		 * }
		 * </pre>
		 * 
		 * @param fn flatMap function
		 * @return flatMapped AnyM
		 */
	 <R> AnyM<R> flatMapSequenceM(Function<? super T,SequenceM<? extends R>> fn);
	 
		/**
		 * Perform a flatMap operation where the result will be a flattened stream of Characters
		 * from the CharSequence returned by the supplied function.
		 * 
		 * <pre>
		 * {@code
		 * List<Character> result = AnyM.fromArray("input.file")
									.liftAndBindCharSequence(i->"hello world")
									.asSequence()
									.toList();
			
			assertThat(result,equalTo(Arrays.asList('h','e','l','l','o',' ','w','o','r','l','d')));
		 * 
		 * }</pre>
		 
		 * AnyM follows the javaslang modified 'monad' laws https://gist.github.com/danieldietrich/71be006b355d6fbc0584
		 * In particular left-identity becomes
		 * Left identity: unit(a).flatMap(f) ≡ select(f.apply(a))
		 * Or in plain English, if your flatMap function returns multiple values (such as flatMap by Stream) but the current Monad only can only hold one value,
		 * only the first value is accepted.
		 * 
		 * Example 1 : multi-values are supported (AnyM wraps a Stream, List, Set etc)
		 * <pre>
		 * {@code 
		 *   AnyM<Integer> anyM = AnyM.fromStream(Stream.of(1,2,3)).flatMap(i-> ""+(i+1)+(i+2));
		 *   
		 *   //AnyM[Stream[2,3,3,4,4,5]]
		 * }
		 * </pre>
		 * Example 2 : multi-values are not supported (AnyM wraps a Stream, List, Set etc)
		 * <pre>
		 * {@code 
		 *   AnyM<Integer> anyM = AnyM.fromOptional(Optional.of(1)).flatMap(i->""+(i+1)+(i+2));
		 *   
		 *   //AnyM[Optional[2]]
		 * }
		 * </pre>
		 
		 * 
		 * 
		 * @param fn
		 * @return
		 */
		 AnyM<Character> flatMapCharSequence(Function<? super T,CharSequence> fn);
		/**
		 *  Perform a flatMap operation where the result will be a flattened stream of Strings
		 * from the text loaded from the supplied files.
		 * 
		 * <pre>
		 * {@code
		 * 		List<String> result = anyM("input.file")
									.map(getClass().getClassLoader()::getResource)
									.peek(System.out::println)
									.map(URL::getFile)
									.liftAndBindFile(File::new)
									.asSequence()
									.toList();
			
			assertThat(result,equalTo(Arrays.asList("hello","world")));
		 * 
		 * }
		 * 
		 * </pre>
		 * AnyM follows the javaslang modified 'monad' laws https://gist.github.com/danieldietrich/71be006b355d6fbc0584
		 * In particular left-identity becomes
		 * Left identity: unit(a).flatMap(f) ≡ select(f.apply(a))
		 * Or in plain English, if your flatMap function returns multiple values (such as flatMap by Stream) but the current Monad only can only hold one value,
		 * only the first value is accepted.
		 * 
		 * Example 1 : multi-values are supported (AnyM wraps a Stream, List, Set etc)
		 * <pre>
		 * {@code 
		 *   AnyM<String> anyM = AnyM.fromStream(Stream.of(1,2,3)).flatMapFile(this::loadFile);
		 *   
		 *   //AnyM[Stream["line1","line2"]]
		 * }
		 * </pre>
		 * Example 2 : multi-values are not supported (AnyM wraps a Stream, List, Set etc)
		 * <pre>
		 * {@code 
		 *   AnyM<Integer> anyM = AnyM.fromOptional(Optional.of(1)).flatMapFile(this::loadFile);
		 *   
		 *   //AnyM[Optional["line1"]]
		 * }
		 * </pre> 
		 * @param fn
		 * @return
		 */
		  AnyM<String> flatMapFile(Function<? super T,File> fn);
		/**
		 *  Perform a flatMap operation where the result will be a flattened stream of Strings
		 * from the text loaded from the supplied URLs 
		 * <pre>
		 * {@code 
		 * List<String> result = anyM("input.file")
									.liftAndBindURL(getClass().getClassLoader()::getResource)
									.asSequence()
									.toList();
			
			assertThat(result,equalTo(Arrays.asList("hello","world")));
		 * 
		 * }
		 * 
		 * </pre>
		  * AnyM follows the javaslang modified 'monad' laws https://gist.github.com/danieldietrich/71be006b355d6fbc0584
		 * In particular left-identity becomes
		 * Left identity: unit(a).flatMap(f) ≡ select(f.apply(a))
		 * Or in plain English, if your flatMap function returns multiple values (such as flatMap by Stream) but the current Monad only can only hold one value,
		 * only the first value is accepted.
		 * 
		 * Example 1 : multi-values are supported (AnyM wraps a Stream, List, Set etc)
		 * <pre>
		 * {@code 
		 *   AnyM<String> anyM = AnyM.fromStream(Stream.of(1,2,3)).flatMapURL(this::loadURL);
		 *   
		 *   //AnyM[Stream["line1","line2"]]
		 * }
		 * </pre>
		 * Example 2 : multi-values are not supported (AnyM wraps a Stream, List, Set etc)
		 * <pre>
		 * {@code 
		 *   AnyM<Integer> anyM = AnyM.fromOptional(Optional.of(1)).flatMapURL(this::loadURL);
		 *   
		 *   //AnyM[Optional["line1"]]
		 * }
		 * </pre>
		 * 
		 * @param fn
		 * @return
		 */
		  AnyM<String> flatMapURL(Function<? super T, URL> fn) ;
		/**
		  *  Perform a flatMap operation where the result will be a flattened stream of Strings
		 * from the text loaded from the supplied BufferedReaders
		 * 
		 * <pre>
		 * {@code
		 * List<String> result = anyM("input.file")
									.map(getClass().getClassLoader()::getResourceAsStream)
									.map(InputStreamReader::new)
									.liftAndBindBufferedReader(BufferedReader::new)
									.asSequence()
									.toList();
			
			assertThat(result,equalTo(Arrays.asList("hello","world")));
		 * 
		 * }
		  * AnyM follows the javaslang modified 'monad' laws https://gist.github.com/danieldietrich/71be006b355d6fbc0584
		 * In particular left-identity becomes
		 * Left identity: unit(a).flatMap(f) ≡ select(f.apply(a))
		 * Or in plain English, if your flatMap function returns multiple values (such as flatMap by Stream) but the current Monad only can only hold one value,
		 * only the first value is accepted.
		 * 
		 * Example 1 : multi-values are supported (AnyM wraps a Stream, List, Set etc)
		 * <pre>
		 * {@code 
		 *   AnyM<String> anyM = AnyM.fromStream(Stream.of(1,2,3)).flatMapBufferedRead(this::reader);
		 *   
		 *   //AnyM[Stream["line1","line2"]]
		 * }
		 * </pre>
		 * Example 2 : multi-values are not supported (AnyM wraps a Stream, List, Set etc)
		 * <pre>
		 * {@code 
		 *   AnyM<Integer> anyM = AnyM.fromOptional(Optional.of(1)).flatMapBufferedRead(this::reader);
		 *   
		 *   //AnyM[Optional["line1"]]
		 * }
		 * </pre>
		 * 
		 * @param fn
		 * @return
		 */
		  AnyM<String> flatMapBufferedReader(Function<? super T,BufferedReader> fn) ;

}
