# Oxalis roadmap
Oxalis should adapt to the changing requirements of PEPPOL transport and the BIS/EHF formats.

## Important PEPPOL, BIS and EHF dates (as of 2014-06-12)

* **2014-xx-xx** PEPPOL BIS v1 will be discontinued 
* **2014-10-01** EHF v1.6 - faktura og kreditnota støttes ikke lenger
* **2014-09-01** PEPPOL BIS v2 becomes mandatory (BIS v1 becomes optional)
* **2014-09-01** EHF v2.0 - faktura og kreditnota obligatorisk for sender (leverandører)
* **2014-09-01** Changed PEPPOL policy - AS2 is required
* **2014-07-01** EHF v2.0 – faktura og kreditnota obligatorisk for mottakere
* **2014-04-01** Changed PEPPOL policy - START is optional for new access points in PEPPOL
* **2014-03-01** PEPPOL BIS v2 is optional

## Upcoming Oxalis versions

### Oxalis 3.1.0 (ETA November 2014)
* Removal of obsolete / discontinued features (eg START, EHF v1.6, PILOT certs)
* Update tests to EHF v2.0 as v1.6 is phased out
* TLSv1 only for outbound https (ref POODLE vulnerability)

### Oxalis 3.0.2 (November 6 2014)
* Defaults to TLS for outbound https (ref POODLE vulnerability)
* Bug fixes, enhancements, optimizations

### Oxalis 3.0.1 (August 22 2014)
* Rewritten meta data extraction from BIS/EHF documents
* Correctly identifying sender / receiver for BIS/EHF document types

### Oxalis 3.0.0 (Early June 2014)
* Finalized AS2 support and related bug fixes.
* Supports BIS v2 and EHF v2 formats

### Oxalis 3.0 Beta (Late May 2014)
* Contains AS2 support aligned with other AS2-implementations in the marked.
* Updated API (should be final for 3.0)
* Should be used to prepare for production

### Oxalis 3.0 Alpha (Late 2013)
* Contains AS2 support
* Updated API (still in flux)
