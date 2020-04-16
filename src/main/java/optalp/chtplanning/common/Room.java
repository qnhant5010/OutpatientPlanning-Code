package optalp.chtplanning.common;

import lombok.NonNull;
import lombok.Value;

import java.util.Objects;
import java.util.Set;

@Value
public class Room {
    /**
     * Id of materials
     */
    @NonNull
    Set<Long> materials;

    public Room(@NonNull Set<Long> materials) {
        if (materials.isEmpty())
            throw new IllegalArgumentException("Room must not be empty");
        this.materials = materials;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return getMaterials().equals(room.getMaterials());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMaterials());
    }
}
