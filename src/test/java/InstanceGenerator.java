import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import optalp.chtplanning.common.Param;
import optalp.chtplanning.common.PatientCycleDemand;
import optalp.chtplanning.common.PatientRdvDemand;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.function.Supplier;

public class InstanceGenerator {

    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) throws IOException {
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
                    Instance instance = new Instance(
                            paramSupplier.get(),
                            generateDemands(problemSize)
                    );
                    // json
                    try (Writer writer = new FileWriter(GeneralConfig.getJsonInstanceFile(problemSize, scenario, testIndex))) {
                        gson.toJson(instance, writer);
                    }
                    // cplex dat
                    try (CPLexDataWriter writer = new CPLexDataWriter(GeneralConfig.getDatInstanceFile(problemSize, scenario, testIndex))) {
                        writer.cprint(instance);
                    }
                }
            }
        }
    }

    private static List<PatientCycleDemand> generateDemands(int numRdv) {
        List<PatientCycleDemand> demands = new ArrayList<>();
        int actualNumRequest = 0;
        long cycleId = 0;
        while (actualNumRequest < numRdv) {
            PatientCycleDemand patientDemand = new PatientCycleDemand();
            patientDemand.setId(cycleId);
            int size = GeneralConfig.randomPick(GeneralConfig.CYCLE_DEMANDS_PER_PATIENT);
            if (actualNumRequest + size > numRdv)
                size = numRdv - actualNumRequest;
            assert size != 0;
            // same protocol
            int treatmentDuration = GeneralConfig.randomPick(GeneralConfig.TREATMENT_DURATIONS);
            int medPrepDuration = GeneralConfig.randomPick(GeneralConfig.MED_PREP_DURATIONS);
            int sectorId = GeneralConfig.randomPick(GeneralConfig.SECTORS);
            // Random requests
            List<PatientRdvDemand> rdvDemandList = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                PatientRdvDemand rdvDemand = PatientRdvDemand.builder()
                        .id(i)
                        .treatmentDuration(treatmentDuration)
                        .medPrepDuration(medPrepDuration)
                        .medPreparedSameDay(GeneralConfig.randomTrueFalse())
                        .afterLastRequest(i == 0 ? 0 : GeneralConfig.randomPick(GeneralConfig.RDV_DELAYS))
                        .needingConsultation(
                                //                                sectorId != 0 &&
                                GeneralConfig.randomTrueFalse())
                        .sectorId(sectorId)
                        .build();
                //                if (rdvDemand.isNeedingConsultation())
                //                    rdvDemand.setMedPreparedSameDay(true);
                rdvDemandList.add(rdvDemand);
            }
            patientDemand.setRdvDemands(rdvDemandList);
            demands.add(patientDemand);
            actualNumRequest += rdvDemandList.size();
            cycleId++;
        }
        return demands;
    }

    private static Param generateUniformParam() {
        Param param = new Param();
        param.setConsultationLength(1);
        param.setInstallationLength(1);
        param.setDays(GeneralConfig.HORIZON_LENGTH);
        param.setNumTimeSlots(GeneralConfig.TIME_SLOTS_PER_DAY);
        param.setMultitasks(GeneralConfig.MULTITASK);
        param.setNumMaterials(GeneralConfig.MATERIALS);
        param.setSectorIds(Collections.singleton(0L));
        final Map<Long, int[][]> doctors = new HashMap<>();
        final int[][] doctorsSector0 = new int[GeneralConfig.HORIZON_LENGTH + 1][GeneralConfig.TIME_SLOTS_PER_DAY + 1];
        for (int i = 1; i <= GeneralConfig.HORIZON_LENGTH; i++) {
            for (int j = 0; j < GeneralConfig.TIME_SLOTS_PER_DAY; j++) {
                doctorsSector0[i][j] = GeneralConfig.randomPick(GeneralConfig.MAX_DOCTOR + 1);
            }
        }
        doctors.put(0L, doctorsSector0);
        param.setDoctors(doctors);
        final int[][] nurses = new int[GeneralConfig.HORIZON_LENGTH + 1][GeneralConfig.TIME_SLOTS_PER_DAY + 1];
        for (int i = 1; i <= GeneralConfig.HORIZON_LENGTH; i++) {
            for (int j = 0; j < GeneralConfig.TIME_SLOTS_PER_DAY; j++) {
                nurses[i][j] = GeneralConfig.randomPick(GeneralConfig.MAX_NURSES + 1);
            }
        }
        param.setNurses(nurses);
        final boolean[][] pharmacy = new boolean[GeneralConfig.HORIZON_LENGTH + 1][GeneralConfig.TIME_SLOTS_PER_DAY + 1];
        for (int i = 0; i <= GeneralConfig.HORIZON_LENGTH; i++) {
            for (int j = 0; j < GeneralConfig.TIME_SLOTS_PER_DAY; j++) {
                pharmacy[i][j] = GeneralConfig.randomTrueFalse();
            }
        }
        param.setPharmacy(pharmacy);
        return param;
    }

    private static Param generateDailyParam() {
        Param param = new Param();
        param.setConsultationLength(1);
        param.setInstallationLength(1);
        param.setDays(GeneralConfig.HORIZON_LENGTH);
        param.setNumTimeSlots(GeneralConfig.TIME_SLOTS_PER_DAY);
        param.setMultitasks(GeneralConfig.MULTITASK);
        param.setNumMaterials(GeneralConfig.MATERIALS);
        param.setSectorIds(Collections.singleton(0L));
        final Map<Long, int[][]> doctors = new HashMap<>();
        final int[][] doctorsSector0 = new int[GeneralConfig.HORIZON_LENGTH + 1][GeneralConfig.TIME_SLOTS_PER_DAY + 1];
        for (int i = 0; i < GeneralConfig.TIME_SLOTS_PER_DAY; i++) {
            final int numDoctor = GeneralConfig.randomPick(GeneralConfig.MAX_DOCTOR + 1);
            for (int j = 1; j <= GeneralConfig.HORIZON_LENGTH; j++) {
                doctorsSector0[j][i] = numDoctor;
            }
        }
        doctors.put(0L, doctorsSector0);
        param.setDoctors(doctors);
        final int[][] nurses = new int[GeneralConfig.HORIZON_LENGTH + 1][GeneralConfig.TIME_SLOTS_PER_DAY + 1];
        for (int i = 0; i < GeneralConfig.TIME_SLOTS_PER_DAY; i++) {
            final int numNurses = GeneralConfig.randomPick(GeneralConfig.MAX_NURSES + 1);
            for (int j = 1; j <= GeneralConfig.HORIZON_LENGTH; j++) {
                nurses[j][i] = numNurses;
            }
        }
        param.setNurses(nurses);
        final boolean[][] pharmacy = new boolean[GeneralConfig.HORIZON_LENGTH + 1][GeneralConfig.TIME_SLOTS_PER_DAY + 1];
        for (int i = 0; i < GeneralConfig.TIME_SLOTS_PER_DAY; i++) {
            final boolean open = GeneralConfig.randomTrueFalse();
            for (int j = 0; j <= GeneralConfig.HORIZON_LENGTH; j++) {
                pharmacy[j][i] = open;
            }
        }
        param.setPharmacy(pharmacy);
        return param;
    }

    private static Param generateWeeklyParam() {
        Param param = new Param();
        param.setConsultationLength(1);
        param.setInstallationLength(1);
        param.setDays(GeneralConfig.HORIZON_LENGTH);
        param.setNumTimeSlots(GeneralConfig.TIME_SLOTS_PER_DAY);
        param.setMultitasks(GeneralConfig.MULTITASK);
        param.setNumMaterials(GeneralConfig.MATERIALS);
        param.setSectorIds(Collections.singleton(0L));
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
        param.setDoctors(doctors);
        final int[][] nurses = new int[GeneralConfig.HORIZON_LENGTH + 1][GeneralConfig.TIME_SLOTS_PER_DAY + 1];
        for (int i = 0; i < Math.min(7, GeneralConfig.HORIZON_LENGTH); i++) {
            for (int j = 0; j < GeneralConfig.TIME_SLOTS_PER_DAY; j++) {
                nurses[i][j] = GeneralConfig.randomPick(GeneralConfig.MAX_NURSES + 1);
            }
        }
        for (int i = 7; i < Math.max(7, GeneralConfig.HORIZON_LENGTH); i++) {
            System.arraycopy(nurses[i % 7], 0, nurses[i], 0, GeneralConfig.TIME_SLOTS_PER_DAY);
        }
        param.setNurses(nurses);
        final boolean[][] pharmacy = new boolean[GeneralConfig.HORIZON_LENGTH + 1][GeneralConfig.TIME_SLOTS_PER_DAY + 1];
        for (int i = 0; i < Math.min(7, GeneralConfig.HORIZON_LENGTH); i++) {
            for (int j = 0; j < GeneralConfig.TIME_SLOTS_PER_DAY; j++) {
                pharmacy[i][j] = GeneralConfig.randomTrueFalse();
            }
        }
        for (int i = 7; i < Math.max(7, GeneralConfig.HORIZON_LENGTH); i++) {
            System.arraycopy(pharmacy[i % 7], 0, pharmacy[i], 0, GeneralConfig.TIME_SLOTS_PER_DAY);
        }
        param.setPharmacy(pharmacy);
        return param;
    }

    private static Param generateWeekendParam() {
        int today = GeneralConfig.randomPick(7);
        Param param = new Param();
        param.setConsultationLength(1);
        param.setInstallationLength(1);
        param.setDays(GeneralConfig.HORIZON_LENGTH);
        param.setNumTimeSlots(GeneralConfig.TIME_SLOTS_PER_DAY);
        param.setMultitasks(GeneralConfig.MULTITASK);
        param.setNumMaterials(GeneralConfig.MATERIALS);
        param.setSectorIds(Collections.singleton(0L));
        final Map<Long, int[][]> doctors = new HashMap<>();
        final int[][] doctorsSector0 = new int[GeneralConfig.HORIZON_LENGTH + 1][GeneralConfig.TIME_SLOTS_PER_DAY + 1];
        for (int i = 0; i <= GeneralConfig.HORIZON_LENGTH; i++) {
            if (isWeekend(i + today)) continue;
            for (int j = 0; j < GeneralConfig.TIME_SLOTS_PER_DAY; j++) {
                doctorsSector0[i][j] = GeneralConfig.randomPick(GeneralConfig.MAX_DOCTOR + 1);
            }
        }
        doctors.put(0L, doctorsSector0);
        param.setDoctors(doctors);
        final int[][] nurses = new int[GeneralConfig.HORIZON_LENGTH + 1][GeneralConfig.TIME_SLOTS_PER_DAY + 1];
        for (int i = 0; i <= GeneralConfig.HORIZON_LENGTH; i++) {
            if (isWeekend(i + today)) continue;
            for (int j = 0; j < GeneralConfig.TIME_SLOTS_PER_DAY; j++) {
                nurses[i][j] = GeneralConfig.randomPick(GeneralConfig.MAX_NURSES + 1);
            }
        }
        param.setNurses(nurses);
        final boolean[][] pharmacy = new boolean[GeneralConfig.HORIZON_LENGTH + 1][GeneralConfig.TIME_SLOTS_PER_DAY + 1];
        for (int i = 0; i <= GeneralConfig.HORIZON_LENGTH; i++) {
            if (isWeekend(i + today)) continue;
            for (int j = 0; j < GeneralConfig.TIME_SLOTS_PER_DAY; j++) {
                pharmacy[i][j] = GeneralConfig.randomTrueFalse();
            }
        }
        param.setPharmacy(pharmacy);
        return param;
    }

    /**
     * @param day on 1-based index
     *
     * @return
     */
    private static boolean isWeekend(int day) {
        return day % 7 == 5 || day % 7 == 6;
    }
}
