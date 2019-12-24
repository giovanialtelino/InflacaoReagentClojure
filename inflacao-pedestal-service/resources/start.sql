CREATE TABLE PRECOS12_IPCA12 (VALDATA date PRIMARY KEY,
                              VALVALOR decimal NOT NULL,
                              NIVNOME varchar(20),
                              TERCODIGO varchar(20));

CREATE TABLE IGP12_IGPM12 (VALDATA date PRIMARY KEY,
                                                        VALVALOR decimal NOT NULL,
                                                        NIVNOME varchar(20),
                                                        TERCODIGO varchar(20));;
CREATE TABLE IGP12_IGPDI12  (VALDATA date PRIMARY KEY,
                                                          VALVALOR decimal NOT NULL,
                                                          NIVNOME varchar(20),
                                                          TERCODIGO varchar(20));;
CREATE TABLE IGP12_IPC12 (VALDATA date PRIMARY KEY,
                                                       VALVALOR decimal NOT NULL,
                                                       NIVNOME varchar(20),
                                                       TERCODIGO varchar(20));;
CREATE TABLE PRECOS12_INPC12 (VALDATA date PRIMARY KEY,
                                                           VALVALOR decimal NOT NULL,
                                                           NIVNOME varchar(20),
                                                           TERCODIGO varchar(20));;
CREATE TABLE LAST_UPDATE (LASTUPDATE date PRIMARY KEY);