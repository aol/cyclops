package com.aol.cyclops.comprehensions.converters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.jooq.lambda.Seq;

import com.aol.cyclops.lambda.api.MonadicConverter;
import com.aol.cyclops.lambda.utils.ExceptionSoftener;

public class FileToStreamConverter implements MonadicConverter<Seq> {

	@Override
	public boolean accept(Object o) {
		return o instanceof File;
	}

	@Override
	public Seq convertToMonadicForm(Object f) {
		try {
			return Seq.seq(Files.lines(Paths.get( ((File)f).getAbsolutePath())));
		} catch (IOException e) {
			ExceptionSoftener.singleton.factory.getInstance().throwSoftenedException(e);
		}
		return null;
	}

}
