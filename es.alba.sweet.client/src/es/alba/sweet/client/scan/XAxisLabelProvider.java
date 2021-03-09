package es.alba.sweet.client.scan;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import es.alba.sweet.client.scan.graph.Legend;

public class XAxisLabelProvider extends ColumnLabelProvider {

	private Map<Object, Button> buttons = new HashMap<Object, Button>();

	public XAxisLabelProvider() {
	}

	@Override
	public void update(ViewerCell cell) {

		Button button;
		if (buttons.containsKey(cell.getElement())) {
			button = buttons.get(cell.getElement());
		} else {
			button = new Button((Composite) cell.getViewerRow().getControl(), SWT.CHECK);
			buttons.put(cell.getElement(), button);
		}
		Legend legend = (Legend) cell.getElement();
		button.setSelection(legend.isxAxis());
	}

	public Map<Object, Button> getButtons() {
		return buttons;
	}

}
