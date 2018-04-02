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

package dasher;

import java.util.ArrayList;

/**
 * The EventHandler class is a fairly simple device whose responsibilities are to
 * <p>a) Allow DasherComponents to register themselves as event listeners,
 * <p>b) Accept events from DasherComponents and notify all other Components
 * using their HandleEvent method, and
 * <p>c) Allow Components to unregister themselves when they are being destroyed.
 * <p>
 * The mode of action is very simple indeed; we have a list of
 * Listeners to which new listeners are added and old ones removed,
 * and which we iterate through when notified of an event, notifying
 * all other Components by calling their HandleEvent methods in turn.
 * <p>
 * There is however one special case: In the event that a listener tries
 * to register itself whilst an event is being handled (for example, a parameter
 * change caused a Component to be created which immediately tried
 * to register itself), the component is added to a queue of listeners
 * who are waiting to be registered.
 * <p>
 * This is because otherwise there would be some ambiguity as to
 * when a given component would start receiving events, particularly
 * as to whether it would receive that which was being handled
 * when it registered itself.
 * <p>
 * The result is that new listeners will begin receiving events
 * as soon as all those which were in progress when it registered
 * have finished.
 * <p>
 * Because events are dispatched immediately and not added to a queue,
 * there cannot be any certainty as to in what order a given component
 * will hear about multiple events inserted without waiting for
 * the first to end.
 * <p>
 * It is also possible to cause the program to enter a tight
 * loop by creating a direct or indirect loop of dispatched events;
 * Components should be careful to avoid causing this situation by
 * avoiding creating events during their HandleEvent procedures which
 * are likely to cause the same event to be re-raised unless they
 * can be certain of stopping the loop at some future point.
 */
public class CEventHandler {
	  
	/**
	 * List of currently active listeners
	 */
	protected ArrayList <CDasherComponent> m_vListeners;
	
	/**
	 * List of Components waiting to be added as listeners
	 * when we finish handling events.
	 */
	protected ArrayList <CDasherComponent> m_vListenerQueue;

	/**
	 * Integer indicating how many times we are 'in' the event
	 * handler (for example, we might be in twice if a component
	 * responded to an event by issuing an event of its own). 
	 */
	protected int m_iInHandler;
	
	/**
	 * Interface to whom this Handler belongs, and which will
	 * have its InterfaceEventHandler method called for all events.
	 */
	protected CDasherInterfaceBase m_pInterface;
	
	/**
	 * Creates a new EventHandler with no listeners and attached
	 * to a specified interface.
	 * 
	 * @param pInterface Parent interface
	 */
	public CEventHandler(CDasherInterfaceBase pInterface){
	    m_iInHandler = 0;
	    m_pInterface = pInterface;
	    m_vListeners = new ArrayList<CDasherComponent>();
	    m_vListenerQueue = new ArrayList<CDasherComponent>();
	}
	
	/**
	 * Informs all registered listeners of a specified Event.
	 * <p>
	 * Before beginning, m_iInHandler is incremented to indicate
	 * that one event is currently in progress; when finished,
	 * it is decremented and, if zero, listeners which are queued
	 * up for registration will be added to the list of active
	 * listeners.
	 * <p>
	 * The last components to be notified of a given event will
	 * always be the parent Interface and then the external
	 * event handler (by way of the interface's ExternalEventHandler
	 * method.
	 * 
	 * @param Event Event to dispatch to all registered listeners.
	 */
	public void InsertEvent(CEvent Event) {

		  // We may end up here recursively, so keep track of how far down we
		  // are, and only permit new handlers to be registered after all
		  // messages are processed.

		  // An alternative approach would be a message queue - this might actually be a bit more sensible
		  ++m_iInHandler;
		  
		  /* CSFS: Rewritten to use ArrayLists and for-each
		   * instead of C++ list iterators.
		   */

		  // Loop through components and notify them of the event
		  		  
		  for(CDasherComponent i : m_vListeners) {
		    i.HandleEvent(Event);
		  }
		  // Call external handler last, to make sure that internal components are fully up to date before external events happen

		  m_pInterface.InterfaceEventHandler(Event);

		  m_pInterface.ExternalEventHandler(Event);

		  --m_iInHandler;
		  
		  if(m_iInHandler == 0) {
			  
			  
			  for(CDasherComponent i : m_vListenerQueue) {
				  m_vListeners.add(i); 
			  }
			  m_vListenerQueue.clear();
		  }
	}

	/**
	 * Registers a given component as an event listener.
	 * <p>
	 * In the event that one or more events are currently in progress,
	 * it will be added to a queue of pending listeners and will
	 * be added when InsertEvent finishes for the last time.
	 * 
	 * @param pListener Component to add as a listener
	 */
	public void RegisterListener(CDasherComponent pListener) {

		if(m_vListeners.contains(pListener) == false && m_vListenerQueue.contains(pListener) == false) {
		    if(!(m_iInHandler > 0))
		      m_vListeners.add(pListener);
		    else
		      m_vListenerQueue.add(pListener);
		  }
		  else {
		    // Can't add the same listener twice
		  }
		}

	/**
	 * Removes a given Component from the list of listeners.
	 * <p>
	 * This is necessary before a given Component can be garbage
	 * collected.
	 * 
	 * @param pListener Component to remove
	 */
	public void UnregisterListener(CDasherComponent pListener) {
		
		if(m_vListeners.contains(pListener)) m_vListeners.remove(pListener);
		if(m_vListenerQueue.contains(pListener)) m_vListenerQueue.remove(pListener);
	}

}
