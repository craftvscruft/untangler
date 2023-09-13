package ai.mender;

import ai.mender.domain.FunctionRec;
import ai.mender.untangler.shared.SourcePosition;
import ai.mender.untangler.shared.SourceRange;
import ai.mender.untangler.shared.SourceText;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GenerateReflectConfig {
    // This must be re-run on changes to the domain package
    // TODO: Make this auto-run and dump to META-INF/native-image/reflect-config.json
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

    public static void main(String[] args) throws JsonProcessingException {
        List<ConfigEntry> configEntries = new ArrayList<>();
        // Naming all in the subproject explicitly for now
        configEntries.add(getConfigEntryForClass(SourcePosition.class));
        configEntries.add(getConfigEntryForClass(SourceRange.class));
        configEntries.add(getConfigEntryForClass(SourceText.class));
        addEntriesInPackage(FunctionRec.class.getPackageName(), configEntries);
        configEntries.add(RECORD_COMPONENT_ENTRY);
        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(configEntries));
    }

    private static void addEntriesInPackage(String packageName, List<ConfigEntry> configEntries) {
        InputStream stream = ClassLoader.getSystemClassLoader()
                .getResourceAsStream(packageName.replaceAll("[.]", "/"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        reader.lines()
                .filter(line -> line.endsWith(".class"))
                .map(line -> getClass(line, packageName))
                .map(klass ->
                        getConfigEntryForClass(klass)).forEachOrdered(configEntries::add);
    }

    private static ConfigEntry getConfigEntryForClass(Class klass) {
        return new ConfigEntry(
                klass.getCanonicalName(),
                true,
                true,
                true,
                Arrays.stream(klass.getDeclaredMethods()).map(m -> new MethodEntry(
                        m.getName(), Arrays.stream(m.getParameterTypes()).map(Class::getCanonicalName).toArray(String[]::new)
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
