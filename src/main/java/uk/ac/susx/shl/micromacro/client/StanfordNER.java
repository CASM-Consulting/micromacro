package uk.ac.susx.shl.micromacro.client;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Map;

public class StanfordNER {

    private static Charset charset = Charset.forName("UTF-8");

    private final int port;


    public StanfordNER(int port) {
        this.port = port;
    }

    public String get(String input) {

        try (
                Socket socket = new Socket("127.0.0.1", port);
                PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), charset), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), charset))
        ) {
            // send material to NER to socket
            out.println(input);
            // Print the results of NER
            String result;

            StringBuilder sb = new StringBuilder();
            while ((result = in.readLine()) != null) {

                sb.append(result);
                sb.append("\n");
            }

            return  sb.toString();
        } catch (IOException e) {

            System.err.println("I/O error in the connection to: ");

            throw new RuntimeException(e);
        }
    }

}
