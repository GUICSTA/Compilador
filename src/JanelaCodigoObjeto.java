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
        // Configurações da Janela
        super("Código objeto"); // Define o título da janela
        setSize(350, 400); // Um tamanho inicial razoável
        setLocationRelativeTo(null); // Centralizar na tela (ou no 'owner' se for JDialog)
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Apenas fecha esta janela

        // Configurar o Modelo da Tabela
        String[] colunas = {"Endereço", "Instrução", "Parâmetro"}; // Nomes de exemplo

        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // "read-only" da nova tela
                return false;
            }
        };

        tabelaCodigo = new JTable(modeloTabela);

        // Impede que o usuário arraste as colunas da nova tela
        tabelaCodigo.getTableHeader().setReorderingAllowed(false);

        tabelaCodigo.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaCodigo.setShowGrid(true);
        tabelaCodigo.setGridColor(Color.LIGHT_GRAY);

        JScrollPane scrollPane = new JScrollPane(tabelaCodigo);

        add(scrollPane, BorderLayout.CENTER);
    }

    public void adicionarLinha(String endereco, String instrucao, String parametro) {
        modeloTabela.addRow(new Object[]{endereco, instrucao, parametro});
    }

    public void limparTabela() {
        modeloTabela.setRowCount(0);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            JanelaCodigoObjeto janela = new JanelaCodigoObjeto();
            janela.setVisible(true);

            janela.adicionarLinha("0", "LDI", "10");
            janela.adicionarLinha("1", "LDI", "5");
            janela.adicionarLinha("2", "ADD", "0");
            janela.adicionarLinha("3", "STR", "100");
            janela.adicionarLinha("4", "STP", "0");
        });
    }
}