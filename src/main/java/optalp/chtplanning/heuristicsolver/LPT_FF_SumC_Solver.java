package optalp.chtplanning.heuristicsolver;

import optalp.chtplanning.common.PatientCycleDemand;

import java.util.Comparator;
import java.util.List;

/**
 * Non-increasing order of total execution time of all stages
 */
public class LPT_FF_SumC_Solver extends FF_SumC_Solver {

    @Override
    protected List<PatientCycleDemand> sortCycleDemands(List<PatientCycleDemand> unorderedList) {
        unorderedList.sort(Comparator.comparing(this::getTotalLength).reversed());
        return unorderedList;
    }

    protected int getTotalLength(PatientCycleDemand cycleDemand) {
        return cycleDemand.getRdvDemands().stream().mapToInt(
                rdvDemand -> rdvDemand.getConsultationDuration()
                        + rdvDemand.getInstallationDuration()
                        + rdvDemand.getDrugMixingDuration()
                        + rdvDemand.getTreatmentDuration()
                                                            ).sum();
    }

}
