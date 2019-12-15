package optalp.chtplanning.common.objective;

public abstract class MonoObjective<N extends Number> extends Objective {
    protected final N value;

    protected MonoObjective(N value) {this.value = value;}

    public N getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
