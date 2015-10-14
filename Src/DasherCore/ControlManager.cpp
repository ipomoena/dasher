// ControlManager.cpp
//
// Copyright (c) 2007 The Dasher Team
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

#include "../Common/Common.h"

#include "ControlManager.h"
#include "DasherInterfaceBase.h"
#include <cstring>

using namespace Dasher;
using namespace std;
// Track memory leaks on Windows to the line that new'd the memory
#ifdef _WIN32
#ifdef _DEBUG_MEMLEAKS
#define DEBUG_NEW new( _NORMAL_BLOCK, THIS_FILE, __LINE__ )
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif
#endif

CControlBase::CControlBase(CSettingsUser *pCreateFrom, CDasherInterfaceBase *pInterface, CNodeCreationManager *pNCManager)
  : CSettingsUser(pCreateFrom), m_pInterface(pInterface), m_pNCManager(pNCManager), m_pScreen(NULL), m_pRoot(NULL) {
}

CControlBase::NodeTemplate *CControlBase::GetRootTemplate() {
  return m_pRoot;
}

void CControlBase::SetRootTemplate(NodeTemplate *pRoot) {
  if (m_pRoot || !pRoot) throw "SetRoot should only be called once, with a non-null root";
  m_pRoot = pRoot;
}

int CControlBase::getColour(NodeTemplate *pTemplate, CDasherNode *pParent) {
  if (pTemplate->m_iColour!=-1) return  pTemplate->m_iColour;
  if (pParent) return (pParent->ChildCount()%99)+11;
  return 11;
}

CDasherNode *CControlBase::GetRoot(CDasherNode *pContext, int iOffset) {
  if (!m_pRoot) return m_pNCManager->GetAlphabetManager()->GetRoot(pContext, false, iOffset);

  CContNode *pNewNode = new CContNode(iOffset, getColour(m_pRoot, pContext), m_pRoot, this);

  // FIXME - handle context properly

  //  pNewNode->SetContext(m_pLanguageModel->CreateEmptyContext());

  return pNewNode;
}

void CControlBase::ChangeScreen(CDasherScreen *pScreen) {
  if (m_pScreen==pScreen) return;
  m_pScreen=pScreen;
  deque<NodeTemplate *> templateQueue(1,m_pRoot);
  set<NodeTemplate *> allTemplates(templateQueue.begin(),templateQueue.end());
  while (!templateQueue.empty()) {
    NodeTemplate *head = templateQueue.front();
    templateQueue.pop_front();
    delete head->m_pLabel;
    head->m_pLabel = pScreen->MakeLabel(head->m_strLabel);
    for (vector<NodeTemplate *>::iterator it = head->successors.begin(); it!=head->successors.end(); it++) {
      if (!(*it)) continue; //an escape back to the alphabet, no label/successors here
      if (allTemplates.find(*it)==allTemplates.end()) {
        allTemplates.insert(*it);
        templateQueue.push_back(*it);
      }
    }
  }
}

CControlBase::NodeTemplate::NodeTemplate(const string &strLabel,int iColour)
: m_strLabel(strLabel), m_iColour(iColour), m_pLabel(NULL) {
}

CControlBase::NodeTemplate::~NodeTemplate() {
  delete m_pLabel;
}

CControlBase::CContNode::CContNode(int iOffset, int iColour, NodeTemplate *pTemplate, CControlBase *pMgr)
: CDasherNode(iOffset, iColour, pTemplate->m_pLabel), m_pTemplate(pTemplate), m_pMgr(pMgr) {
}

void CControlBase::CContNode::PopulateChildren() {

  CDasherNode *pNewNode;

  const unsigned int iNChildren( m_pTemplate->successors.size() );
  unsigned int iLbnd(0), iIdx(0);

  for (vector<NodeTemplate *>::iterator it = m_pTemplate->successors.begin(); it!=m_pTemplate->successors.end(); it++) {

    const unsigned int iHbnd((++iIdx*CDasherModel::NORMALIZATION)/iNChildren);

    if( *it == NULL ) {
      // Escape back to alphabet

      pNewNode = m_pMgr->m_pNCManager->GetAlphabetManager()->GetRoot(this, false, offset()+1);
    }
    else {

      pNewNode = new CContNode(offset(), m_pMgr->getColour(*it, this), *it, m_pMgr);
    }
    pNewNode->Reparent(this, iLbnd, iHbnd);
    iLbnd=iHbnd;
    DASHER_ASSERT(GetChildren().back()==pNewNode);
  }
}

int CControlBase::CContNode::ExpectedNumChildren() {
  return m_pTemplate->successors.size();
}

void CControlBase::CContNode::Output() {
  m_pTemplate->happen(this);
}

const vector<CControlBase::NodeTemplate *> &CControlParser::parsedNodes() {
  return m_vParsed;
}

///Template used for all node defns read in from XML - just
/// execute a list of Actions.
class XMLNodeTemplate : public CControlBase::NodeTemplate {
public:
  XMLNodeTemplate(const string &label, int color) : NodeTemplate(label, color) {
  }

  void happen(CControlBase::CContNode *pNode) {
    for (vector<Action*>::iterator it=actions.begin(); it!=actions.end(); it++)
      (*it)->happen(pNode);
  }

  ~XMLNodeTemplate() {
    for (vector<Action*>::iterator it=actions.begin(); it!=actions.end(); it++)
      delete *it;
  }

  vector<CControlBase::Action*> actions;
};

CControlParser::CControlParser(CMessageDisplay *pMsgs) : AbstractXMLParser(pMsgs), m_bUser(false) {
}

bool CControlParser::ParseFile(const string &strFileName, bool bUser) {
  if (m_bUser) {
    //have user files
    if (!bUser) return true; //so ignore system!
  } else {
    //have system files (or none)
    if (bUser) m_vParsed.clear(); //replace system with user
    m_bUser = true;
  }

  namedNodes.clear();
  unresolvedRefs.clear();
  nodeStack.clear();
  
  if (!AbstractXMLParser::ParseFile(strFileName, bUser)) return false;
  //resolve any forward references to nodes declared later
  for (vector<pair<CControlBase::NodeTemplate**,string> >::iterator it=unresolvedRefs.begin(); it!=unresolvedRefs.end(); it++) {
    map<string,CControlBase::NodeTemplate*>::iterator target = namedNodes.find(it->second);
    if (target != namedNodes.end())
      *(it->first) = target->second;
  }
  //somehow, need to clear out any refs that weren't resolved...???
  return true;
}

void CControlParser::XmlStartHandler(const XML_Char *name, const XML_Char **atts) {
  vector<CControlBase::NodeTemplate *> &parent(nodeStack.empty() ? m_vParsed : nodeStack.back()->successors);
  if (strcmp(name,"node")==0) {
    string label,nodeName; int color=-1;
    while (*atts) {
      if (strcmp(*atts,"name")==0) {
        nodeName=*(atts+1);
        DASHER_ASSERT(namedNodes.find(nodeName)==namedNodes.end());
      } else if (strcmp(*atts,"label")==0) {
        label = *(atts+1);
      } else if (strcmp(*atts,"color")==0) {
        color = atoi(*(atts+1));
      }
      atts+=2;
    }
    XMLNodeTemplate *n = new XMLNodeTemplate(label,color);
    parent.push_back(n);
    nodeStack.push_back(n);
    if (nodeName!="")
      namedNodes[nodeName]=n; //all refs resolved at end.
  } else if (strcmp(name,"ref")==0) {
    string target;
    while (*atts) {
      if (strcmp(*atts,"name")==0)
        target=*(atts+1);
      atts+=2;
    }
    map<string,CControlBase::NodeTemplate*>::iterator it=namedNodes.find(target);
    if (it!=namedNodes.end())
      parent.push_back(it->second);
    else {
      parent.push_back(NULL);
      unresolvedRefs.push_back(pair<CControlBase::NodeTemplate**,string>(&(parent.back()),target));
    }
  } else if (strcmp(name,"alph")==0) {
    parent.push_back(NULL);
  } else if (CControlBase::NodeTemplate *n = parseOther(name, atts)) {
    parent.push_back(n);
  } else if (CControlBase::Action *a=parseAction(name, atts)) {
    DASHER_ASSERT(!nodeStack.empty());
    static_cast<XMLNodeTemplate*>(nodeStack.back())->actions.push_back(a);
  }
}

void CControlParser::XmlEndHandler(const XML_Char *szName) {
  if (strcmp(szName,"node")==0) {
    DASHER_ASSERT(!nodeStack.empty());
    nodeStack.pop_back();
  }
}

class Dasher::SpeechHeader : public CDasherInterfaceBase::TextAction{
public:
  SpeechHeader(CDasherInterfaceBase *pIntf) : TextAction(pIntf) {}
  void operator()(const std::string &strText) {
    m_pIntf->Speak(strText, false);
  }
};

class Dasher::CopyHeader : public CDasherInterfaceBase::TextAction{
public:
  CopyHeader(CDasherInterfaceBase *pIntf) : TextAction(pIntf) {}
  void operator()(const std::string &strText) {
    m_pIntf->CopyToClipboard(strText);
  }
};

class Pause : public CControlBase::Action{
public:
  void happen(CControlBase::CContNode *pNode) {
    pNode->mgr()->GetDasherInterface()->GetActiveInputMethod()->pause();
  }
};

class SpeakCancel : public CControlBase::Action {
public:
  void happen(CControlBase::CContNode *pNode) {
    pNode->mgr()->GetDasherInterface()->Speak("", true);
  }
};

template <typename T> class MethodAction : public CControlBase::Action {
public:
  ///A "Method" is pointer to a function "void X()", that is a member of a T...
  typedef void (T::*Method)();
  MethodAction(T *pRecv, Method f) : m_pRecv(pRecv), m_f(f) {
  }
  virtual void happen(CControlBase::CContNode *pNode) {
    //invoke pointer-to-member-function m_f on object *m_pRecv!
    (m_pRecv->*m_f)();
  }
private:
  T *m_pRecv;
  Method m_f;
};

class Delete : public CControlBase::Action {
  const bool m_bForwards;
  const CControlManager::EditDistance m_dist;
public:
  Delete(bool bForwards, CControlManager::EditDistance dist) : m_bForwards(bForwards), m_dist(dist)  {
  }
  virtual void happen(CControlBase::CContNode *pNode) {
    pNode->mgr()->GetDasherInterface()->ctrlDelete(m_bForwards, m_dist);
  }
};

class Move : public CControlBase::Action {
  const bool m_bForwards;
  const CControlManager::EditDistance m_dist;
public:
  Move(bool bForwards, CControlManager::EditDistance dist) : m_bForwards(bForwards), m_dist(dist)  {
  }
  virtual void happen(CControlBase::CContNode *pNode) {
    pNode->mgr()->GetDasherInterface()->ctrlMove(m_bForwards, m_dist);
  }
};


CControlManager::CControlManager(CSettingsUser *pCreateFrom, CNodeCreationManager *pNCManager, CDasherInterfaceBase *pInterface)
: CControlParser(pInterface), CControlBase(pCreateFrom, pInterface, pNCManager), CSettingsObserver(pCreateFrom), m_pSpeech(NULL), m_pCopy(NULL) {
  //TODO, used to be able to change label+colour of root/pause/stop from controllabels.xml
  // (or, get the root node title "control" from the alphabet!)
  m_pSpeech = new SpeechHeader(pInterface);
  m_pCopy = new CopyHeader(pInterface);
  SetRootTemplate(new NodeTemplate("",8)); //default NodeTemplate does nothing
  GetRootTemplate()->successors.push_back(NULL);

  // Key in actions map is name plus arguments in alphabetical order.
  m_actions["stop"] = new MethodAction<CDasherInterfaceBase>(pInterface, &CDasherInterfaceBase::Done);
  m_actions["pause"] = new Pause();

  m_actions["speak what=cancel"] = new SpeakCancel();
  m_actions["speak what=all"] = new MethodAction<SpeechHeader>(m_pSpeech, &SpeechHeader::executeOnAll);
  m_actions["speak what=new"] = new MethodAction<SpeechHeader>(m_pSpeech, &SpeechHeader::executeOnNew);
  m_actions["speak what=repeat"] = new MethodAction<SpeechHeader>(m_pSpeech, &SpeechHeader::executeLast);

  m_actions["copy what=all"] = new MethodAction<CopyHeader>(m_pCopy, &CopyHeader::executeOnAll);
  m_actions["copy what=new"] = new MethodAction<CopyHeader>(m_pCopy, &CopyHeader::executeOnNew);
  m_actions["copy what=repeat"] = new MethodAction<CopyHeader>(m_pCopy, &CopyHeader::executeLast);

  m_actions["move dist=char forward=yes"] = new Move(true, EDIT_CHAR);
  m_actions["move dist=word forward=yes"] = new Move(true, EDIT_WORD);
  m_actions["move dist=line forward=yes"] = new Move(true, EDIT_LINE);
  m_actions["move dist=sentence forward=yes"] = new Move(true, EDIT_SENTENCE);
  m_actions["move dist=paragraph forward=yes"] = new Move(true, EDIT_PARAGRAPH);
  m_actions["move dist=file forward=yes"] = new Move(true, EDIT_FILE);

  m_actions["move dist=char forward=no"] = new Move(true, EDIT_CHAR);
  m_actions["move dist=word forward=no"] = new Move(true, EDIT_WORD);
  m_actions["move dist=line forward=no"] = new Move(true, EDIT_LINE);
  m_actions["move dist=sentence forward=no"] = new Move(true, EDIT_SENTENCE);
  m_actions["move dist=paragraph forward=no"] = new Move(true, EDIT_PARAGRAPH);
  m_actions["move dist=file forward=no"] = new Move(true, EDIT_FILE);

  m_actions["delete dist=char forward=yes"] = new Delete(true, EDIT_CHAR);
  m_actions["delete dist=word forward=yes"] = new Delete(true, EDIT_WORD);
  m_actions["delete dist=line forward=yes"] = new Delete(true, EDIT_LINE);
  m_actions["delete dist=sentence forward=yes"] = new Delete(true, EDIT_SENTENCE);
  m_actions["delete dist=paragraph forward=yes"] = new Delete(true, EDIT_PARAGRAPH);
  m_actions["delete dist=file forward=yes"] = new Delete(true, EDIT_FILE);

  m_actions["delete dist=char forward=no"] = new Delete(true, EDIT_CHAR);
  m_actions["delete dist=word forward=no"] = new Delete(true, EDIT_WORD);
  m_actions["delete dist=line forward=no"] = new Delete(true, EDIT_LINE);
  m_actions["delete dist=sentence forward=no"] = new Delete(true, EDIT_SENTENCE);
  m_actions["delete dist=paragraph forward=no"] = new Delete(true, EDIT_PARAGRAPH);
  m_actions["delete dist=file forward=no"] = new Delete(true, EDIT_FILE);

  m_pInterface->ScanFiles(this, "control.xml"); //just look for the one

  updateActions();
}


CControlBase::NodeTemplate *CControlManager::parseOther(const XML_Char *name, const XML_Char **atts) {
  if (strcmp(name,"root")==0) return GetRootTemplate();
  return CControlParser::parseOther(name, atts);
}

CControlBase::Action *CControlManager::parseAction(const XML_Char *name, const XML_Char **atts) {
  map<string, string> arguments;
  while (*atts) {
    arguments[*atts] = *(atts + 1);
    atts += 2;
  }
  stringstream key;
  // Key in actions map is name plus arguments in alphabetical order.
  key << name;
  if (!arguments.empty()) {
    for (map<string, string>::iterator it = arguments.begin(); it != arguments.end(); ++it) {
      key << " " << it->first << "=" << it->second;
    }
  }
  if (CControlBase::Action *action = m_actions[key.str()])
    return action;

  return CControlParser::parseAction(name, atts);
}

CControlManager::~CControlManager() {
}

void CControlManager::HandleEvent(int iParameter) {
  switch (iParameter) {
    case BP_COPY_ALL_ON_STOP:
    case BP_SPEAK_ALL_ON_STOP:
    case SP_INPUT_FILTER:
      updateActions();
  }
}

void CControlManager::updateActions() {
  // decide if removal of pause and stop are worth the trouble 
  // reimplement if yes 
  // imo with control.xml it isn't.
  /*
  vector<NodeTemplate *> &vRootSuccessors(GetRootTemplate()->successors);
  vector<NodeTemplate *> vOldRootSuccessors;
  vOldRootSuccessors.swap(vRootSuccessors);
  vector<NodeTemplate *>::iterator it=vOldRootSuccessors.begin();
  DASHER_ASSERT(*it == NULL); //escape back to alphabet
  vRootSuccessors.push_back(*it++);

  //stop does something, and we're told to add a node for it
  // (either a dynamic filter where the user can't use the normal stop mechanism precisely,
  //  or a static filter but a 'stop' action is easier than using speak->all / copy->all then pause)
  if (m_pInterface->hasDone() && GetBoolParameter(BP_CONTROL_MODE_HAS_HALT))
    vRootSuccessors.push_back(m_pStop);
  if (it!=vOldRootSuccessors.end() && *it == m_pStop) it++;

  //filter is pauseable, and either 'stop' would do something (so pause is different),
  // or we're told to have a stop node but it would be indistinguishable from pause (=>have pause)
  CInputFilter *pInput(m_pInterface->GetActiveInputMethod());
  if (pInput && pInput->supportsPause() && (m_pInterface->hasDone() || GetBoolParameter(BP_CONTROL_MODE_HAS_HALT)))
    vRootSuccessors.push_back(m_pPause);
  if (it!=vOldRootSuccessors.end() && *it == m_pPause) it++;

  if (GetBoolParameter(BP_CONTROL_MODE_HAS_SPEECH) && m_pInterface->SupportsSpeech()) {
    if (!m_pSpeech) m_pSpeech = new SpeechHeader(m_pInterface, GetRootTemplate());
    vRootSuccessors.push_back(m_pSpeech);
  }
  if (it!=vOldRootSuccessors.end() && *it == m_pSpeech) it++;

  if (GetBoolParameter(BP_CONTROL_MODE_HAS_COPY) && m_pInterface->SupportsClipboard()) {
    if (!m_pCopy) m_pCopy = new CopyHeader(m_pInterface, GetRootTemplate());
    vRootSuccessors.push_back(m_pCopy);
  }
  if (it!=vOldRootSuccessors.end() && *it == m_pCopy) it++;

  if (GetBoolParameter(BP_CONTROL_MODE_HAS_EDIT)) {
    for (vector<NodeTemplate *>::const_iterator it2=parsedNodes().begin(); it2!=parsedNodes().end(); it2++)
      vRootSuccessors.push_back(*it2);
  }
  for (vector<NodeTemplate *>::const_iterator it2=parsedNodes().begin(); it2!=parsedNodes().end(); it2++)
    if (it!=vOldRootSuccessors.end() && *it == *it2) it++;

  //copy anything else (custom) that might have been added...
  while (it != vOldRootSuccessors.end()) vRootSuccessors.push_back(*it++);
  */
  if (CDasherScreen *pScreen = m_pScreen) {
    //hack to make ChangeScreen do something
    m_pScreen = NULL; //i.e. make it think the screen has changed
    ChangeScreen(pScreen);
  }
}
