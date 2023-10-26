package Clients;

import Abstract.Event;
import Abstract.Federate;
import Abstract.FederateAmbassador;
import Interactions.ClientsExitsRestaurantInteraction;
import Interactions.ClientsImpatiented;
import Interactions.NewClientsCame;
import Interactions.NewClientsOccupyTableInteraction;
import Tables.Table;
import Tables.TablesFederate;
import hla.rti1516e.exceptions.*;

import java.util.*;

public class ClientsFederate extends Federate {
    public static void main(String[] args) throws Exception {
        new ClientsFederate().runFederate(ClientsFederate.class.getName());
    }

    ClientsFederateAmbassador federateAmbassador;
    Random random = new Random();

    NewClientsOccupyTableInteraction newClientsOccupyTableInteraction;
    ClientsExitsRestaurantInteraction clientsExitsRestaurantInteraction;
    NewClientsCame newClientsCame;
    ClientsImpatiented clientsImpatiented;

    List<GroupOfClients> clients = new ArrayList<>();
    Queue<GroupOfClients> clientsQueue = new LinkedList<>();
    double nextTimeForClientsToSpawn;
    int nextNumberOfClients;
    public List<Table> tables = new ArrayList<>();
    PriorityQueue<Event<GroupOfClients>> simCalendar = new PriorityQueue<>(Comparator.comparingDouble(Event::getRunTime));

    @Override
    protected FederateAmbassador createAmbassador() {
        federateAmbassador = new ClientsFederateAmbassador(this);
        return federateAmbassador;
    }

    @Override
    protected void publishAndSubscribe() throws RTIexception {
        newClientsOccupyTableInteraction = new NewClientsOccupyTableInteraction(this);
        newClientsOccupyTableInteraction.publish();

        clientsExitsRestaurantInteraction = new ClientsExitsRestaurantInteraction(this);
        clientsExitsRestaurantInteraction.subscribe();

        newClientsCame = new NewClientsCame(this);
        newClientsCame.publish();

        clientsImpatiented = new ClientsImpatiented(this);
        clientsImpatiented.publish();
    }

    @Override
    protected void simulationLoop() throws RTIexception {
        createTables();

        // zakolejkowanie pierwszego pojawienia się klientów
        nextTimeForClientsToSpawn = (1 + (5 - 1) * random.nextDouble()); // [1, 5]
        nextNumberOfClients = random.nextInt(5) + 1;

        simCalendar.add(new Event<>(Event.Type.generateClients, getSimTime() + nextTimeForClientsToSpawn, null));


        while (federateAmbassador.isRunning) {
            // krokowa obsługa zakolejkowanych zdarzeń
            while(!simCalendar.isEmpty() && simCalendar.peek().runTime < getSimTime())
            {
                Event<GroupOfClients> event = simCalendar.poll();
                if (event.type == Event.Type.letClientsIn) {
                    tryToLetClientsIn(getSimTime());
                    continue;
                }
                if (event.type == Event.Type.generateClients) {
                    createNewClients();
                }
                if (event.type == Event.Type.clientImpatienty) {
                    removeImpatientGroups(event.obj);
                }
            }

            advanceTime(0.1);
        }
    }

    private void removeImpatientGroups(GroupOfClients clientsGroup) throws RTIexception {
        if (clientsGroup.isImpatient) {
            clientsImpatiented.clientId = clientsGroup.id;
            clientsImpatiented.send();

            clients.removeIf(x -> x.id == clientsGroup.id);
            clientsQueue.removeIf(x -> x.id == clientsGroup.id);
            log("[" + getSimTime() + "] grupa klientów o id " + clientsGroup.id + " wyszła z kolejki z powodu zniecierpliwienia");
        }
    }

    private void tryToLetClientsIn(double time) throws RTIexception {
        Table freeTable = getAnyFreeTable();
        if (freeTable != null && !clientsQueue.isEmpty()) {
            GroupOfClients groupOfClients = clientsQueue.poll();
            groupOfClients.isImpatient = false; // kliencie w restauracji nie są niecierpliwi / wyłączenie eventu niecierpliwości
            freeTable.clientsGroup = groupOfClients;
            newClientsOccupyTableInteraction.clientId = groupOfClients.id;
            newClientsOccupyTableInteraction.tableId = freeTable.id;
            newClientsOccupyTableInteraction.numberOfPeopleInGroup = groupOfClients.numberOfPeopleInGroup;
            newClientsOccupyTableInteraction.timeOfWaiting = time - groupOfClients.creationTime;
            newClientsOccupyTableInteraction.send();
            log("[" + time + "] grupa klientów o id " + groupOfClients.id + " została wpuszczona do restauracji i usiadała przy stoliku nr " + freeTable.id);
            tryToLetClientsIn(time);
        }
    }

    private void createNewClients() throws RTIexception {
        GroupOfClients groupOfClients;
        if (random.nextDouble() < 0.2) { // 20% szans na niecierpliwą grupę klientów
            double timeOfImpatienty = 4 + (10 - 4) * random.nextDouble(); // [4, 10] od 4 do 10 min czekania
            groupOfClients = new GroupOfClients(this, getSimTime(), nextNumberOfClients, true, getSimTime() + timeOfImpatienty);
            simCalendar.add(new Event<>(Event.Type.clientImpatienty, groupOfClients.impatientTime, groupOfClients)); // dodanie zdarzenia wyjścia z kolejki
            log("[" + getSimTime() + "] grupa klientów o id " + groupOfClients.id + "(zniecierpliwiona)(" + groupOfClients.impatientTime + ") właśnie przyszła do kolejki o długości " + clientsQueue.size());

        } else {
            groupOfClients = new GroupOfClients(this, getSimTime(), nextNumberOfClients);
            log("[" + getSimTime() + "] grupa klientów o id " + groupOfClients.id + " właśnie przyszła do kolejki o długości " + clientsQueue.size());

        }
        clients.add(groupOfClients);
        clientsQueue.add(groupOfClients);

        newClientsCame.clientId = groupOfClients.id;
        newClientsCame.numberOfPeopleInGroup = groupOfClients.numberOfPeopleInGroup;
        newClientsCame.send();

        // spróbuj wpuścić klientów
        tryToLetClientsIn(getSimTime());

        // wygenerowanie następnych parametrów dla funkcji
        nextTimeForClientsToSpawn = (1 + (2 - 1) * random.nextDouble()); // [1, 5]
        nextNumberOfClients = random.nextInt(5) + 1;
        simCalendar.add(new Event<>(Event.Type.generateClients, getSimTime() + nextTimeForClientsToSpawn, null));
    }

    private Table getAnyFreeTable() {
        for (Table table : tables) {
            if (table.clientsGroup == null) {
                return table;
            }
        }
        return null;
    }

    private void createTables() {
        for (int i = 0; i < TablesFederate.tablesCount; i++) {
            tables.add(new Table());
        }
    }

    public void handle_clientsExitsRestaurant(double time) throws RTIexception {
        Table table = tables.stream().filter(x -> x.id == clientsExitsRestaurantInteraction.tableId).findFirst().get();
        log("[" + time + "] grupa klientów o id " + table.clientsGroup.id + " zjadła posiłek przy stoliku nr " + table.id + " i wychodzi z restauracji");
        table.clientsGroup = null;

        clients.removeIf(x -> x.id == clientsExitsRestaurantInteraction.clientId);
        simCalendar.add(new Event<>(Event.Type.letClientsIn, time, clientsQueue.peek()));
    }
}