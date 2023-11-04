/*
 *	  Copyright (C) 2016  John H. Gaby
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free S4`oftware Foundation, version 3 of the License.
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
package robot.subsystems;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import robotCore.Device;

//import encoder, encoder type, and motor (PWMMotor)
import robotCore.encoder;
import robotCore.PWMMotor;
import robotCore.Encoder.EncoderType;

//import constants
import static robot.Constants.FeederConstants.*;

public class FeederSubsystem extends SubsystemBase {
	
	//create subsystem instance
	private static FeederSubsystem instance;
	public static synchronized FeederSubsystem getInstance(){
		if (instance == null) {
			instance = new FeederSubsystem
		}
	}
}

	//create motor object
	private final PWMMotor f_motor = new PWMMotor(PWMPin, DirPin);

	//create encoder object
	private final Encoder f_encoder = new Encoder(EncoderType.Quadrature);

	

	public FeederSubsystem() {
		
	}


	//set your motor power here
	private void f_powerSet(double f_power) {
		f_motor.set(f_power);
	}


	//get encoder value here
	public Encoder getEncoder() {
		return f_encoder;
	}

	
	@Override
	public void periodic() {
		// This method will be called once per scheduler run
	}

