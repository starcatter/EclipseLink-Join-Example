A basic example on how to:
 - Use JAXB2 for generating classes from XSD schema
 - Use EclipseLink to load classes from XML and join them based on ID fields using binding metadata
 - Join separate XML files /w separate XSD schemas using xlink and schema imports

Also there's the little things that came up along the way:
 - Setup basic validation for the above
 - Fix for xlink default behavior of inserting xml:base attributes into linked documents
 - Actual example on where to put jaxb.properties in case the unmarshaller is setup using package name
 - A bunch of other obscure details that took a while to figure out

Overall this is basic stuff but I really wish I found something like this project while looking for information on how any of this stuff works.
