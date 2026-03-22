package cli;

import java.util.Scanner;

public class ConsoleInputHandler
{
  private final Scanner scanner;

  public ConsoleInputHandler()
  {
    this.scanner = new Scanner(System.in);
  }

  public int readInt(String prompt)
  {
    System.out.print(prompt);
    while (!scanner.hasNextInt())
    {
      System.out.println("Ожидается целое число.");
      scanner.next();
      System.out.print(prompt);
    }
    int value = scanner.nextInt();
    scanner.nextLine();
    return value;
  }

  public String readString(String prompt)
  {
    System.out.print(prompt);
    return scanner.nextLine().trim();
  }

  public boolean readBoolean(String prompt)
  {
    System.out.print(prompt);
    String line = scanner.nextLine().trim().toLowerCase();
    return line.startsWith("y") || line.startsWith("д");
  }

  public void close()
  {
    scanner.close();
  }
}
