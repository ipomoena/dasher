// Prefs.cpp
//
/////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2002 Iain Murray, Inference Group, Cavendish, Cambridge.
//
/////////////////////////////////////////////////////////////////////////////

#include "WinCommon.h"
#include "Prsht.h"

#include "Prefs.h"
#include "../../DasherCore/Parameters.h"

#include "../resource.h"
#include <utility>              // for std::pair
#include "../Dasher.h"

using namespace Dasher;
using namespace std;


CPrefs::CPrefs(HWND hParent, CDasher *pDasher, CAppSettings *pAppSettings) {
  m_hwnd = 0;

  // FIXME - is hParent still needed here?
  m_pAlphabetBox = new CAlphabetBox(hParent, pAppSettings);
  m_pControlPage = new CControlPage(hParent, pDasher, pAppSettings);
  m_pViewPage = new CViewPage(hParent, pAppSettings);
  m_pAdvancedPage = new CAdvancedPage(hParent, pAppSettings);

  // Set up the property sheets which go into the preferences
  // dialogue.
  PROPSHEETPAGE psp[4];
  memset(psp, 0, sizeof(psp));
  psp[0].dwSize = sizeof(PROPSHEETPAGE);
  psp[0].dwFlags = PSP_USEICONID | PSP_USETITLE | PSP_PREMATURE;
  psp[0].hInstance = WinHelper::hInstApp;
  psp[0].pszTemplate = MAKEINTRESOURCE(IDD_ALPHABETPAGE);
  psp[0].pszIcon = NULL;
  psp[0].pfnDlgProc = (DLGPROC) WinWrapMap::PSWndProc;
  psp[0].pszTitle =  MAKEINTRESOURCE(IDS_PREFS_ALPHABET);
  psp[0].lParam = (LPARAM) m_pAlphabetBox;
  psp[0].pfnCallback = NULL;

  psp[1].dwSize = sizeof(PROPSHEETPAGE);
  psp[1].dwFlags = PSP_USEICONID | PSP_USETITLE | PSP_PREMATURE;
  psp[1].hInstance = WinHelper::hInstApp;
  psp[1].pszTemplate = MAKEINTRESOURCE(IDD_CONTROLPAGE);
  psp[1].pszIcon = NULL;
  psp[1].pfnDlgProc = (DLGPROC) WinWrapMap::PSWndProc;
  psp[1].pszTitle = MAKEINTRESOURCE(IDS_PREFS_CONTROL);
  psp[1].lParam = (LPARAM) m_pControlPage;
  psp[1].pfnCallback = NULL;
  
  psp[2].dwSize = sizeof(PROPSHEETPAGE);
  psp[2].dwFlags = PSP_USEICONID | PSP_USETITLE | PSP_PREMATURE;
  psp[2].hInstance = WinHelper::hInstApp;
  psp[2].pszTemplate = MAKEINTRESOURCE(IDD_APPEARANCEPAGE);
  psp[2].pszIcon = NULL;
  psp[2].pfnDlgProc = (DLGPROC) WinWrapMap::PSWndProc;
  psp[2].pszTitle =  MAKEINTRESOURCE(IDS_PREFS_VIEW);
  psp[2].lParam = (LPARAM) m_pViewPage;
  psp[2].pfnCallback = NULL;
  
  psp[3].dwSize = sizeof(PROPSHEETPAGE);
  psp[3].dwFlags = PSP_USEICONID | PSP_USETITLE | PSP_PREMATURE;
  psp[3].hInstance = WinHelper::hInstApp;
  psp[3].pszTemplate = MAKEINTRESOURCE(IDD_APPPAGE);
  psp[3].pszIcon = NULL;
  psp[3].pfnDlgProc = (DLGPROC) WinWrapMap::PSWndProc;
  psp[3].pszTitle =  MAKEINTRESOURCE(IDS_PREFS_LM);
  psp[3].lParam = (LPARAM) m_pAdvancedPage;
  psp[3].pfnCallback = NULL;
  
  PROPSHEETHEADER psh;
  memset(&psh, 0, sizeof(psh));
  psh.dwSize = sizeof(PROPSHEETHEADER);
  psh.dwFlags = PSH_USEICONID | PSH_PROPSHEETPAGE;
  psh.hwndParent = hParent;
  psh.hInstance = WinHelper::hInstApp;
  psh.pszIcon = NULL;
  psh.pszCaption =  MAKEINTRESOURCE(IDS_PREFS_TITLE);
  psh.nPages = sizeof(psp) / sizeof(PROPSHEETPAGE);
  psh.nStartPage = 0;
  psh.ppsp = (LPCPROPSHEETPAGE) &psp;
  psh.pfnCallback = NULL;
  PropertySheet(&psh);
}

CPrefs::~CPrefs() {
  delete m_pAlphabetBox;
  m_pAlphabetBox = NULL;

  delete m_pControlPage;
  m_pControlPage = NULL;

  delete m_pViewPage;
  m_pViewPage = NULL;

  delete m_pAdvancedPage;
  m_pAdvancedPage = NULL;

  
}

LRESULT CPrefs::WndProc(HWND Window, UINT message, WPARAM wParam, LPARAM lParam) {
  switch (message) {
  case WM_INITDIALOG:
    if(!m_hwnd) {             // If this is the initial dialog for the first time
      m_hwnd = Window;
    }
    return TRUE;

  default:
    return FALSE;
  }
}
