---
title: "Linux"
weight: -100
---

Installation
==================

Dasher makes use of the GNU autotools system (automake, autoconf
etc.), which are described in the 'INSTALL' file.  The following is
specific to building and installing Dasher on Linux.  The Dasher
maintainer documentation is at http://live.gnome.org/Dasher.

1) Fork the project from github.

2) Clone the repo to your machine
```
 $ git clone https://github.com/<your account>/dasher.git
 $ cd dasher
```
3) Install dependencies
If you are building sources from the Git repository then you must
first install all the packages required to build Dasher.  On Debian
based distributions the following can be used.
```
 $ packages="g++
            gnome-common
            gnome-doc-utils
            libatspi2.0-dev
            libgtk-3-dev
            libspeechd-dev"

 $ sudo apt-get install $packages
```
4) Configuration and Install
autogen:
```
 $ ./autogen.sh
 $ make
 $ sudo make instal
```
If the speech support is desired then
```
 $ ./autogen.sh --enable-speech=yes
 $ make
 $ sudo make install
```
5) Run dasher
```
 $/usr/local/bin/dasher
```

Options
=======

   --disable-speech    Disable speech support (speech dispatcher).

   --disable-a11y      Disable support for GNOME 2 accessibility features
<br />                       (enabled by default).

   --disable-atspi     Disable support for GNOME 3 accessibility features
<br />                       (enabled by default). 
<br />This flag is just useful
                       for debugging, as accessibility is now built-in
                       and doesn't bring in more dependencies.


   --with-gpe          Build binaries for the GPE palmtop environment.

   --with-qte          Build binaries using the QTE environment.

GSettings
=========

Note that in the GNOME 3 world, GSettings, part of glib, replace
GConf.  By default, dasher's "configure" will use GSettings if
found.  If not it will use GConf if found.  This can be influenced
by the --with-gsettings flag to configure.  If on running dasher,
you see

  GLib-GIO-ERROR **: Settings schema 'org.gnome.Dasher' is not installed
  aborting...

try adding the directory which contains "dasher.gschema.xml",
<br />usually ${prefix}/share/glib-2.0/schemas, to the environment variable
<br />GSETTINGS_SCHEMA_DIR.

Desktop Shortcuts
=================
After the installation is done, for Ubuntu and derivatives
(e.g., Goobuntu), you can create desktop shortcuts starting Dasher in
different profiles by running:
```
 $ cd dasher
 $ ./create-linux-desktop-shortcuts.sh
```

