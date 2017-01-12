package com.aol.cyclops2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import cyclops.Streams;

import cyclops.async.LazyReact;
import cyclops.stream.FutureStream;
import cyclops.stream.ReactiveSeq;
import cyclops.async.Queue;
import cyclops.async.QueueFactories;

public class Javaone {

    
    
    
    
    private String unreliableMethod(String in){
        return "";
    }
    private String load(int i){
        return "";
        
    }
    public String loadStr(String load){
        return "";
    }
    private void expensiveOp(int i){
        
    }
    
    private void save(String in){
        
    }
    /**
    public void optional(){
        String prefix = "file";
        List<String> data;
        
        for(int i=0;i<100;i++){
            String nextFile = prefix+i;
            data.add(loadStr(nextFile));
            
        }
        
        Stream.iterate(0,i->i+1)   
              .limit(100)
              .map(i->"prefix"+i)
              .map(this::loadStr)
              .collect(Collectors.toList());
        
        new LazyReact().of(1,2,3,4)
                       .map(this::load)
                       .forEach(this::save);
        
        
        
        ReactiveSeq.of(1,2,3)
                   .schedule("* * * * * ?", Executors.newScheduledThreadPool(1))
                   .connect()
                   .debounce(1, TimeUnit.SECONDS)
                   .forEach(System.out::println);
        
        
        
        ReactiveSeq.of(1,2,3,4)
                   .futureOperations(Executors.newFixedThreadPool(1))
                   .forEach(this::expensiveOp);
        
        
        
        Subscription s = ReactiveSeq.of(1,2,3,4)
                                    .forEach(2,
                                                    System.out::println, 
                                                    System.err::println,
                                                    ()->System.out.println("complete"));
        
        s.request(2);
        
        
        SeqSubscriber<Integer> sub = SeqSubscriber.subscriber();
        Flux.just(1,2,3,4)
            .map(i->i*2)
            .subscribeAll(sub);
        
        ReactiveSeq<Integer> connected = sub.stream();
        
        ReactiveSeq.of(1,2,3)
                   .map(this::load)
                   .recover(e->"default value")
                   .retry(this::unreliableMethod);
        
        CompletableFuture f;
        f.then
        
        
        Seq.of("a","b","c","d")
            .map(String::toUpperCase)
           .zipWithIndex()
           .filter(t->t.v2%2==0)
           .sliding(3)
           .duplicate();
       
        Optional<Integer> input;
        Optional<Integer> times2 = input.map(i->i*2);
                
        
        QueueFactories.<Data>boundedQueue(100)
                      .build()
                      .futureStream()
                      .map(this::process)
                      .run();
        
    }
    public void stream(){
        
        Stream<Integer> input;
        Stream<Integer> times2 = input.map(i->i*2);
                
        
    }
    public void future(){
        
        CompletableFuture<Integer> input;
        CompletableFuture<Integer> times2 = input.thenApply(i->i*2);
                
        
    }
    
    public void dateTime(){
        
        LocalDate date = LocalDate.of(2016, 9, 18); 
        boolean later = LocalDate.now().isAfter(date);
    }
     Seq.of(1, 2, 4)
        .rightOuterJoin(Seq.of(1, 2, 3), (a, b) -> a == b);
    ReactiveSeq.of(6,5,2,1)
                    .map(e->e*100)
                    .filter(e->e<551)
                    .futureOperations(Executors.newFixedThreadPool(1))
                    .forEach(e-> {
                        System.out.println("Element " + e + " on thread " + Thread.currentThread().getId());
                    });
    **/
    
    public int loadData(int size){
        List<String> list = new ArrayList<>();
        for(int i=0;i<size;i++)
                list.add(""+size);
        return list.size();
    }
    
    /**
     * new LazyReact(Executors.newFixedThreadPool(4)).of(6,5,2,1)
                                                      .map(this::loadData)
                                                      .map(List::size)
                                                      .peek(e->{
                                                          System.out.println("data size is " + e + " on thread "  + Thread.currentThread().getId());
                                                      })
                                                      .map(e->e*100)
                                                      .filter(e->e<551)
                                                      .forEach(System.out::println);
     */
    
   
    
    public String supplyData(){
        try{
            Thread.sleep(500);
        }catch(Exception e){
            
        }
        return "data";
    }
    
    public String process(String in){
        return "produced on " + Thread.currentThread().getId();
    }
    
    
    public void queue(){
        
        
        Queue<String> transferQueue = QueueFactories.<String>boundedQueue(4)
                                                 .build();

        new LazyReact(Executors.newFixedThreadPool(4)).generate(()->"data")
                                                      .map(d->"produced on " + Thread.currentThread().getId())
                                                      .peek(System.out::println)
                                                      .peek(d->transferQueue.offer(d))
                                                      .run();
        

        transferQueue.stream()
                  .map(e->"Consumed on " + Thread.currentThread().getId())
                  .runFuture(Executors.newFixedThreadPool(1),s->s.forEach(System.out::println));
        
        
        
        
        while(true){
          //  System.out.println(inputQueue.size());
        }
        
        
    }
    
   
    public void queue2(){
        
        
        
        Queue<String> inputQueue = QueueFactories.<String>boundedQueue(4)
                                                 .build();
        
        new LazyReact(Executors.newFixedThreadPool(4)).generate(this::supplyData)
                                                      .map(e->"Produced on " + Thread.currentThread().getId())
                                                      .peek(System.out::println)
                                                      .peek(d->inputQueue.offer(d))
                                                      .run();
        
        inputQueue.stream()
                  .map(e->"Consumed on " + Thread.currentThread().getId())
                  .runFuture(Executors.newFixedThreadPool(1),s->s.forEach(System.out::println));
        
        
        
        
        
        
        
        
        
        
        
        while(true){
            //  System.out.println(inputQueue.size());
          }
        
        
        
    }
    
   
    public void streamException(){
        
        try {
            Stream.generate(() -> "next")
                  .map(s -> {
                      throw new RuntimeException();
                  })
                  .forEach(System.out::println);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        
    }
   
    public void reactiveSeqException(){
        
  
            ReactiveSeq.iterate(0,i->i+1)
                        .map(i -> { 
                          if( i%3==0)
                              throw new RuntimeException("" + i);
                            return "success " + i;
                        })
                        .recover(e->"failed " + e.getMessage())
                        .peek(System.out::println)
                        .scheduleFixedDelay(1_000, Executors.newScheduledThreadPool(1));
                        
               
            while(true);
        
        
    }
   
    public void streamEmission(){
        
        Streams.scheduleFixedDelay(Stream.iterate(0, i->i+1)
                                             .peek(System.out::println), 
                                       1_000, Executors.newScheduledThreadPool(1));
        
        
        
        
        while(true);
        
        
        
        
        
    }
    
  
    public void futureStream(){
        
        new LazyReact(Executors.newFixedThreadPool(4)).of(6,5,2,1)
                                                      .map(this::loadData)
                                                      .map(e->e*100)
                                                      .filter(e->e<551)
                                                      .peek(e->{
                                                          System.out.println("e is " + e 
                                                                              + " on thread " 
                                                                              + Thread.currentThread().getId());
                                                      })
                                                      .runOnCurrent();
        
        
        
         
        
        
        
        
        
        
        
        
        
        
        
        
        
    }
    
    public void reactiveSeq(){
        ReactiveSeq.of(6,5,2,1)
        .map(e->e*100)
        .filter(e->e<551)
         .runFuture(Executors.newFixedThreadPool(1),s->s.forEach(e->{
            System.out.println("Element " + e + " on thread " + Thread.currentThread().getId());
        }));
    }
    
    public static void main(String[] args){
        
        for (int i = 0; i < 4; i++) {

            ReactiveSeq.of(6, 5, 2, 1)
                       .map(e -> e * 100)
                       .filter(e -> e < 551)
                    .runFuture(Executors.newFixedThreadPool(1),s->s.forEach(e -> {
                           System.out.println("Element " + e + " on thread " + Thread.currentThread()
                                                                                     .getId());
                       }));

        }
        
      
            for(int i=0;i<4;i++){
                
                ReactiveSeq.of(6,5,2,1)
                           .map(e->e*100)
                           .filter(e->e<551)
                        .runFuture(Executors.newFixedThreadPool(1),s->s.forEach(e->{
                               System.out.println("Element " + e 
                                                  + " on thread " 
                                                  + Thread.currentThread().getId());
                           }));
                          
                
                
            }
        
        
        
        
        
       
        
        
    }
    
    
}