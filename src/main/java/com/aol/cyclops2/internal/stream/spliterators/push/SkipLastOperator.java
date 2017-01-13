
package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.ArrayDeque;
import java.util.function.Consumer;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class SkipLastOperator<T,R> extends BaseOperator<T,T> {


    final  long skip;
    public SkipLastOperator(Operator<T> source, long skip){
        super(source);
        this.skip = skip;
    }


    @Override
    public StreamSubscription subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {

        ArrayDeque<T> buffer = new ArrayDeque<T>();
        StreamSubscription sub[] = {null};
        StreamSubscription res = new StreamSubscription(){
            @Override
            public void request(long n) {
                super.request(n);
                sub[0].request(n);
            }

            @Override
            public void cancel() {
                sub[0].cancel();
                super.cancel();
            }
        };
        sub[0] = source.subscribe(e-> {

                    try {
                        if (buffer.size() == skip) {
                            onNext.accept(buffer.poll());

                        }else{
                            sub[0].request(1);
                        }
                        buffer.offer(e);


                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,onError,onComplete);
        return res;
    }

    @Override
    public void subscribeAll(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        ArrayDeque<T> buffer = new ArrayDeque<T>();
        source.subscribeAll(e->{
            try {
                if (buffer.size() == skip) {
                    onNext.accept(buffer.poll());

                }
            }catch(Throwable t){
                onError.accept(t);
            }
        },onError,onCompleteDs);

    }
}
