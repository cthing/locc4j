⍝ Example APL file
L←(Lι':')↓L←,L       ⍝ drop To:
L←LJUST VTOM',',L    ⍝ mat with one entry per row
S←¯1++/∧\L≠'('       ⍝ length of address
X←0⌈⌈/S
L←S⌽(−(⍴L)+0,X)↑L    ⍝ align the (names)
A←((1↑⍴L),X)↑L       ⍝ address
N←0 1↓DLTB(0,X)↓L    ⍝ names)
N←,'⍺',N
N[(N='_')/ι⍴N]←' '   ⍝ change _ to blank
N←0 ¯1↓RJUST VTOM N  ⍝ names
S←+/∧\' '≠⌽N         ⍝ length of last word in name
