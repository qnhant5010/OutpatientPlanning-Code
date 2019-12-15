import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.function.Supplier;

class GeneralConfig {
    static final String PROJECT_DATA_DIR = System.getProperty("user.home") + File.separator + "OptaSolver";
    static final String INSTANCE_DIR = PROJECT_DATA_DIR + File.separator + "instance";
    static final String RESULT_DIR = PROJECT_DATA_DIR + File.separator + "result";
    private static final String JSON_FORMAT = ".json";
    private static final String DAT_FORMAT = ".dat";
    private static final DateTimeFormatter dtF = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final String INSTANCE_PREFIX = "instance";
    static final Supplier<String> SYNTHESIS_CSV_GETTER = () -> RESULT_DIR + File.separator + dtF.format(LocalDateTime.now()) + ".csv";

    static final String[] SCENARIO_SET = new String[]{
            "uniform",
            "daily",
            "weekly",
            "weekend",
    };
    static final int NUMBER_OF_TESTS = 10;
    static final int[] PROBLEM_SIZES = new int[]{
//            15,
//            30,
//            60,
            90,
            120,
//            150
    };
    static final int TIME_SLOTS_PER_DAY = 22;
    static final int HORIZON_LENGTH = 28;
    static final int MULTITASK = 6;
    static final int MAX_DOCTOR = 5;
    static final int MAX_NURSES = 6;
    static final int MATERIALS = 30;
    private static final boolean[] BOOLEANS = {true, false};
    static final int[] CYCLE_DEMANDS_PER_PATIENT = new int[]{1, 2, 3};
    static final int[] RDV_DELAYS = new int[]{1, 2, 3};
    static final int[] TREATMENT_DURATIONS = new int[]{0, 1, 2, 3};
    static final int[] MED_PREP_DURATIONS = new int[]{0, 1, 2, 3};
    static final int[] SECTORS = new int[]{0};

    private static final Random RANDOMIZE = new Random();

    static int randomPick(int[] ints) {
        return ints[RANDOMIZE.nextInt(ints.length)];
    }

    static int randomPick(int exclusiveBound) {
        return RANDOMIZE.nextInt(exclusiveBound);
    }

    static boolean randomTrueFalse() {
        return BOOLEANS[RANDOMIZE.nextInt(2)];
    }

    static String getJsonInstanceFile(int problemSize, String scenario, int testIndex) {
        return INSTANCE_DIR + File.separator +
                INSTANCE_PREFIX + "_" + getInstanceName(problemSize, scenario, testIndex) + JSON_FORMAT;
    }

    static String getJsonInstanceFile(String instanceName) {
        return INSTANCE_DIR + File.separator + instanceName + JSON_FORMAT;
    }

    static String getDatInstanceFile(int problemSize, String scenario, int testIndex) {
        return INSTANCE_DIR + File.separator +
                INSTANCE_PREFIX + "_" + getInstanceName(problemSize, scenario, testIndex) + DAT_FORMAT;
    }

    static String getInstanceName(int problemSize, String scenario, int testIndex) {
        return problemSize + "_" + scenario + "_" + testIndex;
    }
}
