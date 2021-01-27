package solvertest;

import com.google.gson.stream.JsonReader;
import config.GeneralConfig;
import instance.Instance;
import io.jenetics.*;
import optalp.chtplanning.common.Param;
import optalp.chtplanning.common.PatientCycleDemand;
import optalp.chtplanning.common.Solver;
import optalp.chtplanning.common.SolverException;
import optalp.chtplanning.common.objective.IntegerObjective;
import optalp.chtplanning.common.solution.Solution;
import optalp.chtplanning.heuristicsolver.CIPT_FF_SumC_Solver;
import optalp.chtplanning.simplegasolver.CustomizableGASolver;
import optalp.chtplanning.simplegasolver.SimpleGASolver;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import record.Record;
import record.RecordCSVWriter;
import utils.Utils;

import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

class SolverTest {
    private static RecordCSVWriter CSV_WRITER;
    private static final int RUN_MAX_ITERATION = 50;
    public static final int[] POP_SIZES = new int[]{40, 80};
    public static final int[] GEN_COUNTS = new int[]{25, 50};
    private static final Map<Integer, Executor> executorMap = new HashMap<>();
    private static CustomizableGASolver bestGASolver;

    @BeforeAll
    static void init() throws IOException {
        Utils.createDir();
        CSV_WRITER =
                new RecordCSVWriter(GeneralConfig.SYNTHESIS_CSV_GETTER.get());
        for (int threadCount = 0; threadCount < Runtime.getRuntime().availableProcessors(); threadCount = threadCount + 2) {
            executorMap.put(threadCount,
                            threadCount == 0 ? Executors.newSingleThreadExecutor()
                                             : Executors.newFixedThreadPool(threadCount));
        }
        bestGASolver = new CustomizableGASolver(1000,
                                                100,
                                                new PartiallyMatchedCrossover<PatientCycleDemand, Integer>(1.0)
                                                        .andThen(new SwapMutator<>(0.1)),
                                                Executors.newSingleThreadExecutor(),
                                                0.5,
                                                new TournamentSelector<>(3)
        );
    }

    private static Stream<Arguments> getGeneratedDataset() {
        List<Arguments> arguments = new ArrayList<>();
        for (String scenario : GeneralConfig.SCENARIO_SET) {
            for (int problemSize : GeneralConfig.PROBLEM_SIZES) {
                for (int testIndex = 1; testIndex <= GeneralConfig.NUMBER_OF_TESTS; testIndex++) {
                    arguments.add(Arguments.of(problemSize, scenario, testIndex));
                }
            }
        }
        return arguments.stream();
    }

    private static Stream<Arguments> getRepeatedGeneratedDataset() {
        List<Arguments> arguments = new ArrayList<>();
        for (String scenario : GeneralConfig.SCENARIO_SET) {
            for (int problemSize : GeneralConfig.PROBLEM_SIZES) {
                for (int testIndex = 1; testIndex <= GeneralConfig.NUMBER_OF_TESTS; testIndex++) {
                    for (int genCount : GEN_COUNTS) {
                        for (int popSize : POP_SIZES) {
                            for (int threadCount = 0; threadCount < Runtime.getRuntime().availableProcessors(); threadCount = threadCount + 2) {
                                for (int repeatIdx = 0; repeatIdx < RUN_MAX_ITERATION; repeatIdx++) {
                                    arguments.add(Arguments.of(scenario, problemSize, testIndex, genCount, popSize, threadCount, repeatIdx));
                                }
                            }
                        }
                    }
                }
            }
        }
        return arguments.stream();
    }

//    @ParameterizedTest(name = "LPT_FF - {index}: {0} {1} {2}")
//    @MethodSource("getGeneratedDataset")
//    void runLPTFFSolver( int problemSize, String scenario, int testIndex) throws IOException {
//        runEachTest("LPT_FF", new LPT_FF_SumC_Solver(), problemSize, scenario, testIndex);
//    }
//
//    @ParameterizedTest(name = "SPT_FF - {index}: {0} {1} {2}")
//    @MethodSource("getGeneratedDataset")
//    void runSPTFFSolver(int problemSize, String scenario, int testIndex) throws IOException {
//        runEachTest("SPT_FF", new SPT_FF_SumC_Solver(), problemSize, scenario, testIndex);
//    }
//
//    @ParameterizedTest(name = "LPT_FF - {index}: {0} {1} {2}")
//    @MethodSource("getGeneratedDataset")
//    void runLIPTFFSolver(int problemSize, String scenario, int testIndex) throws IOException {
//        runEachTest("LIPT_FF", new LIPT_FF_SumC_Solver(), problemSize, scenario, testIndex);
//    }
//
//    @ParameterizedTest(name = "SIPT_FF - {index}: {0} {1} {2}")
//    @MethodSource("getGeneratedDataset")
//    void runSIPTFFSolver(int problemSize, String scenario, int testIndex) throws IOException {
//        runEachTest("SIPT_FF", new SIPT_FF_SumC_Solver(), problemSize, scenario, testIndex);
//    }

    @ParameterizedTest(name = "CIPT_II_FF - {index}: {0} {1} {2}")
    @MethodSource("getGeneratedDataset")
    void runCIPTIISolver(int problemSize, String scenario, int testIndex) throws IOException {
        runEachTest("CIPT_II_FF",
                    new CIPT_FF_SumC_Solver(true, true),
                    problemSize,
                    scenario,
                    testIndex, true);
    }

    @ParameterizedTest(name = "CIPT_ID_FF - {index}: {0} {1} {2}")
    @MethodSource("getGeneratedDataset")
    void runCIPTIDSolver(int problemSize, String scenario, int testIndex) throws IOException {
        runEachTest("CIPT_ID_FF",
                    new CIPT_FF_SumC_Solver(true, false),
                    problemSize,
                    scenario,
                    testIndex, true);
    }

    @ParameterizedTest(name = "CIPT_DI_FF - {index}: {0} {1} {2}")
    @MethodSource("getGeneratedDataset")
    void runCIPTDISolver(int problemSize, String scenario, int testIndex) throws IOException {
        runEachTest("CIPT_DI_FF",
                    new CIPT_FF_SumC_Solver(false, true),
                    problemSize,
                    scenario,
                    testIndex, true);
    }

    @ParameterizedTest(name = "CIPT_DD_FF - {index}: {0} {1} {2}")
    @MethodSource("getGeneratedDataset")
    void runCIPTDDSolver(int problemSize, String scenario, int testIndex) throws IOException {
        runEachTest("CIPT_DD_FF",
                    new CIPT_FF_SumC_Solver(false, false),
                    problemSize,
                    scenario,
                    testIndex, true);
    }

    @ParameterizedTest(name = "GA - {index}: {0} {1} {2}")
    @MethodSource("getGeneratedDataset")
    void runSimpleGASolver(int problemSize, String scenario, int testIndex) throws IOException {
        runEachTest("GA-40-25",
                    new SimpleGASolver(40, 25, Executors.newWorkStealingPool()),
                    problemSize,
                    scenario,
                    testIndex,
                    true);
    }

    @ParameterizedTest(name = "Best GA - {index}: {0} {1} {2}")
    @MethodSource("getGeneratedDataset")
    void runBestGASolver(int problemSize, String scenario, int testIndex) throws IOException {
        runEachTest("GA_best",
                    bestGASolver,
                    problemSize,
                    scenario,
                    testIndex,
                    true);
    }

    @ParameterizedTest(name = "GA - {index}: {1} {0} {2} p{3} g{4} t{5} r{6}")
    @MethodSource("getRepeatedGeneratedDataset")
    void runSimpleGASolverSensitivityTest(String scenario, int problemSize, int testIndex,
                                          int popSize, int genCount, int threadCount, int runCount) throws IOException {
        String solverName = "GA" +
                            "-p" + popSize
                            + "-g" + genCount
                            + "-t" + (threadCount == 0 ? 1 : threadCount)
                            + "-r" + (runCount + 1);
        System.out.println(problemSize + " " + scenario + " " + testIndex + " " + solverName);
        runEachTest(solverName,
                    new SimpleGASolver(popSize,
                                       genCount,
                                       executorMap.get(threadCount)),
                    problemSize,
                    scenario,
                    testIndex,
                    false);
    }

//    @ParameterizedTest(name = "TR - {index}: {0} {1} {2}")
//    @MethodSource("getGeneratedDataset")
//    void runTRSolver(int problemSize, String scenario, int testIndex) throws IOException {
//        runEachTest("TR", trSolver, problemSize, scenario, testIndex);
//    }

//    @ParameterizedTest(name = "QGA - {index}: {0} {1} {2}")
//    @MethodSource("getGeneratedDataset")
//    void runQuickGASolver(int problemSize, String scenario, int testIndex) throws IOException {
//        runEachTest("QGA", qGaSolver, problemSize, scenario, testIndex);
//    }
//
//    @ParameterizedTest(name = "HGA - {index}: {0} {1} {2}")
//    @MethodSource("getGeneratedDataset")
//    void runHeavyGASolver(int problemSize, String scenario, int testIndex) throws IOException {
//        runEachTest("HGA", hGaSolver, problemSize, scenario, testIndex);
//    }

//    @ParameterizedTest(name = "UGA - {index}: {0} {1} {2}")
//    @MethodSource("getGeneratedDataset")
//    void runUltraGASolver(int problemSize, String scenario, int testIndex) throws IOException {
//        runEachTest("UGA", uGaSolver, problemSize, scenario, testIndex);
//    }

    void runEachTest(
            String solverName,
            Solver<?> solver,
            int problemSize,
            String scenario,
            int testIndex, boolean solutionOutput)
            throws SolverException, IOException {
        assert solver != null;
        Instance instance = Utils.GSON.fromJson(new JsonReader(new FileReader(
                                                        GeneralConfig.getJsonInstanceFile(problemSize, scenario, testIndex)
                                                )),
                                                Instance.class);
        List<PatientCycleDemand> cycleDemands = instance.getDemands();
        Param param = instance.getParam();
        Solution<?> solution = null;
        SolverException exception = null;
        Record record;
        Instant begin = Instant.now();
        try {
            solution = solver.solve(param,
                                    cycleDemands,
                                    new ArrayList<>());
            record = new Record(
                    solverName,
                    problemSize,
                    scenario,
                    testIndex,
                    solution.getObjective(),
                    ChronoUnit.MILLIS.between(begin, Instant.now())
            );
        } catch (SolverException e) {
            record = new Record(
                    solverName,
                    problemSize,
                    scenario,
                    testIndex,
                    new IntegerObjective(-1),
                    ChronoUnit.MILLIS.between(begin, Instant.now())
            );
            exception = e;
        }
        CSV_WRITER.write(record);
        if (solutionOutput)
            Utils.writeSolution(solverName, problemSize, scenario, testIndex, record, solution);
        if (exception != null) throw exception;
    }

    @AfterAll
    static void terminate() throws IOException {
        CSV_WRITER.flush();
        CSV_WRITER.close();
    }
}