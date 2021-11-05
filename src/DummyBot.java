
/** 
 * 
 */
public class DummyBot extends Bot
{
    public String getPlayerActions(String d1, String d2, String card1, String card2, String board) throws Suspicion.BadActionException
    {
        return "";
    }

    public void reportPlayerActions(String player, String d1, String d2, String cardPlayed, String board, String actions)
    {
    }
    public void answerAsk(String guest, String player, String board, boolean canSee)
    {
    }
    public void answerViewDeck(String player)
    {
    }
    public String reportGuesses()
    {
        return "";
    }

    public DummyBot(String playerName, String guestName, int numStartingGems, String gemLocations, String[] playerNames, String[] guestNames)
    {
        super(playerName, guestName, numStartingGems, gemLocations, playerNames, guestNames);
    }
}
