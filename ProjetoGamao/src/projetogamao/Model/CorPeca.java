package projetogamao.Model;

public enum CorPeca {
    PRETO, BRANCO;

    public CorPeca adversaria() {
        return this == PRETO ? BRANCO : PRETO;
    }
}