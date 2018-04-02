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
 * Subclass of LanguageModel which implements Prediction by
 * Partial Match. For information on the algorithm and its 
 * implementation, see (http://www.compression-links.info/PPM).
 * <p>
 * For the general contract obeyed by LanguageModel methods, see
 * the documentation of CLanguageModel.
 */
public class CPPMLanguageModel extends CLanguageModel {

	public CPPMContext m_RootContext;
	public CPPMnode m_Root;

	public int m_iMaxOrder;
	public double m_dBackOffConstat;

	public int NodesAllocated;

	public boolean bUpdateExclusion;

	private long lpAlpha;
	private long lpBeta;

	// public CSimplePooledAlloc < CPPMnode > m_NodeAlloc;
	// public CPooledAlloc < CPPMContext > m_ContextAlloc;

	/* CSFS: I have modified this to use GC for allocation for the time being; the memory
	 * pools are next to useless in a garbage-collected language anyway since you can't
	 * return a pointer to a free location. I will modify this later to use something
	 * which works in a GC language AND is efficient.
	 */

	/* class BinaryRecord {
    	  public int m_iIndex;
    	  public int m_iChild;
    	  public int m_iNext;
    	  public int m_iVine;
    	  public int m_iCount;
    	  public short m_iSymbol;
    	  } */ // This is part of the LMIO framework and so is not needed at present.

	/**
	 * Class representing a context which contains information
	 * relevant to the PPM model.
	 */
	class CPPMContext extends CContextBase {

		private CPPMnode head;
		private int order;

		public CPPMContext(CPPMnode _head, int _order) {
			this.head = _head;
			this.order = _order;
		};

		/* CSFS: Because bah, Java provides no possibility of
		 * default values in the constructor,
		 * necessitating lots of constructors.
		 */

		public CPPMContext(int _order) {
			this.head = null;
			this.order = _order;
		};

		public CPPMContext(CPPMnode _head)  {
			this.head = _head;
			this.order = 0;
		};

		public CPPMContext() {
			this.head = null;
			this.order = 0;
		};

		public CPPMContext(CPPMContext input) {
			this.head = input.head;
			this.order = input.order;
		}
	}

	/**
	 * Node in PPM's prediction trie.
	 * 
	 * @see CPPMLanguageModel
	 */
	class CPPMnode {
		public CPPMnode child;
		public CPPMnode next;
		public CPPMnode vine;
		public short count;
		public int symbol;

		/* CSFS: Found that the C++ code used a short
		 * to represent a symbol in certain places and an
		 * int in others. As such, I've changed it to int
		 * everywhere. This ought to cause no trouble
		 * except in the case that behaviour on overflow
		 * is relied upon.
		 */

		public CPPMnode(int sym) {
			child = null;
			next = null;
			vine = null;
			count = 1;
			symbol = sym;
		}

		public CPPMnode() {
			child = null;
			next = null;
			vine = null;
			count = 1;
		}

		public CPPMnode find_symbol(int sym) // see if symbol is a child of node
		{
			CPPMnode found = child;

			/* CSFS: I *think* this is supposed to be a pointer-copy but
			 * I'm not perfectly sure. If the find_symbol method fails,
			 * this may need to become a .clone()
			 */

			while(found != null) {
				if(found.symbol == sym) {
					return found;
				}
				found = found.next;
			}
			return null;
		}
	}

	/**
	 * Small struct for use by CPPMLanguageModel's
	 * AddSymbol method.
	 * 
	 * @see CPPMLanguageModel
	 */
	class CAddSymReturnValue {
		public CPPMnode node;
		public int update;
	}

	public CPPMLanguageModel(CEventHandler EventHandler, CSettingsStore SettingsStore, CSymbolAlphabet SymbolAlphabet) {

		super(EventHandler, SettingsStore, SymbolAlphabet); // Constructor of CLanguageModel
		m_iMaxOrder = 4;
		NodesAllocated = 0;
		// m_NodeAlloc = new CSimplePooledAlloc<CPPMnode>(8192);
		// m_ContextAlloc = new CPooledAlloc<CPPMContext>(1024);
		m_Root = new CPPMnode(); // m_NodeAlloc.Alloc();
		m_Root.symbol = -1;

		m_RootContext = new CPPMContext();// m_ContextAlloc.Alloc();
		m_RootContext.head = m_Root;
		m_RootContext.order = 0;

		// Cache the result of update exclusion - otherwise we have to look up a lot when training, which is slow

		// FIXME - this should be a boolean parameter

		bUpdateExclusion = ( GetLongParameter(Elp_parameters.LP_LM_UPDATE_EXCLUSION) !=0 );

		lpAlpha = GetLongParameter(Elp_parameters.LP_LM_ALPHA);
		lpBeta = GetLongParameter(Elp_parameters.LP_LM_BETA);

	}

	public void HandleEvent(CEvent Event) {
		super.HandleEvent(Event);

		if(Event.m_iEventType == 1) { // Parameter change notification
			CParameterNotificationEvent evt = (CParameterNotificationEvent)Event;
			if(evt.m_iParameter == Elp_parameters.LP_LM_ALPHA) {
				lpAlpha = GetLongParameter(Elp_parameters.LP_LM_ALPHA);
			}
			else if(evt.m_iParameter == Elp_parameters.LP_LM_BETA) {
				lpBeta = GetLongParameter(Elp_parameters.LP_LM_BETA);
			}
		}
	}

	public int GetMemory() {
		return NodesAllocated;
	}

	public long[] GetProbs(CContextBase context, long norm) {

		/* CSFS: In the original C++ the norm value was an
		 * unsigned int. Since Java will only provide a signed
		 * int with half the capacity, I've converted it to a long.
		 */

		/* CSFS: All exclusion-related code commented out as it was wasting
		 * time, and was disabled by boolean doExclusion = false; anyway.
		 */

		// System.out.printf("Sending probs for context %s%n", context);

		final CPPMContext ppmcontext = (CPPMContext)(context);

		int iNumSymbols = GetSize();

		long[] probs = new long[iNumSymbols + 1];
		//boolean[] exclusions = new boolean[iNumSymbols];

		// probs.setSize(iNumSymbols);

		//ArrayList<Boolean> exclusions = new ArrayList<Boolean>(iNumSymbols);
		//exclusions.setSize(iNumSymbols);

		int i;
		//for(i = 0; i < iNumSymbols; i++) {
		//  probs.setElementAt(0L, i);
		//  exclusions.setElementAt(false, i);
		//} 

		//  bool doExclusion = GetLongParameter( LP_LM_ALPHA );
//		boolean doExclusion = false; //FIXME

		long iToSpend = norm;

		CPPMnode pTemp = ppmcontext.head;

		while(pTemp != null) {
			int iTotal = 0;

			/* CSFS: Various changes in what follows to convert
			 * from libc-type ArrayLists to Java ArrayLists. Also a variety
			 * of small alterations like changing if(pSymbol) to
			 * if(pSymbol != null) since Java doesn't know about
			 * null pointers.
			 */

			CPPMnode pSymbol = pTemp.child;
			while(pSymbol != null) {
				// if(!(exclusions.elementAt(sym) && doExclusion))
				iTotal += pSymbol.count;
				pSymbol = pSymbol.next;
			}

			if(iTotal != 0) {
				long size_of_slice = iToSpend;
				/* Changed type to long so that we don't run into trouble with overflows. */
				pSymbol = pTemp.child;
				while(pSymbol != null) {
					//if(!(exclusions[pSymbol.symbol] && doExclusion)) {
					//  exclusions[pSymbol.symbol] = true;

					long p = (size_of_slice) * (100 * pSymbol.count - lpBeta) / (100 * iTotal + lpAlpha);

					probs[pSymbol.symbol] += p;
					iToSpend -= p;
					//}
					pSymbol = pSymbol.next;
				}
			}
			pTemp = pTemp.vine;
		}

		long size_of_slice = iToSpend;
		int symbolsleft = 0;

		for(i = 1; i < iNumSymbols; i++) symbolsleft++;
		//if(!(exclusions[i] && doExclusion))


		for(i = 1; i < iNumSymbols; i++) {
			//if(!(exclusions[i] && doExclusion)) {
			long p = size_of_slice / symbolsleft;
			probs[i] += p;
			iToSpend -= p;
			//}
		}

		int iLeft = iNumSymbols-1;

		for(int j = 1; j < iNumSymbols; ++j) {
			long p = iToSpend / iLeft;
			probs[j] += p;
			--iLeft;
			iToSpend -= p;
		}

		assert(iToSpend == 0);

		return probs;
	}

	private void AddSymbol(CPPMContext context, int sym)
	// add symbol to the context
	// creates new nodes, updates counts
	// and leaves 'context' at the new context
	{
		// Ignore attempts to add the root symbol


		if(sym==0) return;

		assert(sym >= 0 && sym < GetSize());

		CPPMnode vineptr, temp;
		int updatecnt = 1;

		temp = context.head.vine;

		/* CSFS: AddSymbolToNode calls want to update two values, one of which
		 * is a primitive and passed by value in Java. Therefore I have modified it
		 * to return a small wrapper class, doubtless at the expense of some
		 * performance.
		 */

		CAddSymReturnValue temp2 = AddSymbolToNode(context.head, sym, updatecnt);
		context.head = temp2.node;

		updatecnt = temp2.update;

		vineptr = context.head;
		context.order++;
		while(temp != null) {
			temp2 = AddSymbolToNode(temp, sym, updatecnt);
			vineptr.vine = temp2.node;
			updatecnt = temp2.update;
			vineptr = vineptr.vine;
			temp = temp.vine;
		}
		vineptr.vine = m_Root;

		m_iMaxOrder = (int)GetLongParameter( Elp_parameters.LP_LM_MAX_ORDER );

		while(context.order > m_iMaxOrder) {
			context.head = context.head.vine;
			context.order--;
		}
	}

	public void EnterSymbol(CContextBase c, int Symbol) {
		if(Symbol==0) return;

		assert(Symbol >= 0 && Symbol < GetSize());

		CPPMContext context = (CPPMContext) (c);

		CPPMnode find;

		while(context.head != null) {

			if(context.order < m_iMaxOrder) {   // Only try to extend the context if it's not going to make it too long
				find = context.head.find_symbol(Symbol);
				if(find != null) {
					context.order++;
					context.head = find;
					return;
				}
			}

			// If we can't extend the current context, follow vine pointer to shorten it and try again

			context.order--;
			context.head = context.head.vine;
		}

		if(context.head == null) {
			context.head = m_Root;
			context.order = 0;
		}

	}	

	public void LearnSymbol(CContextBase c, int Symbol) {
		if(Symbol==0)
			return;

		assert(Symbol >= 0 && Symbol < GetSize());
		CPPMContext context = (CPPMContext) (c);
		AddSymbol(context, Symbol);

		// System.out.printf("Learn symbol %d with context %s%n", Symbol, c);
	}

	/**
	 * Diagnostic method; prints a given symbol.
	 * 
	 * @param sym Symbol to print
	 */
	public void dumpSymbol(int sym) {

		/* CSFS: This method appears never to be referenced.
		 * It exists only here and in one Japanese class.
		 */

		if((sym <= 32) || (sym >= 127))
			System.out.printf("<%d>", sym);
		else
			System.out.printf("%c", sym);
	}

	/**
	 * Diagnostic method; prints a given String starting
	 * at pos and with length len.
	 * 
	 * @param str String to print
	 * @param pos Position to start printing
	 * @param len Number of characters to print
	 */
	public void dumpString(String str, int pos, int len)
	// Dump the string STR starting at position POS
	{
		char cc;
		int p;
		for(p = pos; p < pos + len; p++) {
			cc = str.charAt(p);
			if((cc <= 31) || (cc >= 127))
				System.out.printf("<%d>", cc);
			else
				System.out.printf("%c", cc);
		}
	}

	private CAddSymReturnValue AddSymbolToNode(CPPMnode Node, int sym, int updatein) {
		CPPMnode Return = Node.find_symbol(sym);

		int updateout = updatein;

		if(Return != null) {

			if(updatein != 0 || !bUpdateExclusion) {  // perform update exclusions

				/* CSFS: BUGFIX: This used to read 'bUpdateExclusion' without the !
				 * This led to the language model generating probabilities which were
				 * just ever so slightly off. FIXED.
				 */

				Return.count++;
				updateout = 0;
			}
			CAddSymReturnValue retval = new CAddSymReturnValue();
			retval.node = Return;
			retval.update = updateout;
			return retval;
		}

		Return = new CPPMnode(); //m_NodeAlloc.Alloc();        // count is initialized to 1
		Return.symbol = sym;
		Return.next = Node.child;
		Node.child = Return;

		++NodesAllocated;

		CAddSymReturnValue retval = new CAddSymReturnValue();
		retval.node = Return;
		retval.update = updateout;
		return retval;

	}

	/* Excluded methods: it appears the DumpTrie methods are still
	 * being written as they are festooned with TODO tags. It looks
	 * as if nobody ever uses them, so I've excluded them for now.
	 */

	/*boolean WriteToFile(String strFilename) {


		std::map<CPPMnode *, int> mapIdx;
		int iNextIdx(1); // Index of 0 means NULL;

		std::ofstream oOutputFile(strFilename.c_str());

		RecursiveWrite(m_pRoot, &mapIdx, &iNextIdx, &oOutputFile);

		oOutputFile.close();

		return false; 

		}*/

	/* CSFS: This and following methods commented out where it appears
	 * they do not currently get used. Would be good to get Java IO
	 * going in the future, but for now it's pointless until the
	 * authors of the original C++ version actually use the functions.
	 */

	/* TODO: Rewrite this I/O framework and find out how
	 * it ought to be plumbed into Dasher at large.
	 */

	/* boolean RecursiveWrite(CPPMnode *pNode, std::map<CPPMnode *, int> *pmapIdx, int *pNextIdx, std::ofstream *pOutputFile) {

		  // Dump node here

		  BinaryRecord sBR;

		  sBR.m_iIndex = GetIndex(pNode, pmapIdx, pNextIdx); 
		  sBR.m_iChild = GetIndex(pNode->child, pmapIdx, pNextIdx); 
		  sBR.m_iNext = GetIndex(pNode->next, pmapIdx, pNextIdx); 
		  sBR.m_iVine = GetIndex(pNode->vine, pmapIdx, pNextIdx);
		  sBR.m_iCount = pNode->count;
		  sBR.m_iSymbol = pNode->symbol;

		  pOutputFile->write(reinterpret_cast<char*>(&sBR), sizeof(BinaryRecord));

		  CPPMnode *pCurrentChild(pNode->child);

		  while(pCurrentChild != NULL) {
		    RecursiveWrite(pCurrentChild, pmapIdx, pNextIdx, pOutputFile);
		    pCurrentChild = pCurrentChild->next;
		  }

		  return true;
		}; */

	/* int CPPMLanguageModel::GetIndex(CPPMnode *pAddr, std::map<CPPMnode *, int> *pmapIdx, int *pNextIdx) {

		  int iIndex;
		  if(pAddr == NULL)
		    iIndex = 0;
		  else {
		    std::map<CPPMnode *, int>::iterator it(pmapIdx->find(pAddr));

		    if(it == pmapIdx->end()) {
		      iIndex = *pNextIdx;
		      pmapIdx->insert(std::pair<CPPMnode *, int>(pAddr, iIndex));
		      ++(*pNextIdx);
		    }
		    else {
		      iIndex = it->second;
		    }
		  }
		  return iIndex;
		}; */

	/* boolean ReadFromFile(std::string strFilename) {

		  std::ifstream oInputFile(strFilename.c_str());
		  std::map<int, CPPMnode*> oMap;
		  BinaryRecord sBR;
		  bool bStarted(false);

		  while(!oInputFile.eof()) {
		    oInputFile.read(reinterpret_cast<char *>(&sBR), sizeof(BinaryRecord));

		    CPPMnode *pCurrent(GetAddress(sBR.m_iIndex, &oMap));

		    pCurrent->child = GetAddress(sBR.m_iChild, &oMap);
		    pCurrent->next = GetAddress(sBR.m_iNext, &oMap);
		    pCurrent->vine = GetAddress(sBR.m_iVine, &oMap);
		    pCurrent->count = sBR.m_iCount;
		    pCurrent->symbol = sBR.m_iSymbol;

		    if(!bStarted) {
		      m_pRoot = pCurrent;
		      bStarted = true;
		    }
		  }

		  oInputFile.close();

		  return false;
		} */

	/* CPPMLanguageModel::CPPMnode *CPPMLanguageModel::GetAddress(int iIndex, std::map<int, CPPMnode*> *pMap) {
		  std::map<int, CPPMnode*>::iterator it(pMap->find(iIndex));

		  if(it == pMap->end()) {
		    CPPMnode *pNewNode;
		    pNewNode = m_NodeAlloc.Alloc();
		    pMap->insert(std::pair<int, CPPMnode*>(iIndex, pNewNode));
		    return pNewNode;
		  }
		  else {
		    return it->second;
		  }
		} */

	/* CPPMLanguageModel and others: These were using a 
	 * horrible hack wherein an integer (really a CPPMContext * ) 
	 * was being used to represent the context of a given node when 
	 * outside the generating class, in order that it could be
	 * swapped out for some other CLanguageModel derivation and retain 
	 * type-compatibility at the expense of being entirely type-unsafe. 
	 * Since Java doesn't like to be type unsafe, I've replaced this by 
	 * all Context-representing classes being a child of CContextBase, which 
	 * has no members or methods, thus retaining type-safety. */

	public CContextBase CreateEmptyContext() {
		CPPMContext Cont = new CPPMContext(); // m_ContextAlloc.Alloc();
		Cont.head = m_RootContext.head;
		Cont.order = m_RootContext.order;

		// System.out.printf("Creating new context: %s%n", Cont);

		return Cont;


	}

	public CContextBase CloneContext(CContextBase Input) {
		CPPMContext Cont = new CPPMContext(); // m_ContextAlloc.Alloc();
		CPPMContext Copy = (CPPMContext) Input;
		Cont.head = Copy.head;
		Cont.order = Copy.order;

		// System.out.printf("Cloning context %s to %s%n", Input, Cont);

		return Cont;
	}

	public void ReleaseContext(CContextBase release) {

		// System.out.printf("Releasing context %s%n", release);

		release = null; //m_ContextAlloc.Free((CPPMContext) release);


	}

}
