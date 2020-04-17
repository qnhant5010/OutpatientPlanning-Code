package utils;

import instance.Instance;
import optalp.chtplanning.common.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public class CPlexDataWriterImpl extends PrintWriter implements CPlexDataWriter {
    public CPlexDataWriterImpl(String fileName) throws FileNotFoundException {
        super(fileName);
    }

    @Override
    public void cprint(Instance instance) {
        cprint(instance.getParam());
        cprint(instance.getDemands());
        cprint("Incompatibilities",
               instance.getIncompatibilities()
                       .stream()
                       .map(i -> new Incompatibility(i.getPatients()
                                                      .stream()
                                                      .map(p -> p + 1)
                                                      .collect(Collectors.toSet())))
                       .collect(Collectors.toSet()));
    }

    private void cprint(Param param) {
        cprint("nombre_jours", param.getDays());
        cprint("nombre_creneaux", param.getNumTimeSlots());
        cprint("Specialites", param.getSectorIds());
        cprint("multitache", param.getMultitasks());
        cprint("Medecins", param.getDoctors());
        cprint("nombre_places", param.getNumMaterials());
        cprint("Infirmieres", param.getNurses());
        int[][] binaryPharmacy = new int[param.getDays() + 1][param.getNumTimeSlots() + 1];
        for (int i = 0; i < binaryPharmacy.length; i++) {
            for (int j = 0; j < binaryPharmacy[i].length; j++) {
                binaryPharmacy[i][j] = param.getPharmacy()[i][j] ? 1 : 0;
            }
        }
        cprint("Pharmacie", binaryPharmacy);
        cprint("Salles", param.getRooms()
                              .stream()
                              .map(r -> new Room(r.getMaterials()
                                                  .stream()
                                                  .map(l -> l + 1)
                                                  .collect(Collectors.toSet())))
                              .collect(Collectors.toSet())
              );
    }

    private void cprint(List<PatientCycleDemand> patientCycleDemands) {
        cprint("nombre_patients", patientCycleDemands.size());
        cprint("nombre_rdv",
               patientCycleDemands.stream()
                                  .map(cycleDemand -> cycleDemand.getRdvDemands().size())
                                  .toArray()
              );
        cprint("specialite_consultation",
               patientCycleDemands.stream()
                                  .map(cycleDemand -> Math.toIntExact(cycleDemand.getRdvDemands().get(0).getSectorId()))
                                  .toArray()
              );
        cprint("consultation",
               patientCycleDemands.stream()
                                  .map(cycleDemand ->
                                               cycleDemand.getRdvDemands().stream()
                                                          .mapToInt(PatientRdvDemand::getConsultationDuration)
                                                          .toArray()
                                      )
                                  .toArray()
              );
        cprint("installation",
               patientCycleDemands.stream()
                                  .map(cycleDemand ->
                                               cycleDemand.getRdvDemands().stream()
                                                          .mapToInt(PatientRdvDemand::getInstallationDuration)
                                                          .toArray()
                                      )
                                  .toArray()
              );
        cprint("medpreprep",
               patientCycleDemands.stream()
                                  .map(cycleDemand ->
                                               cycleDemand.getRdvDemands().stream()
                                                          .mapToInt(rdvDemand -> rdvDemand.isDrugMixingSameDay() ? 0 : 1)
                                                          .toArray()
                                      )
                                  .toArray()
              );
        cprint("medprep",
               patientCycleDemands.stream()
                                  .map(cycleDemand ->
                                               cycleDemand.getRdvDemands().stream()
                                                          .mapToInt(PatientRdvDemand::getDrugMixingDuration)
                                                          .toArray()
                                      )
                                  .toArray()
              );
        cprint("traitement",
               patientCycleDemands.stream()
                                  .map(cycleDemand ->
                                               cycleDemand.getRdvDemands().stream()
                                                          .mapToInt(PatientRdvDemand::getTreatmentDuration)
                                                          .toArray()
                                      )
                                  .toArray()
              );
        cprint("delais_rdv",
               patientCycleDemands.stream()
                                  .map(cycleDemand ->
                                               cycleDemand.getRdvDemands().stream()
                                                          .mapToInt(PatientRdvDemand::getAfterLastRequest)
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
        if (value.getClass().isArray())
            cprint((Object[]) value);
        else if (Map.class.isAssignableFrom(value.getClass()))
            cprint((Map<?, ?>) value);
        else if (Set.class.isAssignableFrom(value.getClass()))
            cprint((Set<?>) value);
        else if (List.class.isAssignableFrom(value.getClass()))
            cprint(((List<?>) value).toArray());
        else if (value instanceof Incompatibility)
            cprint((Incompatibility) value);
        else if (value instanceof Room)
            cprint((Room) value);
        else
            print(value);
    }

    private void cprint(Map<?, ?> value) {
        println("#[");
        for (Iterator<? extends Map.Entry<?, ?>> iterator = value.entrySet().iterator();
             iterator.hasNext(); ) {
            Map.Entry<?, ?> entry = iterator.next();
            Object k = entry.getKey();
            Object v = entry.getValue();
            cprint(k);
            print(": ");
            cprint(v);
            if (iterator.hasNext()) println(",");
            else println();
        }
        print("]#");
    }

    private void cprint(Set<?> value) {
        print("{");
        for (Iterator<?> iterator = value.iterator(); iterator.hasNext(); ) {
            Object innerValue = iterator.next();
            cprint(innerValue);
            if (iterator.hasNext()) print(", ");
        }
        print("}");
    }

    private void cprint(Incompatibility incompatibility) {
        print("<");
        cprint(incompatibility.getPatients());
        print(">");
    }

    private void cprint(Room room) {
        print("<");
        cprint(room.getMaterials());
        print(">");
    }

    private void cprint(Object[] value) {
        if (value.length > 0)
            if (value[0] instanceof Integer ||
                value[0] instanceof Boolean) {
                cprintArrayInline(value);
                return;
            }
        println("[");
        for (int i = 0, objectsLength = value.length; i < objectsLength; i++) {
            final Object o = value[i];
            if (o.getClass().isArray()) {
                if (o instanceof int[]) cprint((int[]) o);
                else if (o instanceof boolean[]) cprint((boolean[]) o);
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

    private void cprint(boolean[] value) {
        print(Arrays.toString(value));
    }

    private void cprintArrayInline(Object[] value) {
        print(Arrays.toString(value));
    }
}
