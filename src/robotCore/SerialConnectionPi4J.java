package robotCore;

import java.io.IOException;

import com.pi4j.io.serial.Baud;
import com.pi4j.io.serial.DataBits;
import com.pi4j.io.serial.FlowControl;
import com.pi4j.io.serial.Parity;
import com.pi4j.io.serial.RaspberryPiSerial;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialConfig;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPort;
import com.pi4j.io.serial.StopBits;
import com.pi4j.system.SystemInfo;

public class SerialConnectionPi4J implements SerialConnection {


    SerialConfig config = new SerialConfig();
    private Serial m_serial;
    SerialConnection.DataReceived m_receiver;

    @Override
    public void open(SerialConnection.DataReceived receiver) throws UnsupportedOperationException, IOException, InterruptedException
    {
        String port;

        m_serial = SerialFactory.createInstance();
        m_receiver = receiver;
	
	    // create and register the serial data listener
	    m_serial.addListener(new SerialDataEventListener() {
            @Override
	        public void dataReceived(SerialDataEvent event) 
	        {
	        	try 
	        	{
		        	String data = event.getAsciiString();
		        	
//		        	Logger.Log("ArduinoConnection", 1, String.format("d='%s'", data));
		        	
		            // System.out.print(data);
		        	
					m_receiver.received(data);
				} 
	        	catch (IOException e) 
	        	{
					e.printStackTrace();
				}
	        }            
	    });


        if (SystemInfo.getBoardType() == SystemInfo.BoardType.RaspberryPi_ZeroW)
        {
            port = RaspberryPiSerial.S0_COM_PORT;
        }
        else
        {
            port	= SerialPort.getDefaultPort();
        }
        
        Logger.log("ArduinoConnection", -1, String.format("SerialConfig: port = %s", port));

        config.device(port)	//SerialPort.getDefaultPort())
            .baud(Baud._115200)
            .dataBits(DataBits._8)
            .parity(Parity.NONE)
            .stopBits(StopBits._1)
            .flowControl(FlowControl.NONE);
        
        // open the default serial device/port with the configuration settings
        m_serial.open(config);
    }

    public void writeln(String data) throws IllegalStateException, IOException
    {
        m_serial.writeln(data);
    }
}
