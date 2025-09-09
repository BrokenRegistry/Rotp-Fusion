package rotp.model.empires;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rotp.util.Base;

public final class SpeciesLabels implements Base, Serializable {
	private static final long serialVersionUID	= 1L;
	final List<String> raceNames = new ArrayList<>();
	final List<String> homeSystemNames = new ArrayList<>();
	final List<String> leaderNames = new ArrayList<>();
	final List<String> systemNames = new ArrayList<>();

	final List<String> shipNamesSmall = new ArrayList<>();
	final List<String> shipNamesMedium = new ArrayList<>();
	final List<String> shipNamesLarge = new ArrayList<>();
	final List<String> shipNamesHuge = new ArrayList<>();

	final List<String> remainingRaceNames = new ArrayList<>();
	final List<String> remainingHomeworldNames = new ArrayList<>();
	final List<String> remainingLeaderNames = new ArrayList<>();

	boolean emptyNames()			{
		return raceNames.isEmpty() && homeSystemNames.isEmpty()
				&& leaderNames.isEmpty() && systemNames.isEmpty();
	}
	boolean emptyShipNames()		{
		return shipNamesSmall.isEmpty() && shipNamesMedium.isEmpty()
				&& shipNamesLarge.isEmpty() && shipNamesHuge.isEmpty();
	}
	boolean emptyRemainingNames()	{
		return remainingRaceNames.isEmpty() && remainingHomeworldNames.isEmpty()
				&& remainingLeaderNames.isEmpty();
	}
	boolean isEmpty()	{ return emptyNames() && emptyShipNames() && emptyRemainingNames(); }

	String nextAvailableName() {
		if (remainingRaceNames.isEmpty()) {
			loadNameList();
			if (remainingRaceNames.isEmpty()) 
				return null;
		}
		return remainingRaceNames.remove(0);
	}
	int nameIndex(String n)			{ return raceNames.indexOf(n); }
	String nameVariant(int i)		{ return i>= raceNames.size()? null : raceNames.get(i); }
	String nextAvailableLeader()	{
		if (remainingLeaderNames.isEmpty()) {
			loadLeaderList();
			if (remainingLeaderNames.isEmpty()) 
				return null;
		}
		return remainingLeaderNames.remove(0);
	}
	String nextAvailableHomeworld()	{
		if (remainingHomeworldNames.isEmpty()) {
			loadHomeworldList();
			if (remainingHomeworldNames.isEmpty()) 
				return null;
		}
		return remainingHomeworldNames.remove(0);
	}
	String setupName()				{
		if (raceNames.isEmpty())
			return "";
		return text(substrings(raceNames.get(0), '|').get(0));
	}
	void loadNameList()				{
		remainingRaceNames.clear();
		List<String> secondaryNames =  new ArrayList<>(raceNames);
		if (secondaryNames.isEmpty())
			return;
		remainingRaceNames.clear();
		remainingRaceNames.add(secondaryNames.remove(0));
		Collections.shuffle(secondaryNames);
		remainingRaceNames.addAll(secondaryNames);
	}
	void loadLeaderList()			{
		remainingLeaderNames.clear();
		List<String> secondaryNames =  new ArrayList<>(leaderNames);
		if (secondaryNames.isEmpty())
			return;
		remainingLeaderNames.add(secondaryNames.remove(0));
		Collections.shuffle(secondaryNames);
		remainingLeaderNames.addAll(secondaryNames);
	}
	void loadHomeworldList()		{
		remainingHomeworldNames.clear();
		List<String> homeNames =  new ArrayList<>(homeSystemNames);
		if (homeNames.isEmpty())
			return;
		remainingHomeworldNames.clear();
		remainingHomeworldNames.add(homeNames.remove(0));
		Collections.shuffle(homeNames);
		remainingHomeworldNames.addAll(homeNames);
	}
	void systemNames(List<String> s){
		systemNames.clear();
		systemNames.addAll(s);
	}
	String defaultHomeworldName()	{ return homeSystemNames.isEmpty()? "Empty" : homeSystemNames.get(0); }
	boolean hasHomeworldNames()		{ return !homeSystemNames.isEmpty(); }
	void parseRaceNames(String str)	{
		raceNames.clear();
		raceNames.addAll(substrings(str, ','));
	}
}
