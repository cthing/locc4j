<!DOCTYPE style-sheet system "style-sheet.dtd" >
<!-- you must have James Clark's style-sheet.dtd for this parse to correctly. -->

(element HTMLLite (make simple-page-sequence))

(element H1
  (make paragraph
        font-family-name: "Times New Roman"
        font-weight: 'bold
        font-size: 20pt
        line-spacing: 22pt
        space-before: 15pt
        space-after: 10pt
        start-indent: 6pt
        first-line-start-indent: -6pt
        quadding: 'center
        keep-with-next?: #t))
    ; A comment

(element P (make paragraph
	font-family-name: "Times New Roman"
        font-size: 12pt
	line-spacing: 13.2pt
	space-before: 6pt
	start-indent: 6pt
quadding: 'start))

(element EM (make sequence
    font-posture: 'italic))

(element STRONG (make sequence
    font-weight: 'bold))
