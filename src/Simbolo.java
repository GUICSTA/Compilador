public class Simbolo {

    private String lexema;
    private int categoria; // 0=programa, 1=num, 2=real, 3=text, 4=flag
    private int base;
    private int tamanho;   // -1 para escalar ("–"), N para vetor

    // Construtor corrigido para aceitar 'lexema'
    public Simbolo(String lexema, int categoria, int base, int tamanho) {
        this.lexema = lexema;
        this.categoria = categoria;
        this.base = base;
        this.tamanho = tamanho;
    }

    // --- MÉTODOS QUE ESTAVAM FALTANDO ---
    public String getLexema() {
        return lexema;
    }

    public int getCategoria() {
        return categoria;
    }

    public int getBase() {
        return base;
    }

    public int getTamanho() {
        return tamanho;
    }

    public boolean isVetor() {
        return tamanho > 0;
    }
}