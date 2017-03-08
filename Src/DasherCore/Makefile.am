SUBDIRS = LanguageModelling

noinst_LTLIBRARIES = libdashercore.la libdasherprefs.la

libdasherprefs_la_SOURCES = \
		Parameters.h \
		Parameters.cpp \
		SettingsStore.cpp \
		SettingsStore.h \
		XmlSettingsStore.h \
		XmlSettingsStore.cpp

libdashercore_la_SOURCES = \
		AbstractXMLParser.cpp \
		AbstractXMLParser.h \
		Alphabet/AlphIO.cpp \
		Alphabet/AlphIO.h \
		Alphabet/AlphInfo.cpp \
		Alphabet/AlphInfo.h \
		Alphabet/AlphabetMap.cpp \
		Alphabet/AlphabetMap.h \
		Alphabet/GroupInfo.h \
		AlternatingDirectMode.cpp \
		AlternatingDirectMode.h \
		AlphabetManager.cpp \
		AlphabetManager.h \
		AutoSpeedControl.cpp \
		AutoSpeedControl.h \
		BasicLog.cpp \
		BasicLog.h \
		ButtonMode.cpp \
		ButtonMode.h \
		ButtonMultiPress.h \
		ButtonMultiPress.cpp \
		CircleStartHandler.cpp \
		CircleStartHandler.h \
		ClickFilter.cpp \
		ClickFilter.h \
		ColourIO.cpp \
		ColourIO.h \
		CompassMode.cpp \
		CompassMode.h \
		ControlManager.cpp \
		ControlManager.h \
		ConversionManager.cpp \
		ConversionManager.h \
		ConvertingAlphMgr.cpp \
		ConvertingAlphMgr.h \
		DasherButtons.cpp \
		DasherButtons.h \
		DasherInput.h \
		DasherInterfaceBase.cpp \
		DasherInterfaceBase.h \
		DashIntfSettings.cpp \
		DashIntfSettings.h \
		DashIntfScreenMsgs.h \
		DashIntfScreenMsgs.cpp \
		DasherModel.cpp \
		DasherModel.h \
		DasherModule.cpp \
		DasherModule.h \
		DasherNode.cpp \
		DasherNode.h \
		DasherScreen.h \
		DasherTypes.h \
		DasherView.cpp \
		DasherView.h \
		DasherViewSquare.cpp \
		DasherViewSquare.h \
		DasherViewSquare.inl \
		DefaultFilter.cpp \
		DefaultFilter.h \
		DemoFilter.cpp \
		DemoFilter.h \
		DynamicButtons.cpp \
		DynamicButtons.h \
		DynamicFilter.cpp \
		DynamicFilter.h \
		Event.h \
		FileLogger.cpp \
		FileLogger.h \
		FileWordGenerator.cpp \
		FileWordGenerator.h \
		FrameRate.h \
		FrameRate.cpp \
		GameStatistics.h \
		GameModule.cpp \
		GameModule.h \
		InputFilter.h \
		MandarinAlphMgr.cpp \
		MandarinAlphMgr.h \
		MemoryLeak.cpp \
		MemoryLeak.h \
		Messages.h \
		Messages.cpp \
		ModuleManager.cpp \
		ModuleManager.h \
		NodeCreationManager.cpp \
		NodeCreationManager.h \
		NodeManager.h \
		ExpansionPolicy.cpp \
		ExpansionPolicy.h \
		Observable.h \
		OneButtonDynamicFilter.cpp \
		OneButtonDynamicFilter.h \
		OneButtonFilter.cpp \
		OneButtonFilter.h \
		OneDimensionalFilter.cpp \
		OneDimensionalFilter.h \
		RoutingAlphMgr.cpp \
		RoutingAlphMgr.h \
		SCENode.cpp \
		SCENode.h \
		ScreenGameModule.cpp \
		ScreenGameModule.h \
		SimpleTimer.cpp \
		SimpleTimer.h \
		SocketInput.cpp \
		SocketInput.h \
		SocketInputBase.cpp \
		SocketInputBase.h \
		StartHandler.h \
		StylusFilter.cpp \
		StylusFilter.h \
		TimeSpan.cpp \
		TimeSpan.h \
		Trainer.cpp \
		Trainer.h \
		TwoBoxStartHandler.cpp \
		TwoBoxStartHandler.h \
		TwoButtonDynamicFilter.cpp \
		TwoButtonDynamicFilter.h \
		TwoPushDynamicFilter.cpp \
		TwoPushDynamicFilter.h \
		UserButton.cpp \
		UserButton.h \
		UserLocation.cpp \
		UserLocation.h \
		UserLog.cpp \
		UserLog.h \
		UserLogBase.h \
		UserLogBase.cpp \
		UserLogParam.cpp \
		UserLogParam.h \
		UserLogTrial.cpp \
		UserLogTrial.h \
		WordGeneratorBase.h \
		WordGeneratorBase.cpp \
		XMLUtil.cpp \
		XMLUtil.h

libdashercore_la_LIBADD = @JAPANESE_SOURCES@ ../Common/libdashermisc.la
libdashercore_la_DEPENDENCIES = @JAPANESE_SOURCES@

EXTRA_libdashercore_la_SOURCES = \
		CannaConversionHelper.cpp \
		CannaConversionHelper.h

AM_CXXFLAGS = -I$(srcdir)

EXTRA_DIST = \
		LanguageModelling/KanjiConversionIME.cpp \
		LanguageModelling/KanjiConversionIME.h \
		DasherCore.vcxproj
