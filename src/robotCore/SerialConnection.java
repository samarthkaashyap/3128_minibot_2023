package robotCore;

import java.io.IOException;

public interface SerialConnection {
    public interface DataReceived
	{
		void received(String data);
	}
    
    public void open(SerialConnection.DataReceived receiver) throws Exception;
    public void writeln(String data) throws IllegalStateException, IOException;

}
