

import java.util.*;
import java.io.*;
import java.lang.reflect.*;


public class Suspicion
{
/* *******************************************************/
/* **************** data declarations ********************/
/* *******************************************************/
    private Deck actionDeck;
    private String lastCardPlayed;
    private Deck guestDeck;
    private Dice dice1, dice2;
    private int gems[] = new int[3];
    Board board;
    private String[] guestNames = {"Buford Barnswallow", "Earl of Volesworthy", "Mildred Wellington", "Nadia Bwalya",
                                   "Viola Chung", "Dr. Ashraf Najem", "Remy La Rocque", "Lily Nesbit", "Trudie Mudge",
                                   "Stefano Laconi"};
    private String[] cardActions = {
        "get,yellow:ask,Remy La Rocque,",
        "get,:viewDeck",
        "get,red:ask,Nadia Bwalya,",
        "get,green:ask,Lily Nesbit,",
        "viewDeck:ask,Buford Barnswallow,",
        "get,red:ask,Earl of Volesworthy,",
        "get,:ask,Nadia Bwalya,",
        "get,green:ask,Stefano Laconi,",
        "get,yellow:viewDeck",
        "get,:ask,Dr. Ashraf Najem,",
        "get,green:viewDeck",
        "get,red:viewDeck",
        "get,:ask,Mildred Wellington,",
        "get,:move,",
        "get,:ask,Earl of Volesworthy,",
        "get,:ask,Remy La Rocque,",
        "viewDeck:ask,Viola Chung,",
        "get,:ask,Stefano Laconi,",
        "get,:ask,Viola Chung,",
        "get,:viewDeck",
        "get,:ask,Lily Nesbit,",
        "get,yellow:ask,Mildred Wellington,",
        "get,:ask,Buford Barnswallow,",
        "get,:move,",
        "move,:ask,Dr. Ashraf Najem,",
        "get,:viewDeck",
        "get,:ask,Trudie Mudge,",
        "move,:ask,Trudie Mudge,"
    };

    public static final int RED=0;
    public static final int GREEN=1;
    public static final int YELLOW=2;

    private Vector<BotManager> bots; //This is all the bot managers
    private HashMap<String, BotManager> playerBotManagers; //for keeping track of bot managers keyed off of player names

    private static HashMap<String, Constructor> botConstructors = new HashMap<String, Constructor>();

    private Vector<String> playerFileNames;
    private String playerNames[];
    private Vector<String> playerNamesVector;
    private Vector<String> playerDirNames;

    private Vector<ArgHandler> argHandlers;
    private Random rand = new Random();

    private ArrayList<Display> displays;
    private ArrayList<String> displaysToLoad;
    
    private boolean tournament=false;
    private int numTournaments=0;
    private boolean display=true;
    private int delay=25;


/* *******************************************************/
/* **************** inner classes ************************/
/* *******************************************************/
    private class BotManager
    {
        int[] gems = new int[3];
        Card[] cards = new Card[2];
        Bot bot;
        int row;
        int col;
        int wins=0;
        int gemScore=0;
        int guessScore=0;

        public BotManager(Bot bot)
        {
            this.bot=bot;
        }
    }

    private class Board
    {
        // hashmap of players keyed off of the guest name (*not* the player name)
        HashMap<String, BotManager> players;
        Room rooms[][];
        Random r = new Random();

        public boolean roomHasGem(int row, int col, String gem)
        {
            if(gem.equals("red")) return rooms[row][col].gems[RED];
            if(gem.equals("green")) return rooms[row][col].gems[GREEN];
            if(gem.equals("yellow")) return rooms[row][col].gems[YELLOW];
            return false;
        }

        public String getPlayerLocations()
        {
            String rval = "";
            for(int x=0;x<3;x++) 
            {
                for(int y=0;y<4;y++)
                {
                    boolean comma=false;
                    for(BotManager bot: rooms[x][y].players.values())
                    {
                        if(comma) rval+=",";
                        comma=true;
                        rval+=bot.bot.guestName;
                    }
                    rval+=":";
                }
            }
            // return the string with the trailing ":" removed
            return rval.substring(0, rval.length()-1); 
        }

        public String getGemLocations()
        {
            String rval="";
            for(int x=0;x<3;x++) 
            {
                for(int y=0;y<4;y++)
                {
                    boolean comma=false;
                    if(rooms[x][y].gems[RED])
                    {
                        rval+="red";
                        comma=true;
                    }
                    if(rooms[x][y].gems[GREEN])
                    {
                        if(comma) rval+=",";
                        rval+="green";
                        comma=true;
                    }
                    if(rooms[x][y].gems[YELLOW])
                    {
                        if(comma) rval+=",";
                        rval+="yellow";
                    }
                    rval+=":";
                }
            }
            // return the string with the trailing ":" removed
            return rval.substring(0, rval.length()-1); 
        }

        public void placePlayerOnBoard(BotManager player, int row, int col)
        {
            rooms[row][col].putPlayer(player);
            players.put(player.bot.guestName, player);
        }

        public BotManager getPlayer(String name)
        {
            return players.get(name);
        }

        public void movePlayer(String name, int row, int col)
        {
            movePlayer(getPlayer(name),row,col);
        }

        public void movePlayer(BotManager player, int row, int col)
        {
            rooms[player.row][player.col].players.remove(player.bot.guestName);
//System.out.println("After removing player from old room: " + board.getPlayerLocations());
            rooms[row][col].putPlayer(player);
//System.out.println("After adding player to new room: " + board.getPlayerLocations());
        }

        public Board()
        {
            players = new HashMap<String, BotManager>();
 
            rooms=new Room[3][4];
            rooms[0][0] = new Room(true, false, false, 0,0);
            rooms[0][1] = new Room(true, false, true, 0,1);
            rooms[0][2] = new Room(false, true, true, 0,2);
            rooms[0][3] = new Room(true, true, false, 0,3);
            rooms[1][0] = new Room(false, true, true, 1,0);
            rooms[1][1] = new Room(true, true, false, 1,1);
            rooms[1][2] = new Room(true, false, true, 1,2);
            rooms[1][3] = new Room(false, false, true, 1,3);
            rooms[2][0] = new Room(true, false, true, 2,0);
            rooms[2][1] = new Room(false, true, false, 2,1);
            rooms[2][2] = new Room(true, true, false, 2,2);
            rooms[2][3] = new Room(false, true, true, 2,3);

            /*for(int x=0;x<3;x++) for(int y=0;y<4;y++)
            {
                System.out.print(""+x+","+y+"=");
                if(rooms[x][y].gems[0]) System.out.print("red,");
                if(rooms[x][y].gems[1]) System.out.print("green,");
                if(rooms[x][y].gems[2]) System.out.print("yellow,");
                System.out.println();
            }*/
        }

        public class Room
        {
            public final boolean gems[] = new boolean[3];
            public final int row;
            public final int col;
            //Keyed off of guest name (*not* the player name)
            private HashMap<String, BotManager> players;

            public void removePlayer(String name)
            {
                players.remove(name);
            }

            public void removePlayer(BotManager player)
            {
                removePlayer(player.bot.guestName);
            }

            public void putPlayer(BotManager player)
            {
                players.put(player.bot.guestName, player);
                player.row=row;
                player.col=col;
            }

            public Room(boolean red, boolean green, boolean yellow, int row, int col)
            {
                players = new HashMap<String, BotManager>();
                this.row = row;
                this.col = col;
                gems[Suspicion.RED]=red;
                gems[Suspicion.GREEN]=green;
                gems[Suspicion.YELLOW]=yellow;
            }
        }
    }
    
    public static class BadActionException extends Exception
    {
    }
    
    private class MyClassLoader extends ClassLoader 
    {
       Class cls;

       public Class retClass () {
          return cls;
       }

       public MyClassLoader (String classname) throws Exception
       {
             FileInputStream in = new FileInputStream (classname);
             ByteArrayOutputStream buffer = new ByteArrayOutputStream();
             int ch;
             while ((ch = in.read()) != -1) {
                buffer.write(ch);
             }
             byte[] rec = buffer.toByteArray();
             cls = defineClass (classname.substring(0,classname.length()-6), rec, 0, rec.length);
       }
    }

 
/* *******************************************************/
/* **************** arg handler stuff ********************/
/* *******************************************************/
    private class DisplayArg extends ArgHandler
    {
        public DisplayArg() { arg="-display"; }

        public int handleArg(String[] args, int x) throws Exception
        {
            if(argMatch(args[x]))
            {
                displaysToLoad.add(args[++x]);
                return ++x;
            }
            else return x;
        }
        public String toString() { return "[" + arg + " text|gui]"; }
    }

    private class TournamentArg extends ArgHandler
    {
        public TournamentArg() { arg="-tournament"; }

        public int handleArg(String[] args, int x) throws Exception
        {
            if(argMatch(args[x]))
            {
                tournament=true;
                display=false;
                numTournaments = Integer.parseInt(args[++x]);
                return ++x;
            }
            else return x;
        }
        public String toString() { return "[" + arg + " x]"; }
    }
    private class DelayArg extends ArgHandler
    {
        public DelayArg() { arg="-delay"; }

        public int handleArg(String[] args, int x) throws Exception
        {
            if(argMatch(args[x]))
            {
                delay=Integer.parseInt(args[++x]);
                return ++x;
            }
            else return x;
        }
        public String toString() { return "[" + arg + " x]"; }
    }

    private class LoadPlayerArg extends ArgHandler
    {
        public LoadPlayerArg() { arg="-loadplayer"; }

        public int handleArg(String[] args, int x) throws Exception
        {
            if(argMatch(args[x]))
            {
                System.out.println("Matched -loadplayer");
                playerFileNames.add(args[++x]);
                return ++x;
            }
            else return x;
        }
        public String toString() { return "[" + arg + " player.class]"; }
    }

    private class LoadPlayersArg extends ArgHandler
    {
        public LoadPlayersArg() { arg="-loadplayers"; }

        public int handleArg(String[] args, int x) throws Exception
        {
            if(argMatch(args[x]))
            {
                playerDirNames.add(args[++x]);
                return ++x;
            }
            else return x;
        }
        public String toString() { return "[" + arg + " list_name.txt]"; }
    }
/* ************************************************************************************************/
/* ********************** Constructors and game initalization code ********************************/
/* ************************************************************************************************/
    /** Initializes the argument handlers for parsing command line arguments */
    private void initArgHandlers()
    {
        argHandlers = new Vector<ArgHandler>();
        argHandlers.add(new DisplayArg());
        argHandlers.add(new TournamentArg());
        argHandlers.add(new DelayArg());
        argHandlers.add(new LoadPlayerArg());
        argHandlers.add(new LoadPlayersArg());
    }

    /** Main loop for parsing command line arguments */
    private void getUserPrefs(String[] args)
    {
        int x=0;
        int y=0;
        try
        {
            while(x<args.length)
            {
                for(int z=0;z<argHandlers.size();z++)
                {
                    y=argHandlers.get(z).handleArg(args,x);
                    if(x!=y) break;
                }
                if(x==y) usage();
                x=y;
            }
        }
        catch(Exception e)
        {
            usage();
        }
    }

/* *******************************************************/
/* **************** methods ******************************/
/* *******************************************************/
    public void usage()
    {
        System.out.print("Usage: java Suspicion ");
        for(int x=0;x<argHandlers.size();x++)
        {
            System.out.print(argHandlers.get(x));
        }
        System.out.println();
        System.exit(-1);
    }

    private void loadBots() throws Exception
    {
        Vector<Bot> bots  = new Vector<Bot>();
        Iterator<String> it = playerDirNames.iterator();
        while(it.hasNext())
        {
            loadPlayersFromFile(bots, it.next());
        }


        playerNames = new String[playerFileNames.size()];
        it = playerFileNames.iterator();
        int x=0;
        while(it.hasNext())
        {
            playerNames[x]=it.next()+x;
            x++;
        }
        
        playerNamesVector = new Vector<String>();
        for(String str:playerNames) playerNamesVector.add(str);

        it = playerFileNames.iterator();
        x=0;
        while(it.hasNext())
        {
            loadPlayerFromFile(bots, it.next(), playerNames[x++], guestDeck.drawCard().getFaceValue());
        }

        for(Bot bot:bots)
        {
            BotManager botman;
            this.bots.add(botman= new BotManager(bot));
            playerBotManagers.put(bot.playerName, botman);
        }
    }

    private void loadPlayerFromFile(Vector<Bot> bots, String fname, String pname, String guestName) throws Exception
    {

        System.out.println("Loading player " + fname);
        if(!botConstructors.containsKey(fname))
        {
            Class cls = new MyClassLoader(fname).retClass();
            botConstructors.put(fname, cls.getConstructors()[0]);
        }
        Constructor con = botConstructors.get(fname);

        bots.add((Bot)(con.newInstance(pname,guestName,2*playerFileNames.size(),board.getGemLocations(),playerNames,guestNames)));
    }

    private void loadPlayersInDir(Vector<Bot> bots, String dirname) throws Exception
    {
        File dir = new File(dirname);
        File[] contents = dir.listFiles();
        for(int x=0;x<contents.length;x++)
        {
            String fname = contents[x].toString();
            StringTokenizer st = new StringTokenizer(fname, "\\/");
            st.nextToken();
            playerFileNames.add(st.nextToken());
        }
    }

    private void loadPlayersFromFile(Vector<Bot> bots, String fname) throws Exception
    {
        BufferedReader br = new BufferedReader(new FileReader(fname));
        String line;
        while((line=br.readLine()) != null)
        {
            playerFileNames.add(line);
        }
    }

    public void tabulateResults()
    {
        int bestScore=0;
        BotManager bestBot=null;
        Iterator<BotManager> botit = bots.iterator();
        while(botit.hasNext())
        {
            BotManager bot = botit.next();
            int guessScore=0;
            int gemScore=0;

            String guesses[] = bot.bot.reportGuesses().trim().split(":");
            for(String temp:guesses)
            {
                String p = temp.trim().split(",")[0];
                String g = temp.trim().split(",")[1];
                if(board.players.get(g).bot.playerName.equals(p)) guessScore+=7;
            }

            gemScore = bot.gems[0] + bot.gems[1] + bot.gems[2]; 
            int min = bot.gems[0];
            if(bot.gems[1] < min) min = bot.gems[1];
            if(bot.gems[2] < min) min = bot.gems[2];
            gemScore += 3 * min;

            if(guessScore+gemScore>bestScore)
            {
                bestScore = guessScore+gemScore;
                bestBot=bot;
            }

            bot.guessScore=guessScore;
            bot.gemScore=gemScore;
        }
        bestBot.wins++;
    }

    

    public void printCurrentScores(int sortBy)
    {
        Iterator<BotManager> botit;

        switch(sortBy)
        {
            case 1: // Sort by wins
               Collections.sort(bots, 
                   new Comparator<BotManager>() 
                   {
                       public int compare(BotManager a, BotManager b)
                       {
                           return b.wins- a.wins;
                       }
                   }
               );
            break;
            case 2: // Sort by totalScore
               Collections.sort(bots, 
                   new Comparator<BotManager>() 
                   {
                       public int compare(BotManager a, BotManager b)
                       {
                           return -((a.gemScore+a.guessScore)-(b.gemScore+b.guessScore));
                       }
                   }
               );
            break;
        }

        System.out.println("PlayerName,GuessScore,GemScore,TotalScore,wins");
        botit=bots.iterator();
        while(botit.hasNext())
        {
            BotManager bot = botit.next();

            System.out.print("" + bot.bot.playerName + ",");

            System.out.println(bot.guessScore + "," + bot.gemScore + "," + (bot.guessScore+bot.gemScore) + "," + bot.wins);
        }
        
    }

    public void printResults()
    {
        tabulateResults();
        printCurrentScores(2);
    }

    public void printResults2()
    {
        int bestScore=0;
        BotManager bestBot=null;
        Iterator<BotManager> botit = bots.iterator();
        // Print out the guesses for every bot
        while(botit.hasNext())
        {
            BotManager bot = botit.next();
            System.out.println("Guesses for bot " + bot.bot.playerName + ": " + bot.bot.reportGuesses());
        }
        
        System.out.println("Actual player IDs: " + getPlayerIDs());        

        System.out.println("Scores:");

        botit=bots.iterator();
        while(botit.hasNext())
        {
            BotManager bot = botit.next();
            int guessScore=0;
            int gemScore=0;

            System.out.print("" + bot.bot.playerName + ",");
            String guesses[] = bot.bot.reportGuesses().trim().split(":");
            for(String temp:guesses)
            {
                String p = temp.trim().split(",")[0];
                String g = temp.trim().split(",")[1];
                if(board.players.get(g).bot.playerName.equals(p)) guessScore+=7;
            }

            gemScore = bot.gems[0] + bot.gems[1] + bot.gems[2]; 
            int min = bot.gems[0];
            if(bot.gems[1] < min) min = bot.gems[1];
            if(bot.gems[2] < min) min = bot.gems[2];
            gemScore += 3 * min;

            if(guessScore+gemScore>bestScore)
            {
                bestScore = guessScore+gemScore;
                bestBot=bot;
            }

            bot.guessScore=guessScore;
            bot.gemScore=gemScore;

            System.out.println(guessScore + "," + gemScore + "," + (guessScore+gemScore));
        }
        bestBot.wins++;
        
    }


    public Suspicion(String[] args)
    {
        playerFileNames = new Vector<String>();
        playerDirNames = new Vector<String>();
        displaysToLoad = new ArrayList<String>();
        initArgHandlers();
        getUserPrefs(args);

        bots = new Vector<BotManager>();
        playerBotManagers = new HashMap<String, BotManager>();
    }

    private void initDice()
    {
        String diceFaces1[] = {"Buford Barnswallow", "Earl of Volesworthy", "Mildred Wellington", "Viola Chung", "Dr. Ashraf Najem","?"};
        String diceFaces2[] = {"Nadia Bwalya", "Remy La Rocque", "Lily Nesbit", "Trudie Mudge", "Stefano Laconi","?"};
        //String diceFaces1[] = {"?", "?"};
        //String diceFaces2[] = {"?", "?"};
        dice1= new Dice(diceFaces1);
        dice2= new Dice(diceFaces2);
    }

    private void initGameState() throws Exception
    {
        initDecks();
        initDice();
        initBoard();
        initPlayers();

        displays = new ArrayList<Display>();
        for(String d:displaysToLoad)
        {
            if(d.equalsIgnoreCase("text")) displays.add(new TextDisplay(board.getGemLocations()));
            else if(d.equalsIgnoreCase("tset")) displays.add(new TextDisplay(board.getGemLocations()));
            else if(d.equalsIgnoreCase("gui")) ;
        }
    }

    void initBoard()
    {
        board = new Board();

        //System.out.println(board.getPlayerLocations());
    }

    void initDecks()
    {
        actionDeck = new Deck(cardActions);
        guestDeck = new Deck(guestNames);
    }


    private void initPlayers() throws Exception
    {
        Random r = new Random();
       //Load AI player classes
        if(playerFileNames.size()>0 || playerDirNames.size() > 0)
            loadBots();
        else throw new RuntimeException("need to specify bots on command line");

        //place AI players on the board and init their starting state
        Collections.shuffle(bots);
        for(BotManager bot: bots)
        {
            board.placePlayerOnBoard(bot, r.nextInt(3), r.nextInt(4));
            bot.cards[0] = actionDeck.drawCard();
            bot.cards[1] = actionDeck.drawCard();
        }

        //place Dummy (non-AI) guests on the board
        String[] foobar = {"",""};
        int x=0;
        for(Card card:guestDeck.getCards())
        {
            board.placePlayerOnBoard(new BotManager(new DummyBot("dummy" + (++x),card.getFaceValue(),0,"",foobar, foobar)),r.nextInt(3),r.nextInt(4));
        }

        for(x=0;x<3;x++) gems[x] = 2 * bots.size();
        //for(x=0;x<3;x++) gems[x] = 12 * bots.size();
    }

    private boolean isFourConnectedTwoMoves(BotManager bot, int row, int col)
    {
        if(row<0 || row>=3) return false;
        if(col<0 || col>=4) return false;
        if(Math.abs(bot.col-col) + Math.abs(bot.row-row) > 2) return false;
        return true;
    }

    private boolean isFourConnectedMove(BotManager bot, int row, int col)
    {
        if(row<0 || row>=3) return false;
        if(col<0 || col>=4) return false;
        if(Math.abs(bot.col-col) + Math.abs(bot.row-row) != 1) return false;
        return true;
    }

    private boolean checkDiceAction2Moves(Dice dice, String action)
    {
        String split[] = action.split(",");

        //if(!split[0].trim().equals("move")) return false; // Don't need to check this
        String guestName = split[1].trim();
        if(!dice.getFace().equals("?"))
        {
            if(!dice.getFace().equals(guestName)) return false;
        }
        BotManager bot = board.players.get(guestName);
        int row = Integer.parseInt(split[2].trim());
        int col = Integer.parseInt(split[3].trim());
        if(!isFourConnectedTwoMoves(bot,row,col)) throw new ActionException("Bad move for " + guestName + ". Move from " + bot.row + "," + bot.col + " to " + row + "," + col);
        return true;
    }

    private boolean checkDiceAction(Dice dice, String action)
    {
        String split[] = action.split(",");

        //if(!split[0].trim().equals("move")) return false; // Don't need to check this
        String guestName = split[1].trim();
        if(!dice.getFace().equals("?"))
        {
            if(!dice.getFace().equals(guestName)) return false;
        }
        BotManager bot = board.players.get(guestName);
        int row = Integer.parseInt(split[2].trim());
        int col = Integer.parseInt(split[3].trim());
        if(!isFourConnectedMove(bot,row,col)) throw new ActionException("Bad move for " + guestName + ". Move from " + bot.row + "," + bot.col + " to " + row + "," + col);
        return true;
    }

    private boolean checkCardActionsAgainstCard(BotManager player, String a1, String a2, Card card, String[] boardStates)
    {
        //System.out.println("CARD: " + card.getFaceValue());
        String cardActions[] = card.getFaceValue().split(":");
        String ca1, ca2;
        if(a1.startsWith(cardActions[0]) && a2.startsWith(cardActions[1]))
        {
           ca1=cardActions[0];
           ca2=cardActions[1];
        }
        else if(a1.startsWith(cardActions[1]) && a2.startsWith(cardActions[0]))
        {
           ca1=cardActions[1];
           ca2=cardActions[0];
        }
        //else return false;
        else throw new ActionException("Bad card exception: card actions not found on card.");

        if(!checkCardAction(player,a1,ca1)) return false;
        else 
        {
            boardStates[3] = performAction(player, a1);
            displayPlayerActions(player.bot.getPlayerName(), a1, boardStates[2]);
        }
        if(!checkCardAction(player,a2,ca2)) return false;
        else 
        {
            boardStates[4] = performAction(player, a2);
            displayPlayerActions(player.bot.getPlayerName(), a2, boardStates[3]);
        }
        
        return true;
    }

    private boolean checkCardAction(BotManager player, String action, String cardAction)
    {
        String split[] = action.split(",");
        if(action.startsWith("move")) 
        {
            String guestName = split[1].trim();
            BotManager bot = board.players.get(guestName);
            int row = Integer.parseInt(split[2].trim());
            int col = Integer.parseInt(split[3].trim());
            if(row<0 || row>=3) throw new ActionException("Bad move action from card, row value out of range.");
            if(col<0 || col>=4) throw new ActionException("Bad move action from card, col value out of range.");
            return true;
        }
        else if(action.equals("viewDeck")) return true;
        else if(action.startsWith("get")) 
        {
            if(action.equals(cardAction)) return true;
            //check if gem in get is in room player is in
            if(!board.roomHasGem(player.row, player.col, action.split(",")[1])) throw new ActionException("Bad get action from card, gem not found in room " + player.row + "," + player.col + ".");
            return true;
        }
        else if(action.startsWith("ask")) 
        {
            if(!playerNamesVector.contains(action.split(",")[2])) throw new ActionException("Bad player name " + action.split(",")[2] + " in ask action.");
            return true;
        }
        else return false;
    }

    public static class ActionException extends RuntimeException
    {
        public ActionException(String msg)
        {
            super(msg);
        }
    }

    private boolean legalActions(BotManager bot, String actionString, String[] boardStates)
    {
        String[] actions = actionString.split(":");
        int cardNumber;
        Card card;

        boardStates[0]=board.getPlayerLocations();
        lastCardPlayed="viewDeck:viewDeck"; // Set this to a dummy value, it get's reset in playCard
        
        try
        {
            //Check the dice actions
            if(!checkDiceAction(dice1, actions[0].trim())) throw new ActionException("Bad action on first dice throw: " + dice1.getFace() + " " + actions[0].trim());
            else 
            {
                boardStates[1] = performAction(bot, actions[0].trim());
                displayPlayerActions(bot.bot.getPlayerName(), actions[0].trim(), boardStates[0]);
            }
            /*if(actions[0].trim().split(",")[1].trim().equals(actions[1].trim().split(",")[1].trim())) // Moving the same player twice in a row
            {
                if(!checkDiceAction2Moves(dice2, actions[1].trim())) throw new ActionException("Bad action on second dice throw: " + dice2.getFace() + " " + actions[1].trim());
            }*/
            if(!checkDiceAction(dice2, actions[1].trim())) throw new ActionException("Bad action on second dice throw: " + dice2.getFace() + " " + actions[1].trim());
            else 
            {
                boardStates[2] = performAction(bot, actions[1].trim());
                displayPlayerActions(bot.bot.getPlayerName(), actions[1].trim(), boardStates[1]);
            }

            //Get the played card
            String cardToPlay = actions[2].trim();
            String temp = cardToPlay.split(",")[1].trim();
            cardNumber = Integer.parseInt(temp.substring(temp.length()-1, temp.length()))-1;
            card = bot.cards[cardNumber];

            //Check the card actions against the played card
            if(!checkCardActionsAgainstCard(bot, actions[3].trim(), actions[4].trim(), card, boardStates)) 
            {
                throw new ActionException("Bad card action: " + card.getFaceValue() + " : " + actions[3].trim() + " : " + actions[4].trim());
            }
            else playCard(bot, cardToPlay);
        }
        catch(RuntimeException e) 
        {
            System.out.println(e);
            return false;
        }
        catch(Exception e) 
        { 
            System.out.println(e);
            return false;
        }

        return true;
    }


    private void performMove(String action)
    {
        String split[] = action.split(",");
        String guestName = split[1].trim();
        int row = Integer.parseInt(split[2].trim());
        int col = Integer.parseInt(split[3].trim());
//System.out.println("Before move: " + board.getPlayerLocations());
        board.movePlayer(guestName, row, col);
//System.out.println("After move: " + board.getPlayerLocations());
    }

    private void playCard(BotManager bot, String action)
    {
        String temp = action.split(",")[1].trim();

        int cardNumber = Integer.parseInt(temp.substring(temp.length()-1, temp.length()))-1;
        lastCardPlayed = bot.cards[cardNumber].getFaceValue();
        actionDeck.discard(bot.cards[cardNumber]);
        if(actionDeck.isEmpty()) actionDeck.shuffle();
        bot.cards[cardNumber] = actionDeck.drawCard();
    }

    private void viewDeck(BotManager bot)
    {
        Card card = guestDeck.drawCard();
        bot.bot.answerViewDeck(card.getFaceValue());
        guestDeck.putCardBottom(card);
    }

    private BotManager getBotManByPlayerName(String name)
    {
        for(BotManager bot: bots)
        {
            if(bot.bot.getPlayerName().equals(name))
                return bot;
        }
        throw new RuntimeException("Couldn't find bot for player name: " + name);
    }

    private void ask(BotManager bot, String action)
    {
        String temp[] = action.split(",");
        String guest = temp[1];
        String player = temp[2];        
        BotManager looker = getBotManByPlayerName(player);
        BotManager viewed = board.getPlayer(guest);
        if(looker.row==viewed.row || looker.col==viewed.col) 
        {
            bot.bot.answerAsk(guest, player, board.getPlayerLocations(),true);
        }
        else
        {
            bot.bot.answerAsk(guest, player, board.getPlayerLocations(),false);
        }
    }

    private void getGem(BotManager bot, String action)
    {
        String gem = action.trim().split(",")[1].trim();
        if(gem.equals("red"))
        {
            bot.gems[RED]++;
            gems[RED]--;
        }
        else if(gem.equals("green"))
        {
            bot.gems[GREEN]++;
            gems[GREEN]--;
        }
        else
        {
            bot.gems[YELLOW]++; 
            gems[YELLOW]--;
        }
        
    }


    private String performAction(BotManager bot, String action)
    {
        action=action.trim();
        if(action.startsWith("move")) performMove(action);
        else if(action.startsWith("play")) playCard(bot,action);
        else if(action.startsWith("viewDeck")) viewDeck(bot);
        else if(action.startsWith("get")) getGem(bot,action);
        else if(action.startsWith("ask")) ask(bot,action);
        else throw new RuntimeException("Bad action " + action);

        return board.getPlayerLocations();
    }

    private void performActions(BotManager bot, String actionString)
    {
        String[] actions = actionString.split(":");
        for(String action:actions)
        {
            performAction(bot,action.trim());
        }
    }

    private void displayPlayerActions(String player, String actions, String board)
    {
        for(Display d:displays)
        {
            d.displayPlayerActionSequence(player, actions, board);
        }
    }


    private void play() throws Exception
    {
        initGameState();
        System.out.println("Actual player IDs: " + getPlayerIDs());        
        Iterator<BotManager> botit = bots.iterator();
        while(gems[RED]>0 && gems[GREEN]>0 && gems[YELLOW]>0)
        {
            if(!botit.hasNext()) botit = bots.iterator();
            BotManager bot = botit.next();
            dice1.roll();
            dice2.roll();
            String actions = bot.bot.getPlayerActions(dice1.getFace(),dice2.getFace(),bot.cards[0].getFaceValue(),bot.cards[1].getFaceValue(),board.getPlayerLocations());
            //System.out.println(board.getPlayerLocations());
            //System.out.println(actions);
            displayPlayerActions(bot.bot.playerName, actions, board.getPlayerLocations());
            String[] boardStates=new String[5];
            if(legalActions(bot, actions, boardStates))
            {
                /*String card = actions.split(":")[2].trim().split(",")[1].trim();
                int cardNum = Integer.parseInt(card.substring(card.length()-1,card.length()))-1;*/
                //performActions(bot,actions);
            }
            else
            {
                System.out.println("BAD action by player " + bot.bot.playerName);
            }
            String card = actions.split(":")[2].trim().split(",")[1].trim();
            int cardNum = Integer.parseInt(card.substring(card.length()-1,card.length()))-1;
            for(BotManager b: bots)
            {
                //b.bot.reportPlayerActions(bot.bot.playerName, dice1.getFace(), dice2.getFace(), lastCardPlayed, board.getPlayerLocations(),actions);
                if(lastCardPlayed==null) throw new RuntimeException("lastCardPlayed equals null");
                b.bot.reportPlayerActions(bot.bot.playerName, dice1.getFace(), dice2.getFace(), lastCardPlayed, boardStates, actions);
            }
        }

    }
    
    public String getPlayerIDs()
    {
        String playerIDs="";
        Iterator<BotManager> botit=bots.iterator();
        while(botit.hasNext())
        {
            BotManager bot = botit.next();
            playerIDs += bot.bot.playerName + "," + bot.bot.guestName + ":";
        }
        playerIDs = playerIDs.substring(0, playerIDs.length()-1);
        return playerIDs;
    }

    public static void tabulateResults(Suspicion game, Suspicion tournResults)
    {
        BotManager bestBot=null;
        int bestScore=0;
        for(BotManager bot:game.bots)
        {
            BotManager tbot = tournResults.playerBotManagers.get(bot.bot.playerName);
            if(bot.guessScore+bot.gemScore>bestScore) 
            {
                bestScore=bot.guessScore+bot.gemScore;
                bestBot=tbot;
            }
            tbot.guessScore += bot.guessScore;
            tbot.gemScore += bot.gemScore;
            tbot.wins += bot.wins;
        }
    }

    public static void main(String[] args) throws Exception
    {
        Suspicion game = new Suspicion(args);
        if(game.tournament)
        {
            game.initGameState();
            for(int x=0;x<game.numTournaments;x++)
            {
                Suspicion tgame = new Suspicion(args);
                tgame.play();
                System.out.println("**********************************************************");
                System.out.println("Current game results...");
                tgame.printResults();
                tabulateResults(tgame, game);
                System.out.println("**********************************************************");
                System.out.println("Current tournament results...");
                game.printCurrentScores(1);
            }
        }
        else
        {
            game.play();
            game.printResults();
        }
    }
}


