package optalp.chtplanning.common.objective;

public class IntegerObjective extends MonoObjective<Integer> {

    public IntegerObjective(Integer value) {
        super(value);
    }

    @Override
    public int compareTo(Objective o) {
        if (!(o instanceof IntegerObjective))
            throw new UnsupportedOperationException();
        IntegerObjective that = (IntegerObjective) o;
        return this.value - that.value;
    }
}
