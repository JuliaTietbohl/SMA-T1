import java.util.ArrayList;
import java.util.List;

public class GeradorLgc {

    private long x;
    private final long a = 25214903917L;
    private final long c = 11L;
    private final long mascara = (1L << 48) - 1;
    private final double m = Math.pow(2, 48);
    private int count = 0;
    private final List<Double> numerosFixos;

    public GeradorLgc(long semente) {
        this.x = semente;
        this.numerosFixos = null;
    }

    public GeradorLgc(List<Double> numerosFixos) {
        this.x = 0;
        this.numerosFixos = new ArrayList<>(numerosFixos);
    }

    /**
     * Retorna o proximo numero no intervalo [0,1) e incrementa o contador.
     */
    public double proximo() {
        if (numerosFixos != null) {
            if (count >= numerosFixos.size()) {
                throw new IllegalStateException("Acabaram os numeros aleatorios informados em rndnumbers.");
            }
            double numero = numerosFixos.get(count);
            count++;
            return numero;
        }

        x = (a * x + c) & mascara;
        count++;
        return x / m;
    }

    /**
     * Mapeia o proximo aleatorio para o intervalo [min, max].
     */
    public double intervalo(double min, double max) {
        return min + proximo() * (max - min);
    }

    public int getCount() {
        return count;
    }
}
