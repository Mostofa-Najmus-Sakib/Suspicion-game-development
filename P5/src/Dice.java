
import java.util.*;

public class Dice
{
    private static Random r = new Random();
    private String[] faces;
    private int face;

    public void roll()
    {
        face = r.nextInt(faces.length);
    }

    public String getFace()
    {
        return faces[face];
    }

    public Dice(String[] faces)
    {
       this.faces = faces;
    }
}
