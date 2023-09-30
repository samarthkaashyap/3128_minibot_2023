package robotCore;

/**
 * 
 * @author John Gaby
 * 
 * @brief Controls the state of one Arduino digital pin
 * 
 *
 */
public class DigitalOutput 
{
	private int m_pin;
	private int m_i2cAddr;
	private Device m_arduino = Device.getInstance();

	// private void SendCommand(String cmd)
	// {
	// 	if (m_i2cAddr != 0)
	// 	{
	// 		m_arduino.SendCommand(String.format("i%d%s", m_i2cAddr, cmd));
	// 	}
	// 	else
	// 	{
	// 		m_arduino.SendCommand(cmd);
	// 	}
	// }

	private void init(int pin, int i2cAddr)
	{
		m_pin = pin;
		m_i2cAddr = i2cAddr;
	}
	
	/** 
	 *  @param pin - Specifies the pin to control on the Aux Arduino
	 *  @param i2cAddr - Specifies the i2cAddr of the Aux Arduino
	 */
	public DigitalOutput(int pin, int i2cAddr)
	{
		init(pin, i2cAddr);
	}

	/**
	 * 
	 * @param pin - Specifies the pin to control
	 * 
	 */
	
	public DigitalOutput(int pin)
	{
		init(pin, Device.k_i2cDefAddr);
	}
	
	/** 
	 *  
	 *  @param high - Specifies the desired state of the pin
	 *
	 */	
	public void set(boolean high)
	{
		m_arduino.sendCommand(m_i2cAddr, TwoWire.k_digitalWrite, (byte) m_pin, (byte) (high ? 1 : 0));
	}

}
