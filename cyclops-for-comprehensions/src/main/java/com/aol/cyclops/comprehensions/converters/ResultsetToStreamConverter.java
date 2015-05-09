package com.aol.cyclops.comprehensions.converters;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lombok.val;

import org.jooq.lambda.Seq;

import com.aol.cyclops.lambda.utils.ExceptionSoftener;

public class ResultsetToStreamConverter implements MonadicConverter<Seq> {

	@Override
	public boolean accept(Object o) {
		return o instanceof ResultSet;
	}

	@Override
	public Seq convertToMonadicForm(Object f) {
		val resultset = (ResultSet)f;
		return Seq.seq(new Iterator(){
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
			
		});
	}

}
