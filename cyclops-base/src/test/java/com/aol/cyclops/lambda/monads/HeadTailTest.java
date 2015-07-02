package com.aol.cyclops.lambda.monads;
import static com.aol.cyclops.lambda.api.AsAnyM.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;




import org.junit.Test;

import com.aol.cyclops.streams.StreamUtils;

public class HeadTailTest {

	@Test
	public void headTailReplay(){
	
		SequenceM<String> helloWorld = anyM("hello","world","last").toSequence();
		String head = helloWorld.head();
		assertThat(head,equalTo("hello"));
		
		SequenceM<String> tail = helloWorld.tail();
		assertThat(tail.head(),equalTo("world"));
		
	}
}
