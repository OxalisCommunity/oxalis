#!/bin/bash

################## FUNCTIONS DEFINITIONS ####################

fnDropOxalis() {
    echo ------------------DROPPING Oxalis DATABASE --------------------------------------------------------
	mysql --no-defaults -uroot -p$localhostpassword -e"drop database oxalis"
}

fnImportOxalis () {
    echo ------------------------ IMPORT Oxalis DATABASE --------------------------------------------------
    echo -------------------------------------------------------------------------------------------------------
    echo Creating the oxalis database
    echo -------------------------------------------------------------------------------------------------------
    mysql --no-defaults -uroot -p$localhostpassword -e"create database oxalis"
    mysql --no-defaults -uroot -p$localhostpassword -e"grant all on oxalis.* to skrue@'localhost' identified by '$userPassword'"
    mysql -uroot -p$localhostpassword oxalis < ../sql/create-oxalis-dbms.sql
    mysql --no-defaults -uroot -p$localhostpassword -e"flush privileges"

    echo Done :-\)
}
################## FIN FUNCTIONS DEFINITIONS ####################

################## PROGRAM EXECUTION ####################

echo "Password for user 'root' on the local SQL server:"
read -s localhostpassword
echo "Password for user 'skrue' on the local SQL server:"
read -s userPassword
fnDropOxalis;
fnImportOxalis;


