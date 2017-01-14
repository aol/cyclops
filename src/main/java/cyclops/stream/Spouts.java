package cyclops.stream;

import com.aol.cyclops2.internal.stream.ReactiveStreamX;
import com.aol.cyclops2.internal.stream.StreamX;
import com.aol.cyclops2.internal.stream.spliterators.push.*;
import lombok.experimental.UtilityClass;

import java.util.stream.Stream;

/**
 * Created by johnmcclean on 14/01/2017.
 */
@UtilityClass
public class Spouts {

    public ReactiveSeq<Integer> range(int start, int end){
        return new ReactiveStreamX<Integer>(new RangeIntOperator(start,end));
    }
    public ReactiveSeq<Long> rangeLong(long start, long end){
        return new ReactiveStreamX<>(new RangeLongOperator(start,end));
    }
    public <T> ReactiveSeq<T> of(T value){
        return new ReactiveStreamX<>(new SingleValueOperator<T>(value));
    }
    public <T> ReactiveSeq<T> of(T... values){
        return new ReactiveStreamX<>(new ArrayOfValuesOperator<T>(values));
    }
    public <T> ReactiveSeq<T> concat(Stream<T>... streams){
        Operator<T>[] operators = new Operator[streams.length];
        int index = 0;
        for(Stream<T> next : streams){
            if(next instanceof ReactiveStreamX){
                operators[index] = ((ReactiveStreamX)next).getSource();
            }else{
                operators[index] = new SpliteratorToOperator<T>(next.spliterator());
            }
            index++;
        }

        return new ReactiveStreamX<>(new ArrayConcatonatingOperator<T>(operators));
    }
}
