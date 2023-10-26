package Clients;

import GUI.Restaurant;

import java.awt.*;
import java.awt.image.BufferedImage;

// obiekt klienta to tak naprawę grupa klientów
public class GroupOfClients {
    private static int nextId = 0;
    ClientsFederate clientsFederate;
    public int id;
    public boolean isImpatient;
    public double impatientTime;
    public int numberOfPeopleInGroup;
    public double creationTime;

    public GroupOfClients(int id, int numberOfPeopleInGroup) {
        this.id = id;
        this.numberOfPeopleInGroup = numberOfPeopleInGroup;
    }

    public GroupOfClients(ClientsFederate clientsFederate, double creationTime, int numberOfPeopleInGroup) {
        this.clientsFederate = clientsFederate;
        this.creationTime = creationTime;
        this.numberOfPeopleInGroup = numberOfPeopleInGroup;

        this.id = nextId++;
        this.isImpatient = false;
    }

    public GroupOfClients(ClientsFederate clientsFederate, double creationTime, int numberOfPeopleInGroup, boolean isImpatient, double impatientTime) {
        this.clientsFederate = clientsFederate;
        this.creationTime = creationTime;
        this.numberOfPeopleInGroup = numberOfPeopleInGroup;

        this.id = nextId++;
        this.isImpatient = isImpatient;
        this.impatientTime = impatientTime;
    }


    public Restaurant restaurant;

    public void paint(Graphics g, int x, int y) {
        BufferedImage image = Restaurant.client;
        g.drawImage(image, x, y, null);
        g.setColor(Color.BLACK);
        drawCenteredString(g, String.valueOf(id), new Rectangle(x, y, image.getWidth(), image.getHeight()), new Font("Consolas", Font.BOLD, 30));
    }

    public static void drawCenteredString(Graphics g, String text, Rectangle rect, Font font) {
        FontMetrics metrics = g.getFontMetrics(font);
        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        g.setFont(font);
        g.drawString(text, x, y);
    }
}
