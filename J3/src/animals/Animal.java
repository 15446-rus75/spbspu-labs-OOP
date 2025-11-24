package animals;

public abstract class Animal
{
  private String name;

  public Animal(String name)
  {
    if (name == null)
    {
      throw new IllegalArgumentException("Имя животного не может быть null");
    }
    if (name.trim().isEmpty())
    {
      throw new IllegalArgumentException("Имя животного не может быть пустым");
    }
    this.name = name;
  }

  public String getName()
  {
    return name;
  }

  @Override
  public String toString()
  {
    return name + " (" + this.getClass().getSimpleName() + ")";
  }
}
