package translator.exceptions;

import java.io.*;

public class InvalidFileFormatException extends IOException
{
  public InvalidFileFormatException(String message)
  {
    super(message);
  }
}
