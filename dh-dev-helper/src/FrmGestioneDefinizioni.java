import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import pkg_model.FieldModel;
import pkg_model.TableModel;

import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import java.util.Vector;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.ListSelectionModel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.Font;
import javax.swing.border.TitledBorder;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class FrmGestioneDefinizioni extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField _txtDes;
	private JTextField _txtTipoVista;
	private JTextField _txtNomeVista;
	private JTextField _txtFrequenza;

	/**
	 * Create the frame.
	 */

	private Font DEFAULT_FONT() {
		return new Font("Tahoma", Font.PLAIN, 11);
	}

	DefaultTableModel getColumnModel() {
		DefaultTableModel tmodel = new DefaultTableModel();
		tmodel.addColumn("Short Name");
		tmodel.addColumn("Description");
		tmodel.addColumn("IsKey");
		tmodel.addColumn("SA");
		tmodel.addColumn("Long Name");
		tmodel.addColumn("Type");
		tmodel.addColumn("Len");
		tmodel.addColumn("Scale");
		tmodel.addColumn("Nro");
		return tmodel;
	}

	Vector<Vector<Object>> getFieldDefinitionData(String aTableName) {
		Vector<Vector<Object>> ret = new Vector<Vector<Object>>();
		TableModel t = pkg_model.TableModel.GetTable(aTableName, false);

		for (FieldModel f : t.getFields()) {
			Vector<Object> fieldAttributes = new Vector<Object>();
			fieldAttributes.add(f.getName());
			fieldAttributes.add(f.getDescription());
			fieldAttributes.add(f.getIsKey());
			fieldAttributes.add(f.getIsAnalitycs());
			fieldAttributes.add(f.getNameS2A());
			fieldAttributes.add(f.getType());
			fieldAttributes.add(f.getLength());
			fieldAttributes.add(f.getScale());
			fieldAttributes.add(f.getNro());
			ret.add(fieldAttributes);
		}
		return ret;
	}

	public FrmGestioneDefinizioni() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 1008, 921);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JPanel panel = new JPanel();
		panel.setBounds(10, 11, 972, 859);
		contentPane.add(panel);

		DefaultTableModel tmodel = getColumnModel();
		JTable _tblFields = new JTable(tmodel);
		_tblFields.getColumn("Len").setMaxWidth(40);
		_tblFields.getColumn("Type").setMaxWidth(40);
		_tblFields.getColumn("Scale").setMaxWidth(40);
		_tblFields.getColumn("SA").setMaxWidth(50);
		_tblFields.getColumn("IsKey").setMaxWidth(50);
		_tblFields.getColumn("Nro").setMaxWidth(50);

		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		_tblFields.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
		_tblFields.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
		_tblFields.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
		_tblFields.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
		_tblFields.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);
		_tblFields.getColumnModel().getColumn(8).setCellRenderer(centerRenderer);

		_tblFields.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		_tblFields.setFillsViewportHeight(true);

		JScrollPane scrollPane = new JScrollPane(_tblFields);
		scrollPane.setBounds(0, 215, 962, 633);

		JPanel panel_1 = new JPanel();
		panel_1.setBounds(0, 77, 811, 127);
		panel_1.setLayout(null);

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(null, "Descrizione tabella", TitledBorder.LEADING, TitledBorder.TOP,
				DEFAULT_FONT(), null));
		panel_2.setBounds(0, 0, 524, 127);
		panel_1.add(panel_2);
		panel_2.setLayout(null);

		JLabel lblNewLabel = new JLabel("Descrizione");
		lblNewLabel.setBounds(10, 27, 83, 14);
		panel_2.add(lblNewLabel);
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));

		_txtDes = new JTextField();
		_txtDes.setBounds(103, 24, 398, 20);
		panel_2.add(_txtDes);

		JLabel lblNewLabel_1 = new JLabel("Tipo Vista");
		lblNewLabel_1.setBounds(10, 99, 83, 14);
		panel_2.add(lblNewLabel_1);
		lblNewLabel_1.setFont(new Font("Tahoma", Font.PLAIN, 11));

		_txtTipoVista = new JTextField();
		_txtTipoVista.setBounds(102, 96, 113, 20);
		panel_2.add(_txtTipoVista);

		JLabel lblNewLabel_2 = new JLabel("Nome Vista");
		lblNewLabel_2.setBounds(10, 51, 83, 14);
		panel_2.add(lblNewLabel_2);
		lblNewLabel_2.setFont(new Font("Tahoma", Font.PLAIN, 11));

		_txtNomeVista = new JTextField();
		_txtNomeVista.setBounds(103, 48, 398, 20);
		panel_2.add(_txtNomeVista);

		JLabel lblNewLabel_3 = new JLabel("Frequenza");
		lblNewLabel_3.setBounds(10, 75, 83, 14);
		panel_2.add(lblNewLabel_3);
		lblNewLabel_3.setFont(new Font("Tahoma", Font.PLAIN, 11));

		_txtFrequenza = new JTextField();
		_txtFrequenza.setBounds(102, 72, 113, 20);
		panel_2.add(_txtFrequenza);
		panel.setLayout(null);
		panel.add(scrollPane);
		panel.add(panel_1);

		JPanel panel_3 = new JPanel();
		panel_3.setBorder(new TitledBorder(null, "Selezione tabella", TitledBorder.LEADING, TitledBorder.TOP,
				DEFAULT_FONT(), null));
		panel_3.setBounds(0, -1, 529, 67);
		panel.add(panel_3);
		panel_3.setLayout(null);

		JComboBox<String> _cmbTables = new JComboBox<String>();
		_cmbTables.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String bTableName = (String) _cmbTables.getSelectedItem();
				pkg_model.TableModel btdef = pkg_model.TableModel.GetTable(bTableName, false);
				_txtDes.setText(btdef.getDescription());
				_txtFrequenza.setText(btdef.getFrequenza());
				_txtNomeVista.setText(btdef.getViewName());
				_txtTipoVista.setText(btdef.getViewType());

				DefaultTableModel tmodel = getColumnModel();
				Vector<Vector<Object>> fldDefs = getFieldDefinitionData(bTableName);

				for (Vector<Object> item : fldDefs) {
					tmodel.addRow(item);
				}
				_tblFields.setModel(tmodel);
				_tblFields.getColumn("IsKey").setMaxWidth(40);
				_tblFields.getColumn("SA").setMaxWidth(40);
				_tblFields.getColumn("Type").setMaxWidth(40);
				_tblFields.getColumn("Len").setMaxWidth(40);
				_tblFields.getColumn("Scale").setMaxWidth(40);
				_tblFields.getColumn("Nro").setMaxWidth(40);
				
				DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
				centerRenderer.setHorizontalAlignment( JLabel.CENTER );
				_tblFields.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);				
				_tblFields.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
				_tblFields.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
				_tblFields.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
				_tblFields.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);
				_tblFields.getColumnModel().getColumn(8).setCellRenderer(centerRenderer);
			}
		});
		_cmbTables.setBounds(10, 21, 491, 22);
		panel_3.add(_cmbTables);
		_cmbTables.setFont(new Font("Tahoma", Font.PLAIN, 11));

		for (String s : pkg_model.TableModel.AllTables()) {
			_cmbTables.addItem(s);
		}

	}
}
