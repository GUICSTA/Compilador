import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class ConsoleExecucao extends JFrame {
    private JTextArea areaSaida;
    
    // Variáveis para controlar a leitura de dados
    private boolean aguardandoInput = false;
    private int posicaoInicialInput = 0;
    private String inputCapturado = "";
    private final Object lockInput = new Object(); // Trava para pausar a VM

    public ConsoleExecucao() {
        super("Console de Execução");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        areaSaida = new JTextArea();
        areaSaida.setBackground(Color.BLACK);
        areaSaida.setForeground(Color.WHITE);
        areaSaida.setFont(new Font("Consolas", Font.PLAIN, 14));
        areaSaida.setEditable(false); // Começa bloqueado para edição
        areaSaida.setLineWrap(true);

        // Adiciona o ouvinte de teclado para capturar o ENTER
        areaSaida.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (aguardandoInput) {
                    // Se apertar ENTER
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        e.consume(); // Não pular linha visualmente ainda
                        confirmarInput();
                    }
                    // Bloqueia apagar o texto da pergunta (Backspace)
                    else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                        if (areaSaida.getCaretPosition() <= posicaoInicialInput) {
                            e.consume();
                        }
                    }
                    // Bloqueia setas para não sair da área de input
                    else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_LEFT) {
                        if (areaSaida.getCaretPosition() <= posicaoInicialInput) {
                            e.consume();
                        }
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(areaSaida);
        add(scroll, BorderLayout.CENTER);
    }

    public void escrever(String texto) {
        areaSaida.append(texto);
        areaSaida.setCaretPosition(areaSaida.getDocument().getLength());
    }

    public void limpar() {
        areaSaida.setText("");
    }

    /**
     * Método BLOQUEANTE que pausa a thread da VM até o usuário digitar Enter.
     */
    public String ler() {
        // --- TRUQUE VISUAL: Remove o último \n para digitar na mesma linha ---
        try {
            int tam = areaSaida.getDocument().getLength();
            if (tam > 0) {
                String ultimoChar = areaSaida.getText(tam - 1, 1);
                if ("\n".equals(ultimoChar)) {
                    // Apaga o \n para o cursor subir
                    areaSaida.replaceRange("", tam - 1, tam);
                }
            }
        } catch (Exception e) { /* Ignora erro de UI */ }
        // ---------------------------------------------------------------------

        // 1. Prepara o console para digitar
        aguardandoInput = true;
        areaSaida.setEditable(true); // Libera a digitação
        
        // Atualiza a posição inicial para não deixar apagar a pergunta
        posicaoInicialInput = areaSaida.getDocument().getLength(); 
        
        areaSaida.setCaretPosition(posicaoInicialInput);
        areaSaida.requestFocus();

        // 2. Pausa a thread da VM e espera o ENTER
        synchronized (lockInput) {
            try {
                while (aguardandoInput) {
                    lockInput.wait(); // A VM dorme aqui
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // 3. Retorna o que foi digitado
        return inputCapturado;
    }

    private void confirmarInput() {
        try {
            // Pega o texto desde o ponto inicial até o fim
            int tamTotal = areaSaida.getDocument().getLength();
            if (tamTotal >= posicaoInicialInput) {
                inputCapturado = areaSaida.getText(posicaoInicialInput, tamTotal - posicaoInicialInput);
            } else {
                inputCapturado = "";
            }
            
            // Pula linha visualmente AGORA (depois do Enter)
            areaSaida.append("\n");
            
            // Reseta estados
            aguardandoInput = false;
            areaSaida.setEditable(false); // Bloqueia de novo
            
            // Acorda a VM
            synchronized (lockInput) {
                lockInput.notifyAll();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}