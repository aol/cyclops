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

import java.util.Set;

import org.openjdk.jmh.annotations.*;
import scrabble.Util;


@State(Scope.Benchmark)
public class ShakespearePlaysScrabble {

    static class MutableLong {
        long value;
        long get() {
            return value;
        }

        MutableLong set(long l) {
            value = l;
            return this;
        }

        MutableLong incAndSet() {
            value++;
            return this;
        }

        MutableLong add(MutableLong other) {
            value += other.value;
            return this;
        }
    }

    interface Wrapper<T> {
        T get() ;

        default Wrapper<T> set(T t) {
            return () -> t ;
        }
    }

    interface IntWrapper {
        int get() ;

        default IntWrapper set(int i) {
            return () -> i ;
        }

        default IntWrapper incAndSet() {
            return () -> get() + 1 ;
        }
    }

    interface LongWrapper {
        long get() ;

        default LongWrapper set(long l) {
            return () -> l ;
        }

        default LongWrapper incAndSet() {
            return () -> get() + 1L ;
        }

        default LongWrapper add(LongWrapper other) {
            return () -> get() + other.get() ;
        }
    }

    public static final int [] letterScores = {
            // a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p,  q, r, s, t, u, v, w, x, y,  z
            1, 3, 3, 2, 1, 4, 2, 4, 1, 8, 5, 1, 3, 1, 1, 3, 10, 1, 1, 1, 1, 4, 4, 8, 4, 10} ;

    public static final int [] scrabbleAvailableLetters = {
            // a, b, c, d,  e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z
            9, 2, 2, 1, 12, 2, 3, 2, 9, 1, 1, 4, 2, 6, 8, 2, 1, 6, 4, 6, 4, 2, 2, 1, 2, 1} ;


    public Set<String> scrabbleWords;
    public Set<String> shakespeareWords;

    @Setup
    public void init() {
        scrabbleWords = Util.readScrabbleWords() ;
        shakespeareWords = Util.readShakespeareWords() ;
    }

}