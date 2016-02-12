package com.aol.cyclops.comprehensions.converters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import com.aol.cyclops.control.ExceptionSoftener;
import com.aol.cyclops.lambda.api.MonadicConverter;

public class FileToStreamConverter implements MonadicConverter<Stream> {

	public static int priority = 5;
	public int priority(){
		return priority;
	}
	@Override
	public boolean accept(Object o) {
		return o instanceof File;
	}

	@Override
	public Stream convertToMonadicForm(Object f) {
		try {
			return Files.lines(Paths.get( ((File)f).getAbsolutePath()));
		} catch (IOException e) {
			ExceptionSoftener.throwSoftenedException(e);
			return null;
		}
	
	}

}
