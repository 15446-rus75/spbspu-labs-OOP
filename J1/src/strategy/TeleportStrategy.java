package strategy;

public class TeleportStrategy implements MoveStrategy
{
  @Override
  public void move()
  {
    System.out.println("Hero has teleported to Moon\n");
  }
}
