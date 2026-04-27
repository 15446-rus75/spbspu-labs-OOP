package cli;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleInputHandlerTest
{

  private final InputStream originalSystemIn = System.in;
  private ConsoleInputHandler inputHandler;

  @AfterEach
  void restoreSystemIn()
  {
    System.setIn(originalSystemIn);
  }

  @Test
  void readInt_shouldReturnInteger()
  {
    String input = "42\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));
    inputHandler = new ConsoleInputHandler();

    int result = inputHandler.readInt("Enter number: ");
    assertEquals(42, result);
  }

  @Test
  void readInt_shouldRetryOnInvalidInput()
  {
    String input = "abc\n100\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));
    inputHandler = new ConsoleInputHandler();

    int result = inputHandler.readInt("Enter number: ");
    assertEquals(100, result);
  }

  @Test
  void readString_shouldReturnTrimmedInput()
  {
    String input = "  hello world  \n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));
    inputHandler = new ConsoleInputHandler();

    String result = inputHandler.readString("Enter string: ");
    assertEquals("hello world", result);
  }

  @Test
  void readBoolean_shouldReturnTrueForYesVariants()
  {
    testBooleanInput("y", true);
    testBooleanInput("yes", true);
    testBooleanInput("YES", true);
    testBooleanInput("д", true);
    testBooleanInput("да", true);
  }

  @Test
  void readBoolean_shouldReturnFalseForNoVariants()
  {
    testBooleanInput("n", false);
    testBooleanInput("no", false);
    testBooleanInput("нет", false);
    testBooleanInput("other", false);
  }

  private void testBooleanInput(String input, boolean expected)
  {
    System.setIn(new ByteArrayInputStream((input + "\n").getBytes()));
    inputHandler = new ConsoleInputHandler();
    boolean result = inputHandler.readBoolean("Yes/No? ");
    assertEquals(expected, result);
  }

  @Test
  void close_shouldCloseScanner()
  {
    String input = "\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));
    inputHandler = new ConsoleInputHandler();
    assertDoesNotThrow(() -> inputHandler.close());
  }
}
