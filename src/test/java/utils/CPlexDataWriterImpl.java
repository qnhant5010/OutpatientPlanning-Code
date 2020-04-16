package utils;

import instance.Instance;
import optalp.chtplanning.common.Param;
import optalp.chtplanning.common.PatientCycleDemand;
import optalp.chtplanning.common.PatientRdvDemand;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Writer for v1
 */
public class CPlexDataWriterImpl extends PrintWriter implements CPlexDataWriter {
    public CPlexDataWriterImpl(String fileName) throws FileNotFoundException {
        super(fileName);
    }

    @Override
    public void cprint(Instance instance) {
        cprint(instance.getParam());
        cprint(instance.getDemands());
    }

    private void cprint(Param param) {
        cprint("N_J", param.getDays());
        cprint("N_H", param.getNumTimeSlots());
        cprint("N_D", param.getSectorIds().size());
        cprint("y", param.getMultitasks());
        cprint("a", param.getConsultationLength());
        cprint("b", param.getInstallationLength());
        cprint("D", param.getDoctors().get(0L));
        cprint("M", param.getNumMaterials());
        cprint("I", param.getNurses());
        int[][] binaryPharmacy = new int[param.getDays() + 1][param.getNumTimeSlots() + 1];
        for (int i = 0; i < binaryPharmacy.length; i++) {
            for (int j = 0; j < binaryPharmacy[i].length; j++) {
                binaryPharmacy[i][j] = param.getPharmacy()[i][j] ? 1 : 0;
            }
        }
        cprint("F", binaryPharmacy);
    }

    private void cprint(List<PatientCycleDemand> patientCycleDemands) {
        cprint("N_P", patientCycleDemands.size());
        cprint("n",
               patientCycleDemands.stream()
                       .map(cycleDemand -> cycleDemand.getRdvDemands().size())
                       .collect(Collectors.toList())
              );
        cprint("p_C",
               patientCycleDemands.stream()
                       .map(cycleDemand -> cycleDemand.getRdvDemands().get(0).getMedPrepDuration())
                       .collect(Collectors.toList())
              );
        cprint("p_D",
               patientCycleDemands.stream()
                       .map(cycleDemand -> cycleDemand.getRdvDemands().get(0).getTreatmentDuration())
                       .collect(Collectors.toList())
              );
        cprint("Delta",
               patientCycleDemands.stream()
                       .map(cycleDemand ->
                                    cycleDemand.getRdvDemands().stream()
                                            .mapToInt(PatientRdvDemand::getAfterLastRequest)
                                            .toArray()
                           )
                       .toArray()
              );
        cprint("e_A",
               patientCycleDemands.stream()
                       .map(cycleDemand ->
                                    cycleDemand.getRdvDemands().stream()
                                            .mapToInt(rdvDemand -> rdvDemand.isNeedingConsultation() ? 1 : 0)
                                            .toArray()
                           )
                       .toArray()
              );
        cprint("e_C",
               patientCycleDemands.stream()
                       .map(cycleDemand ->
                                    cycleDemand.getRdvDemands().stream()
                                            .mapToInt(rdvDemand -> rdvDemand.isMedPreparedSameDay() ? 0 : 1)
                                            .toArray()
                           )
                       .toArray()
              );
    }

    private void cprint(String name, Object value) {
        print(name + " = ");
        cprint(value);
        println(";");
    }

    private void cprint(Object value) {
        if (value.getClass().isArray()) {
            cprint((Object[]) value);
        } else if (value.getClass().isAssignableFrom(Collection.class))
            cprint(((List<?>) value).toArray());
        else
            print(value);
    }

    private void cprint(Object[] value) {
        println("[");
        for (int i = 0, objectsLength = value.length; i < objectsLength; i++) {
            final Object o = value[i];
            if (o.getClass().isArray()) {
                if (o instanceof int[]) cprint((int[]) o);
                else if (o instanceof Integer[]) cprint((Integer[]) o);
                else if (o instanceof boolean[]) cprint((boolean[]) o);
                else if (o instanceof Boolean[]) cprint((Boolean[]) o);
                else cprint(o);
            } else
                print(o.toString());
            if (i + 1 < objectsLength)
                print(",");
            println();
        }
        print("]");
    }

    private void cprint(int[] value) {
        print(Arrays.toString(value));
    }

    private void cprint(Integer[] value) {
        print(Arrays.toString(value));
    }

    private void cprint(boolean[] value) {
        print(Arrays.toString(value));
    }

    private void cprint(Boolean[] value) {
        print(Arrays.toString(value));
    }
}
