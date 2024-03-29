/**
 * IMAS base code for the practical work.
 * Copyright (C) 2014 DEIM - URV
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cat.urv.imas.map;

import cat.urv.imas.agent.AgentType;
import cat.urv.imas.gui.CellVisualizer;
import cat.urv.imas.ontology.InfoAgent;

/**
 * This class keeps information about a street cell in the map.
 */
public class PathCell extends Cell {

    /**
     * Information about the agent the cell contains.
     */
    private final Agents agents = new Agents();

    /**
     * Builds a cell with a given type.
     *
     * @param row row number.
     * @param col column number.
     */
    public PathCell(int row, int col) {
        super(CellType.PATH, row, col);
    }

    @Override
    public boolean isEmpty() {
        return agents.isEmpty();
    }


    /* ********************************************************************** */
    /**
     * Checks whether this cell contains a cleaner agent picking up some waste
     *
     * @return boolean
     */
    public boolean isCleanerWorking() {
        // This will never give a conflict as one garbage is ONLY assigned to one Cleaner at a time
        return false;
    }

    /**
     * Adds an agent to this cell.
     *
     * @param newAgent agent
     * @throws Exception
     */
    public void addAgent(InfoAgent newAgent) throws Exception {
        if (this.isCleanerWorking()) {
            throw new Exception("Full STREET cell");
        }
        agents.add(newAgent);
    }

    public void removeAgent(InfoAgent oldInfoAgent) throws Exception {
        agents.remove(oldInfoAgent);
    }

    /**
     * Get the current agents from this cell.
     *
     * @return the current agent from this cell.
     */
    public Agents getAgents() {
        return this.agents;
    }

    /* ********************************************************************** */
    /**
     * Gets the string specialization for a street cell.
     *
     * @return string specialization for a street cell.
     */
    @Override
    public String toStringSpecialization() {
        if (this.isCleanerWorking()) {
            return "(agent " + agents.get(AgentType.CLEANER).toString() + ")";
        } else {
            return agents.toString();
        }
    }

    /* ***************** Map visualization API ********************************/
    @Override
    public void draw(CellVisualizer visual) {
        if (agents == null || agents.isEmpty()) {
            visual.drawEmptyPath(this);
        } else {
            if (agents.size() == 1) {
                InfoAgent first;
                try {
                    first = agents.getFirst();
                    switch (first.getType()) {
                        case CLEANER:
                            visual.drawCleaner(this); break;
                        case SEARCHER:
                            visual.drawSearcher(this); break;                       
                        default:
                        // Do nothing. In fact, we'll never get here.
                    }
                } catch (Exception e) {
                    // do nothing: we already checked that an agent exists.
                }
            } else {
                visual.drawAgents(this);
            }
        }
    }

    @Override
    public String getMapMessage() {
        if (agents.isEmpty()) {
            return "";
        } else if (agents.size() == 1) {
            InfoAgent first;
            try {
                first = agents.getFirst();
                return first.getMapMessage();
            } catch (Exception e) {
                // do nothing: we already checked that an agent exists.
            }
        }
        return agents.getMapMessage();
    }

}
