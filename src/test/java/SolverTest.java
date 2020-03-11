import com.google.gson.stream.JsonReader;
import optalp.chtplanning.common.Param;
import optalp.chtplanning.common.PatientCycleDemand;
import optalp.chtplanning.common.Solver;
import optalp.chtplanning.common.SolverException;
import optalp.chtplanning.common.objective.IntegerObjective;
import optalp.chtplanning.common.solution.Solution;
import optalp.chtplanning.heuristicsolver.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

class SolverTest {
    private static RecordCSVWriter CSV_WRITER;

    @BeforeAll
    static void init() throws IOException {
        Utils.createDir();
        CSV_WRITER =
                new RecordCSVWriter(GeneralConfig.SYNTHESIS_CSV_GETTER.get());
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
                    testIndex);
    }

    @ParameterizedTest(name = "CIPT_ID_FF - {index}: {0} {1} {2}")
    @MethodSource("getGeneratedDataset")
    void runCIPTIDSolver(int problemSize, String scenario, int testIndex) throws IOException {
        runEachTest("CIPT_ID_FF",
                    new CIPT_FF_SumC_Solver(true, false),
                    problemSize,
                    scenario,
                    testIndex);
    }

    @ParameterizedTest(name = "CIPT_DI_FF - {index}: {0} {1} {2}")
    @MethodSource("getGeneratedDataset")
    void runCIPTDISolver(int problemSize, String scenario, int testIndex) throws IOException {
        runEachTest("CIPT_DI_FF",
                    new CIPT_FF_SumC_Solver(false, true),
                    problemSize,
                    scenario,
                    testIndex);
    }

    @ParameterizedTest(name = "CIPT_DD_FF - {index}: {0} {1} {2}")
    @MethodSource("getGeneratedDataset")
    void runCIPTDDSolver(int problemSize, String scenario, int testIndex) throws IOException {
        runEachTest("CIPT_DD_FF",
                    new CIPT_FF_SumC_Solver(false, false),
                    problemSize,
                    scenario,
                    testIndex);
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
            int problemSize, String scenario, int testIndex)
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
        Utils.writeSolution(solverName, problemSize, scenario, testIndex, record, solution);
        System.out.println(record);
        if (exception != null) throw exception;
    }

    @AfterAll
    static void terminate() throws IOException {
        CSV_WRITER.flush();
        CSV_WRITER.close();
    }
}