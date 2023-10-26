package Interactions;

import Abstract.Federate;
import Abstract.Interaction;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.HLAfloat64BE;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.exceptions.RTIexception;
import org.portico.impl.hla1516e.types.encoding.HLA1516eFloat64BE;
import org.portico.impl.hla1516e.types.encoding.HLA1516eInteger32BE;

/**
 * Klasa obsługująca interakcje wejścia klientów do restauracji i przy tym zajęciu stolika
 */
public class NewClientsOccupyTableInteraction extends Interaction {
    ParameterHandle clientIdHandle;
    ParameterHandle tableIdHandle;
    ParameterHandle numberOfPeopleInGroupHandle;
    ParameterHandle timeOfWaitingHandle;

    public int clientId;
    public int tableId;
    public int numberOfPeopleInGroup;
    public double timeOfWaiting;

    public NewClientsOccupyTableInteraction(Federate federate) throws RTIexception {
        super(federate);
    }

    @Override
    protected void prepareParametersHandlers() throws RTIexception {
        clientIdHandle = federate.getRtiAmbassador().getParameterHandle(interactionClassHandle, "clientId");
        tableIdHandle = federate.getRtiAmbassador().getParameterHandle(interactionClassHandle, "tableId");
        numberOfPeopleInGroupHandle = federate.getRtiAmbassador().getParameterHandle(interactionClassHandle, "numberOfPeopleInGroup");
        timeOfWaitingHandle = federate.getRtiAmbassador().getParameterHandle(interactionClassHandle, "timeOfWaiting");
    }

    @Override
    protected void resolveIncomingParams(ParameterHandleValueMap params) throws DecoderException {
        HLAinteger32BE tmp = new HLA1516eInteger32BE();
        tmp.decode(params.get(clientIdHandle));
        clientId = tmp.getValue();
        tmp.decode(params.get(tableIdHandle));
        tableId = tmp.getValue();
        tmp.decode(params.get(numberOfPeopleInGroupHandle));
        numberOfPeopleInGroup = tmp.getValue();

        HLAfloat64BE tmp1 = new HLA1516eFloat64BE();
        tmp1.decode(params.get(timeOfWaitingHandle));
        timeOfWaiting = tmp1.getValue();
    }

    @Override
    public void send() throws RTIexception {
        ParameterHandleValueMap params = federate.getRtiAmbassador().getParameterHandleValueMapFactory().create(4);
        params.put(
                federate.getRtiAmbassador().getParameterHandle(interactionClassHandle, "clientId"),
                federate.getEncoderFactory().createHLAinteger32BE(this.clientId).toByteArray()
        );
        params.put(
                federate.getRtiAmbassador().getParameterHandle(interactionClassHandle, "tableId"),
                federate.getEncoderFactory().createHLAinteger32BE(this.tableId).toByteArray()
        );
        params.put(
                federate.getRtiAmbassador().getParameterHandle(interactionClassHandle, "numberOfPeopleInGroup"),
                federate.getEncoderFactory().createHLAinteger32BE(this.numberOfPeopleInGroup).toByteArray()
        );
        params.put(
                federate.getRtiAmbassador().getParameterHandle(interactionClassHandle, "timeOfWaiting"),
                federate.getEncoderFactory().createHLAfloat64BE(this.timeOfWaiting).toByteArray()
        );

        federate.getRtiAmbassador().sendInteraction(interactionClassHandle, params, federate.generateTag(), federate.getTimeForSync());
        federate.log("[" + federate.getSimTime() + "] "+this.getClass().getName()+" has been send with time " + federate.getTimeForSync());
    }
}
