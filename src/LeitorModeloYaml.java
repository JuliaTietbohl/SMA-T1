import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeitorModeloYaml {
    private static final String ARRIVALS = "arrivals";
    private static final String QUEUES = "queues";
    private static final String NETWORK = "network";
    private static final String SEEDS = "seeds";
    private static final String RNDNUMBERS = "rndnumbers";

    public static ModeloSimulacao ler(String caminho) {
        List<String> linhas;
        try {
            linhas = Files.readAllLines(Paths.get(caminho), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("Nao foi possivel ler o arquivo: " + caminho, e);
        }

        ModeloSimulacao modelo = new ModeloSimulacao();
        String secao = "";
        String filaAtual = "";
        Map<String, String> dadosFila = new HashMap<>();
        RotaBuilder rotaAtual = null;

        for (String linhaOriginal : linhas) {
            String linha = removerComentario(linhaOriginal);
            if (linha.trim().isEmpty() || linha.trim().startsWith("!")) {
                continue;
            }

            int indentacao = contarEspacosIniciais(linha);
            String texto = linha.trim();

            if (texto.startsWith("-") && NETWORK.equals(secao)) {
                if (rotaAtual != null) {
                    adicionarRota(modelo, rotaAtual);
                }
                rotaAtual = new RotaBuilder();
                String resto = texto.substring(1).trim();
                if (!resto.isEmpty()) {
                    preencherRota(rotaAtual, resto);
                }
                continue;
            }

            if (texto.startsWith("-") && SEEDS.equals(secao)) {
                modelo.adicionarSemente(parseLong(texto.substring(1).trim(), "seeds"));
                continue;
            }

            if (texto.startsWith("-") && RNDNUMBERS.equals(secao)) {
                modelo.adicionarRndnumber(parseDouble(texto.substring(1).trim(), "rndnumbers"));
                continue;
            }

            if (indentacao == 0) {
                if (QUEUES.equals(secao) && !filaAtual.isEmpty()) {
                    adicionarFila(modelo, filaAtual, dadosFila);
                    dadosFila.clear();
                    filaAtual = "";
                }
                if (NETWORK.equals(secao) && rotaAtual != null) {
                    adicionarRota(modelo, rotaAtual);
                    rotaAtual = null;
                }

                Par chaveValor = separarChaveValor(texto);
                if (chaveValor == null) {
                    continue;
                }

                if (ARRIVALS.equals(chaveValor.chave)
                        || QUEUES.equals(chaveValor.chave)
                        || NETWORK.equals(chaveValor.chave)
                        || SEEDS.equals(chaveValor.chave)
                        || RNDNUMBERS.equals(chaveValor.chave)) {
                    secao = chaveValor.chave;
                    continue;
                }

                if ("rndnumbersPerSeed".equals(chaveValor.chave)) {
                    modelo.setRndnumbersPerSeed(parseInt(chaveValor.valor, "rndnumbersPerSeed"));
                }
                continue;
            }

            if (ARRIVALS.equals(secao)) {
                Par chaveValor = separarChaveValor(texto);
                if (chaveValor != null) {
                    modelo.adicionarChegada(chaveValor.chave, parseDouble(chaveValor.valor, chaveValor.chave));
                }
            } else if (QUEUES.equals(secao)) {
                Par chaveValor = separarChaveValor(texto);
                if (chaveValor == null) {
                    continue;
                }

                if (chaveValor.valor.isEmpty()) {
                    if (!filaAtual.isEmpty()) {
                        adicionarFila(modelo, filaAtual, dadosFila);
                        dadosFila.clear();
                    }
                    filaAtual = chaveValor.chave;
                } else {
                    dadosFila.put(chaveValor.chave, chaveValor.valor);
                }
            } else if (NETWORK.equals(secao)) {
                if (texto.startsWith("-")) {
                    if (rotaAtual != null) {
                        adicionarRota(modelo, rotaAtual);
                    }
                    rotaAtual = new RotaBuilder();
                    String resto = texto.substring(1).trim();
                    if (!resto.isEmpty()) {
                        preencherRota(rotaAtual, resto);
                    }
                } else if (rotaAtual != null) {
                    preencherRota(rotaAtual, texto);
                }
            } else if (SEEDS.equals(secao)) {
                if (texto.startsWith("-")) {
                    modelo.adicionarSemente(parseLong(texto.substring(1).trim(), "seeds"));
                }
            } else if (RNDNUMBERS.equals(secao)) {
                if (texto.startsWith("-")) {
                    modelo.adicionarRndnumber(parseDouble(texto.substring(1).trim(), "rndnumbers"));
                }
            }
        }

        if (QUEUES.equals(secao) && !filaAtual.isEmpty()) {
            adicionarFila(modelo, filaAtual, dadosFila);
        }
        if (NETWORK.equals(secao) && rotaAtual != null) {
            adicionarRota(modelo, rotaAtual);
        }

        validar(modelo);
        return modelo;
    }

    private static void adicionarFila(ModeloSimulacao modelo, String nome, Map<String, String> dados) {
        int servers = parseInt(obrigatorio(dados, "servers", nome), nome + ".servers");
        int capacity = dados.containsKey("capacity")
                ? parseInt(dados.get("capacity"), nome + ".capacity")
                : Integer.MAX_VALUE;
        double minArrival = dados.containsKey("minArrival")
                ? parseDouble(dados.get("minArrival"), nome + ".minArrival")
                : 0.0;
        double maxArrival = dados.containsKey("maxArrival")
                ? parseDouble(dados.get("maxArrival"), nome + ".maxArrival")
                : 0.0;
        double minService = parseDouble(obrigatorio(dados, "minService", nome), nome + ".minService");
        double maxService = parseDouble(obrigatorio(dados, "maxService", nome), nome + ".maxService");

        modelo.adicionarFila(new ModeloSimulacao.FilaConfig(
                nome, servers, capacity, minArrival, maxArrival, minService, maxService));
    }

    private static void adicionarRota(ModeloSimulacao modelo, RotaBuilder rota) {
        if (rota.source == null || rota.target == null || rota.probability == null) {
            throw new IllegalArgumentException("Rota incompleta na secao network.");
        }
        modelo.adicionarRota(new ModeloSimulacao.Rota(rota.source, rota.target, rota.probability));
    }

    private static void preencherRota(RotaBuilder rota, String texto) {
        Par chaveValor = separarChaveValor(texto);
        if (chaveValor == null) {
            return;
        }

        if ("source".equals(chaveValor.chave)) {
            rota.source = chaveValor.valor;
        } else if ("target".equals(chaveValor.chave)) {
            rota.target = chaveValor.valor;
        } else if ("probability".equals(chaveValor.chave)) {
            rota.probability = parseDouble(chaveValor.valor, "network.probability");
        }
    }

    private static void validar(ModeloSimulacao modelo) {
        if (modelo.getFilas().isEmpty()) {
            throw new IllegalArgumentException("O modelo precisa declarar ao menos uma fila em queues.");
        }

        for (String nomeFila : modelo.getChegadas().keySet()) {
            if (!modelo.getFilas().containsKey(nomeFila)) {
                throw new IllegalArgumentException("Chegada declarada para fila inexistente: " + nomeFila);
            }
        }

        for (ModeloSimulacao.FilaConfig fila : modelo.getFilas().values()) {
            if (fila.servers <= 0) {
                throw new IllegalArgumentException("A fila " + fila.nome + " precisa ter servers > 0.");
            }
            if (fila.capacity <= 0) {
                throw new IllegalArgumentException("A fila " + fila.nome + " precisa ter capacity > 0.");
            }
            if (fila.minArrival > fila.maxArrival) {
                throw new IllegalArgumentException("A fila " + fila.nome + " tem minArrival maior que maxArrival.");
            }
            if (fila.minService > fila.maxService) {
                throw new IllegalArgumentException("A fila " + fila.nome + " tem minService maior que maxService.");
            }
        }

        for (String origem : modelo.getOrigensRede()) {
            if (!modelo.getFilas().containsKey(origem)) {
                throw new IllegalArgumentException("Origem inexistente na rede: " + origem);
            }

            double soma = 0.0;
            for (ModeloSimulacao.Rota rota : modelo.getRotas(origem)) {
                if (!modelo.getFilas().containsKey(rota.target)) {
                    throw new IllegalArgumentException("Destino inexistente na rede: " + rota.target);
                }
                if (rota.probability < 0.0) {
                    throw new IllegalArgumentException("Probabilidade negativa na rota " + rota.source + " -> " + rota.target);
                }
                soma += rota.probability;
            }
            if (soma > 1.000000001) {
                throw new IllegalArgumentException("A soma das probabilidades saindo de " + origem + " passou de 1.");
            }
        }

        if (modelo.usaSementes() && modelo.getRndnumbersPerSeed() <= 0) {
            throw new IllegalArgumentException("Quando seeds for usado, rndnumbersPerSeed precisa estar no YAML e ser maior que zero.");
        }

        if (!modelo.usaSementes()) {
            for (Double numero : modelo.getRndnumbers()) {
                if (numero < 0.0 || numero >= 1.0) {
                    throw new IllegalArgumentException("rndnumbers deve conter valores no intervalo [0,1).");
                }
            }
        }
    }

    private static String obrigatorio(Map<String, String> dados, String chave, String nomeFila) {
        String valor = dados.get(chave);
        if (valor == null || valor.isEmpty()) {
            throw new IllegalArgumentException("Campo obrigatorio ausente em " + nomeFila + ": " + chave);
        }
        return valor;
    }

    private static String removerComentario(String linha) {
        int indice = linha.indexOf('#');
        if (indice >= 0) {
            return linha.substring(0, indice);
        }
        return linha;
    }

    private static int contarEspacosIniciais(String linha) {
        int total = 0;
        while (total < linha.length() && linha.charAt(total) == ' ') {
            total++;
        }
        return total;
    }

    private static Par separarChaveValor(String texto) {
        int indice = texto.indexOf(':');
        if (indice < 0) {
            return null;
        }
        String chave = texto.substring(0, indice).trim();
        String valor = texto.substring(indice + 1).trim();
        return new Par(chave, valor);
    }

    private static int parseInt(String valor, String campo) {
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor inteiro invalido em " + campo + ": " + valor, e);
        }
    }

    private static long parseLong(String valor, String campo) {
        try {
            return Long.parseLong(valor.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor long invalido em " + campo + ": " + valor, e);
        }
    }

    private static double parseDouble(String valor, String campo) {
        try {
            return Double.parseDouble(valor.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor decimal invalido em " + campo + ": " + valor, e);
        }
    }

    private static class Par {
        final String chave;
        final String valor;

        Par(String chave, String valor) {
            this.chave = chave;
            this.valor = valor;
        }
    }

    private static class RotaBuilder {
        String source;
        String target;
        Double probability;
    }
}
