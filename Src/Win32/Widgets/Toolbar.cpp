// Toolbar.cpp
//
/////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2002 Iain Murray, Inference Group, Cavendish, Cambridge.
//
/////////////////////////////////////////////////////////////////////////////

// TODO: Prime candidate for ATL

#include "WinCommon.h"

#include "Toolbar.h"
#include <winres.h>
#include "../resource.h"

// Track memory leaks on Windows to the line that new'd the memory
#ifdef _WIN32
#ifdef _DEBUG_MEMLEAKS
#define DEBUG_NEW new( _NORMAL_BLOCK, THIS_FILE, __LINE__ )
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif
#endif

struct SToolbarButton {
  int iBitmap;
  int iString;
  int iStringID;
  int iCommand;
};

SToolbarButton sButtons[] = {
  {STD_FILENEW, 0, IDS_FILE_NEW, ID_FILE_NEW},
  {STD_FILEOPEN, 1, IDS_FILE_OPEN, ID_FILE_OPEN},
  {STD_FILESAVE, 2, IDS_FILE_SAVE, ID_FILE_SAVE},
  {-1, -1, 0, 0},
  {STD_CUT, 3, IDS_EDIT_CUT, ID_EDIT_CUT},
  {STD_COPY, 4, IDS_EDIT_COPY, ID_EDIT_COPY},
  {-2, 5, IDS_EDIT_COPY_ALL, ID_EDIT_COPY_ALL},
  {STD_PASTE, 6, IDS_EDIT_PASTE, ID_EDIT_PASTE},
  {-1, -1, 0, 0},
  {STD_PROPERTIES, 7, IDS_OPTIONS_PREFS, ID_OPTIONS_PREFS}
};

CToolbar::CToolbar(HWND hParent, bool bVisible) {
  m_hwnd = 0;
  m_hRebar = 0;

  m_hParent = hParent;

  if(bVisible)
    CreateToolbar();
}

void CToolbar::Resize() {
  SendMessage(m_hRebar, WM_SIZE, 0, 0);
}

void CToolbar::ShowToolbar(bool bValue) {
 if(m_hwnd!=0)
    DestroyToolbar();
 if(bValue)
    CreateToolbar();
}

void CToolbar::CreateToolbar() {
  WinHelper::InitCommonControlLib();

 

  m_hRebar = CreateWindowEx(WS_EX_TOOLWINDOW,
                            REBARCLASSNAME,
                            NULL,
                            WS_CHILD|WS_VISIBLE|WS_CLIPSIBLINGS|
                            WS_CLIPCHILDREN|RBS_VARHEIGHT|CCS_NODIVIDER,
                            0,0,0,0,
                            m_hParent, NULL, WinHelper::hInstApp,
                            NULL);

  REBARINFO rbi;
  rbi.cbSize = sizeof(REBARINFO);
  rbi.fMask  = 0;
  rbi.himl   = (HIMAGELIST)NULL;
  SendMessage(m_hRebar, RB_SETBARINFO, 0, (LPARAM)&rbi);

  REBARBANDINFO rbBand;
  rbBand.cbSize = sizeof(REBARBANDINFO); 
  rbBand.fMask  = RBBIM_STYLE | RBBIM_CHILD  | RBBIM_CHILDSIZE;
  rbBand.fStyle = RBBS_CHILDEDGE | RBBS_FIXEDBMP | RBBS_GRIPPERALWAYS | RBBS_USECHEVRON;
 
  // Create Toolbar
  m_hwnd = CreateWindow(TOOLBARCLASSNAME, NULL, WS_CHILD | WS_VISIBLE | TBSTYLE_FLAT | CCS_NODIVIDER | CCS_NORESIZE | CCS_NOPARENTALIGN, 0, 0, 0, 0, m_hRebar, NULL, WinHelper::hInstApp, NULL);

  // Allows system to work with any version of common controls library
  SendMessage(m_hwnd, TB_BUTTONSTRUCTSIZE, (WPARAM) sizeof(TBBUTTON), 0);

  // Get Standard toolbar bitmaps.
  TBADDBITMAP bitmaps;
  bitmaps.hInst = HINST_COMMCTRL;
  bitmaps.nID = IDB_STD_LARGE_COLOR;
  SendMessage(m_hwnd, TB_ADDBITMAP, 0, (LPARAM) & bitmaps);

  // Get Non-standard Copy-All bitmap
  bitmaps.hInst = WinHelper::hInstApp;
  bitmaps.nID = IDB_COPY_ALL_LARGE_COLOR;
  const int COPY_ALL_INDEX = SendMessage(m_hwnd, TB_ADDBITMAP, 0, (LPARAM) & bitmaps);

  Tstring AllButtons;
  Tstring CurButton;

  int iNumButtons(sizeof(sButtons)/sizeof(SToolbarButton));

  for(int i(0); i < iNumButtons; ++i) {
    if(sButtons[i].iBitmap != -1) {
      WinLocalisation::GetResourceString(sButtons[i].iStringID, &CurButton);
      AllButtons += CurButton + TEXT('\0');
    }
  }
    
  const TCHAR *szButtontext = AllButtons.c_str();
  SendMessage(m_hwnd, TB_ADDSTRING, 0, (DWORD) szButtontext);

  // TODO: Should do tooltips

  TBBUTTON *pButtons(new TBBUTTON[iNumButtons]);

  for(int i(0); i < iNumButtons; ++i) {
    if(sButtons[i].iBitmap == -2)
      pButtons[i].iBitmap = COPY_ALL_INDEX;
    else
      pButtons[i].iBitmap = sButtons[i].iBitmap;

    pButtons[i].idCommand = sButtons[i].iCommand;
    pButtons[i].fsState = TBSTATE_ENABLED;

    // TODO: Not sure if this is the best way to handle the separator
    if(sButtons[i].iBitmap == -1)
      pButtons[i].fsStyle = TBSTYLE_SEP;
    else
      pButtons[i].fsStyle = TBSTYLE_BUTTON;
    pButtons[i].iString = sButtons[i].iString;
  }

  SendMessage(m_hwnd, TB_ADDBUTTONS, iNumButtons, (LPARAM)pButtons);

  delete[] pButtons;

  int dwBtnSize = SendMessage(m_hwnd, TB_GETBUTTONSIZE, 0,0);

  rbBand.hwndChild  = m_hwnd;
  rbBand.cxMinChild = 0;
  rbBand.cyMinChild = HIWORD(dwBtnSize);
  rbBand.cxIdeal = 250;

  SendMessage(m_hRebar, RB_INSERTBAND, (WPARAM)-1, (LPARAM)&rbBand);
}

void CToolbar::DestroyToolbar() {
  DestroyWindow(m_hRebar);
  m_hRebar = 0;
}

int CToolbar::GetHeight() {
  RECT sRect;
  GetWindowRect(m_hRebar, &sRect);
  return sRect.bottom - sRect.top;
}
