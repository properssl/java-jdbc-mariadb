### JDBC SSL Connection to MariaDB
This example demonstrates how to properly create an SSL connection to a MariaDB server. 

The majority of examples on the internet of using SSL with MariaDB (and MySQL) instruct the JDBC driver to not validate the server's certificate. This is vulnerable to a [man in the middle](MITM) attack. This is particularly important with the rise of cloud based database-as-a-service platforms where client applications are connecting to the remote servers through the public internet.

The latest release (as of writing this) of the MariaDB Java client driver, [v1.1.3][MariaDB-Java-Client-v1.1.3],  includes the ability to validate self signed server certificates by setting a new connection property, `serverSslCert`. This example demonstrates how to use it to secure your JDBC connection to your database.

When the property `serverSslCert` is set the MariaDB JDBC driver sets up the connection to use a TrustManager and SSLSocketFactory that can validate against a pre-shared certificate. It provides protection against MITM. Additionaly, through certificate pinning a self-signed certificate can be even more secure than one signed by a trusted certificate authority as it will also be immune to [CA] compromises.

For more detail about configuring the MariaDB Java client driver see its [about page][MariaDB-Java-Client-Notes].

For other examples of properly establishing SSL connections check out http://properssl.org/.

### Installing the JDBC driver
At the moment the MariaDB Java client driver is not available in the central Maven repository so you have to manually install it. Here are the steps:

1. Download the latest version of the driver from: https://downloads.mariadb.org/client-java/
2. Change directory to the JAR file you downloaded. It should be named something like `mariadb-java-client-1.1.3.jar`
3. Execute the follow from the command line to add it to your local Maven repository. Make sure to edit the `JDBC_DRIVER_VERSION` to match the version you downloaded.

    $ JDBC_DRIVER_VERSION=1.1.3
    $ mvn install:install-file -Dfile=mariadb-java-client-${JDBC_DRIVER_VERSION}.jar -DgroupId=org.mariadb.jdbc -DartifactId=mariadb-java-client -Dversion=${JDBC_DRIVER_VERSION} -Dpackaging=jar

### Vagrant
The tests are configured to run against the local machine. For convenience a Vagrantfile is included to automatically provision a MariaDB server with a test user and database. To create it run:

    $ vagrant up

After creating the VM the install script will:

1. Run apt-get update/upgrade to update the system packages
2. Install MariaDB
3. Update the MariaDB configuration files to allow inbound connections
4. Create a test user and database

### Server SSL Certificate
After installation run the following from the root directory of the project to extract the randomly generated server SSL certificate:

    $ vagrant ssh 'sudo cat /etc/mysql/server.crt' > src/test/resources/server.crt

### Building
To build use maven:

    mvn clean compile

### Running Tests
To run the tests use maven:

    mvn test

[MITM]: http://en.wikipedia.org/wiki/Man-in-the-middle_attack
[MariaDB-Java-Client-v1.1.3]: https://kb.askmonty.org/en/mariadb-java-client-113-release-notes/
[CA]: http://en.wikipedia.org/wiki/Certificate_authority
[MariaDB-Java-Client-Notes]: https://kb.askmonty.org/en/about-the-mariadb-java-client/
