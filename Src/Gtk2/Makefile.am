noinst_LTLIBRARIES = libdashergtk.la libdashercontrol.la

#noinst_PROGRAMS = generate-schema
#generate_schema_SOURCES = GenerateSchema.cpp ../DasherCore/Parameters.cpp

#bin_PROGRAMS = dasher-config

if WITH_GPE
settings_store = gpesettings_store.cc gpesettings_store.h
else
settings_store = 
endif

#dasher_config_SOURCES = \
#		DasherAppSettings.cpp \
#		DasherAppSettings.h \
#		Preferences.cpp \
#		Preferences.h \
#		dasher_config.cpp \
#		module_settings_window.cpp \
#		module_settings_window.h#
#
#dasher_config_LDADD = \
#	$(GTKBUILD_LIBS)

libdashercontrol_la_SOURCES = \
		Canvas.cpp \
		Canvas.h \
		DasherControl.cpp \
		DasherControl.h \
		FileUtils.h \
		FileUtils.cpp \
		GtkDasherControl.cpp \
		GtkDasherControl.h \
		KeyboardHelper.cpp \
		KeyboardHelper.h \
		Timer.cpp \
		Timer.h \
		Speech.h \
		custom_marshal.cpp \
		custom_marshal.h \
		joystick_input.h \
		mouse_input.h \
		$(settings_store)

if USE_SPEECHDISPATCHER
libdashercontrol_la_SOURCES += SpeechDispatcher.cpp
endif
libdashercontrol_la_CPPFLAGS = @SPEECH_CPPFLAGS@
libdashercontrol_la_LDFLAGS  = @SPEECH_LDFLAGS@
libdashercontrol_la_LIBADD   = @SPEECH_LIBS@

libdashergtk_la_SOURCES = \
		DasherAppSettings.cpp \
		DasherAppSettings.h \
		GtkDasherControl.h \
		Preferences.cpp \
		Preferences.h \
		dasher.h \
		dasher_editor_external.h \
		dasher_editor_private.h \
		dasher_editor.cpp \
		dasher_editor.h \
		dasher_lock_dialogue.cpp \
		dasher_lock_dialogue.h \
		dasher_main_private.h \
		dasher_main.cpp \
		dasher_main.h \
		module_settings_window.cpp \
		module_settings_window.h

if USE_ATSPI
libdashergtk_la_SOURCES += \
		dasher_editor_external_atspi.cpp
libdashergtk_la_CPPFLAGS = @ATSPI_CFLAGS@
libdashergtk_la_LDFLAGS  = @ATSPI_LIBS@
libdashergtk_la_LIBADD   = @ATSPI_LIBS@
else
if USE_CSPI
libdashergtk_la_SOURCES += \
		dasher_editor_external_cspi.cpp
libdashergtk_la_CPPFLAGS = @CSPI_CFLAGS@
libdashergtk_la_LDFLAGS  = @CSPI_LIBS@
libdashergtk_la_LIBADD   = @CSPI_LIBS@
else
libdashergtk_la_SOURCES += \
		dasher_editor_external_xtest.cpp
endif
endif

AM_CXXFLAGS = -I$(srcdir)/../DasherCore -DPROGDATA=\"$(pkgdatadir)\" $(GTKBUILD_CFLAGS) 
