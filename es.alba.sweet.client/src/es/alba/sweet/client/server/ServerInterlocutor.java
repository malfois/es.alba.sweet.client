package es.alba.sweet.client.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import es.alba.sweet.base.ObservableProperty;
import es.alba.sweet.base.communication.command.CommandName;
import es.alba.sweet.base.communication.command.CommandStream;
import es.alba.sweet.base.communication.command.CommandStreamNullException;
import es.alba.sweet.base.communication.command.JsonException;
import es.alba.sweet.base.communication.command.Name;
import es.alba.sweet.base.constant.Application;
import es.alba.sweet.base.output.Message;
import es.alba.sweet.base.output.Output;
import es.alba.sweet.base.scan.Header;
import es.alba.sweet.base.scan.ScanDataSet;
import es.alba.sweet.client.scan.Scan;
import es.alba.sweet.client.scan.ScanState;

public class ServerInterlocutor extends ObservableProperty implements Runnable {

	private Socket			clientSocket;

	private PrintWriter		toServer;
	private BufferedReader	fromServer;

	private String			hostName;
	private int				portNumber;

	private ServerState		serverState	= ServerState.NOT_RUNNING;

	public ServerInterlocutor(String hostName, int portNumber) {
		this.hostName = hostName;
		this.portNumber = portNumber;
	}

	private void setServerState(ServerState serverState) {
		firePropertyChange("serverState", this.serverState, this.serverState = serverState);
	}

	public void connect() throws IOException {
		try {
			clientSocket = new Socket(hostName, portNumber);
			toServer = new PrintWriter(clientSocket.getOutputStream(), true);
			fromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			setServerState(ServerState.RUNNING_AND_CONNECTED);
			Output.MESSAGE.info("es.alba.sweet.client.server.ServerInterlocutor.connect", "Server connected ");
			Name name = new Name();
			name.setName(Application.SWEET.name());
			CommandStream command = new CommandStream(CommandName.NAME, name.toJson());
			send(command);
		} catch (UnknownHostException e) {
			Output.MESSAGE.error("es.alba.sweet.client.server.ServerInterlocutor.connect", e.getMessage());
		} catch (IOException e) {
			Output.MESSAGE.error("es.alba.sweet.client.server.ServerInterlocutor.connect", e.getMessage());
			throw e;
		}
	}

	public void send(CommandStream command) {
		this.toServer.println(command.toString());
	}

	protected void disconnect() {
		try {
			this.clientSocket.close();
			setServerState(ServerState.RUNNING_AND_NOT_CONNECTED);
			Thread.currentThread().interrupt();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			boolean run = true;
			while (run) {

				CommandStream commandStream = new CommandStream(fromServer.readLine());
				// if (fromServerString == null) {
				// Output.MESSAGE.info("es.alba.sweet.client.server.ServerInterlocutor.run", "No answer from SERVER - Maybe closed");
				// Output.MESSAGE.info("es.alba.sweet.client.server.ServerInterlocutor.run", "Closing the socket");
				// setServerState(ServerState.NOT_RUNNING);
				// if (this.clientSocket.isClosed()) return;
				// this.clientSocket.close();
				// Thread.currentThread().interrupt();
				// run = false;
				// return;
				// }

				CommandName command = commandStream.getCommandName();
				switch (command) {
				case MESSAGE:
					Message message = new Message(commandStream.getCommandArgument());
					Output.MESSAGE.print(message);
					break;
				case SCAN_HEADER:
					Header scanHeader = new Header(commandStream.getCommandArgument());
					Output.MESSAGE.info("es.alba.sweet.client.server.ServerInterlocutor.run", "Scan header received");
					Scan.PROCESS.initialise(scanHeader);
					break;
				case SCAN_DATA_POINT:
					ScanDataSet scanDataset = new ScanDataSet(commandStream.getCommandArgument());
					Output.MESSAGE.info("es.alba.sweet.client.server.ServerInterlocutor.run", "Scan data point received " + scanDataset.getText());
					Scan.PROCESS.addDataPoint(scanDataset);
					break;
				case SCAN_STOPPED:
					Output.MESSAGE.info("es.alba.sweet.client.server.ServerInterlocutor.run", "Scan stopped");
					Scan.PROCESS.setScanState(ScanState.IDLE);
					break;
				default:
					break;
				}
			}
		} catch (CommandStreamNullException e) {
			e.printStackTrace();
		} catch (JsonException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public ServerState getServerState() {
		return this.serverState;
	}

}
