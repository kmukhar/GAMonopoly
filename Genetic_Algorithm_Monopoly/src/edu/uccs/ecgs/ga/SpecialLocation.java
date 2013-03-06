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
      result.append("Take the top card from the community chance deck, ");
      result.append("follow the instructions and return the card to the deck.");
      break;
    case 4:
      result.append("Pay 10% or $200.");
      break;
    case 7:
    case 22:
    case 36:
      result.append("Take the top card from the community chance deck, ");
      result.append("follow the instructions and return the card to the deck.");
      break;
    case 10:
      result.append("You land in Jail when...<p>");
      result.append("<table border=0>");
      result.append("<tr>");
      result.append("<td>(1)</td><td>Your token lands on the space marked ");
      result.append("\"Go to Jail\",</td>");
      result.append("</tr>");
      result.append("<tr>");
      result.append("<td>(2)</td><td>You draw a card marked ");
      result.append("\"Go to Jail\" or</td>");
      result.append("</tr>");
      result.append("<tr>");
      result.append("<td>(3)</td><td>You throw doubles three times in ");
      result.append("succession.</td>");
      result.append("</tr>");
      result.append("</table><p><p>");

      result.append("When you are sent to Jail you cannot collect your $200 ");
      result.append("salary in that move since, regardless of where your ");
      result.append("token is on the board, you must move directly into ");
      result.append("Jail. Your turn ends when you are sent to Jail.<p><p>");
      result.append("If you are not \"sent to jail\" but in the ordinary ");
      result.append("course of play lands on that space, you are \"Just ");
      result.append("Visiting\", you incur no penalty, and you move ahead ");
      result.append("in the usual manner on your next turn. You still are ");
      result.append("able to collect rent on your properties because you ");
      result.append("are \"Just Visiting\".<p><p>");

      result.append("A player gets out of Jail by...<p>");
      result.append("<table border=0>");
      result.append("<tr>");
      result.append("<td valign=top>(1)</td><td valign=top>Throwing doubles ");
      result.append("on any of your next three turns, if you succeed in ");
      result.append("doing this you immediately move forward the number of ");
      result.append("spaces shown by your doubles throw. Even though you ");
      result.append("had thrown doubles, you do not take another turn.</td>");
      result.append("</tr>");
      result.append("<tr>");
      result.append("<td valign=top>(2)</td><td valign=top>Using the \"Get ");
      result.append("Out of Jail Free Card\"</td>");
      result.append("</tr>");
      result.append("<tr>");
      result.append("<td valign=top>(3)</td><td valign=top>Purchasing the ");
      result.append("\"Get Out of Jail Free Card\" from another player and ");
      result.append("playing it.</td></tr>");
      result.append("<tr>");
      result.append("<td valign=top>(4)</td><td valign=top>Paying a fine ");
      result.append("of $50 before you roll the dice on either of your next ");
      result.append("two turns. If you do not throw doubles by your third ");
      result.append("turn, you must pay the $50 fine. You then get out of ");
      result.append("Jail and immediately move forward the number of spaces ");
      result.append("shown by your throw. Even though you are in Jail, you ");
      result.append("may buy and trade property, and collect rents. Unlike ");
      result.append("regular Monopoly, you are unable to buy or sell houses ");
      result.append("or hotels.</td>" +
      		"</tr>");
      result.append("</table>");
      break;
      
    case 20:
      result.append("Free Parking");
      break;
    case 30:
      result.append("Go directly to Jail - do not pass Go,  ");
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
