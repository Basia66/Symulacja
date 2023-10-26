package Interactions;

import Abstract.Federate;
import Abstract.Interaction;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.exceptions.RTIexception;
import org.portico.impl.hla1516e.types.encoding.HLA1516eInteger32BE;

public class WaiterServingTable extends Interaction {
    ParameterHandle waiterIdHandle;
    ParameterHandle tableIdHandle;
    public int waiterId;
    public int tableId;

    public WaiterServingTable(Federate federate) throws RTIexception {
        super(federate);
    }

    @Override
    protected void prepareParametersHandlers() throws RTIexception {
        waiterIdHandle = federate.getRtiAmbassador().getParameterHandle(interactionClassHandle, "waiterId");
        tableIdHandle = federate.getRtiAmbassador().getParameterHandle(interactionClassHandle, "tableId");
    }

    @Override
    protected void resolveIncomingParams(ParameterHandleValueMap params) throws DecoderException {
        HLAinteger32BE tmp = new HLA1516eInteger32BE();
        tmp.decode(params.get(waiterIdHandle));
        waiterId = tmp.getValue();
        tmp.decode(params.get(tableIdHandle));
        tableId = tmp.getValue();
    }

    @Override
    public void send() throws RTIexception {
        ParameterHandleValueMap params = federate.getRtiAmbassador().getParameterHandleValueMapFactory().create(2);
        params.put(
                federate.getRtiAmbassador().getParameterHandle(interactionClassHandle, "waiterId"),
                federate.getEncoderFactory().createHLAinteger32BE(this.waiterId).toByteArray()
        );
        params.put(
                federate.getRtiAmbassador().getParameterHandle(interactionClassHandle, "tableId"),
                federate.getEncoderFactory().createHLAinteger32BE(this.tableId).toByteArray()
        );
        federate.getRtiAmbassador().sendInteraction(interactionClassHandle, params, federate.generateTag(), federate.getTimeForSync());
        federate.log("[" + federate.getSimTime() + "] "+this.getClass().getName()+" has been send with time " + federate.getTimeForSync());
    }
}