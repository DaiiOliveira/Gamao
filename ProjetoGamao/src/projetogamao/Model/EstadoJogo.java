package projetogamao.Model;

public class EstadoJogo {
    private int jogadorAtual = 1;

    public int getJogadorAtual() {
        return jogadorAtual;
    }

    public void proximoTurno() {
        jogadorAtual = (jogadorAtual == 1) ? 2 : 1;
    }
}
