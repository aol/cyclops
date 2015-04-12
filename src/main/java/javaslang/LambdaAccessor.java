package javaslang;

import java.io.Serializable;
import java.lang.invoke.MethodType;

/**
 * Created by johnmcclean on 4/12/15.
 */
public class LambdaAccessor {

    public static MethodType getLambdaSignature(Serializable lambda){
        return javaslang.λ.getLambdaSignature(lambda);
    }
}
