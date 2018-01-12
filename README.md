# rabbitmq-java-client-issue-283
Demonstrate RabbitMQ Server TLS connection issue 1474

https://github.com/rabbitmq/rabbitmq-server/issues/1474

# Installing Erlang and RabbitMQ:
```
wget https://packages.erlang-solutions.com/erlang-solutions_1.0_all.deb
sudo dpkg -i erlang-solutions_1.0_all.deb
sudo apt-get update
sudo apt-get install -y esl-erlang=1:20.1.7

wget https://github.com/rabbitmq/rabbitmq-server/releases/download/rabbitmq_v3_6_14/rabbitmq-server_3.6.14-1_all.deb
sudo apt-get install socat
sudo dpkg -i rabbitmq-server_3.6.14-1_all.deb
```

# Configuring for TLS

Put `/etc/rabbitmq.config` as
```
[
  {rabbit, [
     {ssl_listeners, [5671]},
     {ssl_options, [{cacertfile,"/etc/rabbitmq/cacert.pem"},
                    {certfile,"/etc/rabbitmq/cert.pem"},
                    {keyfile,"/etc/rabbitmq/key.pem"},
                    {verify,verify_peer},
                    {fail_if_no_peer_cert,false}]}
   ]}
].
```
Generate self-signed cert and restart broker
```
sudo openssl genrsa -out /etc/rabbitmq/key.pem 2048
sudo openssl req -new -key /etc/rabbitmq/key.pem -out /etc/rabbitmq/request.pem -outform PEM -subj "/CN=localhost/" -nodes
sudo openssl x509 -req -days -1000 -in /etc/rabbitmq/request.pem -signkey /etc/rabbitmq/key.pem -out /etc/rabbitmq/cert.pem
sudo touch /etc/rabbitmq/cacert.pem
sudo service rabbitmq-server restart
```

# Install Java
```
sudo apt-get install -y openjdk-8-jre-headless
```

# Build and run test client
```
sudo apt-get install -y maven
git clone https://github.com/dimas/rabbitmq-broker-tls-issue-1474
cd rabbitmq-broker-tls-issue-1474

mvn clean install

java -jar target/rabbitmq-broker-tls-issue-1474-0.1-shaded.jar localhost
```

