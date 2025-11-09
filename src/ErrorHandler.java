import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * [Arquivo: ErrorHandler.java]
 * * Classe ErrorHandler atualizada com todas as modificações
 * para personalizar as mensagens de erro sintático.
 */
public class ErrorHandler {

    // Trocado 'List' por 'Set' (LinkedHashSet) para sintetizar erros
    private Set<String> errorMessages = new LinkedHashSet<>();

    // O Mapa de erros léxicos
    private static final java.util.Map<Integer, String> LEXICAL_ERROR_MESSAGES = new java.util.HashMap<>();
    static {
        // (O preenchimento do seu mapa de erros léxicos original entra aqui)
        // Ex: LEXICAL_ERROR_MESSAGES.put(CompiladorConstants.ERRO_ID_INICIA_COM_DIGITO, ...);
    }

    /**
     * Processa uma ParseException (ERRO SINTÁTICO).
     * Este é o método que "traduz" a mensagem feia.
     */
    public void processParseException(ParseException e, String context) {
        Token errorToken = (e.currentToken.next != null) ? e.currentToken.next : e.currentToken;
        int line = errorToken.beginLine;
        int column = errorToken.beginColumn;

        String encontrado = getErrorTokenDescription(errorToken);
        String esperado = getExpectedTokensDescription(e);

        // --- MODIFICAÇÃO 1: Adiciona o contexto na mensagem ---
        String finalMessage = String.format("Erro Sintático na linha %d, coluna %d (%s): Encontrado %s, esperado: %s.",
                line, column, context, encontrado, esperado);

        addError(finalMessage);
    }

    /**
     * Processa um erro léxico direto (chamado pelo Passo 1 da compilação).
     */
    public void processLexicalError(Token t, String context) {
        if (t == null) return;

        int line = t.beginLine;
        int column = t.beginColumn;
        String message;

        if (LEXICAL_ERROR_MESSAGES.containsKey(t.kind)) {
            String specific = LEXICAL_ERROR_MESSAGES.get(t.kind);
            message = String.format("Erro Léxico na linha %d, coluna %d: %s",
                    line, column, specific);
        } else {
            message = String.format("Erro Léxico na linha %d, coluna %d: Símbolo não reconhecido '%s'.",
                    line, column, t.image);
        }

        addError(message);
    }

    /**
     * Adiciona um erro semântico (chamado pelo .jj)
     */
    public void addError(String tipo, int linha, int col, String message) {
        errorMessages.add(String.format(
                "Erro %s (linha %d, coluna %d): %s",
                tipo, linha, col, message
        ));
    }


    private String getErrorTokenDescription(Token t) {
        if (t.kind == CompiladorConstants.EOF) {
            return "o final do arquivo";
        }
        return "\"" + t.image.replace("\n", "\\n").replace("\r", "\\r") + "\"";
    }

    /**
     * Método atualizado para incluir os GATILHOS de mensagens personalizadas.
     */
    private String getExpectedTokensDescription(ParseException e) {
        if (e.expectedTokenSequences == null || e.expectedTokenSequences.length == 0) {
            return "uma expressão válida";
        }

        Set<String> expected = new TreeSet<>();
        for (int[] sequence : e.expectedTokenSequences) {
            if (sequence.length == 1) {
                String tokenImage = e.tokenImage[sequence[0]];
                String cleanImage = tokenImage.replace("\"", "'")
                        .replace("<IDENTIFIER>", "um identificador")
                        .replace("<CONST_REAL>", "um número real")
                        .replace("<CONST_INT>", "um número inteiro")
                        .replace("<CONST_LITERAL>", "um texto")
                        .replace("<EOF>", "o final do arquivo");

                // Remove os < > de tokens como <OP_ARIT_SUM>
                if (cleanImage.startsWith("<") && cleanImage.endsWith(">")) {
                    cleanImage = cleanImage.substring(1, cleanImage.length() - 1);
                }
                expected.add(cleanImage);
            }
        }

        // --- GATILHO 1 (Para tipos) ---
        Set<String> tiposEsperados = new TreeSet<>();
        tiposEsperados.add("'num'");
        tiposEsperados.add("'real'");
        tiposEsperados.add("'text'");
        tiposEsperados.add("'flag'");

        if (expected.equals(tiposEsperados)) {
            return "tipo do identificador";
        }

        // --- GATILHO 2 (Para ';' ou inicialização) ---
        Set<String> initEsperados = new TreeSet<>();
        initEsperados.add("';'");
        initEsperados.add("'='");
        initEsperados.add("'['");

        if (expected.equals(initEsperados)) {
            return "';' ou inicialização de identificador";
        }

        // --- GATILHO 3 (Para 'set' faltando ';') ---
        // (Baseado na lista de tokens da image_ee807b.png)
        Set<String> setMissingSemicolon = new TreeSet<>();
        setMissingSemicolon.add("'!='");
        setMissingSemicolon.add("'%'");
        setMissingSemicolon.add("'%%'");
        setMissingSemicolon.add("'&'");
        setMissingSemicolon.add("'*'");
        setMissingSemicolon.add("'**'");
        setMissingSemicolon.add("'+'");
        setMissingSemicolon.add("','");
        setMissingSemicolon.add("'-'");
        setMissingSemicolon.add("'/'");
        setMissingSemicolon.add("';'");
        setMissingSemicolon.add("'<<'");
        setMissingSemicolon.add("'<<='");
        setMissingSemicolon.add("'='");
        setMissingSemicolon.add("'=='");
        setMissingSemicolon.add("'>>'");
        setMissingSemicolon.add("'>>='");
        setMissingSemicolon.add("'|'");

        if (expected.equals(setMissingSemicolon)) {
            return "';' , ')' ou expressão";
        }

        // --- Lógica Padrão (Fallback) ---
        if (expected.isEmpty()) {
            return "uma construção válida";
        }

        List<String> expectedList = new ArrayList<>(expected);
        if (expectedList.size() == 1) {
            return expectedList.get(0);
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < expectedList.size(); i++) {
            sb.append(expectedList.get(i));
            if (i < expectedList.size() - 2) {
                sb.append(", ");
            } else if (i == expectedList.size() - 2) {
                sb.append(" ou ");
            }
        }
        return sb.toString();
    }

    public void addError(String message) {
        errorMessages.add(message);
    }

    public boolean hasErrors() {
        return !errorMessages.isEmpty();
    }

    public List<String> getErrorMessages() {
        return new ArrayList<>(errorMessages);
    }
}