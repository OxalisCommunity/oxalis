# Oxalis developer notes

The purpose of this document is to document how to develop and maintain the Oxalis code base

## The Oxalis home directory

The concept of an "Oxalis home area", was introduced in version 1.18 in order to provide the users of Oxalis with these benefits:

* Maintaining your configuration when installing new releases.
* Several versions of Oxalis may be installed concurrently

The Oxalis home directory is located in the following order:

1. Using the environment variable `OXALIS_HOME`
1. The directory `./oxalis`, located relative to the users home directory. The users home directory is determined by
  inspecting the Java system property `user.home`
