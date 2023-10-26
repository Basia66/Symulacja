package GUI;

import Clients.GroupOfClients;
import Tables.Table;
import Tables.TablesFederate;
import Waiters.Waiter;
import Waiters.WaitersFederate;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Hashtable;
import java.util.LinkedList;

public class Center extends JPanel implements MouseListener, ChangeListener {
    Restaurant restaurant;
//    JSlider framesPerSecond;
//    JCheckBox displayIds;
    int yOffset = 100;

    public Center(Restaurant restaurant) {
        this.restaurant = restaurant;
        this.setBackground(Color.PINK);

//        framesPerSecond = new JSlider(JSlider.HORIZONTAL, Restaurant.MIN_SPEED, Restaurant.MAX_SPEED, (int) Restaurant.simulationSleep);
//        framesPerSecond.setBackground(Color.PINK);
//
//        framesPerSecond.addChangeListener(this);
//        framesPerSecond.setMajorTickSpacing(10);
//        framesPerSecond.setPaintTicks(true);
//        framesPerSecond.setAlignmentX(50);
//        framesPerSecond.setAlignmentY(600);

//        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
//        labelTable.put(Restaurant.MIN_SPEED, new JLabel("Szybko"));
//        labelTable.put(Restaurant.MAX_SPEED, new JLabel("Wolno"));
//        framesPerSecond.setLabelTable(labelTable);
//
//        framesPerSecond.setPaintLabels(true);
//
//        displayIds = new JCheckBox("Display ID");
//        displayIds.setBounds(50, 800, 50, 50);
//        displayIds.addItemListener(e -> Restaurant.showIds = (e.getStateChange() == 1));
//
//        this.add(framesPerSecond);
//        this.add(displayIds);

        this.addMouseListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(Color.PINK);
        g.fillRect(0, 0, 1000, 1000);

        drawTables(g);
        drawWaiters(g);
        drawWallsAndDoors(g);
        drawQueue(g);
    }

    private void drawWallsAndDoors(Graphics g) {
        g.setColor(new Color(216, 216, 216));
        g.fillRect(0, 600, 900, 25);
        g.drawImage(Restaurant.doorImage, 800, 500, null);
    }

    private void drawWaiters(Graphics g) {
        g.setColor(new Color(216, 221, 216));
        for (int i = 0; i < WaitersFederate.waitersCount; i++) {
            Waiter waiter = restaurant.guiFederate.waiters.get(i);
            int x = i * 100 + i * 50;
            int y = 350;
            int width = 50;
            int height = 50;

            y+=yOffset;

            if(waiter.actualServingTable != null) {
                x = waiter.actualServingTable.x + waiter.actualServingTable.width - width/2;
                y = waiter.actualServingTable.y - height/2;
            }

            g.drawImage(Restaurant.client2, x, y, null);
            waiter.x = x;
            waiter.y = y;
            waiter.width = width;
            waiter.height = height;
            GroupOfClients.drawCenteredString(g, String.valueOf(waiter.id), new Rectangle(waiter.x, waiter.y, waiter.width,  waiter.height), new Font("Consolas", Font.BOLD, 30));
        }
    }

    private void drawTables(Graphics g) {
        g.setColor(new Color(42, 112, 3));
        int y = this.yOffset;
        for (int i = 0, j = 0; i < TablesFederate.tablesCount; i++, j++) {
            Table table = restaurant.guiFederate.tables.get(i);
            if(table.needToBeServed) {
                g.setColor(new Color(66, 245, 224));
            }
            else if(table.clientsGroup == null) {
                g.setColor(new Color(42, 112, 3));
            }
            else if(!table.needToBeServed) { // served
                g.setColor(new Color(199, 150, 14));
            }

            int x = j * 100 + j * 50;
            int maxX = 700;
            if(x > maxX)
            {
                y = (int)Math.floor(x/(double)maxX) * 100 + 30;
                y += yOffset;
                j = 0;
                x = 0;
            }

            int width = 100;
            int height = 100;

            g.fillRect(x, y, width, height);
            table.x = x;
            table.y = y;
            table.width = width;
            table.height = height;

            if(table.clientsGroup != null) {
                table.clientsGroup.paint(g, table.x+(int)(width/4), table.y+(int)(height/4));
            }
        }
    }

    private void drawQueue(Graphics g) {
        ArrayList<GroupOfClients> toDraw = new ArrayList<>(restaurant.guiFederate.clientsQueue);
        int x = 800;
        int y = 650;
        for (int i = 0; i < toDraw.size(); i++) {
            if(x < 50)
            {
                // za dużo do rysowania
                g.setColor(new Color(216, 216, 216));
                g.fillRect(x, y, 50, 50);
                g.setColor(Color.RED);
                GroupOfClients.drawCenteredString(g, String.valueOf(toDraw.size()-i), new Rectangle(x, y, 50, 50), new Font("Consolas", Font.BOLD, 30));
                break;
            }

            GroupOfClients groupOfClients = toDraw.get(i);
            groupOfClients.paint(g, x, y);
            x -= 50;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void stateChanged(ChangeEvent e) {
//        Restaurant.simulationSleep = framesPerSecond.getValue();
        restaurant.timer.cancel();
        restaurant.scheduleFrameUpdate();
    }
}
