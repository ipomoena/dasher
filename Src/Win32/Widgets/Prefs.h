// Prefs.h
//
/////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2002 Iain Murray, Inference Group, Cavendish, Cambridge.
//
/////////////////////////////////////////////////////////////////////////////

#ifndef __PrefsBox_h__
#define __PrefsBox_h__

#include "../resource.h"
#include "../../DasherCore/DasherTypes.h"


#include "AlphabetBox.h"
#include "ControlPage.h"
#include "ViewPage.h"
#include "AdvancedPage.h"
#include "../AppSettings.h"

namespace Dasher {
	class CDasher;
}

class CPrefs:public CWinWrap {
public:
	CPrefs(HWND hParent, Dasher::CDasher * pDasher, CAppSettings *pAppSettings);
  ~CPrefs();
  
protected:
  LRESULT WndProc(HWND Window, UINT message, WPARAM wParam, LPARAM lParam);

private:
  CAlphabetBox *m_pAlphabetBox;
  CControlPage *m_pControlPage;
  CViewPage *m_pViewPage;
  CAdvancedPage *m_pAdvancedPage;
};

#endif  /* #ifndef __PrefsBox_h__ */
