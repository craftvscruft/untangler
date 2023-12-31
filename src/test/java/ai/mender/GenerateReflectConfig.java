package ai.mender;

import ai.mender.untangler.shared.response.SourceEdit;
import ai.mender.untangler.shared.response.FunctionRec;
import ai.mender.untangler.shared.response.SourcePosition;
import ai.mender.untangler.shared.response.SourceRange;
import ai.mender.untangler.shared.response.SourceText;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.ast.Modifier;
import com.google.common.io.Files;
import com.google.common.reflect.ClassPath;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GenerateReflectConfig {
    // This must be re-run on changes to the domain package, and the tmp file copied.
    // TODO: Make this auto-run and dump to src/main/resources/META-INF/native-image/reflect-config.json
    public record ConfigEntry(String name, boolean allDeclaredFields, boolean queryAllDeclaredMethods, boolean queryAllDeclaredConstructors, MethodEntry[] methods) {
    }
    public record MethodEntry(String name, String[] parameterTypes) {
    }

    private static final ConfigEntry RECORD_COMPONENT_ENTRY = new ConfigEntry(
            "java.lang.reflect.RecordComponent",
            true,
            true,
            true,
            new MethodEntry[] {
                    new MethodEntry("getName", new String[]{}),
                    new MethodEntry("getType", new String[]{})
            }
    );

    public static void main(String[] args) throws IOException {
        List<ConfigEntry> configEntries = new ArrayList<>();
        // Naming all in the subproject explicitly for now
        configEntries.add(getConfigEntryForClass(SourcePosition.class));
        configEntries.add(getConfigEntryForClass(SourceRange.class));
        configEntries.add(getConfigEntryForClass(SourceText.class));
        addEntriesInPackage(FunctionRec.class.getPackageName(), configEntries);
        addEntriesInPackage("com.github.javaparser.ast", configEntries);

        configEntries.add(RECORD_COMPONENT_ENTRY);
        File tempFile = File.createTempFile("reflect-config", "json");
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(tempFile, configEntries);
        System.out.println("Next copy it:");
        System.out.println("cp " + tempFile.getAbsolutePath() + " src/main/resources/META-INF/native-image/reflect-config.json");
    }

    private static void addEntriesInPackage(String packageName, List<ConfigEntry> configEntries) throws IOException {
        ClassPath classPath = ClassPath.from(Thread.currentThread().getContextClassLoader());
        classPath.getTopLevelClassesRecursive(packageName).stream()
                .forEachOrdered(classInfo ->
                {
                    try {
                        Class<?> klass = Class.forName(classInfo.getName());
                        configEntries.add(getConfigEntryForClass(klass));
                        for (Class<?> innerClass : klass.getDeclaredClasses()) {
                            configEntries.add(getConfigEntryForClass(innerClass));
                        }
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                });
    }



    private static ConfigEntry getConfigEntryForClass(Class<?> klass) {
        return new ConfigEntry(
                klass.getName(),
                true,
                true,
                true,
                Arrays.stream(klass.getDeclaredMethods()).map(m -> new MethodEntry(
                        m.getName(), Arrays.stream(m.getParameterTypes()).map(Class::getName).toArray(String[]::new)
                )).toArray(MethodEntry[]::new)
        );
    }

    private static Class getClass(String className, String packageName) {
        try {
            return Class.forName(packageName + "."
                    + className.substring(0, className.lastIndexOf('.')));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
