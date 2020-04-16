package optalp.chtplanning.common.objective;

import lombok.NonNull;

public abstract class MonoObjective<N extends Number> extends Objective {
    @NonNull
    protected final N value;

    protected MonoObjective(@NonNull N value) {this.value = value;}

    public N getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
