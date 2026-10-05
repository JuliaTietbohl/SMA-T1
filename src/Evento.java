
public class Evento implements Comparable<Evento> {

    public static final int CHEGADA = 0;
    public static final int SAIDA = 1;
    public static final int PASSAGEM = 2;

    private int tipo;
    private double tempo;
    private String nomeFila;
    private String nomeDestino;

    public Evento(int tipo, double tempo, String nomeFila) {
        this(tipo, tempo, nomeFila, null);
    }

    public Evento(int tipo, double tempo, String nomeFila, String nomeDestino) {
        this.tipo = tipo;
        this.tempo = tempo;
        this.nomeFila = nomeFila;
        this.nomeDestino = nomeDestino;
    }

    public int getTipo() {
        return tipo;
    }

    public double getTempo() {
        return tempo;
    }

    public String getNomeFila() {
        return nomeFila;
    }

    public String getNomeDestino() {
        return nomeDestino;
    }

    @Override
    public int compareTo(Evento outro) {
        return Double.compare(this.tempo, outro.tempo);
    }

    @Override
    public String toString() {
        String[] nomes = {"CHEGADA", "SAIDA", "PASSAGEM"};
        return String.format("Evento[%s, t=%.4f]", nomes[tipo], tempo);
    }
}
