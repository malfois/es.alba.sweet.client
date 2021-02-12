package es.alba.sweet.client.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.eclipse.core.databinding.observable.value.WritableValue;

import es.alba.sweet.base.constant.Application;
import es.alba.sweet.base.output.Output;
import es.alba.sweet.communication.command.Command;
import es.alba.sweet.communication.command.Information;
import es.alba.sweet.communication.command.JsonText;
import es.alba.sweet.communication.command.WordArgument;
import es.alba.sweet.server.DefaultRealm;

public class ServerInterlocutor implements Runnable {

	private Socket clientSocket;

	private PrintWriter toServer;
	private BufferedReader fromServer;
	private String fromServerString;

	private String hostName;
	private int portNumber;

	private WritableValue<ServerState> serverState = new WritableValue<>(new DefaultRealm(), ServerState.NOT_RUNNING, null);

	public ServerInterlocutor(String hostName, int portNumber) throws IOException {
		this.hostName = hostName;
		this.portNumber = portNumber;
		this.connect();
	}

	public void connect() throws IOException {
		try {
			clientSocket = new Socket(hostName, portNumber);
			toServer = new PrintWriter(clientSocket.getOutputStream(), true);
			fromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			serverState.setValue(ServerState.RUNNING_AND_CONNECTED);
			Output.MESSAGE.info("es.alba.sweet.client.server.ServerInterlocutor.connect", "Server connected ");
			JsonText<WordArgument> jsonText = new JsonText<>(Command.NAME, new WordArgument(Application.SWEET.name()));
			send(jsonText.toString());

		} catch (UnknownHostException e) {
			Output.MESSAGE.error("es.alba.sweet.client.server.ServerInterlocutor.connect", e.getMessage());
		} catch (IOException e) {
			Output.MESSAGE.error("es.alba.sweet.client.server.ServerInterlocutor.connect", e.getMessage());
			throw e;
		}
	}

	protected void send(String text) {
		this.toServer.println(text);
	}

	public WritableValue<ServerState> getObservableServerState() {
		return this.serverState;
	}

	protected void disconnect() {
		try {
			this.clientSocket.close();
			serverState.setValue(ServerState.RUNNING_AND_NOT_CONNECTED);
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

				fromServerString = fromServer.readLine();
				if (fromServerString == null) {
					Output.MESSAGE.info("es.alba.sweet.client.server.ServerInterlocutor.run", "No answer from SERVER - Maybe closed");
					Output.MESSAGE.info("es.alba.sweet.client.server.ServerInterlocutor.run", "Closing the socket");
					serverState.setValue(ServerState.NOT_RUNNING);
					if (this.clientSocket.isClosed())
						return;
					this.clientSocket.close();
					Thread.currentThread().interrupt();
					run = false;
					return;
				}
				System.out.println("RECEIVED from SERVER - " + fromServerString);
				Command command = JsonText.getCommand(fromServerString);
				switch (command) {
				case INFO:
					JsonText<Information> jsonInfo = new JsonText<>(command, new Information());
					jsonInfo.createObject(fromServerString);
					Output.MESSAGE.info("es.alba.sweet.client.server.ServerInterlocutor.run", jsonInfo.getArgument().getInfo());
					break;
				default:
					break;
				}
			}
		} catch (IOException e) {
			serverState.setValue(ServerState.NOT_RUNNING);
			Output.MESSAGE.info("es.alba.sweet.client.server.ServerInterlocutor.run", "Connection to Server closed");
		}

	}

	// private Boolean action(Message message) {
	// try {
	// Boolean run = true;
	// MessageKey type = message.getKey();
	// switch (type) {
	// case NAME:
	// Message.Print(message.getTo(), message.getFrom(), message.getTo(),
	// message.getValue(), "connected");
	// break;
	// case HOSTNAME:
	// Communication.XML.getConfiguration().setHostName(message.getValue());
	// break;
	// case PORT:
	// Communication.XML.getConfiguration().setPort(Integer.parseInt(message.getValue()));
	// break;
	// case STOP:
	// if (this.clientSocket.isClosed()) return false;
	//
	// this.clientSocket.close();
	// closeConnection();
	// run = false;
	// break;
	// default:
	// break;
	// }
	// return run;
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// return true;
	// }

}
