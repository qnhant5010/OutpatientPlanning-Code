package optalp.chtplanning.simplelptsolver;

import optalp.chtplanning.common.PatientCycleDemand;

import java.util.Comparator;
import java.util.List;

public class SPT_FFS_SumC_Solver extends FFS_SumC_Solver {

    @Override
    protected List<PatientCycleDemand> sortCycleDemands(List<PatientCycleDemand> unorderedList) {
        unorderedList.sort(Comparator.comparing(this::getTotalLength));
        return unorderedList;
    }

    protected int getTotalLength(PatientCycleDemand cycleDemand) {
        return cycleDemand.getRdvDemands().stream().mapToInt(
                rdvDemand -> (rdvDemand.isNeedingConsultation() ?
                              param.getConsultationLength() : 0)
                             + param.getInstallationLength()
                             + rdvDemand.getMedPrepDuration()
                             + rdvDemand.getTreatmentDuration()
                                                            ).sum();
    }

}
