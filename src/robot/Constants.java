/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package robot;

import robotCore.Device;

/**
 * The Constants class provides a convenient place for teams to hold robot-wide
 * numerical or boolean constants. This class should not be used for any other
 * purpose. All constants should be declared globally (i.e. public static). Do
 * not put anything functional in this class.
 *
 * <p>
 * It is advised to statically import this class (or one of its inner classes)
 * wherever the constants are needed, to reduce verbosity.
 */
public final class Constants {
    public static class DrivetrainConstants {
        public final static double  k_ticksPerFoot = 7059.2 / 5.85;

        public static final int k_leftMotorPWMPin = Device.M1_1_PWM;
        public static final int k_leftMotorDirPin = Device.M1_1_DIR;
        public static final int k_rightMotorPWMPin = Device.M1_2_PWM;
        public static final int k_rightMotorDirPin = Device.M1_2_DIR;
        public static final int k_leftEncoderIntPin = Device.Q1_INT;
        public static final int k_leftEncoderDirPin = Device.Q1_DIR;
        public static final int k_rightEncoderIntPin = Device.Q2_INT;
        public static final int k_rightEncoderDirPin = Device.Q2_DIR;
    
        
    }

    public static class FeederConstants {
        public static final int k_PWMPin = Device.M2_1_PWM;
	    public static final int k_DirPin = Device.M2_1_DIR;
	    public static final int k_encPin1 = Device.Q4_INT;
	    public static final int k_encPin2 = -1;
    }

    public static class ShooterConstants {
        public static final int k_PWMPin = Device.M2_2_PWM;
        public static final int k_DirPin = Device.M2_2_DIR;
        public static final int k_encoderPin1 = Device.Q5_INT;
        public static final int k_encoderPin2 = -1;
    }

    public static class TurnTableConstants {
        public static final int k_PWMPin = Device.M3_1_PWM;
        public static final int k_DirPin = Device.M3_1_DIR;
        public static final int k_encoderIntPin = Device.Q3_INT;
        public static final int k_encoderDirPin = Device.Q3_DIR;
    }
}
