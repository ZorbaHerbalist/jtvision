package info.qbnet.jtvision.core.app;

import info.qbnet.jtvision.backend.factory.BackendType;

public class TApplication extends TProgram {

    /**
     * Creates a new application using the specified backend type.
     */
    public TApplication(BackendType type)  {
        super(type);

        logger.debug("{} TApplication@TApplication(type={})", getLogName(), type);
    }

}
