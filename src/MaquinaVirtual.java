import javax.swing.*;
import java.util.List;
import java.util.Stack;

public class MaquinaVirtual implements Runnable {

    private List<Instrucao> instrucoes;
    private ConsoleExecucao console;
    
    // Memória (dados) e Pilha de operandos
    private Object[] memoria = new Object[1000]; // Simula a RAM (ajustar tamanho se necessário)
    private Stack<Object> pilha = new Stack<>();

    public MaquinaVirtual(List<Instrucao> instrucoes, ConsoleExecucao console) {
        this.instrucoes = instrucoes;
        this.console = console;
    }

    @Override
    public void run() {
        console.limpar();
        console.escrever("--- Iniciando Execução ---\n");
        
        // Limpa memória e pilha
        pilha.clear();
        for(int i=0; i<memoria.length; i++) memoria[i] = null;

        int ip = 0; // Instruction Pointer (aponta para a instrução atual)

        try {
            while (ip < instrucoes.size()) {
                Instrucao inst = instrucoes.get(ip);
                String op = inst.getOperacao();
                String par = inst.getParametro(); // Parâmetro como String

                // Avança IP por padrão (pode ser alterado por JMP/JMF)
                ip++; 

                switch (op) {
                    case "STP":
                        console.escrever("\n--- Execução Finalizada ---");
                        return; // Encerra a thread

                    // --- CARGA DE CONSTANTES (Load) ---
                    case "LDI": // Inteiro
                        pilha.push(Integer.parseInt(par));
                        break;
                    case "LDR": // Real
                        pilha.push(Double.parseDouble(par));
                        break;
                    case "LDS": // String
                        // Remove as aspas da string
                        String texto = par;
                        if (texto.startsWith("\"") && texto.endsWith("\"") || texto.startsWith("'") && texto.endsWith("'")) {
                            texto = texto.substring(1, texto.length() - 1);
                        }
                        pilha.push(texto);
                        break;
                    case "LDB": // Booleano (0 ou 1)
                        pilha.push(Integer.parseInt(par)); 
                        break;

                    // --- ARITMÉTICA ---
                    case "ADD":
                    case "SUB":
                    case "MUL":
                    case "DIV":
                    case "MOD":
                    case "POW":
                        executarAritmetica(op);
                        break;

                    // --- MEMÓRIA (Direta) ---
                    case "STR": // Store (guarda valor do topo no endereço)
                        int endStr = Integer.parseInt(par);
                        Object valStr = pilha.pop();
                        memoria[endStr] = valStr;
                        break;

                    case "LDV": // Load Variable (carrega valor do endereço para o topo)
                        int endLdv = Integer.parseInt(par);
                        pilha.push(memoria[endLdv]);
                        break;

                    // --- MEMÓRIA (Indireta - Vetores) ---
                    // Como não usamos SWP, a pilha está: [..., endereço, valor] (valor no topo)
                    case "STX": 
                        Object valorStx = pilha.pop();   // Pega o valor
                        int endStx = converterParaInt(pilha.pop()); // Pega o endereço
                        memoria[endStx] = valorStx;
                        break;

                    case "LDX":
                        int endLdx = converterParaInt(pilha.pop()); // Endereço está no topo
                        pilha.push(memoria[endLdx]);
                        break;

                    // --- ENTRADA E SAÍDA ---
                    case "WRT": // Write
                        Object valorWrt = pilha.pop();
                        console.escrever(String.valueOf(valorWrt) + "\n");
                        break;

                    case "REA": // Read
                    // Pede input no console
                    String input = console.ler(); // <--- A MÁGICA ACONTECE AQUI
                    // Converte e empilha
                    pilha.push(converterEntrada(input, Integer.parseInt(par)));
                    break;
                    // --- DESVIOS ---
                    case "JMP":
                        ip = Integer.parseInt(par) - 1; // -1 pois a lista é base 0
                        break;
                        
                    case "JMF": // Jump if False
                        Object condicao = pilha.pop();
                        boolean falsa = false;
                        if (condicao instanceof Integer) falsa = ((Integer)condicao) == 0;
                        if (condicao instanceof Boolean) falsa = !((Boolean)condicao);
                        
                        if (falsa) {
                            ip = Integer.parseInt(par) - 1;
                        }
                        break;

                    // --- RELACIONAIS ---
                    case "EQL": case "DIF": case "SMR": case "BGR": case "SME": case "BGE":
                        executarRelacional(op);
                        break;
                    
                    // --- ALOCAÇÃO (Apenas reservamos espaço, ignoramos aqui pois array é fixo) ---
                    case "ALI": case "ALR": case "ALS": case "ALB":
                        break; 

                    default:
                        System.out.println("Instrução não implementada na VM: " + op);
                }
            }
        } catch (Exception e) {
            console.escrever("\nERRO DE EXECUÇÃO (Runtime Error): " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Auxiliar para operações matemáticas
    private void executarAritmetica(String op) {
        double b = converterParaDouble(pilha.pop());
        double a = converterParaDouble(pilha.pop());
        double res = 0;

        switch (op) {
            case "ADD": res = a + b; break;
            case "SUB": res = a - b; break;
            case "MUL": res = a * b; break;
            case "DIV": res = a / b; break;
            case "MOD": res = a % b; break;
            case "POW": res = Math.pow(a, b); break;
        }
        
        // Se ambos eram inteiros originais, tenta manter inteiro (opcional, mas bom pra "num")
        if (res == (int)res) {
            pilha.push((int)res);
        } else {
            pilha.push(res);
        }
    }
    
    // Auxiliar para relacionais
    private void executarRelacional(String op) {
        double b = converterParaDouble(pilha.pop());
        double a = converterParaDouble(pilha.pop());
        boolean res = false;
        
        switch (op) {
            case "EQL": res = (a == b); break;
            case "DIF": res = (a != b); break;
            case "SMR": res = (a < b); break;
            case "BGR": res = (a > b); break;
            case "SME": res = (a <= b); break;
            case "BGE": res = (a >= b); break;
        }
        pilha.push(res ? 1 : 0);
    }

    // Utilitários de conversão
    private double converterParaDouble(Object o) {
        return Double.parseDouble(o.toString());
    }
    
    private int converterParaInt(Object o) {
        return Integer.parseInt(o.toString());
    }

    private Object converterEntrada(String input, int tipo) {
        try {
            switch (tipo) {
                case 1: return Integer.parseInt(input); // num
                case 2: return Double.parseDouble(input); // real
                case 3: return input; // text
                case 4: return Integer.parseInt(input); // flag (0 ou 1)
                default: return input;
            }
        } catch (Exception e) {
            return 0; // valor default em erro
        }
    }

    private String tipoPorCod(int i) {
        switch(i) {
            case 1: return "NUM";
            case 2: return "REAL";
            case 3: return "TEXT";
            case 4: return "FLAG";
            default: return "";
        }
    }
}