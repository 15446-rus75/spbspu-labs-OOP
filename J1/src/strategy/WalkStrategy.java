package strategy;

public class WalkStrategy implements MoveStrategy
{
  @Override
  public void move()
  {
    System.out.println("Hero is walking somewhere now\n");
  }
}
