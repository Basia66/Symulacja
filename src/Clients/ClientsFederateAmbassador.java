package Clients;

import Abstract.FederateAmbassador;
import hla.rti1516e.*;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.time.HLAfloat64Time;

public class ClientsFederateAmbassador extends FederateAmbassador
{
	ClientsFederate federate;

	public ClientsFederateAmbassador(ClientsFederate clientsFederate) {
		super(clientsFederate);
		federate = clientsFederate;
	}

	@Override
	public void reflectAttributeValues(ObjectInstanceHandle theObject, AttributeHandleValueMap theAttributes, byte[] tag, OrderType sentOrdering, TransportationTypeHandle theTransport, LogicalTime time, OrderType receivedOrdering, SupplementalReflectInfo reflectInfo) throws FederateInternalError
	{

	}

	@Override
	public void receiveInteraction(InteractionClassHandle interactionClass, ParameterHandleValueMap theParameters, byte[] tag, OrderType sentOrdering, TransportationTypeHandle theTransport, LogicalTime time, OrderType receivedOrdering, SupplementalReceiveInfo receiveInfo) throws FederateInternalError
	{
		try {
			if (interactionClass.equals(federate.clientsExitsRestaurantInteraction.interactionClassHandle)) {
				federate.clientsExitsRestaurantInteraction.receiveInteraction(theParameters);
				federate.handle_clientsExitsRestaurant(((HLAfloat64Time) time).getValue());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
