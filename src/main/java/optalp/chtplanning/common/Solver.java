package optalp.chtplanning.common;

import optalp.chtplanning.common.solution.Solution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Solver<S extends Solution<?>> {
    protected Param param;
    /**
     * sector x day (0 -> J) x time (0 -> H)
     */
    protected Map<Long, int[][]> doctorWorkload;
    /**
     * day (0 -> J) x time (0 -> H)
     * An installation required full attention of a nurse
     */
    protected float[][] nurseWorkload;
    /**
     * day (0 -> J) x time (0 -> H)
     */
    protected int[][] materialWorkload;

    public abstract S solve(Param param,
                            List<PatientCycleDemand> cycleDemands,
                            List<Allocation> existingAllocations)
    throws SolverException;

    protected void initWorkloadWith(List<Allocation> existingAllocations) {
        doctorWorkload = new HashMap<>();
        param.getSectorIds().forEach(
                sectorId -> doctorWorkload.put(sectorId,
                                               new int[param.getDays() + 1][param.getNumTimeSlots() + 1]));
        nurseWorkload = new float[param.getDays() + 1][param.getNumTimeSlots() + 1];
        materialWorkload = new int[param.getDays() + 1][param.getNumTimeSlots() + 1];
        // Pre fill
        registerWorkload(existingAllocations);
    }

    /**
     * @param workloadRegistry doctor, nurse or material
     * @param day              0 -> J
     * @param fromTime         inclusive
     * @param toTime           exclusive
     */
    protected void registerWorkload(int[][] workloadRegistry, int day,
                                    int fromTime, int toTime) {
        for (int i = fromTime; i < toTime; i++) {
            workloadRegistry[day][i] += 1;
        }
    }

    /**
     * @param workloadRegistry doctor, nurse or material
     * @param day              0 -> J
     * @param fromTime         inclusive
     * @param toTime           exclusive
     * @param load             works to register
     */
    protected void registerWorkload(float[][] workloadRegistry, int day,
                                    int fromTime, int toTime, float load) {
        for (int i = fromTime; i < toTime; i++) {
            workloadRegistry[day][i] += load;
        }
    }

    protected boolean cannotRequirePharmacy(int day, int from, int to) {
        return cannotRequireBinaryResource(param.getPharmacy()[day],
                                           from,
                                           to);
    }

    protected boolean cannotRequireMaterial(int day, int start, int end) {
        return overuseConstantResource(param.getNumMaterials(),
                                       start,
                                       end,
                                       materialWorkload[day]);
    }

    protected boolean cannotRequireNurseInstalling(int day, int start, int end) {
        return cannotRequireResource(param.getNurses()[day],
                                     1,
                                     start,
                                     end,
                                     nurseWorkload[day]);
    }

    protected boolean cannotRequireNurseTreating(int day, int start, int end) {
        return cannotRequireResource(param.getNurses()[day],
                                     (float) 1 / param.getMultitasks(),
                                     start,
                                     end,
                                     nurseWorkload[day]);
    }

    protected boolean cannotRequireDoctor(long sectorId, int day, int start, int end) {
        return cannotRequireResource(param.getDoctors().get(sectorId)[day],
                                     start,
                                     end,
                                     doctorWorkload.get(sectorId)[day]);
    }

    /**
     * If cannot use <tt>1</tt> of <tt>resourceOnDay</tt> from
     * <tt>start</tt> to <tt>end</tt> (exclusive), given the
     * current <tt>workloadOnDay</tt> of resource
     */
    private boolean cannotRequireResource(int[] resourceOnDay,
                                          int start,
                                          int end,
                                          int[] workloadOnDay) {
        if (start > resourceOnDay.length || end > resourceOnDay.length)
            return true;
        for (int i = start; i < end; i++) {
            if (workloadOnDay[i] + 1 > resourceOnDay[i])
                return true;
        }
        return false;
    }

    private boolean cannotRequireResource(int[] resourceOnDay,
                                          float amount,
                                          int start,
                                          int end,
                                          float[] workloadOnDay) {
        if (start > resourceOnDay.length || end > resourceOnDay.length)
            return true;
        for (int i = start; i < end; i++) {
            if (workloadOnDay[i] + amount > resourceOnDay[i])
                return true;
        }
        return false;
    }

    /**
     * If cannot use <tt>resourceOnDay</tt> from <tt>start</tt> to <tt>end</tt>
     * (exclusive)
     */
    private boolean cannotRequireBinaryResource(boolean[] resourceOnDay,
                                                int start,
                                                int end) {
        if (start >= resourceOnDay.length || end >= resourceOnDay.length)
            return true;
        for (int i = start; i < end; i++) {
            if (!resourceOnDay[i])
                return true;
        }
        return false;
    }

    private boolean overuseConstantResource(int constant,
                                            int start,
                                            int end,
                                            int[] workloadOnDay) {
        for (int i = start; i < end; i++) {
            if (workloadOnDay[i] + 1 > constant)
                return true;
        }
        return false;
    }

    protected void registerWorkload(List<Allocation> allocations) {
        // Register workload
        allocations.forEach(allocation -> {
            // Doctor
            registerWorkload(doctorWorkload.get(allocation.sectorId()),
                             allocation.sessionDay(),
                             allocation.startConsultation(),
                             allocation.endConsultation()
                            );
            // Nurse on installation
            registerWorkload(nurseWorkload,
                             allocation.sessionDay(),
                             allocation.startInstallation(),
                             allocation.endInstallation(),
                             1);
            // Material
            registerWorkload(materialWorkload,
                             allocation.sessionDay(),
                             allocation.startInstallation(),
                             allocation.endTreatment()
                            );
            // Nurse on treatment
            registerWorkload(nurseWorkload,
                             allocation.sessionDay(),
                             allocation.startTreatment(),
                             allocation.endTreatment(),
                             (float) 1 / param.getMultitasks());
        });
    }
}
