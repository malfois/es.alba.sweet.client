package es.alba.sweet.client.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

import es.alba.sweet.base.output.AMessage;
import es.alba.sweet.base.output.Message;
import es.alba.sweet.base.output.Output;

public class StartServer implements Callable<ServerState> {

	private Process process;
	private ServerState serverState = ServerState.NOT_RUNNING;

	public StartServer(Process process) {
		this.process = process;
	}

	@Override
	public ServerState call() throws Exception {

		try {
			try (BufferedReader bri = new BufferedReader(new InputStreamReader(process.getInputStream()))) {

				String line = null;
				while ((line = bri.readLine()) != null) {
					System.out.println(line);
					Message message = AMessage.Factory(line);
					Output.MESSAGE.print(message);
					if (line.contains("Waiting for client")) {
						Output.MESSAGE.info("es.alba.sweet.client.server.StartServer.run", "Server running");
						return ServerState.RUNNING_AND_NOT_CONNECTED;
					}
				}

				// Check result
				try {
					if (process.waitFor() != 0) {
						if (!process.isAlive()) {
							serverState = ServerState.NOT_RUNNING;
						}
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					e.printStackTrace();
				}
				return serverState;
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return serverState;
	}

}
