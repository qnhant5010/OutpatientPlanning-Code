package optalp.chtplanning.common;

import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
public class Param {
    /**
     * Count from 0 to <tt>days</tt>
     */
    private int days;
    /**
     * Count from 1 to <tt>numTimeSlots + 1</tt>
     */
    private int numTimeSlots;
    private Set<Long> sectorIds;
    private int multitasks;
    private int numMaterials;
    /**
     * Multiple of time slots
     */
    private int consultationLength;
    /**
     * Multiple of time slots
     */
    private int installationLength;
    /**
     * Nurses capacity, should be multiplied by {@link #multitasks}
     */
    private int[][] nurses;
    private Map<Long, int[][]> doctors;
    private boolean[][] pharmacy;
}
