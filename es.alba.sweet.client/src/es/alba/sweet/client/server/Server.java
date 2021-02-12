package es.alba.sweet.client.server;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.core.databinding.observable.value.WritableValue;

import es.alba.sweet.base.configuration.Json;
import es.alba.sweet.base.constant.CommandLine;
import es.alba.sweet.base.output.Output;
import es.alba.sweet.communication.Communication;

public enum Server {

	SERVER;

	private ServerInterlocutor connection;

	private Json<Communication> json = new Json<>(new Communication());

	private Server() {

	}

	public void connect() {
		File jsonFile = json.getFile();
		if (!jsonFile.exists()) {
			String path = jsonFile.getPath().toString();
			Output.MESSAGE.error("es.alba.sweet.client.server.Server.connect",
					"File " + path + " containing information about communication to the server NOT FOUND");
			Output.MESSAGE.error("es.alba.sweet.client.server.Server.connect", "Connection to the server: None");
		} else {
			String filename = json.getFile().getPath().toString();
			Output.MESSAGE.info("es.alba.sweet.client.server.Server.connect",
					"File containing information about communication to the server: " + filename);
			json.read();

			if (this.isServerRunning()) {
				this.connection();
				return;
			}
		}
		this.start();
		this.json.read();
		this.connection();
	}

	private Boolean isServerRunning() {
		System.out.println(json.getFile().toPath());
		if (Files.notExists(json.getFile().toPath())) {
			Output.MESSAGE.warning("es.alba.sweet.client.server.Server.isServerRunning",
					"File does not exist. Assuming the server is not running");
			return false;
		}
		// If the file exists, check the port and hostname are valid
		Communication configuration = json.getConfiguration();
		if (!configuration.isValid()) {
			Output.MESSAGE.warning("es.alba.sweet.client.server.Server.isServerRunning",
					"Hostname or/and port not valid");
			return false;
		}

		return true;
	}

	private Boolean sameNetwork(String serverHostName) {
		try {
			InetAddress thisPC = InetAddress.getLocalHost();
			byte[] pcBytes = thisPC.getAddress();
			InetAddress serverPC = InetAddress.getByName(serverHostName);
			byte[] serverBytes = serverPC.getAddress();
			if (pcBytes[2] != serverBytes[2]) {
				Output.MESSAGE.error("es.alba.sweet.client.server.Server.sameNetwork",
						"Server NOT reachable. Not on the same network");
				return false;
			}
			return true;
		} catch (UnknownHostException ex) {
			Output.MESSAGE.error("es.alba.sweet.client.server.Server.sameNetwork", "Hostname Not resolved");
			return false;
		}
	}

	public void start() {
		Output.MESSAGE.info("es.alba.sweet.client.server.Server.start", "Starting the server");
		List<String> commandLine = CommandLine.SERVER.get();
		System.out.println(commandLine);
		ProcessBuilder pb = new ProcessBuilder(commandLine);
		try {
			Process process = pb.start();
			StartServer startServer = new StartServer(process);
			Future<ServerState> future = Executors.newSingleThreadExecutor().submit(startServer);

			Output.DEBUG.info("es.alba.sweet.client.server.Server.start", future.get().getDescription());

		} catch (IOException e) {
			// Message.Log(ApplicationName.SWEET, NAME, e);
			e.printStackTrace();
		} catch (InterruptedException e) {
			// Message.Log(ApplicationName.SWEET, NAME, e);
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// public void startAndConnect() {
	// this.start();
	// this.connect();
	// }

	public void send(String text) {
		if (this.connection == null) {
			Output.MESSAGE.error("es.alba.sweet.client.server.Server.send", "No connection to the server");
			return;
		}
		this.connection.send(text);
	}

	public WritableValue<ServerState> getObservableServerState() {
		return this.connection.getObservableServerState();
	}

	// public void stop() {
	// Output.MESSAGE.info("es.alba.sweet.client.server.Server.stop", "Stopping the
	// server");
	// JsonText<Stop> jsonText = new JsonText<>(Command.STOP, new Stop());
	// send(jsonText.toString());
	// serverState = ServerState.NOT_RUNNING;
	// Output.MESSAGE.info("es.alba.sweet.client.server.Server.stop", "Server
	// stopped");
	// }

	public void connection() {
		Communication connection = json.getConfiguration();
		if (!this.sameNetwork(connection.getHostName())) {
			Output.MESSAGE.warning("es.alba.sweet.client.server.Server.connection",
					"Server and client not on the same network. No connection to the server possible");
			return;
		}

		try {
			this.connection = new ServerInterlocutor(connection.getHostName(), connection.getPort());
		} catch (IOException e) {
			// this.startAndConnect();
			return;
		}
		Thread connectionThread = new Thread(this.connection);
		connectionThread.setDaemon(true);
		connectionThread.start();

	}

	public void disconnect() {
		this.connection.disconnect();
		this.closeConnection();
	}

	public void closeConnection() {
		this.connection.disconnect();
		this.connection = null;
	}

}
