/**
 *  IMAS base code for the practical work.
 *  Copyright (C) 2014 DEIM - URV
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cat.urv.imas.ontology;

/**
 * Content messages for inter-agent communication.
 */
public class MessageContent {
    
    /**
     * Message sent from Coordinator agent to System agent to get the whole
     * city information.
     */
    public static final String GET_MAP = "Get map";

    public static final String NEW_POS = "New pos";
    public static final String MAP_UPDATED = "Map updated";
    public static final String CHILD_REQUEST = "Child request";
    public static final String NEW_GARBAGE = "New garbage" ;
    public static final String NEW_MAP = "New map";
    public static final String REMOVED_GARBAGE = "Remove garbage";
}
