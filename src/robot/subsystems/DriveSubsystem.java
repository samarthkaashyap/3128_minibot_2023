/*
 *	  Copyright (C) 2022  John H. Gaby
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


package robot.subsystems;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import robotCore.Device;
import robotCore.Encoder;
//import encoder, encoder type, and motor (PWMMotor) 
import robotCore.Encoder.EncoderType;
import robotCore.PWMMotor;
//import constants from constant folder
import static robot.Constants.DrivetrainConstants.*;
public class DriveSubsystem extends SubsystemBase {

  //create motor and encoder objects here

  private final PWMMotor right_Motor = new PWMMotor(k_rightMotorPWMPin, k_rightMotorDirPin);
  private final PWMMotor left_Motor = new PWMMotor(k_leftMotorPWMPin, k_leftMotorDirPin);

  //add stupid screenshot

  private static DriveSubsystem instance;
  public static synchronized DriveSubsystem getInstance(){
    if (instance == null) {
      instance = new DriveSubsystem();
    }
    return instance;
  }

  private final Encoder left_Encoder = new Encoder(EncoderType.Quadrature, k_leftEncoderIntPin, k_leftEncoderDirPin);
  private final Encoder right_Encoder = new Encoder(EncoderType.Quadrature, k_rightEncoderIntPin, k_rightEncoderDirPin);
  
  public DriveSubsystem() {
      
  //be sure to set inverted!
  right_Motor.setInverted(true);
  }

  @Override
  public void periodic() {
  // This method will be called once per scheduler run
    System.out.println();
  }

  //set power here! for both motors
  public void setPower(double rightpower, double leftpower){
    right_Motor.set(rightpower);
    left_Motor.set(leftpower);
  }

  //get your encoders here! for both motors
  public Encoder get_LeftEncoder() {
      return left_Encoder;
  }
  public double get_RightVelocityEncoder() {
      return right_Encoder.getSpeed();
  }

}
