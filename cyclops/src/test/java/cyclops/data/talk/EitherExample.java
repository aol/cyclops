package cyclops.data.talk;

import cyclops.control.Either;

public class EitherExample {

    public static void main(String[] args){

        Either<Integer,String> right = Either.right("hello");

        Either<Integer,String> left = Either.left(10);

        System.out.println(right.map(str->str+" world"));

        System.out.println(left.mapLeft(i->i*2));
    }
}
