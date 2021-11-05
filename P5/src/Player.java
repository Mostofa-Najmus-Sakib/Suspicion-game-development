
import java.util.*;
public class Player
{
    private String name;
    private Location location;

    public Location getLocation()
    {
        return new Location(location);
    }

    public void updateLocation(int row, int col)
    {
        location.row = row;
        location.col= col;
    }

    public Player(String name, int row, int col)
    {
        this.name = name;
        updateLocation(row,col);
    }

}

