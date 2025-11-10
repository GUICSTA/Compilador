import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map;
import java.util.HashMap;

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
        // --- BLOCO PREENCHIDO ---
        LEXICAL_ERROR_MESSAGES.put(CompiladorConstants.ERRO_ID_INICIA_COM_DIGITO, "Identificador inválido: não pode começar com um dígito.");
        LEXICAL_ERROR_MESSAGES.put(CompiladorConstants.ERRO_ID_DIGITOS_CONSECUTIVOS, "Identificador inválido: não pode conter dois ou mais dígitos consecutivos.");
        LEXICAL_ERROR_MESSAGES.put(CompiladorConstants.ERRO_ID_TERMINA_COM_DIGITO, "Identificador inválido: não pode terminar com um dígito.");
        LEXICAL_ERROR_MESSAGES.put(CompiladorConstants.ERRO_INT_LONGO, "Constante numérica inteira muito longa.");
        LEXICAL_ERROR_MESSAGES.put(CompiladorConstants.ERRO_REAL_FRACAO_LONGA, "Constante real com parte fracionária muito longa.");
        LEXICAL_ERROR_MESSAGES.put(CompiladorConstants.ERRO_REAL_INTEIRO_LONGO, "Constante real com parte inteira muito longa.");
        LEXICAL_ERROR_MESSAGES.put(CompiladorConstants.ERRO_REAL_INCOMPLETO, "Constante real malformada (faltam dígitos após o ponto).");
        LEXICAL_ERROR_MESSAGES.put(CompiladorConstants.ERRO_LEXICO, "Símbolo não reconhecido pela linguagem.");
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
        // Se a mensagem customizada já tem "esperado:", não adiciona de novo.
        String finalMessage;
        if (esperado.startsWith("esperado:")) {
            finalMessage = String.format("Erro Sintático na linha %d, coluna %d (%s): Encontrado %s, %s.",
                    line, column, context, encontrado, esperado);
        } else {
            finalMessage = String.format("Erro Sintático na linha %d, coluna %d (%s): Encontrado %s, esperado: %s.",
                    line, column, context, encontrado, esperado);
        }

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
            message = String.format("Erro Léxico na linha %d, coluna %d %s: %s",
                    line, column, context, specific);
        } else {
            message = String.format("Erro Léxico na linha %d, coluna %d %s: Símbolo não reconhecido pela linguagem.",
                    line, column, context);
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
        return "'" + t.image.replace("\n", "\\n").replace("\r", "\\r") + "'";
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

        // --- GATILHO 5 (NOVO - para 'start' faltando 'end') ---
        // Este gatilho é para o erro da linha 49
        Set<String> startMissingEnd = new TreeSet<>();
        startMissingEnd.add("'end'");
        startMissingEnd.add("'if'");
        startMissingEnd.add("'loop'");
        startMissingEnd.add("'read'");
        startMissingEnd.add("'set'");
        startMissingEnd.add("'show'");

        if (expected.equals(startMissingEnd)) {
            return "esperado 'end;' ou comando"; // <-- Sua nova mensagem
        }

        // --- LÓGICA DOS GATILHOS 3 E 4 ---

        // Checa por operadores de expressão (comum aos dois gatilhos)
        boolean temOperador = expected.contains("'+'") || expected.contains("'-'") ||
                expected.contains("'*'") || expected.contains("'=='") ||
                expected.contains("'&'") || expected.contains("'|'") ||
                expected.contains("'!='") || expected.contains("'<<'");

        // --- GATILHO 'IF' (para linha 42) ---
        // Se espera 'then' E operadores, é um 'if' faltando 'then'
        boolean temThen = expected.contains("'then'");
        if (temThen && temOperador) {
            return "esperado: then ou expressão"; // <-- Sua mensagem da linha 42
        }

        // --- GATILHO 'SET' (para linha 37) ---
        // Se espera ';' E operadores, é um 'set' faltando ';'
        boolean temSemicolon = expected.contains("';'");
        if (temSemicolon && temOperador) {
            return "esperado: ';' ou expressão"; // <-- Sua mensagem da linha 37
        }

        // --- GATILHO (extra, para linha 38) ---
        Set<String> readMissingParen = new TreeSet<>();
        readMissingParen.add("')'");
        readMissingParen.add("'['"); // Esperado ')' ou '['

        if (expected.equals(readMissingParen)) {
            return "esperado: ')' ou indíce do vetor";
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