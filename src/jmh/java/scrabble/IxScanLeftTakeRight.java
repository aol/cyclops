/*
 * Copyright (C) 2015 José Paumard
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package scrabble;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;


import ix.*;

/**
 * Shakespeare plays Scrabble with Ix.
 * @author José
 * @author akarnokd
 */
public class IxScanLeftTakeRight extends ShakespearePlaysScrabble {

    /*
    Result: 12,690 ±(99.9%) 0,148 s/op [Average]
              Statistics: (min, avg, max) = (12,281, 12,690, 12,784), stdev = 0,138
              Confidence interval (99.9%): [12,543, 12,838]
              Samples, N = 15
                    mean =     12,690 ±(99.9%) 0,148 s/op
                     min =     12,281 s/op
              p( 0,0000) =     12,281 s/op
              p(50,0000) =     12,717 s/op
              p(90,0000) =     12,784 s/op
              p(95,0000) =     12,784 s/op
              p(99,0000) =     12,784 s/op
              p(99,9000) =     12,784 s/op
              p(99,9900) =     12,784 s/op
              p(99,9990) =     12,784 s/op
              p(99,9999) =     12,784 s/op
                     max =     12,784 s/op


            # Run complete. Total time: 00:06:26

            Benchmark                                               Mode  Cnt   Score   Error  Units
            ShakespearePlaysScrabbleWithRxJava.measureThroughput  sample   15  12,690 ± 0,148   s/op

            Benchmark                                              Mode  Cnt       Score      Error  Units
            ShakespearePlaysScrabbleWithRxJava.measureThroughput   avgt   15  250074,776 ± 7736,734  us/op
            ShakespearePlaysScrabbleWithStreams.measureThroughput  avgt   15   29389,903 ± 1115,836  us/op

    */
    @SuppressWarnings({ "unchecked", "unused" })
    @Benchmark
    @BenchmarkMode(Mode.SampleTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(
        iterations = 5
    )
    @Measurement(
        iterations = 5
    )
    @Fork(1)
    public List<Entry<Integer, List<String>>> measureThroughput() throws InterruptedException {

        // Function to compute the score of a given word
        IxFunction<Integer, Ix<Integer>> scoreOfALetter = letter -> Ix.just(letterScores[letter - 'a']) ;

        // score of the same letters in a word
        IxFunction<Entry<Integer, LongWrapper>, Ix<Integer>> letterScore =
                entry ->
                    Ix.just(
                        letterScores[entry.getKey() - 'a'] *
                        Integer.min(
                                (int)entry.getValue().get(),
                                scrabbleAvailableLetters[entry.getKey() - 'a']
                            )
                    ) ;

        IxFunction<String, Ix<Integer>> toIntegerIx =
                string -> Ix.from(IterableSpliterator.of(string.chars().boxed().spliterator())) ;

        // Histogram of the letters in a given word
        IxFunction<String, Ix<HashMap<Integer, LongWrapper>>> histoOfLetters =
                word -> toIntegerIx.apply(word)
                            .collect(
                                () -> new HashMap<>(),
                                (HashMap<Integer, LongWrapper> map, Integer value) ->
                                    {
                                        LongWrapper newValue = map.get(value) ;
                                        if (newValue == null) {
                                            newValue = () -> 0L ;
                                        }
                                        map.put(value, newValue.incAndSet()) ;
                                    }

                            ) ;

        // number of blanks for a given letter
        IxFunction<Entry<Integer, LongWrapper>, Ix<Long>> blank =
                entry ->
                    Ix.just(
                        Long.max(
                            0L,
                            entry.getValue().get() -
                            scrabbleAvailableLetters[entry.getKey() - 'a']
                        )
                    ) ;

        // number of blanks for a given word
        IxFunction<String, Ix<Long>> nBlanks =
                word -> histoOfLetters.apply(word)
                            .flatMap(map -> Ix.from(() -> map.entrySet().iterator()))
                            .flatMap(blank)
                            .sumLong();


        // can a word be written with 2 blanks?
        IxFunction<String, Ix<Boolean>> checkBlanks =
                word -> nBlanks.apply(word)
                            .flatMap(l -> Ix.just(l <= 2L)) ;

        // score taking blanks into account letterScore1
        IxFunction<String, Ix<Integer>> score2 =
                word -> histoOfLetters.apply(word)
                            .flatMap(map -> Ix.from(() -> map.entrySet().iterator()))
                            .flatMap(letterScore)
                            .sumInt();

        // Placing the word on the board
        // Building the streams of takeOne and last letters
        IxFunction<String, Ix<Integer>> first3 =
                word -> Ix.from(IterableSpliterator.of(word.chars().boxed().limit(3).spliterator())) ;
        IxFunction<String, Ix<Integer>> last3 =
                word -> Ix.from(IterableSpliterator.of(word.chars().boxed().skip(3).spliterator())) ;


        // Stream to be maxed
        IxFunction<String, Ix<Integer>> toBeMaxed =
            word -> Ix.fromArray(first3.apply(word), last3.apply(word))
                        .flatMap(observable -> observable) ;

        // Bonus for double letter
        IxFunction<String, Ix<Integer>> bonusForDoubleLetter =
            word -> toBeMaxed.apply(word)
                        .flatMap(scoreOfALetter)
                        .maxInt();

        // score of the word put on the board
        IxFunction<String, Ix<Integer>> score3 =
            word ->
                Ix.fromArray(
                        score2.apply(word),
                        score2.apply(word),
                        bonusForDoubleLetter.apply(word),
                        bonusForDoubleLetter.apply(word),
                        Ix.just(word.length() == 7 ? 50 : 0)
                )
                .flatMap(observable -> observable)
                .sumInt() ;

        IxFunction<IxFunction<String, Ix<Integer>>, Ix<TreeMap<Integer, List<String>>>> buildHistoOnScore =
                score -> Ix.from(() -> shakespeareWords.iterator())
                                .filter(scrabbleWords::contains)
                                .filter(word -> checkBlanks.apply(word).first())
                                .collect(
                                    () -> new TreeMap<Integer, List<String>>(Comparator.reverseOrder()),
                                    (TreeMap<Integer, List<String>> map, String word) -> {
                                        Integer key = score.apply(word).first() ;
                                        List<String> list = map.get(key) ;
                                        if (list == null) {
                                            list = new ArrayList<>() ;
                                            map.put(key, list) ;
                                        }
                                        list.add(word) ;
                                    }
                                ) ;


        // best key / value pairs
        List<Entry<Integer, List<String>>> finalList2 =
                buildHistoOnScore.apply(score3)
                    .flatMap(map -> Ix.from(() -> map.entrySet().iterator()))
                    .take(3)
                    .collect(
                        () -> new ArrayList<Entry<Integer, List<String>>>(),
                        (list, entry) -> {
                            list.add(entry) ;
                        }
                    )
                    .first() ;


//        System.out.println(finalList2);

        return finalList2 ;
    }
    public static void main(String[] args) throws Exception {
        
        //10289
        IxScanLeftTakeRight s = new IxScanLeftTakeRight();
        s.init();
        System.out.println(s.measureThroughput());
        /**
        Long time = System.currentTimeMillis();
        for(int i=0;i<100;i++){
        System.out.println(s.measureThroughput());
        }
        System.out.println( System.currentTimeMillis()-time); 
        System.out.println(s.measureThroughput());**/
        int count =0;
        boolean run = true;
        while(run)
        {
            long start = System.currentTimeMillis();
            for(int i=0;i<100;i++)
                count +=s.measureThroughput().size();
            System.out.println("Time " + (System.currentTimeMillis()-start));
        }
        System.out.println( "" + count);
    }
}