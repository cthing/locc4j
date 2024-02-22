#!/usr/local/bin/perl
#
# composite series of images over a background image
#

if ($#ARGV != 4) {
 print "usage: compem bg.rgb inbase outbase startNum stopNum\n";
 exit;
}

$bg = $ARGV[0];
$inbase = $ARGV[1];
$outbase = $ARGV[2];
$start = $ARGV[3];
$stop = $ARGV[4];

# for each image
for ($i=$start; $i <= $stop; $i++) {

    # pad numbers
    $num = $i;
    if($i<10) { $num = "00$i"; }
    elsif($i<100) { $num = "0$i"; }

    # call unix command "over"
    $cmd = "over $bg $inbase.$num $outbase.$num 0 0";
    print $cmd."\n";
    if(system($cmd)) { print "over failed\n"; }
}
