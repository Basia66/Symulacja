package Waiters;

import Abstract.Event;
import Abstract.Federate;
import Abstract.FederateAmbassador;
import Clients.GroupOfClients;
import Interactions.*;
import Tables.Table;
import Tables.TablesFederate;
import hla.rti1516e.*;
import hla.rti1516e.exceptions.*;

import java.util.*;

public class WaitersFederate extends Federate {
    public static void main(String[] args) throws Exception {
        new WaitersFederate().runFederate(WaitersFederate.class.getName());
    }

    public static int waitersCount = 2;

    WaitersFederateAmbassador waitersFederateAmbassador;
    Random random = new Random();

    NewClientsOccupyTableInteraction newClientsOccupyTableInteraction;
    TableServedInteraction tableServedInteraction;
    ClientsExitsRestaurantInteraction clientsExitsRestaurantInteraction;
    CallWaiterInteraction callWaiterInteraction;
    WaiterServingTable waiterServingTable;

    public List<Table> tables = new ArrayList<>();
    public List<Waiter> waiters = new ArrayList<>();
    PriorityQueue<Event<Waiter>> simCalendar = new PriorityQueue<>(Comparator.comparingDouble(Event::getRunTime));


    @Override
    protected FederateAmbassador createAmbassador() {
        waitersFederateAmbassador = new WaitersFederateAmbassador(this);
        return waitersFederateAmbassador;
    }

    @Override
    protected void simulationLoop() throws RTIexception {
        createTables();
        createWaiters();

        while (federateAmbassador.isRunning) {
            // krokowa obsługa zakolejkowanych zdarzeń
            while(!simCalendar.isEmpty() && simCalendar.peek().runTime < getSimTime())
            {
                Event<Waiter> event = simCalendar.poll();
                if (event.type == Event.Type.endOfWaiterServing) {
                    handleEndOfServing(event.obj);
                }
                if (event.type == Event.Type.servingBegin) {
                    waiterServingTable.waiterId = event.obj.id;
                    waiterServingTable.tableId = event.obj.actualServingTable.id;
                    waiterServingTable.send();
                }
            }

            advanceTime(0.1);
        }
    }

    private void handleEndOfServing(Waiter waiter) throws RTIexception {
        tableServedInteraction.tableId = waiter.actualServingTable.id;
        tableServedInteraction.send();
        log("[" + getSimTime() + "] grupa klientów o id " + waiter.actualServingTable.clientsGroup.id + " przy stoliku nr " + waiter.actualServingTable.id + " została obsłużona i zaraz zaczną jeść");

        waiter.actualServingTable.needToBeServed = false;
        waiter.actualServingTable = null;

        // jeśli jakikolwiek stolik potrzebuje obsługi
        for (Table table : tables) {
            if(table.needToBeServed) {
                tryToServeTable(table, getSimTime());
            }
        }
    }

    @Override
    protected void publishAndSubscribe() throws RTIexception {
        newClientsOccupyTableInteraction = new NewClientsOccupyTableInteraction(this);
        newClientsOccupyTableInteraction.subscribe();

        tableServedInteraction = new TableServedInteraction(this);
        tableServedInteraction.publish();

        clientsExitsRestaurantInteraction = new ClientsExitsRestaurantInteraction(this);
        clientsExitsRestaurantInteraction.subscribe();

        callWaiterInteraction = new CallWaiterInteraction(this);
        callWaiterInteraction.subscribe();

        waiterServingTable = new WaiterServingTable(this);
        waiterServingTable.publish();
    }

    public void handle_newClientsOccupyTable(double time) throws RTIexception {
        Table table = tables.stream().filter(x -> x.id == newClientsOccupyTableInteraction.tableId).findFirst().get();
        table.clientsGroup = new GroupOfClients(newClientsOccupyTableInteraction.clientId, newClientsOccupyTableInteraction.numberOfPeopleInGroup);
        table.needToBeServed = true;
        log("[" + time + "] grupa klientów o id " + table.clientsGroup.id + " właśnie siadła przy stoliku nr " + table.id + " oraz woła kelnera");
        tryToServeTable(table, time);
    }

    public void handle_callWaiterInteraction(double time) throws RTIexception {
        Table table = tables.stream().filter(x -> x.id == callWaiterInteraction.tableId).findFirst().get();
        table.needToBeServed = true;
        log("[" + time + "] grupa klientów o id " + table.clientsGroup.id + " przy stoliku nr " + table.id + " zawołała kelnera");
        tryToServeTable(table, time);
    }

    public void handle_clientsExitsRestaurant(double time) {
        Table table = tables.stream().filter(x -> x.id == clientsExitsRestaurantInteraction.tableId).findFirst().get();
        log("[" + time + "] grupa klientów o id " + table.clientsGroup.id + " zjadła posiłek przy stoliku nr " + table.id + " i wychodzi z restauracji");
        table.clientsGroup = null;
        table.needToBeServed = false;
    }

    private void tryToServeTable(Table table, double time) throws RTIexception {
        Waiter freeWaiter = getFreeWaiter();
        if (freeWaiter != null) {
            freeWaiter.actualServingTable = table;
            double randomTime = 1 + (3 - 1) * random.nextDouble(); // [1, 3]
            // random time * liczba osób <- ile będzie zajmowało przyjęcie zamówienia
            freeWaiter.servingEndTime = time + (randomTime * table.clientsGroup.numberOfPeopleInGroup);
            table.needToBeServed = false;
            simCalendar.add(new Event<>(Event.Type.endOfWaiterServing, freeWaiter.servingEndTime, freeWaiter));
            log("[" + time + "] Kelner o id " + freeWaiter.id + " będzie obsługiwał stolik o id " + table.id + " do czasu " + freeWaiter.servingEndTime);
            simCalendar.add(new Event<>(Event.Type.servingBegin, time, freeWaiter));
        }
        else
        {
            log("[" + time + "] Brak wolnego kelnera");
        }
    }

    private void createTables() {
        for (int i = 0; i < TablesFederate.tablesCount; i++) {
            tables.add(new Table());
        }
    }

    private void createWaiters() {
        for (int i = 0; i < waitersCount; i++) {
            waiters.add(new Waiter());
        }
    }

    private Waiter getFreeWaiter() {
        for (Waiter waiter : waiters) {
            if (waiter.actualServingTable == null)
                return waiter;
        }
        return null;
    }
}