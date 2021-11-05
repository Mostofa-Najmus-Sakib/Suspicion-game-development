
import java.util.*;

public class Deck
{
    private LinkedList<Card> cards;
    private LinkedList<Card> discardPile;

    public boolean isEmpty()
    {
        return cards.isEmpty();
    }

    public String toString()
    {
        return cards.toString();
    }

    public Card[] getCards()
    {
        Card[] carray = new Card[cards.size()];
        return cards.toArray(carray);
    }

    /**
     * @return returns the top card of the deck
     */
    public Card drawCard()
    {
        return cards.pop();
    }

    /** 
     * puts the card on the bottom of the deck
     * 
     * @param card the card to put back in the deck 
     */
    public void putCardBottom(Card card)
    {
        cards.addLast(card);
    }
    
    /**
     * puts the card on the top of the deck
     * @param card the card to put back in the deck
     */
    public void putCardTop(Card card)
    {
        cards.addFirst(card);
    }

    public void discard(Card card)
    {
        discardPile.push(card);
    }

    /**
     * shuffles the deck
     */
    public void shuffle()
    {
        while(!discardPile.isEmpty())
        {
            cards.push(discardPile.pop());
        }
        Collections.shuffle(cards);
    }

    public Deck(String[] cards)
    {
        this.cards = new LinkedList<Card>();
        discardPile = new LinkedList<Card>();
        for(String card:cards) this.cards.push(new Card(card));
        shuffle();
    }

}


