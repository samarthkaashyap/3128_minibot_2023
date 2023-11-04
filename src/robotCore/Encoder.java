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

import java.util.ArrayList;

//import robot.Robot;

/**
 * 
 * @author John Gaby
 * 
 * @brief The Encoder interface is implemented by classes which track an
 *        relative position.
 * 
 */
public class Encoder {
	/**
	 * 
	 * @brief Specifies the type of encoder.
	 * 
	 */

	public enum EncoderType {
		/**
		 * The encoder consists of a continuous turn potentiometer
		 */
		Analog,

		/**
		 * The encoder is quadrature with digital inputs
		 */
		Quadrature,

		/**
		 * Simple single input encoder which measures speed or distance
		 * but not direction
		 */
		Simple
	}

	private class Processor {
		private int m_addr;
		private int m_nextEncoder = 0;
		private long m_validPins = 0;
		private int m_validAnalogPins = 0;

		Processor(int addr) {
			m_addr = addr;
		}

		private long getValidPins() {
			if (m_validPins == 0) {
				m_validPins = m_device.getLong(m_addr, TwoWire.k_getValidPins);
			}

			// System.out.println(String.format("validPins = %x", m_validPins));

			return (m_validPins);
		}

		private int getValidAnalogPins() {
			if (m_validAnalogPins == 0) {
				m_validAnalogPins = m_device.getInt(m_addr, TwoWire.k_getValidAnalogPins);
			}

			System.out.println(String.format("validAnalogPins = %x", m_validAnalogPins));

			return (m_validAnalogPins);
		}

		private void verifyPin(int pin, boolean analog) {
			long validPins = analog ? getValidAnalogPins() : getValidPins();

			/*
			 * Check if pin is valid for this processor
			 */
			if ((pin < 0) || (pin >= 64) || (((1 << pin) & validPins) == 0)) {
				throw new TwoWire.HardwareException(
						String.format("Encoder: Invalid%s pin: %d", analog ? " Analog" : "", pin));
			}

			/*
			 * Check if pin is currently in use
			 */
			if (m_device.checkUsedPin(m_addr, pin)) {
				throw new TwoWire.HardwareException(String.format("Encoder: Pin %d is in use", pin));
			}
		}
	}

	static ArrayList<Processor> m_processors = new ArrayList<Processor>();

	Processor getProcessor(int addr) {
		Processor processor;

		for (int i = 0; i < m_processors.size(); i++) {
			processor = m_processors.get(i);

			if (processor.m_addr == addr) {
				return (processor);
			}
		}

		processor = new Processor(addr);
		m_processors.add(processor);

		return (processor);
	}

	// private static int m_nextAnalogEncoder = 0;
	// private static int m_nextQuadEncoder = 0;
	// private static int m_nextEncoder = 0;
	// private static Encoder[] m_encoders;
	private static ArrayList<Encoder> m_encoders = new ArrayList<Encoder>();
	private Device m_device = null;
	private int m_encoderNo = -1;
	private int m_zeroPosition = 0;
	private int m_i2cAddr = 0;
	// private boolean m_reset = true;

	private Encoder m_encoder = null; // Pointer to base encoder for cloned encoder
	// static private long m_startTime = System.currentTimeMillis();

	private Encoder(Encoder encoder) {
		m_encoder = encoder;

		if (RobotBase.TW) {
			m_device = Device.getInstance();
		} else {
			m_arduino = ArduinoConnection.getInstance();
		}
	}

	private void init(EncoderType type, int pin1, int pin2, int i2cAddr) {
		if (RobotBase.TW) {
			initTW(type, pin1, pin2, i2cAddr);
		} else {
			initArduino(type, pin1, pin2);
		}
	}

	private void initTW(EncoderType type, int pin1, int pin2, int i2cAddr) {
		m_device = Device.getInstance();

		Processor processor = getProcessor(i2cAddr);

		// System.out.println(String.format("intPin=%d, dataPin=%d", pin1, pin2));
		m_i2cAddr = i2cAddr;

		processor.verifyPin(pin1, type == EncoderType.Analog);
		m_device.setUsedPin(m_i2cAddr, pin1);

		if (pin2 >= 0) {
			processor.verifyPin(pin2, type == EncoderType.Analog);
			m_device.setUsedPin(m_i2cAddr, pin2);
		}

		m_encoderNo = processor.m_nextEncoder++;
		m_encoders.add(this);

		// SendCommand(String.format("e%dc%d %d %d", m_encoderNo, type.ordinal(), pin1,
		// pin2));
		m_device.sendCommand(m_i2cAddr, TwoWire.k_configureEncoder, (byte) m_encoderNo, (byte) type.ordinal(),
				(byte) pin1, (byte) pin2);
	}

	/**
	 * @param type    - Specifies the type of encoder
	 * @param pin1    - Specifies the first pin that the encoder uses. If it is a
	 *                quadrature encoder, this must be the interrupt pin
	 * @param pin2    - Specifies the second pin that the encoder uses (Not use if
	 *                type is simple)
	 * @param i2cAddr - Address of i2c arduino which reads encoder
	 */
	public Encoder(EncoderType type, int pin1, int pin2, int i2cAddr) {
		init(type, pin1, pin2, i2cAddr);
	}

	/**
	 * @param type - Specifies the type of encoder
	 * @param pin1 - Specifies the first pin that the encoder uses. If it is a
	 *             quadrature encoder, this must be the interrupt pin
	 * @param pin2 - Specifies the second pin that the encoder uses (Not use if type
	 *             is simple)
	 */
	public Encoder(EncoderType type, int pin1, int pin2) {
		init(type, pin1, pin2, Device.k_mainTwoWireId);
	}

	public Encoder(EncoderType quadrature) {
	}

	// ! @cond PRIVATE
	public int getEncoderNo() {
		return (m_encoderNo);
	}
	// ! @endcond

	/**
	 * Gets the current encoder value
	 * 
	 * @return Returns the current position or speed
	 */
	public int get() {
		return (getPosition());
	}

	private int getRawPosition() {
		if (RobotBase.TW) {
			return getRawPositionTW();
		} else {
			return getRawPositionArduino();
		}

	}

	private int getRawPositionTW() {
		if (m_encoder != null) {
			return (m_encoder.getRawPosition());
		}

		return (m_device.getInt(m_i2cAddr, TwoWire.k_getEncoderPos, (byte) m_encoderNo));
	}

	/**
	 * Gets the current encoder value
	 * 
	 * @return Returns the current position
	 */
	public int getPosition() {
		return (getRawPosition() - m_zeroPosition);
	}

	/**
	 * Gets the current encoder value
	 * 
	 * @return Returns the current speed
	 */
	public int getSpeed() {
		if (RobotBase.TW) {
			return getSpeedTW();
		} else {
			return getSpeedArduino();
		}
	}

	private int getSpeedTW() {
		if (m_encoder != null) {
			return (m_encoder.getSpeed());
		}

		return (m_device.getShort(m_i2cAddr, TwoWire.k_getEncoderSpeed, (byte) m_encoderNo));
	}

	/**
	 * Creates a copy of the encoder. The copy accesses the same physical
	 * device but can be reset independently.
	 * 
	 */
	public Encoder clone() {
		return (new Encoder(this));
	}

	/**
	 * Resets the encoder to zero
	 */
	public void reset() {
		m_zeroPosition = getRawPosition();
	}

	/**
	 * @param invert = If True, the count and speed directions are reversed
	 * 
	 */
	public void setInverted(boolean invert) {
		if (RobotBase.TW) {
			setInvertedTW(invert);
		} else {
			setInvertedArduino(invert);
		}

	}

	private void setInvertedTW(boolean invert) {
		if (RobotBase.TW) {
			m_device.sendCommand(m_i2cAddr, TwoWire.k_setEncoderInverted, (byte) m_encoderNo, (byte) (invert ? 1 : 0));
		} else {
			sendCommand(String.format("e%di%c", m_encoderNo, invert ? 't' : 'f'));
		}
	}

	// Arduino Version

	private int m_position = 0;
	private int m_speed = 0;
	private long m_lastUpdate = 0;
	private ArduinoConnection m_arduino = null;
	private int m_count = 0;
	private static int m_nextEncoder = 0;

	private void setValues(int position, int speed) {
		// Logger.Log("Encoder", 0, String.format("SetValues: %d %d", position, speed));

		synchronized (this) {
			m_position = position;
			m_speed = speed;
			m_lastUpdate = System.currentTimeMillis();
			// Logger.Log("Encoder", 1, String.format("SetValues: no = %d, lastUpdate = %d",
			// m_encoderNo, m_lastUpdate));
		}
	}

	// ! @cond PRIVATE
	public static void command(String args) {
		// Logger.Log("Encoder", 1, "Command: " + args);

		if (m_encoders != null) {
			int a[] = RobotBase.parseIntegers(args, 3);

			if (a != null) {
				int encoderNo = a[0];

				if ((encoderNo >= 0) && (encoderNo < m_encoders.size())) {
					m_encoders.get(encoderNo).setValues(a[1], a[2]);
				}
			}
		}
	}

	private void sendCommand(String cmd) {
		m_arduino.sendCommand(cmd);
	}

	private void checkUpdates() {
		int i = 0;

		// Logger.Log("Encoder", 1, String.format("encoderNo=%d,lastUpdate=%d",
		// m_encoderNo, m_lastUpdate));

		while ((m_lastUpdate == 0) && (i < 3)) {
			// Logger.Log("Encoder", 1, String.format("CheckUpdates: e%ds 25 0",
			// m_encoderNo));
			sendCommand(String.format("e%ds 25 0", m_encoderNo));

			if (++m_count > 10) {
				Logger.log("Encoder", 9, String.format("CheckUpdates: encoder = %d, count = %d", m_encoderNo, m_count));
			}

			RobotBase.sleep(50);

			i++;
		}
	}

	private void initArduino(EncoderType type, int pin1, int pin2) {
		m_arduino = ArduinoConnection.getInstance();

		// Processor processor = getProcessor(4);

		m_encoderNo = m_nextEncoder++;
		m_encoders.add(this);

		// Logger.Log("Encoder", 1, String.format("Init: no=%d", m_encoderNo));

		sendCommand(String.format("e%dc%d %d %d", m_encoderNo, type.ordinal(), pin1, pin2));
	}

	private int getRawPositionArduino() {
		if (m_encoder != null) {
			return (m_encoder.getRawPositionArduino());
		}

		checkUpdates();

		synchronized (this) {
			return (m_position);
		}
	}

	private int getSpeedArduino() {
		if (m_encoder != null) {
			return (m_encoder.getSpeed());
		}

		checkUpdates();

		synchronized (this) {
			return (m_speed);
		}
	}

	private void setInvertedArduino(boolean invert) {
		sendCommand(String.format("e%di%c", m_encoderNo, invert ? 't' : 'f'));
	}
	// ! @endcond
}
