#!/bin/sh

# **************************************************
# ** Libraries used by Kettle:                    **
# **************************************************

BASEDIR=`dirname $0`
cd $BASEDIR

CLASSPATH=$BASEDIR
CLASSPATH=$CLASSPATH:$BASEDIR/lib/kettle-core.jar
CLASSPATH=$CLASSPATH:$BASEDIR/lib/kettle-db.jar
CLASSPATH=$CLASSPATH:$BASEDIR/lib/kettle-engine.jar

# **************************************************
# ** JDBC & other libraries used by Kettle:       **
# **************************************************

for f in `find $BASEDIR/libext -type f -name "*.jar"` `find $BASEDIR/libext -type f -name "*.zip"`
do
  CLASSPATH=$CLASSPATH:$f
done


# **************************************************
# ** Platform specific libraries ...              **
# **************************************************

JAVA_BIN=java

# ******************************************************************
# ** Set java runtime options                                     **
# ** Change 128m to higher values in case you run out of memory.  **
# ******************************************************************

if [ -z "$JAVAMAXMEM" ]; then
  JAVAMAXMEM="256"
fi

OPT="-Xmx${JAVAMAXMEM}m -cp $CLASSPATH -Djava.library.path=$LIBPATH -DKETTLE_HOME=$KETTLE_HOME -DKETTLE_REPOSITORY=$KETTLE_REPOSITORY -DKETTLE_USER=$KETTLE_USER -DKETTLE_PASSWORD=$KETTLE_PASSWORD -DKETTLE_PLUGIN_PACKAGES=$KETTLE_PLUGIN_PACKAGES -DKETTLE_LOG_SIZE_LIMIT=$KETTLE_LOG_SIZE_LIMIT"

if [ "$1" = "-x" ]; then
  set LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$BASEDIR/libext
  export LD_LIBRARY_PATH
  OPT="-Xruntracer $OPT"
  shift
fi

# ***************
# ** Run...    **
# ***************

$JAVA_BIN $OPT org.pentaho.di.cluster.GenerateClusterSchema "${1+$@}"

