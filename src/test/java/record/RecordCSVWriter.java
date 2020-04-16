package record;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class RecordCSVWriter extends FileWriter {
    private static final String CSV_SEP = ";";

    public RecordCSVWriter(String fileName) throws IOException {
        super(fileName, false);
        write(String.join(CSV_SEP,
                          "solverName",
                          "problemSize",
                          "scenario",
                          "testIndex",
                          "objective",
                          "time"
                         ) + "\n");
    }

    private String cvsJoin(Object... values) {
        return Arrays.stream(values).map(Object::toString)
                .collect(Collectors.joining(CSV_SEP));
    }

    public void write(Record... records) throws IOException {
        for (Record record : records) {
            write(cvsJoin(
                    record.getSolverName(),
                    record.getProblemSize(),
                    record.getScenario(),
                    record.getTestIndex(),
                    record.getObjective(),
                    record.getTime()
                         ) + "\n");
        }
    }
}
