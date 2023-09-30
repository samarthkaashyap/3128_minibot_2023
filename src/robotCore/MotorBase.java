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
 * @brief Provides the base class for all motors.
 * 
 *
 */
public abstract class MotorBase implements RobotBase.UpdateModule
{
	public abstract void disable();
	public abstract void enable();
	
	MotorBase()
	{
		Logger.log("MotorBase", 3, "MotorBase()");
		if (!RobotBase.TW)
		{
			RobotBase.getInstance().addMotor(this);
		}
	}
}
