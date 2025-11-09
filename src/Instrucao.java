public class Instrucao {

    private int ponteiro;
    private String operacao;
    private String parametro;

    public Instrucao(int ponteiro, String operacao, String parametro) {
        this.ponteiro = ponteiro;
        this.operacao = operacao;
        this.parametro = parametro;
    }

    @Override
    public String toString() {
        return String.format("(%d, %s, %s)", ponteiro, operacao, parametro);
    }

    public int getPonteiro() {
        return this.ponteiro;
    }

    public String getOperacao() {
        return this.operacao;
    }

    public String getParametro() {
        return this.parametro;
    }
}