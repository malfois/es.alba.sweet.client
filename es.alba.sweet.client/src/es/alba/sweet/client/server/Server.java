package es.alba.sweet.client.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import es.alba.sweet.base.configuration.Json;
import es.alba.sweet.base.output.Output;
import es.alba.sweet.server.Communication;
//import javafx.beans.property.ObjectProperty;
//import sweet.file.configuration.Communication;
//import sweet.file.configuration.Jre;
//import sweet.system.message.ApplicationName;
//import sweet.system.message.MessageKey;

public enum Server {

	SERVER;

	private Connection			connection;
	private Thread				connectionThread;

	private ServerState			serverState	= ServerState.NOT_RUNNING;

	private Json<Communication>	json		= new Json<>(new Communication());

	private Server() {
	}

	public void connect() {
		File jsonFile = json.getFile();
		if (!jsonFile.exists()) {
			String path = jsonFile.getPath().toString();
			Output.MESSAGE.error("es.alba.sweet.client.server.Server.connect", "File " + path + " containing information about communication to the server NOT FOUND");
			Output.MESSAGE.error("es.alba.sweet.client.server.Server.connect", "Connection to the server: None");
		} else {
			String filename = json.getFile().getPath().toString();
			Output.MESSAGE.info("es.alba.sweet.client.server.Server.connect", "File containing information about communication to the server: " + filename);
			json.read();

			if (this.isServerRunning()) {
				this.connection();
				return;
			}
		}
		this.startAndConnect();
	}

	private Boolean isServerRunning() {
		// If port or host name is not valid, a new server must be started
		Communication configuration = json.getConfiguration();
		if (!configuration.isValid()) {
			Output.MESSAGE.warning("es.alba.sweet.client.server.Server.isServerRunning", "Hostname or/and port not valid");
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
				Message.Log(ApplicationName.SWEET, NAME, "Server", "Not reachable");
				return false;
			}
			return true;
		} catch (UnknownHostException ex) {
			Message.Log(ApplicationName.SWEET, NAME, "Hostname", "Not resolved");
			return false;
		}
	}

	public void start() {
		Message.Log(ApplicationName.SWEET, NAME, "Starting", "the server");
		Jre.XML.read();
		Jre jre = Jre.XML.getConfiguration();
		Message.Log(ApplicationName.SWEET, NAME, "jre file", jre.getJre().getPath().normalize().toAbsolutePath().toString());
		Message.Log(ApplicationName.SWEET, NAME, "jar file", jre.getJar().getPath().normalize().toAbsolutePath().toString());

		ProcessBuilder pb = new ProcessBuilder(jre.getCommandLineArrays());
		try {
			Process process = pb.start();
			StartServer startServer = new StartServer(process);
			Thread startServerThread = new Thread(startServer);
			startServerThread.setDaemon(true);
			startServerThread.start();
			while (serverState.get().equals(ServerState.NOT_RUNNING)) {
				Thread.sleep(100);
			}
			Communication.XML.read();
		} catch (IOException e) {
			Message.Log(ApplicationName.SWEET, NAME, e);
		} catch (InterruptedException e) {
			Message.Log(ApplicationName.SWEET, NAME, e);
		}
	}

	public void startAndConnect() {
		this.start();
		this.connect();
	}

	public void send(String text) {
		if (this.connection == null) {
			Message.Log(ApplicationName.SWEET, NAME, "Server", "No connection");
			return;
		}
		this.connection.send(text);
	}

	public void stop() {
		Message.Log(ApplicationName.SWEET, NAME, "server", "stopped");
		Message message = new Message(ApplicationName.SWEET, ApplicationName.SWEET, ApplicationName.SERVER, MessageKey.STOP, null);
		send(message.toText());
		serverState.set(ServerState.NOT_RUNNING);
	}

	public void connection() {
		Communication connection = Communication.XML.getConfiguration();
		if (!this.sameNetwork(connection.getHostName())) return;

		try {
			this.connection = new Connection(connection.getHostName(), connection.getPort());
		} catch (IOException e) {
			this.startAndConnect();
			return;
		}
		connectionThread = new Thread(this.connection);
		connectionThread.setDaemon(true);
		connectionThread.start();
	}

	public void disconnect() {
		this.connection.disconnect();
		this.closeConnection();
	}

	public void closeConnection() {
		this.connectionThread.interrupt();
		this.connection = null;
	}

	private class Connection implements Runnable {

		private Socket			clientSocket;

		private PrintWriter		toServer;
		private BufferedReader	fromServer;
		private String			fromServerString;

		private String			hostName;
		private int				portNumber;

		public Connection(String hostName, int portNumber) throws IOException {
			this.hostName = hostName;
			this.portNumber = portNumber;
			this.connect();
		}

		public void connect() throws IOException {
			try {
				clientSocket = new Socket(hostName, portNumber);
				toServer = new PrintWriter(clientSocket.getOutputStream(), true);
				fromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				serverState.set(ServerState.RUNNING_AND_CONNECTED);
				Message.Log(ApplicationName.SWEET, NAME, "Server", "Connected ");
				send(new Message(ApplicationName.SWEET, ApplicationName.SWEET, ApplicationName.SERVER, MessageKey.NAME, NAME.name()).toText());

			} catch (UnknownHostException e) {
				Message.Log(ApplicationName.SWEET, NAME, e);
			} catch (IOException e) {
				Message.Log(ApplicationName.SWEET, NAME, e);
				throw e;
			}
		}

		protected void send(String text) {
			this.toServer.println(text);
		}

		protected void disconnect() {
			try {
				this.clientSocket.close();
				serverState.set(ServerState.RUNNING_AND_NOT_CONNECTED);
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
					Message message = Message.Factory(fromServerString);
					if (fromServerString == null) {
						if (this.clientSocket.isClosed()) return;
						this.clientSocket.close();
						closeConnection();
						run = false;
						return;
					}
					ApplicationName name = message.getTo();
					switch (name) {
					case SCAN:
						Message answer = Scan.getInstance().setMessage(message);
						if (answer != null) this.send(answer.toText());
						break;
					case SWEET:
						action(message);
					default:
						break;
					}
				}
			} catch (IOException e) {
				Message.Print(ApplicationName.SWEET, ApplicationName.SWEET, ApplicationName.SWEET, "Connection to Server", "closed");
			}
		}

		private Boolean action(Message message) {
			try {
				Boolean run = true;
				MessageKey type = message.getKey();
				switch (type) {
				case NAME:
					Message.Print(message.getTo(), message.getFrom(), message.getTo(), message.getValue(), "connected");
					break;
				case HOSTNAME:
					Communication.XML.getConfiguration().setHostName(message.getValue());
					break;
				case PORT:
					Communication.XML.getConfiguration().setPort(Integer.parseInt(message.getValue()));
					break;
				case STOP:
					if (this.clientSocket.isClosed()) return false;

					this.clientSocket.close();
					closeConnection();
					run = false;
					break;
				default:
					break;
				}
				return run;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}

	}

	private class StartServer implements Runnable {

		private Process process;

		public StartServer(Process process) {
			this.process = process;
		}

		@Override
		public void run() {
			try {
				try (BufferedReader bri = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
					String line = null;

					while ((line = bri.readLine()) != null) {
						System.out.println(line);
						if (line.contains("Waiting for client")) {
							Message.Log(ApplicationName.SWEET, NAME, "Server", "running");
							serverState.set(ServerState.RUNNING_AND_NOT_CONNECTED);
						}
					}

					// Check result
					try {
						if (process.waitFor() != 0) {
							if (!process.isAlive()) {
								serverState.set(ServerState.NOT_RUNNING);
							}
						}
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						e.printStackTrace();
					}

				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}

		}

	}

}
