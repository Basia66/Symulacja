package Interactions;

import Abstract.Federate;
import Abstract.Interaction;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.exceptions.RTIexception;
import org.portico.impl.hla1516e.types.encoding.HLA1516eInteger32BE;

/**
 * Klasa obsługująca interakcje wyjścia z restauracji i zwolnienia stolika
 */
public class ClientsExitsRestaurantInteraction extends Interaction {
    ParameterHandle clientIdHandle;
    ParameterHandle tableIdHandle;

    public int clientId;
    public int tableId;

    public ClientsExitsRestaurantInteraction(Federate federate) throws RTIexception {
        super(federate);
    }

    @Override
    protected void prepareParametersHandlers() throws RTIexception {
        clientIdHandle = federate.getRtiAmbassador().getParameterHandle(interactionClassHandle, "clientId");
        tableIdHandle = federate.getRtiAmbassador().getParameterHandle(interactionClassHandle, "tableId");
    }

    @Override
    protected void resolveIncomingParams(ParameterHandleValueMap params) throws DecoderException {
        HLAinteger32BE tmp = new HLA1516eInteger32BE();
        tmp.decode(params.get(clientIdHandle));
        clientId = tmp.getValue();
        tmp.decode(params.get(tableIdHandle));
        tableId = tmp.getValue();
    }

    @Override
    public void send() throws RTIexception {
        ParameterHandleValueMap params = federate.getRtiAmbassador().getParameterHandleValueMapFactory().create(2);
        params.put(
                federate.getRtiAmbassador().getParameterHandle(interactionClassHandle, "clientId"),
                federate.getEncoderFactory().createHLAinteger32BE(this.clientId).toByteArray()
        );
        params.put(
                federate.getRtiAmbassador().getParameterHandle(interactionClassHandle, "tableId"),
                federate.getEncoderFactory().createHLAinteger32BE(this.tableId).toByteArray()
        );
        federate.getRtiAmbassador().sendInteraction(interactionClassHandle, params, federate.generateTag(), federate.getTimeForSync());
        federate.log("[" + federate.getSimTime() + "] "+this.getClass().getName()+" has been send with time " + federate.getTimeForSync());
    }
}