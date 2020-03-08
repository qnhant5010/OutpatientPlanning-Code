package optalp.chtplanning.simplelptsolver;

import optalp.chtplanning.common.*;
import optalp.chtplanning.common.solution.MinimizingMeanFlowTimeSolution;

import java.util.ArrayList;
import java.util.List;

/**
 * First fit strategy, using unordered list
 */
public class FFS_SumC_Solver extends Solver<MinimizingMeanFlowTimeSolution> {
    MinimizingMeanFlowTimeSolution solution;

    @Override
    public MinimizingMeanFlowTimeSolution solve(Param param,
                                                List<PatientCycleDemand> cycleDemands,
                                                List<Allocation> existingAllocations)
            throws SolverException {
        this.param = param;
        solution = new MinimizingMeanFlowTimeSolution(param.getNumTimeSlots());
        initWorkloadWith(existingAllocations);
        this.sortCycleDemands(new ArrayList<>(cycleDemands)).forEach(this::allocate);
        solution.deduceObjectiveValue();
        return solution;
    }

    /**
     * @param unorderedList copy of input
     * @return custom ordered list
     */
    protected List<PatientCycleDemand> sortCycleDemands(List<PatientCycleDemand> unorderedList) {
        return unorderedList;
    }

    protected void allocate(PatientCycleDemand cycleDemand) throws SolverException {
        List<PatientRdvDemand> rdvDemands = cycleDemand.getRdvDemands();
        for (int i = 1; i <= param.getDays(); i++) {
            List<Allocation> attempts = new ArrayList<>();
            // Try to book all requests
            boolean allRdvDemandsFitted = true;
            int day = i;
            for (PatientRdvDemand rdvDemand : rdvDemands) {
                // Calculate release day of request
                day += rdvDemand.getAfterLastRequest();
                if (day > param.getDays()) {
                    allRdvDemandsFitted = false;
                    break;
                }
                // Try to book demand on day
                Allocation attempt = this.tryBooking(rdvDemand, day);
                if (attempt != null) {
                    attempt.id(attempts.size() + 1);
                    attempts.add(attempt);
                } else {
                    // This day is not good
                    allRdvDemandsFitted = false;
                    break;
                }
            }
            if (allRdvDemandsFitted) {
                // Stop after the first solution
                // All sessions are fitted with this day as depart
                register(attempts, cycleDemand.getId());
                return;
            }
        }
        throw new SolverException("Cannot fit demand " + cycleDemand);
    }

    /**
     * @param rdvDemand
     * @param day       >= 1 and <= <tt>param.getDays()</tt>
     * @return
     */
    protected Allocation tryBooking(PatientRdvDemand rdvDemand, int day) {
        Allocation allocation = Allocation.builder()
                                          .sessionDay(day)
                                          .sectorId(rdvDemand.getSectorId())
                                          .build();
        // Define the last time slot we can start
        int latestStartSession = param.getNumTimeSlots()
                                 - rdvDemand.getTreatmentDuration()
                                 - (Math.max((rdvDemand.isMedPreparedSameDay() ? rdvDemand.getMedPrepDuration() : 0),
                                             param.getInstallationLength()
                                            ))
                                 - (rdvDemand.isNeedingConsultation() ? param.getConsultationLength() : 0);
        // Start to find the first plausible position
        for (int startSession = 0; startSession <= latestStartSession; startSession++) {
            allocation.startSession(startSession);
            /////////////////////////////
            // Check consultation step
            // Start the consultation as soon as a suitable doctor is available
            allocation.startConsultation(startSession);
            if (rdvDemand.isNeedingConsultation()) {
                if (cannotRequireDoctor(rdvDemand.getSectorId(),
                                        day,
                                        startSession,
                                        startSession + param.getConsultationLength()))
                    continue;
                allocation.endConsultation(startSession + param.getConsultationLength());
            } else
                allocation.endConsultation(startSession);
            //            // If no consultation, then patient should not wait until both
            //            // nurse and material is available
            //            if (!rdvDemand.isNeedingConsultation() &&
            //                    (cannotRequireNurseInstalling(day,
            //                                                  startSession,
            //                                                  startSession + param.getInstallationLength())
            //                            || cannotRequireMaterial(day,
            //                                                     startSession,
            //                                                     startSession + param.getInstallationLength()
            //                                                    ))
            //            )
            //                continue;
            /////////////////////////////
            // Check med preparation step
            int earliestMedPrepDay, earliestMedPrepTime;
            if (rdvDemand.isMedPreparedSameDay()) {
                // If prepare same day,
                // The earliest start is after consultation
                earliestMedPrepDay = day;
                earliestMedPrepTime = allocation.endConsultation();
            } else {
                // If we can prepare in advance
                // Check from the day before
                earliestMedPrepDay = day - 1;
                earliestMedPrepTime = 0;
            }
            // Wait until pharmacy is available
            int medPrepDay, medPrepStartTime = 0;
            for (medPrepDay = earliestMedPrepDay; medPrepDay <= day; medPrepDay++) {
                medPrepStartTime = earliestMedPrepTime;
                while (medPrepStartTime <= param.getNumTimeSlots() &&
                       cannotRequirePharmacy(medPrepDay,
                                             medPrepStartTime,
                                             medPrepStartTime + rdvDemand.getMedPrepDuration()
                                            ))
                    medPrepStartTime++;
                if (medPrepStartTime + rdvDemand.getMedPrepDuration() <= param.getNumTimeSlots())
                    break;
            }
            // If pharmacy is not available for the day before nor this day,
            // no way this booking is possible
            if (medPrepStartTime + rdvDemand.getMedPrepDuration() > param.getNumTimeSlots())
                return null;
            allocation.medPrepDay(medPrepDay);
            allocation.startMedPrep(medPrepStartTime);
            allocation.endMedPrep(medPrepStartTime + rdvDemand.getMedPrepDuration());
            /////////////////////////////
            // A patient will occupy a material from start of installation to
            // end of treatment
            allocation.startInstallation(allocation.endConsultation() - 1);
            do {
                allocation.startInstallation(allocation.startInstallation() + 1);
                allocation.endInstallation(allocation.startInstallation() + param.getInstallationLength());
                if (allocation.endInstallation() > param.getNumTimeSlots())
                    return null;
                // Check installation step
                // If no nurse or material available, wait
                if (cannotRequireNurseInstalling(day,
                                                 allocation.startInstallation(),
                                                 allocation.endInstallation())
                    || cannotRequireMaterial(day,
                                             allocation.startInstallation(),
                                             allocation.endInstallation()
                                            ))
                    continue;
                /////////////////////////////
                // Check treatment step
                if (allocation.medPrepDay() == day)
                    allocation.startTreatment(Math.max(allocation.endMedPrep(),
                                                       allocation.endInstallation()));
                else
                    allocation.startTreatment(allocation.endInstallation());
                // Until a nurse is available, wait
                while ((cannotRequireNurseTreating(day,
                                                   allocation.startTreatment(),
                                                   allocation.startTreatment() + rdvDemand.getTreatmentDuration())
                        || cannotRequireMaterial(day,
                                                 allocation.startTreatment(),
                                                 allocation.startTreatment() + rdvDemand.getTreatmentDuration()))
                       && allocation.startTreatment() + rdvDemand.getTreatmentDuration() <= param.getNumTimeSlots())
                    allocation.startTreatment(allocation.startTreatment() + 1);
                allocation.endTreatment(allocation.startTreatment() + rdvDemand.getTreatmentDuration());
                if (allocation.endTreatment() > param.getNumTimeSlots())
                    // No way the next tries will yield a good solution
                    // Because the next starting points will not lead to any resource availability sooner
                    return null;
                else {
                    // Must verify availabilities of material on whole process
                    // If a place is ready for whole process time, return this value
                    if (!cannotRequireMaterial(day,
                                               allocation.startInstallation(),
                                               allocation.endTreatment()))
                        break;
                }
            } while (allocation.endTreatment() <= param.getNumTimeSlots());
            if (allocation.endTreatment() > param.getNumTimeSlots())
                return null;
            /////////////////////////////
            // Finalize
            allocation.endSession(allocation.endTreatment());
            if (allocation.endSession() > param.getNumTimeSlots())
                return null;
            else
                return allocation;
        }
        return null;
    }

    /**
     * Save attempts into current solution and register workloads for
     * doctors, nurses and materials
     *
     * @param allocations attempts
     * @param patientId   for who
     */
    protected void register(List<Allocation> allocations,
                            long patientId) {
        solution.save(allocations, patientId);
        registerWorkload(allocations);
    }
}
