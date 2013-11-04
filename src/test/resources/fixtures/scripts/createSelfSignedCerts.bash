#! /bin/bash
#
# 1) Generate representative self-signed keypair a customer (client/identity provider) would generate if they wanted
#    to talk SAML with us, and needed to send us the public key part of the key pair for us to check the digital
#    signature of SAML assertions they send us.
#
# 2) Generate representative self-signed keypair we (Northgate/service provider) would generate if we wanted a customer
#    to use the public key part of the key pair to encrypt a SAML2 assertion prior to sending it to us.  Note, SAML1.1
#    doesn't cater for encrypted assertions, so only customers sending SAML2 can encrypt the assertion.
#
# 3) These two commands will actually create the keystores as well.  They also work from a windows command line if you
#    don't have access to a BOURNE shell.
#
# 4) If you are making new ones, copy the resulting jks files into the keystores fixtures directory, and then pull the
#    public certificates out of them by using the extractCertificates.bash script. 
#
# Note: In both cases ensure the keypairs have a long lifetime (10 years) so they don't expire for a good while.
#
keytool -genkeypair -v -alias remoteservice -keyalg RSA -keysize 2048 -dname "cn=remoteservice" -keypass remoteservice -keystore serviceKeyStore.jks -storepass rmi+ssl -validity 3650
keytool -genkeypair -v -alias localclient -keyalg RSA -keysize 2048 -dname "cn=localclient" -keypass localclient -keystore clientKeyStore.jks -storepass rmi+ssl -validity 3650


