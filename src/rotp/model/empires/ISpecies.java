package rotp.model.empires;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface ISpecies {
	String CUSTOM_SPECIES_FOLDER	= "CustomSpecies/";
	String INTRO_FILE_EXTENSION		= ".intro.txt";
	String CR_EMPIRE_NAME_RANDOM	= "Randomized";

	// Exclusions keys
	List<String> notTalking	= new ArrayList<>(Arrays.asList("Mouth"));
	List<String> closed		= new ArrayList<>(Arrays.asList("Open"));
	List<String> open		= new ArrayList<>(Arrays.asList("Closed"));
	List<String> notFiring	= new ArrayList<>(Arrays.asList("Firing"));
}
