#!/usr/bin/env bash

function didSayYes () {
    if [[ $1 == 'y'* || $1 == 'Y'* ]];
        then
        return 1;
    fi
    return 0;
}

printf "Cleaning.. \n"
./gradlew clean

printf "\n\nBuilding.. \n"
./gradlew assembleRelease

read -p 'Should install on device? (y/n)' shouldInstall

didSayYes ${shouldInstall}
RES=$?

if [[ ${RES} == 1 ]];
    then
        printf "\n\nInstalling.. \n"
        adb install -r -d app/build/outputs/apk/release/app-release.apk
fi
