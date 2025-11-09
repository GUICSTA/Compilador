import java.util.ArrayList;
import java.util.List;

/**
 * Gerencia a lista de instruções (código intermediário) e o 'ponteiro'.
 */
public class GeradorDeCodigo {

    private List<Instrucao> codigo;
    private int ponteiro = 1; // Começa em 1 (conforme GLC)

    public GeradorDeCodigo() {
        this.codigo = new ArrayList<>();
    }

    /**
     * --- CORREÇÃO AQUI ---
     * (Corrigido para retornar 'int' - o endereço da instrução gerada)
     */
    public int gerar(String operacao, String parametro) {
        int ponteiroAtual = this.ponteiro;
        codigo.add(new Instrucao(ponteiroAtual, operacao, parametro));
        this.ponteiro++;
        return ponteiroAtual; // Retorna o ponteiro (número) da instrução
    }

    /**
     * --- MÉTODO ADICIONADO ---
     * (Este método estava faltando e era necessário para JMF/JMP)
     */
    public int getProximoPonteiro() {
        return this.ponteiro;
    }

    /**
     * --- MÉTODO ADICIONADO ---
     * "Corrige" o parâmetro de uma instrução que já foi gerada.
     * (Este método estava faltando e era necessário para JMF/JMP)
     */
    public void corrigir(int enderecoInstrucao, String novoParametro) {
        // O ponteiro é base-1, mas a lista é base-0
        if (enderecoInstrucao > 0 && enderecoInstrucao <= codigo.size()) {
            Instrucao inst = codigo.get(enderecoInstrucao - 1);
            // Recria a instrução com o parâmetro corrigido
            codigo.set(enderecoInstrucao - 1, new Instrucao(inst.getPonteiro(), inst.getOperacao(), novoParametro));
        }
    }

    /**
     * Retorna a lista de instruções como uma String formatada.
     */
    public String getCodigoIntermediario() {
        StringBuilder sb = new StringBuilder();
        for (Instrucao inst : codigo) {
            sb.append(inst.toString()).append("\n");
        }
        return sb.toString();
    }
}