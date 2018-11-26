package com.oath.cyclops.internal.stream.spliterators.push;

import cyclops.data.Seq;
import lombok.AllArgsConstructor;
import org.reactivestreams.Publisher;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@AllArgsConstructor
public class AmbOperator <IN> implements Operator<IN> {

    private final Operator<IN>[] racers;
    private Seq<Racer> activeRacers;

    public AmbOperator(Publisher<IN>[] pubs){
        racers = new Operator[pubs.length];
        for(int i=0;i<pubs.length;i++){
            racers[i] = new PublisherToOperator<>(pubs[i]);
        }
    }

    AtomicReference<Racer> winner = new AtomicReference<>(null);
    @Override
    public StreamSubscription subscribe(Consumer<? super IN> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        if(racers.length==1){
            return racers[0].subscribe(onNext,onError,onComplete);
        }else{
            activeRacers = Seq.empty();
            for(Operator<IN> next : racers){
                Racer racer = new Racer(onNext,onError,onComplete);
                StreamSubscription sub = next.subscribe(racer::onNext, racer::onError, racer::onComplete);
                racer.sub=sub;
                activeRacers = activeRacers.plus(racer);
            }
        }
        return new StreamSubscription(){
            @Override
            public void request(long n) {

                Racer local = winner.get();
                    if (local!=null) {
                        local.request(n);
                    }
                    else {
                        for (Racer s : activeRacers) {
                            s.request(n);
                        }
                    }

            }

            @Override
            public void cancel() {
                Racer local = winner.get();
                if (local!=null) {
                    local.cancel();
                }
                else {
                    for (Racer s : activeRacers) {
                        s.cancel();
                    }
                }
            }
        };
    }

    @Override
    public void subscribeAll(Consumer<? super IN> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        if(racers.length==1){
            racers[0].subscribeAll(onNext,onError,onComplete);
        }else{
            activeRacers = Seq.empty();
            for(Operator<IN> next : racers){
                Racer racer = new Racer(onNext,onError,onComplete);
                next.subscribeAll(racer::onNext, racer::onError, racer::onComplete);

                activeRacers = activeRacers.plus(racer);
            }
        }
    }


    class Racer{
        private final Consumer<? super IN> onNext;
        private final Consumer<? super Throwable> onError;
        private final Runnable onComplete;

        public Racer(Consumer<? super IN> onNext, Consumer<? super Throwable> onError, Runnable onComplete){
            this.onNext= onNext;
            this.onError = onError;
            this.onComplete = onComplete;
        }
        boolean won =false;
        StreamSubscription sub;


        public void onNext(IN value){
            if(won)
                onNext.accept(value);
           else if(winner.compareAndSet(null,this)){
               won=true;
               onNext.accept(value);
                activeRacers.peek(i->{
                    if(i!=this)
                        i.cancel();
                });

           }
        }

        public void onError(Throwable throwable) {
            if(won)
                onError.accept(throwable);
            else if(winner.compareAndSet(null,this)){
                won=true;
                onError.accept(throwable);
                activeRacers.peek(i->{
                    if(i!=this)
                        i.cancel();
                });
            }
        }
        public void onComplete(){
            if(won)
               onComplete.run();
            else if(winner.compareAndSet(null,this)){
                won=true;
                onComplete.run();
                activeRacers.peek(i->{
                    if(i!=this)
                        i.cancel();
                });
            }
        }
        public void request(long n) {
            sub.request( n);
        }
        public void cancel(){
            sub.cancel();
        }
    }
}
