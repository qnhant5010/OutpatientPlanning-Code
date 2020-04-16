package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import config.GeneralConfig;
import optalp.chtplanning.common.solution.Solution;
import record.Record;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

public class Utils {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void createDir() {
        //noinspection ResultOfMethodCallIgnored
        Arrays.asList(
                GeneralConfig.PROJECT_DATA_DIR,
                GeneralConfig.INSTANCE_DIR,
                GeneralConfig.RESULT_DIR)
              .forEach(dir -> new File(dir).mkdirs());
    }

    public static void writeSolution(String solverName,
                                     int problemSize,
                                     String scenario,
                                     int testIndex,
                                     Record record,
                                     Solution<?> solution)
            throws IOException {
        try (PrintWriter pw = new PrintWriter(
                GeneralConfig.RESULT_DIR + File.separator
                + "result_" + solverName + "_"
                + GeneralConfig.getInstanceName(problemSize, scenario, testIndex)
                + ".txt"
        )) {
            if (solution == null) {
                pw.println("No solution");
                return;
            }
            pw.println("objectiveValue = " + record.getObjective());
            pw.println("Solved time (ms) = " + record.getTime());
            pw.println("Solution:");
            solution.viewPlannings().forEach((demandId, planning) -> {
                pw.println("Patient " + (demandId + 1));
                planning.forEach(allocation -> pw.println(String.format(
                        "Rdv %d : A %d %d --> %d %d  B %d %d --> %d %d  C %d %d --> %d %d  D %d %d --> %d %d",
                        allocation.id(),
                        allocation.sessionDay(),
                        allocation.startConsultation() + 1,
                        allocation.sessionDay(),
                        allocation.endConsultation() + 1,
                        allocation.sessionDay(),
                        allocation.startInstallation() + 1,
                        allocation.sessionDay(),
                        allocation.endInstallation() + 1,
                        allocation.medPrepDay(),
                        allocation.startMedPrep() + 1,
                        allocation.medPrepDay(),
                        allocation.endMedPrep() + 1,
                        allocation.sessionDay(),
                        allocation.startTreatment() + 1,
                        allocation.sessionDay(),
                        allocation.endTreatment() + 1)));
            });
        }
    }
}
