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
 * A DasherNode represents a node in the DasherModel's tree; it
 * is has a probability, children and one parent, and is typically
 * drawn as a box with a letter or symbol in it.
 * <p>
 * It is capable of finding a Node at a given Screen location
 * and of performing certain tree modifications (such as deleting
 * its children) but otherwise mainly acts as a data structure.
 */
public class CDasherNode {

	//	Information required to display the node
	/**
	 * Colour scheme associated with this node
	 */
	protected EColorSchemes m_ColorScheme;
	
	/**
	 * Phase; this is related to colour cycling from one generation
	 * to the next.
	 */
	protected int m_iPhase;
	
	/**
	 * Colour number if this node is using a colour scheme defined
	 * by an instance of CCustomColours
	 */
	protected int m_iColour;
	
	/**
	 * Lower cumulative probability bound relative to our parent
	 */
	protected long m_iLbnd;
	
	/**
	 * Upper cumulative probability bound relative to our parent
	 */
	protected long m_iHbnd; 

	/**
	 * Indicates whether this node is currently drawable
	 */	
	protected boolean m_bAlive;
	
	/**
	 * Indicates whether this node's symbol has been output already
	 */
	protected boolean m_bSeen;
	 
	// Information internal to the data structure
	
	/**
	 * List of this node's child Nodes
	 */
	protected ArrayList<CDasherNode> m_mChildren;
	
	/**
	 * Flag indicating whether all of this node's children are present
	 */
	protected boolean m_bHasAllChildren;

	/**
	 * Parent Node
	 */
	protected CDasherNode m_Parent;

	// TODO: The following should be included in m_pUserData, as they only apply to nodes managed by CAlphabetManager
	
	/**
	 * LanguageModel which was responsible for generating this
	 * node's probability
	 */
	protected CLanguageModel m_LanguageModel;     // pointer to the language model - in future, could be different for each node      
	
	/**
	 * Language model context corresponding to this node's
	 * position in the tree.
	 */
	protected CContextBase m_Context;
	
	/**
	 * Symbol number represented by this node
	 */
	protected int m_Symbol;	// the character to display
	
	/**
	 * Root of the tree of groups into which this Node's
	 * children are arranged.
	 */
	public SGroupInfo m_BaseGroup;
	
	/**
	 * NodeManager responsible for populating this Node's
	 * children.
	 */
	public CNodeManager m_NodeManager;
    /* CSFS: This was a void pointer in the original C++. Since it always seemed
     * to get cast to an int and used as such, I've made it an int for now.
     * If trouble crops up this may need to become an Object or something else
     * awkward.
     */
    
    /**
     * Stores arbitrary information; may be extended to hold
     * more in the future.
     */
	public int m_UserData;

	/**
	 * This Node's display text
	 */
    public String m_strDisplayText;

    /**
     * Whether this Node shoves or not (ie. whether the symbols
     * of its children should be displaced to the right so as
     * not to obscure this one)
     */
    public boolean m_bShove;
	
	/**
	 * Creates a Node and sets its describing variables.
	 * 
	 * @param Parent Parent Node
	 * @param Symbol Symbol number
	 * @param iphase Colour-cycling phase
	 * @param ColorScheme Colour scheme
	 * @param ilbnd Lower bound of cum. probability relative to parent
	 * @param ihbnd Upper bound of cum. probability relative to parent
	 * @param lm LanguageModel
	 * @param Colour Colour number
	 */
    public CDasherNode(CDasherNode Parent, int Symbol, int iphase, EColorSchemes ColorScheme, long ilbnd, long ihbnd, CLanguageModel lm, int Colour) {
		m_iLbnd = ilbnd;
		m_iHbnd = ihbnd;
		m_Symbol = Symbol;
		m_mChildren = new ArrayList<CDasherNode>();
		// m_bHasAllChildren = false; (default)
		m_bAlive = true;
		// m_bSeen = false; (default)
		m_ColorScheme = ColorScheme;
		m_iPhase = iphase;
		m_iColour = Colour;
		m_LanguageModel = lm;
		m_Context = CLanguageModel.nullContext;
		m_Parent = Parent;
		// m_strDisplayText = new String();
		// WARNING: Whilst I think this is redundant it MAY get used uninitialised.
		
		/* CSFS: BUGFIX: There used to be a chance this was used uninitialised.
		 * Fixed.
		 */
		
	}

	/**
	 * At present, does nothing; Nodes will be "deleted" by virtue
	 * of their parents cutting them loose at the appropriate time,
	 * making them available for garbage collection.
	 *
	 */
    public void DeleteNode() {

	  /* CSFS: Lifted from C++ destructor function. Must be called for all nodes before they
	   * are garbage collected.
	   */
		
		// ClearNode and ReleaseContext both do nothing, so I'm experimenting with this method doing nothing.
		
      // Release any storage that the node manager has allocated,
	  // unreference ref counted stuff etc.

	  //m_NodeManager.ClearNode(this);

	  //Delete_children();
	  //if(m_Context != null)
	  //  m_LanguageModel.ReleaseContext(m_Context);
	}

	/**
	 * Sets the context associated with this node.
	 * <p>
	 * Releases its existing context with the language model
	 * if one exists.
	 * 
	 * @param context New context
	 */
    public void SetContext(CContextBase context) {
	  if(m_Context != null)
	    m_LanguageModel.ReleaseContext(m_Context);
	  m_Context = context;
	}

    /**
     * Gets m_iLbnd
     * 
     * @return m_iLbnd
     */
	public long Lbnd() {
	  return m_iLbnd;
	}

	/**
     * Gets m_iHbnd
     * 
     * @return m_iHbnd
     */
	public long Hbnd() {
	  return m_iHbnd;
	}

	/**
	 * Gets the difference between m_iLbnd and m_iHbnd.
	 * 
	 * @return Range
	 */
	public long Range() {
	  return m_iHbnd - m_iLbnd;
	}

	/**
	 * Gets a reference to this Node's child list. 
	 * 
	 * @return m_mChildren
	 */
	public synchronized ArrayList<CDasherNode> Children() {
	  return m_mChildren;
	}

	/**
	 * Gets a reference to this Node's child list. 
	 * 
	 * @return m_mChildren
	 */
	public synchronized ArrayList<CDasherNode> GetChildren() {
	  return m_mChildren;
	}
	
	/**
	 * Sets this Node's child list. 
	 * 
	 * @param in The list of children to be attached to this node.
	 */
	public synchronized void SetChildren(ArrayList<CDasherNode> in) {
		m_mChildren = in;
	}

	/**
	 * Retrives the LM context associated with this node.
	 * 
	 * @return Context
	 */
	public CContextBase Context() {
	  return m_Context;
	}
	
	/**
	 * Determine if a given node is this one's parent.
	 * 
	 * @param oldnode Potential parent
	 * @return True if parent
	 */
	public boolean NodeIsParent(CDasherNode oldnode) {
		  if(oldnode == m_Parent)
		    return true;
		  else
		    return false;

	}	
	
	/**
	 * Returns the size of our child list.
	 * 
	 * @return Size of m_mChildren.
	 */
	public synchronized int ChildCount() {
	    return m_mChildren.size();
	}

	/**
	 * Gets this node's parent.
	 * 
	 * @return Parent node.
	 */
	public CDasherNode Parent() {
	    return m_Parent;
	}
	  
	/**
	 * Attaches this Node to a given Parent.
	 * 
	 * @param NewParent Our new parent
	 */
	public void SetParent(CDasherNode NewParent) {
	    m_Parent = NewParent;
	}
	
	/**
	 * Sets this node's probabilities.
	 * 
	 * @param iLower New Lbnd
	 * @param iUpper New Hbnd
	 */
	public void SetRange(long iLower, long iUpper) {
	    m_iLbnd = iLower;
	    m_iHbnd = iUpper;
	}

	/**
	 * Determines if this node is currently drawable.
	 * 
	 * @return m_bAlive
	 */
	public boolean Alive() {
	    return m_bAlive;
	}
	 
	/**
	 * Sets this node's Alive flag
	 * 
	 * @param b new value
	 */
	public void Alive(boolean b) {
	    m_bAlive = b;
	}

	/**
	 * Sets this node's Alive flag to false
	 *
	 */
	public void Kill() {
	    m_bAlive = false;
	}

	/**
	 * Gets this node's Seen flag
	 * 
	 * @return Seen
	 */
	public boolean isSeen() {
	    return m_bSeen;
	} 

	/**
	 * Sets this node's Seen flag
	 * 
	 * @param seen new value
	 */
	public void Seen(boolean seen) {
	    m_bSeen = seen;
	}

	/**
	 * Gets this node's symbol
	 * 
	 * @return symbol number
	 */
	public int Symbol() { 
	    return m_Symbol; 
	}

	/**
	 * Gets this node's colour-cycling phase
	 * 
	 * @return Phase
	 */
	public int Phase() {
	    return m_iPhase;
	}
	// DJW Sort out the colour scheme / phase confusion
	
	/**
	 * Gets this Node's colour scheme
	 */
	public EColorSchemes ColorScheme() {
	    return m_ColorScheme;
	} 
	  
	/**
	 * Gets this Node's colour index
	 * 
	 * @return Colour index
	 */
	public int Colour() {
	    return m_iColour;
	} 

	/**
	 * Checks whether all of this Node's children exist at present
	 * 
	 * @return m_bHasAllChildren
	 */
    public boolean HasAllChildren() {
	    return m_bHasAllChildren;
	}
	 
    /**
     * Sets the HasAllChildren flag
     *
     * @param val new value
     */
    public void SetHasAllChildren(boolean val) {
	    m_bHasAllChildren = val;
	}
    
    /**
     * Gets the probability associated with this node's most
     * probable child.
     * 
     * @return Probability
     */
    public long MostProbableChild() {
    	
    	long iMax = 0;
    	    	
    	for(CDasherNode i : this.GetChildren()) {
    		if(i.Range() > iMax) iMax = i.Range();
    	}
    	
    	return iMax;
    }

   
    /**
     * Finds the node under a given mouse location, given our boundaries
     * in Dasher space. Essentially this iterates through our children
     * checking whether the point falls within each child, and if so recalls
     * this method in the child. If no children match, we replace a reference
     * to ourselves.
     * <p>
     * The "mouse position" may of course be replaced by any other
     * point we want to find the node under. 
     * 
     * @param iNormalization Normalization value, usually LP_NORMALIZATION
     * @param miY1 Our upper bound in Dasher co-ordinate space
     * @param miY2 Our lower bound in Dasher co-ordiante space
     * @param miMousex Current mouse position X
     * @param miMousey Current mouse position Y
     * @return Node under this mouse position.
     */
    public CDasherNode Get_node_under(long iNormalization, long miY1, long miY2, long miMousex, long miMousey) {
		
		long miRange = miY2 - miY1;
        m_bAlive = true;
        
        for(CDasherNode i : this.GetChildren()) {
        	long miNewy1 = miY1 + (miRange * i.m_iLbnd) / iNormalization;
        	long miNewy2 = miY1 + (miRange * i.m_iHbnd) / iNormalization;
        	if(miMousey < miNewy2 && miMousey > miNewy1 && miMousex < miNewy2 - miNewy1)
        		return i.Get_node_under(iNormalization, miNewy1, miNewy2, miMousex, miMousey);
        }
        return this;
	}
	
//		 kill ourselves and all other children except for the specified
//		 child
//		 FIXME this probably shouldn't be called after history stuff is working
	
    /**
     * Delete our children except for a given one, the delete ourself.
     * 
     * @param pChild Child to keep
     */
    public void OrphanChild(CDasherNode pChild) {
		  assert(ChildCount() > 0);
		  
		  /* CSFS: Iteration through children changed as per previous
		   * and also the new DeleteNode() routines used.
		   * Finally delete replaced with orphaning the object,
		   * leaving it ready for GC.
		   */
	  
		  for(CDasherNode i : this.GetChildren()) {
			  if(i != pChild) {
				  i.Delete_children();
				  i.DeleteNode();
				  i = null;
			  }
			  
		  }
		  Children().clear();
		  SetHasAllChildren(false);
	}


    /**
     * Deletes the nephews of a given child; that is to say,
     * deletes its siblings' children. 
     * 
     * @param pChild Child whose children will NOT be deleted.
     */
	public void DeleteNephews(CDasherNode pChild) {
		  assert(Children().size() > 0);
		  
		  for(CDasherNode i : this.GetChildren()) {
		    if(i != pChild) {
		      i.Delete_children();
		    }

		  }
	}

	/**
	 * Clears the Children list and sets our HasAllChildren flag
	 * to false; this will leave this section of tree unrefrenced
	 * and available for garbage collection.
	 *
	 */
	public void Delete_children() {
		
		//for(CDasherNode i : this.GetChildren()) {
			// i.DeleteNode(); (stub method)
		//	i = null;
		//}
		Children().clear(); // This should be enough to render them GC-able.
		SetHasAllChildren(false);
	}

//		 Gets the probability of this node 
	
	/**
	 * Gets this node's probability
	 * 
	 * @param iNormalization Norm value, usually LP_NORMALIZATION
	 */
	public double GetProb(int iNormalization) {    
		  return (double) (m_iHbnd - m_iLbnd) / (double) iNormalization;
	}

}
