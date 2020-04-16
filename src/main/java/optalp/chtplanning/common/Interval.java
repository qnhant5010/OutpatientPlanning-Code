package optalp.chtplanning.common;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class Interval {
    /**
     * Inclusive
     */
    private int start;
    /**
     * Exclusive
     */
    private int end;
}
