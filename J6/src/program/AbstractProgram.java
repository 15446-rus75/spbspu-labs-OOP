package program;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class AbstractProgram
{
  private final List<State> stateQueue = new LinkedList<>();
  private final Thread randomChanger;
  private final Thread workerThread;
  private volatile boolean running = true;
  private final Random random = new Random();
  private volatile State currentState = State.UNKNOWN;

  public AbstractProgram()
  {
    addState(State.UNKNOWN);

    randomChanger = new Thread(new RandomStateChanger());
    randomChanger.setName("ProgramRandomChanger");
    randomChanger.setDaemon(true);
    randomChanger.start();

    workerThread = new Thread(new ProgramWorker());
    workerThread.setName("ProgramWorker");
    workerThread.setDaemon(false);
    workerThread.start();
  }

  private void addState(State state)
  {
    synchronized (this)
    {
      stateQueue.add(state);
      currentState = state;
      this.notifyAll();
    }
    System.out.println(Thread.currentThread().getName() + " [Program] State changed to: " + state);
  }

  public State takeState() throws InterruptedException
  {
    synchronized (this)
    {
      while (stateQueue.isEmpty() && running)
      {
        this.wait();
      }
      if (!stateQueue.isEmpty())
      {
        return stateQueue.remove(0);
      }
      else
      {
        return null;
      }
    }
  }

  public void startProgram()
  {
    addState(State.RUNNING);
  }

  public void stopProgram()
  {
    addState(State.STOPPING);
  }

  public void restartProgram()
  {
    addState(State.RUNNING);
  }

  public void shutdown()
  {
    running = false;
    randomChanger.interrupt();
    if (workerThread != null)
    {
      workerThread.interrupt();
    }
    synchronized (this)
    {
      this.notifyAll();
    }
  }

  private class RandomStateChanger implements Runnable
  {
    @Override
    public void run()
    {
      while (running)
      {
        try
        {
          int delay = 2000 + random.nextInt(4000);
          Thread.sleep(delay);
          if (!running)
          {
            break;
          }
          State newState = getRandomState();
          addState(newState);
        }
        catch (InterruptedException e)
        {
          Thread.currentThread().interrupt();
          break;
        }
      }
    }

    private State getRandomState()
    {
      State[] states = {State.RUNNING, State.STOPPING, State.FATAL_ERROR};
      return states[random.nextInt(states.length)];
    }
  }

  private class ProgramWorker implements Runnable
  {
    @Override
    public void run()
    {
      while (running)
      {
        try
        {
          State state = currentState;
          switch (state)
          {
            case RUNNING:
              System.out.println("ProgramWorker: working...");
              Thread.sleep(1000);
              break;
            case STOPPING:
              Thread.sleep(100);
              break;
            case FATAL_ERROR:
              System.out.println("ProgramWorker: fatal error detected, worker stopping.");
              running = false;
              break;
            default:
              Thread.sleep(100);
              break;
          }
        }
        catch (InterruptedException e)
        {
          Thread.currentThread().interrupt();
          break;
        }
      }
      System.out.println("ProgramWorker stopped.");
    }
  }
}
