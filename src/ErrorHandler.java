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
 * Classe ErrorHandler atualizada com:
 * 1. Mensagens personalizadas (Gatilhos)
 * 2. Lógica de prioridade de exibição de erros (Léxico > Sintático > Semântico)
 */
public class ErrorHandler {

    private Set<String> lexicalErrors = new LinkedHashSet<>();
    private Set<String> syntacticErrors = new LinkedHashSet<>();
    private Set<String> semanticErrors = new LinkedHashSet<>();

    // O Mapa de erros léxicos
    private static final java.util.Map<Integer, String> LEXICAL_ERROR_MESSAGES = new java.util.HashMap<>();
    static {
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
     * (Lógica de mensagens customizadas mantida)
     */
    public void processParseException(ParseException e, String context) {
        Token errorToken = (e.currentToken.next != null) ? e.currentToken.next : e.currentToken;
        int line = errorToken.beginLine;
        int column = errorToken.beginColumn;

        String encontrado = getErrorTokenDescription(errorToken);
        String esperado = getExpectedTokensDescription(e);

        String finalMessage;
        if (esperado.startsWith("esperado:")) {
            finalMessage = String.format("Erro Sintático na linha %d, coluna %d (%s): Encontrado %s, %s.",
                    line, column, context, encontrado, esperado);
        } else {
            finalMessage = String.format("Erro Sintático na linha %d, coluna %d (%s): Encontrado %s, esperado: %s.",
                    line, column, context, encontrado, esperado);
        }

        syntacticErrors.add(finalMessage);
    }

    /**
     * Processa um erro léxico direto (chamado pelo Passo 1 da compilação).
     * (Lógica de mensagens customizadas mantida)
     */
    /**
     * Processa um erro léxico direto (chamado pelo Passo 1 da compilação).
     * (Lógica de mensagens customizadas mantida)
     */
    public void processLexicalError(Token t, String context) {
        if (t == null) return;

        int line = t.beginLine;
        int column = t.beginColumn;

        //Pega o token "Encontrado" formatado
        String encontrado = getErrorTokenDescription(t);

        //Pega a descrição específica do erro
        String specificMessage;
        if (LEXICAL_ERROR_MESSAGES.containsKey(t.kind)) {
            specificMessage = LEXICAL_ERROR_MESSAGES.get(t.kind);
        } else {
            specificMessage = "Símbolo não reconhecido pela linguagem.";
        }

        //Formata a mensagem final
        String finalMessage;
        if (context != null && !context.isEmpty()) {
            // Padrão completo (se o contexto for fornecido)
            finalMessage = String.format("Erro Léxico na linha %d, coluna %d (%s): Encontrado %s. %s",
                    line, column, context, encontrado, specificMessage);
        } else {
            // Padrão limpo (se o contexto estiver vazio, como na chamada do CompilerInterface)
            finalMessage = String.format("Erro Léxico na linha %d, coluna %d: Encontrado %s. %s",
                    line, column, encontrado, specificMessage);
        }

        lexicalErrors.add(finalMessage);
    }

    /**
     * Adiciona um erro semântico (chamado pelo .jj)
     */
    public void addError(String tipo, int linha, int col, String message) {
        if ("Semântico".equals(tipo)) {
            semanticErrors.add(String.format(
                    "Erro %s (linha %d, coluna %d): %s",
                    tipo, linha, col, message
            ));
        }
    }


    private String getErrorTokenDescription(Token t) {
        if (t.kind == CompiladorConstants.EOF) {
            return "o final do arquivo";
        }
        return "'" + t.image.replace("\n", "\\n").replace("\r", "\\r") + "'";
    }

    /**
     * Método atualizado para incluir os GATILHOS de mensagens personalizadas.
     * (Totalmente preservado do seu arquivo)
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

                if (cleanImage.startsWith("<") && cleanImage.endsWith(">")) {
                    cleanImage = cleanImage.substring(1, cleanImage.length() - 1);
                }
                expected.add(cleanImage);
            }
        }

        Set<String> tiposEsperados = new TreeSet<>();
        tiposEsperados.add("'num'");
        tiposEsperados.add("'real'");
        tiposEsperados.add("'text'");
        tiposEsperados.add("'flag'");

        if (expected.equals(tiposEsperados)) {
            return "tipo do identificador";
        }

        Set<String> initEsperados = new TreeSet<>();
        initEsperados.add("';'");
        initEsperados.add("'='");
        initEsperados.add("'['");

        if (expected.equals(initEsperados)) {
            return "';' ou inicialização de identificador";
        }

        Set<String> startMissingEnd = new TreeSet<>();
        startMissingEnd.add("'end'");
        startMissingEnd.add("'if'");
        startMissingEnd.add("'loop'");
        startMissingEnd.add("'read'");
        startMissingEnd.add("'set'");
        startMissingEnd.add("'show'");

        if (expected.equals(startMissingEnd)) {
            return "esperado 'end;' ou comando";
        }

        boolean temOperador = expected.contains("'+'") || expected.contains("'-'") ||
                expected.contains("'*'") || expected.contains("'=='") ||
                expected.contains("'&'") || expected.contains("'|'") ||
                expected.contains("'!='") || expected.contains("'<<'");

        boolean temThen = expected.contains("'then'");
        if (temThen && temOperador) {
            return "esperado: then ou expressão";
        }

        boolean temSemicolon = expected.contains("';'");
        if (temSemicolon && temOperador) {
            return "esperado: ';' ou expressão";
        }

        Set<String> readMissingParen = new TreeSet<>();
        readMissingParen.add("')'");
        readMissingParen.add("'['");

        if (expected.equals(readMissingParen)) {
            return "esperado: ')' ou indíce do vetor";
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


    public boolean hasErrors() {
        return !lexicalErrors.isEmpty() || !syntacticErrors.isEmpty() || !semanticErrors.isEmpty();
    }

    //Trazer os erros em ordem -> léxico, sintático e semântico
    public List<String> getErrorMessages() {
        if (!lexicalErrors.isEmpty()) {
            return new ArrayList<>(lexicalErrors);
        }

        if (!syntacticErrors.isEmpty()) {
            return new ArrayList<>(syntacticErrors);
        }
        return new ArrayList<>(semanticErrors);
    }
}