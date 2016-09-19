// Popup.cpp
//
// Copyright (c) 2016 The Dasher Team
//
// This file is part of Dasher.
//
// Dasher is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// Dasher is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Dasher; if not, write to the Free Software 
// Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//
// NOTES: Created by Jeremy Cope to facilitate extended (multiple) displays.
//

#include "WinCommon.h"

#include "Popup.h"
#include <Windows.h>
#include "../../DasherCore/Event.h"
#include "FilenameGUI.h"
#include "../resource.h"
#include "../../DasherCore/DasherInterfaceBase.h"
#include "../Dasher.h"


using namespace Dasher;
using namespace std;
using namespace WinLocalisation;
using namespace WinUTF8;

CPopup::CPopup(CAppSettings *pAppSettings) {
  
  // TODO: Check that this is all working okay (it quite probably
  // isn't). In the long term need specialised editor classes.
  targetwindow = 0;

  m_pAppSettings = pAppSettings;

  UINT CodePage = GetUserCodePage();
  m_Font = GetCodePageFont(CodePage, 14);
}

HWND CPopup::Create(HWND hParent, bool bNewWithDate) {
  RECT r = getInitialWindow();
  m_popup = CWindowImpl<CPopup>::Create(hParent, r, NULL, WS_OVERLAPPEDWINDOW| ES_MULTILINE );
  return *this;
}

CPopup::~CPopup() {
  DeleteObject(m_Font);
}

void CPopup::Move(int x, int y, int Width, int Height) {
  MoveWindow( x, y, Width, Height, TRUE);
}

void CPopup::SetFont(string Name, long Size) {
  Tstring FontName;
  UTF8string_to_wstring(Name, FontName);

  if(Size == 0)
    Size = 14;

  DeleteObject(m_Font);
  if (Name == "") {
    UINT CodePage = GetUserCodePage();
    m_Font = GetCodePageFont(CodePage, -Size);
  }
  else
    m_Font = CreateFont(-Size, 0, 0, 0, FW_DONTCARE, FALSE, FALSE, FALSE, DEFAULT_CHARSET, OUT_DEFAULT_PRECIS, CLIP_DEFAULT_PRECIS, DEFAULT_QUALITY, FF_DONTCARE, FontName.c_str());    // DEFAULT_CHARSET => font made just from Size and FontName

  SendMessage(WM_SETFONT, (WPARAM) m_Font, true);
}

void CPopup::SetInterface(Dasher::CDasherInterfaceBase *DasherInterface) {
  m_pDasherInterface = DasherInterface;
}

void CPopup::updateDisplay(const std::string sText) {
  //If the display output has changed.. update the popup window
  if (sText.compare(m_Output) != 0) {
    m_Output = sText;
    output(m_Output);
  }
}

void CPopup::output(const std::string &sText) {
  wstring String;
  WinUTF8::UTF8string_to_wstring(sText, String);
  InsertText(String);
}

void CPopup::InsertText(Tstring InsertText) {
  //Update entire screen
  SendMessage(WM_SETTEXT, TRUE, (LPARAM)InsertText.c_str());
  //Scroll to bottom
  SendMessage(EM_LINESCROLL, 0, 5);
}

void CPopup::setupOnExtendedDisplay(){
  //MyInfoEnumProc
  //EnumDisplayMonitors(NULL, NULL, MyInfoEnumProc, 0);
}

RECT CPopup::getInitialWindow() {
	RECT rect;
	rect = {
		//-1200,100,-200,600
		1000,100,300,600
	};
	return rect;
}

void CPopup::HandleParameterChange(int iParameter) {
  switch(iParameter) {
  case APP_SP_POPUP_FONT:
  case APP_LP_POPUP_FONT_SIZE:
    SetFont(m_pAppSettings->GetStringParameter(APP_SP_POPUP_FONT), m_pAppSettings->GetLongParameter(APP_LP_POPUP_FONT_SIZE));
    break;
  case APP_BP_POPUP_ENABLE:
	 OutputDebugString(L"Request to configure popup"); //Log Updated Display to console
	if(m_pAppSettings->GetBoolParameter(APP_BP_POPUP_ENABLE) == true){
	  OutputDebugString(L"..show.\n"); //Log Updated Display to console
	  setupOnExtendedDisplay(); //Setup the placyment
	  ShowWindow(SW_SHOW);
	  CDasher* dasher = (CDasher*)m_pDasherInterface; //Cast for concrete implementation
	  dasher->configurePopupTimer(true);
	}
	else {
	  OutputDebugString(L"..hide.\n"); //Log Updated Display to console
	  ShowWindow(SW_HIDE);
	}		
    break;
  case APP_BP_POPUP_FULL_SCREEN:
	if (m_pAppSettings->GetBoolParameter(APP_BP_POPUP_FULL_SCREEN) == true) {
	  ShowWindow(SW_MAXIMIZE);
	}
    else {
	  ShowWindow(SW_SHOWDEFAULT);
	}
    break;
  case APP_BP_POPUP_INFRONT:
	break;
  default:
    break;
  }
}
