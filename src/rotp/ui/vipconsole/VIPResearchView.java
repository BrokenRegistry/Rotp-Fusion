package rotp.ui.vipconsole;

import static rotp.model.tech.TechCategory.MAX_ALLOCATION_TICKS;
import static rotp.model.tech.TechTree.NUM_CATEGORIES;

import java.util.ArrayList;
import java.util.List;

import rotp.model.tech.Tech;
import rotp.model.tech.TechCategory;
import rotp.model.tech.TechLibrary;
import rotp.model.tech.TechTree;
import rotp.ui.RotPUI;
import rotp.ui.tech.AllocateTechUI;
import rotp.ui.vipconsole.VIPConsole.Command;
import rotp.ui.vipconsole.VIPConsole.CommandMenu;
import rotp.util.Basket;

public class VIPResearchView implements IVIPConsole {
	private static final String TECH_CATEGORY	= "C";
	private static final String TECH_UNKNOWN	= "U";
	private static final String TECH_SPENDING	= "S";
	private static final String TECH_SELECT		= "T";
	private static final String TECH_INFO		= "I";
	private static final String TECH_EQUALIZE	= "=";
	private static final String TECH_DIVERT		= "D";
	private static final String TECH_LOCK		= "L";
	
	// Access to AllocateTechUI
	private AllocateTechUI techUI()		{ return RotPUI.instance().techUI(); }
	private int  selectedCategory()		{ return techUI().selectedTechCategory(); }
	private void selectCategory(int i)	{ techUI().selectTechCategory(i); }
	private void refreshCategory(int i)	{ techUI().refreshTechCategory(i); }

	// ##### ResearchView Command
	CommandMenu initTechMenu(CommandMenu parent)	{
		return new TechMenu("Research Technologies Menu", parent);
	}

	private String processSpending(Entries param)	{
		String out = "";
		if (param.isEmpty())
			return out;
		
		int catNum = selectedCategory();
		while (!param.isEmpty()) {
			String str = param.remove(0).toUpperCase();
			switch (str) {
			case TECH_EQUALIZE:
				out += NEWLINE + equalizeSpending();
				break;
			case TECH_LOCK:
				out += NEWLINE + lockSpending(catNum);
				break;
			case TECH_DIVERT:
				out += NEWLINE + toggleDivert();
				break;
			default:
				// Test for category selection
				if (str.startsWith(TECH_CATEGORY)) {
					Basket res = getIndex(param, str, TECH_CATEGORY);
					if (res.integerValue == null)
						return out + NEWLINE + res.stringValue;
					if (!isValidCatTag(res.integerValue))
						return out + NEWLINE + "Error: invalid category number";
					catNum = getCatNumFromTag(res.integerValue);
					break;
				}
				Integer points = getInteger(str);
				if (points == null)
					return out + NEWLINE + "Error: Wrong or misplaced parameter " + str;
				if (isValidPointTag(points))
					out += NEWLINE + setSpending(catNum, points);
				else
					return out + NEWLINE + "Error: Invalid spending points " + points;
			}
		}
		return out;
	}
	private String showGlobalInfo()	{
		String out = "";
		for (int cat=0; cat<NUM_CATEGORIES; cat++) {
			out += NEWLINE + showCategorySummary(cat);
		}
		return out;
	}
	private String showCategorySummary(int catNum)	{
		String out = getTagFromNum(catNum);
		TechCategory cat = player().tech().category(catNum);
		Tech tech = tech(cat.currentTech());

		String title = text(TechCategory.id(catNum));
		out += " " + title + ": ";
		String researching = cat.researchCompleted() ? text("TECH_RESEARCH_COMPLETED") : text("TECH_RESEARCHING");
		out += NEWLINE + researching;

		if (!cat.researchCompleted()) {
			String techName = tech == null ? text("TECH_NONE_RESEARCHED") : tech.name();
			out += techName;
		}
		out += SPACER + showResearchBubble(cat, false);

		String detail1Key = cat.techDescription1(true);
		out += NEWLINE + detail1Key;

		String detail1Value = cat.techDescription1(false);
		out += SPACER + detail1Value;

		String detail2Key = cat.techDescription2(true);
		if (!detail2Key.isEmpty()) {
			out += SPACER + detail2Key;
			String detail2Value = cat.techDescription2(false);
			out += SPACER + detail2Value;
		}
		return out;
	}
	private String showResearchBubble(TechCategory cat, boolean showMinimum)	{
		if (cat.researchCompleted())
			return "";
		float chance = cat.upcomingDiscoveryChance(techUI().totalPlanetaryResearch());
		if (showMinimum)
			chance = max(.15f, chance);
		if (chance <= 1) {
			int pct = (int) (100 * chance);
			return pct + "% completed"; 
		}
		else {
			int pct = (int) (100 * (chance -1));
			return pct + "% chance to get";
		}
	}
	private String categorySliderString(int catNum)	{
		TechCategory cat = player().tech().category(catNum);
		String out = getLabelFromCatNum(catNum);
		out += EQUAL_SEP + cat.allocation() + " / " + MAX_ALLOCATION_TICKS;
		out += EQUAL_SEP + compactFmt(cat.currentResearch(techUI().totalPlanetaryResearch()), 0, "NUM_FORMAT_RP");
		if (cat.locked())
			out += SPACER + "Locked";
		return out;
	}
	private String toggleDivert()		{
		techUI().toggleOverflowSpending();
		String out = text("TECH_EMPIRE_TECH_RESERVE_OPT");
		out += EQUAL_SEP + player().divertColonyExcessToResearch();
		return out;
	}
	private String equalizeSpending()	{
		techUI().equalize();
		return "Unlocked tech spending equalized.";
	}
	private String lockSpending(int catNum)	{
		techUI().toggleCategoryLock(catNum);
		String out = getLabelFromCatNum(catNum);
		TechCategory cat = player().tech().category(catNum);
		if (cat.locked())
			out += " has been locked";
		else
			out += " has been unlocked";
		return out;
	}
	private String setSpending(int catNum, int newAllocation)	{
		TechCategory cat = player().tech().category(catNum);
        int oldAllocation = cat.allocation();
        int delta = newAllocation - oldAllocation;
        String out = getLabelFromCatNum(catNum);
        if (player().tech().adjustTechAllocation(catNum, delta)) {
            softClick();
            techUI().repaint();
            out += " spending has been set to " + newAllocation + " / " + MAX_ALLOCATION_TICKS;
        }
        else {
        	misClick();
        	out = "Error: " + out + " is locked";
        }
		return out;
	}
	private String showSpending()	{
		//String out = text("TECH_RESEARCH_POINTS");
		String out = text("TECH_EMPIRE_SPENDING");
		// out += NEWLINE + text("TECH_EMPIRE_SPENDING_DESC");
		TechTree tree = player().tech();
		int totalSpending = (int) techUI().totalPlanetaryResearchSpending;
		int totalResearch = 0;
		float totalPlanetaryResearch = techUI().totalPlanetaryResearch();
		for (int catNum=0; catNum<NUM_CATEGORIES; catNum++)
			totalResearch += tree.category(catNum).currentResearch(totalPlanetaryResearch);

		out += " " + compactFmt(totalSpending, 0, "TECH_EMPIRE_DETAIL_1");
		out += " " + compactFmt(totalResearch, 0, "TECH_EMPIRE_DETAIL_2");
		out += NEWLINE + text("TECH_EMPIRE_TECH_RESERVE_OPT");
		out += EQUAL_SEP + player().divertColonyExcessToResearch();
		//out += NEWLINE + text("TECH_ALLOCATE_POINTS");
		for (int catNum=0; catNum<NUM_CATEGORIES; catNum++)
			out += NEWLINE + categorySliderString(catNum);
		out += NEWLINE + text("TECH_EMPIRE_BALANCED_DESC");
		return out;
	}
	private String selectTechToResearch(int tag) {
		Tech tech	= getTechFromTag(tag);
		if (tech==null)
			return "Error: Non-existent technology";
		int catNum	= tech.cat.index();
		TechTree techTree	= player().tech();
		TechCategory cat	= techTree.category(catNum);
		String currentTech	= cat.currentTech();
		boolean newResearch	= cat.researchStarted();
		if (!newResearch)
			return "Selecting new searches is currently not allowed in category " + getLabelFromCatNum(catNum);
		boolean isKnown	= techTree.knows(tech);
		if (isKnown)
			return "You already know this technology";
		boolean isCurrentResearch = tech.id().equals(currentTech);
		if (isCurrentResearch)
			return "You are already researching this technology";
		boolean isPossibleTech = cat.possibleTechs().contains(tech.id);
		if (!isPossibleTech)
			return "You are not allowed to research this technology";
		cat.currentTech(tech);
		refreshCategory(catNum);
		String out = "New research technology selected";
		out += NEWLINE + techBoxString(tech, newResearch, isKnown, currentTech);
		return out;
	}

	private String showCategoryTree(boolean onlyUnknownTech)	{
		int catNum = selectedCategory();
		String out = getTagFromNum(catNum);
		String title = text(TechCategory.id(catNum));
		out += " " + title + ": ";
		TechCategory cat = player().tech().category(catNum);
		boolean newResearch = cat.researchStarted();
		String currentT = cat.currentTech();
		
		int maxQ = cat.maxResearchableQuintile();
		// if we haven't started any research yet (currentT == null)
		// but have acquired a tech through other means (e.g. artifact planet),
		// show all of the known tiers but not the next tier
		if (currentT == null)
			maxQ--;

		int maxTechLvl = maxQ*5;
		List<String> knownT = cat.knownTechs();
		List<String> allT = new ArrayList<>(cat.possibleTechs());
		if (onlyUnknownTech) {
			for (String techId: knownT) {
				if (!tech(techId).free && !player().tech().knows(techId))
					allT.add(techId);
			}
		}
		else {
			for (String techId: knownT) {
				if (!tech(techId).free)
					allT.add(techId);
			}
		}
		Tech[] techs = new Tech[maxTechLvl+1];
		for (String techId: allT) {
			Tech tech = tech(techId);
			if ((tech.level > 0) && (tech.level <= maxTechLvl))
				techs[tech.level] = tech;
		}
		// draw quintiles from 1 to maxQ
		for (int tierNum = maxQ; tierNum >= 0; tierNum--) {
			String tier = "";
			int minLevel = (5 * tierNum) + 1;
			int maxLevel = minLevel + 4;
			if (tierNum == maxQ) {
				if (!onlyUnknownTech) {
					title = text("TECH_NEXT_TIER_TITLE");
					String desc;
					if (currentT == null)
						desc = text("TECH_FIRST_TIER_DESC");
					else if (tierNum < 10)
						desc = text("TECH_NEXT_TIER_DESC");
					else 
						desc = text("TECH_FUTURE_TIER_DESC");
					tier += NEWLINE + title + SPACER + desc;
				}
			}
			else {
				// get available techs in this tier
				List<Tech> displayT = new ArrayList<>();
				for (int j=minLevel;j<=maxLevel;j++)
					if (techs[j] != null)
						displayT.add(techs[j]);
				for (Tech tech: displayT) {
					boolean isKnown	= knownT.contains(tech.id);
					tier += NEWLINE + techBoxString(tech, newResearch, isKnown, currentT);
				}
			}
			if (!tier.isEmpty())
				out += NEWLINE +"Tier " + (tierNum+1) + tier;
		}
		return out;
	}
	private String techBoxString(Tech tech, boolean newResearch, boolean isKnown, String currentT) {
		TechCategory cat	=  player().tech().category(selectedCategory());
		boolean allowSelect	= !isKnown && newResearch && !tech.id().equals(currentT);

		String out = "";
		if(isKnown) {
			out += "Known: ";
		}
		else if (tech.id.equals(currentT)) 
			out += "Current: ";
		if (allowSelect)
			out += getTagFromTech(tech) + " ";
		out += tech.name();

		if (!isKnown) {
			String costLbl = techCostString(cat, tech);
			out += SPACER + "Cost" + EQUAL_SEP + costLbl;
		}
		out += NEWLINE + tech.detail();
		return out;
	}
	private String techCostString(TechCategory cat, Tech tech) {
		if (tech == null)
			return "";
		float costRP = cat.costForTech(tech);
		float chance = 1;
		if ((cat.currentTech() != null) && cat.currentTech().equals(tech.id)) {
			costRP -= cat.totalBC();
			chance = cat.upcomingDiscoveryChance();
		}
		int cost = max(0,(int)Math.ceil(costRP));
		if (chance > 1) {
			int pct = (int) (100* (chance -1));
			return text("TECH_DISCOVERY_PCT",pct);
		}
		return compactFmt(cost, 0, "NUM_FORMAT_RP");
	}
	private String getLabelFromCatNum(int catNum)	{
		String out = getTagFromNum(catNum);
		String catId = TechCategory.id(catNum);
		out += " " + text(catId);
		return out;
	}
	private String getTagFromNum(int cat)		{ return bracketed(TECH_CATEGORY, cat + 1); }
	private int getCatNumFromTag(int tag)		{ return tag-1; }
	private String getTagFromTech(Tech tech)	{
		int userTagType	= tech.techType;	// Start at 1 with Armor
		int userTagSeq	= tech.typeSeq;		// Start at 0
		int userTagTech	= 100*userTagType + userTagSeq;
		return bracketed(TECH_SELECT, userTagTech);
	}
	private Tech getTechFromTag(int tag) 		{
		int userTagType	= tag / 100;
		int userTagSeq	= tag - 100 * userTagType;
		String techId = TechLibrary.techMatching(userTagType, userTagSeq);
		if (techId == null)
			return null;
		return TechLibrary.current().tech(techId);
	}
	private boolean isValidCatTag(int tag)		{ return tag>0 && tag <NUM_CATEGORIES; }
	private boolean isValidPointTag(int tag)	{ return tag>=0 && tag <=MAX_ALLOCATION_TICKS; }
	// ################### SUB COMMAND CLASSES ######################
	private class CmdShowUnknownTechInCategory extends Command {
		CmdShowUnknownTechInCategory  (String descr, String... keys) {
		super(descr, keys);
		cmdParam(" Index");
		cmdHelp("No secondary options");
		}
		@Override protected String execute(Entries param) {
			String out = "";
			if (!param.isEmpty()) {
				String s = param.get(0);
				Integer tag = getInteger(s);
				if (tag != null) {
					selectCategory(getCatNumFromTag(tag));
					out += showCategoryTree(true);
				}
			}
			return out;
		}
	}
	private class CmdSelectCategory extends Command {
		CmdSelectCategory  (String descr, String... keys) {
		super(descr, keys);
		cmdParam(" Index");
		cmdHelp("No secondary options");
		}
		@Override protected String execute(Entries param) {
			String out = "";
			if (!param.isEmpty()) {
				String s = param.get(0);
				Integer tag = getInteger(s);
				if (tag != null) {
					selectCategory(getCatNumFromTag(tag));
					out += showCategoryTree(false);
				}
			}
			return out;
		}
	}
	private class CmdSpending extends Command {
		CmdSpending  (String descr, String... keys) {
		super(descr, keys);
		String tag = bracketed(TECH_CATEGORY, "idx");
		cmdParam(" " + optional("Points", TECH_LOCK, TECH_EQUALIZE, TECH_DIVERT,
				tag + " Points", tag + TECH_LOCK));
		cmdHelp("Change spendings and show results"
				+ NEWLINE + "A total of 60 points can be distributed between categories."
				+ NEWLINE + "Points are taken or given back to the category with the highest index."
				+ NEWLINE + optional("Points")			+ " : Points between 0 an 60 to last used category"
				+ NEWLINE + optional(TECH_LOCK)			+ " : Toggle Lock the last used category"
				+ NEWLINE + optional(TECH_EQUALIZE)		+ " : Equalize the unlocked categories"
				+ NEWLINE + optional(TECH_DIVERT)		+ " : Toggle " + text("TECH_EMPIRE_TECH_RESERVE_OPT")
				+ NEWLINE + optional(tag + " Points")	+ " : Points between 0 an 60 to category index"
				+ NEWLINE + optional(tag + TECH_LOCK)	+ " : Toggle Lock the category index"
				);
		}
		@Override protected String execute(Entries param) {
			String out = processSpending(param);
			out += NEWLINE + showSpending();
			return out;
		}
	}
	private class CmdSelectTechnology extends Command {
		CmdSelectTechnology  (String descr, String... keys) {
		super(descr, keys);
		cmdParam(" Index");
		cmdHelp("No secondary options");
		}
		@Override protected String execute(Entries param) {
			String out = "";
			if (!param.isEmpty()) {
				String s = param.get(0);
				Integer tag = getInteger(s);
				if (tag != null) {
					out += selectTechToResearch(tag);
				}
			}
			return out;
		}
	}
	private class CmdGlobalInfo extends Command {
		CmdGlobalInfo  (String descr, String... keys) {
		super(descr, keys);
		//cmdParam(" Index");
		cmdHelp("No secondary options");
		}
		@Override protected String execute(Entries param) { return showGlobalInfo(); }
	}
	// ################### SUB COMMAND MENU CLASSES ######################
	private class TechMenu extends CommandMenu {
		TechMenu  (String name, CommandMenu parent) {
			super(name, parent);
			addCmd(new CmdGlobalInfo("Show Global Info", TECH_INFO));
			addCmd(new CmdSpending("Show and Change spendings", TECH_SPENDING));
			addCmd(new CmdSelectCategory("select Category and list technologies", TECH_CATEGORY));
			addCmd(new CmdShowUnknownTechInCategory("select Category and list unknown technologies", TECH_UNKNOWN));
			addCmd(new CmdSelectTechnology("select Category to be researched", TECH_SELECT));

		}
		@Override public String open(String out)	{
			out = super.open(out);
			RotPUI.instance().selectTechPanel();
			out += showGlobalInfo();
			return out;
		}
		@Override public String exitPanel()	{
			techUI().exit(false);
			return super.exitPanel();
		}
	}
}
