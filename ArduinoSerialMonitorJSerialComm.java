package edu.ufp.cm.arduinoserialmonitor.usingjserialcomm;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortTimeoutException;

import java.awt.event.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class ArduinoSerialMonitorJSerialComm implements WindowListener, ActionListener {
    private SerialPort serialPort;

    private OutputStream outputStream;
    private InputStream inputStream;
    private BufferedReader bufferedReader;

    private volatile boolean running = true;

    private static final String THINGSPEAK_API_KEY = "FZ0WCHSG89EIZJT5";
    private static final String THINGSPEAK_API_URL = "https://api.thingspeak.com/update";


    public static void main(String[] args) {
        ArduinoSerialMonitorJSerialComm monitor = new ArduinoSerialMonitorJSerialComm();
        monitor.initialize();
    }

    public void initialize() {
        SerialPort[] ports = SerialPort.getCommPorts();

        for (SerialPort port : ports) {
            if (port.getSystemPortName().equals("COM4")) {
                serialPort = port;
                break;
            }
        }

        if (serialPort != null) {
            try {
                serialPort.openPort();
                serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 0);

                outputStream = serialPort.getOutputStream();
                inputStream = serialPort.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                Thread readThread = new Thread(() -> {
                    while (running) {
                        try {
                            String line = readLineWithRetry();
                            if (line != null) {
                                processSerialData(line);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                readThread.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendToThingSpeak(int pplCount) {
        try {
            // Construct ThingSpeak API URL
            String urlString = THINGSPEAK_API_URL + "?api_key=" + THINGSPEAK_API_KEY + "&field1=" + pplCount;

            // Create HTTP connection
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Get the response code
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Data sent to ThingSpeak successfully. People Count: " + pplCount);
            } else {
                System.err.println("Failed to send data to ThingSpeak. Response code: " + responseCode);
            }

            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readLineWithRetry() throws IOException {
        StringBuilder sb = new StringBuilder();
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < 5000) {  // Set a timeout for 5 seconds
            try {
                String line = bufferedReader.readLine();
                if (line != null) {
                    sb.append(line);
                    break; // Exit the loop once a complete line is received
                }
            } catch (SerialPortTimeoutException e) {
                System.err.println("Timeout occurred while reading from the serial port. Retrying...");
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    private void processSerialData(String data) {
        try {
            String[] parts = data.split("=");
            if (parts.length == 2) {
                int pplCount = Integer.parseInt(parts[1]);
                sendToThingSpeak(pplCount);
            } else {
                System.err.println("Invalid data format: \"" + data + "\"");
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid data received: \"" + data + "\"");
        }
    }

    public void close() {
        running = false;
        try {
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (bufferedReader != null) bufferedReader.close();
            if (serialPort != null) serialPort.closePort();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Handle action events here if needed
    }

    @Override
    public void windowOpened(WindowEvent e) {}

    @Override
    public void windowClosing(WindowEvent e) {
        close();
        System.exit(0);
    }

    @Override
    public void windowClosed(WindowEvent e) {
        close();
        System.exit(0);
    }

    @Override
    public void windowIconified(WindowEvent e) {}

    @Override
    public void windowDeiconified(WindowEvent e) {}

    @Override
    public void windowActivated(WindowEvent e) {}

    @Override
    public void windowDeactivated(WindowEvent e) {}
}