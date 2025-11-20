import java.util.ArrayList;
import java.util.List;

public class GeradorDeCodigo {

    private List<Instrucao> codigo;
    private int ponteiro = 1; // Começa em 1 (conforme GLC)

    public GeradorDeCodigo() {
        this.codigo = new ArrayList<>();
    }

    public int gerar(String operacao, String parametro) {
        int ponteiroAtual = this.ponteiro;
        codigo.add(new Instrucao(ponteiroAtual, operacao, parametro));
        this.ponteiro++;
        return ponteiroAtual; // Retorna o ponteiro (número) da instrução
    }

    public int getProximoPonteiro() {
        return this.ponteiro;
    }

    public void corrigir(int enderecoInstrucao, String novoParametro) {
        if (enderecoInstrucao > 0 && enderecoInstrucao <= codigo.size()) {
            Instrucao inst = codigo.get(enderecoInstrucao - 1);
            codigo.set(enderecoInstrucao - 1, new Instrucao(inst.getPonteiro(), inst.getOperacao(), novoParametro));
        }
    }

    public String getCodigoIntermediario() {
        StringBuilder sb = new StringBuilder();
        for (Instrucao inst : codigo) {
            sb.append(inst.toString()).append("\n");
        }
        return sb.toString();
    }

    // Este é o método que o Compilador.java está tentando chamar
    public List<Instrucao> getInstrucoes() {
        return this.codigo;
    }

}