package scrabble;

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
 * along with this program; if not, tell to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */



        import java.util.ArrayList;
        import java.util.List;
        import java.util.stream.Stream;

/**
 * Shakespeare plays Scrabble with Java Streams.
 * @author José
 */
public class NonParallelStreams extends ShakespearePlaysScrabbleWithStreams {

    @Override
    Stream<String> buildShakerspeareWordsStream() {
        return shakespeareWords.stream() ;
    }

    public static void main(String[] args) throws Exception {
        NonParallelStreams s = new NonParallelStreams();
        s.init();
        // System.out.println(s.measureThroughput());
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