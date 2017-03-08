SUBDIRS =  training alphabets colours control settings GUI Help

desktopdir = $(datadir)/applications
dist_desktop_DATA = dasher.desktop
dasher.desktop:	dasher.desktop.in
	$(AM_V_GEN)$(MSGFMT) --desktop --template dasher.desktop.in -d $(top_srcdir)/po -o dasher.desktop

icondir = $(datadir)/icons/hicolor/48x48/apps
dist_icon_DATA = dasher.png
svgicondir = $(datadir)/icons/hicolor/scalable/apps
dist_svgicon_DATA = dasher.svg

gtk_update_icon_cache = gtk-update-icon-cache -f -t $(datadir)/icons/hicolor

install-data-hook: update-icon-cache

uninstall-hook: update-icon-cache

update-icon-cache:
	@-if test -z "$(DESTDIR)"; then \
		echo "Updating Gtk icon cache."; \
		$(gtk_update_icon_cache); \
	else \
		echo "*** Icon cache not updated.  After (un)install, run this:"; \
		echo "***   $(gtk_update_icon_cache)"; \
	fi

CLEANFILES = dasher.schemas dasher.gschema.xml dasher.desktop dasher.desktop.in
