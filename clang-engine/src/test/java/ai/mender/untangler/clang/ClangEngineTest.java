package ai.mender.untangler.clang;

import ai.mender.untangler.shared.Ast;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

class ClangEngineTest {

    public static final String BUILD_RESOURCES_PATH = "clang-engine/build/resources/test";
    public static final String RESOURCES_PATH = "clang-engine/src/test/resources";

    private String getTestResourcePath(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        return file.getAbsolutePath();
    }

    private static String convertBuildResourcePathToSourcePath(String path) {
        return path.replace(BUILD_RESOURCES_PATH, RESOURCES_PATH);
    }

    @Test
    public void filterOutsideFileFromClangAst() throws IOException {
        String clangAstPath = getTestResourcePath("clang-ast-system-header-example.json");
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode clangAst = objectMapper.reader().readTree(new FileReader(clangAstPath));
        JsonNode clangAstFiltered = ClangEngine.filterClangAstByFile(clangAst, new ClangParseConfig("/tmp/class.cpp"));
        Approvals.verify(objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(clangAstFiltered));
    }

    @Test
    public void simpleCFileToAst() throws IOException {
        approveAstTransformation("onevar.c");
    }

    @Test
    public void structCExampleToAst() throws IOException {
        approveAstTransformation("struct.c");
    }

    @Test
    public void classCppExampleToAst() throws IOException {
        approveAstTransformation("class.cpp");
    }

    private void approveAstTransformation(String fileName) throws IOException {
        // Generate and save clang ast if it's missing but otherwise use the one that's there,
        // so we can run the tests without clang.
        String codePath = getTestResourcePath(fileName);
        String clangAstSourcePath = convertBuildResourcePathToSourcePath(codePath + ".clang-ast.json");
        File clangAstSourchFile = new File(clangAstSourcePath);
        if (!clangAstSourchFile.exists()) {
            System.out.println("Writing " + clangAstSourcePath);
            ClangParseConfig config = new ClangParseConfig(codePath);
            JsonNode clangAst = ClangEngine.dumpClangAstJson(config);
            JsonNode filteredClangAst = ClangEngine.filterClangAstByFile(clangAst, config);
            ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
            writer.writeValue(clangAstSourchFile, filteredClangAst);
        }
        JsonNode clangAst = new ObjectMapper().reader().readTree(new FileReader(clangAstSourchFile));
        Ast ast = ClangEngine.transformClangAstToUnifiedAst(clangAst, new ClangParseConfig(codePath));
        Approvals.verify(Main.getAstJsonWriter().writeValueAsString(ast));
    }
}