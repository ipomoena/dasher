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

import java.util.LinkedList;

/**
 * AutoSpeedControl is responsible for adaptively tuning Dasher's
 * maximum speed by observing user actions whilst dashing.
 * <p>
 * At present it is only used by DefaultFilter.
 * <p>
 * Its mode of action essentially involves observing how
 * 'eratically' the user is behaving, something quantised
 * by the angle they tend to move at compared to due East
 * (in a left-to-right configuration).
 * <p> A more scientific description can be found in
 * dasher/Doc/speedcontrol.tex in the original Dasher
 * source distribution, available from www.dasher.org.uk
 */
public class CAutoSpeedControl extends CDasherComponent {
	
	/**
	 * Current maximum speed.
	 */
	protected double m_dBitrate; //  stores max bit rate internally
	/**
	 * TODO Find out what this does
	 */
	protected double m_dSampleScale, m_dSampleOffset; // internal, control sample size
	/**
	 * How many samples have been taken to date
	 */
	protected int m_nSpeedCounter;  // keep track of how many samples
	/**
	 * Maximum number of samples to take
	 */
	protected int m_nSpeedSamples;  // upper limit on #samples
	/**
	 * Maximum and minimum speed limits
	 */
	protected double m_dSpeedMax, m_dSpeedMin; // bit rate always within this range
	/**
	 * Critical points in user's movement angle variance.
	 */
	protected double m_dTier1, m_dTier2, m_dTier3, m_dTier4; // variance tolerance tiers 
	/**
	 * Fractional changes to speed which can be used to control speed.
	 * <p>Tier 4 represents the greatest change, so we must have 1 < 2 < 3 < 4.
	 */
	protected double m_dChange1, m_dChange2, m_dChange3, m_dChange4; // fractional changes to bit rate
	/**
	 * Controls rate at which MinRadius is adapted; higher means slower.
	 */
	protected double m_dMinRRate; // controls rate at which min. r adapts HIGHER===SLOWER!
	/**
	 * Currently unused; may be used to tune the sensitivity of auto speed-control in the future. 
	 */
	protected double m_dSensitivity; // not used, control sensitivity of auto speed control
	/**
	 * List of the previous angles observed.
	 */
	protected LinkedList<Double> m_dequeAngles = new LinkedList<Double>(); // store angles for statistics
	/**
	 * Variables for adaptive radius calculations
	 */
	protected double m_dSigma1, m_dSigma2, m_dMinRadius;
	
	/**
	 * Creates a new speed controller configured with a sample
	 * size appropriate to the reported frame rate.
	 * 
	 * @param EventHandler Event handler to dispatch and received events.
	 * @param SettingsStore Settings store to retrieve and set settings.
	 * @param dFrameRate Initial frame rate, in FPS.
	 */
	
	public CAutoSpeedControl(CEventHandler EventHandler, CSettingsStore SettingsStore, double dFrameRate)	  {
	
	super(EventHandler, SettingsStore);
	  
		//scale #samples by #samples = m_dSamplesScale / (current bitrate) + m_dSampleOffset
	m_dSampleScale = 1.5;
	m_dSampleOffset = 1.3;
	m_dMinRRate = 80.0;
	m_dSensitivity = GetLongParameter(Elp_parameters.LP_AUTOSPEED_SENSITIVITY) / 100.0; //param only, no GUI!
		//tolerance for automatic speed control
	m_dTier1 = 0.0005;  //  should be arranged so that tier4 > tier3 > tier2 > tier1 !!!
	m_dTier2 = 0.01;
	m_dTier3 = 0.2;
	m_dTier4 = 0.31;
		//bitrate fractional changes for auto-speed control
	m_dChange1 = 1.1;
	m_dChange2 = 1.02;
	m_dChange3 = 0.97;
	m_dChange4 = 0.94;
		//cap bitrate at...
	m_dSpeedMax = 8.0;
	m_dSpeedMin = 0.1;
		//variance of two-centred-gaussians for adaptive radius
	m_dSigma1 = 0.5; 
	m_dSigma2 = 0.05;
		//Initialise auto-speed control
	m_nSpeedCounter = 0;
	m_dBitrate = (double)(round(GetLongParameter(Elp_parameters.LP_MAX_BITRATE) / 100.0));

	  UpdateMinRadius();
	  UpdateSampleSize(dFrameRate); 
	}

	  ////////////////////////////////////////////////
	  ///
	  ///  Change max bitrate based on variance of angle 
	  ///  in dasher space.
	  ///
	  /////////////////////////////////////////////////

	/**
	 * Updates the current dasher speed according to the current
	 * variance in the user's angle of attack. Essentially, we
	 * compare their variance to the m_dTier variables, and multiply
	 * by the corresponding m_dChange in the case that they are above
	 * or below it.
	 * <p>
	 * Also caps the new speed at SpeedMin and SpeedMax.
	 * 
	 * @return New calculated speed.
	 */
	protected double UpdateBitrate()
	{
	  double var = Variance();
	  if(var < m_dTier1)
	  {
	      m_dBitrate *= m_dChange1;
	  }
	  else if(var < m_dTier2)
	  {
	      m_dBitrate *= m_dChange2;
	  }
	  else if(var > m_dTier4) //Tier 4 comes before tier 3 because tier4 > tier3 !!!
	  {
	      m_dBitrate *= m_dChange4;
	  }
	  else if(var > m_dTier3)
	  {
	      m_dBitrate *= m_dChange3;
	  }
	  //else if( in the middle )
	  //    nothing happens! ;

	  //always keep bitrate values sane
	  if(m_dBitrate > m_dSpeedMax) 
	  {
	    m_dBitrate = m_dSpeedMax;
	  }
	  else if(m_dBitrate < m_dSpeedMin) 
	  {
	    m_dBitrate = m_dSpeedMin;
	  }

	  return m_dBitrate;
	}

	/**
	 * Finds the variance in the user's angle of movement.
	 * 
	 * @return Variance
	 */
	protected double Variance() 
	
		{      
		double avgcos, avgsin;
		avgsin = avgcos = 0.0;
		// find average of cos(theta) and sin(theta) 
		for(double i : m_dequeAngles) {
			
			avgcos += Math.cos(i);
			avgsin += Math.sin(i);
		}
		avgcos /= (1.0 * m_dequeAngles.size());
		avgsin /= (1.0 * m_dequeAngles.size());
		//return variance (see dasher/Doc/speedcontrol.tex)
		return -(Math.log(avgcos * avgcos + avgsin * avgsin));
		
	}

	/**
	 * Determines the number of speed samples to take into account,
	 * based on the current frame rate and the user's movement rate.
	 * 
	 * @param dFrameRate Current frame rate
	 * @return Recommended number of samples
	 */
	
	protected int UpdateSampleSize(double dFrameRate)
	{
	  double dFramerate = dFrameRate;
	  double dSpeedSamples = 0.0;
	  double dBitrate = m_dBitrate; 
	  if(dBitrate < 1.0)// for the purposes of this function
	    dBitrate = 1.0; // we don't care exactly how slow we're going
	                    // *really* low speeds are ~ equivalent?
	  dSpeedSamples = dFramerate * (m_dSampleScale / dBitrate + m_dSampleOffset);
	 
	  m_nSpeedSamples = (int)(round(dSpeedSamples));
	  return m_nSpeedSamples;
	}

	
	/**
	 * Finds adaptive minimum radius for
	 * auto-speed control. Calculated by DJCM's
	 * mixture-of-2-centred-gaussians model.
	 * 
	 * @return New minimum radius.
	 */
	protected double UpdateMinRadius() 
	{
	  m_dMinRadius = Math.sqrt( Math.log( (m_dSigma2 * m_dSigma2) / (m_dSigma1 * m_dSigma1) ) / 
	                ( 1 / (m_dSigma1 * m_dSigma1) - 1 / (m_dSigma2 * m_dSigma2)) );
	  return m_dMinRadius;
	}


	
	/**
	 * Updates VARIANCES of two populations of 
 	 * mixture-of-2-centred-Gaussians model!
 	 * 
 	 * @param r Current minimum radius
 	 * @param dFrameRate Current Dasher frame rate in FPS
 	 */

	public void UpdateSigmas(double r, double dFrameRate)
	{
	  double dSamples = m_dMinRRate* dFrameRate / m_dBitrate;
	  if(r > m_dMinRadius)
	    m_dSigma1 = m_dSigma1 - (m_dSigma1 - r * r) / dSamples;
	  else 
	    m_dSigma2 = m_dSigma2 - (m_dSigma2 - r * r) / dSamples;
	}

/**
 * AUTOMATIC SPEED CONTROL, CEH 7/05: Analyse variance of angle
 * mouse position makes with +ve x-axis in Dasher-space.
 * <p>
 * SpeedControl alters the LP_MAX_BITRATE parameter to reflect
 * the perceived user's need. 
 * 
 * @param iDasherX User's current mouse position in Dasher co-ordinates
 * @param iDasherY User's current mouse position in Dasher co-ordinates
 * @param dFrameRate Current Dasher frame rate in FPS
 * @param View Current DasherView, used to ascertain the user's true
 *             mouse position.
 */
	public void SpeedControl(long iDasherX, long iDasherY, double dFrameRate, CDasherView View) {
	  if(GetBoolParameter(Ebp_parameters.BP_AUTO_SPEEDCONTROL) && !GetBoolParameter(Ebp_parameters.BP_DASHER_PAUSED)) {
	    
//	  Coordinate transforms:    
	    iDasherX = (long)(View.applyXMapping(iDasherX / (double)(GetLongParameter(Elp_parameters.LP_MAX_Y))) * GetLongParameter(Elp_parameters.LP_MAX_Y));
	    iDasherY = (long)(View.ymap(iDasherY));

	    long iDasherOX = (long)(View.applyXMapping(GetLongParameter(Elp_parameters.LP_OX) / (double)(GetLongParameter(Elp_parameters.LP_MAX_Y))) * GetLongParameter(Elp_parameters.LP_MAX_Y));
	    long iDasherOY = (long)(View.ymap(GetLongParameter(Elp_parameters.LP_OY)));

	    double x = -(iDasherX - iDasherOX) / (double)iDasherOX; //Use normalised coords so min r works 
	    double y = -(iDasherY - iDasherOY) / (double)iDasherOY; 
	    double theta = Math.atan2(y, x);
	    double r = Math.sqrt(x * x + y * y);
	    m_dBitrate = GetLongParameter(Elp_parameters.LP_MAX_BITRATE) / 100.0; //  stored as long(round(true bitrate * 100))

	    UpdateSigmas(r, dFrameRate);

//	  Data collection:
	    
	    // CSFS: Replaced push_back with addLast and pop_front with removeFirst.
	    // 		 (C++ deque to Java LinkedList translation)
	    // CSFS: Replaced C++ 'fabs' with Math.abs
	    
	    if(r > m_dMinRadius && Math.abs(theta) < 1.25) {
	      m_nSpeedCounter++;
	      m_dequeAngles.addLast(theta);
	      while(m_dequeAngles.size() > m_nSpeedSamples) {
		    m_dequeAngles.removeFirst();
	      }
	      
	    }
	    m_dSensitivity = GetLongParameter(Elp_parameters.LP_AUTOSPEED_SENSITIVITY) / 100.0;
	    if(m_nSpeedCounter > round(m_nSpeedSamples / m_dSensitivity)) {
	      //do speed control every so often!
	      
	      UpdateSampleSize(dFrameRate);
	      UpdateMinRadius();
	      UpdateBitrate();
	      long lBitrateTimes100 =  (long)(round(m_dBitrate * 100)); //Dasher settings want long numerical parameters
	      SetLongParameter(Elp_parameters.LP_MAX_BITRATE, lBitrateTimes100);
	      m_nSpeedCounter = 0;	  
	    
	    }	
	  
	  }
	}
	  

	/**
	 * Rounds a double to the nearest integer without
	 * casting to an integer type.
	 * 
	 * @param dVal Value to round
	 * @return Nearest integer (as an FP value)
	 */
	
	protected double round(double dVal) {
		  double dF = Math.floor(dVal);
		  double dC = Math.ceil(dVal);
		  if(dVal - dF < dC - dVal)
		    return dF;
		  else
		    return dC;
	  }

	
}
