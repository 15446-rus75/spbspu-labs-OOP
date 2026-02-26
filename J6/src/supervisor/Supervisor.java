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
        Thread.currentThread().interrupt();
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
      case STOPPING:
        System.out.println("Supervisor: restarting program...");
        program.restartProgram();
        break;
      case FATAL_ERROR:
        System.out.println("Supervisor: fatal error, shutting down program...");
        program.shutdown();
        supervising = false;
        break;
      default:
        break;
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
