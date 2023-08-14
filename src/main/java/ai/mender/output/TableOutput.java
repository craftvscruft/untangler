package ai.mender.output;

import org.apache.commons.lang3.StringUtils;

import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TableOutput {
    public static <T extends Record> void writeTableOutput(PrintWriter printWriter, List<T> items, Class<T> recClass) {
        RecordComponent[] rcs = recClass.getRecordComponents();
        if (rcs.length == 0) {
            return;
        }

        var names = Arrays.stream(rcs).map(RecordComponent::getName).map(StringUtils::capitalize).toList();

        int columnCount = rcs.length;

        String paddingForAllFields = computePaddingFormat(items, rcs, columnCount);
        printWriter.format(paddingForAllFields, names.toArray());

        printWriter.println();
        for (Record item : items) {
            Object[] strings = Arrays.stream(rcs).map(rc -> getRecordValue(rc, item)).map(Object::toString).toArray();
            printWriter.format(paddingForAllFields, strings);
            printWriter.println();
        }
        printWriter.flush();
    }

    private static String computePaddingFormat(List<? extends Record> items, RecordComponent[] rcs, int columnCount) {
        List<Integer> maxLengthByColumn = Arrays.stream(rcs).map(rc -> getMaxLengthOfRecordFieldValue(rc, items)).toList();
        String paddedForAllButLast = maxLengthByColumn.stream()
                .map(maxWidth -> "%-" + (maxWidth + 1) + "s")
                .limit(columnCount - 1)
                .collect(Collectors.joining());
        return paddedForAllButLast.concat("%s");
    }

    private static int getMaxLengthOfRecordFieldValue(RecordComponent rc, List<? extends Record> items) {
        Stream<Integer> itemFieldValueLengths = items.stream().map(item ->
                getRecordValue(rc, item).toString().length());
        Stream<Integer> headerAndItemFieldValueLengths = Stream.concat(Stream.of(rc.getName().length()), itemFieldValueLengths);
        return headerAndItemFieldValueLengths.max(Integer::compare).orElse(0);
    }

    private static <T extends Record> Object getRecordValue(RecordComponent rc, T value) {
        try {
            return rc.getAccessor().invoke(value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
