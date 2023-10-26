package Tables;

import Abstract.Event;
import Abstract.Federate;
import Abstract.FederateAmbassador;
import Clients.GroupOfClients;
import Interactions.CallWaiterInteraction;
import Interactions.ClientsExitsRestaurantInteraction;
import Interactions.NewClientsOccupyTableInteraction;
import Interactions.TableServedInteraction;
import hla.rti1516e.exceptions.RTIexception;

import java.util.*;

public class TablesFederate extends Federate {
    public static void main(String[] args) throws Exception {
        new TablesFederate().runFederate(TablesFederate.class.getName());
    }

    TablesFederateAmbassador tablesFederateAmbassador;
    Random random = new Random();

    NewClientsOccupyTableInteraction newClientsOccupyTableInteraction;
    ClientsExitsRestaurantInteraction clientsExitsRestaurantInteraction;
    TableServedInteraction tableServedInteraction;
    CallWaiterInteraction callWaiterInteraction;

    public static int tablesCount = 10;
    public List<Table> tables = new ArrayList<>();
    PriorityQueue<Event<Table>> simCalendar = new PriorityQueue<>(Comparator.comparingDouble(Event::getRunTime));


    @Override
    protected FederateAmbassador createAmbassador() {
        tablesFederateAmbassador = new TablesFederateAmbassador(this);
        return tablesFederateAmbassador;
    }

    @Override
    protected void publishAndSubscribe() throws RTIexception {
        newClientsOccupyTableInteraction = new NewClientsOccupyTableInteraction(this);
        newClientsOccupyTableInteraction.subscribe();

        clientsExitsRestaurantInteraction = new ClientsExitsRestaurantInteraction(this);
        clientsExitsRestaurantInteraction.publish();

        tableServedInteraction = new TableServedInteraction(this);
        tableServedInteraction.subscribe();

        callWaiterInteraction = new CallWaiterInteraction(this);
        callWaiterInteraction.publish();
    }

    @Override
    protected void simulationLoop() throws RTIexception {
        createTables();

        while (federateAmbassador.isRunning) {
            // krokowa obsługa zakolejkowanych zdarzeń
            while(!simCalendar.isEmpty() && simCalendar.peek().runTime < getSimTime())
            {
                Event<Table> event = simCalendar.poll();
                // koniec jedzenia przy tym stoliku
                if (event.type == Event.Type.endOfEating) {

                    // prawdopodobieństwo zamówienia jeszcze jednego dania [20%]
                    if(random.nextDouble() < 0.2) {
                        callWaiterInteraction.tableId = event.obj.id;
                        callWaiterInteraction.send();
                        log("[" + getSimTime() + "] grupa klientów o id " + event.obj.clientsGroup.id + " przy stoliku nr " + event.obj.id + " chce zamówić jeszcze raz");
                    }
                    else // wyjście z restauracji
                    {
                        clientsExitsRestaurantInteraction.clientId = event.obj.clientsGroup.id;
                        clientsExitsRestaurantInteraction.tableId = event.obj.id;
                        clientsExitsRestaurantInteraction.send();
                        log("[" + getSimTime() + "] grupa klientów o id " + event.obj.clientsGroup.id + " przy stoliku nr " + event.obj.id + " skończyła jeść i wychodzi");
                        event.obj.clientsGroup = null;
                    }
                }
            }

            advanceTime(0.1);
        }
    }

    private void createTables() {
        for (int i = 0; i < tablesCount; i++) {
            tables.add(new Table());
        }
    }

    /**
     * Interakcja która usadza klientów przy stolikach
     */
    public void handle_newClientsOccupyTable(double time) throws RTIexception {
        Table table = tables.stream().filter(x -> x.id == newClientsOccupyTableInteraction.tableId).findFirst().get();
        table.clientsGroup = new GroupOfClients(newClientsOccupyTableInteraction.clientId, newClientsOccupyTableInteraction.numberOfPeopleInGroup);
        log("[" + time + "] grupa klientów o id " + table.clientsGroup.id + " właśnie siadła przy stoliku nr " + table.id + " oraz woła kelnera");
    }

    /**
     * Interakcja która rozpoczyna jedzenie przez klientów
     */
    public void handle_tableServedInteraction(double time) {
        Table table = tables.stream().filter(x -> x.id == tableServedInteraction.tableId).findFirst().get();
        double timeOfEating = 5 + (15 - 5) * random.nextDouble(); // [5, 15]
        simCalendar.add(new Event(Event.Type.endOfEating, timeOfEating + time, table));
        log("[" + time + "] grupa klientów o id " + table.clientsGroup.id + " przy stoliku nr " + table.id + " została obsłużona i będzie jeść do " + (time + timeOfEating));
    }
}