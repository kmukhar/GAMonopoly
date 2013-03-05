package edu.uccs.ecgs.players;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.uccs.ecgs.ga.Actions;
import edu.uccs.ecgs.ga.BankruptcyException;
import edu.uccs.ecgs.ga.Chance;
import edu.uccs.ecgs.ga.CommunityChest;
import edu.uccs.ecgs.ga.GameState;
import edu.uccs.ecgs.ga.Location;
import edu.uccs.ecgs.ga.Monopoly;
import edu.uccs.ecgs.ga.TradeProposal;
import edu.uccs.ecgs.play2.LocationChangedEvent;
import edu.uccs.ecgs.states.Events;

public class GamePlayer extends AbstractPlayer implements Comparable<AbstractPlayer> {
  private AbstractPlayer player;
  private ArrayList<ChangeListener> changeListeners;

  /**
   * Constructor
   * 
   * @param index
   *          An id for the player
   * @param chromoType
   */
  public GamePlayer(AbstractPlayer player) {
    super();
    changeListeners = new ArrayList<ChangeListener>();
    this.player = player;
  }

  /**
   * @return A String that gives the name of the player in the form
   * "Player n" where n is the playerIndex.
   */
  @Override
  public String getName() {
    return player.getName();
  }

  /**
   * Remove all properties from the player. This method simply clears the
   * player's list of owned properties. No other changes are made to the
   * player's state or to the properties' state.
   */
  @Override
  public void clearAllProperties() {
    player.clearAllProperties();
    fireChangeEvent();
  }

  /**
   * Does the player have at least as much cash as amount.
   * 
   * @param amount
   *          The amount that is being checked.
   * @return True --> if the player's cash is greater than or equal to amount.<br>
   *         False --> otherwise.
   */
  @Override
  public boolean hasAtLeastCash(int amount) {
    return player.hasAtLeastCash(amount);
  }

  /**
   * Initialize the player's cash to amount.
   * 
   * @param amount
   *          The amount of cash the player should have.
   */
  @Override
  public void initCash(int amount) {
    player.initCash(amount);
  }

  /**
   * Reset the doubles counter for this player; should only be called at the
   * start of the player's turn.
   */
  @Override
  public void resetDoubles() {
    player.resetDoubles();
  }

  /**
   * Set player's state to inactive.
   */
  @Override
  public void setInactive() {
    player.setInactive();
  }

  @Override
  public Actions getNextActionEnum(Events event) {
    return player.getNextActionEnum(event);
  }

  /**
   * Set player's state to gameState.
   * 
   * @param gameState
   *          State in which player is.
   */
  @Override
  public void setNewState(GameState gameState) {
    player.setNewState(gameState);
  }

  /**
   * Set the player's rolledDoubles flag to the input parameter. If the player
   * is in jail and the parameter is false (player did not roll doubles), this
   * method reduces the jail term counter.
   * 
   * @param rolledDoubles
   *          True if the player rolled doubles, false otherwise.
   */
  @Override
  public void setDoubles(boolean rolledDoubles) {
    player.setDoubles(rolledDoubles);
  }

  /**
   * Move the player's location by numSpaces, if the player passes Go, the
   * player receives $200.
   * 
   * @param numSpaces
   *          The number of spaces to move.
   */
  @Override
  public void move(int numSpaces) {
    player.move(numSpaces);
    LocationChangedEvent lce = 
        new LocationChangedEvent(this, getCurrentLocation());
    fireChangeEvent(lce);
  }

  /**
   * Go to jail. Go directly to Jail. Do not pass Go.
   */
  @Override
  public void goToJail(Location jail) {
    player.goToJail(jail);
    fireChangeEvent();
  }

  /**
   * @return The player's current location index.
   */
  @Override
  public int getLocationIndex() {
    return player.getLocationIndex();
  }

  /**
   * @return True --> if the player passed Go or landed on Go during the most
   *         recent movement,<br>
   *         false --> otherwise.
   */
  @Override
  public boolean passedGo() {
    return player.passedGo();
  }

  /**
   * Add cash to the player's current amount of cash.
   * 
   * @param amount
   *          The amount of cash to add the player's current amount of cash.
   */
  @Override
  public void receiveCash(int amount) {
    player.receiveCash(amount);
    fireChangeEvent();
  }

  /**
   * Take some cash from the player.
   * 
   * @param amount
   *          The amount of cash to take from the player
   * @throws BankruptcyException
   *           If player does not have the amount and cannot sell houses or
   *           hotels and cannot mortgage any properties to raise the amount.
   */
  @Override
  public void getCash(int amount) throws BankruptcyException {
    player.getCash(amount);
    fireChangeEvent();
  }

  /**
   * @return The number of railroads that the player owns.
   */
  @Override
  public int getNumRailroads() {
    return player.getNumRailroads();
  }

  /**
   * @return The number of Utilities that the player owns.
   */
  @Override
  public int getNumUtilities() {
    return player.getNumUtilities();
  }

  /**
   * Add a property to the player's inventory, normally by buying a property or
   * receiving a property through another player's bankruptcy.
   * 
   * @param location
   *          The property to be added.
   */
  @Override
  public void addProperty(Location location) {
    player.addProperty(location);
    fireChangeEvent();
  }

  /**
   * Remove a property from the list of properties owned by this player.
   * @param location The property to be removed from player
   */
  @Override
  public void removeProperty(Location location) {
    player.removeProperty(location);
    location.setOwner(null);
  }

  /**
   * Set this player's location to the location at index.
   * 
   * @param index
   *          The index of the location, corresponds to board position with Go
   *          having index 0 and increasing sequentially counter-clockwise
   *          around the board.
   */
  @Override
  public void setLocationIndex(int index) {
    player.setLocationIndex(index);
  }

  /**
   * @return True if the player is in jail, false otherwise.
   */
  @Override
  public boolean inJail() {
    return player.inJail();
  }

  /**
   * @return A reference to the Location where the player current is.
   */
  @Override
  public Location getCurrentLocation() {
    return player.getCurrentLocation();
  }

  /**
   * @return True --> if the player rolled doubles on most recent dice roll,<br>
   *         False --> otherwise.
   */
  @Override
  public boolean rolledDoubles() {
    return player.rolledDoubles();
  }

  /**
   * @return True if the player has either Get Out Of Jail Free card.
   */
  @Override
  public boolean hasGetOutOfJailCard() {
    return player.hasGetOutOfJailCard();
  }

  /**
   * Use the player's Get Out Of Jail Free card by returning it to the Card
   * collection; modifying other state related to being in jail is not performed
   * by this method.
   */
  @Override
  public void useGetOutOfJailCard() {
    player.useGetOutOfJailCard();
    fireChangeEvent();
  }

  /**
   * @return The total worth of the player including cash, value of all houses
   *         and hotels, and value of all property owned by the player.
   */
  @Override
  public int getTotalWorth() {
    return player.getTotalWorth();
  }

  /**
   * Add to the player's fitness score.
   * 
   * @param score
   *          The amount to add to the player's fitness.
   */
  @Override
  public void addToFitness(int score) {
    player.addToFitness(score);
  }

  /**
   * Set the player's fitness score to the given value.
   * 
   * @param score
   *          The new value for the fitness score.
   */
  @Override
  public void setFitness(int score) {
    player.setFitness(score);
  }

  /**
   * Reset the fitness value to 0
   */
  @Override
  public void resetFitness() {
    player.resetFitness();
  }

  /**
   * @return The player's current fitness score.
   */
  @Override
  public int getFitness() {
    return player.getFitness();
  }

  /**
   * @return the finishOrder
   */
  @Override
  public int getFinishOrder() {
    return player.getFinishOrder();
  }

  /**
   * Sets the finish order of a game, 1 for winner (first place), 2 for second
   * place, etc.
   * 
   * @param finishOrder
   *          the finishOrder to set
   */
  @Override
  public void setFinishOrder(int finishOrder) {
    player.setFinishOrder(finishOrder);
  }

  /**
   * @return True --> if the player is bankrupt,<br>
   *         False --> otherwise.
   */
  @Override
  public boolean bankrupt() {
    return player.bankrupt();
  }

  /**
   * Add the Get Out Of Jail Free Card to the player's inventory.
   * 
   * @param chanceJailCard
   *          The card to add.
   */
  @Override
  public void setGetOutOfJail(Chance chanceJailCard) {
    player.setGetOutOfJail(chanceJailCard);
    fireChangeEvent();
  }

  /**
   * Add the Get Out Of Jail Free Card to the player's inventory.
   * 
   * @param ccJailCard
   *          The card to add.
   */
  @Override
  public void setGetOutOfJail(CommunityChest ccJailCard) {
    player.setGetOutOfJail(ccJailCard);
    fireChangeEvent();
  }

  /**
   * @return The number of houses that have been bought for all properties that
   *         are owned by this player
   */
  @Override
  public int getNumHouses() {
    return player.getNumHouses();
  }

  /**
   * @return The number of hotels that have been bought for all properties that
   *         are owned by this player.
   */
  @Override
  public int getNumHotels() {
    return player.getNumHotels();
  }

  /**
   * Calculate whether the player has or can sell enough things to have at least
   * the cash given by amount. If the player's cash is already greater than
   * amount then the method simply returns true. if the player's current cash is
   * less than amount, then the method determines if the player can sell enough
   * houses, hotels, and mortgage properties to have cash greater than or equal
   * to amount. This method does not actually sell any houses, hotels, or
   * properties; it just computes how much cash could be raised if the player
   * sold everything.
   * 
   * @param amount
   *          The amount the player needs to have in cash
   * @return True if the player has or can sell stuff to raise cash greater than
   *         amount, false otherwise.
   */
  @Override
  public boolean canRaiseCash(int amount) {
    return player.canRaiseCash(amount);
  }

  /**
   * Ask if the player wishes to pay bail.
   * 
   * @return True --> player wishes to pay bail to leave jail.<br>
   *         False --> player wishes to attempt to roll doubles to leave jail.
   */
  @Override
  public boolean payBailP() {
    return player.payBailP();
  }

  /**
   * Ask if the player wants to buy their current location.
   * 
   * @return True --> If the player wants to buy the property at their current
   *         location<br>
   *         False --> If the player does not want to buy the property at their
   *         current location.
   */
  @Override
  public boolean buyProperty() {
    return player.buyProperty();
  }

  /**
   * Ask if the player wants to buy the given location.
   * 
   * @return True --> If the player wants to buy the given location <br>
   *         False --> If the player does not want to buy the given location.
   */
  @Override
  public boolean buyProperty(Location location) {
    return player.buyProperty(location);
  }

  /**
   * Output the player genome to a data file.
   * 
   * @param out
   *          The output stream to which data should be written.
   * @throws IOException
   *           If there is a problem writing out the data.
   */
  @Override
  public void dumpGenome(DataOutputStream out) throws IOException
  {}

  /**
   * Determine the amount that this player wants to bid in an auction for the
   * given location. A player can bid on a property in an auction even if the
   * player just decided not to buy it directly after landing on the property.
   * 
   * @param currentLocation
   *          The property being auctioned.
   * @return The amount of this player's bid.
   */
  @Override
  public int getBidForLocation(Location currentLocation) {
    return player.getBidForLocation(currentLocation);
  }

  /**
   * Ask whether player must leave jail or not. Player must leave jail when they
   * have declined to pay bail three times and have had 3 chances to roll
   * doubles but have not rolled doubles.
   * 
   * @return True if the player must pay bail and leave jail, false otherwise.
   */
  @Override
  public boolean jailSentenceCompleted() {
    return player.jailSentenceCompleted();
  }

  /**
   * Change player state based on having paid bail to leave jail.
   */
  @Override
  public void leaveJail() {
    player.leaveJail();
  }

  /**
   * Ask if the player has a monopoly in any property group, ignoring whether
   * any of those properties are mortgaged; callers of this method should also
   * check for mortgaged properties before taking any action for a monopoly.
   * Mortgaged properties can impact what a player can do with a monopoly. For
   * example, even if the player has all the properties of a color group, the
   * player cannot build any houses for a color group if one or more properties
   * are mortgaged.
   * 
   * @return True if the player has at least one monopoly, false otherwise.
   */
  @Override
  public boolean hasMonopoly() {
    return player.hasMonopoly();
  }

  /**
   * Attempt to mortgage properties, then sell hotels, then sell houses until
   * the player's cash is greater than or equal to amount.
   * 
   * @param amount
   *          The amount of cash that the player is trying to have on hand.
   * @throws BankruptcyException
   *           If the player cannot raise enough cash to equal or exceed amount.
   */
  @Override
  public void raiseCash(int amount) throws BankruptcyException {
    player.raiseCash(amount);
  }

  @Override
  public TreeMap<Integer, Location> getAllProperties() {
    return player.getAllProperties();
  }

  /**
   * Add properties from a bankrupt player to this player
   * 
   * @param allProperties
   *          All the properties owned by a player who has gone bankrupt
   * @param gameOver
   *          Whether the game is over because the bankrupt player is the last
   *          other player in the game
   * @throws BankruptcyException
   */
  @Override
  public void addProperties(TreeMap<Integer, Location> allProperties,
      boolean gameOver) throws BankruptcyException {
    player.addProperties(allProperties, gameOver);
    fireChangeEvent();
  }

  /**
   * Create a list of all mortgaged properties and decide whether or not to pay
   * them off.
   */
  @Override
  public void payOffMortgages() {
    player.payOffMortgages();
    fireChangeEvent();
  }

  /**
   * Compute the minimum amount of cash the player should have on hand based on
   * current game conditions.
   * 
   * @return The minimum amount of cash the player should have to avoid problems
   */
  @Override
  protected int getMinimumCash() {
    return player.getMinimumCash();
  }

  /**
   * Take all cash away from the player (called during bankruptcy processing);
   * resets the player cash amount to 0.
   * 
   * @return The value of cash held by the player
   */
  @Override
  public int getAllCash() {
    int allCash = player.getAllCash();
    fireChangeEvent();
    return allCash;
  }

  /**
   * Set the bankrupt flag for this player
   */
  @Override
  public void setBankrupt() {
    player.setBankrupt();
    fireChangeEvent();
  }

  /**
   * Sell all the hotels and houses owned by the player
   */
  @Override
  public void sellAllHousesAndHotels() {
    player.sellAllHousesAndHotels();
    fireChangeEvent();
  }

  /**
   * Attempt to buy a house for a property
   */
  @Override
  public void processDevelopHouseEvent() {
    player.processDevelopHouseEvent();
    fireChangeEvent();
  }

  /**
   * @return The index which indicates in which order the player went bankrupt
   *         in a game: 0 for first, 1 for second, 2 for third, etc.
   */
  @Override
  public Integer getBankruptIndex() {
    return player.getBankruptIndex();
  }

  /**
   * Set an index indicating which order the player went bankrupt, the first
   * player to go bankrupt in a game is assigned index 0, the second to go
   * bankrupt is index 1, and the third player to go bankrupt is 2, etc..
   * 
   * @param index
   *          The value to set.
   */
  @Override
  public void setBankruptIndex(int index) {
    player.setBankruptIndex(index);
  }

  /**
   * Print the chromosome for this player
   */
  @Override
  public void printGenome() {}

  /**
   * Compare players based on fitness score.
   */
  @Override
  public int compareTo(AbstractPlayer arg0) {
    return player.compareTo(arg0);
  }

  /**
   * Set this player's index value
   * 
   * @param index
   *          The ID or index of the player
   */
  @Override
  public void setIndex(int index) {
    player.setIndex(index);
  }

  /**
   * Create a child player by recombination with another parent
   * 
   * @param parent2
   *          The parent to use in reproduction
   * @param index
   *          The index of the child player
   * @return An array containing the child players created by mating this player
   *         with the other parent player.
   */
  @Override
  public AbstractPlayer[] createChildren(AbstractPlayer parent2, int index)
  {
    return player.createChildren(parent2, index);
  }

  @Override
  public String toString() {
    return player.toString();
  }

  /**
   * Copy this player and optionally mutate the new player as well.
   * 
   * @return
   */
  @Override
  public AbstractPlayer copyAndMutate() {
    return player.copyAndMutate();
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    return player.clone();
  }

  /**
   * Join the given game with this player.
   * 
   * @param game
   */
  @Override
  public void joinGame(Monopoly game) {
    player.joinGame(game);
    fireChangeEvent();
  }

  /**
   * Log a string to the debug log using INFO level
   * 
   * @param s
   *          The string to log
   */
  @Override
  public void logInfo(String s) {
    player.logInfo(s);
  }

  /**
   * Log a string to the debug log using FINEST level
   * 
   * @param s
   *          The string to log
   */
  @Override
  public void logFinest(String s) {
    player.logFinest(s);
  }

  /**
   * @return The number of properties owned by the player
   */
  @Override
  public int getNumProperties() {
    return player.getNumProperties();
  }

  /**
   * @return The number of monopolies controlled by this player
   */
  @Override
  public int getNumMonopolies() {
    return player.getNumMonopolies();
  }

  @Override
  public void setGameNetWorth(int totalNetWorth) {
    player.setGameNetWorth(totalNetWorth);
  }

  @Override
  public int getGameNetWorth() {
    return player.getGameNetWorth();
  }

  /**
   * Determine if this player can use the given location to increase the chances
   * of getting monopoly.
   * 
   * @param location2
   *          The location to check
   * @return True if the player owns another property in the same group as
   *         location
   */
  @Override
  public boolean needs(Location location2) {
    return player.needs(location2);
  }

  @Override
  public int evaluateTrade(TradeProposal trade) {
    return player.evaluateTrade(trade);
  }

  /**
   * A method to make trading decisions and act on those decisions.
   * 
   * @param locations
   *          The properties owned by other players that this player can trade
   *          for.
   */
  @Override
  public void processTradeDecisionEvent(ArrayList<Location> locations) {
    player.processTradeDecisionEvent(locations);
  }

  @Override
  public boolean answerProposedTrade(TradeProposal bestTrade) {
    return player.answerProposedTrade(bestTrade);
  }

  /**
   * Return a list of all the players in the game that this player is in.
   * Includes this player
   * 
   * @return An AbstractPlayer array that contains all the players in a game
   */
  @Override
  public AbstractPlayer[] getAllPlayers() {
    return player.getAllPlayers();
  }

  @Override
  public void setGameKey(String factoryKey)
  {
    player.setGameKey(factoryKey);    
  }

  @Override
  public void payIncomeTax() throws BankruptcyException {
    player.payIncomeTax();
  }
  
  public void addChangeListener(ChangeListener cl) {
    changeListeners.add(cl);
  }
  
  protected void fireChangeEvent() {
      fireChangeEvent(new ChangeEvent(this));
  }

  protected void fireChangeEvent(ChangeEvent event) {
    for (ChangeListener cl : changeListeners)
      cl.stateChanged(event);
  }
}
