

public abstract class Display 
{
    String gemLocations;

    public abstract void displayBoard(String board);
    public abstract void displayPlayerActionSequence(String player, String actions, String board);
    public abstract void displayPlayerKnowledge(String player, String guesses);

    public Display(String gemLocations)
    {
        this.gemLocations=gemLocations.trim();
    }
}

