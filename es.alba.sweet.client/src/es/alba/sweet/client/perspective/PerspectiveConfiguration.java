package es.alba.sweet.client.perspective;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import es.alba.sweet.base.configuration.AModelObject;
import es.alba.sweet.base.output.Output;
import es.alba.sweet.client.core.constant.Id;

public class PerspectiveConfiguration extends AModelObject {

	public final static String	DEFAULT	= "default";

	private String				id;

	private List<String>		layout	= new ArrayList<>();

	private String				selectedLayout;

	public PerspectiveConfiguration() {
		layout.add(DEFAULT);
		selectedLayout = DEFAULT;
		id = Id.SCAN_PERSPECTIVE;
	}

	public PerspectiveConfiguration(String id) {
		this();
		this.id = id;
	}

	@JsonIgnore
	public String getLabel() {
		int lastIndex = id.lastIndexOf(".");
		return id.substring(lastIndex + 1);
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return this.id;
	}

	public List<String> getLayout() {
		if (!this.layout.contains(DEFAULT)) {
			this.layout.add(DEFAULT);
		}
		return layout;
	}

	public void setLayout(List<String> layout) {
		this.layout = layout;
	}

	public String getSelectedLayout() {
		// if (selectedLayout == null || !this.getLayout().contains(selectedLayout)) {
		// this.setSelectedLayout(selectedLayout);
		// }
		return selectedLayout;
	}

	public void setSelectedLayout(String selectedLayout) {
		if (!this.getLayout().contains(selectedLayout)) {
			selectedLayout = DEFAULT;
		}
		firePropertyChange("selectedLayout", this.selectedLayout, this.selectedLayout = selectedLayout);
	}

	@JsonIgnore
	public void add(String layoutName) {
		if (this.layout.contains(layoutName)) {
			Output.DEBUG.info("es.alba.sweet.perspective.PerspectiveConfiguration.add", layoutName + " already in the list. It will not be added");
			return;
		}

		this.layout.add(layoutName);
		Output.DEBUG.info("es.alba.sweet.perspective.PerspectiveConfiguration.add", layoutName + " added to the list");
	}

	@JsonIgnore
	public String[] layoutToArray() {
		return this.layout.toArray(String[]::new);
	}

	@Override
	public String toString() {
		return "PerspectiveConfiguration [id=" + id + ", layout=" + layout + ", selectedLayout=" + selectedLayout + "]";
	}

}
