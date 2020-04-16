package utils;

import instance.Instance;

public interface CPlexDataWriter extends AutoCloseable {
    void cprint(Instance instance);
}
