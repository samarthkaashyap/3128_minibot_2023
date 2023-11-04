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

package robot.commands;

import edu.wpi.first.wpilibj2.command.CommandBase;
import robotCore.Encoder;
import robot.subsystems.FeederSubsystem;
//import encoder and feeder subsystem 

public class CmdFeeder extends CommandBase {
	//define stuff here, like subsystem, power, etc.
	private final double power;
	private final FeederSubsystem feeder;

	public CmdFeeder(FeederSubsystem f_feeder, double f_power) {
		//parameters include the subsystem and the power 
		
		power = this.f_power;
		feeder = this.f_feeder;

		addRequirements(FeederSubsystem);
	}

	@Override
	public void initialize() {
		//set power here
		f_powerSet(power);
	}


	@Override
	public void execute() {
		
	}

	//put isFinished here 
	public boolean isFinished(){
		return false;
	}

	@Override
	public void end(boolean interrupted) {
		f_motor.setPower(0,0) 
	}
}
