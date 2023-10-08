package robot.commands;

import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import robot.subsystems.FeederSubsystem;
import robot.subsystems.ShooterSubsystem;

//command which commands feeder and shooter to run at the same time. parallel command group

public class CmdFeedAndShoot extends ParallelCommandGroup{
    private double feeder_power;
    private double shooter_power;
    
    public CmdFeedAndShoot(FeederSubsystem m_feeder, ShooterSubsystem m_shooter, double feederPower, double shooterPower) { //parameters take in one power for feeder, one power for shooter
     this.feeder_power = feederPower;
     this.shooter_power = shooterPower;
        addCommands(
            new CmdFeeder(m_feeder, feeder_power), //the feeder_power value here would correspond to feederPower from the constructor
            new CmdShooter(m_shooter, shooter_power)
        );
        
    }

    //no need for end because it contains commands which has end
  

    
}


