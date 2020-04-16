package optalp.chtplanning.common;

import lombok.Builder;
import lombok.Value;

import java.util.Map;
import java.util.Set;

@Value
@Builder
public class Param {
    /**
     * Count from 0 to <tt>days</tt>
     */
    int days;
    /**
     * Count from 1 to <tt>numTimeSlots + 1</tt>
     */
    int numTimeSlots;
    Set<Long> sectorIds;
    int multitasks;
    int numMaterials;
    /**
     * Nurses capacity, should be multiplied by {@link #multitasks}
     */
    int[][] nurses;
    Map<Long, int[][]> doctors;
    boolean[][] pharmacy;

    /**
     * Allow modifying {@link Room} on the fly
     */
    Set<Room> rooms;
}
