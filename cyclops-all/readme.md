
# FluentFunctions

## AOP

### Before advice 

    int set;
    public boolean events(Integer i){
        return set==i;
    }
    
    set = 0;
    FluentFunctions.of(this::events)
                   .before(i->set=i)
                    .println()
                    .apply(10);
    
    
    (fluent-function-Parameter[10])
    (fluent-function-Result[true])
    
### After advice  


setIn= 0;
setOut = true

FluentFunctions.of(this::events)
               .after((in,out)->{setIn=in;setOut=out;} )
               .println()
               .apply(10);
               
(fluent-function-Parameter[10])
(fluent-function-Result[false])

setIn =10
setOut = false               

### Around advice

public int addOne(int i ){
        return i+1;
}

FluentFunctions.of(this::addOne)
                       .around(advice->advice.proceed(advice.param+1))
                       .println()
                       .apply(10)
 
(fluent-function-Parameter[10])
(fluent-function-Result[12])
                       
//12 because addOne adds one and so does the around advice
   
## Retry

int times =0;
public String exceptionalFirstTime(String input) throws IOException{
        if(times==0){
            times++;
            throw new IOException();
        }
        return input + " world"; 
}
    
FluentFunctions.ofChecked(this::exceptionalFirstTime)
                       .println()
                       .retry(2,500)
                       .apply("hello");   

(fluent-function-Parameter[hello])
java.io.IOException
    at com.aol.cyclops.functions.fluent.FunctionsTest.exceptionalFirstTime(FunctionsTest.java:95)
   ...
(fluent-function-Parameter[hello])
(fluent-function-Result[hello world])

          
## Recover

int times =0;
public String exceptionalFirstTime(String input) throws IOException{
        if(times==0){
            times++;
            throw new IOException();
        }
        return input + " world"; 
}

FluentFunctions.ofChecked(this::exceptionalFirstTime)
                        .recover(IOException.class, in->in+"boo!")
                        .println()
                        .apply("hello ");   
                        
(fluent-function-Parameter[hello ])
(fluent-function-Result[hello boo!])                               
                       
## Caching

int called;
public int addOne(int i ){
        called++;
       return i+1;
}

Function<Integer,Integer> fn = FluentFunctions.of(this::addOne)
                                              .name("myFunction")
                                              .memoize();

fn.apply(10);
fn.apply(10);
fn.apply(10);

called is 1
        
## Printing function data

public int addOne(int i ){
        return i+1;
}
    
FluentFunctions.of(this::addOne)
               .name("myFunction")
               .println()
               .apply(10)
               
(myFunction-Parameter[10])
(myFunction-Result[11])
               