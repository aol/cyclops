//JMH Benchmarking test file : not part of distribution
/*
 * Copyright (C) 2015 José Paumard
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; lazyEither version 2
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

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Util {

    private Util() { }

    public static Set<String> readScrabbleWords() {
        Set<String> scrabbleWords = new HashSet<>() ;

            System.out.println(Util.class.getResource("files/ospd.txt"));


            try (Stream<String> scrabbleWordsStream = Files.lines(Paths.get("src/jmh/resources/files","ospd.txt"))) {
                scrabbleWords.addAll(scrabbleWordsStream.map(String::toLowerCase).collect(Collectors.toSet()));
            } catch (IOException e) {
                e.printStackTrace();
            }

        return scrabbleWords ;
    }

    public static Set<String> readShakespeareWords() {
        Set<String> shakespeareWords = new HashSet<>() ;


            try (Stream<String> shakespeareWordsStream = Files.lines(Paths.get("src/jmh/resources/files","words.shakespeare.txt"))) {
                shakespeareWords.addAll(shakespeareWordsStream.map(String::toLowerCase).collect(Collectors.toSet()));
            } catch (IOException e) {
                e.printStackTrace();
            }


        return shakespeareWords ;
    }
}