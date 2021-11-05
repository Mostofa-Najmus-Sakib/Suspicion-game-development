
import java.util.*;

public class Card <Action>
{
    private String faceValue;

    public String toString()
    {
        return faceValue;
    }

    public String getFaceValue()
    {
        return faceValue;
    }

    public Card(String faceValue)
    {
        this.faceValue = faceValue;
    }
}
