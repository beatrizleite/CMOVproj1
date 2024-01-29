package edu.ufp.cm.arduinoserialmonitor.usingjserialcomm;
import com.fazecast.jSerialComm.*;

public class CheckAvailablePorts {
        public static void main(String[] args) {
            SerialPort[] ports = SerialPort.getCommPorts();
            for (SerialPort port : ports) {
                System.out.println(port.getSystemPortName() + ": " + port.getPortDescription());
            }
        }

}
