package optalp.chtplanning.heuristicsolver;

import optalp.chtplanning.common.*;
import optalp.chtplanning.common.solution.MinimizingMeanFlowTimeSolution;

import java.util.ArrayList;
import java.util.List;

/**
 * First fit strategy, using fifo list
 */
public class FF_SumC_Solver extends Solver<MinimizingMeanFlowTimeSolution> {
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
                                 - (Math.max((rdvDemand.isDrugMixingSameDay() ? rdvDemand.getDrugMixingDuration() : 0),
                                             rdvDemand.getInstallationDuration()
                                            ))
                                 - rdvDemand.getConsultationDuration();
        // Start to find the first plausible position
        for (int startSession = 0; startSession <= latestStartSession; startSession++) {
            allocation.session().start(startSession);
            /////////////////////////////
            // Check consultation step
            // Start the consultation as soon as a suitable doctor is available
            allocation.consultation().start(startSession);
            if (cannotRequireDoctor(rdvDemand.getSectorId(),
                                    day,
                                    startSession,
                                    startSession + rdvDemand.getConsultationDuration()))
                continue;
            allocation.consultation().end(startSession + rdvDemand.getConsultationDuration());
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
            if (rdvDemand.isDrugMixingSameDay()) {
                // If prepare same day,
                // The earliest start is after consultation
                earliestMedPrepDay = day;
                earliestMedPrepTime = allocation.consultation().end();
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
                                             medPrepStartTime + rdvDemand.getDrugMixingDuration()
                                            ))
                    medPrepStartTime++;
                if (medPrepStartTime + rdvDemand.getDrugMixingDuration() <= param.getNumTimeSlots())
                    break;
            }
            // If pharmacy is not available for the day before nor this day,
            // no way this booking is possible
            if (medPrepStartTime + rdvDemand.getDrugMixingDuration() > param.getNumTimeSlots())
                return null;
            allocation.drugMixingDay(medPrepDay);
            allocation.drugMixing().start(medPrepStartTime);
            allocation.drugMixing().end(medPrepStartTime + rdvDemand.getDrugMixingDuration());
            /////////////////////////////
            // A patient will occupy a material from start of installation to
            // end of treatment
            allocation.installation().start(allocation.consultation().end() - 1);
            do {
                allocation.installation().start(allocation.installation().start() + 1);
                allocation.installation().end(allocation.installation().start() + rdvDemand.getInstallationDuration());
                if (allocation.installation().end() > param.getNumTimeSlots())
                    return null;
                // Check installation step
                // If no nurse or material available, wait
                if (cannotRequireNurseInstalling(day,
                                                 allocation.installation().start(),
                                                 allocation.installation().end())
                    || cannotRequireMaterial(day,
                                             allocation.installation().start(),
                                             allocation.installation().end()
                                            ))
                    continue;
                /////////////////////////////
                // Check treatment step
                if (allocation.drugMixingDay() == day)
                    allocation.treatment().start(Math.max(allocation.drugMixing().end(),
                                                       allocation.installation().end()));
                else
                    allocation.treatment().start(allocation.installation().end());
                // Until a nurse is available, wait
                while ((cannotRequireNurseTreating(day,
                                                   allocation.treatment().start(),
                                                   allocation.treatment().start() + rdvDemand.getTreatmentDuration())
                        || cannotRequireMaterial(day,
                                                 allocation.treatment().start(),
                                                 allocation.treatment().start() + rdvDemand.getTreatmentDuration()))
                       && allocation.treatment().start() + rdvDemand.getTreatmentDuration() <= param.getNumTimeSlots())
                    allocation.treatment().start(allocation.treatment().start() + 1);
                allocation.treatment().end(allocation.treatment().start() + rdvDemand.getTreatmentDuration());
                if (allocation.treatment().end() > param.getNumTimeSlots())
                    // No way the next tries will yield a good solution
                    // Because the next starting points will not lead to any resource availability sooner
                    return null;
                else {
                    // Must verify availabilities of material on whole process
                    // If a place is ready for whole process time, return this value
                    if (!cannotRequireMaterial(day,
                                               allocation.installation().start(),
                                               allocation.treatment().end()))
                        break;
                }
            } while (allocation.treatment().end() <= param.getNumTimeSlots());
            if (allocation.treatment().end() > param.getNumTimeSlots())
                return null;
            /////////////////////////////
            // Finalize
            allocation.session().end(allocation.treatment().end());
            if (allocation.session().end() > param.getNumTimeSlots())
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
