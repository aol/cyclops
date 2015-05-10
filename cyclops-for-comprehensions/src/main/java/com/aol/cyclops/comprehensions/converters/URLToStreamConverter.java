package com.aol.cyclops.comprehensions.converters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import lombok.val;

import org.jooq.lambda.Seq;

import com.aol.cyclops.lambda.api.MonadicConverter;
import com.aol.cyclops.lambda.utils.ExceptionSoftener;

public class URLToStreamConverter implements MonadicConverter<Seq> {

	@Override
	public boolean accept(Object o) {
		return o instanceof URL;
	}

	@Override
	public Seq convertToMonadicForm(Object f) {
		val url = (URL)f;
		try {
			BufferedReader in = new BufferedReader(
			        new InputStreamReader(
			        url.openStream()));
			
			return Seq.seq(in.lines());
			
		} catch (IOException e) {
			ExceptionSoftener.singleton.factory.getInstance().throwSoftenedException(e);
		}
		return null;
	}

}
