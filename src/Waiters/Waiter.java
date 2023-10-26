package Waiters;

import Tables.Table;

public class Waiter {
    private static int nextId = 0;
    public int id;
    public Table actualServingTable = null;
    public double servingEndTime;

    public Waiter() {
        this.id = nextId++;
    }



    public int x;
    public int y;
    public int width;
    public int height;
}
