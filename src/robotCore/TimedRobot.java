package robotCore;

public class TimedRobot extends IterativeRobotBase
{
	public static final double kDefaultPeriod = 0.02;

	protected TimedRobot()
	{
		this(kDefaultPeriod);
	}

	protected TimedRobot(double period) 
	{
		super(period);
	}

	@Override
	public void startCompetition() 
	{
		robotInit();

		long nextTime = System.currentTimeMillis() + m_period;
		
		while (true)
		{
			loopFunc();

			int sleep = (int) (nextTime - System.currentTimeMillis());

			nextTime += m_period;

			// System.out.println(String.format("sleep=%d", sleep));

			if (sleep > 0)
			{			
				RobotBase.sleep(sleep);
			}
		}
		
	}
}
