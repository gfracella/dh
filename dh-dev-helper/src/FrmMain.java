
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JCheckBox;
import javax.swing.JTextArea;
import javax.swing.JComboBox;
import javax.swing.JButton;
import java.awt.Font;
import javax.swing.JScrollPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.border.TitledBorder;
import javax.swing.border.EtchedBorder;
import java.awt.Color;

public class FrmMain extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3008084308925140252L;
	private JPanel contentPane;

	private Font DEFAULT_FONT() {
		return new Font("Tahoma", Font.PLAIN, 11);
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FrmMain frame = new FrmMain();
					frame.setLocationRelativeTo(null);
					frame.setVisible(true);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public FrmMain() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1400, 1000);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		setContentPane(contentPane);

		JButton btnGestioneDefinizioni = new JButton("Gestione Definizioni");
		btnGestioneDefinizioni.setFont(DEFAULT_FONT());
		btnGestioneDefinizioni.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FrmGestioneDefinizioni f = new FrmGestioneDefinizioni();
				f.setVisible(true);
			}
		});
		contentPane.setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(7, 160, 1367, 794);
		contentPane.add(scrollPane);

		JTextArea _rtSql = new JTextArea();
		_rtSql.setFont(new Font("Monospaced", Font.PLAIN, 10));
		scrollPane.setViewportView(_rtSql);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), "Selezione tabella", TitledBorder.LEADING, TitledBorder.TOP, DEFAULT_FONT(),
				new Color(0, 0, 0)));
		panel_1.setBounds(7, 11, 304, 138);
		contentPane.add(panel_1);
		panel_1.setLayout(null);

		JComboBox<String> _cmbTables = new JComboBox<String>();
		_cmbTables.setBounds(6, 16, 288, 22);
		panel_1.add(_cmbTables);
		_cmbTables.setFont(DEFAULT_FONT());

		JCheckBox chkPartizionamentoPerMese = new JCheckBox("Partizionata Per Mese");
		chkPartizionamentoPerMese.setBounds(6, 45, 292, 23);
		panel_1.add(chkPartizionamentoPerMese);
		chkPartizionamentoPerMese.setFont(DEFAULT_FONT());

		JCheckBox chkALLView = new JCheckBox("Generazione vista _ALL");
		chkALLView.setFont(new Font("Tahoma", Font.PLAIN, 11));
		chkALLView.setSelected(true);
		chkALLView.setBounds(6, 71, 173, 23);
		panel_1.add(chkALLView);
		for (String s : pkg_model.TableModel.AllTables()) {
			_cmbTables.addItem(s);
		}

		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Creazione", TitledBorder.LEADING, TitledBorder.TOP, DEFAULT_FONT(), null));
		panel.setBounds(312, 11, 148, 138);
		contentPane.add(panel);
		panel.setLayout(null);

		JButton btnCreaTabellaODS = new JButton("Tabella ODS");
		btnCreaTabellaODS.setBounds(10, 14, 120, 23);
		panel.add(btnCreaTabellaODS);
		btnCreaTabellaODS.setFont(DEFAULT_FONT());

		JButton btnCreaTabellaDTO = new JButton("Tabella DTO");
		btnCreaTabellaDTO.setBounds(10, 44, 120, 23);
		panel.add(btnCreaTabellaDTO);
		btnCreaTabellaDTO.setFont(DEFAULT_FONT());

		JButton btnCreaRebuilProc = new JButton("Rebuild");
		btnCreaRebuilProc.setBounds(10, 74, 120, 23);
		panel.add(btnCreaRebuilProc);
		btnCreaRebuilProc.setFont(DEFAULT_FONT());
		
		JButton btnCreaRebuilProcV2 = new JButton("Rebuild V2");
		btnCreaRebuilProcV2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				String sql = "working...";
				_rtSql.setText(sql);
				try {
					sql = pkg_model.RebuildV2Model.CreaRebuildProc();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				_rtSql.setText(sql);
				_rtSql.setCaretPosition(0);
				
			}
		});
		btnCreaRebuilProcV2.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnCreaRebuilProcV2.setBounds(10, 104, 120, 23);
		panel.add(btnCreaRebuilProcV2);

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(null, "Gestione", TitledBorder.LEADING, TitledBorder.TOP, DEFAULT_FONT(), null));
		panel_2.setBounds(470, 11, 185, 138);
		contentPane.add(panel_2);
		panel_2.setLayout(null);

		JButton btnGestionDefinizione = new JButton("Gestione Definizioni");
		btnGestionDefinizione.setFont(DEFAULT_FONT());
		btnGestionDefinizione.setBounds(6, 16, 173, 23);
		panel_2.add(btnGestionDefinizione);
		btnGestionDefinizione.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				FrmGestioneDefinizioni gd = new FrmGestioneDefinizioni();
				gd.setLocationRelativeTo(null);
				gd.setVisible(true);
			}
		});
		btnCreaRebuilProc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String sql = "working...";
				_rtSql.setText(sql);
				try {
					sql = pkg_model.RebuildModel.CreaRebuildProc();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				_rtSql.setText(sql);
				_rtSql.setCaretPosition(0);
			}
		});
		btnCreaTabellaDTO.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Boolean isPartizionamentoPerMESE = chkPartizionamentoPerMese.isSelected();
				Boolean createALLView = chkALLView.isSelected();
				String sql = "working...";
				_rtSql.setText(sql);
				_rtSql.getRootPane().updateUI();
				try {
					String aTableName = (String) _cmbTables.getSelectedItem();
					sql = pkg_model.TableModel.CreatabellaDTO(aTableName, isPartizionamentoPerMESE, createALLView);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				_rtSql.setText(sql);
				_rtSql.setCaretPosition(0);
			}
		});
		btnCreaTabellaODS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				Boolean isPartizionamentoPerMESE = chkPartizionamentoPerMese.isSelected();
				Boolean createALLView = chkALLView.isSelected();
				String sql = "working...";
				_rtSql.setText(sql);
				_rtSql.getRootPane().updateUI();
				try {
					String aTableName = (String) _cmbTables.getSelectedItem();
					sql = pkg_model.TableModel.CreatabellaODS(aTableName, isPartizionamentoPerMESE, createALLView);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				_rtSql.setText(sql);
				_rtSql.setCaretPosition(0);
			}
		});
	}
}
