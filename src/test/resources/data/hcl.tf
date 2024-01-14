variable "foo" "bar" {
  default = "yes"
}

/* We like
   multiple lines
*/

locals {
  // this
  this = "that"

  # list
  more = ["foo", "bar"]

  /* map */
  map = {
    yep  = "nope" # bad comment
    nope = "yep"
  }
}
