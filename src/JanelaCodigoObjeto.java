import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Janela para exibir o "Código Objeto" gerado,
 * baseada na imagem fornecida.
 */
public class JanelaCodigoObjeto extends JFrame {

    private JTable tabelaCodigo;
    private DefaultTableModel modeloTabela;

    public JanelaCodigoObjeto() {
        // 1. Configurações da Janela
        super("Código objeto"); // Define o título da janela
        setSize(350, 400); // Um tamanho inicial razoável
        setLocationRelativeTo(null); // Centralizar na tela (ou no 'owner' se for JDialog)
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Apenas fecha esta janela

        // 2. Configurar o Modelo da Tabela
        String[] colunas = {"Endereço", "Instrução", "Parâmetro"}; // Nomes de exemplo

        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Isso força todas as células a serem "read-only"
                return false;
            }
        }; // <-- ESTE É O PONTO E VÍRGULA QUE FALTAVA

        // 3. Criar a JTable
        tabelaCodigo = new JTable(modeloTabela);

        // Impede que o usuário arraste as colunas (Endereço, Instrução...)
        tabelaCodigo.getTableHeader().setReorderingAllowed(false);

        // Configurações visuais da tabela para se parecer com a imagem
        tabelaCodigo.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaCodigo.setShowGrid(true); // Mostrar a grade
        tabelaCodigo.setGridColor(Color.LIGHT_GRAY); // Cor da grade

        // 4. Adicionar a Tabela a um JScrollPane
        JScrollPane scrollPane = new JScrollPane(tabelaCodigo);

        // 5. Adicionar o Painel de Rolagem à Janela
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Método de exemplo para adicionar uma linha de código à tabela.
     * (Você vai chamar isso do seu CompilerInterface)
     */
    public void adicionarLinha(String endereco, String instrucao, String parametro) {
        modeloTabela.addRow(new Object[]{endereco, instrucao, parametro});
    }

    /**
     * Método para limpar a tabela antes de uma nova compilação.
     */
    public void limparTabela() {
        modeloTabela.setRowCount(0);
    }

    // --- Main para testar a janela de forma independente ---
    public static void main(String[] args) {
        // Define o Look and Feel do sistema, como no seu CompilerInterface
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Executa na thread de eventos do Swing
        SwingUtilities.invokeLater(() -> {
            JanelaCodigoObjeto janela = new JanelaCodigoObjeto();
            janela.setVisible(true);

            // Adiciona dados de exemplo para teste
            janela.adicionarLinha("0", "LDI", "10");
            janela.adicionarLinha("1", "LDI", "5");
            janela.adicionarLinha("2", "ADD", "0");
            janela.adicionarLinha("3", "STR", "100");
            janela.adicionarLinha("4", "STP", "0");
        });
    }
}