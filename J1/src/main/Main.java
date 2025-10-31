package main;

import hero.*;
import strategy.*;

public class Main
{
  public static void main(String[] args)
  {
    Hero hero = new Hero("LinuxPenguin");
    hero.move();

    hero.setMoveStrategy(new HorseRideStrategy());
    hero.move();

    hero.setMoveStrategy(new FlyStrategy());
    hero.move();

    hero.setMoveStrategy(new TeleportStrategy());
    hero.move();
  }
}
