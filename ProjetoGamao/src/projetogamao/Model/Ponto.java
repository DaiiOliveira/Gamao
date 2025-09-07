package projetogamao.Model;

public class Ponto {
    private int indice;
    private CorPeca cor;
    private int quantidade;

    public Ponto(int indice) {
        this.indice = indice;
        this.quantidade = 0;
        this.cor = null;
    }

    public void adicionar(CorPeca cor, int qtd) {
        if (this.quantidade == 0) {
            this.cor = cor;
        }
        this.quantidade += qtd;
    }

    public void removerUma() {
        if (quantidade > 0) {
            quantidade--;
            if (quantidade == 0) {
                cor = null;
            }
        }
    }

    public boolean vazio() {
        return quantidade == 0;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public CorPeca getCor() {
        return cor;
    }

    public int getIndice() {
        return indice;
    }
}
