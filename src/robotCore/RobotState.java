package robotCore;

public final class RobotState 
{
	  public static boolean isDisabled() 
	  {
	    return RobotBase.getInstance().isDisabled();
	  }

	  public static boolean isEnabled() 
	  {
	    return RobotBase.getInstance().isEnabled();
	  }

	  public static boolean isEStopped() 
	  {
	    return false;
	  }

	  public static boolean isOperatorControl() 
	  {
	    return RobotBase.getInstance().isOperatorControl();
	  }

	  public static boolean isAutonomous() 
	  {
	    return RobotBase.getInstance().isAutonomous();
	  }

	  public static boolean isTest() {
	    return RobotBase.getInstance().isTest();
	  }

	  private RobotState() 
	  {
	  }
}
