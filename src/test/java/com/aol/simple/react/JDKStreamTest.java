package com.aol.simple.react;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.simple.react.stream.simple.SimpleReact;

public class JDKStreamTest extends BaseJDKStreamTest{

	<U> Stream<U> of(U... array){
		
		return new SimpleReact().reactToCollection(Arrays.asList(array));
	}
	
	
}
