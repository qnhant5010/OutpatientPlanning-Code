import com.google.gson.stream.JsonReader;
import optalp.chtplanning.common.Param;
import optalp.chtplanning.common.PatientCycleDemand;
import optalp.chtplanning.common.Solver;
import optalp.chtplanning.common.SolverException;
import optalp.chtplanning.common.objective.IntegerObjective;
import optalp.chtplanning.common.solution.Solution;
import optalp.chtplanning.simplegasolver.GASolver;
import optalp.chtplanning.simplelptsolver.LPTMinSumCmaxSolver;
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
    private static LPTMinSumCmaxSolver lptSolver;
    private static GASolver uGaSolver;
    private static RecordCSVWriter CSV_WRITER;

    @BeforeAll
    static void init() throws IOException {
        Utils.createDir();
        lptSolver = new LPTMinSumCmaxSolver();
        uGaSolver = new GASolver(10000, 0, 100);
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

    @ParameterizedTest(name = "LPT - {index}: {0} {1} {2}")
    @MethodSource("getGeneratedDataset")
    void runLPTSolver(int problemSize, String scenario, int testIndex) throws IOException {
        runEachTest("LPT", lptSolver, problemSize, scenario, testIndex);
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

    @ParameterizedTest(name = "UGA - {index}: {0} {1} {2}")
    @MethodSource("getGeneratedDataset")
    void runUltraGASolver(int problemSize, String scenario, int testIndex) throws IOException {
        runEachTest("UGA", uGaSolver, problemSize, scenario, testIndex);
    }

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