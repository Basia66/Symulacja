package Interactions;

import Abstract.Federate;
import Abstract.Interaction;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.exceptions.RTIexception;
import org.portico.impl.hla1516e.types.encoding.HLA1516eInteger32BE;

public class ClientsImpatiented extends Interaction {
    ParameterHandle tableIdHandle;

    public int clientId;

    public ClientsImpatiented(Federate federate) throws RTIexception {
        super(federate);
    }

    @Override
    protected void prepareParametersHandlers() throws RTIexception {
        tableIdHandle = federate.getRtiAmbassador().getParameterHandle(interactionClassHandle, "clientId");
    }

    @Override
    protected void resolveIncomingParams(ParameterHandleValueMap params) throws DecoderException {
        HLAinteger32BE tmp = new HLA1516eInteger32BE();
        tmp.decode(params.get(tableIdHandle));
        clientId = tmp.getValue();
    }

    @Override
    public void send() throws RTIexception {
        ParameterHandleValueMap params = federate.getRtiAmbassador().getParameterHandleValueMapFactory().create(1);
        params.put(
                federate.getRtiAmbassador().getParameterHandle(interactionClassHandle, "clientId"),
                federate.getEncoderFactory().createHLAinteger32BE(this.clientId).toByteArray()
        );
        federate.getRtiAmbassador().sendInteraction(interactionClassHandle, params, federate.generateTag(), federate.getTimeForSync());
    }
}