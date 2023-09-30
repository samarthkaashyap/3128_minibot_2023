
package robotCore;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;


import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

public class TwoWire {
    /*
     * No response commands should be in the range 1-127
     */
    public static final int k_cmdKeepAlive = 1;
    public static final int k_configureMotor = 2;
    public static final int k_setMotor =  3;
    public static final int k_configureEncoder = 4;
    public static final int k_setMotorMode = 5;
    public static final int k_setFeedbackDevice = 6;
    public static final int k_setPIDFTerm = 7;
    public static final int k_setPIDPTerm = 8;
    public static final int k_setPIDITerm = 9;
    public static final int k_setPIDDTerm = 10;
    public static final int k_setPIDIZone = 11;
    public static final int k_enable = 12;
    public static final int k_disable = 13;
    public static final int k_setMotorInverted = 14;
	public static final int k_setEncoderInverted = 15;
    public static final int k_initNavigator = 16;
    public static final int k_resetNavigator = 17;
    public static final int k_invertNavigator = 18;
    public static final int k_digitalWrite = 19;
    public static final int k_createCounter = 20;
    public static final int k_setMinMotorPower = 21;

    /*
     * Response commands should be in the range 129-255
     */
    public static final int k_cmdProcessorType = 129;
    public static final int k_getValidPins = 130;
    public static final int k_getValidPWMPins = 131;
    public static final int k_getMaxMotors = 132;
    public static final int k_getValidAnalogPins = 133;
    public static final int k_getEncoderPos = 134;
    public static final int k_getEncoderSpeed = 135;
    public static final int k_digitalRead = 136;
    public static final int k_getNavigatorData = 137;
    public static final int k_getNavigatorState = 138;
    public static final int k_getNavigatorYaw = 139;
    public static final int k_getDigitalCount = 140;

    /*
     * This exception is thrown when the max retries is exceeded when
     *  sending commands to the Arduino. This probably means that
     *  communication with the Arduino is permanently impaired.
     */
    public static class HardwareException extends RuntimeException
    {
        HardwareException(String msg)
        {
            super(msg);
        }
    }

    private class Command
    {
        private int m_addr;
        private int m_responseSize;
        private byte[] m_command;
        private byte[] m_response;

        /*
         * Simple command, no data
         *  Format: [size=4] [packetNo] [command] [cksum]
         */
        Command(int addr, int command, int responseSize)
        {
            m_addr = addr;
            m_responseSize = responseSize;
            m_command = new byte[4];
            m_command[0] = 4;
            m_command[2] = (byte) command;
        }

        /*
         * command with byte argument
         *  Format: [size=5] [packetNo] [command] [byte] [cksum]
         */
        Command(int addr, int command, int responseSize, byte byte1)
        {
            m_addr = addr;
            m_responseSize = responseSize;
            m_command = new byte[4+1];
            m_command[0] = (byte) m_command.length;
            m_command[2] = (byte) command;
            m_command[3] = byte1;
        }

        /*
         * Command with a byte and 16 bit argument
         *  Format [size=6] [packetNo] [command] [byte] [16bit data] [cksum]
         */
        Command(int addr, int command, int responseSize, byte byte1, short value)
        {
            m_addr = addr;
            m_responseSize = responseSize;
            m_command = new byte[4+3];
            m_command[0] = (byte) m_command.length;
            m_command[2] = (byte) command;
            m_command[3] = byte1;

            ByteBuffer buffer = ByteBuffer.wrap(m_command, 4, m_command.length - 4);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.putShort(value);
        }

        /*
         * Command with 4 bytes of data
         *  Format [size=8] [packetNo] [command] [byte1] [byte2] [byte3] [byte4] [cksum];
         */
        Command(int addr, int command, int responseSize, byte byte1, byte byte2, byte byte3, byte  byte4)
        {
            // System.out.println(String.format("command3: byte2=%d", byte2));
            m_addr = addr;
            m_responseSize = responseSize;
            m_command = new byte[4+4];
            m_command[0] = (byte) m_command.length;
            m_command[2] = (byte) command;
            m_command[3] = byte1;
            m_command[4] = byte2;
            m_command[5] = byte3;
            m_command[6] = byte4;
        }

        /*
         * Command with a byte and float arguments
         *  Format[size=9] [packetNo] [command] [byte] [float (32 bits)] [cksum]
         */
        Command(int addr, int command, int responseSize, byte byte1, float value)
        {
            m_addr = addr;
            m_responseSize = responseSize;
            m_command = new byte[4+5];
            m_command[0] = (byte) m_command.length;
            m_command[2] = (byte) command;
            m_command[3] = byte1;
            
            Logger.log("TwoWire", 1, String.format("float=%f", value));

            ByteBuffer buffer = ByteBuffer.wrap(m_command, 4, m_command.length - 4);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.putFloat(value);
        }

        /*
         * Command with two bytes of arguments
         *  Format [size=6] [packetNo] [command] [byte1] [byte2] [cksum]
         */
        Command(int addr, int command, int responseSize, byte byte1, byte byte2)
        {
            m_addr = addr;
            m_responseSize = responseSize;
            m_command = new byte[4+2];
            m_command[0] = (byte) m_command.length;
            m_command[2] = (byte) command;
            m_command[3] = byte1;
            m_command[4] = byte2;
        }

        Command(int addr, int command, int responseSize, int yaw, int x, int y)
        {
            m_addr = addr;
            m_responseSize = responseSize;
            m_command = new byte[4+12];
            m_command[0] = (byte) m_command.length;
            m_command[2] = (byte) command;

            ByteBuffer buffer = ByteBuffer.wrap(m_command, 3, m_command.length - 3);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.putInt(yaw);
            buffer.putInt(x);
            buffer.putInt(y);
        }
    }

    private class Device
    {
        private int m_addr;
        private I2CDevice m_device;
        private int m_packetNo = 0;
        private long m_usedPins = 0;

        private static final int k_writeDelay = 5;
        private static final int k_readDelay = 5;
        private static final int k_errDelay = 10;
        private static final int k_maxReadRetry = 20;
        private static final int k_maxTries = 10;
        // private static final int k_maxCommandLength = 30;
        private static final int k_maxResponseLength = 30;
        private static final int k_ack = 0x55;
        private static final int k_ack2 = 0x56;
        // private static final int k_nak = 0xcc;
        private static final int k_nak2 = 0xcd;
        
        public Device(int addr)
        {
            m_addr = addr;

            try {
                m_device = m_i2cBus.getDevice(addr);
            } catch (IOException e) {
                e.printStackTrace();
                m_device = null;
            }
        }

        private byte computeCksum(byte[] data, int length)
        {
            byte cksum = 0;
            for (int i = 0 ; i < length ; i++)
            {
                cksum += data[i];
            }

            return(cksum);
        }

        private void addCksum(byte[] data)
        {

            data[data.length - 1] = computeCksum(data, data.length - 1);
        }

        private void sleep(long ms)
        {
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }   
        
        private byte[] readResponse(int responseSize) throws IOException
        {
            byte[] response = new byte[k_maxResponseLength];

            for (int retry = 0 ; retry < k_maxReadRetry ; retry++)
            {
                response[0] = (byte) 0xff;      // in case the read fails

                m_device.read(response, 0, responseSize);   //response.length);
                
                int ack = ((int) response[0]) & 0xff;

            //    if (ack == 0xd5)
            //    {
                //    robotCore.Device.GetInstance().trigger();
                   
                //    System.out.println(responseSize);
                //    for (int i = 0 ; i < responseSize ; i++)
                //    {
                //        System.out.print(String.format("%x,", response[i]));
                //    }
                //    System.out.println("");

                //    while (true);
            //    }

                // System.out.println(String.format("ack=%d", ack));

                if (ack == k_ack)
                {
                    int size = response[1];

                    if ((size >= 3) && (response[2] == (byte) m_packetNo))
                    {
                        // A cksum is only if there is data present
                        if ((size == 3) || computeCksum(response, size - 1) == response[size - 1])
                        {
                            m_packetNo++;

                            return(Arrays.copyOfRange(response, 0, size));			// Packet delivered, return response
                        }
                        else
                        {
                            System.out.println("Invalid response cksum");
                        }
                    }
                    else
                    {
                        System.out.println(String.format("Invalid packet: size=%d, packetNo: %d:%d", response[1], (byte) m_packetNo, ack));
                        // return(null);
                    }

                    System.out.println(String.format("c=%d, e=%d", m_packetCount, m_responseErrors));
                }
                else if (ack == k_ack2)
                {
                    // System.out.println(String.format("%d: Retry read", retry));
                    sleep(k_readDelay);
                }
                else if (ack == k_nak2)
                {
                    System.out.println(String.format("%d: Queue Overflow", m_packetNo));
                    sleep(k_errDelay);
                    return(null);
                }
                else
                {
                    System.out.println(String.format("ack = %x", ack));
                    sleep(k_errDelay);
                    return(null);
                }
            }

            // sleep(1000);

            System.out.println(String.format("TwoWire: lastCommand=%d", m_lastCommand));
            throw new HardwareException("Max read retries exceeded");

            // return(false);
        }

        int m_lastCommand = 0;
        int m_packetCount = 0;
        int m_packetErrors = 0;
        int m_responseErrors = 0;

        /*
         * Packet format [size] [packet #] [command] [data...] [cksum]
         */
        private byte[] sendPacket(byte[] data, int responseSize)
        {
            int count;

            m_packetCount++;

            // System.out.println(String.format("sendPacket: cmd=%d, responseSize = %d", data[2] & 0xff, responseSize));

            m_lastCommand = ((int) data[2]) & 0xff;
 
            data[1] = (byte) m_packetNo;

            addCksum(data);

            for (count = 0 ; count < k_maxTries ; count++)
			{
				try {
					// System.out.println(String.format("%d: Write data: %d", count, m_packetNo));
                    byte[] response;

					m_device.write(data);

                    // sleep(1);

                    if ((response = readResponse(responseSize + 4)) != null) 
                    {
                        return(response);
                    }					
				} catch (IOException e) {
                    // robotCore.Device.GetInstance().trigger();
					// System.out.println("IOException");
				}

                m_packetErrors++;

                if ((m_packetErrors % 10) == 0)
                {
                    Logger.log("TwoWire", 1, String.format("Write Retry: cmd=%d c=%d, e=%d", m_lastCommand, m_packetCount, m_packetErrors));
                }

				sleep(k_writeDelay);
			}
            
            throw new HardwareException("Max write retries exceeded");        // Something is seriously wrong
        }

        private long getUsedPins()
        {
            return(m_usedPins);
        }

        private void setUsedPin(int pin)
        {
            m_usedPins |= (1L << pin);
        }
    }
    
    private I2CBus m_i2cBus;
    private ArrayList<Device> m_devices = new ArrayList<Device>();
    private ArrayBlockingQueue<Command> m_commands = new ArrayBlockingQueue<Command>(10);

    public TwoWire()
    {
        try {
            m_i2cBus = I2CFactory.getInstance(I2CBus.BUS_1);
        } catch (UnsupportedBusNumberException | IOException e) {
            e.printStackTrace();
        }

        /*
         * Start the thread that will actually send packets
         *   This is the ONLY thread that will access the I2C bus
         */
        new Thread(
            new Runnable(){
                public void run(){
                    while (true)
                    {
                        try {
                            Command command = m_commands.take();
                            // System.out.println(String.format("sendPacket: addr=%d, cmd=%d, count=%d", command.m_addr, command.m_command[2], m_commands.size()));
                            // if (command.m_command[2] == 13)
                            // {
                            //     throw new HardwareException(String.format("length=%d", command.m_command[0]));
                            // }
                            Device device = getDevice(command.m_addr);
                            command.m_response = device.sendPacket(command.m_command, command.m_responseSize);

                            synchronized(command)
                            {
                                command.notify();
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            ).start();
    }

    private Device getDevice(int addr)
    {
        Device device;

        for (int i = 0 ; i < m_devices.size() ; i++)
        {
            device = m_devices.get(i);

            if (addr == device.m_addr)
            {
                return(device);
            }
        }

        // System.out.println(String.format("TwoWire: Adding device: %d", addr));
        if (addr == 0)
        {
            throw new HardwareException("Addr is zero");
        }

        device = new Device(addr);

        m_devices.add(device);

        return(device);
    }

    /*
     * Send command, no response expected
     */
    private void sendCommand(Command command)
    {
        try {
            // if (command.m_command[2] == 13)
            // {
            //     throw new HardwareException("test");
            // }
            // System.out.println(String.format("sendCommand:%d", command.m_command[2]));
            m_commands.put(command);
            // System.out.println("put complete");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*
     * Command request, wait for response
     */
    private ByteBuffer sendRequest(Command command)
    {
        try {
            if (command.m_command[2] == 13)
            {
                throw new HardwareException("13");
            }
            // System.out.println(String.format("sendRequest:%d", command.m_command[2]));
            m_commands.put(command);

            synchronized(command)
            {
                ByteBuffer buffer;

                command.wait();
                
                buffer = ByteBuffer.wrap(command.m_response, 3, command.m_response.length - 3);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                // buffer.get();buffer.get();buffer.get();     // Advance to start of data

                return(buffer);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
         
        return null;
    }

    /*
     * Simple command, no arguments
     */
    public void sendCommand(int addr, int commandId)
    {
        Command command = new Command(addr, commandId, 0);

        sendCommand(command);
    }

    public void sendCommandToAll(int commandId)
    {
        for (int i = 0 ; i < m_devices.size() ; i++)
        {
            Device device = m_devices.get(i);
            Command  command = new Command(device.m_addr, commandId, 0);

            sendCommand(command);
        }
    }

    /*
     * Command with a byte argument
     */
    public void sendCommand(int addr, int commandId, byte byte1)
    {
        Command command = new Command(addr, commandId, 0, byte1);

        sendCommand(command);
    }

    /*
     * Command with a byte and a 16 bit argument
     */
    public void sendCommand(int addr, int commandId, byte byte1, short value)
    {
        Command command = new Command(addr, commandId, 0, byte1, value);

        sendCommand(command);
    }

    /*
     * Command with 4 bytes of data
     */
    public void sendCommand(int addr, int commandId, byte byte1, byte byte2, byte byte3, byte byte4)
    {
        Command command = new Command(addr, commandId, 0, byte1, byte2, byte3, byte4);

        sendCommand(command);
    }

    /*
     * Command with byte and a float (32 bit) arguments
     */
    public void sendCommand(int addr, int commandId, byte byte1, float value)
    {
        Command command = new Command(addr, commandId, 0, byte1, value);

        sendCommand(command);
    }

    /*
     * Command with two bytes as arguments
     */
    public void sendCommand(int addr, int commandId, byte byte1, byte byte2)
    {
        Command command = new Command(addr, commandId, 0, byte1, byte2);

        sendCommand(command);
    }

    public void sendCommand(int addr, int commandId, int yaw, int x, int y)
    {
        Command command = new Command(addr, commandId, 0, yaw, x, y);

        sendCommand(command);
    }

    /*
     * Simple request, no arguments
     *  Note: the Arduino will only wait for a response if the commandId is greater than 127
     */
    public ByteBuffer sendRequest(int addr, int commandId, int size)
    {
        return(sendRequest(new Command(addr, commandId, size)));
    }

    /*
     * One byte argument
     */
    public ByteBuffer sendRequest(int addr, int commandId, int size, byte byte1)
    {
        return(sendRequest(new Command(addr, commandId, size, byte1)));
    }

    /*
     * Simple request, no arguments, returns byte
     */
    public int sendRequestByte(int addr, int commandId)
    {
        ByteBuffer buffer = sendRequest(addr, commandId, 1);

        return(buffer.get());
    }

    /*
     * One byte argument, returns byte
     */
    public int sendRequestByte(int addr, int commandId, byte byte1)
    {
        ByteBuffer buffer = sendRequest(addr, commandId, 1, byte1);

        return(buffer.get());
    }

    /*
     * No arguments, returns int (32 bits)
     */
    public int sendRequestInt(int addr, int commandId)
    {
        ByteBuffer buffer = sendRequest(addr, commandId, 4);

        return(buffer.getInt());
    }

    /*
     * No arguments, returns long (64 bits)
     */
    public long sendRequestLong(int addr, int commandId)
    {
        ByteBuffer buffer = sendRequest(addr, commandId, 8);

        return(buffer.getLong());
    }

    /*
     * One byte argument, returns int (32 bits)
     */
    public int sendRequestInt(int addr, int commandId, byte byte1)
    {
        ByteBuffer buffer = sendRequest(addr, commandId, 4, byte1);

        return(buffer.getInt());
    }

    /*
     * One byte argument, returns short (16 bits)
     */
    public int sendRequestShort(int addr, int commandId, byte byte1)
    {
        ByteBuffer buffer = sendRequest(addr, commandId, 2, byte1);

        return(buffer.getShort());
    }

    public boolean checkUsedPin(int addr, int pin)
    {
        Device device = getDevice(addr);

        return((device.getUsedPins() & (1L << pin)) != 0);
    }

    public void setUsedPin(int addr, int pin)
    {
        Device device = getDevice(addr);

        device.setUsedPin(pin);
    }
}
