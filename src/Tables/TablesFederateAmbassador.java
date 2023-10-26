package Tables;

import Abstract.FederateAmbassador;
import hla.rti1516e.*;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.time.HLAfloat64Time;

public class TablesFederateAmbassador extends FederateAmbassador {
    TablesFederate federate;

    public TablesFederateAmbassador(TablesFederate federate) {
        super(federate);
        this.federate = federate;
    }

    @Override
    public void reflectAttributeValues(ObjectInstanceHandle theObject,
                                       AttributeHandleValueMap theAttributes,
                                       byte[] tag,
                                       OrderType sentOrdering,
                                       TransportationTypeHandle theTransport,
                                       LogicalTime time,
                                       OrderType receivedOrdering,
                                       SupplementalReflectInfo reflectInfo) throws FederateInternalError {


    }

    @Override
    public void receiveInteraction(InteractionClassHandle interactionClass,
                                   ParameterHandleValueMap theParameters,
                                   byte[] tag,
                                   OrderType sentOrdering,
                                   TransportationTypeHandle theTransport,
                                   LogicalTime time,
                                   OrderType receivedOrdering,
                                   SupplementalReceiveInfo receiveInfo) throws FederateInternalError {
        try {
            if (interactionClass.equals(federate.newClientsOccupyTableInteraction.interactionClassHandle)) {
                federate.newClientsOccupyTableInteraction.receiveInteraction(theParameters);
                federate.handle_newClientsOccupyTable(((HLAfloat64Time) time).getValue());
            }
            if (interactionClass.equals(federate.tableServedInteraction.interactionClassHandle)) {
                federate.tableServedInteraction.receiveInteraction(theParameters);
                federate.handle_tableServedInteraction(((HLAfloat64Time) time).getValue());
            }
        }
        catch (Exception e) {
           e.printStackTrace();
        }
    }
}