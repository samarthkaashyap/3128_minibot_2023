/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package robotCore;

import edu.wpi.first.wpilibj2.command.CommandBase;

  /**
   * 
   * @brief Command to check the stability of the gyro.
   * 
   * This is a utility command which will periodically read the <b>yaw</b> from the Navigator
   *   and report the result on the <b>Driver Station</b>. It is useful to check to see if the
   *   <b>yaw</b> values have settled. 
   */
  public class CheckGyroCalibrationCommand extends CommandBase {
  private Navigator m_navigator;
  private long m_time;
  private static int k_rate = 500;
  
  /**
   *
   * @param navigator The Navigator class to be used to obtain the yaw.
   */
  public CheckGyroCalibrationCommand(Navigator navigator) {
    Logger.log("CheckGyroCalibrationCommand", 3, "CheckGyroCalibrationCommand()");

    m_navigator = navigator;

    // Use addRequirements() here to declare subsystem dependencies.
    // addRequirements(m_subsystem);
  }

 	//! @cond PRIVATE 

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    Logger.log("CheckGyroCalibrationCommand", 2, "initialize()");

   m_navigator.reset(0, 0, 0);
   m_time = System.currentTimeMillis() + k_rate;
    // m_subsystem.setPower(1, 1);
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    Logger.log("CheckGyroCalibrationCommand", -1, "execute()");

    long time = System.currentTimeMillis();

    if (time >= m_time)
    {
      double yaw = m_navigator.getYaw();

      RobotBase.getInstance().sendDriverStationMessage(String.format("yaw = %f", yaw));

      m_time = time + k_rate;
    }
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    Logger.log("CheckGyroCalibrationCommand", 2, String.format("end(%b)", interrupted));

    m_navigator.reset(0, 0, 0);
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    Logger.log("CheckGyroCalibrationCommand", -1, "isFinished()");
    return false;
  }

//! @endcond
}