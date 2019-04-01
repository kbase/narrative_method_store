#!/usr/bin/env python
'''
Created on Mar 11, 2014

@author: gaprice@lbl.gov
'''
from __future__ import print_function
import sys
from configobj import ConfigObj
import os
import stat

PORT = 'port'
THREADS = 'server-threads'
MINMEM = 'min-memory'
MAXMEM = 'max-memory'


def printerr(*objs):
    print(*objs, file=sys.stderr)
    sys.exit(1)


def make_executable(path):
    st = os.stat(path)
    os.chmod(path, st.st_mode | stat.S_IXUSR | stat.S_IXGRP | stat.S_IXOTH)


def getConfig(param, cfg, cfile):
    if param not in cfg:
        printerr('Missing expected parameter {} in config file {}'
                 .format(param, cfile))
    return cfg[param]


if len(sys.argv) < 8:
    printerr("Missing arguments to build_server_control_scripts")
if len(sys.argv) == 8:
    _, serviceDir, war, target, javaHome, deployCfg, asadmin, serviceDomain =\
        sys.argv
    port = None
else:
    _, serviceDir, war, target, javaHome, deployCfg, asadmin, serviceDomain,\
        port = sys.argv

if not os.path.isfile(deployCfg):
    printerr('Configuration parameter is not a file: ' + deployCfg)
cfg = ConfigObj(deployCfg)
if serviceDomain not in cfg:
    printerr('No {} section in config file {} - '.format(
        serviceDomain, deployCfg))
wscfg = cfg[serviceDomain]

if port is None:
    if PORT not in wscfg:
        printerr("Port not provided as argument or in config")
    port = wscfg[PORT]

threads = getConfig(THREADS, wscfg, deployCfg)
minmem = getConfig(MINMEM, wscfg, deployCfg)
maxmem = getConfig(MAXMEM, wscfg, deployCfg)
