package edu.uccs.ecgs.ga;

public enum LoadTypes {
	NO_LOAD("Don't load"), 
	LOAD_AND_EVOLVE("Load and evolve"), 
	LOAD_AND_COMPETE("Load and compete");

	private String name;

  private LoadTypes(String name) {
    this.name = name;
  }

  public String toString() {
    return name;
  }
}
