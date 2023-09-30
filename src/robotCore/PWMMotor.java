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
 * @brief The PWMMotor class is used to control a motor that requires PWM and direction signals
 * 
 * Controls a motor which requires two inputs. One of the inputs is a PWM (Pulse Width Modulation) signal
 * which controls the speed of the motor while the second input controls the direction.
 *
 */

public class PWMMotor extends SmartMotor
{	
	/** 
	 *  @param pwmPin - Specifies the Arduino PWM pin for the motor.
	 *  @param dirPin - Specifies the Arduino direction pin for the motor.
	 *  
	 */
	public PWMMotor(int pwmPin, int dirPin)
	{
		super(SmartMotorType.PWM, pwmPin, dirPin);
	}
	
	/** 
	 *  @param pwmPin - Specifies the Arduino PWM pin for the motor.
	 *  @param dirPin - Specifies the Arduino direction pin for the motor.
	 *  @param i2cAddr - Specifies the i2c address of the Arduino controlling the motor
	 *  
	 */
	public PWMMotor(int pwmPin, int dirPin, int i2cAddr)
	{
		super(SmartMotorType.PWM, pwmPin, dirPin, i2cAddr);
	}
}
