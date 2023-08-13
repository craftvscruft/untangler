package ai.mender;

import ai.mender.domain.FunctionRec;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

public class Console {
    public static <T> void printJson(T result) throws JsonProcessingException {
        System.out.println(Language.toJson(result));
    }
}
