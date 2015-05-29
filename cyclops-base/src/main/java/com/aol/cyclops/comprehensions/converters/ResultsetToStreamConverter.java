package com.aol.cyclops.comprehensions.converters;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.stream.Stream;

import lombok.val;

import com.aol.cyclops.lambda.api.MonadicConverter;
import com.aol.cyclops.lambda.utils.ExceptionSoftener;

public class ResultsetToStreamConverter implements MonadicConverter<Stream> {
	
	public static int priority = 5;
	public int priority(){
		return priority;
	}
	@Override
	public boolean accept(Object o) {
		return o instanceof ResultSet;
	}

	@Override
	public Stream convertToMonadicForm(Object f) {
		val resultset = (ResultSet)f;
		return toStream((new Iterator(){
			Boolean hasNext;
			@Override
			public boolean hasNext() {
				if(hasNext==null)
					try {
						hasNext = resultset.next();
					} catch (SQLException e) {
						ExceptionSoftener.singleton.factory.getInstance().throwSoftenedException(e);
					}
				return hasNext;
			}

			@Override
			public Object next() {
				hasNext=null;
				return resultset;
			}
			
		}));
	}

	private Stream toStream(Iterator iterator) {
		return new IteratorToStreamConverter().convertToMonadicForm(iterator);
	}

}
