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
 * Monitors the framerate by taking records every time NewFrame
 * is called; this is used to control performance-specific features
 * such as automatic speed control.
 */
public class CFrameRate {
	
	/**
	 * Current frame rate
	 */
	private double m_dFr;
	
	/**
	 * Maximum rate of entering information
	 */
	private double m_dMaxbitrate;         // the maximum rate of entering information
	
	/**
	 * Maximum x movement per frame 
	 */
	private double m_dRXmax;              // the maximum zoomin per frame
	
	/**
	 * Number of frames elapsed
	 */
	private int m_iFrames;
	
	/**
	 * Number of samples currently stored
	 */
	private int m_iSamples;
	
	/**
	 * Time of the second-to-last frame
	 */
	private long m_iTime;
	
	/**
	 * Time of last frame
	 */
	private long m_iTime2;
	
	/**
	 * Not sure! Comments said "see djw thesis" 
	 */
	private int m_iSteps;                 // the 'Steps' parameter. See djw thesis.
	
	/**
	 * Cache of the natural log of 2
	 */
	final double LN2 = Math.log(2.0);

	/**
	 * Gets maximum x movement per frame
	 * 
	 * @return m_dRXmax
	 */
	public double Rxmax() {
	    return m_dRXmax;
	}
	/// Get the minimum size of the target viewport
	////// TODO: Eventually fix this so that it uses integer maths internally. 
	
	/**
	 * Gets the minimum size of the target viewport
	 * 
	 * @param t Current time
	 * @return Minimum size of target viewport
	 */
	public long MinSize(long t) {
		return (long)(t / m_dRXmax);
	}
	
	/**
	 * Gets m_iSteps; see its description
	 * 
	 * @return m_iSteps
	 */
	public int Steps() {
		return m_iSteps;
	} 
	
	/**
	 * Gets current frame rate
	 * 
	 * @return Frame rate (FPS)
	 */
	public double Framerate() {
		return m_dFr;
	} 
	
	// TODO: These two shouldn't be the same thing:
	
	/**
	 * Creates a new framerate monitor; all initial values
	 * are currently hard coded in.
	 * <p>
	 * We start with a frame rate of 32FPS. 
	 */
	public CFrameRate() {
		
		// maxbitrate should be user-defined and/or adaptive. Currently it is not.
		m_dMaxbitrate = 5.5;
		m_dRXmax = 2;                 // only a transient effect
		m_iFrames = 0;
		m_iSamples = 1;
		
		// we dont know the framerate yet - play it safe by setting it high
		m_dFr = 1 << 5;
		
		// start off very slow until we have sampled framerate adequately
		m_iSteps = 2000;
		m_iTime = 0;                  // Hmmm, User must reset framerate before starting.
	}
	
	/**
	 * Resets all our internal values to the same defaults as
	 * are chosen in the constructor.
	 */
	public void Initialise() {
		m_dRXmax = 2;                 // only a transient effect
		m_iFrames = 0;
		m_iSamples = 1;
		
		// we dont know the framerate yet - play it safe by setting it high
		m_dFr = 1 << 5;
		
		// start off very slow until we have sampled framerate adequately
		m_iSteps = 2000;
		m_iTime = 0;                  // Hmmm, User must reset framerate before starting.
	}
	
	/**
	 * Insert a new frame and recompute the frame rate, if enough
	 * samples have been gathered.
	 * <p>
	 * If this would result in a Steps parameter of 0 it is set to 1.
	 * 
	 * @param Time Time at which the new frame began, as a UNIX timestamp.
	 */
	public void NewFrame(long Time)
//	compute framerate if we have sampled enough frames
	{
		m_iFrames++;
		
		if(m_iFrames == m_iSamples) {
			m_iTime2 = Time;
			if(m_iTime2 - m_iTime < 50)
				m_iSamples++;             // increase sample size
			else if(m_iTime2 - m_iTime > 80) {
				m_iSamples--;
				if(m_iSamples < 2)
					m_iSamples = 2;
			}
			if(m_iTime2 - m_iTime != 0) {
				m_dFr = m_iFrames * 1000.0 / (m_iTime2 - m_iTime);
				m_iTime = m_iTime2;
				m_iFrames = 0;
				
			}
			m_dRXmax = Math.exp(m_dMaxbitrate * LN2 / m_dFr);
			m_iSteps = m_iSteps / 2 + (int)(-Math.log(0.2) * m_dFr / LN2 / m_dMaxbitrate) / 2;
			
			// If the framerate slows to < 4 then we end up with steps < 1 ! 
			if(m_iSteps == 0)
				m_iSteps = 1;
			
		}
		
	}
	
	/**
	 * Clears our frame count
	 * 
	 * @param Time System time as a UNIX timestamp.
	 */
	public void Reset(long Time) {
		m_iFrames = 0;
		m_iTime = Time;
	}
	
	/**
	 * Sets the max bitrate
	 * 
	 * @param TargetRate New bitrate
	 */
	public void SetBitrate(double TargetRate) {
		m_dMaxbitrate = TargetRate;
	}
	
	/**
	 * Sets the max bitrate
	 * 
	 * @param MaxRate New bitrate
	 */
	public void SetMaxBitrate(double MaxRate) {
		m_dMaxbitrate = MaxRate;
	}
}
