#!/bin/zsh

# This line sets the variable 'filename' to the first argument passed to the script
filename=$1

# This line prints the hostname of the current system
echo "Hostname: $(hostname)"

# This line creates a subshell and runs the 'ls' command inside it.
# The output of the 'ls' command is then stored in the 'ls_output' variable
ls_output=$(ls)

echo "Output of 'ls' command: $ls_output"

# This line checks if the file specified by the 'filename' variable exists
# and is a regular file (not a directory). If the file exists, it prints the contents
# of the file using the 'cat' command. If the file does not exist, it prints an error message
if [[ -f "$filename" ]]; then
  cat "$filename"
else
  echo "Error: File not found"
fi

# This line prints the current working directory
echo "Current working directory: $(pwd)"

# This line prints the current user's home directory
echo "Home directory: $HOME"

# This line reads input from the user and stores it in the 'input' variable
echo -n "Enter your name: "

read input

# This line prints the value of the 'input' variable
echo "Your name is: $input"
