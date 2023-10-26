package Waiters;

import Abstract.FederateAmbassador;
import hla.rti1516e.*;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.time.HLAfloat64Time;

public class WaitersFederateAmbassador extends FederateAmbassador {
	WaitersFederate federate;
	public WaitersFederateAmbassador(WaitersFederate federate) {
		super(federate);
		this.federate = federate;
	}

	@Override
	public void discoverObjectInstance(ObjectInstanceHandle theObject,
									   ObjectClassHandle theObjectClass,
									   String objectName)
			throws FederateInternalError {
		log("Discoverd Object: handle=" + theObject + ", classHandle=" + theObjectClass + ", name=" + objectName);
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
		try {
		}
		catch (Exception e) {
			e.printStackTrace();
		}
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
			if (interactionClass.equals(federate.clientsExitsRestaurantInteraction.interactionClassHandle)) {
				federate.clientsExitsRestaurantInteraction.receiveInteraction(theParameters);
				federate.handle_clientsExitsRestaurant(((HLAfloat64Time) time).getValue());
			}
			if (interactionClass.equals(federate.callWaiterInteraction.interactionClassHandle)) {
				federate.callWaiterInteraction.receiveInteraction(theParameters);
				federate.handle_callWaiterInteraction(((HLAfloat64Time) time).getValue());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}