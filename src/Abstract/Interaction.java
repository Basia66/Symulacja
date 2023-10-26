package Abstract;

import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.exceptions.RTIexception;

public abstract class Interaction {
    protected Federate federate;
    public InteractionClassHandle interactionClassHandle;
    public Interaction(Federate federate) throws RTIexception {
        this.federate = federate;
        interactionClassHandle = federate.getRtiAmbassador().getInteractionClassHandle("HLAinteractionRoot." + this.getClass().getName());
    }

    protected abstract void prepareParametersHandlers() throws RTIexception;
    protected abstract void resolveIncomingParams(ParameterHandleValueMap params) throws DecoderException;
    public abstract void send() throws RTIexception;

    public void receiveInteraction(ParameterHandleValueMap params) throws RTIexception, DecoderException {
//        federate.stopAdvancingTime();
        resolveIncomingParams(params);
    }

    public void subscribe() throws RTIexception {
        prepareParametersHandlers();
        federate.getRtiAmbassador().subscribeInteractionClass(interactionClassHandle);
    }
    public void publish() throws RTIexception {
        federate.getRtiAmbassador().publishInteractionClass(interactionClassHandle);
    }
}
