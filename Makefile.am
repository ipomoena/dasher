SUBDIRS = Src Data Doc po

ACLOCAL_AMFLAGS = -I m4

distuninstallcheck_listfiles = \
		find . -type f -print | \
		grep -v '^\./var/scrollkeeper' | \
		grep -v '^\./share/gnome/help'

EXTRA_DIST = \
		INSTALL.Linux \
		INSTALL.WinCE \
		INSTALL.Windows \
		MAINTAINERS \
		m4/pkg.m4 \
		m4/dgconf-2.m4 \
		m4/gnome-doc-utils.m4 \
		m4/nls.m4 \
		gnome-doc-utils.make \
		.tarball-version \
		build-aux/mkversion

DISTCLEANFILES = \
		Data/dasher.desktop \
		Data/Help/Gnome/Makefile

MAINTAINERCLEANFILES = \
		$(srcdir)/INSTALL \
		$(srcdir)/aclocal.m4 \
		$(srcdir)/autoscan.log \
		$(srcdir)/compile \
		$(srcdir)/config.guess \
		$(srcdir)/config.h.in \
		$(srcdir)/config.h.in~ \
		$(srcdir)/config.sub \
		$(srcdir)/configure \
		$(srcdir)/configure.scan \
		$(srcdir)/depcomp \
		$(srcdir)/install-sh \
		$(srcdir)/ltmain.sh \
		$(srcdir)/missing \
		$(srcdir)/mkinstalldirs \
		$(srcdir)/omf.make \
		$(srcdir)/xmldocs.make \
		$(srcdir)/po/Makefile.in.in

maemo-install: all
	- mkdir tmp_build
	cp -a Data/maemo/* tmp_build
	cp Src/dasher tmp_build/usr/bin/dasher
	strip tmp_build/usr/bin/dasher
	cp Data/GUI/dashermaemo.ui Data/GUI/dashermaemofullscreen.ui tmp_build/usr/share/dasher
	cp Data/training/training_english_GB.txt Data/alphabets/alphabet.english.xml Data/colours/colour.xml tmp_build/usr/share/dasher
	ln -sf /usr/share/applications/hildon/dasher.desktop tmp_build/etc/others-menu/extra_applications/0301_dasher.desktop
	- find tmp_build -name .git -exec rm -rf \{\} \;
	dpkg-deb -b tmp_build dasher_4.11.0.0inferencemaemo1_armel.deb
	rm -rf tmp_build
