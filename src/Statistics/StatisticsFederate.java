package Statistics;

import Abstract.Federate;
import Abstract.FederateAmbassador;
import Interactions.NewClientsOccupyTableInteraction;
import Statistics.sim.monitors.Diagram;
import Statistics.sim.monitors.MonitoredVar;
import Statistics.sim.monitors.Statistics;
import hla.rti1516e.exceptions.RTIexception;

import java.awt.*;
import java.util.Random;

public class StatisticsFederate extends Federate {
    public static void main(String[] args) throws Exception {
        new StatisticsFederate().runFederate(StatisticsFederate.class.getName());
    }

    StatisticsFederateAmbassador federateAmbassador;
    Random random = new Random();
    NewClientsOccupyTableInteraction newClientsOccupyTableInteraction;
    MonitoredVar timeOfWaiting = new MonitoredVar();
    MonitoredVar avgTimeOfWaiting = new MonitoredVar();

    @Override
    protected FederateAmbassador createAmbassador() {
        federateAmbassador = new StatisticsFederateAmbassador(this);
        return federateAmbassador;
    }

    @Override
    protected void publishAndSubscribe() throws RTIexception {
        newClientsOccupyTableInteraction = new NewClientsOccupyTableInteraction(this);
        newClientsOccupyTableInteraction.subscribe();
    }

    @Override
    protected void simulationLoop() throws RTIexception {
        timeOfWaiting.setValue(0, getSimTime());
        avgTimeOfWaiting.setValue(0, getSimTime());
        Diagram d1 = new Diagram(Diagram.DiagramType.TIME, "Długość kolejki w czasie");
        d1.add(timeOfWaiting, Color.black, "Czas oczekiwania na stolik");
        d1.add(avgTimeOfWaiting, Color.red, "Średni czas oczekiwania na stolik");
        d1.show();

        while (federateAmbassador.isRunning) {
            d1.refresh();
            log("[" + getSimTime() + "] Średni czas oczekiwania na stolik: " + avgTimeOfWaiting.getValue());
            advanceTime(1);
        }
    }

    public void handle_newClientsOccupyTable(double time) throws RTIexception {
        timeOfWaiting.setValue(newClientsOccupyTableInteraction.timeOfWaiting, time);
        avgTimeOfWaiting.setValue(Statistics.arithmeticMean(timeOfWaiting), time);
    }
}