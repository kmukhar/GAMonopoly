package edu.uccs.ecgs.ga;

import java.util.Properties;
import edu.uccs.ecgs.players.AbstractPlayer;


public class SpecialLocation extends Location {

  final private PropertyGroups group = PropertyGroups.SPECIAL;

  public SpecialLocation(String key, Properties properties) {
    super(key, properties);

    _string = "Name           : " + name + 
              "\n  index        : " + index + 
              "\n  type         : " + type +
              "\n  group        : " + group;
  }
  
  @Override
  public AbstractPlayer getOwner() {
    return null;
  }

  @Override
  public int getCost() {
    return 0;
  }

  @Override
  public int getRent(int diceRoll) {
    return 0;
  }

  @Override
  public PropertyGroups getGroup() {
    return group;
  }

  @Override
  public void setMortgaged() {}
  @Override
  public void setMortgaged(boolean b) {}

  @Override
  public int getPotentialRent(int numHouses, int diceRoll)
  {
    return 0;
  }

  @Override
  public String getFormattedString()
  {
    StringBuilder result = new StringBuilder();

    switch(index) {
    case 0:
      result.append("Collect $200 salary as you pass.");
      break;
    case 2:
    case 17:
    case 33:
      result.append("Take the top card from the community chance deck,\n");
      result.append("follow the instructions and return the card to the deck.");
      break;
    case 4:
      result.append("Pay 10% or $200.");
      break;
    case 7:
    case 22:
    case 36:
      result.append("Take the top card from the community chance deck,\n");
      result.append("follow the instructions and return the card to the deck.");
      break;
    case 10:
      result.append("You land in Jail when...\n");
      result.append("(1) Your token lands on the space marked \"Go to Jail\",\n");
      result.append("(2) You draw a card marked \"Go to Jail\" or\n");
      result.append("(3) You throw doubles three times in succession.\n\n");

      result.append("When you are sent to Jail you cannot collect your $200\n");
      result.append("salary in that move since, regardless of where your \n");
      result.append("token is on the board, you must move directly into\n");
      result.append("Jail. Your turn ends when you are sent to Jail.\n\n");
      result.append("If you are not \"sent to jail\" but in the ordinary\n");
      result.append("course of play lands on that space, you are \"Just\n");
      result.append("Visiting\", you incur no penalty, and you move ahead\n");
      result.append("in the usual manner on your next turn. You still are\n");
      result.append("able to collect rent on your properties because you\n");
      result.append("are \"Just Visiting\".");

      result.append("A player gets out of Jail by...\n");
      result.append("(1) Throwing doubles on any of your next three turns,\n");
      result.append("    if you succeed in doing this you immediately move\n");
      result.append("    forward the number of spaces shown by your doubles\n");
      result.append("    throw. Even though you had thrown doubles, you do\n");
      result.append("    not take another turn.\n");
      result.append("(2) Using the \"Get Out of Jail Free Card\"\n");
      result.append("(3) Purchasing the \"Get Out of Jail Free Card\" from\n");
      result.append("another player and playing it.\n");
      result.append("(4) Paying a fine of $50 before you roll the dice on\n");
      result.append("    either of your next two turns. If you do not throw\n");
      result.append("    doubles by your third turn, you must pay the $50\n");
      result.append("    fine. You then get out of Jail and immediately move\n");
      result.append("    forward the number of spaces shown by your throw.\n");
      result.append("    Even though you are in Jail, you may buy and trade\n");
      result.append("    property, and collect rents. Unlike regular Monopoly,\n");
      result.append("    you are unable to buy or sell houses or hotels.\n");
      break;
      
    case 20:
      result.append("Free Parking");
      break;
    case 30:
      result.append("Go directly to Jail - do not pass Go, \n");
      result.append("do not collect $200.");
      break;
    case 38:
      result.append("Pay $75");
      break;
    }

    return result.toString();
  }

  @Override
  public String getFormattedTitle()
  {
    switch(index) {
    case 0:
      return "Go";
    case 2:
    case 17:
    case 33:
      return "Community Chest";
    case 4:
      return "Income Tax";
    case 7:
    case 22:
    case 36:
      return "Chance";
    case 10:
      return "Jail";      
    case 20:
      return "Free Parking";
    case 30:
      return "Go to Jail";
    case 38:
      return "Luxury Tax";
    default:
      return "";
    }
  }
}
