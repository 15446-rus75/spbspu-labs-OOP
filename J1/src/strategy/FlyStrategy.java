package strategy;

public class FlyStrategy implements MoveStrategy
{
  @Override
  public void move()
  {
    System.out.println("Hero is flying a plane\n");
  }
}
