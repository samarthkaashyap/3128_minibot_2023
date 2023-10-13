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


package robot;

import edu.wpi.first.wpilibj2.command.Command;
import frc.team3128.common.hardware.input.NAR_Joystick;
import robot.commands.CmdArcadeDrive;
import robot.commands.CmdFeedAndShoot;
import robot.commands.CmdFeeder;
import robot.commands.CmdShooter;
import robot.commands.CmdTurntable;
import robot.subsystems.DriveSubsystem;
import robot.subsystems.FeederSubsystem;
import robot.subsystems.ShooterSubsystem;
import robot.subsystems.TurntableSubsystem;

/**
 * This class is where the bulk of the robot should be declared. Since
 * Command-based is a "declarative" paradigm, very little robot logic should
 * actually be handled in the {@link Robot} periodic methods (other than the
 * scheduler calls). Instead, the structure of the robot (including subsystems,
 * commands, and button mappings) should be declared here.
 */
public class RobotContainer {
  // The robot's subsystems and commands are defined here...
  private final NAR_Joystick m_joystick = new NAR_Joystick(0);

  private final DriveSubsystem m_driveSubsystem = new DriveSubsystem();
  private final ShooterSubsystem m_shooterSubsystem = new ShooterSubsystem();
  private final FeederSubsystem m_feederSubsystem = new FeederSubsystem();
  private final TurntableSubsystem m_turntableSubsystem = new TurntableSubsystem();

  /**
   * The container for the robot. Contains subsystems, OI devices, and commands.
   */
  public RobotContainer() {
    // Configure the button bindings
    configureButtonBindings();
    m_driveSubsystem.setDefaultCommand(new CmdArcadeDrive(m_driveSubsystem, m_joystick));
  }

  /**
   * Use this method to define your button->command mappings. Buttons can be
   * created by instantiating a {@link GenericHID} or one of its subclasses
   * ({@link edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then
   * passing it to a {@link edu.wpi.first.wpilibj2.command.button.JoystickButton}.
   */
  private void configureButtonBindings() {
    m_joystick.getButton(1).whileTrue(new CmdShooter(m_shooterSubsystem, 0.8));
    m_joystick.getButton(2).whileTrue(new CmdFeeder(m_feederSubsystem, 0.6));
    m_joystick.getButton(3).whileTrue(new CmdTurntable(m_turntableSubsystem, 0.6));
    m_joystick.getButton(4).whileTrue(new CmdTurntable(m_turntableSubsystem, -0.6));
    m_joystick.getButton(5).whileTrue(new CmdFeedAndShoot(m_feederSubsystem, m_shooterSubsystem, 0.5, 0.8));
 
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    // An ExampleCommand will run in autonomous
    return null;
  }
}
