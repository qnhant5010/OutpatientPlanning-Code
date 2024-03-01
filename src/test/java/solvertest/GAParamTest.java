package solvertest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import config.GeneralConfig;
import instance.Instance;
import io.jenetics.*;
import optalp.chtplanning.common.Param;
import optalp.chtplanning.common.PatientCycleDemand;
import optalp.chtplanning.common.PatientRdvDemand;
import optalp.chtplanning.simplegasolver.CustomizableGASolver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

class GAParamTest {
    private static final String GA_TEST_DIR = GeneralConfig.PROJECT_DATA_DIR + File.separator + "result_ga";
    private static final int[] POPULATION_SIZES = new int[]{1000};
    private static final int MAX_GEN = 500;
    private static final int REPETITIONS = 30;
    private static final Map<String, Alterer<EnumGene<PatientCycleDemand>, Integer>> ALTERER_MAP = new LinkedHashMap<>();
    private static final Map<String, Selector<EnumGene<PatientCycleDemand>, Integer>> SELECTOR_MAP = new LinkedHashMap<>();
    // Most difficult instances
    private static final String[] INSTANCE_NAMES = new String[]{
            "instance_180_daily_8",
            "instance_210_daily_9",
            "instance_90_weekly_3",
            };
    private final Executor executor = Executors.newWorkStealingPool();
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Instance.class,
                                 (JsonDeserializer<Instance>) (json, typeOfT, context) -> {
                                     JsonObject jsonObject = json.getAsJsonObject();
                                     JsonObject jsonParamObject = jsonObject.get("param")
                                                                            .getAsJsonObject();
                                     Param param = context.deserialize(jsonParamObject,
                                                                       Param.class);
                                     List<PatientCycleDemand> patientCycleDemandList = new ArrayList<>();
                                     int consultationLength = jsonParamObject.get("consultationLength")
                                                                             .getAsInt();
                                     int installationLength = jsonParamObject.get("installationLength")
                                                                             .getAsInt();
                                     jsonObject.get("demands")
                                               .getAsJsonArray()
                                               .forEach(jsonCycleDemand -> {
                                                   // parse into cycle demand
                                                   Long id = jsonCycleDemand.getAsJsonObject()
                                                                            .get("id")
                                                                            .getAsLong();
                                                   List<PatientRdvDemand> rdvDemands = new ArrayList<>();
                                                   jsonCycleDemand.getAsJsonObject()
                                                                  .get("rdvDemands")
                                                                  .getAsJsonArray()
                                                                  .forEach(jsonRdvDemand -> {
                                                                      JsonObject o = jsonRdvDemand.getAsJsonObject();
                                                                      rdvDemands.add(
                                                                              PatientRdvDemand.builder()
                                                                                              .id(o.get("id")
                                                                                                   .getAsInt())
                                                                                              .sectorId(o.get("sectorId")
                                                                                                         .getAsLong())
                                                                                              .afterLastRequest(o.get("afterLastRequest")
                                                                                                                 .getAsInt())
                                                                                              .treatmentDuration(o.get("treatmentDuration")
                                                                                                                  .getAsInt())
                                                                                              .drugMixingSameDay(o.get("medPreparedSameDay")
                                                                                                                  .getAsBoolean())
                                                                                              .drugMixingDuration(o.get("medPrepDuration")
                                                                                                                   .getAsInt())
                                                                                              .installationDuration(installationLength)
                                                                                              .consultationDuration(o.get("needingConsultation")
                                                                                                                     .getAsBoolean()
                                                                                                                    ? consultationLength
                                                                                                                    : 0)
                                                                                              .needingConsultation(o.get("needingConsultation")
                                                                                                                    .getAsBoolean())
                                                                                              .build());
                                                                  });
                                                   patientCycleDemandList.add(new PatientCycleDemand(id,
                                                                                                     rdvDemands));
                                               });
                                     return new Instance(param,
                                                         patientCycleDemandList,
                                                         new HashSet<>()); // Empty by default
                                 })
            .setPrettyPrinting()
            .create();

    @BeforeAll
    static void init() {
        Utils.createDir();
        new File(GA_TEST_DIR).mkdirs();
        double[] psmProbabilities = {
                0.01,
                0.1
        };
        double[] pmcProbabilities = {
                0.8,
                1.0
        };
        for (final double psmProb : psmProbabilities) {
            for (final double pmcProb : pmcProbabilities) {
                ALTERER_MAP.put("pmc-" + pmcProb + "+" + "psm-" + psmProb,
                                new PartiallyMatchedCrossover<PatientCycleDemand, Integer>(pmcProb)
                                        .andThen(new SwapMutator<>(psmProb))
                               );
            }
        }
        SELECTOR_MAP.put("ts3", new TournamentSelector<>(3));
//        SELECTOR_MAP.put("es", new EliteSelector<EnumGene<PatientCycleDemand>, Integer>(1, new TournamentSelector<>(3)));
    }

    private static Stream<Arguments> getGeneratedDataset() {
        List<Arguments> arguments = new ArrayList<>();
        for (String instanceName : INSTANCE_NAMES) {
            for (String mutatorName : ALTERER_MAP.keySet()) {
                for (String selectorName : SELECTOR_MAP.keySet()) {
                    for (final int populationSize : POPULATION_SIZES) {
                        arguments.add(Arguments.of(instanceName, mutatorName, selectorName, populationSize));
                    }
                }
            }
        }
        return arguments.stream();
    }

    @ParameterizedTest
    @MethodSource("getGeneratedDataset")
    void run(String instanceName, String mutatorName, String selectorName, int populationSize) throws FileNotFoundException {
        System.out.println("Resolution for " + instanceName + " with population " + populationSize + " and mutator " + mutatorName + " and selector " + selectorName);
        Instance instance = GSON.fromJson(new JsonReader(new FileReader(GeneralConfig.getJsonInstanceFile(instanceName))),
                                          Instance.class);
        List<PatientCycleDemand> cycleDemands = instance.getDemands();
        Param param = instance.getParam();
        for (int i = 0; i < REPETITIONS; i++) {
            System.out.println("Repetition " + (i + 1) + " of " + REPETITIONS);
            CustomizableGASolver gaSolver = new CustomizableGASolver(populationSize,
                                                                     MAX_GEN,
                                                                     ALTERER_MAP.get(mutatorName),
                                                                     executor,
                                                                     0.5,
                                                                     SELECTOR_MAP.get(selectorName));
            gaSolver.solve(param, cycleDemands, new ArrayList<>());
            try (PrintWriter pw = new PrintWriter(GA_TEST_DIR + File.separator
                                                  + "result_" + instanceName
                                                  + "_m_" + mutatorName
                                                  + "_s_" + selectorName
                                                  + "_p_" + populationSize
                                                  + "_r_" + (i + 1)
                                                  + ".txt")) {
                gaSolver.getBestObjectivesEvolution()
                        .forEach(pw::println);
            }
        }
    }
}
