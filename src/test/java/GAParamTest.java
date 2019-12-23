import com.google.gson.stream.JsonReader;
import io.jenetics.Alterer;
import io.jenetics.EnumGene;
import io.jenetics.PartiallyMatchedCrossover;
import io.jenetics.SwapMutator;
import optalp.chtplanning.common.Param;
import optalp.chtplanning.common.PatientCycleDemand;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class GAParamTest {
    private static final String GA_TEST_DIR = GeneralConfig.PROJECT_DATA_DIR + File.separator + "result_ga";
    private static final int[] POPULATION_SIZES = new int[]{
            10,
            100,
            1000,
            10000
    };
    private static final int MAX_GEN = 1000;
    private static final int REPETITIONS = 100;
    private static final Map<String, Alterer<EnumGene<PatientCycleDemand>, Integer>> ALTERER_MAP = new LinkedHashMap<>();
    private static final String[] INSTANCE_NAMES = new String[]{
            "instance_15_uniform_1",
            "instance_30_uniform_1",
            "instance_60_uniform_1",
            "instance_90_uniform_1",
            "instance_120_uniform_1",
            "instance_15_daily_2",
            "instance_30_daily_2",
            "instance_60_daily_2",
            "instance_90_daily_2",
            "instance_120_daily_2",
            "instance_15_weekly_1",
            "instance_30_weekly_1",
            "instance_60_weekly_1",
            "instance_90_weekly_1",
            "instance_120_weekly_1",
            "instance_15_weekend_1",
            "instance_30_weekend_1",
            "instance_60_weekend_1",
            "instance_90_weekend_1",
            "instance_120_weekend_1",
    };

    @BeforeAll
    static void init() {
        Utils.createDir();
        new File(GA_TEST_DIR).mkdirs();
        double[] psmProbabilities = {
                0.2,
                0.8,
        };
        double[] pmcProbabilities = {
                0.2,
                0.8,
        };
        for (final double psmProb : psmProbabilities) {
            for (final double pmcProb : pmcProbabilities) {
                ALTERER_MAP.put("psm-" + psmProb + "+" + "pmc-" + pmcProb,
                                new SwapMutator<EnumGene<PatientCycleDemand>, Integer>(psmProb)
                                        .andThen(new PartiallyMatchedCrossover<>(pmcProb))
                               );
            }
        }
    }

    private static Stream<Arguments> getGeneratedDataset() {
        List<Arguments> arguments = new ArrayList<>();
        for (String instanceName : INSTANCE_NAMES) {
            for (String mutatorName : ALTERER_MAP.keySet()) {
                for (final int populationSize : POPULATION_SIZES) {
                    arguments.add(Arguments.of(instanceName, populationSize, mutatorName));
                }
            }
        }
        return arguments.stream();
    }

    @ParameterizedTest
    @MethodSource("getGeneratedDataset")
    void run(String instanceName, int populationSize, String mutatorName) throws FileNotFoundException {
        System.out.println("Resolution for " + instanceName + " with population " + populationSize + " and mutator " + mutatorName);
        Instance instance = Utils.GSON.fromJson(new JsonReader(new FileReader(GeneralConfig.getJsonInstanceFile(instanceName))),
                                                Instance.class);
        List<PatientCycleDemand> cycleDemands = instance.getDemands();
        Param param = instance.getParam();
        try (PrintWriter pw = new PrintWriter(GA_TEST_DIR + File.separator
                                                      + "result_" + instanceName + "_" + mutatorName + "_" + populationSize
                                                      + ".txt")) {
            for (int i = 0; i < REPETITIONS; i++) {
                System.out.println("Repetition " + (i + 1) + " of " + REPETITIONS);
                CustomGASolver gaSolver = new CustomGASolver(populationSize, MAX_GEN, ALTERER_MAP.get(mutatorName));
                gaSolver.solve(param, cycleDemands, new ArrayList<>());
                pw.println(gaSolver.getBestObjectivesEvolution().stream().map(Object::toString).collect(Collectors.joining(",")));
            }
        }
    }
}
