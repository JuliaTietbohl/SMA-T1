import java.util.HashMap;
import java.util.Map;

public class Fila {

    private int servers;
    private int capacity;
    private double minArrival;
    private double maxArrival;
    private double minService;
    private double maxService;
    private int customers;
    private int loss;
    private Map<Integer, Double> timesMap = new HashMap<>();
    private final String nomeFila;

    public Fila(String nomeFila, int servers, int capacity,
                double minArrival, double maxArrival,
                double minService, double maxService) {
        this.servers = servers;
        this.capacity = capacity;
        this.minArrival = minArrival;
        this.maxArrival = maxArrival;
        this.minService = minService;
        this.maxService = maxService;
        this.customers = 0;
        this.loss = 0;
        this.nomeFila = nomeFila;
    }

    public String getNomeFila() {
        return nomeFila;
    }

    public int status() {
        return customers;
    }

    public int capacity() {
        return capacity;
    }

    public int servers() {
        return servers;
    }

    public int loss() {
        return loss;
    }

    public double minArrival() {
        return minArrival;
    }

    public double maxArrival() {
        return maxArrival;
    }

    public double minService() {
        return minService;
    }

    public double maxService() {
        return maxService;
    }

    public void contaPerda() {
        loss++;
    }

    public void in() {
        customers++;
    }

    public void out() {
        customers--;
    }

    public Map<Integer, Double> getTimesMap() {
        return timesMap;
    }

    public void acumulaTempo(double delta) {
        double tempoAcumulado = timesMap.getOrDefault(customers, 0.0);
        timesMap.put(customers, tempoAcumulado + delta);
    }
}
