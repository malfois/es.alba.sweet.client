package es.alba.sweet.client.scan;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import es.alba.sweet.base.communication.command.CommandName;
import es.alba.sweet.base.communication.command.CommandStream;
import es.alba.sweet.base.communication.command.Stop;
import es.alba.sweet.base.output.Output;
import es.alba.sweet.client.server.Server;

public class ButtonStopListener extends SelectionAdapter {
	public void widgetSelected(SelectionEvent e) {
		Scan.PROCESS.setScanState(ScanState.IDLE);
		Server.SERVER.getConnection().send(new CommandStream(CommandName.SCAN_STOPPED, new Stop().toJson()));
		Output.MESSAGE.info("es.alba.sweet.client.scan.ButtonStopListener.widgetSelected", "Scan stopped");
	}
}
