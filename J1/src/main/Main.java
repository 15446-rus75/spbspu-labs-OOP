package main;

import hero.*;
import strategy.*;

import java.util.Scanner;

public class Main
{
  public static void main(String[] args)
  {
    if (args.length == 0)
    {
      System.out.println("Пожалуйста, укажите имя героя в аргументах командной строки");
      return;
    }
    String heroName = args[0];
    Hero hero = new Hero(heroName);
    Scanner scanner = new Scanner(System.in);
    System.out.println("Герой " + heroName + " создан!");
    System.out.println("Введите стратегию движения (Horse, Fly, Teleport, Walk) или 'exit' для выхода:");
    while (true)
    {
      String input = scanner.nextLine().trim();
      if (input.equalsIgnoreCase("exit"))
      {
        break;
      }
      switch (input.toLowerCase())
      {
        case "horse":
          hero.setMoveStrategy(new HorseRideStrategy());
          hero.move();
          break;
        case "fly":
          hero.setMoveStrategy(new FlyStrategy());
          hero.move();
          break;
        case "teleport":
          hero.setMoveStrategy(new TeleportStrategy());
          hero.move();
          break;
        case "walk":
          hero.setMoveStrategy(new WalkStrategy());
          hero.move();
          break;
        default:
          System.out.println("Неизвестная стратегия: " + input);
          break;
      }
      System.out.println("Введите следующую стратегию или 'exit' для выхода:");
    }
    scanner.close();
  }
}
