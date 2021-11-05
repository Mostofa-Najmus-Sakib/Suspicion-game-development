import java.util.*;


/**
 * This is the base class for computer player/bots.
 */

public class RBotPlySearch extends Bot {
    Random r = new Random();
    HashMap<String, Piece> pieces; // Keyed off of guest name
    Board board;
    Piece me;
    HashMap<String, Player> players; // Keyed off of player name
    String[] otherPlayerNames;
    TextDisplay display;
    String lastCardPlayed;

    int[] gemCounts = new int[3];

    public class GameState {
        public Board b;
        public HashMap<String, Player> players;
        public HashMap<String, Piece> pieces;
        public Piece me;
        public int[] myGemCounts;

        public GameState(Board b, HashMap<String, Player> players, HashMap<String, Piece> pieces, Piece me, int[] myGemCounts) {
            this.DeepClone(b, players, pieces, me, myGemCounts);
        }

        private void DeepClone(Board b, HashMap<String, Player> players, HashMap<String, Piece> pieces, Piece me, int[] myGemCounts) {
            this.b = new Board(b);

            this.players = new HashMap<String, Player>();
            for (String key : players.keySet()) {
                Player originalPlayer = players.get(key);
                Player player = new Player(originalPlayer);
                this.players.put(key, player);
            }

            this.pieces = new HashMap<String, Piece>();
            for (String key : pieces.keySet()) {
                Piece originaPiece = pieces.get(key);
                Piece piece = new Piece(originaPiece);
                this.pieces.put(key, piece);
            }

            this.me = new Piece(me);

            this.myGemCounts = new int[3];
            for (int i = 0; i < 3; i++) {
                this.myGemCounts[i] = myGemCounts[i];
            }
        }
    }

    public static class Board {
        public Room[][] rooms;
        public String gemLocations;

        public class Room {
            public final boolean[] gems = new boolean[3];
            public final String[] availableGems;
            public final int row;
            public final int col;
            public HashMap<String, Piece> pieces;

            public void removePlayer(Piece piece) {
                removePlayer(piece.name);
                piece.col = -1;
                piece.row = -1;
            }

            public void removePlayer(String name) {
                pieces.remove(name);
            }

            public void addPlayer(Piece piece) {
                piece.col = this.col;
                piece.row = this.row;
                pieces.put(piece.name, piece);
            }

            public Room(boolean red, boolean green, boolean yellow, int row, int col) {
                pieces = new HashMap<String, Piece>();
                this.row = row;
                this.col = col;
                gems[Suspicion.RED] = red;
                gems[Suspicion.GREEN] = green;
                gems[Suspicion.YELLOW] = yellow;
                String temp = "";
                if (red) temp += "red,";
                if (green) temp += "green,";
                if (yellow) temp += "yellow,";
                availableGems = (temp.substring(0, temp.length() - 1)).split(",");
            }

            public Room(Room room) {
                for (int i = 0; i < 3; i++) {
                    this.gems[i] = room.gems[i];
                }

                this.availableGems = new String[room.availableGems.length];
                for (int i = 0; i < room.availableGems.length; i++) {
                    this.availableGems[i] = room.availableGems[i];
                }

                this.row = room.row;
                this.col = room.col;

                this.pieces = new HashMap<String, Piece>();
                for (String key : room.pieces.keySet()) {
                    Piece originalPiece = room.pieces.get(key);
                    Piece piece = new Piece(originalPiece);
                    this.pieces.put(key, piece);
                }
            }
        }

        public void movePlayer(Piece player, int row, int col) {
            rooms[player.row][player.col].removePlayer(player);
            rooms[row][col].addPlayer(player);
        }

        public void clearRooms() {
            rooms = new Room[3][4];
            int x = 0, y = 0;
            boolean red, green, yellow;

            for (String gems : gemLocations.trim().split(":")) {
                red = gems.contains("red");
                green = gems.contains("green");
                yellow = gems.contains("yellow");
                rooms[x][y] = new Room(red, green, yellow, x, y);
                y++;
                x += y / 4;
                y %= 4;
            }
        }

        public Board(String piecePositions, HashMap<String, Piece> pieces, String gemLocations) {
            Piece piece;
            this.gemLocations = gemLocations;
            clearRooms();
            int col = 0;
            int row = 0;
            for (String room : piecePositions.split(":", -1)) // Split out each room
            {
                room = room.trim();
                if (room.length() != 0) for (String guest : room.split(",")) // Split guests out of each room
                {
                    guest = guest.trim();
                    piece = pieces.get(guest);
                    rooms[row][col].addPlayer(piece);
                }
                col++;
                row = row + col / 4;
                col = col % 4;
            }
        }

        public Board(Board realBoard) {
            this.gemLocations = realBoard.gemLocations;
            this.rooms = new Room[realBoard.rooms.length][realBoard.rooms[0].length];
            for (int i = 0; i < realBoard.rooms.length; i++) {
                for (int j = 0; j < realBoard.rooms[0].length; j++) {
                    this.rooms[i][j] = new Room(realBoard.rooms[i][j]);
                }
            }
        }

        public String getPlayerLocations() {
            String rval = "";
            for (int x = 0; x < 3; x++) {
                for (int y = 0; y < 4; y++) {
                    boolean comma = false;
                    HashMap<String, Piece> Pies = rooms[x][y].pieces;
                    for (String key : Pies.keySet()) {
                        if (comma) rval += ",";
                        comma = true;
                        rval += key;
                    }
                    rval += ":";
                }
            }
            // return the string with the trailing ":" removed
            return rval.substring(0, rval.length() - 1);
        }
    }

    public Piece getPiece(String name) {
        return pieces.get(name);
    }

    public class Player {
        public String playerName;
        public ArrayList<String> possibleGuestNames;

        public void adjustKnowledge(ArrayList<String> possibleGuests) {
            Iterator<String> it = possibleGuestNames.iterator();
            while (it.hasNext()) {
                String g;
                if (!possibleGuests.contains(g = it.next())) {
                    it.remove();
                }
            }
        }

        public void adjustKnowledge(String notPossibleGuest) {
            Iterator<String> it = possibleGuestNames.iterator();
            while (it.hasNext()) {
                if (it.next().equals(notPossibleGuest)) {
                    it.remove();
                    break;
                }
            }
        }

        public Player(String name, String[] guests) {
            playerName = name;
            possibleGuestNames = new ArrayList<String>();
            for (String g : guests) {
                possibleGuestNames.add(g);
            }
        }

        public Player(Player realPlayer) {
            this.playerName = realPlayer.playerName;
            this.possibleGuestNames = new ArrayList<String>();
            for (int i = 0; i < realPlayer.possibleGuestNames.size(); i++) {
                this.possibleGuestNames.add(realPlayer.possibleGuestNames.get(i));
            }
        }
    }

    public static class Piece {
        public int row, col;
        public String name;

        public Piece(String name) {
            this.name = name;
        }

        public Piece(Piece realPiece) {
            this.name = realPiece.name;
            this.row = realPiece.row;
            this.col = realPiece.col;
        }
    }

    private String[] getPossibleMoves(Piece p) {
        LinkedList<String> moves = new LinkedList<String>();
        if (p.row > 0) moves.push((p.row - 1) + "," + p.col);
        if (p.row < 2) moves.push((p.row + 1) + "," + p.col);
        if (p.col > 0) moves.push((p.row) + "," + (p.col - 1));
        if (p.col < 3) moves.push((p.row) + "," + (p.col + 1));

        return moves.toArray(new String[moves.size()]);
    }

    private String[] getDiceCombination(String d){
        String possibleDiceOptions = "";
        if (d.equals("?")) {
            for (String key : pieces.keySet()) {
                possibleDiceOptions += key + ":";
            }
            possibleDiceOptions = possibleDiceOptions.substring(0, possibleDiceOptions.length() - 1);
        } else {
            possibleDiceOptions += d;
        }
        return possibleDiceOptions.split(":");
    }

    private String bestGemToGrab(GameState cloned){
        // Grab each of the gem one at a time and take the highest utility gain
        int[] currentGemState = cloned.myGemCounts;
        String[] availableGem = cloned.b.rooms[cloned.pieces.get(cloned.me.name).row][cloned.pieces.get(cloned.me.name).col].availableGems;
        float gemUtility = -9999999999.0F;
        int gemId = -1;
        int optimum_gem = -1;
        for (String gemPick: availableGem){
            if (gemPick.equals("red")){
                gemId = Suspicion.RED;
            }else if (gemPick.equals("green")){
                gemId = Suspicion.GREEN;
            }else{
                gemId = Suspicion.YELLOW;
            }
            currentGemState[gemId]++;
            float currentGemUtility = getGemPoint(currentGemState) - gemPenalize(cloned, gemPick);
            if (currentGemUtility > gemUtility){
                optimum_gem = gemId;
                gemUtility = currentGemUtility;
            }
            currentGemState[gemId]--;
        }
        if (optimum_gem == Suspicion.RED){
            return "red";
        } else if (optimum_gem == Suspicion.GREEN){
            return "green";
        } else {
            return "yellow";
        }
    }

    private Float pointGainFromViewDeck(HashMap<String, Player> players){
        int lengthOfKbForAllPlayer = 0;
        float KbLoose = 0F;
        for (String singlePlayer: players.keySet()){
            int KbLength = players.get(singlePlayer).possibleGuestNames.size();
            lengthOfKbForAllPlayer += KbLength;
            // loosing probability
            if(KbLength >= 9){
                KbLoose += 1.0F;
            } else {
                KbLoose += (KbLength-1) * .1;
            }
        }
        // Total change in Kb
        float remainingKb = (lengthOfKbForAllPlayer - KbLoose) / lengthOfKbForAllPlayer;
        if (remainingKb == 1 || remainingKb == 0){
            return 1F;
        }
        float gain = (float) (-remainingKb * Math.log(remainingKb) - (1-remainingKb) * Math.log(1-remainingKb));
        return gain;
    }

    private float gemPenalize(GameState cloned, String gemToGrab){
        // How many playes can take this red gem
        float PlayerThatCanTakeThisGem = 0F;
        for(String character: cloned.pieces.keySet()){
            int row = cloned.pieces.get(character).row;
            int col = cloned.pieces.get(character).col;
            if (Arrays.asList(cloned.b.rooms[row][col].availableGems).contains(gemToGrab)){
                PlayerThatCanTakeThisGem += 1F;
            }
        }
        // If all the player can take this Gem I am not revealing any of my information
        return (float) ((10 - PlayerThatCanTakeThisGem) * .1);
    }

    public String getBestCombinationToPlay(String d1, String d2, String card1, String card2, String board) {
        GameState current = new GameState(this.board, this.players, this.pieces, this.me, this.gemCounts);
        String resultAction = "";
        float utility = Float.NEGATIVE_INFINITY;
        // we have the current game state, now we need to generate the game state of every possible combination
        // all combination for the dice roll
        String[] possibleDice1Options = getDiceCombination(d1);
        String[] possibleDice2Options = getDiceCombination(d2);
        String[] allCardsArray = new String[]{card1, card2};
        // Final gem to grab

        int counter = 0;
        float pointFromViewDeck = pointGainFromViewDeck(this.players);
        for (String dice1 : possibleDice1Options) {
            GameState cloned = new GameState(this.board, this.players, this.pieces, this.me, this.gemCounts);
            Piece piece = cloned.pieces.get(dice1);
            String[] moves1 = getPossibleMoves(piece);
            for (int move1 = 0; move1 < moves1.length; move1++) {
                for (String dice2 : possibleDice2Options) {
                    // perform dice1 option here so that we can get the dice to move
                    GameState cloned2 = new GameState(this.board, this.players, this.pieces, this.me, this.gemCounts);
                    // Perform move
                    piece = cloned2.pieces.get(dice1);
                    cloned2.b.movePlayer(piece, Integer.parseInt(moves1[move1].split(",")[0]), Integer.parseInt(moves1[move1].split(",")[1]));
                    Piece piece2 = cloned2.pieces.get(dice2);
                    String[] moves2 = getPossibleMoves(piece2);
                    for (int move2 = 0; move2 < moves2.length; move2++) {
                        for (String card : allCardsArray) {
                            GameState cloned3 = new GameState(cloned2.b, cloned2.players, cloned2.pieces, cloned2.me, cloned2.myGemCounts);
                            String actions = "";
                            actions += "move," + dice1 + "," + moves1[move1];

                            // play the d2
                            piece = cloned3.pieces.get(dice2);
                            actions += ":move," + dice2 + "," + moves2[move2];
                            cloned3.b.movePlayer(piece, Integer.parseInt(moves2[move2].split(",")[0]), Integer.parseInt(moves2[move2].split(",")[1])); // Perform the move on my board


                            actions += ":play,card" + (counter + 1);
                            counter++;
                            if (counter == 2) {
                                counter = 0;
                            }
                            String[] bestPlayerToAsk;
                            float kbGained = 0F;
                            //play the card
                            for (String cardAction : card.split(":")) // just go ahead and do them in this order
                            {
                                if (cardAction.startsWith("move")) {
                                    String guest;
                                    // Hide our
                                    guest = guestNames[r.nextInt(guestNames.length)];
                                    actions += ":move," + guest + "," + r.nextInt(3) + "," + r.nextInt(4);
                                } else if (cardAction.startsWith("viewDeck")) {
                                    actions += ":viewDeck";
                                    // How many number will be reduced if view the deck
                                    kbGained += pointFromViewDeck;
                                } else if (cardAction.startsWith("get")) {
                                    String gemToGrab;
                                    if (cardAction.equals("get,")) {
                                        // Grab a random gem
                                        gemToGrab = bestGemToGrab(cloned3);
                                        actions += ":get," + gemToGrab;
                                        // Penalize for grabbing this gem
                                        kbGained -= gemPenalize(cloned3, gemToGrab);
                                    } else {
                                        gemToGrab = cardAction.trim().split(",")[1];
                                        actions += ":" + cardAction;
                                        // WE will provide some reward because we are not revealing any of our information
                                        // But we are taking the best gem
                                        kbGained += .5;
                                    }
                                    if (gemToGrab.equals("red")) cloned3.myGemCounts[Suspicion.RED]++;
                                    else if (gemToGrab.equals("green")) cloned3.myGemCounts[Suspicion.GREEN]++;
                                    else cloned3.myGemCounts[Suspicion.YELLOW]++;
                                } else if (cardAction.startsWith("ask")) {
                                    bestPlayerToAsk = bestPlayerToAsk(cloned3, cardAction.split(",")[1], cloned3.b.getPlayerLocations());
                                    actions += ":" + cardAction + bestPlayerToAsk[0];
                                    kbGained += Float.parseFloat(bestPlayerToAsk[1]);
                                }
                            }
                            // cloned = this.performAction(cloned, actions);
                            float tempUtilityEarned = GetUtilityEarned(current, cloned3, kbGained);
                            if (tempUtilityEarned > utility) {
                                utility = tempUtilityEarned;
                                resultAction = actions;
                            }
                        }
                    }
                }
            }
        }
        return resultAction;
    }

    private float getGemPoint(int [] gems){
        // [1, 2, 1]
        float gemPoint;
        //clone  this before sorting
        int[] gemCloned = new int[] {gems[0],gems[1],gems[2]};
        //sort the gems value
        Arrays.sort(gemCloned);
        // This is the pair that already created
        int minimumGem = gemCloned[0];  // [0, 1, 2]
        // Now we will check the mid value and max value we will try to minimize the difference between them so that they
        // create a new pair
        gemPoint = minimumGem * 6;
        gemPoint -= gemCloned[2] - gemCloned[1];
        gemPoint += (gemCloned[2] - minimumGem) +(gemCloned[1] - minimumGem);
        return gemPoint;
    }
    private float GetUtilityEarned(GameState real, GameState cloned, float kbGained) {
        float result;
        float gemPoint = getGemPoint(cloned.myGemCounts);
        result = gemPoint/12.0F + kbGained;
        // See Gained point from KB
        //System.out.println("Gem point: "+ gemPoint/15 + " kbGained: "+ kbGained+" --> "+ result);
        return result;
    }

    public void performAction(String resultActions){
        String[] actions = resultActions.split(":");
        for (String action: actions){
            String[] actionArray = action.split(",");
            if (actionArray[0].equals("move")){
                // Move the player
                Piece piece = pieces.get(actionArray[1]);
                this.board.movePlayer(piece, Integer.parseInt(actionArray[2]), Integer.parseInt(actionArray[3])); // Perform the move on my board
            } else if(actionArray[0].equals("get")){
                if(actionArray[1].equals("red")) gemCounts[Suspicion.RED]++;
                else if(actionArray[1].equals("green")) gemCounts[Suspicion.GREEN]++;
                else gemCounts[Suspicion.YELLOW]++;
            }
        }
    }

    public String getPlayerActions(String d1, String d2, String card1, String card2, String board) throws Suspicion.BadActionException {
        this.board = new Board(board, pieces, gemLocations);
        String resultActions = this.getBestCombinationToPlay(d1, d2, card1, card2, board);
        //check if board is same or not
        performAction(resultActions);
        //this.board = resultGameState.b;
        return resultActions;
    }

    // This is the character name that we will be asked question to other player if they can see this player or not
    private String[] bestPlayerToAsk(GameState gameState, String guest, String board) {
        // WE need board
        // pieces have the player locations of the board
        float gain = Float.NEGATIVE_INFINITY;
        float currentGain;
        String playerToAsk = otherPlayerNames[0];
        for (String player : otherPlayerNames) {
            // check the entropy gain if we ask this player
            // we will take the highest gain
            currentGain = getGain(gameState, guest, player, board);
            if (currentGain > gain) {
                playerToAsk = player;
                gain = currentGain;
            }
        }
        String[] retObj = new String[]{playerToAsk, Float.toString(gain)};
        return retObj;
    }

    private float getGain(GameState gameState, String guest, String player, String board) {
        // possible character if the answare is true
        ArrayList<String> possibleGuest_true = getPossibleGuesAfterAsking(gameState, guest, player, board, true);
        // possible character if the answare is false
        // Actual KnowledgeBase
        ArrayList<String> actualKB = gameState.players.get(player).possibleGuestNames;
        int remainningGuessTrue = remainingGuess(actualKB, possibleGuest_true);
        int remainningGuessFalse = actualKB.size() - remainningGuessTrue;
        float trueFrac = (float) remainningGuessTrue / actualKB.size();
        float FalseFrac = (float) remainningGuessFalse / actualKB.size();
        if (trueFrac == 0F || FalseFrac == 0F){
            return 0F;
        }
        float gain = (float) (-trueFrac * Math.log(trueFrac) - FalseFrac * Math.log(FalseFrac));
        return gain;
    }

    private int remainingGuess(ArrayList<String> actualKB, ArrayList<String> possibleGuestNames) {
        int total_now = actualKB.size();
        Iterator<String> it = actualKB.iterator();
        while (it.hasNext()) {
            if (!possibleGuestNames.contains(it.next())) {
                total_now--;
            }
        }
        return total_now;
    }

    private ArrayList<String> getPossibleGuesAfterAsking(GameState gameState, String guest, String player, String board, boolean canSee) {
        ArrayList<String> possibleGuests = new ArrayList<String>();
        Piece p1 = gameState.pieces.get(guest);  // retrieve the guest
        for (String k : gameState.pieces.keySet()) {
            Piece p2 = gameState.pieces.get(k);
            if ((canSee && canSee(p1, p2)) || (!canSee && !canSee(p1, p2))) possibleGuests.add(p2.name);
        }
        return possibleGuests;
    }

    private int countGems(String gem) {
        if (gem.equals("red")) return gemCounts[Suspicion.RED];
        else if (gem.equals("green")) return gemCounts[Suspicion.GREEN];
        else return gemCounts[Suspicion.YELLOW];
    }

    private ArrayList<String> getGuestsInRoomWithGem(String board, String gemcolor) {
        Board b = new Board(board, pieces, gemLocations);
        int gem = -1;
        if (gemcolor.equals("yellow")) gem = Suspicion.YELLOW;
        else if (gemcolor.equals("green")) gem = Suspicion.GREEN;
        else if (gemcolor.equals("red")) gem = Suspicion.RED;
        ArrayList<String> possibleGuests = new ArrayList<String>();

        int y = 0, x = 0;
        for (String guests : board.trim().split(":")) {
            //only get people from rooms with the gem
            if (b.rooms[y][x].gems[gem] && guests.trim().length() > 0) {
                for (String guest : guests.trim().split(",")) {
                    possibleGuests.add(guest.trim());
                }
            }
            x++;
            y += x / 4;
            x %= 4;
        }

        return possibleGuests;
    }

    public void reportPlayerActions(String player, String d1, String d2, String cardPlayed, String board, String actions) {
    }

    public void reportPlayerActions(String player, String d1, String d2, String cardPlayed, String[] board, String actions) {
        if (player.equals(this.playerName)) return; // If player is me, return
        // Check for a get action and use the info to update player knowledge
        if (cardPlayed.split(":")[0].equals("get,") || cardPlayed.split(":")[1].equals("get,")) {
            int splitindex;
            String[] split = actions.split(":");
            String get;
            if (split[3].indexOf("get") >= 0) splitindex = 3;
            else splitindex = 4;
            get = split[splitindex];
            String gem = get.split(",")[1];
            // board[splitIndex+1] will have the state of the board when the gem was taken
            if (board[splitindex] != null) // This would indicate an error in the action
            {
                ArrayList<String> possibleGuests = getGuestsInRoomWithGem(board[splitindex], gem);
                players.get(player).adjustKnowledge(possibleGuests);
            }
        }
    }

    private boolean canSee(Piece p1, Piece p2) // returns whether or not these two pieces see each
    {
        return (p1.row == p2.row || p1.col == p2.col);
    }


    public void answerAsk(String guest, String player, String board, boolean canSee) {
        Board b = new Board(board, pieces, gemLocations);
        ArrayList<String> possibleGuests = new ArrayList<String>();
        Piece p1 = pieces.get(guest);  // retrieve the guest
        for (String k : pieces.keySet()) {
            Piece p2 = pieces.get(k);
            if ((canSee && canSee(p1, p2)) || (!canSee && !canSee(p1, p2))) possibleGuests.add(p2.name);
        }
        players.get(player).adjustKnowledge(possibleGuests);
    }

    public void answerViewDeck(String player) {
        for (String k : players.keySet()) {
            players.get(k).adjustKnowledge(player);
        }
    }


    /* Modify this method to do something more intelligent. */
    public String reportGuesses() {
        //assuming we have six players
        int noOfPlayers = 6;

        //each item in the list is a string array containing the possible guesses of that player
        List<String[]> playerGuessList = new ArrayList<String[]>();

        for (String k : players.keySet()) {
            Player p = players.get(k);

            String[] playerGuesses = new String[p.possibleGuestNames.size() + 1];
            playerGuesses[0] = k;

            for (int i = 1; i < p.possibleGuestNames.size() + 1; i++) {
                playerGuesses[i] = p.possibleGuestNames.get(i - 1);
            }
            //now we have a string array with guesses of this particular player
            //add it to the list
            playerGuessList.add(playerGuesses);
        }

        // we will also need count of each character in the knowledge base for future use
        HashMap<String, Integer> charactersOcc = new HashMap<String, Integer>();
        HashMap<String, Boolean> charactersValidity = new HashMap<String, Boolean>();
        String[] characterNames = {"Buford Barnswallow", "Earl of Volesworthy", "Mildred Wellington", "Nadia Bwalya",
                "Viola Chung", "Dr. Ashraf Najem", "Remy La Rocque", "Lily Nesbit", "Trudie Mudge",
                "Stefano Laconi"};
        for (int i = 0; i < characterNames.length; i++) {
            charactersOcc.put(characterNames[i], 0);
            charactersValidity.put(characterNames[i], Boolean.TRUE);
        }

        // now in stead of a random guessing, we sort the list by size
        // and try to satisfy constraints in order to preditc players
        List<String[]> playerGuessListSorted = new ArrayList<String[]>();
        // List<boolean[]> playerGuessListSortedIsValid = new ArrayList<boolean[]>();


        int sizToLookUp = 1;
        while (sizToLookUp <= 11) {
            for (String[] element : playerGuessList) {
                if (element.length == sizToLookUp) {
                    //add the array in the sorted manner
                    playerGuessListSorted.add(element);
                    //add a boolean tracker for each string array, initially all values are valid
                    //boolean[] boolArray = new boolean[element.length];
                    //Arrays.fill(boolArray, Boolean.TRUE);
                }
            }

            sizToLookUp++;
        }

        //now we have the sorted knowledge base where the player with
        //least info(or minimum amount of predictions) is in the beginning

        //get number of occurances for each character in the KB
        for (String[] individualList : playerGuessListSorted) {
            for (int i = 1; i < individualList.length; i++)    //starting from index 1 because the first index is just the player name
            {
                int temp = charactersOcc.get(individualList[i]);
                charactersOcc.put(individualList[i], temp + 1);
            }
        }

        // now we begin the guessing, starting from the player with minimum amount of characters as guesses
        String rval = "";
        for (String[] list : playerGuessListSorted) {
            //get the player
            String player = list[0];
            rval += player;

            // now get the result character, that has the highest likelihood
            int tempOcc = 100000;    // we will take the character with the lowest occurance because that character has the highest probability
            int tempOccIndex = -1;
            for (int i = 1; i < list.length; i++) {
                if (tempOcc > charactersOcc.get(list[i]) && charactersValidity.get(list[i]) == Boolean.TRUE) {
                    tempOcc = charactersOcc.get(list[i]);
                    tempOccIndex = i;
                }
            }
            // now we know which character to pick for this player
            if (tempOccIndex == -1) {
                rval += "," + list[1] + ":";
                // get rid of this character from other player kbs because now it is irrelevant
                charactersOcc.put(list[1], charactersOcc.get(list[1]) - 1);
                charactersValidity.put(list[1], Boolean.FALSE);
            } else {
                rval += "," + list[tempOccIndex] + ":";
                // get rid of this character from other player kbs because now it is irrelevant
                charactersOcc.put(list[tempOccIndex], charactersOcc.get(list[tempOccIndex]) - 1);
                charactersValidity.put(list[tempOccIndex], Boolean.FALSE);
            }
        }

        return rval.substring(0, rval.length() - 1);

    }

    public RBotPlySearch(String playerName, String guestName, int numStartingGems, String gemLocations, String[] playerNames, String[] guestNames) {
        super(playerName, guestName, numStartingGems, gemLocations, playerNames, guestNames);
        display = new TextDisplay(gemLocations);
        pieces = new HashMap<String, Piece>();
        ArrayList<String> possibleGuests = new ArrayList<String>();
        for (String name : guestNames) {
            pieces.put(name, new Piece(name));
            if (!name.equals(guestName)) possibleGuests.add(name);
        }
        me = pieces.get(guestName);

        players = new HashMap<String, Player>();
        for (String str : playerNames) {
            if (!str.equals(playerName))
                players.put(str, new Player(str, possibleGuests.toArray(new String[possibleGuests.size()])));
        }

        otherPlayerNames = players.keySet().toArray(new String[players.size()]);
    }
}




