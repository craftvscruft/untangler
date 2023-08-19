package ai.mender;

import ai.mender.domain.FunctionRec;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GenerateReflectConfig {
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
        String packageName = FunctionRec.class.getPackageName();
        InputStream stream = ClassLoader.getSystemClassLoader()
                .getResourceAsStream(packageName.replaceAll("[.]", "/"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        List<ConfigEntry> configEntries = new ArrayList<>();
        reader.lines()
                .filter(line -> line.endsWith(".class"))
                .map(line -> getClass(line, packageName))
                .map(klass ->
                        new ConfigEntry(
                                klass.getCanonicalName(),
                                true,
                                true,
                                true,
                                Arrays.stream(klass.getDeclaredMethods()).map(m -> new MethodEntry(
                                        m.getName(), Arrays.stream(m.getParameterTypes()).map(Class::getCanonicalName).toArray(String[]::new)
                                )).toArray(MethodEntry[]::new)
                        )).forEachOrdered(configEntries::add);
        configEntries.add(RECORD_COMPONENT_ENTRY);
        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(configEntries));
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
