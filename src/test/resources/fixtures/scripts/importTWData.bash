#! /bin/bash
#
# Not all keys come from the java world.  In order for us to validate signing and encryption without directly engaging
# a customer we need to borrow their private key and their public certificate (so their key pair) and use these
# keys, one private for signing SAML, and one public for checking thier signature.
#
# Some customers (most to be honest) will be Windows environments.  Getting keys of these dudes and getting them
# into a Java environment that uses JKS keystores can be tricky.
#
# This file covers the steps I did on reception of a pfx file (PCFS#12) holding a private key and a public cert pair
# generated in a windows enivrionment, and how I got them into our local keystores in order to validate their
# joint applicability for digital signing using the private key of the customer to signature validation at the
# consumer
#

# First, convert the pfx file into something an editor can read, and so from which you can pull the private key
# and the public certificate
openssl pkcs12 -in TWTestCert.pfx -out tw_private_key_and_cert_pair.crt -nodes

# This conversion also provides the alias for the private key the customer used, so for example the content of
# the tw_private_key_and_cert_pair.crt starts with, which identifies a private key with the alias
# fd654c2eb8887c9a22f7d55120f0a9e6_435bdcfd-c794-444b-8897-c9feee992b63
#
# Bag Attributes
#   localKeyID: 01 00 00 00
#   Microsoft CSP Name: Microsoft Enhanced Cryptographic Provider v1.0
#   friendlyName: fd654c2eb8887c9a22f7d55120f0a9e6_435bdcfd-c794-444b-8897-c9feee992b63
# Key Attributes
#   X509v3 Key Usage: 10
# -----BEGIN RSA PRIVATE KEY-----
# MIICXgIBAAKBgQC8F0fVWAX6tEftOZ/9393ODtMrrLAbspIkMBIscpNw9WiVZmt+
# QmWyVFC7vdydwP3O3tcn4XAA0pOEHYdHAWs5F7No/u5ah+A02a7s4mdm+Kn9cHoU
# pyKLCMv455DqImrqnrQaRNZBhGQVJs0u7UTWEj7Pu81raQW6X6ZPywQ88wIDAQAB
# AoGAPZZV5Ap9crijMI8EzykFRJpgFNXnmDohVg4TdVBS7NK+WuT9X4s4J2sqQD8L
# xO/Ta3BV/O35MZvBx6mviNVzGN/w41Ke8ZbpVBKf5o1pb8pC1/i4JkUSuzYzjx00
# /jO+wi9VS+KmezqUF9To348V40hLGweUrun/pwDFGM+zfMkCQQDcG4E92g2xoHm/
# jp+HXsiGqsywgOIVYJ7cUdN5PfhA4XvnJTbAAf4c087JwZL/aBgTOX5kiubAhtwa
# +90PtlD3AkEA2sM7HPJqzl1H7tt9SRdr2t0OhEyrgqFvPAlGeqVpAKBNd8CSzY9x
# MKvKtjk8FhJyBogovF4hLhPxK/tld9yw5QJBAKMYtnHQi+kdEloBIvC6KTiwgzAe
# sGhKyixTbbIfWz8oVhl3F9S7JULTKB2UFIqw5XYJWkDCNC5hP+O20Z186Q0CQQDW
# z3gfT4dDRmOZDxvyBraYvhJAJQ0RA7Y3TKBRVFVMERygp48/nEe4VCiEUKVIwyoG
# cfMWLJQGK/zYhE45qZJlAkEAy2K/9kLSCOI95553IDVmlCBq/b8E08XN+C/6/IcF
# 9mZ+JB+VuMVuOymC62vjBAAiDeLvJTKXlsVuPINDtTFPcw==
# -----END RSA PRIVATE KEY-----

#  Copy the CERTIFICATE part of the file out into a seperate file tw_public.crt

# Import the public certificate into your local service keystore, as if this breaks, its NOT a valid
# certificate, so cannot make it into any metadata statement for the customer
keytool -import -trustcacerts -keystore serviceKeyStore.jks -storepass rmi+ssl -alias tw -file tw_public.crt

# Import the private key into your local client keystore as if this breaks, its NOT a valid
# private key, so cannot be used to digitally sign any customer SAML we recieve.
#
# Note we apply the password the customer gave us for the store and the alias of their private key in order to
# create a "copy" of their private key under alias "tw" in our jks keystore.
#
keytool -importkeystore -srckeystore TWTestCert.pfx -destkeystore clientKeyStore.jks -srcstoretype PKCS12 -deststoretype JKS -srcstorepass gfx95fc8 -deststorepass rmi+ssl -srcalias fd654c2eb8887c9a22f7d55120f0a9e6_435bdcfd-c794-444b-8897-c9feee992b63 -destalias tw -destkeypass rmi+ssl -noprompt







