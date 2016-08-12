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
  //m_popup = CWindowImpl<CPopup>::Create(hParent, r, NULL, WS_OVERLAPPEDWINDOW | WS_VISIBLE);
  m_popup = CWindowImpl<CPopup>::Create(hParent, r, NULL, WS_OVERLAPPEDWINDOW);
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
	wstring String;
	WinUTF8::UTF8string_to_wstring(sText, String);
	OutputDebugStringW(String.c_str());
}
void CPopup::output(const std::string &sText) {
  wstring String;
  WinUTF8::UTF8string_to_wstring(sText, String);

  InsertText(String);

  if(m_pAppSettings->GetLongParameter(APP_LP_STYLE) == APP_STYLE_DIRECT) {
    const char *DisplayText = sText.c_str();
    if(DisplayText[0] == 0xd && DisplayText[1] == 0xa) {
      // Newline, so we want to fake an enter
      fakekey[0].type = fakekey[1].type = INPUT_KEYBOARD;
      fakekey[0].ki.wVk = fakekey[1].ki.wVk = VK_RETURN;
      fakekey[0].ki.time = fakekey[1].ki.time = 0;
      fakekey[1].ki.dwFlags = KEYEVENTF_KEYUP;
      SendInput(2, fakekey, sizeof(INPUT));
    }
    else {    
      for(std::wstring::iterator it(String.begin()); it != String.end(); ++it) {
        fakekey[0].type = INPUT_KEYBOARD;
        fakekey[0].ki.dwFlags = KEYEVENTF_UNICODE;
        fakekey[0].ki.wVk = 0;
        fakekey[0].ki.time = NULL;
        fakekey[0].ki.wScan = *it;
        SendInput(1, fakekey, sizeof(INPUT));
      }
    }
  }
  m_Output += sText;
}

int _findAfterChars2(const wchar_t* str, const wchar_t* chrs, int startPos) {
  const wchar_t* ptr = str + startPos;
  ptr += wcscspn(ptr, chrs);
  ptr += wcsspn(ptr, chrs);
  return ptr - str;
}

int _findBeforeChars2(const wchar_t* str, const wchar_t* chrs, int startPos) {
  const wchar_t* ptr = str + startPos;

  // over separators
  while (ptr > str && wcschr(chrs, *ptr)) {
    --ptr;
  }
  // over non separators
  while (ptr > str && !wcschr(chrs, *ptr)) {
    --ptr;
  }
  if (wcschr(chrs, *ptr))
    ++ptr;

  return max(0, ptr - str);
}

// amajorek: Add list of word and sentence separators to alphabet definitions. 
// And use that list instead of hardcoded. 
// Fix exery place where boundaries are needed.
const wchar_t* _wordSeparators2 = L" \t\v\f\r\n";
const wchar_t* _sentenceSeparators2 = L".?!\r\n";
const wchar_t* _paragraphSeparators2 = L"\r\n";


void CPopup::GetRange(bool bForwards, CControlManager::EditDistance iDist, int* pStart, int* pEnd) {
  int& iStart = *pStart;
  int& iEnd = *pEnd;

  switch (iDist) {
  case CControlManager::EDIT_CHAR:
    if (bForwards)
      iEnd = min(iEnd + 1, (int)SendMessage(WM_GETTEXTLENGTH, 0, 0));
    else
      iStart = max(iStart - 1, 0);
    break;

  case CControlManager::EDIT_LINE: {
    if (bForwards) {
      // Make it behave like the 'End' key, unless we're at the end of the current line.
      // Then go down a line.
      int iEndLine = SendMessage(EM_LINEFROMCHAR, iEnd, 0);
      int iNewEnd = SendMessage(EM_LINEINDEX, iEndLine + 1, 0) - 1; // end of this line
      // if we were already at the end so go down a line
      if (iNewEnd <= iEnd)
        iNewEnd = SendMessage(EM_LINEINDEX, iEndLine + 2, 0) - 1;
      // on last line go to the end of text 
      if (iNewEnd <= iEnd)
        iNewEnd = SendMessage(WM_GETTEXTLENGTH, 0, 0);
      iEnd = max(0, iNewEnd);
    }
    else {
      int iStartLine = SendMessage(EM_LINEFROMCHAR, iStart, 0);
      int iNewStart = SendMessage(EM_LINEINDEX, iStartLine, 0); // start of this line
      // if we were already at the start so go up a line
      iStart = (iNewStart == iStart && iStartLine>0) ? SendMessage(EM_LINEINDEX, iStartLine - 1, 0) : iNewStart;
    }
    break;
  }

  case CControlManager::EDIT_FILE:
    if (bForwards)
      iEnd = SendMessage(WM_GETTEXTLENGTH, 0, 0);
    else
      iStart = 0;
    break;

  case CControlManager::EDIT_WORD:
  case CControlManager::EDIT_SENTENCE:
  case CControlManager::EDIT_PARAGRAPH:
  {
    const wchar_t* separators = L"";
    if (iDist == CControlManager::EDIT_WORD)
      separators = _wordSeparators2;
    else if (iDist == CControlManager::EDIT_SENTENCE)
      separators = _sentenceSeparators2;
    else if (iDist == CControlManager::EDIT_PARAGRAPH)
      separators = _paragraphSeparators2;

    CString wideText;
    GetWindowText(wideText);
    if (bForwards)
      iEnd = _findAfterChars2(wideText, separators, iEnd);
    else
      iStart = _findBeforeChars2(wideText, separators, max(0, iStart-1));
    break;
  }
  }
}

std::string CPopup::GetTextAroundCursor(CControlManager::EditDistance iDist) {
  int iStart = 0;
  int iEnd = 0;
  SendMessage(EM_GETSEL, (WPARAM)&iStart, (LPARAM)&iEnd);
  if (iStart == iEnd) { // Ignore distance if text is selected. 
    GetRange(true, iDist, &iStart, &iEnd);
    iStart = iEnd;
    GetRange(false, iDist, &iStart, &iEnd);
  }
  CString wideText;
  GetWindowText(wideText);

  return wstring_to_UTF8string(wideText.Mid(iStart, iEnd-iStart));
}

unsigned int CPopup::OffsetAfterMove(unsigned int offsetBefore, bool bForwards, CControlManager::EditDistance iDist) {
  int iStart, iEnd;
  iStart = iEnd = offsetBefore;
  GetRange(bForwards, iDist, &iStart, &iEnd);
  return bForwards ? iEnd : iStart;
}

int CPopup::Move(bool bForwards, CControlManager::EditDistance iDist) {
  int iStart = 0;
  int iEnd = 0;
  SendMessage(EM_GETSEL, (WPARAM)&iStart, (LPARAM)&iEnd);
  if (iStart == iEnd) // Ignore distance if text is selected. 
    GetRange(bForwards, iDist, &iStart, &iEnd);
  int pos = bForwards ? iEnd : iStart;
  SendMessage(EM_SETSEL, (WPARAM)pos, (LPARAM)pos);
  SendMessage(EM_SCROLLCARET, 0, 0); //scroll the caret into view!
  return pos;
}

int CPopup::Delete(bool bForwards, CControlManager::EditDistance iDist) {
  int iStart = 0;
  int iEnd = 0;
  SendMessage(EM_GETSEL, (WPARAM)&iStart, (LPARAM)&iEnd);
  if (iStart == iEnd) // Ignore distance if text is selected. 
    GetRange(bForwards, iDist, &iStart, &iEnd);
  SendMessage(EM_SETSEL, (WPARAM)iStart, (LPARAM)iEnd);
  SendMessage(EM_REPLACESEL, (WPARAM)true, (LPARAM)TEXT(""));
  SendMessage(EM_SCROLLCARET, 0, 0); //scroll the caret into view!
  return min(iStart, iEnd);
}

/////////////////////////////////////////////////////////////////////////////

void CPopup::SetKeyboardTarget(HWND hwnd)
{
  m_bForwardKeyboard = true;
  m_hTarget = hwnd;
}

void CPopup::InsertText(Tstring InsertText) {
  SendMessage(EM_REPLACESEL, TRUE, (LPARAM) InsertText.c_str());
}

/// Delete text from the editbox

void CPopup::deletetext(const std::string &sText) {
  // Lookup the unicode string that we need to delete - we only actually 
  // need the length of the string, but this is important eg for newline
  // characters which are actually two symbols

  wstring String;
  WinUTF8::UTF8string_to_wstring(sText, String);

  int iLength(String.size());

  // Get the start and end of the current selection, and decrement the start
  // by the number of characters to be deleted

  DWORD start, finish;
  SendMessage(EM_GETSEL, (LONG) & start, (LONG) & finish);
  start -= iLength;
  SendMessage(EM_SETSEL, (LONG) start, (LONG) finish);

  // Replace the selection with a null string

  TCHAR out[2];
  wsprintf(out, TEXT(""));
  SendMessage(EM_REPLACESEL, TRUE, (LONG) out);

  // FIXME - I *think* we still only want to send one keyboard event to delete a 
  // newline pair, but we're now assuming we'll never have two real characters for
  // a single symbol

  if(m_pAppSettings->GetLongParameter(APP_LP_STYLE) == APP_STYLE_DIRECT) {

    fakekey[0].type = fakekey[1].type = INPUT_KEYBOARD;
    fakekey[0].ki.wVk = fakekey[1].ki.wVk = VK_BACK;
    fakekey[0].ki.time = fakekey[1].ki.time = 0;
    fakekey[1].ki.dwFlags = KEYEVENTF_KEYUP;

	::SetFocus(targetwindow);
    SendInput(2, fakekey, sizeof(INPUT));
  }

  // And the output buffer (?)
  if(m_Output.length() >= iLength) {
    m_Output.resize(m_Output.length() - iLength);
  }
}

void CPopup::setupOnExtendedDisplay(){
	//MyInfoEnumProc
	//EnumDisplayMonitors(NULL, NULL, MyInfoEnumProc, 0);
}
RECT CPopup::getInitialWindow() {
	RECT rect;
	rect = {
		//-1200,100,-200,600
		0,100,1000,600
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
	//Not sure why if get bool returns true, we want to hide...
	if(m_pAppSettings->GetBoolParameter(APP_BP_POPUP_ENABLE) == true){
		setupOnExtendedDisplay();
	  ShowWindow(SW_SHOW);
	}
	else {
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
