# Lighttpd demo
Demonstration of Mayhem on the [lighttpd](https://www.lighttpd.net/)
web server.

We show how Mayhem creates a PoV (proof of vulnerability) exploit for
lighttpd 1.4.15 given just the docker image.

We also show how Mayhem can verify that the vulnerability is fixed
by replaying the PoV -- and an improved test suite! -- on 
lighttpd 1.4.52.


## Branches
  * 'main' branch (default) corresponds to lighttpd 1.4.15, which is vulnerable
  * 'vulnerable' branch corresponds to lighttpd 1.4.15 (same as main), which is vulnerable
  * 'fixed' branch corresponds to lighttpd 1.4.52, which fixes the vulnerability


