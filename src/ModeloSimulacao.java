import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ModeloSimulacao {
    private final Map<String, FilaConfig> filas = new LinkedHashMap<>();
    private final Map<String, Double> chegadas = new LinkedHashMap<>();
    private final Map<String, List<Rota>> rede = new LinkedHashMap<>();
    private final List<Long> sementes = new ArrayList<>();
    private final List<Double> rndnumbers = new ArrayList<>();
    private int rndnumbersPerSeed = -1;

    public void adicionarFila(FilaConfig fila) {
        filas.put(fila.nome, fila);
    }

    public void adicionarChegada(String nomeFila, double tempo) {
        chegadas.put(nomeFila, tempo);
    }

    public void adicionarRota(Rota rota) {
        List<Rota> rotas = rede.get(rota.source);
        if (rotas == null) {
            rotas = new ArrayList<>();
            rede.put(rota.source, rotas);
        }
        rotas.add(rota);
        Collections.sort(rotas, (a, b) -> Double.compare(a.probability, b.probability));
    }

    public void adicionarSemente(long semente) {
        sementes.add(semente);
    }

    public void adicionarRndnumber(double numero) {
        rndnumbers.add(numero);
    }

    public Map<String, FilaConfig> getFilas() {
        return Collections.unmodifiableMap(filas);
    }

    public Map<String, Double> getChegadas() {
        return Collections.unmodifiableMap(chegadas);
    }

    public boolean temChegadaExterna(String nomeFila) {
        return chegadas.containsKey(nomeFila);
    }

    public List<Rota> getRotas(String origem) {
        List<Rota> rotas = rede.get(origem);
        if (rotas == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(rotas);
    }

    public Set<String> getOrigensRede() {
        return Collections.unmodifiableSet(rede.keySet());
    }

    public List<Long> getSementes() {
        return Collections.unmodifiableList(sementes);
    }

    public boolean usaSementes() {
        return !sementes.isEmpty();
    }

    public List<Double> getRndnumbers() {
        return Collections.unmodifiableList(rndnumbers);
    }

    public int getRndnumbersPerSeed() {
        return rndnumbersPerSeed;
    }

    public void setRndnumbersPerSeed(int rndnumbersPerSeed) {
        this.rndnumbersPerSeed = rndnumbersPerSeed;
    }

    public static class FilaConfig {
        public final String nome;
        public final int servers;
        public final int capacity;
        public final double minArrival;
        public final double maxArrival;
        public final double minService;
        public final double maxService;

        public FilaConfig(String nome, int servers, int capacity,
                          double minArrival, double maxArrival,
                          double minService, double maxService) {
            this.nome = nome;
            this.servers = servers;
            this.capacity = capacity;
            this.minArrival = minArrival;
            this.maxArrival = maxArrival;
            this.minService = minService;
            this.maxService = maxService;
        }
    }

    public static class Rota {
        public final String source;
        public final String target;
        public final double probability;

        public Rota(String source, String target, double probability) {
            this.source = source;
            this.target = target;
            this.probability = probability;
        }
    }
}
