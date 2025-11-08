package rotp.model.empires.species;

import rotp.ui.util.StringList;

class SpeciesUniqueNames {
	private static final StringList enLabels = new StringList("_empire,_race,_race_plural,_title,_nameTitle", ",");
	private static final StringList frLabels = new StringList("_empireof,_raceadjec,_raceadjecF"
			+ ",_race_pluralnoun,_race_pluralnounof,_race_pluralnounto,_race_pluraladjec,_race_pluraladjecF"
			+ ",_title,_nameTitle", ",");
	private final StringList speciesNames	 = new StringList();
	private final StringList homeSystemNames = new StringList();
	private final StringList leaderNames	 = new StringList();
	private StringList remainingSpeciesNames;
	private StringList remainingHomeworldNames;
	private StringList remainingLeaderNames;

	public void editEnLabels() {
		
	}
}
