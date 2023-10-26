package GUI;


import java.util.TimerTask;

/**
 * Klatka aplikacji(aktulanie wyświetlany obraz)
 */
public class SimulationFrame extends TimerTask {
    Restaurant restaurant;

    public SimulationFrame(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    /**
     * Metoda wywoływana co klatkę symulacji
     */
    @Override
    public void run() {
        restaurant.center.repaint();
    }
}
