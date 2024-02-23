proc bit {varName pos {bitval {}}} {
   upvar 1 $varName var
   if {![info exist var]} {set var 0}
   set element [expr {$pos/32}]
   while {$element >= [llength $var]} {lappend var 0}
   set bitpos [expr {1 << $pos%32}]
   set word [lindex $var $element]
   if {$bitval != ""} {
       if {$bitval} {
           set word [expr {$word | $bitpos}]
       } else {
           set word [expr {$word & ~$bitpos}]
       }
       lset var $element $word
   }
   expr {($word & $bitpos) != 0}
}

#---------------------- now testing...
if {[file tail [info script]] == [file tail $argv0]} {
   foreach {test      expected} {
       {bit foo 5 1}  1
       {set foo}      32
       {bit foo 32 1} {32 1}
   } {
       catch {eval $test} res
       puts $test:$res/$expected
   }
}
