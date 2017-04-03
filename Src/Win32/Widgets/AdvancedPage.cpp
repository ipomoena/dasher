// AdvancedPage.cpp
//
/////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2002 Iain Murray, Inference Group, Cavendish, Cambridge.
//
/////////////////////////////////////////////////////////////////////////////

#include "WinCommon.h"

#include "AdvancedPage.h"
#include "../resource.h"
#include "../Common/StringUtils.h"
#include "../../DasherCore/DasherTypes.h"

using namespace Dasher;
using namespace std;

// Track memory leaks on Windows to the line that new'd the memory
#ifdef _WIN32
#ifdef _DEBUG
#define DEBUG_NEW new( _NORMAL_BLOCK, THIS_FILE, __LINE__ )
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif
#endif

CAdvancedPage::CAdvancedPage(HWND Parent, CAppSettings *pAppSettings)
:CPrefsPageBase(Parent, pAppSettings) {
  pAppSettings->GetPermittedValues(SP_CONTROL_BOX_ID, m_ControlBoxItems);
}

struct menuentry {
  int paramNum; // enum value in Parameters.h for setting store
  int idcNum;   // #define value in resource.h for dasher.rc
};

// List of menu items that will be displayed in the General Preferences
static menuentry menutable[] = {
  {APP_BP_SHOW_TOOLBAR, IDC_CHECK1},
  { APP_BP_SHOW_STATUSBAR, IDC_CHECK2 },
  { APP_BP_TIME_STAMP, IDC_TIMESTAMP },
  { APP_BP_CONFIRM_UNSAVED, IDC_CONFIRM_UNSAVED },
  {BP_CONTROL_MODE, IDC_CONTROLMODE},  // Not global setting - specific to editbox/widget
  {BP_SPEAK_WORDS, IDC_CHECK4},
  {BP_SPEAK_ALL_ON_STOP, IDC_CHECK3},
  { BP_COPY_ALL_ON_STOP, IDC_COPYONSTOP },
  { APP_BP_MIRROR_LAYOUT, IDC_MIRROR_LAYOUT},
};

void CAdvancedPage::PopulateList() {
  // Populate the controls in the dialogue box based on the relevent parameters
  for(int ii = 0; ii<sizeof(menutable)/sizeof(menuentry); ii++)
  {
    if(m_pAppSettings->GetBoolParameter(menutable[ii].paramNum)) {
      SendMessage(GetDlgItem(m_hwnd, menutable[ii].idcNum), BM_SETCHECK, BST_CHECKED, 0);
    }
    else  {
      SendMessage(GetDlgItem(m_hwnd, menutable[ii].idcNum), BM_SETCHECK, BST_UNCHECKED, 0);
    }
  }

  
  switch(m_pAppSettings->GetLongParameter(APP_LP_STYLE)) {
    case APP_STYLE_TRAD:
      SendMessage(GetDlgItem(m_hwnd, IDC_STYLE_STANDALONE), BM_SETCHECK, BST_CHECKED, 0);
      break;
    case APP_STYLE_COMPOSE:
      SendMessage(GetDlgItem(m_hwnd, IDC_STYLE_COMPOSITION), BM_SETCHECK, BST_CHECKED, 0);
      break;
    case APP_STYLE_DIRECT:
      SendMessage(GetDlgItem(m_hwnd, IDC_STYLE_DIRECT), BM_SETCHECK, BST_CHECKED, 0);
      break;
    case APP_STYLE_FULLSCREEN:
      SendMessage(GetDlgItem(m_hwnd, IDC_STYLE_FULL), BM_SETCHECK, BST_CHECKED, 0);
      break;
  }

  HWND ListBox = GetDlgItem(m_hwnd, IDC_CONTROLBOXES);
  auto CurrentControlBox = m_pAppSettings->GetStringParameter(SP_CONTROL_BOX_ID);
  // Add each string to list box and index each one
  bool SelectionSet = false;
  for (auto i = 0; i < m_ControlBoxItems.size(); i++) {
    Tstring Item;
    WinUTF8::UTF8string_to_wstring(m_ControlBoxItems[i], Item);
    if (Item.empty())
      Item = L"<default>";
    LRESULT Index = SendMessage(ListBox, LB_ADDSTRING, 0, (LPARAM)Item.c_str());
    SendMessage(ListBox, LB_SETITEMDATA, Index, (LPARAM)i);
    if (m_ControlBoxItems[i] == CurrentControlBox) {
      SendMessage(ListBox, LB_SETCURSEL, Index, 0);
      SelectionSet = true;
    }
  }
  if (SelectionSet == false) {
    SendMessage(ListBox, LB_SETCURSEL, 0, 0);
    LRESULT CurrentIndex = SendMessage(ListBox, LB_GETITEMDATA, 0, 0);
  }

 CWindow fileEncodingCb = GetDlgItem(m_hwnd, IDC_FILE_ENCODING);
 fileEncodingCb.SendMessage(CB_ADDSTRING, 0, (LPARAM)L"Windows Locale Encoding");
 fileEncodingCb.SendMessage(CB_ADDSTRING, 0, (LPARAM)L"UTF-8");
 fileEncodingCb.SendMessage(CB_ADDSTRING, 0, (LPARAM)L"Unicode little endian / UTF-16 LE");
 fileEncodingCb.SendMessage(CB_ADDSTRING, 0, (LPARAM)L"Unicode big endian / UTF-16 BE");

  switch (m_pAppSettings->GetLongParameter(APP_LP_FILE_ENCODING)) {
  case Opts::UTF8: 
    fileEncodingCb.SendMessage(CB_SETCURSEL, 1, 0);
    break;
  case Opts::UTF16LE: 
    fileEncodingCb.SendMessage(CB_SETCURSEL, 2, 0);
    break;
  case Opts::UTF16BE: 
    fileEncodingCb.SendMessage(CB_SETCURSEL, 3, 0);
    break;
  default:
    fileEncodingCb.SendMessage(CB_SETCURSEL, 0, 0);
    break;
  }
}

bool CAdvancedPage::Apply() {
  if(SendMessage(GetDlgItem(m_hwnd, IDC_STYLE_STANDALONE), BM_GETCHECK, 0, 0))
    m_pAppSettings->SetLongParameter(APP_LP_STYLE, 0);
  else if(SendMessage(GetDlgItem(m_hwnd, IDC_STYLE_COMPOSITION), BM_GETCHECK, 0, 0))
    m_pAppSettings->SetLongParameter(APP_LP_STYLE, 1);
  else if(SendMessage(GetDlgItem(m_hwnd, IDC_STYLE_DIRECT), BM_GETCHECK, 0, 0))
    m_pAppSettings->SetLongParameter(APP_LP_STYLE, 2);
  else if(SendMessage(GetDlgItem(m_hwnd, IDC_STYLE_FULL), BM_GETCHECK, 0, 0))
    m_pAppSettings->SetLongParameter(APP_LP_STYLE, 3);

  HWND ListBox = GetDlgItem(m_hwnd, IDC_CONTROLBOXES);
  LRESULT CurrentItem = SendMessage(ListBox, LB_GETCURSEL, 0, 0);
  LRESULT CurrentIndex = SendMessage(ListBox, LB_GETITEMDATA, CurrentItem, 0);
  if (CurrentIndex != LB_ERR)
  {
	  auto CurrentControlBox = m_ControlBoxItems[CurrentIndex];
	  m_pAppSettings->SetStringParameter(SP_CONTROL_BOX_ID, CurrentControlBox);
  }

  int fileEncodingIdx = SendMessage(GetDlgItem(m_hwnd, IDC_FILE_ENCODING), CB_GETCURSEL, 0, 0);
  switch (fileEncodingIdx) {
  case 1:
    m_pAppSettings->SetLongParameter(APP_LP_FILE_ENCODING, Opts::UTF8);
    break;
  case 2:
    m_pAppSettings->SetLongParameter(APP_LP_FILE_ENCODING, Opts::UTF16LE);
    break;
  case 3:
    m_pAppSettings->SetLongParameter(APP_LP_FILE_ENCODING, Opts::UTF16BE);
    break;
  default:
    m_pAppSettings->SetLongParameter(APP_LP_FILE_ENCODING, Opts::UserDefault);
    break;
  }

  for(int ii = 0; ii<sizeof(menutable)/sizeof(menuentry); ii++) {
    m_pAppSettings->SetBoolParameter(menutable[ii].paramNum, SendMessage(GetDlgItem(m_hwnd, menutable[ii].idcNum), BM_GETCHECK, 0, 0) == BST_CHECKED );
  }

  return TRUE;
}

LRESULT CAdvancedPage::WndProc(HWND Window, UINT message, WPARAM wParam, LPARAM lParam) {

  // most things we pass on to CPrefsPageBase, but we need to handle slider motion
  switch (message) {

  case WM_COMMAND:
    if (HIWORD(wParam) == BN_CLICKED || HIWORD(wParam) == LBN_SELCHANGE) {
      if (LOWORD(wParam) != 0 && m_hPropertySheet != 0 && m_hwnd != 0) {
        PropSheet_Changed(m_hPropertySheet, m_hwnd); // enables the 'Apply' button
                                                     // Behaviour isn't *perfect* since it activates the Apply button even if you, say,
                                                     // click 'new' alphabet then click Cancel when asked for a name.
      }
    }
    switch (LOWORD(wParam)) {

    case IDC_EFONT_BUTTON:
    // TODO: Put this in a function
    {
      CHOOSEFONT Data;
      LOGFONT lf;
      HFONT Font = (HFONT)GetStockObject(DEFAULT_GUI_FONT);
      GetObject(Font, sizeof(LOGFONT), &lf);
      Tstring tstrFaceName;
      WinUTF8::UTF8string_to_wstring(m_pAppSettings->GetStringParameter(APP_SP_EDIT_FONT), tstrFaceName);
      _tcscpy(lf.lfFaceName, tstrFaceName.c_str());
      lf.lfHeight = m_pAppSettings->GetLongParameter(APP_LP_EDIT_FONT_SIZE);
      Data.Flags = CF_INITTOLOGFONTSTRUCT | CF_SCREENFONTS;
      Data.lStructSize = sizeof(CHOOSEFONT);
      Data.hwndOwner = NULL;
      Data.lpLogFont = &lf;
      if (ChooseFont(&Data)) {
        string FontName;
        WinUTF8::wstring_to_UTF8string(lf.lfFaceName, FontName);
        m_pAppSettings->SetStringParameter(APP_SP_EDIT_FONT, FontName);
        m_pAppSettings->SetLongParameter(APP_LP_EDIT_FONT_SIZE, lf.lfHeight);
      }
    }
    break;
    case IDC_CONTROLMODE:
      EnableWindow(GetDlgItem(m_hwnd, IDC_CONTROLBOXES), SendMessage(GetDlgItem(m_hwnd, IDC_CONTROLMODE), BM_GETCHECK, 0, 0) == BST_CHECKED);
      break;
    }
  }

  return CPrefsPageBase::WndProc(Window, message, wParam, lParam);
}
