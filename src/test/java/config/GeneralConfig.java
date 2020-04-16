package config;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.function.Supplier;

public class GeneralConfig {
    public static final String PROJECT_DATA_DIR = System.getProperty("user.home") + File.separator + "ChiPlanAnalytic";
    public static final String INSTANCE_DIR = PROJECT_DATA_DIR + File.separator + "instance";
    public static final String RESULT_DIR = PROJECT_DATA_DIR + File.separator + "result";
    private static final String JSON_FORMAT = ".json";
    private static final String DAT_FORMAT = ".dat";
    private static final DateTimeFormatter dtF = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final String INSTANCE_PREFIX = "instance";
    public static final Supplier<String> SYNTHESIS_CSV_GETTER = () -> RESULT_DIR + File.separator + dtF.format(LocalDateTime.now()) + ".csv";

    public static final String[] SCENARIO_SET = new String[]{
            "uniform",
            "daily",
            "weekly",
            "weekend",
    };
    public static final int NUMBER_OF_TESTS = 10;
    public static final int[] PROBLEM_SIZES = new int[]{
            15,
            30,
            60,
            90,
            120,
            150,
            180,
            210
    };
    public static final int TIME_SLOTS_PER_DAY = 22;
    public static final int HORIZON_LENGTH = 28;
    public static final int MULTITASK = 6;
    public static final int MAX_DOCTOR = 5;
    public static final int MAX_NURSES = 6;
    public static final int MATERIALS = 30;
    private static final boolean[] BOOLEANS = {true, false};
    public static final int[] CYCLE_DEMANDS_PER_PATIENT = new int[]{1, 2, 3};
    public static final int[] RDV_DELAYS = new int[]{1, 2, 3};
    public static final int[] TREATMENT_DURATIONS = new int[]{0, 1, 2, 3};
    public static final int[] MED_PREP_DURATIONS = new int[]{0, 1, 2, 3};
    public static final int[] SECTORS = new int[]{0};

    private static final Random RANDOMIZE = new Random();

    public static int randomPick(int[] ints) {
        return ints[RANDOMIZE.nextInt(ints.length)];
    }

    public static int randomPick(int exclusiveBound) {
        return RANDOMIZE.nextInt(exclusiveBound);
    }

    public static boolean randomTrueFalse() {
        return BOOLEANS[RANDOMIZE.nextInt(2)];
    }

    public static String getJsonInstanceFile(int problemSize, String scenario, int testIndex) {
        return INSTANCE_DIR + File.separator +
                INSTANCE_PREFIX + "_" + getInstanceName(problemSize, scenario, testIndex) + JSON_FORMAT;
    }

    public static String getJsonInstanceFile(String instanceName) {
        return INSTANCE_DIR + File.separator + instanceName + JSON_FORMAT;
    }

    public static String getDatInstanceFile(int problemSize, String scenario, int testIndex) {
        return INSTANCE_DIR + File.separator +
                INSTANCE_PREFIX + "_" + getInstanceName(problemSize, scenario, testIndex) + DAT_FORMAT;
    }

    public static String getInstanceName(int problemSize, String scenario, int testIndex) {
        return problemSize + "_" + scenario + "_" + testIndex;
    }
}
