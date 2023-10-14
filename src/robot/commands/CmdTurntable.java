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
//import Turntable subsystem

public class CmdTurntable extends CommandBase {
    //define subsystem and encoder here

    public CmdTurntable() {

        // Use requires() here to declare subsystem dependencies
        addRequirements();
    }

    // Called just before this Command runs the first time
    @Override
    public void initialize() {

    //set power here

    }

    // Called repeatedly when this Command is scheduled to run
    @Override
    public void execute() {
    
    }

    // Make this return true when this Command no longer needs to run execute()
    @Override
    public boolean isFinished() {
        return (false);
    }

    // Called once after isFinished returns true
    @Override
    public void end(boolean interrupted) {
    
    }
}
