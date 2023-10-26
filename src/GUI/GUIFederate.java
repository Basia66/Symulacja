package GUI;

import Abstract.Federate;
import Abstract.FederateAmbassador;
import Clients.GroupOfClients;
import Interactions.*;
import Tables.Table;
import Tables.TablesFederate;
import Waiters.Waiter;
import Waiters.WaitersFederate;
import hla.rti1516e.exceptions.RTIexception;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class GUIFederate extends Federate {
    public static void main(String[] args) throws Exception {
        new GUIFederate().runFederate(GUIFederate.class.getName());
//        GUIFederate guiFederate = new GUIFederate();
//        guiFederate.test();
//        new Restaurant(guiFederate);

    }

    GUIFederateAmbassador guiFederateAmbassador;

    NewClientsOccupyTableInteraction newClientsOccupyTableInteraction;
    ClientsExitsRestaurantInteraction clientsExitsRestaurantInteraction;
    TableServedInteraction tableServedInteraction;
    CallWaiterInteraction callWaiterInteraction;
    NewClientsCame newClientsCame;
    ClientsImpatiented clientsImpatiented;
    WaiterServingTable waiterServingTable;

    public Restaurant restaurant;
    List<GroupOfClients> clients = new ArrayList<>();
    Queue<GroupOfClients> clientsQueue = new LinkedList<>();
    List<Table> tables = new ArrayList<>();
    List<Waiter> waiters = new ArrayList<>();

    @Override
    protected FederateAmbassador createAmbassador() {
        guiFederateAmbassador = new GUIFederateAmbassador(this);
        return guiFederateAmbassador;
    }

    @Override
    protected void simulationLoop() throws RTIexception {
        createTables();
        createWaiters();
        restaurant = new Restaurant(this);
        while (federateAmbassador.isRunning) {
            advanceTime(1);
        }
    }

    @Override
    protected void publishAndSubscribe() throws RTIexception {
        newClientsOccupyTableInteraction = new NewClientsOccupyTableInteraction(this);
        newClientsOccupyTableInteraction.subscribe();

        clientsExitsRestaurantInteraction = new ClientsExitsRestaurantInteraction(this);
        clientsExitsRestaurantInteraction.subscribe();

        tableServedInteraction = new TableServedInteraction(this);
        tableServedInteraction.subscribe();

        callWaiterInteraction = new CallWaiterInteraction(this);
        callWaiterInteraction.subscribe();

        newClientsCame = new NewClientsCame(this);
        newClientsCame.subscribe();

        clientsImpatiented = new ClientsImpatiented(this);
        clientsImpatiented.subscribe();

        waiterServingTable = new WaiterServingTable(this);
        waiterServingTable.subscribe();
    }

    public void test() {
        createTables();
        createWaiters();
        for (int i = 0; i < 15; i++) {
            GroupOfClients groupOfClients = new GroupOfClients(1, 10);
            clientsQueue.add(groupOfClients);
            clients.add(groupOfClients);
        }

        Table table = tables.stream().filter(x -> x.id == 2).findFirst().get();
        table.clientsGroup = new GroupOfClients(3, 7);
        table.needToBeServed = true;

        table = tables.stream().filter(x -> x.id == 6).findFirst().get();
        table.clientsGroup = new GroupOfClients(2, 5);
        table.needToBeServed = true;

        table = tables.stream().filter(x -> x.id == 7).findFirst().get();
        table.clientsGroup = new GroupOfClients(6, 423);
        table.needToBeServed = false;

        Waiter waiter = waiters.stream().filter(x -> x.id == 0).findFirst().get();
        waiter.actualServingTable = table;


        // TODO
//        Table table = tables.stream().filter(x -> x.id == tableId).findFirst().get();
//        table.clientsGroup = new GroupOfClients(clientId, 5);
//        clients.get(clientId).cashRegisterId = tableId;
//        Shelf shelf = shelves.stream().filter(x -> x.id == tableId).findFirst().get();
//        for (int i = 0; i < shelf.clients.size(); i++) {
//            if (shelf.clients.get(i) == clientId) {
//                shelf.clients.remove(i);
//            }
//        }
    }

    private void createTables() {
        for (int i = 0; i < TablesFederate.tablesCount; i++) {
            tables.add(new Table());
        }
    }

    private void createWaiters() {
        for (int i = 0; i < WaitersFederate.waitersCount; i++) {
            waiters.add(new Waiter());
        }
    }

    public void handle_newClientsOccupyTable(double value) {
        Table table = tables.stream().filter(x -> x.id == newClientsOccupyTableInteraction.tableId).findFirst().get();
        table.clientsGroup = new GroupOfClients(newClientsOccupyTableInteraction.clientId, newClientsOccupyTableInteraction.numberOfPeopleInGroup);
        table.needToBeServed = true;
        clientsQueue.removeIf(x -> x.id == newClientsOccupyTableInteraction.clientId);
    }

    public void handle_tableServedInteraction(double value) {
        Table table = tables.stream().filter(x -> x.id == tableServedInteraction.tableId).findFirst().get();
        table.needToBeServed = false;
        table.servingWaiter.actualServingTable = null;
        table.servingWaiter = null;

        // clear na wszelki
        clientsQueue.removeIf(x -> x.id == table.clientsGroup.id);
    }

    public void handle_clientsExitsRestaurant(double value) {
        Table table = tables.stream().filter(x -> x.id == clientsExitsRestaurantInteraction.tableId).findFirst().get();
        table.clientsGroup = null;
        clients.removeIf(x -> x.id == clientsExitsRestaurantInteraction.clientId);

        // clear na wszelki
        clientsQueue.removeIf(x -> x.id == clientsExitsRestaurantInteraction.clientId);
    }

    public void handle_callWaiterInteraction(double value) {
//        Table table = tables.stream().filter(x -> x.id == callWaiterInteraction.tableId).findFirst().get();
//        table.needToBeServed = true;
    }

    public void handle_newClientsCame(double value) {
        GroupOfClients groupOfClients = new GroupOfClients(newClientsCame.clientId, newClientsCame.numberOfPeopleInGroup);
        clients.add(groupOfClients);
        clientsQueue.add(groupOfClients);
    }

    public void handle_clientsImpatiented(double value) {
        clients.removeIf(x -> x.id == clientsImpatiented.clientId);
        clientsQueue.removeIf(x -> x.id == clientsImpatiented.clientId);
    }

    public void handle_waiterServingTable(double value) {
        Waiter waiter = waiters.stream().filter(x -> x.id == waiterServingTable.waiterId).findFirst().get();
        Table table = tables.stream().filter(x -> x.id == waiterServingTable.tableId).findFirst().get();
        waiter.actualServingTable = table;
        table.servingWaiter = waiter;

        // clear na wszelki
        clientsQueue.removeIf(x -> x.id == table.clientsGroup.id);
    }
}