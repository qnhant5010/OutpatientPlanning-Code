package optalp.chtplanning.common;

import lombok.Data;
import lombok.NonNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Data
public class Incompatibility {
    @NonNull
    private Set<Long> patients;

    public Incompatibility(Long patient0, Long... others) {
        patients = new HashSet<>();
        patients.add(patient0);
        patients.addAll(Arrays.asList(others));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Incompatibility that = (Incompatibility) o;
        return getPatients().equals(that.getPatients());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPatients());
    }
}
