package pkg_model;

import java.util.ArrayList;
import java.util.List;

public class TableBDTModel extends TableModel {

	public TableBDTModel(Boolean anIsPartizionamentoMensile) {
		super(anIsPartizionamentoMensile);
	}

	public String PrimaryKey() {
		String s = "", calf = "", dtvf = "", abif = "";

		for (FieldModel f : getFields()) {
			if (f.getName().endsWith("DTV")) {
				dtvf = Quote(f.getName());
				continue;
			}
			if (f.getName().equals("CAL_DATE")) {
				calf = Quote(f.getName());
				continue;
			}

			if (f.getName().endsWith("ABI")) {
				abif = Quote(f.getName());
				continue;
			}

			s += ",";
			s += f.NameQuoted();

		}
		if (getIsPartizionamentoMensile())
			return "MESE," + calf + "," + abif + s + "," + dtvf;
		else
			return "ANNO," + calf + "," + abif + s + "," + dtvf;
	}

	public FieldModel Field_CAL_DATE() throws Exception {
		for (FieldModel f : getFields()) {
			if (f.getName().equals("CAL_DATE")) {
				return f;
			}
		}
		throw new Exception("Attenzione: la tabella non ha il campo CAL_DATE");
	}

	public List<FieldModel> Fields_NoABI_NoDTV_NoCAL_DATE() throws Exception {
		List<FieldModel> ret = new ArrayList<FieldModel>();
		for (FieldModel f : Fields_NoABI_NoDTV()) {
			if (f != Field_CAL_DATE())
				ret.add(f);
		}
		return ret;

	}
}
