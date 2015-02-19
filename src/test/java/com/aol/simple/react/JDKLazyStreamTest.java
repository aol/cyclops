package com.aol.simple.react;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

public class JDKLazyStreamTest extends BaseJDKStreamTest{

	<U> Stream<U> of(U... array){
	
		return SimpleReact.lazy().reactToCollection(Arrays.asList(array));
	}
	
		
}
