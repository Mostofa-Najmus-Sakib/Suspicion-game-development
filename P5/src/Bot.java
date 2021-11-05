
/** This is the base class for computer player/bots. 
 * 
 */
public abstract class Bot
{
    public String playerName;
    public String guestName;
    public int numStartingGems;
    public String gemLocations;
    public String[] playerNames;
    public String[] guestNames;

    /** 
     * The game calls this method when it is your turn to get the actions you want to perform
     * @param d1 the face value of dice 1
     * @param d2 the face value of dice 2
     * @param card1 a string that represents the actions possible for card1
     * @param card2 a string that represents the actions possible for card2
     * @param board a string representation of the state of the board
     * @return a string with the set of actions the player chooses to take
     * 
     * The return string should have 5 actions in it.  One move action for each 
     * dice, the card played, and one action for each action on the played card.
     * The first action in the action string should be the move corresponding to dice 1.
     * The second action should be the move corresponding to dice 2.
     * The third action should the the card you are going to play.
     * The 4th and 5th action should be actions from the card, in any order.
     * Each action should be separated by a colon :.
     * Move action syntax: move,player_name,x,y
     * Play card syntax: play,card1|card2
     * Card action syntax:
     *          * move,guest_name,x,y
     *          * viewDeck
     *          * ask,guest,player (can player see guest)
     *          * get,red|green|yellow
     * 
     * Example getPlayerActions("Earl of Volesworthy", "?", "get,:viewDeck", "get,yellow:ask,Remy La Rocque", "Stefano Laconi,Trudie Mudge::Lily Nesbitt::Remy La Rocque:Dr. Ashraf Najem,Buford Barnswallow:::::Viola Chung:Nadia Bwalya,Mildred Wellington,Earl of Volesworthy")
     * return value of action: "move,Earl of Volesworthy,1,3:move,Buford Barnswallow,1,2:play,card1:viewDeck:get,yellow"
     */
    public abstract String getPlayerActions(String d1, String d2, String card1, String card2, String board) throws Suspicion.BadActionException;

    /**
     * The game calls this method to inform you about the actions that each player takes
     *
     * @param player name of the player who did the actions
     * @param d1 value of dice1
     * @param d2 value of dice 2
     * @param cardPlayed the face value of the card the player played
     * @param board the state of the board before they performed the actions
     * @param actions the action string the player sent to the game engine
     */
    public abstract void reportPlayerActions(String player, String d1, String d2, String cardPlayed, String board, String actions);
    public void reportPlayerActions(String player, String d1, String d2, String cardPlayed, String[] board, String actions)
    {
        reportPlayerActions(player, d1, d2, cardPlayed, board[4], actions);
    }

    /**
     * The game calls this method to answer the question can player see guest?
     * @param guest the name of the guest
     * @param player the name of the player 
     * @param board the state of the board when the question was asked
     * @param canSee true if player can see the guest, false otherwise
     */
    public abstract void answerAsk(String guest, String player, String board, boolean canSee);
    /**
     * The game calls this method to inform you who is the named player on the top of the deck of NPC cards
     * @param player 
     */
    public abstract void answerViewDeck(String player);
    
    /**
     * 
     * @return a string with your best guesses for each player.  Format of string is player1Name,guest1Name:player2Name,guest2Name...
     */
    public abstract String reportGuesses();

    public String getPlayerName()
    {
        return playerName;
    }

    public String getGuestName()
    {
        return guestName;
    }

    public Bot(String playerName, String guestName, int numStartingGems, String gemLocations, String[] playerNames, String[] guestNames)
    {
        this.playerName= playerName;
        this.guestName = guestName;
        this.numStartingGems = numStartingGems;
        this.gemLocations = gemLocations;
        this.playerNames = playerNames;
        this.guestNames = guestNames;
    }
}
