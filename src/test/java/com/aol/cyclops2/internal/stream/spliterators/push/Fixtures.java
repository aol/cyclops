package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.function.Consumer;
import java.util.function.LongConsumer;

/**
 * Created by johnmcclean on 17/01/2017.
 */
public class Fixtures {
    public static Operator<Integer> twoAndErrorSource = new Operator<Integer>(){

        @Override
        public StreamSubscription subscribe(Consumer<? super Integer> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
            return new StreamSubscription(){
                int index = 1;
                @Override
                public void request(long n) {
                    super.request(n);
                    while(isActive()){
                        if(index==3) {
                            index++;
                            onError.accept(new RuntimeException());

                            onComplete.run();
                            cancel();
                        }

                        else if(index<3)
                            onNext.accept(index++);
                        else
                            break;

                        requested.decrementAndGet();
                    }
                }

                @Override
                public void cancel() {
                    super.cancel();
                }
            };
        }

        @Override
        public void subscribeAll(Consumer<? super Integer> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
            onNext.accept(1);
            onNext.accept(2);
            onError.accept(new RuntimeException());
            onComplete.run();
        }
    };
    public static Operator<Integer> threeAndErrorSource = new Operator<Integer>(){

        @Override
        public StreamSubscription subscribe(Consumer<? super Integer> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
            return new StreamSubscription(){
                int index = 1;
                @Override
                public void request(long n) {
                    super.request(n);
                    while(isActive()){
                        if(index==4) {
                            index++;
                            onError.accept(new RuntimeException());

                            onComplete.run();
                            cancel();
                        }

                        else if(index<4)
                            onNext.accept(index++);
                        else
                            break;

                        requested.decrementAndGet();
                    }
                }

                @Override
                public void cancel() {
                    super.cancel();
                }
            };
        }

        @Override
        public void subscribeAll(Consumer<? super Integer> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
            onNext.accept(1);
            onNext.accept(2);
            onError.accept(new RuntimeException());
            onComplete.run();
        }
    };
    public static Operator<Integer> oneAndErrorSource = new Operator<Integer>(){

        @Override
        public StreamSubscription subscribe(Consumer<? super Integer> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
            return new StreamSubscription(){

                int index = 2;
                @Override
                public void request(long n) {
                    super.request(n);

                    while(isActive()){
                        if(index==3) {
                            index++;
                            onError.accept(new RuntimeException());

                            onComplete.run();
                            cancel();
                        }

                        else if(index==2)
                            onNext.accept(index++);
                        else
                            break;

                        requested.decrementAndGet();
                    }
                }

                @Override
                public void cancel() {
                    super.cancel();
                }
            };
        }

        @Override
        public void subscribeAll(Consumer<? super Integer> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {

            onNext.accept(2);
            onError.accept(new RuntimeException());
            onComplete.run();
        }
    };
    public static Operator<Integer> threeErrorsSource = new Operator<Integer>(){

        @Override
        public StreamSubscription subscribe(Consumer<? super Integer> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
            return new StreamSubscription(){
                int index = 0;
                @Override
                public void request(long n) {
                    super.request(n);
                    while(isActive()) {
                        if (index++ < 3)
                            onError.accept(new RuntimeException());
                        if(index>=3){
                            cancel();
                            onComplete.run();
                        }
                        requested.decrementAndGet();
                    }
                }

                @Override
                public void cancel() {
                    super.cancel();
                }
            };
        }

        @Override
        public void subscribeAll(Consumer<? super Integer> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
            onError.accept(new RuntimeException());
            onError.accept(new RuntimeException());
            onError.accept(new RuntimeException());
            onComplete.run();
        }
    };
}
