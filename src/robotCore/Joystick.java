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


/**
 * 
 * @author John Gaby
 * 
 * @brief The Joystick class read the joystick connected to the driver station
 * 
 * The joystick is connected to the driver station and communicates with <strong>RobotCore</strong>
 * via a network connection which it uses to send the current joystick x and y positions as well
 * as the state of the buttons.
 */

public class Joystick extends GenericHID
{
	static Joystick m_rootJoystick = null;
	Joystick m_joystick;
	boolean m_gamepad = false;
	double m_x = 0;
	double m_y = 0;
	double m_rx = 0;
	double m_ry = 0;
	double m_throttle = 0;
	double m_pov = 0;
	int m_buttons = 0;
	
	//! @cond PRIVATE 
	public static Joystick getInstance()
	{
		if (m_rootJoystick == null)
		{
			new Joystick();
		}
		
		return(m_rootJoystick);
	}
	//! @endcond
	
	private Joystick()
	{
		if (m_rootJoystick == null)
		{
			m_rootJoystick	= this;
		}
		
		m_joystick	= m_rootJoystick;
	}
	
	/**
	 * 
	 * @param id - This is currently unused, but is provided for compatibility with the <strong>wpilib</strong> library.
	 */
	public Joystick(int id)
	{
		m_joystick = getInstance();
	}
	
	/**
	 * @return Returns true if the attached joystick is a gamepad.
	 */
	public boolean isGamepad()
	{
		// System.out.println(String.format("m_gamepad = %b", m_joystick.m_gamepad));
		return(m_joystick.m_gamepad);
	}

	/**
	 * @return Returns the current horizontal position of the joystick.  The value returned will be between -1.0 and +1.0.
	 */
	@Override
	public double getX()
	{
		return(m_joystick.m_x);
	}
	
	/**
	 * @return Returns the current vertical position of the joystick.  The value returned will be between -1.0 and +1.0.
	 */
	@Override
	public double getY()
	{
		return(m_joystick.m_y);
	}

	public double getZ()
	{
		return 0;	// This function is not implemented
	}
	
	/**
	 * @return Returns the current horizontal position of the right joystick on a gamepad.  
	 * The value returned will be between -1.0 and +1.0.
	 */
	public double getRX()
	{
		return(m_joystick.m_rx);
	}
	
	/**
	 * @return Returns the current vertical position of the right joystick on a gamepad.  
	 * The value returned will be between -1.0 and +1.0.
	 */
	public double getRY()
	{
		return(m_joystick.m_ry);
	}
	
	/**
	 * @return Returns the current trottle position of the joystick.  
	 * The value returned will be between -1.0 and +1.0.
	 * This value also retrieves the value of the left and right analog switches
	 * on the front of the gamepad. The left switch returns a value in the range 0.0 to 1.0,
	 * and the right switch returns a value in the range of -1.0 to 0.0.
	 */
	public double getThrottle()
	{
		return(m_joystick.m_throttle);
	}
	
	/**
	 * @return Returns the value for the 8 position switch on the gamepad.
	 * It will return 8 descrete values in the range 0.0 to 1.0.
	 */
	public double getPOV()
	{
		return(m_joystick.m_pov);
	}
	
	/**
	 * @return Returns <strong>true</strong> if the specified button is currently pressed, and <strong>false</strong> if it is not.
	 */
	@Override
	public boolean getRawButton(final int button)
	{
		if (button > 0)
		{
			return((m_joystick.m_buttons & (1 << (button - 1))) != 0);
		}
		
		return(false);
	}
	
	//! @cond PRIVATE 
	public void setData(boolean gamepad, double x, double y, double rx, double ry, double throttle, double pov, int buttons)
	{
		// System.out.println(String.format("gamepad=%b", gamepad));

		m_gamepad = gamepad;
		m_x	= x;
		m_y	= y;
		m_rx = rx;
		m_ry = ry;
		m_throttle = throttle;
		m_pov = pov;
		m_buttons = buttons;
	}
	//! @endcond
}
