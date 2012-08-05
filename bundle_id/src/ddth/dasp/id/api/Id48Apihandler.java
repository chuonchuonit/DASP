package ddth.dasp.id.api;

import ddth.dasp.common.id.IdGenerator;

public class Id48Apihandler extends AbstractIdApiHandler {

    public Id48Apihandler() {
    }

    public Id48Apihandler(IdGenerator idGen) {
        super(idGen);
    }

    @Override
    protected Object internalHandleApiCall(Object params, String authKey) {
        return getIdGenerator().generateId48();
    }
}
