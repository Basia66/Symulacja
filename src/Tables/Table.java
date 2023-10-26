package Tables;

import Clients.GroupOfClients;
import Waiters.Waiter;


public class Table {
    private static int nextId = 0;
    public int id;
    public boolean needToBeServed;
    public double eatingTime;
    public GroupOfClients clientsGroup;
    public Waiter servingWaiter;

    public Table() {
        this.id = nextId++;
    }


    public int x;
    public int y;
    public int width;
    public int height;
}
