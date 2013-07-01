
#
apt-get -y install python-software-properties
apt-key adv --recv-keys --keyserver keyserver.ubuntu.com 0xcbcb082a1bb943db
add-apt-repository 'deb http://mirror.jmu.edu/pub/mariadb/repo/5.5/ubuntu precise main'

# Install MariaDB server:
apt-get update
apt-get -y install mariadb-server

# Install Patch (used to patch my.cnf):
apt-get -y install patch

# Generate a self signed certificate:
cd /etc/mysql
sh /mnt/bootstrap/gen-self-signed.sh "properssl.example.com" "server"

# Patch my.cnf to change listen address and enable SSL:
cd /
patch -p0 < /mnt/bootstrap/my.cnf.patch

# Restart the server:
service mysql restart

# Create the test user/database
mysql < /mnt/bootstrap/database-setup.sql
