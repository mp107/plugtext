package raw;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.StringTokenizer;

import configs.CommandConfig;

import others.Command;
import others.CommunicatorEngine;
import utils.DebugUtil;

/**
 * Klasa bazowa dla wątku połączenia
 *
 * @author mp
 *
 */
public abstract class BasicConnectionThread extends Thread {

    private Socket socket;
    private BufferedReader input;
    private DataOutputStream output;

    /**
     *
     * @param serverAddress
     *            adres IP serwera
     * @param serverPort
     *            numer portu serera
     * @throws IOException
     *             przy wystąpieniu błędu połączenia
     */
    public BasicConnectionThread(String serverAddress, int serverPort)
            throws IOException {
        socket = new Socket(serverAddress, serverPort);
        input = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
        output = new DataOutputStream(socket.getOutputStream());
        start();
    }

    /**
     *
     * @param socket
     *            gniazdo połączenia z serwerem
     * @throws IOException
     *             przy wystąpieniu błędu połączenia
     */
    public BasicConnectionThread(Socket socket) throws IOException {
        this.socket = socket;
        input = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
        output = new DataOutputStream(socket.getOutputStream());
        start();
    }

    public void run() {
        if (CommandConfig.DEBUG_STATE)
            DebugUtil.print("Połączono " + socket.getLocalPort() + " z "
                    + socket.getPort());
        String line;
        while (!socket.isClosed()) {
            try {
                line = input.readLine();
                if (line == null) {
                    closeConnection();
                    return;
                }
                receive2(new Command(line));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                System.err.println("Błąd połączenia:");
                e.printStackTrace();
                closeConnection();
                return;
            }
        }
    }

    /**
     * Zamykanie połączenia
     */
    protected void closeConnection() {
        try {
            socket.close();
        } catch (IOException e) {
            if (CommandConfig.DEBUG_STATE) {
                DebugUtil.print("");
                e.printStackTrace();
            }
        }
    }

    /**
     * Wysyłanie komendy
     *
     * @param command
     *            komenda
     * @throws IOException
     *             przy wystąpieniu błędu połączenia
     */
    protected synchronized void send(Command command) throws IOException {
        if (CommandConfig.DEBUG_STATE)
            DebugUtil.print("Wysyłam z " + socket.getLocalPort() + " do "
                    + socket.getPort() + ": " + command);
        output.writeBytes(command.toString() + CommandConfig.COMMAND_SEPARATOR);
    }

    /**
     * Odbiór komendy
     *
     * @param command
     *            komenda
     */
    protected abstract void receive(Command command);

    private synchronized void receive2(Command command) {
        if (CommandConfig.DEBUG_STATE)
            DebugUtil.print("Odbieram z " + socket.getPort() + " na "
                    + socket.getLocalPort() + ": " + command);
        receive(command);
    }
}
