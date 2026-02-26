package main;

import program.AbstractProgram;
import supervisor.Supervisor;

public class Main
{
  public static void main(String[] args)
  {
    AbstractProgram program = new AbstractProgram();
    Supervisor supervisor = new Supervisor(program);
    supervisor.start();

    try
    {
      Thread.sleep(2000);
      System.out.println("Main: starting program...");
      supervisor.startProgram();

      Thread.sleep(5000);
      System.out.println("Main: stopping program...");
      supervisor.stopProgram();

      Thread.sleep(5000);
      System.out.println("Main: exiting, will shutdown supervisor.");
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
    finally
    {
      supervisor.shutdownSupervisor();
      program.shutdown();
    }
  }
}
