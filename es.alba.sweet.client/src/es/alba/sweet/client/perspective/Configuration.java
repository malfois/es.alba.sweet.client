package es.alba.sweet.client.perspective;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;

import es.alba.sweet.base.configuration.AFileConfiguration;
import es.alba.sweet.base.output.Output;
import es.alba.sweet.client.core.DirectoryLocator;
import es.alba.sweet.client.core.constant.Directory;
import es.alba.sweet.client.core.constant.Id;

public class Configuration extends AFileConfiguration {

	private List<PerspectiveConfiguration>	perspectives	= new ArrayList<>();

	private String							selectedPerspectiveId;

	public Configuration() {
		super(DirectoryLocator.findPath(Directory.CONFIG).toString(), "perspectives");
		perspectives.add(new PerspectiveConfiguration(Id.SCAN_PERSPECTIVE));
	}

	public List<PerspectiveConfiguration> getPerspectives() {
		return perspectives;
	}

	public void setPerspectives(List<PerspectiveConfiguration> perspectives) {
		this.perspectives = perspectives;
	}

	public PerspectiveConfiguration getPerspective(String perspectiveId) {
		Optional<PerspectiveConfiguration> result = perspectives.stream().filter(p -> p.getId().equals(perspectiveId)).findFirst();

		if (!result.isPresent()) {
			PerspectiveConfiguration configuration = new PerspectiveConfiguration(perspectiveId);
			perspectives.add(configuration);
			return configuration;
		}
		return result.get();
	}

	public String getSelectedPerspectiveId() {
		if ((selectedPerspectiveId == null || selectedPerspectiveId.length() == 0) && !perspectives.isEmpty()) selectedPerspectiveId = perspectives.get(0).getId();
		return selectedPerspectiveId;
	}

	public void setSelectedPerspectiveId(String selectedPerspective) {
		firePropertyChange("selectedPerspectiveId", this.selectedPerspectiveId, this.selectedPerspectiveId = selectedPerspective);
	}

	@JsonIgnore
	public PerspectiveConfiguration getSelectedPerspectiveConfiguration() {
		return getPerspective(selectedPerspectiveId);
	}

	public boolean add(PerspectiveConfiguration perspective) {
		Optional<PerspectiveConfiguration> result = perspectives.stream().filter(p -> p.getId().equals(perspective.getId())).findFirst();

		if (!result.isPresent()) {
			perspectives.add(perspective);
			return true;
		}
		Output.DEBUG.error("es.alba.sweet.perspective.Configuration.add", "Perspective " + perspective.getId() + " already configured. New configuration not added");
		return false;
	}

	@JsonIgnore
	public String getSelectedPerspectiveLabel() {
		int index = this.selectedPerspectiveId.lastIndexOf("\\.");
		return this.selectedPerspectiveId.substring(index);
	}

	@Override
	public String toString() {
		return "Configuration [perspectives=" + perspectives + ", selectedPerspectiveId=" + selectedPerspectiveId + "]";
	}

}
