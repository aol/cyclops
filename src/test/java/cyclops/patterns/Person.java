package cyclops.patterns;

import cyclops.patterns.matchers.SealedMatcher2;
import lombok.*;
import org.jooq.lambda.tuple.Tuple2;

import java.util.function.Function;

import static org.jooq.lambda.tuple.Tuple.tuple;



@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class Person implements CaseClass2<String,Integer>, Sealed2<Person.Male,Person.Female> {


    @Value(staticConstructor="female")
    public static class Female extends Person {
        String name;
        Integer age;
    }

    @Value(staticConstructor="male")
    public static class Male extends Person {
        String name;
        Integer age;
    }


    @Override
    public <R> R match(Function<? super Male, ? extends R> fn1, Function<? super Female, ? extends R> fn2) {
        return new SealedMatcher2<>(Person.this, Person.Male.class, Person.Female.class).match(fn1,fn2);
    }

    public Tuple2<String,Integer> unapply() {
        return match(m-> tuple(m.name,m.age),
                              f->tuple(f.name,f.age));
    }

}

