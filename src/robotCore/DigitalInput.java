/*
 *	  Copyright (C) 2021  John H. Gaby
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

import robotCore.TwoWire.HardwareException;

//import robot.Robot;

/**
 * 
 * @author John Gaby
 * 
 * @brief The DigitalInput class reads the state of one Arduino digital pin
 * 
 *
 */
public class DigitalInput 
{
	private int m_pin;
	private int m_addr;
	private Device m_device = null;
	private ArduinoConnection m_arduino = null;
	
	public void init(int pin, int addr)
	{
		if (RobotBase.TW)
		{
			initTW(pin, addr);
		}
		else
		{
			initArduino(pin);
		}
	}

	private void initTW(int pin, int addr)
	{
		m_device = Device.getInstance();

		long validPins = m_device.getLong(addr, TwoWire.k_getValidPins);

		// System.out.println(String.format("DigitalInput: pin=%d, valid = 0x%x", pin, validPins));

		if ((pin < 0) || (pin >= 64) || (validPins & (1 << pin)) == 0)
		{
			throw new HardwareException("Bad pin number");
		}
		
		m_pin = pin;
		m_addr = addr;

		m_device.setUsedPin(m_addr, pin);
	}

	/** 
	 *  @param pin - Specifies the pin to read
	 */
	public DigitalInput(int pin)
	{
		init(pin, Device.k_mainTwoWireId);
	}

	public DigitalInput(int pin, int addr)
	{
		init(pin, addr);
	}
	
	/** 
	 *  Gets the current state of the pin.
	 *  
	 *  @return Returns true if the pin is 1, false if it is 0.
	 */
	public boolean get()
	{
		if (RobotBase.TW)
		{
			return(getTW());
		}
		else
		{
			return(getArduino());
		}
	}

	private boolean getTW()
	{
		return(m_device.getByte(m_addr, TwoWire.k_digitalRead, (byte) m_pin) == 1);
	}

	// Arduino Interface

	private static int m_inputs = 0;
	private static int m_nextInput = 0;
	private int m_inputNo = -1;

	//! @cond PRIVATE 
	public static void command(String args)
	{
		int a[] = RobotBase.parseIntegers(args, 1);
		
		if (a != null)
		{

			m_inputs = a[0];

		}
	}
	//! @endcond

	private void initArduino(int pin)
	{
		m_arduino =  ArduinoConnection.getInstance();

		m_inputNo = m_nextInput++;
		
		m_arduino.sendCommand(String.format("d%dc %d", m_inputNo, pin));
	}
	
	public boolean getArduino()
	{
		if (m_inputNo >= 0)
		{
			return(((m_inputs >> m_inputNo) & 1) != 0);
		}
		
		return(false);
	}
}
