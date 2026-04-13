FROM docker:24.0.7-dind

ADD ca.pem /ca.pem
ADD server-cert.pem /server-cert.pem
ADD server-key.pem /server-key.pem

CMD ["-b", "none", "--tlsverify", "--tlscacert=ca.pem", "--tlscert=server-cert.pem", "--tlskey=server-key.pem", "-H=0.0.0.0:2376"]
