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

import java.nio.ByteBuffer;

// import javax.lang.model.util.ElementScanner6;

/**
 * 
 * @author John Gaby
 * 
 * @brief The Navigator class uses the drive wheel encoders and a gyro to keep
 *        track of the robots absolute position on the field in real time.
 * 
 */
public class Navigator {
    private static final int k_maxRetry = 5;

    Device m_Device;
    double m_yaw = 0;
    double m_xPos = 0;
    double m_yPos = 0;
    double m_ticksPerFoot = 1;
    int m_leftSpeed = 0;
    int m_rightSpeed = 0;
    int m_leftPos = 0;
    int m_rightPos = 0;
    int m_updateCount = 0; // Count of update messages received
    int m_errorCount = 0; // Count of SPI errors
    int m_lastLeftSpeed = 0;
    int m_lastRightSpeed = 0;
    int m_maxSpeed = 2400;
    Object m_lock = new Object();
    private Device m_device = Device.getInstance();

    /**
     * 
     * @brief The NavigatorPos class is used to return the current navigator
     *        position data
     * 
     */
    public class NavigatorPos {
        /**
         * Specifies yaw of the robot in degrees. This value does not wrap when the
         * robot makes a complete 360 turn.
         * Depending on the state the invert flag, this may increase or decrease when
         * the
         * robot is rotating clockwise.
         */
        public final double yaw;
        /**
         * Specifies the x position of the robot. This value will be in encoder ticks
         * unless the ticksPerFoot value is set in which case it is in feet.
         */
        public final double x;
        /**
         * Specifies the y position of the robot. This value will be in encoder ticks
         * unless the ticksPerFoot value is set in which case it is in feet.
         */
        public final double y;
        /**
         * Specifies the speed of the left motor. This value will be in encoder
         * ticks/second unless the ticksPerFoot value is set in which case it will be
         * feet/second.
         */
        public final double leftSpeed;
        /**
         * Specifies the speed of the right motor. This value will be in encoder
         * ticks/second unless the ticksPerFoot value is set in which case it is in
         * feet/second.
         */
        public final double rightSpeed;
        /**
         * Specifies the left motor encoder position in encoder ticks
         */
        public final int leftPos;
        /**
         * Specifies the right motor encoder position in encoder ticks
         */
        public final int rightPos;

        private NavigatorPos(double yaw, double x, double y, int leftSpeed,
                int rightSpeed, int leftPos, int rightPos) // , int updateCount, int errorCount)
        {
            this.yaw = yaw;
            this.x = x / m_ticksPerFoot;
            this.y = y / m_ticksPerFoot;
            this.leftSpeed = leftSpeed / m_ticksPerFoot;
            this.rightSpeed = rightSpeed / m_ticksPerFoot;
            this.leftPos = leftPos;
            this.rightPos = rightPos;
        }
    }

    private void init(Encoder leftEncoder, Encoder rightEncoder) {
        int leftNo;
        int rightNo;

        if ((leftEncoder != null) && (rightEncoder != null)) {
            leftNo = leftEncoder.getEncoderNo();
            rightNo = rightEncoder.getEncoderNo();
        } else {
            leftNo = 0xff;
            rightNo = 0xff;
        }

        Logger.log("Navigator", 1, "Wait for initialization...");

        m_Device = Device.getInstance();

        for (int i = 0; i < k_maxRetry; i++) {
            int state;

            m_Device.sendCommand(Device.k_i2cDefAddr, TwoWire.k_initNavigator, (byte) leftNo, (byte) rightNo);

            /*
             * Wait for navigator to initialize
             */
            do {
                RobotBase.sleep(1000);

                state = m_Device.getByte(Device.k_i2cDefAddr, TwoWire.k_getNavigatorState);

                Logger.log("Navigator", 1, String.format("Waiting for navigator: state = %d", state));

                if (state >= 1) {
                    return; // success
                }
            } while (state == 0);

            Logger.log("Navigator", 1, "Init failed, retrying...");
        }

        throw new TwoWire.HardwareException("Navigator: max retries exceeded");
    }

    /**
     *
     * @param leftEncoder  - Specifies the encoder for the left motor
     * @param rightEncoder - Specifies the encoder for the right motor
     */
    public Navigator(Encoder leftEncoder, Encoder rightEncoder) {
        init(leftEncoder, rightEncoder);
    }

    /**
     * Gets the current position data for the robot.
     *
     * @return Returns the current position data.
     */
    public NavigatorPos getPos() {
        ByteBuffer data = m_device.getData(Device.k_i2cDefAddr, TwoWire.k_getNavigatorData, 24);

        try {
            m_yaw = data.getInt();
            m_xPos = data.getInt();
            m_yPos = data.getInt();
            m_leftSpeed = data.getShort();
            m_rightSpeed = data.getShort();
            m_leftPos = data.getInt();
            m_rightPos = data.getInt();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return (new NavigatorPos(m_yaw / 100.0, m_xPos, m_yPos, m_leftSpeed, m_rightSpeed, m_leftPos, m_rightPos));
    }

    /**
     * Gets only the yaw.
     *
     * @return Returns the yaw.
     */
    public double getYaw() {
        int yaw = m_device.getInt(Device.k_i2cDefAddr, TwoWire.k_getNavigatorYaw);

        return yaw / 100.00;
    }

    /**
     * Sets the number of ticks per foot
     *
     * @param ticks - Specifies the number of ticks per foot.
     */
    public void setTicksPerFoot(double ticks) {
        synchronized (m_lock) {
            m_ticksPerFoot = ticks;
        }
    }

    /**
     * Resets the yaw and position of the robot.
     *
     * @param yaw  - Specifies the new yaw value for the robot in degrees. This
     *             value will be adjusted so that it in the range -180 to 180.
     * @param xPos - Specifies the new x position of the robot. This value is in
     *             encoder ticks unless the ticksPerFoot value is set in which case
     *             it is in feet.
     * @param yPos - Specifies the new y position of the robot. This value is in
     *             encoder ticks unless the ticksPerFoot value is set in which case
     *             it is in feet.
     */
    public void reset(double yaw, double xPos, double yPos) {
        int y = ((int) (yaw * 100)) % 36000;

        if (y < -18000) {
            y += 36000;
        } else if (y > 18000) {
            y -= 36000;
        }

        m_yaw = y;
        m_xPos = xPos;
        m_yPos = yPos;
        m_leftPos = 0;
        m_rightPos = 0;

        // Logger.Log("Navigator", 1, String.format("yaw=%d", y));

        m_Device.sendCommand(Device.k_i2cDefAddr, TwoWire.k_resetNavigator, (int) (yaw * 100),
                (int) (m_xPos * m_ticksPerFoot), (int) (m_yPos * m_ticksPerFoot));
    }

    /**
     * Specifies that the sense of the <b>yaw</b> be inverted. I.E. controls whether
     * the <b>yaw</b> value increases or decreases when the robot turns clockwise.
     * In general, the <b>Pure Pursuit</b> path following assumes that the
     * <b>yaw</b> will
     * decrease when the robot turns clockwise.
     *
     * @param invert - If true, inverts the direction of the yaw
     */
    public void invert(boolean invert) {
        m_Device.sendCommand(Device.k_i2cDefAddr, TwoWire.k_invertNavigator, (byte) (invert ? 1 : 0));
    }
}
