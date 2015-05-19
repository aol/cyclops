package com.aol.cyclops.comprehensions.converters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import com.aol.cyclops.lambda.api.MonadicConverter;
import com.aol.cyclops.lambda.utils.ExceptionSoftener;

import fj.data.Seq;

public class FileToStreamConverter implements MonadicConverter<Stream> {

	@Override
	public boolean accept(Object o) {
		return o instanceof File;
	}

	@Override
	public Stream convertToMonadicForm(Object f) {
		try {
			return Files.lines(Paths.get( ((File)f).getAbsolutePath()));
		} catch (IOException e) {
			ExceptionSoftener.singleton.factory.getInstance().throwSoftenedException(e);
		}
		return null;
	}

}
