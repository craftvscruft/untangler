package ai.mender.untangler.shared;

import ai.mender.untangler.shared.response.FunctionRec;

import java.io.Reader;
import java.util.stream.Stream;

public interface LanguageEngine {
    Stream<FunctionRec> functions(ISourceFile sourceFile);
}
