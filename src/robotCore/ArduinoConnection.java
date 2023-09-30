/*
 *	  Copyright (C) 2016  John H. Gaby
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, version 3 of the License.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *    
 *    Contact: robotics@gabysoft.com
 */

package robotCore;

import java.io.IOException;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;


public class ArduinoConnection 
{
	final static boolean PCTest = false;
	
	public enum ProcessorType
	{
		/** 
		 *  Processor type is currently unknown
		 */
		Unknown,
		
		/** 
		 *  Processor is an Arduino Nano
		 */
		Arduino,

		/** 
		 *  Processor is a STM32
		 */
		STM32
	}
	
	private ProcessorType m_type = ProcessorType.Unknown;
//	private Serial m_serial;
	private SerialConnection m_serialConnection;
	private static ArduinoConnection m_arduino;
	private String m_command = "";
	
	@SuppressWarnings("unused")
	private int m_maxAnalogInputs = 0;
	private int m_maxDigitalInputs = 0;
	private int m_maxMotors = 0;
	private int m_maxAnalogEncoders = 0;
	private int m_maxQuadEncoders = 0;
	private int m_maxLEDStrings = 0;
	// private int m_maxMotionProfile = 0;
	
	@SuppressWarnings("unused")
	private int m_pins = 0;
	private Timer m_pingTimer = new Timer();
	private int[] m_analog = new int[8];

	private final GpioController m_gpio;
    private final GpioPinDigitalOutput m_resetPin;
	
	//! @cond PRIVATE 
	public enum PinMode
	{
		Input,
		Output
	};
	//! @endcond
	
	private ArduinoConnection()
	{
		if (!PCTest)
		{
			m_gpio = GpioFactory.getInstance();
		    m_resetPin = m_gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "Reset", PinState.HIGH);
		}
		else
		{
			m_gpio = null;
			m_resetPin = null;
		}
	}
	
	/** 
	 *  @return Returns the type of processor. Currently Arduino Nano and STM32 are supported.
	 */
	public ProcessorType getProcessorType()
	{
		return(m_type);
	}
	
	/** 
	 *  @return Returns the maximum number of motors that are supported.
	 */
	public int getMaxMotors()
	{
		return(m_maxMotors);
	}
	
	/** 
	 *  @return Returns the maximum number of analog encoders that are supported.
	 */
	public int getMaxAnalogEncoders()
	{
		return(m_maxAnalogEncoders);
	}

	/** 
	 *  @return Returns the maximum number of quadrature encoders that are supported.
	 */
	public int getMaxQuadEncoders()
	{
		return(m_maxQuadEncoders);
	}
	
	/** 
	 *  @return Returns the maximum number of digital inputs that are supported.
	 */
	public int getMaxDigitalInputs()
	{
		return(m_maxDigitalInputs);
	}
	
	/** 
	 *  @return Returns the maximum length of LED strings.
	 */
	public int getMaxLEDStrings()
	{
		return(m_maxLEDStrings);
	}
	
	// /** 
	//  *  @return Returns the size of the motion profile buffer.
	//  */
	// private int GetMaxMotionProfile()
	// {
	// 	return(m_maxMotionProfile);
	// }
	
	private void waitForReset()
	{
    	Logger.log("RobotBase", 0, "Waiting for response");
        
        while (m_type == ProcessorType.Unknown)
        {
        	RobotBase.sleep(100);
        }
        
        Logger.log("RobotBase", 0, "Processor type = " + m_type);
        
        RobotBase.sleep(1000);
	}

	private void resetArduino()
	{
		Logger.log("RobotBase", 2, "ResetArduino");
		
		if (!PCTest)
		{
			m_resetPin.setMode(com.pi4j.io.gpio.PinMode.DIGITAL_OUTPUT);
	        m_resetPin.low();
	        RobotBase.sleep(10);
	        m_resetPin.high();
	        RobotBase.sleep(10);
			m_resetPin.setMode(com.pi4j.io.gpio.PinMode.DIGITAL_INPUT);
	        
	        waitForReset();
		}
	}
		
	public static ArduinoConnection getInstance()
	{
		if (m_arduino == null)
		{
			m_arduino	= new ArduinoConnection();
			
			try
			{
				m_arduino.start();
				m_arduino.setTimeout(1500);
			}
			catch (InterruptedException ex)
			{
				
			}
		}
		
		return(m_arduino);
	}
	
/*	Not used anymore?
 * 
	public void ReadDigitalFromPin(int pin)
	{
		SendCommand("rd " + pin);
	}
*/	
	
	//! @cond PRIVATE 
	public void readAnalogFromPin(int pin)
	{
		sendCommand("ra " + pin);
	}
	
	public void enableServo(int servo, int pin)
	{
		sendCommand("se " + servo + " " + pin);
	}
	
	public void setServo(int servo, int ms)
	{
		sendCommand("ss " + servo + " " + ms);
	}
	
	// public void EnableRGBLed(int pin, int count)
	// {
	// 	sendCommand(String.format("li %d %d", pin, count));
	// }
	
	// public void SetRGBLedColors(int red, int green, int blue)
	// {
	// 	sendCommand(String.format("ls %d %d %d", red, green, blue));
	// }
	
	public void ping()
	{
		m_pingTimer.reset();
		sendCommand("z");
	}
	
	public int getAnalogPin(int pin)
	{
		if ((pin >= 0) && (pin < m_analog.length))
		{
			synchronized(this)
			{
				return(m_analog[pin]);
			}
		}
		
		return(0);
	}	
	
	// private void ReadGyro(String args)
	// {
	// 	int a[] = RobotBase.ParseIntegers(args, 3);
		
	// 	if (a != null)
	// 	{
	// 		Gyro.SetData(a[0], a[1], a[2]);
	// 	}
	// }
	//! @endcond
	
/* not used anymore?	
	private void ReadDigital(String command)
	{
		int[]	args = RobotBase.ParseIntegers(command,  1);
		
//		System.out.println("ReadDigital: " + command);
		
		if (args != null)
		{
			synchronized(this)
			{
				m_pins	= args[0];
			}
		}
	}
*/
	
//	private void ReadAnalog(String command)
//	{
//		ArrayList<Integer>	args = RobotBase.ParseIntegers(command);
//		
//		while (args.size() >= 2)
//		{
//			int	pin	= args.get(0).intValue();
//			args.remove(0);
//			int value = args.get(0).intValue();
//			args.remove(0);
//			
//			if ((pin >= 0) && (pin < m_analog.length))
//			{
//				m_analog[pin]	= value;
//			}
//		}
//	}

/* Not used anymore?	
	public boolean GetPinState(int pin)
	{
		synchronized(this)
		{
			return((m_pins & (1 << pin)) != 0);
		}
	}
*/
	
/*	private void ReadIREncoder(String args)
	{
		int a[]	= Robot.ParseIntegers(args, 3);
		
		if (a != null)
		{
			IREncoder.SetData(a[0], a[1], a[2]);
		}
	}*/
	
	private void processorTypeCommand(String args)
	{
		if (args.length() >= 1)
		{
			switch (args.charAt(0))
			{
			case 'a':
				m_type = ProcessorType.Arduino;
				break;
				
			case 's':
				m_type = ProcessorType.STM32;
				break;
				
			default:
				m_type = ProcessorType.Unknown;
				Logger.log("ArduinoConnection", 3, String.format("Invalid type: '%c'", args.charAt(0)));
				break;
			}
			
			Logger.log("ArduinoConnection", 0, "type: " + m_type);
			
		}
	}
	
	private int readMaxCommand(String args)
	{
		int a[] = RobotBase.parseIntegers(args,  1);
		
		if (a == null)
		{
			return(0);
		}
		
		return(a[0]);
	}
	
	private void infoCommand(String command)
	{
		if (command.length() >= 1)
		{
			String args = command.substring(1).trim();
			
			switch (command.charAt(0))
			{
			case 't':
				processorTypeCommand(args);
				break;
				
			case 'a':	// # analog inputs
				m_maxAnalogInputs = readMaxCommand(args);
				break;
				
			case 'd':	// # digital inputs
				m_maxDigitalInputs = readMaxCommand(args);
				break;
				
			case 'm':	// # motors
				m_maxMotors = readMaxCommand(args);
				break;
				
			case 'e':	// # encoders
				if (command.charAt(1) == 'a')
				{
					m_maxAnalogEncoders = readMaxCommand(args.substring(1).trim());
				}
				else if (command.charAt(1) == 'q')
				{
					m_maxQuadEncoders = readMaxCommand(args.substring(1).trim());
				}
				break;
				
			case 'l':	// # LED strings
				m_maxLEDStrings = readMaxCommand(args);
				break;
				
			// case 'p':	// Size of motion profile buffer
			// 	m_maxMotionProfile = ReadMaxCommand(args);
			// 	break;
			}
		}
	}
	
	private void enableCommand(String args)
	{
		Logger.log("ArduinoConnection", 3, args.charAt(0) == 'e' ? "Enabled" : "Disabled");
		
/*		if (args.charAt(0) == 'e')
		{
			RobotBase.GetInstance().Enable();	
		}
		else
		{
			RobotBase.GetInstance().Disable();
		}*/
	}
	
	private void processCommand(String command)
	{
		// Logger.Log("ArduinoConnection", 0, command);
//		Logger.Log("ArduinoConnection", 0, String.format("cmd='%c'", command.charAt(0)));
		
		if (command.length() >= 1)
		{
			String args = command.substring(1).trim();
			
			switch (command.charAt(0))
			{
			case '.':
					break;
					
/*			case 's':
				StartCommand(args);
				break;*/
				
			case 'i':
				infoCommand(args);
				break;
				
			case 'x':
				enableCommand(args);
				break;
				
			case 'e':
				Encoder.command(args);
				break;
				
			case 'd':
				DigitalInput.command(args);
				break;
				
			case 'c':
				// ColorSensor.Command(args);
				DigitalCounter.command(args);
				break;
				
			// case 'm':
			// 	SmartMotor.Command(args);
			// 	break;

			// case 'n':
			// 	Navigator.getInstance().command(args);
			// 	break;
				
// MUSTFIX				
//			case 'n':
//				Navigator.Command(args);
//				break;
				
/*			case 'e':
				if (command.length() > 1)
				{
					ReadEncoder(command.substring(1).trim());
				}
				break;
				
			case 'a':
				if (command.length() > 1)
				{
					ReadAnalog(command.substring(1).trim());
				}
				
			case 'd':
				if (command.length() > 1)
				{
					ReadDigital(command.substring(1).trim());
				}
				break;
				
			case 'i':
				if (command.length() > 1)
				{
//					System.out.println(command);
					ReadIREncoder(command.substring(1).trim());
				}
				break;*/
				
			// case 'g':
			// 	if (command.length() > 1)
			// 	{
			// 		ReadGyro(command.substring(1).trim());
			// 	}
			// 	break;
				
			case 'z':
				System.out.println("Ping time: " + m_pingTimer.get());
				Logger.log("ArduinoConnection", 0, String.format("Ping time: %.4f", m_pingTimer.get()));
				break;
				
			default:
//				System.out.println("Illegal Command: " + command);
//				Logger.Log("ArduinoConnection", 3, "Illegal Command: " + command);
				break;
			}	
		}
	}
	
	private void dataReceived(String data)
	{
		int	idx;
		
//		System.out.println("DataReceived: " + data);
		
		while ((idx = data.indexOf('\n')) >= 0)
		{
			m_command	+= data.substring(0,  idx);
			processCommand(m_command);
			
			m_command	= "";
			
			data	= data.substring(idx + 1);
		}
		
		m_command	+= data;
	}
	
	//! @cond PRIVATE 
	public void start() throws InterruptedException
	{
	    m_serialConnection = new SerialConnectionPi4J();	//SerialFactory.createInstance();
	    // m_serialConnection = new SerialConnectionRxTx();	//SerialFactory.createInstance();
	
	    // create and register the serial data listener
// 	    m_serial.addListener(new SerialDataEventListener() {
// 	        @Override
// 	        public void dataReceived(SerialDataEvent event) 
// 	        {
// 	        	try 
// 	        	{
// 		        	String data = event.getAsciiString();
		        	
//		        	Logger.Log("ArduinoConnection", 1, String.format("d='%s'", data));
		        	
		            // System.out.print(data);
		        	
// 					DataReceived(data);
// 				} 
// 	        	catch (IOException e) 
// 	        	{
// 					e.printStackTrace();
// 				}
// 	        }            
// 	    });
	    
	    try 
	    {
            if (!PCTest)
            {
				m_serialConnection.open((d) -> { dataReceived(d); });

	            // SerialConfig config = new SerialConfig();
	
	            // // set default serial settings (device, baud rate, flow control, etc)
	            // //
	            // // by default, use the DEFAULT com port on the Raspberry Pi (exposed on GPIO header)
	            // // NOTE: this utility method will determine the default serial port for the
	            // //       detected platform and board/model.  For all Raspberry Pi models
	            // //       except the 3B, it will return "/dev/ttyAMA0".  For Raspberry Pi
	            // //       model 3B may return "/dev/ttyS0" or "/dev/ttyAMA0" depending on
	            // //       environment configuration.
	            // String port;
            
	            // if (SystemInfo.getBoardType() == SystemInfo.BoardType.RaspberryPi_ZeroW)
				// {
				// 	port = RaspberryPiSerial.S0_COM_PORT;
				// }
	            // else
	            // {
	            // 	port	= SerialPort.getDefaultPort();
	            // }
	            
	            // Logger.Log("ArduinoConnection", -1, String.format("SerialConfig: port = %s", port));
            
	            // config.device(port)	//SerialPort.getDefaultPort())
	            //       .baud(Baud._115200)
	            //       .dataBits(DataBits._8)
	            //       .parity(Parity.NONE)
	            //       .stopBits(StopBits._1)
	            //       .flowControl(FlowControl.NONE);
	            
	            // // open the default serial device/port with the configuration settings
	            // m_serial.open(config);
            }
	    }
	    catch(Exception ex) 
	    {
	        System.out.println(" ==>> SERIAL SETUP FAILED : " + ex.getMessage());
	        return;
	    }
		
	    resetArduino();
			    
	    /*
	     * Start keep-alive thread
	     */
	    new Thread(new Runnable() 
	    {
	        @Override
	        public void run() 
	        {
	        	while (true)
	        	{
//		        	System.out.println("Sending keep alive");
		        	sendCommand("k");
		        	
		        	try
		        	{
		        		Thread.sleep(500);
		        	}
		        	catch (InterruptedException ex)
		        	{
		        		
		        	}
	        	}
	        }
	        
	    }).start();	    
	}
	//! @endcond
	
	private void writeln(String data)
	{
		if (PCTest)
		{
			if (!data.equals("k"))
			{
				Logger.log("ArduinoConnection", 1, String.format("writeln(%s)", data));
			}
		}
		else
		{
			try 
			{
				m_serialConnection.writeln(data);
			} 
			catch (IllegalStateException e) 
			{
				e.printStackTrace();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	//! @cond PRIVATE 
	public void sendCommand(String command)
	{
		// if (command.charAt(0) != 'k')
		// {
		// 	Logger.Log("ArduinoConnection", 1, command);
		// }
		
		synchronized (this)
		{
			RobotBase.sleep(1);
			writeln(command);
		}
	}
	
	public void setPinMode(int pin, PinMode mode)
	{
		switch (mode)
		{
		case Input:
			writeln("i " + pin);
			break;
			
		case Output:
			writeln("o " + pin);
			break;
		}
	}
	
	public void AnalogWrite(int pin, int value)
	{
		writeln("wa " + pin + " " + value);
	}
	
	public void DigitalWrite(int pin, int value)
	{
		writeln("wd " + pin + " " + value);
	}
	
	public void setTimeout(int timeout)
	{
		writeln("t " + timeout);
	}
	//! @endcond
}
