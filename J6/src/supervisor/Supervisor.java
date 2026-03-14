package supervisor;

import program.AbstractProgram;

public class Supervisor extends Thread
{
  private final AbstractProgram program;
  private volatile boolean supervising = true;

  public Supervisor(AbstractProgram program)
  {
    this.program = program;
    this.setName("Supervisor");
  }

  @Override
  public void run()
  {
    System.out.println("Supervisor started.");
    while (supervising)
    {
      try
      {
        program.State state = program.takeState();
        if (state == null)
        {
          System.out.println("Program terminated, supervisor exiting.");
          break;
        }
        handleState(state);
      }
      catch (InterruptedException e)
      {
        System.out.println("Supervisor interrupted.");
        break;
      }
    }
    System.out.println("Supervisor stopped.");
  }

  private void handleState(program.State state)
  {
    System.out.println("Supervisor detected state: " + state);
    switch (state)
    {
      case UNKNOWN:
        System.out.println("Program is in unknown state.");
        break;
      case RUNNING:
        System.out.println("Program is running normally.");
        break;
      case STOPPING:
        System.out.println("Program is stopping. Supervisor will restart it.");
        program.restartProgram();
        break;
      case FATAL_ERROR:
        System.out.println("Fatal error detected. Supervisor is shutting down the program.");
        program.shutdown();
        supervising = false;
        break;
      default:
        System.out.println("Unhandled state");
    }
  }

  public void startProgram()
  {
    program.startProgram();
  }

  public void stopProgram()
  {
    program.stopProgram();
  }

  public void shutdownSupervisor()
  {
    supervising = false;
    this.interrupt();
  }
}
