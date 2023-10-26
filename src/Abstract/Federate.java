package Abstract;

import hla.rti1516e.CallbackModel;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.exceptions.*;
import hla.rti1516e.time.HLAfloat64Interval;
import hla.rti1516e.time.HLAfloat64Time;
import hla.rti1516e.time.HLAfloat64TimeFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class Federate {
    public static final String READY_TO_RUN = "ReadyToRun";
    protected RTIambassador rtiAmbassador;
    protected FederateAmbassador federateAmbassador;
    protected HLAfloat64TimeFactory timeFactory;
    protected EncoderFactory encoderFactory;

    public void log(String message) {
        System.out.println(this.getClass().getName() + ": " + message);
    }

    protected void waitForUser() {
        log(" >>>>>>>>>> Press Enter to Continue <<<<<<<<<<");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            reader.readLine();
        } catch (Exception e) {
            log("Error while waiting for user input: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected abstract FederateAmbassador createAmbassador();

    protected void connectToRti() throws RTIinternalError, ConnectionFailed, InvalidLocalSettingsDesignator, UnsupportedCallbackModel, AlreadyConnected, CallNotAllowedFromWithinCallback {
        log("Creating RTIambassador");
        rtiAmbassador = RtiFactoryFactory.getRtiFactory().getRtiAmbassador();
        encoderFactory = RtiFactoryFactory.getRtiFactory().getEncoderFactory();

        federateAmbassador = createAmbassador();
        // connect
        rtiAmbassador.connect(federateAmbassador, CallbackModel.HLA_EVOKED);
    }

    protected void createFederation() throws Exception {
        log("Creating Federation...");
        try {
            URL[] modules = new URL[]{
                    new File("foms/ProducerConsumer.xml").toURI().toURL(),
            };

            rtiAmbassador.createFederationExecution("ProducerConsumerFederation", modules);
            log("Created Federation");
        } catch (FederationExecutionAlreadyExists exists) {
            log("Didn't create federation, it already existed");
        } catch (MalformedURLException urle) {
            log("Exception loading one of the FOM modules from disk: " + urle.getMessage());
            urle.printStackTrace();
            throw new Exception("Exception loading one of the FOM modules from disk: " + urle.getMessage());
        }
    }

    protected void joinFederation(String federateName) throws CouldNotCreateLogicalTimeFactory, FederateNameAlreadyInUse, FederationExecutionDoesNotExist, SaveInProgress, RestoreInProgress, FederateAlreadyExecutionMember, NotConnected, CallNotAllowedFromWithinCallback, RTIinternalError, FederateNotExecutionMember {
        rtiAmbassador.joinFederationExecution(
                federateName,                    // name for the federate
                "consumer",                        // federate type
                "ProducerConsumerFederation"    // name of federation
        );

        log("Joined Federation as " + federateName);

        // save the time factory for easy access
        this.timeFactory = (HLAfloat64TimeFactory) rtiAmbassador.getTimeFactory();
    }

    protected void startSynchronization() throws SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError, CallNotAllowedFromWithinCallback {
        rtiAmbassador.registerFederationSynchronizationPoint(READY_TO_RUN, null);
        while (!federateAmbassador.isAnnounced) {
            rtiAmbassador.evokeMultipleCallbacks(0.1, 0.2);
        }
        rtiAmbassador.evokeMultipleCallbacks(0.1, 0.2);
    }

    protected void endSynchronization() throws SynchronizationPointLabelNotAnnounced, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError, CallNotAllowedFromWithinCallback {
        rtiAmbassador.synchronizationPointAchieved(READY_TO_RUN);
        log("Achieved sync point: " + READY_TO_RUN + ", waiting for federation...");
        while (!federateAmbassador.isReadyToRun) {
            rtiAmbassador.evokeMultipleCallbacks(0.1, 0.2);
        }
        rtiAmbassador.evokeMultipleCallbacks(0.1, 0.2);
    }

    protected void enableTimePolicy() throws Exception {
        HLAfloat64Interval lookahead = timeFactory.makeInterval(federateAmbassador.federateLookahead);
        this.rtiAmbassador.enableTimeRegulation(lookahead);

        while (!federateAmbassador.isRegulating) {
            rtiAmbassador.evokeMultipleCallbacks(0.1, 0.2);
        }

        this.rtiAmbassador.enableTimeConstrained();

        while (!federateAmbassador.isConstrained) {
            rtiAmbassador.evokeMultipleCallbacks(0.1, 0.2);
        }
    }

    protected void cleanAndExitFederation() throws InvalidResignAction, OwnershipAcquisitionPending, FederateOwnsAttributes, FederateNotExecutionMember, NotConnected, CallNotAllowedFromWithinCallback, RTIinternalError {
        rtiAmbassador.resignFederationExecution(ResignAction.DELETE_OBJECTS);
        log("Resigned from Federation");

        try {
            rtiAmbassador.destroyFederationExecution("ExampleFederation");
            log("Destroyed Federation");
        } catch (FederationExecutionDoesNotExist dne) {
            log("No need to destroy federation, it doesn't exist");
        } catch (FederatesCurrentlyJoined fcj) {
            log("Didn't destroy federation, federates still joined");
        }
    }

    public void runFederate(String federateName) throws Exception {
        connectToRti();

        createFederation();

        joinFederation(federateName);

        startSynchronization();
        waitForUser();
        endSynchronization();

        enableTimePolicy();

        log("<<<<<<<<<< Start >>>>>>>>>>");

        publishAndSubscribe();

        simulationLoop();

        cleanAndExitFederation();
    }

    protected void advanceTime(double timestep) throws RTIexception {
        federateAmbassador.isAdvancing = true;
        HLAfloat64Time time = timeFactory.makeTime(federateAmbassador.federateTime + timestep);
        rtiAmbassador.timeAdvanceRequest(time);
//        log("Time is advancing to: [" + (federateAmbassador.federateTime + timestep) + "]");
        while (federateAmbassador.isAdvancing) {
            rtiAmbassador.evokeMultipleCallbacks(0.1, 0.2);
        }
    }

    public RTIambassador getRtiAmbassador() {
        return rtiAmbassador;
    }

    public EncoderFactory getEncoderFactory() {
        return encoderFactory;
    }

    public byte[] generateTag() {
        return ("(timestamp) " + System.currentTimeMillis()).getBytes();
    }

    public double getSimTime() {
        return federateAmbassador.federateTime;
    }

    public HLAfloat64Time getTimeForSync() {
        return timeFactory.makeTime(getSimTime() + federateAmbassador.federateLookahead);
    }

    protected abstract void simulationLoop() throws RTIexception;

    protected abstract void publishAndSubscribe() throws RTIexception;
}
