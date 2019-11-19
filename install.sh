#!/bin/bash
set -x #echo on
name='aws-session-api'
zip -r ${name}.zip src pom.xml build.sh
fission env create --name java --image fission/jvm-env --builder fission/jvm-builder --keeparchive --version 2
pkgname=`fission pkg create --sourcearchive ${name}.zip --env java | cut -f2 -d "'"`
echo $pkgname
#fission fn delete --name ${name}
#fission fn create --name ${name} --pkg $pkgname --fntimeout 180 --entrypoint com.lingk.fission.aws.session.api.AWSSessionTokenHelper 
#fission route create --url /${name} --function ${name} --createingress
fission fn delete --name ${name}-usage
fission fn create --name ${name}-usage --pkg $pkgname --fntimeout 180 --entrypoint com.lingk.fission.aws.session.api.AWSSessionTokenUsage 
fission route create --url /${name}-usage --function ${name}-usage --createingress
