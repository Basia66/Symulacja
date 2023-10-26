package GUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import java.util.Timer;

public class Restaurant extends JFrame {
    public static final int MIN_SPEED = 0;
    public static final int MAX_SPEED = 1000;
    public static boolean showIds = false;
    public static int frameDelay = 33;
    public static double speed = 10f;

    public static int simulationSleep = 0;

    // obrazki
    public static BufferedImage doorImage = loadImage("door.png");
    public static BufferedImage client = loadImage("client.png");
    public static BufferedImage client1 = loadImage("client1.png");
    public static BufferedImage client2 = loadImage("client2.png");

    public final Center center;
    public final GUIFederate guiFederate;

    Timer timer;

    public Restaurant(GUIFederate guiFederate) {
        this.guiFederate = guiFederate;
        guiFederate.restaurant = this;
        setTitle("Restauracja");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(900, 800));
        setMinimumSize(new Dimension(900, 800));
        setResizable(false);

        BorderLayout borderLayout = new BorderLayout();
        center = new Center(this);
        center.setPreferredSize(new Dimension(900, 500));

        borderLayout.preferredLayoutSize(this);
        setLayout(borderLayout);
        add(center);

        borderLayout.addLayoutComponent(center, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        scheduleFrameUpdate();
    }

    public void scheduleFrameUpdate() {
        if(speed == 0)
            return;

        timer = new Timer();
        timer.schedule(new SimulationFrame(this), 0, (long) (frameDelay / speed));
    }

    public static BufferedImage loadImage(String fileName) {
        BufferedImage img = null;

        try {
            img = ImageIO.read(Objects.requireNonNull(Restaurant.class.getResourceAsStream("/images/" + fileName)));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return img;
    }
}
