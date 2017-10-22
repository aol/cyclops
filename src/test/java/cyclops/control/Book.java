package cyclops.control;


import java.util.function.Function;

public abstract class Book {

    //no other sub-class of Book can be created
    //the type hierarchy is Sealed by this private constructor
    private Book(String isbn) {
        this.isbn = isbn;
    }

    private final String isbn;



    public abstract LazyEither<Fiction,NonFiction> match();

    public static class Fiction extends Book{

        private Fiction(String isbn) {
            super(isbn);
        }

        @Override
        public LazyEither<Fiction, NonFiction> match() {
            return LazyEither.left(this);
        }


    }
    public static class NonFiction extends Book{

        private NonFiction(String isbn) {
            super(isbn);
        }

        @Override
        public LazyEither<Fiction, NonFiction> match() {
            return LazyEither.right(this);
        }


    }

    public static Fiction fiction(String isbn){
        return new Fiction(isbn);
    }
    public static NonFiction nonfiction(String isbn){
        return new NonFiction(isbn);
    }


    public <R> R visit(Function<String,? extends R> visitor){
        return visitor.apply(isbn);
    }



    public static void main(String[] args){



        Book functionalProgrammingInJava = nonfiction("978-1617292736");
        Integer a = functionalProgrammingInJava.match()
                                               .visit(fiction->1, nonFiction -> 100);





    }
    /**
    public static void main(String args[]){


        Fiction frankenstein = fiction("978-0486282114");


        String fIsbn = frankenstein.visit(Function.identity());

        Book functionalProgrammingInJava = nonfiction("978-1617292736");

        Integer a = null;
        if(functionalProgrammingInJava instanceof NonFiction)
            a = 100;
        else if(functionalProgrammingInJava instanceof Fiction);
            a=  1;


        Integer b = functionalProgrammingInJava.visit(fiction->1, nonFiction -> 100);



    }
     **/
}


