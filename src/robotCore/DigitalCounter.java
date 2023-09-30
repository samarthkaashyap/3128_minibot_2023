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

import java.util.ArrayList;

//import robot.Robot;

/**
 * 
 * @author John Gaby
 * 
 * @brief The DigitalCounter class provides a way to count the number
 * 			of transitions for a specific pin.
 * 
 *
 */
public class DigitalCounter 
{
	private	int m_inputNo = -1;
	private int m_count = 0;
	private int m_zero = 0;
	private int m_pin = -1;
	private static int m_nextInput = 0;
	private Device m_device = null;
	private ArduinoConnection m_arduino = null;
	private static ArrayList<DigitalCounter> m_list = new ArrayList<DigitalCounter>();
	private DigitalCounter m_counter = null;
	int m_addr = Device.k_i2cDefAddr;
	
	/** 
	 *  @param pin - Specifies the pin to read
	 */
	public DigitalCounter(int pin)
	{
		if (RobotBase.TW)
		{
			initTW(pin);
		}
		else
		{
			initArduino(pin);
		}
	}

	private DigitalCounter find(int pin)
	{
		for (int i = 0 ; i < m_list.size() ; i++)
		{
			DigitalCounter counter = m_list.get(i);

			if (counter.m_pin == pin)
			{
				return(counter);
			}
		}

		return(null);
	}

	private void initTW(int pin)
	{
		m_device = Device.getInstance();

		DigitalCounter counter = find(pin);

		if (counter != null)
		{
			m_counter = counter;
			m_pin = pin;
			m_zero = m_counter.getRaw();
			return;
		}

		m_inputNo = m_nextInput++;
		
		m_device.sendCommand(m_addr, TwoWire.k_createCounter, (byte) m_inputNo, (byte) pin);

		m_list.add(this);
	}

	private int getRaw()
	{
		if (RobotBase.TW)
		{
			return getRawTW();
		}
		else
		{
			return getRawArduino();
		}
	}

	private int getRawTW()
	{
		if (m_counter != null)
		{
			return(m_counter.getRaw());
		}
		
		return(m_device.getInt(m_addr, TwoWire.k_getDigitalCount, (byte) m_inputNo));
	}
	
	/** 
	 *  Gets the current number of transitions on the pin.
	 *  
	 *  @return Returns the current count.
	 */
	public int get()
	{
		return(getRaw() - m_zero);
	}

	/** 
	 *  Resets the counter to zero
	 *  
	 */
	public void reset()
	{
		m_zero = getRaw();
	}

	/** 
	 *  Creates a copy of the specified counter. This counter will reference
	 * 		the same pin, but will nave an independent zero point.
	 *  
	 *  @return Returns a copy of the counter.
	 */

	public DigitalCounter copy()
	{
		return new DigitalCounter(m_pin);
	}

	// Arduino Interface

	private void initArduino(int pin)
	{
		m_arduino = ArduinoConnection.getInstance();

		DigitalCounter counter = find(pin);

		if (counter == null)
		{
			m_counter = counter;
			m_pin = pin;
			m_zero = m_counter.getRaw();
			return;
		}

		// if (m_nextInput < m_arduino.GetMaxDigitalCounters())
		{
			m_inputNo = m_nextInput++;
			
			m_arduino.sendCommand(String.format("c%dc %d", m_inputNo, pin));
		}

		m_list.add(this);
	}

	private void setCount(int count)
	{
		m_count = count;
	}

	private int getRawArduino()
	{
		return(m_count);
	}

	//! @cond PRIVATE 
	public static void command(String args)
	{
		int a[] = RobotBase.parseIntegers(args, 2);

		// Logger.Log("DigitalCounter", 1, String.format("command: %s, id=%d, v=%d", args, a[0], a[1]));
		
		if (a != null)
		{
			for (int i = 0 ; i < m_list.size() ; i++)
			{
				DigitalCounter counter = m_list.get(i);

				// Logger.Log("DigitalCounter", 1, String.format("command: %d: id=%d", i, counter.m_inputNo));

				if (a[0] == counter.m_inputNo)
				{
					counter.setCount(a[1]);
				}
			}

		}
	}
	//! @endcond
}
