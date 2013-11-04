#! /bin/bash
#
# 1) Export the public client (customer/identity provider) certificate from the keystore clientKeyStore.jks
# 2) Export the public service (Northgate/service provider) certificate from the keystore serviceKeyStore.jks
#
# Note: The contents of these public certificates get copied into the SAML metadata file idp_and_sp_metadata.xml
# as this is our contract between ourselves and a customer.  See the X509 sections in the metadata.
#

keytool -export -rfc -keystore clientKeyStore.jks -storepass rmi+ssl -alias localclient -file localclient.cer
keytool -export -rfc -keystore serviceKeyStore.jks -storepass rmi+ssl -alias remoteservice -file remoteservice.cer





