package ai.mender.output;

import ai.mender.domain.FunctionRec;
import org.apache.commons.lang3.StringUtils;

import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TableOutput {
    public static void writeTableOutput(Writer writer, List<FunctionRec> items, Class<FunctionRec> recClass) {
        RecordComponent[] rcs = recClass.getRecordComponents();
        PrintWriter printWriter = new PrintWriter(writer);
        if (rcs.length == 0) {
            return;
        }
        var names = Arrays.stream(rcs).map(RecordComponent::getName).map(StringUtils::capitalize).toList();

        int columnCount = rcs.length;

        List<Integer> maxLengthByColumn = Arrays.stream(rcs).map(rc -> getMaxLengthOfRecordFieldValue(rc, items)).toList();
        String paddedForAllButLast = maxLengthByColumn.stream()
                .map(maxWidth -> "%-" + (maxWidth + 1) + "s")
                .limit(columnCount - 1)
                .collect(Collectors.joining());
        printWriter.format(paddedForAllButLast.concat("%s"), names.toArray());

        printWriter.println();
        for (Record item : items) {
            Object[] strings = Arrays.stream(rcs).map(rc -> getRecordValue(rc, item)).map(Object::toString).toArray();
            printWriter.format(paddedForAllButLast.concat("%s"), strings);
            printWriter.println();
        }
    }

    private static int getMaxLengthOfRecordFieldValue(RecordComponent rc, List<FunctionRec> items1) {
        return items1.stream().map(item ->
                getRecordValue(rc, item).toString().length()).max(Integer::compare).orElse(0);
    }

    private static <T extends Record> Object getRecordValue(RecordComponent rc, T value) {
        try {
            return rc.getAccessor().invoke(value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
