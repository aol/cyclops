package com.aol.cyclops.comprehensions.converters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.stream.Stream;

import lombok.val;

import com.aol.cyclops.lambda.api.MonadicConverter;
import com.aol.cyclops.lambda.utils.ExceptionSoftener;

import fj.data.Seq;

public class URLToStreamConverter implements MonadicConverter<Stream> {

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
			ExceptionSoftener.singleton.factory.getInstance().throwSoftenedException(e);
		}
		return null;
	}

}
