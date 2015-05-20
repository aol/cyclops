package com.aol.cyclops.javaslang;



import static fj.data.List.list;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Stream;

import javaslang.CheckedFunction1;
import javaslang.control.Either;
import javaslang.control.Left;
import javaslang.control.Match;
import javaslang.control.None;
import javaslang.control.Option;
import javaslang.control.Right;
import javaslang.control.Some;
import javaslang.control.Try;
import lombok.val;
import lombok.experimental.ExtensionMethod;

import org.jooq.lambda.tuple.Tuple;
import org.junit.Test;



import fj.Ord;
import fj.data.List;
import fj.data.TreeMap;
/**
 * Created by johnmcclean on 4/10/15.
 */


public class Presentation {

	
	@Test
	public void stream(){
		
		Stream.of(1,2,3,400,500)
			.peek(System.out::println)
			.map(Object::toString)
			.map(String::length)
			.peek(System.out::println)
			.collect(Collectors.toList());
		
		
	}
	
	@Test
	public void curry(){
		
		val addTo100 = FromJDK.λ2((Integer a, Integer b) -> a+b).curried().apply(100);
		
		System.out.println(addTo100.apply(50));
		
		System.out.println(addTo100.apply(500));
		
	}
	
	
    @Test
    public void match() {


        String result = Match.caze((FileNotFoundException e) -> "file not found")
                .caze((Exception e) -> "general exception")
                .apply(new FileNotFoundException("test"));


        System.out.println("matched " + result);

    }

   

    @Test
    public void exceptionHandling(){

        Match.caze((Left<FileNotFoundException, String> exception) -> printStackTrace(exception))
            .caze((Right<FileNotFoundException, String> data) -> printData(data))
            .apply(loadFileToString(new File("./file.text")));


    }

    @Test
    public void exceptionlessFlow(){

        loadFileToString(new File("./file.text"))
                .right()
                .filter(data -> !data.isEmpty())
                .map(data -> data.right().get())
                .map(String::toLowerCase);

    }

    @Test
    public void optionalValues(){

        String value = Match.caze((Some<String> data) -> data.get())
                .caze((None<String> data) -> "not found")
                .apply(loadFromDb("id"));

        System.out.println(value);


    }
    @Test
    public void optionalFlow(){

        loadFromDb("id").map(String::toUpperCase)
                .forEach(System.out::println);


    }

    public String processFiles(){
        return "success";
    }
    @Test
    public void tryExample(){




       String result = Try.of(this::processFiles).recover(e ->
               Match.caze((FileNotFoundException ex) -> "file not found")
                       .caze(((IOException ex) -> "IO Exception"))
                       .apply(e)).orElse("unhandled error");



        System.out.println(result);




        Try.of(this::processFiles).recover ( e ->
                Match.caze((FileNotFoundException ex) -> "file not found")
                        .caze(((IOException ex)-> "IO Exception"))
                        .apply(e));

    }
    public Option<String> loadFromDb(String id){

        return Option.of("hello world");


    }

    public String printData(Right<FileNotFoundException, String> data){
        System.out.println(data.right().get());
        return "success";
    }

    public String printStackTrace(Left<FileNotFoundException, String> exception){
         exception.left().get().printStackTrace();
        return "failure";
    }

    public Either<FileNotFoundException,String> loadFileToString(File f){
        //return new Left(new FileNotFoundException("boo!"));
        return new Right("HELLO WORLD");
    }


    @Test
    public void checkedFunctions()  {

        CheckedFunction1 cf = (x) -> { throw new FileNotFoundException(); };
        try {
            cf.apply(100);
        }catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }

    @Test
    public void collections(){


        List<Integer> list = list(1, 2, 3);
        list.append(list(4,5,6)).forEach(System.out::println);


        val mapBuilder = new HashMap<Long,String>();
        mapBuilder.put(10l,"hello world");

        TreeMap<Long,String> map = TreeMap.fromMutableMap(Ord.longOrd, mapBuilder);
        map.forEach(System.out::println);

    }
}
