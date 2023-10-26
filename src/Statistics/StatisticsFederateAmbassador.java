package Statistics;

import Abstract.FederateAmbassador;
import hla.rti1516e.*;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.time.HLAfloat64Time;

public class StatisticsFederateAmbassador extends FederateAmbassador {

	StatisticsFederate federate;

	public StatisticsFederateAmbassador(StatisticsFederate federate) {
		super(federate);
		this.federate = federate;
	}

	@Override
	public void receiveInteraction(InteractionClassHandle interactionClass, ParameterHandleValueMap theParameters, byte[] tag, OrderType sentOrdering, TransportationTypeHandle theTransport, LogicalTime time, OrderType receivedOrdering, SupplementalReceiveInfo receiveInfo) throws FederateInternalError
	{
		try {
			if (interactionClass.equals(federate.newClientsOccupyTableInteraction.interactionClassHandle)) {
				federate.newClientsOccupyTableInteraction.receiveInteraction(theParameters);
				federate.handle_newClientsOccupyTable(((HLAfloat64Time) time).getValue());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}