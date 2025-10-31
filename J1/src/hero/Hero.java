package hero;

import strategy.*;

public class Hero
{
  private String name;
  private MoveStrategy strategy;

  public Hero(String new_name)
  {
    this.name = new_name;
    this.strategy = new WalkStrategy();
  }

  public Hero(String new_name, MoveStrategy moveStrategy)
  {
    this.name = new_name;
    this.strategy = moveStrategy;
  }

  public void move()
  {
    strategy.move();
  }

  public void setMoveStrategy(MoveStrategy moveStrategy)
  {
    this.strategy = moveStrategy;
    System.out.println("Hero has changed his MoveStrategy\n");
  }

  public String getName()
  {
    return name;
  }
}
