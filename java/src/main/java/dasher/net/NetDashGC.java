/*
  This file is part of JDasher.

  JDasher is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  JDasher is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with JDasher; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

  Copyright (C) 2006      Christopher Smowton <cs448@cam.ac.uk>

  JDasher is a port derived from the Dasher project; for information on
  the project see www.dasher.org.uk; for information on JDasher itself
  and related projects see www.smowton.net/chris

*/

package dasher.net;

/**
 * This task is scheduled to run every 60 seconds to identify and
 * terminate idle NetDasher sessions.
 * <p>
 * The scheduling is set up by NetDasherListener.
 * <p>
 * Each time the thread is run, the following is checked against
 * each Dasher in the Map supplied by NetDasherListener.
 * <p>
 * <ol><li>First, the Dasher in question is locked.
 * <li>Next we check if it is both idle and has not already
 * been destroyed.
 * <li>If it fulfils both these conditions, its destroy() method.
 * The NetDashConnectionHandler should free as much of Dasher
 * as possible at this point.
 * <li>NetDashConnectionHandlers which have been destroyed are removed
 * from the Map, such that they can be garbage collected.
 * <li>Finally, the lock is released.</ol>
 */
public class NetDashGC extends java.util.TimerTask {

	/**
	 * Map which will be checked for idle Dashers when this thread is run
	 */
	protected java.util.concurrent.ConcurrentHashMap<Integer, NetDashConnectionHandler> Dashers;
	
	/**
	 * Creates a new GC thread attached to a given Map of Dashers.
	 * 
	 * @param Dashs Map to check for idle Dashers when run
	 */
	public NetDashGC(java.util.concurrent.ConcurrentHashMap<Integer, NetDashConnectionHandler> Dashs) {
		Dashers = Dashs;
	}
	
	/**
	 * Checks for idle dashers; see the class summary for details.
	 */
	public void run() {
		
		for(NetDashConnectionHandler dash : Dashers.values()) {

			dash.getLock().lock();
			
			if(dash.isIdle() && !dash.isDestroyed()) {
				int nowFree = dash.getSessID();

				System.out.printf("Session #%d idle; destroying Dasher%n", nowFree);

				dash.destroy();
				
				Dashers.remove(dash.getSessID(), dash);

			}
			
			dash.getLock().unlock();
		}
	}
	
}
