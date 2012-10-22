package edu.uccs.ecgs.players;

/**
 * A factory for creating player instances based on the chromosome type.
 *
 */
public class PlayerFactory {
  /**
   * Get an instance of the player class of the given chromosome type.
   * @param index The player id
   * @param chromoType The type of player to create
   * @return An AbstractPlayer reference to a player of the given type
   */
  public static AbstractPlayer getPlayer(int index, ChromoTypes chromoType) {
    AbstractPlayer player = chromoType.getPlayer(index);
    return player;
  }
}
