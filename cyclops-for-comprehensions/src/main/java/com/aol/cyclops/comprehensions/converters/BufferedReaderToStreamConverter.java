package com.aol.cyclops.comprehensions.converters;

import java.io.BufferedReader;

import lombok.val;

import org.jooq.lambda.Seq;



public class BufferedReaderToStreamConverter implements MonadicConverter<Seq> {

	@Override
	public boolean accept(Object o) {
		return o instanceof BufferedReader;
	}

	@Override
	public Seq convertToMonadicForm(Object f) {
		val reader = (BufferedReader)f;
		return Seq.seq(reader.lines());
	}

}
