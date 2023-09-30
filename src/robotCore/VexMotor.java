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
 * @brief The ServoMotor class is used to control a motor that requires single servo PWM signal.
 * 
 * Controls a VEX motor. This class extends the SmartMotor class and configures
 * it with the parameters appropriate to control a VEX motor.
 *
 */	

public class VexMotor extends SmartMotor
{	
	/** 
	 *  @param pin - Specifies the Arduino PWM pin for the motor.
	 *  
	 */
	public VexMotor(int pin)
	{
//		System.out.println("VexMotor");
		super(pin, 1000, 1500, 2200);
	}
}

