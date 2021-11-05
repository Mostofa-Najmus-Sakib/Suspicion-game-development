
import java.util.*;

public class TextDisplay extends Display
{
    private int MaxX = 100, MaxY = 39;
    private char[][] display = new char[MaxY][MaxX];
    private Room[][] rooms = new Room[3][4];


    public void clearDisplay()
    {
        for(int y=0;y<display.length;y++) for(int x=0;x<display[y].length;x++)
        {
            display[y][x]=' ';
        }
    }


    private class Room
    {
        int cx, cy;
        int ux,uy,lx,ly;
        int MaxCX, MaxCY, MinCX, MinCY, width, height;
        String gems;
        private String header;

        public void clearRoom()
        {
            for(int y=uy+1;y<ly-1;y++) for(int x=ux+1;x<lx-1;x++)
            {
                display[y][x]=' ';
            }
            cy=uy+1;
            cx=ux+1;
        }

        private void printRoomHeader()
        {
            cy=MinCY;
            cx = MinCX + (width-header.length())/2;
            print(header);
            cy=MinCY+1;
            cx = MinCX;
        }

        private void printGuests(String guests)
        {
            //System.out.println(guests);
            clearRoom();
            printRoomHeader();

            cy = MinCY+1;
            for(String guest:guests.trim().split(","))
            {
                guest=guest.trim();
                cx=MinCX;
                print(guest);
                cy++;
            }
        }

        private void old_printGuests(String guests)
        {
            boolean left=true;
            cy = MinCY;
            for(String guest:guests.trim().split(","))
            {
                guest=guest.trim();
                if(left)
                {
                    left=false;
                    cx=MinCX;
                    cy++;
                }
                else
                {
                    left=true;
                    cx=MinCX + width-guest.length();
                }
                print(guest);
            }
        }

        private void print(char ch)
        {

            if(cy<MinCY) cy=MinCY;
            if(cx<MinCX) cx=MinCX;
            if(cy>MaxCY) cy=MaxCY;
            if(cx>MaxCX) cx=MaxCX;

            display[cy][cx++]=ch;
        }

        private void print(String str)
        {
           char[] chars=str.toCharArray();
           for(char ch: chars)
           {
               print(ch);
           }
        }

        public void printRoomBorder()
        {
            for(int y = uy;y<ly;y++) 
            {
                display[y][ux]=(char)0x2551;
                display[y][lx-1]=(char)0x2551;
            }
            for(int x=ux;x<lx;x++)
            {
                display[uy][x]=(char)0x2550;
                display[ly-1][x]=(char)0x2550;
            }
        }

        public Room(int y, int x, String gems)
        {
            header = gems;
            uy = y * (MaxY/3);
            ux = x * (MaxX/4);
            ly = uy + MaxY/3;
            lx = ux + MaxX/4;
            MinCY = uy+1;
            MinCX = ux+1;
            MaxCY = ly-2;
            MaxCX = lx-2;
            width = 1 + MaxCX-MinCX;
            height = 1 + MaxCY-MinCY;
            clearRoom();
        }
    }


    public void displayBoard(String board)
    {
        int y=0,x=0;
        for(String guests: board.trim().split(":"))
        {
            rooms[y][x].printGuests(guests);
            x++;
            y+=x/4;
            x%=4;
        }

       

        System.out.print("\r");
        for(y=0;y<MaxY;y++) 
        {
            System.out.println(display[y]);
        }
    }

    public void displayPlayerActionSequence(String player, String actions, String board)
    {
        displayBoard(board);
        System.out.println(player + " actions: " + actions);
    }

    public void displayPlayerKnowledge(String player, String guesses)
    {
        System.out.println(player + " knowledge:\n" + guesses);
    }

    public TextDisplay(String gemLocations)
    {
        super(gemLocations);
        clearDisplay();

        int x=0, y=0;
        for(String gems:gemLocations.trim().split(":"))
        {
            rooms[y][x] = new Room(y,x,gems.trim());
            rooms[y][x].printRoomBorder();
            rooms[y][x].printRoomHeader();
            x++;
            y += x/4;
            x %= 4;
        }
    }
}
