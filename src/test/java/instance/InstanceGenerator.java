package instance;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import config.GeneralConfig;
import optalp.chtplanning.common.*;
import utils.CPlexDataWriter;
import utils.CPlexDataWriterImpl;
import utils.Utils;

import java.io.FileWriter;
import java.io.Writer;
import java.util.*;
import java.util.function.Supplier;

public class InstanceGenerator {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final int CONSULTATION_LENGTH = 1;
    private static final int INSTALLATION_LENGTH = 1;

    public static void main(String[] args) throws Exception {
        Utils.createDir();
        final Map<String, Supplier<Param>> SCENARIO_GENERATORS = new HashMap<>();
        SCENARIO_GENERATORS.put("uniform", InstanceGenerator::generateUniformParam);
        SCENARIO_GENERATORS.put("daily", InstanceGenerator::generateDailyParam);
        SCENARIO_GENERATORS.put("weekly", InstanceGenerator::generateWeeklyParam);
        SCENARIO_GENERATORS.put("weekend", InstanceGenerator::generateWeekendParam);
        for (int problemSize : GeneralConfig.PROBLEM_SIZES) {
            for (final String scenario : SCENARIO_GENERATORS.keySet()) {
                for (int testIndex = 1; testIndex <= GeneralConfig.NUMBER_OF_TESTS; testIndex++) {
                    Supplier<Param> paramSupplier = SCENARIO_GENERATORS.get(scenario);
                    assert paramSupplier != null;
                    List<PatientCycleDemand> demands = generateDemands(problemSize);
                    Instance instance = new Instance(
                            paramSupplier.get(),
                            demands,
                            generateIncompatibilities(demands)
                    );
                    // json
                    try (Writer writer = new FileWriter(GeneralConfig.getJsonInstanceFile(problemSize, scenario, testIndex))) {
                        gson.toJson(instance, writer);
                    }
                    // cplex dat
                    try (CPlexDataWriter writer = new CPlexDataWriterImpl(GeneralConfig.getDatInstanceFile(problemSize, scenario, testIndex))) {
                        writer.cprint(instance);
                    }
                }
            }
        }
    }

    private static Set<Incompatibility> generateIncompatibilities(List<PatientCycleDemand> demands) {
        Set<Incompatibility> incompatibilities = new HashSet<>();
        // Gender incompatibility
        Set<Long> males = new HashSet<>();
        Set<Long> females = new HashSet<>();
        demands.forEach(patientCycleDemand -> {
            if (GeneralConfig.isMale()) {
                males.add(patientCycleDemand.getId());
            } else {
                females.add(patientCycleDemand.getId());
            }
        });
        males.forEach(male -> females.forEach(female -> incompatibilities.add(new Incompatibility(male, female))));
        // Infectious
        Set<Long> infectious = new HashSet<>();
        demands.forEach(patientCycleDemand -> {
            if (GeneralConfig.isInfectious())
                infectious.add(patientCycleDemand.getId());
        });
        infectious.forEach(i -> demands.forEach(d -> {
            if (!Objects.equals(d.getId(), i))
                incompatibilities.add(new Incompatibility(i, d.getId()));
        }));
        return incompatibilities;
    }

    private static List<PatientCycleDemand> generateDemands(int requiredNumRdv) {
        List<PatientCycleDemand> demands = new ArrayList<>();
        int currentNumRdvRequest = 0;
        long cycleId = 0;
        while (currentNumRdvRequest < requiredNumRdv) {
            int size = GeneralConfig.randomPick(GeneralConfig.RDV_DEMANDS_PER_PATIENT);
            if (currentNumRdvRequest + size > requiredNumRdv)
                size = requiredNumRdv - currentNumRdvRequest;
            assert size != 0;
            // same protocol
            int sectorId = GeneralConfig.randomPick(GeneralConfig.SECTORS);
            // Random requests
            List<PatientRdvDemand> rdvDemandList = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                rdvDemandList.add(PatientRdvDemand.builder()
                                                  .id(i)
                                                  .treatmentDuration(GeneralConfig.randomPick(GeneralConfig.TREATMENT_DURATIONS))
                                                  .drugMixingDuration(GeneralConfig.randomPick(GeneralConfig.DRUG_MIXING_DURATIONS))
                                                  .drugMixingSameDay(GeneralConfig.randomTrueFalse())
                                                  .afterLastRequest(i == 0 ? 0 : GeneralConfig.randomPick(GeneralConfig.RDV_DELAYS))
                                                  .consultationDuration(sectorId == 0
                                                                        ? 0
                                                                        : GeneralConfig.randomTrueFalse()
                                                                          ? CONSULTATION_LENGTH
                                                                          : 0)
                                                  .installationDuration(INSTALLATION_LENGTH)
                                                  .sectorId(sectorId)
                                                  .build());
            }
            demands.add(PatientCycleDemand.builder()
                                          .id(cycleId)
                                          .rdvDemands(rdvDemandList)
                                          .build());
            currentNumRdvRequest += rdvDemandList.size();
            cycleId++;
        }
        return demands;
    }

    private static Set<Room> generateRooms() {
        Set<Room> rooms = new HashSet<>();
        int assignedMaterialsCounter = 0;
        while (assignedMaterialsCounter < GeneralConfig.MATERIALS) {
            int numMaterialsToAssign = GeneralConfig.getNumMaterialsToAssignToARoom();
            if (assignedMaterialsCounter + numMaterialsToAssign > GeneralConfig.MATERIALS)
                numMaterialsToAssign = assignedMaterialsCounter + numMaterialsToAssign - GeneralConfig.MATERIALS;
            Set<Long> materials = new HashSet<>();
            for (int i = assignedMaterialsCounter; i < assignedMaterialsCounter + numMaterialsToAssign; i++) {
                materials.add((long) i);
            }
            assignedMaterialsCounter += numMaterialsToAssign;
            rooms.add(new Room(materials));
        }
        return rooms;
    }

    private static Param generateUniformParam() {
        final Map<Long, int[][]> doctors = new HashMap<>();
        for (int sector : GeneralConfig.SECTORS) {
            final int[][] doctorsSector = new int[GeneralConfig.HORIZON_LENGTH + 1][GeneralConfig.TIME_SLOTS_PER_DAY + 1];
            for (int i = 1; i <= GeneralConfig.HORIZON_LENGTH; i++) {
                for (int j = 0; j < GeneralConfig.TIME_SLOTS_PER_DAY; j++) {
                    doctorsSector[i][j] = GeneralConfig.randomPick(GeneralConfig.MAX_DOCTOR + 1);
                }
            }
            doctors.put((long) sector, doctorsSector);
        }
        final int[][] nurses = new int[GeneralConfig.HORIZON_LENGTH + 1][GeneralConfig.TIME_SLOTS_PER_DAY + 1];
        for (int i = 1; i <= GeneralConfig.HORIZON_LENGTH; i++) {
            for (int j = 0; j < GeneralConfig.TIME_SLOTS_PER_DAY; j++) {
                nurses[i][j] = GeneralConfig.randomPick(GeneralConfig.MAX_NURSES + 1);
            }
        }
        final boolean[][] pharmacy = new boolean[GeneralConfig.HORIZON_LENGTH + 1][GeneralConfig.TIME_SLOTS_PER_DAY + 1];
        for (int i = 0; i <= GeneralConfig.HORIZON_LENGTH; i++) {
            for (int j = 0; j < GeneralConfig.TIME_SLOTS_PER_DAY; j++) {
                pharmacy[i][j] = GeneralConfig.randomTrueFalse();
            }
        }
        return Param.builder()
                    .days(GeneralConfig.HORIZON_LENGTH)
                    .numTimeSlots(GeneralConfig.TIME_SLOTS_PER_DAY)
                    .multitasks(GeneralConfig.MULTITASK)
                    .numMaterials(GeneralConfig.MATERIALS)
                    .sectorIds(Collections.singleton(0L))
                    .doctors(doctors)
                    .nurses(nurses)
                    .pharmacy(pharmacy)
                    .rooms(generateRooms())
                    .build();
    }

    private static Param generateDailyParam() {
        final Map<Long, int[][]> doctors = new HashMap<>();
        final int[][] doctorsSector0 = new int[GeneralConfig.HORIZON_LENGTH + 1][GeneralConfig.TIME_SLOTS_PER_DAY + 1];
        for (int i = 0; i < GeneralConfig.TIME_SLOTS_PER_DAY; i++) {
            final int numDoctor = GeneralConfig.randomPick(GeneralConfig.MAX_DOCTOR + 1);
            for (int j = 1; j <= GeneralConfig.HORIZON_LENGTH; j++) {
                doctorsSector0[j][i] = numDoctor;
            }
        }
        doctors.put(0L, doctorsSector0);
        final int[][] nurses = new int[GeneralConfig.HORIZON_LENGTH + 1][GeneralConfig.TIME_SLOTS_PER_DAY + 1];
        for (int i = 0; i < GeneralConfig.TIME_SLOTS_PER_DAY; i++) {
            final int numNurses = GeneralConfig.randomPick(GeneralConfig.MAX_NURSES + 1);
            for (int j = 1; j <= GeneralConfig.HORIZON_LENGTH; j++) {
                nurses[j][i] = numNurses;
            }
        }
        final boolean[][] pharmacy = new boolean[GeneralConfig.HORIZON_LENGTH + 1][GeneralConfig.TIME_SLOTS_PER_DAY + 1];
        for (int i = 0; i < GeneralConfig.TIME_SLOTS_PER_DAY; i++) {
            final boolean open = GeneralConfig.randomTrueFalse();
            for (int j = 0; j <= GeneralConfig.HORIZON_LENGTH; j++) {
                pharmacy[j][i] = open;
            }
        }
        return Param.builder()
                    .days(GeneralConfig.HORIZON_LENGTH)
                    .numTimeSlots(GeneralConfig.TIME_SLOTS_PER_DAY)
                    .multitasks(GeneralConfig.MULTITASK)
                    .numMaterials(GeneralConfig.MATERIALS)
                    .sectorIds(Collections.singleton(0L))
                    .doctors(doctors)
                    .nurses(nurses)
                    .pharmacy(pharmacy)
                    .build();
    }

    private static Param generateWeeklyParam() {
        final Map<Long, int[][]> doctors = new HashMap<>();
        final int[][] doctorsSector0 = new int[GeneralConfig.HORIZON_LENGTH + 1][GeneralConfig.TIME_SLOTS_PER_DAY + 1];
        for (int i = 0; i < Math.min(7, GeneralConfig.HORIZON_LENGTH); i++) {
            for (int j = 0; j < GeneralConfig.TIME_SLOTS_PER_DAY; j++) {
                doctorsSector0[i][j] = GeneralConfig.randomPick(GeneralConfig.MAX_DOCTOR + 1);
            }
        }
        for (int i = 7; i < Math.max(7, GeneralConfig.HORIZON_LENGTH); i++) {
            System.arraycopy(doctorsSector0[i % 7], 0, doctorsSector0[i], 0, GeneralConfig.TIME_SLOTS_PER_DAY);
        }
        doctors.put(0L, doctorsSector0);
        final int[][] nurses = new int[GeneralConfig.HORIZON_LENGTH + 1][GeneralConfig.TIME_SLOTS_PER_DAY + 1];
        for (int i = 0; i < Math.min(7, GeneralConfig.HORIZON_LENGTH); i++) {
            for (int j = 0; j < GeneralConfig.TIME_SLOTS_PER_DAY; j++) {
                nurses[i][j] = GeneralConfig.randomPick(GeneralConfig.MAX_NURSES + 1);
            }
        }
        for (int i = 7; i < Math.max(7, GeneralConfig.HORIZON_LENGTH); i++) {
            System.arraycopy(nurses[i % 7], 0, nurses[i], 0, GeneralConfig.TIME_SLOTS_PER_DAY);
        }
        final boolean[][] pharmacy = new boolean[GeneralConfig.HORIZON_LENGTH + 1][GeneralConfig.TIME_SLOTS_PER_DAY + 1];
        for (int i = 0; i < Math.min(7, GeneralConfig.HORIZON_LENGTH); i++) {
            for (int j = 0; j < GeneralConfig.TIME_SLOTS_PER_DAY; j++) {
                pharmacy[i][j] = GeneralConfig.randomTrueFalse();
            }
        }
        for (int i = 7; i < Math.max(7, GeneralConfig.HORIZON_LENGTH); i++) {
            System.arraycopy(pharmacy[i % 7], 0, pharmacy[i], 0, GeneralConfig.TIME_SLOTS_PER_DAY);
        }
        return Param.builder()
                    .days(GeneralConfig.HORIZON_LENGTH)
                    .numTimeSlots(GeneralConfig.TIME_SLOTS_PER_DAY)
                    .multitasks(GeneralConfig.MULTITASK)
                    .numMaterials(GeneralConfig.MATERIALS)
                    .sectorIds(Collections.singleton(0L))
                    .doctors(doctors)
                    .nurses(nurses)
                    .pharmacy(pharmacy)
                    .build();
    }

    private static Param generateWeekendParam() {
        int today = GeneralConfig.randomPick(7);
        final Map<Long, int[][]> doctors = new HashMap<>();
        final int[][] doctorsSector0 = new int[GeneralConfig.HORIZON_LENGTH + 1][GeneralConfig.TIME_SLOTS_PER_DAY + 1];
        for (int i = 0; i <= GeneralConfig.HORIZON_LENGTH; i++) {
            if (isWeekend(i + today)) continue;
            for (int j = 0; j < GeneralConfig.TIME_SLOTS_PER_DAY; j++) {
                doctorsSector0[i][j] = GeneralConfig.randomPick(GeneralConfig.MAX_DOCTOR + 1);
            }
        }
        doctors.put(0L, doctorsSector0);
        final int[][] nurses = new int[GeneralConfig.HORIZON_LENGTH + 1][GeneralConfig.TIME_SLOTS_PER_DAY + 1];
        for (int i = 0; i <= GeneralConfig.HORIZON_LENGTH; i++) {
            if (isWeekend(i + today)) continue;
            for (int j = 0; j < GeneralConfig.TIME_SLOTS_PER_DAY; j++) {
                nurses[i][j] = GeneralConfig.randomPick(GeneralConfig.MAX_NURSES + 1);
            }
        }
        final boolean[][] pharmacy = new boolean[GeneralConfig.HORIZON_LENGTH + 1][GeneralConfig.TIME_SLOTS_PER_DAY + 1];
        for (int i = 0; i <= GeneralConfig.HORIZON_LENGTH; i++) {
            if (isWeekend(i + today)) continue;
            for (int j = 0; j < GeneralConfig.TIME_SLOTS_PER_DAY; j++) {
                pharmacy[i][j] = GeneralConfig.randomTrueFalse();
            }
        }
        return Param.builder()
                    .days(GeneralConfig.HORIZON_LENGTH)
                    .numTimeSlots(GeneralConfig.TIME_SLOTS_PER_DAY)
                    .multitasks(GeneralConfig.MULTITASK)
                    .numMaterials(GeneralConfig.MATERIALS)
                    .sectorIds(Collections.singleton(0L))
                    .doctors(doctors)
                    .nurses(nurses)
                    .pharmacy(pharmacy)
                    .build();
    }

    /**
     * @param day on 1-based index
     * @return
     */
    private static boolean isWeekend(int day) {
        return day % 7 == 5 || day % 7 == 6;
    }
}
