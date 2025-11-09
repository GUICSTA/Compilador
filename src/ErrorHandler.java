import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ErrorHandler {

    // Trocado 'List' por 'Set' (LinkedHashSet) para sintetizar erros
    private Set<String> errorMessages = new LinkedHashSet<>();

    // O Mapa de erros léxicos
    private static final java.util.Map<Integer, String> LEXICAL_ERROR_MESSAGES = new java.util.HashMap<>();
    static {
        // (Vou omitir o preenchimento para economizar espaço, o seu está ótimo)
        // LEXICAL_ERROR_MESSAGES.put(CompiladorConstants.ERRO_ID_INICIA_COM_DIGITO, ...);
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

        String finalMessage = String.format("Erro Sintático na linha %d, coluna %d. Encontrado %s, esperado: %s.",
                line, column, encontrado, esperado);

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