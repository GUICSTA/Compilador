import java.util.HashMap;
import java.util.Map;

public class TabelaDeSimbolos {

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


    public Simbolo inserir(String lexema, int categoria, int tamanho) {

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


    public void inserirPrograma(String lexema) {
        tabela.put(lexema, new Simbolo(lexema, 0, 0, -1));
    }

    public Simbolo buscar(String lexema) {
        return tabela.get(lexema);
    }

    public int getVT() {
        return this.VT;
    }
}