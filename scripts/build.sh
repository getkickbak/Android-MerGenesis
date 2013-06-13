#!/bin/bash

appPath="/Users/eric/Documents/GetKickBak/V2.1.0/V2.1.0/public/javascripts/mobile";
PROJECT_DIR="$1"
NDK_ROOT="/Developer/SDKs/android-ndk-r8c"

libPath="../lib/touch-2.1.1";

touch -cm ${PROJECT_DIR}/www
mkdir -p $PROJECT_DIR/www/app/worker
mkdir -p $PROJECT_DIR/www/app/store
mkdir -p $PROJECT_DIR/www/app/profile
mkdir -p $PROJECT_DIR/www/resources/themes/images/v1

rsync -pvtrlL --delete --cvs-exclude "$appPath"/app/store/*Server*.json $PROJECT_DIR/www/app/store/
rsync -pvtrlL --delete --cvs-exclude "$appPath"/app/profile/Android.js $PROJECT_DIR/www/app/profile/
                                                          
rsync -pvtrlL --delete --cvs-exclude "$appPath"/*.wav $PROJECT_DIR/www/
rsync -pvtrlL --delete --cvs-exclude "$appPath"/*.htm $PROJECT_DIR/www/
rsync -pvtrlL --delete --cvs-exclude "$appPath"/worker/server.js $PROJECT_DIR/www/worker/
rsync -pvtrlL --delete --cvs-exclude "$appPath"/server-all.js $PROJECT_DIR/www/
rsync -pvtrlL --delete --cvs-exclude "$appPath"/core.js $PROJECT_DIR/www/
rsync -pvtrlL --delete --cvs-exclude "$appPath"/index_android_server.html $PROJECT_DIR/www/index.html
rsync -pvtrlL --delete --cvs-exclude "$appPath"/lib/*.android.js $PROJECT_DIR/www/lib/
rsync -pvtrlL --delete --cvs-exclude "$appPath"/../lib/*min.js $PROJECT_DIR/www/lib/
rsync -pvtrlL --delete --cvs-exclude "$appPath"/lib/core/WifiConnMgr.js $PROJECT_DIR/www/lib/core/
rsync -pvtrlL --delete --cvs-exclude "$appPath"/lib/core/websocket.js $PROJECT_DIR/www/lib/core/
rsync -pvtrlL --delete --cvs-exclude "$appPath"/lib/core/*nfc*.js $PROJECT_DIR/www/lib/core/
rsync -pvtrlL --delete --cvs-exclude "$appPath"/lib/core/*.android.js $PROJECT_DIR/www/lib/core/
rsync -pvtrlL --delete --cvs-exclude "$appPath"/../lib/core/date.js $PROJECT_DIR/www/lib/core/
rsync -pvtrlL --delete --cvs-exclude "$appPath"/../lib/core/extras.js $PROJECT_DIR/www/lib/core/
rsync -pvtrlL --delete --cvs-exclude "$appPath"/$libPath/sencha-touch-all.js $PROJECT_DIR/www/lib/
                                                          
rsync -pvtrlL --delete --cvs-exclude "$appPath"/resources/keys.txt $PROJECT_DIR/www/resources/
rsync -pvtrlL --delete --cvs-exclude "$appPath"/resources/css/a*.css $PROJECT_DIR/www/resources/css/
rsync -pvtrlL --delete --cvs-exclude "$appPath"/resources/audio/*.mp3 $PROJECT_DIR/www/resources/audio/
rsync -pvtrlL --delete --cvs-exclude "$appPath"/resources/audio/*.wav $PROJECT_DIR/www/resources/audio/
rsync -pvtrlL --delete --cvs-exclude "$appPath"/resources/themes/images/v1/android $PROJECT_DIR/www/resources/themes/images/v1


# FourierTest/build.sh
# Compiles fftw3 for Android
# Make sure you have NDK_ROOT defined in .bashrc or .bash_profile
 
INSTALL_DIR="$PROJECT_DIR/../jni/fftw3"
SRC_DIR="$PROJECT_DIR/../../fftw-3.3.3"
 
cd $SRC_DIR
 
export PATH="$NDK_ROOT/toolchains/arm-linux-androideabi-4.4.3/prebuilt/darwin-x86/bin/:$PATH"
export SYS_ROOT="$NDK_ROOT/platforms/android-8/arch-arm/"
export CC="arm-linux-androideabi-gcc --sysroot=$SYS_ROOT"
export LD="arm-linux-androideabi-ld"
export AR="arm-linux-androideabi-ar"
export RANLIB="arm-linux-androideabi-ranlib"
export STRIP="arm-linux-androideabi-strip"
 
#mkdir -p $INSTALL_DIR
#./configure --host=arm-eabi --build=i386-apple-darwin10.8.0 --prefix=$INSTALL_DIR LIBS="-lc -lgcc"
 
#make
#make install
 
#exit 0
