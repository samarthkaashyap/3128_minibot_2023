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
 * @brief The GeneriacHID class is the base class for input devices such as joysticks
 * 
 */

public abstract class GenericHID
{
	/**
	 * Reads the state of a specified button
	 * 
	 * @param button - specifies the button to read
	 * 
	 * @return Returns <strong>true</strong> if the button is pressed, <strong>false</strong> if it is not.
	 */
	abstract public boolean getRawButton(int button);
	
	/**
	 * @return Returns the current horizontal position, a value between -1.0 and 1.0
	 */
	abstract public double getX();

	/**
	 * @return Returns the current vertical position, a value between -1.0 and 1.0
	 */
	abstract public double getY();
}
