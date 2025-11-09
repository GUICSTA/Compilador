import java.util.HashMap;
import java.util.Map;

/**
 * Gerencia a Tabela de Símbolos (TS), o escopo e os endereços de memória (VT).
 */
public class TabelaDeSimbolos {

    // O HashMap principal. A Chave (String) é o nome da variável (ex: "x").
    private Map<String, Simbolo> tabela;
    private ErrorHandler errorHandler;

    // VT: Variável de Topo. Controla a próxima base livre.
    private int VT = 1;

    public TabelaDeSimbolos() {
        this.tabela = new HashMap<>();
    }

    public void setErrorHandler(ErrorHandler handler) {
        this.errorHandler = handler;
    }

    /**
     * --- CORREÇÃO AQUI ---
     * A assinatura do método foi alterada para (String, int, int)
     * para corresponder ao que o Compilador.jj está chamando.
     */
    public Simbolo inserir(String lexema, int categoria, int tamanho) {

        // --- VALIDAÇÃO SEMÂNTICA #1 ---
        // (Movida de volta para cá, pois o .jj não faz mais isso)
        if (tabela.containsKey(lexema)) {
            if (errorHandler != null) {
                // (Usamos 0,0 para linha/coluna pois não temos o Token aqui)
                errorHandler.addError(String.format(
                        "Erro Semântico (linha 0, coluna 0): O identificador '%s' já foi declarado.",
                        lexema
                ));
            }
            return null; // Retorna nulo para indicar falha
        }

        // 'tamanho' já é o inteiro correto (ex: 3 para vetor, -1 para escalar)
        int tam = (tamanho > 0) ? tamanho : -1;

        Simbolo novoSimbolo = new Simbolo(lexema, categoria, this.VT, tam);

        tabela.put(lexema, novoSimbolo);

        // --- Atualiza o ponteiro da memória (VT) ---
        if (tamanho > 0) {
            this.VT += tamanho; // Vetor
        } else {
            this.VT += 1; // Escalar
        }

        return novoSimbolo;
    }

    /**
     * Insere o nome do programa (categoria 0)
     * (Método necessário para a regra 'identificador_de_programa')
     */
    public void inserirPrograma(String lexema) {
        tabela.put(lexema, new Simbolo(lexema, 0, 0, -1));
    }

    /**
     * Busca um símbolo na tabela.
     * (Método necessário para 'atribuicao', 'elemento', etc.)
     */
    public Simbolo buscar(String lexema) {
        return tabela.get(lexema);
    }

    /**
     * Retorna o VT atual (próxima base livre)
     * (Método necessário para #V2 e #E2)
     */
    public int getVT() {
        return this.VT;
    }
}