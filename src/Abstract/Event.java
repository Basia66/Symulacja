package Abstract;

public class Event<T> {
    public enum Type {
        endOfEating,
        endOfWaiterServing,
        generateClients,
        clientImpatienty,
        letClientsIn,
        servingBegin
    }

    public Type type;
    public double runTime;
    public T obj;

    public Event(Type type, double runTime, T el) {
        this.type = type;
        this.runTime = runTime;
        this.obj = el;
    }

    public double getRunTime() {
        return runTime;
    }
}