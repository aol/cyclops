package com.aol.cyclops.internal.comprehensions.converters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.stream.Stream;

import lombok.val;

import com.aol.cyclops.types.extensability.MonadicConverter;
import com.aol.cyclops.util.ExceptionSoftener;

public class URLToStreamConverter implements MonadicConverter<Stream> {

	public static int priority = 5;
	public int priority(){
		return priority;
	}
	@Override
	public boolean accept(Object o) {
		return o instanceof URL;
	}

	@Override
	public Stream convertToMonadicForm(Object f) {
		val url = (URL)f;
		try {
			BufferedReader in = new BufferedReader(
			        new InputStreamReader(
			        url.openStream()));
			
			return in.lines();
			
		} catch (IOException e) {
			ExceptionSoftener.throwSoftenedException(e);
			return null;
		}
		
	}

}
