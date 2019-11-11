#!/bin/bash
zip package.zip src pom.xml build.sh
pkgname=`fission pkg create --sourcearchive package.zip --env nodeenv | cut -f2 -d "'"`
echo $pkgname
fission fn delete --name aws.session.api
fission fn create --name aws.session.api --pkg $pkgname --entrypoint com.lingk.aws.session.api.AWSSessionTokenHelper