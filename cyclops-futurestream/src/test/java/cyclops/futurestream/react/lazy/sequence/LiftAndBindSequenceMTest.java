package cyclops.futurestream.react.lazy.sequence;


import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import cyclops.companion.Streams;
import cyclops.futurestream.LazyReact;
import org.junit.Test;

public class LiftAndBindSequenceMTest {
	@Test
	public void testLiftAndBindFile(){


		List<String> result = Streams.flatMapFile(LazyReact.sequentialBuilder().of("input.file")
								.map(getClass().getClassLoader()::getResource)
								.peek(System.out::println)
								.map(URL::getFile)
								,File::new)
								.collect(Collectors.toList());

		assertThat(result,equalTo(Arrays.asList("hello","world")));
	}
	@Test
	public void testLiftAndBindURL(){


		List<String> result = Streams.flatMapURL(LazyReact.sequentialBuilder().of("input.file")
								,getClass().getClassLoader()::getResource)
								.collect(Collectors.toList());

		assertThat(result,equalTo(Arrays.asList("hello","world")));
	}
	@Test
	public void testLiftAndBindString(){


		List<Character> result = Streams.flatMapCharSequence(LazyReact.sequentialBuilder().of("input.file")
									,i->"hello world")
									.collect(Collectors.toList());

		assertThat(result,equalTo(Arrays.asList('h','e','l','l','o',' ','w','o','r','l','d')));
	}
	@Test
	public void testLiftAndBindBufferedReader(){


		List<String> result = Streams.flatMapBufferedReader(LazyReact.sequentialBuilder().of("input.file")
								.map(getClass().getClassLoader()::getResourceAsStream)
								.map(InputStreamReader::new)
								,r-> new BufferedReader(r))
								.collect(Collectors.toList());

		assertThat(result,equalTo(Arrays.asList("hello","world")));
	}
}
