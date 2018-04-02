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

/**
 * Displays a circle centred around the crosshair
 * which the user can click to start dasher.
 * <p>
 *  Status flags:<br>
 * -1 undefined<br>
 * 0 = out of circle, stopped<br>
 * 1 = out of circle, started<br>
 * 2 = in circle, stopped<br>
 * 3 = in circle, started<br>
 * 4 = in circle, stopping<br>
 * 5 = in circle, starting
 */

public class CCircleStartHandler extends CStartHandler{
	
	/**
	 * Current status of the handler.
	 */
	protected int m_iStatus;
	/**
	 * Time (recorded as a UNIX timestamp) when the status
	 * last changed.
	 */
	protected long m_iChangeTime;
	/**
	 * Radius of the displayed circle in Dasher co-ordinates. 
	 */	
	protected int m_iCircleRadius;
	/**
	 * Radius of the circle in screen co-ordinates.
	 */
	protected int m_iScreenRadius;
	
	/**
	 * Sole constructor. Creates a start handler in its default start state.
	 * Calculates the intial circle radius using LP_CIRCLE_PERCENT and LP_MAX_X.
	 * 
	 * @param EventHandler
	 * @param SettingsStore
	 * @param Interface
	 */
	public CCircleStartHandler(CEventHandler EventHandler, CSettingsStore SettingsStore, CDasherInterfaceBase Interface) 
    {
		super(EventHandler, SettingsStore, Interface);
		m_iStatus = -1;
		m_iChangeTime = 0;
		m_iCircleRadius = (int)(GetLongParameter(Elp_parameters.LP_MAX_Y) * GetLongParameter(Elp_parameters.LP_CIRCLE_PERCENT) / 100);
	}
	
	/**
	 * Draws the start handler to the passed view.
	 * 
	 * @param View DasherView upon which to run drawing commands.
	 * @return True, to indicate that something has been drawn.
	 */
	public boolean DecorateView(CDasherView View) {
				
		CDasherView.Point C = View.Dasher2Screen(2048, 2048);
		
		CDasherView.Point C2 = View.Dasher2Screen(2048, 2048 + m_iCircleRadius);
		
		m_iScreenRadius = C2.y - C.y;
		
		if((m_iStatus == 0) || (m_iStatus == 2))
			View.Screen().DrawCircle(C.x, C.y, m_iScreenRadius, 0, true);
		else if(m_iStatus == 5)
			View.Screen().DrawCircle(C.x, C.y, m_iScreenRadius, 1, true);
		else
			View.Screen().DrawCircle(C.x, C.y, m_iScreenRadius, 0, false);
					
		return true;
	}
	
	/**
	 * Updates the start handler's current state dependent on the current
	 * mouse position.
	 * <p>
	 * In a nutshell, it determines whether the mouse is within the circle,
	 * and if so, instructs Dasher to start or stop dependent on its
	 * current state and how long the user has hovered over the circle.
	 * <p>
	 * Specifically, we start and stop if the user has hovered for over 1 second.
	 * 
	 * @param iTime Current system time, as a UNIX time stamp.
	 * @param m_DasherView View against which co-ordinate transforms should be performed.
	 * @param m_DasherModel Model to which commands should be passed.
	 */
	public void Timer(long iTime, CDasherView m_DasherView, CDasherModel m_DasherModel) {

		CDasherView.DPoint dashXY = m_DasherView.getInputDasherCoords();
		
		CDasherView.Point C = m_DasherView.Dasher2Screen(2048, 2048);
		
		CDasherView.Point Cursor = m_DasherView.Dasher2Screen(dashXY.x, dashXY.y);
		
		double dR;
		
		dR = Math.sqrt(Math.pow((double)(C.x - Cursor.x), 2.0) + Math.pow((double)(C.y - Cursor.y), 2.0));
		
		int iNewStatus = 0;
		
		// Status flags:
		// -1 undefined
		// 0 = out of circle, stopped
		// 1 = out of circle, started
		// 2 = in circle, stopped
		// 3 = in circle, started
		// 4 = in circle, stopping
		// 5 = in circle, starting
		
		// TODO - need to check that these respond correctly to (eg) external pauses
		
		if(dR < m_iScreenRadius) {
			switch(m_iStatus) {
			case -1:
				if(m_Interface.GetBoolParameter(Ebp_parameters.BP_DASHER_PAUSED))
					iNewStatus = 2;
				else
					iNewStatus = 3;
				break;
			case 0:
				iNewStatus = 5;
				break;
			case 1:
				iNewStatus = 4;
				break;
			case 2:
			case 3:
			case 4:
			case 5:
				iNewStatus = m_iStatus;
				break;
			}
		}
		else {
			switch(m_iStatus) {
			case -1:
				if(m_Interface.GetBoolParameter(Ebp_parameters.BP_DASHER_PAUSED))
					iNewStatus = 0;
				else
					iNewStatus = 1;
				break;
			case 0:
			case 1:
				iNewStatus = m_iStatus;
				break;
			case 2:
				iNewStatus = 0;
				break;
			case 3:
				iNewStatus = 1;
				break;
			case 4:
				iNewStatus = 1;
				break;
			case 5:
				iNewStatus = 0;
				break;
			}
		}
		
		if(iNewStatus != m_iStatus) {
			m_iChangeTime = iTime;
		}
		
		if(iTime - m_iChangeTime > 1000) {
			if(iNewStatus == 4) {
				iNewStatus = 2;
				m_Interface.PauseAt(0, 0);
			} 
			else if(iNewStatus == 5) {
				iNewStatus = 3;
				m_Interface.Unpause(iTime);
			}
		}
		
		m_iStatus = iNewStatus;
		
	}
	
	/**
	 * Responds to events:
	 * <p>
	 * BP_DASHER_PAUSED changes: Updates start handler state
	 * so that we don't try to stop when already stopped, etc.
	 */
	public void HandleEvent(CEvent Event) {
		if(Event.m_iEventType == 1) {
			CParameterNotificationEvent Evt = (CParameterNotificationEvent)Event;
			
			if(Evt.m_iParameter == Ebp_parameters.BP_DASHER_PAUSED) {
				m_iStatus = -1;
			}
		}
	}
	
}
