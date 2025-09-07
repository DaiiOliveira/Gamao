package projetogamao.Model;

public class Movimento {
    private int origem;
    private int destino;

    public Movimento(int origem, int destino) {
        this.origem = origem;
        this.destino = destino;
    }

    public int getOrigem() {
        return origem;
    }

    public int getDestino() {
        return destino;
    }
}
