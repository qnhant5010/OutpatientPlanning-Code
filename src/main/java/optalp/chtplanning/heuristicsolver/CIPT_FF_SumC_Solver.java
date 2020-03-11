package optalp.chtplanning.heuristicsolver;

import optalp.chtplanning.common.PatientCycleDemand;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Chained ideal processing time.
 * <br>
 * Increasing / Decreasing number of sessions in {@link PatientCycleDemand}
 * and increasing / decreasing ideal total processing time
 */
public class CIPT_FF_SumC_Solver extends FF_SumC_Solver {
    private Comparator<PatientCycleDemand> comparator;

    public CIPT_FF_SumC_Solver(boolean demandLengthIncreasing,
                               boolean totalIdealProcessingTimeIncreasing) {
        comparator = Comparator.comparing(this::getDemandLength,
                                          demandLengthIncreasing ? Comparator.naturalOrder()
                                                                 : Comparator.reverseOrder())
                               .thenComparing(this::getIdealTotalLength,
                                              totalIdealProcessingTimeIncreasing ? Comparator.naturalOrder()
                                                                                 : Comparator.reverseOrder());
    }

    @Override
    protected List<PatientCycleDemand> sortCycleDemands(List<PatientCycleDemand> unorderedList) {
        return unorderedList.stream()
                            .sorted(comparator)
                            .collect(Collectors.toList());
    }

    protected int getDemandLength(PatientCycleDemand cycleDemand) {
        return cycleDemand.getRdvDemands().size();
    }

    protected int getIdealTotalLength(PatientCycleDemand cycleDemand) {
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
