# Write `foo`'s standard error (file descriptor 2)
# to a file called "output.stderr":
foo 2> output.stderr

# if $num doesn't contain a number,
# this test will be false and print an error,
# so by ignoring the error we can be sure that we're dealing
# with a number in the "if" block:
if test "$num" -gt 2 2>/dev/null
    # do things with $num as a number greater than 2
else
    # do things if $num is <= 2 or not a number
end

# Save `make`s output in a file:
make &>/log

# Redirections stack and can be used with blocks:
begin
    echo stdout
    echo stderr >&2 # <- this goes to stderr!
end >/dev/null # ignore stdout, so this prints "stderr"
