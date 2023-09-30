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

// import java.io.IOException;
import java.nio.ByteBuffer;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

//import robot.Robot;

/**
 * 
 * @brief Device.
 * 
 *        This class controls the microcontroller device.
 */
public class Device {
	final static boolean PCTest = false;

	public final static int PA0 = 0;
	public final static int PA1 = 1;
	public final static int PA2 = 2;
	public final static int PA3 = 3;
	public final static int PA4 = 4;
	public final static int PA5 = 5;
	public final static int PA6 = 6;
	public final static int PA7 = 7;
	public final static int PA8 = 8;
	public final static int PA9 = 9;
	public final static int PA10 = 10;
	public final static int PA11 = 11;
	public final static int PA12 = 12;
	public final static int PA13 = 13;
	public final static int PA14 = 14;
	public final static int PA15 = 15;

	public final static int PB0 = 16;
	public final static int PB1 = 17;
	public final static int PB2 = 18;
	public final static int PB3 = 19;
	public final static int PB4 = 20;
	public final static int PB5 = 21;
	public final static int PB6 = 22;
	public final static int PB7 = 23;
	public final static int PB8 = 24;
	public final static int PB9 = 25;
	public final static int PB10 = 26;
	public final static int PB11 = 27;
	public final static int PB12 = 28;
	public final static int PB13 = 29;
	public final static int PB14 = 30;
	public final static int PB15 = 31;

	public final static int PC13 = 32;
	public final static int PC14 = 33;
	public final static int PC15 = 34;

	/**
	 * Interrupt pin for Quad Encoder #1
	 */
	public final static int Q1_INT = PB3;
	/**
	 * Direction pin for Quad Encoder #1
	 */
	public final static int Q1_DIR = PA15;
	/**
	 * Interrupt pin for Quad Encoder #2
	 */
	public final static int Q2_INT = PB4;
	/**
	 * Direction pin for Quad Encoder #1
	 */
	public final static int Q2_DIR = PA12;
	/**
	 * Interrupt pin for Quad Encoder #3
	 */
	public final static int Q3_INT = PB5;
	/**
	 * Direction pin for Quad Encoder #1
	 */
	public final static int Q3_DIR = PC13;
	/**
	 * Interrupt pin for Quad Encoder #4
	 */
	public final static int Q4_INT = PB1;
	/**
	 * Direction pin for Quad Encoder #1
	 */
	public final static int Q4_DIR = PC14;
	/**
	 * Interrupt pin for Quad Encoder #5
	 */
	public final static int Q5_INT = PB0;
	/**
	 * Direction pin for Quad Encoder #1
	 */
	public final static int Q5_DIR = PC15;

	/**
	 * Direction pin for Motor 1 on connector 1
	 */
	public final static int M1_1_DIR = PA4;
	/**
	 * PWM pin for Motor 1 on connector 1
	 */
	public final static int M1_1_PWM = PA1;
	/**
	 * Direction pin for Motor 2 on connector 1
	 */
	public final static int M1_2_DIR = PA5;
	/**
	 * PWM pin for Motor 2 on connector 1
	 */
	public final static int M1_2_PWM = PA0;

	/**
	 * Direction pin for Motor 1 on connector 2
	 */
	public final static int M2_1_DIR = PB15;
	/**
	 * PWM pin for Motor 1 on connector 2
	 */
	public final static int M2_1_PWM = PA3;
	/**
	 * Direction pin for Motor 2 on connector 2
	 */
	public final static int M2_2_DIR = PB14;
	/**
	 * PWM pin for Motor 2 on connector 2
	 */
	public final static int M2_2_PWM = PA2;

	/**
	 * Direction pin for Motor 1 on connector 3
	 */
	public final static int M3_1_DIR = PB13;
	/**
	 * PWM pin for Motor 1 on connector 3
	 */
	public final static int M3_1_PWM = PA7;
	/**
	 * Direction pin for Motor 2 on connector 3
	 */
	public final static int M3_2_DIR = PB12;
	/**
	 * PWM pin for Motor 2 on connector 3
	 */
	public final static int M3_2_PWM = PA6;

	/**
	 * General I/O pin 1
	 */
	public final static int IO_1 = PA8;
	/**
	 * General I/O pin 2
	 */
	public final static int IO_2 = PA9;
	/**
	 * General I/O pin 3
	 */
	public final static int IO_3 = PA10;
	/**
	 * General I/O pin 4
	 */
	public final static int IO_4 = PA11;

	/**
	 * Analog pin 1
	 */
	public final static int A1_1 = PA0;
	/**
	 * Analog pin 2
	 */
	public final static int A1_2 = PA1;
	/**
	 * Analog pin 3
	 */
	public final static int A2_1 = PA2;
	/**
	 * Analog pin 4
	 */
	public final static int A2_2 = PA3;

	// ! @cond PRIVATE
	public final static int k_i2cDefAddr = 5;

	public enum ProcessorType {
		/**
		 * Processor type is currently unknown
		 */
		Unknown,

		/**
		 * Processor is an Arduino Nano
		 */
		Arduino,

		/**
		 * Processor is a STM32
		 */
		STM32
	}

	private ProcessorType m_type = ProcessorType.Unknown;
	// private Serial m_serial;
	private SerialConnection m_serialConnection;
	private static Device m_device;
	private String m_command = "";

	@SuppressWarnings("unused")
	private int m_maxAnalogInputs = 0;
	// private int m_maxDigitalInputs = 0;
	// private int m_maxMotors = 0;
	// private int m_maxAnalogEncoders = 0;
	// private int m_maxQuadEncoders = 0;
	// private int m_maxLEDStrings = 0;
	// private int m_maxMotionProfile = 0;

	@SuppressWarnings("unused")
	private int m_pins = 0;
	// private Timer m_pingTimer = new Timer();
	// private int[] m_analog = new int[8];

	private final GpioController m_gpio;
	private final GpioPinDigitalOutput m_resetPin;
	private final GpioPinDigitalOutput m_trigPin;

	/*
	 * TwoWire
	 */
	public final static int k_mainTwoWireId = 5; // ID of main Arduino
	public final static int k_processorArduino = 1;
	public final static int k_processorSTM32 = 2;
	private final TwoWire m_twoWire = new TwoWire();
	// private int m_validPins = 0;

	/*
	 * Requests
	 */
	private final int k_requestProcessorType = 129;

	public enum PinMode {
		Input,
		Output
	};

	private Device() {
		if (!PCTest) {
			m_gpio = GpioFactory.getInstance();
			m_resetPin = m_gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "Reset", PinState.HIGH);
			m_resetPin.setMode(com.pi4j.io.gpio.PinMode.DIGITAL_OUTPUT);
			m_resetPin.high();
			// System.out.println("Set GPI01 high");
			m_trigPin = m_gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "Trigger", PinState.HIGH);
			m_trigPin.high();
		} else {
			m_gpio = null;
			m_resetPin = null;
			m_trigPin = null;
		}
	}

	/**
	 * @return Returns the type of processor. Currently Arduino Nano and STM32 are
	 *         supported.
	 */
	public ProcessorType getProcessorType(int addr) {
		int type = m_twoWire.sendRequestByte(addr, k_requestProcessorType);

		if (type == k_processorArduino) {
			return (ProcessorType.Arduino);
		} else if (type == k_processorSTM32) {
			return (ProcessorType.STM32);
		}

		Logger.log("Device", 9, String.format("Unknown processor type: %d", type));

		return (ProcessorType.Unknown);
	}

	public ProcessorType getProcessorType() {
		return (getProcessorType(k_mainTwoWireId));
	}

	// public int getValidPins(int addr)
	// {
	// return(m_twoWire.sendRequestInt(addr, TwoWire.k_getValidPins));
	// }

	// public int getValidPWMPins(int addr)
	// {
	// return(m_twoWire.sendRequestInt(addr, TwoWire.k_getValidPWMPins));
	// }

	public long getLong(int addr, int command) {
		return (m_twoWire.sendRequestLong(addr, command));
	}

	public int getInt(int addr, int command) {
		return (m_twoWire.sendRequestInt(addr, command));
	}

	public int getInt(int addr, int command, byte byte1) {
		return (m_twoWire.sendRequestInt(addr, command, byte1));
	}

	public int getShort(int addr, int command, byte byte1) {
		return (m_twoWire.sendRequestShort(addr, command, byte1));
	}

	public int getByte(int addr, int command) {
		return (m_twoWire.sendRequestByte(addr, command));
	}

	public int getByte(int addr, int command, byte byte1) {
		return (m_twoWire.sendRequestByte(addr, command, byte1));
	}

	public ByteBuffer getData(int addr, int command, int size) {
		return (m_twoWire.sendRequest(addr, command, size));
	}

	public boolean checkUsedPin(int addr, int pin) {
		return (m_twoWire.checkUsedPin(addr, pin));
	}

	public void setUsedPin(int addr, int pin) {
		m_twoWire.setUsedPin(addr, pin);
	}

	private void waitForReset() {
		Logger.log("RobotBase", 0, "Waiting for response");

		RobotBase.sleep(3000);

		m_type = getProcessorType();
		Logger.log("RobotBase", 0, "Processor type = " + m_type);
	}

	private void resetDevice() {
		Logger.log("RobotBase", 2, "ResetArduino");

		if (!PCTest) {
			m_resetPin.low();
			RobotBase.sleep(10);
			m_resetPin.high();

			waitForReset();
		}
	}

	public void trigger() {
		m_trigPin.low();
		RobotBase.sleep(1);
		m_trigPin.high();
	}

	public static Device getInstance() {
		if (m_device == null) {
			m_device = new Device();

			try {
				m_device.start();
				// m_device.SetTimeout(1500);
			} catch (InterruptedException ex) {

			}
		}

		return (m_device);
	}

	private void dataReceived(String data) {
		int idx;

		// System.out.println("DataReceived: " + data);

		while ((idx = data.indexOf('\n')) >= 0) {
			m_command += data.substring(0, idx);
			// ProcessCommand(m_command);
			Logger.log("Device", 1, String.format("STM:%s", m_command));

			m_command = "";

			data = data.substring(idx + 1);
		}

		m_command += data;
	}

	// ! @cond PRIVATE
	public void start() throws InterruptedException {
		Logger.log("Device", 1, "Start receiver");

		try {
			if (!PCTest) {
				if (m_serialConnection != null) {
					m_serialConnection.open((d) -> {
						dataReceived(d);
					});
				}
			}
		} catch (Exception ex) {
			System.out.println(" ==>> SERIAL SETUP FAILED : " + ex.getMessage());
			return;
		}

		resetDevice();

		/*
		 * Start keep-alive thread
		 */
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					// System.out.println("Sending keep alive");
					sendCommandToAll(TwoWire.k_cmdKeepAlive);

					try {
						Thread.sleep(500);
					} catch (InterruptedException ex) {

					}
				}
			}

		}).start();
	}
	// ! @endcond

	public void sendCommandToAll(int command) {
		m_twoWire.sendCommandToAll(command);
	}

	public void sendCommand(int addr, int command, byte byte1, byte byte2, byte byte3, byte byte4) {
		m_twoWire.sendCommand(addr, command, byte1, byte2, byte3, byte4);
	}

	public void sendCommand(int addr, int command, byte byte1, short value) {
		m_twoWire.sendCommand(addr, command, byte1, value);
	}

	public void sendCommand(int addr, int command, byte byte1, float value) {
		m_twoWire.sendCommand(addr, command, byte1, value);
	}

	public void sendCommand(int addr, int command, byte byte1, byte byte2) {
		m_twoWire.sendCommand(addr, command, byte1, byte2);
	}

	public void sendCommand(int addr, int command, int yaw, int x, int y) {
		m_twoWire.sendCommand(addr, command, yaw, x, y);
	}

	public void sendCommand(int addr, int command, byte byte1) {
		m_twoWire.sendCommand(addr, command, byte1);
	}
	// ! @endcond

}
