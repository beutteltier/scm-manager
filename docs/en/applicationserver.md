# Applicationserver

## Tomcat

Mercurial uses very large http headers for branch and head informations.
You have to increase the maxHttpHeaderSize parameter on the connector
configuration. Open the conf/server.xml and add the parameter to your
connector e.g.:

```xml
<Connector port="8080" protocol="HTTP/1.1"
           connectionTimeout="20000"
           redirectPort="8443" 
           maxHttpHeaderSize="16384" />
```

After changing the configuration you have to restart the tomcat.

Source: <http://tomcat.apache.org/tomcat-7.0-doc/config/http.html>

**Note**: If you have problems with big mercurial pushes on instances which are newer than tomcat 7.0.55,
you have to increase/disable the maxSwallowSize (see issue [#691](https://github.com/scm-manager/scm-manager/issues/691)).

#### Access Log

If you wan\'t like to print the current user in tomcats access log you
could use a valve like the following:

```xml
<Valve className="org.apache.catalina.valves.AccessLogValve" directory="logs"
       prefix="localhost_access_log" suffix=".txt"
       pattern="%h %l %u %{principal}r %t &quot;%r&quot; %s %b" />
```

The pattern **%{principal}r** is responsible for logging the
username to the access log (see issue [#877](https://github.com/scm-manager/scm-manager/issues/877)).

## GlassFish

To use SCM-Manager 1.6 and above with GlassFish 3 you have to add a
JVM-Option to the GlassFish configuration to override the jax-rs
packages. Please execute the following commands.

```bash
# override jax-rs packages
$ bin/asadmin create-jvm-options -Dcom.sun.enterprise.overrideablejavaxpackages=javax.ws.rs,javax.ws.rs.core,javax.ws.rs.ext
# increase http header size
$ bin/asadmin set configs.config.default-config.network-config.protocols.protocol.http-listener-1.http.header-buffer-length-bytes=16384
$ bin/asadmin set configs.config.default-config.network-config.protocols.protocol.http-listener-2.http.header-buffer-length-bytes=16384
```

**Note** Glassfish 3.1.2 and above seems not to work with SCM-Manager.
Please have a look at [QeO-QVX6wmsJ](https://groups.google.com/forum/#!searchin/scmmanager/glassfish/scmmanager/SEbuEU8H-qo/QeO-QVX6wmsJ "Exception deploying to Glassfish 3.1.2").

Restart the GlassFish-Server.

Source: <http://jersey.java.net/nonav/documentation/latest/glassfish.html>

## Jetty

If Jetty returns a HTTP \"413 FULL head\" status, you may want to
further increase the maximum header size. You can configure the limit by
setting the headerBufferSize for the connector you\'re using in the
jetty.xml file:

```xml
<Call name="addConnector">
  <Arg>
    <New class="org.mortbay.jetty.nio.SelectChannelConnector">
      [...]
      <Set name='headerBufferSize'>32768</Set>
    </New>
  </Arg>
</Call>
```
