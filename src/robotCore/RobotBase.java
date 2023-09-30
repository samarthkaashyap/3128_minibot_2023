package robotCore;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public abstract class RobotBase 
{
	//! @cond PRIVATE 
	public abstract void startCompetition();
	public static boolean TW = true;			// Using two wire interface
	//! @endcond
	
	public static <T extends RobotBase> void startRobot(Supplier<T> robotSupplier, boolean twoWire) 
	{
		TW = twoWire;

		T robot = robotSupplier.get();
		
		robot.Start();
	}
	
	//! @cond PRIVATE 
	public enum RobotMode
	{
		Autonomous,
		OperatorControl,
		Test,
		Disabled
	};
	
	public interface UpdateModule
	{
		public void update();
	}
	
	private static final int k_keepAliveTime = 2000;
	private static final int m_refreshTime = 50;
	private static final ScheduledExecutorService m_scheduler = Executors.newScheduledThreadPool(2);

	private RobotMode m_mode = RobotMode.OperatorControl;
	private DriverStation m_driverStation;
	private boolean m_enabled = false;
	private static RobotBase m_instance = null;
	private Device m_device = null;
	private ArduinoConnection m_arduino = null;
	
	private ArrayList<UpdateModule> m_updater = new ArrayList<UpdateModule>();
	private ArrayList<MotorBase> m_motors = new ArrayList<MotorBase>();

	class DriverStation implements Runnable
	{
		private PrintStream m_printStream = null;
		private boolean m_connected = false;
		private long m_timeout;
		private RobotBase m_robotBase;
		
		public DriverStation(RobotBase robotBase)
		{
			// Logger.SetLogFile("DriverStation", "DriverStation", true, true);

			Logger.log("DriverStation", 3, "DriverStation()");
			
			m_robotBase = robotBase;
		}
		
		private void checkKeepAlive()
		{
//			Logger.Log("DriverStation", 0, "CheckKeepAlive");
			
			if (RobotBase.getInstance().isEnabled())
			{
				if (System.currentTimeMillis() > m_timeout)
				{
					Logger.log("DriverStation", 3, "Timeout");
					
					disable();
				}
			}
		}
		
		private void sendRobotMode()
		{
			switch (m_mode)
			{
			case Autonomous:
				m_driverStation.sendCommand("A");
				break;
				
			case OperatorControl:
				m_driverStation.sendCommand("O");
				break;
				
			case Test:
				m_driverStation.sendCommand("T");
				break;
				
			default:
				break;
			}
		}
		
		public void setMode(RobotMode mode)
		{
			Logger.log("DriverStation", 1, "RobotMode: " + mode);
			m_mode	= mode;
			
			sendRobotMode();
		}

		// long lastTime = 0;
		
		private void processCommand(String command)
		{
			// long time = System.currentTimeMillis();
			// int dt = (int) (time - lastTime);
			// lastTime = time;

			// Logger.Log("DriverStation", 1, String.format("ProcessCommand,%d,%s", dt, command), dt <= 100);

			if (command.length() >= 1)
			{
				switch(command.charAt(0))
				{
				case 'E':
					enable();
					break;
					
				case 'D':
					disable();
					break;
					
				case 'A':
					setMode(RobotMode.Autonomous);
					break;
					
				case 'O':
					setMode(RobotMode.OperatorControl);
					break;
					
				case 'T':
					setMode(RobotMode.Test);
					break;
					
				case 'j':
					readJoystick(command);
					break;
					
				case 'k':
					break;
					
				default:
					Logger.log("DriverStation", 3, String.format("Invalid Command: %s", command));
	//				System.out.print("Invalid command: ");System.out.println(command);
	//				System.out.println("ch = " + (int) command.charAt(0));
				}

				// time = System.currentTimeMillis() - time;
				// Logger.Log("DriverStation", 1, String.format("ProcessCommand: dt=%d", time), (time < 100));
				
				
				m_timeout = System.currentTimeMillis() + k_keepAliveTime;
			}
		}
		
		private void readJoystick(String command)
		{
			// Logger.Log("RobotBase", 0, "ReadJoystick: " + command);
			
			int[]	args = parseIntegers(command.substring(2), 7);
			
			if (args != null)
			{
				Joystick.getInstance().setData( command.charAt(1) == 'g',
												(double) args[0] / 1000.0,
												(double) args[1] / 1000.0,
												(double) args[2] / 1000.0,
												(double) args[3] / 1000.0,
												(double) args[4] / 1000.0,
												(double) args[5] / 1000.0,
												args[6]);
			}
			
		}
		
		private void disable() 
		{
			Logger.log("DriverStation", 2, "Disable()");
			m_robotBase.disable();
			
			sendCommand("D");
		}

		private void enable() 
		{
			Logger.log("DriverStation", 2, "Enable()");
			m_robotBase.enable();
			
			sendCommand("E");
			
			m_timeout = System.currentTimeMillis() + k_keepAliveTime;
		}
		
		public void sendMessage(String message)
		{
			if (m_printStream != null)
			{
				synchronized(this)
				{
//					System.out.println("Sending message: " + message);
					m_printStream.println(message);
				}
			}
			else
			{
				Logger.log("DriverStation", 3, "SendMessage: m_printStream is null");
			}
		}
		
		public void sendCommand(String command)
		{
			sendMessage("!" + command);
		}
		
		public boolean isConnected()
		{
			synchronized(this)
			{
				return(m_connected);
			}
		}

		@Override
		public void run()
		{
			String command = "";
			
			Logger.log("DriverStation", 2, "Starting driver station server");
			
			while (true)
			{
				Logger.log("DriverStation", 1, "Waiting for connection");
				
				try ( 
					    ServerSocket serverSocket = new ServerSocket(5802);
					    Socket clientSocket = serverSocket.accept();
						InputStream inputStream = clientSocket.getInputStream();
						OutputStream outputStream = clientSocket.getOutputStream();
					) 
				{
					int ch;
					
					Logger.log("DriverStation", 1, "DriverStationServer: Connected");
					
					synchronized(this)
					{
						m_connected	= true;
						m_printStream = new PrintStream(outputStream);
					}

					sendCommand("C");
					sendRobotMode();
					
//					Gyro.StartCalibration();
					
					long lastTime = System.currentTimeMillis();

					do
					{
						ch = inputStream.read();
						
						if (ch == '\n')
						{
							long time = System.currentTimeMillis();
							long dt = time - lastTime;
							lastTime = time;

							if (dt > 250)
							{
								String msg = String.format("Lag:%d", dt);

								sendMessage(msg);
//								Logger.Log("DriverStation", 1, msg);
							}
							processCommand(command);
//							System.out.print("command: ");System.out.println(command);
							command	= "";
							
						}
						else if (ch != -1)
						{
							command += (char) ch;
						}
					} while (ch > 0);
				}
				catch (Exception ex)
				{
					Logger.log("DriverStation", 3, "DriverStation: StartServer exception: " + ex);
					
					synchronized(this)
					{
						if (m_printStream != null)
						{
							m_printStream.close();
							m_printStream	= null;
						}
					}
				}

				m_connected	= false;
				Logger.log("DriverStation", 3, "Disconnected");
				disable();
			}
			
//			System.out.println("Driver Station thread exit");
		}
	}
	
	public RobotBase()
	{
		if (m_instance ==  null)
		{
			m_instance = this;

			Logger.log("RobotBase", 3, String.format("TW=%b", TW));

			if (TW)
			{
				m_device = Device.getInstance();
			}
			else
			{
				m_arduino = ArduinoConnection.getInstance();
			}
		}
		else
		{
			Logger.log("RobotBase", 999, "ERROR: Only one instance of RobotBase is allowed");
		}
	}

	public void sendDriverStationMessage(String message)
	{
		m_driverStation.sendMessage(message);
	}
	
	public static RobotBase getInstance()
	{
		return m_instance;
	}
	
	/**
	 * 
	 * Schedules a <strong>Runnable</strong> task to be executed on a schedule.
	 * Task will run in a thread separate from the main thread.
	 * 
	 * @param task - Specifies the task to run
	 * @param time - Specifies the time interval in milliseconds
	 * 
	 * @return - Returns the <strong>Future<strong> for the task which allows for it's cancellation. 
	 * 
	 */
	static public Future<?> Schedule(Runnable task, int time)
	{
		Logger.log("RobotBase", 1, "Schedule()");
		
		return(m_scheduler.scheduleAtFixedRate(task, 0, time, TimeUnit.MILLISECONDS));
	}
	
	public void Start()
	{
		m_driverStation	= new DriverStation(this);
		
		(new Thread(m_driverStation)).start();
		
		Schedule(
				new Runnable() 
				{
					@Override
					public void run() 
					{
		        		for (UpdateModule module : m_updater)
		        		{
		        			module.update();
		        		}
					} 
				}, m_refreshTime);	
		
	    /*
	     * Start keep-alive check thread
	     */
	    Schedule(
	    		new Runnable() 
			    {
			        @Override
			        public void run() 
			        {
//		        		System.out.println("Checking keep alive");
			        	m_driverStation.checkKeepAlive();
			        }
			    }, k_keepAliveTime);	
		
		startCompetition();
	}
	
	/**
	 * 
	 * Disables the robot. 
	 * 
	 */
	public void disable()
	{
		synchronized (this)
		{
			m_enabled	= false;
		}
		
		Logger.log("RobotBase", 1, "Disable()");
		
//		CommandScheduler.getInstance().disable();
		
		// m_arduino.SendCommand("xd");
		m_device.sendCommandToAll(TwoWire.k_disable);
	}
	
	/**
	 * 
	 * Enables the robot. 
	 * 
	 */
	public void enable()
	{
		synchronized (this)
		{
			m_enabled	= true;
		}
		
		// m_arduino.SendCommand("xe");
		if (TW)
		{
			m_device.sendCommandToAll(TwoWire.k_enable);
		}
		else
		{
			m_arduino.sendCommand("xe");
		}
		
		Logger.log("RobotBase", 1, "Enable()");
		
//		CommandScheduler.getInstance().enable();
	}
	
	/**
	 * Determine if the Robot is currently disabled.
	 *
	 * @return True if the Robot is currently disabled by the field controls.
	 */
	public boolean isDisabled() 
	{
	  return !m_enabled;
	}
	
	public boolean isEnabled()
	{
		return m_enabled;
	}
	  
	/**
	 * Determine if the robot is currently in Autonomous mode as determined by the field
	 * controls.
	 *
	 * @return True if the robot is currently operating Autonomously.
	 */
	public boolean isAutonomous() {
	  return m_mode == RobotMode.Autonomous;
	}
	
	/**
	 * Determine if the robot is currently in Operator Control mode as determined by the field
	 * controls.
	 *
	 * @return True if the robot is currently operating in Tele-Op mode.
	 */
	public boolean isOperatorControl() {
	  return m_mode == RobotMode.OperatorControl;
	}
	
	public boolean isTest()
	{
		return m_mode == RobotMode.Test;
	}
	
	//! @cond PRIVATE 
	public void addUpdate(UpdateModule module)
	{
		m_updater.add(module);
	}
	//! @endcond
	
	public void addMotor(MotorBase motor)
	{
		m_motors.add(motor);
		addUpdate(motor);
	}

	/**
	 * 
	 * Causes the current thread to sleep for a specified number of milliseconds
	 * 
	 * @param ms - Specifies the time to sleep in milliseconds
	 * 
	 */
	static public void sleep(int ms)
	{
		try 
		{
			Thread.sleep(ms);
		}
		catch (InterruptedException ex)
		{
			  
		}
	 }	
	
	/**
	 * 
	 * Parses a String and extracts a number of integer values
	 * 
	 * @param str - Specifies the string to parse
	 * @param count - Specifies the number of integers that are expected
	 * 
	 * @return - If the string has the correct number of integers then an array if <strong>int</strong> is returned which contains the values parsed. 
	 * Otherwise a <strong>null</strong> is returned.
	 * 
	 */
	public static int[] parseIntegers(String str, int count)
	{
		int[]	args = new int[count];
		int 	i = 0;
		
		String[] tokens = str.trim().split(" ");
		
		for (String token : tokens)
		{
			try
			{
				args[i]	= Integer.parseInt(token);
				
			}
			catch (NumberFormatException nfe)
		    {
				break;
		    }
			
			if (++i >= count)
			{
				break;
			}
		}

		// Logger.Log("RobotBase", 0, String.format("ParseIntegers: i=%d", i));
		
		if (i == count)
		{
			return(args);
		}
		
		return(null);
	}
}
