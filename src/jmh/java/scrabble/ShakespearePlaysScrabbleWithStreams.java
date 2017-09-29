//JMH Benchmarking code, not distributed with the release
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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;

/**
 * Shakespeare plays Scrabble with Streams.
 * @author José
 */
public abstract class ShakespearePlaysScrabbleWithStreams extends ShakespearePlaysScrabble {


    @SuppressWarnings("unused")
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
    public List<Entry<Integer, List<String>>> measureThroughput() {

        // Function to compute the score of a given word
        IntUnaryOperator scoreOfALetter = letter -> letterScores[letter - 'a'];

        // score of the same letters in a word
        ToIntFunction<Entry<Integer, Long>> letterScore =
                entry ->
                        letterScores[entry.getKey() - 'a'] *
                                Integer.min(
                                        entry.getValue().intValue(),
                                        scrabbleAvailableLetters[entry.getKey() - 'a']
                                );


        // Histogram of the letters in a given word
        Function<String, Map<Integer, Long>> histoOfLetters =
                word -> word.chars().boxed()
                        .collect(
                                Collectors.groupingBy(
                                        Function.identity(),
                                        Collectors.counting()
                                )
                        );

        // number of blanks for a given letter
        ToLongFunction<Entry<Integer, Long>> blank =
                entry ->
                        Long.max(
                                0L,
                                entry.getValue() -
                                        scrabbleAvailableLetters[entry.getKey() - 'a']
                        );

        // number of blanks for a given word
        Function<String, Long> nBlanks =
                word -> histoOfLetters.apply(word)
                        .entrySet().stream()
                        .mapToLong(blank)
                        .sum();

        // can a word be written with 2 blanks?
        Predicate<String> checkBlanks = word -> nBlanks.apply(word) <= 2;

        // score taking blanks into account
        Function<String, Integer> score2 =
                word -> histoOfLetters.apply(word)
                        .entrySet().stream()
                        .mapToInt(letterScore)
                        .sum();

        // Placing the word on the board
        // Building the streams of first and last letters
        Function<String, IntStream> first3 = word -> word.chars().limit(3);
        Function<String, IntStream> last3 = word -> word.chars().skip(Integer.max(0, word.length() - 4));

        // Stream to be maxed
        Function<String, IntStream> toBeMaxed =
                word -> Stream.of(first3.apply(word), last3.apply(word))
                        .flatMapToInt(Function.identity());

        // Bonus for double letter
        ToIntFunction<String> bonusForDoubleLetter =
                word -> toBeMaxed.apply(word)
                        .map(scoreOfALetter)
                        .max()
                        .orElse(0);

        // score of the word put on the board
        Function<String, Integer> score3 =
                word ->
                        (score2.apply(word) + bonusForDoubleLetter.applyAsInt(word))
                                + (score2.apply(word) + bonusForDoubleLetter.applyAsInt(word))
                                + (word.length() == 7 ? 50 : 0);

        Function<Function<String, Integer>, Stream<Map<Integer, List<String>>>> buildHistoOnScore =
                score -> Stream.of(buildShakerspeareWordsStream()
                        .filter(scrabbleWords::contains)
                        // .filter(canWrite)    // filter out the words that needs blanks
                        .filter(checkBlanks) // filter out the words that needs more than 2 blanks
                        .collect(
                                Collectors.groupingBy(
                                        score,
                                        () -> new TreeMap<Integer, List<String>>(Comparator.reverseOrder()),
                                        Collectors.toList()
                                )
                        ));


        // best key / value pairs
        List<Entry<Integer, List<String>>> finalList =
                buildHistoOnScore.apply(score3).map(e->e.entrySet()
                        .stream()
                        .limit(3)
                        .collect(Collectors.toList())).findAny().get() ;

//        System.out.println(finalList) ;

        return finalList ;
    }

    abstract Stream<String> buildShakerspeareWordsStream() ;
}