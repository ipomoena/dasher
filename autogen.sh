#!/bin/sh
autoreconf -vif
test -n "$NOCONFIGURE" || ./configure $*
