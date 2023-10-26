package Interactions;

import Abstract.Federate;
import Abstract.Interaction;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.exceptions.RTIexception;
import org.portico.impl.hla1516e.types.encoding.HLA1516eInteger32BE;

public class NewClientsCame extends Interaction {
    ParameterHandle tableIdHandle;
    ParameterHandle numberOfPeopleInGroupHandle;

    public int clientId;
    public int numberOfPeopleInGroup;

    public NewClientsCame(Federate federate) throws RTIexception {
        super(federate);
    }

    @Override
    protected void prepareParametersHandlers() throws RTIexception {
        tableIdHandle = federate.getRtiAmbassador().getParameterHandle(interactionClassHandle, "clientId");
        numberOfPeopleInGroupHandle = federate.getRtiAmbassador().getParameterHandle(interactionClassHandle, "numberOfPeopleInGroup");
    }

    @Override
    protected void resolveIncomingParams(ParameterHandleValueMap params) throws DecoderException {
        HLAinteger32BE tmp = new HLA1516eInteger32BE();
        tmp.decode(params.get(tableIdHandle));
        clientId = tmp.getValue();
        tmp.decode(params.get(numberOfPeopleInGroupHandle));
        numberOfPeopleInGroup = tmp.getValue();
    }

    @Override
    public void send() throws RTIexception {
        ParameterHandleValueMap params = federate.getRtiAmbassador().getParameterHandleValueMapFactory().create(2);
        params.put(
                federate.getRtiAmbassador().getParameterHandle(interactionClassHandle, "clientId"),
                federate.getEncoderFactory().createHLAinteger32BE(this.clientId).toByteArray()
        );
        params.put(
                federate.getRtiAmbassador().getParameterHandle(interactionClassHandle, "numberOfPeopleInGroup"),
                federate.getEncoderFactory().createHLAinteger32BE(this.numberOfPeopleInGroup).toByteArray()
        );
        federate.getRtiAmbassador().sendInteraction(interactionClassHandle, params, federate.generateTag(), federate.getTimeForSync());
    }
}