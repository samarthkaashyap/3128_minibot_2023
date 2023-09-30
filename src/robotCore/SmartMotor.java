package robotCore;

import java.util.ArrayList;

/**
 * 
 * @author John Gaby
 * 
 * @brief Smart motor controller class
 * 
 * This class serves as the base class for the different types of motors
 * It can handle advanced motor control using PID to control of the motor speed.
 * 
 */
public class SmartMotor extends MotorBase 
{
	/**
	 * 
	 * @brief Specifies the type of motor
	 * 
	 */
	public enum SmartMotorType
	{
		/** 
		 *  The type is unknown
		 */
		Unknown,
		/** 
		 *  The motor is controlled by a PWM signal
		 */
		PWM,
		/** 
		 *  The motor is controlled by Servo style signal
		 */
		Servo
	}
	
	/**
	 * 
	 * @brief Specifies the type of control
	 * 
	 */	
	public enum SmartMotorMode
	{
		/** 
		 *  The motor is controlled by power
		 */
		Power,
		/** 
		 *  The motor uses a PID loop to control the speed
		 */
		Speed,
		/** 
		 *  The motor is controlled using a Motion Control list (not currently implemented)
		 */
		MotionProfile,
	}

	private class Processor
	{
		private int m_addr;
		private int m_nextMotor = 0;
		private SmartMotor[] m_motors = null;
		private long m_validPins = 0;
		private int m_validPWMPins = 0;
		// private int m_usedPins = 0;
		private int m_maxMotors = 0;
	
		Processor(int addr)
		{
			m_addr = addr;
		}

		private long getValidPins()
		{
			if (m_validPins == 0)
			{
				m_validPins = m_device.getLong(m_addr, TwoWire.k_getValidPins);
			}

			// System.out.println(String.format("validPins = %x", m_validPins));

			return(m_validPins);
		}

		private int getValidPWMPins()
		{
			if (m_validPWMPins == 0)
			{
				m_validPWMPins = m_device.getInt(m_addr, TwoWire.k_getValidPWMPins);
			}

			// System.out.println(String.format("validPWMPins = %x", m_validPWMPins));

			return(m_validPWMPins);
		}

		private int getMaxMotors()
		{
			if (m_maxMotors == 0)
			{
				m_maxMotors = m_device.getByte(m_addr, TwoWire.k_getMaxMotors);
			}

			// System.out.println(String.format("maxMotors = %d", m_maxMotors));

			return(m_maxMotors);
		}

		private void verifyPin(int pin, boolean pwm)
		{
			long validPins = pwm ? getValidPWMPins() : getValidPins();
	
			/*
			 * Check if pin is valid for this processor
			 */
			if ((pin < 0) || (pin >= 64) || (((1 << pin) & validPins) == 0))
			{
				throw new TwoWire.HardwareException(String.format("SmartMotor: Invalid%s pin: %d", pwm ? " PWM" : "", pin));
			}

			/*
			 * Check if pin is currently in use
			 */
			if (m_device.checkUsedPin(m_addr, pin))
			{
				throw new TwoWire.HardwareException(String.format("SmartMotor: Pin %d is in use", pin));
			}
		}	
	}

	static ArrayList<Processor> m_processors = new ArrayList<Processor>();

	Processor getProcessor(int addr)
	{
		Processor processor;

		for (int i = 0 ; i < m_processors.size() ; i++)
		{
			processor = m_processors.get(i);

			if (processor.m_addr == addr)
			{
				return(processor);
			}
		}

		processor = new Processor(addr);
		m_processors.add(processor);

		return(processor);
	}

	private Processor m_processor;
	private Device m_device = null;
	private ArduinoConnection m_arduino = null;
	private SmartMotorType m_type = SmartMotorType.Unknown;
	private SmartMotorMode m_mode = SmartMotorMode.Power;
	private int m_motorNo;
	private int m_i2cAddr = 0;
	private double m_maxSpeed = 0;
	
	private Encoder m_encoder = null;

	private void init(SmartMotorType type, int pwmPin, int dirPin, int min, int zero, int max, int i2cAddr)
	{
		if (RobotBase.TW)
		{
			initTW(type, pwmPin, dirPin, min, zero, max, i2cAddr);
		}
		else
		{
			initArduino(type, pwmPin, dirPin, min, zero, max);
		}
	}

	private void initTW(SmartMotorType type, int pwmPin, int dirPin, int min, int zero, int max, int i2cAddr)
	{
		m_device = Device.getInstance();
		m_processor = getProcessor(i2cAddr);
		int maxMotors = m_processor.getMaxMotors();

		// System.out.println(String.format("SmartMotor init: addr = %d", i2cAddr));

		if (pwmPin == dirPin)
		{
			throw new TwoWire.HardwareException(String.format("SmartMotor: The pwmPin and dirPin must be different"));
		}

		m_processor.verifyPin(pwmPin, true);
		if (dirPin != 0)
		{
			m_processor.verifyPin(dirPin, false);
		}

		if (((type != SmartMotorType.PWM) && (type != SmartMotorType.Servo)) || (m_processor.m_nextMotor >= maxMotors))
		{
			throw new TwoWire.HardwareException("Invalid motor param");
		}
		
		if (m_processor.m_motors == null)
		{
			m_processor.m_motors = new SmartMotor[maxMotors];
		}
		
		m_type = type;
		m_motorNo = m_processor.m_nextMotor++;
		m_processor.m_motors[m_motorNo]	= this;
		m_i2cAddr = i2cAddr;

		m_device.setUsedPin(m_i2cAddr, pwmPin);

		if (dirPin != 0)
		{
			m_device.setUsedPin(m_i2cAddr, dirPin);
		}
		
		switch (type)
		{
		case PWM:
			// SendCommand(String.format("m%dcp %d %d", m_motorNo, pwmPin, dirPin));
			m_device.sendCommand(m_i2cAddr, TwoWire.k_configureMotor, (byte) m_motorNo, (byte) SmartMotorType.PWM.ordinal(), (byte) pwmPin, (byte) dirPin);
			break;
			
		case Servo:
			sendCommand(String.format("m%dcs %d %d %d %d", m_motorNo, pwmPin, min, zero, max));
			break;
			
		default:
			break;
		}
	}
	
	/** 
	 * Creates an instance of a PWM duty cycle motor using 1 or 2 pins
	 *  
	 *  @param type - Specifies the type of motor
	 *  @param pwmPin - Specifies the pin for the PWM signal
	 *  @param dirPin - Specifies the pin for the direction control. 
	 *  If this value is zero then only the pwmPin is used and the motor cannot be reversed.
	 */
	public SmartMotor(SmartMotorType type, int pwmPin, int dirPin)
	{
		init(type, pwmPin, dirPin, 1000, 1500, 2000, Device.k_mainTwoWireId);
	}

	/** 
	 * Creates instance of a PWM duty cycle motor using 1 or 2 pins when the motor
	 * is connected to an auxiliary Arduino via i2c
	 *  
	 *  @param type - Specifies the type of motor
	 *  @param pwmPin - Specifies the pin for the PWM signal
	 *  @param dirPin - Specifies the pin for the direction control.
	 *  If this value is zero then only the pwmPin is used and the motor cannot be reversed.
	 *  @param i2cAddr - Specifies the address of the i2c connected Arduino
	 */
	public SmartMotor(SmartMotorType type, int pwmPin, int dirPin, int i2cAddr)
	{
		init(type, pwmPin, dirPin, 1000, 1500, 2000, i2cAddr);
	}

	/** 
	 *  Creates an instance of a servo style motor
	 *  
	 *  @param pin - Specifies the pin servo signal
	 *  @param min - Specifies the minimum (full reverse) PWM time in ms
	 *  @param zero - Specifies the zero (stop) PWM time in ms.
	 *  @param max - Specifies the max (full forward) PWM time in ms.
	 */
	public SmartMotor(int pin, int min, int zero, int max)
	{
		init(SmartMotorType.Servo, pin, 0, min, zero, max,  Device.k_mainTwoWireId);
	}
	
	/** 
	 *  Creates instance of a servo style motor when the motor
	 * is connected to an auxiliary Arduino via i2c
	 *  
	 *  @param pin - Specifies the pin servo signal
	 *  @param min - Specifies the minimum (full reverse) PWM time in ms
	 *  @param zero - Specifies the zero (stop) PWM time in ms.
	 *  @param max - Specifies the max (full forward) PWM time in ms.
	 *  @param i2cAddr - Specifies the address of the i2c connected Arduino
	 */
	public SmartMotor(int pin, int min, int zero, int max, int i2cAddr)
	{
		init(SmartMotorType.Servo, pin, 0, min, zero, max, i2cAddr);
	}
	
	/** 
	 *  
	 *  @param maxSpeed - Specifies the maximum speed of the motor. Setting this
	 *  				  value allows control of the speed using the range
	 *  				  -1 to +1 similar to when the motor is controlled by power.
	 */
	public void setMaxSpeed(double maxSpeed)
	{
		m_maxSpeed = maxSpeed;
	}
	
	/** 
	 *  
	 *  @param value - Sets either the power or the speed depending on the current mode.
	 *  				If the motor is in power mode, this should be in the range -1 to +1.
	 *  				If the motor is in speed mode, then this should either be the desired
	 *  				speed, or -1 to +1 if the <strong>maxSpeed</strong> value has been set.
	 */
	public void set(double value)
	{
		if (RobotBase.TW)
		{
			setTW(value);
		}
		else
		{
			setArduino(value);
		}
	}

	// public void setMinPower(double value)
	// {
	// 	if (RobotBase.TW)
	// 	{
	// 		m_device.sendCommand(m_i2cAddr, TwoWire.k_setMinMotorPower, (byte) m_motorNo, (short) (value * 1000));
	// 	}
	// 	else
	// 	{
	// 		throw new HardwareException("Function not supported");
	// 	}
	// }

	private void setTW(double value)
	{
//		Logger.Log("SmartMotor", 0, String.format("value = %.3f", value));
		
		if (m_type != SmartMotorType.Unknown)
		{
			switch (m_mode)
			{
			case Power:
				if (value > 1.0)
				{
					value = 1.0;
				}
				else if (value < -1.0)
				{
					value = -1.0;
				}

				m_device.sendCommand(m_i2cAddr, TwoWire.k_setMotor, (byte) m_motorNo, (short) (value * 1000));
				
				// m_motorValue = (int) (value * 1000);
				// m_motorUpdate = true;
				break;
				
			case Speed:
				if (m_maxSpeed != 0)
				{
					value *= m_maxSpeed;
				}
				m_device.sendCommand(m_i2cAddr, TwoWire.k_setMotor, (byte) m_motorNo, (short) value);
//				SendCommand(String.format("m%ds %d", m_motorNo, (int) value));
				// m_motorValue = (int) value;
				// m_motorUpdate = true;
				break;
				
			default:
				break;
			}
		}
	}
	
	/** 
	 *  
	 *  @param encoder - Specifies the feedback device to be used to control the speed.
	 */
	public void setFeedbackDevice(Encoder encoder)
	{
		m_encoder	= encoder;
		
		// SendCommand(String.format("m%dd %d", m_motorNo, encoder.getEncoderNo()));
		m_device.sendCommand(m_i2cAddr, TwoWire.k_setFeedbackDevice, (byte) m_motorNo, (byte) encoder.getEncoderNo());
	}
	
	/** 
	 *  
	 *  @return Returns the current feedback device
	 */
	public Encoder getFeedbackDevice()
	{
		return(m_encoder);
	}
	
	/** 
	 *  
	 *  @param mode - Sets the current control mode.
	 */
	public void setControlMode(SmartMotorMode mode)
	{
		if (mode != m_mode)
		{
			m_mode = mode;
			// SendCommand(String.format("m%dm %d", m_motorNo, mode.ordinal()));
			m_device.sendCommand(m_i2cAddr, TwoWire.k_setMotorMode, (byte) m_motorNo, (byte) m_mode.ordinal());
		}
	}
	
	// private class FloatParams
	// {
	// 	int value;
	// 	int divisor;
		
	// 	FloatParams(double f)
	// 	{
	// 		divisor = 1;
			
	// 		if (f != 0)
	// 		{
	// 			while (f < 1)
	// 			{
	// 				f	*= 10;
	// 				divisor	*= 10;
	// 			}
	// 		}
			
	// 		value = (int) (f * 1000);
	// 		divisor *= 1000;
	// 	}
	// }
	
	/** 
	 *  
	 *  @param f - Specifies the 'feed forward' term for the PID control.
	 */
	public void setFTerm(double f)
	{
		// FloatParams params = new FloatParams(f);
		
		// SendCommand(String.format("m%df %d %d", m_motorNo, params.value, params.divisor));
		m_device.sendCommand(m_i2cAddr, TwoWire.k_setPIDFTerm, (byte) m_motorNo, (float) f);
	}
	
	/** 
	 *  
	 *  @param i - Specifies the integral term for the PID control.
	 */
	public void setITerm(double i)
	{
		// FloatParams params = new FloatParams(i);
		
		// SendCommand(String.format("m%di %d %d", m_motorNo, params.value, params.divisor));
		m_device.sendCommand(m_i2cAddr, TwoWire.k_setPIDITerm, (byte) m_motorNo, (float) i);
	}
	
	/** 
	 *  
	 *  @param p - Specifies the proportional term for the PID control.
	 */
	public void setPTerm(double p)
	{
		// FloatParams params = new FloatParams(p);
		
		// SendCommand(String.format("m%dp %d %d", m_motorNo, params.value, params.divisor));
		m_device.sendCommand(m_i2cAddr, TwoWire.k_setPIDPTerm, (byte) m_motorNo, (float) p);
	}
	
	/** 
	 *  
	 *  @param z - Specifies the I Zone term for the PID control. This is the region
	 *  			outside of which, the I term is ignored.
	 */
	public void setIZone(double z)
	{
		// FloatParams params = new FloatParams(z);
		
		// SendCommand(String.format("m%dz %d %d", m_motorNo, params.value, params.divisor));
		m_device.sendCommand(m_i2cAddr, TwoWire.k_setPIDIZone, (byte) m_motorNo, (float) z);
	}
	
	/** 
	 *  
	 *  @param invert - If true then the direction of the motor is reversed
	 */
	public void setInverted(boolean invert)
	{
		// SendCommand(String.format("m%d%c", m_motorNo, invert ? '-' : '+'));
		m_device.sendCommand(m_i2cAddr, TwoWire.k_setMotorInverted, (byte) m_motorNo, (byte) (invert ? 1 : 0));
	}
	
	/** 
	 *  
	 *  Disables the motor.
	 */

	@Override
	public void disable() 
	{
	}

	/** 
	 *  
	 *  Enables the motor.
	 */
	@Override
	public void enable() 
	{
	}
	//! @endcond

	// Arduino Interface

	private static int m_nextMotor = 0;
	private static SmartMotor[] m_motors = null;
	private long m_motorValue = 0;
	private boolean m_motorUpdate = false;
		
	private void sendCommand(String cmd)
	{
		if (m_i2cAddr != 0)
		{
			m_arduino.sendCommand(String.format("i%d%s", m_i2cAddr, cmd));
		}
		else
		{
			m_arduino.sendCommand(cmd);
		}
	}

	private void initArduino(SmartMotorType type, int pwmPin, int dirPin, int min, int zero, int max)
	{
		m_arduino = ArduinoConnection.getInstance();

		if ((type != SmartMotorType.PWM) && (type != SmartMotorType.Servo))
		{
			Logger.log("SmartMotor", 3, "Invalid motor param");
			return;
		}
		
		if (m_motors == null)
		{
			m_motors = new SmartMotor[4];
		}
		
		m_type = type;
		m_motorNo = m_nextMotor++;
		m_motors[m_motorNo]	= this;
		
		switch (type)
		{
		case PWM:
			sendCommand(String.format("m%dcp %d %d", m_motorNo, pwmPin, dirPin));
			break;
			
		case Servo:
			sendCommand(String.format("m%dcs %d %d %d %d", m_motorNo, pwmPin, min, zero, max));
			break;

		default:
			break;
		}
	}

	private void setArduino(double value)
	{
//		Logger.Log("SmartMotor", 0, String.format("value = %.3f", value));
		
		if (m_type != SmartMotorType.Unknown)
		{
			switch (m_mode)
			{
			case Power:
//				Logger.Log("SmartMotor", 1, String.format("m%ds %d", m_motorNo, (int) (value * 1000)));
//				SendCommand(String.format("m%ds %d", m_motorNo, (int) (value * 1000)));
				if (value > 1.0)
				{
					value = 1.0;
				}
				else if (value < -1.0)
				{
					value = -1.0;
				}
				
				m_motorValue = (int) (value * 1000);
				m_motorUpdate = true;
				break;
				
			case Speed:
				if (m_maxSpeed != 0)
				{
					value *= m_maxSpeed;
				}
//				SendCommand(String.format("m%ds %d", m_motorNo, (int) value));
				m_motorValue = (int) value;
				m_motorUpdate = true;
				break;
				
			default:
				break;
			}
		}
	}

	//! @cond PRIVATE 
	@Override
	public void update() 
	{
		if (m_motorUpdate)
		{
			sendCommand(String.format("m%ds %d", m_motorNo, m_motorValue));
			// Logger.Log("SmartMotor", 1, String.format("m%ds %d", m_motorNo, m_motorValue));
			m_motorUpdate = false;
		}
	}
	//! @endcond
}
