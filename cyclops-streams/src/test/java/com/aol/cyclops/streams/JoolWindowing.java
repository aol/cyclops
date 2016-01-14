package com.aol.cyclops.streams;
/**
 * Copyright (c) 2014-2016, Data Geekery GmbH, contact@datageekery.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import static java.util.Arrays.asList;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.toList;
import static org.jooq.lambda.Agg.count;
import static org.jooq.lambda.Agg.max;
import static org.jooq.lambda.Agg.min;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.*;
import java.util.function.Predicate;

import org.jooq.lambda.Seq;
import org.jooq.lambda.Window;
import org.jooq.lambda.tuple.Tuple3;
import org.junit.Test;

import com.aol.cyclops.sequence.SequenceM;
/**
 * @author Lukas Eder
 * @author Roman Tkalenko
 */
public class JoolWindowing {
	/**
	 @Test
	    public void testCount() {
		
	        assertEquals(0L, SequenceM.of().count());
	        assertEquals(0L, SequenceM.of().countDistinct());
	        assertEquals(0L, SequenceM.<Integer>of().countDistinctBy(l -> l % 3));

	        assertEquals(1L, SequenceM.of(1).count());
	        assertEquals(1L, SequenceM.of(1).countDistinct());
	        assertEquals(1L, SequenceM.of(1).countDistinctBy(l -> l % 3L));

	        assertEquals(2L, SequenceM.of(1, 2).count());
	        assertEquals(2L, SequenceM.of(1, 2).countDistinct());
	        assertEquals(2L, SequenceM.of(1, 2).countDistinctBy(l -> l % 3L));

	        assertEquals(3L, SequenceM.of(1, 2, 2).count());
	        assertEquals(2L, SequenceM.of(1, 2, 2).countDistinct());
	        assertEquals(2L, SequenceM.of(1, 2, 2).countDistinctBy(l -> l % 3L));

	        assertEquals(4L, SequenceM.of(1, 2, 2, 4).count());
	        assertEquals(3L, SequenceM.of(1, 2, 2, 4).countDistinct());
	        assertEquals(2L, SequenceM.of(1, 2, 2, 4).countDistinctBy(l -> l % 3L));
	    }
	    
	    @Test
	    public void testSum() {
	        assertEquals(Optional.empty(), SequenceM.of().sum());
	        
	        assertEquals(Optional.of(1), SequenceM.of(1).sum());
	        assertEquals(Optional.of(3), SequenceM.of(1, 2).sum());
	        assertEquals(Optional.of(6), SequenceM.of(1, 2, 3).sum());
	        
	        assertEquals(Optional.of(1.0), SequenceM.of(1.0).sum());
	        assertEquals(Optional.of(3.0), SequenceM.of(1.0, 2.0).sum());
	        assertEquals(Optional.of(6.0), SequenceM.of(1.0, 2.0, 3.0).sum());
	    }
	    
	    @Test
	    public void testAvg() {
	        assertEquals(Optional.empty(), SequenceM.of().avg());
	        
	        assertEquals(Optional.of(1), SequenceM.of(1).avg());
	        assertEquals(Optional.of(1), SequenceM.of(1, 2).avg());
	        assertEquals(Optional.of(2), SequenceM.of(1, 2, 3).avg());
	        
	        assertEquals(Optional.of(1.0), SequenceM.of(1.0).avg());
	        assertEquals(Optional.of(1.5), SequenceM.of(1.0, 2.0).avg());
	        assertEquals(Optional.of(2.0), SequenceM.of(1.0, 2.0, 3.0).avg());
	    }

	    @Test
	    public void testCollect() {
	        assertEquals(
	            tuple(0L, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()),
	            SequenceM.<Integer>of().collect(count(), min(), min(i -> -i), max(), max(i -> -i))
	        );

	        assertEquals(
	            tuple(1L, Optional.of(1), Optional.of(-1), Optional.of(1), Optional.of(-1)),
	            SequenceM.of(1).collect(count(), min(), min(i -> -i), max(), max(i -> -i))
	        );

	        assertEquals(
	            tuple(2L, Optional.of(1), Optional.of(-2), Optional.of(2), Optional.of(-1)),
	            SequenceM.of(1, 2).collect(count(), min(), min(i -> -i), max(), max(i -> -i))
	        );

	        assertEquals(
	            tuple(3L, Optional.of(1), Optional.of(-3), Optional.of(3), Optional.of(-1)),
	            SequenceM.of(1, 2, 3).collect(count(), min(), min(i -> -i), max(), max(i -> -i))
	        );

	        assertEquals(
	            tuple(4L, Optional.of(1), Optional.of(-4), Optional.of(4), Optional.of(-1)),
	            SequenceM.of(1, 2, 3, 4).collect(count(), min(), min(i -> -i), max(), max(i -> -i))
	        );
	    }
	    
	    @Test
	    public void testWindowSpecifications() {
	        assertEquals(
	            asList(
	                tuple(0, 0, 0, 0, 4),
	                tuple(1, 0, 1, 1, 2),
	                tuple(2, 1, 0, 4, 0),
	                tuple(3, 2, 2, 2, 3),
	                tuple(4, 1, 1, 3, 1)
	            ),
	            SequenceM.of(1, 2, 4, 2, 3)
	               .window(
	                    Window.of(),
	                    Window.of(i -> i % 2),
	                    Window.of(i -> i < 3),
	                    Window.of(naturalOrder()),
	                    Window.of(reverseOrder())
	                )
	                .map(t -> tuple(
	                    (int) t.v1.rowNumber(),
	                    (int) t.v2.rowNumber(),
	                    (int) t.v3.rowNumber(),
	                    (int) t.v4.rowNumber(),
	                    (int) t.v5.rowNumber()
	                ))
	                .toList()
	        );
	    }
	    
	    @Test
	    public void testRunningTotal() {
	        
	        // Do the calculation from this blog post in Java
	        // http://blog.jooq.org/2014/04/29/nosql-no-sql-how-to-calculate-running-totals/
	        
	        // | ID   | VALUE_DATE | AMOUNT |  BALANCE |
	        // |------|------------|--------|----------|
	        // | 9997 | 2014-03-18 |  99.17 | 19985.81 |
	        // | 9981 | 2014-03-16 |  71.44 | 19886.64 |
	        // | 9979 | 2014-03-16 | -94.60 | 19815.20 |
	        // | 9977 | 2014-03-16 |  -6.96 | 19909.80 |
	        // | 9971 | 2014-03-15 | -65.95 | 19916.76 |
	        
	        BigDecimal currentBalance = new BigDecimal("19985.81");
	        
	        assertEquals(
	            asList(
	                new BigDecimal("19985.81"),
	                new BigDecimal("19886.64"),
	                new BigDecimal("19815.20"),
	                new BigDecimal("19909.80"),
	                new BigDecimal("19916.76")
	            ),
	            SequenceM.of(
	                    tuple(9997, "2014-03-18", new BigDecimal("99.17")),
	                    tuple(9981, "2014-03-16", new BigDecimal("71.44")),
	                    tuple(9979, "2014-03-16", new BigDecimal("-94.60")),
	                    tuple(9977, "2014-03-16", new BigDecimal("-6.96")),
	                    tuple(9971, "2014-03-15", new BigDecimal("-65.95")))
	               .window(Comparator.comparing((Tuple3<Integer, String, BigDecimal> t) -> t.v1, reverseOrder()).thenComparing(t -> t.v2), Long.MIN_VALUE, -1)
	               .map(w -> w.value().concat(
	                    currentBalance.subtract(w.sum(t -> t.v3).orElse(BigDecimal.ZERO))
	               ))
	               .map(t -> t.v4)
	               .toList()
	        );
	    }**/
	    Function<Number,Number> mod2 =i -> i;
	    @Test
	    public void testWindowFunctionRowNumber() {
	    	SequenceM.of(1, 2, 4, 2, 3).window((Integer i)->i%2, java.util.Comparator.reverseOrder());
	        assertEquals(asList(0L, 1L, 2L, 3L, 4L), SequenceM.of(1, 2, 4, 2, 3).window().map(Window::rowNumber).toList());
	        assertEquals(asList(0L, 1L, 4L, 2L, 3L), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder()).map(Window::rowNumber).toList());
	        assertEquals(asList(0L, 0L, 1L, 2L, 1L), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2).map(Window::rowNumber).toList());
	        SequenceM.of(1, 2, 4, 2, 3).window(mod2, naturalOrder());
	        assertEquals(asList(0L, 0L, 2L, 1L, 1L), SequenceM.of(1, 2, 4, 2, 3).window(mod2, naturalOrder()).map(Window::rowNumber).toList());
	    }
	/**        
	    @Test
	    public void testWindowFunctionRank() {
	        assertEquals(asList(0L, 1L, 4L, 1L, 3L), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder()).map(Window::rank).toList());
	        assertEquals(asList(0L, 0L, 2L, 0L, 1L), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder()).map(Window::rank).toList());
	    }
	      
	    @Test
	    public void testWindowFunctionDenseRank() {
	        assertEquals(asList(0L, 1L, 3L, 1L, 2L), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder()).map(Window::denseRank).toList());
	        assertEquals(asList(0L, 0L, 1L, 0L, 1L), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder()).map(Window::denseRank).toList());
	    }
	    
	    @Test
	    public void testWindowFunctionPercentRank() {
	        assertEquals(asList(0.0, 0.25, 1.0, 0.25, 0.75), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder()).map(Window::percentRank).toList());
	        assertEquals(asList(0.0, 0.0, 1.0, 0.0, 1.0), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder()).map(Window::percentRank).toList());
	    }
	    
	    @Test
	    public void testWindowFunctionNtile() {
	        assertEquals(asList(0L, 0L, 0L, 0L, 0L), SequenceM.of(1, 2, 4, 2, 3).window().map(w -> w.ntile(1)).toList());
	        assertEquals(asList(0L, 0L, 0L, 0L, 0L), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder()).map(w -> w.ntile(1)).toList());
	        assertEquals(asList(0L, 0L, 0L, 0L, 0L), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2).map(w -> w.ntile(1)).toList());
	        assertEquals(asList(0L, 0L, 0L, 0L, 0L), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder()).map(w -> w.ntile(1)).toList());
	        
	        assertEquals(asList(0L, 0L, 0L, 1L, 1L), SequenceM.of(1, 2, 4, 2, 3).window().map(w -> w.ntile(2)).toList());
	        assertEquals(asList(0L, 0L, 1L, 0L, 1L), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder()).map(w -> w.ntile(2)).toList());
	        assertEquals(asList(0L, 0L, 0L, 1L, 1L), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2).map(w -> w.ntile(2)).toList());
	        assertEquals(asList(0L, 0L, 1L, 0L, 1L), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder()).map(w -> w.ntile(2)).toList());
	        
	        assertEquals(asList(0L, 0L, 1L, 1L, 2L), SequenceM.of(1, 2, 4, 2, 3).window().map(w -> w.ntile(3)).toList());
	        assertEquals(asList(0L, 0L, 2L, 1L, 1L), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder()).map(w -> w.ntile(3)).toList());
	        assertEquals(asList(0L, 0L, 1L, 2L, 1L), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2).map(w -> w.ntile(3)).toList());
	        assertEquals(asList(0L, 0L, 2L, 1L, 1L), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder()).map(w -> w.ntile(3)).toList());
	        
	        assertEquals(asList(0L, 0L, 1L, 2L, 3L), SequenceM.of(1, 2, 4, 2, 3).window().map(w -> w.ntile(4)).toList());
	        assertEquals(asList(0L, 0L, 3L, 1L, 2L), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder()).map(w -> w.ntile(4)).toList());
	        assertEquals(asList(0L, 0L, 1L, 2L, 2L), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2).map(w -> w.ntile(4)).toList());
	        assertEquals(asList(0L, 0L, 2L, 1L, 2L), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder()).map(w -> w.ntile(4)).toList());
	        
	        assertEquals(asList(0L, 1L, 2L, 3L, 4L), SequenceM.of(1, 2, 4, 2, 3).window().map(w -> w.ntile(5)).toList());
	        assertEquals(asList(0L, 1L, 4L, 2L, 3L), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder()).map(w -> w.ntile(5)).toList());
	        assertEquals(asList(0L, 0L, 1L, 3L, 2L), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2).map(w -> w.ntile(5)).toList());
	        assertEquals(asList(0L, 0L, 3L, 1L, 2L), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder()).map(w -> w.ntile(5)).toList());
	    }
	    
	    @Test
	    public void testWindowFunctionLead() {
	        assertEquals(optional(2, 4, 2, 3, null), SequenceM.of(1, 2, 4, 2, 3).window().map(Window::lead).toList());
	        assertEquals(optional(3, 4, 2, null, null), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2).map(Window::lead).toList());
	        
	        assertEquals(optional(2, 2, null, 3, 4), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder()).map(Window::lead).toList());
	        assertEquals(optional(3, 2, null, 4, null), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder()).map(Window::lead).toList());
	        
	        
	        assertEquals(optional(4, 2, 3, null, null), SequenceM.of(1, 2, 4, 2, 3).window().map(w -> w.lead(2)).toList());
	        assertEquals(optional(null, 2, null, null, null), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2).map(w -> w.lead(2)).toList());
	        
	        assertEquals(optional(2, 3, null, 4, null), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder()).map(w -> w.lead(2)).toList());
	        assertEquals(optional(null, 4, null, null, null), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder()).map(w -> w.lead(2)).toList());
	    }
	    
	    @Test
	    public void testWindowFunctionLag() {
	        assertEquals(optional(null, 1, 2, 4, 2), SequenceM.of(1, 2, 4, 2, 3).window().map(Window::lag).toList());
	        assertEquals(optional(null, null, 2, 4, 1), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2).map(Window::lag).toList());
	        
	        assertEquals(optional(null, 1, 3, 2, 2), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder()).map(Window::lag).toList());
	        assertEquals(optional(null, null, 2, 2, 1), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder()).map(Window::lag).toList());
	        
	        
	        assertEquals(optional(null, null, 1, 2, 4), SequenceM.of(1, 2, 4, 2, 3).window().map(w -> w.lag(2)).toList());
	        assertEquals(optional(null, null, null, 2, null), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2).map(w -> w.lag(2)).toList());
	        
	        assertEquals(optional(null, null, 2, 1, 2), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder()).map(w -> w.lag(2)).toList());
	        assertEquals(optional(null, null, 2, null, null), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder()).map(w -> w.lag(2)).toList());
	    }
	    
	    @Test
	    public void testWindowFunctionFirstValue() {
	        assertEquals(optional(1, 1, 1, 1, 1), SequenceM.of(1, 2, 4, 2, 3).window().map(Window::firstValue).toList());
	        assertEquals(optional(1, 1, 2, 4, 2), SequenceM.of(1, 2, 4, 2, 3).window(-1, 1).map(Window::firstValue).toList());
	        assertEquals(optional(null, 1, 1, 1, 2), SequenceM.of(1, 2, 4, 2, 3).window(-3, -1).map(Window::firstValue).toList());
	        
	        assertEquals(optional(1, 2, 2, 2, 1), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2).map(Window::firstValue).toList());
	        assertEquals(optional(1, 2, 2, 4, 1), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, -1, 1).map(Window::firstValue).toList());
	        assertEquals(optional(null, null, 2, 2, 1), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, -3, -1).map(Window::firstValue).toList());
	        
	        assertEquals(optional(1, 1, 1, 1, 1), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder()).map(Window::firstValue).toList());
	        assertEquals(optional(1, 1, 3, 2, 2), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder(), -1, 1).map(Window::firstValue).toList());
	        assertEquals(optional(null, 1, 2, 1, 1), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder(), -3, -1).map(Window::firstValue).toList());
	        
	        assertEquals(optional(1, 2, 2, 2, 1), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder()).map(Window::firstValue).toList());
	        assertEquals(optional(1, 2, 2, 2, 1), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder(), -1, 1).map(Window::firstValue).toList());
	        assertEquals(optional(null, null, 2, 2, 1), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder(), -3, -1).map(Window::firstValue).toList());
	    }
	    
	    @Test
	    public void testWindowFunctionLastValue() {
	        assertEquals(optional(3, 3, 3, 3, 3), SequenceM.of(1, 2, 4, 2, 3).window().map(Window::lastValue).toList());
	        assertEquals(optional(2, 4, 2, 3, 3), SequenceM.of(1, 2, 4, 2, 3).window(-1, 1).map(Window::lastValue).toList());
	        assertEquals(optional(null, 1, 2, 4, 2), SequenceM.of(1, 2, 4, 2, 3).window(-3, -1).map(Window::lastValue).toList());
	        
	        assertEquals(optional(3, 2, 2, 2, 3), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2).map(Window::lastValue).toList());
	        assertEquals(optional(3, 4, 2, 2, 3), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, -1, 1).map(Window::lastValue).toList());
	        assertEquals(optional(null, null, 2, 4, 1), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, -3, -1).map(Window::lastValue).toList());
	        
	        assertEquals(optional(1, 2, 4, 2, 3), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder()).map(Window::lastValue).toList());
	        assertEquals(optional(2, 2, 4, 3, 4), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder(), -1, 1).map(Window::lastValue).toList());
	        assertEquals(optional(null, 1, 3, 2, 2), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder(), -3, -1).map(Window::lastValue).toList());
	        
	        assertEquals(optional(1, 2, 4, 2, 3), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder()).map(Window::lastValue).toList());
	        assertEquals(optional(3, 2, 4, 4, 3), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder(), -1, 1).map(Window::lastValue).toList());
	        assertEquals(optional(null, null, 2, 2, 1), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder(), -3, -1).map(Window::lastValue).toList());
	    }
	    
	    @Test
	    public void testWindowFunctionNthValue() {
	        
	        // N = 0
	        assertEquals(optional(1, 1, 1, 1, 1), SequenceM.of(1, 2, 4, 2, 3).window().map(w -> w.nthValue(0)).toList());
	        assertEquals(optional(1, 1, 2, 4, 2), SequenceM.of(1, 2, 4, 2, 3).window(-1, 1).map(w -> w.nthValue(0)).toList());
	        assertEquals(optional(null, 1, 1, 1, 2), SequenceM.of(1, 2, 4, 2, 3).window(-3, -1).map(w -> w.nthValue(0)).toList());
	        
	        assertEquals(optional(1, 2, 2, 2, 1), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2).map(w -> w.nthValue(0)).toList());
	        assertEquals(optional(1, 2, 2, 4, 1), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, -1, 1).map(w -> w.nthValue(0)).toList());
	        assertEquals(optional(null, null, 2, 2, 1), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, -3, -1).map(w -> w.nthValue(0)).toList());
	        
	        assertEquals(optional(1, 1, 1, 1, 1), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder()).map(w -> w.nthValue(0)).toList());
	        assertEquals(optional(1, 1, 3, 2, 2), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder(), -1, 1).map(w -> w.nthValue(0)).toList());
	        assertEquals(optional(null, 1, 2, 1, 1), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder(), -3, -1).map(w -> w.nthValue(0)).toList());
	        
	        assertEquals(optional(1, 2, 2, 2, 1), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder()).map(w -> w.nthValue(0)).toList());
	        assertEquals(optional(1, 2, 2, 2, 1), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder(), -1, 1).map(w -> w.nthValue(0)).toList());
	        assertEquals(optional(null, null, 2, 2, 1), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder(), -3, -1).map(w -> w.nthValue(0)).toList());
	        

	        // N = 2
	        assertEquals(optional(4, 4, 4, 4, 4), SequenceM.of(1, 2, 4, 2, 3).window().map(w -> w.nthValue(2)).toList());
	        assertEquals(optional(null, 4, 2, 3, null), SequenceM.of(1, 2, 4, 2, 3).window(-1, 1).map(w -> w.nthValue(2)).toList());
	        assertEquals(optional(null, null, null, 4, 2), SequenceM.of(1, 2, 4, 2, 3).window(-3, -1).map(w -> w.nthValue(2)).toList());
	        
	        assertEquals(optional(null, 2, 2, 2, null), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2).map(w -> w.nthValue(2)).toList());
	        assertEquals(optional(null, null, 2, null, null), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, -1, 1).map(w -> w.nthValue(2)).toList());
	        assertEquals(optional(null, null, null, null, null), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, -3, -1).map(w -> w.nthValue(2)).toList());
	        
	        assertEquals(optional(null, null, 2, 2, 2), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder()).map(w -> w.nthValue(2)).toList());
	        assertEquals(optional(null, 2, null, 3, 4), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder(), -1, 1).map(w -> w.nthValue(2)).toList());
	        assertEquals(optional(null, null, 3, null, 2), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder(), -3, -1).map(w -> w.nthValue(2)).toList());
	        
	        assertEquals(optional(null, null, 4, null, null), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder()).map(w -> w.nthValue(2)).toList());
	        assertEquals(optional(null, null, null, 4, null), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder(), -1, 1).map(w -> w.nthValue(2)).toList());
	        assertEquals(optional(null, null, null, null, null), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder(), -3, -1).map(w -> w.nthValue(2)).toList());
	        
	        
	        // N = 3
	        assertEquals(optional(2, 2, 2, 2, 2), SequenceM.of(1, 2, 4, 2, 3).window().map(w -> w.nthValue(3)).toList());
	        assertEquals(optional(null, null, null, null, null), SequenceM.of(1, 2, 4, 2, 3).window(-1, 1).map(w -> w.nthValue(3)).toList());
	        assertEquals(optional(null, null, null, null, null), SequenceM.of(1, 2, 4, 2, 3).window(-3, -1).map(w -> w.nthValue(3)).toList());
	        
	        assertEquals(optional(null, null, null, null, null), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2).map(w -> w.nthValue(3)).toList());
	        assertEquals(optional(null, null, null, null, null), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, -1, 1).map(w -> w.nthValue(3)).toList());
	        assertEquals(optional(null, null, null, null, null), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, -3, -1).map(w -> w.nthValue(3)).toList());
	        
	        assertEquals(optional(null, null, 3, null, 3), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder()).map(w -> w.nthValue(3)).toList());
	        assertEquals(optional(null, null, null, null, null), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder(), -1, 1).map(w -> w.nthValue(3)).toList());
	        assertEquals(optional(null, null, null, null, null), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder(), -3, -1).map(w -> w.nthValue(3)).toList());
	        
	        assertEquals(optional(null, null, null, null, null), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder()).map(w -> w.nthValue(3)).toList());
	        assertEquals(optional(null, null, null, null, null), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder(), -1, 1).map(w -> w.nthValue(3)).toList());
	        assertEquals(optional(null, null, null, null, null), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder(), -3, -1).map(w -> w.nthValue(3)).toList());
	    }
	    
	    @Test
	    public void testWindowFunctionCount() {
	        assertEquals(asList(5L, 5L, 5L, 5L, 5L), SequenceM.of(1, 2, 4, 2, 3).window().map(Window::count).toList());
	        assertEquals(asList(2L, 3L, 3L, 3L, 2L), SequenceM.of(1, 2, 4, 2, 3).window(-1, 1).map(Window::count).toList());
	        assertEquals(asList(0L, 1L, 2L, 3L, 3L), SequenceM.of(1, 2, 4, 2, 3).window(-3, -1).map(Window::count).toList());
	        
	        assertEquals(asList(2L, 3L, 3L, 3L, 2L), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2).map(Window::count).toList());
	        assertEquals(asList(2L, 2L, 3L, 2L, 2L), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, -1, 1).map(Window::count).toList());
	        assertEquals(asList(0L, 0L, 1L, 2L, 1L), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, -3, -1).map(Window::count).toList());
	                
	        assertEquals(asList(1L, 2L, 5L, 3L, 4L), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder()).map(Window::count).toList());
	        assertEquals(asList(2L, 3L, 2L, 3L, 3L), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder(), -1, 1).map(Window::count).toList());
	        assertEquals(asList(0L, 1L, 3L, 2L, 3L), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder(), -3, -1).map(Window::count).toList());
	        
	        assertEquals(asList(1L, 1L, 3L, 2L, 2L), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder()).map(Window::count).toList());
	        assertEquals(asList(2L, 2L, 2L, 3L, 2L), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder(), -1, 1).map(Window::count).toList());
	        assertEquals(asList(0L, 0L, 2L, 1L, 1L), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder(), -3, -1).map(Window::count).toList());
	    }
	    
	    @Test
	    public void testWindowFunctionSum() {
	        assertEquals(optional(12, 12, 12, 12, 12), SequenceM.of(1, 2, 4, 2, 3).window().map(Window::sum).toList());
	        assertEquals(optional(3, 7, 8, 9, 5), SequenceM.of(1, 2, 4, 2, 3).window(-1, 1).map(Window::sum).toList());
	        assertEquals(optional(null, 1, 3, 7, 8), SequenceM.of(1, 2, 4, 2, 3).window(-3, -1).map(Window::sum).toList());
	        
	        assertEquals(optional(4, 8, 8, 8, 4), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2).map(Window::sum).toList());
	        assertEquals(optional(4, 6, 8, 6, 4), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, -1, 1).map(Window::sum).toList());
	        assertEquals(optional(null, null, 2, 6, 1), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, -3, -1).map(Window::sum).toList());
	                
	        assertEquals(optional(1, 3, 12, 5, 8), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder()).map(Window::sum).toList());
	        assertEquals(optional(3, 5, 7, 7, 9), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder(), -1, 1).map(Window::sum).toList());
	        assertEquals(optional(null, 1, 7, 3, 5), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder(), -3, -1).map(Window::sum).toList());
	        
	        assertEquals(optional(1, 2, 8, 4, 4), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder()).map(Window::sum).toList());
	        assertEquals(optional(4, 4, 6, 8, 4), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder(), -1, 1).map(Window::sum).toList());
	        assertEquals(optional(null, null, 4, 2, 1), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder(), -3, -1).map(Window::sum).toList());
	    }
	    
	    @Test
	    public void testWindowFunctionMax() {
	        assertEquals(optional(4, 4, 4, 4, 4), SequenceM.of(1, 2, 4, 2, 3).window().map(Window::max).toList());
	        assertEquals(optional(2, 4, 4, 4, 3), SequenceM.of(1, 2, 4, 2, 3).window(-1, 1).map(Window::max).toList());
	        assertEquals(optional(null, 1, 2, 4, 4), SequenceM.of(1, 2, 4, 2, 3).window(-3, -1).map(Window::max).toList());
	        
	        assertEquals(optional(3, 4, 4, 4, 3), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2).map(Window::max).toList());
	        assertEquals(optional(3, 4, 4, 4, 3), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, -1, 1).map(Window::max).toList());
	        assertEquals(optional(null, null, 2, 4, 1), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, -3, -1).map(Window::max).toList());
	                
	        assertEquals(optional(1, 2, 4, 2, 3), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder()).map(Window::max).toList());
	        assertEquals(optional(2, 2, 4, 3, 4), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder(), -1, 1).map(Window::max).toList());
	        assertEquals(optional(null, 1, 3, 2, 2), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder(), -3, -1).map(Window::max).toList());
	        
	        assertEquals(optional(1, 2, 4, 2, 3), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder()).map(Window::max).toList());
	        assertEquals(optional(3, 2, 4, 4, 3), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder(), -1, 1).map(Window::max).toList());
	        assertEquals(optional(null, null, 2, 2, 1), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder(), -3, -1).map(Window::max).toList());
	    }
	    
	    @Test
	    public void testWindowFunctionMin() {
	        assertEquals(optional(1, 1, 1, 1, 1), SequenceM.of(1, 2, 4, 2, 3).window().map(Window::min).toList());
	        assertEquals(optional(1, 1, 2, 2, 2), SequenceM.of(1, 2, 4, 2, 3).window(-1, 1).map(Window::min).toList());
	        assertEquals(optional(null, 1, 1, 1, 2), SequenceM.of(1, 2, 4, 2, 3).window(-3, -1).map(Window::min).toList());
	        
	        assertEquals(optional(1, 2, 2, 2, 1), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2).map(Window::min).toList());
	        assertEquals(optional(1, 2, 2, 2, 1), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, -1, 1).map(Window::min).toList());
	        assertEquals(optional(null, null, 2, 2, 1), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, -3, -1).map(Window::min).toList());
	                
	        assertEquals(optional(1, 1, 1, 1, 1), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder()).map(Window::min).toList());
	        assertEquals(optional(1, 1, 3, 2, 2), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder(), -1, 1).map(Window::min).toList());
	        assertEquals(optional(null, 1, 2, 1, 1), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder(), -3, -1).map(Window::min).toList());
	        
	        assertEquals(optional(1, 2, 2, 2, 1), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder()).map(Window::min).toList());
	        assertEquals(optional(1, 2, 2, 2, 1), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder(), -1, 1).map(Window::min).toList());
	        assertEquals(optional(null, null, 2, 2, 1), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder(), -3, -1).map(Window::min).toList());
	    }
	    
	    @Test
	    public void testWindowFunctionAll() {
	        assertEquals(asList(false, false, false, false, false), SequenceM.of(1, 2, 4, 2, 3).window().map(w -> w.allMatch(i -> i < 4)).toList());
	        assertEquals(asList(true, false, false, false, true), SequenceM.of(1, 2, 4, 2, 3).window(-1, 1).map(w -> w.allMatch(i -> i < 4)).toList());
	        assertEquals(asList(true, true, true, false, false), SequenceM.of(1, 2, 4, 2, 3).window(-3, -1).map(w -> w.allMatch(i -> i < 4)).toList());

	        assertEquals(asList(true, false, false, false, true), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2).map(w -> w.allMatch(i -> i < 4)).toList());
	        assertEquals(asList(true, false, false, false, true), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, -1, 1).map(w -> w.allMatch(i -> i < 4)).toList());
	        assertEquals(asList(true, true, true, false, true), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, -3, -1).map(w -> w.allMatch(i -> i < 4)).toList());

	        assertEquals(asList(true, true, false, true, true), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder()).map(w -> w.allMatch(i -> i < 4)).toList());
	        assertEquals(asList(true, true, false, true, false), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder(), -1, 1).map(w -> w.allMatch(i -> i < 4)).toList());
	        assertEquals(asList(true, true, true, true, true), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder(), -3, -1).map(w -> w.allMatch(i -> i < 4)).toList());

	        assertEquals(asList(true, true, false, true, true), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder()).map(w -> w.allMatch(i -> i < 4)).toList());
	        assertEquals(asList(true, true, false, false, true), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder(), -1, 1).map(w -> w.allMatch(i -> i < 4)).toList());
	        assertEquals(asList(true, true, true, true, true), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder(), -3, -1).map(w -> w.allMatch(i -> i < 4)).toList());
	    }
	        
	    @Test
	    public void testWindowFunctionAny() {
	        assertEquals(asList(true, true, true, true, true), SequenceM.of(1, 2, 4, 2, 3).window().map(w -> w.anyMatch(i -> i > 2)).toList());
	        assertEquals(asList(false, true, true, true, true), SequenceM.of(1, 2, 4, 2, 3).window(-1, 1).map(w -> w.anyMatch(i -> i > 2)).toList());
	        assertEquals(asList(false, false, false, true, true), SequenceM.of(1, 2, 4, 2, 3).window(-3, -1).map(w -> w.anyMatch(i -> i > 2)).toList());

	        assertEquals(asList(true, true, true, true, true), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2).map(w -> w.anyMatch(i -> i > 2)).toList());
	        assertEquals(asList(true, true, true, true, true), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, -1, 1).map(w -> w.anyMatch(i -> i > 2)).toList());
	        assertEquals(asList(false, false, false, true, false), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, -3, -1).map(w -> w.anyMatch(i -> i > 2)).toList());

	        assertEquals(asList(false, false, true, false, true), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder()).map(w -> w.anyMatch(i -> i > 2)).toList());
	        assertEquals(asList(false, false, true, true, true), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder(), -1, 1).map(w -> w.anyMatch(i -> i > 2)).toList());
	        assertEquals(asList(false, false, true, false, false), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder(), -3, -1).map(w -> w.anyMatch(i -> i > 2)).toList());

	        assertEquals(asList(false, false, true, false, true), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder()).map(w -> w.anyMatch(i -> i > 2)).toList());
	        assertEquals(asList(true, false, true, true, true), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder(), -1, 1).map(w -> w.anyMatch(i -> i > 2)).toList());
	        assertEquals(asList(false, false, false, false, false), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder(), -3, -1).map(w -> w.anyMatch(i -> i > 2)).toList());
	    }
	           
	    @Test
	    public void testWindowFunctionNone() {
	        assertEquals(asList(false, false, false, false, false), SequenceM.of(1, 2, 4, 2, 3).window().map(w -> w.noneMatch(i -> i > 2)).toList());
	        assertEquals(asList(true, false, false, false, false), SequenceM.of(1, 2, 4, 2, 3).window(-1, 1).map(w -> w.noneMatch(i -> i > 2)).toList());
	        assertEquals(asList(true, true, true, false, false), SequenceM.of(1, 2, 4, 2, 3).window(-3, -1).map(w -> w.noneMatch(i -> i > 2)).toList());

	        assertEquals(asList(false, false, false, false, false), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2).map(w -> w.noneMatch(i -> i > 2)).toList());
	        assertEquals(asList(false, false, false, false, false), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, -1, 1).map(w -> w.noneMatch(i -> i > 2)).toList());
	        assertEquals(asList(true, true, true, false, true), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, -3, -1).map(w -> w.noneMatch(i -> i > 2)).toList());

	        assertEquals(asList(true, true, false, true, false), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder()).map(w -> w.noneMatch(i -> i > 2)).toList());
	        assertEquals(asList(true, true, false, false, false), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder(), -1, 1).map(w -> w.noneMatch(i -> i > 2)).toList());
	        assertEquals(asList(true, true, false, true, true), SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder(), -3, -1).map(w -> w.noneMatch(i -> i > 2)).toList());

	        assertEquals(asList(true, true, false, true, false), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder()).map(w -> w.noneMatch(i -> i > 2)).toList());
	        assertEquals(asList(false, true, false, false, false), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder(), -1, 1).map(w -> w.noneMatch(i -> i > 2)).toList());
	        assertEquals(asList(true, true, true, true, true), SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder(), -3, -1).map(w -> w.noneMatch(i -> i > 2)).toList());
	    }
	          
	    @Test
	    public void testWindowFunctionCollect() {
	        assertEquals(asList(
	            asList(1, 2, 4, 2, 3), 
	            asList(1, 2, 4, 2, 3), 
	            asList(1, 2, 4, 2, 3), 
	            asList(1, 2, 4, 2, 3), 
	            asList(1, 2, 4, 2, 3)), 
	            SequenceM.of(1, 2, 4, 2, 3).window().map(w -> w.collect(toList())).toList());
	        
	        assertEquals(asList(
	            asList(1, 2), 
	            asList(1, 2, 4), 
	            asList(2, 4, 2), 
	            asList(4, 2, 3), 
	            asList(2, 3)), 
	            SequenceM.of(1, 2, 4, 2, 3).window(-1, 1).map(w -> w.collect(toList())).toList());
	        
	        assertEquals(asList(
	            asList(), 
	            asList(1), 
	            asList(1, 2), 
	            asList(1, 2, 4), 
	            asList(2, 4, 2)), 
	            SequenceM.of(1, 2, 4, 2, 3).window(-3, -1).map(w -> w.collect(toList())).toList());

	        
	        assertEquals(asList(
	            asList(1, 3), 
	            asList(2, 4, 2), 
	            asList(2, 4, 2), 
	            asList(2, 4, 2), 
	            asList(1, 3)),
	            SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2).map(w -> w.collect(toList())).toList());
	        
	        assertEquals(asList(
	            asList(1, 3), 
	            asList(2, 4), 
	            asList(2, 4, 2), 
	            asList(4, 2), 
	            asList(1, 3)),
	            SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, -1, 1).map(w -> w.collect(toList())).toList());
	        
	        assertEquals(asList(
	            asList(), 
	            asList(), 
	            asList(2), 
	            asList(2, 4), 
	            asList(1)),
	            SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, -3, -1).map(w -> w.collect(toList())).toList());

	        
	        assertEquals(asList(
	            asList(1), 
	            asList(1, 2), 
	            asList(1, 2, 2, 3, 4), 
	            asList(1, 2, 2), 
	            asList(1, 2, 2, 3)),
	            SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder()).map(w -> w.collect(toList())).toList());
	        
	        assertEquals(asList(
	            asList(1, 2), 
	            asList(1, 2, 2), 
	            asList(3, 4), 
	            asList(2, 2, 3), 
	            asList(2, 3, 4)),
	            SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder(), -1, 1).map(w -> w.collect(toList())).toList());
	        
	        assertEquals(asList(
	            asList(), 
	            asList(1), 
	            asList(2, 2, 3), 
	            asList(1, 2), 
	            asList(1, 2, 2)),
	            SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder(), -3, -1).map(w -> w.collect(toList())).toList());

	        
	        assertEquals(asList(
	            asList(1), 
	            asList(2), 
	            asList(2, 2, 4), 
	            asList(2, 2), 
	            asList(1, 3)),
	            SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder()).map(w -> w.collect(toList())).toList());
	        
	        assertEquals(asList(
	            asList(1, 3), 
	            asList(2, 2), 
	            asList(2, 4), 
	            asList(2, 2, 4), 
	            asList(1, 3)),
	            SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder(), -1, 1).map(w -> w.collect(toList())).toList());
	        
	        assertEquals(asList(
	            asList(), 
	            asList(), 
	            asList(2, 2), 
	            asList(2), 
	            asList(1)),
	            SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder(), -3, -1).map(w -> w.collect(toList())).toList());
	    }
	    
	    @SafeVarargs
	    private final <T> List<Optional<T>> optional(T... list) {
	        return SequenceM.of(list).map(Optional::ofNullable).toList();
	    }
	    **/

}
