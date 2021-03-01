package es.alba.sweet.client.perspective;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.sideeffect.ISideEffect;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.Properties;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.opal.dialog.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import es.alba.sweet.base.output.Output;
import es.alba.sweet.client.EclipseUI;
import es.alba.sweet.client.core.IconLoader;
import es.alba.sweet.client.core.constant.Tag;

public class PerspectiveControl {

	@Inject
	Configuration	configuration;

	private Name	name;
	private Layout	layout;
	private Views	views;

	@PostConstruct
	public void createGui(Composite parent) {

		name = new Name(parent);
		layout = new Layout(parent, configuration.getSelectedPerspectiveConfiguration());
		views = new Views(parent);

	}

	public void addButtons(List<MPart> parts) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				views.addButtons(parts);
			}
		});
	}

	public void updateButton(MPart part, boolean visible) {
		this.views.updateButton(part, visible);
	}

	public void setDecoratorDirty() {
		this.layout.setDecoratorDirty();
	}

	private class Layout {
		private PerspectiveConfiguration	configuration;

		private ComboViewer					combo;
		private Button						loadLayout;
		private Button						saveLayout;
		private Button						saveAsLayout;
		private Button						deleteLayout;
		private Button						renameLayout;

		private ControlDecoration			decoratorChanged;

		private IObservableList<String>		input;

		private String						previousSelection;

		@SuppressWarnings("rawtypes")
		private IObservableValue			observableSelectedLayout;
		private ChangeLayoutListener		changeLayoutListener	= new ChangeLayoutListener();

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public Layout(Composite parent, PerspectiveConfiguration configuration) {
			this.configuration = configuration;

			Composite layoutComposite = new Composite(parent, SWT.NONE);
			GridLayout compositeLayout = new GridLayout();
			compositeLayout.horizontalSpacing = 10;
			compositeLayout.numColumns = 8;
			compositeLayout.marginLeft = 30;
			layoutComposite.setLayout(compositeLayout);

			Label layout = new Label(layoutComposite, SWT.NONE | SWT.BOTTOM);
			layout.setText("Layout");

			List<String> layouts = configuration.getLayout();

			combo = new ComboViewer(layoutComposite, SWT.READ_ONLY);
			combo.setContentProvider(new ObservableListContentProvider<String>());
			IListProperty<List<String>, String> objects = Properties.selfList(String.class);
			input = objects.observe(layouts);
			combo.setInput(input);
			combo.setSelection(new StructuredSelection(configuration.getSelectedLayout()));

			this.previousSelection = configuration.getSelectedLayout();

			// create the decoration for the text UI component
			decoratorChanged = new ControlDecoration(combo.getControl(), SWT.TOP | SWT.LEFT);

			// re-use an existing image
			Image image = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_WARNING).getImage();
			// set description and image
			decoratorChanged.setDescriptionText("Layout of the perspective has been changed but not saved");
			decoratorChanged.setImage(image);
			decoratorChanged.hide();

			loadLayout = new Button(layoutComposite, SWT.PUSH);
			loadLayout.setImage(IconLoader.load("load.gif").createImage());
			loadLayout.setToolTipText("Load the current layout");
			loadLayout.addSelectionListener(new LoadLayoutAdapter());

			saveLayout = new Button(layoutComposite, SWT.PUSH);
			saveLayout.setImage(IconLoader.load("save.png").createImage());
			saveLayout.setToolTipText("Save the current layout");
			saveLayout.addSelectionListener(new SaveLayoutAdapter());

			saveAsLayout = new Button(layoutComposite, SWT.PUSH);
			saveAsLayout.setImage(IconLoader.load("save_as.png").createImage());
			saveAsLayout.setToolTipText("Save the current layout as...");
			saveAsLayout.addSelectionListener(new SaveAsLayoutAdapter());

			deleteLayout = new Button(layoutComposite, SWT.PUSH);
			deleteLayout.setImage(IconLoader.load("delete.png").createImage());
			deleteLayout.setToolTipText("Delete the current layout in the list");
			deleteLayout.addSelectionListener(new DeleteLayoutAdapter());

			renameLayout = new Button(layoutComposite, SWT.PUSH);
			renameLayout.setImage(IconLoader.load("rename.png").createImage());
			renameLayout.setToolTipText("Rename the current layout");
			renameLayout.addSelectionListener(new RenameLayoutAdapter());

			// create new Context
			DataBindingContext ctx = new DataBindingContext();

			// define the IObservables
			IViewerObservableValue source = ViewerProperties.singleSelection().observe(combo);
			observableSelectedLayout = BeanProperties.value(PerspectiveConfiguration.class, "selectedLayout").observe(configuration);
			ISWTObservableValue saveButtonEnabledObservable = WidgetProperties.enabled().observe(saveLayout);
			ISWTObservableValue deleteButtonEnabledObservable = WidgetProperties.enabled().observe(deleteLayout);
			ISWTObservableValue renameButtonEnabledObservable = WidgetProperties.enabled().observe(renameLayout);
			// Create a boolean observable, which check the layout name is not default
			IObservableValue<Boolean> isNameNotDefault = ComputedValue.create(() -> {
				return !source.getValue().equals(PerspectiveConfiguration.DEFAULT);
			});

			// connect them
			ctx.bindValue(observableSelectedLayout, source);
			ctx.bindValue(saveButtonEnabledObservable, isNameNotDefault);
			ctx.bindValue(deleteButtonEnabledObservable, isNameNotDefault);
			ctx.bindValue(renameButtonEnabledObservable, isNameNotDefault);

			observableSelectedLayout.addChangeListener(changeLayoutListener);

		}

		public void setDecoratorDirty() {
			this.decoratorChanged.show();
		}

		private boolean isLayoutDirty() {
			return this.decoratorChanged.isVisible();
		}

		private void loadLayout() {
			String layoutName = configuration.getSelectedLayout();// (String) combo.getStructuredSelection().getFirstElement();

			Output.DEBUG.info("es.alba.sweet.perspective.PerspectiveControl.Layout.ChangeLayoutListener.handleChange", "Changing from " + previousSelection + " to " + layoutName);

			if (isLayoutDirty()) {
				String savedLayoutName = Perspective.SaveCurrentPerspective(previousSelection, true);

				if (savedLayoutName != null && !input.contains(savedLayoutName)) input.add(savedLayoutName);

			}

			Output.DEBUG.info("es.alba.sweet.perspective.PerspectiveControl.Layout.ChangeLayoutListener.handleChange", "Loading layout " + layoutName);

			Perspective.loadPerspective(layoutName);

			previousSelection = layoutName;
			decoratorChanged.hide();

			List<MPart> parts = EclipseUI.partService().getParts().stream().collect(Collectors.toList());
			parts.stream().forEach(a -> {
				views.updateButton(a, EclipseUI.partService().isPartVisible(a));
			});

		}

		private class ChangeLayoutListener implements IChangeListener {

			@Override
			public void handleChange(ChangeEvent event) {
				loadLayout();
			}

		}

		private class LoadLayoutAdapter extends SelectionAdapter {

			// Selecting a name in the list
			public void widgetSelected(SelectionEvent e) {
				loadLayout();
			}
		}

		private class SaveAsLayoutAdapter extends SelectionAdapter {

			// Selecting a name in the list
			public void widgetSelected(SelectionEvent e) {
				Output.DEBUG.info("es.alba.sweet.perspective.PerspectiveControl.Layout.SaveAsLayoutAdapter.widgetSelected", "Saving layout as...");

				String savedLayoutName = Perspective.SaveAsCurrentPerspective("");

				if (savedLayoutName == null) {
					Output.DEBUG.info("es.alba.sweet.perspective.PerspectiveControl.Layout.SaveAsLayoutAdapter.widgetSelected", "Cancel button selected, layout will not be saved");
					return;
				}

				decoratorChanged.hide();

				previousSelection = savedLayoutName;

				if (!input.contains(savedLayoutName)) {
					input.add(savedLayoutName);
				}
				observableSelectedLayout.removeChangeListener(changeLayoutListener);
				configuration.setSelectedLayout(savedLayoutName);
				observableSelectedLayout.addChangeListener(changeLayoutListener);
			}
		}

		private class SaveLayoutAdapter extends SelectionAdapter {

			// Selecting a name in the list
			public void widgetSelected(SelectionEvent e) {
				String selectedLayout = combo.getCombo().getText();
				Output.DEBUG.info("es.alba.sweet.perspective.PerspectiveControl.Layout.SaveLayoutAdapter.widgetSelected", "Saving layout " + selectedLayout);

				String savedLayoutName = Perspective.SaveCurrentPerspective(selectedLayout, false);

				if (savedLayoutName == null) {
					Output.DEBUG.info("es.alba.sweet.perspective.PerspectiveControl.Layout.SaveLayoutAdapter.widgetSelected", "Cancel button selected, layout will not be saved");
					return;
				}

				decoratorChanged.hide();

				previousSelection = savedLayoutName;

				if (!input.contains(savedLayoutName)) input.add(savedLayoutName);
				observableSelectedLayout.removeChangeListener(changeLayoutListener);
				configuration.setSelectedLayout(savedLayoutName);
				observableSelectedLayout.addChangeListener(changeLayoutListener);
			}
		}

		private class DeleteLayoutAdapter extends SelectionAdapter {

			// Selecting a name in the list
			public void widgetSelected(SelectionEvent e) {
				String selectedLayout = (String) combo.getStructuredSelection().getFirstElement();

				Output.DEBUG.info("es.alba.sweet.perspective.PerspectiveControl.Layout.DeleteLayoutAdapter.widgetSelected", "Deleting layout " + selectedLayout);
				DeleteLayoutChoice delete = LayoutModificationDialog.deleteLayout(selectedLayout);

				if (delete.isButtonChoice()) {
					decoratorChanged.hide();

					int index = input.indexOf(selectedLayout);

					if (index > 0) index--;
					String newSelectedLayout = input.get(index);

					configuration.setSelectedLayout(newSelectedLayout);

					input.remove(selectedLayout);

					if (delete.isDiskChoice()) {
						MPerspective activePerspective = EclipseUI.activePerspective();
						Path path = Perspective.getLayoutFilename(activePerspective.getLabel(), selectedLayout).toAbsolutePath();
						try {
							Files.delete(path);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		}

		private class RenameLayoutAdapter extends SelectionAdapter {

			// Selecting a name in the list
			public void widgetSelected(SelectionEvent e) {
				String selectedLayout = (String) combo.getStructuredSelection().getFirstElement();

				Output.DEBUG.info("es.alba.sweet.perspective.PerspectiveControl.Layout.RenameLayoutAdapter.widgetSelected", "Renaming layout " + selectedLayout);
				String newLayoutName = LayoutModificationDialog.inputDialog("");

				if (newLayoutName == null) {
					Output.DEBUG.info("es.alba.sweet.perspective.PerspectiveControl.Layout.RenameLayoutAdapter.widgetSelected", "Cancel button selected, layout will not be saved");
					return;
				}

				if (input.contains(newLayoutName)) {
					boolean confirm = Dialog.isConfirmed("The layout " + newLayoutName + " already exists.", "Do you want to overwrite it?");
					if (!confirm) {
						Output.DEBUG.info("es.alba.sweet.perspective.PerspectiveControl.Layout.RenameLayoutAdapter.widgetSelected", newLayoutName + "will not be overwriten");
						return;
					}
				}

				decoratorChanged.hide();

				previousSelection = newLayoutName;

				observableSelectedLayout.removeChangeListener(changeLayoutListener);

				int index = input.indexOf(selectedLayout);
				input.add(index, newLayoutName);
				configuration.setSelectedLayout(newLayoutName);
				input.remove(selectedLayout);
				observableSelectedLayout.addChangeListener(changeLayoutListener);

			}
		}

	}

	private class Views {

		private GridLayout		gridLayout	= new GridLayout();
		private Composite		viewComposite;

		private EPartService	partService	= EclipseUI.partService();

		public Views(Composite parent) {

			viewComposite = new Composite(parent, SWT.NONE);
			gridLayout.horizontalSpacing = 0;
			gridLayout.numColumns = 1;
			gridLayout.marginLeft = 30;
			viewComposite.setLayout(gridLayout);

			Label layout = new Label(viewComposite, SWT.NONE);
			layout.setText("Views");
		}

		public void updateButton(MPart part, boolean visible) {
			Output.DEBUG.info("es.alba.sweet.perspective.PerspectiveControl.Views.updateButton", "updating button image from part " + part.getLabel());

			List<Button> buttons = List.of(viewComposite.getChildren()).stream().filter(p -> (p instanceof Button)).map(m -> (Button) m).collect(Collectors.toList());
			if (buttons.isEmpty()) {
				Output.DEBUG.info("es.alba.sweet.perspective.PerspectiveControl.Views.updateButton", "No button yet in the toolbar - No update possible");
				return;
			}

			Button button = buttons.stream().filter(p -> new ToolTipText(p.getToolTipText()).getLabel().equals(part.getLabel())).findFirst().orElse(null);
			if (button == null) {
				Output.DEBUG.info("es.alba.sweet.perspective.PerspectiveControl.Views.updateButton", "No button with label " + part.getLabel() + " found");
				return;
			}

			ImageDescriptor descriptor = IconLoader.loadFromURI(part.getIconURI());
			Image image = descriptor.createImage();
			image = visible ? image : new Image(Display.getCurrent(), image, SWT.IMAGE_GRAY);
			button.setImage(image);
			button.setToolTipText(updateToolTipText(part, visible));
			button.setSelection(visible);

			Output.DEBUG.info("es.alba.sweet.client.perspective.PerspectiveControl.Views.updateButton", "Button image updated from part " + part.getElementId());

		}

		public void addButtons(List<MPart> parts) {
			List.of(viewComposite.getChildren()).stream().filter(p -> (p instanceof Button)).forEach(a -> a.dispose());

			List<MPart> renderParts = parts.stream().filter(f -> f.getTags().contains(Tag.RELEASE)).collect(Collectors.toList());
			gridLayout.numColumns = renderParts.size() + 1;

			for (MPart part : renderParts) {
				Button button = new Button(viewComposite, SWT.TOGGLE);

				button.setToolTipText(updateToolTipText(part, partService.isPartVisible(part)));

				ImageDescriptor descriptor = IconLoader.loadFromURI(part.getIconURI());
				Image image = descriptor.createImage();
				button.setImage(image);

				if (partService.isPartVisible(part)) {
					button.setSelection(true);
				}

				button.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
					boolean visible = partService.isPartVisible(part);
					if (!visible) {
						// partService.activate(part);
						partService.showPart(part, PartState.VISIBLE);
						partService.bringToTop(part);
						Output.DEBUG.info("es.alba.sweet.perspective.PerspectiveControl.Views.addButtons", "View " + part.getLabel() + " visible");
					} else {
						partService.hidePart(part);
						Output.DEBUG.info("es.alba.sweet.perspective.PerspectiveControl.Views.addButtons", "View " + part.getLabel() + " non visible");
					}
				}));

			}
			viewComposite.pack();

		}

		private String updateToolTipText(MPart part, boolean visible) {
			String label = part.getLabel();
			String comment = visible ? "Part visible - Click for closing the part" : "Part non visible - Click for activating the part";
			return new ToolTipText(label, comment).toString();

		}

	}

	private class Name {

		private Label	nameLabel;
		private Label	imageLabel;

		@SuppressWarnings({ "rawtypes" })
		public Name(Composite parent) {
			// MPerspective activePerspective = EclipseUI.activePerspective();

			Composite nameComposite = new Composite(parent, SWT.NONE);
			GridLayout nameLayout = new GridLayout();
			nameLayout.horizontalSpacing = 5;
			nameLayout.numColumns = 3;
			nameComposite.setLayout(nameLayout);

			Label perspectiveLabel = new Label(nameComposite, SWT.NONE);
			perspectiveLabel.setText("Current Perspective:");

			nameLabel = new Label(nameComposite, SWT.NONE);

			imageLabel = new Label(nameComposite, SWT.NONE);
			GridData imageGridData = new GridData();
			imageGridData.widthHint = 20;
			imageLabel.setLayoutData(imageGridData);

			IObservableValue model = BeanProperties.value(Configuration.class, "selectedPerspectiveId").observe(configuration);

			// Bind the perspective id to the Label
			ISideEffect.create(() -> {
				nameLabel.setText(getLabel((String) model.getValue()));
			});

			// Bind the perspective id to the image
			ISideEffect.create(() -> {
				String label = getLabel((String) model.getValue());
				String iconURI = "platform:/plugin/es.alba.sweet.client/icons/" + label + ".png";
				Image image = IconLoader.loadFromURI(iconURI).createImage();
				imageLabel.setImage(image);
			});
		}

		private String getLabel(String perspectiveId) {
			int index = perspectiveId.lastIndexOf(".");
			return perspectiveId.substring(index + 1);
		}
	}
}
