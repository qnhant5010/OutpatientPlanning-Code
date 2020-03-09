package optalp.chtplanning.heuristicsolver;

import optalp.chtplanning.common.PatientCycleDemand;

import java.util.Comparator;
import java.util.List;

/**
 * Non-increasing order of ideal total processing time
 */
public class LIPT_FF_SumC_Solver extends FF_SumC_Solver {

    @Override
    protected List<PatientCycleDemand> sortCycleDemands(List<PatientCycleDemand> unorderedList) {
        unorderedList.sort(Comparator.comparing(this::getTotalLength).reversed());
        return unorderedList;
    }

    protected int getTotalLength(PatientCycleDemand cycleDemand) {
        return cycleDemand.getRdvDemands().stream().mapToInt(
                rdvDemand -> (rdvDemand.isNeedingConsultation()
                              ? param.getConsultationLength()
                              : 0)
                             + (rdvDemand.isMedPreparedSameDay()
                                ? Math.max(param.getInstallationLength(),
                                           rdvDemand.getMedPrepDuration())
                                : param.getInstallationLength())
                             + rdvDemand.getTreatmentDuration()
                                                            ).sum();
    }
}