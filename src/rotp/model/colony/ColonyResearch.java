/*
 * Copyright 2015-2020 Ray Fowler
 * 
 * Licensed under the GNU General Public License, Version 3 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     https://www.gnu.org/licenses/gpl-3.0.html
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rotp.model.colony;

import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import rotp.model.empires.Empire;

public final class ColonyResearch extends ColonySpendingCategory {
    private static final long serialVersionUID = 1L;
    private float projectBC = 0;
    private float unallocatedBC = 0;
    private ColonyResearchProject project;
    private transient ColonyResearchProject completedProject;

    public boolean hasProject()                    { return project != null; }
    public void project(ColonyResearchProject p)   { project = p; }
    public void endProject()                       { 
        completedProject = project; 
        project = null; 
        colony().reallocationRequired = true;
    }
    public boolean hasCompletedProject()           { return completedProject != null; }
    public ColonyResearchProject completedProject() { return completedProject; }

    @Override
    public int categoryType()           { return Colony.RESEARCH; }
    @Override
    public boolean isCompleted()        { return false; }
    @Override
    public float totalBC()              { return totalSpending() * researchBonus(); }
    @Override
    public float totalBCForEmpire()     { return max(0, totalBC() - projectRemainingBC()); }
	float totalSpending()		{ return super.totalBC(); }
	float projectRemainingBC()	{ return project == null ? 0 : project.remainingResearchBC(); }
    @Override
    public void nextTurn(float totalProd, float totalReserve) {
        completedProject = null;
        unallocatedBC = totalBC();
        projectBC = 0;
        // there may be a special project at this colony that consumes research
        if (project != null) {
            projectBC = min(unallocatedBC, project.remainingResearchBC());
            project.addResearchBC(projectBC);
            unallocatedBC -= projectBC;
        }       
    }
	float researchBonus()	{
    	return  planet().researchAdj()
    			* empire().researchBonusPct()
    			* empire().researchNoSpyBonusPct()
    			* session().researchBonus();
    }
    @Override
    public boolean warning()      {
        return (project != null) && (max(0, totalBC()) < project.remainingResearchBC()); }
    @Override
    public String upcomingResult() {
        if (colony().allocation(categoryType()) == 0)
            return text(noneText);

        float bc = totalBC();
        if (project != null) {
            bc -=  project.remainingResearchBC();
            if (bc <= 0)
                return text(project.projectKey());
        }
        
        if (colony().empire().tech().researchCompleted())
            return text(reserveText);

        return text(researchPointsText, (int)bc);
    }
    @Override
    public void assessTurn() { }
	void commitTurn()	{ 
        if (empire().tech().researchCompleted()) 
            empire().addReserve(unallocatedBC);
           
        unallocatedBC = 0;
    }
	void capturedBy(Empire newCiv)	{
        if (newCiv == empire())
            return;
        projectBC = 0;
        unallocatedBC = 0;
    }
    @Override public int smoothAllocationNeeded(boolean prioritized) { 
		if (empire().tech().researchCompleted())
			return 0;
		else
			return MAX_TICKS;
    }
    @Override public int smartAllocationNeeded(MouseEvent e) {
    	if (empire().tech().researchCompleted())
			return 0;
    	if (e==null || SwingUtilities.isLeftMouseButton(e))
   			return smoothAllocationNeeded(colony().prioritizeResearch());
    	if (SwingUtilities.isRightMouseButton(e))
    		return 0;
    	if (SwingUtilities.isMiddleMouseButton(e))
    		return 0;
    	return 0;
    }
}
