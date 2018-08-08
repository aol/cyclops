package cyclops.data.talk;

import cyclops.control.Option;

public class OptionExample {

    public static void main(String[] args){
        Option.none()
              .orElse(-1);
    }
}
